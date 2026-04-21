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
import t.uni.common.core.utils.MaskUtil;
import t.uni.server.auth.mapper.CoreUserMapper;
import t.uni.server.auth.service.UserDefaultService;
import t.uni.server.auth.service.WxAuthService;
import t.uni.server.common.auth.TokenService;
import t.uni.server.common.config.WxAuthProperties;
import t.uni.server.domain.auth.IBusinessUser;
import t.uni.server.domain.auth.IBusinessUserMapper;
import t.uni.server.domain.dto.auth.RefreshTokenDTO;
import t.uni.server.domain.dto.auth.WxLoginDTO;
import t.uni.server.domain.entity.BizUser;
import t.uni.server.domain.entity.CoreUser;
import t.uni.server.domain.vo.auth.TokenVO;

import java.time.LocalDateTime;

/**
 * 微信认证服务实现
 * <p>
 * 基于 core_user + 业务用户表双表实现登录，使用双 Token 机制。
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
    private final WxAuthProperties wxAuthProperties;
    private final UserDefaultService userDefaultService;
    private final IBusinessUserMapper<? extends IBusinessUser> businessUserMapper;

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

            log.info("微信登录成功，openId: {}, unionId: {}",
                    MaskUtil.maskOpenId(openId), MaskUtil.maskUnionId(unionId));

            // 2. 根据配置选择登录标识
            var loginIdentifier = getLoginIdentifier(openId, unionId);

            // 3. 先查缓存
            var cachedToken = tokenService.getCachedToken(openId);
            if (cachedToken != null) {
                log.info("命中Token缓存，用户直接登录，openId: {}", MaskUtil.maskOpenId(openId));
                return cachedToken;
            }

            // 4. 缓存未命中，查询业务用户是否存在
            IBusinessUser businessUser = queryBusinessUser(loginIdentifier);

            Long userId;
            if (businessUser == null) {
                userId = tryRecoverBusinessUserFromCore(openId, unionId);
                if (userId != null) {
                    log.warn("检测到 biz_user 缺失并已自动补建，openId: {}, userId: {}",
                            MaskUtil.maskOpenId(openId), userId);
                } else {
                    userId = createNewUser(openId, unionId);
                    log.info("创建新用户，openId: {}, userId: {}", MaskUtil.maskOpenId(openId), userId);
                }
            } else {
                userId = businessUser.getId();
                updateExistingUser(businessUser, openId, unionId);
                ensureCoreUserExists(businessUser, unionId);
                log.info("用户登录，userId: {}", userId);
            }

            // 7. 更新最后登录时间
            updateLastLoginTime(userId);

            // 8. 缓存用户信息
            tokenService.cacheUserInfo(openId, userId, unionId);

            // 9. 生成并返回双Token
            return tokenService.generateTokens(userId, openId);

        } catch (WxErrorException e) {
            log.error("微信登录失败，code: {}, error: {}", code, e.getMessage(), e);
            throw new BaseException(ResultCodeEnum.WX_LOGIN_FAILED.getCode(), "微信登录失败：" + e.getMessage());
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

            log.info("获取手机号成功，userId: {}, phone: {}", userId, MaskUtil.maskPhone(phoneNumber));

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
            throw new BaseException(ResultCodeEnum.WX_GET_PHONE_FAILED.getCode(), "获取手机号失败：" + e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 创建新用户（core_user + biz_user）
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
        coreUser.setNickName(StrUtil.blankToDefault(nickName, "user"));
        coreUser.setGender(0);
        coreUser.setIsDisable(0);
        coreUser.setIsDestroy(0);
        coreUser.setIsFake(0);
        coreUser.setLastLoginTime(LocalDateTime.now());
        coreUser.setNewUsageTime(LocalDateTime.now());
        coreUserMapper.insert(coreUser);

        var bizUser = new BizUser();
        bizUser.setId(coreUser.getId());
        bizUser.setUniqueId(uniqueId);
        bizUser.setMaOpenId(openId);
        bizUser.setUnionId(unionId);
        businessUserMapper.insertBusinessUser(bizUser);

        return coreUser.getId();
    }

    /**
     * 修复异常数据：core_user 已存在但 biz_user 缺失。
     */
    private Long tryRecoverBusinessUserFromCore(String openId, String unionId) {
        if (StrUtil.isBlank(unionId)) {
            return null;
        }
        var coreUser = coreUserMapper.selectOne(
                Wrappers.<CoreUser>lambdaQuery().eq(CoreUser::getUnionId, unionId).last("limit 1"));
        if (coreUser == null || coreUser.getId() == null) {
            return null;
        }

        var uniqueId = StrUtil.blankToDefault(coreUser.getUniqueId(), "U" + IdUtil.getSnowflakeNextIdStr());
        if (StrUtil.isBlank(coreUser.getUniqueId())) {
            coreUser.setUniqueId(uniqueId);
            coreUserMapper.updateById(coreUser);
        }

        var bizUser = new BizUser();
        bizUser.setId(coreUser.getId());
        bizUser.setUniqueId(uniqueId);
        bizUser.setMaOpenId(openId);
        bizUser.setUnionId(unionId);
        businessUserMapper.insertBusinessUser(bizUser);
        return coreUser.getId();
    }

    /**
     * 更新已存在用户的信息。
     */
    private void updateExistingUser(IBusinessUser businessUser, String openId, String unionId) {
        boolean changed = false;
        if (StrUtil.isNotBlank(openId) && !openId.equals(businessUser.getMaOpenId())) {
            businessUser.setMaOpenId(openId);
            changed = true;
        }
        if (StrUtil.isNotBlank(unionId) && !unionId.equals(businessUser.getUnionId())) {
            businessUser.setUnionId(unionId);
            changed = true;
        }
        if (!changed) {
            return;
        }

        businessUserMapper.updateBusinessUserById(businessUser);

        if (StrUtil.isNotBlank(unionId)) {
            var coreUser = coreUserMapper.selectById(businessUser.getId());
            if (coreUser != null && !unionId.equals(coreUser.getUnionId())) {
                coreUser.setUnionId(unionId);
                coreUserMapper.updateById(coreUser);
            }
        }
    }

    /**
     * 修复异常数据：业务用户存在但 core_user 缺失。
     */
    private void ensureCoreUserExists(IBusinessUser businessUser, String unionId) {
        if (businessUser == null || businessUser.getId() == null || coreUserMapper.selectById(businessUser.getId()) != null) {
            return;
        }

        var uniqueId = StrUtil.blankToDefault(businessUser.getUniqueId(), "U" + IdUtil.getSnowflakeNextIdStr());
        if (StrUtil.isBlank(businessUser.getUniqueId())) {
            businessUser.setUniqueId(uniqueId);
            businessUserMapper.updateBusinessUserById(businessUser);
        }

        var coreUser = new CoreUser();
        coreUser.setId(businessUser.getId());
        coreUser.setUniqueId(uniqueId);
        coreUser.setUnionId(unionId);
        coreUser.setAvatarUrl(userDefaultService.getRandomAvatarUrl());
        coreUser.setNickName(StrUtil.blankToDefault(userDefaultService.getRandomNickName(), "user"));
        coreUser.setGender(0);
        coreUser.setIsDisable(0);
        coreUser.setIsDestroy(0);
        coreUser.setIsFake(0);
        coreUser.setLastLoginTime(LocalDateTime.now());
        coreUser.setNewUsageTime(LocalDateTime.now());
        coreUserMapper.insert(coreUser);

        log.warn("检测到 core_user 缺失并已自动补建，userId: {}, uniqueId: {}", coreUser.getId(), uniqueId);
    }

    /**
     * 更新最后登录时间。
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
     * 根据配置获取登录标识。
     */
    private String getLoginIdentifier(String openId, String unionId) {
        if ("UNION_ID".equals(wxAuthProperties.getLoginIdentifier())) {
            if (StrUtil.isBlank(unionId)) {
                throw new BaseException(ResultCodeEnum.SERVICE_ERROR.getCode(), "当前配置要求使用 unionId 登录，但本次未获取到 unionId");
            }
            log.info("使用 unionId 作为登录标识: {}", MaskUtil.maskUnionId(unionId));
            return unionId;
        }
        log.info("使用 maOpenId 作为登录标识: {}", MaskUtil.maskOpenId(openId));
        return openId;
    }

    /**
     * 查询业务用户。
     */
    private IBusinessUser queryBusinessUser(String loginIdentifier) {
        if ("UNION_ID".equals(wxAuthProperties.getLoginIdentifier())) {
            return businessUserMapper.findByUnionId(loginIdentifier);
        } else {
            return businessUserMapper.findByMaOpenId(loginIdentifier);
        }
    }
}
