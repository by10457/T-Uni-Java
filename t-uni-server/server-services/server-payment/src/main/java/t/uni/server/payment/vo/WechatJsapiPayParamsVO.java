package t.uni.server.payment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 小程序调起微信收银台参数。
 */
@Data
@Builder
@Schema(description = "小程序调起微信收银台参数")
public class WechatJsapiPayParamsVO {

    /** 小程序 AppID。 */
    @Schema(description = "小程序 AppID")
    private String appId;

    /** 签名时间戳，字符串格式传给 wx.requestPayment。 */
    @Schema(description = "时间戳")
    private String timeStamp;

    /** 签名随机串。 */
    @Schema(description = "随机串")
    private String nonceStr;

    /** wx.requestPayment 的 package 参数，格式为 prepay_id=xxx。 */
    @Schema(description = "prepay_id 包")
    private String packageValue;

    /** 微信 JSAPI 支付签名类型，当前为 RSA。 */
    @Schema(description = "签名类型")
    private String signType;

    /** 微信 JSAPI 支付签名。 */
    @Schema(description = "支付签名")
    private String paySign;

    /** 微信预支付ID，便于前端或日志排查。 */
    @Schema(description = "微信预支付ID")
    private String prepayId;
}
