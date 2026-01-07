package t.uni.schedule.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import t.uni.common.core.result.PageResult;
import t.uni.domain.schedule.dto.SchedulersGroupDto;
import t.uni.domain.schedule.entity.SchedulersGroup;
import t.uni.domain.schedule.vo.SchedulersGroupVo;

import java.util.List;

/**
 * <p>
 * 任务调度分组 服务类
 * </p>
 *
 * @since 2024-10-15 20:26:32
 */
public interface SchedulersGroupService extends IService<SchedulersGroup> {

    /**
     * * 获取任务调度分组列表
     *
     * @return 任务调度分组返回列表
     */
    PageResult<SchedulersGroupVo> getSchedulersGroupPage(Page<SchedulersGroup> pageParams, SchedulersGroupDto dto);

    /**
     * * 添加任务调度分组
     *
     * @param dto 添加表单
     */
    void createSchedulersGroup(SchedulersGroupDto dto);

    /**
     * * 更新任务调度分组
     *
     * @param dto 更新表单
     */
    void updateSchedulersGroup(SchedulersGroupDto dto);

    /**
     * * 删除|批量删除任务调度分组类型
     *
     * @param ids 删除id列表
     */
    void deleteSchedulersGroup(List<Long> ids);

    /**
     * * 获取所有任务调度分组
     *
     * @return 获取所有任务分组
     */
    List<SchedulersGroupVo> getSchedulersGroupList();
}
