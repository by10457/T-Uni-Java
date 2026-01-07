package t.uni.server.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import t.uni.server.domain.entity.ServerUser;

/**
 * 小程序用户Mapper
 */
@Mapper
public interface ServerUserMapper extends BaseMapper<ServerUser> {
}
