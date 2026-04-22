package t.uni.server.domain.constant;

/**
 * Server 端 Redis Key 与缓存阈值常量。
 */
public final class ServerRedisKeys {

    private static final String WX_USER_INFO = "wx:user:{}";
    private static final String WX_USER_TOKEN = "wx:token:{}";
    private static final String WX_REFRESH_TOKEN = "wx:refresh:{}";
    private static final int TOKEN_REFRESH_THRESHOLD_DAYS = 1;

    public static final long TOKEN_REFRESH_THRESHOLD_MS =
            TOKEN_REFRESH_THRESHOLD_DAYS * 24L * 60L * 60L * 1000L;

    private ServerRedisKeys() {
    }

    public static String wxUserInfo(String openid) {
        return WX_USER_INFO.replace("{}", openid);
    }

    public static String wxUserToken(Long userId) {
        return WX_USER_TOKEN.replace("{}", String.valueOf(userId));
    }

    public static String wxRefreshToken(String refreshToken) {
        return WX_REFRESH_TOKEN.replace("{}", refreshToken);
    }
}
