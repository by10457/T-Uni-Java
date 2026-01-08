package t.uni.server.auth.service;

import t.uni.server.domain.dto.auth.RefreshTokenDTO;
import t.uni.server.domain.dto.auth.WxLoginDTO;
import t.uni.server.domain.vo.auth.TokenVO;

/**
 * 微信认证服务接口
 */
public interface WxAuthService {

    /**
     * 微信小程序登录
     *
     * @param dto 登录参数
     * @return Token响应（双Token + 过期时间）
     */
    TokenVO wxLogin(WxLoginDTO dto);

    /**
     * 刷新 Token
     *
     * @param dto 刷新请求
     * @return 新的Token响应
     */
    TokenVO refreshToken(RefreshTokenDTO dto);

    /**
     * 获取用户手机号
     *
     * @param code   手机号code
     * @param userId 用户ID
     * @return 手机号
     */
    String getPhoneNumber(String code, Long userId);
}
