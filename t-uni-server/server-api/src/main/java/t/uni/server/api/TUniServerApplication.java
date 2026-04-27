package t.uni.server.api;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * T-Uni 小程序后端启动类。
 * <p>
 * 统一扫描 t.uni 包下组件和服务 Mapper，并开启缓存与事务管理。
 * </p>
 */
@MapperScan({"t.uni.server.*.mapper"})
@ComponentScan("t.uni")
@EnableCaching
@EnableTransactionManagement
@SpringBootApplication
public class TUniServerApplication {

    /**
     * 应用启动入口。
     *
     * @param args 命令行启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(TUniServerApplication.class, args);
    }
}
