package t.uni.server.payment.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 支付成功业务事件。
 *
 * <p>可能来自微信回调，也可能来自关单前查单兜底。业务侧必须按支付单号或业务订单号幂等履约。</p>
 */
@Data
@Builder
public class PaymentPaidEvent {

    /** 下单用户ID。 */
    private Long userId;

    /** 业务类型。 */
    private String bizType;

    /** 业务订单号。 */
    private String bizOrderNo;

    /** 商户支付单号。 */
    private String orderNo;

    /** 微信支付交易号。 */
    private String transactionId;

    /** 支付金额，单位分。 */
    private Integer totalFeeFen;

    /** 微信支付成功时间。 */
    private LocalDateTime paidTime;

    /** 锁单时保存的业务透传数据。 */
    private String attachData;
}
