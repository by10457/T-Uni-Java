package t.uni.server.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import t.uni.server.domain.entity.CoreUserDefaultNickName;

/**
 * 默认昵称池 Mapper 接口
 */
@Mapper
public interface CoreUserDefaultNickNameMapper extends BaseMapper<CoreUserDefaultNickName> {
}
