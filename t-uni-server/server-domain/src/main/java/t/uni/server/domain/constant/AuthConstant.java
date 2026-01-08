package t.uni.server.domain.constant;

/**
 * 认证相关常量
 */
public final class AuthConstant {

    private AuthConstant() {
        // 工具类禁止实例化
    }

    // ==================== Token 相关 ====================

    /**
     * Access Token 有效期（小时）
     */
    public static final int ACCESS_TOKEN_EXPIRE_HOURS = 2;

    /**
     * Refresh Token 有效期（天）
     */
    public static final int REFRESH_TOKEN_EXPIRE_DAYS = 7;

    /**
     * Access Token 有效期（毫秒）
     */
    public static final long ACCESS_TOKEN_EXPIRE_MS = ACCESS_TOKEN_EXPIRE_HOURS * 60 * 60 * 1000L;

    /**
     * Refresh Token 有效期（毫秒）
     */
    public static final long REFRESH_TOKEN_EXPIRE_MS = REFRESH_TOKEN_EXPIRE_DAYS * 24 * 60 * 60 * 1000L;

    // ==================== Redis Key 前缀 ====================

    /**
     * 微信认证 Refresh Token 前缀
     * 完整格式: wx:auth:refresh:{userId}
     */
    public static final String REDIS_KEY_REFRESH_TOKEN = "wx:auth:refresh:";

    // ==================== JWT Claims ====================

    /**
     * JWT 中的用户ID字段
     */
    public static final String CLAIM_USER_ID = "userId";

    /**
     * JWT 中的 OpenId 字段
     */
    public static final String CLAIM_OPEN_ID = "openId";

    // ==================== Header 相关 ====================

    /**
     * Authorization 请求头
     */
    public static final String HEADER_AUTHORIZATION = "Authorization";

    /**
     * Bearer 前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    // ==================== 新用户判定 ====================

    /**
     * 新用户判定时间阈值（分钟）
     * 如果用户创建时间距离当前时间小于等于此值，判定为新用户
     */
    public static final int NEW_USER_THRESHOLD_MINUTES = 2;
}
