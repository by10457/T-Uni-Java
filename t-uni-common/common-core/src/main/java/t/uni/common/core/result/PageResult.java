package t.uni.common.core.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页查询统一响应模型。
 * <p>
 * 只描述当前页数据和分页元信息，不负责执行分页查询或转换 MyBatis 分页对象。
 * </p>
 *
 * @param <T> 列表元素类型
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

    /**
     * 根据分页查询结果组装统一分页响应。
     *
     * @param list     当前页数据
     * @param total    总记录数
     * @param pageNo   当前页码
     * @param pageSize 每页大小
     * @param <T>      列表元素类型
     * @return 分页响应
     */
    public static <T> PageResult<T> of(List<T> list, Long total, int pageNo, int pageSize) {
        PageResult<T> result = new PageResult<>();
        result.setList(list);
        result.setTotal(total);
        result.setPageNo((long) pageNo);
        result.setPageSize((long) pageSize);
        return result;
    }
}
