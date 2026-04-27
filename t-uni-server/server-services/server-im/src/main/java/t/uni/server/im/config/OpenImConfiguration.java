package t.uni.server.im.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * OpenIM 条件装配入口
 * <p>
 * openim.enabled=true 时生效，注册 OpenImProperties 并扫描 IM Mapper。
 * 未启用时 IM Controller、Client、Service、Webhook 和启动初始化器都不会装配。
 *
 * @author t-uni
 * @since 2026-04-24
 */
@Configuration
@ConditionalOnOpenImEnabled
@EnableConfigurationProperties(OpenImProperties.class)
@MapperScan("t.uni.server.im.persistence")
public class OpenImConfiguration {
}
