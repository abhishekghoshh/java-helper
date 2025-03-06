package app.basics;

import app.runner.Run;
import app.runner.Runner;

import java.io.Serial;
import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReflectionsTest {

    @Run(active = false, showError = true, showStacktrace = true)
    private static void loadClass() throws ClassNotFoundException {
        System.out.println("person.getName() " + Person.class.getName());
        System.out.println("person.getCanonicalName() " + Person.class.getCanonicalName());

        Class<?> className = Class.forName("app.basics.ReflectionsTest.Person");
        System.out.println("className.getCanonicalName() : " + className.getCanonicalName());
    }

    @Run(active = false, showError = true)
    private static void classInformation() {
        Class<Person> personClass = Person.class;
        Person person = new Person("Abhishek Ghosh", 24);

        System.out.println("person.getName() " + person.getName());
        System.out.println("personClass.getCanonicalName() " + personClass.getCanonicalName());
        System.out.println("personClass.getPackageName() " + personClass.getPackageName());
    }

    @Run(active = false, showError = true)
    private static void checkFields() {
        Class<Person> personClass = Person.class;
        checkFields(personClass.getFields(), "personClass.getFields()");
        checkFields(personClass.getDeclaredFields(), "personClass.getDeclaredFields()");
        // getting all fields by getDeclaredFields
        setFieldsToPublic(personClass.getDeclaredFields(), "this fields will be public");
    }

    @Run(active = false)
    private static void checkMethods() throws InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<Person> personClass = Person.class;

        // Getting public methods of the class through the object of the class by using getMethods
        checkMethods(personClass.getMethods(), "personClass.getMethods()");
        // creates an object of desired method by providing the method name and parameter class as arguments to the getDeclaredMethod
        checkMethods(personClass.getDeclaredMethods(), "personClass.getDeclaredMethods()");

        // Getting the constructor of the class through the object of the class
        checkConstructor(personClass.getDeclaredConstructors(), "personClass.getDeclaredConstructors()");
    }


    @Run(active = false)
    public static void checkInterfaces() {
        Class<?>[] interfaces = Person.class.getInterfaces();
        System.out.println("personClass.getInterfaces() : " + interfaces.length);
        for (Class<?> interfaceName : interfaces) {
            System.out.println("Interface name is " + interfaceName.getCanonicalName());
        }
    }

    @Run(active = false)
    public static void checkSuperClass() {
        Class<?> superclass = Person.class.getSuperclass();
        System.out.println("personClass.getSuperclass() superclass name is " + superclass.getCanonicalName());
    }


    @Run(active = false)
    public static <T> void checkAnnotation() throws NoSuchMethodException,
            InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Person person = new Person();
        System.out.println("Custom Annotations");
        Class<?> cls = person.getClass();
        // Creates object of desired method by providing the method name as argument to the getDeclaredMethod
        Method printMethod = cls.getDeclaredMethod("print", String.class);
        printMethod.setAccessible(true);

        if (printMethod.isAnnotationPresent(Logging.class)) {
            // get logging type
            Logging logging = printMethod.getAnnotation(Logging.class);
            Level level = logging.type().equalsIgnoreCase(Logging.INFO) ? Level.INFO : Level.WARNING;
            // setting the logger to the field
            Logger logger = Logger.getLogger(cls.getCanonicalName());
            logger.setLevel(level);
            Field field = cls.getDeclaredField("logger");
            field.setAccessible(true);
            field.set(person, logger);
            // get output of the toString and invokes the method at runtime
            Method toStringMethod = cls.getDeclaredMethod("toString");
            String output = (String) toStringMethod.invoke(person);
            printMethod.invoke(person, output);
        }
    }


    public static void checkFields(Field[] fields, String identifier) {
        System.out.println(identifier + " : " + fields.length);
        for (Field field : fields) {
            System.out.println(field.getName() + " return type : " + field.getType() +
                    ", modifier type : " + Modifier.toString(field.getModifiers()));
        }
        System.out.println();
    }

    public static void checkMethods(Method[] methods, String identifier) {
        System.out.println(identifier + " : " + methods.length);
        for (Method method : methods) {
            System.out.println(method.getName() + " return type : " + method.getReturnType() +
                    ", modifier type : " + Modifier.toString(method.getModifiers()));
        }
        System.out.println();
    }

    public static void setFieldsToPublic(Field[] declaredFields, String identifier) {
        System.out.println(identifier + " : " + declaredFields.length);
        for (Field field : declaredFields) {
            if (Modifier.isPrivate(field.getModifiers())) {
                System.out.println("Field name : " + field.getName() + ", modifier type " + Modifier.toString(field.getModifiers()));
                // allows the object to access the field irrespective of the access specifier used with the field
                field.setAccessible(true);
            }
        }
        System.out.println();
    }

    public static void checkConstructor(Constructor<?>[] constructors, String identifier)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        System.out.println(identifier + " : " + constructors.length);
        for (Constructor<?> constructor : constructors) {
            System.out.println(constructor.getName() + " constructor with " + constructor.getParameterCount() +
                    " parameters, is accessible :" + Modifier.isPrivate(constructor.getModifiers()));
            if (constructor.getParameterCount() == 0) {
                constructor.setAccessible(true);
                System.out.println("creating new instance of the " + constructor.newInstance());
            }
        }

    }


    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Logging {
        String type();
        String INFO = "INFO";
    }

    static class Person extends Thread implements Comparable<Person>, Serializable {
        /**
         *
         */
        @Serial
        private static final long serialVersionUID = -3123937727503616788L;
        private String fullName;
        private int age;
        Logger logger;

        public Person(String fullName, int age) {
            super();
            this.fullName = fullName;
            this.age = age;
        }

        @SuppressWarnings("unused")
        private Person() {
            this.fullName = "Default";
            this.age = 0;
            // throw new RuntimeException("don't instantiate from reflection also");
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        @Logging(type = Logging.INFO)
        public void print(String output) {
            logger.log(logger.getLevel(), output);
        }

        @Override
        public String toString() {
            return "Person [fullName=" + fullName + ", age=" + age + "]";
        }

        @Override
        public int compareTo(Person o) {
            return this.getFullName().compareTo(o.getFullName());
        }

        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName() + " : Person thread is running");
        }

    }


}
