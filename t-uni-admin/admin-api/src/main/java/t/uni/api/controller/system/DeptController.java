package t.uni.api.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import t.uni.common.core.result.PageResult;
import t.uni.common.core.result.Result;
import t.uni.common.core.result.ResultCodeEnum;
import t.uni.core.annotation.PermissionTag;
import t.uni.domain.common.ValidationGroups;
import t.uni.domain.system.dto.DeptDto;
import t.uni.domain.system.entity.Dept;
import t.uni.domain.system.vo.DeptVo;
import t.uni.system.service.DeptService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 部门 前端控制器
 * </p>
 *
 * @since 2024-10-04 10:39:08
 */
@Tag(name = "部门", description = "部门相关接口")
@PermissionTag(permission = "dept:*")
@RestController
@RequestMapping
public class DeptController {

    @Resource
    private DeptService deptService;

    @Operation(summary = "分页查询部门", description = "分页查询部门")
    @PermissionTag(permission = "dept:query")
    @GetMapping("/api/dept/{page}/{limit}")
    public Result<PageResult<DeptVo>> getDeptPage(
            @PathVariable @Parameter(name = "page", description = "当前页", required = true) Integer page,
            @PathVariable @Parameter(name = "limit", description = "每页记录数", required = true) Integer limit,
            DeptDto dto) {
        Page<Dept> pageParams = new Page<>(page, limit);
        PageResult<DeptVo> pageResult = deptService.getDeptPage(pageParams, dto);
        return Result.success(pageResult);
    }

    @Operation(summary = "管理端部门列表", description = "查询 Vben 管理端部门树")
    @GetMapping("/api/system/dept/list")
    public Result<List<Map<String, Object>>> adminPortalDeptList() {
        return Result.success(deptService.adminPortalDeptList());
    }

    @Operation(summary = "管理端创建部门", description = "创建 Vben 管理端部门")
    @PostMapping("/api/system/dept")
    public Result<String> createAdminPortalDept(@RequestBody Map<String, Object> request) {
        deptService.createAdminPortalDept(request);
        return Result.success(ResultCodeEnum.CREATE_SUCCESS);
    }

    @Operation(summary = "管理端更新部门", description = "更新 Vben 管理端部门")
    @PutMapping("/api/system/dept/{id}")
    public Result<String> updateAdminPortalDept(@PathVariable String id, @RequestBody Map<String, Object> request) {
        deptService.updateAdminPortalDept(id, request);
        return Result.success(ResultCodeEnum.UPDATE_SUCCESS);
    }

    @Operation(summary = "管理端删除部门", description = "删除 Vben 管理端部门")
    @DeleteMapping("/api/system/dept/{id}")
    public Result<String> deleteAdminPortalDept(@PathVariable String id) {
        deptService.deleteAdminPortalDept(id);
        return Result.success(ResultCodeEnum.DELETE_SUCCESS);
    }

    @Operation(summary = "添加部门", description = "添加部门")
    @PermissionTag(permission = "dept:add")
    @PostMapping("/api/dept")
    public Result<String> createDept(@Validated(ValidationGroups.Add.class) @RequestBody DeptDto dto) {
        deptService.createDept(dto);
        return Result.success(ResultCodeEnum.CREATE_SUCCESS);
    }

    @Operation(summary = "更新部门", description = "更新部门")
    @PermissionTag(permission = "dept:update")
    @PutMapping("/api/dept")
    public Result<String> updateDept(@Validated(ValidationGroups.Update.class) @RequestBody DeptDto dto) {
        deptService.updateDept(dto);
        return Result.success(ResultCodeEnum.UPDATE_SUCCESS);
    }

    @Operation(summary = "删除部门", description = "删除部门")
    @PermissionTag(permission = "dept:delete")
    @DeleteMapping("/api/dept")
    public Result<String> deleteDept(@RequestBody List<Long> ids) {
        deptService.deleteDept(ids);
        return Result.success(ResultCodeEnum.DELETE_SUCCESS);
    }

    @Operation(summary = "获取所有部门", description = "获取所有部门")
    @GetMapping("/api/dept/private/departments")
    public Result<List<DeptVo>> getDeptPage() {
        List<DeptVo> voList = deptService.getDeptPage();
        return Result.success(voList);
    }
}
