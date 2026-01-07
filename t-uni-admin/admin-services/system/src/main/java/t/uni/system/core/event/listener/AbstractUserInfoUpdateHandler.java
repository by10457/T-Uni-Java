package t.uni.system.core.event.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import t.uni.domain.common.constant.RedisUserConstant;
import t.uni.domain.system.entity.AdminUser;
import t.uni.system.core.cache.UserCacheCleaner;
import t.uni.system.mapper.UserMapper;

import java.util.List;
import java.util.function.Consumer;

@Component("AbstractUserInfoUpdateHandler")
public abstract class AbstractUserInfoUpdateHandler {

    @Autowired
    protected UserMapper userMapper;
    @Autowired
    protected UserCacheCleaner userCacheCleaner;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void processUserUpdate(List<Long> userIds, Consumer<AdminUser> postProcess) {
        if (userIds.isEmpty()) return;

        List<AdminUser> adminUsers = userMapper.selectBatchIds(userIds);
        adminUsers.stream()
                .filter(user -> redisTemplate.hasKey(RedisUserConstant.getUserLoginInfoPrefix(user.getUsername())))
                .forEach(postProcess);
    }
}