# T-Uni-Java

`T-Uni-Java` 是一个面向微信小程序后端场景的多模块 Java 模板项目，包含小程序服务端与管理后台两个入口。

## 模块结构

- `t-uni-common`: 通用基础能力，包含核心工具、异常结果结构、Redis/MyBatis/MinIO/Qiniu 等配置。
- `t-uni-server`: 小程序服务端，重点提供微信登录、鉴权、用户基础模型等模板能力。
- `t-uni-admin`: 管理后台，作为模板附带的管理端能力，非本次模板主路径重点。
- `init_sql`: 数据库初始化脚本，`init.sql` 为模板最小启动脚本。

## 快速启动

1. 准备 JDK 21、MySQL 8、Redis。
2. 执行 [init.sql](/Users/xua/Code/WorkSpaces/T-Uni/T-Uni-Java/init_sql/init.sql) 初始化数据库。
3. 设置环境变量后启动 `t-uni-server/server-api` 或 `t-uni-admin/admin-api`。
4. 默认通过 `SPRING_PROFILES_ACTIVE=dev` 启动，生产环境请切到 `prod`。

## 关键环境变量

- 服务端数据库: `T_UNI_DB_HOST`、`T_UNI_DB_PORT`、`T_UNI_DB_NAME`、`T_UNI_DB_USERNAME`、`T_UNI_DB_PASSWORD`
- 服务端 Redis: `T_UNI_REDIS_HOST`、`T_UNI_REDIS_PORT`、`T_UNI_REDIS_DATABASE`、`T_UNI_REDIS_PASSWORD`
- 服务端 JWT: `T_UNI_JWT_SECRET`
- 微信小程序: `WX_MINIAPP_APPID`、`WX_MINIAPP_SECRET`
- 七牛云: `QINIU_ACCESS_KEY`、`QINIU_SECRET_KEY`、`QINIU_BUCKET`、`QINIU_DOMAIN`
- 管理端数据库: `T_UNI_ADMIN_DB_HOST`、`T_UNI_ADMIN_DB_PORT`、`T_UNI_ADMIN_DB_NAME`、`T_UNI_ADMIN_DB_USERNAME`、`T_UNI_ADMIN_DB_PASSWORD`
- 管理端 Redis: `T_UNI_ADMIN_REDIS_HOST`、`T_UNI_ADMIN_REDIS_PORT`、`T_UNI_ADMIN_REDIS_DATABASE`、`T_UNI_ADMIN_REDIS_PASSWORD`
- 管理端 JWT: `T_UNI_ADMIN_JWT_SECRET`

## 微信与七牛接入

- 微信小程序需要在微信公众平台获取 `AppID` 与 `AppSecret`。
- 七牛云配置默认支持私有下载签名、CDN 时间戳防盗链和服务端字节流上传。
- 如果不使用七牛 callback，可将 `QINIU_CALLBACK_URL` 留空。
- Qiniu 默认不自动启用，只有在实际提供 `qiniu.*` 配置后才会装配相关能力。

## 说明

- 模板默认保留最小可运行主链路，不携带 `wxy-server` 中的业务模块表结构。
- 若需要扩展业务用户表，可替换 `WxAuthConfig` 中注入的业务用户 Mapper。
