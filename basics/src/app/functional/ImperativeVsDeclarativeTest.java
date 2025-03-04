package app.functional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static app.util.Runner.code;


public class ImperativeVsDeclarativeTest {
    public static void main(String[] args) {
        code(() -> {
            List<Integer> list = List.of(1, 2, 3, 4, 4, 5, 5, 6, 7, 7, 8, 9, 9);
            //Remove the duplicates from the list.
            List<Integer> uniqueList = new ArrayList<>();
            for (int item : list)
                if (!uniqueList.contains(item)) {
                    uniqueList.add(item);
                }
            System.out.println("unique List : " + uniqueList);

            List<Integer> uniqueList1 = list.stream()
                    .distinct()
                    .toList();
            System.out.println("uniqueList1 : " + uniqueList1);
        }).id("Imperative vs declarative style 1");


        code(() -> {
            // sum of integers for the range from 0 to 100
            // shared mutable state and its sequential anf it will go step by step,
            // and it will have issues if we try to run the code in multithreaded environment
            int sum = 0;
            for (int i = 0; i <= 100; i++) {
                sum += i;
            }
            System.out.println("Sum is : " + sum);

            int sum1 = IntStream.rangeClosed(0, 100).sum();
            System.out.println("sum1 : " + sum1);
        }).id("Imperative vs declarative style 2");
    }
}
