package t.uni.server.im.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IM Token 响应 VO
 * <p>
 * 返回给客户端用于登录 OpenIM SDK 的最小凭据和连接地址。
 *
 * @author t-uni
 * @since 2026-04-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "IM Token 响应")
public class ImTokenVO {

    /** OpenIM userID，不是本地用户 ID */
    @Schema(description = "OpenIM userID")
    private String userId;

    /** OpenIM user token，客户端只用于登录 SDK */
    @Schema(description = "OpenIM user token")
    private String token;

    /** token 有效期秒数，来源于 OpenIM */
    @Schema(description = "token 过期秒数")
    private Long expireTimeSeconds;

    /** 本次签发 token 对应的 OpenIM 平台枚举值 */
    @Schema(description = "平台ID")
    private Integer platformId;

    /** OpenIM REST API 地址 */
    @Schema(description = "OpenIM REST API 地址")
    private String apiAddr;

    /** OpenIM WebSocket 地址 */
    @Schema(description = "OpenIM WebSocket 地址")
    private String wsAddr;
}
