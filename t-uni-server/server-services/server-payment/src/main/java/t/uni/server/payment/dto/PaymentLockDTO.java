package t.uni.server.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * 通用锁单请求。
 */
@Data
@Schema(description = "通用锁单请求")
public class PaymentLockDTO {

    /** 业务类型，用于路由到对应的 PaymentBizHandler。 */
    @NotBlank(message = "业务类型不能为空")
    @Schema(description = "业务类型，如 AI_PAPER", requiredMode = Schema.RequiredMode.REQUIRED)
    private String bizType;

    /** 业务锁单参数，支付模块不解析，由业务侧 handler 自行解释。 */
    @Schema(description = "业务锁单参数，由具体 PaymentBizHandler 解释")
    private Map<String, Object> bizContent;
}
