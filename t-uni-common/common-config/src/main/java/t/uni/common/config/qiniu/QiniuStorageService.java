package t.uni.common.config.qiniu;

import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

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
     * 生成自定义文件 Key
     * <p>
     * 格式: upload/{userId}/{yyyyMMdd}/{uuid}.{ext}
     *
     * @param userId 用户 ID
     * @param ext    文件扩展名（如 jpg、png）
     * @return 文件 Key
     */
    public String generateFileKey(Long userId, String ext) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return String.format("upload/%d/%s/%s.%s", userId, dateStr, uuid, ext);
    }

    /**
     * 生成带回调的上传凭证
     *
     * @param fileKey 文件 Key
     * @return 上传凭证 Token
     */
    public String generateUploadToken(String fileKey) {
        StringMap putPolicy = new StringMap();
        putPolicy.put("callbackUrl", properties.getCallbackUrl());
        putPolicy.put("callbackBody",
                "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"bucket\":\"$(bucket)\",\"fsize\":$(fsize),\"mimeType\":\"$(mimeType)\",\"width\":$(imageInfo.width),\"height\":$(imageInfo.height)}");
        putPolicy.put("callbackBodyType", "application/json");

        String token = auth.uploadToken(properties.getBucket(), fileKey, properties.getTokenExpireSeconds(), putPolicy);

        log.debug("生成上传凭证，fileKey: {}", fileKey);
        return token;
    }

    /**
     * 验证七牛云回调签名
     *
     * @param authHeader   请求头 Authorization 字段
     * @param callbackBody 请求体原始字节
     * @return 是否合法
     */
    public boolean verifyCallback(String authHeader, byte[] callbackBody) {
        // application/json 时，签名不包括请求内容
        boolean valid = auth.isValidCallback(
                authHeader,
                properties.getCallbackUrl(),
                callbackBody,
                "application/json");

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
