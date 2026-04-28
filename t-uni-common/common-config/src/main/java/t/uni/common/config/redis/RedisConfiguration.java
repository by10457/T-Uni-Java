package t.uni.common.config.redis;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Redis 序列化和缓存管理配置。
 * <p>
 * 统一使用字符串 key 和 JSON value，避免默认 JDK 序列化产生不可读数据。
 * 当前 RedisTemplate 不启用事务支持，调用方如需事务或 Pipeline 应使用 RedisUtil 的显式方法。
 * </p>
 */
@Component
@EnableConfigurationProperties(RedisNamespaceProperties.class)
public class RedisConfiguration {
    /**
     * 创建通用 RedisTemplate。
     *
     * @param connectionFactory Lettuce 连接工厂
     * @return 使用字符串 key 和 JSON value 的 RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory connectionFactory,
                                                        RedisKeyNamespace redisKeyNamespace) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        redisTemplate.setKeySerializer(new NamespacedStringRedisSerializer(redisKeyNamespace));
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        // 设置value序列化为JSON，使用GenericJackson2JsonRedisSerializer替换默认序列化
        redisTemplate.setValueSerializer(jsonRedisSerializer());
        redisTemplate.setHashValueSerializer(jsonRedisSerializer());

        // 注意：不启用 Redis 事务支持
        // 原因：启用后，在 Spring @Transactional 方法中，setIfAbsent 等命令会返回 null
        // redisTemplate.setEnableTransactionSupport(true);
        return redisTemplate;
    }

    /**
     * 创建默认缓存管理器，缓存有效期为 30 天。
     *
     * @param factory Redis 连接工厂
     * @return 默认 CacheManager
     */
    @Bean
    @Primary
    @SuppressWarnings("all")
    public CacheManager cacheManagerWithMouth(RedisConnectionFactory factory,
                                               RedisKeyNamespace redisKeyNamespace) {
        // 配置序列化
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .computePrefixWith(cacheName -> redisKeyNamespace.apply(cacheName + "::"))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonRedisSerializer()))
                .entryTtl(Duration.ofDays(30));

        return RedisCacheManager.builder(factory).cacheDefaults(config).build();
    }

    /**
     * 创建缓存有效期为 15 天的缓存管理器。
     *
     * @param factory Redis 连接工厂
     * @return CacheManager
     */
    @Bean
    @SuppressWarnings("all")
    public CacheManager cacheManagerWithHalfOfMouth(RedisConnectionFactory factory,
                                                     RedisKeyNamespace redisKeyNamespace) {
        // 配置序列化
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .computePrefixWith(cacheName -> redisKeyNamespace.apply(cacheName + "::"))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonRedisSerializer()))
                .entryTtl(Duration.ofDays(15));

        return RedisCacheManager.builder(factory).cacheDefaults(config).build();
    }

    /**
     * 创建缓存有效期为 1 小时的缓存管理器。
     *
     * @param factory Redis 连接工厂
     * @return CacheManager
     */
    @Bean
    @SuppressWarnings("all")
    public CacheManager cacheManagerWithHours(RedisConnectionFactory factory,
                                               RedisKeyNamespace redisKeyNamespace) {
        // 配置序列化
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .computePrefixWith(cacheName -> redisKeyNamespace.apply(cacheName + "::"))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonRedisSerializer()))
                .entryTtl(Duration.ofHours(1));

        return RedisCacheManager.builder(factory).cacheDefaults(config).build();
    }

    /**
     * 创建 Redis JSON 序列化器。
     * <p>
     * 统一处理 Java 8 时间类型，避免 LocalDateTime 在缓存中使用时间戳或 UTC 默认格式。
     * </p>
     *
     * @return Jackson Redis 序列化器
     */
    public Jackson2JsonRedisSerializer<Object> jsonRedisSerializer() {
        // LocalDatetime序列化，默认不兼容jdk8日期序列化
        JavaTimeModule timeModule = new JavaTimeModule();
        timeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        timeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        timeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        timeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // 对象序列化
        ObjectMapper mapper = new ObjectMapper();

        // 设置ObjectMapper访问权限
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        // 关闭默认的日期格式化方式，默认UTC日期格式 yyyy-MM-dd'T'HH:mm:ss.SSS
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.registerModule(timeModule);

        return new Jackson2JsonRedisSerializer<>(mapper, Object.class);
    }
}
