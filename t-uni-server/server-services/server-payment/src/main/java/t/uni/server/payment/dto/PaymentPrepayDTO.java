package t.uni.server.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 重新预下单请求。
 */
@Data
@Schema(description = "重新预下单请求")
public class PaymentPrepayDTO {

    /** 商户支付单号，对应微信 out_trade_no。 */
    @NotBlank(message = "支付单号不能为空")
    @Schema(description = "支付单号 out_trade_no", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orderNo;
}
