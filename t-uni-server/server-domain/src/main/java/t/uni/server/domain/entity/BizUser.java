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
 * <p>
 * 这是可直接运行的最小业务用户表实现。真实业务可替换为自己的用户实体，
 * 但需要继续实现 {@link IBusinessUser} 并保持与 {@code core_user.id} 一对一关联。
 * </p>
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
     * 用户唯一ID，对外展示用
     */
    private String uniqueId;

    /**
     * 微信小程序 openid，{@code MA_OPEN_ID} 登录策略下用于定位用户
     */
    private String maOpenId;

    /**
     * 微信公众号 openid，公众号接入场景预留
     */
    private String mpOpenId;

    /**
     * 微信用户 unionId，{@code UNION_ID} 登录策略下用于定位用户
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
