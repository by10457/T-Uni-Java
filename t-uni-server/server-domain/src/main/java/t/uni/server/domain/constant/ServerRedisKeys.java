package t.uni.server.domain.constant;

/**
 * Server 端 Redis 逻辑 Key 与缓存阈值常量。
 * <p>
 * 统一维护认证链路使用的 Redis 逻辑 Key 模板，物理 namespace 由 common Redis 配置统一处理。
 * </p>
 */
public final class ServerRedisKeys {

    /**
     * 微信用户信息缓存 Key 模板，参数为 openId。
     */
    private static final String WX_USER_INFO = "wx:user:{}";
    /**
     * 用户 Token 缓存 Key 模板，参数为用户 ID。
     */
    private static final String WX_USER_TOKEN = "wx:token:{}";
    /**
     * Refresh Token 反查缓存 Key 模板，参数为 refreshToken。
     */
    private static final String WX_REFRESH_TOKEN = "wx:refresh:{}";
    /**
     * Token 距离过期小于该天数时可触发刷新。
     */
    private static final int TOKEN_REFRESH_THRESHOLD_DAYS = 1;

    /**
     * Token 刷新阈值（毫秒）。
     */
    public static final long TOKEN_REFRESH_THRESHOLD_MS =
            TOKEN_REFRESH_THRESHOLD_DAYS * 24L * 60L * 60L * 1000L;

    private ServerRedisKeys() {
    }

    /**
     * 生成微信用户信息缓存 Key。
     *
     * @param openid 微信 openId
     * @return Redis Key
     */
    public static String wxUserInfo(String openid) {
        return WX_USER_INFO.replace("{}", openid);
    }

    /**
     * 生成用户 Token 缓存 Key。
     *
     * @param userId 用户 ID
     * @return Redis Key
     */
    public static String wxUserToken(Long userId) {
        return WX_USER_TOKEN.replace("{}", String.valueOf(userId));
    }

    /**
     * 生成 Refresh Token 反查缓存 Key。
     *
     * @param refreshToken 刷新令牌
     * @return Redis Key
     */
    public static String wxRefreshToken(String refreshToken) {
        return WX_REFRESH_TOKEN.replace("{}", refreshToken);
    }
}
