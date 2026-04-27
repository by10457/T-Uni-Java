# TODO

这份文件记录的是“当前明确不放进主模板，但后续值得模块化推进”的能力。

这些能力不是不重要，而是现在直接塞进主模板会把模板边界做脏。

## 1. 支付

当前结论：

- 已纳入 `t-uni-server/server-services/server-payment`
- 作为可选微信支付/退款模板提供
- 默认 `payment.wechat.enabled=false`，不影响最小启动路径

已纳入模板的能力：

- 微信小程序 JSAPI 锁单、预下单、支付参数生成
- 支付回调验签、解析、幂等落库
- 超时未支付自动查单关单
- 退款申请 Service
- 退款回调验签、解析、幂等落库
- 支付单、交易流水、退款单、回调日志 core 表
- `PaymentBizHandler` 业务扩展 SPI

不纳入主模板的部分：

- 具体业务支付订单逻辑
- 佣金、分账、代理、营销优惠等复杂资金链路
- 具体业务售后退款规则
- 用户自助退款入口
- 管理端退款页面

## 2. 消息通知

当前结论：

- 这轮先不并入主模板实现
- 后续先做轻量通知内核

推荐拆分：

- `common-notify-core`
  - 站内通知模型
  - outbox
  - 去重
  - 重试
  - 通道抽象
- `common-notify-email-starter`
  - 邮件通道适配

不纳入主模板的部分：

- 社交通知类型
- 页面跳转路径
- 帖子 / 评论 / 举报等具体业务通知拼装

## 3. IM

当前结论：

- 已完成 OpenIM 最小模板版
- 作为 `t-uni-server/server-services/server-im` 可选模块提供
- 默认 `openim.enabled=false`，不影响最小启动路径

已纳入模板的能力：

- OpenIM 用户按需注册
- OpenIM user token 获取
- admin token JVM 内存缓存
- Webhook token 验签与统一放行
- 系统通知同步直发

后续可增强：

- 多 IM 厂商抽象
- 更完整的消息 outbox / 重试
- 管理端 IM 运维视图

不纳入主模板的部分：

- 社交私信逻辑
- 社交通知回流
- 社交审核规则

## 4. 七牛统一资源解析

当前结论：

- 这是模板值得持续增强的一项能力
- 这轮已经完成最小版 `PUBLIC / PRIVATE` 和基础 URL / fileKey 解析入口

目标方向：

- 统一处理 `fileKey / 系统七牛 URL / 外链 URL`
- 显式支持 `PUBLIC / PRIVATE`
- 内部资源尽量只存 `fileKey`

推荐后续增加：

- 字段级白名单策略
- 更完整的外链允许规则
- 更细的多 bucket / 多 domain 策略

## 5. common-core 结构整理

当前结论：

- 这是必做优化方向
- 但应先立文档规范，再分阶段迁移代码

优先方向：

- 把运行时配置和中间件接入能力往 `common-config` 收
- 收紧 `common-core` 的放置边界

## 6. 状态码规范收敛

当前结论：

- 先统一异常出口和鉴权出口
- 再整理码段
- 不做一次性硬重排
