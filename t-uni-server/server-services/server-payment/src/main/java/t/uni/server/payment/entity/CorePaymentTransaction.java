package t.uni.server.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 支付交易流水。
 *
 * <p>保存微信预下单、支付回调和交易号。该表用于对账和回溯，不承担业务订单幂等。</p>
 */
@Data
@TableName("core_payment_transaction")
public class CorePaymentTransaction implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 本地支付单ID。 */
    private Long orderId;

    /** 商户支付单号，对应微信 out_trade_no。 */
    private String outTradeNo;

    /** 业务订单号。 */
    private String bizOrderNo;

    /** 下单用户ID。 */
    private Long userId;

    /** 支付渠道。 */
    private Integer payChannel;

    /** 微信交易类型，如 JSAPI。 */
    private String tradeType;

    /** 交易状态，见 PaymentConstants.TransactionStatus。 */
    private Integer status;

    /** 交易金额，单位分。 */
    private Integer totalFeeFen;

    /** 微信支付交易号。 */
    private String transactionId;

    /** 微信预支付ID。 */
    private String prepayId;

    /** 收到微信支付回调时间。 */
    private LocalDateTime notifyTime;

    /** 微信支付成功时间。 */
    private LocalDateTime successTime;

    /** 失败原因。 */
    private String failReason;

    /** 微信预下单或回调原始响应。 */
    private String rawResponse;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 修改时间。 */
    private LocalDateTime updateTime;
}
