package t.uni.system.core.cache;

import jakarta.annotation.Resource;
import lombok.Value;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import t.uni.common.core.utils.JwtTokenUtil;
import t.uni.domain.common.constant.LocalDateTimeConstant;
import t.uni.domain.common.constant.RedisUserConstant;
import t.uni.domain.common.model.vo.LoginVo;
import t.uni.domain.system.entity.AdminUser;
import t.uni.domain.system.entity.Permission;
import t.uni.domain.system.entity.Role;
import t.uni.system.mapper.PermissionMapper;
import t.uni.system.mapper.RoleMapper;
import t.uni.system.utils.RoleHelper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UserLoginVoBuilderCacheService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private PermissionMapper permissionMapper;

    public LoginVo buildLoginUserVo(AdminUser user, long readMeDay) {
        String username = user.getUsername();
        Long userId = user.getId();

        UserAuthInfo authInfo = getAuthInfo(userId, user);

        // 设置用户返回对象
        LoginVo loginVo = new LoginVo();
        BeanUtils.copyProperties(user, loginVo);
        loginVo.setPersonDescription(user.getSummary());
        loginVo.setRoles(authInfo.getRoles());
        loginVo.setPermissions(authInfo.getPermissions());

        // 使用用户名创建token
        String token = JwtTokenUtil.createToken(userId, username, (int) readMeDay);
        loginVo.setToken(token);
        loginVo.setRefreshToken(token);
        loginVo.setReadMeDay(readMeDay);

        // 计算过期时间，并格式化返回
        String expires = calculateExpires(readMeDay);
        loginVo.setExpires(expires);

        String loginInfoPrefix = RedisUserConstant.getUserLoginInfoPrefix(username);
        redisTemplate.opsForValue().set(loginInfoPrefix, loginVo, readMeDay, TimeUnit.DAYS);
        return loginVo;
    }

    private UserAuthInfo getAuthInfo(Long userId, AdminUser user) {
        // 用户角色
        List<String> roles = roleMapper.selectListByUserId(userId).stream().map(Role::getRoleCode).collect(Collectors.toList());

        // 判断是否是 admin 如果是admin 赋予所有权限
        List<String> permissions = new ArrayList<>();
        boolean isAdmin = RoleHelper.checkAdmin(roles, permissions, user);
        if (!isAdmin) {
            permissions = permissionMapper.selectListByUserId(userId).stream()
                    .map(Permission::getPowerCode)
                    .toList();
        }

        // 为这两个去重，在判断 checkAdmin 会重新赋值
        permissions = permissions.stream().distinct().toList();
        roles = roles.stream().distinct().toList();

        return new UserAuthInfo(roles, permissions);
    }

    private String calculateExpires(long readMeDay) {
        LocalDateTime localDateTime = LocalDateTime.now();
        LocalDateTime plusDay = localDateTime.plusDays(readMeDay);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(LocalDateTimeConstant.YYYY_MM_DD_HH_MM_SS_SLASH);
        return plusDay.format(dateTimeFormatter);
    }

    @Value
    private static class UserAuthInfo {
        List<String> roles;
        List<String> permissions;
    }
}
