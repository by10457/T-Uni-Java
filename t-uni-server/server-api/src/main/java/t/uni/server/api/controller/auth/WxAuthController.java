package t.uni.server.api.controller.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import t.uni.common.core.result.Result;
import t.uni.server.auth.service.WxAuthService;
import t.uni.server.domain.dto.auth.WxLoginDTO;
import t.uni.server.domain.vo.auth.WxLoginVO;

/**
 * 微信认证Controller
 */
@Tag(name = "微信认证", description = "微信小程序登录相关接口")
@RestController
@RequestMapping("/api/wx/auth")
public class WxAuthController {

    @Resource
    private WxAuthService wxAuthService;

    @Operation(summary = "微信小程序登录", description = "使用微信登录code进行登录")
    @PostMapping("/login")
    public Result<WxLoginVO> wxLogin(@Valid @RequestBody WxLoginDTO dto) {
        WxLoginVO vo = wxAuthService.wxLogin(dto);
        return Result.success(vo);
    }

    @Operation(summary = "获取手机号", description = "获取用户手机号")
    @PostMapping("/phone/{userId}")
    public Result<String> getPhone(@PathVariable Long userId, @RequestParam String code) {
        String phone = wxAuthService.getPhoneNumber(code, userId);
        return Result.success(phone);
    }
}
