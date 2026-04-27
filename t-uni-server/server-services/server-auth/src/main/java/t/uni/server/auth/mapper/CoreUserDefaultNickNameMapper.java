package t.uni.server.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import t.uni.server.domain.entity.CoreUserDefaultNickName;

/**
 * 默认昵称池 Mapper。
 * <p>
 * 访问 core_user_default_nick_name，供新用户自动分配昵称时读取启用项。
 * </p>
 */
@Mapper
public interface CoreUserDefaultNickNameMapper extends BaseMapper<CoreUserDefaultNickName> {
}
