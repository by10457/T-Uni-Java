package t.uni.server.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import t.uni.server.domain.entity.CoreUserDefaultAvatar;

/**
 * 默认头像池 Mapper 接口
 */
@Mapper
public interface CoreUserDefaultAvatarMapper extends BaseMapper<CoreUserDefaultAvatar> {
}
