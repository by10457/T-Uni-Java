# WeChat Mini Program

## 必填配置

当前模板服务端通过 `application-wx.yml` 读取微信小程序配置。

至少需要：

- `WX_MINIAPP_APPID`
- `WX_MINIAPP_SECRET`

可选：

- `WX_MINIAPP_TOKEN`
- `WX_MINIAPP_AES_KEY`

## 登录标识选择

当前模板支持两种登录标识：

- `MA_OPEN_ID`
- `UNION_ID`

推荐默认保持：

```text
WX_AUTH_LOGIN_IDENTIFIER=MA_OPEN_ID
```

只有在你确认当前小程序主体下稳定能拿到 `unionId` 时，才建议切到：

```text
WX_AUTH_LOGIN_IDENTIFIER=UNION_ID
```

否则你可能会遇到：

```text
当前配置要求使用 unionId 登录，但本次未获取到 unionId
```

## 小程序平台侧还要配什么

除了后端环境变量，还要在微信公众平台补平台配置。

至少要配置：

- request 合法域名

如果你启用了上传或下载能力，还要额外配置：

- upload 合法域名
- download 合法域名

如果你接手机号能力，也要确认对应的小程序能力已开通。

## 最小联调流程

1. 小程序前端调用 `wx.login()`
2. 后端调用 `POST /api/auth/wxLogin`
3. 请求体：

```json
{
  "code": "wx_login_code"
}
```

4. 成功后返回：

- `accessToken`
- `refreshToken`

5. 后续访问需要登录的接口时，统一带：

```text
Authorization: Bearer <accessToken>
```

## 获取手机号

模板当前提供：

- `POST /api/wechat/getPhone`

使用前提：

- 用户已登录
- 前端已拿到微信手机号能力 `code`

## 常见问题

### 微信登录失败

优先检查：

- `AppID / AppSecret`
- 小程序环境是否匹配
- 前端 `code` 是否有效

### 登录能成功，但业务接口报未登录

优先检查：

- 是否带了 `Authorization: Bearer ...`
- 是否把 token 写错成 `refreshToken`
- 服务端 Redis 是否正常
