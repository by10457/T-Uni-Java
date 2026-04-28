package t.uni.server.auth.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import t.uni.common.config.jwt.JwtTokenUtil;
import t.uni.common.config.redis.RedisUtil;
import t.uni.common.core.exception.BaseException;
import t.uni.common.core.result.ResultCodeEnum;
import t.uni.common.core.utils.MaskUtil;
import t.uni.server.auth.mapper.CoreUserMapper;
import t.uni.server.common.auth.TokenService;
import t.uni.server.domain.constant.AuthConstant;
import t.uni.server.domain.constant.ServerRedisKeys;
import t.uni.server.domain.vo.auth.TokenVO;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Token 服务实现。
 * <p>
 * 维护微信登录的双 Token 和 Redis 缓存：
 * AccessToken 是短期 JWT，RefreshToken 是长期随机串。
 * Redis 中同时保存 userId -> Token Hash 与 refreshToken -> userId 反向索引，
 * openId -> userId 的登录缓存由 cacheUserInfo 维护。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private static final int CACHE_SCHEMA_VERSION = 1;
    private static final String TOKEN_CACHE_TYPE = "TUNI_WX_AUTH_TOKEN";
    private static final String USER_INFO_CACHE_TYPE = "TUNI_WX_USER_INFO";
    private static final long CACHE_TTL_BUFFER_SECONDS = 60L;

    private final RedisUtil redisUtil;
    private final CoreUserMapper coreUserMapper;

    /**
     * 为用户生成新的 AccessToken 与 RefreshToken。
     * <p>
     * 会覆盖该用户现有 Token Hash，并建立新的 refreshToken 反向索引。
     * </p>
     *
     * @param userId 核心用户 ID
     * @param openId 微信小程序 openId
     * @return 双 Token 与有效期秒数
     */
    @Override
    public TokenVO generateTokens(Long userId, String openId) {
        var accessToken = JwtTokenUtil.createTokenWithHours(userId, AuthConstant.ACCESS_TOKEN_EXPIRE_HOURS);

        var refreshToken = UUID.randomUUID().toString().replace("-", "");

        long accessExpiresIn = TimeUnit.HOURS.toSeconds(AuthConstant.ACCESS_TOKEN_EXPIRE_HOURS);
        long refreshExpiresIn = TimeUnit.DAYS.toSeconds(AuthConstant.REFRESH_TOKEN_EXPIRE_DAYS);

        var tokenVO = TokenVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessExpiresIn(accessExpiresIn)
                .refreshExpiresIn(refreshExpiresIn)
                .build();

        cacheToken(userId, tokenVO, openId);

        log.info("为用户 {} 生成双Token成功，AccessToken {}小时，RefreshToken {}天",
                userId, AuthConstant.ACCESS_TOKEN_EXPIRE_HOURS, AuthConstant.REFRESH_TOKEN_EXPIRE_DAYS);
        return tokenVO;
    }

    /**
     * 使用 RefreshToken 换发双 Token。
     * <p>
     * 刷新时先通过反向索引定位用户，再校验用户状态和 Token Hash 中的 RefreshToken。
     * 新 RefreshToken 继承旧 RefreshToken 剩余有效期。
     * </p>
     *
     * @param refreshToken 客户端提交的 RefreshToken
     * @return 换发后的双 Token
     */
    @Override
    public TokenVO refreshTokens(String refreshToken) {
        if (StrUtil.isBlank(refreshToken)) {
            throw new BaseException(ResultCodeEnum.REFRESH_TOKEN_EMPTY);
        }

        var indexKey = ServerRedisKeys.wxRefreshToken(refreshToken);
        var userIdStr = redisUtil.get(indexKey, String.class);

        if (StrUtil.isBlank(userIdStr)) {
            throw new BaseException(ResultCodeEnum.REFRESH_TOKEN_INVALID);
        }

        var userId = parseLongOrNull(userIdStr);
        if (userId == null) {
            redisUtil.delete(indexKey);
            throw new BaseException(ResultCodeEnum.REFRESH_TOKEN_INVALID);
        }

        validateUserStatus(userId);

        var tokenKey = ServerRedisKeys.wxUserToken(userId);
        var tokenDataRaw = redisUtil.hGetAll(tokenKey);

        if (!isExpectedTokenCache(tokenDataRaw, userId)) {
            throw new BaseException(ResultCodeEnum.REFRESH_TOKEN_INVALID);
        }

        var storedRefreshToken = stringValue(tokenDataRaw.get("refreshToken"));
        if (!refreshToken.equals(storedRefreshToken)) {
            throw new BaseException(ResultCodeEnum.REFRESH_TOKEN_INVALID);
        }

        var refreshExpireTimeMs = parseLongOrNull(tokenDataRaw.get("refreshExpireTimeMs"));
        if (refreshExpireTimeMs == null) {
            throw new BaseException(ResultCodeEnum.REFRESH_TOKEN_INVALID);
        }
        long refreshRemainingMs = refreshExpireTimeMs - System.currentTimeMillis();

        if (refreshRemainingMs <= 0) {
            throw new BaseException(ResultCodeEnum.REFRESH_TOKEN_EXPIRED);
        }

        long refreshRemainingSeconds = Math.max(1L, refreshRemainingMs / 1000);
        var openId = stringValue(tokenDataRaw.get("openId"));
        if (StrUtil.isBlank(openId)) {
            throw new BaseException(ResultCodeEnum.REFRESH_TOKEN_INVALID);
        }

        redisUtil.delete(indexKey);

        return generateTokensWithTtl(userId, openId, refreshRemainingSeconds);
    }

    /**
     * 校验 AccessToken 并返回用户 ID。
     * <p>
     * 除 JWT 自身合法性外，还要求与 Redis 中当前 accessToken 一致，用于实现服务端主动失效。
     * </p>
     *
     * @param accessToken 客户端提交的 AccessToken
     * @return Token 归属的用户 ID
     */
    @Override
    public Long validateAndGetUserId(String accessToken) {
        if (StrUtil.isBlank(accessToken)) {
            throw new BaseException(ResultCodeEnum.ACCESS_TOKEN_EMPTY);
        }

        if (!JwtTokenUtil.isValidAndNotExpired(accessToken)) {
            throw new BaseException(ResultCodeEnum.ACCESS_TOKEN_INVALID);
        }

        var userId = JwtTokenUtil.getUserId(accessToken);
        var tokenDataRaw = redisUtil.hGetAll(ServerRedisKeys.wxUserToken(userId));
        if (!isExpectedTokenCache(tokenDataRaw, userId)) {
            throw new BaseException(ResultCodeEnum.ACCESS_TOKEN_INVALID);
        }

        var accessExpireTimeMs = parseLongOrNull(tokenDataRaw.get("accessExpireTimeMs"));
        if (accessExpireTimeMs == null || accessExpireTimeMs <= System.currentTimeMillis()) {
            throw new BaseException(ResultCodeEnum.ACCESS_TOKEN_INVALID);
        }

        var requestToken = normalizeTokenForCompare(accessToken);
        var currentToken = normalizeTokenForCompare(stringValue(tokenDataRaw.get("accessToken")));
        if (StrUtil.isBlank(currentToken) || !requestToken.equals(currentToken)) {
            throw new BaseException(ResultCodeEnum.ACCESS_TOKEN_INVALID);
        }
        return userId;
    }

    /**
     * 使指定用户当前登录 Token 失效。
     * <p>
     * 同时删除 userId -> Token Hash 和 refreshToken -> userId 反向索引。
     * </p>
     *
     * @param userId 核心用户 ID
     */
    @Override
    public void invalidateToken(Long userId) {
        var tokenKey = ServerRedisKeys.wxUserToken(userId);

        var refreshTokenObj = redisUtil.hGet(tokenKey, "refreshToken");
        if (refreshTokenObj != null) {
            var refreshToken = String.valueOf(refreshTokenObj);
            redisUtil.delete(ServerRedisKeys.wxRefreshToken(refreshToken));
        }

        redisUtil.delete(tokenKey);
        log.info("用户 {} 的Token已失效", userId);
    }

    /**
     * 获取 openId 对应的可用缓存 Token。
     * <p>
     * 会检查用户状态、Token Hash 完整性和绝对过期时间；缓存不可用时清理并返回 null，
     * 由登录流程回落到数据库双表校验。
     * </p>
     *
     * @param openId 微信小程序 openId
     * @return 可复用 Token；缓存缺失或无效时返回 null
     */
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

        var tokenDataRaw = redisUtil.hGetAll(ServerRedisKeys.wxUserToken(userId));

        if (!isExpectedTokenCache(tokenDataRaw, userId) || !openId.equals(stringValue(tokenDataRaw.get("openId")))) {
            clearLoginCache(openId, userId);
            return null;
        }

        var refreshExpireTimeMs = parseLongOrNull(tokenDataRaw.get("refreshExpireTimeMs"));
        if (refreshExpireTimeMs == null) {
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

        var accessExpireTimeMs = parseLongOrNull(tokenDataRaw.get("accessExpireTimeMs"));
        if (accessExpireTimeMs == null) {
            clearLoginCache(openId, userId);
            return null;
        }
        long accessRemainingMs = accessExpireTimeMs - currentTimeMs;

        boolean refreshNeedsRenew = refreshRemainingMs < ServerRedisKeys.TOKEN_REFRESH_THRESHOLD_MS;
        boolean accessExpired = accessRemainingMs <= 0;

        if (refreshNeedsRenew || accessExpired) {
            if (accessExpired) {
                log.info("用户 {} 的AccessToken已过期，重新生成双Token", userId);
            } else {
                log.info("用户 {} 的RefreshToken即将过期（剩余 {} ms），自动续期", userId, refreshRemainingMs);
            }
            updateLastLoginTime(userId);
            return generateTokens(userId, openId);
        }

        var accessToken = stringValue(tokenDataRaw.get("accessToken"));
        if (StrUtil.isBlank(accessToken)) {
            log.warn("用户 {} 缓存 accessToken 为空，重新生成双Token", userId);
            updateLastLoginTime(userId);
            return generateTokens(userId, openId);
        }
        if (!JwtTokenUtil.isValidAndNotExpired(accessToken) || !userId.equals(JwtTokenUtil.getUserId(accessToken))) {
            log.warn("用户 {} 缓存 accessToken 在当前服务下无效，重新生成双Token", userId);
            updateLastLoginTime(userId);
            return generateTokens(userId, openId);
        }

        long accessExpiresIn = TimeUnit.HOURS.toSeconds(AuthConstant.ACCESS_TOKEN_EXPIRE_HOURS);
        long refreshExpiresIn = TimeUnit.DAYS.toSeconds(AuthConstant.REFRESH_TOKEN_EXPIRE_DAYS);

        log.info("用户 {} 的Token仍然有效，AccessToken剩余 {} 秒，RefreshToken剩余 {} 秒",
                userId, accessRemainingMs / 1000, refreshRemainingMs / 1000);

        return TokenVO.builder()
                .accessToken(accessToken)
                .refreshToken(stringValue(tokenDataRaw.get("refreshToken")))
                .accessExpiresIn(accessExpiresIn)
                .refreshExpiresIn(refreshExpiresIn)
                .build();
    }

    /**
     * 缓存微信登录身份到用户 ID 的映射。
     * <p>
     * 登录入口用该缓存从 openId 快速定位 userId，再读取 Token Hash。
     * </p>
     *
     * @param openId  微信小程序 openId
     * @param userId  核心用户 ID
     * @param unionId 微信开放平台 unionId，可能为空
     */
    @Override
    public void cacheUserInfo(String openId, Long userId, String unionId) {
        var key = ServerRedisKeys.wxUserInfo(openId);

        var userInfo = new HashMap<String, Object>();
        userInfo.put("schemaVersion", CACHE_SCHEMA_VERSION);
        userInfo.put("cacheType", USER_INFO_CACHE_TYPE);
        userInfo.put("userId", userId);
        userInfo.put("openId", openId);
        if (StrUtil.isNotBlank(unionId)) {
            userInfo.put("unionId", unionId);
        }

        var saved = redisUtil.hSetAll(key, userInfo, fullRefreshCacheTtlSeconds());
        if (!saved || !isExpectedUserInfoCache(redisUtil.hGetAll(key), openId, userId)) {
            redisUtil.delete(key);
            throw cacheWriteException("微信用户登录缓存写入失败", userId, openId, key);
        }
    }

    /**
     * 从 openId 登录缓存中读取用户 ID。
     *
     * @param openId 微信 openId
     * @return 已缓存的用户 ID；未登录或缓存缺失时返回 null
     */
    @Override
    public Long getCachedUserId(String openId) {
        var key = ServerRedisKeys.wxUserInfo(openId);
        var userInfoRaw = redisUtil.hGetAll(key);
        var userId = parseLongOrNull(userInfoRaw.get("userId"));

        if (!isExpectedUserInfoCache(userInfoRaw, openId, userId)) {
            if (CollUtil.isNotEmpty(userInfoRaw)) {
                redisUtil.delete(key);
            }
            return null;
        }

        return userId;
    }

    // ==================== 私有方法 ====================

    /**
     * 校验用户状态。
     * <p>
     * 刷新 Token 前必须确认用户仍存在且未禁用、未注销。
     * </p>
     */
    private void validateUserStatus(Long userId) {
        var coreUser = coreUserMapper.selectById(userId);
        if (coreUser == null) {
            log.warn("刷新Token失败：用户不存在，userId: {}", userId);
            throw new BaseException(ResultCodeEnum.USER_IS_EMPTY);
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
     * 更新最后登录时间。
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
     * 缓存 Token 信息到 Redis Hash。
     * <p>
     * Hash 保存绝对过期时间，反向索引用于 RefreshToken 快速定位用户。
     * </p>
     */
    private void cacheToken(Long userId, TokenVO tokenVO, String openId) {
        cacheTokenWithTtl(userId, tokenVO, openId, TimeUnit.DAYS.toSeconds(AuthConstant.REFRESH_TOKEN_EXPIRE_DAYS));
    }

    /**
     * 使用指定剩余时间生成双 Token。
     * <p>
     * 用于刷新 Token：AccessToken 重新给完整有效期，RefreshToken 沿用旧剩余时间。
     * </p>
     */
    private TokenVO generateTokensWithTtl(Long userId, String openId, long refreshRemainingSeconds) {
        var accessToken = JwtTokenUtil.createTokenWithHours(userId, AuthConstant.ACCESS_TOKEN_EXPIRE_HOURS);

        var refreshToken = UUID.randomUUID().toString().replace("-", "");

        long accessExpiresIn = TimeUnit.HOURS.toSeconds(AuthConstant.ACCESS_TOKEN_EXPIRE_HOURS);

        var tokenVO = TokenVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessExpiresIn(accessExpiresIn)
                .refreshExpiresIn(refreshRemainingSeconds)
                .build();

        cacheTokenWithTtl(userId, tokenVO, openId, refreshRemainingSeconds);

        log.info("用户 {} 刷新Token成功，AccessToken {}小时，RefreshToken剩余 {} 秒",
                userId, AuthConstant.ACCESS_TOKEN_EXPIRE_HOURS, refreshRemainingSeconds);
        return tokenVO;
    }

    /**
     * 使用指定剩余时间缓存 Token。
     */
    private void cacheTokenWithTtl(Long userId, TokenVO tokenVO, String openId, long refreshRemainingSeconds) {
        var tokenKey = ServerRedisKeys.wxUserToken(userId);
        var indexKey = ServerRedisKeys.wxRefreshToken(tokenVO.getRefreshToken());

        long currentTimeMs = System.currentTimeMillis();
        long accessExpireTimeMs = currentTimeMs + AuthConstant.ACCESS_TOKEN_EXPIRE_MS;
        long refreshExpireTimeMs = currentTimeMs + refreshRemainingSeconds * 1000;
        long cacheTtlSeconds = refreshRemainingSeconds + CACHE_TTL_BUFFER_SECONDS;

        var tokenData = new HashMap<String, Object>();
        tokenData.put("schemaVersion", CACHE_SCHEMA_VERSION);
        tokenData.put("cacheType", TOKEN_CACHE_TYPE);
        tokenData.put("userId", userId);
        tokenData.put("openId", openId);
        tokenData.put("accessToken", tokenVO.getAccessToken());
        tokenData.put("refreshToken", tokenVO.getRefreshToken());
        tokenData.put("accessExpireTimeMs", accessExpireTimeMs);
        tokenData.put("refreshExpireTimeMs", refreshExpireTimeMs);

        var tokenSaved = redisUtil.hSetAll(tokenKey, tokenData, cacheTtlSeconds);
        var indexSaved = redisUtil.set(indexKey, String.valueOf(userId), cacheTtlSeconds);
        if (!tokenSaved || !indexSaved || !isTokenCacheWritten(userId, openId, tokenVO, tokenKey, indexKey)) {
            redisUtil.delete(tokenKey, indexKey);
            throw cacheWriteException("Token缓存写入失败", userId, openId, tokenKey);
        }
    }

    /**
     * 判断缓存中的用户是否仍可登录。
     */
    private boolean isUserAvailable(Long userId) {
        var coreUser = coreUserMapper.selectById(userId);
        if (coreUser == null) {
            return false;
        }
        return !Integer.valueOf(1).equals(coreUser.getIsDestroy())
                && !Integer.valueOf(1).equals(coreUser.getIsDisable());
    }

    /**
     * 清理 openId 登录缓存、用户 Token Hash 和 RefreshToken 反向索引。
     */
    private void clearLoginCache(String openId, Long userId) {
        if (userId == null) {
            redisUtil.delete(ServerRedisKeys.wxUserInfo(openId));
            return;
        }
        var tokenKey = ServerRedisKeys.wxUserToken(userId);
        var refreshTokenObj = redisUtil.hGet(tokenKey, "refreshToken");
        if (refreshTokenObj != null) {
            redisUtil.delete(ServerRedisKeys.wxRefreshToken(String.valueOf(refreshTokenObj)));
        }
        redisUtil.delete(tokenKey, ServerRedisKeys.wxUserInfo(openId));
    }

    /**
     * 兼容 Redis 序列化可能带来的字符串引号差异，仅用于 Token 等值比较。
     */
    private String normalizeTokenForCompare(String token) {
        if (token == null) {
            return "";
        }
        return StrUtil.removeAll(token.trim(), "\"");
    }

    private boolean isTokenCacheWritten(Long userId, String openId, TokenVO tokenVO, String tokenKey, String indexKey) {
        var tokenDataRaw = redisUtil.hGetAll(tokenKey);
        var indexedUserId = redisUtil.get(indexKey, String.class);
        return isExpectedTokenCache(tokenDataRaw, userId)
                && openId.equals(stringValue(tokenDataRaw.get("openId")))
                && tokenVO.getAccessToken().equals(stringValue(tokenDataRaw.get("accessToken")))
                && tokenVO.getRefreshToken().equals(stringValue(tokenDataRaw.get("refreshToken")))
                && String.valueOf(userId).equals(indexedUserId)
                && redisUtil.getExpire(tokenKey) > 0
                && redisUtil.getExpire(indexKey) > 0;
    }

    private boolean isExpectedTokenCache(Map<Object, Object> tokenDataRaw, Long userId) {
        if (CollUtil.isEmpty(tokenDataRaw) || userId == null) {
            return false;
        }
        return TOKEN_CACHE_TYPE.equals(stringValue(tokenDataRaw.get("cacheType")))
                && Integer.valueOf(CACHE_SCHEMA_VERSION).equals(parseIntegerOrNull(tokenDataRaw.get("schemaVersion")))
                && userId.equals(parseLongOrNull(tokenDataRaw.get("userId")));
    }

    private boolean isExpectedUserInfoCache(Map<Object, Object> userInfoRaw, String openId, Long userId) {
        if (CollUtil.isEmpty(userInfoRaw) || StrUtil.isBlank(openId) || userId == null) {
            return false;
        }
        return USER_INFO_CACHE_TYPE.equals(stringValue(userInfoRaw.get("cacheType")))
                && Integer.valueOf(CACHE_SCHEMA_VERSION).equals(parseIntegerOrNull(userInfoRaw.get("schemaVersion")))
                && openId.equals(stringValue(userInfoRaw.get("openId")))
                && userId.equals(parseLongOrNull(userInfoRaw.get("userId")));
    }

    private long fullRefreshCacheTtlSeconds() {
        return TimeUnit.DAYS.toSeconds(AuthConstant.REFRESH_TOKEN_EXPIRE_DAYS) + CACHE_TTL_BUFFER_SECONDS;
    }

    private BaseException cacheWriteException(String message, Long userId, String openId, String key) {
        log.error("{}，userId={}, openId={}, key={}", message, userId, MaskUtil.maskOpenId(openId), key);
        return new BaseException(ResultCodeEnum.SERVICE_ERROR.getCode(), "登录态缓存写入失败，请稍后重试");
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Long parseLongOrNull(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseIntegerOrNull(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
