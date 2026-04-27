package t.uni.common.config.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 配置属性。
 * <p>
 * 绑定配置前缀 {@code t.uni.jwt}，只保存签名密钥等基础配置。
 * 密钥属于敏感配置，应通过环境变量或安全配置中心注入，不应提交到代码仓库。
 * </p>
 */
@Data
@ConfigurationProperties(prefix = "t.uni.jwt")
public class JwtProperties {

    /**
     * JWT 签名密钥，至少 256 位 / 32 字节。
     * <p>
     * 用于 HMAC-SHA 算法签名 Token。未配置或长度不足时应用启动失败。
     * </p>
     */
    private String secret;
}
