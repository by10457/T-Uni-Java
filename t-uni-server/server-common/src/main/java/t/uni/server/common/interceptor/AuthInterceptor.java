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
 * 认证拦截器
 * <p>
 * 验证 JWT Token 并将用户信息存入上下文
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenService tokenService;

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request,
                             @NotNull HttpServletResponse response,
                             @NotNull Object handler) {
        // 1. 获取 Authorization 请求头
        var authHeader = request.getHeader(AuthConstant.HEADER_AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(AuthConstant.TOKEN_PREFIX)) {
            throw new BaseException(ResultCodeEnum.TOKEN_NOT_PROVIDED);
        }

        // 2. 提取 Token
        var accessToken = authHeader.substring(AuthConstant.TOKEN_PREFIX.length());

        // 3. 验证 Token 并获取用户ID
        var userId = tokenService.validateAndGetUserId(accessToken);

        // 4. 将用户ID存入上下文
        UserContext.setUserId(userId);

        log.debug("用户 {} 认证通过", userId);
        return true;
    }

    @Override
    public void afterCompletion(@NotNull HttpServletRequest request,
                                @NotNull HttpServletResponse response,
                                @NotNull Object handler, Exception ex) {
        // 请求结束后清理上下文，避免内存泄漏
        UserContext.clear();
    }
}
