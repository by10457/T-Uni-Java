package t.uni.common.config.mybatis;

/**
 * 用户上下文提供者接口
 * 用于解耦MybatisPlusFieldConfig和具体的上下文实现
 * 各项目需要实现此接口以提供当前用户信息
 */
public interface UserContextProvider {
    /**
     * 获取当前用户ID
     *
     * @return 当前用户ID，如果未登录或无法获取则返回null
     */
    Long getCurrentUserId();

    /**
     * 获取当前用户名
     *
     * @return 当前用户名，如果未登录或无法获取则返回null
     */
    String getCurrentUsername();
}
