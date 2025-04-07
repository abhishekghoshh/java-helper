package io.github.abhishekghoshh.collection;

import io.github.abhishekghoshh.runner.Run;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ListTest {

    private static final int COUNTER = 100000;

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

    @Run(active = false, id = "For adding at first into ArrayList :", timer = true)
    static void addFirstIntoArrayList() {
        addFirst(new ArrayList<>());
    }

    @Run(active = false, id = "For adding at first into ArrayList with size given :", timer = true)
    static void addFirstIntoArrayListWithCounter() {
        addFirst(new ArrayList<>(COUNTER));
    }

    @Run(active = false, id = "For adding at first into Vector :", timer = true)
    static void addFirstIntoVector() {
        addFirst(new Vector<>());
    }

    @Run(active = false, id = "For adding at first into Vector with size given :", timer = true)
    static void addFirstIntoVectorWithCounter() {
        addFirst(new Vector<>(COUNTER));
    }


    @Run(active = false, id = "For adding at first into LinkedList :", timer = true)
    static void addFirstIntoLinkedList() {
        addFirst(new LinkedList<>());
    }

    @Run(active = false, id = "For adding at last into ArrayList :", timer = true)
    static void addLastToArrayList() {
        addLast(new ArrayList<>());
    }

    @Run(active = false, id = "For adding at last into ArrayList with size given :", timer = true)
    static void addLastToArrayListWithCounter() {
        addLast(new ArrayList<>(COUNTER));
    }

    @Run(active = false, id = "For adding at last into Vector :", timer = true)
    static void addLastToVector() {
        addLast(new Vector<>());
    }

    @Run(active = false, id = "For adding at last into Vector with size given :", timer = true)
    static void addLastToVectorWithCounter() {
        addLast(new Vector<>(COUNTER));
    }

    @Run(active = false, id = "For adding at last into LinkedList :", timer = true)
    static void addLastToLinkedList() {
        addLast(new LinkedList<>());
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
