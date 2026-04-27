package t.uni.server.auth.service;

import t.uni.server.domain.vo.auth.UserInfoVO;

/**
 * 用户信息服务。
 * <p>
 * 面向已认证用户读取核心用户资料，不负责登录态校验。
 * </p>
 */
public interface IUserInfoService {

    /**
     * 获取当前用户对外展示资料。
     *
     * @param userId 核心用户 ID
     * @return 用户基础资料和新用户标记
     */
    UserInfoVO getUserInfo(Long userId);
}
