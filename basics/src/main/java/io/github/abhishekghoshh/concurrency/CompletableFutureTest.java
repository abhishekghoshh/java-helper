package io.github.abhishekghoshh.concurrency;

import io.github.abhishekghoshh.runner.Run;

import java.util.List;
import java.util.concurrent.*;

@Run(active = false)
public class CompletableFutureTest {

    @Run(active = false, timer = true)
    private static void completableFutureTest() throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
                    System.out.println(Thread.currentThread());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return "Hello";
                }, executorService)
                .thenApply(String::toUpperCase)
                .thenAccept(System.out::println);
        System.out.println("CompletableFuture is running in the background");
        future.get();
    }

    @Run(active = false)
    private static void completableFutureTest2() throws ExecutionException, InterruptedException {
        Executor executorService = Executors.newVirtualThreadPerTaskExecutor();
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            List.of(1, 2, 3, 4, 5)
                    .parallelStream()
                    .forEach((x) -> {
                        System.out.println(x + " " + Thread.currentThread());
                    });
        }, executorService);
        System.out.println("CompletableFuture is running in the background");
        future.get();
    }

    @Run(active = true)
    static void failureTest() throws ExecutionException, InterruptedException {
        generateReport()
                .exceptionally(ex -> {
                    return "There is some error generating the code with error: " + ex.getMessage();
                }).thenAccept((report) -> {
                    System.out.println("Report: " + report);
                }).get();
    }

    static CompletableFuture<String> generateReport() {
        try {
            // generate a random number from 1 to 100 and if it is odd, then throw exception
            if (ThreadLocalRandom.current().nextInt(1, 101) % 2 == 1) {
                throw new RuntimeException("Report generation failed");
            }
            return CompletableFuture.completedFuture("Successfully generated the Report");
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
