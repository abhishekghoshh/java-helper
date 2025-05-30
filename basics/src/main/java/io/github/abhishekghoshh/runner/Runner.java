package io.github.abhishekghoshh.runner;


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


    /**
     * Run the one class or classes with Run annotation
     */
    public static void run(Class<?> clazz) throws Exception {
        String packageName = clazz.getPackageName();
        List<Class<?>> classes = ClassUtil.allClasses(packageName, Runner::isActive);
        if (classes.isEmpty()) {
            System.out.println("No Active Run Class found in the package: " + packageName);
            return;
        }
        classes.forEach(Runner::runAnnotatedMethods);
    }

    public static boolean isActive(Class<?> cls) {
        return cls.isAnnotationPresent(Run.class) && cls.getAnnotation(Run.class).active();
    }

    static ClassLoader classLoader = ClassLoader.getSystemClassLoader();


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
        System.out.println("packageName is " + packageName);
        File[] files = getFiles(packageName);
        Optional<? extends Class<?>> runningClass = Arrays.stream(files)
                .parallel()
                .filter(Runner::isClassFile)
                .map(file -> removeClassExtension(packageName, file))
                .map(Runner::loadClass)
                .filter(Objects::nonNull)
                .filter(RunnerUtils::isActive)
                .findFirst();

        // we have found one class with Run annotation, so we will return the class
        if (runningClass.isPresent()) return runningClass.get();

        // now we will check if this current directory has any subdirectories or not
        return Arrays.stream(files)
                .parallel()
                .filter(File::isDirectory)
                .map(file -> packageName + "." + file.getName())
                .map(Runner::getClass)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private static List<Class<?>> getAllClasses(String packageName) {
        System.out.println("packageName is " + packageName);
        File[] files = getFiles(packageName);
        List<? extends Class<?>> runningClasses = Arrays.stream(files)
                .parallel()
                .filter(Runner::isClassFile)
                .map(file -> removeClassExtension(packageName, file))
                .map(Runner::loadClass)
                .filter(Objects::nonNull)
                .filter(RunnerUtils::isActive)
                .toList();

        List<Class<?>> all = new ArrayList<>(runningClasses);

        List<? extends Class<?>> nestedRunningClasses = Arrays.stream(files)
                .parallel()
                .filter(File::isDirectory)
                .map(file -> packageName + "." + file.getName())
                .flatMap(p -> getAllClasses(p).stream())
                .toList();

        all.addAll(nestedRunningClasses);

        return all;
    }

    private static String removeClassExtension(String packageName, File file) {
        return packageName + "." + file.getName().replace(".class", "");
    }

    private static boolean isClassFile(File file) {
        return file.isFile() && file.getName().endsWith(".class");
    }


    static String updatePackageName(String packageName) {
        return packageName.replace('.', '/');
    }


    /**
     * it takes almost 25 ms to load all the class,
     * but here we are running all the classes one by one
     */
    public static void runAll(Class<?> clazz) {
        String packageName = clazz.getPackageName();
        List<Class<?>> runningClasses = getAllClasses(packageName);
        runningClasses.forEach(Runner::runAnnotatedMethods);
    }


    private static Class<?> loadClass(String className) {
        try {
            System.out.println("Loading class " + className);
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            System.err.println("Skipping: " + className + " (missing dependencies?)");
            return null;
        }
    }

    private static File[] getFiles(String packageName) {
        String updatePackageName = updatePackageName(packageName);
        URL packageUrl = classLoader.getResource(updatePackageName);
        Objects.requireNonNull(packageUrl);
        File packageDir = new File(packageUrl.getFile());
        return Objects.requireNonNull(packageDir.listFiles());
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
            // should all methods run?
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
