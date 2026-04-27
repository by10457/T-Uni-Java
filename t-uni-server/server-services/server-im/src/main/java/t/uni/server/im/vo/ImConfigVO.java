package t.uni.server.im.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IM 公共配置 VO
 * <p>
 * 只包含客户端连接 OpenIM 所需的公开字段，不包含管理员密钥和 Webhook token。
 *
 * @author t-uni
 * @since 2026-04-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "IM 公共配置")
public class ImConfigVO {

    /** OpenIM REST API 公网或内网访问地址 */
    @Schema(description = "OpenIM REST API 地址")
    private String apiAddr;

    /** OpenIM WebSocket 连接地址 */
    @Schema(description = "OpenIM WebSocket 地址")
    private String wsAddr;

    /** 系统通知发送者 OpenIM userID，客户端可用于识别系统消息 */
    @Schema(description = "系统通知发送者 userID")
    private String systemNoticeUserId;
}
