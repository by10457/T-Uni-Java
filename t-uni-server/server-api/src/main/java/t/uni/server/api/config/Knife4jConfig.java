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
 * Knife4j 文档配置。
 * <p>
 * 注册 OpenAPI 基础信息，并在应用启动完成后打印本机可访问的文档地址。
 * </p>
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class Knife4jConfig {

    private final Environment environment;

    /**
     * 应用启动完成后输出接口文档地址。
     * <p>
     * 优先同时打印 localhost 和内网地址，便于局域网设备调试小程序接口。
     * </p>
     */
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
     * 获取本机内网 IP 地址。
     *
     * @return 优先返回常见私有网段 IPv4；未找到时返回 null
     */
    private String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                // 跳过非活动接口和 loopback 接口，避免打印不可访问地址。
                if (!ni.isUp() || ni.isLoopback()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    // 只取 IPv4，Knife4j 启动日志中 IPv6 地址对本地调试价值较低。
                    if (!addr.isLoopbackAddress() && addr.getHostAddress().indexOf(':') == -1) {
                        var ip = addr.getHostAddress();
                        // 优先返回私有网段地址，供同一局域网设备访问。
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
     * 检查 IP 是否在 172.16.0.0 - 172.31.255.255 私有网段内。
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

    /**
     * 注册 OpenAPI 基础信息。
     *
     * @return OpenAPI 文档元数据
     */
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

    /**
     * 注册全部接口分组。
     *
     * @return Knife4j 接口分组
     */
    @Bean
    public GroupedOpenApi all() {
        return GroupedOpenApi.builder().group("全部接口").pathsToMatch("/api/**").build();
    }
}
