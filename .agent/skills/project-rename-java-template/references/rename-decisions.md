# 改名前必须确认的决策项

开始编辑前先确认这些值：

- `display_name`
- `repo_slug`
- `group_id`
- `root_package`
- `config_prefix`
- `env_prefix`
- `server_db_name`
- `admin_db_name`
- `module_map`
- `rename_admin`

不要猜这些值。

## 默认映射示例

以 `T-Uni-Java → wxy-server` 为例的完整映射：

```text
display_name:    wxy-server
repo_slug:       wxy-server
group_id:        com.wxy
root_package:    com.wxy
config_prefix:   wxy
env_prefix:      WXY
server_db_name:  wxy
admin_db_name:   wxy_admin
module_map:
  - old: t-uni-common    new: wxy-common
  - old: t-uni-server    new: wxy-server
  - old: t-uni-admin     new: wxy-admin
rename_admin:    true
dry_run:         true
```

对应的转换结果：

| 改名维度 | 模板默认值 | 改后值 |
|---------|----------|-------|
| Maven groupId | `t.uni` | `com.wxy` |
| 根包名 | `t.uni` | `com.wxy` |
| common-core 包 | `t.uni.common.core` | `com.wxy.common.core` |
| server 包 | `t.uni.server` | `com.wxy.server` |
| 环境变量前缀 | `T_UNI_` | `WXY_` |
| 配置前缀 | `t.uni.` | `wxy.` |
| spring.application.name | `t-uni-server` | `wxy-server` |
| 数据库名 | `tuni` | `wxy` |
| 启动类名 | `TUniServerApplication` | `WxyServerApplication` |

## admin 改名范围（当 rename_admin=true 时）

如果 `rename_admin=true`，以下位置也必须纳入改名范围：

- `t-uni-admin/` 下所有子模块目录名
- admin 侧 `pom.xml` 的 artifactId、groupId
- admin 侧 Java 包名（当前是 `t.uni.domain.*`、`t.uni.admin.*`）
- admin 侧 `@MapperScan`、`@ComponentScan` 路径
- admin 侧 MyBatis XML 的 namespace
- admin 侧启动类 `TUniAdminApplication` → 新名字
- admin 侧环境变量前缀 `T_UNI_ADMIN_*` → `{ENV_PREFIX}_ADMIN_*`
- admin 侧 `spring.application.name`
- admin 侧 SQL schema 名称
