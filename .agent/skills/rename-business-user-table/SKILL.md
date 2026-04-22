---
name: rename-business-user-table
description: 当基于 T-Uni-Java 的仓库需要把默认的 biz_user 实现替换成新的业务用户表、实体、Mapper 和 SQL 命名方案时使用，同时必须保留 core_user 加业务用户双表模型。
---

# 业务用户表改名

## 概述

用这个技能来安全地替换默认业务用户模型。它不只是改一张表名，还会影响默认实体、Mapper、认证配置、认证服务、SQL 和模板对外文档。

## 先读这些参考文件

- 核心约束：`references/constraints.md`
- 逐文件清单：`references/checklist.md`

## 工作流程

1. 先收集目标名称：
   - entity
   - mapper
   - table
   - label 文案
   - 列名是否保持不变
2. 编辑前先扫描所有耦合点。
3. 优先做结构性修改：
   - entity
   - mapper
   - auth config
   - auth service
   - SQL
4. 最后再改日志、README、docs 和 prompts。
5. 最后用 grep 检查是否还残留 `biz_user` / `BizUser`。

## 硬规则

- `core_user` 必须继续作为核心身份表。
- 业务用户表必须继续作为一对一扩展表。
- 两张表必须继续共用同一个 `id`。
- 不要把它改成单表模型。
- 不要把“自动补建用户表”当成模板默认叙事。
- 如果列名变了，默认 `IBusinessUserMapper` 里的字符串列名查询也必须一起改。
