package t.uni.server.im.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import t.uni.server.domain.entity.CoreUser;

/**
 * IM 模块专用 CoreUser Mapper
 * <p>
 * 只包含 IM 需要的最小查询/更新，不依赖 server-auth 的 CoreUserMapper。
 * 由 OpenImConfiguration 的 @MapperScan 条件扫描注册。
 *
 * @author t-uni
 * @since 2026-04-24
 */
@Mapper
public interface OpenImCoreUserMapper {

    /**
     * 查询 IM 同步和 token 签发需要的本地用户记录。
     *
     * @param id 本地用户 ID
     * @return 用户不存在时返回 null
     */
    @Select("SELECT * FROM core_user WHERE id = #{id}")
    CoreUser selectById(@Param("id") Long id);

    /**
     * 更新本地用户的 OpenIM 同步标记。
     *
     * @param id 本地用户 ID
     * @param registered true 表示已确认可在 OpenIM 使用
     * @return 受影响行数
     */
    @Update("UPDATE core_user SET im_registered = #{registered} WHERE id = #{id}")
    int updateImRegistered(@Param("id") Long id, @Param("registered") Boolean registered);
}
