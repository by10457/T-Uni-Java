package t.uni.server.core.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.util.StringUtils;
import t.uni.common.core.exception.BaseException;
import t.uni.common.core.result.ResultCodeEnum;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 服务端 JWT Token 工具类
 * <p>
 * 基于 common-core 的 JwtTokenUtil，添加服务端特有的功能：
 * - 小程序用户相关的token生成
 * - 双token机制支持（access token + refresh token）
 * - 服务端特定的密钥管理
 * </p>
 */
public class ServerJwtTokenUtil {

    /**
     * 服务端密钥（与admin端不同）
     */
    private static final SecretKey SERVER_KEY = Keys.hmacShaKeyFor(
            "T-Uni-Server-Private-SecretKey-2024".getBytes(StandardCharsets.UTF_8));

    /**
     * 默认过期时间：1天（毫秒）
     */
    private static final long TOKEN_EXPIRATION_MS = 24 * 60 * 60 * 1000L;

    /**
     * 刷新token过期时间：7天（毫秒）
     */
    private static final long REFRESH_TOKEN_EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000L;

    /**
     * 默认主题
     */
    private static final String DEFAULT_SUBJECT = "T-Uni-Server";

    // ==================== 小程序用户 Token ====================

    /**
     * 根据小程序用户信息创建token
     *
     * @param userId   用户ID
     * @param openid   小程序openid
     * @param nickname 用户昵称
     * @param avatar   头像URL
     * @return JWT token
     */
    public static String createWxToken(Long userId, String openid, String nickname, String avatar) {
        return createWxToken(userId, openid, nickname, avatar, 7);
    }

    /**
     * 根据小程序用户信息创建token，指定过期天数
     *
     * @param userId   用户ID
     * @param openid   小程序openid
     * @param nickname 用户昵称
     * @param avatar   头像URL
     * @param days     过期天数
     * @return JWT token
     */
    public static String createWxToken(Long userId, String openid, String nickname, String avatar, Integer days) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("openid", openid);
        claims.put("nickname", nickname);
        claims.put("avatar", avatar);
        claims.put("userType", "WX_MINI"); // 标识为小程序用户

        return Jwts.builder()
                .subject(DEFAULT_SUBJECT)
                .expiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRATION_MS * days))
                .claims(claims)
                .id(UUID.randomUUID().toString())
                .signWith(SERVER_KEY)
                .compressWith(Jwts.ZIP.GZIP)
                .compact();
    }

    /**
     * 根据用户ID创建token（简化版）
     *
     * @param userId 用户ID
     * @return JWT token
     */
    public static String createToken(Long userId) {
        return createToken(userId, 7);
    }

    /**
     * 根据用户ID创建token，指定过期天数
     *
     * @param userId 用户ID
     * @param days   过期天数
     * @return JWT token
     */
    public static String createToken(Long userId, Integer days) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);

        return Jwts.builder()
                .subject(DEFAULT_SUBJECT)
                .expiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRATION_MS * days))
                .claims(claims)
                .id(UUID.randomUUID().toString())
                .signWith(SERVER_KEY)
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
        return createTokenWithMap(claims, 7);
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
                .signWith(SERVER_KEY)
                .compressWith(Jwts.ZIP.GZIP)
                .compact();
    }

    /**
     * 创建双token（access token + refresh token）
     *
     * @param userId 用户ID
     * @return 双token Map
     */
    public static Map<String, String> createDoubleTokens(Long userId) {
        Map<String, String> tokens = new HashMap<>();

        // Access Token - 2小时过期
        String accessToken = createToken(userId, 1);
        tokens.put("accessToken", accessToken);

        // Refresh Token - 7天过期
        String refreshToken = createToken(userId, 7);
        tokens.put("refreshToken", refreshToken);

        return tokens;
    }

    /**
     * 创建带用户信息的双token
     *
     * @param userId   用户ID
     * @param openid   小程序openid
     * @param nickname 用户昵称
     * @param avatar   头像URL
     * @return 双token Map
     */
    public static Map<String, String> createWxDoubleTokens(Long userId, String openid, String nickname, String avatar) {
        Map<String, String> tokens = new HashMap<>();

        // Access Token - 2小时过期
        String accessToken = createWxToken(userId, openid, nickname, avatar, 1);
        tokens.put("accessToken", accessToken);

        // Refresh Token - 7天过期
        String refreshToken = createWxToken(userId, openid, nickname, avatar, 7);
        tokens.put("refreshToken", refreshToken);

        return tokens;
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
     * 从 token 获取openid（小程序用户）
     *
     * @param token JWT token
     * @return openid
     */
    public static String getOpenid(String token) {
        Claims claims = parseToken(token);
        return (String) claims.get("openid");
    }

    /**
     * 从 token 获取用户昵称
     *
     * @param token JWT token
     * @return 用户昵称
     */
    public static String getNickname(String token) {
        Claims claims = parseToken(token);
        return (String) claims.get("nickname");
    }

    /**
     * 从 token 获取头像URL
     *
     * @param token JWT token
     * @return 头像URL
     */
    public static String getAvatar(String token) {
        Claims claims = parseToken(token);
        return (String) claims.get("avatar");
    }

    /**
     * 从 token 获取用户类型
     *
     * @param token JWT token
     * @return 用户类型
     */
    public static String getUserType(String token) {
        Claims claims = parseToken(token);
        return (String) claims.get("userType");
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
     * 获取 token 过期时间
     *
     * @param token JWT token
     * @return 过期时间
     */
    public static Date getExpiration(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration();
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
        } catch (ExpiredJwtException e) {
            return true;
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
     * 验证 token 是否为小程序用户
     *
     * @param token JWT token
     * @return true-是小程序用户，false-不是
     */
    public static boolean isWxUser(String token) {
        try {
            String userType = getUserType(token);
            return "WX_MINI".equals(userType);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证 token 是否包含openid
     *
     * @param token JWT token
     * @return true-包含openid，false-不包含
     */
    public static boolean hasOpenid(String token) {
        try {
            return getOpenid(token) != null;
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== 刷新 Token ====================

    /**
     * 刷新 token（使用原 token 的声明创建新 token）
     *
     * @param token 原 JWT token
     * @return 新 JWT token
     */
    public static String refreshToken(String token) {
        return refreshToken(token, 7);
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

    // ==================== 公开方法 ====================

    /**
     * 解析 token 获取 Claims
     *
     * @param token JWT token
     * @return Claims
     */
    public static Claims parseToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new BaseException(ResultCodeEnum.TOKEN_PARSING_FAILED);
        }
        try {
            Jws<Claims> claimsJws = Jwts.parser()
                    .verifyWith(SERVER_KEY)
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