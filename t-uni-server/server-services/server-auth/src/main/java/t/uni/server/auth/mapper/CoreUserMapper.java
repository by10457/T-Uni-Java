package t.uni.server.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import t.uni.server.domain.entity.CoreUser;

/**
 * 核心用户 Mapper 接口
 */
@Mapper
public interface CoreUserMapper extends BaseMapper<CoreUser> {
}
