package app.collection;

import java.util.*;

public class SetTest {
    public static void main(String[] args) {
        // based on hashcode , does not maintain order almost same as hashmap
        Set<String> hashSet = new HashSet<>();

        // based on hashcode and doubly linked list, this maintains order
        // almost same as linked hashmap
        Set<String> linkedHashSet = new LinkedHashSet<>();

        // based on red black tree, implementation of SortedSet interface ,sorted
        Set<String> treeSet = new TreeSet<>();

        // tree set implements Navigable set and transitively Sorted Set
        SortedSet<Integer> sortedSet = new TreeSet<>();
        sortedSet.add(1);
        sortedSet.add(5);
        sortedSet.add(2);
        sortedSet.add(4);
        sortedSet.add(3);
        sortedSet.add(6);
        sortedSet.add(9);
        sortedSet.add(10);

        System.out.println("sortedSet.first() : " + sortedSet.first());
        System.out.println("sortedSet.last() : " + sortedSet.last());

        print(sortedSet);

        SortedSet<Integer> subSet = sortedSet.subSet(2, 8);
        print(subSet);

        SortedSet<Integer> taiSet = sortedSet.tailSet(7);
        print(taiSet);
    }


    private static void print(Set<Integer> set) {
        for (int item : set) {
            System.out.print(item + " ");
        }
        System.out.println();
    }
}
