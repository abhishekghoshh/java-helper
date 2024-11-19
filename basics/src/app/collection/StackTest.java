package app.collection;

import java.util.Stack;

public class StackTest {
    public static void main(String[] args) {
        /*
         * We have considered Vectors - and we came to the conclusion that ArrayList is
         * a better option usually. Stack extends the Vector class - which means that
         * stacks are inherently synchronized. however synchronization is not always
         * needed - in such cases it is better to use ArrayDeque
         */
        Stack<Integer> stack = new Stack<>();
        stack.push(10);
        System.out.println("stack.peek() : " + stack.peek());
        System.out.println("stack.pop() : " + stack.pop());
    }
}
