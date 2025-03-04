package app.collection;

import app.util.Run;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static app.util.Utils.time;

public class ListTest {

    private static final int COUNTER = 100000;

    public static void main(String[] args) throws Exception {


        time(() -> addFirst(new LinkedList<>()), "For adding at first into LinkedList :");

        time(() -> addLast(new ArrayList<>()), "For adding at last into ArrayList :");
        time(() -> addLast(new ArrayList<>(COUNTER)), "For adding at last into ArrayList with size given :");
        time(() -> addLast(new Vector<>()), "For adding at last into Vector :");
        time(() -> addLast(new Vector<>(COUNTER)), "For adding at last into Vector with size given :");
        time(() -> addLast(new LinkedList<>()), "For adding at last into LinkedList :");
    }

    @Run(active = false)
    private static void basicListOperation() {
        List<String> names = Arrays.asList("Dave", "Jessie", "Alex", "Dan");
        // both lines are same
        for (String name : names) System.out.println(name);
        names.forEach(System.out::println);

        List<String> numbers = Arrays.asList("one", "two", "three", "four");
        AtomicInteger count = new AtomicInteger();
        numbers.forEach((number) -> {
            System.out.println("number : " + number);
            System.out.println(count.getAndIncrement());
        });
    }

    @Run(active = false, id = "For adding at first into ArrayList :")
    static void addFirstIntoArrayList() {
        addFirst(new ArrayList<>());
    }

    @Run(active = false, id = "For adding at first into ArrayList with size given :")
    static void addFirstIntoArrayListWithCounter() {
        addFirst(new ArrayList<>(COUNTER));
    }

    @Run(active = false, id = "For adding at first into Vector :")
    static void addFirstIntoVector() {
        addFirst(new Vector<>());
    }

    @Run(active = false, id = "For adding at first into Vector with size given :")
    static void addFirstIntoVectorWithCounter() {
        addFirst(new Vector<>(COUNTER));
    }






    public static void addFirst(ArrayList<Integer> list) {
        for (int i = 0; i < COUNTER; i++) {
            list.add(0, i);
        }
    }

    public static void addFirst(Vector<Integer> list) {
        for (int i = 0; i < COUNTER; i++) {
            list.add(0, i);
        }
    }

    public static void addFirst(LinkedList<Integer> list) {
        for (int i = 0; i < COUNTER; i++) {
            list.addFirst(i);
        }
    }

    public static void addLast(ArrayList<Integer> list) {
        for (int i = 0; i < COUNTER; i++) {
            list.add(i);
        }
    }

    public static void addLast(Vector<Integer> list) {
        for (int i = 0; i < COUNTER; i++) {
            list.add(i);
        }
    }

    public static void addLast(LinkedList<Integer> list) {
        for (int i = 0; i < COUNTER; i++) {
            list.addLast(i);
        }
    }
}
