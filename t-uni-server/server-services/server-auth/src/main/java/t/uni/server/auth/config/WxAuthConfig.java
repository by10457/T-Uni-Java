package t.uni.server.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import t.uni.server.auth.mapper.SocialUserMapper;
import t.uni.server.domain.auth.IBusinessUser;
import t.uni.server.domain.auth.IBusinessUserMapper;

/**
 * 微信认证配置类
 * 通过 Spring 配置类手动指定使用哪个 Mapper 实现
 * <p>
 * 复制项目时，只需修改此配置类即可切换到其他 Mapper（如 EduUserMapper、HygieneUserMapper）
 *
 * @author Claude
 * @since 2026-01-08
 */
@Configuration
@RequiredArgsConstructor
public class WxAuthConfig {

    /**
     * 注入业务用户 Mapper
     * 将 SocialUserMapper 注入为 IBusinessUserMapper Bean
     * <p>
     * 如果需要切换到其他业务场景（如教育、卫生），只需：
     * 1. 创建新的实体类（如 EduUser）并实现 IBusinessUser 接口
     * 2. 创建新的 Mapper（如 EduUserMapper）并继承 IBusinessUserMapper
     * 3. 修改此方法的参数和返回值，注入新的 Mapper
     *
     * @param socialUserMapper 社交用户 Mapper
     * @return 业务用户 Mapper
     */
    @Bean
    public IBusinessUserMapper<? extends IBusinessUser> businessUserMapper(
            SocialUserMapper socialUserMapper) {
        return socialUserMapper;
    }
}
