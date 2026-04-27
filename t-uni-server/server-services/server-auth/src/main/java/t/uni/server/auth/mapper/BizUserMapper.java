package t.uni.server.auth.mapper;

import org.apache.ibatis.annotations.Mapper;
import t.uni.server.domain.auth.IBusinessUserMapper;
import t.uni.server.domain.entity.BizUser;

/**
 * 模板默认业务用户 Mapper。
 * <p>
 * 访问 biz_user 表，承载微信小程序 openId/unionId 与核心用户 ID 的业务侧映射。
 * </p>
 */
@Mapper
public interface BizUserMapper extends IBusinessUserMapper<BizUser> {
}
