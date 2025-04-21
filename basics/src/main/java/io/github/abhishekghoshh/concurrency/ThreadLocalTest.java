package io.github.abhishekghoshh.concurrency;

import io.github.abhishekghoshh.runner.Run;

@Run(active = false)
public class ThreadLocalTest {

    @Run(active = false)
    static void test1() {
        ThreadLocal<String> threadLocal = new ThreadLocal<>();
        threadLocal.set("Hello");
        System.out.println(threadLocal.get());
        threadLocal.remove();
        System.out.println(threadLocal.get());
    }
}
