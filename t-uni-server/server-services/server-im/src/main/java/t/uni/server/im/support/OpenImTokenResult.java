package t.uni.server.im.support;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OpenIM Token 解析结果
 *
 * @author t-uni
 * @since 2026-04-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenImTokenResult {

    private String token;
    private Long expireTimeSeconds;
}
