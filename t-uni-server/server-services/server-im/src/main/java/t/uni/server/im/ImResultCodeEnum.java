package t.uni.server.im;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * IM 模块专属错误码（5200-5299）
 * <p>
 * 不放入 common-core 的 ResultCodeEnum，遵守"端专属状态码放各自模块"的硬规则。
 * 主要覆盖 OpenIM 外部接口、用户同步、Webhook 鉴权和通知发送失败。
 *
 * @author t-uni
 * @since 2026-04-24
 */
@Getter
@AllArgsConstructor
public enum ImResultCodeEnum {

    IM_OPENIM_REQUEST_FAIL(5200, "OpenIM 请求失败"),
    IM_OPENIM_ADMIN_TOKEN_FAIL(5201, "获取 OpenIM admin token 失败"),
    IM_OPENIM_USER_TOKEN_FAIL(5202, "获取 OpenIM user token 失败"),
    IM_OPENIM_USER_REGISTER_FAIL(5203, "同步 OpenIM 用户失败"),
    IM_OPENIM_SEND_MSG_FAIL(5204, "发送 OpenIM 消息失败"),
    IM_OPENIM_USER_NOT_FOUND(5205, "OpenIM 用户不存在"),
    IM_WEBHOOK_AUTH_FAIL(5206, "OpenIM Webhook 鉴权失败"),
    IM_CONFIG_MISSING(5208, "OpenIM 配置缺失"),
    ;

    /** 对外返回的业务错误码 */
    private final Integer code;

    /** 对外返回的默认错误文案 */
    private final String message;
}
