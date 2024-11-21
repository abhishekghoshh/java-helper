package app.functional;

import app.functional.model.Course;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class StreamTest {


    public static void main(String[] args) {
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