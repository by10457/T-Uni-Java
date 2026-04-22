---
name: project-rename-java-template
description: 当基于 T-Uni-Java 的仓库需要做完整项目改名时使用，包括 Maven 坐标、模块名、Java 包根、Spring 扫描路径、环境变量前缀、SQL 名称和对外文案。用它替代简单粗暴的全局字符串替换。
---

# Java 模板项目改名

## 概述

这个技能用于结构化的项目改名。目标是安全改名，而不是图快。

## 先读这些参考文件

- 必填决策项：`references/rename-decisions.md`
- 真实改名影响面：`references/rename-surfaces.md`
- 校验清单：`references/verify.md`

## 工作流程

1. 在动文件前，先收集完整的改名映射。
2. 扫描仓库，把改动分成三类：
   - 可结构化替换项
   - 文档语义改写项
   - 需要人工确认项
3. 先做结构化修改：
   - Maven 坐标
   - 模块名
   - Java 包名
   - XML namespace 和 type
   - 配置前缀
   - 环境变量前缀
4. 等结构修改稳定后，再改用户可见文案。
5. 运行校验扫描，并报告剩余遗留项。

## 硬规则

- 不要做盲目的全局替换。
- 不要猜缺失的映射值。
- 除非用户明确要求，否则 `rename_admin` 默认按 `false` 处理。
- 项目改名时，不要顺手修改 `/api` 或 `wx.auth` 的语义。
- SQL 中保存的全限定类名要视为高风险人工复核项。
