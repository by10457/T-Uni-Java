package t.uni.server.im.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import t.uni.server.im.config.ConditionalOnOpenImEnabled;
import t.uni.server.im.service.WebhookSecurityService;
import t.uni.server.im.vo.OpenImWebhookResponse;

/**
 * OpenIM Webhook 回调（通用 catch-all）
 * <p>
 * 只做共享 token 鉴权，验签通过后统一放行。
 * 当前不做消息内容审核、隐私策略处理或业务事件分发。
 *
 * @author t-uni
 * @since 2026-04-24
 */
@Slf4j
@RestController
@ConditionalOnOpenImEnabled
@RequestMapping("/openim/webhook")
@RequiredArgsConstructor
public class OpenImWebhookController {

    private final WebhookSecurityService webhookSecurityService;

    /**
     * 处理 OpenIM 任意命令回调。
     * <p>
     * 鉴权失败返回 OpenIM 可识别的拦截响应；通过后不解析 body，保持模板默认放行策略。
     *
     * @param command OpenIM 回调命令
     * @param token 回调 URL 中的共享 token
     * @param body OpenIM 原始回调体
     * @return OpenIM Webhook 响应
     */
    @PostMapping("/{command}")
    public OpenImWebhookResponse handleCallback(
            @PathVariable String command,
            @RequestParam(value = "token", required = false) String token,
            @RequestBody String body) {
        if (webhookSecurityService.isUnauthorized(token)) {
            log.warn("Webhook鉴权失败: command={}", command);
            return OpenImWebhookResponse.reject(5206, "鉴权失败", "token invalid");
        }
        log.debug("Webhook放行: command={}", command);
        return OpenImWebhookResponse.allow();
    }
}
