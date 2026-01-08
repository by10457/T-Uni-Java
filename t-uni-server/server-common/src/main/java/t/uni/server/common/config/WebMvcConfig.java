package t.uni.server.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import t.uni.server.common.interceptor.AuthInterceptor;

/**
 * Web MVC 配置
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册认证拦截器
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**") // 拦截所有请求
                .excludePathPatterns("/auth/**", "/error", "/swagger-ui/**", "/v3/api-docs/**"); // 排除登录等路径
    }
}
