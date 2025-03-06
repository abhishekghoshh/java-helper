package app.runner;


import app.runner.RunnerConfig.CallableV2;
import app.runner.RunnerConfig.RunnableV2;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.stream.Stream;

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



    public static void run(Class<?> clazz) {
        runOne(clazz);
    }


    /**
     * it takes almost 25ms to load all the class,
     * but here we are using find first
     * once we find any class any Run annotation we are returning
     */
    public static void runOne(Class<?> clazz) {
        String packageName = clazz.getPackageName();
        Class<?> runningClass = getClass(packageName);
        if (null == runningClass) return;
        runAnnotatedMethods(runningClass);
    }

    private static Class<?> getClass(String packageName) {
        String updatePackageName = updatePackageName(packageName);
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL packageUrl = classLoader.getResource(updatePackageName);
        Objects.requireNonNull(packageUrl);
        File packageDir = new File(packageUrl.getFile());
        // trying to find class with Run annotation
        Optional<? extends Class<?>> runningClass = Arrays.stream(Objects.requireNonNull(packageDir.listFiles()))
                .parallel()
                .filter(file -> file.isFile() && file.getName().endsWith(".class"))
                .map(file -> packageName + "." + file.getName().replace(".class", ""))
                .map(clasName -> {
                    try {
                        return classLoader.loadClass(clasName);
                    } catch (ClassNotFoundException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(cls -> cls.isAnnotationPresent(Run.class) && cls.getAnnotation(Run.class).active())
                .findFirst();
        // we have found one class with Run annotation, so we will return the class
        if (runningClass.isPresent()) return runningClass.get();
        // now we will check if this current directory has any subdirectories or not
        return Arrays.stream(Objects.requireNonNull(packageDir.listFiles()))
                .parallel()
                .filter(File::isDirectory)
                .map(file -> packageName + "." + file.getName())
                .map(Runner::getClass)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    static String updatePackageName(String packageName) {
        return packageName.replace('.', '/');
    }


    /**
     * it takes almost 25ms to load all the class,
     * but here we are running all the classes one by one
     */
    public static void runAll(Class<?> clazz) {
        String packageName = clazz.getPackageName();
        List<Class<?>> runningClasses = getAllClasses(packageName);
        runningClasses.forEach(Runner::runAnnotatedMethods);
    }

    private static List<Class<?>> getAllClasses(String packageName) {

        String updatePackageName = updatePackageName(packageName);
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL packageUrl = classLoader.getResource(updatePackageName);
        Objects.requireNonNull(packageUrl);
        File packageDir = new File(packageUrl.getFile());

        List<? extends Class<?>> runningClasses = Arrays.stream(Objects.requireNonNull(packageDir.listFiles()))
                .parallel()
                .filter(file -> file.isFile() && file.getName().endsWith(".class"))
                .map(file -> packageName + "." + file.getName().replace(".class", ""))
                .map(clasName -> {
                    try {
                        return classLoader.loadClass(clasName);
                    } catch (ClassNotFoundException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(cls -> cls.isAnnotationPresent(Run.class) && cls.getAnnotation(Run.class).active())
                .toList();

        List<Class<?>> all = new ArrayList<>(runningClasses);

        List<? extends Class<?>> nestedRunningClasses = Arrays.stream(Objects.requireNonNull(packageDir.listFiles()))
                .parallel()
                .filter(File::isDirectory)
                .map(file -> packageName + "." + file.getName())
                .flatMap(p -> getAllClasses(p).stream())
                .toList();

        all.addAll(nestedRunningClasses);

        return all;
    }


    public static void runAnnotatedMethods(Class<?> clazz) {
        boolean runAllMethods;
        Object instance;
        try {
            instance = clazz.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (clazz.isAnnotationPresent(Run.class)) {
            Run clazzRun = clazz.getAnnotation(Run.class);
            // is not an active Run annotated class
            if (!clazzRun.active()) return;
            // should all methods run
            runAllMethods = clazzRun.all();
            if (clazzRun.print()) {
                System.out.println("Executing class : " + clazz.getCanonicalName());
            }
        } else {
            runAllMethods = false;
        }
        // method filtering
        List<Method> methods = Stream.of(clazz.getDeclaredMethods())
                .parallel()
                .filter(method -> method.isAnnotationPresent(Run.class))
                .filter(method -> runAllMethods || method.getAnnotation(Run.class).active())
                .peek(method -> method.setAccessible(true))
                .toList();

        // mapping methods to runner config
        List<RunnerConfig<Object>> runnerConfigs = methods.parallelStream()
                .map(method -> {
                    Run annotation = method.getAnnotation(Run.class);
                    return code(() -> method.invoke(instance))
                            .id(annotation.id())
                            .print(annotation.print())
                            .timer(annotation.timer(), annotation.timeIdentifier())
                            .error(annotation.showError())
                            .showStackTrace(annotation.showStacktrace())
                            .throwing(annotation.throwing())
                            .printMethodName(annotation.print())
                            .methodName(method.getName());
                }).toList();

        // running individual runner config
        runnerConfigs.forEach(rc -> {
            try {
                if (rc.printMethodName()) System.out.println("Executing : " + rc.methodName());
                rc.run();
            } catch (Exception e) {
                System.err.println("Error invoking method: " + e.getMessage());
                System.exit(0);
            }
        });
    }
}
