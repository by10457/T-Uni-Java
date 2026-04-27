package t.uni.server.im.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Objects;

/**
 * OpenIM 配置属性
 * <p>
 * 承载服务端访问 OpenIM 所需的地址、管理员凭据、用户 ID 映射和 Webhook 校验配置。
 * 公开给客户端的配置只应从 Controller 白名单字段返回。
 *
 * @author t-uni
 * @since 2026-04-24
 */
@Data
@ConfigurationProperties(prefix = "openim")
public class OpenImProperties {

    /** 是否启用 OpenIM 可选模块 */
    private boolean enabled = false;

    /** OpenIM REST API 地址 */
    private String apiAddress;

    /** OpenIM WebSocket 地址 */
    private String wsAddress;

    /** OpenIM 管理员 userID，仅服务端使用 */
    private String adminUserId;

    /** OpenIM 管理员 secret，仅服务端用于换取 admin token */
    private String adminSecret;

    /** 本地用户 ID 映射为 OpenIM userID 时使用的前缀 */
    private String userIdPrefix = "tuni_";

    /** 系统通知发送者 OpenIM userID */
    private String systemNoticeUserId = "tuni_system";

    /** 系统通知发送者昵称 */
    private String systemNoticeNickname = "系统通知";

    /**
     * 系统通知头像
     * <p>
     * add_notification_account 的 faceURL 不能为空（OpenIM v3.8 protobuf 校验），
     * 因此提供一个默认值兜底。
     */
    private String systemNoticeAvatar = "https://img.icons8.com/color/96/appointment-reminders.png";

    /** 默认用户头像（用于 user_register 时 faceURL 为空的兜底） */
    private String defaultUserAvatar = "";

    /** admin token 刷新提前量（秒），用于避免临界过期 */
    private long adminTokenRefreshAheadSeconds = 60;

    /** 用户注册锁 TTL（秒） */
    private long userRegisterLockSeconds = 5;

    private Http http = new Http();
    private Webhook webhook = new Webhook();

    /**
     * 构建 OpenIM userID。
     *
     * @param userId 本地用户 ID
     * @return OpenIM userID，入参为空时返回 null
     */
    public String buildImUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        return userIdPrefix + userId;
    }

    /**
     * 判断指定 OpenIM 用户是否为系统通知发送者。
     *
     * @param openimUserId OpenIM userID
     * @return true 表示系统通知账号
     */
    public boolean isSystemNoticeSender(String openimUserId) {
        return Objects.equals(systemNoticeUserId, openimUserId);
    }

    /**
     * OpenIM HTTP 客户端配置。
     */
    @Data
    public static class Http {
        /** OpenIM HTTP 请求超时时间（毫秒） */
        private int timeoutMs = 5000;
    }

    /**
     * OpenIM Webhook 安全配置。
     */
    @Data
    public static class Webhook {
        /** Webhook 共享密钥（用于 URL token 校验） */
        private String token;
    }
}
