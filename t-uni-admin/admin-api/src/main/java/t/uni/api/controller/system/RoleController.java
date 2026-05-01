package t.uni.api.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import t.uni.common.core.result.PageResult;
import t.uni.common.core.result.Result;
import t.uni.common.core.result.ResultCodeEnum;
import t.uni.core.annotation.PermissionTag;
import t.uni.domain.common.ValidationGroups;
import t.uni.domain.system.dto.RoleDto;
import t.uni.domain.system.entity.Role;
import t.uni.domain.system.vo.RoleVo;
import t.uni.system.service.RoleService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 角色 前端控制器
 * </p>
 *
 * @since 2024-10-03 14:26:24
 */
@Tag(name = "角色", description = "角色相关接口")
@PermissionTag(permission = "role:*")
@RestController
@RequestMapping
public class RoleController {

    @Resource
    private RoleService roleService;

    @Operation(summary = "分页查询角色", description = "分页查询角色")
    @PermissionTag(permission = "role:query")
    @GetMapping("api/role/{page}/{limit}")
    public Result<PageResult<RoleVo>> getRolePage(
            @PathVariable @Parameter(name = "page", description = "当前页", required = true) Integer page,
            @PathVariable @Parameter(name = "limit", description = "每页记录数", required = true) Integer limit,
            RoleDto dto) {
        Page<Role> pageParams = new Page<>(page, limit);
        PageResult<RoleVo> pageResult = roleService.getRolePage(pageParams, dto);
        return Result.success(pageResult);
    }

    @Operation(summary = "管理端角色列表", description = "分页查询 Vben 管理端角色")
    @GetMapping("/api/system/role/list")
    public Result<Map<String, Object>> adminPortalRoleList(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam Map<String, Object> query) {
        return Result.success(roleService.adminPortalRoleList(page, pageSize, query));
    }

    @Operation(summary = "管理端创建角色", description = "创建 Vben 管理端角色并绑定菜单权限")
    @PostMapping("/api/system/role")
    public Result<Object> createAdminPortalRole(@RequestBody Map<String, Object> request) {
        roleService.createAdminPortalRole(request);
        return Result.success(ResultCodeEnum.CREATE_SUCCESS);
    }

    @Operation(summary = "管理端更新角色", description = "更新 Vben 管理端角色与菜单权限")
    @PutMapping("/api/system/role/{id}")
    public Result<Object> updateAdminPortalRole(@PathVariable String id, @RequestBody Map<String, Object> request) {
        roleService.updateAdminPortalRole(id, request);
        return Result.success(ResultCodeEnum.UPDATE_SUCCESS);
    }

    @Operation(summary = "管理端删除角色", description = "删除 Vben 管理端角色")
    @DeleteMapping("/api/system/role/{id}")
    public Result<Object> deleteAdminPortalRole(@PathVariable String id) {
        roleService.deleteAdminPortalRole(id);
        return Result.success(ResultCodeEnum.DELETE_SUCCESS);
    }

    @Operation(summary = "添加角色", description = "添加角色")
    @PermissionTag(permission = "role:add")
    @PostMapping("api/role")
    public Result<Object> addRole(@Validated(ValidationGroups.Add.class) @RequestBody RoleDto dto) {
        roleService.addRole(dto);
        return Result.success(ResultCodeEnum.CREATE_SUCCESS);
    }

    @Operation(summary = "更新角色", description = "更新角色")
    @PermissionTag(permission = "role:update")
    @PutMapping("api/role")
    public Result<Object> updateRole(@Validated(ValidationGroups.Update.class) @RequestBody RoleDto dto) {
        roleService.updateRole(dto);
        return Result.success(ResultCodeEnum.UPDATE_SUCCESS);
    }

    @Operation(summary = "删除角色", description = "删除角色")
    @PermissionTag(permission = "role:delete")
    @DeleteMapping("api/role")
    public Result<Object> deleteRole(@RequestBody List<Long> ids) {
        roleService.deleteRole(ids);
        return Result.success(ResultCodeEnum.DELETE_SUCCESS);
    }

    @Operation(summary = "获取所有角色", description = "获取所有角色")
    @GetMapping("api/role/private/roleList")
    public Result<List<RoleVo>> roleList() {
        List<RoleVo> roleVoList = roleService.roleList();
        return Result.success(roleVoList);
    }

    @Operation(summary = "导出角色列表", description = "使用Excel导出导出角色列表")
    @PermissionTag(permission = "role:update")
    @GetMapping("api/role/file/export")
    public ResponseEntity<byte[]> exportByExcel() {
        return roleService.exportByExcel();
    }

    @Operation(summary = "更新角色列表", description = "使用Excel更新角色列表")
    @PermissionTag(permission = "role:update")
    @PutMapping("api/role/file/import")
    public Result<String> updateRoleByFile(MultipartFile file) {
        roleService.updateRoleByFile(file);
        return Result.success(ResultCodeEnum.UPDATE_SUCCESS);
    }
}
