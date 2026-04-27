package t.uni.server.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import t.uni.server.payment.service.PaymentOrderService;
import t.uni.server.payment.service.PaymentRefundService;

import java.util.Map;

/**
 * 微信支付回调入口。
 *
 * <p>该 Controller 只转交原始请求头和报文，不在 Web 层解析报文，避免验签前改写内容。</p>
 */
@Tag(name = "微信支付回调", description = "微信支付和退款回调")
@RestController
@RequestMapping("/payment/notify/wechat")
@RequiredArgsConstructor
public class PaymentNotifyController {

    private final PaymentOrderService paymentOrderService;
    private final PaymentRefundService paymentRefundService;

    /**
     * 接收微信支付成功/失败通知。
     *
     * @param headers 微信签名头
     * @param body 原始通知报文
     * @return 微信 V3 回调应答 JSON
     */
    @Operation(summary = "微信支付回调")
    @PostMapping("/pay")
    public String payNotify(@RequestHeader Map<String, String> headers, @RequestBody String body) {
        return paymentOrderService.handlePayNotify(headers, body);
    }

    /**
     * 接收微信退款结果通知。
     *
     * @param headers 微信签名头
     * @param body 原始通知报文
     * @return 微信 V3 回调应答 JSON
     */
    @Operation(summary = "微信退款回调")
    @PostMapping("/refund")
    public String refundNotify(@RequestHeader Map<String, String> headers, @RequestBody String body) {
        return paymentRefundService.handleRefundNotify(headers, body);
    }
}
