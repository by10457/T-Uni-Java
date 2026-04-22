# common-core Guidelines

## 一句话原则

`common-core` 只放“纯共享内核”：

- admin 和 server 都会用
- 不依赖 Spring Bean 装配
- 不直接绑定某一端业务语义
- 不直接接中间件客户端

## 什么应该放进来

- `Result`
- `PageResult`
- `BaseException`
- 少量真正公共的基础错误码
- 纯函数工具类
- 基础格式校验注解

## 什么不该放进来

- `@Configuration`
- `@Component`
- `@ConfigurationProperties`
- `@RestControllerAdvice`
- `RedisTemplate` 封装
- 中间件客户端 Bean
- 某个端专用 Redis Key
- 某个业务模块专用错误码
- admin 或 server 单端专用状态码

## 包级判断

### `config`

默认不建议放运行时配置类。

如果类需要容器装配，优先放 `common-config`。

### `constant`

只放跨端稳定协议常量。

不要把：

- `WX_*`
- `ADMIN_*`
- 存储厂商 key
- 具体 Redis key 前缀

都堆进来。

### `exception`

`BaseException` 这类基础异常可以保留。

但像全局异常处理器这种 Web 适配层能力，更适合放 `common-config`。

### `redis`

如果类直接依赖 `RedisTemplate`，原则上不应放在 `common-core`。

### `security`

如果类依赖运行时密钥、Spring 生命周期或外部配置，也更适合放 `common-config`。

### `utils`

只放纯函数工具类。

如果工具类依赖：

- 配置
- 缓存
- 上下文
- HTTP 请求
- 线程本地状态

就不要放在这里。

## 给 AI 的落包规则

新增一个类前，先问自己 3 个问题：

1. admin 和 server 会不会都用？
2. 这个类需不需要 Spring / Redis / 中间件装配？
3. 这个类有没有某个端或某个业务的强语义？

只有答案是：

- 会共用
- 不需要装配
- 没有强业务语义

才建议进入 `common-core`。
