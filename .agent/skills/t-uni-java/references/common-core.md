# common-core 放置规则

完整说明见 [../../../../docs/common-core-guidelines.md](../../../../docs/common-core-guidelines.md)。

这个 skill 侧只保留最关键的落包判断：

1. `admin` 和 `server` 是否都会复用
2. 是否不需要 Spring / Redis / 中间件装配
3. 是否没有单端、单业务强语义

只有这三条同时满足，才建议进入 `common-core`。
