package app.basics;

import app.functional.model.Student;
import app.functional.model.StudentDataBase;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class InterfacesDefaultMethodTest {
    public static void main(String[] args) {
        Client14 client14 = new Client14();
        client14.methodA();

        Client123 client123 = new Client123();
        client123.methodA(); // resolves to child Interface Implementation
        client123.methodB();
        client123.methodC();

        Multiplier multiplier = new MultiplierImpl();
        List<Integer> inputList = Arrays.asList(1, 3, 5);
        System.out.println("Result is : " + multiplier.multiply(inputList));
        System.out.println("Input List size is : " + multiplier.size(inputList));
        System.out.println("Is Empty : " + Multiplier.isEmpty(inputList));


        List<Student> studentList = StudentDataBase.getAllStudents();
        System.out.println("Original List");
        studentList.forEach(student -> System.out.println("student : " + student));
        sortByGender();
        sortByName(studentList);
        sortByGPA(studentList);
        comparatorChaining();
    }

    static Comparator<Student> nameComparator = Comparator.comparing(Student::getName);
    static Comparator<Student> gpaComparator = Comparator.comparing(Student::getGpa);
    static Comparator<Student> genderComparator = Comparator.comparing(Student::getGender);
    static Comparator<Student> gradeComparator = Comparator.comparing(Student::getGradeLevel);
    static Consumer<Student> studentConsumer = student -> System.out.println("student : " + student);

    public static void sortByName(List<Student> studentList) {
        studentList.sort(Comparator.comparing(Student::getName)); // inline
        studentList.sort(nameComparator); // Using a reference
        System.out.println("After Sort BY Name : ");
        studentList.forEach(studentConsumer);
    }

    public static void sortByGPA(List<Student> studentList) {
        studentList.sort(gpaComparator);
        System.out.println("After Sort BY GPA : ");
        studentList.forEach(studentConsumer);
    }

    public static void sortByGender() {
        List<Student> studentList = StudentDataBase.getAllStudents();
        Comparator<Student> nullLast = Comparator.nullsFirst(genderComparator);
        studentList.sort(nullLast); // sort is a default method
        System.out.println("After Sort By Gender : ");
        studentList.forEach(studentConsumer); // foreach is a default method

    }


    public static void comparatorChaining() {
        List<Student> studentList = StudentDataBase.getAllStudents();
        studentList.sort(gradeComparator.thenComparing(nameComparator));
        System.out.println("Comparator Chaining List");
        studentList.forEach(studentConsumer);
    }
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
