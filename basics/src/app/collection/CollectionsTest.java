package app.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static app.util.Utils.run;

public class CollectionsTest {

    public static void main(String[] args) {
        // If we create an arraylist using Arrays.asList, then we can't add anything
        run(() -> {
            List<Integer> list = Arrays.asList(1, 5, 3, 2, 4);
            // it else it will give java.lang.UnsupportedOperationException
            list.add(5);
        }, true);

        // If we create an arraylist using List.of then we can't add anything
        run(() -> {
            List<Integer> list = List.of(1, 5, 3, 2, 4);
            // into it else it will give java.lang.UnsupportedOperationException
            list.add(5);
        }, true);

        // Shuffles the list
        run(() -> {
            List<Integer> list = list();
            Collections.shuffle(list);
            System.out.println("shuffled list " + list);
        });

        // rotates the list
        run(() -> {
            List<Integer> list = list();
            Collections.rotate(list, 2);
            System.out.println("rotated list " + list);
        });

        // copy one list to another
        run(() -> {
            List<Integer> src = list();
            List<Integer> copy = new ArrayList<>(Collections.nCopies(src.size(), null));
            Collections.copy(copy, src);
            System.out.println("copied list " + copy);
        });

        // create an unmodifiable list
        run(() -> {
            List<Integer> list = Collections.unmodifiableList(list());
            list.add(5);
        }, true);

        // creates a synchronized thread-safe list by using intrinsic lock
        run(() -> {
            Collections.synchronizedList(list());
        });


    }

    static List<Integer> list() {
        return new ArrayList<>(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
    }
}
