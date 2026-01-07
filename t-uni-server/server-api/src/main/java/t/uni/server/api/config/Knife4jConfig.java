package t.uni.server.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j API文档配置
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("T-Uni Server API")
                        .version("1.0.0")
                        .description("微信小程序后端API文档")
                        .contact(new Contact()
                                .name("T-Uni Team")
                                .url("http://t-uni-web.site/")
                        )
                );
    }
}
