package t.uni.server.api.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import t.uni.common.core.result.Result;
import t.uni.server.common.context.UserContext;
import t.uni.server.auth.service.IUserInfoService;
import t.uni.server.domain.vo.auth.UserInfoVO;

/**
 * 用户信息Controller
 * <p>
 * 需要认证后才能访问的接口
 * </p>
 */
@Tag(name = "用户信息", description = "用户信息相关接口（需认证）")
@RestController
@RequestMapping("/api/wx/user")
@RequiredArgsConstructor
public class UserController {

    private final IUserInfoService userInfoService;

    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @PostMapping("/getUserInfo")
    public Result<UserInfoVO> getUserInfo() {
        // 从上下文获取当前用户ID
        var userId = UserContext.getUserId();
        var userInfo = userInfoService.getUserInfo(userId);
        return Result.success(userInfo);
    }
}
