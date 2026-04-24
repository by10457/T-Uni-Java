package t.uni.server.im.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.*;

/**
 * 当 openim.enabled=true 时才注册 Bean
 *
 * @author t-uni
 * @since 2026-04-24
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConditionalOnProperty(prefix = "openim", name = "enabled", havingValue = "true")
public @interface ConditionalOnOpenImEnabled {
}
