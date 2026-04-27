package t.uni.server.im.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.*;

/**
 * OpenIM 可选模块装配开关。
 * <p>
 * 只有配置 openim.enabled=true 时，标注的 Bean 才会注册，避免未部署 OpenIM 时加载客户端、
 * Webhook、初始化器等依赖外部服务的组件。
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
