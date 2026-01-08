package t.uni.server.common.auth;

import t.uni.server.domain.vo.auth.TokenVO;

/**
 * Token 服务接口
 * <p>
 * 负责双 Token（Access Token + Refresh Token）的生成、验证和刷新
 * </p>
 */
public interface TokenService {

    /**
     * 生成双 Token
     *
     * @param userId 用户ID
     * @param openId 微信 openId
     * @return Token 响应
     */
    TokenVO generateTokens(Long userId, String openId);

    /**
     * 刷新 Token
     *
     * @param refreshToken 刷新令牌
     * @return 新的 Token 响应
     */
    TokenVO refreshTokens(String refreshToken);

    /**
     * 验证 Access Token 并获取用户ID
     *
     * @param accessToken 访问令牌
     * @return 用户ID
     */
    Long validateAndGetUserId(String accessToken);

    /**
     * 使 Refresh Token 失效（用于登出）
     *
     * @param userId 用户ID
     */
    void invalidateRefreshToken(Long userId);
}
