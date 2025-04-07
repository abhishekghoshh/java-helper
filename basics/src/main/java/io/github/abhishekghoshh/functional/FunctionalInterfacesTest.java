package io.github.abhishekghoshh.functional;

import io.github.abhishekghoshh.model.Student;
import io.github.abhishekghoshh.model.StudentDataBase;
import io.github.abhishekghoshh.runner.Run;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.*;
import java.util.stream.Collectors;

@Run(active = false)
public class FunctionalInterfacesTest {

    @Run(active = false, id = "This is runnable test", timer = true)
    static void runnableTest() {
        Runnable runnable = () -> System.out.println("I'm Inside runnable");
        runnable.run();
    }


    @Run(active = false, id = "This is callable")
    static void callableTest() throws Exception {
        Callable<Integer> callable = () -> {
            System.out.println("I am inside callable, Exit code is 0");
            return 0;
        };
        System.out.println(callable.call());
    }

    @Run(active = false, id = "Predicates and its composition")
    static void predicateTest() {
        Predicate<Integer> p = num -> (num % 2 == 0);
        System.out.println("Result is p : " + p.test(2));


        Predicate<Integer> p1 = num -> (num % 4 == 0);
        System.out.println("Result is p1 : " + p1.test(3));

        Predicate<Integer> p2 = num -> (num % 5 == 0);
        System.out.println("Result in predicate and : " + p1.and(p2).test(10));

        System.out.println("Result in predicate or : " + p1.or(p2).test(4));

        // equivalent to reversing the result
        System.out.println("Result in predicateNegate : " + p1.and(p2).negate().test(4));
    }

    @Run(active = false, id = "Primitive type predicate : IntPredicate and DoublePredicate")
    static void primitiveTypePredicate() {
        int random = (int) (Math.random() * 10);
        IntPredicate intPredicate = num -> (num % 2 == 0);
        System.out.println(random + " intPredicate.test(random)is : " + intPredicate.test(random));

        DoublePredicate doublePredicate = num -> (num % 4.0 == 0);
        System.out.println(random + " doublePredicate.test(random)is : " + doublePredicate.test(random));
    }

    @Run(active = false, id = "Bi-predicate example")
    static void biPredicate() {
        BiPredicate<Integer, Double> biPredicate = (gradeLevel, gpa) -> gradeLevel >= 3 && gpa >= 3.9;
        Consumer<Student> consumer = (student) -> {
            if (biPredicate.test(student.getGradeLevel(), student.getGpa())) {
                System.out.println(student);
            }
        };
        List<Student> studentList = StudentDataBase.getAllStudents();
        studentList.forEach(consumer);
    }

    @Run(active = false, id = "Predicate on custom class")
    static void predicateWithCustomClass() {
        List<Student> studentList = StudentDataBase.getAllStudents();

        Predicate<Student> p1 = (s) -> s.getGradeLevel() >= 3;
        Predicate<Student> p2 = (s) -> s.getGpa() >= 3.9;

        System.out.println(
                studentList.stream()
                        .filter(p1)
                        .collect(Collectors.toList())
        );

        studentList.forEach((student -> {
            if (p2.test(student)) {
                System.out.println(student);
            }
        }));

        Function<Integer, Predicate<Integer>> gradePredicate = num1 -> (num -> (num > num1));
        Function<Double, Predicate<Double>> gpaPredicate = num1 -> (num -> (num > num1));

        boolean result = gradePredicate.apply(2).test(5)
                && gpaPredicate.apply(3.6).test(3.9);
        System.out.println("Result from Predicate : " + result);
    }


    @Run(active = false, id = "This is consumer example")
    static void consumerTest() {
        Consumer<String> c1 = (s) -> System.out.println(s.toUpperCase());
        c1.accept("java8");

        Consumer<Student> c2 = p -> System.out.print(p.getName().toUpperCase());
        Consumer<Student> c3 = p -> System.out.println(p.getActivities());
        List<Student> personList = StudentDataBase.getAllStudents();
        personList.forEach(c2.andThen(c3));

        List<Student> students = StudentDataBase.getAllStudents();
        students.forEach((s) -> {
            if (s.getGradeLevel() >= 3 && s.getGpa() > 3.9)
                c2.andThen(c3).accept(s);
        });

        Consumer<String> cd = s -> System.out.println(s.toUpperCase());
        cd.accept("abc");
    }

    @Run(active = false, id = "Primitive consumer")
    static void primitiveConsumer() {
        IntConsumer intConsumer = c -> System.out.println(c * c);
        intConsumer.accept(3);

        DoubleConsumer doubleConsumer = c -> System.out.println(c * c);
        doubleConsumer.accept(3.0);
        doubleConsumer.accept(3);
    }

    @Run(active = false, id = "This is bi consumer example")
    static void biConsumer() {
        BiConsumer<String, String> biConsumer = (a, b) -> System.out.println(" a : " + a + " b : " + b);
        biConsumer.accept("java7", "java8");

        BiConsumer<Integer, Integer> multiply = (a, b) -> System.out.println("Multiplication : " + (a * b));
        BiConsumer<Integer, Integer> addition = (a, b) -> System.out.println("Addition : " + (a + b));
        BiConsumer<Integer, Integer> division = (a, b) -> System.out.println("Division : " + (a / b));
        multiply.andThen(addition).andThen(division).accept(10, 5);

        BiConsumer<String, List<String>> studentBiConsumer = (name, activities) -> System.out.println(name + " : " + activities);
        List<Student> students = StudentDataBase.getAllStudents();
        students.forEach((s) -> studentBiConsumer.accept(s.getName(), s.getActivities()));
    }

    @Run(active = false, id = "Predicate and consumer example")
    static void predicateAndConsumerExample() {
        Predicate<Student> p1 = (s) -> s.getGradeLevel() >= 3;
        Predicate<Student> p2 = (s) -> s.getGpa() >= 3.9;
        BiConsumer<String, List<String>> studentBiConsumer = (name, activities) -> System.out.println(name + " : " + activities);
        Consumer<Student> studentConsumer = (student) -> {
            if (p1.and(p2).test(student)) {
                studentBiConsumer.accept(student.getName(), student.getActivities());
            }
        };
        List<Student> studentList = StudentDataBase.getAllStudents();
        studentList.forEach(studentConsumer);
    }

    @Run(active = false, id = "This is supplier")
    static void supplierExample() {
        Supplier<Student> studentSupplier = () -> Student.adam;
        System.out.println("Student is : " + studentSupplier.get());

        Supplier<List<Student>> studentsSupplier = StudentDataBase::getAllStudents;
        System.out.println("Students are : " + studentsSupplier.get());
    }

    @Run(active = false, id = "This is function functional interface")
    static void functionTest() {
        Function<String, String> upperCase = String::toUpperCase;
        Function<String, String> addSomeString = (name) -> name.toUpperCase().concat("default");

        System.out.println("Result of andthen : " + upperCase.andThen(addSomeString).apply("java8"));
        System.out.println("Result of compose : " + upperCase.compose(addSomeString).apply("java8"));

        System.out.println("Result is : " + upperCase.apply("java8"));

        Function<String, Integer> strLength = String::length;
        System.out.println(strLength.apply("Abhishek"));


        Function<String, String> abc = Function.identity();
        System.out.println(abc.apply("ABC"));
    }

    @Run(active = false, id = "This is function with the custom class")
    static void functionWithCustomClass() {
        Predicate<Student> p1 = (s) -> s.getGradeLevel() >= 3;
        Function<List<Student>, Map<String, Double>> function = (students -> {
            Map<String, Double> studentGradeMap = new HashMap<>();
            students.forEach((student -> {
                if (p1.test(student)) {
                    studentGradeMap.put(student.getName(), student.getGpa());
                }
            }));
            return studentGradeMap;
        });
        System.out.println(function.apply(StudentDataBase.getAllStudents()));
    }


    @Run(active = false, id = "This is Bi function example")
    static void biFunctionExample() {
        Map<String, String> loginPageLocs = new HashMap<>();
        Predicate<Student> p2 = (s) -> s.getGpa() >= 3.9;
        BiFunction<List<Student>, Predicate<Student>, Map<String, Double>> biFunction = (students, studentPredicate) -> {
            Map<String, Double> studentGradeMap = new HashMap<>();
            students.forEach((student -> {
                if (studentPredicate.test(student)) {
                    studentGradeMap.put(student.getName(), student.getGpa());
                }
            }));
            return studentGradeMap;
        };
        System.out.println(biFunction.apply(StudentDataBase.getAllStudents(), p2));

        BiFunction<String, String, String> getLoginLocs = (sLocator, elementType) -> loginPageLocs.get(sLocator);
        getLoginLocs.apply("locator", "elementType");
    }


    @Run(active = false, id = "This is unary operator, accepts same and return the same")
    static void unaryExample() {
        UnaryOperator<String> unaryOperator = (s) -> s.concat("Default");
        System.out.println(unaryOperator.apply("java8"));
    }

    @Run(active = false, id = "Binary operator functional interface")
    static void binaryOperatorExample() {
        BinaryOperator<Integer> binaryOperator = (a, b) -> a * b;
        System.out.println(binaryOperator.apply(3, 4));

        Comparator<Integer> comparator = Integer::compareTo;

        BinaryOperator<Integer> maxBy = BinaryOperator.maxBy(comparator);
        System.out.println("Result is: " + maxBy.apply(5, 6));

        BinaryOperator<Integer> minBy = BinaryOperator.minBy(comparator);
        System.out.println("Result is: " + minBy.apply(5, 6));
    }
}
