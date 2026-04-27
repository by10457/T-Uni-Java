package t.uni.common.core.result;

import lombok.Getter;

/**
 * 统一响应业务状态码。
 * <p>
 * 用于 Result、BaseException 和全局异常处理之间传递稳定的业务语义。
 * 新增状态码应保持分段清晰，不复用已经对外暴露的 code。
 * </p>
 * <p>
 * 分段设计：
 * - 2000-2099: 通用成功
 * - 3000-3099: 参数校验
 * - 3100-3199: 数据/状态
 * - 3200-3299: 认证/会话
 * - 3300-3399: 权限/Token
 * - 3400-3499: 业务提示
 * - 5000-5099: 系统错误
 * </p>
 */
@Getter
public enum ResultCodeEnum {
    // ==================== 通用成功 2000-2099 ====================
    SUCCESS(2000, "操作成功"),
    CREATE_SUCCESS(2001, "添加成功"),
    UPDATE_SUCCESS(2002, "修改成功"),
    DELETE_SUCCESS(2003, "删除成功"),
    SORT_SUCCESS(2004, "排序成功"),
    SUCCESS_UPLOAD(2005, "上传成功"),
    LOGOUT_SUCCESS(2006, "退出成功"),

    // ==================== 参数校验 3000-3099 ====================
    PARAM_ERROR(3000, "参数错误"),
    USERNAME_OR_PASSWORD_NOT_EMPTY(3001, "用户名或密码不能为空"),
    LOGIN_ERROR_USERNAME_PASSWORD_NOT_EMPTY(3005, "登录信息不能为空"),
    REQUEST_IS_EMPTY(3006, "请求数据为空"),
    DATA_TOO_LARGE(3007, "请求数据过大"),
    UPDATE_NEW_PASSWORD_SAME_AS_OLD_PASSWORD(3008, "新密码与密码相同"),

    // ==================== 数据/状态 3100-3199 ====================
    ILLEGAL_REQUEST(3100, "非法请求"),
    REPEAT_SUBMIT(3101, "重复提交"),
    DATA_ERROR(3102, "数据异常"),
    DATA_EXIST(3103, "数据已存在"),
    DATA_NOT_EXIST(3104, "数据不存在"),
    USER_IS_EMPTY(3106, "用户不存在"),
    ALREADY_USER_EXCEPTION(3107, "用户已存在"),
    FILE_NOT_EXIST(3108, "文件不存在"),
    MISSING_TEMPLATE_FILES(3109, "缺少模板文件"),
    NEW_PASSWORD_SAME_OLD_PASSWORD(3113, "新密码不能和旧密码相同"),

    // ==================== 认证/会话 3200-3299 ====================
    LOGIN_AUTH(3200, "请先登陆"),
    AUTHENTICATION_EXPIRED(3201, "身份验证过期"),
    SESSION_EXPIRATION(3202, "会话过期"),
    FAIL_NO_ACCESS_DENIED_USER_LOCKED(3203, "该账户已封禁"),
    THE_SAME_USER_HAS_LOGGED_IN(3204, "相同用户已登录"),
    LOGIN_ERROR(3205, "账号或密码错误"),

    // ==================== 权限/Token 3300-3399 ====================
    FAIL_NO_ACCESS_DENIED(3300, "无权访问"),
    FAIL_NO_ACCESS_DENIED_USER_OFFLINE(3301, "用户强制下线"),
    TOKEN_PARSING_FAILED(3302, "token解析失败"),
    TOKEN_EXPIRED(3303, "token已过期"),
    TOKEN_NOT_PROVIDED(3304, "未提供有效的认证信息"),
    REFRESH_TOKEN_EMPTY(3305, "刷新令牌不能为空"),
    REFRESH_TOKEN_INVALID(3306, "刷新令牌已失效或不存在"),
    REFRESH_TOKEN_EXPIRED(3307, "刷新令牌已过期"),
    ACCESS_TOKEN_EMPTY(3308, "访问令牌不能为空"),
    ACCESS_TOKEN_INVALID(3309, "访问令牌已过期或无效"),

    // ==================== 业务提示 3400-3499 ====================
    UPDATE_ERROR(3400, "修改失败"),
    URL_ENCODE_ERROR(3401, "URL编码失败"),
    ILLEGAL_CALLBACK_REQUEST_ERROR(3402, "非法回调请求"),
    FETCH_USERINFO_ERROR(3403, "获取用户信息失败"),
    ILLEGAL_DATA_REQUEST(3404, "非法数据请求"),
    CLASS_NOT_FOUND(3405, "类名不存在"),
    GET_BUCKET_EXCEPTION(3408, "获取文件信息失败"),

    // ==================== 服务端用户状态 4100-4199 ====================
    NO_PERMISSION(4102, "无权限操作"),
    USER_DISABLED(4108, "账号已被禁用"),
    USER_DESTROYED(4109, "账号已注销"),

    // ==================== 系统错误 5000-5099 ====================
    SERVICE_ERROR(5001, "服务异常"),
    UPLOAD_ERROR(5002, "上传失败"),
    FAIL(5003, "失败"),
    WX_LOGIN_FAILED(5004, "微信登录失败"),
    WX_GET_PHONE_FAILED(5005, "获取手机号失败"),

    // ==================== 存储错误 5100-5199 ====================
    FILE_KEY_EMPTY(5101, "文件标识不能为空"),
    UPLOAD_BYTES_EMPTY(5102, "上传内容不能为空"),
    ;

    private final Integer code;
    private final String message;

    ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
