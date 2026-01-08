# 小程序登录与双 Token

- 先阅读 `C:\Code\WorkSpaces\T-Uni\T-Uni-Java\.codex\skills\t-uni-java\WX_LOGIN_PLAN.md`。
- 实现位置：`t-uni-server/server-services/server-auth` 与 `t-uni-server/server-api`。
- 认证方式：JWT + AuthInterceptor（双 Token 机制）。
- JWT 工具类：通用使用 `t.uni.common.core.utils.JwtTokenUtil`，小程序扩展使用 `t.uni.server.common.utils.ServerJwtTokenUtil`。
- 主键规则：core_user/social_user 使用 `IdType.ASSIGN_ID`（Long），保持主键一致。
- Redis key、Token 结构与接口路径以 `WX_LOGIN_PLAN.md` 为准。
