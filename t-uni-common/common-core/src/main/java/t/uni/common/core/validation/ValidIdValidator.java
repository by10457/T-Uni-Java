package t.uni.common.core.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * ID 格式校验器。
 * <p>
 * 保护字符串 ID 转 Long 前的格式边界，避免超长数字或非法字符在控制器后续流程中触发 500。
 * null 视为通过，是否必填由组合的空值校验注解决定。
 * </p>
 *
 * @author lzx
 * @since 2026-01-30
 */
public class ValidIdValidator implements ConstraintValidator<ValidId, String> {

    private static final String ID_REGEX = "^[1-9]\\d{0,18}$";

    /**
     * 校验字符串是否是可安全转换为 Long 的正整数 ID。
     *
     * @param value   待校验值
     * @param context Bean Validation 上下文
     * @return true 表示格式合法
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (value.isBlank()) {
            return false;
        }
        if (!value.matches(ID_REGEX)) {
            return false;
        }
        try {
            Long.parseLong(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
