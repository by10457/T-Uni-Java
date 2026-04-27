package t.uni.server.payment.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 支付单关闭业务事件。
 *
 * <p>只在支付模块确认微信未支付或已关闭后发出，业务侧可释放库存、名额等锁定资源。</p>
 */
@Data
@Builder
public class PaymentClosedEvent {

    /** 下单用户ID。 */
    private Long userId;

    /** 业务类型。 */
    private String bizType;

    /** 业务订单号。 */
    private String bizOrderNo;

    /** 商户支付单号。 */
    private String orderNo;

    /** 本地关单时间。 */
    private LocalDateTime closeTime;
}
