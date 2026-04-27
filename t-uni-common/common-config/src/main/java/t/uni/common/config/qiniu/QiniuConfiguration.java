package t.uni.common.config.qiniu;

import com.qiniu.util.Auth;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 七牛云自动配置。
 * <p>
 * 当配置了 qiniu.access-key 时创建 Auth 客户端，供存储服务生成上传和下载签名。
 * 不校验业务 bucket 权限，也不输出 Secret Key。
 * </p>
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(QiniuProperties.class)
@ConditionalOnProperty(name = "qiniu.access-key")
public class QiniuConfiguration {

    /**
     * 创建七牛云认证客户端。
     *
     * @param properties 七牛配置属性
     * @return 七牛 Auth 客户端
     */
    @Bean
    public Auth qiniuAuth(QiniuProperties properties) {
        log.info("初始化七牛云认证客户端，bucket: {}", properties.getBucket());
        return Auth.create(properties.getAccessKey(), properties.getSecretKey());
    }
}
