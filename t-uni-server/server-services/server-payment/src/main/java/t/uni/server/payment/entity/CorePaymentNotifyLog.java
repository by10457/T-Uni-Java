package t.uni.server.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 支付回调日志。
 *
 * <p>保存微信支付和退款回调的原始请求，用于幂等判断、问题排查和对账追踪。</p>
 */
@Data
@TableName("core_payment_notify_log")
public class CorePaymentNotifyLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 回调类型，支付或退款。 */
    private Integer notifyType;

    /** 微信通知ID，用于识别重复回调。 */
    private String notifyId;

    /** 商户支付单号。 */
    private String outTradeNo;

    /** 微信支付交易号。 */
    private String transactionId;

    /** 商户退款单号。 */
    private String outRefundNo;

    /** 微信退款单号。 */
    private String refundId;

    /** 原始请求头 JSON，包含验签相关字段。 */
    private String headersJson;

    /** 原始回调报文。 */
    private String rawBody;

    /** 处理状态，见 PaymentConstants.NotifyProcessStatus。 */
    private Integer processStatus;

    /** 处理失败或忽略原因。 */
    private String errorMsg;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 修改时间。 */
    private LocalDateTime updateTime;
}
