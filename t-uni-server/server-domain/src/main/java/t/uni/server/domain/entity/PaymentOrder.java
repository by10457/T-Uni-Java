package t.uni.server.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付订单实体
 */
@Data
@TableName("payment_order")
public class PaymentOrder implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 微信openid
     */
    private String openid;

    /**
     * 金额（元）
     */
    private BigDecimal amount;

    /**
     * 状态 0-待支付 1-已支付 2-已退款 3-已关闭
     */
    private Integer status;

    /**
     * 交易类型 JSAPI
     */
    private String tradeType;

    /**
     * 微信订单号
     */
    private String transactionId;

    /**
     * 预支付ID
     */
    private String prepayId;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 回调时间
     */
    private LocalDateTime notifyTime;

    /**
     * 退款单号
     */
    private String refundNo;

    /**
     * 退款时间
     */
    private LocalDateTime refundTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 逻辑删除 0-未删除 1-已删除
     */
    @TableLogic
    private Integer isDeleted;
}
