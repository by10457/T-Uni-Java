# Coding Standards

这份规范优先约束新代码，尤其是 `t-uni-common` 与 `t-uni-server`。

`t-uni-admin` 中的历史代码不要求一次性重构，但新增能力尽量向这里对齐。

## Controller

推荐：

- 类上使用 `@Tag`
- 类上使用 `@RestController`
- 类上使用 `@RequestMapping`
- 优先使用 `@RequiredArgsConstructor`
- 方法上使用 `@Operation`
- JSON 入参统一 `@RequestBody + @Valid / @Validated`
- 返回统一 `Result<T>` 或 `PageResult<T>`

不推荐：

- Controller 直接调 Mapper
- Controller 写业务判断
- Controller 手写通用异常捕获
- Controller 直接返回 Entity

## Service

推荐：

- 接口在 `service`
- 实现在 `service.impl`
- 写操作显式 `@Transactional(rollbackFor = Exception.class)`
- 业务失败统一抛 `BaseException(ResultCodeEnum.xxx)`
- 敏感日志必须脱敏

不推荐：

- 把事务放到 Controller
- 到处抛裸 `RuntimeException`
- 在 Service 中直接泄露底层异常消息给前端

## Mapper

推荐：

- 统一 `*Mapper`
- 继承 `BaseMapper<Entity>`
- 简单 CRUD 用 MyBatis-Plus
- 复杂查询走 XML

不推荐：

- 把业务规则下沉到 Mapper
- 在 Mapper 做事务

## Entity / DTO / VO

推荐：

- `Entity` 只面向持久化
- `DTO` 只面向请求
- `VO` 只面向响应
- 新增命名统一用大写后缀：`DTO`、`VO`

不推荐：

- DTO / VO / Entity 混着用
- 新代码继续混用 `Dto` / `DTO`

## 依赖注入

推荐：

- `@RequiredArgsConstructor`
- `private final`

不推荐：

- 新代码继续使用 `@Resource`
- 新代码继续使用字段注入 `@Autowired`

## 日志

推荐：

- 关键路径记 `info`
- 兜底修复、业务拒绝记 `warn`
- 外部依赖失败记 `error`
- 用户标识、手机号、token、openId 先脱敏再打印

## 路由

推荐：

- 使用全局 `api.prefix`
- Controller 中只写业务相对路径

不推荐：

- 新代码在 Controller 里硬编码全局 `/api`

## Auth

当前仓库实际上有两套认证模式：

- 轻量 API 鉴权：
  `Interceptor + TokenService + UserContext`
- 后台 RBAC：
  `Spring Security + PermissionTag + AuthorizationManager`

模板化时不要把两套模式硬糅在一个模块里。
