package t.uni.server.payment.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyV3Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import t.uni.common.core.exception.BaseException;
import t.uni.common.core.result.ResultCodeEnum;
import t.uni.server.payment.client.WechatPayClient;
import t.uni.server.payment.constant.PaymentConstants;
import t.uni.server.payment.constant.PaymentResultCodeEnum;
import t.uni.server.payment.dto.PaymentRefundApplyDTO;
import t.uni.server.payment.entity.CorePaymentNotifyLog;
import t.uni.server.payment.entity.CorePaymentOrder;
import t.uni.server.payment.entity.CorePaymentRefund;
import t.uni.server.payment.mapper.CorePaymentNotifyLogMapper;
import t.uni.server.payment.mapper.CorePaymentOrderMapper;
import t.uni.server.payment.mapper.CorePaymentRefundMapper;
import t.uni.server.payment.model.PaymentRefundEvent;
import t.uni.server.payment.service.PaymentRefundService;
import t.uni.server.payment.spi.PaymentBizHandlerRegistry;
import t.uni.server.payment.vo.PaymentRefundVO;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 退款核心服务。
 *
 * <p>维护退款单状态和支付单已退金额。退款申请通过支付单状态锁住并发入口，退款成功回调只允许从处理中状态推进，
 * 防止重复通知或并发申请造成超退。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRefundServiceImpl implements PaymentRefundService {

    private static final DateTimeFormatter NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final SecureRandom RANDOM = new SecureRandom();

    private final CorePaymentOrderMapper orderMapper;
    private final CorePaymentRefundMapper refundMapper;
    private final CorePaymentNotifyLogMapper notifyLogMapper;
    private final WechatPayClient wechatPayClient;
    private final PaymentBizHandlerRegistry handlerRegistry;
    private final TransactionTemplate transactionTemplate;

    /**
     * 申请微信退款。
     *
     * <p>先校验剩余可退金额，再把支付单从 PAID/PARTIAL_REFUNDED 条件更新为 REFUNDING。该状态锁是本地防超退边界，
     * 保证同一支付单同一时间只有一笔退款申请进入微信。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentRefundVO applyRefund(Long userId, PaymentRefundApplyDTO dto) {
        assertLogin(userId);
        if (dto == null || StrUtil.isBlank(dto.getOrderNo())) {
            throw new BaseException(ResultCodeEnum.PARAM_ERROR);
        }
        var order = getRequiredOrder(dto.getOrderNo());
        assertOrderOwner(order, userId);
        if (!List.of(PaymentConstants.OrderStatus.PAID, PaymentConstants.OrderStatus.PARTIAL_REFUNDED).contains(order.getStatus())) {
            throw paymentException(PaymentResultCodeEnum.PAYMENT_ORDER_STATUS_ERROR);
        }

        var refunded = order.getRefundFeeFen() == null ? 0 : order.getRefundFeeFen();
        var remaining = order.getTotalFeeFen() - refunded;
        var refundFee = dto.getRefundFeeFen() == null ? remaining : dto.getRefundFeeFen();
        if (refundFee <= 0 || refundFee > remaining) {
            throw paymentException(PaymentResultCodeEnum.REFUND_AMOUNT_ERROR);
        }

        var now = LocalDateTime.now();
        // 用支付单状态抢占退款处理权，防止并发读取同一 remaining 后同时发起退款。
        var locked = orderMapper.update(null, Wrappers.<CorePaymentOrder>lambdaUpdate()
                .eq(CorePaymentOrder::getId, order.getId())
                .in(CorePaymentOrder::getStatus, PaymentConstants.OrderStatus.PAID, PaymentConstants.OrderStatus.PARTIAL_REFUNDED)
                .set(CorePaymentOrder::getStatus, PaymentConstants.OrderStatus.REFUNDING)
                .set(CorePaymentOrder::getUpdateTime, now));
        if (locked <= 0) {
            throw paymentException(PaymentResultCodeEnum.REFUND_STATUS_ERROR);
        }

        wechatPayClient.assertReady();
        var refund = new CorePaymentRefund();
        refund.setOrderId(order.getId());
        refund.setOutTradeNo(order.getOrderNo());
        refund.setOutRefundNo(generateNo("RF"));
        refund.setUserId(userId);
        refund.setRefundFeeFen(refundFee);
        refund.setRefundReason(StrUtil.subPre(dto.getRefundReason(), 255));
        refund.setStatus(PaymentConstants.RefundStatus.APPLYING);
        refund.setCreateTime(now);
        refund.setUpdateTime(now);
        refundMapper.insert(refund);

        var result = wechatPayClient.applyRefund(order, refund);
        refund.setRefundId(result.getRefundId());
        refund.setRawResponse(result.getRawResponse());
        refund.setSuccessTime(result.getSuccessTime());
        refund.setStatus(toRefundStatus(result.getStatus()));
        refund.setUpdateTime(LocalDateTime.now());
        refundMapper.updateById(refund);

        if (refund.getStatus() == PaymentConstants.RefundStatus.SUCCESS) {
            markRefundSuccess(refund, order, result.getSuccessTime());
        } else if (refund.getStatus() == PaymentConstants.RefundStatus.FAILED
                || refund.getStatus() == PaymentConstants.RefundStatus.CLOSED) {
            notifyRefundFailed(refund, order);
        }
        return toRefundVO(refund);
    }

    /**
     * 查询当前用户退款单。
     */
    @Override
    public PaymentRefundVO queryRefund(Long userId, String outRefundNo) {
        assertLogin(userId);
        var refund = getRequiredRefund(outRefundNo);
        if (!Objects.equals(refund.getUserId(), userId)) {
            throw paymentException(PaymentResultCodeEnum.REFUND_ORDER_NOT_FOUND);
        }
        return toRefundVO(refund);
    }

    /**
     * 处理微信退款通知。
     *
     * <p>验签解析在事务外完成；回调日志、退款状态、支付单累计退款金额和业务通知在同一事务中处理。</p>
     */
    @Override
    public String handleRefundNotify(Map<String, String> headers, String body) {
        WechatPayClient.RefundNotifyResult notify;
        try {
            notify = wechatPayClient.parseRefundNotify(headers, body);
        } catch (Exception e) {
            log.warn("退款回调验签或解析失败", e);
            return WxPayNotifyV3Response.fail("失败");
        }
        try {
            transactionTemplate.executeWithoutResult(status -> processRefundNotify(headers, body, notify));
            return WxPayNotifyV3Response.success("成功");
        } catch (DuplicateKeyException e) {
            return successIfProcessed(PaymentConstants.NotifyType.REFUND, notify.getNotifyId());
        } catch (Exception e) {
            log.warn("退款回调处理失败 outRefundNo={}", notify.getOutRefundNo(), e);
            return WxPayNotifyV3Response.fail("失败");
        }
    }

    /**
     * 写入退款回调日志并推进退款状态。
     *
     * <p>只允许 APPLYING/PROCESSING 更新到终态或处理中；已处理过的重复通知会被标记忽略，不再累计退款金额。</p>
     */
    private void processRefundNotify(Map<String, String> headers, String body, WechatPayClient.RefundNotifyResult notify) {
        var notifyLog = insertNotifyLog(PaymentConstants.NotifyType.REFUND, notify.getNotifyId(), notify.getOutTradeNo(),
                null, notify.getOutRefundNo(), notify.getRefundId(), headers, body);
        var refund = getRequiredRefund(notify.getOutRefundNo());
        var order = getRequiredOrder(refund.getOutTradeNo());
        var targetStatus = toRefundStatus(notify.getRefundStatus());
        // 条件更新是退款回调幂等边界，避免微信重试导致重复加已退金额。
        var updated = refundMapper.update(null, Wrappers.<CorePaymentRefund>lambdaUpdate()
                .eq(CorePaymentRefund::getId, refund.getId())
                .in(CorePaymentRefund::getStatus, PaymentConstants.RefundStatus.APPLYING, PaymentConstants.RefundStatus.PROCESSING)
                .set(CorePaymentRefund::getStatus, targetStatus)
                .set(CorePaymentRefund::getRefundId, notify.getRefundId())
                .set(CorePaymentRefund::getNotifyTime, LocalDateTime.now())
                .set(CorePaymentRefund::getSuccessTime, notify.getSuccessTime())
                .set(CorePaymentRefund::getRawResponse, notify.getRawResponse())
                .set(CorePaymentRefund::getFailReason, targetStatus == PaymentConstants.RefundStatus.SUCCESS ? null : notify.getRefundStatus())
                .set(CorePaymentRefund::getUpdateTime, LocalDateTime.now()));
        if (updated <= 0) {
            updateNotifyLog(notifyLog.getId(), PaymentConstants.NotifyProcessStatus.IGNORED, "重复退款状态通知");
            return;
        }

        refund.setRefundId(notify.getRefundId());
        refund.setStatus(targetStatus);
        refund.setSuccessTime(notify.getSuccessTime());
        refund.setFailReason(targetStatus == PaymentConstants.RefundStatus.SUCCESS ? null : notify.getRefundStatus());
        if (targetStatus == PaymentConstants.RefundStatus.SUCCESS) {
            markRefundSuccess(refund, order, notify.getSuccessTime());
        } else if (targetStatus == PaymentConstants.RefundStatus.FAILED
                || targetStatus == PaymentConstants.RefundStatus.CLOSED) {
            notifyRefundFailed(refund, order);
        }
        updateNotifyLog(notifyLog.getId(), PaymentConstants.NotifyProcessStatus.SUCCESS, null);
    }

    /**
     * 累计已退金额并通知业务侧退款成功。
     *
     * <p>该方法只在退款单首次进入 SUCCESS 时调用；随后按累计已退金额决定支付单是部分退款还是全额退款。</p>
     */
    private void markRefundSuccess(CorePaymentRefund refund, CorePaymentOrder order, LocalDateTime refundTime) {
        var now = LocalDateTime.now();
        orderMapper.update(null, Wrappers.<CorePaymentOrder>lambdaUpdate()
                .eq(CorePaymentOrder::getId, order.getId())
                .setSql("refund_fee_fen = refund_fee_fen + " + refund.getRefundFeeFen())
                .set(CorePaymentOrder::getUpdateTime, now));
        var latest = orderMapper.selectById(order.getId());
        var status = latest.getRefundFeeFen() >= latest.getTotalFeeFen()
                ? PaymentConstants.OrderStatus.REFUNDED
                : PaymentConstants.OrderStatus.PARTIAL_REFUNDED;
        orderMapper.update(null, Wrappers.<CorePaymentOrder>lambdaUpdate()
                .eq(CorePaymentOrder::getId, order.getId())
                .set(CorePaymentOrder::getStatus, status)
                .set(CorePaymentOrder::getUpdateTime, now));

        handlerRegistry.getRequiredHandler(order.getBizType()).onRefundSuccess(PaymentRefundEvent.builder()
                .userId(order.getUserId())
                .bizType(order.getBizType())
                .bizOrderNo(order.getBizOrderNo())
                .orderNo(order.getOrderNo())
                .outRefundNo(refund.getOutRefundNo())
                .refundId(refund.getRefundId())
                .refundFeeFen(refund.getRefundFeeFen())
                .totalRefundFeeFen(latest.getRefundFeeFen())
                .fullyRefunded(status == PaymentConstants.OrderStatus.REFUNDED)
                .refundTime(refundTime == null ? now : refundTime)
                .build());
    }

    /**
     * 通知业务侧退款失败并恢复支付单状态。
     */
    private void notifyRefundFailed(CorePaymentRefund refund, CorePaymentOrder order) {
        restoreOrderAfterRefundFailed(order);
        handlerRegistry.getRequiredHandler(order.getBizType()).onRefundFailed(PaymentRefundEvent.builder()
                .userId(order.getUserId())
                .bizType(order.getBizType())
                .bizOrderNo(order.getBizOrderNo())
                .orderNo(order.getOrderNo())
                .outRefundNo(refund.getOutRefundNo())
                .refundId(refund.getRefundId())
                .refundFeeFen(refund.getRefundFeeFen())
                .totalRefundFeeFen(order.getRefundFeeFen())
                .fullyRefunded(false)
                .refundTime(LocalDateTime.now())
                .failReason(refund.getFailReason())
                .build());
    }

    /**
     * 退款失败后恢复支付单可再次申请退款的状态。
     *
     * <p>已有部分退款则回到 PARTIAL_REFUNDED，否则回到 PAID，避免失败退款长期占用 REFUNDING。</p>
     */
    private void restoreOrderAfterRefundFailed(CorePaymentOrder order) {
        var status = order.getRefundFeeFen() != null && order.getRefundFeeFen() > 0
                ? PaymentConstants.OrderStatus.PARTIAL_REFUNDED
                : PaymentConstants.OrderStatus.PAID;
        orderMapper.update(null, Wrappers.<CorePaymentOrder>lambdaUpdate()
                .eq(CorePaymentOrder::getId, order.getId())
                .set(CorePaymentOrder::getStatus, status)
                .set(CorePaymentOrder::getUpdateTime, LocalDateTime.now()));
    }

    /**
     * 映射微信退款状态到本地状态机。
     */
    private int toRefundStatus(String wechatStatus) {
        if ("SUCCESS".equals(wechatStatus)) {
            return PaymentConstants.RefundStatus.SUCCESS;
        }
        if ("CLOSED".equals(wechatStatus) || "CLOSE".equals(wechatStatus)) {
            return PaymentConstants.RefundStatus.CLOSED;
        }
        if ("PROCESSING".equals(wechatStatus)) {
            return PaymentConstants.RefundStatus.PROCESSING;
        }
        return PaymentConstants.RefundStatus.FAILED;
    }

    private CorePaymentNotifyLog insertNotifyLog(Integer notifyType, String notifyId, String outTradeNo,
                                                 String transactionId, String outRefundNo, String refundId,
                                                 Map<String, String> headers, String body) {
        // 保留原始回调材料，用于定位微信重试、验签和退款对账问题。
        var now = LocalDateTime.now();
        var log = new CorePaymentNotifyLog();
        log.setNotifyType(notifyType);
        log.setNotifyId(StrUtil.blankToDefault(notifyId, "UNKNOWN-" + generateNo("N")));
        log.setOutTradeNo(outTradeNo);
        log.setTransactionId(transactionId);
        log.setOutRefundNo(outRefundNo);
        log.setRefundId(refundId);
        log.setHeadersJson(JSONUtil.toJsonStr(headers));
        log.setRawBody(body);
        log.setProcessStatus(PaymentConstants.NotifyProcessStatus.RECEIVED);
        log.setCreateTime(now);
        log.setUpdateTime(now);
        notifyLogMapper.insert(log);
        return log;
    }

    private void updateNotifyLog(Long id, Integer status, String errorMsg) {
        notifyLogMapper.update(null, Wrappers.<CorePaymentNotifyLog>lambdaUpdate()
                .eq(CorePaymentNotifyLog::getId, id)
                .set(CorePaymentNotifyLog::getProcessStatus, status)
                .set(CorePaymentNotifyLog::getErrorMsg, StrUtil.subPre(errorMsg, 500))
                .set(CorePaymentNotifyLog::getUpdateTime, LocalDateTime.now()));
    }

    private CorePaymentOrder getRequiredOrder(String orderNo) {
        var order = orderMapper.selectOne(Wrappers.<CorePaymentOrder>lambdaQuery()
                .eq(CorePaymentOrder::getOrderNo, orderNo)
                .last("limit 1"));
        if (order == null) {
            throw paymentException(PaymentResultCodeEnum.PAYMENT_ORDER_NOT_FOUND);
        }
        return order;
    }

    private CorePaymentRefund getRequiredRefund(String outRefundNo) {
        var refund = refundMapper.selectOne(Wrappers.<CorePaymentRefund>lambdaQuery()
                .eq(CorePaymentRefund::getOutRefundNo, outRefundNo)
                .last("limit 1"));
        if (refund == null) {
            throw paymentException(PaymentResultCodeEnum.REFUND_ORDER_NOT_FOUND);
        }
        return refund;
    }

    private String successIfProcessed(Integer notifyType, String notifyId) {
        // 已成功或已忽略的重复通知直接回成功，避免微信继续推送同一通知。
        var notifyLog = notifyLogMapper.selectOne(Wrappers.<CorePaymentNotifyLog>lambdaQuery()
                .eq(CorePaymentNotifyLog::getNotifyType, notifyType)
                .eq(CorePaymentNotifyLog::getNotifyId, StrUtil.blankToDefault(notifyId, ""))
                .last("limit 1"));
        if (notifyLog != null && List.of(PaymentConstants.NotifyProcessStatus.SUCCESS,
                PaymentConstants.NotifyProcessStatus.IGNORED).contains(notifyLog.getProcessStatus())) {
            return WxPayNotifyV3Response.success("重复通知");
        }
        return WxPayNotifyV3Response.fail("失败");
    }

    private void assertLogin(Long userId) {
        if (userId == null) {
            throw new BaseException(ResultCodeEnum.LOGIN_AUTH);
        }
    }

    private void assertOrderOwner(CorePaymentOrder order, Long userId) {
        if (!Objects.equals(order.getUserId(), userId)) {
            throw paymentException(PaymentResultCodeEnum.PAYMENT_ORDER_NOT_FOUND);
        }
    }

    private String generateNo(String prefix) {
        return prefix + LocalDateTime.now().format(NO_FORMATTER) + String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    private PaymentRefundVO toRefundVO(CorePaymentRefund refund) {
        return PaymentRefundVO.builder()
                .refundId(refund.getId())
                .orderNo(refund.getOutTradeNo())
                .outRefundNo(refund.getOutRefundNo())
                .refundFeeFen(refund.getRefundFeeFen())
                .status(refund.getStatus())
                .wechatRefundId(refund.getRefundId())
                .successTime(refund.getSuccessTime())
                .failReason(refund.getFailReason())
                .build();
    }

    private BaseException paymentException(PaymentResultCodeEnum codeEnum) {
        return new BaseException(codeEnum.getCode(), codeEnum.getMessage());
    }
}
