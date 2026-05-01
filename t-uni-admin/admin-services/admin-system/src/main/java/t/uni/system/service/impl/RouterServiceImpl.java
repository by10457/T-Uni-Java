package t.uni.system.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import t.uni.common.core.exception.BaseException;
import t.uni.common.core.result.ResultCodeEnum;
import t.uni.core.context.BaseContext;
import t.uni.domain.common.model.vo.LoginVo;
import t.uni.domain.system.entity.AdminUser;
import t.uni.domain.system.entity.Dept;
import t.uni.domain.system.entity.Role;
import t.uni.domain.system.entity.RouterRole;
import t.uni.domain.system.entity.UserDept;
import t.uni.domain.system.entity.UserRole;
import t.uni.domain.system.dto.RouterDto;
import t.uni.domain.system.entity.router.Router;
import t.uni.domain.system.entity.router.RouterMeta;
import t.uni.domain.system.entity.router.RouterMetaTransition;
import t.uni.domain.system.views.ViewRolePermission;
import t.uni.domain.system.views.ViewRouterRole;
import t.uni.domain.system.vo.router.RouterManageVo;
import t.uni.domain.system.vo.router.RouterVo;
import t.uni.domain.system.vo.router.WebUserRouterVo;
import t.uni.system.mapper.RolePermissionMapper;
import t.uni.system.mapper.RouterMapper;
import t.uni.system.mapper.RouterRoleMapper;
import t.uni.system.mapper.UserDeptMapper;
import t.uni.system.mapper.UserRoleMapper;
import t.uni.system.service.DeptService;
import t.uni.system.service.RoleService;
import t.uni.system.service.RouterService;
import t.uni.system.service.UserService;
import t.uni.system.utils.RoleHelper;
import t.uni.system.utils.RouterServiceHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @since 2024-09-27
 */
@Service
@Transactional
public class RouterServiceImpl extends ServiceImpl<RouterMapper, Router> implements RouterService {
    private static final String CACHE_NAMES = "router";

    private static final Long ADMIN_USER_ID = 1L;
    private static final Long ADMIN_ROLE_ID = 1L;
    private static final Long DEFAULT_DEPT_ID = 1842883239493881857L;

    private static final Long ROUTE_SYSTEM = 1L;
    private static final Long ROUTE_SYSTEM_MENU = 2L;
    private static final Long ROUTE_SYSTEM_ROLE = 1841726844983701505L;
    private static final Long ROUTE_SYSTEM_USER = 1841803086252548097L;
    private static final Long ROUTE_SYSTEM_DEPT = 1842033245832458241L;
    private static final Long ROUTE_SYSTEM_FILES = 1843932804747603970L;
    private static final Long ROUTE_DASHBOARD = 1955000000000000001L;
    private static final Long ROUTE_ANALYTICS = 1955000000000000002L;
    private static final Long ROUTE_WORKSPACE = 1955000000000000003L;
    private static final Long ROUTE_MONITOR = 1844644093987880962L;
    private static final Long ROUTE_MONITOR_SERVER = 1844644779039358978L;
    private static final Long ROUTE_MONITOR_LOGGED_IN = 1918937765434429441L;
    private static final Long ROUTE_MONITOR_CACHE_RETIRED = 1848989760243838978L;

    private static final Long ROUTE_SYSTEM_USER_CREATE = 1955000000000000201L;
    private static final Long ROUTE_SYSTEM_USER_EDIT = 1955000000000000202L;
    private static final Long ROUTE_SYSTEM_USER_DELETE = 1955000000000000203L;
    private static final Long ROUTE_SYSTEM_ROLE_CREATE = 1955000000000000211L;
    private static final Long ROUTE_SYSTEM_ROLE_EDIT = 1955000000000000212L;
    private static final Long ROUTE_SYSTEM_ROLE_DELETE = 1955000000000000213L;
    private static final Long ROUTE_SYSTEM_MENU_CREATE = 1955000000000000221L;
    private static final Long ROUTE_SYSTEM_MENU_EDIT = 1955000000000000222L;
    private static final Long ROUTE_SYSTEM_MENU_DELETE = 1955000000000000223L;
    private static final Long ROUTE_SYSTEM_DEPT_CREATE = 1955000000000000231L;
    private static final Long ROUTE_SYSTEM_DEPT_EDIT = 1955000000000000232L;
    private static final Long ROUTE_SYSTEM_DEPT_DELETE = 1955000000000000233L;
    private static final Long ROUTE_SYSTEM_FILES_UPLOAD = 1955000000000000241L;
    private static final Long ROUTE_SYSTEM_FILES_DELETE = 1955000000000000242L;

    private static final Set<Long> BUILTIN_ROUTE_IDS = Set.of(
            ROUTE_SYSTEM, ROUTE_SYSTEM_MENU, ROUTE_SYSTEM_ROLE, ROUTE_SYSTEM_USER, ROUTE_SYSTEM_DEPT, ROUTE_SYSTEM_FILES,
            ROUTE_DASHBOARD, ROUTE_ANALYTICS, ROUTE_WORKSPACE,
            ROUTE_MONITOR, ROUTE_MONITOR_SERVER, ROUTE_MONITOR_LOGGED_IN,
            ROUTE_SYSTEM_USER_CREATE, ROUTE_SYSTEM_USER_EDIT, ROUTE_SYSTEM_USER_DELETE,
            ROUTE_SYSTEM_ROLE_CREATE, ROUTE_SYSTEM_ROLE_EDIT, ROUTE_SYSTEM_ROLE_DELETE,
            ROUTE_SYSTEM_MENU_CREATE, ROUTE_SYSTEM_MENU_EDIT, ROUTE_SYSTEM_MENU_DELETE,
            ROUTE_SYSTEM_DEPT_CREATE, ROUTE_SYSTEM_DEPT_EDIT, ROUTE_SYSTEM_DEPT_DELETE,
            ROUTE_SYSTEM_FILES_UPLOAD, ROUTE_SYSTEM_FILES_DELETE
    );

    @Resource
    private RouterRoleMapper routerRoleMapper;

    @Resource
    private RouterServiceHelper routerServiceHelper;

    @Resource
    private RolePermissionMapper rolePermissionMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private UserDeptMapper userDeptMapper;

    @Resource
    private UserService userService;

    @Resource
    private RoleService roleService;

    @Resource
    private DeptService deptService;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void initAdminPortalBootstrapData() {
        ensureAdminPortalSchema();
        ensureBuiltinAdminPortalMenus();
    }

    /**
     * 获取路由内容
     *
     * @return 路遇列表
     */
    @Override
    public List<WebUserRouterVo> routerAsync() {
        // 返回路由列表
        List<WebUserRouterVo> voList = new ArrayList<>();

        // 当前的所有的路由列表
        List<Router> routerList = list();

        // 查询路由角色列表
        Map<Long, List<ViewRouterRole>> routerRoleList = routerRoleMapper.selectRouterRoleList().stream()
                .collect(Collectors.groupingBy(ViewRouterRole::getRouterId, Collectors.toList()));

        // 查询角色和权限列表，根据角色id获取对应权限
        Map<Long, List<ViewRolePermission>> rolePermissionList = rolePermissionMapper.viewRolePowerWithAll().stream()
                .collect(Collectors.groupingBy(ViewRolePermission::getRoleId, Collectors.toList()));

        // 整理web用户所能看到的路由列表，并检查当前用户是否是admin
        List<WebUserRouterVo> webUserRouterVoList = routerServiceHelper.getWebUserRouterVos(routerList, routerRoleList, rolePermissionList);
        routerServiceHelper.propagateRolesToParents(webUserRouterVoList);

        // 添加 admin 管理路由权限
        webUserRouterVoList.forEach(routerVo -> {
            // 递归添加路由节点
            if (routerVo.getParentId() == 0) {
                routerVo.setChildren(routerServiceHelper.buildTreeSetChildren(routerVo.getId(), webUserRouterVoList));
                voList.add(routerVo);
            }
        });

        return voList;
    }

    @Override
    public List<String> adminPortalCodes() {
        ensureBuiltinAdminPortalMenus();

        LinkedHashSet<String> codes = new LinkedHashSet<>();
        LoginVo loginVo = BaseContext.getLoginVo();
        if (loginVo != null && loginVo.getPermissions() != null) {
            codes.addAll(loginVo.getPermissions());
        }

        List<Router> routers = getVisibleRouters(true);
        if (isCurrentAdmin()) {
            routers.stream()
                    .map(this::extractAuthCode)
                    .filter(StringUtils::hasText)
                    .forEach(codes::add);
            return new ArrayList<>(codes);
        }

        Set<Long> roleIds = getCurrentRoleIds();
        if (roleIds.isEmpty()) {
            return new ArrayList<>(codes);
        }

        Set<Long> routerIds = routerRoleMapper.selectList(
                        Wrappers.<RouterRole>lambdaQuery().in(RouterRole::getRoleId, roleIds)
                ).stream()
                .map(RouterRole::getRouterId)
                .collect(Collectors.toSet());

        routers.stream()
                .filter(router -> routerIds.contains(router.getId()))
                .map(this::extractAuthCode)
                .filter(StringUtils::hasText)
                .forEach(codes::add);
        return new ArrayList<>(codes);
    }

    @Override
    public List<Map<String, Object>> adminPortalAllMenus() {
        ensureBuiltinAdminPortalMenus();
        List<Map<String, Object>> menuList = getVisibleRouters(false).stream()
                .filter(router -> !Objects.equals(router.getMenuType(), 3))
                .map(this::toAdminPortalMenu)
                .toList();
        return toRouteTree(menuList);
    }

    @Override
    public List<Map<String, Object>> adminPortalMenuList() {
        ensureBuiltinAdminPortalMenus();
        List<Map<String, Object>> menuList = getPortalRouters().stream()
                .map(this::toAdminPortalMenu)
                .toList();
        return buildTree(menuList);
    }

    @Override
    public boolean adminPortalMenuNameExists(String name, String id) {
        Long currentId = parseNullableLong(id);
        Long count = count(Wrappers.<Router>lambdaQuery()
                .eq(Router::getRouteName, name)
                .ne(currentId != null, Router::getId, currentId));
        return count > 0;
    }

    @Override
    public boolean adminPortalMenuPathExists(String path, String id) {
        Long currentId = parseNullableLong(id);
        Long count = count(Wrappers.<Router>lambdaQuery()
                .eq(Router::getPath, path)
                .ne(currentId != null, Router::getId, currentId));
        return count > 0;
    }

    @Override
    @CacheEvict(cacheNames = CACHE_NAMES, key = "'routerList'", beforeInvocation = true)
    public void createAdminPortalMenu(Map<String, Object> request) {
        save(toAdminPortalRouterEntity(null, request));
    }

    @Override
    @CacheEvict(cacheNames = CACHE_NAMES, key = "'routerList'", beforeInvocation = true)
    public void updateAdminPortalMenu(String id, Map<String, Object> request) {
        updateById(toAdminPortalRouterEntity(parseRequiredLong(id), request));
    }

    @Override
    @CacheEvict(cacheNames = CACHE_NAMES, key = "'routerList'", beforeInvocation = true)
    public void deleteAdminPortalMenu(String id) {
        deletedRouterByIds(new ArrayList<>(List.of(parseRequiredLong(id))));
    }

    /**
     * 管理菜单列表
     *
     * @return 系统菜单表
     */
    @Override
    @Cacheable(cacheNames = CACHE_NAMES, key = "'routerList'", cacheManager = "cacheManagerWithMouth")
    public List<RouterManageVo> routerList() {
        // 查询菜单路由
        List<RouterVo> routerList = baseMapper.selectRouterList();

        return routerList.stream().map(router -> {
                    // 管理路由
                    RouterManageVo routerManageVo = new RouterManageVo();
                    BeanUtils.copyProperties(router, routerManageVo);

                    // 将字符串JSON转成实体类，需要判断 meta和transition 是否存在
                    String meta = router.getMeta();
                    if (StringUtils.hasText(meta)) {
                        // 路由 Meta
                        RouterMeta routerMeta = JSON.parseObject(meta, RouterMeta.class);
                        BeanUtils.copyProperties(routerMeta, routerManageVo);

                        // 路由动画
                        RouterMetaTransition transition = routerMeta.getTransition();
                        if (transition != null) {
                            BeanUtils.copyProperties(transition, routerManageVo);
                        }
                    }
                    return routerManageVo;
                })
                .sorted(Comparator.comparing(RouterManageVo::getRank))
                .toList();
    }

    /**
     * 添加路由菜单
     *
     * @param dto 添加菜单表单
     */
    @Override
    @CacheEvict(cacheNames = CACHE_NAMES, key = "'routerList'", beforeInvocation = true)
    public void createRouter(RouterDto dto) {
        // 添加路由
        Router router = new Router();
        BeanUtils.copyProperties(dto, router);

        // 将 meta转成json
        RouterMeta meta = dto.getMeta();
        String jsonString = JSON.toJSONString(meta);
        router.setMeta(jsonString);

        // 将数据提出role 和 power 存储到数据库
        Long id = router.getId();
        routerServiceHelper.insertRouterRoleAndPermission(meta, id);

        // 添加路由
        save(router);
    }

    /**
     * * 更新路由菜单
     *
     * @param dto 更新表单
     */
    @Override
    @CacheEvict(cacheNames = CACHE_NAMES, key = "'routerList'", beforeInvocation = true)
    public void updateRouter(RouterDto dto) {
        // 更新路由
        Router router = new Router();
        BeanUtils.copyProperties(dto, router);

        // 将前端meta转成JSON 存储到数据库
        RouterMeta meta = dto.getMeta();
        String jsonString = JSON.toJSONString(meta);
        router.setMeta(jsonString);

        Long id = router.getId();
        // 先删除路由和角色下所有内容
        routerRoleMapper.deleteBatchIdsByRouterIds(List.of(id));

        // 将数据提出role 和 power 存储到数据库
        routerServiceHelper.insertRouterRoleAndPermission(meta, id);

        // 更新路由信息
        updateById(router);
    }

    /**
     * * 删除路由菜单
     *
     * @param ids 删除id列表
     */
    @Override
    @CacheEvict(cacheNames = CACHE_NAMES, key = "'routerList'", beforeInvocation = true)
    public void deletedRouterByIds(List<Long> ids) {
        // 判断数据请求是否为空
        if (ids.isEmpty()) throw new BaseException(ResultCodeEnum.REQUEST_IS_EMPTY);

        // 查找子级菜单，一起删除
        List<Long> longList = list(Wrappers.<Router>lambdaQuery().in(Router::getParentId, ids)).stream().map(Router::getId).toList();
        ids.addAll(longList);

        removeBatchByIds(ids);
    }

    private void ensureBuiltinAdminPortalMenus() {
        upsertRouter(ROUTE_DASHBOARD, 0L, "/dashboard", "Dashboard", null, "/analytics", 0,
                meta("page.dashboard.title", "lucide:layout-dashboard", -1, null));
        upsertRouter(ROUTE_ANALYTICS, ROUTE_DASHBOARD, "/analytics", "Analytics", "/dashboard/analytics/index", null, 0,
                meta("page.dashboard.analytics", "lucide:area-chart", 1, null, "affixTab", true));
        upsertRouter(ROUTE_WORKSPACE, ROUTE_DASHBOARD, "/workspace", "Workspace", "/dashboard/workspace/index", null, 0,
                meta("page.dashboard.workspace", "carbon:workspace", 2, null));

        upsertRouter(ROUTE_SYSTEM, 0L, "/system", "System", null, "/system/user", 0,
                meta("system.title", "carbon:settings", 9997, null));
        upsertRouter(ROUTE_SYSTEM_USER, ROUTE_SYSTEM, "/system/user", "SystemUser", "/system/user/list", null, 0,
                meta("system.user.title", "mdi:account", 1, "System:User:List"));
        upsertRouter(ROUTE_SYSTEM_ROLE, ROUTE_SYSTEM, "/system/role", "SystemRole", "/system/role/list", null, 0,
                meta("system.role.title", "mdi:account-group", 2, "System:Role:List"));
        upsertRouter(ROUTE_SYSTEM_MENU, ROUTE_SYSTEM, "/system/menu", "SystemMenu", "/system/menu/list", null, 0,
                meta("system.menu.title", "mdi:menu", 3, "System:Menu:List"));
        upsertRouter(ROUTE_SYSTEM_DEPT, ROUTE_SYSTEM, "/system/dept", "SystemDept", "/system/dept/list", null, 0,
                meta("system.dept.title", "charm:organisation", 4, "System:Dept:List"));
        upsertRouter(ROUTE_SYSTEM_FILES, ROUTE_SYSTEM, "/system/files", "SystemFiles", "/system/files/index", null, 0,
                meta("system.files.title", "lucide:folder-open", 5, "System:Files:List"));

        upsertRouter(ROUTE_MONITOR, 0L, "/monitor", "Monitor", null, "/monitor/server", 0,
                meta("monitor.title", "carbon:cloud-monitoring", 9998, null));
        upsertRouter(ROUTE_MONITOR_SERVER, ROUTE_MONITOR, "/monitor/server", "MonitorServer", "/monitor/server/index", null, 0,
                meta("monitor.server.title", "mingcute:server-fill", 1, "Monitor:Server:List"));
        upsertRouter(ROUTE_MONITOR_LOGGED_IN, ROUTE_MONITOR, "/monitor/logged-in", "MonitorLoggedIn", "/monitor/logged-in/index", null, 0,
                meta("monitor.loggedIn.title", "material-symbols:login", 2, "Monitor:LoggedIn:List"));

        upsertButton(ROUTE_SYSTEM_USER_CREATE, ROUTE_SYSTEM_USER, "SystemUserCreate", "System:User:Create", "common.create");
        upsertButton(ROUTE_SYSTEM_USER_EDIT, ROUTE_SYSTEM_USER, "SystemUserEdit", "System:User:Edit", "common.edit");
        upsertButton(ROUTE_SYSTEM_USER_DELETE, ROUTE_SYSTEM_USER, "SystemUserDelete", "System:User:Delete", "common.delete");
        upsertButton(ROUTE_SYSTEM_ROLE_CREATE, ROUTE_SYSTEM_ROLE, "SystemRoleCreate", "System:Role:Create", "common.create");
        upsertButton(ROUTE_SYSTEM_ROLE_EDIT, ROUTE_SYSTEM_ROLE, "SystemRoleEdit", "System:Role:Edit", "common.edit");
        upsertButton(ROUTE_SYSTEM_ROLE_DELETE, ROUTE_SYSTEM_ROLE, "SystemRoleDelete", "System:Role:Delete", "common.delete");
        upsertButton(ROUTE_SYSTEM_MENU_CREATE, ROUTE_SYSTEM_MENU, "SystemMenuCreate", "System:Menu:Create", "common.create");
        upsertButton(ROUTE_SYSTEM_MENU_EDIT, ROUTE_SYSTEM_MENU, "SystemMenuEdit", "System:Menu:Edit", "common.edit");
        upsertButton(ROUTE_SYSTEM_MENU_DELETE, ROUTE_SYSTEM_MENU, "SystemMenuDelete", "System:Menu:Delete", "common.delete");
        upsertButton(ROUTE_SYSTEM_DEPT_CREATE, ROUTE_SYSTEM_DEPT, "SystemDeptCreate", "System:Dept:Create", "common.create");
        upsertButton(ROUTE_SYSTEM_DEPT_EDIT, ROUTE_SYSTEM_DEPT, "SystemDeptEdit", "System:Dept:Edit", "common.edit");
        upsertButton(ROUTE_SYSTEM_DEPT_DELETE, ROUTE_SYSTEM_DEPT, "SystemDeptDelete", "System:Dept:Delete", "common.delete");
        upsertButton(ROUTE_SYSTEM_FILES_UPLOAD, ROUTE_SYSTEM_FILES, "SystemFilesUpload", "System:Files:Upload", "system.files.upload");
        upsertButton(ROUTE_SYSTEM_FILES_DELETE, ROUTE_SYSTEM_FILES, "SystemFilesDelete", "System:Files:Delete", "common.delete");

        removeRetiredRouter(ROUTE_MONITOR_CACHE_RETIRED);
        ensureAdminUserRole();
        BUILTIN_ROUTE_IDS.forEach(routeId -> ensureRouterRole(routeId, ADMIN_ROLE_ID));
    }

    private void ensureAdminPortalSchema() {
        ensureColumn("sys_role", "status", "`status` tinyint NULL DEFAULT 0 COMMENT '1:禁用 0:正常' AFTER `description`");
        ensureColumn("sys_dept", "status", "`status` tinyint NULL DEFAULT 0 COMMENT '1:禁用 0:正常' AFTER `summary`");
        ensureColumn("sys_router", "status", "`status` tinyint NULL DEFAULT 0 COMMENT '1:禁用 0:正常' AFTER `meta`");
    }

    private void ensureColumn(String tableName, String columnName, String definition) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                  AND COLUMN_NAME = ?
                """, Integer.class, tableName, columnName);
        if (count != null && count == 0) {
            jdbcTemplate.execute("ALTER TABLE `" + tableName + "` ADD COLUMN " + definition);
        }
    }

    private void upsertButton(Long id, Long parentId, String name, String authCode, String title) {
        upsertRouter(id, parentId, "/__button/" + authCode, name, null, null, 3,
                meta(title, null, null, authCode));
    }

    private void upsertRouter(Long id, Long parentId, String path, String name, String component, String redirect,
                              Integer menuType, Map<String, Object> meta) {
        Router router = new Router();
        router.setId(id);
        router.setParentId(parentId);
        router.setPath(path);
        router.setRouteName(name);
        router.setComponent(component);
        router.setRedirect(redirect);
        router.setMenuType(menuType);
        router.setMeta(JSON.toJSONString(meta));
        router.setStatus(false);
        router.setIsDeleted(false);

        Router existing = getById(id);
        if (existing == null) {
            save(router);
        } else if (isRouterChanged(existing, router)) {
            updateById(router);
        }
    }

    private boolean isRouterChanged(Router existing, Router router) {
        return !Objects.equals(existing.getParentId(), router.getParentId())
                || !Objects.equals(existing.getPath(), router.getPath())
                || !Objects.equals(existing.getRouteName(), router.getRouteName())
                || !Objects.equals(existing.getComponent(), router.getComponent())
                || !Objects.equals(existing.getRedirect(), router.getRedirect())
                || !Objects.equals(existing.getMenuType(), router.getMenuType())
                || !Objects.equals(existing.getMeta(), router.getMeta())
                || !Objects.equals(existing.getStatus(), router.getStatus())
                || !Objects.equals(existing.getIsDeleted(), router.getIsDeleted());
    }

    private void removeRetiredRouter(Long id) {
        if (getById(id) == null) {
            return;
        }
        routerRoleMapper.deleteBatchIdsByRouterIds(List.of(id));
        removeById(id);
    }

    private void ensureAdminUserRole() {
        ensureDefaultAdminUser();
        ensureDefaultAdminRole();

        Long count = userRoleMapper.selectCount(Wrappers.<UserRole>lambdaQuery()
                .eq(UserRole::getUserId, ADMIN_USER_ID)
                .eq(UserRole::getRoleId, ADMIN_ROLE_ID));
        if (count > 0) {
            return;
        }

        UserRole userRole = new UserRole();
        userRole.setUserId(ADMIN_USER_ID);
        userRole.setRoleId(ADMIN_ROLE_ID);
        userRole.setCreateUser(ADMIN_USER_ID);
        userRole.setUpdateUser(ADMIN_USER_ID);
        userRoleMapper.insert(userRole);
    }

    private void ensureDefaultAdminUser() {
        AdminUser user = userService.getById(ADMIN_USER_ID);
        if (user == null) {
            user = new AdminUser();
            user.setId(ADMIN_USER_ID);
            user.setUsername("Administrator");
            user.setNickname("Administrator");
            user.setEmail("admin@qq.com");
            user.setPhone("1864692046");
            user.setPassword(passwordEncoder.encode("admin123"));
            user.setSex((byte) 1);
            user.setSummary("系统默认管理员");
            user.setStatus(false);
            user.setIsDeleted(false);
            user.setCreateUser(ADMIN_USER_ID);
            user.setUpdateUser(ADMIN_USER_ID);
            userService.save(user);
            replaceUserDept(ADMIN_USER_ID, DEFAULT_DEPT_ID);
            return;
        }

        boolean changed = false;
        if (!StringUtils.hasText(user.getUsername())) {
            user.setUsername("Administrator");
            changed = true;
        }
        if (!StringUtils.hasText(user.getNickname())) {
            user.setNickname(user.getUsername());
            changed = true;
        }
        if (!StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode("admin123"));
            changed = true;
        }
        if (Boolean.TRUE.equals(user.getStatus()) || user.getStatus() == null) {
            user.setStatus(false);
            changed = true;
        }
        if (Boolean.TRUE.equals(user.getIsDeleted()) || user.getIsDeleted() == null) {
            user.setIsDeleted(false);
            changed = true;
        }
        if (changed) {
            userService.updateById(user);
        }

        Long deptCount = userDeptMapper.selectCount(Wrappers.<UserDept>lambdaQuery()
                .eq(UserDept::getUserId, ADMIN_USER_ID)
                .eq(UserDept::getDeptId, DEFAULT_DEPT_ID));
        if (deptCount == 0 && deptService.getById(DEFAULT_DEPT_ID) != null) {
            UserDept userDept = new UserDept();
            userDept.setUserId(ADMIN_USER_ID);
            userDept.setDeptId(DEFAULT_DEPT_ID);
            userDept.setCreateUser(ADMIN_USER_ID);
            userDept.setUpdateUser(ADMIN_USER_ID);
            userDeptMapper.insert(userDept);
        }
    }

    private void ensureDefaultAdminRole() {
        Role role = roleService.getById(ADMIN_ROLE_ID);
        if (role == null) {
            role = new Role();
            role.setId(ADMIN_ROLE_ID);
            role.setRoleCode("admin");
            role.setDescription("管理员用户");
            role.setStatus(false);
            role.setCreateUser(ADMIN_USER_ID);
            role.setUpdateUser(ADMIN_USER_ID);
            roleService.save(role);
            return;
        }

        boolean changed = false;
        if (!"admin".equals(role.getRoleCode())) {
            role.setRoleCode("admin");
            changed = true;
        }
        if (!StringUtils.hasText(role.getDescription())) {
            role.setDescription("管理员用户");
            changed = true;
        }
        if (role.getStatus() == null || Boolean.TRUE.equals(role.getStatus())) {
            role.setStatus(false);
            changed = true;
        }
        if (changed) {
            roleService.updateById(role);
        }
    }

    private void ensureRouterRole(Long routerId, Long roleId) {
        Long count = routerRoleMapper.selectCount(Wrappers.<RouterRole>lambdaQuery()
                .eq(RouterRole::getRouterId, routerId)
                .eq(RouterRole::getRoleId, roleId));
        if (count > 0) {
            return;
        }

        RouterRole routerRole = new RouterRole();
        routerRole.setRouterId(routerId);
        routerRole.setRoleId(roleId);
        routerRole.setCreateUser(ADMIN_USER_ID);
        routerRole.setUpdateUser(ADMIN_USER_ID);
        routerRoleMapper.insert(routerRole);
    }

    private List<Router> getPortalRouters() {
        return list(Wrappers.<Router>lambdaQuery()
                        .eq(Router::getIsDeleted, false)
                        .orderByAsc(Router::getParentId)
                        .orderByAsc(Router::getId))
                .stream()
                .filter(this::isPortalRouter)
                .sorted(Comparator.comparingInt(this::routerOrder))
                .toList();
    }

    private List<Router> getVisibleRouters(boolean includeButtons) {
        List<Router> routers = getPortalRouters().stream()
                .filter(this::isEnabledRouter)
                .toList();
        if (isCurrentAdmin()) {
            return routers.stream()
                    .filter(router -> includeButtons || !Objects.equals(router.getMenuType(), 3))
                    .toList();
        }

        Map<Long, Router> routerById = routers.stream().collect(Collectors.toMap(Router::getId, Function.identity()));
        Map<Long, List<RouterRole>> rolesByRouterId = routerRoleMapper.selectList(null)
                .stream()
                .collect(Collectors.groupingBy(RouterRole::getRouterId));
        Set<Long> currentRoleIds = getCurrentRoleIds();
        Set<Long> visibleIds = new HashSet<>();

        for (Router router : routers) {
            List<RouterRole> routeRoles = rolesByRouterId.getOrDefault(router.getId(), List.of());
            boolean visible = routeRoles.isEmpty()
                    || routeRoles.stream().anyMatch(routerRole -> currentRoleIds.contains(routerRole.getRoleId()));
            if (visible) {
                addSelfAndParents(router.getId(), routerById, visibleIds);
            }
        }

        return routers.stream()
                .filter(router -> visibleIds.contains(router.getId()))
                .filter(router -> includeButtons || !Objects.equals(router.getMenuType(), 3))
                .toList();
    }

    private void addSelfAndParents(Long routerId, Map<Long, Router> routerById, Set<Long> visibleIds) {
        Router router = routerById.get(routerId);
        while (router != null && visibleIds.add(router.getId())) {
            router = routerById.get(router.getParentId());
        }
    }

    private boolean isCurrentAdmin() {
        if (Objects.equals(BaseContext.getUserId(), ADMIN_USER_ID)) {
            return true;
        }
        LoginVo loginVo = BaseContext.getLoginVo();
        return loginVo != null
                && loginVo.getRoles() != null
                && RoleHelper.checkAdmin(new ArrayList<>(loginVo.getRoles()));
    }

    private Set<Long> getCurrentRoleIds() {
        LoginVo loginVo = BaseContext.getLoginVo();
        if (loginVo == null || loginVo.getRoles() == null || loginVo.getRoles().isEmpty()) {
            return Set.of();
        }
        return roleService.list(new QueryWrapper<Role>()
                        .in("role_code", loginVo.getRoles())
                        .and(wrapper -> wrapper.eq("status", 0).or().isNull("status")))
                .stream()
                .map(Role::getId)
                .collect(Collectors.toSet());
    }

    private boolean isEnabledRouter(Router router) {
        return router.getStatus() == null || !Boolean.TRUE.equals(router.getStatus());
    }

    private boolean isPortalRouter(Router router) {
        return BUILTIN_ROUTE_IDS.contains(router.getId()) || Boolean.TRUE.equals(metaMap(router).get("vben"));
    }

    private int routerOrder(Router router) {
        Object order = metaMap(router).get("order");
        if (order instanceof Number number) {
            return number.intValue();
        }
        return 0;
    }

    private Map<String, Object> toAdminPortalMenu(Router router) {
        Map<String, Object> meta = metaMap(router);
        String type = resolveMenuType(router, meta);
        String authCode = extractAuthCode(router);

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", toId(router.getId()));
        item.put("pid", router.getParentId() == null ? "0" : toId(router.getParentId()));
        item.put("name", router.getRouteName());
        item.put("path", router.getPath());
        item.put("component", resolveComponent(router, type));
        item.put("redirect", router.getRedirect());
        item.put("type", type);
        item.put("authCode", authCode);
        item.put("status", toFrontendStatus(router.getStatus()));
        item.put("meta", meta);
        if (meta.get("activePath") != null) {
            item.put("activePath", meta.get("activePath"));
        }
        return item;
    }

    private List<Map<String, Object>> toRouteTree(List<Map<String, Object>> menus) {
        return buildTree(menus).stream().map(this::toRouteRecord).toList();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toRouteRecord(Map<String, Object> menu) {
        Map<String, Object> route = new LinkedHashMap<>();
        route.put("name", menu.get("name"));
        route.put("path", menu.get("path"));
        if (StringUtils.hasText((String) menu.get("component"))) {
            route.put("component", menu.get("component"));
        }
        if (StringUtils.hasText((String) menu.get("redirect"))) {
            route.put("redirect", menu.get("redirect"));
        }
        route.put("meta", menu.get("meta"));

        List<Map<String, Object>> children = (List<Map<String, Object>>) menu.get("children");
        if (children != null && !children.isEmpty()) {
            route.put("children", children.stream().map(this::toRouteRecord).toList());
        }
        return route;
    }

    private String resolveMenuType(Router router, Map<String, Object> meta) {
        Integer menuType = router.getMenuType();
        if (Objects.equals(menuType, 3)) {
            return "button";
        }
        if (Objects.equals(menuType, 1)) {
            return "embedded";
        }
        if (Objects.equals(menuType, 2)) {
            return "link";
        }
        if (meta.get("iframeSrc") != null) {
            return "embedded";
        }
        if (meta.get("link") != null) {
            return "link";
        }
        return StringUtils.hasText(router.getComponent()) ? "menu" : "catalog";
    }

    private String resolveComponent(Router router, String type) {
        if ("embedded".equals(type) || "link".equals(type)) {
            return "IFrameView";
        }
        return router.getComponent();
    }

    private String extractAuthCode(Router router) {
        Map<String, Object> meta = metaMap(router);
        Object authCode = meta.get("authCode");
        if (authCode instanceof String text && StringUtils.hasText(text)) {
            return text;
        }

        Object auths = meta.get("auths");
        if (auths instanceof Collection<?> collection && !collection.isEmpty()) {
            Object first = collection.iterator().next();
            return first == null ? null : first.toString();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> metaMap(Router router) {
        Map<String, Object> meta = new LinkedHashMap<>();
        if (StringUtils.hasText(router.getMeta())) {
            try {
                Map<String, Object> parsed = JSON.parseObject(router.getMeta(), Map.class);
                if (parsed != null) {
                    meta.putAll(parsed);
                }
            } catch (Exception ignored) {
                meta.put("title", router.getRouteName());
            }
        }

        migrateLegacyMeta(meta);
        if (!StringUtils.hasText((String) meta.get("title"))) {
            meta.put("title", router.getRouteName());
        }
        return meta;
    }

    private void migrateLegacyMeta(Map<String, Object> meta) {
        moveMetaKey(meta, "rank", "order");
        moveMetaKey(meta, "fixedTag", "affixTab");
        moveMetaKey(meta, "hiddenTag", "hideInTab");
        moveMetaKey(meta, "frameSrc", "iframeSrc");
        if (Boolean.FALSE.equals(meta.remove("showLink"))) {
            meta.put("hideInMenu", true);
        }
        meta.remove("showParent");
        meta.remove("transition");
        meta.remove("roles");
    }

    private void moveMetaKey(Map<String, Object> meta, String oldKey, String newKey) {
        if (!meta.containsKey(newKey) && meta.containsKey(oldKey)) {
            meta.put(newKey, meta.get(oldKey));
        }
        meta.remove(oldKey);
    }

    @SuppressWarnings("unchecked")
    private Router toAdminPortalRouterEntity(Long id, Map<String, Object> request) {
        String name = toStringValue(request, "name");
        if (!StringUtils.hasText(name)) {
            throw new BaseException(ResultCodeEnum.PARAM_ERROR);
        }
        validateMaxLength(name, 100);
        validateMaxLength(toStringValue(request, "path"), 255);
        validateMaxLength(toStringValue(request, "component"), 255);
        validateMaxLength(toStringValue(request, "redirect"), 255);
        validateMaxLength(toStringValue(request, "authCode"), 255);

        Map<String, Object> meta = request != null && request.get("meta") instanceof Map<?, ?> requestMeta
                ? new LinkedHashMap<>((Map<String, Object>) requestMeta)
                : new LinkedHashMap<>();
        meta.put("vben", true);
        String authCode = toStringValue(request, "authCode");
        if (StringUtils.hasText(authCode)) {
            meta.put("authCode", authCode);
            meta.put("auths", List.of(authCode));
        }
        if (StringUtils.hasText(toStringValue(request, "activePath"))) {
            meta.put("activePath", toStringValue(request, "activePath"));
        }

        Router router = new Router();
        router.setId(id);
        Long parentId = parseNullableLong(toStringValue(request, "pid"));
        router.setParentId(parentId == null ? 0L : parentId);
        router.setRouteName(name);
        router.setPath(StringUtils.hasText(toStringValue(request, "path")) ? toStringValue(request, "path") : "/__button/" + authCode);
        router.setComponent(toStringValue(request, "component"));
        router.setRedirect(toStringValue(request, "redirect"));
        router.setMenuType(toMenuType(toStringValue(request, "type")));
        router.setMeta(JSON.toJSONString(meta));
        router.setStatus(toDisabledStatus(toInteger(request == null ? null : request.get("status"))));
        router.setIsDeleted(false);
        return router;
    }

    private Integer toMenuType(String type) {
        if ("embedded".equals(type)) {
            return 1;
        }
        if ("link".equals(type)) {
            return 2;
        }
        if ("button".equals(type)) {
            return 3;
        }
        return 0;
    }

    private Map<String, Object> meta(String title, String icon, Integer order, String authCode, Object... extras) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("title", title);
        meta.put("vben", true);
        if (StringUtils.hasText(icon)) {
            meta.put("icon", icon);
        }
        if (order != null) {
            meta.put("order", order);
        }
        if (StringUtils.hasText(authCode)) {
            meta.put("authCode", authCode);
            meta.put("auths", List.of(authCode));
        }
        for (int i = 0; i + 1 < extras.length; i += 2) {
            meta.put(String.valueOf(extras[i]), extras[i + 1]);
        }
        return meta;
    }

    private void replaceUserDept(Long userId, Long deptId) {
        userDeptMapper.deleteBatchIdsByUserIds(List.of(userId));
        if (deptId == null) {
            return;
        }

        UserDept userDept = new UserDept();
        userDept.setUserId(userId);
        userDept.setDeptId(deptId);
        userDeptMapper.insert(userDept);
    }

    private List<Map<String, Object>> buildTree(List<Map<String, Object>> items) {
        Map<String, List<Map<String, Object>>> childrenByPid = items.stream()
                .collect(Collectors.groupingBy(item -> Objects.toString(item.get("pid"), "0"), LinkedHashMap::new, Collectors.toList()));
        Set<String> ids = items.stream().map(item -> Objects.toString(item.get("id"), "")).collect(Collectors.toSet());
        return items.stream()
                .filter(item -> {
                    String pid = Objects.toString(item.get("pid"), "0");
                    return "0".equals(pid) || !ids.contains(pid);
                })
                .map(item -> attachChildren(item, childrenByPid))
                .toList();
    }

    private Map<String, Object> attachChildren(Map<String, Object> item, Map<String, List<Map<String, Object>>> childrenByPid) {
        Map<String, Object> copy = new LinkedHashMap<>(item);
        List<Map<String, Object>> children = childrenByPid.getOrDefault(Objects.toString(item.get("id"), ""), List.of())
                .stream()
                .map(child -> attachChildren(child, childrenByPid))
                .toList();
        if (!children.isEmpty()) {
            copy.put("children", children);
        }
        return copy;
    }

    private Long parseNullableLong(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return parseRequiredLong(value);
    }

    private Long parseRequiredLong(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException exception) {
            throw new BaseException(ResultCodeEnum.PARAM_ERROR);
        }
    }

    private Integer toInteger(Object value) {
        if (value == null || !StringUtils.hasText(value.toString())) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.valueOf(value.toString());
    }

    private String toStringValue(Map<String, Object> request, String key) {
        if (request == null) {
            return null;
        }
        Object value = request.get(key);
        return value == null ? null : value.toString();
    }

    private String toId(Long id) {
        return id == null ? null : id.toString();
    }

    private int toFrontendStatus(Boolean disabled) {
        return Boolean.TRUE.equals(disabled) ? 0 : 1;
    }

    private Boolean toDisabledStatus(Integer status) {
        return status != null && status == 0;
    }

    private void validateMaxLength(String value, int maxLength) {
        if (value != null && value.length() > maxLength) {
            throw new BaseException(ResultCodeEnum.PARAM_ERROR);
        }
    }
}
