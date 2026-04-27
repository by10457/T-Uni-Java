package t.uni.common.core.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * REST 接口统一响应模型。
 * <p>
 * 只承载业务状态码、提示文案和响应数据，不代表 HTTP 状态码，也不负责异常记录或国际化处理。
 * 调用方应避免把密钥、Token、手机号等敏感原文直接放入 message 或 data。
 * </p>
 *
 * @param <T> 响应数据类型
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {

    @Schema(description = "* 状态码")
    private Integer code;

    @Schema(description = "* 返回消息")
    private String message;

    @Schema(description = "* 返回数据")
    private T data;

    private static <T> Result<T> build(T data, Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    private static <T> Result<T> build(T data, ResultCodeEnum codeEnum) {
        return build(data, codeEnum.getCode(), codeEnum.getMessage());
    }

    /**
     * 创建无业务数据的成功响应。
     *
     * @param <T> 响应数据类型
     * @return 成功响应
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 创建携带业务数据的成功响应。
     *
     * @param data 响应数据
     * @param <T>  响应数据类型
     * @return 成功响应
     */
    public static <T> Result<T> success(T data) {
        return build(data, ResultCodeEnum.SUCCESS);
    }

    /**
     * 创建携带业务数据并使用指定状态码的成功响应。
     *
     * @param data     响应数据
     * @param codeEnum 业务状态码枚举
     * @param <T>      响应数据类型
     * @return 成功响应
     */
    public static <T> Result<T> success(T data, ResultCodeEnum codeEnum) {
        return build(data, codeEnum);
    }

    /**
     * 创建携带业务数据和自定义提示的成功响应。
     *
     * @param data    响应数据
     * @param message 提示文案
     * @param <T>     响应数据类型
     * @return 成功响应
     */
    public static <T> Result<T> success(T data, String message) {
        return build(data, ResultCodeEnum.SUCCESS.getCode(), message);
    }

    /**
     * 创建携带自定义状态码和提示的成功响应。
     *
     * @param data    响应数据
     * @param code    业务状态码
     * @param message 提示文案
     * @param <T>     响应数据类型
     * @return 成功响应
     */
    public static <T> Result<T> success(T data, Integer code, String message) {
        return build(data, code, message);
    }

    /**
     * 创建使用指定状态码且无业务数据的成功响应。
     *
     * @param codeEnum 业务状态码枚举
     * @param <T>      响应数据类型
     * @return 成功响应
     */
    public static <T> Result<T> success(ResultCodeEnum codeEnum) {
        return build(null, codeEnum);
    }

    /**
     * 创建使用指定状态码的失败响应。
     *
     * @param codeEnum 业务状态码枚举
     * @param <T>      响应数据类型
     * @return 失败响应
     */
    public static <T> Result<T> error(ResultCodeEnum codeEnum) {
        return build(null, codeEnum);
    }

    /**
     * 创建携带自定义状态码、提示和可选数据的失败响应。
     *
     * @param data    响应数据，通常为空
     * @param code    业务状态码
     * @param message 提示文案
     * @param <T>     响应数据类型
     * @return 失败响应
     */
    public static <T> Result<T> error(T data, Integer code, String message) {
        return build(data, code, message);
    }
}
