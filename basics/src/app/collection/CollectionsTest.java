package app.collection;

import app.runner.Run;
import app.runner.Runner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CollectionsTest {

    public static void main(String[] args) {
        Runner.runAnnotatedMethods(CollectionsTest.class);
    }

    @Run(active = false)
    private static void unmodifiableList2() {
        // If we create an arraylist using List.of then we can't add anything
        List<Integer> list = List.of(1, 5, 3, 2, 4);
        // into it else it will give java.lang.UnsupportedOperationException
        list.add(5);
    }

    @Run(active = false)
    private static void unmodifiableList3() {
        // create an unmodifiable list
        List<Integer> list = Collections.unmodifiableList(list());
        list.add(5);
    }

    @Run(active = false)
    private static void shuffleList() {
        // Shuffles the list
        List<Integer> list = list();
        Collections.shuffle(list);
        System.out.println("shuffled list " + list);
    }

    @Run(active = false)
    private static void synchronizedList() {
        // creates a synchronized thread-safe list by using intrinsic lock
        List<Integer> synchronizedList = Collections.synchronizedList(list());
    }

    @Run(active = false)
    private static void rotateList() {
        // rotates the list
        List<Integer> list = list();
        Collections.rotate(list, 2);
        System.out.println("rotated list " + list);
    }

    @Run(active = false)
    private static void copyList() {
        // copy one list to another
        List<Integer> src = list();
        List<Integer> copy = new ArrayList<>(Collections.nCopies(src.size(), null));
        Collections.copy(copy, src);
        System.out.println("copied list " + copy);
    }

    static List<Integer> list() {
        return new ArrayList<>(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
    }
}
