package t.uni.common.config.jwt;

import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

/**
 * JWT 密钥持有者
 * <p>
 * 在应用启动时从配置文件读取 JWT 密钥，并提供静态访问方法供工具类使用
 * 使用 AtomicReference 实现线程安全的静态密钥管理（兼容 JDK 17+）
 * </p>
 */
@Slf4j
@Component
public class JwtKeyHolder {

    /**
     * 静态密钥字段（使用 AtomicReference 确保线程安全）
     */
    private static final AtomicReference<SecretKey> KEY = new AtomicReference<>();

    /**
     * 构造函数：在 Spring 初始化时执行
     *
     * @param properties JWT 配置属性
     */
    public JwtKeyHolder(JwtProperties properties) {
        String secret = properties.getSecret();

        // 校验密钥是否配置
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT密钥未配置：请在配置文件中设置 t.uni.jwt.secret（至少256位/32字节）");
        }

        // 校验密钥长度
        if (secret.length() < 32) {
            throw new IllegalStateException("JWT密钥长度不足：必须至少32字节（256位），当前长度：" + secret.length());
        }

        // 创建密钥对象并设置到静态字段
        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        KEY.set(secretKey);

        log.info("JWT密钥初始化成功，长度：{} 字节", secret.length());
    }

    /**
     * 获取 JWT 密钥（静态方法）
     * <p>
     * 供静态工具类 JwtTokenUtil 使用
     * </p>
     *
     * @return JWT 密钥
     */
    public static SecretKey getKey() {
        SecretKey key = KEY.get();
        if (key == null) {
            throw new IllegalStateException("JWT密钥未初始化：请确保 JwtKeyHolder 已被加载");
        }
        return key;
    }
}
