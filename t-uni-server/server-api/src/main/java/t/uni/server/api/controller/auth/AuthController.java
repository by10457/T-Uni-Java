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
import t.uni.server.domain.dto.auth.RefreshTokenDTO;
import t.uni.server.domain.dto.auth.WxLoginDTO;
import t.uni.server.domain.vo.auth.TokenVO;

/**
 * 认证 Controller。
 * <p>
 * 提供微信小程序登录和 Token 刷新入口，不直接处理用户资料读取。
 * </p>
 */
@Tag(name = "授权认证", description = "授权认证相关接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final WxAuthService wxAuthService;

    /**
     * 微信小程序登录。
     * <p>
     * 使用客户端传入的微信 code 完成登录，返回 AccessToken 与 RefreshToken。
     * </p>
     *
     * @param dto 微信登录请求
     * @return 双 Token 响应
     */
    @Operation(summary = "微信小程序登录", description = "使用微信登录code进行登录，返回双Token")
    @PostMapping("/wxLogin")
    public Result<TokenVO> wxLogin(@Valid @RequestBody WxLoginDTO dto) {
        var vo = wxAuthService.wxLogin(dto);
        return Result.success(vo);
    }

    /**
     * 刷新 Token。
     *
     * @param dto RefreshToken 请求
     * @return 换发后的双 Token
     */
    @Operation(summary = "刷新Token", description = "使用refreshToken刷新accessToken")
    @PostMapping("/refreshToken")
    public Result<TokenVO> refreshToken(@Valid @RequestBody RefreshTokenDTO dto) {
        var vo = wxAuthService.refreshToken(dto);
        return Result.success(vo);
    }
}
