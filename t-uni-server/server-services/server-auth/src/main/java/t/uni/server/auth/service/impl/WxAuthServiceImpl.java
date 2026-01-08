package t.uni.server.auth.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import t.uni.common.core.exception.BaseException;
import t.uni.common.core.result.ResultCodeEnum;
import t.uni.server.auth.mapper.CoreUserMapper;
import t.uni.server.auth.service.UserDefaultService;
import t.uni.server.auth.service.WxAuthService;
import t.uni.server.common.auth.TokenService;
import t.uni.server.common.config.WxAuthProperties;
import t.uni.server.domain.auth.IBusinessUser;
import t.uni.server.domain.auth.IBusinessUserMapper;
import t.uni.server.domain.dto.auth.RefreshTokenDTO;
import t.uni.server.domain.dto.auth.WxLoginDTO;
import t.uni.server.domain.entity.CoreUser;
import t.uni.server.domain.entity.SocialUser;
import t.uni.server.domain.vo.auth.TokenVO;

import java.time.LocalDateTime;

/**
 * 微信认证服务实现
 * <p>
 * 基于 core_user + 业务用户表（如 social_user）双表实现登录，使用双Token机制
 * 支持通过配置切换登录标识（maOpenId 或 unionId）
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WxAuthServiceImpl implements WxAuthService {

    private final WxMaService wxMaService;
    private final TokenService tokenService;
    private final CoreUserMapper coreUserMapper;
    private final IBusinessUserMapper<? extends IBusinessUser> businessUserMapper;
    private final UserDefaultService userDefaultService;
    private final WxAuthProperties wxAuthProperties;

    /**
     * 微信小程序登录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TokenVO wxLogin(WxLoginDTO dto) {
        var code = dto.getCode();

        try {
            // 1. 调用微信API获取session信息
            var sessionInfo = wxMaService.getUserService().getSessionInfo(code);
            var openId = sessionInfo.getOpenid();
            var unionId = sessionInfo.getUnionid();

            log.info("微信登录成功，openId: {}, unionId: {}", openId, unionId);

            // 2. 根据配置选择登录标识
            String loginIdentifier = getLoginIdentifier(openId, unionId);

            // 3. 查询业务用户是否存在
            IBusinessUser businessUser = queryBusinessUser(loginIdentifier);

            Long userId;
            if (businessUser == null) {
                // 4. 新用户：创建 core_user 和业务用户
                userId = createNewUser(openId, unionId);
                log.info("创建新用户，openId: {}, userId: {}", openId, userId);
            } else {
                // 5. 老用户：更新 unionId（如果有变化）
                userId = businessUser.getId();
                updateExistingUser(businessUser, unionId);
                log.info("用户登录，userId: {}", userId);
            }

            // 6. 更新最后登录时间
            updateLastLoginTime(userId);

            // 7. 生成并返回双Token
            return tokenService.generateTokens(userId, openId);

        } catch (WxErrorException e) {
            log.error("微信登录失败，code: {}, error: {}", code, e.getMessage(), e);
            throw new BaseException(ResultCodeEnum.SERVICE_ERROR.getCode(), "微信登录失败：" + e.getMessage());
        }
    }

    /**
     * 刷新 Token
     */
    @Override
    public TokenVO refreshToken(RefreshTokenDTO dto) {
        return tokenService.refreshTokens(dto.getRefreshToken());
    }

    /**
     * 获取用户手机号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String getPhoneNumber(String code, Long userId) {
        try {
            // 1. 调用微信API获取手机号
            var phoneNumberInfo = wxMaService.getUserService().getPhoneNumber(code);
            var phoneNumber = phoneNumberInfo.getPhoneNumber();

            log.info("获取手机号成功，userId: {}, phone: {}", userId, phoneNumber);

            // 2. 更新用户手机号和授权时间
            var coreUser = coreUserMapper.selectById(userId);
            if (coreUser != null) {
                coreUser.setPhone(phoneNumber);
                coreUser.setAuthPhoneTime(LocalDateTime.now());
                coreUserMapper.updateById(coreUser);
            }

            return phoneNumber;

        } catch (WxErrorException e) {
            log.error("获取手机号失败，userId: {}, error: {}", userId, e.getMessage(), e);
            throw new BaseException(ResultCodeEnum.SERVICE_ERROR.getCode(), "获取手机号失败：" + e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 创建新用户（core_user + social_user）
     *
     * @param openId  微信 openId
     * @param unionId 微信 unionId
     * @return 新用户ID
     */
    private Long createNewUser(String openId, String unionId) {
        // 1. 从默认池获取头像和昵称
        var avatarUrl = userDefaultService.getRandomAvatarUrl();
        var nickName = userDefaultService.getRandomNickName();

        // 2. 生成用户唯一ID
        var uniqueId = "U" + IdUtil.getSnowflakeNextIdStr();

        // 3. 创建 core_user
        var coreUser = new CoreUser();
        coreUser.setUniqueId(uniqueId);
        coreUser.setUnionId(unionId);
        coreUser.setAvatarUrl(avatarUrl);
        coreUser.setNickName(nickName);
        coreUser.setGender(0);
        coreUser.setIsDisable(0);
        coreUser.setIsDestroy(0);
        coreUser.setIsFake(0);
        coreUser.setLastLoginTime(LocalDateTime.now());
        coreUser.setNewUsageTime(LocalDateTime.now());
        coreUserMapper.insert(coreUser);

        // 4. 创建 social_user（使用相同的ID进行关联）
        var socialUser = new SocialUser();
        socialUser.setId(coreUser.getId()); // 主键关联
        socialUser.setUniqueId(uniqueId);
        socialUser.setMaOpenId(openId);
        socialUser.setUnionId(unionId);
        socialUser.setStatus(1);

        ((IBusinessUserMapper<IBusinessUser>) businessUserMapper).insert(socialUser);

        return coreUser.getId();
    }

    /**
     * 更新已存在用户的信息
     */
    @SuppressWarnings("unchecked")
    private void updateExistingUser(IBusinessUser businessUser, String unionId) {
        if (StrUtil.isNotBlank(unionId) && !unionId.equals(businessUser.getUnionId())) {
            businessUser.setUnionId(unionId);
            ((IBusinessUserMapper<IBusinessUser>) businessUserMapper).updateById(businessUser);

            // 同步更新 core_user 的 unionId
            var coreUser = coreUserMapper.selectById(businessUser.getId());
            if (coreUser != null && !unionId.equals(coreUser.getUnionId())) {
                coreUser.setUnionId(unionId);
                coreUserMapper.updateById(coreUser);
            }
        }
    }

    /**
     * 更新最后登录时间
     */
    private void updateLastLoginTime(Long userId) {
        var coreUser = coreUserMapper.selectById(userId);
        if (coreUser != null) {
            coreUser.setLastLoginTime(LocalDateTime.now());
            coreUser.setNewUsageTime(LocalDateTime.now());
            coreUserMapper.updateById(coreUser);
        }
    }

    /**
     * 根据配置获取登录标识
     *
     * @param openId  微信 openId
     * @param unionId 微信 unionId
     * @return 登录标识（openId 或 unionId）
     */
    private String getLoginIdentifier(String openId, String unionId) {
        if ("UNION_ID".equals(wxAuthProperties.getLoginIdentifier())) {
            if (StrUtil.isBlank(unionId)) {
                throw new BaseException(ResultCodeEnum.SERVICE_ERROR.getCode(),
                        "当前小程序配置为 UNION_ID 登录，但未获取到 unionId，请检查小程序是否已绑定开放平台");
            }
            log.info("使用 unionId 作为登录标识: {}", unionId);
            return unionId;
        }
        log.info("使用 maOpenId 作为登录标识: {}", openId);
        return openId;
    }

    /**
     * 查询业务用户
     *
     * @param loginIdentifier 登录标识（openId 或 unionId）
     * @return 业务用户，不存在则返回 null
     */
    @SuppressWarnings("unchecked")
    private IBusinessUser queryBusinessUser(String loginIdentifier) {
        IBusinessUserMapper<IBusinessUser> mapper = (IBusinessUserMapper<IBusinessUser>) businessUserMapper;
        if ("UNION_ID".equals(wxAuthProperties.getLoginIdentifier())) {
            return mapper.selectOne(
                    Wrappers.lambdaQuery(IBusinessUser.class)
                            .eq(IBusinessUser::getUnionId, loginIdentifier));
        } else {
            return mapper.selectOne(
                    Wrappers.lambdaQuery(IBusinessUser.class)
                            .eq(IBusinessUser::getMaOpenId, loginIdentifier));
        }
    }
}
