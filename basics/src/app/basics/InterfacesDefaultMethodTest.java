package app.basics;

import app.functional.model.Student;
import app.functional.model.StudentDataBase;
import app.util.Run;
import app.util.Runner;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;


public class InterfacesDefaultMethodTest {

    public static void main(String[] args) throws Exception {
        Runner.runAnnotatedMethods(InterfacesDefaultMethodTest.class);
    }

    @Run(active = false)
    static void defaultInterfaceMethods() {
        Client14 client14 = new Client14();
        client14.methodA();

        Client123 client123 = new Client123();
        client123.methodA(); // resolves to child Interface Implementation
        client123.methodB();
        client123.methodC();
    }

    @Run(active = false)
    private static void defaultInterfaceMethods1() {
        Multiplier multiplier = new MultiplierImpl();
        List<Integer> inputList = Arrays.asList(1, 3, 5);
        System.out.println("Result is : " + multiplier.multiply(inputList));
        System.out.println("Input List size is : " + multiplier.size(inputList));
        System.out.println("Is Empty : " + Multiplier.isEmpty(inputList));
    }

    @Run(active = false)
    public static void sortByName() {
        Comparator<Student> nameComparator = Comparator.comparing(Student::getName);
        Consumer<Student> studentConsumer = student -> System.out.println("student : " + student);


        List<Student> studentList = StudentDataBase.getAllStudents();
        System.out.println("Original List");
        studentList.forEach(student -> System.out.println("student : " + student));
        studentList.sort(Comparator.comparing(Student::getName)); // inline
        studentList.sort(nameComparator); // Using a reference
        System.out.println("After Sort BY Name : ");
        studentList.forEach(studentConsumer);
    }

    @Run(active = false)
    public static void sortByGPA() {
        Comparator<Student> gpaComparator = Comparator.comparing(Student::getGpa);
        Consumer<Student> studentConsumer = student -> System.out.println("student : " + student);


        List<Student> studentList = StudentDataBase.getAllStudents();
        System.out.println("Original List");
        studentList.forEach(student -> System.out.println("student : " + student));
        
        studentList.sort(gpaComparator);
        System.out.println("After Sort BY GPA : ");
        studentList.forEach(studentConsumer);
    }

    @Run(active = false)
    public static void sortByGender() {
        Comparator<Student> genderComparator = Comparator.comparing(Student::getGender);
        Consumer<Student> studentConsumer = student -> System.out.println("student : " + student);
        
        
        List<Student> studentList = StudentDataBase.getAllStudents();
        Comparator<Student> nullLast = Comparator.nullsFirst(genderComparator);
        studentList.sort(nullLast); // sort is a default method
        System.out.println("After Sort By Gender : ");
        studentList.forEach(studentConsumer); // foreach is a default method

    }

    @Run(active = false)
    public static void comparatorChaining() {
        Comparator<Student> nameComparator = Comparator.comparing(Student::getName);
        Comparator<Student> gradeComparator = Comparator.comparing(Student::getGradeLevel);
        Consumer<Student> studentConsumer = student -> System.out.println("student : " + student);
        
        
        List<Student> studentList = StudentDataBase.getAllStudents();
        studentList.sort(gradeComparator.thenComparing(nameComparator));
        System.out.println("Comparator Chaining List");
        studentList.forEach(studentConsumer);
    }
}
