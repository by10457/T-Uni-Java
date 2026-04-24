package t.uni.server.im.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IM Token 响应 VO
 *
 * @author t-uni
 * @since 2026-04-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "IM Token 响应")
public class ImTokenVO {

    @Schema(description = "OpenIM userID")
    private String userId;

    @Schema(description = "OpenIM user token")
    private String token;

    @Schema(description = "token 过期秒数")
    private Long expireTimeSeconds;

    @Schema(description = "平台ID")
    private Integer platformId;

    @Schema(description = "OpenIM REST API 地址")
    private String apiAddr;

    @Schema(description = "OpenIM WebSocket 地址")
    private String wsAddr;
}
