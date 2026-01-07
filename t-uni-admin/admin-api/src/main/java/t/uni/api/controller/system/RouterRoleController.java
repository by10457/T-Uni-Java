package t.uni.api.controller.system;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import t.uni.common.core.result.Result;
import t.uni.core.annotation.PermissionTag;
import t.uni.system.service.RouterRoleService;

import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @since 2024-09-26
 */
@Tag(name = "路由菜单和角色", description = "路由和角色相关接口")
@PermissionTag(permission = "routerRole:*")
@RestController
@RequestMapping("api/router-role")
public class RouterRoleController {

    @Resource
    private RouterRoleService routerRoleService;

    @Operation(summary = "根据菜单id获取所有角色", description = "根据菜单id获取所有角色")
    @GetMapping("private/roles/{routerId}")
    public Result<List<String>> getRoleListByRouterId(@PathVariable Long routerId) {
        List<String> roleListByRouterId = routerRoleService.getRoleListByRouterId(routerId);
        return Result.success(roleListByRouterId);
    }

    @Operation(summary = "清除选中菜单所有角色", description = "清除选中菜单所有角色")
    @PermissionTag(permission = "routerRole:delete")
    @DeleteMapping("/batch")
    public Result<String> clearRouterRole(@RequestBody List<Long> routerIds) {
        routerRoleService.clearRouterRole(routerIds);
        return Result.success();
    }
}
