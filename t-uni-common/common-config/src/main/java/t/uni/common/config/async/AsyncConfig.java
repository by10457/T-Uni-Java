package t.uni.common.config.async;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 默认异步执行器配置。
 * <p>
 * 为 @Async 提供虚拟线程执行器，适合 IO 密集型短任务。
 * 该配置不提供业务重试、任务持久化或限流，长耗时任务应按业务场景单独配置线程池。
 * </p>
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    /**
     * 创建 @Async 默认使用的虚拟线程执行器。
     *
     * @return 虚拟线程执行器
     */
    @Bean(name = {"taskExecutor", "asyncExecutor"}, destroyMethod = "close")
    public ExecutorService asyncExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * 指定 Spring 异步方法使用的执行器。
     *
     * @return 异步执行器
     */
    @Override
    public Executor getAsyncExecutor() {
        return asyncExecutor();
    }

    /**
     * 处理 void 异步方法中未捕获的异常。
     *
     * @return 异步异常处理器
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, objects) -> {
            log.error("异步任务执行失败, method={}", method.getName(), throwable);
            new SimpleAsyncUncaughtExceptionHandler().handleUncaughtException(throwable, method, objects);
        };
    }
}
