package app;

import app.basics.GenericsTest;
import app.basics.InterfacesDefaultMethodTest;
import app.basics.ReflectionsTest;
import app.collection.*;
import app.concurrency.ThreadTest;
import app.functional.*;

import static app.util.Runner.runMain;

public class App {
    public static void main(String[] args) {
        // basics
        runMain(InterfacesDefaultMethodTest.class);
        runMain(GenericsTest.class);
        runMain(ReflectionsTest.class);

        // collections
        runMain(CollectionsTest.class);
        runMain(ListTest.class);
        runMain(SynchronizedListTest.class);
        runMain(MapTest.class);
        runMain(SetTest.class);
        runMain(QueueTest.class);
        runMain(StackTest.class);
        runMain(CollectionSortTest.class);

        // concurrency
        runMain(ThreadTest.class);

        // functional
        runMain(ImperativeVsDeclarativeTest.class);
        runMain(FunctionalInterfacesTest.class);
        runMain(MethodReferenceTest.class);
        runMain(PrimitiveTypeStreamTest.class);
        runMain(StreamOfStringTest.class);
        runMain(FileStreamTest.class);
        runMain(StreamTest.class);

    }
}
