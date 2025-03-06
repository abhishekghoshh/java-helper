package app.concurrency;

import app.runner.Run;

import java.util.stream.IntStream;


/**
 * It uses java 21
 */
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
}
