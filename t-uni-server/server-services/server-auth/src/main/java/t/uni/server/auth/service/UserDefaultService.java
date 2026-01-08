package t.uni.server.auth.service;

/**
 * 用户默认值服务接口
 * <p>
 * 负责为新用户分配默认头像和昵称
 * </p>
 */
public interface UserDefaultService {

    /**
     * 获取一个随机默认头像 URL
     *
     * @return 头像 URL，如果无可用头像返回 null
     */
    String getRandomAvatarUrl();

    /**
     * 获取一个随机默认昵称
     *
     * @return 昵称，如果无可用昵称返回 null
     */
    String getRandomNickName();
}
