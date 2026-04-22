package t.uni.domain.common.enums;

import lombok.Getter;

/**
 * 管理端专属状态码。
 * <p>
 * 这些状态码只服务于 admin 侧邮件、角色等后台能力，
 * 不应继续放在模板公共的 common-core 中。
 * </p>
 */
@Getter
public enum AdminResultCodeEnum {
    EMAIL_CODE_SEND_SUCCESS(2008, "邮箱验证码已发送"),
    EMAIL_CODE_NOT_MATCHING(3004, "邮箱验证码不匹配"),
    EMAIL_CODE_EMPTY(3009, "邮箱验证码过期或不存在"),
    EMAIL_TEMPLATE_IS_EMPTY(3111, "邮件模板为空"),
    EMAIL_USER_IS_EMPTY(3112, "关联邮件用户配置为空"),
    ADMIN_ROLE_CAN_NOT_DELETED(3406, "无法删除admin角色"),
    SEND_MAIL_CODE_ERROR(3409, "邮件发送失败"),
    ;

    private final Integer code;
    private final String message;

    AdminResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
