package t.uni.server.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import t.uni.server.domain.auth.IBusinessUser;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 模板默认业务用户实体。
 */
@Data
@TableName("biz_user")
public class BizUser implements IBusinessUser, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID（主键，与 core_user 主键一致，需手动设置）
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
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
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;
}
