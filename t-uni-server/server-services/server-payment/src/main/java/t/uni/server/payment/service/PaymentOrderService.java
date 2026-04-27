package t.uni.server.payment.service;

import t.uni.server.payment.dto.PaymentLockDTO;
import t.uni.server.payment.dto.PaymentPrepayDTO;
import t.uni.server.payment.vo.PaymentOrderVO;
import t.uni.server.payment.vo.PaymentPrepayVO;

import java.util.Map;

/**
 * 支付单服务。
 *
 * <p>负责业务锁单、微信预下单、支付回调入账和超时关单；业务订单本身的创建、解锁和履约由
 * {@code PaymentBizHandler} 承担。</p>
 */
public interface PaymentOrderService {

    /**
     * 先让业务侧锁定一笔可支付订单，再创建或复用支付单并向微信预下单。
     *
     * <p>同一 {@code bizType + bizOrderNo} 只允许有一张支付单；重复请求返回已有单，避免重复扣减业务库存。</p>
     *
     * @param userId 当前登录用户ID
     * @param dto 锁单参数，业务内容由对应 handler 解释
     * @return 支付单和小程序调起支付参数；已终态订单不会返回支付参数
     */
    PaymentPrepayVO lockAndPrepay(Long userId, PaymentLockDTO dto);

    /**
     * 对仍可支付的支付单重新生成小程序支付参数。
     *
     * @param userId 当前登录用户ID
     * @param dto 支付单号
     * @return 支付单和支付参数
     */
    PaymentPrepayVO reprepay(Long userId, PaymentPrepayDTO dto);

    /**
     * 查询当前用户自己的支付单。
     *
     * @param userId 当前登录用户ID
     * @param orderNo 支付单号
     * @return 支付单状态视图
     */
    PaymentOrderVO queryOrder(Long userId, String orderNo);

    /**
     * 处理微信支付结果通知。
     *
     * <p>实现必须先验签再入库回调日志，回调日志唯一键用于抵御微信重试导致的重复通知。</p>
     *
     * @param headers 微信回调头，包含签名字段
     * @param body 微信回调原始报文
     * @return 微信 V3 回调应答 JSON
     */
    String handlePayNotify(Map<String, String> headers, String body);

    /**
     * 扫描并关闭已过期的未支付订单。
     *
     * <p>关闭前必须先查微信订单状态，避免本地超时但微信已支付时误关单。</p>
     */
    void closeExpiredOrders();
}
