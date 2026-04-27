package t.uni.server.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信认证配置属性。
 * <p>
 * 从 {@code wx.auth} 配置段读取微信登录标识策略。
 * 本配置只保存认证模式，不保存 appId、secret 等敏感配置。
 * </p>
 *
 * @author lzx
 * @since 2026-01-08
 */
@Data
@Component
@ConfigurationProperties(prefix = "wx.auth")
public class WxAuthProperties {

    /**
     * 登录标识类型。
     * <p>
     * 决定业务用户表使用小程序 openId 还是 unionId 作为微信侧唯一标识。
     * 未打通开放平台 unionId 前应使用 {@code MA_OPEN_ID}。
     * </p>
     *
     * 可选值：
     * - MA_OPEN_ID: 使用 maOpenId 作为唯一标识（场景A：未申请unionId）
     * - UNION_ID: 使用 unionId 作为唯一标识（场景B：已申请unionId）
     */
    private String loginIdentifier = "MA_OPEN_ID";

    /**
     * 校验登录标识配置是否在支持范围内。
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
