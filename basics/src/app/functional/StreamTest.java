package app.functional;

import app.functional.model.Course;

import java.util.List;

import static app.util.Utils.print;
import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.*;

public class StreamTest {

    final static List<Course> courses = Course.courses();

    public static void main(String[] args) {

        print(() -> courses.stream()
                .allMatch(course ->
                        Course.isReviewScoreGreaterThan(course, 95)
                )
        );
        print(() -> courses.stream()
                .noneMatch(course ->
                        Course.isReviewScoreGreaterThan(course, 95)
                )
        );
        print(() -> courses.stream()
                .anyMatch(course ->
                        Course.isReviewScoreGreaterThan(course, 95)
                )
        );
        print(() -> courses.stream()
                .sorted(comparing(Course::getNoOfStudents)
                                .thenComparing(Course::getName)
                        )
                .collect(toList())
        );
        print(() -> courses.stream()
                .sorted(comparing(Course::getNoOfStudents)
                                .reversed()
                        )
                .collect(toList())
        );
        print(() -> courses.stream()
                .sorted(comparingInt(Course::getNoOfStudents))
                        .limit(2)
                .collect(toList())
        );
        print(() -> courses.stream()
                .sorted(comparingInt(Course::getNoOfStudents))
                        .skip(2)
                .collect(toList())
        );
        print(() -> courses.stream()
                .sorted(comparingInt(Course::getNoOfStudents))
                        .limit(1)
                        .skip(2)
                .collect(toList())
        );
        print(() -> courses.stream()
                .sorted(comparingInt(Course::getReviewScore))
                .takeWhile(course -> Course.isReviewScoreGreaterThan(course, 95))
                .collect(toList())
        );
        print(() -> courses.stream()
                .sorted(comparingInt(Course::getReviewScore))
                .dropWhile(course -> Course.isReviewScoreGreaterThan(course, 95))
                .collect(toList())
        );
        print(() -> courses.stream()
                .max(comparingInt(Course::getReviewScore))
                        .orElse(new Course("Kubernetes", "Cloud", 95, 21000))
        );

        print(() -> courses.stream()
                        .mapToInt(Course::getNoOfStudents)
                        .max()
        );
        print(() -> courses.stream()
                        .mapToInt(Course::getNoOfStudents)
                        .min()
        );
        print(() -> courses.stream()
                        .mapToInt(Course::getNoOfStudents)
                        .sum()
        );
        print(() -> courses.stream()
                        .mapToInt(Course::getNoOfStudents)
                        .average()
        );

        print(() -> courses.stream()
                .collect(groupingBy(Course::getCategory))
        );
        print(() -> courses.stream()
                .collect(groupingBy(Course::getCategory, counting()))
        );
        print(() -> courses.stream()
                .collect(groupingBy(
                                Course::getCategory,
                        maxBy(comparing(Course::getReviewScore))
                        ))
        );
        print(() -> courses.stream()
                .collect(
                        groupingBy(
                                Course::getCategory,
                                mapping(Course::getName, toList()
                                )
                        )
                )
        );
    }
}