package t.uni.server.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 支付主订单。
 *
 * <p>一条记录对应一个商户支付单号 {@code orderNo}。业务订单通过 {@code bizType + bizOrderNo} 关联，
 * 金额字段单位均为分。</p>
 */
@Data
@TableName("core_payment_order")
public class CorePaymentOrder implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 业务类型，用于路由到对应 PaymentBizHandler。 */
    private String bizType;

    /** 业务订单号，和 bizType 一起构成业务幂等键。 */
    private String bizOrderNo;

    /** 商户支付单号，对应微信 out_trade_no。 */
    private String orderNo;

    /** 下单用户ID。 */
    private Long userId;

    /** 微信账单展示描述。 */
    private String description;

    /** 支付总金额，单位分。 */
    private Integer totalFeeFen;

    /** 累计已成功退款金额，单位分。 */
    private Integer refundFeeFen;

    /** 币种，默认 CNY。 */
    private String currency;

    /** 支付渠道，见 PaymentConstants.PAY_CHANNEL_*。 */
    private Integer payChannel;

    /** 支付单状态，见 PaymentConstants.OrderStatus。 */
    private Integer status;

    /** 支付过期时间。 */
    private LocalDateTime expireTime;

    /** 微信确认支付成功时间。 */
    private LocalDateTime paidTime;

    /** 本地关单时间。 */
    private LocalDateTime closeTime;

    /** 透传业务扩展数据，随支付成功事件回传业务侧。 */
    private String attachData;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 修改时间。 */
    private LocalDateTime updateTime;
}
