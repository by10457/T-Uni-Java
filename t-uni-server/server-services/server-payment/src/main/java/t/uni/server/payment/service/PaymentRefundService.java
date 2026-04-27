package t.uni.server.payment.service;

import t.uni.server.payment.dto.PaymentRefundApplyDTO;
import t.uni.server.payment.vo.PaymentRefundVO;

import java.util.Map;

/**
 * 退款服务。
 *
 * <p>负责退款申请、退款查询和微信退款回调；退款成功或失败后的业务补偿由业务 handler 承担。</p>
 */
public interface PaymentRefundService {

    /**
     * 申请当前用户支付单退款。
     *
     * <p>先把支付单置为退款中锁住本地状态，再创建微信退款单，防止并发申请造成超退。</p>
     *
     * @param userId 当前登录用户ID
     * @param dto 退款金额和原因；金额为空时默认退剩余可退金额
     * @return 退款单状态
     */
    PaymentRefundVO applyRefund(Long userId, PaymentRefundApplyDTO dto);

    /**
     * 查询当前用户自己的退款单。
     *
     * @param userId 当前登录用户ID
     * @param outRefundNo 商户退款单号
     * @return 退款单状态
     */
    PaymentRefundVO queryRefund(Long userId, String outRefundNo);

    /**
     * 处理微信退款结果通知。
     *
     * <p>回调日志和退款状态更新在同一事务内完成，重复通知只返回成功，不重复累计已退金额。</p>
     *
     * @param headers 微信回调头，包含签名字段
     * @param body 微信回调原始报文
     * @return 微信 V3 回调应答 JSON
     */
    String handleRefundNotify(Map<String, String> headers, String body);
}
