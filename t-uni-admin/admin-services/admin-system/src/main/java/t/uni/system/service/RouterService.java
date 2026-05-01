package t.uni.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import t.uni.domain.system.dto.RouterDto;
import t.uni.domain.system.entity.router.Router;
import t.uni.domain.system.vo.router.RouterManageVo;
import t.uni.domain.system.vo.router.WebUserRouterVo;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @since 2024-09-27
 */
public interface RouterService extends IService<Router> {
    /**
     * * 获取路由内容
     *
     * @return 路遇列表
     */
    List<WebUserRouterVo> routerAsync();

    /**
     * 获取 管理端当前用户权限码。
     *
     * @return 权限码列表
     */
    List<String> adminPortalCodes();

    /**
     * 获取 管理端动态路由。
     *
     * @return 动态路由树
     */
    List<Map<String, Object>> adminPortalAllMenus();

    /**
     * 获取 管理端菜单管理树。
     *
     * @return 菜单树
     */
    List<Map<String, Object>> adminPortalMenuList();

    boolean adminPortalMenuNameExists(String name, String id);

    boolean adminPortalMenuPathExists(String path, String id);

    void createAdminPortalMenu(Map<String, Object> request);

    void updateAdminPortalMenu(String id, Map<String, Object> request);

    void deleteAdminPortalMenu(String id);

    /**
     * * 管理菜单列表
     *
     * @return 系统菜单表
     */
    List<RouterManageVo> routerList();

    /**
     * * 添加路由菜单
     *
     * @param dto 添加菜单表单
     */
    void createRouter(RouterDto dto);

    /**
     * * 更新路由菜单
     *
     * @param dto 更新表单
     */
    void updateRouter(RouterDto dto);

    /**
     * * 删除路由菜单
     *
     * @param ids 删除id列表
     */
    void deletedRouterByIds(List<Long> ids);
}
