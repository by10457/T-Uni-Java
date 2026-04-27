package t.uni.server.api.controller.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import t.uni.common.core.exception.BaseException;
import t.uni.common.core.result.Result;
import t.uni.common.core.result.ResultCodeEnum;
import t.uni.server.auth.service.WxAuthService;
import t.uni.server.common.context.UserContext;
import t.uni.server.domain.dto.auth.GetPhoneDTO;

/**
 * 微信相关 Controller。
 * <p>
 * 提供需要登录态的微信能力入口，例如手机号授权。
 * </p>
 */
@Tag(name = "微信相关", description = "微信相关接口")
@RestController
@RequestMapping("/wechat")
@RequiredArgsConstructor
public class WeChatController {

    private final WxAuthService wxAuthService;

    /**
     * 获取当前用户授权手机号。
     * <p>
     * 依赖拦截器写入 UserContext；未登录时直接返回登录鉴权错误。
     * </p>
     *
     * @param dto 微信手机号授权请求
     * @return 微信返回的手机号
     */
    @Operation(summary = "获取手机号", description = "获取用户手机号")
    @PostMapping("/getPhone")
    public Result<String> getPhone(@Valid @RequestBody GetPhoneDTO dto) {
        var userId = UserContext.getUserId();
        if (userId == null) {
            throw new BaseException(ResultCodeEnum.LOGIN_AUTH);
        }
        var phone = wxAuthService.getPhoneNumber(dto.getCode(), userId);
        return Result.success(phone);
    }
}
