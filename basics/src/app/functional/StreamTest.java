package app.functional;

import app.functional.model.Course;
import app.runner.Run;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Run(active = false)
public class StreamTest {

    final static List<Course> courses = Course.courses();


    @Run(active = false, id = "All match example")
    static void allMatch() {
        boolean ans = courses.stream()
                .allMatch(course ->
                        Course.isReviewScoreGreaterThan(course, 95)
                );

        System.out.println(ans);
    }

    @Run(active = false, id = "None match example")
    static void noneMatch() {
        boolean ans = courses.stream()
                .noneMatch(course ->
                        Course.isReviewScoreGreaterThan(course, 95)
                );

        System.out.println(ans);
    }

    @Run(active = false, id = "any match example")
    static void anyMatch() {
        boolean ans = courses.stream()
                .anyMatch(course ->
                        Course.isReviewScoreGreaterThan(course, 95)
                );
        System.out.println(ans);
    }

    @Run(active = false, id = "sorted by number of student then by name and collect to list")
    static void sortedExample1() {
        List<Course> list = courses.stream()
                .sorted(
                        Comparator.comparing(Course::getNoOfStudents)
                                .thenComparing(Course::getName)
                )
                .collect(Collectors.toList());
        System.out.println(list);
    }

    @Run(active = false, id = "sorted by number of student but in reverse and collect to list")
    static void sortedExample2() {
        List<Course> list = courses.stream()
                .sorted(
                        Comparator.comparing(Course::getNoOfStudents)
                                .reversed()
                )
                .collect(Collectors.toList());
        System.out.println(list);
    }

    @Run(active = false, id = "sorted by number of student with a limit 2 and collect to list")
    static void sortedExample3() {
        List<Course> list = courses.stream()
                .sorted(Comparator.comparingInt(Course::getNoOfStudents))
                .limit(2)
                .collect(Collectors.toList());
        System.out.println(list);
    }

    @Run(active = false, id = "sorted by number of student with a skip 2 and collect to list")
    static void sortedExample4() {
        List<Course> list = courses.stream()
                .sorted(Comparator.comparingInt(Course::getNoOfStudents))
                .skip(2)
                .collect(Collectors.toList());
        System.out.println(list);
    }

    @Run(active = false, id = "sorted by number of student with limit 2 and skip 2 and collect to list")
    static void sortedExample5() {
        List<Course> list = courses.stream()
                .sorted(Comparator.comparingInt(Course::getNoOfStudents))
                .limit(1)
                .skip(2)
                .collect(Collectors.toList());

        System.out.println(list);
    }


    @Run(active = false, id = "sorted by number of student and take while the review score is greater than 91 and collect to list")
    static void sortedTakeWhileExample() {
        List<Course> list = courses.stream()
                .sorted(Comparator.comparingInt(Course::getReviewScore).reversed())
                .takeWhile(course -> Course.isReviewScoreGreaterThan(course, 91))
                .collect(Collectors.toList());

        System.out.println(list);
    }

    @Run(active = false, id = "sorted by number of student and drop while the review score is greater than 95 and collect to list")
    static void sortedDropWhileExample() {
        List<Course> list = courses.stream()
                .sorted(Comparator.comparingInt(Course::getReviewScore).reversed())
                .dropWhile(course -> Course.isReviewScoreGreaterThan(course, 95))
                .collect(Collectors.toList());

        System.out.println(list);
    }

    @Run(active = false, id = "get course with max review or get default")
    static void getMaxOrDefaultCourse() {
        Course defaultCourse = new Course("Kubernetes", "Cloud", 95, 21000);

        Course course = courses.stream()
                .max(Comparator.comparingInt(Course::getReviewScore))
                .orElse(defaultCourse);

        System.out.println(course);
    }

    @Run(active = false, id = "get max number of student for a course")
    static void mapToIntExample1() {

        int max = courses.stream()
                .mapToInt(Course::getNoOfStudents)
                .max()
                .orElse(-1);

        System.out.println(max);
    }

    @Run(active = false, id = "get minimum number of student for a course")
    static void mapToIntExample2() {

        int min = courses.stream()
                .mapToInt(Course::getNoOfStudents)
                .min()
                .orElse(-1);

        System.out.println(min);
    }

    @Run(active = false, id = "get min number of student for a course")
    static void mapToIntExample3() {

        int total = courses.stream()
                .mapToInt(Course::getNoOfStudents)
                .sum();

        System.out.println(total);
    }

    @Run(active = false, id = "get min number of student for a course")
    static void mapToIntExample4() {

        double average = courses.stream()
                .mapToInt(Course::getNoOfStudents)
                .average()
                .orElse(0);

        System.out.println(average);
    }


    @Run(active = false, id = "group by in collect example 1")
    static void collectGroupByThenCreateMap1() {
        Map<String, List<Course>> coursesGroupByCourseCategory = courses.stream()
                .collect(Collectors.groupingBy(Course::getCategory));

        System.out.println(coursesGroupByCourseCategory.keySet());
        System.out.println(coursesGroupByCourseCategory);
    }

    @Run(active = false, id = "group by in collect example 2")
    static void collectGroupByThenCreateMap2() {
        Map<String, Long> coursesCountGroupByCourseCategory = courses.stream()
                .collect(
                        Collectors.groupingBy(
                                Course::getCategory,
                                Collectors.counting()
                        )
                );

        System.out.println(coursesCountGroupByCourseCategory.keySet());
        System.out.println(coursesCountGroupByCourseCategory);
    }

    @Run(active = false, id = "group by in collect example 3")
    static void collectGroupByThenCreateMap3() {
        Map<String, Optional<Course>> highestReviewedCourseGroupByCourseCategory = courses.stream()
                .collect(
                        Collectors.groupingBy(
                                Course::getCategory,
                                Collectors.maxBy(Comparator.comparing(Course::getReviewScore))
                        )
                );

        System.out.println(highestReviewedCourseGroupByCourseCategory.keySet());
        System.out.println(highestReviewedCourseGroupByCourseCategory);
    }

    @Run(active = false, id = "group by in collect example 4")
    static void collectGroupByThenCreateMa4() {
        Map<String, List<String>> coursesNameGroupByCategory = courses.stream()
                .collect(
                        Collectors.groupingBy(
                                Course::getCategory,
                                Collectors.mapping(Course::getName, Collectors.toList())
                        )
                );

        System.out.println(coursesNameGroupByCategory.keySet());
        System.out.println(coursesNameGroupByCategory);
    }

}