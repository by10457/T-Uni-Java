package t.uni.server.im.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Objects;

/**
 * OpenIM 配置属性
 *
 * @author t-uni
 * @since 2026-04-24
 */
@Data
@ConfigurationProperties(prefix = "openim")
public class OpenImProperties {

    /** 是否启用 IM */
    private boolean enabled = false;

    /** OpenIM REST API 地址 */
    private String apiAddress;

    /** OpenIM WebSocket 地址 */
    private String wsAddress;

    /** 管理员 userID */
    private String adminUserId;

    /** 管理员 secret */
    private String adminSecret;

    /** userID 前缀 */
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

    /** admin token 刷新提前量（秒） */
    private long adminTokenRefreshAheadSeconds = 60;

    /** 用户注册锁 TTL（秒） */
    private long userRegisterLockSeconds = 5;

    private Http http = new Http();
    private Webhook webhook = new Webhook();

    /**
     * 构建 OpenIM userID
     */
    public String buildImUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        return userIdPrefix + userId;
    }

    /**
     * 是否系统通知发送者
     */
    public boolean isSystemNoticeSender(String openimUserId) {
        return Objects.equals(systemNoticeUserId, openimUserId);
    }

    @Data
    public static class Http {
        /** HTTP 超时时间（毫秒） */
        private int timeoutMs = 5000;
    }

    @Data
    public static class Webhook {
        /** Webhook 共享密钥（用于 URL token 校验） */
        private String token;
    }
}
