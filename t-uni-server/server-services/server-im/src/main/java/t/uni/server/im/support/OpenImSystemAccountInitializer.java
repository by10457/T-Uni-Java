package t.uni.server.im.support;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import t.uni.server.im.client.OpenImApiClient;
import t.uni.server.im.config.ConditionalOnOpenImEnabled;
import t.uni.server.im.config.OpenImProperties;
import t.uni.server.im.service.OpenImAdminTokenProvider;

/**
 * 启动时幂等注册系统通知账号
 *
 * @author t-uni
 * @since 2026-04-24
 */
@Slf4j
@Component
@Order(100)
@ConditionalOnOpenImEnabled
@RequiredArgsConstructor
public class OpenImSystemAccountInitializer implements ApplicationRunner {

    private final OpenImApiClient openImApiClient;
    private final OpenImProperties openImProperties;
    private final OpenImAdminTokenProvider openImAdminTokenProvider;

    @Override
    public void run(ApplicationArguments args) {
        if (!isOpenImConfigured()) {
            log.info("[OpenIM] 配置不完整，跳过系统通知账号初始化");
            return;
        }
        if (StrUtil.isBlank(openImProperties.getSystemNoticeUserId())) {
            log.warn("[OpenIM] 未配置 systemNoticeUserId，跳过");
            return;
        }
        try {
            ensureSystemNoticeAccount();
        } catch (Exception e) {
            log.error("[OpenIM] 系统通知账号初始化失败: {}", e.getMessage(), e);
        }
    }

    private boolean isOpenImConfigured() {
        return StrUtil.isNotBlank(openImProperties.getApiAddress())
            && StrUtil.isNotBlank(openImProperties.getAdminUserId())
            && StrUtil.isNotBlank(openImProperties.getAdminSecret());
    }

    private void ensureSystemNoticeAccount() {
        var adminToken = openImAdminTokenProvider.getAdminToken();
        var userId = openImProperties.getSystemNoticeUserId();
        var nickName = openImProperties.getSystemNoticeNickname();
        var faceURL = openImProperties.getSystemNoticeAvatar();

        var response = openImApiClient.addNotificationAccount(adminToken, userId, nickName, faceURL);
        if (response.isSuccess()) {
            log.info("[OpenIM] 系统通知账号初始化成功: userId={}", userId);
        } else if (response.getErrCode() != null && response.getErrCode() == 1101) {
            log.info("[OpenIM] 系统通知账号已存在（幂等）: userId={}", userId);
        } else {
            log.warn("[OpenIM] 系统通知账号初始化异常: errCode={}, errMsg={}",
                response.getErrCode(), response.getErrMsg());
        }
    }
}
