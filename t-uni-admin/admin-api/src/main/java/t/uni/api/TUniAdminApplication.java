package t.uni.api;

import org.dromara.x.file.storage.spring.EnableFileStorage;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Admin后台管理系统启动类
 */
@MapperScan({"t.uni.*.mapper"})
@ComponentScan("t.uni")
@EnableScheduling
@EnableFileStorage
@EnableCaching
@EnableTransactionManagement
@SpringBootApplication
public class TUniAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(TUniAdminApplication.class, args);
    }
}
