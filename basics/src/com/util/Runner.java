package com.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Runner {
    public static void run(Class<?> clazz) {
        run(clazz, true);
    }

    private static void run(Class<?> clazz, boolean run) {
        if (!run) return;
        try {
            // Get the main method
            Method mainMethod = clazz.getMethod("main", String[].class);
            // Invoke the main method
            mainMethod.invoke(null, (Object) new String[]{});
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
