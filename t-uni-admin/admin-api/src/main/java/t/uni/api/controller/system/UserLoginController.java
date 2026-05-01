package t.uni.api.controller.system;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import t.uni.common.core.exception.BaseException;
import t.uni.common.core.result.Result;
import t.uni.common.core.result.ResultCodeEnum;
import t.uni.core.context.BaseContext;
import t.uni.domain.common.enums.AdminResultCodeEnum;
import t.uni.domain.common.model.vo.LoginVo;
import t.uni.domain.system.dto.user.AdminUserUpdateByLocalUserDto;
import t.uni.domain.system.dto.user.LoginDto;
import t.uni.domain.system.dto.user.RefreshTokenDto;
import t.uni.domain.system.vo.user.RefreshTokenVo;
import t.uni.system.service.RouterService;
import t.uni.system.service.UserLoginService;

import java.util.List;
import java.util.Map;


@Tag(name = "普通用户登录", description = "用户登录相关接口")
@RestController
@RequiredArgsConstructor
@RequestMapping
public class UserLoginController {

    private final UserLoginService userLoginService;
    private final RouterService routerService;

    @Operation(summary = "普通用户登录", description = "前端用户登录")
    @PostMapping("/api/user/login")
    public Result<LoginVo> login(@Valid @RequestBody LoginDto loginDto) {
        LoginVo loginVo = userLoginService.login(loginDto);
        return Result.success(loginVo);
    }

    @Operation(summary = "管理端登录", description = "Vben 管理端账号密码登录")
    @PostMapping("/api/auth/login")
    public Result<Map<String, Object>> adminPortalLogin(@RequestBody Map<String, Object> request) {
        return Result.success(userLoginService.adminPortalLogin(request));
    }

    @Operation(summary = "管理端刷新令牌", description = "Vben 管理端刷新当前登录用户令牌")
    @PostMapping("/api/auth/refresh")
    public Result<Map<String, Object>> adminPortalRefresh() {
        return Result.success(userLoginService.adminPortalRefresh());
    }

    @Operation(summary = "管理端退出登录", description = "Vben 管理端退出当前登录用户")
    @PostMapping("/api/auth/logout")
    public Result<String> adminPortalLogout() {
        userLoginService.logout();
        return Result.success(null, ResultCodeEnum.LOGOUT_SUCCESS.getMessage());
    }

    @Operation(summary = "管理端权限码", description = "获取当前用户拥有的按钮权限码")
    @GetMapping("/api/auth/codes")
    public Result<List<String>> adminPortalCodes() {
        return Result.success(routerService.adminPortalCodes());
    }

    @Operation(summary = "普通用户登录发送邮件验证码", description = "登录发送邮件验证码")
    @PostMapping("/api/user/public/email-code")
    public Result<String> sendLoginEmail(String email) {
        if (!StringUtils.hasText(email))
            throw new BaseException(ResultCodeEnum.REQUEST_IS_EMPTY);

        userLoginService.sendLoginEmail(email);
        return Result.success(null,
                AdminResultCodeEnum.EMAIL_CODE_SEND_SUCCESS.getCode(),
                AdminResultCodeEnum.EMAIL_CODE_SEND_SUCCESS.getMessage());
    }

    @Operation(summary = "普通用户登录刷新token", description = "刷新用户token")
    @PostMapping("/api/user/private/refresh-token")
    public Result<RefreshTokenVo> refreshToken(@Valid @RequestBody RefreshTokenDto dto) {
        RefreshTokenVo vo = userLoginService.refreshToken(dto);
        return Result.success(vo);
    }

    @Operation(summary = "获取本地登录用户信息", description = "获取用户信息从Redis中获取")
    @GetMapping("/api/user/private/userinfo")
    public Result<LoginVo> userinfo() {
        LoginVo vo = BaseContext.getLoginVo();
        return Result.success(vo);
    }

    @Operation(summary = "管理端用户信息", description = "获取当前登录用户的管理端前端用户信息")
    @GetMapping("/api/user/info")
    public Result<Map<String, Object>> adminPortalUserInfo() {
        return Result.success(userLoginService.adminPortalUserInfo());
    }

    @Operation(summary = "普通用户登录退出登录", description = "退出登录")
    @PostMapping("/api/user/private/logout")
    public Result<String> logout() {
        userLoginService.logout();
        return Result.success(ResultCodeEnum.LOGOUT_SUCCESS);
    }

    @Operation(summary = "更新本地用户信息", description = "更新本地用户信息，需要更新Redis中的内容")
    @PutMapping("/api/user/private/update/userinfo")
    public Result<String> updateAdminUserByLocalUser(@Valid @RequestBody AdminUserUpdateByLocalUserDto dto) {
        userLoginService.updateAdminUserByLocalUser(dto);
        return Result.success(ResultCodeEnum.UPDATE_SUCCESS);
    }

    @Operation(summary = "更新本地用户密码", description = "更新本地用户密码")
    @PutMapping("/api/user/private/update/password")
    public Result<String> updateUserPasswordByLocalUser(String password) {
        userLoginService.updateUserPasswordByLocalUser(password);
        return Result.success(ResultCodeEnum.UPDATE_SUCCESS);
    }
}
