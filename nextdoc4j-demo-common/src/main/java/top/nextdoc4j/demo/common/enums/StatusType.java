package top.nextdoc4j.demo.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.nextdoc4j.demo.common.enums.base.BaseEnum;

/**
 * 状态类型
 *
 * @author echo
 * @since  2025/11/05
 */
@Getter
@RequiredArgsConstructor
@Schema(description = "状态枚举")
public enum StatusType implements BaseEnum<String> {

    ACTIVE("ACTIVE", "启用"),
    INACTIVE("INACTIVE", "停用"),
    PENDING("PENDING", "待处理");

    @JsonValue
    private final String value;
    private final String description;
}