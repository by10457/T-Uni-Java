# 开发规范与示例

## 目录
- 依赖注入
- Controller
- Service
- Mapper
- Entity
- DTO/VO
- 异常处理
- MyBatis-Plus 查询

## 依赖注入
- 新写代码使用 `@RequiredArgsConstructor` + `private final`。
- 旧 admin 代码存在 `@Resource`/`@Autowired`，不强制重构，除非明确要求。

## Controller
- 新接口采用 RPC 风格动词路径（如 `/api/user/getById`）。
- 路径统一 `/api` 前缀，优先使用 `@PostMapping`。
- 返回 `Result<T>`，使用 `@Operation` 描述接口。
- admin 旧代码可能使用 RESTful 的 `@GetMapping/@PutMapping/@DeleteMapping` 与 `public/private` 路径，保持现状。

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

## Service
- 接口继承 `IService<Entity>`。
- 实现类继承 `ServiceImpl<Mapper, Entity>`。
- 写操作使用 `@Transactional(rollbackFor = Exception.class)`。

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

## Mapper
- 继承 `BaseMapper<Entity>`。
- 复杂查询使用 XML 映射文件。
- 禁止使用 JPA Repository。

```java
@Mapper
public interface UserMapper extends BaseMapper<User> {
    List<UserVo> selectUserList(@Param("query") UserQueryDto query);
}
```

## Entity
- 使用 `@TableName` 指定表名。
- 主键统一使用 `@TableId(value = "id", type = IdType.ASSIGN_ID)`（Long）。
- 逻辑删除字段 `isDeleted` 按全局配置生效，不要求 `@TableLogic`。
- 时间字段命名 `createTime` / `updateTime`，由数据库默认值维护，新增实体不要使用 `@TableField(fill = ...)`。
- admin 旧代码存在 `BaseEntity` 自动填充字段，不强制改造。

```java
@Data
@TableName("core_user")
public class CoreUser implements Serializable {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String nickName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

## DTO/VO
- DTO 用于接收请求参数，使用 `@NotNull`、`@Size` 等校验注解。
- VO 用于返回响应数据，使用 `@Builder`。

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
- 业务异常使用 `BaseException`。
- 使用 `ResultCodeEnum` 定义错误码。

```java
if (user == null) {
    throw new BaseException(ResultCodeEnum.USER_NOT_FOUND);
}
```

## MyBatis-Plus 查询
- 使用 `Wrappers.<Entity>lambdaQuery()` 链式风格。
- 新代码禁止 `new LambdaQueryWrapper<>()`（旧代码不强制改造）。

```java
var user = userMapper.selectOne(Wrappers.<User>lambdaQuery()
        .eq(User::getUsername, username)
        .eq(User::getStatus, 1));
```
