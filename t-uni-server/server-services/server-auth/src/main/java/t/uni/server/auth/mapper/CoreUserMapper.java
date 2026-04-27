package t.uni.server.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import t.uni.server.domain.entity.CoreUser;

/**
 * 核心用户 Mapper。
 * <p>
 * 访问 core_user 表，保存平台通用用户资料、手机号授权状态和登录活跃时间。
 * </p>
 */
@Mapper
public interface CoreUserMapper extends BaseMapper<CoreUser> {
}
