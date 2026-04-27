package t.uni.server.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 核心用户实体。
 * <p>
 * 保存跨业务通用的用户资料、状态和认证时间。业务用户表通过相同主键 {@code id}
 * 与该表一对一关联，业务私有字段应放在业务用户表。
 * </p>
 */
@Data
@TableName("core_user")
public class CoreUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户唯一ID（对外展示，不作为数据库主键）
     */
    private String uniqueId;

    /**
     * 邀请人ID
     */
    private Long inviteUserId;

    /**
     * 微信用户 unionId，同一微信开放平台主体下共享
     */
    private String unionId;

    /**
     * 头像地址
     */
    private String avatarUrl;

    /**
     * 背景图像地址
     */
    private String backUrl;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 性别 0:未知，1:男性，2:女性
     */
    private Integer gender;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 生日，精确到时间由客户端或业务侧决定
     */
    private LocalDateTime birthday;

    /**
     * 国家
     */
    private String country;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 简介
     */
    private String remark;

    /**
     * 是否禁用：0否，1是
     */
    private Integer isDisable;

    /**
     * 是否注销：0否，1是
     */
    private Integer isDestroy;

    /**
     * 是否虚拟用户：0否，1是
     */
    private Integer isFake;

    /**
     * 是否已同步到 OpenIM，用于避免重复注册 IM 用户
     */
    private Boolean imRegistered;

    /**
     * 认证学校编码，来自学校认证流程
     */
    private String authSchoolCode;

    /**
     * 认证学校时间
     */
    private LocalDateTime authSchoolTime;

    /**
     * 授权手机号时间
     */
    private LocalDateTime authPhoneTime;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最新使用时间
     */
    private LocalDateTime newUsageTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;
}
