package t.uni.server.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import t.uni.server.auth.mapper.BizUserMapper;
import t.uni.server.domain.auth.IBusinessUser;
import t.uni.server.domain.auth.IBusinessUserMapper;

/**
 * 微信认证配置类。
 * <p>
 * 定义认证模块依赖的业务用户 Mapper。默认使用模板内置 biz_user，
 * 业务方 fork 模板后只替换该 Bean 即可接入自己的业务用户表。
 * </p>
 *
 * @author lzx
 * @since 2026-01-08
 */
@Configuration
public class WxAuthConfig {

    /**
     * 默认注入模板内置的 BizUserMapper。
     *
     * @param bizUserMapper 模板默认业务用户 Mapper
     * @return 认证服务使用的业务用户 Mapper
     */
    @Bean
    public IBusinessUserMapper<? extends IBusinessUser> businessUserMapper(
            BizUserMapper bizUserMapper) {
        return bizUserMapper;
    }
}
