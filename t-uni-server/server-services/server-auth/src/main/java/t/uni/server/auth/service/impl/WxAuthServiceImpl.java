package t.uni.server.auth.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
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
import t.uni.server.auth.mapper.SocialUserMapper;
import t.uni.server.auth.service.IUserDefaultService;
import t.uni.server.auth.service.WxAuthService;
import t.uni.server.common.auth.ITokenService;
import t.uni.server.domain.dto.auth.RefreshTokenDTO;
import t.uni.server.domain.dto.auth.WxLoginDTO;
import t.uni.server.domain.entity.CoreUser;
import t.uni.server.domain.entity.SocialUser;
import t.uni.server.domain.vo.auth.TokenVO;

import java.time.LocalDateTime;

/**
 * 微信认证服务实现
 * <p>
 * 基于 core_user + social_user 双表实现登录，使用双Token机制
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WxAuthServiceImpl implements WxAuthService {

    private final WxMaService wxMaService;
    private final CoreUserMapper coreUserMapper;
    private final SocialUserMapper socialUserMapper;
    private final ITokenService tokenService;
    private final IUserDefaultService userDefaultService;

    /**
     * 微信小程序登录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TokenVO wxLogin(WxLoginDTO dto) {
        var code = dto.getCode();

        try {
            // 1. 调用微信API获取session信息
            WxMaJscode2SessionResult sessionInfo = wxMaService.getUserService().getSessionInfo(code);
            var openId = sessionInfo.getOpenid();
            var unionId = sessionInfo.getUnionid();

            log.info("微信登录成功，openId: {}", openId);

            // 2. 查询社交用户是否存在（通过 ma_open_id）
            var socialUser = socialUserMapper.selectOne(Wrappers.<SocialUser>lambdaQuery().eq(SocialUser::getMaOpenId, openId));

            Long userId;
            if (socialUser == null) {
                // 3. 新用户：创建 core_user 和 social_user
                userId = createNewUser(openId, unionId);
                log.info("创建新用户，openId: {}, userId: {}", openId, userId);
            } else {
                // 4. 老用户：更新 unionId（如果有变化）
                userId = socialUser.getId();
                updateExistingUser(socialUser, unionId);
                log.info("用户登录，userId: {}", userId);
            }

            // 5. 更新最后登录时间
            updateLastLoginTime(userId);

            // 6. 生成并返回双Token
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
            WxMaPhoneNumberInfo phoneNumberInfo = wxMaService.getUserService().getPhoneNumber(code);
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

        // 由于 social_user 的 id 是自增的，需要先插入再更新，或者使用手动设置
        // 这里使用 REPLACE INTO 或直接设置（需要表允许手动设置主键）
        socialUserMapper.insert(socialUser);

        return coreUser.getId();
    }

    /**
     * 更新已存在用户的信息
     */
    private void updateExistingUser(SocialUser socialUser, String unionId) {
        if (StrUtil.isNotBlank(unionId) && !unionId.equals(socialUser.getUnionId())) {
            socialUser.setUnionId(unionId);
            socialUserMapper.updateById(socialUser);

            // 同步更新 core_user 的 unionId
            var coreUser = coreUserMapper.selectById(socialUser.getId());
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
}
