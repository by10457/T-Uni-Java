package t.uni.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import t.uni.domain.system.dto.MessageTypeDto;
import t.uni.domain.system.entity.MessageType;
import t.uni.domain.system.vo.MessageTypeVo;

/**
 * <p>
 * 系统消息类型 Mapper 接口
 * </p>
 *
 * @since 2024-10-30 13:19:33
 */
@Mapper
public interface MessageTypeMapper extends BaseMapper<MessageType> {

    /**
     * * 分页查询系统消息类型内容
     *
     * @param pageParams 系统消息类型分页参数
     * @param dto        系统消息类型查询表单
     * @return 系统消息类型分页结果
     */
    IPage<MessageTypeVo> selectListByPage(@Param("page") Page<MessageType> pageParams, @Param("dto") MessageTypeDto dto);

}
