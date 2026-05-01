package t.uni.api.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import t.uni.common.core.result.PageResult;
import t.uni.common.core.result.Result;
import t.uni.common.core.result.ResultCodeEnum;
import t.uni.core.annotation.PermissionTag;
import t.uni.domain.system.dto.user.AdminUserAddDto;
import t.uni.domain.system.dto.user.AdminUserDto;
import t.uni.domain.system.dto.user.AdminUserUpdateDto;
import t.uni.domain.system.entity.AdminUser;
import t.uni.domain.system.vo.user.AdminUserVo;
import t.uni.domain.system.vo.user.UserVo;
import t.uni.system.service.UserService;

import java.util.List;
import java.util.Map;


@Tag(name = "用户", description = "用户信息相关接口")
@PermissionTag(permission = "user:*")
@RestController
@RequestMapping
public class UserController {

    @Resource
    private UserService userService;

    @Operation(summary = "分页查询用户", description = "分页查询用户信息")
    @PermissionTag(permission = "user:query")
    @GetMapping("/api/user/{page}/{limit}")
    public Result<PageResult<AdminUserVo>> getUserPageByAdmin(
            @PathVariable @Parameter(name = "page", description = "当前页", required = true) Integer page,
            @PathVariable @Parameter(name = "limit", description = "每页记录数", required = true) Integer limit,
            AdminUserDto dto) {
        Page<AdminUser> pageParams = new Page<>(page, limit);
        PageResult<AdminUserVo> pageResult = userService.getUserPageByAdmin(pageParams, dto);
        return Result.success(pageResult);
    }

    @Operation(summary = "管理端用户列表", description = "分页查询 Vben 管理端用户")
    @GetMapping("/api/system/user/list")
    public Result<Map<String, Object>> adminPortalUserList(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam Map<String, Object> query) {
        return Result.success(userService.adminPortalUserList(page, pageSize, query));
    }

    @Operation(summary = "管理端创建用户", description = "创建 Vben 管理端用户")
    @PostMapping("/api/system/user")
    public Result<Object> createAdminPortalUser(@RequestBody Map<String, Object> request) {
        userService.createAdminPortalUser(request);
        return Result.success(ResultCodeEnum.CREATE_SUCCESS);
    }

    @Operation(summary = "管理端更新用户", description = "更新 Vben 管理端用户")
    @PutMapping("/api/system/user/{id}")
    public Result<String> updateAdminPortalUser(@PathVariable String id, @RequestBody Map<String, Object> request) {
        userService.updateAdminPortalUser(id, request);
        return Result.success(ResultCodeEnum.UPDATE_SUCCESS);
    }

    @Operation(summary = "管理端删除用户", description = "删除 Vben 管理端用户")
    @DeleteMapping("/api/system/user/{id}")
    public Result<Object> deleteAdminPortalUser(@PathVariable String id) {
        userService.deleteAdminPortalUser(id);
        return Result.success(ResultCodeEnum.DELETE_SUCCESS);
    }

    @Operation(summary = "添加用户", description = "添加用户信息")
    @PermissionTag(permission = "user:add")
    @PostMapping("/api/user")
    public Result<Object> createUserByAdmin(@Valid @RequestBody AdminUserAddDto dto) {
        userService.createUserByAdmin(dto);
        return Result.success(ResultCodeEnum.CREATE_SUCCESS);
    }

    @Operation(summary = "更新用户", description = "更新用户信息，需要更新Redis中的内容")
    @PermissionTag(permission = "user:update")
    @PutMapping("/api/user")
    public Result<String> updateUserByAdmin(@Valid AdminUserUpdateDto dto,
                                            @RequestPart(value = "avatar", required = false) MultipartFile avatar) {
        if (avatar != null) dto.setAvatar(avatar);

        userService.updateUserByAdmin(dto);
        return Result.success(ResultCodeEnum.UPDATE_SUCCESS);
    }

    @Operation(summary = "删除用户", description = "删除用户")
    @PermissionTag(permission = "user:delete")
    @DeleteMapping("/api/user")
    public Result<Object> deleteUserByAdmin(@RequestBody List<Long> ids) {
        userService.deleteUserByAdmin(ids);
        return Result.success(ResultCodeEnum.DELETE_SUCCESS);
    }

    @Operation(summary = "根据用户id查询用户", description = "根据用户ID获取用户信息，不包含Redis中的信息")
    @GetMapping("/api/user/private/users/{id}")
    public Result<UserVo> getUserinfoById(@PathVariable Long id) {
        UserVo vo = userService.getUserinfoById(id);
        return Result.success(vo);
    }

    @Operation(summary = "根据关键字查询用户", description = "根据用户名查询用户列表")
    @GetMapping("/api/user/private/users/search")
    public Result<List<UserVo>> getUserListByKeyword(String keyword) {
        List<UserVo> voList = userService.getUserListByKeyword(keyword);
        return Result.success(voList);
    }

    @Operation(summary = "强制退出用户", description = "强制退出")
    @PermissionTag(permission = "user:update")
    @PutMapping("/api/user/{id}/force-logout")
    public Result<String> forcedOfflineByAdmin(@PathVariable Long id) {
        userService.forcedOfflineByAdmin(id);
        return Result.success();
    }

    @Operation(summary = "已登录用户", description = "查询缓存中已登录用户")
    @PermissionTag(permission = "user:query")
    @GetMapping("/api/user/users/logged-in/{page}/{limit}")
    public Result<PageResult<UserVo>> getCacheUserPage(
            @Parameter(name = "page", description = "当前页", required = true)
            @PathVariable("page") Integer page,
            @Parameter(name = "limit", description = "每页记录数", required = true)
            @PathVariable("limit") Integer limit) {
        Page<AdminUser> pageParams = new Page<>(page, limit);
        PageResult<UserVo> pageResult = userService.getCacheUserPage(pageParams);
        return Result.success(pageResult);
    }

}
