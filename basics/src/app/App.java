package app;

import app.basics.GenericsTest;
import app.basics.ReflectionsTest;
import app.collection.*;
import app.functional.StreamTest;

import static app.util.Runner.run;

public class App {
    public static void main(String[] args) {
        // basics
        run(GenericsTest.class);
        run(ReflectionsTest.class);

        // collections
        run(CollectionsTest.class);
        run(ListTest.class);
        run(SynchronizedListTest.class);
        run(MapTest.class);
        run(SetTest.class);
        run(QueueTest.class);
        run(StackTest.class);
        run(CollectionSortTest.class);

        // concurrency


        // streams
        run(StreamTest.class);
    }
}
