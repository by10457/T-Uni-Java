package t.uni.server.im.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import cn.hutool.core.util.StrUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import t.uni.common.core.exception.BaseException;
import t.uni.common.core.result.Result;
import t.uni.server.im.ImResultCodeEnum;
import t.uni.server.im.config.ConditionalOnOpenImEnabled;
import t.uni.server.im.config.OpenImProperties;
import t.uni.server.im.dto.ImTokenRequestDTO;
import t.uni.server.im.service.OpenImTokenService;
import t.uni.server.im.vo.ImConfigVO;
import t.uni.server.im.vo.ImTokenVO;
import t.uni.server.common.context.UserContext;

/**
 * IM 前端接口。
 * <p>
 * 仅在 OpenIM 模块启用时暴露，返回客户端连接 OpenIM 所需的公开配置和用户 token。
 *
 * @author t-uni
 * @since 2026-04-24
 */
@Tag(name = "IM 模块", description = "OpenIM 相关接口")
@RestController
@ConditionalOnOpenImEnabled
@RequestMapping("/im")
@RequiredArgsConstructor
public class ImController {

    private final OpenImProperties openImProperties;
    private final OpenImTokenService openImTokenService;

    /**
     * 返回客户端可见的 OpenIM 连接配置。
     * <p>
     * 管理员密钥、Webhook token 等服务端敏感配置不会通过该接口返回。
     *
     * @return OpenIM REST、WebSocket 地址和系统通知账号
     */
    @Operation(summary = "获取 IM 配置", description = "返回 OpenIM 公开连接配置")
    @GetMapping("/config")
    public Result<ImConfigVO> getConfig() {
        if (StrUtil.isBlank(openImProperties.getApiAddress()) || StrUtil.isBlank(openImProperties.getWsAddress())) {
            throw new BaseException(ImResultCodeEnum.IM_CONFIG_MISSING.getCode(),
                ImResultCodeEnum.IM_CONFIG_MISSING.getMessage());
        }
        var config = new ImConfigVO(
            openImProperties.getApiAddress(),
            openImProperties.getWsAddress(),
            openImProperties.getSystemNoticeUserId()
        );
        return Result.success(config);
    }

    /**
     * 为当前登录用户签发 OpenIM user token。
     *
     * @param dto 平台 ID 请求参数
     * @return OpenIM 登录 token
     */
    @Operation(summary = "获取 IM Token", description = "获取 OpenIM user token")
    @PostMapping("/token")
    public Result<ImTokenVO> getToken(@RequestBody @Valid ImTokenRequestDTO dto) {
        var userId = UserContext.getUserId();
        return Result.success(openImTokenService.getUserToken(userId, dto.getPlatformId()));
    }
}
