package io.github.abhishekghoshh.runner;

public class RunnerUtils {
    public static boolean isActive(Class<?> cls) {
        return cls.isAnnotationPresent(Run.class) && cls.getAnnotation(Run.class).active();
    }
}
