package t.uni.system.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import t.uni.common.core.exception.BaseException;
import t.uni.common.core.result.PageResult;
import t.uni.common.core.result.ResultCodeEnum;
import t.uni.domain.common.enums.AdminResultCodeEnum;
import t.uni.domain.common.constant.FileStorageConstant;
import t.uni.domain.common.constant.UserConstant;
import t.uni.domain.common.model.vo.LoginVo;
import t.uni.domain.system.dto.FileUploadDto;
import t.uni.domain.system.dto.user.AdminUserAddDto;
import t.uni.domain.system.dto.user.AdminUserDto;
import t.uni.domain.system.dto.user.AdminUserUpdateDto;
import t.uni.domain.system.entity.AdminUser;
import t.uni.domain.system.entity.Role;
import t.uni.domain.system.entity.UserDept;
import t.uni.domain.system.entity.UserLoginLog;
import t.uni.domain.system.entity.UserRole;
import t.uni.domain.system.views.ViewUserDept;
import t.uni.domain.system.vo.FileInfoVo;
import t.uni.domain.system.vo.user.AdminUserVo;
import t.uni.domain.system.vo.user.UserVo;
import t.uni.system.core.cache.RedisService;
import t.uni.system.core.event.ClearAllUserCacheEvent;
import t.uni.system.core.event.UpdateUserinfoByUserIdsEvent;
import t.uni.system.mapper.*;
import t.uni.system.service.FilesService;
import t.uni.system.service.UserService;

import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 用户信息 服务实现类
 *
 * @since 2024-09-26
 */
@Service
@Transactional
public class UserServiceImpl extends ServiceImpl<UserMapper, AdminUser> implements UserService {

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private FilesService filesService;

    @Resource
    private UserDeptMapper userDeptMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private UserLoginLogMapper userLoginLogMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private RedisService redisService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    @Value("${dromara.local-plus.domain:/api/local-file}")
    private String localFileDomain;

    @Value("${dromara.local-plus.storage-path:/tmp/t-uni-admin/}")
    private String localStoragePath;

    /**
     * 获取用户信息
     *
     * @param id 用户id
     * @return 用户信息
     */
    @Override
    public UserVo getUserinfoById(Long id) {
        // 判断请求Id是否为空
        if (id == null) throw new BaseException(ResultCodeEnum.REQUEST_IS_EMPTY);
        AdminUser user = getById(id);

        // 用户是否存在
        if (user == null) throw new BaseException(ResultCodeEnum.DATA_NOT_EXIST);

        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(user, userVo);

        return userVo;
    }

    /**
     * 管理员强制用户下线
     *
     * <p><b>功能说明</b>：管理员强制指定用户退出登录状态，并记录操作日志</p>
     *
     * <p><b>处理流程</b>：</p>
     * <ol>
     *   <li>参数校验：检查用户ID是否为空</li>
     *   <li>查询用户信息：根据ID获取用户实体</li>
     *   <li>记录操作日志：保存强制下线记录到用户登录日志表</li>
     *   <li>清除登录状态：删除Redis中的用户登录信息</li>
     * </ol>
     *
     * @param id 用户ID（不可为空）
     * @see UserConstant#FORCE_LOGOUT 强制下线类型常量
     */
    @Override
    public void forcedOfflineByAdmin(Long id) {
        if (id == null) throw new BaseException(ResultCodeEnum.REQUEST_IS_EMPTY);

        // 根据id查询用户登录前缀
        AdminUser adminUser = getOne(Wrappers.<AdminUser>lambdaQuery().eq(AdminUser::getId, id));

        // 将用户登录保存在用户登录日志表中
        UserLoginLog userLoginLog = new UserLoginLog();
        BeanUtils.copyProperties(adminUser, userLoginLog);
        userLoginLog.setId(null);
        userLoginLog.setUserId(adminUser.getId());
        userLoginLog.setType(UserConstant.FORCE_LOGOUT);
        userLoginLogMapper.insert(userLoginLog);

        // 删除Redis中用户信息
        String username = adminUser.getUsername();
        applicationEventPublisher.publishEvent(new ClearAllUserCacheEvent(this, username));
    }

    /**
     * 查询用户
     *
     * @param keyword 查询用户信息关键字
     * @return 用户信息列表
     */
    @Override
    public List<UserVo> getUserListByKeyword(String keyword) {
        // 如果没有输入，返回前20条用户信息
        if (!StringUtils.hasText(keyword)) {
            Page<AdminUser> page = Page.of(1, 20);
            LambdaQueryWrapper<AdminUser> queryWrapper = Wrappers.<AdminUser>lambdaQuery().eq(AdminUser::getStatus, false);

            return list(page, queryWrapper).stream()
                    .map(adminUser -> {
                        UserVo adminUserVo = new UserVo();
                        BeanUtils.copyProperties(adminUser, adminUserVo);
                        return adminUserVo;
                    }).toList();
        }

        List<AdminUser> list = baseMapper.queryUser(keyword);
        return list.stream().map(adminUser -> {
            UserVo adminUserVo = new UserVo();
            BeanUtils.copyProperties(adminUser, adminUserVo);
            return adminUserVo;
        }).toList();
    }

    /**
     * 查询缓存中已登录用户
     *
     * @param pageParams 分页查询
     * @return 分页查询结果
     */
    @Override
    public PageResult<UserVo> getCacheUserPage(Page<AdminUser> pageParams) {
        long pageNum = pageParams.getCurrent();
        long pageSize = pageParams.getSize();
        List<String> keys = redisService.scannerRedisKeyByPage(pageNum, pageSize);

        List<UserVo> list = keys.stream().map(key -> {
            Object loginVoObject = redisTemplate.opsForValue().get(key);
            LoginVo loginVo = JSON.parseObject(JSON.toJSONString(loginVoObject), LoginVo.class);

            UserVo userVo = new UserVo();
            BeanUtils.copyProperties(loginVo, userVo);
            userVo.setSummary(loginVo.getPersonDescription());
            return userVo;
        }).toList();

        return PageResult.<UserVo>builder()
                .pageNo(pageNum).pageSize(pageSize)
                .list(list).total((long) keys.size())
                .build();
    }

    /**
     * 用户信息 服务实现类
     *
     * @param pageParams 用户信息分页查询page对象
     * @param dto        用户信息分页查询对象
     * @return 查询分页用户信息返回对象
     */
    @Override
    public PageResult<AdminUserVo> getUserPageByAdmin(Page<AdminUser> pageParams, AdminUserDto dto) {
        IPage<ViewUserDept> page = baseMapper.selectListByPage(pageParams, dto);

        List<AdminUserVo> voList = page.getRecords().stream()
                .map(adminUser -> {
                    AdminUserVo adminUserVo = new AdminUserVo();
                    BeanUtils.copyProperties(adminUser, adminUserVo);
                    return adminUserVo;
                })
                .filter(adminUserVo -> !adminUserVo.getId().equals(1L))
                .toList();

        return PageResult.<AdminUserVo>builder()
                .list(voList)
                .pageNo(page.getCurrent())
                .pageSize(page.getSize())
                .total(page.getTotal())
                .build();
    }

    @Override
    public Map<String, Object> adminPortalUserList(long page, long pageSize, Map<String, Object> query) {
        Integer status = toInteger(query == null ? null : query.get("status"));
        var wrapper = Wrappers.<AdminUser>lambdaQuery()
                .eq(AdminUser::getIsDeleted, false)
                .like(StringUtils.hasText(toStringValue(query, "username")), AdminUser::getUsername, toStringValue(query, "username"))
                .like(StringUtils.hasText(toStringValue(query, "realName")), AdminUser::getNickname, toStringValue(query, "realName"))
                .eq(status != null, AdminUser::getStatus, status != null && status == 0)
                .orderByDesc(AdminUser::getCreateTime);

        Page<AdminUser> userPage = page(Page.of(page, pageSize), wrapper);
        List<Long> userIds = userPage.getRecords().stream().map(AdminUser::getId).toList();
        Map<Long, Long> deptByUserId = getDeptByUserId(userIds);
        Map<Long, List<String>> rolesByUserId = getRolesByUserId(userIds);

        List<Map<String, Object>> items = userPage.getRecords().stream()
                .map(user -> toAdminPortalUserMap(user, deptByUserId.get(user.getId()), rolesByUserId.get(user.getId())))
                .toList();
        return pageResult(items, userPage.getTotal());
    }

    @Override
    public void createAdminPortalUser(Map<String, Object> request) {
        AdminUser user = new AdminUser();
        applyAdminPortalUserRequest(user, request, true);
        save(user);
        replaceUserDept(user.getId(), parseNullableLong(toStringValue(request, "deptId")));
        replaceUserRoles(user.getId(), toStringList(request.get("roleIds")));
    }

    @Override
    public void updateAdminPortalUser(String id, Map<String, Object> request) {
        AdminUser user = getById(parseRequiredLong(id));
        if (user == null) {
            throw new BaseException(ResultCodeEnum.DATA_NOT_EXIST);
        }
        applyAdminPortalUserRequest(user, request, false);
        updateById(user);
        if (request.containsKey("deptId")) {
            replaceUserDept(user.getId(), parseNullableLong(toStringValue(request, "deptId")));
        }
        if (request.containsKey("roleIds")) {
            replaceUserRoles(user.getId(), toStringList(request.get("roleIds")));
        }
        applicationEventPublisher.publishEvent(new UpdateUserinfoByUserIdsEvent(this, List.of(user.getId())));
    }

    @Override
    public void deleteAdminPortalUser(String id) {
        deleteUserByAdmin(List.of(parseRequiredLong(id)));
    }

    /**
     * 添加用户信息
     * 需要确认用户名-username是唯一的
     * 需要确认邮箱-email是唯一的
     *
     * @param dto 用户信息添加
     */
    @Override
    public void createUserByAdmin(AdminUserAddDto dto) {
        // 对密码加密
        String encodePassword = passwordEncoder.encode(dto.getPassword());

        // 保存数据
        AdminUser adminUser = new AdminUser();
        BeanUtils.copyProperties(dto, adminUser);
        adminUser.setPassword(encodePassword);
        save(adminUser);

        // 插入用户部门关系表
        Long userId = adminUser.getId();
        Long deptId = dto.getDeptId();
        UserDept userDept = new UserDept();
        userDept.setDeptId(deptId);
        userDept.setUserId(userId);

        // 插入分配后的用户内容
        userDeptMapper.insert(userDept);
    }

    /**
     * 更新用户信息
     * 如果更新了用户名需要用户重新登录，因为Redis中的key已经被删除
     *
     * @param dto 用户信息更新
     */
    @Override
    public void updateUserByAdmin(AdminUserUpdateDto dto) {
        Long userId = dto.getId();

        // 判断更新内容是否存在
        AdminUser adminUser = getOne(Wrappers.<AdminUser>lambdaQuery().eq(AdminUser::getId, userId));
        if (adminUser == null) throw new BaseException(ResultCodeEnum.DATA_NOT_EXIST);

        // 更新用户
        BeanUtils.copyProperties(dto, adminUser);

        // 部门Id
        Long deptId = dto.getDeptId();
        if (deptId != null) {
            // 更新用户部门
            UserDept userDept = new UserDept();
            userDept.setDeptId(deptId);
            userDept.setUserId(userId);
            // 删除这个用户部门关系下所有
            userDeptMapper.deleteBatchIdsByUserIds(List.of(userId));
            // 插入分配后的用户内容
            userDeptMapper.insert(userDept);
        }

        // 更新头像
        uploadAvatarByAdmin(dto, adminUser);

        // 更新密码，放在最后，如果更新密码就将密码删除
        updateUserPasswordByAdmin(adminUser);

        // 更新用户信息
        updateById(adminUser);

        // 同步到 Redis
        List<Long> ids = List.of(adminUser.getId());
        applicationEventPublisher.publishEvent(new UpdateUserinfoByUserIdsEvent(this, ids));
    }

    /**
     * 删除|批量删除用户信息
     *
     * @param ids 删除id列表
     */
    @Override
    public void deleteUserByAdmin(List<Long> ids) {
        // 判断数据请求是否为空
        if (ids.isEmpty()) throw new BaseException(ResultCodeEnum.REQUEST_IS_EMPTY);

        // 根据用户Id列表查询用户角色
        List<Role> list = roleMapper.selectListByUserIds(ids);
        List<Role> roleList = list.stream().filter(role -> role.getRoleCode().equals("admin") || ids.contains(1L)).toList();
        if (!roleList.isEmpty()) {
            throw new BaseException(
                    AdminResultCodeEnum.ADMIN_ROLE_CAN_NOT_DELETED.getCode(),
                    AdminResultCodeEnum.ADMIN_ROLE_CAN_NOT_DELETED.getMessage()
            );
        }

        // 清除Redis中数据
        applicationEventPublisher.publishEvent(new UpdateUserinfoByUserIdsEvent(this, ids));

        // 删除部门相关
        userDeptMapper.deleteBatchIdsByUserIds(ids);

        // 删除用户角色相关
        userRoleMapper.deleteBatchIdsByUserIds(ids);

        // 逻辑删除
        removeByIds(ids);
    }

    /**
     * 管理员修改管理员用户密码
     *
     * @param adminUser 管理员用户修改密码
     */
    private void updateUserPasswordByAdmin(AdminUser adminUser) {
        Long userId = adminUser.getId();
        String password = adminUser.getPassword();

        // 密码更新是否存在
        if (!StringUtils.hasText(password)) return;

        // 对密码加密
        String encode = passwordEncoder.encode(password);

        // 判断新密码是否与旧密码相同
        if (adminUser.getPassword().equals(encode)) {
            throw new BaseException(ResultCodeEnum.UPDATE_NEW_PASSWORD_SAME_AS_OLD_PASSWORD);
        }

        // 更新用户密码
        adminUser.setPassword(encode);
        adminUser.setId(userId);
    }

    /**
     * 管理员上传用户头像
     *
     * @param dto 管理员用户修改头像
     */
    private void uploadAvatarByAdmin(AdminUserUpdateDto dto, AdminUser adminUser) {
        MultipartFile avatar = dto.getAvatar();
        Long userId = dto.getId();

        // 上传头像是否存在
        if (avatar == null) return;

        // 上传头像
        FileUploadDto uploadDto = FileUploadDto.builder().file(avatar).type(FileStorageConstant.AVATAR).build();
        FileInfoVo fileInfoVo = filesService.upload(uploadDto);

        // 更新用户
        adminUser.setId(userId);
        adminUser.setAvatar(fileInfoVo.getUrl());
    }

    private Map<String, Object> toAdminPortalUserMap(AdminUser user, Long deptId, List<String> roleIds) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", toId(user.getId()));
        item.put("username", user.getUsername());
        item.put("realName", user.getNickname());
        item.put("email", user.getEmail());
        item.put("phone", user.getPhone());
        item.put("avatar", normalizeAvatar(user.getAvatar()));
        item.put("deptId", deptId == null ? null : toId(deptId));
        item.put("roleIds", roleIds == null ? List.of() : roleIds);
        item.put("status", Boolean.TRUE.equals(user.getStatus()) ? 0 : 1);
        item.put("remark", user.getSummary());
        item.put("createTime", user.getCreateTime() == null ? null : user.getCreateTime().toString());
        return item;
    }

    private void applyAdminPortalUserRequest(AdminUser user, Map<String, Object> request, boolean create) {
        if (request == null) {
            throw new BaseException(ResultCodeEnum.PARAM_ERROR);
        }
        validateMaxLength(toStringValue(request, "username"), 30);
        validateMaxLength(toStringValue(request, "realName"), 20);
        validateMaxLength(toStringValue(request, "email"), 150);
        validateMaxLength(toStringValue(request, "phone"), 50);
        validateMaxLength(toStringValue(request, "remark"), 255);

        if (StringUtils.hasText(toStringValue(request, "username"))) {
            user.setUsername(toStringValue(request, "username"));
        }
        if (StringUtils.hasText(toStringValue(request, "realName"))) {
            user.setNickname(toStringValue(request, "realName"));
        }
        if (request.containsKey("email")) {
            user.setEmail(toStringValue(request, "email"));
        }
        if (request.containsKey("phone")) {
            user.setPhone(toStringValue(request, "phone"));
        }
        if (request.containsKey("remark")) {
            user.setSummary(toStringValue(request, "remark"));
        }

        Integer status = toInteger(request.get("status"));
        if (status != null) {
            user.setStatus(status == 0);
        } else if (create && user.getStatus() == null) {
            user.setStatus(false);
        }

        String password = toStringValue(request, "password");
        if (StringUtils.hasText(password)) {
            user.setPassword(passwordEncoder.encode(password));
        } else if (create) {
            user.setPassword(passwordEncoder.encode("admin123"));
        }
        if (create && user.getSex() == null) {
            user.setSex((byte) 1);
        }
    }

    private Map<Long, Long> getDeptByUserId(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userDeptMapper.selectList(Wrappers.<UserDept>lambdaQuery().in(UserDept::getUserId, userIds))
                .stream()
                .collect(Collectors.toMap(UserDept::getUserId, UserDept::getDeptId, (left, right) -> left));
    }

    private Map<Long, List<String>> getRolesByUserId(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userRoleMapper.selectList(Wrappers.<UserRole>lambdaQuery().in(UserRole::getUserId, userIds))
                .stream()
                .collect(Collectors.groupingBy(
                        UserRole::getUserId,
                        Collectors.mapping(userRole -> toId(userRole.getRoleId()), Collectors.toList())
                ));
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

    private void replaceUserRoles(Long userId, List<String> roleIds) {
        userRoleMapper.deleteBatchIdsByUserIds(List.of(userId));
        for (Long roleId : parseIdList(roleIds)) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
        }
    }

    private Map<String, Object> pageResult(List<Map<String, Object>> items, long total) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("items", items);
        result.put("total", total);
        return result;
    }

    private List<Long> parseIdList(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ids.stream()
                .filter(StringUtils::hasText)
                .map(this::parseRequiredLong)
                .distinct()
                .toList();
    }

    private List<String> toStringList(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(Object::toString).toList();
        }
        return List.of();
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

    private String normalizeAvatar(String avatar) {
        if (!StringUtils.hasText(avatar)) {
            return null;
        }

        String value = avatar.trim();
        if (value.startsWith("http://")
                || value.startsWith("https://")
                || value.startsWith("data:")
                || value.startsWith("blob:")
                || value.startsWith("svg:")) {
            return value;
        }

        String normalizedPath = value.startsWith("/") ? value : "/" + value;
        String normalizedDomain = normalizePathPrefix(localFileDomain);
        String localFilePrefix = normalizedDomain + "/";
        if (normalizedPath.startsWith(localFilePrefix)) {
            String relativePath = normalizedPath.substring(localFilePrefix.length());
            return isExistingLocalFile(relativePath) ? normalizedPath : null;
        }

        if (normalizedPath.startsWith("/avatar/")) {
            String relativePath = normalizedPath.substring(1);
            return isExistingLocalFile(relativePath) ? normalizedDomain + normalizedPath : null;
        }

        return null;
    }

    private String normalizePathPrefix(String pathPrefix) {
        String prefix = StringUtils.hasText(pathPrefix) ? pathPrefix.trim() : "/api/local-file";
        if (!prefix.startsWith("/")) {
            prefix = "/" + prefix;
        }
        while (prefix.endsWith("/") && prefix.length() > 1) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        return prefix;
    }

    private boolean isExistingLocalFile(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            return false;
        }

        Path storageRoot = Path.of(localStoragePath).toAbsolutePath().normalize();
        Path filePath = storageRoot.resolve(relativePath).normalize();
        return filePath.startsWith(storageRoot) && Files.isRegularFile(filePath);
    }

    private void validateMaxLength(String value, int maxLength) {
        if (value != null && value.length() > maxLength) {
            throw new BaseException(ResultCodeEnum.PARAM_ERROR);
        }
    }
}
