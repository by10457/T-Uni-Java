package t.uni.server.api;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Server小程序后端启动类
 */
@MapperScan({"t.uni.server.*.mapper"})
@ComponentScan("t.uni")
@EnableCaching
@EnableTransactionManagement
@SpringBootApplication
public class TUniServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TUniServerApplication.class, args);
    }
}
