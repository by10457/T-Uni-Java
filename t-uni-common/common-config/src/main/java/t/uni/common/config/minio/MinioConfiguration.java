package t.uni.common.config.minio;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * MinIO 客户端配置。
 * <p>
 * 当启用 dromara.minio.enabled 时创建 MinioClient，并可在桶不存在时创建桶与公开读策略。
 * accessKey、secretKey 属于敏感配置，不应输出到日志或接口响应。
 * </p>
 */
@Configuration
@ConfigurationProperties(prefix = "dromara.minio")
@ConditionalOnProperty(prefix = "dromara.minio", name = "enabled", havingValue = "true")
@Data
public class MinioConfiguration {

    /**
     * MinIO 服务地址。
     */
    private String endpointUrl;
    /**
     * 访问密钥。
     */
    private String accessKey;
    /**
     * 私有密钥。
     */
    private String secretKey;
    /**
     * 桶名称。
     */
    private String bucketName;
    /**
     * 是否自动创建桶。
     */
    private boolean autoCreateBucket = true;
    /**
     * 自动创建桶时是否设置公开读策略。
     */
    private boolean publicRead = true;

    /**
     * 创建 MinIO 客户端，并在桶不存在时初始化公开读策略。
     *
     * @return MinIO 客户端
     */
    @Bean
    public MinioClient minioClient() {
        validateProperties();
        MinioClient minioClient = MinioClient.builder().endpoint(endpointUrl).credentials(accessKey, secretKey).build();

        if (autoCreateBucket) {
            ensureBucket(minioClient);
        }

        return minioClient;
    }

    private void validateProperties() {
        if (!StringUtils.hasText(endpointUrl)
                || !StringUtils.hasText(accessKey)
                || !StringUtils.hasText(secretKey)
                || !StringUtils.hasText(bucketName)) {
            throw new IllegalStateException("MinIO 已启用，但 endpointUrl/accessKey/secretKey/bucketName 配置不完整");
        }
    }

    private void ensureBucket(MinioClient minioClient) {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
            if (publicRead) {
                minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucketName).config(publicReadPolicy()).build());
            }
        } catch (Exception exception) {
            throw new IllegalStateException("初始化 MinIO bucket 失败: " + bucketName, exception);
        }
    }

    private String publicReadPolicy() {
        return String.format("""
                {
                  "Version": "2012-10-17",
                  "Statement": [
                    {
                      "Effect": "Allow",
                      "Principal": "*",
                      "Action": ["s3:GetObject"],
                      "Resource": ["arn:aws:s3:::%s/*"]
                    }
                  ]
                }
                """, bucketName);
    }
}
