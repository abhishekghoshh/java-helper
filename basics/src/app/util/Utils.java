package app.util;

import app.util.RunnerConfig.CallableV2;
import app.util.RunnerConfig.RunnableV2;

public class Utils {



    public static void run(RunnableV2 runnable) {
        run(runnable, false, null, false);
    }

    public static void run(RunnableV2 runnable, String identifier) {
        run(runnable, false, identifier, false);
    }

    public static void run(RunnableV2 runnable, String identifier, boolean toRun) {
        run(runnable, false, identifier, toRun);
    }

    public static void run(RunnableV2 runnable, boolean showStackTrace) {
        run(runnable, showStackTrace, null, false);
    }

    public static void run(RunnableV2 runnable, boolean showStackTrace, String identifier, boolean toRun) {
        if (!toRun) return;
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
