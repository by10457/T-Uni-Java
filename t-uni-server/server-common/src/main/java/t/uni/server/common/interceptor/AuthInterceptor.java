package t.uni.server.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import t.uni.common.core.exception.BaseException;
import t.uni.common.core.result.ResultCodeEnum;
import t.uni.server.common.auth.TokenService;
import t.uni.server.common.context.UserContext;
import t.uni.server.domain.constant.AuthConstant;

/**
 * 轻量 API 认证拦截器。
 * <p>
 * 负责校验请求头中的 Bearer Access Token，并把用户 ID 写入 {@link UserContext}。
 * 本拦截器只处理登录态识别，不承载后台 RBAC 权限判断。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenService tokenService;

    /**
     * 请求进入业务处理前校验 Access Token。
     *
     * @return 校验通过返回 true；缺少或无效 Token 时抛出业务异常
     */
    @Override
    public boolean preHandle(@NotNull HttpServletRequest request,
                             @NotNull HttpServletResponse response,
                             @NotNull Object handler) {
        // 仅接受标准 Bearer Token，避免把其他 Authorization 方案误当作登录态。
        var authHeader = request.getHeader(AuthConstant.HEADER_AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(AuthConstant.TOKEN_PREFIX)) {
            throw new BaseException(ResultCodeEnum.TOKEN_NOT_PROVIDED);
        }

        var accessToken = authHeader.substring(AuthConstant.TOKEN_PREFIX.length());

        var userId = tokenService.validateAndGetUserId(accessToken);

        UserContext.setUserId(userId);

        log.debug("用户 {} 认证通过", userId);
        return true;
    }

    /**
     * 请求结束后清理线程上下文。
     * <p>
     * 无论业务处理成功或失败都要执行，防止容器线程复用导致用户 ID 泄漏。
     * </p>
     */
    @Override
    public void afterCompletion(@NotNull HttpServletRequest request,
                                @NotNull HttpServletResponse response,
                                @NotNull Object handler, Exception ex) {
        UserContext.clear();
    }
}
