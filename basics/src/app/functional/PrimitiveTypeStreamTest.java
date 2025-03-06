package app.functional;

import app.runner.Run;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

@Run
public class PrimitiveTypeStreamTest {

    @Run(active = false, id = "even numbers multiplied by 2 and print")
    static void integerStream1() {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9);
        numbers.stream()
                .filter(num -> num % 2 == 0)
                .map(num -> num * 2)
                .forEach(i -> System.out.print(i + " "));
    }

    @Run(active = false, id = "reducing to one number")
    static void integerStream2() {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9);
        Integer ans = numbers.stream()
                .reduce(0, Integer::sum);

        System.out.println(ans);
    }

    @Run(active = false, id = "collecting to a list")
    static void integerStream3() {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9);
        List<Double> ans = numbers.stream()
                .map(Math::sqrt)
                .collect(Collectors.toList());

        System.out.println(ans);
    }

    @Run(active = false, id = "distinct and sorted in natural order")
    static void integerStream4() {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9);
        numbers.stream()
                .distinct()
                .sorted(Comparator.naturalOrder())
                .forEach(i -> System.out.print(i + " "));
        System.out.println();
    }

    @Run(active = false, id = "distinct and sorted in reversed order")
    static void integerStream5() {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9);
        numbers.stream()
                .distinct()
                .sorted(Comparator.reverseOrder())
                .forEach(i -> System.out.print(i + " "));
    }

    @Run(active = false, id = "distinct and sorted with out custom comparator")
    static void integerStream6() {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9);
        numbers.stream()
                .distinct()
                .sorted(Comparator.comparing(num -> num))
                .forEach(i -> System.out.print(i + " "));
    }

    @Run(active = false, printMethodName = true)
    static void referencePipelineAndPrimitiveStreamExample() {
        // this is integer stream(Wrapper class stream)  => java.util.stream.ReferencePipeline$Head@29ee9faa
        Stream<Integer> referencePipeline = Stream.of(1, 2, 3, 4, 5, 6);
        System.out.println(referencePipeline);

        // this is int stream(primitive type stream)  => java.util.stream.IntPipeline$Head@14acaea5
        IntStream intStream = Arrays.stream(new int[]{1, 2, 3, 4, 5, 6});
        System.out.println(intStream);
    }

    @Run(active = false, id = "Integer stream sum")
    static void intStreamSum() {
        int sum = IntStream.range(1, 10)
                .sum();
        System.out.println(sum);
    }

    @Run(active = false, id = "Integer stream with range closed [1,10] sum")
    static void intStreamClosedExample() {
        int sum = IntStream.rangeClosed(1, 10)
                .sum();
        System.out.println(sum);
    }

    @Run(active = false, id = "iterate with limit")
    static void intStreamWithLimitExample() {
        int sum = IntStream.iterate(0, e -> e + 1)
                .limit(10)
                .sum();
        System.out.println(sum);
    }

    @Run(active = false, id = "iterate with limit then sum")
    static void intStreamWithLimitAndPeekThenSumExample() {
        int sum = IntStream.iterate(0, e -> e + 1)
                .limit(10)
                .peek(i -> System.out.print(i + " "))
                .sum();
        System.out.println();
        System.out.println(sum);
    }

    @Run(active = false, id = "int stream to stream then collect")
    static void intStreamToStreamThenCollectExample() {
        List<Integer> ans = IntStream.iterate(0, e -> e + 1)
                .limit(10)
                .boxed()
                .collect(Collectors.toList());

        System.out.println(ans);
    }

    @Run(active = false, id = "int stream to stream then reduce")
    static void intStream() {
        BigInteger ans = IntStream.rangeClosed(1, 10)
                .mapToObj(BigInteger::valueOf)
                .reduce(BigInteger.ONE, BigInteger::multiply);

        System.out.println(ans);
    }

    public static final int COUNTER = 100000000;

    @Run(active = false, id = "sum without parallel", timer = true)
    static void longStream1() {
        long sum = LongStream
                .rangeClosed(0, COUNTER)
                .sum();
        System.out.println(sum);
    }

    @Run(active = false, id = "sum with parallel", timer = true)
    static void longStream2() {
        long sum = LongStream
                .rangeClosed(0, COUNTER)
                .parallel()
                .sum();
        System.out.println(sum);
    }

}
