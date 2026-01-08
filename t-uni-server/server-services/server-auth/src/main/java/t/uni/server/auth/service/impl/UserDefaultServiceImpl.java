package t.uni.server.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import t.uni.server.auth.mapper.CoreUserDefaultAvatarMapper;
import t.uni.server.auth.mapper.CoreUserDefaultNickNameMapper;
import t.uni.server.auth.service.IUserDefaultService;
import t.uni.server.auth.util.WeightedRandomSelector;
import t.uni.server.domain.entity.CoreUserDefaultAvatar;
import t.uni.server.domain.entity.CoreUserDefaultNickName;

/**
 * 用户默认值服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDefaultServiceImpl implements IUserDefaultService {

    private final CoreUserDefaultAvatarMapper avatarMapper;
    private final CoreUserDefaultNickNameMapper nickNameMapper;

    /**
     * 获取一个随机默认头像 URL
     */
    @Override
    public String getRandomAvatarUrl() {
        // 查询所有启用的头像
        var avatars = avatarMapper.selectList(Wrappers.<CoreUserDefaultAvatar>lambdaQuery()
                .eq(CoreUserDefaultAvatar::getIsEnable, 1)
                .orderByAsc(CoreUserDefaultAvatar::getSort));

        if (avatars.isEmpty()) {
            log.warn("默认头像池为空，无法分配头像");
            return null;
        }

        // 加权随机选择
        var selected = WeightedRandomSelector.select(avatars, CoreUserDefaultAvatar::getWeight);
        return selected != null ? selected.getAvatarUrl() : avatars.get(0).getAvatarUrl();
    }

    /**
     * 获取一个随机默认昵称
     */
    @Override
    public String getRandomNickName() {
        // 查询所有启用的昵称
        var nickNames = nickNameMapper.selectList(Wrappers.<CoreUserDefaultNickName>lambdaQuery()
                .eq(CoreUserDefaultNickName::getIsEnable, 1)
                .orderByAsc(CoreUserDefaultNickName::getSort));

        if (nickNames.isEmpty()) {
            log.warn("默认昵称池为空，无法分配昵称");
            return null;
        }

        // 加权随机选择
        var selected = WeightedRandomSelector.select(nickNames, CoreUserDefaultNickName::getWeight);
        return selected != null ? selected.getNickName() : nickNames.get(0).getNickName();
    }
}
