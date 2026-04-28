# Business User Rename Prompt

把下面这份 Prompt 直接交给 AI 使用，用来把模板默认的 `biz_user` 改成你自己的业务用户表名字。

项目改名和业务用户表改名是两个任务：如果你同时要改项目名 / 包名和 `biz_user`，先运行 `scripts/rename-project/` 下的项目改名脚本并验证通过，再执行本 Prompt。

## 使用前先确认

```text
business_user_entity:
business_user_mapper:
business_user_table:
business_user_label:
keep_column_names:
extra_business_fields:
```

说明：

- `keep_column_names=true` 表示继续使用 `ma_open_id / mp_open_id / union_id`
- 如果列名也要改，AI 必须同步修改默认 Mapper 查询逻辑
- `extra_business_fields` 是新业务表需要额外增加的字段（如 `student_no VARCHAR(32)`），留空表示只保留默认字段

重要提示：

- 当前仓库已有 `IBusinessUser` / `IBusinessUserMapper` / `WxAuthConfig` 抽象间接层，优先利用而不是重建
- `WxAuthServiceImpl` 中有 `new BizUser()` 硬编码，改表时**必须一并替换成新实体类**
- 不要把项目改名脚本和业务用户表替换混成一次盲替换

## Prompt

```text
你正在修改一个采用 `core_user + 业务用户表` 双表模型的 Java 模板仓库。

目标：
把默认的 `biz_user` 实现替换成新的业务用户表实现。

如果本仓库还没有完成项目名 / 包名改名，请先停下来确认是否需要运行项目改名脚本；不要把项目改名和业务用户表改名混成一个任务。

输入：
- business_user_entity:
- business_user_mapper:
- business_user_table:
- business_user_label:
- keep_column_names:
- extra_business_fields:

执行要求：
1. 先只读扫描以下位置：
   - 默认业务用户实体
   - IBusinessUser / IBusinessUserMapper
   - WxAuthConfig
   - WxAuthServiceImpl
   - init.sql 与 init_sql/README.md
   - README / docs / 日志文案
2. 先列出影响点，再执行修改。
3. 修改时必须保留下面这些硬约束：
   - core_user 仍然是核心身份表
   - 业务用户表仍然是一对一扩展表
   - 两张表共用同一个 id
   - createNewUser 仍然先创建 core_user，再创建业务用户表记录
   - uniqueId 只能生成一次
   - 不允许改成单表模型
   - 不允许让业务用户表自增生成不同 id
4. 如果 keep_column_names=false，必须同步修改默认 Mapper 里的字符串列名查询。
5. 不要把历史兼容恢复逻辑当成模板主能力去放大。
6. 修改后必须自检：
   rg -n "biz_user|BizUser|businessUserMapper|IBusinessUser"

额外约束：
- 不要顺手引入 H5 / mp_open_id 新登录链路，除非任务明确要求。
- 不要自动引入具体业务项目里依赖业务态字段的联查逻辑。
- 不要把业务表字段职责改回 core_user。
```
