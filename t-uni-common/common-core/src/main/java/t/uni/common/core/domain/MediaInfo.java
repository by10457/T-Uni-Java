package t.uni.common.core.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 媒体资源基础信息。
 * <p>
 * 用于接口返回图片、视频、实况图片等资源的展示元数据。
 * url 可以是外链或存储层解析后的访问地址，类本身不负责鉴权签名和链接续期。
 * </p>
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
     * 创建媒体资源信息。
     *
     * @param type   媒体类型
     * @param url    媒体访问地址
     * @param width  宽度，单位像素
     * @param height 高度，单位像素
     */
    public MediaInfo(String type, String url, Integer width, Integer height) {
        this.type = type;
        this.url = url;
        this.width = width;
        this.height = height;
    }
}
