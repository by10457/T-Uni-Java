package t.uni.server.auth.service;

import t.uni.server.domain.vo.auth.UserInfoVO;

/**
 * 用户信息服务接口
 */
public interface IUserInfoService {

    /**
     * 获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserInfoVO getUserInfo(Long userId);
}
