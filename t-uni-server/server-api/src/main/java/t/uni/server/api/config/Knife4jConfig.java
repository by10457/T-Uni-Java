package t.uni.server.api.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Knife4j 文档配置
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class Knife4jConfig {

    private final Environment environment;

    @EventListener(ApplicationReadyEvent.class)
    public void printApiDocUrl() {
        var port = environment.getProperty("server.port", "8080");
        var contextPath = environment.getProperty("server.servlet.context-path", "");

        var localUrl = "http://localhost:" + port + contextPath + "/doc.html";
        var lanIp = getLocalIpAddress();
        var lanUrl = lanIp != null ? "http://" + lanIp + ":" + port + contextPath + "/doc.html" : null;

        if (lanUrl != null) {
            log.info("""

                    ----------------------------------------------------------
                    \tKnife4j 文档 (本地):  {}
                    \tKnife4j 文档 (内网):  {}
                    ----------------------------------------------------------""", localUrl, lanUrl);
        } else {
            log.info("""

                    ----------------------------------------------------------
                    \tKnife4j 文档: {}
                    ----------------------------------------------------------""", localUrl);
        }
    }

    /**
     * 获取本机内网 IP 地址
     */
    private String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                // 跳过非活动接口和 loopback 接口
                if (!ni.isUp() || ni.isLoopback()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    // 只获取 IPv4 地址，且不是 loopback
                    if (!addr.isLoopbackAddress() && addr.getHostAddress().indexOf(':') == -1) {
                        var ip = addr.getHostAddress();
                        // 优先返回 192.168.x.x 或 10.x.x.x 或 172.16-31.x.x（内网地址）
                        if (ip.startsWith("192.168.") || ip.startsWith("10.") ||
                                (ip.startsWith("172.") && isInRange(ip))) {
                            return ip;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取内网 IP 失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 检查 IP 是否在 172.16.0.0 - 172.31.255.255 范围内
     */
    private boolean isInRange(String ip) {
        try {
            var parts = ip.split("\\.");
            if (parts.length == 4) {
                int second = Integer.parseInt(parts[1]);
                return second >= 16 && second <= 31;
            }
        } catch (Exception e) {
            // ignore
        }
        return false;
    }

    @Bean
    public OpenAPI openAPI() {
        Contact contact = new Contact().name("T-Uni");
        License license = new License().name("MIT").url("https://mit-license.org");
        Info info = new Info()
                .title("T-Uni Server API")
                .contact(contact)
                .license(license)
                .description("T-Uni 服务端接口文档")
                .version("v1.0.0");

        return new OpenAPI().info(info).externalDocs(new ExternalDocumentation());
    }

    @Bean
    public GroupedOpenApi all() {
        return GroupedOpenApi.builder().group("全部接口").pathsToMatch("/api/**").build();
    }
}
