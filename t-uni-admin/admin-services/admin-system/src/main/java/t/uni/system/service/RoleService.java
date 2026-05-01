package t.uni.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import t.uni.common.core.result.PageResult;
import t.uni.domain.system.dto.RoleDto;
import t.uni.domain.system.entity.Role;
import t.uni.domain.system.vo.RoleVo;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 角色 服务类
 * </p>
 *
 * @since 2024-10-03 14:26:24
 */
public interface RoleService extends IService<Role> {

    /**
     * * 获取角色列表
     *
     * @return 角色返回列表
     */
    PageResult<RoleVo> getRolePage(Page<Role> pageParams, RoleDto dto);

    /**
     * 管理端角色分页列表。
     */
    Map<String, Object> adminPortalRoleList(long page, long pageSize, Map<String, Object> query);

    void createAdminPortalRole(Map<String, Object> request);

    void updateAdminPortalRole(String id, Map<String, Object> request);

    void deleteAdminPortalRole(String id);

    /**
     * * 添加角色
     *
     * @param dto 添加表单
     */
    void addRole(RoleDto dto);

    /**
     * * 更新角色
     *
     * @param dto 更新表单
     */
    void updateRole(RoleDto dto);

    /**
     * * 删除|批量删除角色类型
     *
     * @param ids 删除id列表
     */
    void deleteRole(List<Long> ids);

    /**
     * * 获取所有角色
     *
     * @return 所有角色列表
     */
    List<RoleVo> roleList();

    /**
     * 使用Excel导出导出角色列表
     *
     * @return Excel
     */
    ResponseEntity<byte[]> exportByExcel();

    /**
     * 使用Excel更新角色列表
     *
     * @param file Excel文件
     */
    void updateRoleByFile(MultipartFile file);
}
