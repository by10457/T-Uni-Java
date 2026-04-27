package t.uni.common.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * ID 格式校验注解
 * <p>
 * 用于校验字符串类型的 ID 字段，确保可以安全转换为 Long 类型。
 * 规则：1-19 位数字，首位不能为 0，不超过 Long.MAX_VALUE。
 * null 值交给 @NotNull / @NotBlank 控制，本注解只约束格式边界。
 * </p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * @ValidId(message = "帖子ID格式无效")
 * private String threadId;
 * }</pre>
 *
 * @author lzx
 * @since 2026-01-29
 */
@Documented
@Constraint(validatedBy = {ValidIdValidator.class})
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidId {

    /**
     * 校验失败时返回的提示信息。
     */
    String message() default "ID格式无效";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
