# Architecture

## 模板目标

`T-Uni-Java` 的目标不是做一个“什么都内置”的大仓库，而是提供一套适合二次定制的后端模板基线。

核心原则：

- 优先沉淀 `common` 与 `server` 的通用能力
- `admin` 作为可选后台能力保留，但不反向绑架模板主路径
- 不机械复制 `wxy-server` 的业务实现
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

这是当前模板的主路径。

### `t-uni-admin`

职责：

- 可选后台后端
- RBAC、权限、系统管理、定时任务等历史能力

定位：

- 可选接入
- 不是模板主路径
- 可以吸收它的成熟能力，但不必强制和 `server` 完全统一

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

- 支付
- 消息通知
- IM

原因不是它们不重要，而是当前 `wxy-server` 中这几块都带有较强业务上下文。

后续会按模块化方式处理，见 [../TODO.md](../TODO.md)。
