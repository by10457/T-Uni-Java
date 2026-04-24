package t.uni.common.core.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
     * 操作成功
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 操作成功，返回数据
     */
    public static <T> Result<T> success(T data) {
        return build(data, ResultCodeEnum.SUCCESS);
    }

    /**
     * 操作成功，返回数据并使用指定成功状态码
     */
    public static <T> Result<T> success(T data, ResultCodeEnum codeEnum) {
        return build(data, codeEnum);
    }

    /**
     * 操作成功，返回数据并使用自定义消息
     */
    public static <T> Result<T> success(T data, String message) {
        return build(data, ResultCodeEnum.SUCCESS.getCode(), message);
    }

    /**
     * 操作成功，返回数据并使用自定义状态码和消息
     */
    public static <T> Result<T> success(T data, Integer code, String message) {
        return build(data, code, message);
    }

    /**
     * 操作成功，使用指定成功状态码
     */
    public static <T> Result<T> success(ResultCodeEnum codeEnum) {
        return build(null, codeEnum);
    }

    /**
     * 操作失败，使用指定失败状态码
     */
    public static <T> Result<T> error(ResultCodeEnum codeEnum) {
        return build(null, codeEnum);
    }

    /**
     * 操作失败，返回自定义状态码和消息
     */
    public static <T> Result<T> error(T data, Integer code, String message) {
        return build(data, code, message);
    }
}
