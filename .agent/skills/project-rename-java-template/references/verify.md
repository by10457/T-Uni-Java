# 校验

真实改名后必须先运行验证脚本，再做人工复核。

## macOS / Linux / Git Bash

```bash
./scripts/rename-project/verify-rename.sh --rename-admin true
```

如果本次故意不改 admin：

```bash
./scripts/rename-project/verify-rename.sh --rename-admin false
```

## Windows PowerShell

```powershell
.\scripts\rename-project\verify-rename.ps1 -RenameAdmin true
```

如果本次故意不改 admin：

```powershell
.\scripts\rename-project\verify-rename.ps1 -RenameAdmin false
```

## 验证结果处理

- `rename_admin=true` 时，admin 旧命名残留应纳入失败项处理。
- `rename_admin=false` 时，admin 旧命名残留不直接判失败，但必须在最终摘要中列为人工确认项。
- 验证脚本失败后，先修复旧命名残留，再重新运行验证脚本。
- 验证脚本通过后，再运行必要的 Maven 编译或测试。

## 人工复核清单

- `@MapperScan`、`@ComponentScan`、基于字符串的包名判断
- `pom.xml` 的 `finalName` 与 Spring Boot `mainClass`
- XML mapper 的 `namespace`、`type`、`resultType`
- SQL 文件里的 schema 名和全限定类名
- `application*.yml` 的环境变量前缀与 `spring.application.name`
- Docker volume、日志目录、数据目录、镜像名、容器名
- OpenIM userID、MinIO bucket、线上回调域名
- LICENSE holder 和用户可见展示文案

## 手动兜底扫描

如果验证脚本不可用，再运行手动扫描作为兜底：

```bash
rg -n --hidden --glob '!.git/**' --glob '!target/**' --glob '!**/target/**' --glob '!node_modules/**' --glob '!**/node_modules/**' --glob '!.idea/**' --glob '!**/.idea/**' --glob '!.claude/**' --glob '!**/.claude/**' --glob '!scripts/rename-project/**' --glob '!.env' --glob '!.env.*' --glob '.env.example' "t\\.uni|t/uni|t-uni|T-Uni|T_UNI|tuni|t_uni"
```
