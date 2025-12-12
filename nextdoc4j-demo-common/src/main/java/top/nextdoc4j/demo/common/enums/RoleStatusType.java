package top.nextdoc4j.demo.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * 角色状态类型
 *
 * @author echo
 * @since 2025/11/05
 */
@Getter
@RequiredArgsConstructor
public enum RoleStatusType  {


    ACTIVE(1, "启用"),

    INACTIVE(2, "禁用");

    private final Integer value;
    private final String description;
}