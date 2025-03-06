package app.concurrency;

import app.runner.Run;
import app.runner.Runner;

import java.util.stream.IntStream;

public class VirtualThreadTest {

    /**
     * It uses java 21
     */
    public static void main(String[] args) {
        Runner.runAnnotatedMethods(VirtualThreadTest.class);
    }

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
