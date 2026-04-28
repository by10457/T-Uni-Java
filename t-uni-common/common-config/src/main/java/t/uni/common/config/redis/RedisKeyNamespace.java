package t.uni.common.config.redis;

import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Component;

/**
 * Redis 逻辑 key 与物理 key 命名空间转换器。
 */
@Component
public class RedisKeyNamespace {

    private static final String SEPARATOR = "::";

    private final String prefix;

    public RedisKeyNamespace(RedisNamespaceProperties properties) {
        var namespace = properties.getNamespace();
        if (StrUtil.isBlank(namespace)) {
            this.prefix = "";
            return;
        }
        var normalized = namespace.trim();
        while (normalized.endsWith(SEPARATOR)) {
            normalized = normalized.substring(0, normalized.length() - SEPARATOR.length());
        }
        this.prefix = normalized + SEPARATOR;
    }

    public String apply(String key) {
        if (StrUtil.isBlank(prefix) || key == null || key.startsWith(prefix)) {
            return key;
        }
        return prefix + key;
    }

    public String applyPattern(String pattern) {
        return apply(pattern);
    }

    public String strip(String key) {
        if (StrUtil.isBlank(prefix) || key == null || !key.startsWith(prefix)) {
            return key;
        }
        return key.substring(prefix.length());
    }

    public boolean enabled() {
        return StrUtil.isNotBlank(prefix);
    }

    public String prefix() {
        return prefix;
    }
}
