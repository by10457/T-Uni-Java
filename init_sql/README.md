# init_sql

## 结构说明

- `init.sql`: 模板最小启动脚本，只保留当前模板主链路真正依赖的表。

## 默认初始化表

- `core_user`
- `core_user_default_avatar`
- `core_user_default_nick_name`
- `biz_user`

## 使用建议

1. 先执行 [init.sql](/Users/xua/Code/WorkSpaces/T-Uni/T-Uni-Java/init_sql/init.sql)。
2. `biz_user` 只是模板默认实现，后续可替换成你自己的业务用户表。
3. `core_user_default_avatar` 默认不预置远程图片，首次接入前请自行补充可访问的头像资源。
