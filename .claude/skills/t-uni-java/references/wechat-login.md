# 小程序登录与双 Token

## 参考文档
- 详细设计方案：`C:\Code\WorkSpaces\T-Uni\T-Uni-Java\.codex\skills\t-uni-java\WX_LOGIN_PLAN.md`

## 实现位置
- 认证服务：`t-uni-server/server-services/server-auth`
- API 入口：`t-uni-server/server-api`

## 关键技术
- **认证方式**：JWT + AuthInterceptor（双 Token 机制）
- **JWT 工具类**：
  - 通用：`t.uni.common.core.utils.JwtTokenUtil`
  - 小程序扩展：`t.uni.server.common.utils.ServerJwtTokenUtil`
- **主键规则**：`core_user` / `social_user` 使用 `IdType.ASSIGN_ID`（Long），保持主键一致
- **Redis key、Token 结构与接口路径**：以 `WX_LOGIN_PLAN.md` 为准

---

## IBusinessUser 策略模式（核心设计）

### 设计目标
使微信登录逻辑（登录、刷新Token、获取手机号）可在**不同业务场景**下复用，无需修改 `WxAuthServiceImpl` 核心代码。

### 架构概览

```
server-domain（领域层 - 接口定义）
├── IBusinessUser           # 业务用户接口（定义微信登录必需字段）
├── IBusinessUserMapper<T>  # 泛型 Mapper 接口（约束 T 实现 IBusinessUser）
└── SocialUser              # 默认实现（对应 social_user 表）

server-auth（认证服务 - 具体实现）
├── SocialUserMapper        # 继承 IBusinessUserMapper<SocialUser>
├── WxAuthConfig            # ★ 配置类：决定注入哪个 Mapper 实现
└── WxAuthServiceImpl       # 只依赖接口，不关心具体实现
```

### 核心组件

#### 1. `IBusinessUser` 接口
**位置**: `server-domain/src/main/java/t/uni/server/domain/auth/IBusinessUser.java`

必须实现的方法：

| 方法 | 说明 |
|------|------|
| `getId()` / `setId(Long)` | 用户ID（与 `core_user.id` 一对一关联） |
| `getUniqueId()` / `setUniqueId(String)` | 业务唯一ID（格式：`U` + Snowflake ID） |
| `getMaOpenId()` / `setMaOpenId(String)` | 微信小程序 openId |
| `getMpOpenId()` / `setMpOpenId(String)` | 微信公众号 openId |
| `getUnionId()` / `setUnionId(String)` | 微信 unionId |
| `getStatus()` / `setStatus(Integer)` | 关注状态（0/1） |

#### 2. `IBusinessUserMapper<T>` 接口
**位置**: `server-domain/src/main/java/t/uni/server/domain/auth/IBusinessUserMapper.java`

```java
public interface IBusinessUserMapper<T extends IBusinessUser> extends BaseMapper<T> {
}
```

#### 3. `WxAuthConfig` 配置类（切换入口）
**位置**: `server-auth/src/main/java/t/uni/server/auth/config/WxAuthConfig.java`

```java
@Bean
public IBusinessUserMapper<? extends IBusinessUser> businessUserMapper(
        SocialUserMapper socialUserMapper) {  // ← 切换这里
    return socialUserMapper;
}
```

---

## 新增业务场景操作指南

当需要复制项目到新业务（如教育类 `EduUser`、卫生类 `HygieneUser`）时，按以下步骤操作：

### Step 1: 创建业务用户实体

**位置**: `server-domain/src/main/java/t/uni/server/domain/entity/`

```java
import java.io.Serializable;

@Data
@TableName("edu_user")  // 对应数据库表名
public class EduUser implements IBusinessUser, Serializable, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;           // 与 core_user.id 相同

    private String uniqueId;
    private String maOpenId;
    private String mpOpenId;
    private String unionId;
    private Integer status;

    // ========== 业务特有字段（可扩展）==========
    private Long schoolId;     // 学校ID
    private String studentNo;  // 学号
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

### Step 2: 创建业务 Mapper

**位置**: `server-auth/src/main/java/t/uni/server/auth/mapper/`

```java
@Mapper
public interface EduUserMapper extends IBusinessUserMapper<EduUser> {
}
```

### Step 3: 创建数据库表

```sql
CREATE TABLE `edu_user` (
    `id` BIGINT NOT NULL COMMENT 'ID（与 core_user.id 一致）',
    `unique_id` VARCHAR(32) NOT NULL COMMENT '用户唯一ID',
    `ma_open_id` VARCHAR(64) DEFAULT NULL COMMENT '小程序openId',
    `mp_open_id` VARCHAR(64) DEFAULT NULL COMMENT '公众号openId',
    `union_id` VARCHAR(64) DEFAULT NULL COMMENT '微信unionId',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0未关注 1已关注',
    -- 业务特有字段
    `school_id` BIGINT DEFAULT NULL COMMENT '学校ID',
    `student_no` VARCHAR(32) DEFAULT NULL COMMENT '学号',
    -- 时间字段
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_unique_id` (`unique_id`),
    UNIQUE KEY `uk_ma_open_id` (`ma_open_id`),
    UNIQUE KEY `uk_union_id` (`union_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教育业务用户表';
```

### Step 4: 修改配置类（唯一改动点）

**位置**: `server-auth/src/main/java/t/uni/server/auth/config/WxAuthConfig.java`

```java
@Bean
public IBusinessUserMapper<? extends IBusinessUser> businessUserMapper(
        EduUserMapper eduUserMapper) {  // ← 改为新的 Mapper
    return eduUserMapper;
}
```

### Step 5: 更新 createNewUser 方法

**位置**: `WxAuthServiceImpl.createNewUser()`

需要将 `new SocialUser()` 改为 `new EduUser()`，或通过工厂模式动态创建。

**推荐做法**：在 `WxAuthConfig` 中增加实体工厂 Bean：

```java
@Bean
public Supplier<IBusinessUser> businessUserFactory() {
    return EduUser::new;  // 返回新实体的 Supplier
}
```

然后在 `WxAuthServiceImpl` 中注入使用。

---

## 设计优势

| 优势 | 说明 |
|------|------|
| **低耦合** | `WxAuthServiceImpl` 只依赖接口，不依赖具体实体 |
| **易切换** | 切换业务场景仅需修改 `WxAuthConfig`，无需改服务代码 |
| **可扩展** | 各业务实体可添加特有字段（如 `schoolId`、`hospitalId`） |
| **复用性** | 登录、刷新Token、获取手机号逻辑 100% 复用 |

---

## 注意事项

1. **主键关联**：业务用户表的 `id` 必须与 `core_user.id` 保持一致（手动设置）
2. **唯一索引**：`ma_open_id` 和 `union_id` 列需建立唯一索引
3. **登录标识配置**：通过 `wx.auth.login-identifier` 配置使用 `MA_OPEN_ID` 或 `UNION_ID`
4. **泛型强转**：`WxAuthServiceImpl` 中使用 `@SuppressWarnings("unchecked")` 处理泛型转换
