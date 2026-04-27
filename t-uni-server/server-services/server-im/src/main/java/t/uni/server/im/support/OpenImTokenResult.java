package t.uni.server.im.support;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OpenIM Token 解析结果
 * <p>
 * 用于隔离 OpenIM 响应 data 字段解析，避免服务层直接依赖 JSON 字段访问结果。
 *
 * @author t-uni
 * @since 2026-04-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenImTokenResult {

    /** OpenIM user token */
    private String token;

    /** token 有效期秒数，来源于 OpenIM 响应 */
    private Long expireTimeSeconds;
}
