package t.uni.common.config.qiniu;

import com.qiniu.http.Headers;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
        putPolicy.put("callbackUrl", properties.getCallbackUrl());
        putPolicy.put("callbackBody",
                "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"bucket\":\"$(bucket)\"," +
                "\"fsize\":$(fsize),\"mimeType\":\"$(mimeType)\"," +
                "\"width\":$(imageInfo.width),\"height\":$(imageInfo.height)}");
        putPolicy.put("callbackBodyType", "application/json");
        putPolicy.put("mimeLimit", "image/*");  // 限制只能上传图片
        putPolicy.put("fsizeLimit", 15 * 1024 * 1024);  // 限制 15MB

        // scope 只指定 bucket，不指定 key，允许上传任意文件
        String token = auth.uploadToken(properties.getBucket(), null, properties.getTokenExpireSeconds(), putPolicy);

        log.debug("生成通用上传凭证");
        return token;
    }

    /**
     * 获取 Token 过期时间戳（秒）
     *
     * @return Unix 时间戳（秒）
     */
    public long getTokenExpiresAt() {
        return System.currentTimeMillis() / 1000 + properties.getTokenExpireSeconds();
    }

    /**
     * 验证七牛云回调签名
     *
     * @param authHeader   请求头 Authorization 字段
     * @param callbackBody 请求体原始字节
     * @return 是否合法
     */
    public boolean verifyCallback(String authHeader, byte[] callbackBody) {
        // 构建回调请求对象
        var headers = new Headers.Builder()
                .add(Auth.HTTP_HEADER_KEY_CONTENT_TYPE, "application/json")
                .build();
        var request = new Auth.Request(properties.getCallbackUrl(), "POST", headers, callbackBody);

        // 验证签名
        boolean valid = auth.isValidCallback(authHeader, request);

        if (!valid) {
            log.warn("七牛云回调签名验证失败");
        }

        return valid;
    }

    /**
     * 生成私有空间下载链接
     *
     * @param fileKey 文件 Key
     * @return 带签名的下载 URL
     */
    public String generateDownloadUrl(String fileKey) {
        String encodedKey = URLEncoder.encode(fileKey, StandardCharsets.UTF_8).replace("+", "%20");
        String publicUrl = String.format("%s/%s", properties.getDomain(), encodedKey);
        return auth.privateDownloadUrl(publicUrl, properties.getDownloadExpireSeconds());
    }
}
