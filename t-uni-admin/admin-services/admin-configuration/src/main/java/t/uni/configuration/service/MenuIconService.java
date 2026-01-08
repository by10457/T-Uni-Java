package t.uni.configuration.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import t.uni.common.core.result.PageResult;
import t.uni.domain.configuration.dto.MenuIconDto;
import t.uni.domain.configuration.entity.MenuIcon;
import t.uni.domain.configuration.vo.MenuIconVo;

import java.util.List;

/**
 * <p>
 * 系统菜单图标 服务类
 * </p>
 *
 * @since 2024-10-02 12:18:29
 */
public interface MenuIconService extends IService<MenuIcon> {

    /**
     * * 获取系统菜单图标列表
     *
     * @return 系统菜单图标返回列表
     */
    PageResult<MenuIconVo> getMenuIconPage(Page<MenuIcon> pageParams, MenuIconDto dto);

    /**
     * * 添加系统菜单图标
     *
     * @param dto 添加表单
     */
    void createMenuIcon(MenuIconDto dto);

    /**
     * * 更新系统菜单图标
     *
     * @param dto 更新表单
     */
    void updateMenuIcon(MenuIconDto dto);

    /**
     * * 删除|批量删除系统菜单图标类型
     *
     * @param ids 删除id列表
     */
    void deleteMenuIcon(List<Long> ids);

    /**
     * * 获取查询图标名称列表
     *
     * @param iconName 查询图标名称
     * @return 图标返回列表
     */
    List<MenuIconVo> getIconNameListByIconName(String iconName);
}
