package app.runner;


import app.runner.RunnerConfig.CallableV2;
import app.runner.RunnerConfig.RunnableV2;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Runner {
    public static void runMain(Class<?> clazz) {
        runMain(clazz, false);
    }

    public static void runMain(Class<?> clazz, boolean run) {
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
    public static <T> RunnerConfig<T> code(CallableV2<T> callable) {
        return new RunnerConfig<>(callable);
    }


    public static <T> RunnerConfig<T> code(RunnableV2 runnable) {
        return new RunnerConfig<>(runnable);
    }

    public static void run(RunnableV2 runnable) throws Exception {
        code(runnable).run();
    }

    public static <T> T run(CallableV2<T> callable) throws Exception {
        return code(callable).run();
    }

    public static void time(RunnableV2 runnable) throws Exception {
        code(runnable).timer().run();
    }

    public static <T> T time(CallableV2<T> callable) throws Exception {
        return code(callable).timer().run();
    }

    public static void time(RunnableV2 runnable, String timeId) throws Exception {
        code(runnable).timer(timeId).run();
    }

    public static <T> T time(CallableV2<T> callable, String timeId) throws Exception {
        return code(callable).timer(timeId).run();
    }

    public static void runAnnotatedMethods(Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (Modifier.isStatic(method.getModifiers())
                    && method.isAnnotationPresent(Run.class)) {
                method.setAccessible(true);
                try {
                    Run annotation = method.getAnnotation(Run.class);
                    if (!annotation.active()) continue;
                    if (annotation.printMethodName()) System.out.println("Executing : " + method.getName());
                    code(() -> method.invoke(null))
                            .id(annotation.id())
                            .print(annotation.print())
                            .timer(annotation.timer(), annotation.timeIdentifier())
                            .error(annotation.showError())
                            .showStackTrace(annotation.showStacktrace())
                            .throwing(annotation.throwing())
                            .run();
                } catch (Exception e) {
                    System.err.println("Error invoking method: " + e.getMessage());
                    System.exit(0);
                }
            }
        }
    }

}
