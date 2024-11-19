package app.functional;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class StreamTest {

    public static final int COUNTER = 100000000;

    public static void main(String[] args) {
        integerStreams();
        customClasses();
        otherStreams();
        stringJoining();
        parallelStream();
        directStreamMethod();
        withFiles();
        threads();

    }

    public static void threads() {
        Runnable runnable = () -> System.out.println(Thread.currentThread().threadId());
        new Thread(runnable).start();

    }

    public static void withFiles() {
        // System.out.println(System.getProperty("user.dir"));
        try {
            Path path = Paths.get("user.txt");
            Files.lines(path)
                    .forEach(System.out::println);

            System.out.println(
                    Files.lines(path)
                            .map(name -> name.toLowerCase().split(""))
                            .flatMap(Arrays::stream)
                            .distinct()
                            .sorted()
                            .collect(Collectors.joining())
            );


            Files.list(Paths.get("."))
                    .filter(Files::isDirectory)
                    .forEach(System.out::println);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void directStreamMethod() {
        List<String> courses = new ArrayList<>(List.of("Spring", "Spring Boot", "API", "Microservices", "AWS", "PCF", "Azure", "Docker",
                "Kubernetes", "GCP"));
        courses.replaceAll(String::toUpperCase);
        System.out.println(courses);
        courses.removeIf(course -> course.length() < 4);
        System.out.println(courses);
    }

    public static void parallelStream() {
        timeCheck(() ->
                        System.out.println(
                                LongStream
                                        .rangeClosed(0, COUNTER)
                                        .sum()
                        )
                , "sum without parallel");
        timeCheck(() ->
                        System.out.println(
                                LongStream
                                        .rangeClosed(0, COUNTER)
                                        .parallel()
                                        .sum()
                        )
                , "sum with parallel");
    }

    public static void timeCheck(Runnable runnable, String identifier) {
        long startTime = System.currentTimeMillis();
        runnable.run();
        long endTime = System.currentTimeMillis();
        identifier = "time taken " + (null != identifier ? "for " + identifier + " : " : "") + (endTime - startTime);
        System.out.println(identifier);
    }

    public static void stringJoining() {
        List<String> courses = List.of("Spring", "Spring Boot", "API", "Microservices", "AWS", "PCF", "Azure", "Docker",
                "Kubernetes", "GCP");
        System.out.println(
                courses
                        .stream()
                        .collect(Collectors.joining(","))
        );
        System.out.println(
                courses
                        .stream()
                        .collect(Collectors.joining(",", "[", "]"))
        );
        System.out.println(
                courses
                        .stream()
                        .map(course -> course.split(""))
                        .flatMap(Arrays::stream)
                        .collect(Collectors.toList())
        );
        System.out.println(
                courses
                        .stream()
                        .map(course -> course.split(""))
                        .flatMap(Arrays::stream)
                        .distinct()
                        .collect(Collectors.toList())
        );

        List<List<String>> allCountries = List.of(
                List.of("Colombia", "Finland", "Greece", "Iceland", "Liberia", "Mali", "Mauritius"),
                List.of("Peru", "Serbia", "Singapore", "Turkey", "Uzbekistan", "Yemen", "Zimbabwe", "Greece", "Iceland"),
                List.of("India", "Bangladesh", "Sri Lanka", "Russia", "USA", "China", "England", "Pakistan"));
        System.out.println(
                allCountries.stream()
                        .flatMap(List::stream)
                        .distinct()
                        .collect(Collectors.toList())
        );
    }

    public static void integerStreams() {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9);

        numbers.stream()
                .filter(num -> num % 2 == 0)
                .map(num -> num * 2)
                .forEach(System.out::println);

        System.out.println(
                numbers.stream()
                        .reduce(0, Integer::sum)
        );
        numbers.stream()
                .distinct()
                .sorted(Comparator.naturalOrder())
                //.sorted(Comparator.reverseOrder())
                //.sorted(Comparator.comparing(num->num))
                .forEach(System.out::println);

        System.out.println(
                numbers.stream()
                        .map(Math::sqrt)
                        .collect(Collectors.toList())
        );
    }

    public static void otherStreams() {
        Stream<Integer> referencePipeline = Stream.of(1, 2, 3, 4, 5, 6); // java.util.stream.ReferencePipeline$Head@29ee9faa
        System.out.println(referencePipeline);

        IntStream intStream = Arrays.stream(new int[]{1, 2, 3, 4, 5, 6}); //java.util.stream.IntPipeline$Head@14acaea5
        System.out.println(intStream)
        ;
        System.out.println(
                IntStream.range(1, 10)
                        .sum()
        );
        System.out.println(
                IntStream.rangeClosed(1, 10)
                        .sum()
        );
        System.out.println(
                IntStream.iterate(0, e -> e + 1)
                        .limit(10)
                        .sum()
        );
        System.out.println(
                IntStream.iterate(0, e -> e + 1)
                        .limit(10)
                        .peek(System.out::println)
                        .sum()
        );
        System.out.println(
                IntStream.iterate(0, e -> e + 1)
                        .limit(10)
                        .boxed()
                        .collect(Collectors.toList())
        );
        System.out.println(
                IntStream.rangeClosed(1, 10)
                        .mapToObj(BigInteger::valueOf)
                        .reduce(BigInteger.ONE, BigInteger::multiply)
        );
    }

    private static void customClasses() {
        List<Course> courses = Course.courses();

        System.out.println(
                courses.stream()
                        .allMatch(course -> Course.isReviewScoreGreaterThan(course, 95))
        );
        System.out.println(
                courses.stream()
                        .noneMatch(course -> Course.isReviewScoreGreaterThan(course, 95))
        );
        System.out.println(
                courses.stream()
                        .anyMatch(course -> Course.isReviewScoreGreaterThan(course, 95))
        );
        System.out.println(
                courses.stream()
                        .sorted(Comparator
                                .comparing(Course::getNoOfStudents)
                                .thenComparing(Course::getName)
                        )
                        .collect(Collectors.toList())
        );
        System.out.println(
                courses.stream()
                        .sorted(Comparator
                                .comparing(Course::getNoOfStudents)
                                .reversed()
                        )
                        .collect(Collectors.toList())
        );
        System.out.println(
                courses.stream()
                        .sorted(Comparator.comparingInt(Course::getNoOfStudents))
                        .limit(2)
                        .collect(Collectors.toList())
        );
        System.out.println(
                courses.stream()
                        .sorted(Comparator.comparingInt(Course::getNoOfStudents))
                        .skip(2)
                        .collect(Collectors.toList())
        );
        System.out.println(
                courses.stream()
                        .sorted(Comparator.comparingInt(Course::getNoOfStudents))
                        .limit(1)
                        .skip(2)
                        .collect(Collectors.toList())
        );
        System.out.println(
                courses
                        .stream()
                        .sorted(Comparator.comparingInt(Course::getReviewScore))
                        .takeWhile(course -> Course.isReviewScoreGreaterThan(course, 95))
                        .collect(Collectors.toList())
        );
        System.out.println(
                courses.stream()
                        .sorted(Comparator.comparingInt(Course::getReviewScore))
                        .dropWhile(course -> Course.isReviewScoreGreaterThan(course, 95))
                        .collect(Collectors.toList())
        );
        System.out.println(
                courses.stream()
                        .max(Comparator.comparingInt(Course::getReviewScore))
                        .orElse(new Course("Kubernetes", "Cloud", 95, 21000))
        );

        System.out.println(
                courses.stream()
                        .mapToInt(Course::getNoOfStudents)
                        .max()
        );
        System.out.println(
                courses.stream()
                        .mapToInt(Course::getNoOfStudents)
                        .min()
        );
        System.out.println(
                courses.stream()
                        .mapToInt(Course::getNoOfStudents)
                        .sum()
        );
        System.out.println(
                courses.stream()
                        .mapToInt(Course::getNoOfStudents)
                        .average()
        );

        System.out.println(
                courses.stream()
                        .collect(Collectors.groupingBy(Course::getCategory))
        );
        System.out.println(
                courses.stream()
                        .collect(Collectors.groupingBy(Course::getCategory, Collectors.counting()))
        );
        System.out.println(
                courses.stream()
                        .collect(Collectors.groupingBy(
                                Course::getCategory,
                                Collectors.maxBy(Comparator.comparing(Course::getReviewScore))
                        ))
        );
        System.out.println(
                courses.stream()
                        .collect(Collectors.groupingBy(
                                Course::getCategory,
                                Collectors.mapping(Course::getName, Collectors.toList())
                        ))
        );
    }
}


class Course {

    private String name;
    private String category;
    private int reviewScore;
    private int noOfStudents;

    public Course() {
    }

    public Course(String name, String category, int reviewScore, int noOfStudents) {
        super();
        this.name = name;
        this.category = category;
        this.reviewScore = reviewScore;
        this.noOfStudents = noOfStudents;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getReviewScore() {
        return reviewScore;
    }

    public void setReviewScore(int reviewScore) {
        this.reviewScore = reviewScore;
    }

    public int getNoOfStudents() {
        return noOfStudents;
    }

    public void setNoOfStudents(int noOfStudents) {
        this.noOfStudents = noOfStudents;
    }

    @Override
    public String toString() {
        return "Course [name=" + name +
                ", category=" + category +
                ", reviewScore=" + reviewScore +
                ", noOfStudents=" + noOfStudents + "]";
    }

    public static List<Course> courses() {
        return List.of(
                new Course("Spring", "Framework", 98, 20000),
                new Course("Spring Boot", "Framework", 95, 18000),
                new Course("API", "Microservices", 97, 22000),
                new Course("Microservices", "Microservices", 96, 25000),
                new Course("FullStack", "FullStack", 91, 14000),
                new Course("AWS", "Cloud", 92, 21000),
                new Course("Azure", "Cloud", 99, 21000),
                new Course("Docker", "Cloud", 92, 20000),
                new Course("Kubernetes", "Cloud", 91, 20000)
        );
    }

    public static boolean isReviewScoreGreaterThan(Course course, int reviewScore) {
        return course.getReviewScore() > reviewScore;
    }

    public static boolean isReviewScoreLessThan(Course course, int reviewScore) {
        return course.getReviewScore() < reviewScore;
    }

    public static Predicate<Course> isReviewScoreGreaterThan(int reviewScore) {
        return course -> course.getReviewScore() > reviewScore;
    }
}