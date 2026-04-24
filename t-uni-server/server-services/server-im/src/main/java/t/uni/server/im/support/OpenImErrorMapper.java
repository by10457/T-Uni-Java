package t.uni.server.im.support;

import lombok.AllArgsConstructor;
import lombok.Getter;
import t.uni.server.im.ImResultCodeEnum;

/**
 * OpenIM 错误码映射
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

    public static boolean isRecordNotFound(Integer errCode) {
        return errCode != null && errCode.equals(RECORD_NOT_FOUND.openimCode);
    }

    /**
     * 判断是否为"用户已存在"错误（幂等场景可忽略）
     */
    public static boolean isUserAlreadyRegistered(Integer errCode) {
        return errCode != null && errCode.equals(USER_ALREADY_REGISTERED.openimCode);
    }

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
