package t.uni.system.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import t.uni.common.config.jwt.JwtTokenUtil;
import t.uni.common.core.exception.BaseException;
import t.uni.common.core.result.ResultCodeEnum;
import t.uni.core.context.BaseContext;
import t.uni.core.utils.IpUtil;
import t.uni.domain.common.constant.RedisUserConstant;
import t.uni.domain.common.constant.UserConstant;
import t.uni.domain.common.enums.EmailTemplateEnums;
import t.uni.domain.common.enums.LoginEnums;
import t.uni.domain.common.model.vo.LoginVo;
import t.uni.domain.configuration.entity.EmailTemplate;
import t.uni.domain.system.dto.user.AdminUserUpdateByLocalUserDto;
import t.uni.domain.system.dto.user.LoginDto;
import t.uni.domain.system.dto.user.RefreshTokenDto;
import t.uni.domain.system.entity.AdminUser;
import t.uni.domain.system.entity.UserLoginLog;
import t.uni.domain.system.vo.user.RefreshTokenVo;
import t.uni.system.core.cache.EmailCacheService;
import t.uni.system.core.cache.UserLoginVoBuilderCacheService;
import t.uni.system.core.event.ClearAllUserCacheEvent;
import t.uni.system.core.login.DefaultLoginStrategy;
import t.uni.system.core.login.EmailLoginStrategy;
import t.uni.system.core.login.LoginContext;
import t.uni.system.core.login.LoginStrategy;
import t.uni.system.core.template.email.ConcreteSenderEmailTemplate;
import t.uni.system.mapper.EmailTemplateMapper;
import t.uni.system.mapper.UserLoginLogMapper;
import t.uni.system.mapper.UserMapper;
import t.uni.system.service.UserLoginService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class UserLoginServiceImpl extends ServiceImpl<UserMapper, AdminUser> implements UserLoginService {

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private UserMapper userMapper;

    @Resource
    private EmailTemplateMapper emailTemplateMapper;

    @Resource
    private UserLoginLogMapper userLoginLogMapper;

    @Resource
    private ConcreteSenderEmailTemplate concreteSenderEmailTemplate;

    @Resource
    private UserLoginVoBuilderCacheService userLoginVoBuilderCacheService;

    @Resource
    private EmailCacheService emailCacheService;

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    @Value("${dromara.local-plus.domain:/api/local-file}")
    private String localFileDomain;

    @Value("${dromara.local-plus.storage-path:/tmp/t-uni-admin/}")
    private String localStoragePath;

    /**
     * 前台用户登录接口
     * 这里不用判断用户是否为空，因为在登录时已经校验过了
     * <p>
     * 抛出异常使用自带的 UsernameNotFoundException 或者自己封装<br/>
     * 但是这两个效果传入参数都是一样的，所以全部使用 UsernameNotFoundException
     * </p>
     *
     * @param loginDto 登录参数
     * @return 登录后结果返回
     */
    @Override
    public LoginVo login(LoginDto loginDto) {
        // 初始化所有策略（可扩展）
        HashMap<String, LoginStrategy> loginStrategyHashMap = new HashMap<>();
        // 默认的登录方式
        loginStrategyHashMap.put(LoginEnums.default_STRATEGY.getValue(), new DefaultLoginStrategy(userMapper));
        // 注册邮箱
        loginStrategyHashMap.put(LoginEnums.EMAIL_STRATEGY.getValue(), new EmailLoginStrategy(emailCacheService, userMapper));

        // 使用登录上下文调用登录策略
        LoginContext loginContext = new LoginContext(loginStrategyHashMap);
        AdminUser user = loginContext.executeStrategy(loginDto);

        // 验证登录逻辑
        if (user == null) throw new UsernameNotFoundException(ResultCodeEnum.USER_IS_EMPTY.getMessage());

        // 数据库密码
        String dbPassword = user.getPassword();
        String password = loginDto.getPassword();
        if (!passwordEncoder.matches(password, dbPassword)) {
            throw new UsernameNotFoundException(ResultCodeEnum.LOGIN_ERROR.getMessage());
        }

        // 判断用户是否禁用
        if (user.getStatus()) {
            throw new UsernameNotFoundException(ResultCodeEnum.FAIL_NO_ACCESS_DENIED_USER_LOCKED.getMessage());
        }

        // 登录结束后的操作
        loginContext.loginAfter(loginDto, user);

        // 获取IP地址并更新用户登录信息，
        String ipAddr = IpUtil.getCurrentUserIpAddress().getIpAddr();
        String ipRegion = IpUtil.getCurrentUserIpAddress().getIpRegion();
        // 设置用户IP地址，并更新用户信息
        user.setIpAddress(ipAddr);
        user.setIpRegion(ipRegion);

        // 设置用户创建用户id 和 更新用户id
        user.setCreateUser(user.getId());
        user.setUpdateUser(user.getId());
        updateById(user);

        // 构建用户返回对象
        Long readMeDay = loginDto.getReadMeDay();
        LoginVo loginVo = userLoginVoBuilderCacheService.buildLoginUserVo(user, readMeDay);

        // 将用户登录保存在用户登录日志表中
        setUserLoginLog(user, loginVo.getToken(), UserConstant.LOGIN);

        return loginVo;
    }

    @Override
    public Map<String, Object> adminPortalLogin(Map<String, Object> request) {
        String username = toStringValue(request, "username");
        String password = toStringValue(request, "password");
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new BaseException(ResultCodeEnum.LOGIN_ERROR_USERNAME_PASSWORD_NOT_EMPTY);
        }

        LoginDto loginDto = new LoginDto();
        loginDto.setUsername(username);
        loginDto.setPassword(password);
        loginDto.setType("default");
        loginDto.setReadMeDay(1L);

        LoginVo loginVo;
        try {
            loginVo = login(loginDto);
        } catch (UsernameNotFoundException exception) {
            throw toLoginBusinessException(exception);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("accessToken", loginVo.getToken());
        data.put("refreshToken", loginVo.getRefreshToken());
        data.put("expires", loginVo.getExpires());
        return data;
    }

    @Override
    public Map<String, Object> adminPortalRefresh() {
        LoginVo loginVo = BaseContext.getLoginVo();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("accessToken", loginVo.getToken());
        data.put("refreshToken", loginVo.getRefreshToken());
        return data;
    }

    @Override
    public Map<String, Object> adminPortalUserInfo() {
        LoginVo loginVo = BaseContext.getLoginVo();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userId", BaseContext.getUserId() == null ? null : BaseContext.getUserId().toString());
        data.put("username", loginVo.getUsername());
        data.put("realName", StringUtils.hasText(loginVo.getNickname()) ? loginVo.getNickname() : loginVo.getUsername());
        data.put("avatar", normalizeAvatar(loginVo.getAvatar()));
        data.put("roles", loginVo.getRoles());
        data.put("desc", loginVo.getPersonDescription());
        data.put("homePath", "/analytics");
        return data;
    }

    /**
     * 发送登录邮件验证码
     *
     * <p>完整处理流程：</p>
     * <ol>
     *   <li><b>查询模板</b>：从数据库获取默认的验证码邮件模板</li>
     *   <li><b>生成验证码</b>：创建4位数字验证码</li>
     *   <li><b>模板处理</b>：替换模板中的动态变量（系统名称、验证码等）</li>
     *   <li><b>发送邮件</b>：通过邮件服务发送处理后的模板</li>
     *   <li><b>缓存验证码</b>：将验证码存入Redis 有效期 xxx</li>
     * </ol>
     *
     * @param email 接收邮箱地址（不可为空）
     *              <ul>
     *                <li>未找到默认邮件模板</li>
     *                <li>邮件发送失败</li>
     *                <li>Redis操作异常</li>
     *              </ul>
     * @see EmailTemplateEnums#VERIFICATION_CODE 验证码模板类型枚举
     * @see RedisUserConstant  Redis键和过期时间常量
     */
    @Override
    public void sendLoginEmail(@NotNull String email) {
        // 查询验证码邮件模板
        LambdaQueryWrapper<EmailTemplate> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(EmailTemplate::getIsDefault, true);
        lambdaQueryWrapper.eq(EmailTemplate::getType, EmailTemplateEnums.VERIFICATION_CODE.getType());
        EmailTemplate emailTemplate = emailTemplateMapper.selectOne(lambdaQueryWrapper);

        // 生成验证码
        CircleCaptcha captcha = CaptchaUtil.createCircleCaptcha(150, 48, 4, 2);
        String emailCode = captcha.getCode();

        // 需要替换模板内容
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("#title#", "BunnyAdmin");
        hashMap.put("#verifyCode#", emailCode);
        hashMap.put("#expires#", 15);
        hashMap.put("#sendEmailUser#", emailTemplate.getEmailUser());
        hashMap.put("#companyName#", "BunnyAdmin");

        // 发送邮件
        concreteSenderEmailTemplate.sendEmailTemplate(email, emailTemplate, hashMap);
        emailCacheService.buildEmailCodeCache(email, emailCode);
    }

    /**
     * 刷新用户token
     *
     * @param dto 请求token
     * @return 刷新token返回内容
     */
    @NotNull
    @Override
    public RefreshTokenVo refreshToken(@NotNull RefreshTokenDto dto) {
        Long userId = JwtTokenUtil.getUserId(dto.getRefreshToken());
        AdminUser adminUser = getOne(Wrappers.<AdminUser>lambdaQuery().eq(AdminUser::getId, userId));

        // 用户存在且没有禁用
        if (adminUser == null) throw new BaseException(ResultCodeEnum.USER_IS_EMPTY);
        if (adminUser.getStatus()) throw new BaseException(ResultCodeEnum.FAIL_NO_ACCESS_DENIED_USER_LOCKED);

        // 构建 LoginVo 对象
        LoginVo loginVo = userLoginVoBuilderCacheService.buildLoginUserVo(adminUser, dto.getReadMeDay());
        RefreshTokenVo refreshTokenVo = new RefreshTokenVo();
        BeanUtils.copyProperties(loginVo, refreshTokenVo);

        return refreshTokenVo;
    }

    /**
     * 退出登录
     */
    @Override
    public void logout() {
        // 获取上下文对象中的用户ID和用户token
        LoginVo loginVo = BaseContext.getLoginVo();
        String token = loginVo.getToken();
        Long userId = BaseContext.getUserId();

        // 获取IP地址
        String ipAddr = IpUtil.getCurrentUserIpAddress().getIpAddr();
        String ipRegion = IpUtil.getCurrentUserIpAddress().getIpRegion();

        // 查询用户信息
        AdminUser adminUser = getOne(Wrappers.<AdminUser>lambdaQuery().eq(AdminUser::getId, userId));
        adminUser.setIpAddress(ipAddr);
        adminUser.setIpRegion(ipRegion);
        setUserLoginLog(adminUser, token, UserConstant.LOGOUT);

        // 删除Redis中用户信息
        String username = adminUser.getUsername();
        applicationEventPublisher.publishEvent(new ClearAllUserCacheEvent(this, username));
    }

    /**
     * 更新本地用户信息
     *
     * @param dto 用户信息
     */
    @Override
    public void updateAdminUserByLocalUser(AdminUserUpdateByLocalUserDto dto) {
        Long userId = BaseContext.getUserId();

        // 判断是否存在这个用户
        AdminUser user = getOne(Wrappers.<AdminUser>lambdaQuery().eq(AdminUser::getId, userId));
        if (user == null) throw new BaseException(ResultCodeEnum.USER_IS_EMPTY);

        // 更新用户
        BeanUtils.copyProperties(dto, user);
        updateById(user);

        // 重新生成用户信息到Redis中
        userLoginVoBuilderCacheService.buildLoginUserVo(user, RedisUserConstant.REDIS_EXPIRATION_TIME);
    }

    /**
     * 更新本地用户密码
     *
     * @param password 更新本地用户密码
     */
    @Override
    public void updateUserPasswordByLocalUser(String password) {
        // 根据当前用户查询用户信息
        Long userId = BaseContext.getUserId();
        AdminUser adminUser = getOne(Wrappers.<AdminUser>lambdaQuery().eq(AdminUser::getId, userId));

        // 判断用户是否存在
        if (adminUser == null) throw new BaseException(ResultCodeEnum.USER_IS_EMPTY);

        // 数据库中的密码
        String dbPassword = adminUser.getPassword();

        // 判断数据库中密码是否和更新用户密码相同
        if (passwordEncoder.matches(password, dbPassword)) {
            throw new BaseException(ResultCodeEnum.NEW_PASSWORD_SAME_OLD_PASSWORD);
        }

        // 更新用户密码
        String encodePassword = passwordEncoder.encode(password);
        adminUser = new AdminUser();
        adminUser.setId(userId);
        adminUser.setPassword(encodePassword);
        updateById(adminUser);

        // 删除Redis中登录用户信息、角色、权限信息
        String username = adminUser.getUsername();
        applicationEventPublisher.publishEvent(new ClearAllUserCacheEvent(this, username));
    }

    /**
     * 设置用户登录日志内容
     * <p>
     * 该方法用于将管理员用户信息复制到用户登录日志对象中，同时处理特殊字段映射关系。
     * <p>
     * 实现说明：
     * 1. 使用BeanUtils.copyProperties()复制属性时，会自动将AdminUser.id复制到UserLoginLog.id
     * 2. 由于UserLoginLog实际需要的是userId字段而非id字段，需要特殊处理：
     * - 先进行属性复制
     * - 然后将UserLoginLog.userId设置为AdminUser.id
     * - 最后将UserLoginLog.id显式设为null（避免自动生成的id被覆盖）
     *
     * @param user  管理员用户实体对象，包含用户基本信息
     * @param token 本次登录/退出的认证令牌
     * @param type  操作类型（LOGIN-登录/LOGOUT-退出）
     */
    public void setUserLoginLog(AdminUser user, String token, String type) {
        UserLoginLog userLoginLog = new UserLoginLog();
        BeanUtils.copyProperties(user, userLoginLog);
        userLoginLog.setUserId(user.getId());
        userLoginLog.setId(null);
        userLoginLog.setToken(token);
        userLoginLog.setType(type);

        // 当前请求request
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            HttpServletRequest request = requestAttributes.getRequest();

            // 获取User-Agent
            String userAgent = request.getHeader("User-Agent");
            userLoginLog.setUserAgent(userAgent);

            // 获取X-Requested-With
            String xRequestedWith = request.getHeader("X-Requested-With");
            userLoginLog.setXRequestedWith(xRequestedWith);
        }

        userLoginLogMapper.insert(userLoginLog);
    }

    private String toStringValue(Map<String, Object> request, String key) {
        if (request == null) {
            return null;
        }
        Object value = request.get(key);
        return value == null ? null : value.toString();
    }

    private BaseException toLoginBusinessException(UsernameNotFoundException exception) {
        String message = exception.getMessage();
        if (ResultCodeEnum.USER_IS_EMPTY.getMessage().equals(message)) {
            return new BaseException(ResultCodeEnum.USER_IS_EMPTY);
        }
        if (ResultCodeEnum.FAIL_NO_ACCESS_DENIED_USER_LOCKED.getMessage().equals(message)) {
            return new BaseException(ResultCodeEnum.FAIL_NO_ACCESS_DENIED_USER_LOCKED);
        }
        return new BaseException(ResultCodeEnum.LOGIN_ERROR);
    }

    private String normalizeAvatar(String avatar) {
        if (!StringUtils.hasText(avatar)) {
            return null;
        }

        String value = avatar.trim();
        if (value.startsWith("http://")
                || value.startsWith("https://")
                || value.startsWith("data:")
                || value.startsWith("blob:")
                || value.startsWith("svg:")) {
            return value;
        }

        String normalizedPath = value.startsWith("/") ? value : "/" + value;
        String normalizedDomain = normalizePathPrefix(localFileDomain);
        String localFilePrefix = normalizedDomain + "/";
        if (normalizedPath.startsWith(localFilePrefix)) {
            String relativePath = normalizedPath.substring(localFilePrefix.length());
            return isExistingLocalFile(relativePath) ? normalizedPath : null;
        }

        if (normalizedPath.startsWith("/avatar/")) {
            String relativePath = normalizedPath.substring(1);
            return isExistingLocalFile(relativePath) ? normalizedDomain + normalizedPath : null;
        }

        return null;
    }

    private String normalizePathPrefix(String pathPrefix) {
        String prefix = StringUtils.hasText(pathPrefix) ? pathPrefix.trim() : "/api/local-file";
        if (!prefix.startsWith("/")) {
            prefix = "/" + prefix;
        }
        while (prefix.endsWith("/") && prefix.length() > 1) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        return prefix;
    }

    private boolean isExistingLocalFile(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            return false;
        }

        Path storageRoot = Path.of(localStoragePath).toAbsolutePath().normalize();
        Path filePath = storageRoot.resolve(relativePath).normalize();
        return filePath.startsWith(storageRoot) && Files.isRegularFile(filePath);
    }
}
