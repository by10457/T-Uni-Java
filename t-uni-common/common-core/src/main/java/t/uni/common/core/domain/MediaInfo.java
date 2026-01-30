package t.uni.common.core.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 媒体信息
 */
@Data
@NoArgsConstructor
@Schema(description = "* 媒体信息")
public class MediaInfo {

    @Schema(description = "* 媒体类型：Image-图片，Video-视频，Live-实况图片")
    private String type;

    @Schema(description = "* 媒体URL")
    private String url;

    @Schema(description = "* 图片宽度")
    private Integer width;

    @Schema(description = "* 图片高度")
    private Integer height;

    /**
     * 全参构造函数
     */
    public MediaInfo(String type, String url, Integer width, Integer height) {
        this.type = type;
        this.url = url;
        this.width = width;
        this.height = height;
    }
}
