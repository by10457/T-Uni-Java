# 微信小程序登录全流程实现计划

> 基于 T-Uni-Java 项目规范，遵循 MyBatis-Plus + RPC 风格 Controller
>
> **代码风格**：业务层中，当变量类型已明确时使用 `var` 关键字

## 一、需求概述

实现微信小程序完整登录流程：

| 接口 | 功能 | 响应内容 |
|-----|------|---------|
| 登录/注册 | 微信 code 换取双 Token | 仅返回双 Token + 过期时间 |
| Token 刷新 | refresh_token 换新 Token | 仅返回双 Token + 过期时间 |
| 用户信息 | 查询当前用户详情 | userId、uniqueId、nickname、avatar、gender、isNewUser |

**关键决策**：
- 复用 `social_user` 表（不新建 `social_user_wx`）
- 通过**主键 id** 关联 `core_user` 和 `social_user`（使用 MyBatis-Plus UUID 生成）
- 移除 Spring Security 依赖，使用自定义拦截器 + JWT
- `isNewUser` 判定：`core_user.create_time` 与当前时间差 ≤ 2分钟

## 二、架构设计

### 2.1 数据表关系

```
┌─────────────────┐       id        ┌─────────────────┐
│   core_user     │◄───────────────►│   social_user   │
│  (核心用户表)    │    (主键关联)    │  (社交用户表)    │
│  id (UUID)      │                 │  id (UUID)      │
└─────────────────┘                 └─────────────────┘
        │
        │ 加权随机分配
        ▼
┌─────────────────────────────────────────────────────┐
│  core_user_default_avatar / core_user_default_nick_name │
└─────────────────────────────────────────────────────┘
```

**说明**：core_user 和 social_user 的主键 id 保持一致，通过 id 进行关联。

### 2.2 双 Token 机制

| Token 类型 | 格式 | 有效期 | 存储 |
|-----------|------|--------|------|
| Access Token | JWT | 2小时 | 客户端 |
| Refresh Token | UUID | 7天 | Redis + 客户端 |

### 2.3 Redis Key 设计

```
wx:auth:refresh:{id}  -> refreshToken (TTL: 7天)
```

### 2.4 MyBatis-Plus UUID 配置

```java
// 在 CoreUser 和 SocialUser 实体中配置
@TableId(type = IdType.ASSIGN_UUID)
private String id;
```

## 三、API 接口设计（RPC 风格）

### 3.1 登录接口

```
POST /wx/auth/login
Request:  { "code": "微信登录code" }
Response: {
    "accessToken": "jwt...",
    "refreshToken": "uuid...",
    "accessTokenExpireTime": "2026-01-08T17:54:00",
    "refreshTokenExpireTime": "2026-01-15T15:54:00"
}
```

### 3.2 Token 刷新接口

```
POST /wx/auth/refreshToken
Request:  { "refreshToken": "uuid..." }
Response: {
    "accessToken": "新jwt...",
    "refreshToken": "新uuid...",
    "accessTokenExpireTime": "2026-01-08T19:54:00",
    "refreshTokenExpireTime": "2026-01-15T17:54:00"
}
```

### 3.3 用户信息接口

```
POST /wx/user/getUserInfo
Header: Authorization: Bearer {accessToken}
Response: {
    "userId": 123,
    "uniqueId": "U123456",
    "nickname": "昵称",
    "avatar": "头像URL",
    "gender": 0,
    "isNewUser": true
}
```

## 四、数据表调整

### 4.1 表结构说明

- `core_user` 和 `social_user` 表使用现有结构，无需修改
- 两表的主键 `id` 字段将通过 MyBatis-Plus 的 `@TableId(type = IdType.ASSIGN_UUID)` 自动生成 UUID
- 登录时，core_user 和 social_user 使用相同的 UUID 作为主键，实现一对一关联

## 五、文件清单

### 5.1 新建文件

#### Domain 层 (server-domain)

| 文件 | 说明 |
|-----|------|
| `entity/CoreUser.java` | 核心用户实体（@TableId UUID） |
| `entity/SocialUser.java` | 社交用户实体（@TableId UUID） |
| `entity/CoreUserDefaultAvatar.java` | 默认头像实体 |
| `entity/CoreUserDefaultNickName.java` | 默认昵称实体 |
| `dto/auth/RefreshTokenDTO.java` | Token刷新请求 |
| `vo/auth/TokenVO.java` | Token响应（登录/刷新共用） |
| `vo/auth/UserInfoVO.java` | 用户信息响应 |
| `constant/AuthConstant.java` | 认证常量 |

#### Service 层 (server-auth)

| 文件 | 说明 |
|-----|------|
| `mapper/CoreUserMapper.java` | 核心用户Mapper |
| `mapper/SocialUserMapper.java` | 社交用户Mapper |
| `mapper/CoreUserDefaultAvatarMapper.java` | 默认头像Mapper |
| `mapper/CoreUserDefaultNickNameMapper.java` | 默认昵称Mapper |
| `service/ITokenService.java` | Token服务接口 |
| `service/impl/TokenServiceImpl.java` | Token服务实现 |
| `service/IUserDefaultService.java` | 用户默认值服务接口 |
| `service/impl/UserDefaultServiceImpl.java` | 用户默认值服务实现 |
| `service/IUserInfoService.java` | 用户信息服务接口 |
| `service/impl/UserInfoServiceImpl.java` | 用户信息服务实现 |
| `util/WeightedRandomSelector.java` | 加权随机选择器 |

#### API 层 (server-api)

| 文件 | 说明 |
|-----|------|
| `controller/user/UserController.java` | 用户信息Controller |
| `interceptor/AuthInterceptor.java` | 认证拦截器 |
| `config/WebMvcConfig.java` | Web配置 |
| `context/UserContext.java` | 用户上下文(ThreadLocal) |

### 5.2 修改文件

| 文件 | 修改内容 |
|-----|---------|
| `server-api/pom.xml` | 移除 spring-boot-starter-security 依赖 |
| `WxAuthService.java` | 添加 refreshToken 方法声明 |
| `WxAuthServiceImpl.java` | 重构登录逻辑，使用 core_user + social_user |
| `WxAuthController.java` | 添加 refreshToken 接口 |

### 5.3 删除文件

| 文件 | 原因 |
|-----|------|
| `entity/ServerUser.java` | 改用 CoreUser + SocialUser |
| `mapper/ServerUserMapper.java` | 改用新 Mapper |
| `vo/auth/WxLoginVO.java` | 改用 TokenVO |

## 六、实现步骤

### 阶段一：基础设施

1. 创建 `AuthConstant.java` 常量类
2. 移除 `server-api/pom.xml` 中的 Security 依赖

### 阶段二：实体和数据访问层

3. 创建 Entity：`CoreUser`、`SocialUser`（配置 `@TableId(type = IdType.ASSIGN_UUID)`）
4. 创建 Entity：`CoreUserDefaultAvatar`、`CoreUserDefaultNickName`
5. 创建 Mapper：4个 Mapper 接口
6. 创建 `WeightedRandomSelector.java` 加权随机工具

### 阶段三：服务层

7. 创建 `ITokenService` + `TokenServiceImpl`（双Token生成/刷新/验证）
8. 创建 `IUserDefaultService` + `UserDefaultServiceImpl`（加权随机分配）
9. 重构 `WxAuthServiceImpl`（使用 id 主键关联）
10. 创建 `IUserInfoService` + `UserInfoServiceImpl`

### 阶段四：API层

11. 创建 `UserContext.java`（ThreadLocal存储当前用户）
12. 创建 `AuthInterceptor.java`（JWT验证拦截器）
13. 创建 `WebMvcConfig.java`（注册拦截器）
14. 修改 `WxAuthController`，新建 `UserController`

### 阶段五：DTO/VO

15. 创建 `RefreshTokenDTO`、`TokenVO`、`UserInfoVO`

## 七、核心代码逻辑

### 7.1 登录流程

```
1. 接收微信 code
2. 调用 wxMaService.getUserService().getSessionInfo(code)
3. 查询 social_user (by ma_open_id)
4. 用户不存在:
   - 生成 UUID 作为主键 id（MyBatis-Plus 自动生成）
   - 从默认池加权随机获取头像和昵称
   - 创建 core_user 记录（id = UUID）
   - 创建 social_user 记录（id = 相同的 UUID）
5. 用户存在:
   - 查询对应的 core_user（通过 social_user.id）
6. 生成双 Token (access + refresh)
7. 将 refresh_token 存入 Redis（key: wx:auth:refresh:{id}）
8. 返回 TokenVO
```

### 7.2 加权随机算法

```java
// 计算总权重 -> 生成随机数 -> 累加权重直到超过随机数
var totalWeight = items.stream().mapToInt(Item::getWeight).sum();
var random = ThreadLocalRandom.current().nextInt(totalWeight);
var cumulative = 0;
for (var item : items) {
    cumulative += item.getWeight();
    if (random < cumulative) return item;
}
```

### 7.3 isNewUser 判定逻辑

```java
// 在 UserInfoServiceImpl 中
var isNewUser = ChronoUnit.MINUTES.between(
    coreUser.getCreateTime(),
    LocalDateTime.now()
) <= 2;
```

## 八、关键文件路径

需要重点修改的现有文件：
- `t-uni-server/server-api/pom.xml`
- `t-uni-server/server-services/server-auth/src/main/java/t/uni/server/auth/service/impl/WxAuthServiceImpl.java`
- `t-uni-server/server-api/src/main/java/t/uni/server/api/controller/auth/WxAuthController.java`

参考的表结构文件：
- `init_sql/core_user.sql`
- `init_sql/social_user.sql`
- `init_sql/core_user_default_avatar.sql`
- `init_sql/core_user_default_nick_name.sql`
