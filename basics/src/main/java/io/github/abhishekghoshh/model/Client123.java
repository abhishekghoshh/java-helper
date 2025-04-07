package io.github.abhishekghoshh.model;

public class Client123 implements Interface1, Interface2, Interface3 {

    public void methodA() {
        //overriding the default method in the implementation class.
        System.out.println("Inside method A " + Client123.class);
    }
}
