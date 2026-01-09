---
name: t-uni-java
description: 在 T-Uni-Java 仓库进行后端开发、接口调整、实体/Mapper/SQL 变更、微信小程序登录或支付相关实现时使用；强调模块边界、MyBatis-Plus、/api RPC 风格、Result 返回、ASSIGN_ID 主键、逻辑删除全局配置与双 Token 规则（以新代码规范为准）。
---

# T-Uni-Java 开发技能

## 使用范围与基调
- t-uni-common 为共享基础模块；t-uni-admin 为旧项目（历史代码可能不完全符合本规范）；t-uni-server 为新写模块（新代码严格按规范）。
- 新写代码遵循本规范；旧代码不强制重构，除非明确要求。

## 快速流程
1. 确认目标模块与边界（只通过 common 或对外模块依赖，避免跨模块直接引用内部包）。
2. 先读实体/Mapper/XML/配置，再补 DTO/VO 与 Service 实现。
3. 新接口遵循 API 路径前缀规范（`/api/{端标识}/{业务模块}/{操作}`），Controller 只写业务路径，`/api` 前缀自动添加。RPC 风格，优先 `@PostMapping`，返回 `Result<T>`，使用 `@Operation`。
4. 仅使用 MyBatis-Plus；复杂查询走 XML；查询构造器用 `Wrappers.<Entity>lambdaQuery()`，禁止 `new LambdaQueryWrapper<>()`。
5. 写操作加 `@Transactional(rollbackFor = Exception.class)`。
6. 依赖注入：新代码使用 `@RequiredArgsConstructor` + `private final`，禁止 `@Resource`/`@Autowired`。
7. 实体主键使用 `IdType.ASSIGN_ID`（Long）；逻辑删除字段 `isDeleted` 按全局配置；时间字段靠数据库默认值（不加 FieldFill）。
8. 业务层类型明确时用 `var`；日志与注释用中文。

## 参考文件（按需阅读）
- 架构、模块职责与 admin/server/common 关系：`references/architecture.md`。
- Controller/Service/Mapper/Entity/DTO/VO/异常规范与示例：`references/standards.md`。
- **API 路径前缀规范（端标识、业务模块划分）：`references/api-prefix.md`**。
- 数据库命名、逻辑删除与时间字段规则：`references/db.md`。
- 小程序登录/双 Token：`references/wechat-login.md`（全新重构版。
- 构建或运行命令：`references/commands.md`。
