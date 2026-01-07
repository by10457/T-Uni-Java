package t.uni.domain.common.model.dto.security;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import t.uni.domain.common.model.vo.LoginVo;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "TokenInfo", title = "TokenInfo")
public class TokenInfo {

    @Schema(name = "token", title = "令牌")
    private String token;

    @Schema(name = "username", title = "用户名")
    private String username;

    @Schema(name = "userId", title = "用户id")
    private Long userId;

    @Schema(name = "loginVo", title = "登录成功返回内容")
    private LoginVo loginVo;

}
