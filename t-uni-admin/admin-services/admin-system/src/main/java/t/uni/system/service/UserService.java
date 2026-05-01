package t.uni.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import t.uni.common.core.result.PageResult;
import t.uni.domain.system.dto.user.AdminUserAddDto;
import t.uni.domain.system.dto.user.AdminUserDto;
import t.uni.domain.system.dto.user.AdminUserUpdateDto;
import t.uni.domain.system.entity.AdminUser;
import t.uni.domain.system.vo.user.AdminUserVo;
import t.uni.domain.system.vo.user.UserVo;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户信息 服务类
 * </p>
 *
 * @since 2024-09-26
 */
public interface UserService extends IService<AdminUser> {

    /**
     * * 获取用户信息列表
     *
     * @return 用户信息返回列表
     */
    PageResult<AdminUserVo> getUserPageByAdmin(Page<AdminUser> pageParams, AdminUserDto dto);

    /**
     * 管理端用户分页列表。
     */
    Map<String, Object> adminPortalUserList(long page, long pageSize, Map<String, Object> query);

    void createAdminPortalUser(Map<String, Object> request);

    void updateAdminPortalUser(String id, Map<String, Object> request);

    void deleteAdminPortalUser(String id);

    /**
     * * 添加用户信息
     *
     * @param dto 添加表单
     */
    void createUserByAdmin(AdminUserAddDto dto);

    /**
     * * 更新用户信息
     *
     * @param dto 更新表单
     */
    void updateUserByAdmin(AdminUserUpdateDto dto);

    /**
     * * 删除|批量删除用户信息类型
     *
     * @param ids 删除id列表
     */
    void deleteUserByAdmin(List<Long> ids);

    /**
     * * 获取用户信息
     *
     * @param id 用户id
     * @return 用户信息
     */
    UserVo getUserinfoById(Long id);

    /**
     * * 强制退出
     *
     * @param id 用户id
     */
    void forcedOfflineByAdmin(Long id);

    /**
     * * 查询用户
     *
     * @param keyword 查询用户信息关键字
     * @return 用户信息列表
     */
    List<UserVo> getUserListByKeyword(String keyword);

    /**
     * 查询缓存中已登录用户
     *
     * @param pageParams 分页查询
     * @return 分页查询结果
     */
    PageResult<UserVo> getCacheUserPage(Page<AdminUser> pageParams);
}
