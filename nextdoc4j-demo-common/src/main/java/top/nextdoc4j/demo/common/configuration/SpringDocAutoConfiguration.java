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
import lombok.RequiredArgsConstructor;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.customizers.OpenApiBuilderCustomizer;
import org.springdoc.core.customizers.ServerBaseUrlCustomizer;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.providers.JavadocProvider;
import org.springdoc.core.service.OpenAPIService;
import org.springdoc.core.service.SecurityService;
import org.springdoc.core.utils.PropertyResolverUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.nextdoc4j.demo.common.configuration.customizer.GlobalAuthenticationCustomizer;
import top.nextdoc4j.demo.common.configuration.customizer.GlobalDescriptionCustomizer;
import top.nextdoc4j.demo.common.configuration.exception.GlobalExceptionHandler;
import top.nextdoc4j.demo.common.configuration.handler.OpenApiHandler;
import top.nextdoc4j.demo.common.configuration.properties.ProjectProperties;
import top.nextdoc4j.demo.common.configuration.properties.SpringDocExtensionProperties;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * API 文档自动配置
 *
 * @author Charles7c
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@AutoConfiguration(before = SpringDocConfiguration.class)
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
    @ConditionalOnMissingBean(OpenAPI.class)
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
     * 自定义 openapi 处理器
     */
    @Bean
    public OpenAPIService openApiBuilder(Optional<OpenAPI> openAPI,
                                         SecurityService securityParser,
                                         SpringDocConfigProperties springDocConfigProperties, PropertyResolverUtils propertyResolverUtils,
                                         Optional<List<OpenApiBuilderCustomizer>> openApiBuilderCustomisers,
                                         Optional<List<ServerBaseUrlCustomizer>> serverBaseUrlCustomisers, Optional<JavadocProvider> javadocProvider) {
        return new OpenApiHandler(openAPI, securityParser, springDocConfigProperties, propertyResolverUtils, openApiBuilderCustomisers, serverBaseUrlCustomisers, javadocProvider);
    }

    @Bean
    public GlobalDescriptionCustomizer globalDescriptionCustomizer() {
        return new GlobalDescriptionCustomizer();
    }

    @Bean
    public GlobalAuthenticationCustomizer globalAuthenticationCustomizer(SpringDocExtensionProperties properties,
                                                                         ApplicationContext context) {
        return new GlobalAuthenticationCustomizer(properties, context);
    }

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }


}
