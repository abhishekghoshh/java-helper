package io.github.abhishekghoshh.functional;

import io.github.abhishekghoshh.runner.Run;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Run(active = false)
public class StreamOfStringTest {

    static List<String> courses = new ArrayList<>(
            List.of("Spring", "Spring Boot", "API", "Microservices", "AWS", "PCF", "Azure", "Docker", "Kubernetes", "GCP")
    );


    @Run(active = false, id = "join stream of string with Collector joining")
    static void collectorJoining() {
        String ans = courses.stream()
                .collect(Collectors.joining(","));
        System.out.println(ans);
        System.out.println(String.join(",", courses));
    }

    @Run(active = false, id = "Collectors joining with prefix and suffix")
    static void collectorJoiningWithPrefixAndSuffix() {
        String ans = courses.stream()
                .collect(Collectors.joining(",", "[", "]"));

        System.out.println(ans);
    }


    @Run(active = false, id = "replace with upper case directly with stream map")
    static void collectToList() {
        List<String> ans = courses.stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());

        System.out.println(ans);
    }

    @Run(active = false, id = "replace with upper case directly without stream map")
    static void replaceList() {
        List<String> copy = new ArrayList<>(courses);
        copy.replaceAll(String::toUpperCase);
        System.out.println(copy);
    }

    @Run(active = false, id = "filter string directly with stream filter")
    static void stringStreamWithFilter() {
        List<String> list = courses.stream()
                .filter(course -> course.length() < 4)
                .collect(Collectors.toList());

        System.out.println(list);
    }


    @Run(active = false, id = "filter string directly without stream filter")
    static void filterWithoutStream() {
        List<String> copy = new ArrayList<>(courses);
        copy.removeIf(course -> course.length() < 4);
        System.out.println(copy);
    }


    @Run(active = false, id = "collect with split then flat map then collect")
    static void flatMapExample() {
        List<String> list = courses.stream()
                .map(course -> course.split(""))
                .flatMap(Arrays::stream)
                .collect(Collectors.toList());

        System.out.println(list);
    }

    @Run(active = false, id = "collect with split then flat map then distinct then collect")
    static void flatMapAndDistinct() {
        List<String> list = courses.stream()
                .map(course -> course.split(""))
                .flatMap(Arrays::stream)
                .distinct()
                .collect(Collectors.toList());

        System.out.println(list);
    }

    @Run(active = false, id = "use flat map then distinct then collect")
    static void flatMapTest() {
        List<List<String>> allCountries = List.of(
                List.of("Colombia", "Finland", "Greece", "Iceland", "Liberia", "Mali", "Mauritius"),
                List.of("Peru", "Serbia", "Singapore", "Turkey", "Uzbekistan", "Yemen", "Zimbabwe", "Greece", "Iceland"),
                List.of("India", "Bangladesh", "Sri Lanka", "Russia", "USA", "China", "England", "Pakistan"));

        List<String> list = allCountries.stream()
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());

        System.out.println(list);
    }


}
