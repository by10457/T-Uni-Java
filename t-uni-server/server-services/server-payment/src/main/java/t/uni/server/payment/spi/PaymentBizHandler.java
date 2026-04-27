package t.uni.server.payment.spi;

import t.uni.server.payment.model.PaymentBizLockRequest;
import t.uni.server.payment.model.PaymentBizLockedOrder;
import t.uni.server.payment.model.PaymentClosedEvent;
import t.uni.server.payment.model.PaymentPaidEvent;
import t.uni.server.payment.model.PaymentRefundEvent;

/**
 * 支付业务扩展点。
 *
 * <p>支付模块只维护支付单、退款单和微信交互；业务订单的锁定、履约、关闭和退款补偿都通过该 SPI
 * 回调给业务模块。实现方需要保证回调幂等，因为微信通知和自动查单可能重复触发同一业务事件。</p>
 */
public interface PaymentBizHandler {

    /**
     * 该处理器负责的业务类型，如 AI_PAPER、VIP、COURSE。
     */
    String getBizType();

    /**
     * 业务锁单：校验参数、创建或复用业务订单，并返回支付金额等信息。
     *
     * <p>实现方应以业务订单维度做幂等，重复锁同一业务订单时返回同一 {@code bizOrderNo}，不要重复扣库存或发放权益。</p>
     */
    PaymentBizLockedOrder lock(PaymentBizLockRequest request);

    /**
     * 支付成功后的业务处理。
     *
     * <p>可能由支付回调或过期查单兜底触发；实现方应按 {@code orderNo} 或业务订单号防重复履约。</p>
     */
    void onPaid(PaymentPaidEvent event);

    /**
     * 超时关单后的业务处理。
     *
     * <p>只在支付模块确认微信订单未支付或已关闭后触发，用于释放业务锁定资源。</p>
     */
    void onClosed(PaymentClosedEvent event);

    /**
     * 退款成功后的业务处理。
     *
     * <p>同一退款单只应补偿一次，部分退款时不能按全额退款处理。</p>
     */
    void onRefundSuccess(PaymentRefundEvent event);

    /**
     * 退款失败、异常或关闭后的业务处理。
     *
     * <p>支付模块会先恢复本地支付单可退款状态，再通知业务侧撤销退款中的占位或提示。</p>
     */
    void onRefundFailed(PaymentRefundEvent event);
}
