package t.uni.server.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import t.uni.common.core.result.Result;
import t.uni.server.common.context.UserContext;
import t.uni.server.payment.dto.PaymentLockDTO;
import t.uni.server.payment.dto.PaymentPrepayDTO;
import t.uni.server.payment.service.PaymentOrderService;
import t.uni.server.payment.vo.PaymentOrderVO;
import t.uni.server.payment.vo.PaymentPrepayVO;

/**
 * 小程序端支付接口。
 *
 * <p>只暴露当前用户维度的支付操作；用户身份从 {@link UserContext} 获取，不信任客户端传入用户ID。</p>
 */
@Tag(name = "微信支付", description = "微信小程序 JSAPI 支付接口")
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentOrderService paymentOrderService;

    /**
     * 锁定业务订单并发起微信预下单。
     *
     * @param dto 业务锁单参数
     * @return 支付单和 JSAPI 支付参数
     */
    @Operation(summary = "锁单并预下单")
    @PostMapping("/wechat/lock")
    public Result<PaymentPrepayVO> lockAndPrepay(@Valid @RequestBody PaymentLockDTO dto) {
        return Result.success(paymentOrderService.lockAndPrepay(UserContext.getUserId(), dto));
    }

    /**
     * 对已有未过期支付单重新获取 JSAPI 支付参数。
     *
     * @param dto 支付单号
     * @return 支付单和 JSAPI 支付参数
     */
    @Operation(summary = "重新获取支付参数")
    @PostMapping("/wechat/prepay")
    public Result<PaymentPrepayVO> reprepay(@Valid @RequestBody PaymentPrepayDTO dto) {
        return Result.success(paymentOrderService.reprepay(UserContext.getUserId(), dto));
    }

    /**
     * 查询当前用户支付单状态。
     *
     * @param orderNo 支付单号
     * @return 支付单视图
     */
    @Operation(summary = "查询支付单")
    @GetMapping("/orders/{orderNo}")
    public Result<PaymentOrderVO> queryOrder(@PathVariable String orderNo) {
        return Result.success(paymentOrderService.queryOrder(UserContext.getUserId(), orderNo));
    }
}
