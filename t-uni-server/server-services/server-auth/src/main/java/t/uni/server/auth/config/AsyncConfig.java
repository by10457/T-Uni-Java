package t.uni.server.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 异步任务配置（虚拟线程版本）
 * <p>
 * JDK 21 虚拟线程特性：
 * <ul>
 *     <li>虚拟线程是轻量级线程，由 JVM 管理，不需要池化</li>
 *     <li>每个任务创建一个虚拟线程，内存开销极小（约几 KB vs 平台线程约 1MB）</li>
 *     <li>非常适合 IO 密集型任务（网络请求、数据库操作）</li>
 *     <li>无需配置线程池参数（corePoolSize/maxPoolSize/queueCapacity 等）</li>
 * </ul>
 *
 * @author lzx
 * @since 2026-01-10
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 通用异步执行器（虚拟线程）
     * <p>
     * 使用场景：
     * <ul>
     *     <li>异步任务执行</li>
     *     <li>调用外部 API，IO 密集型</li>
     * </ul>
     * <p>
     * 虚拟线程优势：
     * <ul>
     *     <li>并发能力：无上限，仅受外部 API 限流约束</li>
     *     <li>资源占用：极低，每个虚拟线程约几 KB</li>
     *     <li>无需调参：不再需要考虑线程池大小、队列容量等</li>
     * </ul>
     */
    @Bean("asyncExecutor")
    public Executor asyncExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
