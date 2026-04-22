---
name: t-uni-java
description: 在 T-Uni-Java 仓库内进行后端开发、模板整理、common/server 重构、认证链路调整、接口变更或共享模块设计时使用。这个技能会约束模块边界、代码规范、common-core 放置规则，以及默认的 core_user 加 biz_user 双表模型。
---

# T-Uni Java

## 概述

把这个技能当作在 `T-Uni-Java` 仓库内工作的默认操作指南。它的作用是让改动始终贴合这个仓库的模板目标，尤其是 `t-uni-common`、`t-uni-server`、认证链路以及共享规范。

## 先读这些参考文件

- 模块职责和边界：`references/architecture.md`
- controller/service/mapper/entity 规则：`references/standards.md`
- common-core 放置规则：`references/common-core.md`
- 登录、token 和用户模型约束：`references/auth.md`
- 常用命令和仓库检查：`references/commands.md`

## 硬规则

- 优先把 `t-uni-common` 和 `t-uni-server` 视为模板主路径，`t-uni-admin` 是可选且历史包袱较重的模块。
- 不要机械复制 `wxy-server` 的业务代码，只吸收通用能力。
- 除非用户明确重做身份模型，否则必须保留 `core_user + biz_user` 双表模型。
- 不要把“自动修复缺失用户表”的逻辑包装成模板主能力。
- 不要把 Spring 托管配置、RedisTemplate 封装或中间件客户端放进 `common-core`。
- 新代码统一使用 `@RequiredArgsConstructor` 和 `private final`，不要继续用字段注入。
- 新 API 代码统一走 `Result<T>` / `PageResult<T>` / `BaseException` 这套返回和异常链路。
- 端专属状态码放各自模块枚举（如 `AdminResultCodeEnum`），不要塞回 `common-core` 的 `ResultCodeEnum`。
- 七牛相关存储代码统一走 `QiniuStorageService` 提供的 `normalizeForStorage` / `resolveAccessUrl`，不要在业务层自行拼接签名 URL。

## 禁区文件

以下文件定义了模板核心骨架，**不要在日常开发中随意重构或删除**，除非用户明确要求：

- `IBusinessUser.java` — 业务用户通用接口
- `IBusinessUserMapper.java` — 业务用户通用 Mapper
- `WxAuthConfig.java` — 业务用户 Mapper 注入间接层
- `AuthInterceptor.java` — 认证拦截器
- `UserContext.java` — ThreadLocal 登录上下文
- `JwtKeyHolder.java` — JWT 密钥管理
- `GlobalExceptionHandler.java` — 全局异常出口
- `ResultCodeEnum.java` — 跨端共享状态码（不要往里加端专属码）

## 工作流程

1. 先判断任务属于 `common`、`server`，还是可选的 `admin`。
2. 修改前先读对应的 entity、mapper、XML、config 和 service。
3. 如果任务会改变模板边界，文档和 prompts 要和代码一起更新。
4. 如果任务其实是项目改名或业务用户表改名，就切到旁边两个技能：
   - `$project-rename-java-template`
   - `$rename-business-user-table`
5. 最后至少做一轮仓库检查，比如定向编译或 `rg` 扫描。
