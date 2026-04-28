# Architecture

## 模板目标

`T-Uni-Java` 的目标不是做一个“什么都内置”的大仓库，而是提供一套适合二次定制的后端模板基线。

核心原则：

- 优先沉淀 `common` 与 `server` 的通用能力
- `admin` 作为可选后台能力保留，但不反向绑架模板主路径
- 不机械复制任何具体业务项目的业务实现
- AI 和人都应该能快速看懂、快速接手

## 模块边界

### `t-uni-common`

职责：

- 返回结构
- 基础异常
- 基础校验
- Redis / MyBatis / Qiniu 等基础配置
- 通用工具类

约束：

- 只沉淀跨模块通用能力
- 不直接沉淀强业务逻辑

### `t-uni-server`

职责：

- 微信小程序登录
- Token 鉴权
- 用户基础模型
- 模板主 API
- 可选 OpenIM 基础能力

这是当前模板的主路径。

`server-services/server-im` 是可选模块，默认关闭。它只沉淀 OpenIM 的通用模板能力，包括配置下发、用户同步、token 获取、webhook 验签和系统通知直发，不复制具体社交业务逻辑。

`server-services/server-payment` 是可选微信支付模块，默认 `payment.wechat.enabled=false`。它只沉淀微信小程序 JSAPI 支付、退款、回调、支付核心表和业务 SPI，不复制具体业务订单、售后审批、分账、佣金等业务逻辑。

### `t-uni-admin`

职责：

- 可选后台后端
- RBAC、权限、系统管理、定时任务等历史能力

定位：

- 可选接入
- 不是模板主路径
- 可以吸收它的成熟能力，但不必强制和 `server` 完全统一
- 代码量较大，后续可考虑拆到独立仓库，主模板只保留接入说明

## 用户模型边界

模板默认保留双表模型：

- `core_user`
- `biz_user`

其中：

- `core_user` 负责核心身份与基础资料
- `biz_user` 负责业务扩展身份

硬约束：

- 两张表一对一
- 共用同一个 `id`
- 不建议改成单表模型

如果要改 `biz_user` 的名字，请改“默认业务用户表实现”，不要破坏这条边界。

## Redis key 边界

模板允许多个派生项目共用 Redis database 0，但不能共用同一组裸 key。所有应用自有 Redis 顶层 key 都应通过 `t.uni.redis.namespace` 加项目级 namespace，例如 `project-a::wx:token:1`。

约束：

- 业务代码和常量类只生成 Redis 逻辑 key，不手动拼物理 namespace
- `RedisTemplate` 顶层 key、Spring Cache prefix、SCAN pattern 和 raw Redis connection byte key 都必须 namespace-aware
- Hash field 不加 namespace，例如 `accessToken`、`refreshToken`、`userId`、`openId` 保持原样
- namespace 只影响 Redis key，不改变 JWT、微信 openId/unionId、OpenIM userID、七牛 object key、微信支付回调参数

## 认证边界

服务端当前主链路是：

- `wxLogin`
- `refreshToken`
- `AuthInterceptor`
- `UserContext`

模板级建议是：

- 把它看成“轻量 API 鉴权模式”
- 保持 `controller -> service -> mapper` 分层
- 不把历史兼容自愈逻辑当成模板主叙事

## `common-core` 的定位

`common-core` 应该只放“纯共享内核”：

- admin 和 server 都会用
- 不依赖运行时 Bean 装配
- 不带某个端的业务语义

更细的约束见 [common-core-guidelines.md](common-core-guidelines.md)。

## 暂不纳入主模板的能力

以下能力当前不直接塞进主模板：

- 消息通知

原因不是它们不重要，而是这几块通常带有较强业务上下文。

支付和 OpenIM 已按可选模块方式沉淀为最小模板能力，但支付业务订单、售后审批、分账佣金、社交私信、社交通知回流、内容审核和隐私策略仍不纳入主模板。后续规划见 [../TODO.md](../TODO.md)。
