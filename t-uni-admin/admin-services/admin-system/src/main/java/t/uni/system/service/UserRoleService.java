package t.uni.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import t.uni.domain.system.dto.user.AssignRolesToUsersDto;
import t.uni.domain.system.entity.UserRole;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @since 2024-09-26
 */
public interface UserRoleService extends IService<UserRole> {

    /**
     * * 为用户分配角色
     *
     * @param dto 用户分配角色
     */
    void addUserRole(AssignRolesToUsersDto dto);

    /**
     * * 根据用户id获取角色列表
     *
     * @param userId 用户id
     * @return 角色列表
     */
    List<String> getRoleListByUserId(Long userId);
}
