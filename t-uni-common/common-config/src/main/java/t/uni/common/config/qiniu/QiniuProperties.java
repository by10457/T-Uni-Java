package t.uni.common.config.qiniu;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 七牛云配置属性
 *
 * @author lzx
 * @since 2026-01-17
 */
@Data
@ConfigurationProperties(prefix = "qiniu")
public class QiniuProperties {

    /**
     * Access Key
     */
    private String accessKey;

    /**
     * Secret Key
     */
    private String secretKey;

    /**
     * 存储空间名称
     */
    private String bucket;

    /**
     * CDN 加速域名或空间绑定域名（用于下载）
     */
    private String domain;

    /**
     * 上传成功后的回调地址
     */
    private String callbackUrl;

    /**
     * 上传凭证有效期（秒），默认 1 小时
     */
    private long tokenExpireSeconds = 3600;

    /**
     * 下载链接有效期（秒），默认 1 小时
     */
    private long downloadExpireSeconds = 3600;
}
