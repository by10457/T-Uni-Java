package t.uni.server.domain.auth;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 业务用户通用Mapper接口
 * 所有业务场景的Mapper都应继承此接口
 * 支持不同业务场景使用不同的Mapper实现（如 SocialUserMapper、EduUserMapper、HygieneUserMapper）
 *
 * @param <T> 业务用户实体类型，必须实现 IBusinessUser 接口
 * @author lzx
 * @since 2026-01-08
 */
public interface IBusinessUserMapper<T extends IBusinessUser> extends BaseMapper<T> {
}
