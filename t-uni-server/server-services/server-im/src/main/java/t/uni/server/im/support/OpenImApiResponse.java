package t.uni.server.im.support;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OpenIM API 通用响应
 * <p>
 * 保留 OpenIM 原始 errCode、errMsg、errDlt 和 data，便于上层按具体错误码做幂等、
 * 重试或业务异常映射。
 *
 * @author t-uni
 * @since 2026-04-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenImApiResponse {

    /** OpenIM 错误码，0 表示成功 */
    private Integer errCode;

    /** OpenIM 错误摘要 */
    private String errMsg;

    /** OpenIM 错误详情 */
    private String errDlt;

    /** OpenIM data 字段，结构随接口变化 */
    private Object data;

    /**
     * 判断 OpenIM 响应是否成功。
     *
     * @return true 表示 errCode 为 0
     */
    public boolean isSuccess() {
        return errCode != null && errCode == 0;
    }
}
