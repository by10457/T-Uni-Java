package t.uni.server.payment.config;

import cn.hutool.core.util.StrUtil;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 微信支付装配。
 *
 * <p>只把配置映射到第三方 SDK，不在装配阶段执行业务校验；运行期完整性由 {@code WechatPayClient.assertReady()} 兜底。</p>
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(WechatPayProperties.class)
public class WechatPayConfiguration {

    /**
     * 构建微信支付 SDK 服务。
     *
     * @param properties 微信支付配置
     * @return SDK 服务实例
     */
    @Bean
    public WxPayService wxPayService(WechatPayProperties properties) {
        var config = new WxPayConfig();
        config.setAppId(properties.getAppId());
        config.setMchId(properties.getMchId());
        config.setCertSerialNo(properties.getMchSerialNo());
        config.setApiV3Key(properties.getApiV3Key());
        config.setNotifyUrl(properties.buildPayNotifyUrl());

        if (StrUtil.isNotBlank(properties.getPrivateKey())) {
            config.setPrivateKeyString(properties.getPrivateKey());
        }
        if (StrUtil.isNotBlank(properties.getPrivateKeyPath())) {
            config.setPrivateKeyPath(properties.getPrivateKeyPath());
        }

        var service = new WxPayServiceImpl();
        service.setConfig(config);
        return service;
    }
}
