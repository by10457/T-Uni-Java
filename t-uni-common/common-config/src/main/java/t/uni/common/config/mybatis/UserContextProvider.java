package t.uni.common.config.mybatis;

/**
 * 用户上下文提供者接口。
 * <p>
 * 用于解耦公共 MyBatis 自动填充和业务侧登录上下文。
 * 各应用按自身认证方案实现，公共模块不直接依赖网关、Security 或 ThreadLocal 细节。
 * </p>
 */
public interface UserContextProvider {
    /**
     * 获取当前登录用户 ID。
     *
     * @return 当前用户 ID；未登录或无法获取时返回 null
     */
    Long getCurrentUserId();

    /**
     * 获取当前登录用户名。
     *
     * @return 当前用户名；未登录或无法获取时返回 null
     */
    String getCurrentUsername();
}
