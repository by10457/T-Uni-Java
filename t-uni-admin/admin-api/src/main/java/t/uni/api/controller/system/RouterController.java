package t.uni.api.controller.system;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import t.uni.common.core.result.Result;
import t.uni.common.core.result.ResultCodeEnum;
import t.uni.core.annotation.PermissionTag;
import t.uni.domain.common.ValidationGroups;
import t.uni.domain.system.dto.RouterDto;
import t.uni.domain.system.vo.router.RouterManageVo;
import t.uni.domain.system.vo.router.WebUserRouterVo;
import t.uni.system.service.RouterService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 系统菜单 前端控制器
 * </p>
 *
 * @since 2024-09-26
 */
@Tag(name = "路由菜单", description = "系统路由相关接口")
@PermissionTag(permission = "router:*")
@RestController
@RequestMapping
public class RouterController {

    @Resource
    private RouterService routerService;

    @Operation(summary = "获取用户菜单", description = "获取用户菜单")
    @PermissionTag(permission = "router:query")
    @GetMapping("api/router/private/router-async")
    public Result<List<WebUserRouterVo>> routerAsync() {
        List<WebUserRouterVo> voList = routerService.routerAsync();
        return Result.success(voList);
    }

    @Operation(summary = "管理端所有菜单", description = "获取 Vben 管理端当前用户可访问的动态菜单")
    @GetMapping("/api/menu/all")
    public Result<List<Map<String, Object>>> adminPortalAllMenus() {
        return Result.success(routerService.adminPortalAllMenus());
    }

    @Operation(summary = "管理端菜单列表", description = "查询 Vben 管理端菜单管理树")
    @GetMapping("/api/system/menu/list")
    public Result<List<Map<String, Object>>> adminPortalMenuList() {
        return Result.success(routerService.adminPortalMenuList());
    }

    @Operation(summary = "管理端菜单名称是否存在", description = "校验菜单名称是否已存在")
    @GetMapping("/api/system/menu/name-exists")
    public Result<Boolean> adminPortalMenuNameExists(@RequestParam String name, @RequestParam(required = false) String id) {
        return Result.success(routerService.adminPortalMenuNameExists(name, id));
    }

    @Operation(summary = "管理端菜单路径是否存在", description = "校验菜单路径是否已存在")
    @GetMapping("/api/system/menu/path-exists")
    public Result<Boolean> adminPortalMenuPathExists(@RequestParam String path, @RequestParam(required = false) String id) {
        return Result.success(routerService.adminPortalMenuPathExists(path, id));
    }

    @Operation(summary = "管理端创建菜单", description = "创建 Vben 管理端菜单或按钮")
    @PostMapping("/api/system/menu")
    public Result<String> createAdminPortalMenu(@RequestBody Map<String, Object> request) {
        routerService.createAdminPortalMenu(request);
        return Result.success(ResultCodeEnum.CREATE_SUCCESS);
    }

    @Operation(summary = "管理端更新菜单", description = "更新 Vben 管理端菜单或按钮")
    @PutMapping("/api/system/menu/{id}")
    public Result<String> updateAdminPortalMenu(@PathVariable String id, @RequestBody Map<String, Object> request) {
        routerService.updateAdminPortalMenu(id, request);
        return Result.success(ResultCodeEnum.UPDATE_SUCCESS);
    }

    @Operation(summary = "管理端删除菜单", description = "删除 Vben 管理端菜单或按钮")
    @DeleteMapping("/api/system/menu/{id}")
    public Result<String> deleteAdminPortalMenu(@PathVariable String id) {
        routerService.deleteAdminPortalMenu(id);
        return Result.success(ResultCodeEnum.DELETE_SUCCESS);
    }

    @Operation(summary = "查询管理路由菜单", description = "查询管理菜单列表")
    @PermissionTag(permission = "router:query")
    @GetMapping("api/router/routers")
    public Result<List<RouterManageVo>> routerList() {
        List<RouterManageVo> voPageResult = routerService.routerList();
        return Result.success(voPageResult);
    }

    @Operation(summary = "添加路由菜单", description = "添加路由菜单")
    @PermissionTag(permission = "router:add")
    @PostMapping("api/router")
    public Result<String> createRouter(@Validated({ValidationGroups.Add.class}) @RequestBody RouterDto dto) {
        routerService.createRouter(dto);
        return Result.success(ResultCodeEnum.CREATE_SUCCESS);
    }

    @Operation(summary = "更新路由菜单", description = "更新路由菜单")
    @PermissionTag(permission = "router:update")
    @PutMapping("api/router")
    public Result<String> updateRouter(@Validated({ValidationGroups.Update.class}) @RequestBody RouterDto dto) {
        routerService.updateRouter(dto);
        return Result.success(ResultCodeEnum.UPDATE_SUCCESS);
    }

    @Operation(summary = "删除路由菜单", description = "删除路由菜单")
    @PermissionTag(permission = "router:delete")
    @DeleteMapping("api/router")
    public Result<String> deletedRouterByIds(@RequestBody List<Long> ids) {
        routerService.deletedRouterByIds(ids);
        return Result.success(ResultCodeEnum.DELETE_SUCCESS);
    }
}
