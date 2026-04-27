# 微信支付与退款接入

`server-payment` 是模板级微信小程序 JSAPI 支付模块，只提供通用支付能力，不绑定具体业务订单表。

## 模块能力

- 统一锁单入口：`POST /payment/wechat/lock`
- 重新获取支付参数：`POST /payment/wechat/prepay`
- 支付单查询：`GET /payment/orders/{orderNo}`
- 微信支付回调：`POST /payment/notify/wechat/pay`
- 微信退款回调：`POST /payment/notify/wechat/refund`
- 退款 Service：`PaymentRefundService.applyRefund(...)`
- 自动关单：每分钟扫描过期未支付订单，先查微信订单状态再关单
- core 表：`core_payment_order`、`core_payment_transaction`、`core_payment_refund`、`core_payment_notify_log`

模块不默认提供用户自助退款接口。退款通常涉及售后、审批、权益回收和风控，应由业务模块或后台模块封装后调用 `PaymentRefundService`。

## 环境变量

```yaml
payment:
  wechat:
    enabled: ${WECHAT_PAY_ENABLED:false}
    app-id: ${WECHAT_PAY_APP_ID:${WX_MINIAPP_APPID:}}
    mch-id: ${WECHAT_PAY_MCH_ID:}
    mch-serial-no: ${WECHAT_PAY_MCH_SERIAL_NO:}
    api-v3-key: ${WECHAT_PAY_API_V3_KEY:}
    private-key-path: ${WECHAT_PAY_PRIVATE_KEY_PATH:}
    private-key: ${WECHAT_PAY_PRIVATE_KEY:}
    notify-base-url: ${WECHAT_PAY_NOTIFY_BASE_URL:}
    pay-notify-path: ${WECHAT_PAY_PAY_NOTIFY_PATH:/payment/notify/wechat/pay}
    refund-notify-path: ${WECHAT_PAY_REFUND_NOTIFY_PATH:/payment/notify/wechat/refund}
    order-expire-minutes: ${WECHAT_PAY_ORDER_EXPIRE_MINUTES:15}
```

必填项：

- `WECHAT_PAY_ENABLED=true`
- `WECHAT_PAY_APP_ID`
- `WECHAT_PAY_MCH_ID`
- `WECHAT_PAY_MCH_SERIAL_NO`
- `WECHAT_PAY_API_V3_KEY`
- `WECHAT_PAY_PRIVATE_KEY` 或 `WECHAT_PAY_PRIVATE_KEY_PATH`
- `WECHAT_PAY_NOTIFY_BASE_URL`

`enabled=false` 时应用可以正常启动；调用支付能力会返回 `PAYMENT_CONFIG_MISSING`。

## 微信商户平台配置

需要在微信支付商户平台和小程序后台完成：

- 配置 APIv3 密钥
- 下载商户私钥并记录商户证书序列号
- 确认小程序 AppID 已绑定商户号
- 支付回调地址：`https://你的域名/payment/notify/wechat/pay`
- 退款回调地址：`https://你的域名/payment/notify/wechat/refund`
- 回调地址必须公网 HTTPS 可访问

## 前端调用流程

1. 小程序调用 `POST /payment/wechat/lock`。
2. 后端返回 `payParams`。
3. 小程序调用 `wx.requestPayment`。
4. 前端支付完成后轮询 `GET /payment/orders/{orderNo}`。
5. 最终支付状态以后端微信回调或查单结果为准，不以前端返回为准。

示例：

```js
const res = await request('/payment/wechat/lock', {
  method: 'POST',
  data: {
    bizType: 'PRODUCT',
    bizContent: {
      productId: 1,
      quantity: 1
    }
  }
})

await wx.requestPayment({
  timeStamp: res.data.payParams.timeStamp,
  nonceStr: res.data.payParams.nonceStr,
  package: res.data.payParams.packageValue,
  signType: res.data.payParams.signType,
  paySign: res.data.payParams.paySign
})
```

## 业务 SPI

业务项目实现 `PaymentBizHandler`：

```java
@Component
public class ProductPaymentBizHandler implements PaymentBizHandler {

    @Override
    public String getBizType() {
        return "PRODUCT";
    }

    @Override
    public PaymentBizLockedOrder lock(PaymentBizLockRequest request) {
        // TODO 业务侧实现：
        // 1. 解析 request.getBizContent()
        // 2. 校验参数、价格、用户权限
        // 3. 创建或复用业务订单
        // 4. 返回 bizOrderNo、金额、描述、attachData
        return PaymentBizLockedOrder.builder()
                .bizType("PRODUCT")
                .bizOrderNo("业务订单号")
                .description("商品描述")
                .totalFeeFen(3500)
                .attachData("可选附加数据")
                .build();
    }

    @Override
    public void onPaid(PaymentPaidEvent event) {
        // TODO 业务侧实现：将业务订单改为已支付，触发发货/生成/权益下发
    }

    @Override
    public void onClosed(PaymentClosedEvent event) {
        // TODO 业务侧实现：释放锁定资源，将业务订单改为已关闭
    }

    @Override
    public void onRefundSuccess(PaymentRefundEvent event) {
        // TODO 业务侧实现：记录退款成功，按需回滚权益或更新订单
    }

    @Override
    public void onRefundFailed(PaymentRefundEvent event) {
        // TODO 业务侧实现：记录退款失败/异常，按需通知运营处理
    }
}
```

同一个 `bizType` 只能有一个 handler。重复配置会导致启动失败。

## 退款接入

业务或后台在完成自己的售后校验后调用：

```java
paymentRefundService.applyRefund(userId, dto);
```

`PaymentRefundApplyDTO`：

```json
{
  "orderNo": "PP20260427223000123456",
  "refundFeeFen": 3500,
  "refundReason": "用户申请退款"
}
```

注意：

- `refundFeeFen` 为空时默认退剩余可退金额。
- 一笔支付单可多次部分退款。
- 总退款金额不能超过 `total_fee_fen - refund_fee_fen`。
- 申请退款成功只代表微信受理，最终结果以退款回调为准。
- 只有退款成功回调后才累计 `core_payment_order.refund_fee_fen`。

## 常见问题

`PAYMENT_CONFIG_MISSING`：
检查 `payment.wechat.enabled=true`，并确认 AppID、商户号、证书序列号、APIv3 密钥、私钥、回调基础地址都已配置。

`PAYMENT_OPENID_MISSING`：
JSAPI 支付必须使用小程序 openid。当前模板默认从 `biz_user.ma_open_id` 获取。

无业务 handler：
需要实现 `PaymentBizHandler`，并确保 `getBizType()` 和锁单请求的 `bizType` 一致。

回调收不到：
确认回调域名公网 HTTPS 可访问，商户平台回调地址正确，网关没有拦截 `/payment/notify/**`。

签名失败：
检查 APIv3 密钥、商户证书序列号、商户私钥是否属于同一商户号，回调 body 是否被网关改写。

本地已关闭但微信已支付：
自动关单前会先查微信订单。若微信返回已支付，本地会按支付成功处理。业务侧 `onPaid` 需要具备幂等能力。

退款申请成功但用户没到账：
微信退款是异步流程，申请成功只代表受理。到账结果以退款回调和微信退款查询为准。
