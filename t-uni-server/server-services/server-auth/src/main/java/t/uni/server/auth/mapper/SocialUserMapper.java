package t.uni.server.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import t.uni.server.domain.entity.SocialUser;

/**
 * 社交用户 Mapper 接口
 */
@Mapper
public interface SocialUserMapper extends BaseMapper<SocialUser> {
}
