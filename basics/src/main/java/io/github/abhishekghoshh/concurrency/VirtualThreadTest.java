package io.github.abhishekghoshh.concurrency;

import io.github.abhishekghoshh.runner.Run;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;


/**
 * It uses java 21
 */

@Run(active = false)
public class VirtualThreadTest {


    static int COUNTER = 10000;

    @Run(active = false, timer = true, timeIdentifier = "normal thread")
    static void startNormal() {
        IntStream.rangeClosed(1, COUNTER)
                .forEach(num -> Thread.ofPlatform().start(() -> {
                }));
    }

    @Run(active = false, timer = true, timeIdentifier = "virtual thread")
    static void startVirtual() {
        IntStream.rangeClosed(1, COUNTER)
                .forEach((num) -> Thread.ofVirtual().start(() -> {
                }));
    }


    @Run(active = true)
    static void parallelStreamInVirtualThread() throws ExecutionException, InterruptedException {
        List<Callable<Void>> tasks = IntStream.rangeClosed(1, 10)
                .<Callable<Void>>mapToObj(i -> () -> {
                    threadName(i);
                    return null;
                })
                .toList();

        try (ExecutorService executor = Executors.newThreadPerTaskExecutor(
                Thread.ofVirtual().name("virtual-thread-", 0).factory())) {
            for (Future<Void> f : executor.invokeAll(tasks)) {
                f.get();
            }
        }
    }

    @Run(active = false)
    static void forkJoinPoolTest() throws ExecutionException, InterruptedException {
        try (ForkJoinPool customThreadPool = new ForkJoinPool(3)) {
            Runnable task = () ->
                    IntStream.rangeClosed(1, 10)
                            .parallel()
                            .forEach(VirtualThreadTest::threadName);

            customThreadPool.submit(task).get();
        }
    }

    private static void threadName(int i) {
        System.out.println(i + " : " + Thread.currentThread());
    }
}
