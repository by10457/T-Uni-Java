package t.uni.common.config.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 配置MP在修改和新增时的操作
 */
@Component
public class MybatisPlusFieldConfig implements MetaObjectHandler {

    @Autowired(required = false)
    private UserContextProvider userContextProvider;

    /**
     * 使用mp做添加操作时候，这个方法执行
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // 设置属性值
        this.strictInsertFill(metaObject, "isDeleted", Integer.class, 0);
        this.setFieldValByName("createTime", LocalDateTime.now(), metaObject);
        this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);

        if (userContextProvider != null && userContextProvider.getCurrentUserId() != null) {
            this.setFieldValByName("createUser", userContextProvider.getCurrentUserId(), metaObject);
            this.setFieldValByName("updateUser", userContextProvider.getCurrentUserId(), metaObject);
        }
    }

    /**
     * 使用mp做修改操作时候，这个方法执行
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        if (userContextProvider != null && userContextProvider.getCurrentUserId() != null) {
            this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
            this.setFieldValByName("updateUser", userContextProvider.getCurrentUserId(), metaObject);
        }
    }
}
