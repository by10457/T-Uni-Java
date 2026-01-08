package t.uni.server.domain.vo.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户信息响应 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户信息响应")
public class UserInfoVO {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 用户唯一ID（对外展示）
     */
    @Schema(description = "用户唯一ID")
    private String uniqueId;

    /**
     * 昵称
     */
    @Schema(description = "昵称")
    private String nickname;

    /**
     * 头像URL
     */
    @Schema(description = "头像URL")
    private String avatar;

    /**
     * 性别 0:未知，1:男性，2:女性
     */
    @Schema(description = "性别 0:未知，1:男性，2:女性")
    private Integer gender;

    /**
     * 是否新用户
     */
    @Schema(description = "是否新用户")
    private Boolean isNewUser;
}
