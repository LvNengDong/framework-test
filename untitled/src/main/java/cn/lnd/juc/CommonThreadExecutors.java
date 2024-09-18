package cn.lnd.juc;

import cn.lnd.mock.QMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.*;

/**
 * @Author lnd
 * @Description、

    java.util.concurrent.ThreadPoolExecutor#prestartAllCoreThreads() 方法的作用是预启动核心线程池中的所有线程。
    在使用 ThreadPoolExecutor 创建线程池时，可以设置核心线程数（corePoolSize）来指定线程池中保持活动状态的线程数量。默认情况下，线程池中的核心线程是懒启动的，即只有当有任务提交时才会创建和启动核心线程。
    而 prestartAllCoreThreads() 方法可以在创建线程池后立即启动并执行所有的核心线程，而不需要等待任务的提交。这样可以提前创建并准备好核心线程，以便在任务到达时能够立即执行。
    需要注意的是，prestartAllCoreThreads() 方法只会启动核心线程，而不会启动非核心线程。非核心线程是在任务量增加时才会创建和启动的。
    使用 prestartAllCoreThreads() 方法可以在初始化线程池后预先启动核心线程，以提高任务处理的响应速度和效率。但需要根据具体情况来决定是否使用该方法，以避免不必要的资源消耗。
 * @Date 2024/5/7 17:35
 */
public class CommonThreadExecutors {

    private static final ThreadPoolExecutor executor;

    public static final Logger logger = LoggerFactory.getLogger(CommonThreadExecutors.class);

    static {
        // 调整阻塞队列的最大值
        final LinkedBlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<>(Short.MAX_VALUE);

        // 重写拒绝策略，增加拒绝监控、日志
        final RejectedExecutionHandler rejectedExeHandler = new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(final Runnable r, final ThreadPoolExecutor executor) {
                QMonitor.recordOne("CommonExecutor_ASYNC_rejected");
                logger.error("commonexecutor async rejected");
                throw new RejectedExecutionException();
            }
        };

        executor = new ThreadPoolExecutor(6, 12, 60L, TimeUnit.SECONDS, blockingQueue,
                rejectedExeHandler) {
            // 重写 execute 执行方法，执行前增加监控记录
            @Override
            public final void execute(final Runnable command) {
                QMonitor.recordOne("CommonExecutor_pool_size", this.getPoolSize());
                QMonitor.recordSize("CommonExecutor_pool_max_size", this.getMaximumPoolSize());
                QMonitor.recordOne("CommonExecutor_pool_activecount", this.getActiveCount());
                QMonitor.recordOne("CommonExecutor_pool_queuesize", blockingQueue.size());
                QMonitor.generateRate("CommonExecutor_pool_ratio","CommonExecutor_pool_max_size", "CommonExecutor_pool_activecount");
                super.execute(command);
            }
        };

        // 预启动核心线程池中的所有线程。
        executor.prestartAllCoreThreads();
    }

    // 提供执行方法，使用重写后的执行器
    public static void execute(Runnable command) {
        executor.execute(command);
    }


    // 注意：线程池对象没有重写 submit 方法，所以如果使用该方法提交任务，监控是无法统计到的
    public static Future submit(Callable command) {
        return executor.submit(command);
    }

    // 对外提供的获取线程池的方法
    public static ThreadPoolExecutor getExecutor() {
        return executor;
    }
}
