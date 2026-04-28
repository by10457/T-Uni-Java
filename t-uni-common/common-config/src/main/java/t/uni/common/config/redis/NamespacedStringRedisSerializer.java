package t.uni.common.config.redis;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 顶层 key 命名空间序列化器。
 */
public class NamespacedStringRedisSerializer implements RedisSerializer<String> {

    private final StringRedisSerializer delegate = new StringRedisSerializer();
    private final RedisKeyNamespace redisKeyNamespace;

    public NamespacedStringRedisSerializer(RedisKeyNamespace redisKeyNamespace) {
        this.redisKeyNamespace = redisKeyNamespace;
    }

    @Override
    public byte[] serialize(String value) throws SerializationException {
        return delegate.serialize(redisKeyNamespace.apply(value));
    }

    @Override
    public String deserialize(byte[] bytes) throws SerializationException {
        return redisKeyNamespace.strip(delegate.deserialize(bytes));
    }
}
