package t.uni.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import t.uni.common.core.result.PageResult;
import t.uni.domain.system.dto.MessageTypeDto;
import t.uni.domain.system.entity.MessageType;
import t.uni.domain.system.vo.MessageTypeVo;

import java.util.List;

/**
 * <p>
 * 系统消息类型 服务类
 * </p>
 *
 * @since 2024-10-30 13:19:33
 */
public interface MessageTypeService extends IService<MessageType> {

    /**
     * * 获取系统消息类型列表
     *
     * @return 系统消息类型返回列表
     */
    PageResult<MessageTypeVo> getMessageTypePage(Page<MessageType> pageParams, MessageTypeDto dto);

    /**
     * * 添加系统消息类型
     *
     * @param dto 添加表单
     */
    void createMessageType(MessageTypeDto dto);

    /**
     * * 更新系统消息类型
     *
     * @param dto 更新表单
     */
    void updateMessageType(MessageTypeDto dto);

    /**
     * * 删除|批量删除系统消息类型类型
     *
     * @param ids 删除id列表
     */
    void deleteMessageType(List<Long> ids);

    /**
     * 获取所有消息类型
     *
     * @return 系统消息类型列表
     */
    List<MessageTypeVo> getMessageList();
}
