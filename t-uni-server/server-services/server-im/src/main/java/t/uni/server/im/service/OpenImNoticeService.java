package t.uni.server.im.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import t.uni.common.core.exception.BaseException;
import t.uni.common.core.result.ResultCodeEnum;
import t.uni.server.im.ImResultCodeEnum;
import t.uni.server.im.client.OpenImApiClient;
import t.uni.server.im.config.ConditionalOnOpenImEnabled;
import t.uni.server.im.config.OpenImProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * OpenIM 系统通知直发服务（同步，不走 outbox）
 * <p>
 * 适合用户操作链路中必须立即反馈的轻量通知。外部发送失败会直接抛业务异常，
 * 不在本服务内做异步补偿或持久化重试。
 *
 * @author t-uni
 * @since 2026-04-24
 */
@Slf4j
@Service
@ConditionalOnOpenImEnabled
@RequiredArgsConstructor
public class OpenImNoticeService {

    private static final int SESSION_TYPE_SYSTEM = 4;
    private static final int CONTENT_TYPE_SYSTEM_NOTICE = 1400;

    private final OpenImApiClient openImApiClient;
    private final OpenImProperties openImProperties;
    private final OpenImAdminTokenProvider openImAdminTokenProvider;
    private final OpenImUserSyncService openImUserSyncService;

    /**
     * 向单个用户同步发送系统通知。
     * <p>
     * 目标用户未导入 OpenIM 时会先按需注册；OpenIM 发送失败直接抛出 IM 错误码异常。
     *
     * @param targetUserId 本地目标用户 ID
     * @param text 通知正文
     */
    public void sendSystemNotice(Long targetUserId, String text) {
        var targetOpenimUserId = openImProperties.buildImUserId(targetUserId);
        var targetUser = openImUserSyncService.getUser(targetUserId);
        if (targetUser == null) {
            throw new BaseException(ResultCodeEnum.USER_IS_EMPTY);
        }
        if (!Boolean.TRUE.equals(targetUser.getImRegistered())) {
            openImUserSyncService.registerUser(targetUser, targetOpenimUserId);
            openImUserSyncService.markRegistered(targetUserId);
        }
        var payload = buildPayload(targetOpenimUserId, text);
        var adminToken = openImAdminTokenProvider.getAdminToken();
        var response = openImApiClient.sendMessage(adminToken, payload);
        if (!response.isSuccess()) {
            throw new BaseException(ImResultCodeEnum.IM_OPENIM_SEND_MSG_FAIL.getCode(),
                "发送系统通知失败: " + response.getErrMsg());
        }
    }

    /**
     * 组装 OpenIM 系统通知消息体。
     * <p>
     * sendID 必须是已初始化的通知账号，否则 OpenIM 会返回无权限错误。
     */
    private Map<String, Object> buildPayload(String targetOpenimUserId, String text) {
        var content = new HashMap<String, Object>();
        content.put("notificationName", openImProperties.getSystemNoticeNickname());
        content.put("notificationFaceURL", openImProperties.getSystemNoticeAvatar());
        content.put("notificationType", 2);
        content.put("text", text);
        content.put("mixType", 0);

        var payload = new HashMap<String, Object>();
        payload.put("sendID", openImProperties.getSystemNoticeUserId());
        payload.put("recvID", targetOpenimUserId);
        payload.put("senderNickname", openImProperties.getSystemNoticeNickname());
        payload.put("senderFaceURL", openImProperties.getSystemNoticeAvatar());
        payload.put("senderPlatformID", 1);
        payload.put("sessionType", SESSION_TYPE_SYSTEM);
        payload.put("contentType", CONTENT_TYPE_SYSTEM_NOTICE);
        payload.put("content", content);
        return payload;
    }
}
