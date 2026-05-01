package t.uni.api.controller.system;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import t.uni.common.core.result.Result;
import t.uni.core.annotation.PermissionTag;
import t.uni.domain.system.vo.monitor.ServerMonitorVo;
import t.uni.system.service.MonitorService;

@Tag(name = "系统监控", description = "系统监控相关接口")
@PermissionTag(permission = "monitor:*")
@RestController
@RequestMapping("/api/monitor")
public class MonitorController {

    @Resource
    private MonitorService monitorService;

    @Operation(summary = "服务监控", description = "查询当前管理服务资源使用情况")
    @PermissionTag(permission = "monitor:server:query")
    @GetMapping("/server")
    public Result<ServerMonitorVo> getServerMonitor() {
        return Result.success(monitorService.getServerMonitor());
    }
}
