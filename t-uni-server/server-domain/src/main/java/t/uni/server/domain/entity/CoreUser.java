package t.uni.server.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 核心用户实体
 * 与业务用户表通过主键 id 一对一关联
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
     * 用户唯一ID（对外展示）
     */
    private String uniqueId;

    /**
     * 邀请人ID
     */
    private Long inviteUserId;

    /**
     * 微信用户 unionId
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
     * 生日
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
     * 认证学校编码
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
