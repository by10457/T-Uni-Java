package t.uni.server.domain.auth;

import java.io.Serializable;

/**
 * 业务用户实体扩展点。
 * <p>
 * 模板只假定业务用户具备与 {@code core_user} 一对一关联的主键、对外唯一 ID 和微信身份字段。
 * 具体业务可自定义用户表和扩展字段，但实体需要实现该接口，以便认证流程完成查询、创建和更新。
 * </p>
 *
 * @author lzx
 * @since 2026-01-08
 */
public interface IBusinessUser extends Serializable {

    /**
     * 获取业务用户主键。
     * <p>
     * 该值必须与 {@code core_user.id} 保持一对一一致。
     * </p>
     *
     * @return 业务用户 ID
     */
    Long getId();

    /**
     * 设置业务用户主键。
     *
     * @param id 与 {@code core_user.id} 一致的用户 ID
     */
    void setId(Long id);

    /**
     * 获取对外展示的用户唯一 ID。
     * <p>
     * 默认格式为 {@code U + Snowflake ID}，业务侧不要把它当作数据库主键使用。
     * </p>
     *
     * @return 用户唯一ID
     */
    String getUniqueId();

    /**
     * 设置对外展示的用户唯一 ID。
     *
     * @param uniqueId 用户唯一ID
     */
    void setUniqueId(String uniqueId);

    /**
     * 获取微信小程序 openId。
     * <p>
     * 当登录标识策略为 {@code MA_OPEN_ID} 时，该字段用于定位业务用户。
     * </p>
     *
     * @return 微信小程序 openId
     */
    String getMaOpenId();

    /**
     * 设置微信小程序 openId。
     *
     * @param maOpenId 微信小程序 openId
     */
    void setMaOpenId(String maOpenId);

    /**
     * 获取微信公众号 openId。
     * <p>
     * 预留给公众号接入场景；小程序登录流程不要求该字段必填。
     * </p>
     *
     * @return 微信公众号 openId
     */
    String getMpOpenId();

    /**
     * 设置微信公众号 openId
     *
     * @param mpOpenId 微信公众号 openId
     */
    void setMpOpenId(String mpOpenId);

    /**
     * 获取微信 unionId。
     * <p>
     * 当登录标识策略为 {@code UNION_ID} 时，该字段用于跨小程序、公众号统一识别用户。
     * </p>
     *
     * @return 微信 unionId
     */
    String getUnionId();

    /**
     * 设置微信 unionId。
     *
     * @param unionId 微信 unionId
     */
    void setUnionId(String unionId);

}
