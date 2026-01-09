# 微信小程序登录与 Token 规范 (全新重构版)

## 核心策略
采用 **双 Token 机制** + **Python 风格两层缓存**，最大化 Redis 命中率，减少数据库压力。

## 1. 缓存架构 (两层缓存)

所有复杂对象使用 **Redis Hash** 存储，避免 JSON 字符串序列化。

### Layer 1: 用户信息缓存 (永久)
- **Key**: `wx:user:{openid}`
- **Type**: Redis Hash
- **Fields**: `userId`, `openId`, `unionId`
- **作用**: 建立 `openid` -> `userId` 的快速映射。

### Layer 2: Token 缓存 (永久 + 逻辑过期)
- **Key**: `wx:token:{userId}`
- **Type**: Redis Hash
- **Fields**:
  - `accessToken`: JWT 字符串
  - `refreshToken`: UUID 字符串
  - `expireTimeMs`: 绝对过期时间戳 (Long)
  - `openId`: 冗余字段
- **作用**: 存储用户当前的有效 Token。过期检查在代码逻辑中进行。

### 辅助索引 (刷新用)
- **Key**: `wx:refresh:index:{refreshToken}`
- **Type**: String
- **Value**: `userId`
- **作用**: 刷新 Token 时反查用户。

## 2. 登录流程 (WxAuthService)

1.  **前端请求**: `POST /auth/wx/login` (code)
2.  **微信交互**: `jscode2session` 获取 `openid`, `unionid`。
3.  **Layer 1 & 2 缓存检查 (快速路径)**:
    - 查 `wx:user:{openid}` 获取 `userId`。
    - 查 `wx:token:{userId}` 获取 Token 信息。
    - **命中且未过期**: 直接返回缓存的 TokenVO (无 DB 操作)。
4.  **DB 兜底 (缓存未命中)**:
    - 查询 `business_user` (按 openid 或 unionid)。
    - 不存在则注册 (创建 `core_user` + `business_user`)。
    - 存在则更新 last_login_time。
5.  **更新缓存**:
    - `hSetAll` 更新 Layer 1 用户信息。
    - 生成新 Token。
    - `hSetAll` 更新 Layer 2 Token 信息。
6.  **返回**: TokenVO (含 `expiresIn` 秒数)。

## 3. Token 机制

- **Access Token**: JWT (HS256), 仅包含 `userId` claim。
- **Refresh Token**: UUID (32位)，用于刷新。
- **有效期**: 默认 7 天 (可配置)。
- **刷新逻辑**:
    - 校验 Refresh Token 是否存在且匹配。
    - 检查剩余有效期。
    - 返回剩余有效期内的有效 Token (不无限续期，过期需重新登录)。

## 4. 代码实现类

- **服务接口**: `TokenService` (common)
- **实现类**: `TokenServiceImpl` (server-auth)
- **工具类**: `JwtTokenUtil` (common), `RedisUtil` (common)
- **常量**: `RedisKeyConstants` (common)
