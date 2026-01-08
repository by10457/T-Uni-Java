package t.uni.server.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 社交用户实体
 * 与 core_user 通过主键 id 一对一关联
 */
@Data
@TableName("social_user")
public class SocialUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID（主键，与 core_user 主键一致，需手动设置）
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    /**
     * 用户唯一ID
     */
    private String uniqueId;

    /**
     * 微信小程序 openid
     */
    private String maOpenId;

    /**
     * 微信公众号 openid
     */
    private String mpOpenId;

    /**
     * 微信用户 unionId
     */
    private String unionId;

    /**
     * 当前是否关注（0 没关注 1 已关注）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    /**
     * 取消关注时间
     */
    private LocalDateTime cancelTime;
}
