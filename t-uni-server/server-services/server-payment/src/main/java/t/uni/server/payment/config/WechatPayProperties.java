package t.uni.server.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 微信支付配置。
 *
 * <p>配置值来自 {@code payment.wechat} 前缀。密钥字段只承载配置，不应在日志或回调表中输出。</p>
 */
@Data
@ConfigurationProperties(prefix = "payment.wechat")
public class WechatPayProperties {

    /**
     * 是否启用真实微信支付调用。
     */
    private boolean enabled = false;

    /**
     * 小程序 AppID。
     */
    private String appId;

    /**
     * 商户号。
     */
    private String mchId;

    /**
     * 商户 API 证书序列号。
     */
    private String mchSerialNo;

    /**
     * API v3 密钥。
     */
    private String apiV3Key;

    /**
     * 商户私钥文件路径，和 privateKey 二选一。
     */
    private String privateKeyPath;

    /**
     * 商户私钥内容，和 privateKeyPath 二选一。
     */
    private String privateKey;

    /**
     * 外网可访问的回调基础地址，如 https://api.example.com。
     */
    private String notifyBaseUrl;

    /**
     * 支付回调路径。
     */
    private String payNotifyPath = "/payment/notify/wechat/pay";

    /**
     * 退款回调路径。
     */
    private String refundNotifyPath = "/payment/notify/wechat/refund";

    /**
     * 默认锁单有效期。
     */
    private long orderExpireMinutes = 15;

    /**
     * 自动关单扫描间隔。
     */
    private Duration closeScanInterval = Duration.ofMinutes(1);

    /**
     * 拼接支付回调完整地址。
     *
     * @return 微信支付回调 URL
     */
    public String buildPayNotifyUrl() {
        return joinUrl(notifyBaseUrl, payNotifyPath);
    }

    /**
     * 拼接退款回调完整地址。
     *
     * @return 微信退款回调 URL
     */
    public String buildRefundNotifyUrl() {
        return joinUrl(notifyBaseUrl, refundNotifyPath);
    }

    private String joinUrl(String baseUrl, String path) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return path;
        }
        var cleanBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        var cleanPath = path == null || path.isBlank() ? "" : path;
        if (!cleanPath.startsWith("/")) {
            cleanPath = "/" + cleanPath;
        }
        return cleanBase + cleanPath;
    }
}
