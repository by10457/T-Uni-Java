package t.uni.server.common.context;

/**
 * 当前请求的登录用户上下文。
 * <p>
 * 由认证拦截器在请求进入时写入用户 ID，业务代码只从本上下文读取当前登录用户。
 * 该上下文基于 {@link ThreadLocal}，必须在请求结束后清理，避免线程复用时串号。
 * </p>
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();

    /**
     * 获取当前请求绑定的用户 ID。
     *
     * @return 用户 ID；未经过认证拦截器或上下文已清理时返回 null
     */
    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }

    /**
     * 绑定当前请求的用户 ID。
     *
     * @param userId Token 校验通过后得到的用户 ID
     */
    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    /**
     * 清理当前线程上的用户上下文。
     * <p>
     * 只能由请求生命周期边界调用，避免影响同线程后续请求。
     * </p>
     */
    public static void clear() {
        USER_ID_HOLDER.remove();
    }
}
