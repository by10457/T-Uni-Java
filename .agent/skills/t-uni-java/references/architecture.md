# 架构说明

完整说明见 [../../../../docs/architecture.md](../../../../docs/architecture.md)。

这个 skill 侧只强调几条执行时最容易踩歪的边界：

- 模板主路径是 `t-uni-common` 和 `t-uni-server`
- `t-uni-admin` 是可选模块，不要让 admin 的历史写法反向覆盖主模板规范
- `admin` 和 `server` 只能通过共享模型或 common 模块协作，不要直接引用彼此内部实现
- `core_user + biz_user` 是默认双表边界，不要随手改单表
- 支付、通知、IM 暂不作为主模板默认能力
