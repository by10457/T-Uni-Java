# Quick Start

这份文档面向第一次 fork `T-Uni-Java` 的使用者。

如果你只想先把小程序服务端跑起来，优先看“服务端最小路径”。

## 1. 先决条件

- `JDK 21`
- `Maven 3.9+`
- `MySQL 8`
- `Redis`
- 微信小程序 `AppID / AppSecret`

可选：

- 七牛云
- 管理后台后端
- OpenIM
- 微信支付

### 快速起本地依赖（可选）

如果你还没有 MySQL 和 Redis，推荐直接用仓库根目录的 `docker-compose.yml`：

```bash
docker compose up -d
```

也可以手动用 Docker 启动：

```bash
docker run -d --name tuni-mysql -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=123456 \
  mysql:8

docker run -d --name tuni-redis -p 6379:6379 \
  redis:7-alpine
```

### 关于 MySQL 版本

`init.sql` 使用了 `utf8mb4_0900_ai_ci` 排序规则，这**要求 MySQL 8.0+**。如果使用 MySQL 5.7 或 MariaDB，需要手动替换为 `utf8mb4_general_ci`。

### 关于微信测试号

如果你还没有正式的小程序 AppID，可以先用微信测试号进行本地开发：

1. 访问 https://mp.weixin.qq.com/debug/cgi-bin/sandbox 申请测试号
2. 测试号的 `AppID` / `AppSecret` 与正式号用法一致
3. 注意：测试号环境下 `unionId` 通常拿不到，建议保持 `WX_AUTH_LOGIN_IDENTIFIER=MA_OPEN_ID`

## 2. 服务端最小路径

### Step 1. 初始化数据库

执行：

```sql
source init_sql/init.sql
```

默认会创建：

- `tuni`
- `core_user`
- `core_user_default_avatar`
- `core_user_default_nick_name`
- `biz_user`
- `core_payment_order`
- `core_payment_transaction`
- `core_payment_refund`
- `core_payment_notify_log`

说明：

- `biz_user` 只是模板默认业务用户表
- 如果你后续有自己的业务用户表，可以替换，但请保留 `core_user + 业务用户表` 双表边界
- 支付表是可选支付模块使用的通用 core 表，不绑定具体业务订单表

### Step 2. 配置环境变量

最少需要：

```bash
export T_UNI_DB_HOST=localhost
export T_UNI_DB_PORT=3306
export T_UNI_DB_NAME=tuni
export T_UNI_DB_USERNAME=root
export T_UNI_DB_PASSWORD=your_password

export T_UNI_REDIS_HOST=localhost
export T_UNI_REDIS_PORT=6379
export T_UNI_REDIS_DATABASE=0
export T_UNI_REDIS_NAMESPACE=tuni-local
export T_UNI_REDIS_PASSWORD=

export T_UNI_JWT_SECRET=replace_with_a_secret_at_least_32_bytes_long

export WX_MINIAPP_APPID=your_appid
export WX_MINIAPP_SECRET=your_secret
```

注意：

- `T_UNI_JWT_SECRET` 不能为空
- 长度至少 32 字节
- `T_UNI_REDIS_NAMESPACE` 用于隔离 Redis 顶层 key；多个模板派生项目共用 Redis 0 时必须配置成不同值
- 如果你把 `WX_AUTH_LOGIN_IDENTIFIER` 改成 `UNION_ID`，必须确认当前小程序场景稳定能拿到 `unionId`

本地同时启动多个模板项目且都使用 Redis 0 时，可以这样区分：

| 项目 | Redis DB | Redis namespace |
| --- | --- | --- |
| 项目 A | `0` | `project-a` |
| 项目 B | `0` | `project-b` |

```bash
# 项目 A
export T_UNI_REDIS_DATABASE=0
export T_UNI_REDIS_NAMESPACE=project-a

# 项目 B
export T_UNI_REDIS_DATABASE=0
export T_UNI_REDIS_NAMESPACE=project-b
```

### Step 3. 启动服务端

```bash
mvn -pl t-uni-server/server-api -am spring-boot:run
```

如果本地装的是 `mvnd`，也可以用：

```bash
mvnd -pl t-uni-server/server-api -am spring-boot:run
```

### Step 4. 验证服务端

健康检查：

```bash
curl http://localhost:10457/api/health
```

Swagger / Knife4j：

- `http://localhost:10457/doc.html`

### Step 5. 小程序最小联调

前端流程：

1. 小程序端调用 `wx.login()` 拿到 `code`
2. 调用 `POST /api/auth/wxLogin`
3. 请求体：

```json
{
  "code": "wx_login_code"
}
```

4. 成功后拿到：
   `accessToken`、`refreshToken`
5. 访问受保护接口时统一带：

```text
Authorization: Bearer <accessToken>
```

可先验证：

- `POST /api/user/getUserInfo`
- `POST /api/wechat/getPhone`
- `POST /api/auth/refreshToken`

## 3. 微信支付可选路径

默认 `payment.wechat.enabled=false`，不影响最小启动路径。需要接入微信小程序 JSAPI 支付时再启用。

### Step 1. 配置微信支付环境变量

```bash
export WECHAT_PAY_ENABLED=true
export WECHAT_PAY_APP_ID=$WX_MINIAPP_APPID
export WECHAT_PAY_MCH_ID=your_mch_id
export WECHAT_PAY_MCH_SERIAL_NO=your_mch_serial_no
export WECHAT_PAY_API_V3_KEY=your_api_v3_key
export WECHAT_PAY_PRIVATE_KEY_PATH=/secure/apiclient_key.pem
export WECHAT_PAY_NOTIFY_BASE_URL=https://api.example.com
```

也可以用 `WECHAT_PAY_PRIVATE_KEY` 直接传私钥内容，和 `WECHAT_PAY_PRIVATE_KEY_PATH` 二选一。

### Step 2. 实现业务支付处理器

业务模块实现 `PaymentBizHandler`，处理自己的业务锁单、支付成功、关单、退款成功和退款失败逻辑。

接口入口：

- `POST /api/payment/wechat/lock`
- `POST /api/payment/wechat/prepay`
- `GET /api/payment/orders/{orderNo}`

微信回调：

- `POST /api/payment/notify/wechat/pay`
- `POST /api/payment/notify/wechat/refund`

退款默认不开放用户接口，由业务或后台调用 `PaymentRefundService.applyRefund(...)`。

更完整说明见 [payment-wechat.md](payment-wechat.md)。

## 4. 管理端可选路径

如果你需要后台管理端后端，再额外做下面几步。

### Step 1. 初始化管理端数据库

导入：

- [t-uni-admin/t_uni_test.sql](../t-uni-admin/t_uni_test.sql)

默认数据库名：

- `tuni_admin`

### Step 2. 配置管理端环境变量

最少需要：

```bash
export T_UNI_ADMIN_DB_HOST=localhost
export T_UNI_ADMIN_DB_PORT=3306
export T_UNI_ADMIN_DB_NAME=tuni_admin
export T_UNI_ADMIN_DB_USERNAME=root
export T_UNI_ADMIN_DB_PASSWORD=your_password

export T_UNI_ADMIN_REDIS_HOST=localhost
export T_UNI_ADMIN_REDIS_PORT=6379
export T_UNI_ADMIN_REDIS_DATABASE=0
export T_UNI_ADMIN_REDIS_PASSWORD=

export T_UNI_ADMIN_JWT_SECRET=replace_with_a_secret_at_least_32_bytes_long
```

如果使用本地文件存储，可额外配置：

```bash
export T_UNI_ADMIN_LOCAL_STORAGE_PATH=/tmp/t-uni-admin/
```

如果使用 MinIO，可额外配置：

```bash
export T_UNI_ADMIN_MINIO_ENDPOINT=http://localhost:9000
export T_UNI_ADMIN_MINIO_ACCESS_KEY=minioadmin
export T_UNI_ADMIN_MINIO_SECRET_KEY=minioadmin
export T_UNI_ADMIN_MINIO_BUCKET=tuni-admin
```

### Step 3. 启动管理端

```bash
mvn -pl t-uni-admin/admin-api -am spring-boot:run
```

默认端口：

- `7840`

### Step 4. 验证管理端

默认账号：

- 用户名：`Administrator`
- 密码：`admin123`

登录接口：

```text
POST /api/user/login
```

最小请求体：

```json
{
  "username": "Administrator",
  "password": "admin123",
  "type": 1
}
```

## 5. 常见坑

### JWT 密钥错误

表现：

- 服务启动失败
- 提示 JWT 密钥未配置或长度不足

处理：

- 检查 `T_UNI_JWT_SECRET` / `T_UNI_ADMIN_JWT_SECRET`
- 确保长度至少 32 字节

### 微信登录失败

优先检查：

- `WX_MINIAPP_APPID`
- `WX_MINIAPP_SECRET`
- 小程序 `code`
- 当前小程序环境和后台配置是否匹配

### `UNION_ID` 登录时报错

如果报：

```text
当前配置要求使用 unionId 登录，但本次未获取到 unionId
```

处理：

- 把 `WX_AUTH_LOGIN_IDENTIFIER` 改回 `MA_OPEN_ID`
- 或先确认当前主体下确实能稳定返回 `unionId`

### 首次登录头像为空

这是当前模板的已知行为：

- 默认昵称池有示例数据
- 默认头像池没有远程图片种子

它不影响登录，但会影响用户体验。建议你在正式使用前先补头像池数据。

## 5. 下一步

- 看 [environment-variables.md](environment-variables.md)
- 看 [wechat-miniapp.md](wechat-miniapp.md)
- 看 [storage-qiniu.md](storage-qiniu.md)
- 看 [architecture.md](architecture.md)
- 如果你要改项目名或改业务用户表，直接用 `prompts/` 目录里的 Prompt
