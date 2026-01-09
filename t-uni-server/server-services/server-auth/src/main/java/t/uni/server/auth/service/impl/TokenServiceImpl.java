package t.uni.server.auth.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import t.uni.common.core.constant.RedisKeyConstants;
import t.uni.common.core.exception.BaseException;
import t.uni.common.core.redis.RedisUtil;
import t.uni.common.core.result.ResultCodeEnum;
import t.uni.common.core.utils.JwtTokenUtil;
import t.uni.server.common.auth.TokenService;
import t.uni.server.domain.vo.auth.TokenVO;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Token 服务实现
 * <p>
 * 实现双 Token 机制 + 两层缓存
 * - 第一层：用户信息缓存（openId -> userId/uniqueId 映射，永久，Redis Hash）
 * - 第二层：Token 缓存（userId -> token 信息，永久，取出时检查过期，Redis Hash）
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final RedisUtil redisUtil;

    /**
     * 生成双 Token
     */
    @Override
    public TokenVO generateTokens(Long userId, String openId) {
        // 1. 生成 Access Token（JWT），只存 userId
        String accessToken = JwtTokenUtil.createToken(userId, RedisKeyConstants.TOKEN_EXPIRE_DAYS);

        // 2. 生成 Refresh Token（UUID）
        String refreshToken = UUID.randomUUID().toString().replace("-", "");

        // 3. 计算有效期（秒）
        long expiresIn = TimeUnit.DAYS.toSeconds(RedisKeyConstants.TOKEN_EXPIRE_DAYS);

        // 4. 构建 TokenVO
        TokenVO tokenVO = TokenVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessExpiresIn(expiresIn)
                .refreshExpiresIn(expiresIn)
                .build();

        // 5. 缓存 Token 信息到 Redis Hash（永久存储，取出时检查过期）
        cacheToken(userId, tokenVO, openId);

        log.info("为用户 {} 生成双Token成功", userId);
        return tokenVO;
    }

    /**
     * 刷新 Token
     */
    @Override
    public TokenVO refreshTokens(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BaseException(ResultCodeEnum.TOKEN_PARSING_FAILED.getCode(), "刷新令牌不能为空");
        }

        // 1. 从缓存获取索引 (String type is fine for simple index)
        String indexKey = "wx:refresh:index:" + refreshToken;
        String userIdStr = redisUtil.get(indexKey, String.class);

        if (userIdStr == null || userIdStr.isBlank()) {
            throw new BaseException(ResultCodeEnum.TOKEN_EXPIRED.getCode(), "刷新令牌已失效或不存在");
        }

        Long userId = Long.valueOf(userIdStr);

        // 2. 获取缓存的 Token 信息 (Redis Hash)
        String tokenKey = RedisKeyConstants.wxUserToken(userId);
        Map<Object, Object> tokenDataRaw = redisUtil.hGetAll(tokenKey);

        if (tokenDataRaw == null || tokenDataRaw.isEmpty()) {
            throw new BaseException(ResultCodeEnum.TOKEN_EXPIRED.getCode(), "刷新令牌已失效或不存在");
        }

        // 3. 校验 refreshToken 是否匹配
        String storedRefreshToken = (String) tokenDataRaw.get("refreshToken");
        if (!refreshToken.equals(storedRefreshToken)) {
            throw new BaseException(ResultCodeEnum.TOKEN_EXPIRED.getCode(), "刷新令牌已失效或不存在");
        }

        // 4. 获取原始过期时间，计算剩余时间
        Object expireTimeObj = tokenDataRaw.get("expireTimeMs");
        long expireTimeMs = expireTimeObj instanceof Number ? ((Number) expireTimeObj).longValue() : Long.parseLong(String.valueOf(expireTimeObj));
        long remainingMs = expireTimeMs - System.currentTimeMillis();

        if (remainingMs <= 0) {
            throw new BaseException(ResultCodeEnum.TOKEN_EXPIRED.getCode(), "刷新令牌已过期");
        }

        long remainingSeconds = remainingMs / 1000;
        String openId = (String) tokenDataRaw.get("openId");

        // 5. 删除旧的反向索引
        redisUtil.delete(indexKey);

        // 6. 生成新的双 Token，使用剩余时间
        return generateTokensWithTtl(userId, openId, remainingSeconds);
    }

    /**
     * 验证 Access Token 并获取用户ID
     */
    @Override
    public Long validateAndGetUserId(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new BaseException(ResultCodeEnum.TOKEN_PARSING_FAILED.getCode(), "访问令牌不能为空");
        }

        // 使用 JwtTokenUtil 验证并解析
        if (!JwtTokenUtil.isValidAndNotExpired(accessToken)) {
            throw new BaseException(ResultCodeEnum.TOKEN_EXPIRED.getCode(), "访问令牌已过期或无效");
        }

        return JwtTokenUtil.getUserId(accessToken);
    }

    /**
     * 使 Token 失效
     */
    @Override
    public void invalidateToken(Long userId) {
        String tokenKey = RedisKeyConstants.wxUserToken(userId);

        // 获取并删除 refreshToken 的反向索引
        Object refreshTokenObj = redisUtil.hGet(tokenKey, "refreshToken");
        if (refreshTokenObj != null) {
            String refreshToken = String.valueOf(refreshTokenObj);
            redisUtil.delete("wx:refresh:index:" + refreshToken);
        }

        redisUtil.delete(tokenKey);
        log.info("用户 {} 的Token已失效", userId);
    }

    /**
     * 从缓存获取有效 Token（Python 风格）
     */
    @Override
    public TokenVO getCachedToken(String openId) {
        // 1. 先获取用户ID
        Long userId = getCachedUserId(openId);
        if (userId == null) {
            return null;
        }

        // 2. 获取 Token 缓存 (Redis Hash)
        String tokenKey = RedisKeyConstants.wxUserToken(userId);
        Map<Object, Object> tokenDataRaw = redisUtil.hGetAll(tokenKey);

        if (tokenDataRaw == null || tokenDataRaw.isEmpty()) {
            return null;
        }

        // 3. 检查是否过期
        Object expireTimeObj = tokenDataRaw.get("expireTimeMs");
        if (expireTimeObj == null) return null;
        
        long expireTimeMs = expireTimeObj instanceof Number ? ((Number) expireTimeObj).longValue() : Long.parseLong(String.valueOf(expireTimeObj));
        
        if (System.currentTimeMillis() > expireTimeMs) {
            log.info("用户 {} 的Token缓存已过期", userId);
            // todo 继续生成两个token
            return null;
        }

        // 4. 计算剩余秒数
        long remainingSeconds = (expireTimeMs - System.currentTimeMillis()) / 1000;

        return TokenVO.builder()
                .accessToken((String) tokenDataRaw.get("accessToken"))
                .refreshToken((String) tokenDataRaw.get("refreshToken"))
                .accessExpiresIn(remainingSeconds)
                .refreshExpiresIn(remainingSeconds)
                .build();
    }

    /**
     * 缓存用户信息（Redis Hash）
     */
    @Override
    public void cacheUserInfo(String openId, Long userId, String unionId) {
        String key = RedisKeyConstants.wxUserInfo(openId);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", userId);
        userInfo.put("openId", openId);
        if (unionId != null && !unionId.isBlank()) {
            userInfo.put("unionId", unionId);
        }

        // 使用 Redis Hash 存储
        redisUtil.hSetAll(key, userInfo);
    }

    /**
     * 从缓存获取用户ID
     */
    @Override
    public Long getCachedUserId(String openId) {
        String key = RedisKeyConstants.wxUserInfo(openId);
        // 直接获取 Hash 中的 userId 字段
        Object userIdObj = redisUtil.hGet(key, "userId");

        if (userIdObj == null) {
            return null;
        }

        return Long.valueOf(String.valueOf(userIdObj));
    }

    // ==================== 私有方法 ====================

    /**
     * 缓存 Token 信息到 Redis Hash
     */
    private void cacheToken(Long userId, TokenVO tokenVO, String openId) {
        String tokenKey = RedisKeyConstants.wxUserToken(userId);

        // 计算绝对过期时间（毫秒）
        long expireTimeMs = System.currentTimeMillis() + RedisKeyConstants.TOKEN_EXPIRE_MS;

        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("accessToken", tokenVO.getAccessToken());
        tokenData.put("refreshToken", tokenVO.getRefreshToken());
        tokenData.put("expireTimeMs", expireTimeMs);
        tokenData.put("openId", openId);

        // 使用 Redis Hash 存储 (永久存储，取出时检查过期)
        redisUtil.hSetAll(tokenKey, tokenData);

        // 建立 refreshToken -> userId 的反向索引（用于刷新时查找）
        String indexKey = "wx:refresh:index:" + tokenVO.getRefreshToken();
        redisUtil.set(indexKey, String.valueOf(userId));
    }

    /**
     * 使用指定 TTL 生成双 Token（用于刷新场景）
     */
    private TokenVO generateTokensWithTtl(Long userId, String openId, long remainingSeconds) {
        // 计算天数（向上取整，至少1天）
        int days = Math.max(1, (int) Math.ceil(remainingSeconds / (24.0 * 60 * 60)));

        // 1. 生成 Access Token
        String accessToken = JwtTokenUtil.createToken(userId, days);

        // 2. 生成 Refresh Token
        String refreshToken = UUID.randomUUID().toString().replace("-", "");

        // 3. 构建 TokenVO
        TokenVO tokenVO = TokenVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessExpiresIn(remainingSeconds)
                .refreshExpiresIn(remainingSeconds)
                .build();

        // 4. 缓存 Token 信息（使用剩余时间）
        cacheTokenWithTtl(userId, tokenVO, openId, remainingSeconds);

        log.info("用户 {} 刷新Token成功，剩余有效期 {} 秒", userId, remainingSeconds);
        return tokenVO;
    }

    /**
     * 使用指定 TTL 缓存 Token (Redis Hash)
     */
    private void cacheTokenWithTtl(Long userId, TokenVO tokenVO, String openId, long remainingSeconds) {
        String tokenKey = RedisKeyConstants.wxUserToken(userId);

        // 计算绝对过期时间
        long expireTimeMs = System.currentTimeMillis() + remainingSeconds * 1000;

        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("accessToken", tokenVO.getAccessToken());
        tokenData.put("refreshToken", tokenVO.getRefreshToken());
        tokenData.put("expireTimeMs", expireTimeMs);
        tokenData.put("openId", openId);

        // 使用 Redis Hash 存储
        redisUtil.hSetAll(tokenKey, tokenData);

        // 建立反向索引
        String indexKey = "wx:refresh:index:" + tokenVO.getRefreshToken();
        redisUtil.set(indexKey, String.valueOf(userId));
    }
}
