package t.uni.common.config.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 字段自动填充配置。
 * <p>
 * 在新增和更新时填充审计字段。用户信息来自可选的 UserContextProvider；
 * 未接入登录上下文时只填充时间和删除标记，不强制依赖具体认证实现。
 * </p>
 */
@Component
public class MybatisPlusFieldConfig implements MetaObjectHandler {

    @Autowired(required = false)
    private UserContextProvider userContextProvider;

    /**
     * 新增记录时填充删除标记、创建时间、更新时间和用户审计字段。
     *
     * @param metaObject MyBatis 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "isDeleted", Integer.class, 0);
        this.setFieldValByName("createTime", LocalDateTime.now(), metaObject);
        this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);

        if (userContextProvider != null && userContextProvider.getCurrentUserId() != null) {
            this.setFieldValByName("createUser", userContextProvider.getCurrentUserId(), metaObject);
            this.setFieldValByName("updateUser", userContextProvider.getCurrentUserId(), metaObject);
        }
    }

    /**
     * 更新记录时填充更新时间和更新人。
     *
     * @param metaObject MyBatis 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        if (userContextProvider != null && userContextProvider.getCurrentUserId() != null) {
            this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
            this.setFieldValByName("updateUser", userContextProvider.getCurrentUserId(), metaObject);
        }
    }
}
