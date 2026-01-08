package t.uni.server.domain.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信登录DTO
 */
@Data
@Schema(description = "微信登录请求")
public class WxLoginDTO {

    /**
     * 微信登录code
     */
    @NotBlank(message = "code不能为空")
    @Schema(description = "微信登录code", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;
}
