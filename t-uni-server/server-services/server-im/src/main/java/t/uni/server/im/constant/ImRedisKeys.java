package t.uni.server.im.constant;

/**
 * IM 模块 Redis Key 常量
 *
 * @author t-uni
 * @since 2026-04-24
 */
public final class ImRedisKeys {

    private ImRedisKeys() {
    }

    private static final String PREFIX = "im:";

    /**
     * OpenIM 用户注册短锁
     */
    public static String openimUserRegisterLock(String openimUserId) {
        return PREFIX + "openim:user:register:lock:" + openimUserId;
    }
}
