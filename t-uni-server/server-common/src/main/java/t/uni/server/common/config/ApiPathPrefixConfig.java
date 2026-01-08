package t.uni.server.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * API 路径前缀配置
 * <p>
 * 自动为所有 Controller 的路径添加统一前缀
 * </p>
 *
 * @author T-Uni-Java
 * @since 2026-01-09
 */
@Configuration
@RequiredArgsConstructor
public class ApiPathPrefixConfig implements WebMvcConfigurer {

    @Value("${api.prefix:}")
    private String apiPrefix;

    /**
     * 配置路径匹配规则
     * <p>
     * 所有 @RequestMapping 的路径都会自动加上 api.prefix 前缀
     * </p>
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(apiPrefix, clazz -> true);
    }
}
