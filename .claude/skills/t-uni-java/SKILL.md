---
name: t-uni-java
description: 适用于在 T-Uni-Java 仓库进行后端开发、接口调整、数据库实体/Mapper 变更、微信小程序登录与支付相关实现时使用；强调 MyBatis-Plus、/api 前缀 RPC 风格、Result 返回、模块边界与表前缀规则。
---

# T-Uni-Java 开发技能

## 概览
遵循本仓库既有结构与规范完成后端开发与重构，优先复用现有工具类与模块边界。

## 工作方式
1. 先确认目标模块（t-uni-admin / t-uni-server / t-uni-common），不要跨模块直接引用内部包。
2. 先读实体/Mapper/XML/配置，再写 DTO/VO 与 Service 实现。
3. Controller 使用 `/api` 前缀、RPC 风格路径、`@PostMapping`、`Result<T>`、`@Operation`。
4. ORM 只用 MyBatis-Plus，复杂查询走 XML。
5. 事务写操作使用 `@Transactional(rollbackFor = Exception.class)`。
6. **依赖注入使用 Lombok `@RequiredArgsConstructor` + `private final`**，禁止使用 `@Resource` 或 `@Autowired`。
7. 业务层类型明确时用 `var`；日志与注释用中文。

## 数据与命名
- 表前缀：admin 用 `sys_`；server 以业务域前缀为主（`server_` / `payment_` / `core_` / `social_` 等，以实体/SQL为准）。
- 时间字段优先使用 `createTime` / `updateTime` 命名，便于通用填充器工作。

## 微信登录相关
- 如涉及小程序登录/Token 刷新/用户信息查询，先对照 `WX_LOGIN_PLAN.md` 明确双 Token、Redis key、表结构与接口路径。
- 当前实现位于 `t-uni-server/server-services/server-auth` 与 `t-uni-server/server-api`；修改前先梳理依赖与 DTO/VO。

---

## 项目架构总览

### 核心技术栈
- **Java 17** + **Spring Boot 3.5.3**
- **Spring Security** + **JWT** （认证授权）
- **MyBatis Plus 3.5.8** （ORM框架）
- **MySQL 9.2.0** + **Redis** （数据存储）
- **WxJava 4.8.0** （微信开发SDK）
- **MinIO** （文件存储）
- **Quartz** （定时任务）
- **Knife4j** （API文档）

### 项目架构

```
T-Uni-Java/
├── t-uni-common/              # 通用基础模块（所有模块共享）
│   ├── common-core/           # 核心工具类
│   ├── common-config/         # 中间件配置
│   └── common-web/           # Web通用配置
├── t-uni-admin/              # 后台管理系统
│   ├── admin-domain/         # 领域模型
│   ├── admin-core-common/    # 核心通用工具
│   ├── admin-services/       # 业务服务
│   │   ├── system/           # 系统管理服务
│   │   ├── configuration/    # 配置管理服务
│   │   └── schedule/        # 定时任务服务
│   └── admin-api/            # API入口
└── t-uni-server/             # 小程序后端
    ├── server-common/        # 专属通用模块
    ├── server-core-common/  # 核心通用工具
    ├── server-domain/       # 领域模型
    ├── server-services/     # 业务服务
    │   ├── server-auth/      # 认证服务
    │   └── server-payment/ # 支付服务
    └── server-api/           # API入口
```

### 核心模块说明

#### 1. t-uni-common（通用基础模块）

##### common-core
**职责**：核心基础功能 - 异常处理、返回值封装、通用工具

**关键类**：
- `BaseException` - 基础异常类
- `GlobalExceptionHandler` - 全局异常处理器
- `Result<T>` - 统一返回值封装
- `PageResult<T>` - 分页返回值封装
- `ResultCodeEnum` - 返回码枚举
- `JwtTokenUtil` - JWT Token工具类（**所有模块统一使用**）
- `RedisUtil` - Redis工具类

##### common-config
**职责**：中间件配置模块 - MyBatis Plus、Redis、MinIO 等

**关键类**：
- `MybatisPlusConfig` - MyBatis Plus配置
- `RedisConfiguration` - Redis配置
- `MinioConfiguration` - MinIO配置
- `UserContextProvider` - 用户上下文提供者接口
- `MybatisPlusFieldConfig` - 字段自动填充配置

##### common-web
**职责**：Web通用配置 - Web配置、通用注解（待完善）

#### 2. t-uni-admin（后台管理系统）

##### admin-core-common
**职责**：Admin核心通用工具（依赖common-core和common-config）

**关键类**：
- `BaseContext` - 用户上下文管理
- `AdminUserContextProvider` - Admin用户上下文提供者
- 各种工具类：`IpUtil`、`FileUtil`、`ResponseUtil`等

#### 3. t-uni-server（小程序后端）

**端口**：7850
**文档地址**：http://localhost:7850/doc.html

**核心功能**：
- 用户、角色、权限管理
- 系统配置管理
- 定时任务管理
- 文件上传管理

**认证方式**：JWT + Spring Security + 策略模式登录

#### 3. t-uni-server（小程序后端）

**端口**：7850
**文档地址**：http://localhost:7850/doc.html

**核心功能**：
- 微信小程序登录
- 微信支付集成
- 用户管理
- 订单管理

**认证方式**：JWT（**已实现双Token机制**，详见 `server-core-common` 模块）
**登录方案**：详见 `WX_LOGIN_PLAN.md`（当前实现基于 `server_user`）

### 数据库设计

**数据库名称**：`t_uni`（Admin和Server共享）

**表命名规范**：
- Admin表前缀：`sys_`（如 `sys_user`、`sys_role`）
- Server表前缀：以业务域为主（`server_` / `payment_` / `core_` / `social_` 等，以实体/SQL为准）

**初始化脚本**：`init_sql/`（包含 core/social/edu/hygiene 等业务表及触发器，按模块需要选择执行）

---

## 开发规范详解

### AI Persona
你是一位资深 Java 开发工程师，精通 Spring Boot、MyBatis-Plus、微信小程序开发。请始终使用中文回复。

### 架构规则
1. **禁止使用 JPA**：只使用 MyBatis-Plus
2. **包边界**：仅通过对外模块或 common 模块引用，避免跨模块直接引用内部包
3. **依赖注入**：使用 Lombok `@RequiredArgsConstructor` + `private final` 进行构造器注入
4. **数据库表前缀**：admin 用 `sys_`；server 以业务域前缀为主（`server_` / `payment_` / `core_` / `social_` 等，以实体/SQL为准）
5. **接口前缀**：Controller 路径统一以 `/api` 开头

### Controller 层规范
- 使用 RPC 风格，动词路径（如 `/api/user/getById`）
- 优先使用 `@PostMapping`
- 返回统一响应：`Result<T>`
- 使用 `@Operation` 注解描述接口

```java
@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @Operation(summary = "根据ID获取用户")
    @PostMapping("/getById")
    public Result<UserVo> getById(@RequestBody IdDto dto) {
        return Result.success(userService.getById(dto.getId()));
    }
}
```

### Service 层规范
- 接口继承 `IService<Entity>`
- 实现类继承 `ServiceImpl<Mapper, Entity>`
- 写操作使用 `@Transactional(rollbackFor = Exception.class)`

```java
public interface IUserService extends IService<User> {
    UserVo getUserInfo(Long id);
}

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVo getUserInfo(Long id) {
        // 业务逻辑
    }
}
```

### Mapper 层规范
- 继承 `BaseMapper<Entity>`
- 复杂查询使用 XML 映射文件
- 禁止使用 JPA Repository
- **查询构造器使用 `Wrappers.<Entity>lambdaQuery()` 链式风格**

```java
@Mapper
public interface UserMapper extends BaseMapper<User> {
    List<UserVo> selectUserList(@Param("query") UserQueryDto query);
}
```

#### MyBatis Plus 查询最佳实践

**推荐风格**（使用 Wrappers 工具类）：
```java
// ✅ 推荐：使用 Wrappers.lambdaQuery() 链式风格
var user = userMapper.selectOne(Wrappers.<User>lambdaQuery()
    .eq(User::getUsername, username)
    .eq(User::getStatus, 1));

// ✅ 推荐：多条件查询
var users = userMapper.selectList(Wrappers.<User>lambdaQuery()
    .eq(User::getStatus, 1)
    .like(User::getUsername, keyword)
    .orderByDesc(User::getCreateTime));
```

**避免的风格**（传统 new 构造器方式）：
```java
// ❌ 避免：new LambdaQueryWrapper<>() 方式
var queryWrapper = new LambdaQueryWrapper<User>();
queryWrapper.eq(User::getUsername, username);
queryWrapper.eq(User::getStatus, 1);
var user = userMapper.selectOne(queryWrapper);
```

**优势**：
- 代码更简洁，一行搞定
- 更好的可读性，查询条件一目了然
- 符合现代 Java 链式调用的习惯
- 减少临时变量，提升代码整洁度

### 领域模型规范

#### Entity（实体）
- 使用 `@TableName` 指定表名
- **主键统一使用 `@TableId(value = "id", type = IdType.ASSIGN_UUID)` 雪花算法生成 ID**
- 使用 `@TableLogic` 实现逻辑删除
- **时间字段不使用自动填充，完全靠数据库层面的 `DEFAULT CURRENT_TIMESTAMP` 和 `ON UPDATE CURRENT_TIMESTAMP` 自动生成即可**
- **禁止在 Entity 的时间字段上使用 `@TableField(fill = FieldFill.INSERT)` 等填充注解**

**示例：**
```java
@Data
@TableName("core_user")
public class CoreUser implements Serializable {
    /**
     * 用户ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private Long id;

    private String nickName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

#### DTO（数据传输对象）
- 用于接收请求参数
- 使用 `@NotNull`、`@Size` 等校验注解

#### VO（视图对象）
- 用于返回响应数据
- 使用 `@Builder` 模式构建

```java
@Data
@Builder
public class UserVo {
    private Long id;
    private String nickname;
    private String avatar;
}
```

### 异常处理
- 业务异常使用 `BaseException`
- 使用 `ResultCodeEnum` 定义错误码

```java
if (user == null) {
    throw new BaseException(ResultCodeEnum.USER_NOT_FOUND);
}
```

### 编码风格
1. 方法长度控制在 40-50 行以内
2. 避免过度抽象，保持代码简洁
3. 使用 Lombok 减少样板代码
4. 业务层变量类型明确时使用 `var`
5. 日志使用中文描述
6. 注释使用中文

### 常用命令

```bash
# 构建项目（使用 mvnd）
mvnd clean install -DskipTests

# 运行 admin
mvnd spring-boot:run -pl t-uni-admin/admin-api

# 运行 server
mvnd spring-boot:run -pl t-uni-server/server-api
```

---

## 快速开始

### 启动Admin
```bash
cd t-uni-admin/admin-api
mvnd spring-boot:run
```

### 启动Server
```bash
cd t-uni-server/server-api
mvnd spring-boot:run
```

## 项目特色

1. **开箱即用**：提供完整的小程序+后台模板，快速开发
2. **架构清晰**：DDD分层架构，职责明确，易于扩展
3. **策略模式**：登录策略、支付策略，支持多种方式
4. **统一规范**：统一的返回值封装、异常处理、权限控制
5. **文档完善**：Knife4j自动生成API文档
6. **安全可靠**：JWT认证、环境变量注入敏感配置
7. **性能优化**：Redis缓存、连接池优化、异步处理

---

---

## 🔄 2026-01-08 重构记录

### 已完成的重构工作

1. **消除重复代码**
   - 删除了 `admin-core-common` 中的重复 `JwtTokenUtil.java` 文件
   - 更新了 admin 模块中 3 个文件的 JWT 导入语句，统一使用 `t.uni.common.core.utils.JwtTokenUtil`

2. **删除无用模块**
   - 完全删除了 `admin-common` 空模块
   - 更新了相关 pom.xml 文件以移除依赖引用

3. **创建缺失模块**
   - 新建了 `server-core-common` 模块，与 `admin-core-common` 保持架构对称
   - 创建了增强版的 `ServerJwtTokenUtil.java`，支持：
     - 小程序用户 token 生成
     - 双 token 机制（access + refresh）
     - 服务器端特有的用户信息提取（openid、昵称、头像）
   - 创建了 `TokenProvider.java` 安全处理类，提供：
     - Token 黑名单管理
     - Token 刷新机制
     - 安全上下文管理
     - 多环境密钥支持

4. **修复依赖关系**
   - 为 `admin-core-common` 添加了 `common-config` 依赖
   - 修正了 `ServerJwtTokenUtil.parseToken()` 方法访问权限
   - 确保所有模块依赖关系正确

### 重要发现

- **AuthConstant 位置正确**：`t.uni.server.domain.constant.AuthConstant` 位置恰当，不应移动到 common
- **JWT 工具统一**：所有模块现在统一使用 `common-core` 中的 `JwtTokenUtil`
- **模块结构规范**：每个模块都有自己的 `core-common` 模块，职责明确

### 开发注意事项

- **JWT 使用**：统一使用 `t.uni.common.core.utils.JwtTokenUtil`
- **服务器端增强**：小程序相关功能使用 `t.uni.server.core.utils.ServerJwtTokenUtil`
- **安全处理**：使用 `t.uni.server.core.security.TokenProvider` 进行 Token 管理
- **模块依赖**：遵循"不允许跨模块直接引用"原则

## 参考资料
- `WX_LOGIN_PLAN.md` - 微信登录实现计划（本文件夹内）
- `../../../pom.xml` - 项目依赖配置
- `../../../init_sql/` - 数据库初始化脚本
