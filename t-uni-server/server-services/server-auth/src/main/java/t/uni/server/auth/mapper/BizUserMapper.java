package t.uni.server.auth.mapper;

import org.apache.ibatis.annotations.Mapper;
import t.uni.server.domain.auth.IBusinessUserMapper;
import t.uni.server.domain.entity.BizUser;

/**
 * 模板默认业务用户 Mapper。
 */
@Mapper
public interface BizUserMapper extends IBusinessUserMapper<BizUser> {
}
