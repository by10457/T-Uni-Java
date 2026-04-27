package t.uni.common.config.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import t.uni.common.core.exception.BaseException;
import t.uni.common.core.result.Result;
import t.uni.common.core.result.ResultCodeEnum;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局 REST 异常处理器。
 * <p>
 * 负责把业务异常、参数校验异常和基础设施异常转换为统一 Result。
 * 已知业务提示可直接返回；未知系统异常只记录日志，不向客户端暴露 SQL、类名、堆栈或敏感配置。
 * </p>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String DEFAULT_SERVER_ERROR_MSG = "服务器繁忙，请稍后重试";

    /**
     * 处理业务层主动抛出的异常。
     *
     * @param e 业务异常
     * @return 统一错误响应
     */
    @ExceptionHandler(BaseException.class)
    public Result<Object> handleBaseException(BaseException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        Integer code = e.getCode() != null ? e.getCode() : ResultCodeEnum.SERVICE_ERROR.getCode();
        return Result.error(null, code, e.getMessage());
    }

    /**
     * 处理 @RequestBody 触发的 Bean Validation 校验异常。
     *
     * @param e 参数校验异常
     * @return 参数错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败(Body): {}", message);
        return Result.error(null, ResultCodeEnum.PARAM_ERROR.getCode(), message);
    }

    /**
     * 处理 @RequestParam 或表单参数绑定校验异常。
     *
     * @param e 参数绑定异常
     * @return 参数错误响应
     */
    @ExceptionHandler(BindException.class)
    public Result<Object> handleBindException(BindException e) {
        String message = e.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败(Param): {}", message);
        return Result.error(null, ResultCodeEnum.PARAM_ERROR.getCode(), message);
    }

    /**
     * 处理必传请求参数缺失异常。
     *
     * @param e 缺失参数异常
     * @return 参数错误响应
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Object> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e) {
        String message = e.getParameterName() + "不能为空";
        log.warn("必传参数缺失: {}", message);
        return Result.error(null, ResultCodeEnum.PARAM_ERROR.getCode(), message);
    }

    /**
     * 处理数字格式异常。
     * <p>
     * 常见于字符串 ID 超出 Long 范围，统一返回参数错误，避免泄露转换栈信息。
     * </p>
     *
     * @param e 数字格式异常
     * @return 参数错误响应
     */
    @ExceptionHandler(NumberFormatException.class)
    public Result<Object> handleNumberFormatException(NumberFormatException e) {
        log.warn("数字格式错误: {}", e.getMessage());
        return Result.error(null, ResultCodeEnum.PARAM_ERROR.getCode(), "ID格式无效");
    }

    /**
     * 处理请求体不可读异常，例如 JSON 语法错误。
     *
     * @param e 请求体读取异常
     * @return 参数错误响应
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Object> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("请求体读取失败: {}", e.getMessage());
        return Result.error(null, ResultCodeEnum.PARAM_ERROR.getCode(), "请求参数格式错误，请检查JSON格式");
    }

    /**
     * 处理数据库唯一键冲突。
     *
     * @param e 唯一键冲突异常
     * @return 数据已存在响应
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Object> handleDuplicateKeyException(DuplicateKeyException e) {
        log.error("数据库唯一键冲突", e);
        return Result.error(null, ResultCodeEnum.DATA_EXIST.getCode(), "数据已存在，请勿重复提交");
    }

    /**
     * 处理数据库完整性约束异常，例如字段过长或非空字段为空。
     *
     * @param e 数据完整性异常
     * @return 数据错误响应
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<Object> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("数据库完整性异常", e);
        return Result.error(null, ResultCodeEnum.DATA_ERROR.getCode(), "提交的数据不符合规范(如内容过长或缺项)");
    }

    /**
     * 处理未被 Spring 翻译的 SQL 完整性约束异常。
     *
     * @param e SQL 完整性约束异常
     * @return 数据错误响应
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<Object> handleSQLIntegrityException(SQLIntegrityConstraintViolationException e) {
        log.error("SQL原生约束异常", e);
        String msg = e.getMessage();
        if (msg != null && msg.contains("Duplicate entry")) {
            return Result.error(null, ResultCodeEnum.DATA_EXIST.getCode(), "数据已存在，请勿重复提交");
        }
        return Result.error(null, ResultCodeEnum.DATA_ERROR.getCode(), "数据操作失败");
    }

    /**
     * 处理路由不存在异常。
     * <p>
     * 需要开启 throw-exception-if-no-handler-found 才会进入该处理器。
     * </p>
     *
     * @param e 路由不存在异常
     * @return 接口不存在响应
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<Object> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("接口不存在: {}", e.getRequestURL());
        return Result.error(null, ResultCodeEnum.DATA_NOT_EXIST.getCode(), "接口不存在");
    }

    /**
     * 处理未被前置处理器捕获的异常。
     * <p>
     * 本方法是安全兜底边界：日志记录完整异常，客户端只返回泛化提示。
     * </p>
     *
     * @param e 未知异常
     * @return 统一错误响应
     */
    @ExceptionHandler(Exception.class)
    public Result<Object> handleException(Exception e) {
        String className = e.getClass().getName();
        String message = e.getMessage();

        // 使用类名匹配，避免 common 模块强依赖 Spring Security。
        if (className.contains("AccessDeniedException") ||
                className.contains("AuthorizationDeniedException")) {
            log.warn("权限不足: {}", message);
            return Result.error(ResultCodeEnum.FAIL_NO_ACCESS_DENIED);
        }

        if (className.contains("MyBatisSystemException") ||
                className.contains("PersistenceException")) {
            log.error("持久层严重错误", e);
            return Result.error(null, ResultCodeEnum.SERVICE_ERROR.getCode(), "数据库服务异常");
        }

        log.error("系统未知异常", e);

        // 未知异常不返回 e.getMessage()，避免泄露内部实现和敏感参数。
        return Result.error(null, ResultCodeEnum.SERVICE_ERROR.getCode(), DEFAULT_SERVER_ERROR_MSG);
    }
}
