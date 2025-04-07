package app.model;

import java.util.List;

public class MultiplierImpl implements Multiplier {

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
