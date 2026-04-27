package t.uni.server.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 退款单。
 *
 * <p>记录一次商户退款请求。金额单位均为分，{@code outRefundNo} 对应微信 out_refund_no。</p>
 */
@Data
@TableName("core_payment_refund")
public class CorePaymentRefund implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 本地支付单ID。 */
    private Long orderId;

    /** 商户支付单号，对应微信 out_trade_no。 */
    private String outTradeNo;

    /** 商户退款单号，对应微信 out_refund_no，也是退款幂等键。 */
    private String outRefundNo;

    /** 申请退款用户ID。 */
    private Long userId;

    /** 本次退款金额，单位分。 */
    private Integer refundFeeFen;

    /** 退款原因，提交给微信。 */
    private String refundReason;

    /** 退款状态，见 PaymentConstants.RefundStatus。 */
    private Integer status;

    /** 微信退款单号。 */
    private String refundId;

    /** 收到微信退款回调时间。 */
    private LocalDateTime notifyTime;

    /** 微信退款成功时间。 */
    private LocalDateTime successTime;

    /** 失败或关闭原因。 */
    private String failReason;

    /** 微信退款申请或回调原始响应。 */
    private String rawResponse;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 修改时间。 */
    private LocalDateTime updateTime;
}
