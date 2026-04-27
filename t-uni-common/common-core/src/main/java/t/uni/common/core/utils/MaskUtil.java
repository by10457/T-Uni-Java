package t.uni.common.core.utils;

import cn.hutool.core.util.StrUtil;

/**
 * 敏感信息脱敏工具。
 * <p>
 * 面向日志、异常提示和审计展示场景，仅做字符串展示脱敏，不提供加密、解密或权限校验能力。
 * 调用方应在写日志前完成脱敏，避免敏感原文进入日志链路。
 * </p>
 */
public final class MaskUtil {

    private MaskUtil() {
    }

    /**
     * 脱敏手机号，保留前三后四。
     *
     * @param value 手机号原文
     * @return 脱敏后的手机号
     */
    public static String maskPhone(String value) {
        return maskKeepEdges(value, 3, 4);
    }

    /**
     * 脱敏微信 openid，保留前四后四。
     *
     * @param value openid 原文
     * @return 脱敏后的 openid
     */
    public static String maskOpenId(String value) {
        return maskKeepEdges(value, 4, 4);
    }

    /**
     * 脱敏微信 unionid，保留前四后四。
     *
     * @param value unionid 原文
     * @return 脱敏后的 unionid
     */
    public static String maskUnionId(String value) {
        return maskKeepEdges(value, 4, 4);
    }

    /**
     * 脱敏 Token，保留前六后四，避免完整凭证进入日志。
     *
     * @param value Token 原文
     * @return 脱敏后的 Token
     */
    public static String maskToken(String value) {
        return maskKeepEdges(value, 6, 4);
    }

    /**
     * 脱敏邮箱用户名部分，域名保持可识别。
     *
     * @param value 邮箱原文
     * @return 脱敏后的邮箱
     */
    public static String maskEmail(String value) {
        if (StrUtil.isBlank(value) || !value.contains("@")) {
            return value;
        }
        var parts = value.split("@", 2);
        return maskKeepEdges(parts[0], 1, 1) + "@" + parts[1];
    }

    /**
     * 脱敏身份证号，保留前三后四。
     *
     * @param value 身份证号原文
     * @return 脱敏后的身份证号
     */
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
