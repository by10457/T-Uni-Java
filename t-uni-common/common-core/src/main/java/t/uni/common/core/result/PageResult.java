package t.uni.common.core.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页返回结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageResult<T> implements Serializable {

    @Schema(description = "* 当前页")
    private Long pageNo;

    @Schema(description = "* 每页大小")
    private Long pageSize;

    @Schema(description = "* 总记录数")
    private Long total;

    @Schema(description = "* 数据列表")
    private List<T> list;

    public static <T> PageResult<T> of(List<T> list, Long total, int pageNo, int pageSize) {
        PageResult<T> result = new PageResult<>();
        result.setList(list);
        result.setTotal(total);
        result.setPageNo((long) pageNo);
        result.setPageSize((long) pageSize);
        return result;
    }
}
