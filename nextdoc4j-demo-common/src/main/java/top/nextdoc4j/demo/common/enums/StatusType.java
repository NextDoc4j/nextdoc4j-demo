package top.nextdoc4j.demo.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.nextdoc4j.enums.core.BaseEnum;

/**
 * 状态类型
 *
 * @author echo
 * @since 2025/11/05
 */
@Getter
@RequiredArgsConstructor
public enum StatusType implements BaseEnum<String> {

    ACTIVE("ACTIVE", "启用"),
    INACTIVE("INACTIVE", "停用"),
    PENDING("PENDING", "待处理");

    @JsonValue
    private final String value;
    private final String description;
}