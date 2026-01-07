package t.uni.configuration.service;

import com.baomidou.mybatisplus.extension.service.IService;
import t.uni.domain.configuration.dto.I18nTypeDto;
import t.uni.domain.configuration.entity.I18nType;
import t.uni.domain.configuration.vo.I18nTypeVo;

import java.util.List;

/**
 * <p>
 * 多语言类型表 服务类
 * </p>
 *
 * @since 2024-09-28
 */
public interface I18nTypeService extends IService<I18nType> {

    /**
     * 获取多语言类型
     *
     * @return 多语言类型列表
     */
    List<I18nTypeVo> getI18nTypeList(I18nTypeDto dto);

    /**
     * 添加多语言类型
     *
     * @param dto 多语言类型添加
     */
    void createI18nType(I18nTypeDto dto);

    /**
     * 更新多语言类型
     *
     * @param dto 多语言类型更新
     */
    void updateI18nType(I18nTypeDto dto);

    /**
     * 删除多语言类型
     *
     * @param ids 删除id列表
     */
    void deleteI18nType(List<Long> ids);
}
