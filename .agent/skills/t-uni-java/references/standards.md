# 编码规范

完整说明见 [../../../../docs/coding-standards.md](../../../../docs/coding-standards.md)。

这个 skill 侧只保留执行时必须立刻遵守的约束：

- Controller 统一使用 `@Tag`、`@RestController`、`@RequestMapping`
- 返回统一走 `Result<T>` / `PageResult<T>`
- Service 失败统一抛 `BaseException(...)`
- 新代码统一使用 `@RequiredArgsConstructor + private final`
- 不要继续扩散字段注入
- 打印敏感标识前先脱敏
