package t.uni.server.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import t.uni.server.common.interceptor.AuthInterceptor;

/**
 * Web MVC 运行时配置。
 * <p>
 * 当前只注册轻量 API 认证拦截器，并集中维护无需登录态的白名单路径。
 * 新增公开端点时应优先在本配置中评估是否放行。
 * </p>
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    /**
     * 注册认证拦截器及其排除路径。
     *
     * @param registry Spring MVC 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 拦截业务请求，登录、健康检查、文档和外部回调入口不依赖用户登录态。
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**") // 拦截所有请求
                .excludePathPatterns(
                        "/auth/wxLogin",
                        "/auth/refreshToken",
                        "/health/**",
                        "/error",

                        // Knife4j 文档
                        "/doc.html",
                        "/doc.html/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/swagger-resources",
                        "/swagger-resources/**",
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/webjars/**",
                        "/favicon.ico",

                        // 微信支付回调（不走登录态认证）
                        "/payment/notify/**",

                        // OpenIM Webhook 回调（不走认证拦截）
                        "/openim/webhook/**");
    }
}
