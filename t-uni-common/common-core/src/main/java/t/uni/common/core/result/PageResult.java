package t.uni.common.core.result;

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
    /**
     * 当前页
     */
    private Long pageNo;
    /**
     * 每页大小
     */
    private Long pageSize;
    /**
     * 总记录数
     */
    private Long total;
    /**
     * 数据列表
     */
    private List<T> list;
}
