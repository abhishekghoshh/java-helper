package app.util;

public class Utils {
    public interface RunnableV2 {
        void run() throws Exception;
    }

    public interface CallableV2<V> {
        V call() throws Exception;
    }

    public static void run(RunnableV2 runnable) {
        run(runnable, false);
    }

    public static void run(RunnableV2 runnable, boolean showStackTrace) {
        try {
            runnable.run();
        } catch (Exception ex) {
            if (showStackTrace) ex.printStackTrace();
            else System.out.println(ex.getMessage());
        }
    }

    public static void time(RunnableV2 runnable, String identifier) throws Exception {
        long currentTime = System.currentTimeMillis();
        runnable.run();
        System.out.println(identifier + " taking " + (System.currentTimeMillis() - currentTime) + " milliseconds");
    }

    public static <T> T time(CallableV2<T> callable, String identifier) {
        T obj = null;
        long currentTime = System.currentTimeMillis();
        try {
            obj = callable.call();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println(identifier + " taking " + (System.currentTimeMillis() - currentTime) + " milliseconds");
        return obj;
    }

}
