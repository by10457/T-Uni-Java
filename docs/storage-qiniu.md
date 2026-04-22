# Qiniu Storage

## 当前状态

七牛云在模板里是可选能力，不是默认强依赖。

当前策略：

- 只有配置了 `qiniu.*` 才会装配
- 已支持上传 token、头像上传 token、回调验签、服务端字节流上传
- 已支持下载签名、CDN 时间戳防盗链、下载 URL 缓存
- 已支持显式访问策略：`PUBLIC / PRIVATE`
- 已提供最小版 `normalizeForStorage(...)` 与 `resolveAccessUrl(...)`

## 当前模板更适合怎样使用

### 1. 内部资源尽量存 `fileKey`

推荐：

- 库里存 `fileKey`
- 对外返回可访问 URL
- 不要把签名下载 URL 直接落库

原因：

- 签名 URL 会过期
- 私有空间、公有空间、CDN 防盗链切换时，`fileKey` 最稳定

### 2. 外链 URL 只在明确允许的字段保留

比如：

- 用户外部头像
- 第三方回传图片

如果字段语义本身就是“外部 URL 可长期存在”，可以保留原值。

否则，模板更建议把内部资源统一转为 `fileKey` 存储。

## 关于公有空间和私有空间

### 私有空间

当访问策略为 `PRIVATE` 时：

- 下载时走签名 URL
- 可选叠加 CDN 时间戳防盗链

### 公有空间

当访问策略为 `PUBLIC` 时，直接按：

```text
domain + "/" + encodedKey
```

生成可访问地址。

## 当前统一解析入口

当前 common 层已经落了两个统一入口：

- `normalizeForStorage(rawValue)`
- `resolveAccessUrl(rawValue)`

它们的行为是：

- 非 HTTP 值：按内部 `fileKey` 处理
- 命中当前七牛域名的 URL：尝试回收为 `fileKey`
- 非七牛外链：原样保留

这样业务层不需要每次都自己判断“这是 fileKey、系统 URL，还是第三方外链”。

## 推荐的模板方向

推荐调用方式：

1. 入库前先走 `normalizeForStorage(rawValue)`
2. 对外返回前再走 `resolveAccessUrl(rawValue)`
3. 内部资源字段尽量只存 `fileKey`
4. 一个应用实例默认使用一套 `accessPolicy`

当前 `accessPolicy` 已显式支持：

- `PUBLIC`
- `PRIVATE`

## 为什么这里要特别强调

参考 `wxy-server` 的真实数据可以看到：

- 有的字段存完整 URL
- 有的字段存 `fileKey`

如果模板不提前统一这件事，后续很容易出现：

- 同一个字段混存 URL 和 key
- 把过期签名 URL 落库
- 私有空间切换后批量失效

## 当前建议

如果你正在基于这个模板写业务，建议优先遵守：

1. 内部资源尽量存 `fileKey`
2. 不要把已签名下载链接落库
3. 外链 URL 只在明确允许的字段保留
4. 不要把 `mediaId` 这类业务 ID 直接沉到 common

## 当前限制

- 当前统一解析依赖当前配置里的 `domain`
- 无参版的 `generateDownloadUrl(fileKey)` / `resolveAccessUrl(rawValue)` 走的是全局 `accessPolicy`
- 如果一个 bucket 内同时存有公开和私有资源，可以使用带 `QiniuAccessPolicy` 参数的重载：
  - `generateDownloadUrl(fileKey, QiniuAccessPolicy.PRIVATE)`
  - `resolveAccessUrl(rawValue, QiniuAccessPolicy.PUBLIC)`

## 后续计划

七牛这条目前已经完成最小可用版本，但字段级白名单、混合 bucket 策略和更细的跨业务资源约束，仍然可以继续增强，见 [../TODO.md](../TODO.md)。
