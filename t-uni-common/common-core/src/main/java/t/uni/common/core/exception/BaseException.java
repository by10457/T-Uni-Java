package t.uni.common.core.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import t.uni.common.core.result.ResultCodeEnum;

/**
 * 业务基础异常。
 * <p>
 * 用于业务层主动中断流程并向全局异常处理器传递可展示的业务状态码和提示。
 * 不应携带数据库 SQL、密钥、Token 等敏感细节。
 * </p>
 */
@NoArgsConstructor
@Getter
@ToString
@Slf4j
public class BaseException extends RuntimeException {
    /**
     * 业务状态码。
     */
    Integer code;

    /**
     * 面向调用方的提示信息。
     */
    String message = "服务异常";

    /**
     * 原始状态枚举，便于上层保留统一语义。
     */
    ResultCodeEnum resultCodeEnum;

    /**
     * 使用自定义业务状态码和提示创建异常。
     *
     * @param code    业务状态码
     * @param message 面向调用方的提示信息
     */
    public BaseException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 使用默认服务异常语义和自定义提示创建异常。
     *
     * @param message 面向调用方的提示信息
     */
    public BaseException(String message) {
        super(message);
        this.message = message;
    }

    /**
     * 使用统一状态枚举创建异常。
     *
     * @param codeEnum 业务状态枚举
     */
    public BaseException(ResultCodeEnum codeEnum) {
        super(codeEnum.getMessage());
        this.code = codeEnum.getCode();
        this.message = codeEnum.getMessage();
        this.resultCodeEnum = codeEnum;
    }
}
