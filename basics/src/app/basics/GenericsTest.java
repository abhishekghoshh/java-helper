package app.basics;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class GenericsTest {
    public static void main(String[] args) {
        Item<String> item1 = item("Test1");
        Item<String> item2 = item("Test2");
        print(min(item1, item2));
        print(add(1f, 4.500010f));

        // Type Inference
        List<String> list1 = new ArrayList<>();
        // Type Inference
        List<String> list2 = get(new ArrayList<>(), new LinkedList<>());

        // Type witness
        List<String> list3 = get(new ArrayList<>(), new LinkedList<>());

        List<?> list = Arrays.asList(1, 2, "Abhishek", 4);
        printList(list);

        // upper bounded wildcard example
        List<? extends Number> list4 = new ArrayList<Integer>();
        List<? extends Number> list5 = new ArrayList<Double>();
        List<? extends Number> list6 = new ArrayList<Float>();
        // We can not insert when we are using upper bounded wildcards
        // list4.add(10); -> this operation is not possible

        // lower bound wild card example
        List<? super Integer> list7 = new ArrayList<Integer>();
        List<? super Integer> list8 = new ArrayList<Number>();
        List<? super Integer> list9 = new ArrayList<Object>();

        // We can add any Number type but when we want to get then we have no other option other than Object class type
        List<? super Number> list10 = new ArrayList<>();
        list10.add(5);
        list10.add(5.01);
        list10.add(5.00000502f);
        Object obj = list10.get(0);
        System.out.println(obj);


        List<Number> l = new ArrayList<>();
        l.add(10);
        l.add(1.23);
        System.out.println(l);
    }

    // Bounded type parameter
    public static <T extends Comparable<T>> T min(T ob1, T ob2) {
        return ob1.compareTo(ob2) < 0 ? ob1 : ob2;
    }

    public static <T, R> Entry<T, R> entry(T key, R value) {
        return new Entry<>(key, value);
    }

    public static <T extends Comparable<T>> Item<T> item(T item) {
        return new Item<>(item);
    }

    public static <T> void print(T object) {
        System.out.println(object);
    }


    // Bounded type parameter
    public static <T extends Number> double add(T num1, T num2) {
        return num1.doubleValue() + num2.doubleValue();
    }

    // Type Inference
    public static <T> T get(T item1, T item2) {
        if (Math.random() < 0.5) {
            return item1;
        } else {
            return item2;
        }
    }

    // Wild card
    public static <T> void printList(List<?> list) {
        for (Object obj : list) {
            System.out.print(obj + " ");
        }
    }

    public static <T> void printList2(List<? super Number> list) {
        // we can not do (Number obj : list) it will give us the compile time exception
        // we have no other option other than using Object
        for (Object obj : list) {
            System.out.print(obj + " ");
        }
    }

    // reading from upper bound and inserting with lower bound
    public static <T> void copy(List<? extends T> source, List<? super T> destination) {
        destination.addAll(source);
    }

    //Bounded type parameter
    static class Item<T extends Comparable<T>> implements Comparable<Item<T>> {
        private final T item;

        public Item(T item) {
            this.item = item;
        }

        @Override
        public String toString() {
            return "Item [item=" + item + "]";
        }

        @Override
        public int compareTo(Item<T> o) {
            return this.item.compareTo(o.item);
        }

    }

    static class Entry<T, R> {
        private final T key;
        private final R value;

        public Entry(T key, R value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return "Entry [key=" + key + ", value=" + value + "]";
        }

    }


    static class Notes<T> extends ArrayList<T> {

        /**
         *
         */
        @Serial
        private static final long serialVersionUID = 1L;

    }
}