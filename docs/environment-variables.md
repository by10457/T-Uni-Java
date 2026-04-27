# Environment Variables

## 服务端必填

| 变量名 | 说明 | 示例 |
| --- | --- | --- |
| `T_UNI_DB_HOST` | 服务端 MySQL Host | `localhost` |
| `T_UNI_DB_PORT` | 服务端 MySQL Port | `3306` |
| `T_UNI_DB_NAME` | 服务端数据库名 | `tuni` |
| `T_UNI_DB_USERNAME` | 服务端数据库用户名 | `root` |
| `T_UNI_DB_PASSWORD` | 服务端数据库密码 | `your_password` |
| `T_UNI_REDIS_HOST` | 服务端 Redis Host | `localhost` |
| `T_UNI_REDIS_PORT` | 服务端 Redis Port | `6379` |
| `T_UNI_REDIS_DATABASE` | 服务端 Redis DB | `0` |
| `T_UNI_REDIS_PASSWORD` | 服务端 Redis 密码 | 空字符串可留空 |
| `T_UNI_JWT_SECRET` | 服务端 JWT 密钥，至少 32 字节 | `replace_with_a_long_secret` |
| `WX_MINIAPP_APPID` | 微信小程序 AppID | `wx123...` |
| `WX_MINIAPP_SECRET` | 微信小程序 AppSecret | `abcd...` |

## 服务端可选

| 变量名 | 说明 |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | 默认 `dev` |
| `SERVER_PORT` | 覆盖服务端端口，默认 `10457` |
| `T_UNI_SERVER_LOG_LEVEL` | 服务端日志级别 |
| `ROOT_LOG_LEVEL` | 根日志级别 |
| `WX_MINIAPP_TOKEN` | 微信消息推送 token |
| `WX_MINIAPP_AES_KEY` | 微信消息加解密 key |
| `WX_AUTH_LOGIN_IDENTIFIER` | `MA_OPEN_ID` 或 `UNION_ID` |

## 七牛云可选

只有在你真的要启用七牛云时，才需要配置对应的 `qiniu.*`。

当前模板没有在默认 `application-*.yml` 中强制写死 `qiniu.*`，是为了避免“空配置也把七牛自动装配起来”。

建议配置：

| 变量名 | 说明 |
| --- | --- |
| `QINIU_ACCESS_KEY` | 七牛 Access Key |
| `QINIU_SECRET_KEY` | 七牛 Secret Key |
| `QINIU_BUCKET` | 空间名 |
| `QINIU_DOMAIN` | 绑定域名或 CDN 域名 |
| `QINIU_CDN_TIMESTAMP_KEY` | 可选，CDN 时间戳防盗链密钥 |

## OpenIM 可选

默认关闭。只有需要启用 IM 时，才设置 `OPENIM_ENABLED=true` 并补齐连接信息。

| 变量名 | 说明 | 示例 |
| --- | --- | --- |
| `OPENIM_ENABLED` | 是否启用 OpenIM，默认 `false` | `true` |
| `OPENIM_API_ADDRESS` | OpenIM REST API 地址 | `http://localhost:10002` |
| `OPENIM_WS_ADDRESS` | OpenIM WebSocket 地址 | `ws://localhost:10001` |
| `OPENIM_ADMIN_USER_ID` | OpenIM 管理员 userID | `imAdmin` |
| `OPENIM_ADMIN_SECRET` | OpenIM 管理员 secret | `openim_admin_secret` |
| `OPENIM_USER_ID_PREFIX` | 模板用户映射前缀，默认 `tuni_` | `tuni_` |
| `OPENIM_SYSTEM_NOTICE_USER_ID` | 系统通知账号 userID，默认 `tuni_system` | `tuni_system` |
| `OPENIM_SYSTEM_NOTICE_NICKNAME` | 系统通知账号昵称 | `系统通知` |
| `OPENIM_SYSTEM_NOTICE_AVATAR` | 系统通知账号头像 URL | `https://...` |
| `OPENIM_DEFAULT_USER_AVATAR` | 用户头像为空时的兜底 URL | `https://...` |
| `OPENIM_WEBHOOK_TOKEN` | OpenIM Webhook URL token | `replace_with_token` |

## 微信支付可选

默认关闭。只有需要启用微信小程序 JSAPI 支付和退款时，才设置 `WECHAT_PAY_ENABLED=true` 并补齐商户配置。

| 变量名 | 说明 | 示例 |
| --- | --- | --- |
| `WECHAT_PAY_ENABLED` | 是否启用微信支付，默认 `false` | `true` |
| `WECHAT_PAY_APP_ID` | 微信小程序 AppID，默认复用 `WX_MINIAPP_APPID` | `wx123...` |
| `WECHAT_PAY_MCH_ID` | 微信支付商户号 | `1900000000` |
| `WECHAT_PAY_MCH_SERIAL_NO` | 商户 API 证书序列号 | `123ABC...` |
| `WECHAT_PAY_API_V3_KEY` | APIv3 密钥 | `replace_with_api_v3_key` |
| `WECHAT_PAY_PRIVATE_KEY_PATH` | 商户私钥文件路径，和 `WECHAT_PAY_PRIVATE_KEY` 二选一 | `/secure/apiclient_key.pem` |
| `WECHAT_PAY_PRIVATE_KEY` | 商户私钥内容，和 `WECHAT_PAY_PRIVATE_KEY_PATH` 二选一 | `-----BEGIN PRIVATE KEY-----...` |
| `WECHAT_PAY_NOTIFY_BASE_URL` | 外网可访问的回调基础地址 | `https://api.example.com` |
| `WECHAT_PAY_PAY_NOTIFY_PATH` | 支付回调路径，默认 `/payment/notify/wechat/pay` | `/payment/notify/wechat/pay` |
| `WECHAT_PAY_REFUND_NOTIFY_PATH` | 退款回调路径，默认 `/payment/notify/wechat/refund` | `/payment/notify/wechat/refund` |
| `WECHAT_PAY_ORDER_EXPIRE_MINUTES` | 支付单过期分钟数，默认 `15` | `15` |

说明：

- `enabled=false` 时应用可正常启动。
- 调用支付接口时，如果配置不完整，会返回 `PAYMENT_CONFIG_MISSING`。
- JSAPI 支付依赖 `biz_user.ma_open_id`，用户缺少 openid 时会返回 `PAYMENT_OPENID_MISSING`。

## 管理端必填

| 变量名 | 说明 | 示例 |
| --- | --- | --- |
| `T_UNI_ADMIN_DB_HOST` | 管理端 MySQL Host | `localhost` |
| `T_UNI_ADMIN_DB_PORT` | 管理端 MySQL Port | `3306` |
| `T_UNI_ADMIN_DB_NAME` | 管理端数据库名 | `tuni_admin` |
| `T_UNI_ADMIN_DB_USERNAME` | 管理端数据库用户名 | `root` |
| `T_UNI_ADMIN_DB_PASSWORD` | 管理端数据库密码 | `your_password` |
| `T_UNI_ADMIN_REDIS_HOST` | 管理端 Redis Host | `localhost` |
| `T_UNI_ADMIN_REDIS_PORT` | 管理端 Redis Port | `6379` |
| `T_UNI_ADMIN_REDIS_DATABASE` | 管理端 Redis DB | `0` |
| `T_UNI_ADMIN_REDIS_PASSWORD` | 管理端 Redis 密码 | 空字符串可留空 |
| `T_UNI_ADMIN_JWT_SECRET` | 管理端 JWT 密钥，至少 32 字节 | `replace_with_a_long_secret` |

## 管理端存储可选

### 本地文件存储

| 变量名 | 说明 |
| --- | --- |
| `T_UNI_ADMIN_LOCAL_STORAGE_PATH` | 本地文件存储路径，默认 `/tmp/t-uni-admin/` |

### MinIO

| 变量名 | 说明 |
| --- | --- |
| `T_UNI_ADMIN_MINIO_ENDPOINT` | MinIO Endpoint |
| `T_UNI_ADMIN_MINIO_ACCESS_KEY` | MinIO Access Key |
| `T_UNI_ADMIN_MINIO_SECRET_KEY` | MinIO Secret Key |
| `T_UNI_ADMIN_MINIO_BUCKET` | MinIO Bucket 名 |

## 推荐做法

- 本地开发：先只配服务端最小变量
- 需要后台时：再补管理端变量
- 需要对象存储时：最后再补七牛或 MinIO

不要在开源模板里提交真实密钥、真实公网域名、真实回调地址。
