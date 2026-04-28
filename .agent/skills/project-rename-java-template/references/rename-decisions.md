# 改名前必须确认的决策项

开始执行脚本前先确认这些值：

- `display_name`
- `repo_slug`
- `group_id`
- `root_package`
- `config_prefix`
- `env_prefix`
- `server_db_name`
- `admin_db_name`
- `common_module`
- `server_module`
- `admin_module`
- `rename_admin`

不要猜这些值。`rename_admin=false` 时，`admin_module` 可以作为展示和验证参考保留，但脚本不会默认改 admin 模块结构。

## 默认映射示例

以 `T-Uni-Java → wxy-server` 为例的完整映射：

```text
display_name:    Wxy Server
repo_slug:       wxy-server
group_id:        com.wxy
root_package:    com.wxy
config_prefix:   wxy
env_prefix:      WXY
server_db_name:  wxy
admin_db_name:   wxy_admin
common_module:   wxy-common
server_module:   wxy-server
admin_module:    wxy-admin
rename_admin:    true
```

对应的转换结果：

| 改名维度 | 模板默认值 | 改后值 |
|---|---|---|
| Maven groupId | `t.uni` | `com.wxy` |
| 根包名 | `t.uni` | `com.wxy` |
| common 模块 | `t-uni-common` | `wxy-common` |
| server 模块 | `t-uni-server` | `wxy-server` |
| admin 模块 | `t-uni-admin` | `wxy-admin` |
| 环境变量前缀 | `T_UNI_` | `WXY_` |
| 配置前缀 | `t.uni.` | `wxy.` |
| spring.application.name | `t-uni-server` | `wxy-server` |
| 服务端数据库名 | `tuni` | `wxy` |
| 管理端数据库名 | `tuni_admin` | `wxy_admin` |

## 脚本参数映射

| 决策项 | Bash 参数 | PowerShell 参数 | 样例值 |
|---|---|---|---|
| `display_name` | `--display-name` | `-DisplayName` | `"Wxy Server"` |
| `repo_slug` | `--repo-slug` | `-RepoSlug` | `wxy-server` |
| `group_id` | `--group-id` | `-GroupId` | `com.wxy` |
| `root_package` | `--root-package` | `-RootPackage` | `com.wxy` |
| `config_prefix` | `--config-prefix` | `-ConfigPrefix` | `wxy` |
| `env_prefix` | `--env-prefix` | `-EnvPrefix` | `WXY` |
| `server_db_name` | `--server-db-name` | `-ServerDbName` | `wxy` |
| `admin_db_name` | `--admin-db-name` | `-AdminDbName` | `wxy_admin` |
| `common_module` | `--common-module` | `-CommonModule` | `wxy-common` |
| `server_module` | `--server-module` | `-ServerModule` | `wxy-server` |
| `admin_module` | `--admin-module` | `-AdminModule` | `wxy-admin` |
| `rename_admin` | `--rename-admin` | `-RenameAdmin` | `true` |

## 配置文件方式

可以复制 `scripts/rename-project/rename.example.env` 为本地配置文件，例如 `scripts/rename-project/rename.local.env`，再用脚本读取：

```bash
./scripts/rename-project/rename-project.sh --config scripts/rename-project/rename.local.env --dry-run
```

```powershell
.\scripts\rename-project\rename-project.ps1 -Config scripts\rename-project\rename.local.env -DryRun
```

本地配置文件不要提交到 Git。

## admin 改名范围（当 rename_admin=true 时）

如果 `rename_admin=true`，以下位置也必须纳入改名范围：

- `t-uni-admin/` 下所有子模块目录名
- admin 侧 `pom.xml` 的 artifactId、groupId
- admin 侧 Java 包名
- admin 侧 `@MapperScan`、`@ComponentScan` 路径
- admin 侧 MyBatis XML 的 namespace
- admin 侧环境变量前缀 `T_UNI_ADMIN_*` → `{ENV_PREFIX}_ADMIN_*`
- admin 侧 `spring.application.name`
- admin 侧 SQL schema 名称

如果 `rename_admin=false`，admin 旧命名不直接视为失败，但最终摘要必须单独列出让用户人工确认。
