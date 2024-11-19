package app.basics;

import java.io.Serial;
import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static app.util.Utils.run;

public class ReflectionsTest {
    public static void main(String[] args) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        run(() -> {
            Class<?> className = Class.forName("app.basics.ReflectionsTest.Person");
            System.out.println("className.getCanonicalName() : " + className.getCanonicalName());
        });

        // we can get via this method
        Class<Person> personClass = Person.class;
        Person person = new Person("Abhishek Ghosh", 24);
        System.out.println(personClass.getCanonicalName());
        System.out.println(personClass.getPackageName());

        checkFields(personClass.getFields(), "personClass.getFields()");
        checkFields(personClass.getDeclaredFields(), "personClass.getDeclaredFields()");

        checkMethods(personClass.getMethods(), "personClass.getMethods()");
        checkMethods(personClass.getDeclaredMethods(), "personClass.getDeclaredMethods()");

        setFieldsToPublic(personClass.getDeclaredFields(), "this fields will be public");

        checkConstructor(personClass.getDeclaredConstructors(), "personClass.getDeclaredConstructors()");

        checkInterfaces(personClass.getInterfaces(), "personClass.getInterfaces()");

        checkSuperClass(personClass.getSuperclass(), "personClass.getSuperclass()");

        checkAnnotation(person, "Custom Annotations");
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
                System.out.println(constructor.newInstance());
            }
        }

    }

    public static void checkInterfaces(Class<?>[] interfaces, String identifier) {
        System.out.println(identifier + " : " + interfaces.length);
        for (Class<?> interfaceName : interfaces) {
            System.out.println("Interface name is " + interfaceName.getCanonicalName());
        }
    }

    public static void checkSuperClass(Class<? super Person> superclass, String identifier) {
        System.out.println(identifier + " superclass name is " + superclass.getCanonicalName());
    }

    public static void checkAnnotation(Person obj, String identifier) throws NoSuchMethodException {
        System.out.println(identifier);
        Class<?> cls = obj.getClass();
        Method printMethod = cls.getMethod("print", Logger.class);
        if (printMethod.isAnnotationPresent(Logging.class)) {
            Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
            Logging logging = printMethod.getAnnotation(Logging.class);
            Level level = logging.type().equalsIgnoreCase(Logging.INFO) ? Level.INFO : Level.WARNING;
            logger.setLevel(level);
            obj.print(logger);
        }
    }


    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Logging {
        String type();

        String WARNING = "WARNING";
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
        public void print(Logger logger) {
            logger.log(logger.getLevel(), toString());
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
