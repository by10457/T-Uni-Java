# OpenIM 接入说明

`server-im` 是 T-Uni-Java 的可选 OpenIM 模板模块，默认关闭。

它的目标是提供“微信小程序后端 + IM”的最小可复用能力，而不是复制具体社交业务。

## 1. 模块边界

已包含：

- OpenIM 公开连接配置下发
- OpenIM 用户按需注册
- OpenIM user token 获取
- admin token JVM 内存缓存
- Webhook token 验签与统一放行
- 系统通知同步直发

不包含：

- 社交私信业务
- 社交通知类型与跳转路径
- 内容审核
- 隐私策略
- outbox、重试任务、消息状态机

## 2. 启用配置

默认 `OPENIM_ENABLED=false`，IM Controller、Service、Mapper 都不会注册。

启用时至少配置：

```bash
export OPENIM_ENABLED=true
export OPENIM_API_ADDRESS=http://localhost:10002
export OPENIM_WS_ADDRESS=ws://localhost:10001
export OPENIM_ADMIN_USER_ID=imAdmin
export OPENIM_ADMIN_SECRET=replace_with_openim_admin_secret
export OPENIM_WEBHOOK_TOKEN=replace_with_webhook_token
```

可选配置：

```bash
export OPENIM_USER_ID_PREFIX=tuni_
export OPENIM_SYSTEM_NOTICE_USER_ID=tuni_system
export OPENIM_SYSTEM_NOTICE_NICKNAME=系统通知
export OPENIM_SYSTEM_NOTICE_AVATAR=https://img.icons8.com/color/96/appointment-reminders.png
export OPENIM_DEFAULT_USER_AVATAR=https://img.icons8.com/color/96/user-male-circle--v1.png
```

## 3. 后端接口

### 获取 IM 配置

```text
GET /im/config
Authorization: Bearer <accessToken>
```

返回：

```json
{
  "apiAddr": "http://localhost:10002",
  "wsAddr": "ws://localhost:10001",
  "systemNoticeUserId": "tuni_system"
}
```

### 获取用户 IM Token

```text
POST /im/token
Authorization: Bearer <accessToken>
Content-Type: application/json
```

请求体：

```json
{
  "platformId": 6
}
```

说明：

- 后端使用当前登录用户 ID 生成 OpenIM userID，例如 `tuni_10001`
- 如果用户尚未注册到 OpenIM，会先按需注册
- `platformId` 使用 OpenIM 平台枚举，小程序端按实际 SDK 要求传入

## 4. 小程序侧调用顺序

1. 走模板原有微信登录，拿到 `accessToken`
2. 调用 `GET /im/config` 获取 `apiAddr`、`wsAddr`
3. 调用 `POST /im/token` 获取 OpenIM user token
4. 用 OpenIM SDK 初始化并登录

## 5. Webhook 配置

模板提供统一回调入口：

```text
POST /openim/webhook/{command}?token=<OPENIM_WEBHOOK_TOKEN>
```

行为：

- token 正确：统一放行
- token 错误或未配置：拒绝
- 不做内容审核、隐私策略和业务通知回流

在 OpenIM 管理端配置回调 URL 时，把不同 command 指向同一个后端路径即可。

## 6. 系统通知

业务代码可以注入：

```java
OpenImNoticeService openImNoticeService;
```

同步发送：

```java
openImNoticeService.sendSystemNotice(userId, "你有一条新的系统通知");
```

说明：

- 发送前会确保目标用户已同步到 OpenIM
- 失败直接抛异常
- 模板不内置 outbox 和自动重试

## 7. 默认关闭时的表现

`OPENIM_ENABLED=false` 或未配置时：

- `/im/config` 不存在
- `/im/token` 不存在
- `/openim/webhook/**` 不存在
- `server-im` 的 Service 和 Mapper 不注册

这保证了 IM 不影响模板最小启动路径。
