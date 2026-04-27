package t.uni.server.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 新用户默认昵称池实体。
 * <p>
 * 用于注册初始化时为缺少昵称的用户提供候选值。
 * </p>
 */
@Data
@TableName("core_user_default_nick_name")
public class CoreUserDefaultNickName implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 默认昵称
     */
    private String nickName;

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
