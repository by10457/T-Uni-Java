package t.uni.server.api.controller.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import t.uni.common.core.result.Result;
import t.uni.server.auth.service.WxAuthService;
import t.uni.server.domain.dto.auth.RefreshTokenDTO;
import t.uni.server.domain.dto.auth.WxLoginDTO;
import t.uni.server.domain.vo.auth.TokenVO;

/**
 * 微信认证Controller
 */
@Tag(name = "微信认证", description = "微信小程序登录相关接口")
@RestController
@RequestMapping("/api/wx/auth")
@RequiredArgsConstructor
public class WxAuthController {

    private final WxAuthService wxAuthService;

    @Operation(summary = "微信小程序登录", description = "使用微信登录code进行登录，返回双Token")
    @PostMapping("/login")
    public Result<TokenVO> wxLogin(@Valid @RequestBody WxLoginDTO dto) {
        var vo = wxAuthService.wxLogin(dto);
        return Result.success(vo);
    }

    @Operation(summary = "刷新Token", description = "使用refreshToken刷新accessToken")
    @PostMapping("/refreshToken")
    public Result<TokenVO> refreshToken(@Valid @RequestBody RefreshTokenDTO dto) {
        var vo = wxAuthService.refreshToken(dto);
        return Result.success(vo);
    }

    @Operation(summary = "获取手机号", description = "获取用户手机号")
    @PostMapping("/phone/{userId}")
    public Result<String> getPhone(@PathVariable Long userId, @RequestParam String code) {
        var phone = wxAuthService.getPhoneNumber(code, userId);
        return Result.success(phone);
    }
}
