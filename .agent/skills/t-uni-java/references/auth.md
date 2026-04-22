# 认证约束

## 当前主链路

- `POST /api/auth/wxLogin`
- `POST /api/auth/refreshToken`
- `AuthInterceptor`
- `UserContext`

## 必须保留的边界

- `core_user` 是核心身份表
- `biz_user` 是业务扩展表
- 两张表必须保持同一个 `id` 的一对一关系

## 不要过度放大旧兼容逻辑

- 不要把“自动补建用户表”当成模板卖点
- 写模板代码和文档时，优先描述干净的正常链路

## UserContext 指引

- 全局上下文聚焦通用登录用户信息
- 不要默认把 `openId`、`unionId`、`phone` 都塞进全局上下文模型
