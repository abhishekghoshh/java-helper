package app.functional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static app.util.Utils.print;

public class StreamOfStringTest {
    public static void main(String[] args) {
        List<String> courses = new ArrayList<>(
                List.of("Spring", "Spring Boot", "API", "Microservices",
                        "AWS", "PCF", "Azure", "Docker", "Kubernetes", "GCP"
                )
        );

        print(() -> courses.stream()
                        .collect(Collectors.joining(","))
                , "join stream of string with Collector joining");

        print(() -> String.join(",", courses)
                , "join ");

        print(() -> courses.stream()
                        .map(String::toUpperCase)
                        .collect(Collectors.toList())
                , "replace with upper case directly with stream map");

        print(() -> {
            List<String> clonedCourses = clone(courses);
            clonedCourses.replaceAll(String::toUpperCase);
            return clonedCourses;
        }, "replace with upper case  directly without stream map");


        print(() -> courses.stream()
                        .filter(course -> course.length() < 4)
                        .collect(Collectors.toList())
                , "filter string directly with stream filter");

        print(() -> {
            List<String> clonedCourses = clone(courses);
            clonedCourses.removeIf(course -> course.length() < 4);
            return clonedCourses;
        }, "filter string directly without stream filter");


        print(() -> courses.stream()
                        .collect(Collectors.joining(",", "[", "]"))
                , "Collectors joining with prefix and suffix");

        print(() -> courses.stream()
                        .map(course -> course.split(""))
                        .flatMap(Arrays::stream)
                        .collect(Collectors.toList())
                , "collect with split then flat map then collect");


        print(() -> courses.stream()
                        .map(course -> course.split(""))
                        .flatMap(Arrays::stream)
                        .distinct()
                        .collect(Collectors.toList())
                , "collect with split then flat map then distinct then collect");


        List<List<String>> allCountries = List.of(
                List.of("Colombia", "Finland", "Greece", "Iceland", "Liberia", "Mali", "Mauritius"),
                List.of("Peru", "Serbia", "Singapore", "Turkey", "Uzbekistan", "Yemen", "Zimbabwe", "Greece", "Iceland"),
                List.of("India", "Bangladesh", "Sri Lanka", "Russia", "USA", "China", "England", "Pakistan"));

        print(() -> allCountries.stream()
                        .flatMap(List::stream)
                        .distinct()
                        .collect(Collectors.toList())
                , "use flat map then distinct then collect");
    }

    private static <T> List<T> clone(Collection<T> collections) {
        return new ArrayList<>(collections);
    }


}
