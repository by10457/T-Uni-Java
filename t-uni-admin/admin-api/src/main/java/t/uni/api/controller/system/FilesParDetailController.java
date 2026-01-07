package t.uni.api.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import t.uni.common.core.result.PageResult;
import t.uni.common.core.result.Result;
import t.uni.common.core.result.ResultCodeEnum;
import t.uni.core.annotation.PermissionTag;
import t.uni.domain.system.dto.FilesParDetailDto;
import t.uni.domain.system.entity.FilesParDetail;
import t.uni.domain.system.vo.FilesParDetailVo;
import t.uni.system.service.FilesParDetailService;

import java.util.List;

/**
 * <p>
 * 文件分片信息表，仅在手动分片上传时使用 前端控制器
 * </p>
 *
 * @since 2025-05-08 23:01:19
 */
@Tag(name = "文件分片信息表，仅在手动分片上传时使用", description = "文件分片信息表，仅在手动分片上传时使用相关接口")
@PermissionTag(permission = "filesParDetail:*")
@RestController
@RequestMapping("/api/files-par-detail")
public class FilesParDetailController {

    @Resource
    private FilesParDetailService filesPardetailService;

    @Operation(summary = "分页查询文件分片信息表，仅在手动分片上传时使用", description = "分页文件分片信息表，仅在手动分片上传时使用")
    @PermissionTag(permission = "filesParDetail:query")
    @GetMapping("{page}/{limit}")
    public Result<PageResult<FilesParDetailVo>> getFilesParDetailPage(
            @PathVariable @Parameter(name = "page", description = "当前页", required = true) Integer page,
            @PathVariable @Parameter(name = "limit", description = "每页记录数", required = true) Integer limit,
            FilesParDetailDto dto) {
        Page<FilesParDetail> pageParams = new Page<>(page, limit);
        PageResult<FilesParDetailVo> pageResult = filesPardetailService.getFilesParDetailPage(pageParams, dto);
        return Result.success(pageResult);
    }

    @Operation(summary = "添加文件分片信息表，仅在手动分片上传时使用", description = "添加文件分片信息表，仅在手动分片上传时使用")
    @PermissionTag(permission = "filesParDetail:add")
    @PostMapping()
    public Result<String> createFilesParDetail(@Valid @RequestBody FilesParDetailDto dto) {
        filesPardetailService.createFilesParDetail(dto);
        return Result.success(ResultCodeEnum.CREATE_SUCCESS);
    }

    @Operation(summary = "更新文件分片信息表，仅在手动分片上传时使用", description = "更新文件分片信息表，仅在手动分片上传时使用")
    @PermissionTag(permission = "filesParDetail:update")
    @PutMapping()
    public Result<String> updateFilesParDetail(@Valid @RequestBody FilesParDetailDto dto) {
        filesPardetailService.updateFilesParDetail(dto);
        return Result.success(ResultCodeEnum.UPDATE_SUCCESS);
    }

    @Operation(summary = "删除文件分片信息表，仅在手动分片上传时使用", description = "删除文件分片信息表，仅在手动分片上传时使用")
    @PermissionTag(permission = "filesParDetail:delete")
    @DeleteMapping()
    public Result<String> deleteFilesParDetail(@RequestBody List<Long> ids) {
        filesPardetailService.deleteFilesParDetail(ids);
        return Result.success(ResultCodeEnum.DELETE_SUCCESS);
    }
}