package app.basics;

import java.util.List;

public class Others {
}

interface Interface1 {
    default void methodA() {
        System.out.println("Inside method A" + Interface1.class);
    }
}

interface Interface2 extends Interface1 {
    default void methodB() {
        System.out.println("Inside method B");
    }

    default void methodA() {
        System.out.println("Inside method A " + Interface2.class);
    }
}

interface Interface3 extends Interface1, Interface2 {
    default void methodC() {
        System.out.println("Inside method C");
    }

    default void methodA() {
        System.out.println("Inside method A " + Interface3.class);
    }
}

interface Interface4 {
    default void methodA() {
        System.out.println("Inside method A" + Interface4.class);
    }
}

class Client14 implements Interface1, Interface4 {

    public void methodA() {
        System.out.println("Inside method A " + Client14.class);
    }
}

class Client123 implements Interface1, Interface2, Interface3 {

    public void methodA() {
        //overriding the default method in the implementation class.
        System.out.println("Inside method A " + Client123.class);
    }
}


interface Multiplier {
    int multiply(List<Integer> integerList);

    default int size(List<Integer> list) {
        System.out.println("Inside interface");
        return list.size();
    }

    static boolean isEmpty(List<Integer> list) {
        return list != null && list.isEmpty();
    }
}

class MultiplierImpl implements Multiplier {

    @Override
    public int multiply(List<Integer> list) {
        return list.stream()
                .reduce(1, (x, y) -> x * y);
    }

    @Override
    public int size(List<Integer> list) {
        System.out.println("Inside Implementation class");
        return list.size();
    }

}
