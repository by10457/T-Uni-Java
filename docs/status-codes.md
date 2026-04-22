# 状态码规范

`T-Uni-Java` 默认使用 4 位业务状态码。

设计目标：

- 通用成功、参数错误、认证错误、系统错误一眼可分
- `common-core` 只保留跨端共享的状态码
- admin、server、未来业务模块各自扩展，不再把端专属码堆回 common

## 通用码段

| 码段 | 含义 | 归属 |
| --- | --- | --- |
| `2000-2099` | 通用成功 | `common-core` |
| `3000-3099` | 参数校验 / 请求格式错误 | `common-core` |
| `3100-3199` | 数据状态 / 资源状态 | `common-core` |
| `3200-3299` | 认证 / 会话 | `common-core` |
| `3300-3399` | 权限 / Token | `common-core` |
| `3400-3499` | 通用业务提示 | `common-core` |
| `4100-4199` | 服务端用户状态 | `server` 共享使用 |
| `5000-5099` | 系统错误 | `common-core` |
| `5100-5199` | 存储 / 文件错误 | `common-core` |

## 当前约束

- `common-core` 只放跨端共享码，不再放 admin 邮件、角色、路由等后台专属状态码
- admin 专属状态码放到 `t-uni-admin/admin-domain` 自己维护
- server 业务扩展状态码优先放到 `t-uni-server` 对应模块，不要把业务语义回流到 common

## 推荐使用方式

### Controller

- 成功返回优先使用 `Result.success(...)`
- 如果需要自定义成功码，使用 `Result.success(data, code, message)`

### Service

- 共享错误使用 `throw new BaseException(ResultCodeEnum.xxx)`
- 端专属错误使用 `throw new BaseException(code, message)`

## 当前已落地的边界

- admin 邮件验证码、邮件模板、admin 角色删除等状态码，已经从 `common-core` 迁出
- `common-core` 保留的是模板主链路真正共享的状态码

## 后续扩展建议

- 新增业务模块前，先确定它是 `server` 专属、`admin` 专属，还是两端共享
- 只有“两端共享”的状态码，才进入 `common-core`
- 如果某个模块出现 10 个以上专属状态码，优先给该模块建独立枚举，而不是继续扩充公共枚举
