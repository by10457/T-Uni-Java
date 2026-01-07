package t.uni.server.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付状态枚举
 */
@Getter
@AllArgsConstructor
public enum PaymentStatusEnum {

    /**
     * 待支付
     */
    UNPAID(0, "待支付"),

    /**
     * 已支付
     */
    PAID(1, "已支付"),

    /**
     * 已退款
     */
    REFUNDED(2, "已退款"),

    /**
     * 已关闭
     */
    CLOSED(3, "已关闭");

    private final Integer code;
    private final String desc;

    /**
     * 根据code获取枚举
     */
    public static PaymentStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PaymentStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
