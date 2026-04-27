package t.uni.server.auth.service;

/**
 * 用户默认值服务。
 * <p>
 * 负责从默认池中为新用户分配头像和昵称，供微信登录自动建档时使用。
 * </p>
 */
public interface UserDefaultService {

    /**
     * 获取一个随机默认头像 URL。
     *
     * @return 头像 URL；无可用头像时返回 null，由调用方决定兜底策略
     */
    String getRandomAvatarUrl();

    /**
     * 获取一个随机默认昵称。
     *
     * @return 昵称；无可用昵称时返回 null，由调用方决定兜底策略
     */
    String getRandomNickName();
}
