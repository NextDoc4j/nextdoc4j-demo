package top.nextdoc4j.demo.common.configuration;


import cn.hutool.core.map.MapUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.nextdoc4j.demo.common.configuration.properties.ProjectProperties;
import top.nextdoc4j.demo.common.configuration.properties.SpringDocExtensionProperties;

import java.util.Map;

/**
 * API 文档自动配置
 *
 * @author Charles7c
 * @since 1.0.0
 */
@Component
@EnableConfigurationProperties({SpringDocExtensionProperties.class, ProjectProperties.class})
public class SpringDocAutoConfiguration implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/favicon.ico").addResourceLocations("classpath:/");
    }

    /**
     * Open API 配置
     */
    @Bean
    @ConditionalOnMissingBean
    public OpenAPI openApi(ProjectProperties projectProperties, SpringDocExtensionProperties properties) {
        Info info = new Info().title("%s %s".formatted(projectProperties.getName(), "API 文档"))
                .version(projectProperties.getVersion())
                .description(projectProperties.getDescription());
        ProjectProperties.Contact contact = projectProperties.getContact();
        if (null != contact) {
            info.contact(new Contact().name(contact.getName()).email(contact.getEmail()).url(contact.getUrl()));
        }
        ProjectProperties.License license = projectProperties.getLicense();
        if (null != license) {
            info.license(new License().name(license.getName()).url(license.getUrl()));
        }
        OpenAPI openApi = new OpenAPI();
        openApi.info(info);
        Components components = properties.getComponents();
        if (null != components) {
            openApi.components(components);
            Map<String, SecurityScheme> securitySchemeMap = components.getSecuritySchemes();
            if (MapUtil.isNotEmpty(securitySchemeMap)) {
                SecurityRequirement securityRequirement = new SecurityRequirement();
                securitySchemeMap.keySet().forEach(securityRequirement::addList);
                openApi.addSecurityItem(securityRequirement);
            }
        }
        return openApi;
    }

    /**
     * 全局自定义配置（全局添加鉴权参数和响应示例）
     */
    @Bean
    @ConditionalOnMissingBean
    public GlobalOpenApiCustomizer globalOpenApiCustomizer(SpringDocExtensionProperties properties) {
        return openApi -> {
            if (null != openApi.getPaths()) {
                openApi.getPaths().forEach((path, pathItem) -> {
                    // 为所有接口添加鉴权
                    Components components = properties.getComponents();
                    if (null != components && MapUtil.isNotEmpty(components.getSecuritySchemes())) {
                        Map<String, SecurityScheme> securitySchemeMap = components.getSecuritySchemes();
                        pathItem.readOperations().forEach(operation -> {
                            SecurityRequirement securityRequirement = new SecurityRequirement();
                            securitySchemeMap.keySet().forEach(securityRequirement::addList);
                            operation.addSecurityItem(securityRequirement);
                        });
                    }
                });
            }
        };
    }

    /**
     * 自定义 BaseEnum 枚举参数配置（针对实现了 BaseEnum 的枚举，优化其枚举值和描述展示）
     *
     * @return {@link BaseEnumParameterHandler }
     * @since 2.4.0
     */
    @Bean
    public BaseEnumParameterHandler customParameterCustomizer(ObjectMapper mapper) {
        return new BaseEnumParameterHandler(mapper);
    }


}
