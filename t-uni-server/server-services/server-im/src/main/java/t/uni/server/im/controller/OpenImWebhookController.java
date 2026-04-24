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
 * 模板级实现：验签后统一放行，不做内容审核和隐私策略。
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
