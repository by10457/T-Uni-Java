package t.uni.common.config.qiniu;

import com.qiniu.http.Headers;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import t.uni.common.core.exception.BaseException;
import t.uni.common.config.redis.RedisUtil;
import t.uni.common.core.result.ResultCodeEnum;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.stream.Collectors;

/**
 * 七牛云存储服务
 *
 * @author lzx
 * @since 2026-01-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(Auth.class)
public class QiniuStorageService {

    private final Auth auth;
    private final QiniuProperties properties;
    private final RedisUtil redisUtil;
    private UploadManager uploadManager;

    @PostConstruct
    public void init() {
        this.uploadManager = new UploadManager(new Configuration(Region.autoRegion()));
    }

    /**
     * 生成通用上传凭证（不指定 key，允许上传任意文件到 bucket）
     * <p>
     * 适用于批量上传场景，前端自行控制文件名。
     * 一个 Token 可复用上传多张图片。
     *
     * @return 上传凭证 Token
     */
    public String generateBucketToken() {
        StringMap putPolicy = new StringMap();
        if (properties.getCallbackUrl() != null && !properties.getCallbackUrl().isBlank()) {
            putPolicy.put("callbackUrl", properties.getCallbackUrl());
            putPolicy.put("callbackBody",
                    "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"bucket\":\"$(bucket)\"," +
                            "\"fsize\":$(fsize),\"mimeType\":\"$(mimeType)\"," +
                            "\"width\":$(imageInfo.width),\"height\":$(imageInfo.height)}");
            putPolicy.put("callbackBodyType", "application/json");
        }
        putPolicy.put("mimeLimit", "image/*");
        putPolicy.put("fsizeLimit", 15 * 1024 * 1024);
        return auth.uploadToken(properties.getBucket(), null, properties.getTokenExpireSeconds(), putPolicy);
    }

    /**
     * 头像上传令牌：绑定 fileKey，限制为图片且禁止覆盖。
     */
    public String generateAvatarUploadToken(String fileKey) {
        var normalizedKey = normalizeFileKey(fileKey);
        StringMap putPolicy = new StringMap();
        putPolicy.put("mimeLimit", "image/*");
        putPolicy.put("fsizeLimit", 15 * 1024 * 1024);
        putPolicy.put("insertOnly", 1);
        return auth.uploadToken(properties.getBucket(), normalizedKey, properties.getTokenExpireSeconds(), putPolicy);
    }

    /**
     * 获取 Token 过期时间戳（秒）
     *
     * @return Unix 时间戳（秒）
     */
    public long getTokenExpiresAt() {
        return System.currentTimeMillis() / 1000 + properties.getTokenExpireSeconds();
    }

    public String getUploadHost() {
        return properties.getUploadHost();
    }

    public String getDomain() {
        return properties.getDomain();
    }

    /**
     * 验证七牛云回调签名
     *
     * @param authHeader   请求头 Authorization 字段
     * @param callbackBody 请求体原始字节
     * @return 是否合法
     */
    public boolean verifyCallback(String authHeader, byte[] callbackBody) {
        var headers = new Headers.Builder()
                .add(Auth.HTTP_HEADER_KEY_CONTENT_TYPE, "application/json")
                .build();
        var request = new Auth.Request(properties.getCallbackUrl(), "POST", headers, callbackBody);
        boolean valid = auth.isValidCallback(authHeader, request);
        if (!valid) {
            log.warn("七牛云回调签名验证失败");
        }
        return valid;
    }

    /**
     * 服务端直接上传字节数组到七牛云。
     */
    public void uploadBytes(String fileKey, byte[] bytes, String mimeType) {
        var normalizedKey = normalizeFileKey(fileKey);
        if (bytes == null || bytes.length == 0) {
            throw new BaseException(ResultCodeEnum.UPLOAD_BYTES_EMPTY);
        }
        Response response = null;
        try {
            response = uploadManager.put(bytes, normalizedKey, auth.uploadToken(properties.getBucket(), normalizedKey), null, mimeType, false);
            if (response == null || !response.isOK()) {
                throw new BaseException(ResultCodeEnum.UPLOAD_ERROR.getCode(),
                        "七牛上传失败，status=" + (response == null ? -1 : response.statusCode));
            }
            log.info("七牛上传成功: key={}, size={}bytes", normalizedKey, bytes.length);
        } catch (Exception e) {
            if (e instanceof BaseException baseException) {
                throw baseException;
            }
            log.error("七牛上传失败: key={}, error={}", normalizedKey, e.getMessage(), e);
            throw new BaseException(ResultCodeEnum.SERVICE_ERROR);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    public long getDownloadExpireSeconds() {
        return getDownloadExpireSeconds(properties.getAccessPolicy());
    }

    private long getDownloadExpireSeconds(QiniuAccessPolicy policy) {
        if (policy == QiniuAccessPolicy.PUBLIC) {
            return properties.getDownloadExpireSeconds();
        }
        return useTimestampSignedUrl()
                ? properties.getCdnTimestampExpireSeconds()
                : properties.getDownloadExpireSeconds();
    }

    /**
     * 生成下载链接（使用全局 accessPolicy）。
     */
    public String generateDownloadUrl(String fileKey) {
        return generateDownloadUrl(fileKey, properties.getAccessPolicy());
    }

    /**
     * 入库前标准化：系统内七牛 URL 尽量回收成 fileKey，外链保持原样。
     */
    public String normalizeForStorage(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return rawValue;
        }
        var trimmed = rawValue.trim();
        if (!isHttpUrl(trimmed)) {
            return normalizeFileKey(trimmed);
        }
        var fileKey = tryExtractFileKeyFromManagedUrl(trimmed);
        return fileKey == null ? trimmed : fileKey;
    }

    /**
     * 响应前转换成可访问地址：外链原样返回，内部资源统一转访问地址。
     * <p>
     * 使用全局 accessPolicy。
     */
    public String resolveAccessUrl(String rawValue) {
        return resolveAccessUrl(rawValue, properties.getAccessPolicy());
    }

    /**
     * 响应前转换成可访问地址（显式指定访问策略）。
     * <p>
     * 当一个 bucket 内同时存有公开和私有资源时，调用方可以按字段/场景传入不同的 policy。
     *
     * @param rawValue 原始值（fileKey 或 URL）
     * @param policy   访问策略
     * @return 可访问地址
     */
    public String resolveAccessUrl(String rawValue, QiniuAccessPolicy policy) {
        if (rawValue == null || rawValue.isBlank()) {
            return rawValue;
        }
        var trimmed = rawValue.trim();
        if (!isHttpUrl(trimmed)) {
            return generateDownloadUrl(trimmed, policy);
        }
        var fileKey = tryExtractFileKeyFromManagedUrl(trimmed);
        return fileKey == null ? trimmed : generateDownloadUrl(fileKey, policy);
    }

    /**
     * 生成下载链接（显式指定访问策略）。
     *
     * @param fileKey 文件标识
     * @param policy  访问策略
     * @return 可访问地址
     */
    public String generateDownloadUrl(String fileKey, QiniuAccessPolicy policy) {
        var normalizedKey = normalizeFileKey(fileKey);
        var encodedKey = encodePathPreserveSlash(normalizedKey);
        var domain = trimTrailingSlash(properties.getDomain());

        if (policy == QiniuAccessPolicy.PUBLIC) {
            return domain + "/" + encodedKey;
        }

        var expireSeconds = getDownloadExpireSeconds(policy);
        var useTimestamp = useTimestampSignedUrl();

        if (!properties.isDownloadUrlCacheEnabled()) {
            return useTimestamp
                    ? buildTimestampSignedUrl(domain, encodedKey, expireSeconds)
                    : auth.privateDownloadUrl(domain + "/" + encodedKey, expireSeconds);
        }

        var signerVersion = useTimestamp
                ? sha256Hex("ts|" + nullToEmpty(properties.getCdnTimestampKey()))
                : sha256Hex("private|" + nullToEmpty(properties.getAccessKey()) + "|" + nullToEmpty(properties.getSecretKey()));
        var cacheId = sha256Hex((useTimestamp ? "ts" : "private") + "|" + signerVersion + "|" + domain + "|" + normalizedKey + "|" + expireSeconds);
        var cacheKey = QiniuRedisKeys.qiniuDownloadUrl(cacheId);
        var cachedUrl = getCachedDownloadUrl(cacheKey);
        if (cachedUrl != null) {
            log.debug("命中七牛下载链接缓存: key={}", normalizedKey);
            return cachedUrl;
        }

        var signedUrl = useTimestamp
                ? buildTimestampSignedUrl(domain, encodedKey, expireSeconds)
                : auth.privateDownloadUrl(domain + "/" + encodedKey, expireSeconds);
        cacheDownloadUrl(cacheKey, signedUrl, expireSeconds);
        return signedUrl;
    }

    private String encodePathPreserveSlash(String path) {
        return Arrays.stream(path.split("/", -1))
                .map(part -> URLEncoder.encode(part, StandardCharsets.UTF_8).replace("+", "%20"))
                .collect(Collectors.joining("/"));
    }

    private String normalizeFileKey(String fileKey) {
        if (fileKey == null || fileKey.isBlank()) {
            throw new BaseException(ResultCodeEnum.FILE_KEY_EMPTY);
        }
        var normalized = fileKey.trim();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.isBlank()) {
            throw new BaseException(ResultCodeEnum.FILE_KEY_EMPTY);
        }
        return normalized;
    }

    private String trimTrailingSlash(String domain) {
        if (domain == null) {
            return "";
        }
        var normalized = domain.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private boolean isPublicAccessPolicy() {
        return properties.getAccessPolicy() == QiniuAccessPolicy.PUBLIC;
    }

    private String getCachedDownloadUrl(String cacheKey) {
        try {
            var cached = redisUtil.get(cacheKey);
            if (cached instanceof String url && !url.isBlank()) {
                return url;
            }
        } catch (Exception e) {
            log.warn("读取七牛下载链接缓存失败: cacheKey={}, error={}", cacheKey, e.getMessage());
        }
        return null;
    }

    private void cacheDownloadUrl(String cacheKey, String signedUrl, long expireSeconds) {
        try {
            long ttlSeconds = expireSeconds - Math.max(0, properties.getDownloadUrlCacheRefreshAheadSeconds());
            if (ttlSeconds > 0) {
                redisUtil.set(cacheKey, signedUrl, ttlSeconds);
            }
        } catch (Exception e) {
            log.warn("写入七牛下载链接缓存失败: cacheKey={}, error={}", cacheKey, e.getMessage());
        }
    }

    private boolean useTimestampSignedUrl() {
        return properties.getCdnTimestampKey() != null && !properties.getCdnTimestampKey().isBlank();
    }

    private boolean isHttpUrl(String value) {
        return value.startsWith("http://") || value.startsWith("https://");
    }

    private String tryExtractFileKeyFromManagedUrl(String rawUrl) {
        try {
            var domain = trimTrailingSlash(properties.getDomain());
            if (domain.isBlank()) {
                return null;
            }
            var rawUri = URI.create(rawUrl);
            var domainUri = URI.create(domain);
            if (!nullToEmpty(rawUri.getHost()).equalsIgnoreCase(nullToEmpty(domainUri.getHost()))) {
                return null;
            }
            // URI.getPath() 已经完成了 %XX 解码，不要再用 URLDecoder.decode，
            // 因为 URLDecoder 会把 path 中合法的 '+' 错误地转成空格。
            var path = rawUri.getPath();
            if (path == null || path.isBlank() || "/".equals(path)) {
                return null;
            }
            return normalizeFileKey(path.substring(1));
        } catch (Exception e) {
            log.debug("从七牛 URL 提取 fileKey 失败: url={}, error={}", rawUrl, e.getMessage());
            return null;
        }
    }

    private String buildTimestampSignedUrl(String domain, String encodedPath, long expireSeconds) {
        long deadline = System.currentTimeMillis() / 1000 + expireSeconds;
        String timestamp = Long.toHexString(deadline).toLowerCase();
        String path = "/" + encodedPath;
        String sign = md5Hex(properties.getCdnTimestampKey() + path + timestamp);
        String privateUrl = auth.privateDownloadUrl(domain + path, expireSeconds);
        return privateUrl + "&sign=" + sign + "&t=" + timestamp;
    }

    private String md5Hex(String input) {
        return digestHex("MD5", input);
    }

    private String sha256Hex(String input) {
        return digestHex("SHA-256", input);
    }

    private String digestHex(String algorithm, String input) {
        try {
            var digest = MessageDigest.getInstance(algorithm);
            return HexFormat.of().formatHex(digest.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("计算七牛签名失败", e);
        }
    }
}
