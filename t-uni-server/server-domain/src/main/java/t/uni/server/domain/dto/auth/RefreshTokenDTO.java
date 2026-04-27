package t.uni.server.domain.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Token 刷新请求 DTO。
 */
@Data
@Schema(description = "Token刷新请求")
public class RefreshTokenDTO {

    /**
     * Refresh Token，由登录或上次刷新接口签发
     */
    @NotBlank(message = "refreshToken不能为空")
    @Schema(description = "刷新令牌", requiredMode = Schema.RequiredMode.REQUIRED)
    private String refreshToken;
}
