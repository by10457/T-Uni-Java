package t.uni.api.controller.schedule;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import t.uni.common.core.result.PageResult;
import t.uni.common.core.result.Result;
import t.uni.common.core.result.ResultCodeEnum;
import t.uni.core.annotation.PermissionTag;
import t.uni.domain.schedule.dto.ScheduleExecuteLogDto;
import t.uni.domain.schedule.entity.ScheduleExecuteLog;
import t.uni.domain.schedule.vo.ScheduleExecuteLogVo;
import t.uni.schedule.service.ScheduleExecuteLogService;

import java.util.List;

/**
 * <p>
 * 调度任务执行日志表 前端控制器
 * </p>
 *
 * @since 2024-10-18 12:56:39
 */
@Tag(name = "任务调度执行日志", description = "调度任务执行日志相关接口")
@PermissionTag(permission = "scheduleExecuteLog:*")
@RestController
@RequestMapping("api/schedule-execute-log")
public class ScheduleExecuteLogController {

    @Resource
    private ScheduleExecuteLogService scheduleExecuteLogService;

    @Operation(summary = "分页查询任务调度执行日志", description = "分页查询调度任务执行日志")
    @PermissionTag(permission = "scheduleExecuteLog:query")
    @GetMapping("{page}/{limit}")
    public Result<PageResult<ScheduleExecuteLogVo>> getScheduleExecuteLogPage(
            @PathVariable @Parameter(name = "page", description = "当前页", required = true) Integer page,
            @PathVariable @Parameter(name = "limit", description = "每页记录数", required = true) Integer limit,
            ScheduleExecuteLogDto dto) {
        Page<ScheduleExecuteLog> pageParams = new Page<>(page, limit);
        PageResult<ScheduleExecuteLogVo> pageResult = scheduleExecuteLogService.getScheduleExecuteLogPage(pageParams, dto);
        return Result.success(pageResult);
    }

    @Operation(summary = "删除任务调度执行日志", description = "删除调度任务执行日志")
    @PermissionTag(permission = "scheduleExecuteLog:delete")
    @DeleteMapping()
    public Result<String> deleteScheduleExecuteLog(@RequestBody List<Long> ids) {
        scheduleExecuteLogService.deleteScheduleExecuteLog(ids);
        return Result.success(ResultCodeEnum.DELETE_SUCCESS);
    }
}
