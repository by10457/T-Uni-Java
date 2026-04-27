package t.uni.server.im.constant;

/**
 * IM 模块 Redis Key 常量
 * <p>
 * 仅放置 IM 内部使用的 key 生成规则，避免与其他模块共享 Redis 命名细节。
 *
 * @author t-uni
 * @since 2026-04-24
 */
public final class ImRedisKeys {

    private ImRedisKeys() {
    }

    private static final String PREFIX = "im:";

    /**
     * OpenIM 用户注册短锁。
     *
     * @param openimUserId OpenIM userID
     * @return Redis 锁 key
     */
    public static String openimUserRegisterLock(String openimUserId) {
        return PREFIX + "openim:user:register:lock:" + openimUserId;
    }
}
