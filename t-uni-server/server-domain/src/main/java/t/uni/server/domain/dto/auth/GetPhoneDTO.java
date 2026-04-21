package t.uni.server.domain.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 获取手机号DTO
 */
@Data
@Schema(description = "获取手机号请求")
public class GetPhoneDTO {

    /**
     * 手机号验证code
     */
    @NotBlank(message = "code不能为空")
    @Schema(description = "手机号验证code", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;
}
