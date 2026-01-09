package t.uni.common.core.constant;

/**
 * Redis Key 常量类
 * <p>
 * 统一管理所有 Redis Key 前缀，使用 {} 作为占位符
 * </p>
 */
public final class RedisKeyConstants {

    // ==================== Server 端 - 微信登录相关 ====================

    /**
     * 用户信息缓存（通过 openid 查询）
     * 格式: wx:user:{openid}
     * 存储: Redis Hash，包含 userId, uniqueId, unionId 等
     * 过期: 永不过期
     */
    public static final String WX_USER_INFO = "wx:user:{}";

    /**
     * 用户 Token 缓存（通过 userId 查询）
     * 格式: wx:token:{userId}
     * 存储: Redis Hash，包含 accessToken, expireTime 等
     * 过期: 永不过期（取出时检查 expireTime）
     */
    public static final String WX_USER_TOKEN = "wx:token:{}";

    // ==================== Admin 端 - 用户登录相关 ====================

    /**
     * 管理员用户登录信息
     * 格式: user:login_info:{username}
     */
    public static final String ADMIN_USER_LOGIN_INFO = "user:login_info:{}";

    /**
     * 管理员用户邮箱验证码
     * 格式: user:email_code:{email}
     */
    public static final String ADMIN_USER_EMAIL_CODE = "user:email_code:{}";

    /**
     * 管理员用户角色
     * 格式: user:roles:{username}
     */
    public static final String ADMIN_USER_ROLES = "user:roles:{}";

    /**
     * 管理员用户权限
     * 格式: user:permission:{username}
     */
    public static final String ADMIN_USER_PERMISSION = "user:permission:{}";

    // ==================== 通用 ====================

    /**
     * Web 配置缓存
     */
    public static final String WEB_CONFIG = "webConfig:platformConfig";

    // ==================== 过期时间常量 ====================

    /**
     * 用户 Token 默认有效期（天）
     */
    public static final int TOKEN_EXPIRE_DAYS = 7;

    /**
     * 用户 Token 默认有效期（毫秒）
     */
    public static final long TOKEN_EXPIRE_MS = TOKEN_EXPIRE_DAYS * 24 * 60 * 60 * 1000L;

    /**
     * Token 续期阈值（天），在此时间内过期会自动续期
     */
    public static final int TOKEN_REFRESH_THRESHOLD_DAYS = 1;

    /**
     * Token 续期阈值（毫秒）
     */
    public static final long TOKEN_REFRESH_THRESHOLD_MS = TOKEN_REFRESH_THRESHOLD_DAYS * 24 * 60 * 60 * 1000L;

    private RedisKeyConstants() {
        // 禁止实例化
    }

    // ==================== 工具方法 ====================

    /**
     * 获取微信用户信息缓存 Key
     */
    public static String wxUserInfo(String openid) {
        return WX_USER_INFO.replace("{}", openid);
    }

    /**
     * 获取微信用户 Token 缓存 Key
     */
    public static String wxUserToken(Long userId) {
        return WX_USER_TOKEN.replace("{}", String.valueOf(userId));
    }

    /**
     * 获取管理员用户登录信息 Key
     */
    public static String adminUserLoginInfo(String username) {
        return ADMIN_USER_LOGIN_INFO.replace("{}", username);
    }

    /**
     * 获取管理员用户邮箱验证码 Key
     */
    public static String adminUserEmailCode(String email) {
        return ADMIN_USER_EMAIL_CODE.replace("{}", email);
    }

    /**
     * 获取管理员用户角色 Key
     */
    public static String adminUserRoles(String username) {
        return ADMIN_USER_ROLES.replace("{}", username);
    }

    /**
     * 获取管理员用户权限 Key
     */
    public static String adminUserPermission(String username) {
        return ADMIN_USER_PERMISSION.replace("{}", username);
    }
}
