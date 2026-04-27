package t.uni.server.api.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import t.uni.common.core.result.Result;
import t.uni.server.auth.service.IUserInfoService;
import t.uni.server.common.context.UserContext;
import t.uni.server.domain.vo.auth.UserInfoVO;

/**
 * 用户信息 Controller。
 * <p>
 * 提供已登录用户资料查询入口，用户 ID 从 UserContext 获取。
 * </p>
 */
@Tag(name = "用户信息", description = "用户信息相关接口（需认证）")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final IUserInfoService userInfoService;

    /**
     * 获取当前登录用户信息。
     *
     * @return 当前用户资料和新用户标记
     */
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/getUserInfo")
    public Result<UserInfoVO> getUserInfo() {
        // 用户 ID 由认证拦截器解析 Token 后写入线程上下文。
        var userId = UserContext.getUserId();
        var userInfo = userInfoService.getUserInfo(userId);
        return Result.success(userInfo);
    }
}
