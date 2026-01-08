package t.uni.server.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 新用户默认头像池实体
 */
@Data
@TableName("core_user_default_avatar")
public class CoreUserDefaultAvatar implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private Long id;

    /**
     * 默认头像URL
     */
    private String avatarUrl;

    /**
     * 是否启用：0否 1是
     */
    private Integer isEnable;

    /**
     * 权重（用于随机/加权抽取，越大概率越高）
     */
    private Integer weight;

    /**
     * 排序（用于固定取值时）
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;
}
