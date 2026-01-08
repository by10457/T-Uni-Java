package t.uni.server.domain.constant;

/**
 * 认证相关常量
 */
public final class AuthConstant {

    /**
     * Access Token 有效期（小时）
     */
    public static final int ACCESS_TOKEN_EXPIRE_HOURS = 2;

    // ==================== Token 相关 ====================
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
    /**
     * 微信认证 Refresh Token 前缀
     * 完整格式: wx:auth:refresh:{userId}
     */
    public static final String REDIS_KEY_REFRESH_TOKEN = "wx:auth:refresh:";

    // ==================== Redis Key 前缀 ====================
    /**
     * Refresh Token 反向索引前缀
     * 完整格式: wx:auth:refresh:token:{refreshToken}
     */
    public static final String REDIS_KEY_REFRESH_TOKEN_INDEX = "wx:auth:refresh:token:";
    /**
     * Refresh Token Hash 字段: token
     */
    public static final String REFRESH_TOKEN_FIELD_TOKEN = "token";
    /**
     * Refresh Token Hash 字段: openId
     */
    public static final String REFRESH_TOKEN_FIELD_OPEN_ID = "openId";
    /**
     * JWT 中的用户ID字段
     */
    public static final String CLAIM_USER_ID = "userId";

    // ==================== JWT Claims ====================
    /**
     * JWT 中的 OpenId 字段
     */
    public static final String CLAIM_OPEN_ID = "openId";
    /**
     * Authorization 请求头
     */
    public static final String HEADER_AUTHORIZATION = "Authorization";

    // ==================== Header 相关 ====================
    /**
     * Bearer 前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";
    /**
     * 新用户判定时间阈值（分钟）
     * 如果用户创建时间距离当前时间小于等于此值，判定为新用户
     */
    public static final int NEW_USER_THRESHOLD_MINUTES = 2;

    // ==================== 新用户判定 ====================

    private AuthConstant() {
        // 工具类禁止实例化
    }
}
