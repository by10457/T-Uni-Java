package t.uni.server.payment.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * 业务锁单入参。
 *
 * <p>由支付模块传给业务 handler。支付模块不解析 {@code bizContent}，只负责透传。</p>
 */
@Data
@Builder
public class PaymentBizLockRequest {

    /** 当前登录用户ID。 */
    private Long userId;

    /** 业务类型，用于选择 handler。 */
    private String bizType;

    /** 业务锁单参数，由具体 handler 解释。 */
    private Map<String, Object> bizContent;
}
