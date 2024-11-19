package app.collection;

import java.util.*;

import static app.util.Utils.time;

public class QueueTest {

    public static final int COUNTER = 500000;

    public static void main(String[] args) throws Exception {
        Queue<Integer> queue = new LinkedList<>();
        queue.offer(10);// same as add method
        System.out.println("queue.peek() : " + queue.peek()); // getting the front element
        System.out.println("queue.poll() : " + queue.poll()); // removing the front element
        System.out.println("queue.size() : " + queue.size()); // queue size

        Queue<String> names = new LinkedList<>();
        names.offer("Abhishek");
        names.offer("Bishal");
        names.offer("Nasim");
        names.offer("Kushal");
        poll(names, "Names are : ");

        // Priority queue is based on priority heap
        // Each item has its priority
        // the elements of the priority queue are ordered according to their natural
        // ordering defined by the comparable interface
        // add() method add in the queue
        // peek() method retrieves the head of the queue
        // poll() method return the head else null

        // We can add our comparator implementation here as well
        Queue<Person> persons = new PriorityQueue<>();
        persons.add(new Person("Abhishek", 24));
        persons.add(new Person("Bishal", 25));
        persons.add(new Person("Nasim", 25));
        persons.add(new Person("Kushal", 23));
        poll(persons, "Persons using priority queue without comparator ");


        persons = new PriorityQueue<>(Comparator.comparingInt(Person::getAge));
        persons.add(new Person("Abhishek", 24));
        persons.add(new Person("Bishal", 25));
        persons.add(new Person("Nasim", 25));
        persons.add(new Person("Kushal", 23));
        poll(persons, "Persons using priority queue with custom comparator ");

        // Double ended queue
        Deque<Integer> deque = new ArrayDeque<>();
        deque.addFirst(10);
        deque.addLast(12);
        System.out.println("deque.removeFirst() : " + deque.removeFirst());
        System.out.println("deque.removeLast() : " + deque.removeLast());


        // stack by Deque
        Deque<Integer> stackByQueue = new ArrayDeque<>();
        stackByQueue.push(1);
        stackByQueue.push(2);
        stackByQueue.push(3);
        stackByQueue.push(4);
        stackByQueue.push(5);
        pop(stackByQueue, "Stack by Array deque");

        final Deque<Integer> dq = time(() -> add(new ArrayDeque<>()), "adding into array deque");
        time(() -> pop(dq), "popping from array deque");

        // all methods are synchronized as everytime when we want to access any method there is lock
        final Stack<Integer> stk = time(() -> add(new Stack<>()), "adding into array deque");
        time(() -> pop(stk), "popping from array deque");
    }

    private static void poll(Queue<?> queue, String identifier) {
        System.out.println(identifier);
        while (!queue.isEmpty()) {
            System.out.print(queue.poll() + " ");
        }
        System.out.println();
    }

    private static void pop(Deque<?> stack, String identifier) {
        System.out.println(identifier);
        while (!stack.isEmpty()) {
            System.out.print(stack.pop() + " ");
        }
        System.out.println();
    }

    public static Deque<Integer> add(Deque<Integer> stack) {
        for (int i = 0; i < COUNTER; i++) {
            stack.push(i);
        }
        return stack;
    }

    public static void pop(Deque<Integer> stack) {
        while (!stack.isEmpty()) {
            stack.pop();
        }
    }

    public static Stack<Integer> add(Stack<Integer> stack) {
        for (int i = 0; i < COUNTER; i++) {
            stack.push(i);
        }
        return stack;
    }

    public static void pop(Stack<Integer> stack) {
        while (!stack.isEmpty()) {
            stack.pop();
        }
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
//		return Integer.compare(this.getAge(), o.getAge());
            return this.getName().compareTo(o.getName());
        }

    }
}