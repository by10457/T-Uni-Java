# 检查清单

## 当前仓库已有的抽象层

改表之前先了解：当前仓库已经有 `IBusinessUser` / `IBusinessUserMapper` / `WxAuthConfig` 这套抽象间接层。优先利用这套接口，不需要从零重建。

## 必须检查并更新的位置

- 默认业务用户实体 `BizUser.java`
- `IBusinessUser` 接口（如果需要改字段）
- `IBusinessUserMapper` 接口
- `WxAuthConfig` 里的 Bean 注入（换成新的 Mapper）
- `WxAuthServiceImpl` 中的以下硬编码：
  - `createNewUser()` 方法里的 `new BizUser()` — **必须改成新实体类**
  - `tryRecoverBusinessUserFromCore()` 方法里的 `new BizUser()` — **必须改成新实体类**
  - `updateExistingUser()` 中的 `new BizUser()` — 如果有的话也要改
- `BizUserMapper.java`（默认的 Mapper 实现）
- `init_sql/init.sql`（建表语句）
- `init_sql/README.md`
- 根 README 和 prompts 里的文案

## 容易遗漏的点

- 只换 Mapper 注入是**不够的**，Service 里的 `new BizUser()` 也必须改
- 如果 `keep_column_names=false`，`IBusinessUserMapper` 里的字符串列名（`"ma_open_id"`、`"union_id"`）也要同步改
- `extra_business_fields` 如果有新字段，需要同时更新 `IBusinessUser` 接口的 getter/setter

## 改完后自检

```bash
rg -n "biz_user|BizUser|businessUserMapper|IBusinessUser"
```
