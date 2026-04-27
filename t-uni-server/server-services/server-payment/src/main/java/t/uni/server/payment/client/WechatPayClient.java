package t.uni.server.payment.client;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.github.binarywang.wxpay.bean.notify.SignatureHeader;
import com.github.binarywang.wxpay.bean.request.WxPayRefundV3Request;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderV3Result;
import com.github.binarywang.wxpay.bean.result.enums.TradeTypeEnum;
import com.github.binarywang.wxpay.service.WxPayService;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import t.uni.common.core.exception.BaseException;
import t.uni.server.payment.config.WechatPayProperties;
import t.uni.server.payment.constant.PaymentConstants;
import t.uni.server.payment.constant.PaymentResultCodeEnum;
import t.uni.server.payment.entity.CorePaymentOrder;
import t.uni.server.payment.entity.CorePaymentRefund;
import t.uni.server.payment.vo.WechatJsapiPayParamsVO;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

/**
 * 微信支付 SDK 封装。
 *
 * <p>统一屏蔽第三方 SDK 请求/响应结构，向服务层只暴露支付模块需要的稳定结果对象。本客户端不处理本地状态机，
 * 也不吞掉验签失败等安全异常。</p>
 */
@Component
@RequiredArgsConstructor
public class WechatPayClient {

    private static final String HEADER_TIMESTAMP = "wechatpay-timestamp";
    private static final String HEADER_NONCE = "wechatpay-nonce";
    private static final String HEADER_SIGNATURE = "wechatpay-signature";
    private static final String HEADER_SERIAL = "wechatpay-serial";

    private final WechatPayProperties properties;
    private final WxPayService wxPayService;

    /**
     * 校验真实微信支付调用所需配置。
     *
     * <p>放在调用前检查，避免模板环境启动失败，同时让业务请求明确返回支付配置缺失。</p>
     */
    public void assertReady() {
        if (!properties.isEnabled()
                || StrUtil.isBlank(properties.getAppId())
                || StrUtil.isBlank(properties.getMchId())
                || StrUtil.isBlank(properties.getMchSerialNo())
                || StrUtil.isBlank(properties.getApiV3Key())
                || (StrUtil.isBlank(properties.getPrivateKey()) && StrUtil.isBlank(properties.getPrivateKeyPath()))
                || StrUtil.isBlank(properties.getNotifyBaseUrl())) {
            throw paymentException(PaymentResultCodeEnum.PAYMENT_CONFIG_MISSING);
        }
    }

    /**
     * 调用微信 JSAPI 预下单。
     *
     * @param order 本地支付单
     * @param openId 小程序用户 openId
     * @return 预支付ID、调起支付参数和微信原始响应
     */
    public PrepayResult prepay(CorePaymentOrder order, String openId) {
        assertReady();
        try {
            var request = new WxPayUnifiedOrderV3Request()
                    .setAppid(properties.getAppId())
                    .setMchid(properties.getMchId())
                    .setDescription(order.getDescription())
                    .setOutTradeNo(order.getOrderNo())
                    .setTimeExpire(toWechatTime(order.getExpireTime().atOffset(ZoneOffset.ofHours(8))))
                    .setAttach(order.getAttachData())
                    .setNotifyUrl(properties.buildPayNotifyUrl());

            var amount = new WxPayUnifiedOrderV3Request.Amount();
            amount.setTotal(order.getTotalFeeFen());
            amount.setCurrency(PaymentConstants.CURRENCY_CNY);
            request.setAmount(amount);

            var payer = new WxPayUnifiedOrderV3Request.Payer();
            payer.setOpenid(openId);
            request.setPayer(payer);

            var result = wxPayService.unifiedOrderV3(TradeTypeEnum.JSAPI, request);
            return PrepayResult.builder()
                    .prepayId(result.getPrepayId())
                    .payParams(buildJsapiPayParams(result.getPrepayId()))
                    .rawResponse(JSONUtil.toJsonStr(result))
                    .build();
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(PaymentResultCodeEnum.PAYMENT_NOTIFY_ERROR.getCode(), "微信预下单失败");
        }
    }

    /**
     * 根据 prepayId 生成小程序调起微信收银台参数。
     *
     * @param prepayId 微信预支付ID
     * @return JSAPI 支付参数
     */
    public WechatJsapiPayParamsVO buildJsapiPayParams(String prepayId) {
        assertReady();
        try {
            var payInfo = WxPayUnifiedOrderV3Result.getJsapiPayInfo(
                    prepayId,
                    properties.getAppId(),
                    wxPayService.getConfig().getPrivateKey()
            );
            return WechatJsapiPayParamsVO.builder()
                    .appId(payInfo.getAppId())
                    .timeStamp(payInfo.getTimeStamp())
                    .nonceStr(payInfo.getNonceStr())
                    .packageValue(payInfo.getPackageValue())
                    .signType(payInfo.getSignType())
                    .paySign(payInfo.getPaySign())
                    .prepayId(payInfo.getPrepayId())
                    .build();
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(PaymentResultCodeEnum.PAYMENT_CONFIG_MISSING.getCode(), "生成微信支付参数失败");
        }
    }

    /**
     * 验签并解析微信支付回调。
     *
     * @param headers 原始回调请求头
     * @param body 原始回调报文
     * @return 支付通知结果
     */
    public PayNotifyResult parsePayNotify(Map<String, String> headers, String body) {
        assertReady();
        try {
            var parsed = wxPayService.parseOrderNotifyV3Result(body, signatureHeader(headers));
            var raw = parsed.getRawData();
            var result = parsed.getResult();
            return PayNotifyResult.builder()
                    .notifyId(raw == null ? null : raw.getId())
                    .outTradeNo(result.getOutTradeNo())
                    .transactionId(result.getTransactionId())
                    .tradeState(result.getTradeState())
                    .successTime(toLocalDateTime(result.getSuccessTime()))
                    .totalFeeFen(result.getAmount() == null ? null : result.getAmount().getTotal())
                    .mchId(result.getMchid())
                    .rawResponse(JSONUtil.toJsonStr(parsed))
                    .build();
        } catch (Exception e) {
            throw new BaseException(PaymentResultCodeEnum.PAYMENT_NOTIFY_ERROR.getCode(), "支付回调验签或解析失败");
        }
    }

    /**
     * 验签并解析微信退款回调。
     *
     * @param headers 原始回调请求头
     * @param body 原始回调报文
     * @return 退款通知结果
     */
    public RefundNotifyResult parseRefundNotify(Map<String, String> headers, String body) {
        assertReady();
        try {
            var parsed = wxPayService.parseRefundNotifyV3Result(body, signatureHeader(headers));
            var raw = parsed.getRawData();
            var result = parsed.getResult();
            return RefundNotifyResult.builder()
                    .notifyId(raw == null ? null : raw.getId())
                    .outTradeNo(result.getOutTradeNo())
                    .outRefundNo(result.getOutRefundNo())
                    .refundId(result.getRefundId())
                    .refundStatus(result.getRefundStatus())
                    .successTime(toLocalDateTime(result.getSuccessTime()))
                    .refundFeeFen(result.getAmount() == null ? null : result.getAmount().getRefund())
                    .mchId(result.getMchid())
                    .rawResponse(JSONUtil.toJsonStr(parsed))
                    .build();
        } catch (Exception e) {
            throw new BaseException(PaymentResultCodeEnum.REFUND_NOTIFY_ERROR.getCode(), "退款回调验签或解析失败");
        }
    }

    /**
     * 按商户支付单号查询微信订单。
     *
     * <p>用于自动关单前兜底确认，防止本地超时但微信已支付时误关单。</p>
     *
     * @param outTradeNo 商户支付单号
     * @return 微信订单状态
     */
    public OrderQueryResult queryOrder(String outTradeNo) {
        assertReady();
        try {
            var result = wxPayService.queryOrderV3(null, outTradeNo);
            return OrderQueryResult.builder()
                    .outTradeNo(result.getOutTradeNo())
                    .transactionId(result.getTransactionId())
                    .tradeState(result.getTradeState())
                    .successTime(toLocalDateTime(result.getSuccessTime()))
                    .totalFeeFen(result.getAmount() == null ? null : result.getAmount().getTotal())
                    .rawResponse(JSONUtil.toJsonStr(result))
                    .build();
        } catch (Exception e) {
            throw new BaseException(PaymentResultCodeEnum.PAYMENT_NOTIFY_ERROR.getCode(), "微信查单失败");
        }
    }

    /**
     * 关闭微信侧未支付订单。
     *
     * @param outTradeNo 商户支付单号
     */
    public void closeOrder(String outTradeNo) {
        assertReady();
        try {
            wxPayService.closeOrderV3(outTradeNo);
        } catch (Exception e) {
            throw new BaseException(PaymentResultCodeEnum.PAYMENT_ORDER_STATUS_ERROR.getCode(), "微信关单失败");
        }
    }

    /**
     * 向微信申请退款。
     *
     * <p>请求同时传入退款金额和原支付总金额，微信会据此校验，配合本地剩余可退金额校验防止超退。</p>
     *
     * @param order 本地支付单
     * @param refund 本地退款单
     * @return 微信退款申请结果
     */
    public RefundApplyResult applyRefund(CorePaymentOrder order, CorePaymentRefund refund) {
        assertReady();
        try {
            var request = new WxPayRefundV3Request()
                    .setOutTradeNo(order.getOrderNo())
                    .setOutRefundNo(refund.getOutRefundNo())
                    .setReason(refund.getRefundReason())
                    .setNotifyUrl(properties.buildRefundNotifyUrl());

            var amount = new WxPayRefundV3Request.Amount();
            amount.setRefund(refund.getRefundFeeFen());
            amount.setTotal(order.getTotalFeeFen());
            amount.setCurrency(PaymentConstants.CURRENCY_CNY);
            request.setAmount(amount);

            var result = wxPayService.refundV3(request);
            return RefundApplyResult.builder()
                    .outRefundNo(result.getOutRefundNo())
                    .refundId(result.getRefundId())
                    .status(result.getStatus())
                    .successTime(toLocalDateTime(result.getSuccessTime()))
                    .rawResponse(JSONUtil.toJsonStr(result))
                    .build();
        } catch (Exception e) {
            throw new BaseException(PaymentResultCodeEnum.REFUND_AMOUNT_ERROR.getCode(), "微信退款申请失败");
        }
    }

    /**
     * 查询微信退款单。
     *
     * @param outRefundNo 商户退款单号
     * @return 微信退款状态
     */
    public RefundApplyResult queryRefund(String outRefundNo) {
        assertReady();
        try {
            var result = wxPayService.refundQueryV3(outRefundNo);
            return RefundApplyResult.builder()
                    .outRefundNo(result.getOutRefundNo())
                    .refundId(result.getRefundId())
                    .status(result.getStatus())
                    .successTime(toLocalDateTime(result.getSuccessTime()))
                    .rawResponse(JSONUtil.toJsonStr(result))
                    .build();
        } catch (Exception e) {
            throw new BaseException(PaymentResultCodeEnum.REFUND_ORDER_NOT_FOUND.getCode(), "微信退款查询失败");
        }
    }

    /**
     * 从原始请求头构造 SDK 验签对象。
     */
    private SignatureHeader signatureHeader(Map<String, String> headers) {
        return SignatureHeader.builder()
                .timeStamp(header(headers, HEADER_TIMESTAMP))
                .nonce(header(headers, HEADER_NONCE))
                .signature(header(headers, HEADER_SIGNATURE))
                .serial(header(headers, HEADER_SERIAL))
                .build();
    }

    /**
     * 微信回调头大小写不固定，按忽略大小写读取以避免代理层规范化导致验签失败。
     */
    private String header(Map<String, String> headers, String name) {
        if (headers == null) {
            return null;
        }
        for (var entry : headers.entrySet()) {
            if (name.equalsIgnoreCase(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String toWechatTime(OffsetDateTime time) {
        return time == null ? null : time.toString();
    }

    private java.time.LocalDateTime toLocalDateTime(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        return OffsetDateTime.parse(value).toLocalDateTime();
    }

    private BaseException paymentException(PaymentResultCodeEnum codeEnum) {
        return new BaseException(codeEnum.getCode(), codeEnum.getMessage());
    }

    /**
     * 微信预下单结果。
     */
    @Data
    @Builder
    public static class PrepayResult {
        private String prepayId;
        private WechatJsapiPayParamsVO payParams;
        private String rawResponse;
    }

    /**
     * 微信支付回调解析结果。
     */
    @Data
    @Builder
    public static class PayNotifyResult {
        private String notifyId;
        private String outTradeNo;
        private String transactionId;
        private String tradeState;
        private java.time.LocalDateTime successTime;
        private Integer totalFeeFen;
        private String mchId;
        private String rawResponse;
    }

    /**
     * 微信订单查询结果。
     */
    @Data
    @Builder
    public static class OrderQueryResult {
        private String outTradeNo;
        private String transactionId;
        private String tradeState;
        private java.time.LocalDateTime successTime;
        private Integer totalFeeFen;
        private String rawResponse;
    }

    /**
     * 微信退款申请或查询结果。
     */
    @Data
    @Builder
    public static class RefundApplyResult {
        private String outRefundNo;
        private String refundId;
        private String status;
        private java.time.LocalDateTime successTime;
        private String rawResponse;
    }

    /**
     * 微信退款回调解析结果。
     */
    @Data
    @Builder
    public static class RefundNotifyResult {
        private String notifyId;
        private String outTradeNo;
        private String outRefundNo;
        private String refundId;
        private String refundStatus;
        private java.time.LocalDateTime successTime;
        private Integer refundFeeFen;
        private String mchId;
        private String rawResponse;
    }
}
