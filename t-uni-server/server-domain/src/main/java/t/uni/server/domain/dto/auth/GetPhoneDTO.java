package t.uni.server.domain.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 获取微信绑定手机号请求 DTO。
 * <p>
 * code 来自微信小程序手机号授权组件，只用于向微信服务端换取手机号。
 * </p>
 */
@Data
@Schema(description = "获取手机号请求")
public class GetPhoneDTO {

    /**
     * 手机号验证 code，一次性凭证
     */
    @NotBlank(message = "code不能为空")
    @Schema(description = "手机号验证code", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;
}
