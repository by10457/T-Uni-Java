package t.uni.server.domain.vo.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
     * 访问令牌过期时间
     */
    @Schema(description = "访问令牌过期时间")
    private LocalDateTime accessTokenExpireTime;

    /**
     * 刷新令牌过期时间
     */
    @Schema(description = "刷新令牌过期时间")
    private LocalDateTime refreshTokenExpireTime;
}
