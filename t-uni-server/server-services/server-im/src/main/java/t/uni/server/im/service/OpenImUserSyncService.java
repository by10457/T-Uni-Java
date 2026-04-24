package t.uni.server.im.service;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import t.uni.common.config.qiniu.QiniuStorageService;
import t.uni.common.config.redis.RedisUtil;
import t.uni.server.domain.entity.CoreUser;
import t.uni.server.im.client.OpenImApiClient;
import t.uni.server.im.config.ConditionalOnOpenImEnabled;
import t.uni.server.im.config.OpenImProperties;
import t.uni.server.im.constant.ImRedisKeys;
import t.uni.server.im.persistence.OpenImCoreUserMapper;
import t.uni.server.im.support.OpenImErrorMapper;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * OpenIM 用户同步服务
 *
 * @author t-uni
 * @since 2026-04-24
 */
@Slf4j
@Service
@ConditionalOnOpenImEnabled
public class OpenImUserSyncService {

    private final RedisUtil redisUtil;
    private final OpenImApiClient openImApiClient;
    private final OpenImProperties openImProperties;
    private final OpenImCoreUserMapper openImCoreUserMapper;
    private final QiniuStorageService qiniuStorageService; // nullable
    private final OpenImAdminTokenProvider openImAdminTokenProvider;
    private static final String FALLBACK_USER_AVATAR = "https://img.icons8.com/color/96/user-male-circle--v1.png";

    public OpenImUserSyncService(
            RedisUtil redisUtil,
            OpenImApiClient openImApiClient,
            OpenImProperties openImProperties,
            OpenImCoreUserMapper openImCoreUserMapper,
            OpenImAdminTokenProvider openImAdminTokenProvider,
            ObjectProvider<QiniuStorageService> qiniuProvider) {
        this.redisUtil = redisUtil;
        this.openImApiClient = openImApiClient;
        this.openImProperties = openImProperties;
        this.openImCoreUserMapper = openImCoreUserMapper;
        this.openImAdminTokenProvider = openImAdminTokenProvider;
        this.qiniuStorageService = qiniuProvider.getIfAvailable();
    }

    /**
     * 按需注册 OpenIM 用户（幂等）
     */
    public void registerUser(CoreUser user, String openimUserId) {
        if (user == null || StrUtil.isBlank(openimUserId)) {
            return;
        }
        var lockKey = ImRedisKeys.openimUserRegisterLock(openimUserId);
        var locked = redisUtil.setIfAbsent(lockKey, "1",
            openImProperties.getUserRegisterLockSeconds(), TimeUnit.SECONDS);
        if (!locked) {
            return;
        }
        var adminToken = openImAdminTokenProvider.getAdminToken();
        var userPayload = buildUserPayload(user, openimUserId);
        var response = openImApiClient.registerUsers(adminToken, List.of(userPayload));
        if (response.isSuccess()) {
            return;
        }
        if (OpenImErrorMapper.isUserAlreadyRegistered(response.getErrCode())) {
            log.info("OpenIM用户已存在（幂等忽略）: userId={}", openimUserId);
            return;
        }
        log.warn("OpenIM用户导入失败: userId={}, errCode={}, errMsg={}",
            openimUserId, response.getErrCode(), response.getErrMsg());
    }

    /**
     * 标记用户已同步
     */
    public void markRegistered(Long userId) {
        if (userId == null) {
            return;
        }
        openImCoreUserMapper.updateImRegistered(userId, true);
    }

    public CoreUser getUser(Long userId) {
        if (userId == null) {
            return null;
        }
        return openImCoreUserMapper.selectById(userId);
    }

    private HashMap<String, Object> buildUserPayload(CoreUser user, String openimUserId) {
        var payload = new HashMap<String, Object>();
        payload.put("userID", openimUserId);
        payload.put("nickname", StrUtil.blankToDefault(user.getNickName(), openimUserId));

        // 头像：尝试七牛解析 → 失败用原值 → 再失败用默认
        var avatarUrl = user.getAvatarUrl();
        if (qiniuStorageService != null && StrUtil.isNotBlank(avatarUrl)) {
            try {
                avatarUrl = qiniuStorageService.resolveAccessUrl(avatarUrl);
            } catch (Exception e) {
                log.debug("七牛解析头像失败，使用原值: {}", avatarUrl);
            }
        }
        payload.put("faceURL", StrUtil.blankToDefault(StrUtil.blankToDefault(avatarUrl,
            openImProperties.getDefaultUserAvatar()), FALLBACK_USER_AVATAR));
        return payload;
    }
}
