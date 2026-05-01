package t.uni.domain.common.enums;

import lombok.Getter;

/**
 * 管理端专属状态码。
 * <p>
 * 这些状态码只服务于 admin 侧角色等后台能力，
 * 不应继续放在模板公共的 common-core 中。
 * </p>
 */
@Getter
public enum AdminResultCodeEnum {
    ADMIN_ROLE_CAN_NOT_DELETED(3406, "无法删除admin角色"),
    ;

    private final Integer code;
    private final String message;

    AdminResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
