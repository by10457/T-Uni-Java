---
name: project-rename-java-template
description: 当基于 T-Uni-Java 的仓库需要做完整项目改名时使用，包括 Maven 坐标、模块名、Java 包根、Spring 扫描路径、环境变量前缀、SQL 名称和对外文案。优先运行 scripts/rename-project/ 下的跨平台脚本，而不是手工全局替换。
---

# Java 模板项目改名

## 概述

这个技能用于结构化的项目改名。目标是先用脚本完成机械一致性改名，再由 AI 做验证、编译和人工复核，不做盲目的全文替换。

## 先读这些参考文件

- 必填决策项：`references/rename-decisions.md`
- 真实改名影响面：`references/rename-surfaces.md`
- 校验清单：`references/verify.md`

## 工作流程

1. 在动文件前，先收集完整映射：`display_name`、`repo_slug`、`group_id`、`root_package`、`config_prefix`、`env_prefix`、`server_db_name`、`admin_db_name`、`common_module`、`server_module`、`admin_module`、`rename_admin`。
2. 如果映射不完整，停下来问用户，不要猜默认值。
3. 判断当前系统并选择脚本：
   - macOS / Linux / Git Bash：`scripts/rename-project/rename-project.sh`
   - Windows PowerShell：`scripts/rename-project/rename-project.ps1`
4. 必须先运行 dry-run，并把摘要、路径重命名、文本变更数和人工确认项展示给用户。
5. 用户确认 dry-run 摘要后，才允许去掉 dry-run 执行真实改名。
6. 真实执行后，运行对应验证脚本：
   - macOS / Linux / Git Bash：`scripts/rename-project/verify-rename.sh`
   - Windows PowerShell：`scripts/rename-project/verify-rename.ps1`
7. 根据验证结果做少量语义修正和人工复核，尤其检查扫描路径、XML mapper、SQL 全限定类名、Docker/OpenIM/MinIO/License 等高风险项。
8. 最后运行必要的编译或测试，并输出：脚本自动修改项、验证发现并修复项、仍需人工确认项、风险项。

## 硬规则

- 不要绕过 dry-run。
- 不要在用户确认 dry-run 摘要前执行真实改名。
- 不要猜缺失的映射值。
- 除非用户明确要求，否则 `rename_admin` 默认按 `false` 处理。
- `rename_admin=false` 时，不要默认改 `t-uni-admin` 的模块结构和 `T_UNI_ADMIN_*`，但必须报告 admin 目录里的保留命中项。
- 不要修改真实 `.env` 或 `.env.*`，只允许脚本处理 `.env.example`。
- 不要自动迁移 Docker volume、OpenIM userID、MinIO bucket 或线上回调域名。
- 项目改名时，不要顺手修改 `/api` 或 `wx.auth` 的业务语义。
- 项目改名不等于业务用户表改名；`biz_user` 替换使用单独 Prompt。
- SQL 中保存的全限定类名要视为高风险人工复核项。
