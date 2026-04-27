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
import t.uni.server.domain.auth.IBusinessUser;
import t.uni.server.domain.auth.IBusinessUserMapper;
import t.uni.server.payment.client.WechatPayClient;
import t.uni.server.payment.constant.PaymentConstants;
import t.uni.server.payment.constant.PaymentResultCodeEnum;
import t.uni.server.payment.dto.PaymentLockDTO;
import t.uni.server.payment.dto.PaymentPrepayDTO;
import t.uni.server.payment.entity.CorePaymentNotifyLog;
import t.uni.server.payment.entity.CorePaymentOrder;
import t.uni.server.payment.entity.CorePaymentTransaction;
import t.uni.server.payment.mapper.CorePaymentNotifyLogMapper;
import t.uni.server.payment.mapper.CorePaymentOrderMapper;
import t.uni.server.payment.mapper.CorePaymentTransactionMapper;
import t.uni.server.payment.model.PaymentBizLockRequest;
import t.uni.server.payment.model.PaymentBizLockedOrder;
import t.uni.server.payment.model.PaymentClosedEvent;
import t.uni.server.payment.model.PaymentPaidEvent;
import t.uni.server.payment.service.PaymentOrderService;
import t.uni.server.payment.spi.PaymentBizHandlerRegistry;
import t.uni.server.payment.vo.PaymentOrderVO;
import t.uni.server.payment.vo.PaymentPrepayVO;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 支付核心服务。
 *
 * <p>维护本地支付单状态机，并把业务订单锁定、履约、释放委托给 {@code PaymentBizHandler}。
 * 状态变化入口集中在支付回调、查单兜底和关单任务，所有会触发业务回调的更新都用条件更新防重复。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentOrderServiceImpl implements PaymentOrderService {

    private static final DateTimeFormatter NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int CLOSE_BATCH_SIZE = 100;

    private final CorePaymentOrderMapper orderMapper;
    private final CorePaymentTransactionMapper transactionMapper;
    private final CorePaymentNotifyLogMapper notifyLogMapper;
    private final IBusinessUserMapper<? extends IBusinessUser> businessUserMapper;
    private final PaymentBizHandlerRegistry handlerRegistry;
    private final WechatPayClient wechatPayClient;
    private final TransactionTemplate transactionTemplate;

    /**
     * 锁定业务订单并创建或复用本地支付单。
     *
     * <p>{@code bizType + bizOrderNo} 是业务维度幂等键；并发插入撞唯一键时回查已有支付单，避免重复生成支付单
     * 和重复锁业务资源。方法事务覆盖本地锁单记录和交易流水，微信预下单失败会回滚本地新增记录。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentPrepayVO lockAndPrepay(Long userId, PaymentLockDTO dto) {
        assertLogin(userId);
        if (dto == null || StrUtil.isBlank(dto.getBizType())) {
            throw new BaseException(ResultCodeEnum.PARAM_ERROR);
        }

        var handler = handlerRegistry.getRequiredHandler(dto.getBizType());
        wechatPayClient.assertReady();
        var openId = getMaOpenId(userId);

        var lockedOrder = handler.lock(PaymentBizLockRequest.builder()
                .userId(userId)
                .bizType(dto.getBizType())
                .bizContent(dto.getBizContent())
                .build());
        validateLockedOrder(dto.getBizType(), lockedOrder);

        var existing = orderMapper.selectOne(Wrappers.<CorePaymentOrder>lambdaQuery()
                .eq(CorePaymentOrder::getBizType, lockedOrder.getBizType())
                .eq(CorePaymentOrder::getBizOrderNo, lockedOrder.getBizOrderNo())
                .last("limit 1"));
        if (existing != null) {
            assertOrderOwner(existing, userId);
            // 已有未过期支付单直接复用，避免同一业务订单出现多笔待支付。
            if (isPayable(existing)) {
                return prepayExisting(existing, openId);
            }
            return toPrepayVO(existing, null);
        }

        var now = LocalDateTime.now();
        var order = new CorePaymentOrder();
        order.setBizType(lockedOrder.getBizType());
        order.setBizOrderNo(lockedOrder.getBizOrderNo());
        order.setOrderNo(generateNo("PP"));
        order.setUserId(userId);
        order.setDescription(StrUtil.subPre(lockedOrder.getDescription(), 128));
        order.setTotalFeeFen(lockedOrder.getTotalFeeFen());
        order.setRefundFeeFen(0);
        order.setCurrency(PaymentConstants.CURRENCY_CNY);
        order.setPayChannel(PaymentConstants.PAY_CHANNEL_WECHAT);
        order.setStatus(PaymentConstants.OrderStatus.CREATED);
        order.setExpireTime(lockedOrder.getExpireTime() == null ? now.plusMinutes(15) : lockedOrder.getExpireTime());
        order.setAttachData(StrUtil.subPre(lockedOrder.getAttachData(), 512));
        order.setCreateTime(now);
        order.setUpdateTime(now);
        try {
            orderMapper.insert(order);
        } catch (DuplicateKeyException e) {
            // 并发锁单以唯一键兜底，拿到对方已创建的支付单继续后续流程。
            var duplicate = findOrderByBiz(lockedOrder.getBizType(), lockedOrder.getBizOrderNo());
            if (duplicate == null) {
                throw e;
            }
            assertOrderOwner(duplicate, userId);
            if (isPayable(duplicate)) {
                return prepayExisting(duplicate, openId);
            }
            return toPrepayVO(duplicate, null);
        }

        var transaction = buildTransaction(order, now);
        transactionMapper.insert(transaction);
        return prepayExisting(order, openId);
    }

    /**
     * 重新获取仍可支付订单的微信支付参数。
     *
     * <p>只允许订单归属人操作，且不会改变业务订单锁定结果。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentPrepayVO reprepay(Long userId, PaymentPrepayDTO dto) {
        assertLogin(userId);
        if (dto == null || StrUtil.isBlank(dto.getOrderNo())) {
            throw new BaseException(ResultCodeEnum.PARAM_ERROR);
        }
        var order = getRequiredOrder(dto.getOrderNo());
        assertOrderOwner(order, userId);
        if (!isPayable(order)) {
            throw paymentException(PaymentResultCodeEnum.PAYMENT_ORDER_STATUS_ERROR);
        }
        wechatPayClient.assertReady();
        return prepayExisting(order, getMaOpenId(userId));
    }

    /**
     * 查询当前用户支付单。
     */
    @Override
    public PaymentOrderVO queryOrder(Long userId, String orderNo) {
        assertLogin(userId);
        var order = getRequiredOrder(orderNo);
        assertOrderOwner(order, userId);
        return toOrderVO(order);
    }

    /**
     * 处理微信支付通知。
     *
     * <p>验签解析在事务外完成；本地状态更新和回调日志写入在同一事务内完成。重复通知依赖通知日志唯一键识别，
     * 已成功或已忽略的重复通知直接回成功，避免微信继续重试。</p>
     */
    @Override
    public String handlePayNotify(Map<String, String> headers, String body) {
        WechatPayClient.PayNotifyResult notify;
        try {
            notify = wechatPayClient.parsePayNotify(headers, body);
        } catch (Exception e) {
            log.warn("支付回调验签或解析失败", e);
            return WxPayNotifyV3Response.fail("失败");
        }
        try {
            transactionTemplate.executeWithoutResult(status -> processPayNotify(headers, body, notify));
            return WxPayNotifyV3Response.success("成功");
        } catch (DuplicateKeyException e) {
            return successIfProcessed(PaymentConstants.NotifyType.PAYMENT, notify.getNotifyId());
        } catch (Exception e) {
            log.warn("支付回调处理失败 outTradeNo={}", notify.getOutTradeNo(), e);
            return WxPayNotifyV3Response.fail("失败");
        }
    }

    /**
     * 批量处理过期未支付订单。
     *
     * <p>每张订单独立事务，单笔异常不阻塞后续订单；真正关单前会查微信订单状态做兜底。</p>
     */
    @Override
    public void closeExpiredOrders() {
        var orders = orderMapper.selectList(Wrappers.<CorePaymentOrder>lambdaQuery()
                .in(CorePaymentOrder::getStatus, PaymentConstants.OrderStatus.CREATED, PaymentConstants.OrderStatus.PREPAY_SUCCESS)
                .lt(CorePaymentOrder::getExpireTime, LocalDateTime.now())
                .orderByAsc(CorePaymentOrder::getExpireTime)
                .last("limit " + CLOSE_BATCH_SIZE));
        for (var order : orders) {
            try {
                transactionTemplate.executeWithoutResult(status -> closeExpiredOrder(order));
            } catch (Exception e) {
                log.warn("自动关单处理失败 orderNo={}", order.getOrderNo(), e);
            }
        }
    }

    private PaymentPrepayVO prepayExisting(CorePaymentOrder order, String openId) {
        // 已有 prepayId 时只重新签支付参数，不重复调用微信下单。
        var transaction = getOrCreateTransaction(order);
        if (StrUtil.isNotBlank(transaction.getPrepayId())) {
            var payParams = wechatPayClient.buildJsapiPayParams(transaction.getPrepayId());
            return toPrepayVO(order, payParams);
        }

        var prepay = wechatPayClient.prepay(order, openId);
        var now = LocalDateTime.now();
        transaction.setPrepayId(prepay.getPrepayId());
        transaction.setRawResponse(prepay.getRawResponse());
        transaction.setStatus(PaymentConstants.TransactionStatus.PREPAY_SUCCESS);
        transaction.setUpdateTime(now);
        transactionMapper.updateById(transaction);

        if (Objects.equals(order.getStatus(), PaymentConstants.OrderStatus.CREATED)) {
            order.setStatus(PaymentConstants.OrderStatus.PREPAY_SUCCESS);
            order.setUpdateTime(now);
            orderMapper.updateById(order);
        }
        return toPrepayVO(order, prepay.getPayParams());
    }

    /**
     * 关闭过期订单前的微信查单兜底。
     *
     * <p>微信返回 SUCCESS 时反向补本地支付成功；只有确认 NOTPAY/CLOSED 才关闭本地单，防止本地时间差误关已支付订单。</p>
     */
    private void closeExpiredOrder(CorePaymentOrder order) {
        var queryResult = wechatPayClient.queryOrder(order.getOrderNo());
        if ("SUCCESS".equals(queryResult.getTradeState())) {
            markPaid(order.getOrderNo(), queryResult.getTransactionId(), queryResult.getTotalFeeFen(),
                    queryResult.getSuccessTime(), queryResult.getRawResponse());
            return;
        }
        if ("NOTPAY".equals(queryResult.getTradeState())) {
            wechatPayClient.closeOrder(order.getOrderNo());
            closeLocalOrder(order);
            return;
        }
        if ("CLOSED".equals(queryResult.getTradeState())) {
            closeLocalOrder(order);
        }
    }

    /**
     * 写入支付回调日志并推进支付状态。
     *
     * <p>非 SUCCESS 通知只记录为忽略，不触发失败状态，避免微信中间状态覆盖后续成功结果。</p>
     */
    private void processPayNotify(Map<String, String> headers, String body, WechatPayClient.PayNotifyResult notify) {
        var notifyLog = insertNotifyLog(PaymentConstants.NotifyType.PAYMENT, notify.getNotifyId(), notify.getOutTradeNo(),
                notify.getTransactionId(), null, null, headers, body);
        if (!"SUCCESS".equals(notify.getTradeState())) {
            updateNotifyLog(notifyLog.getId(), PaymentConstants.NotifyProcessStatus.IGNORED, "非支付成功状态：" + notify.getTradeState());
            return;
        }
        markPaid(notify.getOutTradeNo(), notify.getTransactionId(), notify.getTotalFeeFen(),
                notify.getSuccessTime(), notify.getRawResponse());
        updateNotifyLog(notifyLog.getId(), PaymentConstants.NotifyProcessStatus.SUCCESS, null);
    }

    /**
     * 标记支付成功并通知业务侧履约。
     *
     * <p>金额必须与本地支付单一致；条件更新只允许未支付或已关闭状态推进到 PAID，确保重复回调、查单兜底或并发执行时
     * 业务履约只触发一次。允许 CLOSED 转 PAID 是为了修复本地误超时但微信已支付的情况。</p>
     */
    private void markPaid(String orderNo, String transactionId, Integer totalFeeFen,
                          LocalDateTime paidTime, String rawResponse) {
        var order = getRequiredOrder(orderNo);
        if (totalFeeFen != null && !Objects.equals(order.getTotalFeeFen(), totalFeeFen)) {
            throw paymentException(PaymentResultCodeEnum.PAYMENT_AMOUNT_ERROR);
        }
        if (!List.of(PaymentConstants.OrderStatus.CREATED, PaymentConstants.OrderStatus.PREPAY_SUCCESS,
                PaymentConstants.OrderStatus.CLOSED).contains(order.getStatus())) {
            return;
        }

        var now = LocalDateTime.now();
        var updateCount = orderMapper.update(null, Wrappers.<CorePaymentOrder>lambdaUpdate()
                .eq(CorePaymentOrder::getId, order.getId())
                .in(CorePaymentOrder::getStatus, PaymentConstants.OrderStatus.CREATED,
                        PaymentConstants.OrderStatus.PREPAY_SUCCESS, PaymentConstants.OrderStatus.CLOSED)
                .set(CorePaymentOrder::getStatus, PaymentConstants.OrderStatus.PAID)
                .set(CorePaymentOrder::getPaidTime, paidTime == null ? now : paidTime)
                .set(CorePaymentOrder::getUpdateTime, now));
        if (updateCount <= 0) {
            return;
        }

        transactionMapper.update(null, Wrappers.<CorePaymentTransaction>lambdaUpdate()
                .eq(CorePaymentTransaction::getOutTradeNo, orderNo)
                .set(CorePaymentTransaction::getStatus, PaymentConstants.TransactionStatus.PAY_SUCCESS)
                .set(CorePaymentTransaction::getTransactionId, transactionId)
                .set(CorePaymentTransaction::getNotifyTime, now)
                .set(CorePaymentTransaction::getSuccessTime, paidTime == null ? now : paidTime)
                .set(CorePaymentTransaction::getRawResponse, rawResponse)
                .set(CorePaymentTransaction::getUpdateTime, now));

        handlerRegistry.getRequiredHandler(order.getBizType()).onPaid(PaymentPaidEvent.builder()
                .userId(order.getUserId())
                .bizType(order.getBizType())
                .bizOrderNo(order.getBizOrderNo())
                .orderNo(order.getOrderNo())
                .transactionId(transactionId)
                .totalFeeFen(order.getTotalFeeFen())
                .paidTime(paidTime == null ? now : paidTime)
                .attachData(order.getAttachData())
                .build());
    }

    /**
     * 关闭本地支付单并通知业务侧释放锁定资源。
     *
     * <p>条件更新只允许 CREATED/PREPAY_SUCCESS 关闭，支付成功或退款中的订单不会被定时任务错关。</p>
     */
    private void closeLocalOrder(CorePaymentOrder order) {
        var now = LocalDateTime.now();
        var updateCount = orderMapper.update(null, Wrappers.<CorePaymentOrder>lambdaUpdate()
                .eq(CorePaymentOrder::getId, order.getId())
                .in(CorePaymentOrder::getStatus, PaymentConstants.OrderStatus.CREATED, PaymentConstants.OrderStatus.PREPAY_SUCCESS)
                .set(CorePaymentOrder::getStatus, PaymentConstants.OrderStatus.CLOSED)
                .set(CorePaymentOrder::getCloseTime, now)
                .set(CorePaymentOrder::getUpdateTime, now));
        if (updateCount <= 0) {
            return;
        }
        transactionMapper.update(null, Wrappers.<CorePaymentTransaction>lambdaUpdate()
                .eq(CorePaymentTransaction::getOutTradeNo, order.getOrderNo())
                .set(CorePaymentTransaction::getStatus, PaymentConstants.TransactionStatus.CLOSED)
                .set(CorePaymentTransaction::getUpdateTime, now));
        handlerRegistry.getRequiredHandler(order.getBizType()).onClosed(PaymentClosedEvent.builder()
                .userId(order.getUserId())
                .bizType(order.getBizType())
                .bizOrderNo(order.getBizOrderNo())
                .orderNo(order.getOrderNo())
                .closeTime(now)
                .build());
    }

    /**
     * 获取或补建交易流水。
     *
     * <p>用于历史异常或并发场景下保证支付单有交易记录，交易流水不作为业务幂等键。</p>
     */
    private CorePaymentTransaction getOrCreateTransaction(CorePaymentOrder order) {
        var transaction = transactionMapper.selectOne(Wrappers.<CorePaymentTransaction>lambdaQuery()
                .eq(CorePaymentTransaction::getOutTradeNo, order.getOrderNo())
                .last("limit 1"));
        if (transaction != null) {
            return transaction;
        }
        transaction = buildTransaction(order, LocalDateTime.now());
        transactionMapper.insert(transaction);
        return transaction;
    }

    private CorePaymentTransaction buildTransaction(CorePaymentOrder order, LocalDateTime now) {
        var transaction = new CorePaymentTransaction();
        transaction.setOrderId(order.getId());
        transaction.setOutTradeNo(order.getOrderNo());
        transaction.setBizOrderNo(order.getBizOrderNo());
        transaction.setUserId(order.getUserId());
        transaction.setPayChannel(PaymentConstants.PAY_CHANNEL_WECHAT);
        transaction.setTradeType(PaymentConstants.TRADE_TYPE_JSAPI);
        transaction.setStatus(PaymentConstants.TransactionStatus.INIT);
        transaction.setTotalFeeFen(order.getTotalFeeFen());
        transaction.setCreateTime(now);
        transaction.setUpdateTime(now);
        return transaction;
    }

    private CorePaymentNotifyLog insertNotifyLog(Integer notifyType, String notifyId, String outTradeNo,
                                                 String transactionId, String outRefundNo, String refundId,
                                                 Map<String, String> headers, String body) {
        // 保留请求头和原始报文，便于回调重试、验签失败和对账问题追踪。
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

    private CorePaymentOrder findOrderByBiz(String bizType, String bizOrderNo) {
        return orderMapper.selectOne(Wrappers.<CorePaymentOrder>lambdaQuery()
                .eq(CorePaymentOrder::getBizType, bizType)
                .eq(CorePaymentOrder::getBizOrderNo, bizOrderNo)
                .last("limit 1"));
    }

    private String successIfProcessed(Integer notifyType, String notifyId) {
        // 只有已处理成功或明确忽略的重复通知才回成功，处理中或失败状态继续让微信重试。
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

    /**
     * 判断本地支付单是否还能继续拉起支付。
     */
    private boolean isPayable(CorePaymentOrder order) {
        return List.of(PaymentConstants.OrderStatus.CREATED, PaymentConstants.OrderStatus.PREPAY_SUCCESS).contains(order.getStatus())
                && order.getExpireTime() != null
                && order.getExpireTime().isAfter(LocalDateTime.now());
    }

    private String getMaOpenId(Long userId) {
        var user = businessUserMapper.selectById(userId);
        if (user == null || StrUtil.isBlank(user.getMaOpenId())) {
            throw paymentException(PaymentResultCodeEnum.PAYMENT_OPENID_MISSING);
        }
        return user.getMaOpenId();
    }

    private void validateLockedOrder(String requestBizType, PaymentBizLockedOrder lockedOrder) {
        // 支付金额必须为正，业务类型不能被 handler 改到其他处理器名下。
        if (lockedOrder == null
                || StrUtil.isBlank(lockedOrder.getBizOrderNo())
                || lockedOrder.getTotalFeeFen() == null
                || lockedOrder.getTotalFeeFen() <= 0) {
            throw paymentException(PaymentResultCodeEnum.PAYMENT_AMOUNT_ERROR);
        }
        if (StrUtil.isBlank(lockedOrder.getBizType())) {
            lockedOrder.setBizType(requestBizType);
        }
        if (!Objects.equals(requestBizType, lockedOrder.getBizType())) {
            throw new BaseException(ResultCodeEnum.PARAM_ERROR);
        }
        if (StrUtil.isBlank(lockedOrder.getDescription())) {
            lockedOrder.setDescription(lockedOrder.getBizType() + "-" + lockedOrder.getBizOrderNo());
        }
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

    private PaymentPrepayVO toPrepayVO(CorePaymentOrder order, t.uni.server.payment.vo.WechatJsapiPayParamsVO payParams) {
        return PaymentPrepayVO.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .bizType(order.getBizType())
                .bizOrderNo(order.getBizOrderNo())
                .totalFeeFen(order.getTotalFeeFen())
                .status(order.getStatus())
                .expireTime(order.getExpireTime())
                .payParams(payParams)
                .build();
    }

    private PaymentOrderVO toOrderVO(CorePaymentOrder order) {
        return PaymentOrderVO.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .bizType(order.getBizType())
                .bizOrderNo(order.getBizOrderNo())
                .totalFeeFen(order.getTotalFeeFen())
                .refundFeeFen(order.getRefundFeeFen())
                .status(order.getStatus())
                .expireTime(order.getExpireTime())
                .paidTime(order.getPaidTime())
                .closeTime(order.getCloseTime())
                .build();
    }

    private BaseException paymentException(PaymentResultCodeEnum codeEnum) {
        return new BaseException(codeEnum.getCode(), codeEnum.getMessage());
    }
}
