package t.uni.server.im.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import t.uni.common.core.exception.BaseException;
import t.uni.common.core.result.ResultCodeEnum;
import t.uni.server.im.ImResultCodeEnum;
import t.uni.server.im.client.OpenImApiClient;
import t.uni.server.im.config.ConditionalOnOpenImEnabled;
import t.uni.server.im.config.OpenImProperties;
import t.uni.server.im.persistence.OpenImCoreUserMapper;
import t.uni.server.im.support.OpenImApiResponse;
import t.uni.server.im.support.OpenImErrorMapper;
import t.uni.server.im.support.OpenImTokenResult;
import t.uni.server.im.vo.ImTokenVO;

/**
 * OpenIM Token 服务
 *
 * @author t-uni
 * @since 2026-04-24
 */
@Slf4j
@Service
@ConditionalOnOpenImEnabled
@RequiredArgsConstructor
public class OpenImTokenService {

    private final OpenImCoreUserMapper openImCoreUserMapper;
    private final OpenImApiClient openImApiClient;
    private final OpenImProperties openImProperties;
    private final OpenImUserSyncService openImUserSyncService;
    private final OpenImAdminTokenProvider openImAdminTokenProvider;

    public ImTokenVO getUserToken(Long userId, Integer platformId) {
        // 1. 参数校验
        if (userId == null || platformId == null) {
            throw new BaseException(ResultCodeEnum.PARAM_ERROR);
        }

        // 2. 查询用户信息
        var user = openImCoreUserMapper.selectById(userId);
        if (user == null) {
            throw new BaseException(ResultCodeEnum.USER_IS_EMPTY);
        }

        // 3. 构建 OpenIM 用户ID并获取管理员Token
        var openimUserId = openImProperties.buildImUserId(userId);
        var adminToken = openImAdminTokenProvider.getAdminToken();

        // 4. 获取用户Token
        var response = openImApiClient.getUserToken(adminToken, openimUserId, platformId);
        if (!response.isSuccess() && OpenImErrorMapper.isRecordNotFound(response.getErrCode())) {
            // 4.1 用户未导入则先注册
            openImUserSyncService.registerUser(user, openimUserId);
            // 4.2 注册可能正在进行，需等待并重试
            response = retryGetUserTokenUntilAvailable(adminToken, openimUserId, platformId);
        }
        if (!response.isSuccess()) {
            var resultCode = OpenImErrorMapper.map(response.getErrCode());
            throw new BaseException(resultCode.getCode(), "获取OpenIM user token失败: " + response.getErrMsg());
        }

        // 5. 解析Token结果
        var tokenResult = parseTokenResult(response);
        if (tokenResult == null || StrUtil.isBlank(tokenResult.getToken())) {
            throw new BaseException(ImResultCodeEnum.IM_OPENIM_USER_TOKEN_FAIL.getCode(),
                ImResultCodeEnum.IM_OPENIM_USER_TOKEN_FAIL.getMessage());
        }

        // 6. 标记用户已注册IM
        if (!Boolean.TRUE.equals(user.getImRegistered())) {
            openImUserSyncService.markRegistered(userId);
        }

        // 7. 构建并返回TokenVO
        return new ImTokenVO(
            openimUserId,
            tokenResult.getToken(),
            tokenResult.getExpireTimeSeconds(),
            platformId,
            openImProperties.getApiAddress(),
            openImProperties.getWsAddress()
        );
    }

    private OpenImTokenResult parseTokenResult(OpenImApiResponse response) {
        if (response == null || response.getData() == null) {
            return null;
        }
        var data = JSONUtil.parseObj(response.getData());
        var token = data.getStr("token");
        var expireTime = data.getLong("expireTimeSeconds");
        if (expireTime == null) {
            expireTime = data.getLong("expireTime");
        }
        return new OpenImTokenResult(token, expireTime);
    }

    private OpenImApiResponse retryGetUserTokenUntilAvailable(String adminToken, String openimUserId, Integer platformId) {
        var maxWaitMs = Math.max(200L, openImProperties.getUserRegisterLockSeconds() * 1000L);
        var start = System.currentTimeMillis();
        var attempt = 0;
        OpenImApiResponse response;
        while (true) {
            response = openImApiClient.getUserToken(adminToken, openimUserId, platformId);
            if (response.isSuccess() || !OpenImErrorMapper.isRecordNotFound(response.getErrCode())) {
                return response;
            }
            var sleepMs = calcRetrySleepMs(attempt++);
            if (System.currentTimeMillis() - start + sleepMs > maxWaitMs) {
                return response;
            }
            sleepQuietly(sleepMs);
        }
    }

    private long calcRetrySleepMs(int attempt) {
        var base = 100L;
        var cap = 1000L;
        var shift = Math.min(attempt, 4);
        return Math.min(cap, base * (1L << shift));
    }

    private void sleepQuietly(long sleepMs) {
        try {
            Thread.sleep(sleepMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
