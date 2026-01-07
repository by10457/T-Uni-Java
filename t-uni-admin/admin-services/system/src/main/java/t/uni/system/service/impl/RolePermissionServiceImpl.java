package t.uni.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import t.uni.domain.system.dto.AssignPowersToRoleDto;
import t.uni.domain.system.entity.AdminUser;
import t.uni.domain.system.entity.RolePermission;
import t.uni.domain.system.entity.UserRole;
import t.uni.system.core.event.UpdateUserinfoByRoleIdsEvent;
import t.uni.system.mapper.RolePermissionMapper;
import t.uni.system.mapper.UserMapper;
import t.uni.system.mapper.UserRoleMapper;
import t.uni.system.service.RolePermissionService;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @since 2024-09-26
 */
@Service
@Transactional
public class RolePermissionServiceImpl extends ServiceImpl<RolePermissionMapper, RolePermission> implements RolePermissionService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * * 根据角色id获取权限内容
     *
     * @param id 角色id
     * @return 已选择的权限列表
     */
    @Override
    public List<String> getPermissionListByRoleId(Long id) {
        List<RolePermission> rolePermissionList = baseMapper.selectRolePermissionListByRoleId(id);
        return rolePermissionList.stream()
                .map(rolePermission -> rolePermission.getPowerId().toString())
                .toList();
    }

    /**
     * 为角色分配权限
     *
     * @param dto 为角色分配权限表单
     */
    @Override
    public void saveRolPermission(AssignPowersToRoleDto dto) {
        List<Long> powerIds = dto.getPowerIds();
        Long roleId = dto.getRoleId();

        // 删除这个角色下所有权限
        List<Long> ids = List.of(roleId);
        baseMapper.deleteBatchRoleIds(ids);

        // 保存分配数据
        List<RolePermission> rolePermissionList = powerIds.stream().map(powerId -> {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPowerId(powerId);
            return rolePermission;
        }).toList();
        saveBatch(rolePermissionList);

        // 找到所有和当前更新角色相同的用户
        List<Long> roleIds = userRoleMapper.selectList(Wrappers.<UserRole>lambdaQuery().eq(UserRole::getRoleId, roleId))
                .stream().map(UserRole::getUserId).toList();

        // 根据Id查找所有用户
        List<AdminUser> adminUsers = userMapper.selectList(Wrappers.<AdminUser>lambdaQuery().in(!roleIds.isEmpty(), AdminUser::getId, roleIds));

        // 用户为空时不更新Redis的key
        if (adminUsers.isEmpty()) return;

        // 更新角色绑定的用户
        applicationEventPublisher.publishEvent(new UpdateUserinfoByRoleIdsEvent(this, ids));
    }
}
