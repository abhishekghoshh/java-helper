package com.util;

public class Utils {
    public static void run(Runnable runnable) {
        run(runnable, false);
    }

    public static void run(Runnable runnable, boolean showStackTrace) {
        try {
            runnable.run();
        } catch (Exception ex) {
            if (showStackTrace) ex.printStackTrace();
            else System.out.println(ex.getMessage());
        }
    }
}
