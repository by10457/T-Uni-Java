package t.uni.server.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 申请退款请求。
 */
@Data
@Schema(description = "申请退款请求")
public class PaymentRefundApplyDTO {

    /** 原支付单号，对应微信 out_trade_no。 */
    @NotBlank(message = "支付单号不能为空")
    @Schema(description = "支付单号 out_trade_no", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orderNo;

    /** 本次退款金额，单位分；为空时按剩余可退金额处理。 */
    @Min(value = 1, message = "退款金额必须大于0")
    @Schema(description = "退款金额，单位分；为空默认全额可退金额")
    private Integer refundFeeFen;

    /** 退款原因，会随退款申请提交给微信。 */
    @Schema(description = "退款原因")
    private String refundReason;
}
