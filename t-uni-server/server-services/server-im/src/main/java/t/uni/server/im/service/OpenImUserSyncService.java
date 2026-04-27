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
 * <p>
 * 负责把本地 CoreUser 按需导入 OpenIM，并维护本地 im_registered 标记。
 * 不保证分布式事务；OpenIM 写入失败只记录日志，调用方可根据后续 token 获取结果决定是否重试。
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
    /** 七牛模块可选存在；未装配时直接使用用户头像原值或默认头像。 */
    private final QiniuStorageService qiniuStorageService;
    private final OpenImAdminTokenProvider openImAdminTokenProvider;
    private static final String FALLBACK_USER_AVATAR = "https://img.icons8.com/color/96/user-male-circle--v1.png";

    /**
     * 构造用户同步服务。
     * <p>
     * QiniuStorageService 通过 ObjectProvider 获取，避免 IM 模块强依赖对象存储模块。
     */
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
     * 按需注册 OpenIM 用户。
     * <p>
     * 使用 Redis 短锁降低并发重复导入；OpenIM 返回“用户已存在”视为幂等成功。
     * 其他外部失败只记录日志，不更新本地注册标记。
     *
     * @param user 本地用户快照
     * @param openimUserId 由本地用户 ID 映射出的 OpenIM userID
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
     * 标记本地用户已完成 OpenIM 同步。
     * <p>
     * 只更新本地状态，不校验 OpenIM 侧是否真实存在；调用方应在确认导入或 token 获取成功后调用。
     *
     * @param userId 本地用户 ID
     */
    public void markRegistered(Long userId) {
        if (userId == null) {
            return;
        }
        openImCoreUserMapper.updateImRegistered(userId, true);
    }

    /**
     * 查询 IM 模块需要的本地用户信息。
     *
     * @param userId 本地用户 ID
     * @return 用户不存在或入参为空时返回 null
     */
    public CoreUser getUser(Long userId) {
        if (userId == null) {
            return null;
        }
        return openImCoreUserMapper.selectById(userId);
    }

    /**
     * 组装 OpenIM user_register 单个用户载荷。
     * <p>
     * 头像优先解析七牛访问地址；解析失败不阻断用户同步，继续使用原值或默认头像。
     */
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
