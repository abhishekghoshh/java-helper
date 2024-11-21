package app.util;

public class Utils {

    public interface RunnableV2 {
        void run() throws Exception;
    }

    public interface CallableV2<V> {
        V call() throws Exception;
    }

    public static void run(RunnableV2 runnable) {
        run(runnable, false, null);
    }

    public static void run(RunnableV2 runnable, String identifier) {
        run(runnable, false, identifier);
    }

    public static void run(RunnableV2 runnable, boolean showStackTrace) {
        run(runnable, showStackTrace, null);
    }

    public static void run(RunnableV2 runnable, boolean showStackTrace, String identifier) {
        if (null != identifier && !identifier.isEmpty()) System.out.println(identifier);
        try {
            runnable.run();
        } catch (Exception ex) {
            if (showStackTrace) ex.printStackTrace();
            else System.out.println(ex.getMessage());
        }
        System.out.println();
    }

    public static void time(RunnableV2 runnable, String identifier) throws Exception {
        long currentTime = System.currentTimeMillis();
        runnable.run();
        System.out.println(identifier + " taking " + (System.currentTimeMillis() - currentTime) + " milliseconds");
    }

    public static <T> T time(CallableV2<T> callable, String identifier) {
        return time(callable, identifier, false);
    }

    public static <T> T time(CallableV2<T> callable, String identifier, boolean toPrint) {
        T obj = null;
        long currentTime = System.currentTimeMillis();
        try {
            obj = callable.call();
            if (toPrint) System.out.println(obj);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println(identifier + " is taking " + (System.currentTimeMillis() - currentTime) + " milliseconds");
        return obj;
    }


    public static <T> void print(CallableV2<T> callable) {
        print(callable, null);
    }

    public static <T> void print(CallableV2<T> callable, String identifier) {
        if (null != identifier && !identifier.isEmpty()) System.out.println(identifier);
        try {
            System.out.println(callable.call());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    public static <T> void print(T t) {
        System.out.print(t + " ");
    }
}
