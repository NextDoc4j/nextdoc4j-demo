package top.nextdoc4j.demo.springboot.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.customizers.GlobalOperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import top.nextdoc4j.demo.core.annotation.ApiError;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Configuration(proxyBeanMethods = false)
public class GlobalOpenApiAutoConfiguration {

    public static final String API_ERROR_RESPONSE_NAME = "ApiErrorResponse";
    private static final String DEFAULT_MEDIA_TYPE = "application/json";

    /**
     * 扩展属性 key，用于在 operation 中存储需要提升的 examples
     */
    private static final String X_PENDING_EXAMPLES = "x-pending-examples";

    /**
     * 定义全局错误响应 Schema 模型
     */
    public static final Schema<?> API_ERROR_RESPONSE;

    static {
        Schema<Object> schema = new Schema<>();
        schema.setName(API_ERROR_RESPONSE_NAME);
        schema.setDescription("通用错误响应模型，用于 4xx 和 5xx 错误");
        schema.setType("object");
        schema.addProperty("code", new StringSchema()
                .title("业务错误码")
                .description("场景 code 码/业务 code 码，可以追溯具体原因"));
        schema.addProperty("message", new StringSchema()
                .title("错误消息")
                .description("人类可读的错误描述"));
        API_ERROR_RESPONSE = schema;
    }

    /**
     * 阶段 2：全局定制器
     * 在所有接口处理完成后，统一注册 Schema 和公共 Examples
     */
    @Bean
    public GlobalOpenApiCustomizer globalOpenApiCustomizer() {
        return openApi -> {
            Components components = openApi.getComponents();
            if (components == null) {
                components = new Components();
                openApi.setComponents(components);
            }

            // 1. 注册通用错误 Schema
            components.addSchemas(API_ERROR_RESPONSE_NAME, API_ERROR_RESPONSE);

            // 2. 从所有 operation 的扩展属性中提取 pending examples
            Map<String, Example> examplesToRegister = new LinkedHashMap<>();

            if (openApi.getPaths() != null) {
                openApi.getPaths().forEach((path, pathItem) -> {
                    if (pathItem == null) return;

                    pathItem.readOperations().forEach(operation -> {
                        if (operation == null) return;

                        Map<String, Object> extensions = operation.getExtensions();
                        if (extensions != null && extensions.containsKey(X_PENDING_EXAMPLES)) {
                            @SuppressWarnings("unchecked")
                            Map<String, Example> pending = (Map<String, Example>) extensions.get(X_PENDING_EXAMPLES);
                            if (pending != null) {
                                examplesToRegister.putAll(pending);
                            }
                            // 清理扩展属性，不输出到最终文档
                            extensions.remove(X_PENDING_EXAMPLES);
                        }
                    });
                });
            }

            // 3. 注册提取到的 examples 到 components/examples
            if (!examplesToRegister.isEmpty()) {
                if (components.getExamples() == null) {
                    components.setExamples(new LinkedHashMap<>());
                }
                components.getExamples().putAll(examplesToRegister);
            }
        };
    }

    /**
     * 阶段 1：操作定制器
     * 扫描方法上的 @ApiError 注解，构建 ApiResponse 并挂载到 operation 上
     */
    @Bean
    public GlobalOperationCustomizer apiErrorOperationCustomizer() {
        return (operation, handlerMethod) -> {
            Method method = handlerMethod.getMethod();
            Set<ApiError> apiErrors = AnnotatedElementUtils
                    .getMergedRepeatableAnnotations(method, ApiError.class);

            if (apiErrors.isEmpty()) {
                return operation;
            }

            ApiResponses responses = operation.getResponses();
            if (responses == null) {
                responses = new ApiResponses();
                operation.setResponses(responses);
            }
            final ApiResponses finalResponses = responses;

            // 用于收集需要提升到 components/examples 的 examples
            Map<String, Example> pendingExamples = new LinkedHashMap<>();

            // 按状态码分组
            Map<Integer, List<ApiError>> groups = apiErrors.stream()
                    .sorted(Comparator.comparingInt(ApiError::status)
                            .thenComparing(ApiError::code))
                    .collect(Collectors.groupingBy(
                            ApiError::status,
                            LinkedHashMap::new,
                            Collectors.toList()));

            groups.forEach((status, errors) -> {
                String statusKey = status == -1 ? "default" : String.valueOf(status);
                // 如果已存在手动定义的响应（如 @ApiResponse），则不覆盖
                if (finalResponses.containsKey(statusKey)) {
                    return;
                }

                ApiResponse response = (errors.size() == 1)
                        ? buildSingleResponse(errors.get(0), pendingExamples)
                        : buildMultiResponse(errors, pendingExamples);

                finalResponses.addApiResponse(statusKey, response);
            });

            // 将 pending examples 存储到 operation 的扩展属性中
            if (!pendingExamples.isEmpty()) {
                operation.addExtension(X_PENDING_EXAMPLES, pendingExamples);
            }

            return operation;
        };
    }

    // -------------------------------------------------------------------------
    // 构建方法
    // -------------------------------------------------------------------------

    /**
     * 构建单个错误的响应
     */
    private ApiResponse buildSingleResponse(ApiError err, Map<String, Example> pendingExamples) {
        ApiResponse response = new ApiResponse().description(resolveDescription(err));

        if (err.bareContent()) {
            return response; // 场景 4：裸响应
        }

        MediaType mediaType = new MediaType().schema(new Schema<>().$ref(schemaRef()));
        String key = buildExampleKey(err);
        Example example = buildExample(err);

        if (err.refExample()) {
            // 存储到 pendingExamples，稍后由 GlobalOpenApiCustomizer 提取
            pendingExamples.put(key, example);
            // 在 response 中使用 $ref 引用
            mediaType.addExamples(key, new Example().$ref(componentsExampleRef(key)));
        } else {
            mediaType.addExamples(key, example);
        }

        return response.content(new Content().addMediaType(err.mediaType(), mediaType));
    }

    /**
     * 构建同一状态码下多个错误的响应（生成 examples 下拉列表）
     */
    private ApiResponse buildMultiResponse(List<ApiError> errors, Map<String, Example> pendingExamples) {
        String description = errors.stream()
                .map(this::resolveDescription)
                .collect(Collectors.joining(" / "));

        Map<String, Example> examplesMap = new LinkedHashMap<>();

        for (ApiError err : errors) {
            String key = buildExampleKey(err);
            Example example = buildExample(err);

            if (err.refExample()) {
                pendingExamples.put(key, example);
                examplesMap.put(key, new Example().$ref(componentsExampleRef(key)));
            } else {
                examplesMap.put(key, example);
            }
        }

        MediaType mediaType = new MediaType()
                .schema(new Schema<>().$ref(schemaRef()))
                .examples(examplesMap);

        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType(errors.get(0).mediaType(), mediaType));
    }

    // -------------------------------------------------------------------------
    // 工具方法
    // -------------------------------------------------------------------------

    private String resolveDescription(ApiError err) {
        return err.reason().isBlank()
                ? "HTTP " + (err.status() == -1 ? "default" : err.status()) + " 错误响应"
                : err.reason();
    }

    private Map<String, Object> buildExampleValue(ApiError err) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("code", err.code());
        // 场景 7：支持 reason 与 message 分离
        map.put("message", err.message().isBlank() ? err.reason() : err.message());
        return map;
    }

    private Example buildExample(ApiError err) {
        return new Example()
                .summary(err.reason().isBlank() ? err.code() : err.reason())
                .value(buildExampleValue(err));
    }

    private String buildExampleKey(ApiError err) {
        String prefix = err.status() == -1 ? "default" : String.valueOf(err.status());
        return prefix + "_" + err.code();
    }

    private String schemaRef() {
        return "#/components/schemas/" + API_ERROR_RESPONSE_NAME;
    }

    private String componentsExampleRef(String key) {
        return "#/components/examples/" + key;
    }
}