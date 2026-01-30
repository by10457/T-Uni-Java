package t.uni.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 性别枚举
 * <p>
 * 首次登录未选择时存 0；用户手动选择或修改资料时为 1/2/3。
 * </p>
 */
@Getter
@AllArgsConstructor
public enum GenderEnum {

    /**
     * 未知（首次登录未选择）
     */
    UNKNOWN(0, "未知"),

    /**
     * 男性
     */
    MALE(1, "男性"),

    /**
     * 女性
     */
    FEMALE(2, "女性"),

    /**
     * 不愿透露
     */
    PREFER_NOT_TO_SAY(3, "不愿透露");

    private final Integer code;
    private final String desc;

    public static GenderEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (GenderEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}
