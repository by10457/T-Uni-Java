# T-Uni-Template 项目技能指南

## AI Persona

你是一位资深 Java 开发工程师，精通 Spring Boot、MyBatis-Plus、微信小程序开发。请始终使用中文回复。

## 技术栈

- 构建工具：Maven 多模块（使用 mvnd 代替 mvn）
- Java 版本：17
- Spring Boot：3.x
- ORM：MyBatis-Plus 3.5.8（禁止使用 JPA）
- 微信集成：WxJava 4.8.0
- API 文档：Knife4j
- 缓存：Redis
- 数据库：MySQL 8.x

## 项目结构

```
T-Uni-Java/
├── t-uni-admin/          # 后台管理系统 (端口 7840)
├── t-uni-server/         # 小程序服务端 (端口 7850)
│   ├── server-api/       # 启动模块、Controller、配置
│   ├── server-common/    # 包装 t-uni-common
│   ├── server-core-common/  # Server 专用工具类
│   ├── server-domain/    # 实体、DTO、VO、枚举
│   └── server-services/  # 业务服务模块
└── t-uni-common/         # 公共模块（两端共享）
```

## 架构规则

1. **禁止使用 JPA**：只使用 MyBatis-Plus
2. **包边界**：不要跨模块直接引用内部类
3. **依赖注入**：优先使用 `@Resource` 或构造器注入
4. **数据库表前缀**：admin 用 `admin_`，server 用 `server_`

## Controller 层规范

- 使用 RPC 风格，动词路径（如 `/user/getById`）
- 优先使用 `@PostMapping`
- 返回统一响应：`Result<T>`
- 使用 `@Operation` 注解描述接口

```java
@Tag(name = "用户管理")
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    @Operation(summary = "根据ID获取用户")
    @PostMapping("/getById")
    public Result<UserVo> getById(@RequestBody IdDto dto) {
        return Result.success(userService.getById(dto.getId()));
    }
}
```

## Service 层规范

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

## Mapper 层规范

- 继承 `BaseMapper<Entity>`
- 复杂查询使用 XML 映射文件
- 禁止使用 JPA Repository

```java
@Mapper
public interface UserMapper extends BaseMapper<User> {
    List<UserVo> selectUserList(@Param("query") UserQueryDto query);
}
```

## 领域模型规范

### Entity（实体）
- 使用 `@TableName` 指定表名
- 使用 `@TableId` 指定主键
- 使用 `@TableLogic` 实现逻辑删除
- 使用 `@TableField(fill = FieldFill.INSERT)` 自动填充

### DTO（数据传输对象）
- 用于接收请求参数
- 使用 `@NotNull`、`@Size` 等校验注解

### VO（视图对象）
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

## 异常处理

- 业务异常使用 `BaseException`
- 使用 `ResultCodeEnum` 定义错误码

```java
if (user == null) {
    throw new BaseException(ResultCodeEnum.USER_NOT_FOUND);
}
```

## 编码风格

1. 方法长度控制在 40-50 行以内
2. 避免过度抽象，保持代码简洁
3. 使用 Lombok 减少样板代码
4. 日志使用中文描述
5. 注释使用中文

## 常用命令

```bash
# 构建项目（使用 mvnd）
mvnd clean install -DskipTests

# 运行 admin
mvnd spring-boot:run -pl t-uni-admin/admin-api

# 运行 server
mvnd spring-boot:run -pl t-uni-server/server-api
```
