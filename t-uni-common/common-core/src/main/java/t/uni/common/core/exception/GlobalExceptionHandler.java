package t.uni.common.core.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import t.uni.common.core.result.Result;
import t.uni.common.core.result.ResultCodeEnum;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局异常拦截器 (最终优化版)
 * <p>
 * 核心策略：
 * 1. 精准捕获：优先使用框架具体的异常类 (如 DuplicateKeyException)，而不是去解析字符串。
 * 2. 安全兜底：未知的 RuntimeException 统一屏蔽细节，防止 SQL/类结构泄露。
 * 3. 规范响应：参数错误返回明确提示，系统错误记录详细日志但只返回模糊提示。
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String DEFAULT_SERVER_ERROR_MSG = "服务器繁忙，请稍后重试";

    /**
     * 1. 业务自定义异常
     * 说明：业务层主动抛出的，通常包含对用户友好的提示，直接返回。
     */
    @ExceptionHandler(BaseException.class)
    public Result<Object> handleBaseException(BaseException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        Integer code = e.getCode() != null ? e.getCode() : 500;
        return Result.error(null, code, e.getMessage());
    }

    /**
     * 2. 参数校验异常 (@RequestBody JSON格式)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败(Body): {}", message);
        // 建议使用 400 状态码或业务约定的参数错误码
        return Result.error(null, ResultCodeEnum.PARAM_ERROR.getCode(), message);
    }

    /**
     * 2.1 参数绑定异常 (@RequestParam / Form表单)
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
     * 3. 请求体格式错误 (如 JSON 语法错误)
     * 说明：这就代替了原来的 "JSON parse error" 正则匹配
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Object> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("请求体读取失败: {}", e.getMessage());
        return Result.error(null, 500, "请求参数格式错误，请检查JSON格式");
    }

    /**
     * 4. 数据库唯一键冲突
     * 说明：这就代替了原来的 "Duplicate entry" 正则匹配
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Object> handleDuplicateKeyException(DuplicateKeyException e) {
        log.error("数据库唯一键冲突", e);
        return Result.error(null, 500, "数据已存在，请勿重复提交");
    }

    /**
     * 5. 数据库完整性约束异常 (如字段过长、非空字段为空)
     * 说明：这就代替了原来的 "Data too long" 正则匹配
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<Object> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("数据库完整性异常", e);
        return Result.error(null, 500, "提交的数据不符合规范(如内容过长或缺项)");
    }

    /**
     * 6. 兜底 SQL 异常 (防止部分数据库驱动未封装成 DataIntegrityViolationException)
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<Object> handleSQLIntegrityException(SQLIntegrityConstraintViolationException e) {
        log.error("SQL原生约束异常", e);
        String msg = e.getMessage();
        if (msg != null && msg.contains("Duplicate entry")) {
            return Result.error(null, 500, "数据已存在，请勿重复提交");
        }
        return Result.error(null, 500, "数据操作失败");
    }

    /**
     * 7. 404 异常 (需要在 yml 配置 throw-exception-if-no-handler-found: true)
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<Object> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("接口不存在: {}", e.getRequestURL());
        return Result.error(null, 404, "接口不存在");
    }

    /**
     * 8. 最终全能兜底
     * 说明：处理 Security 异常、MyBatis 系统异常以及所有未知的 RuntimeException
     */
    @ExceptionHandler(Exception.class)
    public Result<Object> handleException(Exception e) {
        String className = e.getClass().getName();
        String message = e.getMessage();

        // --- 特殊处理 Spring Security ---
        // 使用字符串匹配，防止 common 模块没有引入 security 依赖导致编译报错
        if (className.contains("AccessDeniedException") ||
                className.contains("AuthorizationDeniedException")) {
            log.warn("权限不足: {}", message);
            return Result.error(ResultCodeEnum.FAIL_NO_ACCESS_DENIED);
        }

        // --- MyBatis / 数据库连接层面的致命错误 ---
        if (className.contains("MyBatisSystemException") ||
                className.contains("PersistenceException")) {
            log.error("持久层严重错误", e);
            return Result.error(null, 500, "数据库服务异常");
        }

        // --- 未知异常 ---
        // 记录完整堆栈给开发看
        log.error("系统未知异常", e);

        // 给用户看模糊提示，绝对不要 return e.getMessage()
        return Result.error(null, 500, DEFAULT_SERVER_ERROR_MSG);
    }
}
