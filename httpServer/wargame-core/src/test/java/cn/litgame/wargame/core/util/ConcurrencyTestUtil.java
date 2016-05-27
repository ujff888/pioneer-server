package cn.litgame.wargame.core.util;

import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
 
import org.springframework.util.StopWatch;
 
public class ConcurrencyTestUtil {
     
    public static void assertConcurrent(final String message,
            final List<? extends Runnable> runnables,
            final int maxTimeoutSeconds, final int maxThreadPoolSize) throws InterruptedException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start(message);
         
        final int numThreads = runnables.size();
        final List<Throwable> exceptions = Collections
                .synchronizedList(new ArrayList<Throwable>());
        final ExecutorService threadPool = Executors
                .newFixedThreadPool(numThreads > maxThreadPoolSize ? maxThreadPoolSize : numThreads);
        try {
            final CountDownLatch afterInitBlocker = new CountDownLatch(1);
            final CountDownLatch allDone = new CountDownLatch(numThreads);
            for (final Runnable submittedTestRunnable : runnables) {
                threadPool.submit(new Runnable() {
                    public void run() {
                        try {
                            afterInitBlocker.await();   // 相当于加了个阀门等待所有任务都准备就绪
                            submittedTestRunnable.run();
                        } catch (final Throwable e) {
                            exceptions.add(e);
                            System.out.println("[exceptions added.]");
                            e.printStackTrace();
                            System.out.println("[exceptions added.]");
                        } finally {
                            allDone.countDown();
                        }
                    }
                });
            }
             
            // start all test runners
            afterInitBlocker.countDown();
            assertTrue(message + " timeout! More than" + maxTimeoutSeconds + "seconds",
                       allDone.await(maxTimeoutSeconds, TimeUnit.SECONDS));
             
            stopWatch.stop();
            System.out.println(stopWatch.prettyPrint());
        } finally {
            threadPool.shutdownNow();
        }
        assertTrue(message + "failed with exception(s)" + exceptions,
                exceptions.isEmpty());
    }
}