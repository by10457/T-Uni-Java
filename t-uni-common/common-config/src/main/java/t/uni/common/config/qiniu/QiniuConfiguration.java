package t.uni.common.config.qiniu;

import com.qiniu.util.Auth;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 七牛云配置类
 *
 * @author lzx
 * @since 2026-01-17
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(QiniuProperties.class)
@ConditionalOnProperty(name = "qiniu.access-key")
public class QiniuConfiguration {

    /**
     * 创建七牛云认证客户端
     */
    @Bean
    public Auth qiniuAuth(QiniuProperties properties) {
        log.info("初始化七牛云认证客户端，bucket: {}", properties.getBucket());
        return Auth.create(properties.getAccessKey(), properties.getSecretKey());
    }
}
