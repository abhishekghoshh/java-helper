package app.collection;

import app.runner.Run;
import app.runner.Runner;

import java.util.*;

public class CollectionSortTest {
    public static void main(String[] args) {
        Runner.runAnnotatedMethods(CollectionSortTest.class);
    }

    @Run(active = false)
    private static void primitiveSort() {
        int[] nums = {1, 5, -5, 4, 12, 3};
        // sorts in ascending
        Arrays.sort(nums);
        for (int num : nums) System.out.print(num + " ");
        System.out.println();
    }

    @Run(active = false)
    private static void stringSort() {
        List<String> names = Arrays.asList(
                "Abhishek Ghosh",
                "Abhishek Pal",
                "Bishal Mukherjee",
                "Nasim Molla",
                "Kushal Ghosh"
        );

        // Sorts ascending
        Collections.sort(names);
        System.out.println(names);

        // Sorts descending using => Collections.sort(names, Collections.reverseOrder())
        names.sort(Collections.reverseOrder());
        System.out.println(names);
    }

    @Run(active = false)
    private static void customClassSorting() {
        List<Person> persons = new ArrayList<>();
        persons.add(new Person("Abhishek Ghosh", 24));
        persons.add(new Person("Abhishek Ghosh", 23));
        persons.add(new Person("Abhishek Pal", 24));
        persons.add(new Person("Bishal Mukherjee", 25));
        persons.add(new Person("Nasim Molla", 25));
        persons.add(new Person("Kushal Mukherjee", 23));

        Collections.sort(persons);// Sorting based on Comparable.compareTo implementation
        System.out.println(persons);

        // Sorting based on lambda => (p1, p2) -> p1.getName().compareTo(p2.getName())
        persons.sort(Comparator.comparing(Person::getName));
        System.out.println(persons);

        // implementation of Comparator.compare
        persons.sort(Comparator
                .comparing(Person::getName)
                .thenComparing(Person::getAge)
        );
        System.out.println(persons);

        // usually comparator is better than comparable as the comparing logic is separated for the actual class
        persons.sort(Comparator
                .comparing(Person::getName)
                .thenComparing(Person::getAge)
                .reversed()
        );
        System.out.println(persons);
    }

    static class Person implements Comparable<Person> {
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

        @Override
        public String toString() {
            return "Person [name=" + name + ", age=" + age + "]";
        }

        @Override
        public int compareTo(Person o) {
            return Integer.compare(this.getAge(), o.getAge());
        }

    }
}
