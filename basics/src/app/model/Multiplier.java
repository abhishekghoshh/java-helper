package app.model;

import java.util.List;

public interface Multiplier {
    int multiply(List<Integer> integerList);

    default int size(List<Integer> list) {
        System.out.println("Inside interface");
        return list.size();
    }

    static boolean isEmpty(List<Integer> list) {
        return list != null && list.isEmpty();
    }
}
