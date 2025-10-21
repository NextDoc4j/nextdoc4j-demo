package top.nextdoc4j.demo.configuration;


import cn.hutool.core.map.MapUtil;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.nextdoc4j.demo.configuration.properties.ProjectProperties;

import java.util.List;
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
            // 鉴权配置
            Map<String, SecurityScheme> securitySchemeMap = components.getSecuritySchemes();
            if (MapUtil.isNotEmpty(securitySchemeMap)) {
                SecurityRequirement securityRequirement = new SecurityRequirement();
                List<String> list = securitySchemeMap.values().stream().map(SecurityScheme::getName).toList();
                list.forEach(securityRequirement::addList);
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


    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("account")
                .displayName("用户与角色管理 API")
                .packagesToScan("top.nextdoc4j.demo.controller.user", "top.nextdoc4j.demo.controller.role")
                .addOpenApiCustomizer(getCustomizer("用户与角色管理相关接口"))
                .build();
    }

    @Bean
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder()
                .group("system")
                .displayName("系统管理 API")
                .packagesToScan("top.nextdoc4j.demo.controller.system", "top.nextdoc4j.demo.controller.notification")
                .addOpenApiCustomizer(getCustomizer("系统管理相关接口"))
                .build();
    }

    @Bean
    public GroupedOpenApi fileApi() {
        return GroupedOpenApi.builder()
                .group("file")
                .displayName("文件管理 API")
                .packagesToScan("top.nextdoc4j.demo.controller.file")
                .addOpenApiCustomizer(getCustomizer("文件管理相关接口"))
                .build();
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("auth")
                .displayName(" 认证管理 API")
                .packagesToScan("top.nextdoc4j.demo.controller.auth")
                .addOpenApiCustomizer(getCustomizer("认证管理相关接口"))
                .build();
    }

    /**
     * 获取自定义配置器
     */
    private OpenApiCustomizer getCustomizer(String description) {
        return openApi -> {
            if (openApi.getInfo() != null) {
                openApi.getInfo().setDescription(description);
            }
        };
    }


}
