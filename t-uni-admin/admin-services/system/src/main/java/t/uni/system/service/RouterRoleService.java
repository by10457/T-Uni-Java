package t.uni.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import t.uni.domain.system.entity.RouterRole;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @since 2024-09-26
 */
public interface RouterRoleService extends IService<RouterRole> {

    /**
     * * 根据路由id获取所有角色
     *
     * @param routerId 路由id
     * @return 角色列表
     */
    List<String> getRoleListByRouterId(Long routerId);

    /**
     * 清除选中菜单所有角色
     *
     * @param routerIds 路由id
     */
    void clearRouterRole(List<Long> routerIds);
}
