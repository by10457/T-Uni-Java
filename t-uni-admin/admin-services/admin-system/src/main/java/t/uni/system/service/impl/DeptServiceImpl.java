package t.uni.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import t.uni.common.core.exception.BaseException;
import t.uni.common.core.result.PageResult;
import t.uni.common.core.result.ResultCodeEnum;
import t.uni.domain.system.dto.DeptDto;
import t.uni.domain.system.entity.Dept;
import t.uni.domain.system.vo.DeptVo;
import t.uni.system.mapper.DeptMapper;
import t.uni.system.mapper.UserDeptMapper;
import t.uni.system.service.DeptService;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 部门 服务实现类
 * </p>
 *
 * @since 2024-10-04 10:39:08
 */
@Service
@Transactional
public class DeptServiceImpl extends ServiceImpl<DeptMapper, Dept> implements DeptService {
    private static final String CACHE_NAMES = "dept";

    @Resource
    private UserDeptMapper userDeptMapper;

    /**
     * * 部门 服务实现类
     *
     * @param pageParams 部门分页查询page对象
     * @param dto        部门分页查询对象
     * @return 查询分页部门返回对象
     */
    @Override
    public PageResult<DeptVo> getDeptPage(Page<Dept> pageParams, DeptDto dto) {
        IPage<DeptVo> page = baseMapper.selectListByPage(pageParams, dto);

        return PageResult.<DeptVo>builder()
                .list(page.getRecords())
                .pageNo(page.getCurrent())
                .pageSize(page.getSize())
                .total(page.getTotal())
                .build();
    }

    @Override
    public List<Map<String, Object>> adminPortalDeptList() {
        List<Dept> depts = list(new QueryWrapper<Dept>()
                .eq("is_deleted", 0)
                .orderByAsc("parent_id", "create_time"));
        List<Map<String, Object>> items = depts.stream().map(this::toAdminPortalDeptMap).toList();
        return buildTree(items);
    }

    @Override
    @CacheEvict(cacheNames = CACHE_NAMES, key = "'deptList'", beforeInvocation = true)
    public void createAdminPortalDept(Map<String, Object> request) {
        Dept dept = new Dept();
        applyAdminPortalDeptRequest(dept, request);
        save(dept);
    }

    @Override
    @CacheEvict(cacheNames = CACHE_NAMES, key = "'deptList'", beforeInvocation = true)
    public void updateAdminPortalDept(String id, Map<String, Object> request) {
        Dept dept = getById(parseRequiredLong(id));
        if (dept == null) {
            throw new BaseException(ResultCodeEnum.DATA_NOT_EXIST);
        }
        applyAdminPortalDeptRequest(dept, request);
        updateById(dept);
    }

    @Override
    @CacheEvict(cacheNames = CACHE_NAMES, key = "'deptList'", beforeInvocation = true)
    public void deleteAdminPortalDept(String id) {
        deleteDept(List.of(parseRequiredLong(id)));
    }

    /**
     * * 获取所有部门
     *
     * @return 所有部门列表
     */
    @Override
    @Cacheable(cacheNames = CACHE_NAMES, key = "'deptList'", cacheManager = "cacheManagerWithMouth")
    public List<DeptVo> getDeptPage() {
        return list().stream().map(dept -> {
            DeptVo deptVo = new DeptVo();
            BeanUtils.copyProperties(dept, deptVo);
            return deptVo;
        }).toList();
    }

    /**
     * 添加部门
     *
     * @param dto 部门添加
     */
    @Override
    @CacheEvict(cacheNames = CACHE_NAMES, key = "'deptList'", beforeInvocation = true)
    public void createDept(DeptDto dto) {
        // 整理管理者人员
        String mangerList = dto.getManager().stream().map(String::trim).collect(Collectors.joining(","));

        // 保存数据
        Dept dept = new Dept();
        BeanUtils.copyProperties(dto, dept);
        dept.setManager(mangerList);

        save(dept);
    }

    /**
     * 更新部门
     *
     * @param dto 部门更新
     */
    @Override
    @CacheEvict(cacheNames = CACHE_NAMES, key = "'deptList'", beforeInvocation = true)
    public void updateDept(DeptDto dto) {
        if (dto.getId().equals(dto.getParentId())) throw new BaseException(ResultCodeEnum.ILLEGAL_DATA_REQUEST);

        // 将管理员用户逗号分隔
        String mangerList = dto.getManager().stream().map(String::trim).collect(Collectors.joining(","));

        // 更新内容
        Dept dept = new Dept();
        BeanUtils.copyProperties(dto, dept);
        dept.setManager(mangerList);

        updateById(dept);
    }

    /**
     * 删除|批量删除部门
     *
     * @param ids 删除id列表
     */
    @Override
    @CacheEvict(cacheNames = CACHE_NAMES, key = "'deptList'", beforeInvocation = true)
    public void deleteDept(List<Long> ids) {
        // 判断数据请求是否为空
        if (ids.isEmpty()) throw new BaseException(ResultCodeEnum.REQUEST_IS_EMPTY);

        // 删除当前部门
        removeByIds(ids);

        // 删除用户部门关联
        userDeptMapper.deleteBatchIdsByDeptIds(ids);
    }

    private Map<String, Object> toAdminPortalDeptMap(Dept dept) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", toId(dept.getId()));
        item.put("pid", StringUtils.hasText(dept.getParentId()) ? dept.getParentId() : "0");
        item.put("name", dept.getDeptName());
        item.put("remark", dept.getSummary());
        item.put("status", toFrontendStatus(dept.getStatus()));
        item.put("createTime", dept.getCreateTime() == null ? null : dept.getCreateTime().toString());
        return item;
    }

    private void applyAdminPortalDeptRequest(Dept dept, Map<String, Object> request) {
        String name = toStringValue(request, "name");
        validateMaxLength(name, 255);
        validateMaxLength(toStringValue(request, "remark"), 255);
        if (!StringUtils.hasText(name)) {
            throw new BaseException(ResultCodeEnum.PARAM_ERROR);
        }
        dept.setDeptName(name);
        dept.setParentId(StringUtils.hasText(toStringValue(request, "pid")) ? toStringValue(request, "pid") : "0");
        dept.setSummary(toStringValue(request, "remark"));
        dept.setStatus(toDisabledStatus(toInteger(request.get("status"))));
        if (!StringUtils.hasText(dept.getManager())) {
            dept.setManager("1");
        }
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
