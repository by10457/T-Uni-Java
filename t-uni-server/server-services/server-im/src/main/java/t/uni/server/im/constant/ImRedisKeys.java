package t.uni.server.im.constant;

/**
 * IM 模块 Redis 逻辑 Key 常量
 * <p>
 * 仅生成 IM 内部使用的 Redis 逻辑 key，物理 namespace 由 common Redis 配置统一处理。
 * </p>
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
