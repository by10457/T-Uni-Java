package t.uni.server.payment.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 业务锁单结果。
 *
 * <p>handler 返回给支付模块的可支付订单信息。支付模块会用 {@code bizType + bizOrderNo} 做支付单幂等。</p>
 */
@Data
@Builder
public class PaymentBizLockedOrder {

    /** 业务类型；为空时沿用请求业务类型。 */
    private String bizType;

    /** 业务订单号，业务侧必须保证稳定。 */
    private String bizOrderNo;

    /** 微信账单描述。 */
    private String description;

    /** 应付金额，单位分，必须大于0。 */
    private Integer totalFeeFen;

    /** 透传数据，支付成功后回传业务侧。 */
    private String attachData;

    /** 支付单过期时间；为空时支付模块使用默认有效期。 */
    private LocalDateTime expireTime;
}
