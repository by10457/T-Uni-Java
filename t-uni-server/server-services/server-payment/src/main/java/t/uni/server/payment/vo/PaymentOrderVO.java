package t.uni.server.payment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 支付单信息。
 *
 * <p>返回给小程序端展示支付、关闭和退款进度。金额单位为分。</p>
 */
@Data
@Builder
@Schema(description = "支付单信息")
public class PaymentOrderVO {

    /** 支付单ID。 */
    private Long orderId;

    /** 支付单号 out_trade_no。 */
    private String orderNo;

    /** 业务类型。 */
    private String bizType;

    /** 业务订单号。 */
    private String bizOrderNo;

    /** 支付金额，单位分。 */
    private Integer totalFeeFen;

    /** 累计已退金额，单位分。 */
    private Integer refundFeeFen;

    /** 支付单状态。 */
    private Integer status;

    /** 支付过期时间。 */
    private LocalDateTime expireTime;

    /** 支付成功时间。 */
    private LocalDateTime paidTime;

    /** 关单时间。 */
    private LocalDateTime closeTime;
}
