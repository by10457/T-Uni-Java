package t.uni.server.payment.spi;

import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Component;
import t.uni.common.core.exception.BaseException;
import t.uni.server.payment.constant.PaymentResultCodeEnum;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付业务处理器注册表。
 *
 * <p>按业务类型路由到对应 {@link PaymentBizHandler}。启动期检查空类型和重复类型，避免支付请求运行时路由错业务。</p>
 */
@Component
public class PaymentBizHandlerRegistry {

    private final Map<String, PaymentBizHandler> handlerMap;

    /**
     * 收集所有业务支付处理器并按业务类型建索引。
     *
     * @param handlers Spring 容器中的支付业务处理器
     */
    public PaymentBizHandlerRegistry(List<PaymentBizHandler> handlers) {
        this.handlerMap = new HashMap<>();
        for (var handler : handlers) {
            var bizType = handler.getBizType();
            if (StrUtil.isBlank(bizType)) {
                throw new IllegalStateException("PaymentBizHandler bizType must not be blank: " + handler.getClass().getName());
            }
            var old = handlerMap.putIfAbsent(bizType, handler);
            if (old != null) {
                throw new IllegalStateException("Duplicate PaymentBizHandler bizType: " + bizType);
            }
        }
    }

    /**
     * 获取指定业务类型的处理器。
     *
     * @param bizType 业务类型
     * @return 业务处理器
     */
    public PaymentBizHandler getRequiredHandler(String bizType) {
        var handler = handlerMap.get(bizType);
        if (handler == null) {
            throw paymentException(PaymentResultCodeEnum.PAYMENT_BIZ_HANDLER_MISSING);
        }
        return handler;
    }

    private BaseException paymentException(PaymentResultCodeEnum codeEnum) {
        return new BaseException(codeEnum.getCode(), codeEnum.getMessage());
    }
}
