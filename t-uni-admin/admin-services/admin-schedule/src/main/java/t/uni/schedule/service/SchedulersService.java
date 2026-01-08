package t.uni.schedule.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import t.uni.common.core.result.PageResult;
import t.uni.domain.schedule.dto.SchedulersDto;
import t.uni.domain.schedule.entity.Schedulers;
import t.uni.domain.schedule.vo.SchedulersVo;

/**
 * <p>
 * Schedulers视图 服务类
 * </p>
 *
 * @since 2024-10-15 16:35:10
 */
public interface SchedulersService extends IService<Schedulers> {

    /**
     * * 获取Schedulers视图列表
     *
     * @return Schedulers视图返回列表
     */
    PageResult<SchedulersVo> getSchedulersPage(Page<Schedulers> pageParams, SchedulersDto dto);

    /**
     * * 添加Schedulers视图
     *
     * @param dto 添加表单
     */
    void createSchedulers(SchedulersDto dto);

    /**
     * * 暂停Schedulers任务
     *
     * @param dto Schedulers公共操作表单
     */
    void pauseScheduler(SchedulersDto dto);

    /**
     * * 恢复Schedulers任务
     *
     * @param dto Schedulers公共操作表单
     */
    void resumeScheduler(SchedulersDto dto);

    /**
     * * 移出Schedulers任务
     *
     * @param dto Schedulers公共操作表单
     */
    void deleteSchedulers(SchedulersDto dto);

    /**
     * 更新任务
     *
     * @param dto 更新任务表单
     */
    void updateSchedulers(SchedulersDto dto);
}
