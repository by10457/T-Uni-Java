package t.uni.server.im.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IM 公共配置 VO
 *
 * @author t-uni
 * @since 2026-04-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "IM 公共配置")
public class ImConfigVO {

    @Schema(description = "OpenIM REST API 地址")
    private String apiAddr;

    @Schema(description = "OpenIM WebSocket 地址")
    private String wsAddr;

    @Schema(description = "系统通知发送者 userID")
    private String systemNoticeUserId;
}
