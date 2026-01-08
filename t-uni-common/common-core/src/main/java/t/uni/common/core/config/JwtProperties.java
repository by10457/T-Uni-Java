package t.uni.common.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性类
 * <p>
 * 从配置文件读取 JWT 相关配置
 * 配置前缀：t.uni.jwt
 * </p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "t.uni.jwt")
public class JwtProperties {

    /**
     * JWT 密钥（至少256位/32字节）
     * <p>
     * 用于 HMAC-SHA 算法签名 JWT token
     * 必须在配置文件中设置，否则启动会失败
     * </p>
     */
    private String secret;
}
