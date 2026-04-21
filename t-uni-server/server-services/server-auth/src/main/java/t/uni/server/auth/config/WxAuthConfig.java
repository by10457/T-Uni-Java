package t.uni.server.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import t.uni.server.auth.mapper.BizUserMapper;
import t.uni.server.domain.auth.IBusinessUser;
import t.uni.server.domain.auth.IBusinessUserMapper;

/**
 * 微信认证配置类。
 * 业务方 fork 模板后，只需替换这里注入的 Mapper，即可切换自己的业务用户表实现。
 *
 * @author lzx
 * @since 2026-01-08
 */
@Configuration
public class WxAuthConfig {

    /**
     * 默认注入模板内置的 BizUserMapper。
     */
    @Bean
    public IBusinessUserMapper<? extends IBusinessUser> businessUserMapper(
            BizUserMapper bizUserMapper) {
        return bizUserMapper;
    }
}
