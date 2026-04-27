package t.uni.server.im.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OpenIM Webhook 响应 VO
 * <p>
 * 字段名和语义按 OpenIM Webhook 协议保留，Controller 通过 actionCode 控制放行或拦截。
 *
 * @author t-uni
 * @since 2026-04-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OpenIM Webhook 响应")
public class OpenImWebhookResponse {

    /** OpenIM 行为码：0 放行，1 拦截 */
    @Schema(description = "actionCode：0=放行，1=拦截")
    private Integer actionCode;

    /** 拦截时返回的业务错误码 */
    @Schema(description = "errCode")
    private Integer errCode;

    /** 拦截时返回的错误摘要 */
    @Schema(description = "errMsg")
    private String errMsg;

    /** 拦截时返回的错误详情 */
    @Schema(description = "errDlt")
    private String errDlt;

    /** OpenIM 下一步处理码，模板实现中放行取 0、拦截取 1 */
    @Schema(description = "nextCode")
    private Integer nextCode;

    /**
     * 构造 OpenIM 放行响应。
     */
    public static OpenImWebhookResponse allow() {
        return new OpenImWebhookResponse(0, 0, "", "", 0);
    }

    /**
     * 构造 OpenIM 拦截响应。
     *
     * @param errCode 业务错误码
     * @param errMsg 错误摘要
     * @param errDlt 错误详情
     */
    public static OpenImWebhookResponse reject(int errCode, String errMsg, String errDlt) {
        return new OpenImWebhookResponse(1, errCode, errMsg, errDlt, 1);
    }
}
