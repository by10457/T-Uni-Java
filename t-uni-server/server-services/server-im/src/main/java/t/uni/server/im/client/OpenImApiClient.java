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
 * 仅封装 IM 模块当前使用的服务端接口，不缓存 token，也不处理业务幂等。
 * OpenIM 要求 operationID 和 token 放在请求头中；除本地配置缺失外，
 * 外部接口异常会转换为 {@link OpenImApiResponse}，由上层决定是否重试或抛错。
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
     * 使用管理员账号密钥向 OpenIM 换取 admin token。
     *
     * @return OpenIM 原始响应，失败时 errCode 非 0
     */
    public OpenImApiResponse getAdminToken() {
        var body = Map.of(
            "userID", openImProperties.getAdminUserId(),
            "secret", openImProperties.getAdminSecret()
        );
        return post("/auth/get_admin_token", body, null);
    }

    /**
     * 通过 admin token 为指定 OpenIM 用户签发登录 token。
     *
     * @param adminToken OpenIM 管理员 token
     * @param userId OpenIM userID
     * @param platformId OpenIM 平台枚举值
     * @return OpenIM 原始响应，用户未导入等错误由上层映射
     */
    public OpenImApiResponse getUserToken(String adminToken, String userId, Integer platformId) {
        var body = Map.of(
            "userID", userId,
            "platformID", platformId
        );
        return post("/auth/get_user_token", body, adminToken);
    }

    /**
     * 批量导入 OpenIM 用户。
     *
     * @param adminToken OpenIM 管理员 token
     * @param users OpenIM user_register 接口要求的 users 数组
     * @return OpenIM 原始响应，已存在用户由上层按幂等场景处理
     */
    public OpenImApiResponse registerUsers(String adminToken, Object users) {
        var body = Map.of(
            "users", users
        );
        return post("/user/user_register", body, adminToken);
    }

    /**
     * 添加系统通知账号。
     * <p>
     * OpenIM v3.8 约束：appMangerLevel 必须 >= 3，faceURL 不能为空。
     * 调用方负责保证该账号仅用于系统通知发送。
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
     * 发送 OpenIM 消息。
     *
     * @param adminToken OpenIM 管理员 token
     * @param payload 已按 OpenIM send_msg 结构组装的请求体
     * @return OpenIM 原始响应，权限、用户不存在等错误由上层映射
     */
    public OpenImApiResponse sendMessage(String adminToken, Map<String, Object> payload) {
        return post("/msg/send_msg", payload, adminToken);
    }

    /**
     * 统一执行 OpenIM POST 请求。
     * <p>
     * 网络异常、空响应、响应解析异常都不向外抛出，避免外部接口波动破坏调用链的错误映射。
     */
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

    /**
     * 构造 OpenIM 必需请求头；业务 token 为空时仅发送 operationID。
     */
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

    /**
     * 生成用于 OpenIM 链路排查的单次请求标识。
     */
    private String buildOperationId() {
        return System.currentTimeMillis() + "-" + UUID.randomUUID().toString().replace("-", "");
    }
}
