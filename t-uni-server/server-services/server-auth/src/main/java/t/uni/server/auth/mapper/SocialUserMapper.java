package t.uni.server.auth.mapper;

import org.apache.ibatis.annotations.Mapper;
import t.uni.server.domain.auth.IBusinessUserMapper;
import t.uni.server.domain.entity.SocialUser;

/**
 * 社交用户 Mapper 接口
 */
@Mapper
public interface SocialUserMapper extends IBusinessUserMapper<SocialUser> {
}
