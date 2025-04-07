package io.github.abhishekghoshh.collection;

import io.github.abhishekghoshh.runner.Run;

import java.util.*;

public class SetTest {

    @Run(active = false)
    private static void hashSet() {
        // based on hashcode , does not maintain order almost same as hashmap
        Set<Integer> hashSet = new HashSet<>();
        add(hashSet);
        print(hashSet);
    }

    @Run(active = false)
    private static void linkedHashSet() {
        // based on hashcode and doubly linked list, this maintains order
        // almost same as linked hashmap
        Set<Integer> linkedHashSet = new LinkedHashSet<>();
        add(linkedHashSet);
        print(linkedHashSet);
    }

    @Run(active = false)
    private static void treeSet() {
        // based on red black tree, implementation of SortedSet interface ,sorted
        Set<Integer> treeSet = new TreeSet<>();
        add(treeSet);
        print(treeSet);
    }

    @Run(active = false)
    private static void sortedSet() {
        // tree set implements Navigable set and transitively Sorted Set
        SortedSet<Integer> sortedSet = new TreeSet<>();
        add(sortedSet);

        System.out.println("sortedSet.first() : " + sortedSet.first());
        System.out.println("sortedSet.last() : " + sortedSet.last());

        print(sortedSet);

        SortedSet<Integer> subSet = sortedSet.subSet(2, 8);
        print(subSet);

        SortedSet<Integer> taiSet = sortedSet.tailSet(7);
        print(taiSet);
    }

    private static void add(Set<Integer> set) {
        set.add(1);
        set.add(5);
        set.add(2);
        set.add(4);
        set.add(3);
        set.add(6);
        set.add(9);
        set.add(10);
    }


    private static void print(Set<Integer> set) {
        for (int item : set) {
            System.out.print(item + " ");
        }
        System.out.println();
    }
}
