package app.collection;

import app.runner.Run;
import app.runner.Runner;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.synchronizedList;

@Run(active = false, all = true)
public class SynchronizedListTest {

    public static final int COUNTER = 10000000;

    public static void main(String[] args) throws Exception {
        Runner.runAnnotatedMethods(SynchronizedListTest.class);
    }

    @Run(active = false, timer = true, timeIdentifier = "Normal Array List", printMethodName = true)
    private static void checkForNormalArrayList() throws InterruptedException {
        ArrayList<String> arrayList = new ArrayList<>();
        checkSynchronizedMethods(arrayList);
    }

    @Run(active = false, timer = true, timeIdentifier = "Synchronized Array List", printMethodName = true)
    private static void checkForNormalSynchronizedArrayList() throws InterruptedException {
        List<String> synchronizedArrayList = synchronizedList(new ArrayList<>());
        checkSynchronizedMethods(synchronizedArrayList);
    }

    private static void checkSynchronizedMethods(List<String> list) throws InterruptedException {
        Thread t1 = new Thread(new ListTester(list, "class 1"));
        Thread t2 = new Thread(new ListTester(list, "class 2"));

        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println(list.size());
    }

    static public class ListTester implements Runnable {

        private final List<String> list;
        private final String message;

        public ListTester(List<String> list, String message) {
            this.message = message;
            this.list = list;
        }

        public void run() {
            try {
                this.add();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        }

        synchronized void add() throws InterruptedException {
            for (int i = 0; i < COUNTER; i++) {
                list.add("I am inside " + message + " and the value is " + i);
            }
        }
    }

}
