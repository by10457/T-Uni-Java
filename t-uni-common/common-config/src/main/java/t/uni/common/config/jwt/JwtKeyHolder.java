package t.uni.common.config.jwt;

import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

/**
 * JWT 签名密钥持有者。
 * <p>
 * 在 Spring 初始化时把配置密钥转换为 SecretKey，并提供静态访问给 JwtTokenUtil。
 * 只保存派生后的密钥对象，不在日志中输出密钥原文。
 * </p>
 */
@Slf4j
@Component
public class JwtKeyHolder {

    /**
     * 静态密钥引用，供非 Spring 管理的静态工具方法读取。
     */
    private static final AtomicReference<SecretKey> KEY = new AtomicReference<>();

    /**
     * 初始化 JWT 签名密钥。
     *
     * @param properties JWT 配置属性
     * @throws IllegalStateException 密钥缺失或长度不足时抛出，阻止应用以不安全配置启动
     */
    public JwtKeyHolder(JwtProperties properties) {
        String secret = properties.getSecret();

        // 启动期强校验，避免运行时签发弱签名 Token。
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT密钥未配置：请在配置文件中设置 t.uni.jwt.secret（至少256位/32字节）");
        }

        if (secret.length() < 32) {
            throw new IllegalStateException("JWT密钥长度不足：必须至少32字节（256位），当前长度：" + secret.length());
        }

        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        KEY.set(secretKey);

        log.info("JWT密钥初始化成功，长度：{} 字节", secret.length());
    }

    /**
     * 获取已初始化的 JWT 签名密钥。
     *
     * @return JWT 密钥
     * @throws IllegalStateException JwtKeyHolder 尚未由 Spring 初始化时抛出
     */
    public static SecretKey getKey() {
        SecretKey key = KEY.get();
        if (key == null) {
            throw new IllegalStateException("JWT密钥未初始化：请确保 JwtKeyHolder 已被加载");
        }
        return key;
    }
}
