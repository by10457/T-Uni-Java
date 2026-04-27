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

## 注释

注释默认使用中文，目标是解释职责、边界、约束和风险，不重复代码本身。

### 类注释

推荐：

- 业务类、配置类、Service、Controller、Client、SPI、Scheduler、ExceptionHandler 都写 JavaDoc。
- 类注释说明这个类负责什么、不负责什么，以及关键使用边界。
- Entity、DTO、VO、Constant、Enum 说明数据用途和使用场景。
- Mapper 只说明数据访问范围，不展开普通 CRUD 细节。

不推荐：

- 只把类名翻译成中文。
- 在注释里写已经过期的业务背景。
- 给 Lombok 自动生成的 getter/setter 补无意义说明。

### 方法注释

推荐：

- 接口方法、Controller 方法、Service public 方法、SPI 方法写 JavaDoc。
- 重点说明入参含义、返回结果、幂等性、事务边界和异常行为。
- private 方法只在逻辑复杂、状态转换敏感、外部依赖调用或容易误用时补注释。
- 支付、退款、鉴权、Token、回调、外部存储、OpenIM 等敏感流程，要说明关键状态转换和失败处理。

不推荐：

- 给简单字段赋值、普通对象转换、直接返回值写注释。
- 用注释复述方法名，例如“查询用户”“设置状态”。
- 在方法注释里承诺代码没有实现的行为。

### 字段和常量注释

推荐：

- DTO、VO、Entity 的关键字段写中文说明。
- 状态字段、金额字段、外部系统编号、时间字段必须说明单位或来源。
- 常量按业务分组说明含义，避免只重复变量名。

不推荐：

- 把所有字段都写成同一个模板句式。
- 注释中混入真实密钥、token、openid、手机号、证书内容。

### 方法内注释

推荐：

- 只解释为什么这么做，以及这里保护什么不变量。
- 在幂等、事务、金额校验、签名验签、兜底修复前补短注释。
- 注释要紧贴对应代码，代码变化时同步更新。

不推荐：

- 写“设置用户ID”“调用接口”“返回结果”这类显然注释。
- 用大段注释代替拆分清晰的方法。

### TODO 注释

推荐：

- TODO 必须写清责任方，例如“业务侧实现”“后续增强”“接入方配置”。
- 模板扩展点里的 TODO 可以保留，但要说明二次开发者应该补什么。

不推荐：

- 使用“待处理”“后面再说”“这里改一下”等无法落地的描述。
- 把已知缺陷长期留成 TODO，而不记录到 `TODO.md`。

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
