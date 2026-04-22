# T-Uni-Java

`T-Uni-Java` 是一个面向“微信小程序服务端 + 可选管理后台”的多模块 Java 模板仓库。

这个仓库的目标不是复刻 `wxy-server` 的全部业务，而是沉淀一套适合二次定制、适合 AI 接手、也适合开源发布的模板基线：

- 默认主路径聚焦 `t-uni-common` 和 `t-uni-server`
- `t-uni-admin` 保留为可选后台能力，不作为模板主叙事
- 保留 `core_user + biz_user` 双表模型，方便后续替换业务用户表
- 不内置 `social-server` / `hygiene-server` 那类强业务耦合实现

## 这份模板包含什么

- 小程序登录、Token 鉴权、用户基础模型
- `common-core` / `common-config` 基础设施
- MyBatis-Plus、Redis、Knife4j、微信小程序 SDK
- 可选的七牛云基础能力
- 可选的管理后台后端
- 面向 AI 的 Prompt / Skill / 上下文文档

## 这份模板当前不包含什么

- 业务域模型与业务表全量复制
- 默认接入支付、消息通知、IM
- 默认开放完整后台前端
- 把 `wxy-server` 的历史兼容逻辑直接照搬进主链路

这些暂不纳入主模板的事项，统一记录在 [TODO.md](TODO.md)。

## 模块结构

- `t-uni-common`
  说明：共享基础能力，包含返回结构、异常、校验、Redis / MyBatis / Qiniu 等配置。
- `t-uni-server`
  说明：小程序服务端主路径，重点是登录鉴权、用户基础模型、模板 API 能力。
- `t-uni-admin`
  说明：可选管理后台后端，适合需要后台管理时再接入。
- `init_sql`
  说明：数据库初始化脚本。`init.sql` 是服务端最小启动脚本。
- `docs`
  说明：开箱文档、架构文档、AI 上下文、规范说明。
- `prompts`
  说明：给 AI 直接使用的模板定制 Prompt。
- `.agent/skills`
  说明：仓库内统一的技能目录，供 AI 在本仓库中使用。

## 最小启动路径

1. 准备 `JDK 21`、`Maven 3.9+`、`MySQL 8`、`Redis`。
2. 初始化服务端数据库：
   执行 [init_sql/init.sql](init_sql/init.sql)。
3. 配置最小环境变量：
   `T_UNI_DB_*`、`T_UNI_REDIS_*`、`T_UNI_JWT_SECRET`、`WX_MINIAPP_APPID`、`WX_MINIAPP_SECRET`。
4. 启动服务端：
   `mvn -pl t-uni-server/server-api -am spring-boot:run`
5. 健康检查：
   `GET http://localhost:10457/api/health`

如果你还要启用管理后台后端，再额外：

1. 导入 [t-uni-admin/t_uni_test.sql](t-uni-admin/t_uni_test.sql)
2. 配置 `T_UNI_ADMIN_DB_*`、`T_UNI_ADMIN_REDIS_*`、`T_UNI_ADMIN_JWT_SECRET`
3. 启动：
   `mvn -pl t-uni-admin/admin-api -am spring-boot:run`

更完整的步骤见 [docs/quick-start.md](docs/quick-start.md)。

如果你更习惯使用 IDE：

- `server-api` 的启动类是 `TUniServerApplication`
- `admin-api` 的启动类是 `TUniAdminApplication`
- 推荐先在 IDEA 中分别建立两个 Spring Boot Run Configuration

如果你想快速起本地依赖，当前仓库还没有提供 `docker-compose`，但至少建议准备：

- 一个 `MySQL 8`
- 一个 `Redis`

## 文档导航

- [快速启动](docs/quick-start.md)
- [环境变量清单](docs/environment-variables.md)
- [架构与模块边界](docs/architecture.md)
- [认证与登录链路](docs/auth-flows.md)
- [微信小程序接入说明](docs/wechat-miniapp.md)
- [七牛云存储说明](docs/storage-qiniu.md)
- [状态码规范](docs/status-codes.md)
- [全局编码规范](docs/coding-standards.md)
- [common-core 放置规范](docs/common-core-guidelines.md)
- [AI 必读上下文](docs/ai-context.md)

## AI 资产

### Prompt

- [项目改名 Prompt](prompts/project-rename-java-template.prompt.md)
- [业务用户表改名 Prompt](prompts/rename-business-user-table.prompt.md)

### Skill

- [仓库技能目录](.agent/skills)
- [全局开发技能](.agent/skills/t-uni-java/SKILL.md)
- [项目改名技能](.agent/skills/project-rename-java-template/SKILL.md)
- [业务用户表改名技能](.agent/skills/rename-business-user-table/SKILL.md)

## 模板定制建议

### 1. 改项目名

不要只做字符串替换。优先使用 [项目改名 Prompt](prompts/project-rename-java-template.prompt.md)，按映射一次性处理：

- Maven 坐标
- 模块目录名
- Java 包名
- Mapper XML 全限定类名
- Spring 扫描路径
- 环境变量前缀
- 文档与展示文案

### 2. 改业务用户表

默认业务用户表是 `biz_user`，但它只是模板示例，不是强制模型。

替换时请使用 [业务用户表改名 Prompt](prompts/rename-business-user-table.prompt.md)，并遵守下面这条硬边界：

- `core_user` 负责核心身份
- 业务用户表负责业务扩展
- 两张表保持一对一、同主键 `id`
- 不要改成单表模型

### 3. 七牛云接入

当前七牛能力是可选装配，只有配置了 `qiniu.*` 才会启用。

现阶段模板更适合这样使用：

- 内部资源尽量存 `fileKey`
- 不要把签名下载 URL 直接落库
- 外部 URL 只保留在明确允许的字段中

更细的说明见 [docs/storage-qiniu.md](docs/storage-qiniu.md)。

## 当前已知注意事项

- 服务端默认端口是 `10457`
- 管理端默认端口是 `7840`
- `WX_AUTH_LOGIN_IDENTIFIER` 默认推荐保持 `MA_OPEN_ID`
- 默认昵称池有示例数据，但默认头像池没有远程图片种子，首次登录头像可能为空
- `t-uni-admin` 是可选能力，不接后台时可以只使用 `t-uni-server`

## 后续规划

支付、消息通知、IM 这类能力暂不直接塞进主模板，原因和后续模块化方案见 [TODO.md](TODO.md)。
