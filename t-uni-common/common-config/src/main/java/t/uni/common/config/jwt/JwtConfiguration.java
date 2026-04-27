package t.uni.common.config.jwt;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT 自动配置入口。
 * <p>
 * 负责启用 JwtProperties 绑定，不创建会话或认证过滤器。
 * </p>
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfiguration {
}
