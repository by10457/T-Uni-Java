package t.uni.server.auth.service;

import t.uni.server.domain.dto.auth.RefreshTokenDTO;
import t.uni.server.domain.dto.auth.WxLoginDTO;
import t.uni.server.domain.vo.auth.TokenVO;

/**
 * 微信认证服务。
 * <p>
 * 负责微信小程序登录、Token 刷新和手机号授权，不处理具体 Controller 鉴权入口。
 * 登录流程基于 core_user 与业务用户表双表模型，返回 AccessToken 与 RefreshToken。
 * </p>
 */
public interface WxAuthService {

    /**
     * 使用微信临时 code 登录。
     * <p>
     * 服务端通过微信接口换取 openId/unionId，再按配置选择登录标识，必要时自动创建或修复用户双表数据。
     * </p>
     *
     * @param dto 微信登录请求，code 为一次性凭证
     * @return 双 Token 与各自有效期
     */
    TokenVO wxLogin(WxLoginDTO dto);

    /**
     * 使用 RefreshToken 换发 Token。
     *
     * @param dto 刷新请求，refreshToken 不能为空
     * @return 新的 AccessToken 与 RefreshToken
     */
    TokenVO refreshToken(RefreshTokenDTO dto);

    /**
     * 使用微信手机号授权 code 获取手机号并写入当前用户。
     *
     * @param code   微信手机号授权 code
     * @param userId 当前登录用户 ID
     * @return 微信返回的手机号
     */
    String getPhoneNumber(String code, Long userId);
}
