package t.uni.core.context;

import org.springframework.stereotype.Component;
import t.uni.common.config.mybatis.UserContextProvider;

/**
 * Admin用户上下文提供者
 * 实现UserContextProvider接口，为MyBatis Plus字段自动填充提供用户信息
 */
@Component
public class AdminUserContextProvider implements UserContextProvider {

    @Override
    public Long getCurrentUserId() {
        return BaseContext.getUserId();
    }

    @Override
    public String getCurrentUsername() {
        return BaseContext.getUsername();
    }
}
