package app.concurrency;

public class ThreadTest {
    public static void main(String[] args) {
        Runnable runnable = () -> System.out.println(
                Thread.currentThread().getName() + " " + Thread.currentThread().threadId()
        );
        new Thread(runnable).start();

        new Thread(runnable).start();
    }
}
