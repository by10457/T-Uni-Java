package t.uni.common.core.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * ID 格式校验器
 *
 * @author lzx
 * @since 2026-01-30
 */
public class ValidIdValidator implements ConstraintValidator<ValidId, String> {

    private static final String ID_REGEX = "^[1-9]\\d{0,18}$";

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
