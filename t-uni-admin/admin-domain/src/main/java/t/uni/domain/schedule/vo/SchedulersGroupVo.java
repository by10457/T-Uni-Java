package t.uni.domain.schedule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import t.uni.domain.common.model.vo.BaseUserVo;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "SchedulersGroupVo", title = "任务调度分组返回对象", description = "任务调度分组返回对象")
public class SchedulersGroupVo extends BaseUserVo {

    @Schema(name = "groupName", title = "分组名称")
    private String groupName;

    @Schema(name = "description", title = "分组详情")
    private String description;

}