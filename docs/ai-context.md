# AI Context

这份文档给未来接手这个仓库的 AI 用。

如果 AI 在没有读完这份上下文前就开始大改，极容易把模板边界改坏。

## 1. 仓库主路径

当前模板主路径是：

- `t-uni-common`
- `t-uni-server`

`t-uni-admin` 是可选后台能力，不是这次模板主叙事。

## 2. 不要机械复制具体业务项目

这个仓库是模板仓库，不是任何具体业务项目的代码搬运目标。

允许吸收的是（具体举例）：

- `QiniuStorageService` 的 CDN 签名、防盗链实现
- `GlobalExceptionHandler` 的异常分层策略
- `MaskUtil` 的脱敏工具
- `RedisUtil` 的 Pipeline 批量操作封装
- `JwtTokenUtil` 的双 Token 刷新机制
- MyBatis-Plus 分页、自动填充配置
- 构造器注入、统一返回结构等工程规范

不允许直接回流的是：

- social 业务耦合
- hygiene 业务耦合
- OpenIM 私信业务逻辑
- 具体支付业务订单逻辑
- 具体 notice 类型、页面路径、营销或运营规则

## 3. 业务用户模型边界不能破

模板默认保留：

- `core_user`
- `biz_user`

AI 在改业务用户表时：

- 可以替换默认业务用户表名字
- 可以替换默认实体、Mapper、SQL
- 但不要把双表边界改成单表
- 不要让业务用户表自己生成不同的主键

## 4. 不要把“自动补建逻辑”当成模板主能力

如果源码里存在：

- 缺 `biz_user` 自动补建
- 缺 `core_user` 自动补建

这类逻辑，不要在 Prompt、Skill、README 里把它当作模板主卖点。

模板默认叙事应该是正常创建链路，而不是历史脏数据自愈。

## 5. `common-core` 很容易被改坏

AI 必须先看：

- [common-core-guidelines.md](common-core-guidelines.md)

关键约束：

- 纯共享、纯 Java、纯语义才进 `common-core`
- 需要装配、需要中间件客户端的能力进 `common-config`
- 带端语义、带业务语义、带存储命名的能力进具体模块

## 6. Redis namespace 是模板隔离边界

多个模板派生项目可以共用 Redis database 0，但必须配置不同的 `T_UNI_REDIS_NAMESPACE`。AI 不要建议通过切 Redis DB 作为唯一方案，也不要只给认证 key 单独拼前缀；namespace 应覆盖应用自有 Redis 顶层 key、Spring Cache、SCAN pattern 和 raw connection byte key。

规则：

- 常量类只生成 Redis 逻辑 key
- 物理 namespace 由 `common-config` 的 Redis 配置统一处理
- Hash field 不加 namespace
- 不自动扫描、迁移或删除旧裸 key，避免误伤其他业务缓存
- 认证缓存写入失败或写后校验失败应 fail fast，不返回后续必然 `ACCESS_TOKEN_INVALID` 的 token

## 7. 七牛这条不能只看类名猜设计

当前模板 common 层已经有七牛能力，并且已经支持：

- `PUBLIC / PRIVATE` 访问策略
- `normalizeForStorage(...)`
- `resolveAccessUrl(...)`

但业务层还没有完全收敛到统一资源存储模型。

AI 不要假设：

- 所有资源字段都已经只存 `fileKey`
- 所有 URL 都是公有空间 URL
- 当前仓库已经有统一的 `StorageAccessResolver`

推荐方向是：

- 内部资源尽量存 `fileKey`
- 对外再转可访问 URL
- 不要假设所有历史字段都已经统一完成

## 8. 支付 / 通知 / IM 边界

当前阶段：

- 支付：已作为 `server-payment` 可选模块纳入，默认关闭，只提供微信小程序 JSAPI 支付/退款模板能力
- 通知：后续做轻量内核
- IM：已作为 `server-im` 可选模块纳入，默认关闭，只提供 OpenIM 最小接入能力

## 9. 状态码不要一上来硬重排

当前更重要的是：

- 统一异常出口
- 统一认证失败出口
- 统一鉴权失败出口

不是立刻把所有数字全改成另一套码段。

## 10. admin 专属能力不要回流 common

像下面这些能力：

- admin 邮件验证码
- admin 角色/路由后台提示
- admin 内部模板或邮箱配置错误

应该留在 `t-uni-admin` 自己的枚举或模块里，不要再塞回 `common-core`。

## 11. 碰 admin 代码时要谨慎

`t-uni-admin` 内部有较多历史代码，包括：

- 使用了 `AntPathRequestMatcher` 等已废弃 API
- 部分代码仍然使用字段注入
- 内部的 Spring Security 配置较复杂

AI 不应该用 admin 侧的代码风格指导 server 侧的新代码。如果 admin 和 server 有类似功能，以 server 侧的实现为准。
