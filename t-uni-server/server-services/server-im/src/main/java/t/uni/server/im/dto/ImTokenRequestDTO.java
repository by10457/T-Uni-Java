package t.uni.server.im.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * IM Token 请求 DTO
 *
 * @author t-uni
 * @since 2026-04-24
 */
@Data
@Schema(description = "IM Token 请求")
public class ImTokenRequestDTO {

    @NotNull(message = "platformId不能为空")
    @Schema(description = "平台ID（OpenIM 平台枚举）", example = "6")
    private Integer platformId;
}
