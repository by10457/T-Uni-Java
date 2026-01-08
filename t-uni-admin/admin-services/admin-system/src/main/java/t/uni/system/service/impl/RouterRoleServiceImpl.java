package t.uni.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import t.uni.common.core.exception.BaseException;
import t.uni.common.core.result.ResultCodeEnum;
import t.uni.domain.system.entity.RouterRole;
import t.uni.system.mapper.RouterRoleMapper;
import t.uni.system.service.RouterRoleService;

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
public class RouterRoleServiceImpl extends ServiceImpl<RouterRoleMapper, RouterRole> implements RouterRoleService {

    /**
     * * 根据路由id获取所有角色
     *
     * @param routerId 路由id
     * @return 角色列表
     */
    @Override
    public List<String> getRoleListByRouterId(Long routerId) {
        return list(Wrappers.<RouterRole>lambdaQuery().eq(RouterRole::getRouterId, routerId)).stream()
                .map(routerRole -> routerRole.getRoleId().toString()).toList();
    }

    /**
     * 清除选中菜单所有角色
     *
     * @param routerIds 路由id
     */
    @Override
    public void clearRouterRole(List<Long> routerIds) {
        if (routerIds.isEmpty()) {
            throw new BaseException(ResultCodeEnum.REQUEST_IS_EMPTY);
        }
        baseMapper.deleteBatchIdsByRouterIds(routerIds);
    }
}

