package t.uni.common.core.utils;

import cn.hutool.core.util.StrUtil;

/**
 * 敏感信息脱敏工具。
 */
public final class MaskUtil {

    private MaskUtil() {
    }

    public static String maskPhone(String value) {
        return maskKeepEdges(value, 3, 4);
    }

    public static String maskOpenId(String value) {
        return maskKeepEdges(value, 4, 4);
    }

    public static String maskUnionId(String value) {
        return maskKeepEdges(value, 4, 4);
    }

    public static String maskToken(String value) {
        return maskKeepEdges(value, 6, 4);
    }

    public static String maskEmail(String value) {
        if (StrUtil.isBlank(value) || !value.contains("@")) {
            return value;
        }
        var parts = value.split("@", 2);
        return maskKeepEdges(parts[0], 1, 1) + "@" + parts[1];
    }

    public static String maskIdCard(String value) {
        return maskKeepEdges(value, 3, 4);
    }

    private static String maskKeepEdges(String value, int left, int right) {
        if (StrUtil.isBlank(value)) {
            return value;
        }
        if (value.length() <= left + right) {
            return "*".repeat(value.length());
        }
        return value.substring(0, left) + "*".repeat(value.length() - left - right) + value.substring(value.length() - right);
    }
}
