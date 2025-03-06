package app.collection;

import app.runner.Run;

import java.util.*;

import static app.runner.Runner.time;

public class MapTest {


    private static final int COUNTER = 100000;

    @Run(active = false)
    private static void hashMap() {
        Map<String, String> agesMap = new HashMap<>();
        populateAges(agesMap);
        print(agesMap); // HashMap does not maintain the insertion order
    }

    @Run(active = false)
    private static void linkedHashMap() {
        Map<String, String> agesMap;
        agesMap = new LinkedHashMap<>();
        populateAges(agesMap);
        print(agesMap); // LinkedHashMap maintain the insertion order
    }

    @Run(active = false)
    private static void treeMap() {
        Map<String, String> agesMap;
        agesMap = new TreeMap<>();// Tree map relies on Red Black tree data structure
        populateAges(agesMap);
        print(agesMap);// treemap is sort the keys internally as it's a tree data structure so traversal of items will be depending on keys
    }

    @Run(active = false)
    private static void reverseTreeMap() {
        Map<String, String> agesMap;
        agesMap = new TreeMap<>(Collections.reverseOrder());// We can assign our comparator as well
        populateAges(agesMap);
        print(agesMap);

        Set<Map.Entry<String, String>> agesEntrySet = agesMap.entrySet();
        System.out.println(agesEntrySet);
        Set<String> keySet = agesMap.keySet();
        System.out.println(keySet);
    }

    @Run(active = false)
    private static void hashMapTimeChecking() throws Exception {
        final Map<Integer, String> hashMap = time(() -> put(new HashMap<>()), "Hashmap adding :");
        time(() -> get(hashMap), "Hashmap getting :");
    }

    @Run(active = false)
    private static void linkedHashMapTimeChecking() throws Exception {
        final Map<Integer, String> linkedHashMap = time(() -> put(new LinkedHashMap<>()), "linkedHashMap adding :");
        time(() -> get(linkedHashMap), "linkedHashMap getting :");
    }

    @Run(active = false)
    private static void treeMapTimeChecking() throws Exception {
        final Map<Integer, String> treeMap = time(() -> put(new TreeMap<>()), "treeMap adding :");
        time(() -> get(treeMap), "treeMap getting :");
    }

    @Run(active = false)
    private static void hashMapWithCustomClass() {
        Map<Person, String> persons = new HashMap<>();
        persons.put(new Person("Abhishek Ghosh", 24), "abhishek1@@gmail.com");
        persons.put(new Person("Abhishek Pal", 24), "abhishek2@gmail.com");
        print(persons);

        /*
         * if we don't define hasCode and equals method then hasCode method of the
         * object class will be invoked then every time a new hashCode will be generated
         * when there is a new creation of same object though there value can be same
         */
        System.out.println(persons.get(new Person("Abhishek Ghosh", 24)));
        System.out.println(persons.get(new Person("Abhishek Pal", 24)));

        /*
         * From java 8, hash map has started using red-black tree once threshold of
         * linkedList is reached in case of hash collision. So in the worst case hashMap
         * has read time complexity as O(log(n)) rather than o(n)
         */
    }


    public static Map<Integer, String> put(Map<Integer, String> map) {
        for (int i = 0; i < COUNTER; i++) {
            map.put(i, "Number_" + i);
        }
        return map;
    }

    public static Map<Integer, String> get(Map<Integer, String> map) {
        for (int i = 0; i < COUNTER; i++) {
            map.get(i);
        }
        return map;
    }

    public static void populateAges(Map<String, String> agesMap) {
        agesMap.put("Abhishek Ghosh", "24");
        agesMap.put("Nasim Molla", "25");
        agesMap.put("Bishal Mukherjee", "25");
        agesMap.put("Kushal Ghosh", "23");
        agesMap.put("Abhishek Pal", "24");
    }

    public static void print(Map<?, ?> map) {
        System.out.println(map.getClass().getCanonicalName() + " : ");
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            System.out.print(entry.getKey() + " : " + entry.getValue() + " ");
        }
        System.out.println();
    }

    static class Person {
        private String name;
        private int age;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        // hasCode method is used to determine the hashcode value of an object
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + age;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            return result;
        }

        /*
         * when there is any collision then on the bucket's linkedList on each item
         * equal method is checked if it matches with any item then the item's value is
         * just updated else it will just add a new node in the end of the linkedlist
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            Person other = (Person) obj;
            if (age != other.age) return false;
            if (name == null) return other.name == null;
            return name.equals(other.name);
        }

        @Override
        public String toString() {
            return "Person [name=" + name + ", age=" + age + "]";
        }

    }
}
