package t.uni.server.api.config;

import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 微信支付配置
 */
@Slf4j
@Configuration
public class WxPayConfiguration {

    @Value("${wx.pay.appId}")
    private String appId;

    @Value("${wx.pay.mchId}")
    private String mchId;

    @Value("${wx.pay.mchKey}")
    private String mchKey;

    @Value("${wx.pay.apiV3Key}")
    private String apiV3Key;

    @Value("${wx.pay.certPath:}")
    private String certPath;

    @Value("${wx.pay.notifyUrl}")
    private String notifyUrl;

    @Value("${wx.pay.refundNotifyUrl:}")
    private String refundNotifyUrl;

    @Bean
    public WxPayService wxPayService() {
        WxPayConfig config = new WxPayConfig();
        config.setAppId(appId);
        config.setMchId(mchId);
        config.setMchKey(mchKey);
        config.setApiV3Key(apiV3Key);
        config.setKeyPath(certPath);
        config.setNotifyUrl(notifyUrl);

        if (refundNotifyUrl != null && !refundNotifyUrl.isEmpty()) {
            config.setPayScoreNotifyUrl(refundNotifyUrl);
        }

        WxPayService service = new WxPayServiceImpl();
        service.setConfig(config);

        log.info("微信支付服务初始化完成，商户号: {}", mchId);
        return service;
    }
}
