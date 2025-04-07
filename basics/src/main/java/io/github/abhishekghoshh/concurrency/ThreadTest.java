package io.github.abhishekghoshh.concurrency;

import io.github.abhishekghoshh.runner.Run;

public class ThreadTest {


    @Run(active = false)
    private static void fromRunnableInterface() {
        Runnable runnable = () -> System.out.println(
                "From runnable with name : " +
                        Thread.currentThread().getName() + ", id : " + Thread.currentThread().threadId()
        );
        new Thread(runnable).start();
        new Thread(runnable).start();
    }

    @Run(active = false)
    private static void fromThreadClass() {
        new ThreadExample().start();
        new ThreadExample().start();
    }

    static class ThreadExample extends Thread {
        @Override
        public void run() {
            System.out.println(
                    "From ThreadExample with name : " +
                            Thread.currentThread().getName() + ", id : " + Thread.currentThread().threadId()
            );
        }
    }
}
