package io.github.abhishekghoshh.collection;

import io.github.abhishekghoshh.runner.Run;

import java.util.Stack;
import java.util.stream.IntStream;

public class StackTest {


    @Run(active = false)
    private static void stack() {
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

        System.out.println("adding 1..10 to stack");
        IntStream.rangeClosed(1, 10).forEach(stack::push);
        System.out.println(stack);
        while (!stack.isEmpty()) System.out.print(stack.pop() + " ");
        System.out.println();
    }
}
