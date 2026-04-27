package t.uni.server.payment.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 退款业务事件。
 *
 * <p>用于通知业务侧退款成功或失败。部分退款时 {@code fullyRefunded} 为 false，业务侧不能按全额撤销处理。</p>
 */
@Data
@Builder
public class PaymentRefundEvent {

    /** 下单用户ID。 */
    private Long userId;

    /** 业务类型。 */
    private String bizType;

    /** 业务订单号。 */
    private String bizOrderNo;

    /** 商户支付单号。 */
    private String orderNo;

    /** 商户退款单号。 */
    private String outRefundNo;

    /** 微信退款单号。 */
    private String refundId;

    /** 本次退款金额，单位分。 */
    private Integer refundFeeFen;

    /** 累计成功退款金额，单位分。 */
    private Integer totalRefundFeeFen;

    /** 是否已全额退款。 */
    private boolean fullyRefunded;

    /** 退款成功或失败通知时间。 */
    private LocalDateTime refundTime;

    /** 退款失败原因。 */
    private String failReason;
}
