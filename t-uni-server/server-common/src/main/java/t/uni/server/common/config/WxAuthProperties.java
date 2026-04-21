package t.uni.server.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信认证配置属性
 * 从 application-wx.yml 中读取配置
 *
 * @author lzx
 * @since 2026-01-08
 */
@Data
@Component
@ConfigurationProperties(prefix = "wx.auth")
public class WxAuthProperties {

    /**
     * 登录标识类型
     * 可选值：
     * - MA_OPEN_ID: 使用 maOpenId 作为唯一标识（场景A：未申请unionId）
     * - UNION_ID: 使用 unionId 作为唯一标识（场景B：已申请unionId）
     */
    private String loginIdentifier = "MA_OPEN_ID";

    /**
     * 验证配置的有效性
     *
     * @throws IllegalArgumentException 如果配置无效
     */
    public void validate() {
        if (!"MA_OPEN_ID".equals(loginIdentifier) && !"UNION_ID".equals(loginIdentifier)) {
            throw new IllegalArgumentException(
                    "Invalid loginIdentifier: " + loginIdentifier +
                            ". Must be 'MA_OPEN_ID' or 'UNION_ID'");
        }
    }
}
