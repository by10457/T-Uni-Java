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
 * <p>
 * 面向业务登录态签发 OpenIM user token。用户未同步到 OpenIM 时会先触发导入，
 * 再在短时间内重试 token 获取；最终失败统一映射为 IM 模块错误码。
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

    /**
     * 获取当前业务用户的 OpenIM user token。
     * <p>
     * 该方法不创建本地用户，只在 OpenIM 用户缺失时按需导入。导入后 token 可用才会更新本地
     * im_registered 标记，避免外部导入失败时提前标记成功。
     *
     * @param userId 本地用户 ID
     * @param platformId OpenIM 平台枚举值
     * @return 客户端连接 OpenIM 所需的 token 和公开地址
     */
    public ImTokenVO getUserToken(Long userId, Integer platformId) {
        if (userId == null || platformId == null) {
            throw new BaseException(ResultCodeEnum.PARAM_ERROR);
        }

        var user = openImCoreUserMapper.selectById(userId);
        if (user == null) {
            throw new BaseException(ResultCodeEnum.USER_IS_EMPTY);
        }

        var openimUserId = openImProperties.buildImUserId(userId);
        var adminToken = openImAdminTokenProvider.getAdminToken();

        var response = openImApiClient.getUserToken(adminToken, openimUserId, platformId);
        if (!response.isSuccess() && OpenImErrorMapper.isRecordNotFound(response.getErrCode())) {
            openImUserSyncService.registerUser(user, openimUserId);
            // OpenIM 导入存在短暂可见性延迟，注册后等待到短锁窗口结束前重试。
            response = retryGetUserTokenUntilAvailable(adminToken, openimUserId, platformId);
        }
        if (!response.isSuccess()) {
            var resultCode = OpenImErrorMapper.map(response.getErrCode());
            throw new BaseException(resultCode.getCode(), "获取OpenIM user token失败: " + response.getErrMsg());
        }

        var tokenResult = parseTokenResult(response);
        if (tokenResult == null || StrUtil.isBlank(tokenResult.getToken())) {
            throw new BaseException(ImResultCodeEnum.IM_OPENIM_USER_TOKEN_FAIL.getCode(),
                ImResultCodeEnum.IM_OPENIM_USER_TOKEN_FAIL.getMessage());
        }

        if (!Boolean.TRUE.equals(user.getImRegistered())) {
            openImUserSyncService.markRegistered(userId);
        }

        return new ImTokenVO(
            openimUserId,
            tokenResult.getToken(),
            tokenResult.getExpireTimeSeconds(),
            platformId,
            openImProperties.getApiAddress(),
            openImProperties.getWsAddress()
        );
    }

    /**
     * 解析 OpenIM token 数据。
     * <p>
     * 兼容不同 OpenIM 版本中 expireTimeSeconds 与 expireTime 的字段名差异。
     */
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

    /**
     * 用户刚导入后轮询获取 token。
     * <p>
     * 最长等待时间与用户注册短锁 TTL 对齐，避免接口线程长期阻塞。
     */
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

    /**
     * 指数退避间隔，限制单次等待上限。
     */
    private long calcRetrySleepMs(int attempt) {
        var base = 100L;
        var cap = 1000L;
        var shift = Math.min(attempt, 4);
        return Math.min(cap, base * (1L << shift));
    }

    /**
     * 保留中断标记，避免吞掉调用线程的取消信号。
     */
    private void sleepQuietly(long sleepMs) {
        try {
            Thread.sleep(sleepMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
