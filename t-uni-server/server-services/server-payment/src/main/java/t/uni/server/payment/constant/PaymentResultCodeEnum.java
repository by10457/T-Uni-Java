package t.uni.server.payment.constant;

import lombok.Getter;

/**
 * 支付模块状态码。
 */
@Getter
public enum PaymentResultCodeEnum {

    PAYMENT_CONFIG_MISSING(4301, "微信支付配置不完整"),
    PAYMENT_BIZ_HANDLER_MISSING(4302, "未配置业务支付处理器"),
    PAYMENT_ORDER_NOT_FOUND(4303, "支付单不存在"),
    PAYMENT_ORDER_STATUS_ERROR(4304, "支付单状态不允许当前操作"),
    PAYMENT_AMOUNT_ERROR(4305, "支付金额不合法"),
    PAYMENT_OPENID_MISSING(4306, "当前用户缺少微信小程序 openId"),
    PAYMENT_NOTIFY_ERROR(4307, "支付回调处理失败"),
    PAYMENT_BIZ_HANDLER_DUPLICATE(4308, "业务支付处理器重复"),
    PAYMENT_NOTIFY_SIGNATURE_ERROR(4309, "支付回调签名校验失败"),
    REFUND_AMOUNT_ERROR(4311, "退款金额不合法"),
    REFUND_ORDER_NOT_FOUND(4312, "退款单不存在"),
    REFUND_NOTIFY_ERROR(4313, "退款回调处理失败"),
    REFUND_STATUS_ERROR(4314, "退款单状态不允许当前操作");

    private final Integer code;
    private final String message;

    PaymentResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
