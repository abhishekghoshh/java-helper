package app.functional;

import app.model.Student;
import app.runner.Run;

import java.util.function.Function;
import java.util.function.Supplier;

@Run(active = false)
public class MethodReferenceTest {

    @Run(active = false)
    static void methodReferenceTest() {
        // Supplier functional interface, both are same
        Supplier<Student> studentSupplier1 = () -> new Student();
        System.out.println("studentSupplier1 " + studentSupplier1.get());
        Supplier<Student> studentSupplier2 = Student::new;
        System.out.println(" studentSupplier2 " + studentSupplier2.get());

        // Function functional interface, both are same
        Function<String, Student> studentFunction1 = name -> new Student(name);
        System.out.println("studentFunction1 " + studentFunction1.apply("abhishek"));
        Function<String, Student> studentFunction2 = Student::new;
        System.out.println(" studentSupplier2 " + studentFunction2.apply("abhishek"));
    }
}
