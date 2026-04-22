# Project Rename Prompt

把下面这份 Prompt 直接交给 AI 使用，用来把 `T-Uni-Java` 改成你的新项目名。

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
module_map:
  - old: t-uni-common
    new:
  - old: t-uni-server
    new:
  - old: t-uni-admin
    new:
rename_admin:
dry_run:
```

## Prompt

```text
你正在修改一个 Java 多模块模板仓库，请按“结构化改名”方式执行，而不是做全文盲替换。

输入映射：
- display_name:
- repo_slug:
- group_id:
- root_package:
- config_prefix:
- env_prefix:
- server_db_name:
- admin_db_name:
- module_map:
- rename_admin:
- dry_run:

执行要求：
1. 先只读扫描真实影响面，列出以下位置：
   - Maven 坐标与模块名
   - 模块目录名
   - Java package 与 import
   - 启动类文件名与类名
   - Spring 扫描路径
   - MyBatis XML 的 namespace / type / resultType
   - application*.yml 的配置前缀与环境变量前缀
   - spring.application.name
   - README / docs / HTML / JSON / SQL / Prompt / Skill 文案
2. 按三类输出：
   - 适合脚本改
   - 适合语义改写
   - 必须人工确认
3. 如果映射不完整，先停下来补齐，不要猜。
4. 只在用户确认后再执行修改。
5. 修改后必须做遗留扫描：
   rg -n "t\\.uni|t-uni|T-Uni|T_UNI_|tuni|t_uni"
6. 重点检查：
   - @MapperScan / @ComponentScan / startsWith("...")
   - pom.xml 的 finalName 与 mainClass
   - XML mapper 的全限定类名
   - SQL 里的全限定类名
   - 环境变量前缀与 spring.application.name
7. 输出最终摘要时，必须区分：
   - 已自动修改项
   - 仍需人工确认项
   - 风险项

约束：
- 不要把 rename_admin 默认视为 true。
- 不要擅自改 /api 路径前缀。
- 不要擅自改 wx.auth 配置前缀。
- 不要把项目改名和业务表改名混成一个任务。
```
