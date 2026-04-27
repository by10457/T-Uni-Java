package t.uni.server.common.auth;

import t.uni.server.domain.vo.auth.TokenVO;

/**
 * 轻量 API 的 Token 服务接口。
 * <p>
 * 负责 Access Token 与 Refresh Token 的签发、校验、刷新和缓存映射。
 * 该接口只表达登录态能力，不包含后台权限模型。
 * </p>
 */
public interface TokenService {

    /**
     * 为指定用户生成一组登录 Token。
     * <p>
     * 登录成功后调用，通常同时维护 Redis 中的用户与 Token 映射。
     * </p>
     *
     * @param userId 业务登录用户 ID
     * @param openId 微信 openId，用于缓存反查用户
     * @return Token 响应
     */
    TokenVO generateTokens(Long userId, String openId);

    /**
     * 使用 Refresh Token 换取新的 Token。
     *
     * @param refreshToken 客户端提交的刷新令牌
     * @return 新的 Token 响应
     */
    TokenVO refreshTokens(String refreshToken);

    /**
     * 校验 Access Token 并解析用户 ID。
     *
     * @param accessToken 去掉 Bearer 前缀后的访问令牌
     * @return Token 中声明的用户 ID
     */
    Long validateAndGetUserId(String accessToken);

    /**
     * 使指定用户的缓存 Token 失效。
     *
     * @param userId 需要登出的用户 ID
     */
    void invalidateToken(Long userId);

    /**
     * 从缓存获取仍可复用的 Token。
     * <p>
     * 登录入口可先查缓存，命中且未过期时直接返回，避免重复签发。
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
