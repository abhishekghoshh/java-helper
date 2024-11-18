package com.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.util.Utils.run;

public class CollectionsTest {

    public static void main(String[] args) {
        run(() -> {
            // into it else it will give java.lang.UnsupportedOperationException
            List<Integer> list = Arrays.asList(1, 5, 3, 2, 4);
            // If we create an arraylist using Arrays.asList then we can't add anything
            list.add(5);
        }, true);

        run(() -> {
            // into it else it will give java.lang.UnsupportedOperationException
            List<Integer> list = List.of(1, 5, 3, 2, 4);
            // If we create an arraylist using Arrays.asList then we can't add anything
            list.add(5);
        }, true);

        run(() -> {
            List<Integer> list = list();
            // Shuffles the list
            Collections.shuffle(list);
            System.out.println("shuffled list " + list);
        });

        run(() -> {
            List<Integer> list = list();
            // rotates the list
            Collections.rotate(list, 2);
            System.out.println("rotated list " + list);
        });

        run(() -> {
            List<Integer> src = list();
            List<Integer> copy = new ArrayList<>(Collections.nCopies(src.size(), null));
            // copy one list to another
            Collections.copy(copy, src);
            System.out.println("copied list " + copy);
        });

        run(() -> {
            // create an unmodifiable list
            List<Integer> list = Collections.unmodifiableList(list());
            list.add(5);
        }, true);

        run(() -> {
            // creates synchronized thread-safe list by using intrinsic lock
            Collections.synchronizedList(list());
        });


    }

    static List<Integer> list() {
        return new ArrayList<>(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
    }
}
