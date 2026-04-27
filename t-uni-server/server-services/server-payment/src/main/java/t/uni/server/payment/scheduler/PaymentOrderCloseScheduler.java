package t.uni.server.payment.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import t.uni.server.payment.config.WechatPayProperties;
import t.uni.server.payment.service.PaymentOrderService;

/**
 * 超时未支付订单自动关单任务。
 *
 * <p>只负责触发扫描，具体关单前查微信状态由支付服务完成，避免定时任务直接误关已支付订单。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentOrderCloseScheduler {

    private final WechatPayProperties properties;
    private final PaymentOrderService paymentOrderService;

    /**
     * 周期扫描过期支付单。
     *
     * <p>微信支付未启用时跳过，方便本地或模板环境无证书启动。</p>
     */
    @Scheduled(fixedDelay = 60_000L)
    public void closeExpiredOrders() {
        if (!properties.isEnabled()) {
            return;
        }
        paymentOrderService.closeExpiredOrders();
    }
}
