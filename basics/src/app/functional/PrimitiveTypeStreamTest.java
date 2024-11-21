package app.functional;

import app.util.Utils;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static app.util.Utils.*;

public class PrimitiveTypeStreamTest {

    public static void main(String[] args) {
        integerStream();
        intStream();
        longStream();
    }


    private static void integerStream() {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9);

        run(() -> numbers.stream()
                        .filter(num -> num % 2 == 0)
                        .map(num -> num * 2)
                        .forEach(Utils::print),
                "even numbers multiplied by 2 and print");

        print(() -> numbers.stream()
                        .reduce(0, Integer::sum),
                "reducing to one number");

        print(() -> numbers.stream()
                        .map(Math::sqrt)
                        .collect(Collectors.toList()),
                "collecting to a list");

        run(() -> numbers.stream()
                        .distinct()
                        .sorted(Comparator.naturalOrder())
                        .forEach(Utils::print),
                "distinct and sorted in natural order"
        );

        run(() -> numbers.stream()
                        .distinct()
                        .sorted(Comparator.reverseOrder())
                        .forEach(Utils::print),
                "distinct and sorted in reversed order"
        );

        run(() -> numbers.stream()
                        .distinct()
                        .sorted(Comparator.comparing(num -> num))
                        .forEach(Utils::print),
                "distinct and sorted with out custom comparator"
        );
    }

    private static void intStream() {
        // this is integer stream(Wrapper class stream)  => java.util.stream.ReferencePipeline$Head@29ee9faa
        Stream<Integer> referencePipeline = Stream.of(1, 2, 3, 4, 5, 6);
        System.out.println(referencePipeline);

        // this is int stream(primitive type stream)  => java.util.stream.IntPipeline$Head@14acaea5
        IntStream intStream = Arrays.stream(new int[]{1, 2, 3, 4, 5, 6});
        System.out.println(intStream);

        print(() -> IntStream.range(1, 10)
                        .sum(),
                "Integer stream sum");

        print(() -> IntStream.rangeClosed(1, 10)
                        .sum(),
                "Integer stream with range closed [1,10] sum");

        print(() -> IntStream.iterate(0, e -> e + 1)
                        .limit(10)
                        .sum()
                , "iterate with limit");

        print(() -> IntStream.iterate(0, e -> e + 1)
                        .limit(10)
                        .peek(Utils::print)
                        .sum()
                , "iterate with limit then sum");

        print(() -> IntStream.iterate(0, e -> e + 1)
                        .limit(10)
                        .boxed()
                        .collect(Collectors.toList())
                , "int stream to stream then collect");

        print(() -> IntStream.rangeClosed(1, 10)
                        .mapToObj(BigInteger::valueOf)
                        .reduce(BigInteger.ONE, BigInteger::multiply)
                , "int stream to stream then reduce");
    }

    public static final int COUNTER = 100000000;

    private static void longStream() {
        time(() -> LongStream
                        .rangeClosed(0, COUNTER)
                        .sum()
                , "sum without parallel");

        time(() -> LongStream
                        .rangeClosed(0, COUNTER)
                        .parallel()
                        .sum()
                , "sum with parallel");
    }
}
