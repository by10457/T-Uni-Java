package t.uni.api.config.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import t.uni.core.context.BaseContext;

@Component
public class ThreadLocalCleanupInterceptor implements HandlerInterceptor {

    /* 请求完成后清理 */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.removeUser();
    }
}