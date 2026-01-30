package t.uni.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 布尔状态枚举
 * <p>
 * 用于数据库中 0/1 字段的语义化表示
 * </p>
 */
@Getter
@AllArgsConstructor
public enum TrueFalseEnum {

    /**
     * 否/假/未启用
     */
    FALSE(0, "否"),

    /**
     * 是/真/已启用
     */
    TRUE(1, "是");

    private final Integer status;
    private final String desc;

    public static TrueFalseEnum getByStatus(Integer status) {
        if (status == null) {
            return null;
        }
        for (TrueFalseEnum e : values()) {
            if (e.getStatus().equals(status)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 判断是否为 TRUE
     */
    public static boolean isTrue(Integer status) {
        return TRUE.getStatus().equals(status);
    }

    /**
     * 判断是否为 FALSE
     */
    public static boolean isFalse(Integer status) {
        return FALSE.getStatus().equals(status);
    }
}
