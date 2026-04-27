package t.uni.common.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.springframework.util.StringUtils;
import t.uni.common.core.exception.BaseException;
import t.uni.common.core.result.ResultCodeEnum;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JWT Token 工具类。
 * <p>
 * 提供 Token 创建、解析、校验和刷新能力，签名密钥由 JwtKeyHolder 从配置注入。
 * 只处理 JWT 的结构和签名校验，不负责登录态存储、刷新令牌持久化或权限判断。
 * claims 中不要放入密码、手机号、密钥等敏感原文。
 * </p>
 */
public class JwtTokenUtil {

    /**
     * 默认单日毫秒数，用于按天计算过期时间。
     */
    private static final long TOKEN_EXPIRATION_MS = 24 * 60 * 60 * 1000L;

    /**
     * 默认过期天数
     */
    private static final int DEFAULT_EXPIRE_DAYS = 7;

    /**
     * 默认主题，用于区分本系统签发的 Token。
     */
    private static final String DEFAULT_SUBJECT = "T-Uni";

    // ==================== 创建 Token ====================

    /**
     * 根据用户 ID 创建 Token。
     *
     * @param userId 用户ID
     * @param days   过期天数
     * @return JWT token
     */
    public static String createToken(Long userId, Integer days) {
        return Jwts.builder()
                .subject(DEFAULT_SUBJECT)
                .expiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRATION_MS * days))
                .claim("userId", userId)
                .id(UUID.randomUUID().toString())
                .signWith(getKey())
                .compressWith(Jwts.ZIP.GZIP)
                .compact();
    }

    /**
     * 根据用户ID创建 token，指定过期小时数
     *
     * @param userId 用户ID
     * @param hours  过期小时数
     * @return JWT token
     */
    public static String createTokenWithHours(Long userId, Integer hours) {
        long expirationMs = hours * 60 * 60 * 1000L;
        return Jwts.builder()
                .subject(DEFAULT_SUBJECT)
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .claim("userId", userId)
                .id(UUID.randomUUID().toString())
                .signWith(getKey())
                .compressWith(Jwts.ZIP.GZIP)
                .compact();
    }

    /**
     * 根据用户ID和用户名创建 token，指定过期天数
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param days     过期天数
     * @return JWT token
     */
    public static String createToken(Long userId, String username, Integer days) {
        return Jwts.builder()
                .subject(DEFAULT_SUBJECT)
                .expiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRATION_MS * days))
                .claim("userId", userId)
                .claim("username", username)
                .id(UUID.randomUUID().toString())
                .signWith(getKey())
                .compressWith(Jwts.ZIP.GZIP)
                .compact();
    }

    /**
     * 使用 Map 创建 token（默认7天过期）
     * <p>
     * claims 会直接写入 Token 载荷，调用方负责过滤敏感字段。
     * </p>
     *
     * @param claims 自定义声明
     * @return JWT token
     */
    public static String createTokenWithMap(Map<String, Object> claims) {
        return createTokenWithMap(claims, DEFAULT_EXPIRE_DAYS);
    }

    /**
     * 使用 Map 创建 token，指定过期天数
     * <p>
     * claims 会直接写入 Token 载荷，调用方负责过滤敏感字段。
     * </p>
     *
     * @param claims 自定义声明
     * @param days   过期天数
     * @return JWT token
     */
    public static String createTokenWithMap(Map<String, Object> claims, Integer days) {
        return Jwts.builder()
                .subject(DEFAULT_SUBJECT)
                .expiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRATION_MS * days))
                .claims(claims)
                .id(UUID.randomUUID().toString())
                .signWith(getKey())
                .compressWith(Jwts.ZIP.GZIP)
                .compact();
    }

    /**
     * 使用 Map 创建 token，指定过期时间
     * <p>
     * claims 会直接写入 Token 载荷，调用方负责过滤敏感字段。
     * </p>
     *
     * @param claims     自定义声明
     * @param expireTime 过期时间
     * @return JWT token
     */
    public static String createTokenWithMap(Map<String, Object> claims, Date expireTime) {
        return Jwts.builder()
                .subject(DEFAULT_SUBJECT)
                .expiration(expireTime)
                .claims(claims)
                .id(UUID.randomUUID().toString())
                .signWith(getKey())
                .compressWith(Jwts.ZIP.GZIP)
                .compact();
    }

    /**
     * 使用自定义主题创建 token
     * <p>
     * claims 会直接写入 Token 载荷，调用方负责过滤敏感字段。
     * </p>
     *
     * @param claims  自定义声明
     * @param subject 主题
     * @param days    过期天数
     * @return JWT token
     */
    public static String createTokenWithSubject(Map<String, Object> claims, String subject, Integer days) {
        return Jwts.builder()
                .subject(subject)
                .expiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRATION_MS * days))
                .claims(claims)
                .id(UUID.randomUUID().toString())
                .signWith(getKey())
                .compressWith(Jwts.ZIP.GZIP)
                .compact();
    }

    // ==================== 解析 Token ====================

    /**
     * 从 token 获取用户ID
     *
     * @param token JWT token
     * @return 用户ID
     * @throws BaseException token 缺失、过期、签名无效或不包含 userId 时抛出
     */
    public static Long getUserId(String token) {
        Claims claims = parseToken(token);
        Object userId = claims.get("userId");
        if (userId == null) {
            throw new BaseException(ResultCodeEnum.TOKEN_PARSING_FAILED);
        }
        return Long.valueOf(String.valueOf(userId));
    }

    /**
     * 从 token 获取用户名
     *
     * @param token JWT token
     * @return 用户名
     */
    public static String getUsername(String token) {
        Claims claims = parseToken(token);
        return (String) claims.get("username");
    }

    /**
     * 从 token 获取 openid（小程序用户）
     *
     * @param token JWT token
     * @return openid
     */
    public static String getOpenid(String token) {
        Claims claims = parseToken(token);
        return (String) claims.get("openid");
    }

    /**
     * 从 token 获取主题
     *
     * @param token JWT token
     * @return 主题
     */
    public static String getSubject(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 从 token 获取所有声明（Map 形式）
     * <p>
     * 返回值包含 JWT 标准字段和业务自定义字段，调用方输出日志前需要自行脱敏。
     * </p>
     *
     * @param token JWT token
     * @return 声明 Map
     */
    public static Map<String, Object> getMapByToken(String token) {
        Claims claims = parseToken(token);
        return new HashMap<>(claims);
    }

    /**
     * 从 token 获取指定声明
     *
     * @param token     JWT token
     * @param claimName 声明名称
     * @return 声明值
     */
    public static Object getClaim(String token, String claimName) {
        Claims claims = parseToken(token);
        return claims.get(claimName);
    }

    /**
     * 从 token 获取指定声明（泛型）
     *
     * @param token     JWT token
     * @param claimName 声明名称
     * @param clazz     返回类型
     * @return 声明值
     */
    public static <T> T getClaim(String token, String claimName, Class<T> clazz) {
        Claims claims = parseToken(token);
        return claims.get(claimName, clazz);
    }

    /**
     * 获取 token 过期时间
     *
     * @param token JWT token
     * @return 过期时间
     */
    public static Date getExpiration(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration();
    }

    /**
     * 获取 token ID
     *
     * @param token JWT token
     * @return token ID
     */
    public static String getTokenId(String token) {
        Claims claims = parseToken(token);
        return claims.getId();
    }

    // ==================== 验证 Token ====================

    /**
     * 判断 token 是否过期
     *
     * @param token JWT token
     * @return true-已过期，false-未过期
     */
    public static boolean isExpired(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            return expiration != null && expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 验证 token 是否有效
     *
     * @param token JWT token
     * @return true-有效，false-无效
     */
    public static boolean isValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证 token 是否有效且未过期
     *
     * @param token JWT token
     * @return true-有效且未过期，false-无效或已过期
     */
    public static boolean isValidAndNotExpired(String token) {
        return isValid(token) && !isExpired(token);
    }

    // ==================== 刷新 Token ====================

    /**
     * 刷新 token（使用原 token 的声明创建新 token）
     * <p>
     * 仅复制声明并重签新过期时间，不检查服务端会话、黑名单或刷新令牌状态。
     * </p>
     *
     * @param token 原 JWT token
     * @return 新 JWT token
     */
    public static String refreshToken(String token) {
        return refreshToken(token, DEFAULT_EXPIRE_DAYS);
    }

    /**
     * 刷新 token，指定过期天数
     * <p>
     * 仅复制声明并重签新过期时间，不检查服务端会话、黑名单或刷新令牌状态。
     * </p>
     *
     * @param token 原 JWT token
     * @param days  过期天数
     * @return 新 JWT token
     */
    public static String refreshToken(String token, Integer days) {
        Claims claims = parseToken(token);
        Map<String, Object> newClaims = new HashMap<>(claims);
        // 移除标准声明，避免旧 Token 的过期时间和 ID 被原样继承。
        newClaims.remove("exp");
        newClaims.remove("iat");
        newClaims.remove("jti");
        return createTokenWithMap(newClaims, days);
    }

    // ==================== 私有方法 ====================

    /**
     * 获取当前应用初始化后的 JWT 签名密钥。
     *
     * @return JWT 密钥
     */
    private static SecretKey getKey() {
        return JwtKeyHolder.getKey();
    }

    /**
     * 解析并校验 Token 签名。
     * <p>
     * 解析失败统一转换为业务异常，避免上层暴露 jjwt 内部异常细节。
     * </p>
     *
     * @param token JWT token
     * @return Claims
     * @throws BaseException token 缺失、过期或签名校验失败时抛出
     */
    private static Claims parseToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new BaseException(ResultCodeEnum.TOKEN_PARSING_FAILED);
        }
        try {
            Jws<Claims> claimsJws = Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token);
            return claimsJws.getPayload();
        } catch (ExpiredJwtException e) {
            throw new BaseException(ResultCodeEnum.TOKEN_EXPIRED);
        } catch (Exception e) {
            throw new BaseException(ResultCodeEnum.TOKEN_PARSING_FAILED);
        }
    }
}
