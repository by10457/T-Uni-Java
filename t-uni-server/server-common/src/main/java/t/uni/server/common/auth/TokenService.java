package t.uni.server.common.auth;

import t.uni.server.domain.vo.auth.TokenVO;

/**
 * Token 服务接口
 * <p>
 * 负责双 Token（Access Token + Refresh Token）的生成、验证和刷新
 * 同时支持 Python 风格的两层缓存策略
 * </p>
 */
public interface TokenService {

    /**
     * 生成双 Token（登录时调用）
     * <p>
     * 同时缓存用户信息和 Token 信息到 Redis
     * </p>
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
     * 使 Token 失效（用于登出）
     *
     * @param userId 用户ID
     */
    void invalidateToken(Long userId);

    /**
     * 从缓存获取有效 Token（如果存在且未过期）
     * <p>
     * Python 风格：先查缓存，命中则直接返回
     * </p>
     *
     * @param openId 微信 openId
     * @return Token 响应，无效或过期返回 null
     */
    TokenVO getCachedToken(String openId);

    /**
     * 缓存用户信息（openId -> userId 映射）
     *
     * @param openId  微信 openId
     * @param userId  用户ID
     * @param unionId 微信 unionId（可选）
     */
    void cacheUserInfo(String openId, Long userId, String unionId);

    /**
     * 从缓存获取用户ID
     *
     * @param openId 微信 openId
     * @return 用户ID，不存在返回 null
     */
    Long getCachedUserId(String openId);
}
