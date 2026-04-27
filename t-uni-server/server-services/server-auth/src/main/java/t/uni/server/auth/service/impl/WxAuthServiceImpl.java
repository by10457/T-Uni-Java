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
 * 微信认证服务实现。
 * <p>
 * 基于 core_user 与业务用户表双表实现登录：core_user 保存平台通用资料，
 * 业务用户表保存小程序 openId/unionId 等业务侧身份。登录标识支持按配置在 maOpenId 与 unionId 间切换。
 * </p>
 * <p>
 * 登录成功后使用 TokenService 维护 AccessToken/RefreshToken 与登录缓存；发现双表缺失时会尽量自动补建。
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
     * 微信小程序登录。
     * <p>
     * 通过微信 code 换取 openId/unionId，优先命中 Token 缓存；缓存未命中时查询或创建用户双表数据。
     * 本方法有事务边界，用户自动创建、异常数据补建和登录时间更新会一起回滚。
     * </p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TokenVO wxLogin(WxLoginDTO dto) {
        var code = dto.getCode();

        try {
            // 微信 code 只能换取一次 session 信息，日志仅打印脱敏标识。
            var sessionInfo = wxMaService.getUserService().getSessionInfo(code);
            var openId = sessionInfo.getOpenid();
            var unionId = sessionInfo.getUnionid();

            log.info("微信登录成功，openId: {}, unionId: {}",
                    MaskUtil.maskOpenId(openId), MaskUtil.maskUnionId(unionId));

            // 登录标识决定查业务用户表的字段：默认 maOpenId，跨应用打通场景可切换为 unionId。
            var loginIdentifier = getLoginIdentifier(openId, unionId);

            // 已登录用户可直接复用缓存 Token，减少微信接口换码后的数据库访问。
            var cachedToken = tokenService.getCachedToken(openId);
            if (cachedToken != null) {
                log.info("命中Token缓存，用户直接登录，openId: {}", MaskUtil.maskOpenId(openId));
                return cachedToken;
            }

            // 缓存未命中后进入双表校验链路。
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

            // 登录成功后刷新活跃时间，再写入 openId -> userId 登录缓存。
            updateLastLoginTime(userId);

            tokenService.cacheUserInfo(openId, userId, unionId);

            return tokenService.generateTokens(userId, openId);

        } catch (WxErrorException e) {
            log.error("微信登录失败，code: {}, error: {}", code, e.getMessage(), e);
            throw new BaseException(ResultCodeEnum.WX_LOGIN_FAILED.getCode(), "微信登录失败：" + e.getMessage());
        }
    }

    /**
     * 刷新 Token。
     * <p>
     * 具体校验、换发和旧索引清理由 TokenService 统一处理。
     * </p>
     */
    @Override
    public TokenVO refreshToken(RefreshTokenDTO dto) {
        return tokenService.refreshTokens(dto.getRefreshToken());
    }

    /**
     * 获取并保存微信授权手机号。
     * <p>
     * 手机号 code 由微信一次性授权产生，只写入 core_user 的手机号和授权时间。
     * </p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String getPhoneNumber(String code, Long userId) {
        try {
            // 手机号属于敏感信息，日志只输出脱敏值。
            var phoneNumberInfo = wxMaService.getUserService().getPhoneNumber(code);
            var phoneNumber = phoneNumberInfo.getPhoneNumber();

            log.info("获取手机号成功，userId: {}, phone: {}", userId, MaskUtil.maskPhone(phoneNumber));

            // 用户不存在时不自动建档，登录链路才负责双表创建。
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
     * 创建新用户双表数据。
     * <p>
     * core_user 记录平台通用资料，biz_user 复用相同主键并记录小程序身份，保证后续按 userId 关联。
     * 默认头像和昵称来自默认池，昵称为空时兜底为 user。
     * </p>
     *
     * @param openId  微信 openId
     * @param unionId 微信 unionId
     * @return 新用户ID
     */
    private Long createNewUser(String openId, String unionId) {
        // 默认池为空时允许继续建档，头像可为空，昵称由下面兜底。
        var avatarUrl = userDefaultService.getRandomAvatarUrl();
        var nickName = userDefaultService.getRandomNickName();

        var uniqueId = "U" + IdUtil.getSnowflakeNextIdStr();

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
     * <p>
     * 仅在拿到 unionId 时执行，避免不同小程序 openId 误关联到同一个核心用户。
     * </p>
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
     * <p>
     * 当微信返回的 openId/unionId 与业务表不一致时同步业务表；unionId 同时回写 core_user。
     * </p>
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
     * <p>
     * 以业务用户主键补建 core_user，保持双表 userId 一致。
     * </p>
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
     * <p>
     * 仅更新 core_user 活跃时间，业务用户表不承载该状态。
     * </p>
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
     * <p>
     * UNION_ID 模式要求微信本次返回 unionId；否则无法可靠定位跨应用用户。
     * </p>
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
     * <p>
     * 查询字段必须与 getLoginIdentifier 返回的标识保持一致。
     * </p>
     */
    private IBusinessUser queryBusinessUser(String loginIdentifier) {
        if ("UNION_ID".equals(wxAuthProperties.getLoginIdentifier())) {
            return businessUserMapper.findByUnionId(loginIdentifier);
        } else {
            return businessUserMapper.findByMaOpenId(loginIdentifier);
        }
    }
}
