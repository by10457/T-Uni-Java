# Auth Flows

## 当前服务端主链路

当前模板已经具备这几段核心能力：

- 微信小程序登录：`POST /api/auth/wxLogin`
- 刷新 Token：`POST /api/auth/refreshToken`
- 受保护接口鉴权：`AuthInterceptor`
- 登录用户上下文：`UserContext`

## 推荐的模板理解方式

请把当前模板的认证主链路理解为：

- 正常登录
- 正常发 token
- 正常鉴权
- 正常读取当前登录用户

不要把历史脏数据修复或用户表自愈，当成模板主链路设计目标。

## 服务端登录流程

1. 小程序前端调用 `wx.login()`
2. 后端 `wxLogin` 用 `code` 换取 `openId / unionId`
3. 查询登录标识对应的业务用户
4. 不存在则创建 `core_user + biz_user`
5. 生成并缓存 `accessToken + refreshToken`
6. 返回 token

## 鉴权流程

1. 客户端调用受保护接口
2. 请求头带 `Authorization: Bearer <accessToken>`
3. `AuthInterceptor` 校验 token
4. 校验通过后写入 `UserContext`
5. Controller / Service 通过上下文读取当前用户
6. 请求结束后清理上下文

## 刷新流程

1. 客户端调用 `POST /api/auth/refreshToken`
2. 带上 `refreshToken`
3. 服务端校验 Redis 当前态和 refresh 过期时间
4. 重新生成 token 对

## 模板级约束

### 1. 保留双表边界

认证链路默认依赖：

- `core_user`
- `biz_user`

请保留这条边界，不要把业务用户表改成独立用户主表。

### 2. 不默认强调兼容补建逻辑

如果你在阅读源码时看到“自动补建业务用户 / 自动补建核心用户”这类逻辑，请把它当作实现细节或历史兜底，不要在开源模板文档、Prompt、Skill 里把它当成默认推荐能力。

### 3. `UserContext` 只放通用登录人信息

推荐放：

- `userId`
- `uniqueId`
- `username` 或展示名

不推荐默认放：

- `openId`
- `unionId`
- `phone`

这些更适合放在认证明细对象里，而不是全局上下文核心字段。

## 后续建议

从“模板产品化”的角度，认证闭环更理想的最终形态是：

- `login`
- `me`
- `refresh`
- `logout`
- 一个统一鉴权拦截器

当前模板已经覆盖其中大部分，但 `me / logout` 仍值得后续收敛成更显式的模板接口。
