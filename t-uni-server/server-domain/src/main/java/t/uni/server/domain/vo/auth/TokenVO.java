package t.uni.server.domain.vo.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token 响应 VO（登录和刷新共用）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Token响应")
public class TokenVO {

    /**
     * 访问令牌（JWT）
     */
    @Schema(description = "访问令牌")
    private String accessToken;

    /**
     * 刷新令牌（UUID）
     */
    @Schema(description = "刷新令牌")
    private String refreshToken;

    /**
     * 访问令牌有效期（秒）
     */
    @Schema(description = "访问令牌有效期（秒）")
    private Long accessExpiresIn;

    /**
     * 刷新令牌有效期（秒）
     */
    @Schema(description = "刷新令牌有效期（秒）")
    private Long refreshExpiresIn;
}
