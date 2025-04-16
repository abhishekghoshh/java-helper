package io.github.abhishekghoshh.runner;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassUtil {
    static ClassLoader classLoader = ClassLoader.getSystemClassLoader();

    public static List<Class<?>> allClasses(String packageName) throws Exception {
        return allClasses(packageName, cls -> true);
    }

    public static List<Class<?>> allClasses(String packageName, Predicate<Class<?>> predicate) throws Exception {
        Objects.requireNonNull(packageName);
        String updatePackageName = makeDotToSlash(packageName);
        URL packageUrl = classLoader.getResource(updatePackageName);
        Objects.requireNonNull(packageUrl);
        String packagePath = packageUrl.getPath();
        Objects.requireNonNull(packagePath);
        return (packagePath.contains("!")) ?
                getFromJar(packagePath, updatePackageName, predicate) :
                getFromDir(packageName, predicate);
    }

    private static List<Class<?>> getFromJar(String packagePath, String basePath, Predicate<Class<?>> predicate) throws IOException {
        packagePath = packagePath.substring(0, packagePath.indexOf("!"))
                .replaceFirst("file:", "");
        packagePath = URLDecoder.decode(packagePath, StandardCharsets.UTF_8);
        try (JarFile jarFile = new JarFile(packagePath)) {
            List<? extends Class<?>> classes = jarFile.stream()
                    .parallel()
                    .filter(ClassUtil::isAClassFile)
                    .filter(e -> e.getName().startsWith(basePath))
                    .map(ClassUtil::createClassNameFromJarEntry)
                    .map(ClassUtil::loadClass)
                    .filter(Objects::nonNull)
                    .filter(predicate)
                    .toList();
            return (List<Class<?>>) classes;
        }
    }

    private static List<Class<?>> getFromDir(String packageName, Predicate<Class<?>> predicate) {
        File[] files = getFiles(packageName);
        if (null == files || files.length == 0) {
            return List.of();
        }
        List<? extends Class<?>> runningClasses = Arrays.stream(files)
                .parallel()
                .filter(ClassUtil::isAClassFile)
                .map(file -> createClassNameFromFile(packageName, file))
                .map(ClassUtil::loadClass)
                .filter(Objects::nonNull)
                .filter(predicate)
                .toList();
        List<Class<?>> all = new ArrayList<>(runningClasses);
        List<? extends Class<?>> nestedRunningClasses = Arrays.stream(files)
                .parallel()
                .filter(File::isDirectory)
                .map(file -> packageName + "." + file.getName())
                .flatMap(p -> getFromDir(p, predicate).stream())
                .toList();
        all.addAll(nestedRunningClasses);
        return all;
    }

    private static String createClassNameFromFile(String packageName, File file) {
        String className = file.getName();
        className = removeDotClass(className);
        return prependPackagePath(packageName, className);
    }

    private static String prependPackagePath(String packageName, String className) {
        return packageName + '.' + className;
    }


    private static String createClassNameFromJarEntry(JarEntry entry) {
        String className = entry.getName();
        className = makeSlashToDot(className);
        return removeDotClass(className);
    }

    private static Class<?> loadClass(String className) {
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException | NoClassDefFoundError e1) {
            System.err.println("Skipping: " + className + " (missing dependencies?)");
            return null;
        }
    }

    private static File[] getFiles(String packageName) {
        String updatePackageName = makeDotToSlash(packageName);
        URL packageUrl = classLoader.getResource(updatePackageName);
        Objects.requireNonNull(packageUrl);
        File packageDir = new File(packageUrl.getFile());
        return Objects.requireNonNull(packageDir.listFiles());
    }

    private static boolean isAClassFile(JarEntry e) {
        return e.getName().endsWith(".class");
    }

    private static boolean isAClassFile(File file) {
        return file.isFile() && file.getName().endsWith(".class");
    }

    static String makeDotToSlash(String str) {
        return str.replace('.', '/');
    }

    static String makeSlashToDot(String str) {
        return str.replace('/', '.');
    }

    static String removeDotClass(String str) {
        return str.replace(".class", "");
    }

}
