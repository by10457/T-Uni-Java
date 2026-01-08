# 数据库约定

- 数据库名称：`t_uni`（Admin 与 Server 共享）。
- 表前缀：Admin 主要使用 `sys_`；Server 以业务域前缀为主（`server_` / `payment_` / `core_` / `social_` 等）。Admin 旧项目还存在 `log_` / `view_` / `qrtz_` 前缀，保持现状。
- 逻辑删除：字段 `isDeleted` + 全局配置（`mybatis-plus.global-config.db-config`），新实体需包含 `isDeleted` 字段，不强制使用 `@TableLogic`。
- 时间字段命名：`createTime` / `updateTime`；由数据库 `DEFAULT CURRENT_TIMESTAMP` / `ON UPDATE CURRENT_TIMESTAMP` 维护，新代码不使用 FieldFill。
- 初始化脚本目录：`init_sql/`（含 core/social/edu/hygiene 等业务表与触发器，按模块选择执行）。
