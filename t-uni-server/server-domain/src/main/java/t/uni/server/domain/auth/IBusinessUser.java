package t.uni.server.domain.auth;

import java.io.Serializable;

/**
 * 业务用户通用接口
 * 所有业务场景的用户实体都应实现此接口
 * 支持不同业务场景使用不同的实体类（如 SocialUser、EduUser、HygieneUser）
 *
 * @author Claude
 * @since 2026-01-08
 */
public interface IBusinessUser extends Serializable {

    /**
     * 获取用户ID（主键）
     * 与 core_user 表的 id 字段一对一关联
     *
     * @return 用户ID
     */
    Long getId();

    /**
     * 设置用户ID
     *
     * @param id 用户ID
     */
    void setId(Long id);

    /**
     * 获取用户唯一ID
     * 格式：U + Snowflake ID
     *
     * @return 用户唯一ID
     */
    String getUniqueId();

    /**
     * 设置用户唯一ID
     *
     * @param uniqueId 用户唯一ID
     */
    void setUniqueId(String uniqueId);

    /**
     * 获取微信小程序 openId
     * 用于场景A（未申请unionId）的唯一标识
     *
     * @return 微信小程序 openId
     */
    String getMaOpenId();

    /**
     * 设置微信小程序 openId
     *
     * @param maOpenId 微信小程序 openId
     */
    void setMaOpenId(String maOpenId);

    /**
     * 获取微信公众号 openId
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
     * 获取微信 unionId
     * 用于场景B（已申请unionId）的唯一标识
     * 同一主体下的多个小程序/公众号共享同一个 unionId
     *
     * @return 微信 unionId
     */
    String getUnionId();

    /**
     * 设置微信 unionId
     *
     * @param unionId 微信 unionId
     */
    void setUnionId(String unionId);

    /**
     * 获取状态
     * 0: 未关注
     * 1: 已关注
     *
     * @return 状态
     */
    Integer getStatus();

    /**
     * 设置状态
     *
     * @param status 状态
     */
    void setStatus(Integer status);
}
