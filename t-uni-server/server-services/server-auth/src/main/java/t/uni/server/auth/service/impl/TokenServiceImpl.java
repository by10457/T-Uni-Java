package t.uni.server.auth.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import t.uni.common.core.exception.BaseException;
import t.uni.common.core.redis.RedisUtil;
import t.uni.common.core.result.ResultCodeEnum;
import t.uni.server.common.auth.TokenService;
import t.uni.server.common.utils.ServerJwtTokenUtil;
import t.uni.server.domain.constant.AuthConstant;
import t.uni.server.domain.vo.auth.TokenVO;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Token 服务实现
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
        // 1. 生成 Access Token（JWT）
        var claims = new HashMap<String, Object>();
        claims.put(AuthConstant.CLAIM_USER_ID, userId);
        claims.put(AuthConstant.CLAIM_OPEN_ID, openId);

        var accessTokenExpireTime = LocalDateTime.now().plusHours(AuthConstant.ACCESS_TOKEN_EXPIRE_HOURS);
        var accessToken = ServerJwtTokenUtil.createTokenWithMap(
                claims,
                Date.from(accessTokenExpireTime.atZone(ZoneId.systemDefault()).toInstant()));

        // 2. 生成 Refresh Token（UUID）
        var refreshToken = UUID.randomUUID().toString().replace("-", "");
        var refreshTokenExpireTime = LocalDateTime.now().plusDays(AuthConstant.REFRESH_TOKEN_EXPIRE_DAYS);

        // 3. 存储 Refresh Token 到 Redis
        var redisKey = AuthConstant.REDIS_KEY_REFRESH_TOKEN + userId;
        var tokenMeta = new HashMap<String, Object>();
        tokenMeta.put(AuthConstant.REFRESH_TOKEN_FIELD_TOKEN, refreshToken);
        if (openId != null && !openId.isBlank()) {
            tokenMeta.put(AuthConstant.REFRESH_TOKEN_FIELD_OPEN_ID, openId);
        }
        redisUtil.hSetAll(redisKey, tokenMeta, TimeUnit.DAYS.toSeconds(AuthConstant.REFRESH_TOKEN_EXPIRE_DAYS));

        var tokenIndexKey = AuthConstant.REDIS_KEY_REFRESH_TOKEN_INDEX + refreshToken;
        redisUtil.set(tokenIndexKey, String.valueOf(userId), AuthConstant.REFRESH_TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);

        log.info("为用户 {} 生成双Token成功", userId);

        return TokenVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpireTime(accessTokenExpireTime)
                .refreshTokenExpireTime(refreshTokenExpireTime)
                .build();
    }

    /**
     * 刷新 Token
     */
    @Override
    public TokenVO refreshTokens(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BaseException(ResultCodeEnum.TOKEN_PARSING_FAILED.getCode(), "刷新令牌不能为空");
        }

        // 1. 通过反向索引定位 userId
        var tokenIndexKey = AuthConstant.REDIS_KEY_REFRESH_TOKEN_INDEX + refreshToken;
        var userIdValue = redisUtil.get(tokenIndexKey, String.class);
        if (userIdValue == null || userIdValue.isBlank()) {
            throw new BaseException(ResultCodeEnum.TOKEN_EXPIRED.getCode(), "刷新令牌已失效或不存在");
        }
        var userId = Long.valueOf(userIdValue);

        // 2. 校验 Refresh Token 与用户缓存一致
        var redisKey = AuthConstant.REDIS_KEY_REFRESH_TOKEN + userId;
        var storedToken = redisUtil.hGet(redisKey, AuthConstant.REFRESH_TOKEN_FIELD_TOKEN);
        if (storedToken == null || !refreshToken.equals(String.valueOf(storedToken))) {
            throw new BaseException(ResultCodeEnum.TOKEN_EXPIRED.getCode(), "刷新令牌已失效或不存在");
        }

        var openIdObj = redisUtil.hGet(redisKey, AuthConstant.REFRESH_TOKEN_FIELD_OPEN_ID);
        var openId = openIdObj != null ? String.valueOf(openIdObj) : null;

        // 3. 删除旧的 Refresh Token
        redisUtil.delete(tokenIndexKey);
        redisUtil.delete(redisKey);

        // 3. 生成新的双 Token
        log.info("用户 {} 刷新Token", userId);
        return generateTokens(userId, openId);
    }

    /**
     * 验证 Access Token 并获取用户ID
     */
    @Override
    public Long validateAndGetUserId(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new BaseException(ResultCodeEnum.TOKEN_PARSING_FAILED.getCode(), "访问令牌不能为空");
        }

        // 验证并解析 Token
        if (!ServerJwtTokenUtil.isValid(accessToken) || ServerJwtTokenUtil.isExpired(accessToken)) {
            throw new BaseException(ResultCodeEnum.TOKEN_EXPIRED.getCode(), "访问令牌已过期或无效");
        }

        return ServerJwtTokenUtil.getUserId(accessToken);
    }

    /**
     * 使 Refresh Token 失效
     */
    @Override
    public void invalidateRefreshToken(Long userId) {
        var redisKey = AuthConstant.REDIS_KEY_REFRESH_TOKEN + userId;
        var storedToken = redisUtil.hGet(redisKey, AuthConstant.REFRESH_TOKEN_FIELD_TOKEN);
        if (storedToken != null) {
            redisUtil.delete(AuthConstant.REDIS_KEY_REFRESH_TOKEN_INDEX + storedToken);
        }
        redisUtil.delete(redisKey);
        log.info("用户 {} 的刷新令牌已失效", userId);
    }
}
