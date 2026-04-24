package t.uni.server.im.client;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import t.uni.common.core.exception.BaseException;
import t.uni.server.im.ImResultCodeEnum;
import t.uni.server.im.config.ConditionalOnOpenImEnabled;
import t.uni.server.im.config.OpenImProperties;
import t.uni.server.im.support.OpenImApiResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * OpenIM REST API 客户端
 * <p>
 * operationID 必须放在请求头（Header）中，token 放在请求头（Header）中。
 *
 * @author t-uni
 * @since 2026-04-24
 */
@Slf4j
@Service
@ConditionalOnOpenImEnabled
@RequiredArgsConstructor
public class OpenImApiClient {

    private final OpenImProperties openImProperties;

    /**
     * 获取 admin token
     */
    public OpenImApiResponse getAdminToken() {
        var body = Map.of(
            "userID", openImProperties.getAdminUserId(),
            "secret", openImProperties.getAdminSecret()
        );
        return post("/auth/get_admin_token", body, null);
    }

    /**
     * 获取 user token
     */
    public OpenImApiResponse getUserToken(String adminToken, String userId, Integer platformId) {
        var body = Map.of(
            "userID", userId,
            "platformID", platformId
        );
        return post("/auth/get_user_token", body, adminToken);
    }

    /**
     * 注册用户
     */
    public OpenImApiResponse registerUsers(String adminToken, Object users) {
        var body = Map.of(
            "users", users
        );
        return post("/user/user_register", body, adminToken);
    }

    /**
     * 添加系统通知账号
     * <p>
     * OpenIM v3.8 约束：appMangerLevel 必须 >= 3，faceURL 不能为空
     */
    public OpenImApiResponse addNotificationAccount(String adminToken, String userID, String nickName, String faceURL) {
        var body = new HashMap<String, Object>();
        body.put("userID", userID);
        body.put("nickName", nickName);
        body.put("faceURL", faceURL);
        body.put("appMangerLevel", 3);
        return post("/user/add_notification_account", body, adminToken);
    }

    /**
     * 发送消息
     */
    public OpenImApiResponse sendMessage(String adminToken, Map<String, Object> payload) {
        return post("/msg/send_msg", payload, adminToken);
    }

    private OpenImApiResponse post(String path, Object body, String token) {
        var apiAddress = openImProperties.getApiAddress();
        if (StrUtil.isBlank(apiAddress)) {
            throw new BaseException(ImResultCodeEnum.IM_CONFIG_MISSING.getCode(),
                ImResultCodeEnum.IM_CONFIG_MISSING.getMessage());
        }
        var url = apiAddress + path;
        var operationId = buildOperationId();
        try (HttpResponse response = buildRequest(url, token, operationId)
            .body(JSONUtil.toJsonStr(body))
            .execute()) {
            var responseBody = response.body();
            if (StrUtil.isBlank(responseBody)) {
                log.error("OpenIM响应为空: url={}, operationID={}", url, operationId);
                return new OpenImApiResponse(-1, "OpenIM响应为空", "", null);
            }
            var json = JSONUtil.parseObj(responseBody);
            return new OpenImApiResponse(
                json.getInt("errCode", -1),
                json.getStr("errMsg", ""),
                json.getStr("errDlt", ""),
                json.get("data")
            );
        } catch (Exception e) {
            log.error("OpenIM请求失败: url={}, operationID={}, err={}", url, operationId, e.getMessage(), e);
            return new OpenImApiResponse(-1, "OpenIM请求异常", e.getMessage(), null);
        }
    }

    private HttpRequest buildRequest(String url, String token, String operationId) {
        var request = HttpRequest.post(url)
            .timeout(openImProperties.getHttp().getTimeoutMs())
            .header("Content-Type", "application/json")
            .header("operationID", operationId);
        if (StrUtil.isNotBlank(token)) {
            request.header("token", token);
        }
        return request;
    }

    private String buildOperationId() {
        return System.currentTimeMillis() + "-" + UUID.randomUUID().toString().replace("-", "");
    }
}
