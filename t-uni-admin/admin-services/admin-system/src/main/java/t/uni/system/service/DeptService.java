package t.uni.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import t.uni.common.core.result.PageResult;
import t.uni.domain.system.dto.DeptDto;
import t.uni.domain.system.entity.Dept;
import t.uni.domain.system.vo.DeptVo;

import java.util.List;

/**
 * <p>
 * 部门 服务类
 * </p>
 *
 * @since 2024-10-04 10:39:08
 */
public interface DeptService extends IService<Dept> {

    /**
     * * 获取部门列表
     *
     * @return 部门返回列表
     */
    PageResult<DeptVo> getDeptPage(Page<Dept> pageParams, DeptDto dto);

    /**
     * * 添加部门
     *
     * @param dto 添加表单
     */
    void createDept(DeptDto dto);

    /**
     * * 更新部门
     *
     * @param dto 更新表单
     */
    void updateDept(DeptDto dto);

    /**
     * * 删除|批量删除部门类型
     *
     * @param ids 删除id列表
     */
    void deleteDept(List<Long> ids);

    /**
     * * 获取所有部门
     *
     * @return 所有部门列表
     */
    List<DeptVo> getDeptPage();
}
