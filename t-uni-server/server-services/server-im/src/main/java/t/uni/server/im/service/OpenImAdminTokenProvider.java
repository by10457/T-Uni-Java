package t.uni.server.im.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import t.uni.common.core.exception.BaseException;
import t.uni.server.im.ImResultCodeEnum;
import t.uni.server.im.client.OpenImApiClient;
import t.uni.server.im.config.ConditionalOnOpenImEnabled;
import t.uni.server.im.config.OpenImProperties;

import java.util.concurrent.locks.ReentrantLock;

/**
 * OpenIM admin token 管理（JVM 内存缓存）
 * <p>
 * 仅在 openim.enabled=true 时装配。缓存只在当前 JVM 内生效，集群节点各自刷新。
 * 获取失败会抛出业务异常，调用方不应使用空 token 继续访问 OpenIM。
 *
 * @author t-uni
 * @since 2026-04-24
 */
@Slf4j
@Service
@ConditionalOnOpenImEnabled
@RequiredArgsConstructor
public class OpenImAdminTokenProvider {

    private final OpenImApiClient openImApiClient;
    private final OpenImProperties openImProperties;

    private final ReentrantLock lock = new ReentrantLock();
    private volatile String cachedToken;
    private volatile long expireAtMs;

    /**
     * 获取可用的 OpenIM admin token。
     * <p>
     * 过期前按配置提前刷新；并发刷新通过本地锁收敛为一次外部请求。
     *
     * @return 当前可用 admin token
     */
    public String getAdminToken() {
        var now = System.currentTimeMillis();
        var refreshAheadMs = openImProperties.getAdminTokenRefreshAheadSeconds() * 1000L;
        if (StrUtil.isNotBlank(cachedToken) && now < expireAtMs - refreshAheadMs) {
            return cachedToken;
        }
        if (StrUtil.isBlank(openImProperties.getAdminUserId()) || StrUtil.isBlank(openImProperties.getAdminSecret())) {
            throw new BaseException(ImResultCodeEnum.IM_CONFIG_MISSING.getCode(),
                ImResultCodeEnum.IM_CONFIG_MISSING.getMessage());
        }
        lock.lock();
        try {
            now = System.currentTimeMillis();
            if (StrUtil.isNotBlank(cachedToken) && now < expireAtMs - refreshAheadMs) {
                return cachedToken;
            }
            // 双重检查后才访问 OpenIM，避免高并发下重复刷新 admin token。
            var response = openImApiClient.getAdminToken();
            if (!response.isSuccess()) {
                throw new BaseException(ImResultCodeEnum.IM_OPENIM_ADMIN_TOKEN_FAIL.getCode(),
                    "获取OpenIM admin token失败: " + response.getErrMsg());
            }
            var data = response.getData() == null ? null : JSONUtil.parseObj(response.getData());
            var token = data == null ? null : data.getStr("token");
            var expireSeconds = data == null ? null : data.getLong("expireTimeSeconds");
            if (StrUtil.isBlank(token) || expireSeconds == null) {
                throw new BaseException(ImResultCodeEnum.IM_OPENIM_ADMIN_TOKEN_FAIL.getCode(),
                    ImResultCodeEnum.IM_OPENIM_ADMIN_TOKEN_FAIL.getMessage());
            }
            cachedToken = token;
            expireAtMs = System.currentTimeMillis() + expireSeconds * 1000L;
            return cachedToken;
        } finally {
            lock.unlock();
        }
    }
}
