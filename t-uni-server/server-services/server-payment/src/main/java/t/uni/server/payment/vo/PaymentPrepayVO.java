package t.uni.server.payment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 支付预下单结果。
 */
@Data
@Builder
@Schema(description = "支付预下单结果")
public class PaymentPrepayVO {

    /** 支付单ID。 */
    @Schema(description = "支付单ID")
    private Long orderId;

    /** 商户支付单号，对应微信 out_trade_no。 */
    @Schema(description = "支付单号 out_trade_no")
    private String orderNo;

    /** 业务类型，用于前端或业务侧识别订单来源。 */
    @Schema(description = "业务类型")
    private String bizType;

    /** 业务订单号，由业务侧 PaymentBizHandler 返回。 */
    @Schema(description = "业务订单号")
    private String bizOrderNo;

    /** 支付金额，单位分。 */
    @Schema(description = "支付金额，单位分")
    private Integer totalFeeFen;

    /** 支付单状态，见 PaymentConstants.OrderStatus。 */
    @Schema(description = "支付单状态")
    private Integer status;

    /** 支付单失效时间，前端倒计时以该值为准。 */
    @Schema(description = "失效时间")
    private LocalDateTime expireTime;

    /** 小程序调用 wx.requestPayment 所需参数；已支付或不可支付订单可为空。 */
    @Schema(description = "微信JSAPI支付参数")
    private WechatJsapiPayParamsVO payParams;
}
