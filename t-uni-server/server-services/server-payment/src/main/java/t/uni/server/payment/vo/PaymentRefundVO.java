package t.uni.server.payment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 退款单信息。
 *
 * <p>返回给小程序端展示退款进度。金额单位为分。</p>
 */
@Data
@Builder
@Schema(description = "退款单信息")
public class PaymentRefundVO {

    /** 退款单ID。 */
    private Long refundId;

    /** 支付单号 out_trade_no。 */
    private String orderNo;

    /** 商户退款单号 out_refund_no。 */
    private String outRefundNo;

    /** 本次退款金额，单位分。 */
    private Integer refundFeeFen;

    /** 退款状态。 */
    private Integer status;

    /** 微信退款单号。 */
    private String wechatRefundId;

    /** 退款成功时间。 */
    private LocalDateTime successTime;

    /** 失败原因。 */
    private String failReason;
}
