package t.uni.server.im.support;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OpenIM API 通用响应
 *
 * @author t-uni
 * @since 2026-04-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenImApiResponse {

    private Integer errCode;
    private String errMsg;
    private String errDlt;
    private Object data;

    public boolean isSuccess() {
        return errCode != null && errCode == 0;
    }
}
