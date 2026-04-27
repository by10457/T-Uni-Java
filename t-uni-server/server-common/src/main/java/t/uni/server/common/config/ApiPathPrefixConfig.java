package t.uni.server.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * API 全局路径前缀配置。
 * <p>
 * 根据 {@code api.prefix} 为业务 Controller 自动增加统一前缀。
 * Controller 内只维护业务相对路径，避免各模块硬编码全局 {@code /api}。
 * </p>
 *
 * @author T-Uni-Java
 * @since 2026-01-09
 */
@Configuration
public class ApiPathPrefixConfig implements WebMvcConfigurer {

    @Value("${api.prefix:}")
    private String apiPrefix;

    /**
     * 配置 Controller 路径前缀规则。
     * <p>
     * 当前只作用于 {@code t.uni.server.api.controller} 包下的 Controller；
     * springdoc、Knife4j 等非业务端点不走该前缀规则。
     * </p>
     *
     * @param configurer Spring MVC 路径匹配配置器
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(apiPrefix,
                clazz -> clazz.getPackageName().startsWith("t.uni.server.api.controller"));
    }
}
