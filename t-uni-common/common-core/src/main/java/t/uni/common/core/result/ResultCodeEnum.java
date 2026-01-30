package t.uni.common.core.result;

import lombok.Getter;

/**
 * 统一返回结果状态信息类
 * <p>
 * 错误码分段设计：
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
    SUCCESS_LOGOUT(2006, "退出成功"),
    LOGOUT_SUCCESS(2006, "退出成功"),
    EMAIL_CODE_REFRESH(2007, "邮箱验证码已刷新"),
    EMAIL_CODE_SEND_SUCCESS(2008, "邮箱验证码已发送"),

    // ==================== 参数校验 3000-3099 ====================
    PARAM_ERROR(3000, "参数错误"),
    USERNAME_OR_PASSWORD_NOT_EMPTY(3001, "用户名或密码不能为空"),
    EMAIL_CODE_NOT_EMPTY(3002, "邮箱验证码不能为空"),
    SEND_EMAIL_CODE_NOT_EMPTY(3003, "请先发送邮箱验证码"),
    EMAIL_CODE_NOT_MATCHING(3004, "邮箱验证码不匹配"),
    LOGIN_ERROR_USERNAME_PASSWORD_NOT_EMPTY(3005, "登录信息不能为空"),
    REQUEST_IS_EMPTY(3006, "请求数据为空"),
    DATA_TOO_LARGE(3007, "请求数据过大"),
    UPDATE_NEW_PASSWORD_SAME_AS_OLD_PASSWORD(3008, "新密码与密码相同"),
    EMAIL_CODE_EMPTY(3009, "邮箱验证码过期或不存在"),

    // ==================== 数据/状态 3100-3199 ====================
    ILLEGAL_REQUEST(3100, "非法请求"),
    REPEAT_SUBMIT(3101, "重复提交"),
    DATA_ERROR(3102, "数据异常"),
    DATA_EXIST(3103, "数据已存在"),
    DATA_NOT_EXIST(3104, "数据不存在"),
    EMAIL_EXIST(3105, "邮箱已存在"),
    USER_IS_EMPTY(3106, "用户不存在"),
    ALREADY_USER_EXCEPTION(3107, "用户已存在"),
    FILE_NOT_EXIST(3108, "文件不存在"),
    MISSING_TEMPLATE_FILES(3109, "缺少模板文件"),
    EMAIL_USER_TEMPLATE_IS_EMPTY(3110, "邮件模板为空"),
    EMAIL_TEMPLATE_IS_EMPTY(3111, "邮件模板为空"),
    EMAIL_USER_IS_EMPTY(3112, "关联邮件用户配置为空"),
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
    ADMIN_ROLE_CAN_NOT_DELETED(3406, "无法删除admin角色"),
    ROUTER_RANK_NEED_LARGER_THAN_THE_PARENT(3407, "设置路由等级需要大于或等于父级的路由等级"),
    GET_BUCKET_EXCEPTION(3408, "获取文件信息失败"),
    SEND_MAIL_CODE_ERROR(3409, "邮件发送失败"),

    // ==================== 用户模块 4101-4199 ====================
    USER_NOT_EXIST(4101, "用户不存在"),
    NO_PERMISSION(4102, "无权限操作"),
    USER_DISABLED(4108, "账号已被禁用"),
    USER_DESTROYED(4109, "账号已注销"),

    // ==================== 系统错误 5000-5099 ====================
    UNKNOWN_EXCEPTION(5000, "服务异常"),
    SERVICE_ERROR(5001, "服务异常"),
    UPLOAD_ERROR(5002, "上传失败"),
    FAIL(5003, "失败"),
    WX_LOGIN_FAILED(5004, "微信登录失败"),
    WX_GET_PHONE_FAILED(5005, "获取手机号失败"),
    ;

    private final Integer code;
    private final String message;

    ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
