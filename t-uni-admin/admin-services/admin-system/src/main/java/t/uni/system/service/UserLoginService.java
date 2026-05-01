package t.uni.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jetbrains.annotations.NotNull;
import t.uni.domain.common.model.vo.LoginVo;
import t.uni.domain.system.dto.user.AdminUserUpdateByLocalUserDto;
import t.uni.domain.system.dto.user.LoginDto;
import t.uni.domain.system.dto.user.RefreshTokenDto;
import t.uni.domain.system.entity.AdminUser;
import t.uni.domain.system.vo.user.RefreshTokenVo;

import java.util.Map;

public interface UserLoginService extends IService<AdminUser> {

    /**
     * 前台用户登录接口
     *
     * @param loginDto 登录参数
     * @return 登录后结果返回
     */
    LoginVo login(LoginDto loginDto);

    /**
     * 管理端登录，复用系统原有登录、JWT 和缓存逻辑，只适配前端字段名。
     *
     * @param request 登录请求
     * @return 管理端登录结果
     */
    Map<String, Object> adminPortalLogin(Map<String, Object> request);

    /**
     * 管理端刷新令牌响应。
     *
     * @return 当前登录令牌信息
     */
    Map<String, Object> adminPortalRefresh();

    /**
     * 管理端当前用户信息。
     *
     * @return 当前用户信息
     */
    Map<String, Object> adminPortalUserInfo();

    /**
     * 登录发送邮件验证码
     *
     * @param email 邮箱
     */
    void sendLoginEmail(@NotNull String email);

    /**
     * 刷新用户token
     *
     * @param dto 请求token
     * @return 刷新token返回内容
     */
    @NotNull
    RefreshTokenVo refreshToken(@NotNull RefreshTokenDto dto);

    /**
     * 退出登录
     */
    void logout();

    /**
     * 更新本地用户信息
     *
     * @param dto 用户信息
     */
    void updateAdminUserByLocalUser(AdminUserUpdateByLocalUserDto dto);

    /**
     * 更新本地用户密码
     *
     * @param password 更新本地用户密码
     */
    void updateUserPasswordByLocalUser(String password);

}
