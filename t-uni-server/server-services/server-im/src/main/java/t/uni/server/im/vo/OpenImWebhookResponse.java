package t.uni.server.im.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OpenIM Webhook 响应 VO
 *
 * @author t-uni
 * @since 2026-04-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OpenIM Webhook 响应")
public class OpenImWebhookResponse {

    @Schema(description = "actionCode：0=放行，1=拦截")
    private Integer actionCode;

    @Schema(description = "errCode")
    private Integer errCode;

    @Schema(description = "errMsg")
    private String errMsg;

    @Schema(description = "errDlt")
    private String errDlt;

    @Schema(description = "nextCode")
    private Integer nextCode;

    public static OpenImWebhookResponse allow() {
        return new OpenImWebhookResponse(0, 0, "", "", 0);
    }

    public static OpenImWebhookResponse reject(int errCode, String errMsg, String errDlt) {
        return new OpenImWebhookResponse(1, errCode, errMsg, errDlt, 1);
    }
}
