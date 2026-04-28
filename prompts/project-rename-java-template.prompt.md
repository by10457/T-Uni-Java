# Project Rename Prompt

把下面这份 Prompt 直接交给 AI 使用，用来把 `T-Uni-Java` 改成你的新项目名。

核心原则：AI 不要手工到处猜着改。先收集映射，再运行 `scripts/rename-project/` 下的跨平台改名脚本，最后用验证脚本和人工复核兜底。

## 使用前必须先补齐的映射

```text
display_name:
repo_slug:
group_id:
root_package:
config_prefix:
env_prefix:
server_db_name:
admin_db_name:
common_module:
server_module:
admin_module:
rename_admin:
```

说明：

- `rename_admin=false` 时，不要默认改 `t-uni-admin` 的模块结构和 `T_UNI_ADMIN_*`。
- 真实 `.env` 不会由脚本自动修改，只会处理 `.env.example`。
- 项目改名不等于业务用户表改名；`biz_user` 替换请使用单独 Prompt。

## Prompt

```text
你正在修改一个 Java 多模块模板仓库，请按“脚本驱动的结构化改名”方式执行，而不是做全文盲替换。

输入映射：
- display_name:
- repo_slug:
- group_id:
- root_package:
- config_prefix:
- env_prefix:
- server_db_name:
- admin_db_name:
- common_module:
- server_module:
- admin_module:
- rename_admin:

执行要求：
1. 如果映射不完整，先停下来补齐，不要猜。
2. 判断当前系统：
   - macOS / Linux / Git Bash：使用 `scripts/rename-project/rename-project.sh`
   - Windows PowerShell：使用 `scripts/rename-project/rename-project.ps1`
3. 先运行 dry-run，不允许跳过。
4. 展示 dry-run 摘要和人工确认项，等待用户确认后，才允许执行真实改名。
5. 真实执行后，运行对应验证脚本：
   - macOS / Linux / Git Bash：`scripts/rename-project/verify-rename.sh`
   - Windows PowerShell：`scripts/rename-project/verify-rename.ps1`
6. 对验证残留做人工复核，尤其检查：
   - `rename_admin=false` 时 admin 目录里的旧命名是否是故意保留
   - `@MapperScan` / `@ComponentScan` / 字符串包名判断
   - `pom.xml` 的 `finalName` 与 `mainClass`
   - XML mapper 的全限定类名
   - SQL 里的全限定类名
   - 环境变量前缀与 `spring.application.name`
   - Docker volume、OpenIM userID、MinIO bucket、LICENSE holder
7. 输出最终摘要时，必须区分：
   - 脚本已自动修改项
   - 验证脚本发现并已修复项
   - 仍需人工确认项
   - 风险项

macOS / Linux 示例：

```bash
./scripts/rename-project/rename-project.sh \
  --display-name "Wxy Server" \
  --repo-slug wxy-server \
  --group-id com.wxy \
  --root-package com.wxy \
  --config-prefix wxy \
  --env-prefix WXY \
  --server-db-name wxy \
  --admin-db-name wxy_admin \
  --common-module wxy-common \
  --server-module wxy-server \
  --admin-module wxy-admin \
  --rename-admin true \
  --dry-run
```

Windows PowerShell 示例：

```powershell
.\scripts\rename-project\rename-project.ps1 `
  -DisplayName "Wxy Server" `
  -RepoSlug wxy-server `
  -GroupId com.wxy `
  -RootPackage com.wxy `
  -ConfigPrefix wxy `
  -EnvPrefix WXY `
  -ServerDbName wxy `
  -AdminDbName wxy_admin `
  -CommonModule wxy-common `
  -ServerModule wxy-server `
  -AdminModule wxy-admin `
  -RenameAdmin true `
  -DryRun
```

约束：
- 不要绕过 dry-run。
- 不要把 `rename_admin` 默认视为 true。
- 不要擅自改 `/api` 路径前缀。
- 不要擅自改 `wx.auth` 的业务语义。
- 不要修改真实 `.env`。
- 不要自动迁移 Docker volume、OpenIM userID、MinIO bucket。
- 不要把项目改名和业务用户表改名混成一个任务。
```
