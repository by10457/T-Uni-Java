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

    private static final long DEFAULT_EXPIRE_SECONDS = 3600;

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
     * 上传域名
     */
    private String uploadHost = "https://upload.qiniup.com";

    /**
     * 上传凭证有效期（秒），默认 1 小时
     */
    private long tokenExpireSeconds = DEFAULT_EXPIRE_SECONDS;

    /**
     * 下载链接有效期（秒），默认 1 小时
     */
    private long downloadExpireSeconds = DEFAULT_EXPIRE_SECONDS;

    /**
     * CDN 时间戳防盗链密钥，为空时回退为私有空间签名。
     */
    private String cdnTimestampKey;

    /**
     * CDN 时间戳防盗链链接有效期（秒）。
     */
    private long cdnTimestampExpireSeconds = 600;

    /**
     * 是否启用下载链接缓存。
     */
    private boolean downloadUrlCacheEnabled = true;

    /**
     * 下载链接缓存提前刷新时间（秒）。
     */
    private long downloadUrlCacheRefreshAheadSeconds = 60;
}
