package t.uni.server.auth.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import t.uni.common.core.constant.RedisKeyConstants;
import t.uni.common.core.exception.BaseException;
import t.uni.common.core.redis.RedisUtil;
import t.uni.common.core.result.ResultCodeEnum;
import t.uni.common.core.utils.JwtTokenUtil;
import t.uni.common.core.utils.MaskUtil;
import t.uni.server.auth.mapper.CoreUserMapper;
import t.uni.server.common.auth.TokenService;
import t.uni.server.domain.constant.AuthConstant;
import t.uni.server.domain.vo.auth.TokenVO;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Token 服务实现
 * <p>
 * 实现双 Token 机制 + 两层缓存
 * - AccessToken: 2小时过期（JWT）
 * - RefreshToken: 7天过期（UUID）
 * - 分别存储 accessExpireTimeMs 和 refreshExpireTimeMs
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final RedisUtil redisUtil;
    private final CoreUserMapper coreUserMapper;

    @Override
    public TokenVO generateTokens(Long userId, String openId) {
        // 1. 生成 Access Token（JWT），2小时过期
        var accessToken = JwtTokenUtil.createTokenWithHours(userId, AuthConstant.ACCESS_TOKEN_EXPIRE_HOURS);

        // 2. 生成 Refresh Token（UUID）
        var refreshToken = UUID.randomUUID().toString().replace("-", "");

        // 3. 计算有效期（秒）- AccessToken 2小时，RefreshToken 7天
        long accessExpiresIn = TimeUnit.HOURS.toSeconds(AuthConstant.ACCESS_TOKEN_EXPIRE_HOURS);
        long refreshExpiresIn = TimeUnit.DAYS.toSeconds(AuthConstant.REFRESH_TOKEN_EXPIRE_DAYS);

        // 4. 构建 TokenVO
        var tokenVO = TokenVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessExpiresIn(accessExpiresIn)
                .refreshExpiresIn(refreshExpiresIn)
                .build();

        // 5. 缓存 Token 信息到 Redis Hash（永久存储，取出时检查过期）
        cacheToken(userId, tokenVO, openId);

        log.info("为用户 {} 生成双Token成功，AccessToken {}小时，RefreshToken {}天",
                userId, AuthConstant.ACCESS_TOKEN_EXPIRE_HOURS, AuthConstant.REFRESH_TOKEN_EXPIRE_DAYS);
        return tokenVO;
    }

    @Override
    public TokenVO refreshTokens(String refreshToken) {
        if (StrUtil.isBlank(refreshToken)) {
            throw new BaseException(ResultCodeEnum.REFRESH_TOKEN_EMPTY);
        }

        // 1. 从缓存获取索引
        var indexKey = RedisKeyConstants.wxRefreshToken(refreshToken);
        var userIdStr = redisUtil.get(indexKey, String.class);

        if (StrUtil.isBlank(userIdStr)) {
            throw new BaseException(ResultCodeEnum.REFRESH_TOKEN_INVALID);
        }

        var userId = Long.valueOf(userIdStr);

        // 2. 校验用户状态（是否被禁用或注销）
        validateUserStatus(userId);

        // 3. 获取缓存的 Token 信息 (Redis Hash)
        var tokenKey = RedisKeyConstants.wxUserToken(userId);
        var tokenDataRaw = redisUtil.hGetAll(tokenKey);

        if (CollUtil.isEmpty(tokenDataRaw)) {
            throw new BaseException(ResultCodeEnum.REFRESH_TOKEN_INVALID);
        }

        // 4. 校验 refreshToken 是否匹配
        var storedRefreshToken = (String) tokenDataRaw.get("refreshToken");
        if (!refreshToken.equals(storedRefreshToken)) {
            throw new BaseException(ResultCodeEnum.REFRESH_TOKEN_INVALID);
        }

        // 5. 获取 RefreshToken 过期时间，计算剩余时间
        var refreshExpireTimeObj = tokenDataRaw.get("refreshExpireTimeMs");
        long refreshExpireTimeMs = refreshExpireTimeObj instanceof Number
                ? ((Number) refreshExpireTimeObj).longValue()
                : Long.parseLong(String.valueOf(refreshExpireTimeObj));
        long refreshRemainingMs = refreshExpireTimeMs - System.currentTimeMillis();

        if (refreshRemainingMs <= 0) {
            throw new BaseException(ResultCodeEnum.REFRESH_TOKEN_EXPIRED);
        }

        long refreshRemainingSeconds = refreshRemainingMs / 1000;
        var openId = (String) tokenDataRaw.get("openId");

        // 6. 删除旧的反向索引
        redisUtil.delete(indexKey);

        // 7. 生成新的双 Token：AccessToken 新2小时，RefreshToken 保持剩余时间
        return generateTokensWithTtl(userId, openId, refreshRemainingSeconds);
    }

    @Override
    public Long validateAndGetUserId(String accessToken) {
        if (StrUtil.isBlank(accessToken)) {
            throw new BaseException(ResultCodeEnum.ACCESS_TOKEN_EMPTY);
        }

        if (!JwtTokenUtil.isValidAndNotExpired(accessToken)) {
            throw new BaseException(ResultCodeEnum.ACCESS_TOKEN_INVALID);
        }

        var userId = JwtTokenUtil.getUserId(accessToken);
        var cachedAccessToken = redisUtil.hGet(RedisKeyConstants.wxUserToken(userId), "accessToken");
        var requestToken = normalizeTokenForCompare(accessToken);
        var currentToken = normalizeTokenForCompare(cachedAccessToken == null ? null : String.valueOf(cachedAccessToken));
        if (StrUtil.isBlank(currentToken) || !requestToken.equals(currentToken)) {
            throw new BaseException(ResultCodeEnum.ACCESS_TOKEN_INVALID);
        }
        return userId;
    }

    @Override
    public void invalidateToken(Long userId) {
        var tokenKey = RedisKeyConstants.wxUserToken(userId);

        // 获取并删除 refreshToken 的反向索引
        var refreshTokenObj = redisUtil.hGet(tokenKey, "refreshToken");
        if (refreshTokenObj != null) {
            var refreshToken = String.valueOf(refreshTokenObj);
            redisUtil.delete(RedisKeyConstants.wxRefreshToken(refreshToken));
        }

        redisUtil.delete(tokenKey);
        log.info("用户 {} 的Token已失效", userId);
    }

    @Override
    public TokenVO getCachedToken(String openId) {
        var userId = getCachedUserId(openId);
        if (userId == null) {
            return null;
        }
        if (!isUserAvailable(userId)) {
            log.warn("检测到无效登录缓存，自动清理并回落重登流程，openId={}, userId={}",
                    MaskUtil.maskOpenId(openId), userId);
            clearLoginCache(openId, userId);
            return null;
        }

        var tokenKey = RedisKeyConstants.wxUserToken(userId);
        var tokenDataRaw = redisUtil.hGetAll(tokenKey);

        if (CollUtil.isEmpty(tokenDataRaw)) {
            redisUtil.delete(RedisKeyConstants.wxUserInfo(openId));
            return null;
        }

        var refreshExpireTimeObj = tokenDataRaw.get("refreshExpireTimeMs");
        if (refreshExpireTimeObj == null) {
            clearLoginCache(openId, userId);
            return null;
        }

        long refreshExpireTimeMs = refreshExpireTimeObj instanceof Number ?
                ((Number) refreshExpireTimeObj).longValue() : Long.parseLong(String.valueOf(refreshExpireTimeObj));

        var openIdFromCache = tokenDataRaw.get("openId");
        if (openIdFromCache == null) {
            clearLoginCache(openId, userId);
            return null;
        }

        long currentTimeMs = System.currentTimeMillis();
        long refreshRemainingMs = refreshExpireTimeMs - currentTimeMs;

        if (refreshRemainingMs <= 0) {
            log.info("用户 {} 的RefreshToken已过期，需要重新登录", userId);
            clearLoginCache(openId, userId);
            return null;
        }

        var accessExpireTimeObj = tokenDataRaw.get("accessExpireTimeMs");
        long accessExpireTimeMs = accessExpireTimeObj instanceof Number ?
                ((Number) accessExpireTimeObj).longValue() : Long.parseLong(String.valueOf(accessExpireTimeObj));
        long accessRemainingMs = accessExpireTimeMs - currentTimeMs;

        boolean refreshNeedsRenew = refreshRemainingMs < RedisKeyConstants.TOKEN_REFRESH_THRESHOLD_MS;
        boolean accessExpired = accessRemainingMs <= 0;

        if (refreshNeedsRenew || accessExpired) {
            if (accessExpired) {
                log.info("用户 {} 的AccessToken已过期，重新生成双Token", userId);
            } else {
                log.info("用户 {} 的RefreshToken即将过期（剩余 {} ms），自动续期", userId, refreshRemainingMs);
            }
            updateLastLoginTime(userId);
            return generateTokens(userId, String.valueOf(openIdFromCache));
        }

        var accessTokenObj = tokenDataRaw.get("accessToken");
        if (accessTokenObj == null || StrUtil.isBlank(String.valueOf(accessTokenObj))) {
            log.warn("用户 {} 缓存 accessToken 为空，重新生成双Token", userId);
            updateLastLoginTime(userId);
            return generateTokens(userId, String.valueOf(openIdFromCache));
        }
        var accessToken = String.valueOf(accessTokenObj);
        if (!JwtTokenUtil.isValidAndNotExpired(accessToken)) {
            log.warn("用户 {} 缓存 accessToken 在当前服务下无效，重新生成双Token", userId);
            updateLastLoginTime(userId);
            return generateTokens(userId, String.valueOf(openIdFromCache));
        }

        long accessExpiresIn = TimeUnit.HOURS.toSeconds(AuthConstant.ACCESS_TOKEN_EXPIRE_HOURS);
        long refreshExpiresIn = TimeUnit.DAYS.toSeconds(AuthConstant.REFRESH_TOKEN_EXPIRE_DAYS);

        log.info("用户 {} 的Token仍然有效，AccessToken剩余 {} 秒，RefreshToken剩余 {} 秒",
                userId, accessRemainingMs / 1000, refreshRemainingMs / 1000);

        return TokenVO.builder()
                .accessToken(accessToken)
                .refreshToken((String) tokenDataRaw.get("refreshToken"))
                .accessExpiresIn(accessExpiresIn)
                .refreshExpiresIn(refreshExpiresIn)
                .build();
    }

    @Override
    public void cacheUserInfo(String openId, Long userId, String unionId) {
        var key = RedisKeyConstants.wxUserInfo(openId);

        var userInfo = new HashMap<String, Object>();
        userInfo.put("userId", userId);
        userInfo.put("openId", openId);
        if (StrUtil.isNotBlank(unionId)) {
            userInfo.put("unionId", unionId);
        }

        // 使用 Redis Hash 存储
        redisUtil.hSetAll(key, userInfo);
    }

    @Override
    public Long getCachedUserId(String openId) {
        var key = RedisKeyConstants.wxUserInfo(openId);
        // 直接获取 Hash 中的 userId 字段
        var userIdObj = redisUtil.hGet(key, "userId");

        if (userIdObj == null) {
            return null;
        }

        return Long.valueOf(String.valueOf(userIdObj));
    }

    // ==================== 私有方法 ====================

    /**
     * 校验用户状态
     */
    private void validateUserStatus(Long userId) {
        var coreUser = coreUserMapper.selectById(userId);
        if (coreUser == null) {
            log.warn("刷新Token失败：用户不存在，userId: {}", userId);
            throw new BaseException(ResultCodeEnum.USER_NOT_EXIST);
        }
        if (Integer.valueOf(1).equals(coreUser.getIsDestroy())) {
            log.warn("刷新Token失败：用户已注销，userId: {}", userId);
            invalidateToken(userId);
            throw new BaseException(ResultCodeEnum.USER_DESTROYED);
        }
        if (Integer.valueOf(1).equals(coreUser.getIsDisable())) {
            log.warn("刷新Token失败：用户已禁用，userId: {}", userId);
            invalidateToken(userId);
            throw new BaseException(ResultCodeEnum.USER_DISABLED);
        }
    }

    /**
     * 更新最后登录时间
     */
    private void updateLastLoginTime(Long userId) {
        var coreUser = coreUserMapper.selectById(userId);
        if (coreUser == null) {
            return;
        }
        coreUser.setLastLoginTime(LocalDateTime.now());
        coreUser.setNewUsageTime(LocalDateTime.now());
        coreUserMapper.updateById(coreUser);
    }

    /**
     * 缓存 Token 信息到 Redis Hash
     */
    private void cacheToken(Long userId, TokenVO tokenVO, String openId) {
        var tokenKey = RedisKeyConstants.wxUserToken(userId);

        // 计算绝对过期时间（毫秒）
        long currentTimeMs = System.currentTimeMillis();
        long accessExpireTimeMs = currentTimeMs + AuthConstant.ACCESS_TOKEN_EXPIRE_MS;
        long refreshExpireTimeMs = currentTimeMs + AuthConstant.REFRESH_TOKEN_EXPIRE_MS;

        var tokenData = new HashMap<String, Object>();
        tokenData.put("accessToken", tokenVO.getAccessToken());
        tokenData.put("refreshToken", tokenVO.getRefreshToken());
        tokenData.put("accessExpireTimeMs", accessExpireTimeMs);
        tokenData.put("refreshExpireTimeMs", refreshExpireTimeMs);
        tokenData.put("openId", openId);

        // 使用 Redis Hash 存储 (永久存储，取出时检查过期)
        redisUtil.hSetAll(tokenKey, tokenData);

        // 建立 refreshToken -> userId 的反向索引（用于刷新时查找）
        var indexKey = RedisKeyConstants.wxRefreshToken(tokenVO.getRefreshToken());
        redisUtil.set(indexKey, String.valueOf(userId));
    }

    /**
     * 使用指定 TTL 生成双 Token
     */
    private TokenVO generateTokensWithTtl(Long userId, String openId, long refreshRemainingSeconds) {
        // 1. 生成 Access Token（新的2小时）
        var accessToken = JwtTokenUtil.createTokenWithHours(userId, AuthConstant.ACCESS_TOKEN_EXPIRE_HOURS);

        // 2. 生成 Refresh Token
        var refreshToken = UUID.randomUUID().toString().replace("-", "");

        // 3. 计算有效期（秒）
        long accessExpiresIn = TimeUnit.HOURS.toSeconds(AuthConstant.ACCESS_TOKEN_EXPIRE_HOURS);

        // 4. 构建 TokenVO
        var tokenVO = TokenVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessExpiresIn(accessExpiresIn)
                .refreshExpiresIn(refreshRemainingSeconds)
                .build();

        // 5. 缓存 Token 信息（AccessToken 新2小时，RefreshToken 使用剩余时间）
        cacheTokenWithTtl(userId, tokenVO, openId, refreshRemainingSeconds);

        log.info("用户 {} 刷新Token成功，AccessToken {}小时，RefreshToken剩余 {} 秒",
                userId, AuthConstant.ACCESS_TOKEN_EXPIRE_HOURS, refreshRemainingSeconds);
        return tokenVO;
    }

    /**
     * 使用指定 TTL 缓存 Token
     */
    private void cacheTokenWithTtl(Long userId, TokenVO tokenVO, String openId, long refreshRemainingSeconds) {
        var tokenKey = RedisKeyConstants.wxUserToken(userId);

        // 计算绝对过期时间
        long currentTimeMs = System.currentTimeMillis();
        long accessExpireTimeMs = currentTimeMs + AuthConstant.ACCESS_TOKEN_EXPIRE_MS;
        long refreshExpireTimeMs = currentTimeMs + refreshRemainingSeconds * 1000;

        var tokenData = new HashMap<String, Object>();
        tokenData.put("accessToken", tokenVO.getAccessToken());
        tokenData.put("refreshToken", tokenVO.getRefreshToken());
        tokenData.put("accessExpireTimeMs", accessExpireTimeMs);
        tokenData.put("refreshExpireTimeMs", refreshExpireTimeMs);
        tokenData.put("openId", openId);

        // 使用 Redis Hash 存储
        redisUtil.hSetAll(tokenKey, tokenData);

        // 建立反向索引
        var indexKey = RedisKeyConstants.wxRefreshToken(tokenVO.getRefreshToken());
        redisUtil.set(indexKey, String.valueOf(userId));
    }

    private boolean isUserAvailable(Long userId) {
        var coreUser = coreUserMapper.selectById(userId);
        if (coreUser == null) {
            return false;
        }
        return !Integer.valueOf(1).equals(coreUser.getIsDestroy())
                && !Integer.valueOf(1).equals(coreUser.getIsDisable());
    }

    private void clearLoginCache(String openId, Long userId) {
        if (userId == null) {
            redisUtil.delete(RedisKeyConstants.wxUserInfo(openId));
            return;
        }
        var tokenKey = RedisKeyConstants.wxUserToken(userId);
        var refreshTokenObj = redisUtil.hGet(tokenKey, "refreshToken");
        if (refreshTokenObj != null) {
            redisUtil.delete(RedisKeyConstants.wxRefreshToken(String.valueOf(refreshTokenObj)));
        }
        redisUtil.delete(tokenKey, RedisKeyConstants.wxUserInfo(openId));
    }

    private String normalizeTokenForCompare(String token) {
        if (token == null) {
            return "";
        }
        return StrUtil.removeAll(token.trim(), "\"");
    }
}
