package t.uni.server.im.service;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import t.uni.server.im.config.ConditionalOnOpenImEnabled;
import t.uni.server.im.config.OpenImProperties;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * OpenIM Webhook 鉴权服务（常数时间比较，防止时序攻击）
 * <p>
 * 当前只校验 URL token，不解析回调业务内容，也不承担内容审核。
 *
 * @author t-uni
 * @since 2026-04-24
 */
@Service
@ConditionalOnOpenImEnabled
@RequiredArgsConstructor
public class WebhookSecurityService {

    private final OpenImProperties openImProperties;

    /**
     * 判断 Webhook 请求是否未授权。
     * <p>
     * 服务端未配置共享密钥或请求未携带 token 时均拒绝，避免误放行回调入口。
     *
     * @param token OpenIM 回调 URL 中携带的共享 token
     *
     * @return true 表示鉴权失败，应拒绝请求
     */
    public boolean isUnauthorized(String token) {
        var expected = openImProperties.getWebhook().getToken();
        if (StrUtil.isBlank(expected) || StrUtil.isBlank(token)) {
            return true;
        }
        return !MessageDigest.isEqual(
            expected.getBytes(StandardCharsets.UTF_8),
            token.getBytes(StandardCharsets.UTF_8));
    }
}
