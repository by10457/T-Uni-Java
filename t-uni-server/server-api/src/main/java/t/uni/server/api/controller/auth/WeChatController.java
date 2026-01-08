package t.uni.server.api.controller.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import t.uni.common.core.result.Result;
import t.uni.server.auth.service.WxAuthService;
import t.uni.server.domain.dto.auth.GetPhoneDTO;

/**
 * 微信相关Controller
 */
@Tag(name = "微信相关", description = "微信相关接口")
@RestController
@RequestMapping("/wechat")
@RequiredArgsConstructor
public class WeChatController {

    private final WxAuthService wxAuthService;

    @Operation(summary = "获取手机号", description = "获取用户手机号")
    @PostMapping("/getPhone")
    public Result<String> getPhone(@Valid @RequestBody GetPhoneDTO dto) {
        var phone = wxAuthService.getPhoneNumber(dto.getCode(), dto.getUserId());
        return Result.success(phone);
    }
}
