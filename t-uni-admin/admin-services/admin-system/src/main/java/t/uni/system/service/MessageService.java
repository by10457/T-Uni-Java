package t.uni.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import t.uni.common.core.result.PageResult;
import t.uni.domain.system.dto.MessageDto;
import t.uni.domain.system.entity.Message;
import t.uni.domain.system.vo.MessageDetailVo;
import t.uni.domain.system.vo.MessageReceivedWithUserVo;
import t.uni.domain.system.vo.MessageVo;

import java.util.List;

/**
 * <p>
 * 系统消息 服务类
 * </p>
 *
 * @since 2024-10-30 15:19:56
 */
public interface MessageService extends IService<Message> {

    /**
     * 根据消息id获取接收人信息
     *
     * @param messageId 消息id
     * @return 消息接收人用户名等信息
     */
    List<MessageReceivedWithUserVo> getReceivedUserinfoByMessageId(Long messageId);

    /**
     * 根据消息id查询消息详情
     *
     * @param id 消息id
     * @return 消息详情
     */
    MessageDetailVo getMessageDetailById(Long id);

    /**
     * * 添加系统消息
     *
     * @param dto 添加表单
     */
    void createMessage(MessageDto dto);

    /**
     * * 更新系统消息
     *
     * @param dto 更新表单
     */
    void updateMessage(MessageDto dto);

    /**
     * * 删除|批量删除系统消息类型
     *
     * @param ids 删除id列表
     */
    void deleteMessage(List<Long> ids);

    /**
     * 分页查询发送消息
     *
     * @param pageParams 分页参数
     * @param dto        查询表单
     * @return 系统消息返回列表
     */
    PageResult<MessageVo> getMessagePage(Page<Message> pageParams, MessageDto dto);
}
