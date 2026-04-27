package t.uni.server.api.config;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 微信小程序配置。
 * <p>
 * 从应用配置读取 appid、secret 等参数，初始化微信小程序 SDK 服务。
 * secret 仅写入 SDK 配置，不在日志中输出。
 * </p>
 */
@Slf4j
@Configuration
public class WxMaConfiguration {

    @Value("${wx.miniapp.appid}")
    private String appid;

    @Value("${wx.miniapp.secret}")
    private String secret;

    @Value("${wx.miniapp.token:}")
    private String token;

    @Value("${wx.miniapp.aesKey:}")
    private String aesKey;

    @Value("${wx.miniapp.msgDataFormat:JSON}")
    private String msgDataFormat;

    /**
     * 创建微信小程序 SDK 服务。
     *
     * @return 已设置小程序配置的 WxMaService
     */
    @Bean
    public WxMaService wxMaService() {
        WxMaDefaultConfigImpl config = new WxMaDefaultConfigImpl();
        config.setAppid(appid);
        config.setSecret(secret);
        config.setToken(token);
        config.setAesKey(aesKey);
        config.setMsgDataFormat(msgDataFormat);

        WxMaService service = new WxMaServiceImpl();
        service.setWxMaConfig(config);

        log.info("微信小程序服务初始化完成，appid: {}", appid);
        return service;
    }
}
