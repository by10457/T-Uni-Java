package t.uni.server.im.support;

import lombok.AllArgsConstructor;
import lombok.Getter;
import t.uni.server.im.ImResultCodeEnum;

/**
 * OpenIM 错误码映射
 * <p>
 * 只维护当前 IM 模块显式依赖的 OpenIM 错误码。未知错误统一映射为通用 OpenIM 请求失败。
 *
 * @author t-uni
 * @since 2026-04-24
 */
@Getter
@AllArgsConstructor
public enum OpenImErrorMapper {

    ARGS_ERROR(1001, ImResultCodeEnum.IM_OPENIM_REQUEST_FAIL, "OpenIM 参数错误"),
    NO_PERMISSION(1002, ImResultCodeEnum.IM_OPENIM_SEND_MSG_FAIL, "OpenIM 无权限（sendID 非通知账号）"),
    RECORD_NOT_FOUND(1004, ImResultCodeEnum.IM_OPENIM_USER_NOT_FOUND, "用户未导入 OpenIM"),
    USER_ALREADY_REGISTERED(1101, null, "用户已在 OpenIM 中注册");

    private final Integer openimCode;
    private final ImResultCodeEnum resultCodeEnum;
    private final String desc;

    /**
     * 判断 OpenIM 是否返回用户未导入。
     */
    public static boolean isRecordNotFound(Integer errCode) {
        return errCode != null && errCode.equals(RECORD_NOT_FOUND.openimCode);
    }

    /**
     * 判断是否为"用户已存在"错误（幂等场景可忽略）
     */
    public static boolean isUserAlreadyRegistered(Integer errCode) {
        return errCode != null && errCode.equals(USER_ALREADY_REGISTERED.openimCode);
    }

    /**
     * 将 OpenIM 错误码映射为 IM 模块错误码。
     *
     * @param errCode OpenIM 原始错误码
     * @return IM 模块错误码，未知值返回通用请求失败
     */
    public static ImResultCodeEnum map(Integer errCode) {
        if (errCode == null) {
            return ImResultCodeEnum.IM_OPENIM_REQUEST_FAIL;
        }
        for (OpenImErrorMapper value : values()) {
            if (errCode.equals(value.openimCode) && value.resultCodeEnum != null) {
                return value.resultCodeEnum;
            }
        }
        return ImResultCodeEnum.IM_OPENIM_REQUEST_FAIL;
    }
}
