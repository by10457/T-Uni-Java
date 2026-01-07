package t.uni.common.core.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import t.uni.common.core.result.ResultCodeEnum;

@NoArgsConstructor
@Getter
@ToString
@Slf4j
public class BaseException extends RuntimeException {
    // 状态码
    Integer code;

    // 描述信息
    String message = "服务异常";

    // 返回结果状态
    ResultCodeEnum resultCodeEnum;


    public BaseException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BaseException(String message) {
        super(message);
        this.message = message;
    }

    public BaseException(ResultCodeEnum codeEnum) {
        super(codeEnum.getMessage());
        this.code = codeEnum.getCode();
        this.message = codeEnum.getMessage();
        this.resultCodeEnum = codeEnum;
    }
}
