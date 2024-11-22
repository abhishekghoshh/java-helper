package app;

import app.basics.GenericsTest;
import app.basics.InterfacesDefaultMethodTest;
import app.basics.ReflectionsTest;
import app.collection.*;
import app.concurrency.ThreadTest;
import app.functional.*;

import static app.util.Runner.run;

public class App {
    public static void main(String[] args) {
        // basics
        run(InterfacesDefaultMethodTest.class);
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
        run(ThreadTest.class);

        // functional
        run(ImperativeVsDeclarativeTest.class);
        run(FunctionalInterfacesTest.class);
        run(MethodReferenceTest.class);
        run(PrimitiveTypeStreamTest.class);
        run(StreamOfStringTest.class);
        run(FileStreamTest.class);
        run(StreamTest.class);

    }
}
