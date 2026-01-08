package t.uni.server.common.context;

/**
 * 用户上下文
 * <p>
 * 用于在当前线程中存储和获取当前登录用户的ID
 * </p>
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();

    /**
     * 获取当前用户ID
     *
     * @return 用户ID，未登录则返回 null
     */
    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }

    /**
     * 设置当前用户ID
     *
     * @param userId 用户ID
     */
    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    /**
     * 清理上下文
     */
    public static void clear() {
        USER_ID_HOLDER.remove();
    }
}
