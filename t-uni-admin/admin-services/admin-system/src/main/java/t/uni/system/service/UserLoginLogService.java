package t.uni.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import t.uni.common.core.result.PageResult;
import t.uni.domain.system.dto.user.UserLoginLogDto;
import t.uni.domain.system.entity.UserLoginLog;
import t.uni.domain.system.vo.user.UserLoginLogLocalVo;
import t.uni.domain.system.vo.user.UserLoginLogVo;

import java.util.List;

/**
 * <p>
 * 用户登录日志 服务类
 * </p>
 *
 * @since 2024-10-19 01:01:01
 */
public interface UserLoginLogService extends IService<UserLoginLog> {

    /**
     * * 获取用户登录日志列表
     *
     * @return 用户登录日志返回列表
     */
    PageResult<UserLoginLogVo> getUserLoginLogPage(Page<UserLoginLog> pageParams, UserLoginLogDto dto);

    /**
     * * 删除|批量删除用户登录日志类型
     *
     * @param ids 删除id列表
     */
    void deleteUserLoginLog(List<Long> ids);

    /**
     * * 获取本地用户登录日志
     *
     * @param pageParams 分页查询内容
     * @return 用户登录日志返回列表
     */
    PageResult<UserLoginLogLocalVo> getUserLoginLogPageByUser(Page<UserLoginLog> pageParams);
}
