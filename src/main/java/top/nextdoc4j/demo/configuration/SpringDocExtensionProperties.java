package top.nextdoc4j.demo.configuration;

import io.swagger.v3.oas.models.Components;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * API 文档扩展配置属性
 *
 * @author Charles7c
 * @since 1.0.1
 */
@Data
@ConfigurationProperties("springdoc")
public class SpringDocExtensionProperties {

    /**
     * 组件配置（包括鉴权配置等）
     */
    @NestedConfigurationProperty
    private Components components;
}
