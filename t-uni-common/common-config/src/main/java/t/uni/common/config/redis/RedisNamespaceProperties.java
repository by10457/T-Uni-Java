package t.uni.common.config.redis;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Redis 命名空间配置。
 * <p>
 * namespace 用于隔离共用同一 Redis database 的不同项目实例，只作用于 Redis 顶层 key。
 * </p>
 */
@Data
@ConfigurationProperties(prefix = "t.uni.redis")
public class RedisNamespaceProperties {

    /**
     * 项目级 Redis key 命名空间，多个项目共用同一 Redis database 时必须保持唯一。
     */
    private String namespace;
}
