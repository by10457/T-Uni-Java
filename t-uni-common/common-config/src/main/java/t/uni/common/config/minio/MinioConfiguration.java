package t.uni.common.config.minio;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 客户端配置。
 * <p>
 * 当配置了 dromara.x-file-storage.minio.bucket-name 时创建 MinioClient，并在桶不存在时创建公开读策略。
 * accessKey、secretKey 属于敏感配置，不应输出到日志或接口响应。
 * </p>
 */
@Configuration
@ConditionalOnProperty(name = "dromara.x-file-storage.minio.bucket-name")
@Data
public class MinioConfiguration {

    /**
     * MinIO 服务地址。
     */
    private String domain;
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
     * 创建 MinIO 客户端，并在桶不存在时初始化公开读策略。
     *
     * @return MinIO 客户端
     */
    @Bean
    public MinioClient minioClient() {
        MinioClient minioClient = MinioClient.builder().endpoint(domain).credentials(accessKey, secretKey).build();

        try {
            // 只在桶不存在时创建并设置公开读策略，避免反复覆盖已有桶策略。
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                String publicPolicy = String.format("{\n" +
                        "  \"Version\": \"2012-10-17\",\n" +
                        "  \"Statement\": [\n" +
                        "    {\n" +
                        "      \"Effect\": \"Allow\",\n" +
                        "      \"Principal\": \"*\",\n" +
                        "      \"Action\": [\"s3:GetObject\"],\n" +
                        "      \"Resource\": [\"arn:aws:s3:::%s/*\"]\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}", bucketName);

                minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucketName).config(publicPolicy).build());
            }
        } catch (Exception exception) {
            exception.getStackTrace();
        }

        return minioClient;
    }
}
