package t.uni.server.domain.auth;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

/**
 * 业务用户通用Mapper接口
 * 所有业务场景的Mapper都应继承此接口
 * 支持不同业务场景使用不同的Mapper实现（如 SocialUserMapper、EduUserMapper、HygieneUserMapper）
 *
 * @param <T> 业务用户实体类型，必须实现 IBusinessUser 接口
 * @author lzx
 * @since 2026-01-08
 */
public interface IBusinessUserMapper<T extends IBusinessUser> extends BaseMapper<T> {

    /**
     * 根据微信小程序 openId 查询用户
     * <p>
     * 注意：不能使用 LambdaQueryWrapper + IBusinessUser::getMaOpenId，
     * 因为 MyBatis-Plus 会尝试从接口类型解析表元数据，导致 lambda 缓存失败。
     * 使用普通 QueryWrapper + 字符串字段名（数据库列名）可以避免此问题。
     * </p>
     *
     * @param maOpenId 微信小程序 openId
     * @return 业务用户，不存在返回 null
     */
    default T findByMaOpenId(String maOpenId) {
        return selectOne(Wrappers.<T>query().eq("ma_open_id", maOpenId));
    }

    /**
     * 根据微信 unionId 查询用户
     *
     * @param unionId 微信 unionId
     * @return 业务用户，不存在返回 null
     */
    default T findByUnionId(String unionId) {
        return selectOne(Wrappers.<T>query().eq("union_id", unionId));
    }

    /**
     * 插入业务用户
     * <p>
     * 该方法封装了泛型转换，避免在 Service 层出现未检查类型转换警告。
     * 调用者传入 IBusinessUser 类型，内部转换为 T 进行插入。
     * </p>
     *
     * @param user 业务用户实体
     */
    @SuppressWarnings("unchecked")
    default void insertBusinessUser(IBusinessUser user) {
        insert((T) user);
    }

    /**
     * 根据 ID 更新业务用户
     * <p>
     * 该方法封装了泛型转换，避免在 Service 层出现未检查类型转换警告。
     * 调用者传入 IBusinessUser 类型，内部转换为 T 进行更新。
     * </p>
     *
     * @param user 业务用户实体
     */
    @SuppressWarnings("unchecked")
    default void updateBusinessUserById(IBusinessUser user) {
        updateById((T) user);
    }
}
