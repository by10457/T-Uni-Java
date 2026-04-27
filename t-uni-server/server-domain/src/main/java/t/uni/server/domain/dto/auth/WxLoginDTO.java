package t.uni.server.domain.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信小程序登录请求 DTO。
 * <p>
 * code 来自微信登录接口，只用于服务端换取 openId/unionId。
 * </p>
 */
@Data
@Schema(description = "微信登录请求")
public class WxLoginDTO {

    /**
     * 微信登录 code，一次性凭证
     */
    @NotBlank(message = "code不能为空")
    @Schema(description = "微信登录code", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;
}
