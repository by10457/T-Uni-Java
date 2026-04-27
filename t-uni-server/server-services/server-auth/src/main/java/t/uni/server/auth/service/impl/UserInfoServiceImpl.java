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
 * 用户信息服务实现。
 * <p>
 * 读取 core_user 中的展示资料，并根据创建时间计算新用户标记。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserInfoServiceImpl implements IUserInfoService {

    private final CoreUserMapper coreUserMapper;

    /**
     * 获取用户信息。
     *
     * @param userId 核心用户 ID
     * @return 对客户端展示的用户资料
     */
    @Override
    public UserInfoVO getUserInfo(Long userId) {
        var coreUser = coreUserMapper.selectById(userId);
        if (coreUser == null) {
            throw new BaseException(ResultCodeEnum.USER_IS_EMPTY.getCode(), "用户不存在");
        }

        // 新用户只按创建时间窗口判断，避免依赖客户端是否完成资料填写。
        var isNewUser = false;
        if (coreUser.getCreateTime() != null) {
            var minutesDiff = ChronoUnit.MINUTES.between(coreUser.getCreateTime(), LocalDateTime.now());
            isNewUser = minutesDiff <= AuthConstant.NEW_USER_THRESHOLD_MINUTES;
        }

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
