package t.uni.system.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import t.uni.common.core.exception.BaseException;
import t.uni.common.core.result.PageResult;
import t.uni.common.core.result.ResultCodeEnum;
import t.uni.core.utils.FileUtil;
import t.uni.domain.common.model.dto.excel.RoleExcel;
import t.uni.domain.system.dto.RoleDto;
import t.uni.domain.system.entity.Role;
import t.uni.domain.system.entity.RouterRole;
import t.uni.domain.system.vo.RoleVo;
import t.uni.system.core.cache.RoleExcelListener;
import t.uni.system.core.event.UpdateUserinfoByRoleIdsEvent;
import t.uni.system.mapper.RoleMapper;
import t.uni.system.mapper.RouterMapper;
import t.uni.system.mapper.RouterRoleMapper;
import t.uni.system.mapper.UserRoleMapper;
import t.uni.system.service.RoleService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * <p>
 * 角色 服务实现类
 * </p>
 *
 * @since 2024-10-03 14:26:24
 */
@Service
@Transactional
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {
    private static final String CACHE_NAMES = "role";

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    @Resource
    private RouterRoleMapper routerRoleMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private RouterMapper routerMapper;

    /**
     * 角色 服务实现类
     *
     * @param pageParams 角色分页查询page对象
     * @param dto        角色分页查询对象
     * @return 查询分页角色返回对象
     */
    @Override
    public PageResult<RoleVo> getRolePage(Page<Role> pageParams, RoleDto dto) {
        IPage<RoleVo> page = baseMapper.selectListByPage(pageParams, dto);

        return PageResult.<RoleVo>builder()
                .list(page.getRecords())
                .pageNo(page.getCurrent())
                .pageSize(page.getSize())
                .total(page.getTotal())
                .build();
    }

    @Override
    public Map<String, Object> adminPortalRoleList(long page, long pageSize, Map<String, Object> query) {
        QueryWrapper<Role> wrapper = new QueryWrapper<Role>().eq("is_deleted", 0);
        String id = toStringValue(query, "id");
        String name = toStringValue(query, "name");
        String remark = toStringValue(query, "remark");
        Integer status = toInteger(query == null ? null : query.get("status"));
        if (StringUtils.hasText(id)) {
            wrapper.eq("id", parseRequiredLong(id));
        }
        if (StringUtils.hasText(name)) {
            wrapper.like("role_code", name);
        }
        if (StringUtils.hasText(remark)) {
            wrapper.like("description", remark);
        }
        if (status != null) {
            wrapper.eq("status", toDisabledStatus(status));
        }
        wrapper.orderByAsc("id");

        Page<Role> rolePage = page(Page.of(page, pageSize), wrapper);
        List<Map<String, Object>> items = rolePage.getRecords().stream()
                .map(this::toAdminPortalRoleMap)
                .toList();
        return pageResult(items, rolePage.getTotal());
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = CACHE_NAMES, key = "'roleList'", beforeInvocation = true),
    })
    public void createAdminPortalRole(Map<String, Object> request) {
        String name = toStringValue(request, "name");
        validateMaxLength(name, 255);
        validateMaxLength(toStringValue(request, "remark"), 255);
        if (!StringUtils.hasText(name)) {
            throw new BaseException(ResultCodeEnum.PARAM_ERROR);
        }

        Role role = new Role();
        role.setRoleCode(name);
        role.setDescription(toStringValue(request, "remark"));
        role.setStatus(toDisabledStatus(toInteger(request.get("status"))));
        save(role);
        saveAdminPortalRoleMenus(role.getId(), toStringList(request.get("permissions")));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = CACHE_NAMES, key = "'roleList'", beforeInvocation = true),
    })
    public void updateAdminPortalRole(String id, Map<String, Object> request) {
        Long roleId = parseRequiredLong(id);
        Role role = getById(roleId);
        if (role == null) {
            throw new BaseException(ResultCodeEnum.DATA_NOT_EXIST);
        }

        boolean changed = false;
        String name = toStringValue(request, "name");
        String remark = toStringValue(request, "remark");
        Integer status = toInteger(request.get("status"));
        validateMaxLength(name, 255);
        validateMaxLength(remark, 255);
        if (StringUtils.hasText(name)) {
            role.setRoleCode(name);
            changed = true;
        }
        if (remark != null) {
            role.setDescription(remark);
            changed = true;
        }
        if (status != null) {
            role.setStatus(toDisabledStatus(status));
            changed = true;
        }
        if (changed) {
            updateById(role);
        }
        if (request.containsKey("permissions")) {
            saveAdminPortalRoleMenus(roleId, toStringList(request.get("permissions")));
        }
        applicationEventPublisher.publishEvent(new UpdateUserinfoByRoleIdsEvent(this, List.of(roleId)));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = CACHE_NAMES, key = "'roleList'", beforeInvocation = true),
    })
    public void deleteAdminPortalRole(String id) {
        Long roleId = parseRequiredLong(id);
        routerRoleMapper.deleteBatchIdsByRoleIds(List.of(roleId));
        userRoleMapper.deleteBatchIdsByRoleIds(List.of(roleId));
        applicationEventPublisher.publishEvent(new UpdateUserinfoByRoleIdsEvent(this, List.of(roleId)));
        removeByIds(List.of(roleId));
    }

    /**
     * * 获取所有角色
     *
     * @return 所有角色列表
     */
    @Override
    @Cacheable(cacheNames = CACHE_NAMES, key = "'roleList'", cacheManager = "cacheManagerWithMouth")
    public List<RoleVo> roleList() {
        return list().stream().map(role -> {
            RoleVo roleVo = new RoleVo();
            BeanUtils.copyProperties(role, roleVo);
            return roleVo;
        }).toList();
    }

    /**
     * 使用Excel导出导出角色列表
     *
     * @return Excel
     */
    @Override
    public ResponseEntity<byte[]> exportByExcel() {
        String timeFormat = new SimpleDateFormat("yyyy-MM-dd HH_mm_ss").format(new Date());
        String zipFilename = "role-" + timeFormat + ".zip";

        String dateFormat = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String filename = "role-" + dateFormat + ".xlsx";

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {

            List<RoleExcel> list = list().stream().map(role -> {
                RoleExcel roleExcel = new RoleExcel();
                BeanUtils.copyProperties(role, roleExcel);

                roleExcel.setId(role.getId().toString());
                return roleExcel;
            }).toList();

            // 创建临时ByteArrayOutputStream
            ByteArrayOutputStream excelOutputStream = new ByteArrayOutputStream();

            ZipEntry zipEntry = new ZipEntry(filename);
            zipOutputStream.putNextEntry(zipEntry);

            // 先写入到临时流
            EasyExcel.write(excelOutputStream, RoleExcel.class).sheet("role").doWrite(list);
            zipOutputStream.write(excelOutputStream.toByteArray());
            zipOutputStream.closeEntry();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 设置响应头
        HttpHeaders headers = FileUtil.buildHttpHeadersByBinary(zipFilename);

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        return new ResponseEntity<>(byteArrayInputStream.readAllBytes(), headers, HttpStatus.OK);
    }

    /**
     * 使用Excel更新角色列表
     * 不做任何操作有需要让用户重新登录
     *
     * @param file Excel文件
     */
    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = CACHE_NAMES, key = "'roleList'", beforeInvocation = true),
    })
    public void updateRoleByFile(MultipartFile file) {
        if (file == null) {
            throw new BaseException(ResultCodeEnum.REQUEST_IS_EMPTY);
        }

        InputStream fileInputStream;
        try {
            fileInputStream = file.getInputStream();
            EasyExcel.read(fileInputStream, RoleExcel.class, new RoleExcelListener(this)).sheet().doRead();
        } catch (IOException e) {
            throw new BaseException(ResultCodeEnum.UPLOAD_ERROR);
        }
    }

    /**
     * 添加角色
     *
     * @param dto 角色添加
     */
    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = CACHE_NAMES, key = "'roleList'", beforeInvocation = true),
    })
    public void addRole(RoleDto dto) {
        Role role = new Role();
        BeanUtils.copyProperties(dto, role);
        save(role);
    }

    /**
     * 更新角色信息及关联用户缓存
     *
     * <p><b>功能说明</b>：</p>
     * <ol>
     *   <li>更新角色基础信息</li>
     *   <li>触发关联用户权限信息更新（通过事件机制异步处理）</li>
     *   <li>清理相关缓存数据</li>
     * </ol>
     *
     * <p><b>处理流程</b>：</p>
     * <ol>
     *   <li>校验角色是否存在</li>
     *   <li>更新角色实体信息</li>
     *   <li>清理角色列表和用户角色列表缓存</li>
     * </ol>
     *
     * <p><b>注意事项</b>：</p>
     * <ul>
     *   <li>使用 {@code @CacheEvict} 在方法执行前清理缓存，保证数据一致性</li>
     *   <li>通过事件机制解耦用户权限更新操作，提高响应速度</li>
     * </ul>
     *
     * @param dto 角色更新数据传输对象，包含角色ID和更新字段
     * @throws BaseException 当指定角色不存在时抛出（错误码：DATA_NOT_EXIST）
     */
    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = CACHE_NAMES, key = "'roleList'", beforeInvocation = true),
    })
    public void updateRole(RoleDto dto) {
        // 查询更新的角色是否存在
        Long roleId = dto.getId();
        List<Role> roleList = list(Wrappers.<Role>lambdaQuery().eq(Role::getId, roleId));
        if (roleList.isEmpty()) throw new BaseException(ResultCodeEnum.DATA_NOT_EXIST);

        // 更新内容
        Role role = new Role();
        BeanUtils.copyProperties(dto, role);
        updateById(role);

        // 发布角色更新事件
        List<Long> ids = List.of(roleId);
        applicationEventPublisher.publishEvent(new UpdateUserinfoByRoleIdsEvent(this, ids));
    }

    /**
     * 删除|批量删除角色
     * 做的物理删除，已经将角色路由、角色权限做了外键
     *
     * @param ids 删除id列表
     */
    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = CACHE_NAMES, key = "'roleList'", beforeInvocation = true),
    })
    public void deleteRole(List<Long> ids) {
        // 判断数据请求是否为空
        if (ids.isEmpty()) throw new BaseException(ResultCodeEnum.REQUEST_IS_EMPTY);

        // 重新构建角色和用户缓存
        applicationEventPublisher.publishEvent(new UpdateUserinfoByRoleIdsEvent(this, ids));

        // 删除角色
        removeByIds(ids);
    }

    private Map<String, Object> toAdminPortalRoleMap(Role role) {
        List<String> permissions = routerRoleMapper.selectList(Wrappers.<RouterRole>lambdaQuery()
                        .eq(RouterRole::getRoleId, role.getId()))
                .stream()
                .map(RouterRole::getRouterId)
                .map(this::toId)
                .toList();

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", toId(role.getId()));
        item.put("name", role.getRoleCode());
        item.put("permissions", permissions);
        item.put("remark", role.getDescription());
        item.put("status", toFrontendStatus(role.getStatus()));
        item.put("createTime", role.getCreateTime() == null ? null : role.getCreateTime().toString());
        return item;
    }

    private void saveAdminPortalRoleMenus(Long roleId, List<String> permissions) {
        routerRoleMapper.deleteBatchIdsByRoleIds(List.of(roleId));
        for (Long routerId : parseIdList(permissions)) {
            if (routerMapper.selectById(routerId) == null) {
                continue;
            }
            RouterRole routerRole = new RouterRole();
            routerRole.setRoleId(roleId);
            routerRole.setRouterId(routerId);
            routerRoleMapper.insert(routerRole);
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

    @SuppressWarnings("unchecked")
    private List<String> toStringList(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(Object::toString).toList();
        }
        return List.of();
    }

    private String toStringValue(Map<String, Object> request, String key) {
        if (request == null) {
            return null;
        }
        Object value = request.get(key);
        return value == null ? null : value.toString();
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

    private Long parseRequiredLong(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException exception) {
            throw new BaseException(ResultCodeEnum.PARAM_ERROR);
        }
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
