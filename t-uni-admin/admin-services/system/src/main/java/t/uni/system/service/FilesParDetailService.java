package t.uni.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import t.uni.common.core.result.PageResult;
import t.uni.domain.system.dto.FilesParDetailDto;
import t.uni.domain.system.entity.FilesParDetail;
import t.uni.domain.system.vo.FilesParDetailVo;

import java.util.List;

/**
 * <p>
 * 文件分片信息表，仅在手动分片上传时使用 服务类
 * </p>
 *
 * @since 2025-05-08 23:01:19
 */
public interface FilesParDetailService extends IService<FilesParDetail> {

    /**
     * 分页查询文件分片信息表，仅在手动分片上传时使用
     *
     * @return {@link FilesParDetailVo}
     */
    PageResult<FilesParDetailVo> getFilesParDetailPage(Page<FilesParDetail> pageParams, FilesParDetailDto dto);

    /**
     * 添加文件分片信息表，仅在手动分片上传时使用
     *
     * @param dto 添加表单
     */
    void createFilesParDetail(FilesParDetailDto dto);

    /**
     * 更新文件分片信息表，仅在手动分片上传时使用
     *
     * @param dto {@link FilesParDetailDto}
     */
    void updateFilesParDetail(FilesParDetailDto dto);

    /**
     * 删除|批量删除文件分片信息表，仅在手动分片上传时使用类型
     *
     * @param ids 删除id列表
     */
    void deleteFilesParDetail(List<Long> ids);
}
