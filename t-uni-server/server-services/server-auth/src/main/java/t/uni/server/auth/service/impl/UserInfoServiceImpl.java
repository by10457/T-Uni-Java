package t.uni.server.auth.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import t.uni.common.core.exception.BaseException;
import t.uni.common.core.result.ResultCodeEnum;
import t.uni.server.auth.mapper.CoreUserMapper;
import t.uni.server.auth.service.IUserInfoService;
import t.uni.server.domain.constant.AuthConstant;
import t.uni.server.domain.vo.auth.UserInfoVO;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 用户信息服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserInfoServiceImpl implements IUserInfoService {

    private final CoreUserMapper coreUserMapper;

    /**
     * 获取用户信息
     */
    @Override
    public UserInfoVO getUserInfo(Long userId) {
        // 1. 查询核心用户信息
        var coreUser = coreUserMapper.selectById(userId);
        if (coreUser == null) {
            throw new BaseException(ResultCodeEnum.USER_IS_EMPTY.getCode(), "用户不存在");
        }

        // 2. 判断是否为新用户（创建时间距离当前时间 ≤ 2分钟）
        var isNewUser = false;
        if (coreUser.getCreateTime() != null) {
            var minutesDiff = ChronoUnit.MINUTES.between(coreUser.getCreateTime(), LocalDateTime.now());
            isNewUser = minutesDiff <= AuthConstant.NEW_USER_THRESHOLD_MINUTES;
        }

        // 3. 构建并返回用户信息
        return UserInfoVO.builder()
                .userId(coreUser.getId())
                .uniqueId(coreUser.getUniqueId())
                .nickname(coreUser.getNickName())
                .avatar(coreUser.getAvatarUrl())
                .gender(coreUser.getGender())
                .isNewUser(isNewUser)
                .build();
    }
}
