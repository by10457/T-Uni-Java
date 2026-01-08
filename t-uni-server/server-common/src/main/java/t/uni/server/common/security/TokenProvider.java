package t.uni.server.common.security;

import io.jsonwebtoken.Claims;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import t.uni.server.common.utils.ServerJwtTokenUtil;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token安全处理类
 * <p>
 * 提供token的生成、解析、验证和安全增强功能
 * - Token黑名单管理
 * - Token刷新机制
 * - 安全上下文管理
 * - 多环境密钥管理
 * </p>
 */
@Getter
@Slf4j
@Component
public class TokenProvider {

    /**
     * 默认过期时间：1天（毫秒）
     */
    private static final long TOKEN_EXPIRATION_MS = 24 * 60 * 60 * 1000L;

    /**
     * 刷新token过期时间：7天（毫秒）
     */
    private static final long REFRESH_TOKEN_EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000L;

    /**
     * Token黑名单
     * 用于管理已注销或失效的token
     */
    private static final Map<String, Long> TOKEN_BLACKLIST = new ConcurrentHashMap<>();

    /**
     * 创建token
     *
     * @param userId 用户ID
     * @return JWT token
     */
    public String createToken(Long userId) {
        return ServerJwtTokenUtil.createToken(userId);
    }

    /**
     * 创建双token（access token + refresh token）
     *
     * @param userId 用户ID
     * @return 双token Map
     */
    public Map<String, String> createDoubleTokens(Long userId) {
        return ServerJwtTokenUtil.createDoubleTokens(userId);
    }

    /**
     * 创建带用户信息的token（小程序用户）
     *
     * @param userId   用户ID
     * @param openid   小程序openid
     * @param nickname 用户昵称
     * @param avatar   头像URL
     * @return JWT token
     */
    public String createWxToken(Long userId, String openid, String nickname, String avatar) {
        return ServerJwtTokenUtil.createWxToken(userId, openid, nickname, avatar);
    }

    /**
     * 创建小程序用户的双token
     *
     * @param userId   用户ID
     * @param openid   小程序openid
     * @param nickname 用户昵称
     * @param avatar   头像URL
     * @return 双token Map
     */
    public Map<String, String> createWxDoubleTokens(Long userId, String openid, String nickname, String avatar) {
        return ServerJwtTokenUtil.createWxDoubleTokens(userId, openid, nickname, avatar);
    }

    /**
     * 解析token获取用户ID
     *
     * @param token JWT token
     * @return 用户ID
     */
    public Long getUserId(String token) {
        // 检查token是否在黑名单中
        if (isBlacklisted(token)) {
            throw new SecurityException("Token已失效");
        }
        return ServerJwtTokenUtil.getUserId(token);
    }

    /**
     * 解析token获取所有声明
     *
     * @param token JWT token
     * @return 声明Map
     */
    public Map<String, Object> getClaims(String token) {
        if (isBlacklisted(token)) {
            throw new SecurityException("Token已失效");
        }
        return ServerJwtTokenUtil.getMapByToken(token);
    }

    /**
     * 验证token是否有效
     *
     * @param token JWT token
     * @return true-有效，false-无效
     */
    public boolean validateToken(String token) {
        try {
            if (isBlacklisted(token)) {
                return false;
            }
            return ServerJwtTokenUtil.isValid(token);
        } catch (Exception e) {
            log.warn("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 判断token是否过期
     *
     * @param token JWT token
     * @return true-已过期，false-未过期
     */
    public boolean isTokenExpired(String token) {
        try {
            if (isBlacklisted(token)) {
                return true;
            }
            return ServerJwtTokenUtil.isExpired(token);
        } catch (Exception e) {
            log.warn("Token过期检查失败: {}", e.getMessage());
            return true;
        }
    }

    /**
     * 获取token过期时间
     *
     * @param token JWT token
     * @return 过期时间
     */
    public Date getExpiration(String token) {
        if (isBlacklisted(token)) {
            throw new SecurityException("Token已失效");
        }
        return ServerJwtTokenUtil.getExpiration(token);
    }

    /**
     * 刷新token
     *
     * @param token 原token
     * @return 新token
     */
    public String refreshToken(String token) {
        // 检查原token是否有效
        if (!validateToken(token)) {
            throw new SecurityException("Token无效，无法刷新");
        }

        // 将原token加入黑名单
        blacklistToken(token);

        // 获取用户信息创建新token
        Claims claims = ServerJwtTokenUtil.parseToken(token);
        Long userId = Long.valueOf(String.valueOf(claims.get("userId")));

        // 检查是否为小程序用户
        if ("WX_MINI".equals(claims.get("userType"))) {
            String openid = (String) claims.get("openid");
            String nickname = (String) claims.get("nickname");
            String avatar = (String) claims.get("avatar");
            return createWxToken(userId, openid, nickname, avatar);
        } else {
            return createToken(userId);
        }
    }

    /**
     * 将token加入黑名单
     *
     * @param token JWT token
     */
    public void blacklistToken(String token) {
        try {
            Date expiration = ServerJwtTokenUtil.getExpiration(token);
            long expireTime = expiration != null ? expiration.getTime() : System.currentTimeMillis() + TOKEN_EXPIRATION_MS;
            TOKEN_BLACKLIST.put(token, expireTime);
            log.info("Token已加入黑名单: {}", token.substring(0, 10) + "...");
        } catch (Exception e) {
            log.warn("Token加入黑名单失败: {}", e.getMessage());
        }
    }

    /**
     * 检查token是否在黑名单中
     *
     * @param token JWT token
     * @return true-在黑名单中，false-不在黑名单中
     */
    public boolean isBlacklisted(String token) {
        Long expireTime = TOKEN_BLACKLIST.get(token);
        if (expireTime == null) {
            return false;
        }

        // 如果已过期，从黑名单中移除
        if (expireTime < System.currentTimeMillis()) {
            TOKEN_BLACKLIST.remove(token);
            return false;
        }

        return true;
    }

    /**
     * 清理过期的黑名单token
     */
    public void cleanupBlacklist() {
        long currentTime = System.currentTimeMillis();
        TOKEN_BLACKLIST.entrySet().removeIf(entry -> entry.getValue() < currentTime);
    }

    /**
     * 获取当前安全上下文信息
     *
     * @return 安全上下文Map
     */
    public Map<String, Object> getSecurityContext(String token) {
        Map<String, Object> context = new ConcurrentHashMap<>();

        try {
            if (validateToken(token)) {
                Map<String, Object> claims = getClaims(token);
                context.put("userId", claims.get("userId"));
                context.put("userType", claims.get("userType"));
                context.put("openid", claims.get("openid"));
                context.put("nickname", claims.get("nickname"));
                context.put("avatar", claims.get("avatar"));
                context.put("isValid", true);
            } else {
                context.put("isValid", false);
                context.put("error", "Token无效或已过期");
            }
        } catch (Exception e) {
            context.put("isValid", false);
            context.put("error", e.getMessage());
        }

        return context;
    }

    /**
     * 检查token是否为小程序用户
     *
     * @param token JWT token
     * @return true-是小程序用户，false-不是
     */
    public boolean isWxUser(String token) {
        try {
            return ServerJwtTokenUtil.isWxUser(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从token中获取openid
     *
     * @param token JWT token
     * @return openid
     */
    public String getOpenid(String token) {
        if (isBlacklisted(token)) {
            throw new SecurityException("Token已失效");
        }
        return ServerJwtTokenUtil.getOpenid(token);
    }

    /**
     * 从token中获取用户昵称
     *
     * @param token JWT token
     * @return 用户昵称
     */
    public String getNickname(String token) {
        if (isBlacklisted(token)) {
            throw new SecurityException("Token已失效");
        }
        return ServerJwtTokenUtil.getNickname(token);
    }

    /**
     * 从token中获取头像URL
     *
     * @param token JWT token
     * @return 头像URL
     */
    public String getAvatar(String token) {
        if (isBlacklisted(token)) {
            throw new SecurityException("Token已失效");
        }
        return ServerJwtTokenUtil.getAvatar(token);
    }

}
