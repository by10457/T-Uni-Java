package t.uni.configuration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import t.uni.domain.configuration.dto.MenuIconDto;
import t.uni.domain.configuration.entity.MenuIcon;
import t.uni.domain.configuration.vo.MenuIconVo;

/**
 * <p>
 * 系统菜单图标 Mapper 接口
 * </p>
 *
 * @since 2024-10-02 12:18:29
 */
@Mapper
public interface MenuIconMapper extends BaseMapper<MenuIcon> {

    /**
     * * 分页查询系统菜单图标内容
     *
     * @param pageParams 系统菜单图标分页参数
     * @param dto        系统菜单图标查询表单
     * @return 系统菜单图标分页结果
     */
    IPage<MenuIconVo> selectListByPage(@Param("page") Page<MenuIcon> pageParams, @Param("dto") MenuIconDto dto);

}
