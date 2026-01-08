package t.uni.common.core.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.springframework.util.StringUtils;
import t.uni.common.core.exception.BaseException;
import t.uni.common.core.result.ResultCodeEnum;
import t.uni.common.core.security.JwtKeyHolder;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JWT Token 工具类
 * <p>
 * 提供 JWT token 的创建、解析、验证等功能
 * 支持 admin 和 server 两端使用
 * </p>
 */
public class JwtTokenUtil {

    /**
     * 默认过期时间：1天（毫秒）
     */
    private static final long TOKEN_EXPIRATION_MS = 24 * 60 * 60 * 1000L;

    /**
     * 默认过期天数
     */
    private static final int DEFAULT_EXPIRE_DAYS = 7;

    /**
     * 默认主题
     */
    private static final String DEFAULT_SUBJECT = "T-Uni";

    // ==================== 创建 Token ====================

    /**
     * 根据用户ID和用户名创建 token（默认7天过期）
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return JWT token
     */
    public static String createToken(Long userId, String username) {
        return createToken(userId, username, DEFAULT_EXPIRE_DAYS);
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
     *
     * @param claims 自定义声明
     * @return JWT token
     */
    public static String createTokenWithMap(Map<String, Object> claims) {
        return createTokenWithMap(claims, DEFAULT_EXPIRE_DAYS);
    }

    /**
     * 使用 Map 创建 token，指定过期天数
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
     *
     * @param token 原 JWT token
     * @return 新 JWT token
     */
    public static String refreshToken(String token) {
        return refreshToken(token, DEFAULT_EXPIRE_DAYS);
    }

    /**
     * 刷新 token，指定过期天数
     *
     * @param token 原 JWT token
     * @param days  过期天数
     * @return 新 JWT token
     */
    public static String refreshToken(String token, Integer days) {
        Claims claims = parseToken(token);
        Map<String, Object> newClaims = new HashMap<>(claims);
        // 移除原有的过期时间等系统字段
        newClaims.remove("exp");
        newClaims.remove("iat");
        newClaims.remove("jti");
        return createTokenWithMap(newClaims, days);
    }

    // ==================== 私有方法 ====================

    /**
     * 获取 JWT 密钥
     * <p>
     * 从配置文件中读取的密钥（通过 JwtKeyHolder 管理）
     * </p>
     *
     * @return JWT 密钥
     */
    private static SecretKey getKey() {
        return JwtKeyHolder.getKey();
    }

    /**
     * 解析 token 获取 Claims
     *
     * @param token JWT token
     * @return Claims
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
