# 16. Functional Programming

## Lambda

### Theory

- **Core Concepts**: A lambda expression (Java 8+) is a concise, anonymous function-like value — `(params) -> expression-or-block` — that provides an implementation for a single abstract method of a functional interface. It is Java's mechanism for treating behavior as data without the verbosity of an anonymous inner class.
- **Internal Working**: Unlike anonymous inner classes, lambdas are **not** compiled into a separate `.class` file per lambda at compile time; instead, `javac` emits an `invokedynamic` instruction whose bootstrap method (`LambdaMetafactory.metafactory`) generates the actual implementing class lazily at first execution, using the `java.lang.invoke` API.
- **When to Use It**: Whenever you need to pass a short, focused piece of behavior as an argument — `Comparator`, stream pipeline operations (`map`, `filter`, `forEach`), event handlers, `Runnable`/`Callable` tasks.
- **Advantages**: Dramatically reduces boilerplate versus anonymous classes, enables the entire Streams API and functional-style composition, captures enclosing variables without an explicit constructor.
- **Limitations**: Lambdas can only implement functional interfaces (exactly one abstract method); captured local variables must be effectively final; overuse of deeply nested lambdas can hurt readability and make stack traces harder to read; debugging can be less intuitive than named methods.

### Internal Working

- **Step-by-Step Explanation**: 1) `javac` type-checks the lambda against its target functional interface (inferred from context — assignment, method parameter, cast) and compiles its body into a private synthetic method on the enclosing class (e.g., `lambda$main$0`). 2) At the call site, the compiler emits `invokedynamic`, whose bootstrap method is `LambdaMetafactory.metafactory` with static/dynamic arguments describing the functional interface's method signature and a `MethodHandle` pointing at the synthetic method. 3) On first execution of that `invokedynamic` call site, the JVM invokes the bootstrap method exactly once, which uses `MethodHandle`/`MethodType` machinery (and, since it's implemented via generated hidden classes, `ASM`-style bytecode spinning under the hood) to define a small hidden implementation class implementing the functional interface, wrapping the `MethodHandle`. 4) The `CallSite` returned is cached, so subsequent invocations at that call site directly invoke the generated implementation with near-zero further linkage overhead.
- **Memory Layout**: The lambda's captured variables (for non-static lambdas capturing `this` or local effectively-final variables) are stored as constructor-supplied fields on the heap-allocated lambda instance, analogous to an anonymous inner class instance; the class metadata for the dynamically generated implementation lives in metaspace, but as a **hidden class** it is more readily unloadable by the GC (via `Lookup.defineHiddenClass`) when no longer referenced, unlike a classic named nested class.
- **Diagrams**:

```
Comparator<String> byLength = (a, b) -> a.length() - b.length();
                    |
                    v (javac)
private static int lambda$main$0(String a, String b) { return a.length() - b.length(); }
                    |
        invokedynamic call site -> LambdaMetafactory.metafactory (bootstrap, first call only)
                    |
        generates hidden class implementing Comparator, wrapping a MethodHandle to lambda$main$0
                    |
        CallSite cached -> subsequent calls invoke directly, no further metafactory cost
```

- **JVM Behaviour**: Because generation is deferred to first use via `invokedynamic`, classes for lambdas that are never executed are never generated at all (unlike anonymous classes, which are always compiled to `.class` files whether used or not); the JIT compiler treats the resulting call sites like any other monomorphic call after linkage, allowing full inlining once warmed up, and the hidden classes are eligible for unloading, reducing metaspace pressure in lambda-heavy, dynamically-generated-class-heavy applications.

### Interview Questions

**Basic**
1. What is a lambda expression, and what must the target type be?
2. What does "effectively final" mean in the context of variables captured by a lambda?

**Intermediate**
3. Why don't lambda expressions produce a visible `.class` file for each lambda the way anonymous inner classes do?
4. What is the difference between a lambda capturing `this` versus one that doesn't reference any enclosing instance state?

**Advanced**
5. Explain the role of `invokedynamic` and `LambdaMetafactory` in how lambdas are implemented.
6. Why are lambda-generated classes described as "hidden classes," and why does that matter for memory management?

**Scenario-based**
7. A lambda inside a loop captures a loop variable and a colleague gets a compile error "local variables referenced from a lambda expression must be final or effectively final." Explain why this restriction exists and how to work around it.

### Detailed Answers

1. A lambda expression is an anonymous implementation of a single abstract method, of the form `(params) -> body`. Its target type must be a **functional interface** — an interface with exactly one abstract method (default/static/`Object` methods don't count) — determined by the assignment context, method parameter type, or explicit cast; Java infers this target type rather than the lambda having an intrinsic type of its own.
2. "Effectively final" means a local variable is never reassigned after its initial assignment, even though it isn't explicitly declared `final`. Lambdas may only capture such variables (by value, at the point of lambda creation) because the captured value is copied into the lambda instance's field; allowing later reassignment of the original variable would create ambiguity about which value the lambda "sees," especially since the lambda might outlive the enclosing method's stack frame.
3. Lambdas use `invokedynamic` with `LambdaMetafactory` to generate their implementing class **lazily at runtime**, on first execution of the call site, rather than having `javac` eagerly emit a named `.class` file for every lambda at compile time. This keeps the compiled JAR smaller and defers (or entirely avoids, for unexecuted code paths) the cost of class generation.
4. A lambda that references `this` or an instance field/method of the enclosing class captures a reference to the enclosing instance (similar to a non-static inner class), meaning it implicitly keeps that instance reachable for as long as the lambda lives. A lambda that only uses its own parameters or effectively-final local variables (and no enclosing instance state) can be implemented without holding an enclosing-instance reference, which is sometimes compiled to a "non-capturing" lambda that the JVM can even reuse as a singleton instance for that call site, since it needs no per-invocation captured state.
5. `invokedynamic` is a bytecode instruction that defers the choice of what code actually runs (and how) to a bootstrap method resolved at first execution, rather than baking in a fixed method reference at compile time. For lambdas, the compiler emits an `invokedynamic` instruction whose bootstrap method is `java.lang.invoke.LambdaMetafactory.metafactory` (or `altMetafactory` for special cases like serializable lambdas); this method dynamically generates a class implementing the target functional interface that delegates to a `MethodHandle` referencing the compiler-synthesized lambda body method, and returns a `CallSite` that is cached for all future invocations of that specific lambda expression's call site.
6. Hidden classes (`Lookup.defineHiddenClass`, formally introduced as a JVM/API concept in Java 15, and used internally for lambda implementation since Java 8's original mechanism, later formalized) are classes that cannot be discovered by name via normal reflection/classloading and are not registered in the defining class loader's normal class list, meaning they can be unloaded independently of — and more eagerly than — regular named classes once unreachable, even while their defining class loader remains alive. This matters for memory management because applications generating large numbers of distinct lambda expressions (e.g., dynamically per request) don't permanently bloat metaspace, since the JVM can reclaim these hidden classes once the corresponding call sites/instances become unreachable.
7. Java requires captured local variables to be effectively final because the lambda's generated implementation class stores a **copy** of the captured variable's value in a field at the time the lambda instance is created; if the original loop variable could still change afterward, the lambda's captured copy and the "live" variable would diverge, creating confusing semantics (which value should the lambda see — the one at creation time, or the current one?). Since Java, unlike JavaScript, doesn't support true mutable closures, the workaround is to introduce a new effectively-final local variable inside the loop body (e.g., `final int captured = i;`) that is re-created fresh on each iteration and never reassigned, so each lambda instance captures its own independent, immutable snapshot.

### Code Examples

```java
import java.util.List;
import java.util.function.Predicate;

public class OrderFiltering {
    public static void main(String[] args) {
        List<Order> orders = List.of(
                new Order("ORD-1", 150.0, "SHIPPED"),
                new Order("ORD-2", 45.0, "PENDING"),
                new Order("ORD-3", 999.0, "SHIPPED")
        );

        // Lambda implementing Predicate<Order> - no target-type boilerplate needed
        Predicate<Order> isHighValueShipped = order ->
                order.status().equals("SHIPPED") && order.total() > 100.0;

        orders.stream()
              .filter(isHighValueShipped)
              .forEach(o -> System.out.println("High value shipped: " + o.id()));
    }

    record Order(String id, double total, String status) {}
}
```

```java
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class LambdaCaptureDemo {
    public static void main(String[] args) {
        List<Supplier<Integer>> suppliers = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            final int captured = i; // fresh effectively-final variable per iteration
            suppliers.add(() -> captured * captured);
        }

        // Each lambda captured its own independent snapshot: prints 0, 1, 4
        suppliers.forEach(s -> System.out.println(s.get()));
    }
}
```

## Functional Interfaces

### Theory

- **Core Concepts**: A functional interface is any interface with exactly one abstract method (a "Single Abstract Method" or SAM type), optionally annotated with `@FunctionalInterface` for compiler-enforced documentation. It may still declare any number of `default`, `static`, or `private` methods, and inherited `Object` methods (`equals`, `hashCode`, `toString`) don't count toward the abstract-method count. `java.util.function` provides a rich standard library of them: `Function<T,R>`, `Predicate<T>`, `Supplier<T>`, `Consumer<T>`, `BiFunction<T,U,R>`, `UnaryOperator<T>`, and primitive specializations (`IntPredicate`, `ToDoubleFunction<T>`, etc.).
- **Internal Working**: The interface itself is a completely ordinary compiled interface at the bytecode level; what makes it usable as a lambda target is purely the SAM-shape rule checked by `javac` at the lambda/method-reference call site, not any special runtime marker.
- **When to Use It**: Whenever an API needs to accept "a piece of behavior" as a parameter — reuse a standard `java.util.function` interface whenever its shape fits, rather than declaring a redundant custom interface, to maximize interoperability with the Streams/Optional APIs.
- **Advantages**: Enables lambda/method-reference syntax, promotes composability (`Function.andThen`, `Predicate.and/or/negate`), standard interfaces improve API consistency across the JDK and third-party libraries.
- **Limitations**: Only one abstract method is allowed, so representing multi-method behavioral contracts still requires a traditional interface with an implementing class; primitive functional interfaces (`IntFunction`, `ToIntFunction`, etc.) exist only for common primitive combinations, so uncommon combinations still incur autoboxing.

### Internal Working

- **Step-by-Step Explanation**: 1) `javac` scans an interface's methods, counting only truly abstract, non-`Object`-overriding methods; if there is exactly one, the interface qualifies as a valid lambda/method-reference target. 2) `@FunctionalInterface` is purely a compile-time assertion — if present, the compiler raises an error if the interface does *not* have exactly one abstract method, catching accidental additions of a second abstract method during maintenance. 3) When a lambda or method reference targets this interface, the previously-described `invokedynamic`/`LambdaMetafactory` machinery generates an implementation of that specific interface at the call site.
- **Memory Layout**: Not directly applicable beyond ordinary interface metadata in metaspace; standard `java.util.function` interfaces are loaded once by the bootstrap/platform class loader and shared across the entire application, with zero additional footprint from being "functional."
- **Diagrams**:

```
@FunctionalInterface
interface Validator<T> {
    boolean isValid(T value);        // exactly one abstract method -> SAM

    default Validator<T> and(Validator<T> other) {   // default methods allowed, don't count
        return v -> this.isValid(v) && other.isValid(v);
    }
}
```

- **JVM Behaviour**: No special runtime behavior distinguishes a functional interface from any other interface — `instanceof` checks, virtual dispatch (`invokeinterface`), and class loading proceed identically; the "functional" nature is a purely compile-time contract enforced by `javac` and, optionally, self-documented via `@FunctionalInterface`.

### Interview Questions

**Basic**
1. What qualifies an interface as a "functional interface"?
2. What does the `@FunctionalInterface` annotation actually do?

**Intermediate**
3. Name four core interfaces from `java.util.function` and describe their shapes (input/output arity).
4. Can a functional interface have `default` and `static` methods? Does that break the SAM rule?

**Advanced**
5. Why do `Object`'s methods (`equals`, `hashCode`, `toString`) not count against the single-abstract-method rule, even if redeclared abstractly in the functional interface?
6. Why does the JDK provide primitive-specialized functional interfaces like `IntPredicate` and `ToDoubleFunction<T>` instead of relying solely on generic `Predicate<Integer>`/`Function<T,Double>`?

**Scenario-based**
7. You need an interface representing "a function that takes two `int`s and returns an `int`, and may throw a checked `IOException`." None of `java.util.function`'s built-ins fit. What's the right approach?

### Detailed Answers

1. An interface qualifies as a functional interface if it declares exactly one abstract method that is not already implicitly provided by `Object` (methods like `equals(Object)`, `toString()`, `hashCode()` are excluded from the count even if redeclared). It may have any number of `default`, `static`, or `private` methods in addition.
2. `@FunctionalInterface` is a compiler-checked marker annotation: applying it causes `javac` to emit a compile error if the annotated interface does **not** satisfy the single-abstract-method rule (zero or more than one qualifying abstract method). It has no runtime effect whatsoever — it exists purely to catch accidental interface-contract violations early and to self-document intent for readers and tools.
3. `Function<T,R>` takes one argument of type `T` and returns `R` (method `apply`); `Predicate<T>` takes one argument and returns `boolean` (method `test`); `Supplier<T>` takes no arguments and returns `T` (method `get`); `Consumer<T>` takes one argument and returns `void` (method `accept`). `BiFunction<T,U,R>` extends this pattern to two input arguments.
4. Yes — `default` and `static` methods are fully permitted and do not affect the SAM rule at all, since the rule only counts **abstract** methods. This is precisely what allows rich standard interfaces like `Predicate` to offer combinators such as `and(Predicate)`, `or(Predicate)`, and `negate()` as `default` methods while still being usable as a plain lambda target for their single abstract `test` method.
5. The Java Language Specification explicitly excludes public methods of `Object` from the abstract-method count for functional interfaces, because every implementing class — including every lambda-generated implementation — automatically inherits concrete implementations of those methods from `Object` regardless of what the interface declares; redeclaring them abstractly in the interface is purely a documentation/type-refinement device (e.g., to add different Javadoc) and doesn't represent "real" missing behavior a lambda would need to supply.
6. Generic functional interfaces like `Predicate<Integer>` require autoboxing every `int` into an `Integer` object on each invocation, which allocates wrapper objects and adds GC pressure in hot loops (e.g., inside a stream pipeline processing millions of primitives). Primitive-specialized interfaces (`IntPredicate`, `ToDoubleFunction<T>`, `IntUnaryOperator`, etc.) operate directly on primitive `int`/`long`/`double` values without boxing, which is why `IntStream`/`LongStream`/`DoubleStream` exist as primitive-specialized stream types built around these interfaces for performance-sensitive numeric pipelines.
7. None of the standard `java.util.function` interfaces permit checked exceptions in their abstract method signatures (they'd have to be caught/wrapped inside the lambda body, since interfaces like `BiFunction` don't declare `throws`). The correct approach is to declare a small custom `@FunctionalInterface` — e.g., `interface ThrowingIntBinaryOperator { int apply(int a, int b) throws IOException; }` — tailored to the exact shape and checked-exception requirement, since `java.util.function.IntBinaryOperator` cannot express a checked `throws` clause.

### Code Examples

```java
import java.util.function.Predicate;

public class CompositePredicateDemo {
    public static void main(String[] args) {
        Predicate<String> isNotBlank = s -> s != null && !s.isBlank();
        Predicate<String> isShort = s -> s.length() <= 20;

        // Combinators (default methods) compose functional interfaces without new classes
        Predicate<String> isValidUsername = isNotBlank.and(isShort);

        System.out.println(isValidUsername.test("alice"));           // true
        System.out.println(isValidUsername.test(""));                // false
        System.out.println(isValidUsername.test("a".repeat(50)));    // false
    }
}
```

```java
import java.io.IOException;

// Custom functional interface: java.util.function has no built-in shape
// that both operates on two ints AND declares a checked exception.
@FunctionalInterface
interface ThrowingIntBinaryOperator {
    int apply(int a, int b) throws IOException;
}

public class CustomFunctionalInterfaceDemo {
    static int readAndCombine(ThrowingIntBinaryOperator op, int a, int b) throws IOException {
        return op.apply(a, b);
    }

    public static void main(String[] args) throws IOException {
        int result = readAndCombine((a, b) -> {
            if (a < 0 || b < 0) throw new IOException("negative operand");
            return a + b;
        }, 3, 4);
        System.out.println(result); // 7
    }
}
```

## Method References

### Theory

- **Core Concepts**: A method reference (`::` syntax) is shorthand for a lambda whose body does nothing but call an existing method or constructor. Java defines four kinds: static (`ClassName::staticMethod`), bound instance (`instance::instanceMethod`, referring to a specific object), unbound instance (`ClassName::instanceMethod`, where the first lambda parameter becomes the receiver), and constructor references (`ClassName::new`).
- **Internal Working**: Method references compile through the exact same `invokedynamic`/`LambdaMetafactory` pipeline as lambdas — `javac` treats `Foo::bar` as sugar for `(args) -> Foo.bar(args)` (adjusted per the four kinds), generating a `MethodHandle` to the target method rather than a synthetic lambda-body method (since the target method already exists, no new private method needs to be synthesized).
- **When to Use It**: Whenever a lambda's entire body is a single call to an existing method/constructor with matching parameters — improves readability by referencing well-named existing methods instead of re-describing behavior inline.
- **Advantages**: More concise and often more readable than an equivalent lambda; reuses (and reads as) an already-named, already-tested method; avoids introducing a redundant synthetic lambda body method since it points directly at the referenced method.
- **Limitations**: Only applicable when the lambda body is literally just a delegating call with compatible signature/arity — any additional logic (even a null check) forces falling back to a full lambda; can occasionally be less obvious to less experienced readers which of the four reference kinds is in play.

### Internal Working

- **Step-by-Step Explanation**: 1) `javac` determines which of the four method-reference kinds applies based on syntax and whether the left-hand side names a type or an expression/instance. 2) For an unbound instance reference (`String::toUpperCase` used as a `Function<String,String>`), the compiler arranges for the functional interface's first parameter to become the implicit receiver of the referenced instance method. 3) A `MethodHandle` is resolved for the target method (static, virtual, or constructor, via `invokestatic`/`invokevirtual`/`invokespecial`-equivalent handle kinds) and passed to `LambdaMetafactory.metafactory` as with lambdas, producing a generated implementation class at first call-site execution that simply forwards to that handle. 4) No synthetic lambda-body method is generated in the enclosing class for a method reference, unlike a full lambda expression, because the handle can point directly at the pre-existing target method.
- **Memory Layout**: Not directly applicable beyond ordinary `invokedynamic` call-site linkage; bound instance references (`instance::method`) capture the bound instance as a field of the generated implementation object, exactly like a lambda capturing `this` or a local variable.
- **Diagrams**:

```
Static:            Integer::parseInt        ==  s -> Integer.parseInt(s)
Bound instance:    order::getTotal          ==  () -> order.getTotal()
Unbound instance:  String::toUpperCase      ==  s -> s.toUpperCase()
Constructor:       ArrayList::new           ==  () -> new ArrayList<>()
```

- **JVM Behaviour**: Identical to lambdas — deferred, `invokedynamic`-based generation of a hidden implementation class on first execution, cached `CallSite` for subsequent calls, and eligibility for hidden-class unloading; the JIT can inline through the generated forwarding call exactly as it would for a directly-written lambda once the call site is warmed up and monomorphic.

### Interview Questions

**Basic**
1. What are the four kinds of method references in Java?
2. Rewrite the lambda `s -> s.length()` as a method reference.

**Intermediate**
3. What is the difference between a "bound" and an "unbound" instance method reference?
4. Can you use a method reference to refer to a constructor? Give an example with `Function`.

**Advanced**
5. Under the hood, is a method reference compiled differently from an equivalent lambda expression? What specifically differs?

**Scenario-based**
6. A teammate writes `list.forEach(x -> System.out.println(x));` and you suggest simplifying it. What would you suggest, and would it change behavior?

### Detailed Answers

1. The four kinds are: (1) reference to a static method (`ClassName::staticMethod`), (2) reference to an instance method of a particular, already-existing object (`instance::instanceMethod`), (3) reference to an instance method of an arbitrary object of a particular type, where the receiver becomes the functional interface's first parameter (`ClassName::instanceMethod`), and (4) reference to a constructor (`ClassName::new`).
2. `String::length` (an unbound instance method reference), used wherever a `Function<String,Integer>` or `ToIntFunction<String>` is expected — the implicit receiver `s` becomes the object on which `.length()` is invoked.
3. A bound instance reference captures a *specific, already-evaluated* object at the time the reference is created (e.g., `order::getTotal` where `order` is a concrete variable already in scope) — the object is fixed, like a closure capturing a variable. An unbound instance reference (`String::toUpperCase`) has no fixed receiver; instead, the first parameter supplied to the functional interface at call time *becomes* the receiver on which the method is invoked, meaning the object varies per invocation (e.g., in `stream.map(String::toUpperCase)`, each stream element is, in turn, the receiver).
4. Yes — `ClassName::new` refers to a constructor. For example, `Function<String, StringBuilder> factory = StringBuilder::new;` creates a `StringBuilder` from a `String` argument by matching the `StringBuilder(String)` constructor to the `Function`'s single-argument signature; `factory.apply("hello")` invokes `new StringBuilder("hello")`.
5. Yes, subtly: both ultimately use the `invokedynamic`/`LambdaMetafactory` pipeline and produce a dynamically-generated implementation class at the call site, but a method reference does **not** require the compiler to synthesize a new private lambda-body method in the enclosing class, since the `MethodHandle` passed to the metafactory can point directly at the already-existing target method (static, instance, or constructor). A lambda expression, by contrast, always needs its body compiled into a fresh synthetic method first, which the generated implementation class then delegates to via a handle.
6. The lambda `x -> System.out.println(x)` is exactly an unbound... actually here it's a bound reference to the specific `System.out` `PrintStream` instance's `println` method, so it can be simplified to `list.forEach(System.out::println);`. Behavior is identical — both print each element — but the method reference is more concise and, arguably, more directly communicates "call `println` for each element" rather than restating the call as an explicit lambda body.

### Code Examples

```java
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MethodReferenceDemo {
    public static void main(String[] args) {
        List<String> names = List.of("alice", "Bob", "CHARLIE");

        // Unbound instance method reference: receiver comes from each stream element
        List<String> upper = names.stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());
        System.out.println(upper); // [ALICE, BOB, CHARLIE]

        // Static method reference
        Function<String, Integer> parse = Integer::parseInt;
        System.out.println(parse.apply("42")); // 42

        // Constructor reference
        Function<String, StringBuilder> builderFactory = StringBuilder::new;
        System.out.println(builderFactory.apply("hi").reverse()); // ih
    }
}
```

```java
import java.util.List;

public class BoundReferenceDemo {
    record Order(String id, double total) {
        double getTotal() { return total; }
    }

    public static void main(String[] args) {
        Order order = new Order("ORD-1", 249.99);

        // Bound instance method reference: 'order' is fixed at reference-creation time
        java.util.function.Supplier<Double> totalSupplier = order::getTotal;
        System.out.println(totalSupplier.get()); // 249.99

        List<Order> orders = List.of(order, new Order("ORD-2", 50.0));
        orders.forEach(System.out::println); // bound reference to System.out's println
    }
}
```

## Closures

### Theory

- **Core Concepts**: A closure is a function value that "closes over" (captures) variables from its enclosing lexical scope so it can use them even after the enclosing scope has finished executing. Java's lambdas and anonymous inner classes are closures in this sense, but with a key restriction versus languages like JavaScript or Python: captured local variables must be effectively final — Java implements "capture by value of an immutable snapshot," not true mutable shared-variable closures.
- **Internal Working**: The compiler copies each captured effectively-final local variable's value into a field of the generated lambda/anonymous-class implementation instance at the moment the closure object is created; captured instance fields of the enclosing object are instead accessed indirectly by capturing a reference to `this`, so those fields *can* reflect later mutation (since they're read through the live object, not copied by value).
- **When to Use It**: Whenever behavior needs contextual data from its defining scope — event handler configuration, deferred/lazy computations, callback parameterization, building `Comparator`/`Predicate` instances with locally-scoped configuration values.
- **Advantages**: Enables powerful, concise parameterized behavior without manually threading extra parameters through every call; supports currying-like patterns and configuration-by-closure.
- **Limitations**: Cannot mutate a captured local variable directly (must resort to a mutable wrapper like an `AtomicInteger`, single-element array, or an instance field on an object); accidental capture of large objects (e.g., a whole enclosing entity) can create unintended reference retention, delaying garbage collection of the enclosing instance for as long as the closure lives.

### Internal Working

- **Step-by-Step Explanation**: 1) `javac` performs definite-assignment/effectively-final analysis over every local variable referenced inside a lambda or anonymous class body. 2) For each captured local variable, the compiler adds a corresponding field to the generated implementation class and a constructor parameter that copies the value in at creation time (for lambdas, this manifests as extra static/dynamic arguments passed to `LambdaMetafactory`, effectively bound into the returned `CallSite`'s target). 3) If the closure references `this` (an instance method/field of the enclosing object) or a `static` field, no by-value copy is needed for that access — the generated class instead holds (for `this`) an implicit reference to the enclosing instance, or (for `static` members) simply reads/writes the shared static field directly, meaning static-field or enclosing-instance-field mutations *are* visible through the closure, unlike captured locals.
- **Memory Layout**: The captured-value fields live on the heap as part of the lambda/anonymous-class instance; if `this` is captured, that reference keeps the entire enclosing object reachable for as long as any closure derived from it survives (a common, subtle memory-leak vector if such closures are stored long-term, e.g., registered as long-lived listeners).
- **Diagrams**:

```
class ReportBuilder {
    private double taxRate = 0.08;               // instance field: captured by reference via 'this'

    Function<Double, Double> buildTaxCalculator(double discount) {  // 'discount' is a local param
        return amount -> (amount - discount) * (1 + this.taxRate);
        //                          ^ copied by value at closure creation
        //                                          ^ read live through captured 'this'
    }
}
```

- **JVM Behaviour**: Captured-by-value fields are ordinary final-like fields on the generated instance, subject to normal JIT constant-propagation/inlining once the closure's shape is stable; capturing `this` produces a strong reference edge from the closure object to the enclosing instance that the GC must trace through normally — no special "weak capture" exists in vanilla Java (unlike some GUI frameworks' weak-listener patterns, which must be built manually on top).

### Interview Questions

**Basic**
1. What is a closure, and how do Java lambdas qualify as closures?
2. Why can't a lambda in Java reassign a captured local variable?

**Intermediate**
3. If a lambda captures an instance field of its enclosing object rather than a local variable, can it observe later mutations to that field? Why is this different from local-variable capture?
4. How would you work around Java's "effectively final" restriction if you genuinely need a closure to accumulate a running total across invocations?

**Advanced**
5. What memory-leak risk can arise from a closure capturing `this`, and in what kind of application is this most likely to bite?

**Scenario-based**
6. You register a lambda as a long-lived event listener in a long-running server application, and the lambda captures `this` from a short-lived request-handling object. What problem might occur, and how would you avoid it?

### Detailed Answers

1. A closure is a function-like value that retains access to variables from the lexical scope in which it was created, even after that scope has exited. Java lambdas (and anonymous inner classes) qualify because they can reference local variables, method parameters, and enclosing instance state from their defining context, packaging that context alongside the executable code in a single object.
2. Because the compiler implements capture as a one-time, by-value copy of the variable into a field of the generated closure object at creation time — if the original variable could be reassigned afterward, the copied field and the "live" variable would diverge, and Java has no shared-cell/box mechanism (as JavaScript does) to keep them in sync; requiring effective finality avoids this ambiguity entirely by construction.
3. Yes, it can observe later mutations. Capturing an instance field means the closure actually holds a reference to the enclosing `this` object and reads the field *through* that reference at invocation time (like any other field access), rather than copying the field's value at creation time. This is different from local-variable capture, where the value is copied once into the closure's own field and frozen at that point — subsequent changes to the original local variable (which, being effectively final, can't happen anyway) would never be visible even if they were allowed.
4. Wrap the mutable state in an object whose *reference* can be effectively final even though its *contents* are mutable — common choices are `java.util.concurrent.atomic.AtomicInteger`/`AtomicLong` (also providing thread-safety if needed), a single-element array (`int[] total = {0};`), or a small mutable holder class field. The closure then captures the (effectively final) reference to this container and mutates its contents, sidestepping the restriction on reassigning the captured variable itself.
5. If a closure captures `this`, it holds a strong reference to the entire enclosing object for as long as the closure itself is reachable. In a long-running application, if that closure is registered somewhere long-lived (a static listener list, a cache, a thread pool's queued task), the enclosing object — and everything it transitively references — cannot be garbage-collected even after its "logical" lifetime should have ended, causing a memory leak. This is especially likely in GUI applications (listeners registered on long-lived components) and server applications (callbacks registered against long-lived caches/executors) where a request-scoped or short-lived object accidentally becomes pinned in memory via a captured closure.
6. The event listener will keep the request-handling object (and its entire object graph — any large payloads, database connections, etc. it references) alive for as long as the listener remains registered, potentially indefinitely in a long-running server, causing a memory leak and possibly stale/incorrect behavior if the captured object represents per-request state that should have been discarded. The fix is to avoid capturing `this` (or the request object) directly in a long-lived closure — instead, extract only the specific small pieces of data actually needed into local effectively-final variables before creating the lambda, or explicitly unregister the listener when the request/scope ends, or use a weak reference wrapper if the framework supports it.

### Code Examples

```java
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.function.Consumer;

public class ClosureAccumulatorDemo {
    public static void main(String[] args) {
        // AtomicInteger's reference is effectively final; its VALUE is mutable -
        // this is how you "mutate state across closure invocations" in Java.
        AtomicInteger runningTotal = new AtomicInteger(0);

        Consumer<Integer> accumulate = amount -> runningTotal.addAndGet(amount);

        List.of(10, 20, 30).forEach(accumulate);
        System.out.println("Total: " + runningTotal.get()); // Total: 60
    }
}
```

```java
import java.util.function.Function;

public class ClosureOverInstanceStateDemo {
    private double taxRate;

    ClosureOverInstanceStateDemo(double taxRate) {
        this.taxRate = taxRate;
    }

    // Closure captures 'this' implicitly, so it reads the LIVE taxRate field, not a snapshot.
    Function<Double, Double> priceWithTax() {
        return amount -> amount * (1 + this.taxRate);
    }

    public static void main(String[] args) {
        ClosureOverInstanceStateDemo pricing = new ClosureOverInstanceStateDemo(0.08);
        Function<Double, Double> calculator = pricing.priceWithTax();

        System.out.println(calculator.apply(100.0)); // 108.0

        pricing.taxRate = 0.15; // mutate the captured instance field after closure creation
        System.out.println(calculator.apply(100.0)); // 115.0 - closure sees the updated field
    }
}
```

## Optional

### Theory

- **Core Concepts**: `java.util.Optional<T>` (Java 8+) is a container object that either holds a non-null value or represents its absence (`Optional.empty()`), designed as an explicit, type-visible alternative to returning `null` from methods. It is primarily intended as a **method return type** to signal "this may or may not have a result," not as a general-purpose replacement for `null` everywhere (fields, parameters, collection elements).
- **Internal Working**: `Optional<T>` is an ordinary immutable final class wrapping a single reference field, with methods like `map`, `filter`, `flatMap`, `orElse`, `orElseGet`, `orElseThrow`, and `ifPresent` implementing a small monadic-style API for chaining operations without explicit null checks. There is no special JVM support — it is plain library code (though the JIT can often eliminate `Optional` allocation entirely via escape analysis for tightly-scoped usage).
- **When to Use It**: As the return type of a method whose absence-of-result is a normal, expected outcome (e.g., `findById` in a repository) that callers should be forced to explicitly handle.
- **Advantages**: Makes "no result" part of the type signature (impossible to ignore silently the way a nullable return type is easy to forget to check), encourages a fluent, chainable style (`repository.findById(id).map(...).orElse(...)`), reduces `NullPointerException`s at call sites that use it correctly.
- **Limitations**: Not `Serializable` (by design), inappropriate as a field type (adds an extra allocation/indirection with no compile-time-enforced benefit since fields aren't forced through `Optional`'s API the way method returns are), inappropriate as a method parameter type (forces callers to wrap even non-optional arguments, and overload resolution/null-handling becomes awkward), and `Optional.of(null)` throws `NullPointerException` immediately (use `Optional.ofNullable` for a possibly-null source value).

### Internal Working

- **Step-by-Step Explanation**: 1) A method returns `Optional.ofNullable(result)` (or `Optional.of(result)` if `result` is statically known non-null, or `Optional.empty()`), wrapping the possibly-absent value. 2) The caller chains operations: `map(Function)` transforms the contained value only if present, returning a new `Optional`; `filter(Predicate)` turns a present-but-non-matching value into empty; `flatMap(Function<T, Optional<U>>)` avoids nested `Optional<Optional<U>>` by flattening one level. 3) Terminal operations extract the value: `orElse(T)` provides a default (always evaluated eagerly, even if unused — a common subtle performance pitfall), `orElseGet(Supplier<T>)` provides a lazily-computed default, `orElseThrow(...)` throws a custom exception if empty, and `get()` (or `orElseThrow()` with no arguments, Java 10+) throws `NoSuchElementException` if empty and should generally be avoided in favor of the more explicit alternatives.
- **Memory Layout**: `Optional<T>` instances are ordinary heap-allocated objects wrapping a single reference; for tightly-scoped local usage (created and consumed within the same method without escaping), the JIT's escape analysis can frequently scalar-replace the `Optional` wrapper entirely, avoiding actual heap allocation — but this optimization is not guaranteed and shouldn't be relied upon architecturally.
- **Diagrams**:

```
Optional<Customer> found = repository.findById(id);
        |
        v
   map(Customer::getEmail)         -- only applied if present
        |
        v
   filter(email -> !email.isBlank())
        |
        v
   orElseThrow(() -> new CustomerNotFoundException(id))

empty at any stage --> short-circuits remaining map/filter calls --> falls through to orElse*/orElseThrow
```

- **JVM Behaviour**: No special bytecode instructions exist for `Optional` — every operation compiles to ordinary virtual method calls (`invokevirtual`) on a plain final class; performance-sensitive code paths that create and immediately consume `Optional` within a single method are good candidates for JIT scalar replacement (avoiding actual allocation) once the method is hot enough to be compiled by C2.

### Interview Questions

**Basic**
1. What problem does `Optional<T>` solve, and where is it best used?
2. What is the difference between `Optional.of(value)` and `Optional.ofNullable(value)`?

**Intermediate**
3. What is the difference between `orElse(defaultValue)` and `orElseGet(supplier)`, and why does it matter for performance/correctness?
4. Why is it generally considered bad practice to use `Optional` as a field type or method parameter type?

**Advanced**
5. What is the difference between `map` and `flatMap` on `Optional`, and when would using `map` instead of `flatMap` cause a compile error or an awkward nested type?

**Scenario-based**
6. A code reviewer flags `if (optionalUser.isPresent()) { doSomething(optionalUser.get()); }` as an anti-pattern. What would they suggest instead, and why?

### Detailed Answers

1. `Optional<T>` solves the ambiguity of a `null` return value — callers of a method returning `Optional<T>` are forced (by the type system, since `Optional` has no `T`-shaped methods directly) to explicitly consider the "absent" case via `map`/`orElse`/`orElseThrow`/etc., rather than silently forgetting a null check and risking a `NullPointerException` deep in unrelated code. It is best used as a method's **return type** when "no result" is a legitimate, expected outcome.
2. `Optional.of(value)` throws `NullPointerException` immediately if `value` is `null` — use it when you can statically guarantee non-null (fail-fast on a programming error). `Optional.ofNullable(value)` safely handles a possibly-null `value`, producing `Optional.empty()` if it is `null` and a present `Optional` otherwise — use it when wrapping a value from a source that may legitimately be null (e.g., a `Map.get(key)` result).
3. `orElse(defaultValue)` **always evaluates** its argument eagerly, even when the `Optional` is present and the default won't be used — this is a correctness/performance trap if the argument is an expensive computation or has side effects (e.g., `orElse(expensiveDatabaseCall())` always runs the database call). `orElseGet(supplier)` only invokes the `Supplier` lazily, if and only if the `Optional` is actually empty, making it the correct choice whenever the default value is non-trivial to compute or has side effects.
4. As a field type, `Optional` adds an extra allocation and a layer of indirection without gaining any of its main benefit — a field's nullability isn't enforced by the type system the way a method return value's handling is, and `Optional` fields aren't `Serializable`, complicating classes that need serialization. As a parameter type, it forces every caller to wrap arguments in `Optional.of(...)`/`ofNullable(...)` even when the "optionality" could simply be expressed via method overloading or a sensible default, and it makes null-argument handling inconsistent (should a `null` `Optional` parameter itself be tolerated?) — overloaded methods or well-named "withX"-style builder methods are the idiomatic alternative for optional parameters.
5. `map(Function<T,R>)` applies a function that returns a plain `R`; if that function itself would naturally return an `Optional<R>` (e.g., another method that itself returns `Optional`), using `map` produces an awkward `Optional<Optional<R>>`, which is difficult to work with (you'd need a further `map`/`get` just to unwrap the inner `Optional`). `flatMap(Function<T, Optional<R>>)` is designed exactly for this case: it expects the mapping function to already return an `Optional<R>` and flattens the result into a single `Optional<R>`, avoiding nested wrapping — analogous to `flatMap` on `Stream` avoiding nested streams.
6. The reviewer would suggest replacing the `isPresent()`/`get()` pattern with `optionalUser.ifPresent(user -> doSomething(user));` (or, if a value needs to be returned/transformed, `map`/`orElse`/`orElseThrow` chains). The `isPresent()`+`get()` combination defeats much of `Optional`'s purpose — it reintroduces an imperative null-check style, is more verbose, and is more error-prone (nothing stops a future edit from calling `.get()` without the preceding `isPresent()` check, throwing `NoSuchElementException`) — whereas `ifPresent`/`map`/`orElseThrow` encode the presence-check and the action atomically, making the incorrect "get without checking" mistake impossible by construction.

### Code Examples

```java
import java.util.Map;
import java.util.Optional;

public class CustomerLookupService {
    private final Map<String, Customer> customersById;

    CustomerLookupService(Map<String, Customer> customersById) {
        this.customersById = customersById;
    }

    // Optional as a RETURN TYPE: forces callers to handle the "not found" case explicitly.
    public Optional<Customer> findById(String id) {
        return Optional.ofNullable(customersById.get(id));
    }

    public String resolvePrimaryEmail(String id) {
        return findById(id)
                .map(Customer::email)                       // only runs if present
                .filter(email -> !email.isBlank())           // becomes empty if blank
                .orElseThrow(() ->
                        new IllegalStateException("No usable email for customer " + id));
    }

    record Customer(String id, String email) {}
}
```

```java
import java.util.Optional;

public class OptionalPitfallsDemo {
    static String expensiveFallback() {
        System.out.println("Computing expensive fallback...");
        return "fallback-value";
    }

    public static void main(String[] args) {
        Optional<String> present = Optional.of("actual-value");

        // BAD: expensiveFallback() runs even though 'present' already has a value
        System.out.println(present.orElse(expensiveFallback()));

        // GOOD: orElseGet only invokes the supplier if the Optional is actually empty
        System.out.println(present.orElseGet(OptionalPitfallsDemo::expensiveFallback));
    }
}
```
