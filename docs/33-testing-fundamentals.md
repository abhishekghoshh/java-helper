# 33. Testing Fundamentals *(new)*

## Unit Testing Concepts

### Theory

- **Core Concepts**: Unit testing is the practice of verifying the smallest testable pieces of an application — typically a single method or class — in isolation from its collaborators, database, network, or filesystem. In the Java ecosystem this is dominated by JUnit (4 and 5/Jupiter) and TestNG, structured around the Arrange-Act-Assert (AAA) pattern: set up inputs/state, invoke the unit under test, then assert on the outcome.
- **Internal Working**: A test runner (e.g., JUnit Platform) discovers test classes/methods via annotations (`@Test`) and reflection, instantiates the test class (a fresh instance per test method by default in JUnit 5), executes lifecycle callbacks (`@BeforeEach`/`@AfterEach`), invokes the test method, and records pass/fail/error based on whether an assertion exception or unexpected exception propagated.
- **When to Use It**: For every unit of business logic with non-trivial behavior — validation rules, calculations, branching logic, edge cases — ideally written alongside or before (TDD) the production code, and run continuously in CI on every commit.
- **Advantages**: Fast feedback (milliseconds per test), pinpoints exactly which unit broke, enables safe refactoring (a comprehensive suite acts as a regression safety net), documents intended behavior through executable examples.
- **Limitations**: Unit tests alone don't catch integration issues (wrong SQL, misconfigured wiring, serialization mismatches); over-mocking can make tests brittle and coupled to implementation details rather than behavior; a green suite gives false confidence if coverage is shallow or assertions are weak.

### Internal Working

- **Step-by-Step Explanation**: 1) Build tool (Maven/Gradle) compiles test sources and places JUnit Platform + engine JARs on the test classpath. 2) The JUnit Platform Launcher discovers test classes via classpath/module scanning, filtering for `@Test`-annotated methods (or meeting other engine-specific discovery predicates). 3) For each test class, a new instance is created per test method (JUnit 5 default `PER_METHOD` lifecycle), ensuring test isolation — no shared mutable state leaks between tests unless explicitly requested via `@TestInstance(PER_CLASS)`. 4) Lifecycle callbacks run in order: static `@BeforeAll` once per class, `@BeforeEach` before every test, the test method itself, `@AfterEach`, and finally static `@AfterAll`. 5) Assertion failures throw `AssertionError` (or JUnit's `AssertionFailedError`), which the runner catches and reports as a failed (not errored) test; any other uncaught exception is reported as an error.
- **Memory Layout**: Not directly applicable in a deep JVM-internals sense, but practically relevant: each test method typically gets a fresh object graph on the heap (test fixture + unit under test + any stubs), which is garbage-collected once the test class/instance is no longer referenced by the runner, keeping test suites memory-bounded even with thousands of tests.
- **Diagrams**:

```
Maven/Gradle -> compiles test sources -> JUnit Platform Launcher
                                              |
                                     discovers @Test methods
                                              |
                          for each test class: new instance per method
                                              |
                     @BeforeEach -> test method (Arrange/Act/Assert) -> @AfterEach
                                              |
                                  pass / failed (AssertionError) / error (other exception)
```

- **JVM Behaviour**: Each test runs as ordinary bytecode on the same JVM instance (unless the build tool forks a new JVM per test class, which Maven Surefire/Gradle can do for isolation); JIT warms up across the suite the same way as any long-running process, so early tests in a large suite may run slower than later ones due to interpreter-mode execution before JIT compilation kicks in.

### Interview Questions

**Basic**
1. What is the Arrange-Act-Assert pattern, and why is it useful in unit tests?
2. What is the difference between a unit test and an integration test?

**Intermediate**
3. Why does JUnit 5 create a new test instance per test method by default, and when would you override this?
4. What is the difference between a test "failure" and a test "error" in JUnit's reporting?

**Advanced**
5. How does JUnit 5's extension model (`@ExtendWith`) differ architecturally from JUnit 4's `@RunWith` runners?
6. What is test isolation, and what specific JVM/runner mechanisms help enforce it?

**Scenario-based**
7. A previously-passing test suite starts intermittently failing after tests were reordered (e.g., via a new build tool version). What does this strongly suggest about the test suite, and how would you fix it?

### Detailed Answers

1. Arrange-Act-Assert structures a test into three clearly separated phases: arranging inputs and preconditions, invoking the single action under test, and asserting the expected outcome. It keeps tests readable and focused on one behavior, making failures easy to diagnose because each test exercises exactly one logical scenario.
2. A unit test isolates a single class/method from its real collaborators (using stubs/mocks for dependencies) and runs fast, in-memory, with no I/O. An integration test exercises multiple components together — real database, real HTTP calls, real message queue — to verify that the wiring and contracts between components actually work, at the cost of being slower and more environment-dependent.
3. Creating a fresh instance per test method guarantees that instance fields (test fixtures) cannot leak state between tests, eliminating a common source of order-dependent flaky tests. You'd override this with `@TestInstance(Lifecycle.PER_CLASS)` when you need non-static `@BeforeAll`/`@AfterAll` methods, or want to share an expensive-to-construct fixture across tests in the same class (while remaining careful to reset any mutable state manually).
4. A "failure" means an assertion inside the test explicitly did not hold (an `AssertionError`/`AssertionFailedError` was thrown by `assertEquals`, `assertTrue`, etc.) — the test ran to completion but the expected condition was false. An "error" means some other, unexpected exception (e.g., `NullPointerException`, `IOException`) propagated out of the test or its lifecycle methods, indicating the test itself broke rather than the assertion logic failing.
5. JUnit 4's `@RunWith` binds an entire test class to exactly one `Runner` implementation, making it hard to compose multiple pieces of cross-cutting behavior (e.g., Spring context + parameterized tests) without runner subclassing gymnastics. JUnit 5's extension model allows multiple, independently-authored `Extension` implementations (each implementing focused interfaces like `BeforeEachCallback`, `ParameterResolver`, `TestInstancePostProcessor`) to be composed via repeatable `@ExtendWith` annotations on a single test class, enabling much more modular and combinable test infrastructure.
6. Test isolation means one test's execution and outcome cannot be influenced by another test's side effects or execution order. JUnit enforces this via per-method test instantiation (fresh fields each time), predictable (though technically unspecified unless `@TestMethodOrder` is used) execution, and encouraging stateless static resources; build tools reinforce it further by optionally forking a new JVM per test class to eliminate static-state leakage or classloader pollution between classes.
7. Intermittent failures after reordering strongly suggest hidden inter-test dependencies — e.g., a static field, a shared singleton, or a database record left behind by a prior test that a later test implicitly relies on. The fix is to audit for shared mutable state (static fields, singletons, files, external system state) and ensure every test independently arranges its own preconditions in `@BeforeEach`/setup code and cleans up afterward, rather than relying on execution order.

### Code Examples

```java
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class ShoppingCartTest {

    private ShoppingCart cart;

    @BeforeEach
    void setUp() {
        // Arrange: fresh cart for every test method, guaranteeing isolation
        cart = new ShoppingCart();
    }

    @Test
    void addingItemIncreasesTotal() {
        // Act
        cart.addItem("SKU-1", 2, 19.99);

        // Assert
        assertEquals(39.98, cart.getTotal(), 0.001, "Total should reflect quantity * unit price");
    }

    @Test
    void removingNonExistentItemThrows() {
        assertThrows(NoSuchElementException.class, () -> cart.removeItem("SKU-404"));
    }
}
```

## Assertions (`assert` keyword)

### Theory

- **Core Concepts**: Java has two distinct assertion mechanisms that are easy to conflate: (1) the built-in `assert` language keyword (Java 1.4+, JLS section 14.10), used for internal self-checks/invariants (`assert x > 0 : "x must be positive";`), which is disabled by default at runtime; and (2) test-framework assertion methods like JUnit's `Assertions.assertEquals(...)`/`assertTrue(...)`, which always execute and are the primary way test outcomes are verified.
- **Internal Working**: The `assert` keyword compiles to a bytecode check guarded by a synthetic static field (`$assertionsDisabled`, populated from `Class.desiredAssertionStatus()`) that is `false` unless assertions are explicitly enabled via the `-ea`/`-enableassertions` JVM flag; JUnit assertions are ordinary static method calls that throw `AssertionFailedError` on failure, with no JVM flag dependency.
- **When to Use It**: Use the `assert` keyword for internal invariants/preconditions in library or application code where you control the runtime flags (rare in production, common in defensive internal checks during development); use JUnit/AssertJ/Hamcrest assertion methods for all automated test verification, since they always run regardless of JVM flags.
- **Advantages**: The `assert` keyword has zero runtime cost when disabled (the default), documents invariants inline; test-framework assertions provide rich failure messages, fluent matchers (AssertJ), and are guaranteed to execute in CI regardless of flags.
- **Limitations**: `assert` is silently a no-op unless `-ea` is passed, making it unsuitable for validating untrusted input (use explicit `if`/exception checks instead) or production business rules; over-reliance on generic `assertTrue(condition)` in tests (instead of a specific matcher like `assertEquals`) produces poor failure messages that don't show expected-vs-actual values.

### Internal Working

- **Step-by-Step Explanation**: 1) `javac` compiles `assert condition : message;` into bytecode roughly equivalent to `if ($assertionsDisabled == false && !condition) throw new AssertionError(message);`. 2) `$assertionsDisabled` is a synthetic `static final` field initialized once per class, at class-initialization time, via `!ClassName.class.desiredAssertionStatus()`. 3) At JVM startup, `-ea`/`-enableassertions` (optionally scoped to specific packages/classes) or `-da`/`-disableassertions` set the assertion status consulted by `desiredAssertionStatus()`; without any flag, assertions default to disabled system-wide. 4) In contrast, JUnit's `assertEquals(expected, actual)` is a plain static method: it directly compares the values and throws `org.opentest4j.AssertionFailedError` (which JUnit's launcher specifically recognizes as a "failure" rather than an "error") if they differ — no flag or special JVM configuration is involved.
- **Memory Layout**: Not directly applicable — the only structure involved is the single synthetic boolean field per class for the `assert` keyword; JUnit assertion failures allocate a normal exception object on the heap like any other thrown exception, subject to standard GC.
- **Diagrams**:

```
assert age >= 0 : "age cannot be negative";
        |
        v  (javac)
if (!ClassName.$assertionsDisabled && !(age >= 0)) {
    throw new AssertionError("age cannot be negative");
}

JVM flags: (default) assertions disabled -> check skipped entirely, zero cost
           -ea                            -> check executed every time
```

- **JVM Behaviour**: Because the guard is a `static final` field, JIT can constant-fold and eliminate the entire `assert` block at compile-optimization time when assertions are disabled (the common production default), meaning `assert` statements impose no measurable runtime overhead unless explicitly enabled — the opposite of test-framework assertions, which always execute their comparison and are expected to run in every test invocation.

### Interview Questions

**Basic**
1. Why does `assert x > 0;` do nothing at all by default when you run a Java program?
2. What is the difference between the `assert` keyword and JUnit's `assertEquals`/`assertTrue` methods?

**Intermediate**
3. How do you enable Java's built-in assertions, and can you scope this to specific packages?
4. Why shouldn't you use the `assert` keyword to validate method arguments from external/untrusted callers?

**Advanced**
5. How does the compiler implement `assert` under the hood such that it has zero cost when disabled?

**Scenario-based**
6. A developer writes `assert userInput != null;` inside a public API method to guard against null input, then is confused when a `NullPointerException` occurs later instead of an `AssertionError` at that line in production. Explain what happened.

### Detailed Answers

1. Java assertions are disabled by default at the JVM level; the `assert` statement compiles to a conditional check guarded by a synthetic flag that defaults to `false` (meaning "assertions disabled"), so the check is skipped entirely unless the JVM is launched with `-ea`. This default exists so that internal sanity checks don't impose runtime cost or unexpected `AssertionError`s in production.
2. The `assert` keyword is a language-level construct that is conditionally compiled/executed based on a JVM flag, intended for internal self-checks during development/testing, and is a no-op by default. JUnit's assertion methods are ordinary static method calls in a test-framework library that unconditionally execute every time and are the mechanism by which automated tests actually verify behavior — they have no dependency on JVM flags.
3. Assertions are enabled with the `-ea` (or `-enableassertions`) JVM flag when launching `java`. It can be scoped: `-ea:com.acme.billing...` enables assertions only for that package and its sub-packages, `-ea:com.acme.billing.Invoice` enables it only for a specific class, and combining with `-da:some.package...` selectively disables a subset while `-ea` is broadly enabled.
4. Because assertions are disabled by default in production, using `assert` to validate untrusted/external input means the validation silently vanishes unless `-ea` happens to be set — leaving no protection against invalid input in the deployed environment. Untrusted input should always be validated with explicit `if` checks that throw a real exception (e.g., `IllegalArgumentException`), which execute unconditionally regardless of JVM flags.
5. The compiler introduces a synthetic `static final boolean $assertionsDisabled` field per class, initialized once at class-initialization time from `Class.desiredAssertionStatus()`. Every `assert` statement is rewritten to check `!$assertionsDisabled && !condition` before throwing. Because the field is `static final` and its value is fixed at class-init time, the JIT compiler can treat the whole guarded block as dead code and eliminate it entirely when the field is `true` for "disabled" (default), yielding zero runtime overhead in the common case.
6. Because assertions are off by default, the `assert userInput != null;` line is entirely skipped at runtime in production (no `-ea` flag set), so it provides no actual protection. Execution proceeds past the assertion with `userInput` still `null`, and the `NullPointerException` occurs later, at whatever line first dereferences the null value — illustrating exactly why `assert` must never be relied upon for input validation in code paths that need to run correctly without special JVM flags.

### Code Examples

```java
public class DiscountCalculator {
    // Internal invariant check: only meaningful in dev/test runs with -ea enabled.
    // Never a substitute for real input validation on public APIs.
    double applyDiscount(double price, double rate) {
        assert rate >= 0.0 && rate <= 1.0 : "rate must be between 0 and 1, was " + rate;
        return price * (1 - rate);
    }

    // Real validation for untrusted/public input: always executes, regardless of JVM flags.
    public double applyValidatedDiscount(double price, double rate) {
        if (rate < 0.0 || rate > 1.0) {
            throw new IllegalArgumentException("rate must be between 0 and 1, was " + rate);
        }
        return applyDiscount(price, rate);
    }
}
```

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DiscountCalculatorTest {
    private final DiscountCalculator calculator = new DiscountCalculator();

    @Test
    void appliesDiscountCorrectly() {
        // JUnit assertions always run - no -ea flag needed, unlike the `assert` keyword
        assertEquals(80.0, calculator.applyValidatedDiscount(100.0, 0.2), 0.0001);
    }

    @Test
    void rejectsOutOfRangeRate() {
        assertThrows(IllegalArgumentException.class,
                () -> calculator.applyValidatedDiscount(100.0, 1.5));
    }
}
```

## Mocking Concepts

### Theory

- **Core Concepts**: Mocking replaces a real collaborator (a dependency of the class under test — a repository, an HTTP client, a clock) with a controllable test double so the unit test can exercise the class under test in isolation, without invoking real I/O or complex logic in the dependency. Common flavors: a **stub** returns canned answers; a **mock** additionally records interactions so the test can verify calls were made (`verify(mock).save(order)`); a **spy** wraps a real object, delegating by default but allowing selective overrides; a **fake** is a lightweight working implementation (e.g., an in-memory `Map`-backed repository).
- **Internal Working**: Frameworks like Mockito generate a dynamic subclass (or, for interfaces, a dynamic proxy) of the collaborator's type at runtime using bytecode generation (historically cglib/Objenesis, now the Byte Buddy library), overriding every method to record invocations and return pre-programmed (`when(...).thenReturn(...)`) or default values, without requiring a real constructor call (via Objenesis instantiation strategies that bypass constructors).
- **When to Use It**: When the real collaborator is slow, non-deterministic (network, time, randomness), expensive to set up, or when you specifically want to verify that the unit under test *calls* the collaborator correctly (interaction-based testing) rather than just checking end state.
- **Advantages**: Enables true unit isolation and fast, deterministic tests; lets you simulate hard-to-trigger conditions (network timeout, database exception) trivially; verifies collaboration contracts (e.g., "was `sendEmail` called exactly once with the right recipient").
- **Limitations**: Over-mocking couples tests to implementation details (mocking every single collaborator call can make tests break on harmless refactors even when behavior is unchanged); mocks can drift from the real collaborator's actual contract (a mock might return something the real implementation never would), giving false confidence — mitigated by combining unit tests with a smaller number of real integration tests.

### Internal Working

- **Step-by-Step Explanation**: 1) At test setup, `Mockito.mock(SomeService.class)` (or `@Mock` + `MockitoExtension`) asks Mockito to create a proxy type. 2) For a class target, Byte Bddy generates a new subclass at runtime overriding every non-final method; for an interface, a JDK dynamic proxy or Byte Buddy-generated implementer is used. 3) Objenesis instantiates this generated type **without calling any constructor** (bypassing potential side effects or required constructor arguments), producing a bare, mock-tagged instance. 4) Every overridden method routes into Mockito's `MockHandler`, which consults a registered `InvocationContainer` of stubbed answers (`when(mock.foo()).thenReturn(x)`) and records every call for later `verify(mock).foo()` assertions. 5) If no stub matches an invoked method, Mockito returns a type-appropriate default ("smart null": `0`/`false` for primitives, `null` for objects, empty collections for collection return types).
- **Memory Layout**: Not directly applicable to heap/stack specifics beyond ordinary object allocation, but notably: the dynamically generated proxy *class* is itself loaded into metaspace at runtime (via a class loader Mockito manages), which is why heavy mocking in large test suites can measurably increase metaspace usage and test startup time, particularly when mocking many distinct concrete classes (interfaces are cheaper — no subclass generation is required for JDK proxies of pure interfaces in some configurations).
- **Diagrams**:

```
Test code:
  PaymentGateway mockGateway = mock(PaymentGateway.class);
  when(mockGateway.charge(order)).thenReturn(Result.SUCCESS);

Runtime:
  mock(PaymentGateway.class)
        |
        v
  Byte Buddy generates PaymentGateway$MockitoMock$123 (subclass/proxy)
        |
  Objenesis instantiates it WITHOUT calling any constructor
        |
  charge(order) invocation --> MockHandler --> matches stub --> returns Result.SUCCESS
                                            \-> no stub match --> returns null/default + records call
```

- **JVM Behaviour**: Mock classes are loaded and defined via a dedicated class loader at test runtime, participate in normal JIT compilation like any other class, and are garbage-collected along with the test class instance once the test method/class completes; each distinct mocked type triggers its own one-time class-generation cost (cached per type within a test run), which is why creating thousands of mocks of many different types can noticeably slow down a very large test suite versus reusing fewer mock types across tests.

### Interview Questions

**Basic**
1. What is the difference between a mock, a stub, and a spy?
2. Why would you mock a collaborator instead of using the real implementation in a unit test?

**Intermediate**
3. How does Mockito create a mock instance of a concrete class without calling its constructor?
4. What is the difference between `verify(mock).method(x)` (behavior verification) and asserting on a return value (state verification)?

**Advanced**
5. What are the risks of over-mocking, and how can it lead to tests that pass despite the code being broken (or fail despite the code being correct)?
6. Why can't Mockito (by default) mock `final` classes or `final`/`static`/`private` methods, and how do modern versions work around this?

**Scenario-based**
7. A test mocks a `Clock`/date-time collaborator to simulate "today," but a colleague argues this test doesn't actually verify real behavior. Under what circumstances is mocking time appropriate versus a smell?

### Detailed Answers

1. A stub is a test double that returns pre-programmed canned answers to calls, with no expectation of verifying interactions. A mock does the same but is additionally used for **behavior verification** — the test asserts specific method calls happened with specific arguments (`verify(mock).save(order)`). A spy wraps a real object instance, delegating to the real methods by default while allowing the test to selectively override specific method behaviors — useful when you want mostly-real behavior with a few controlled exceptions.
2. The real collaborator might be slow (network/database calls), non-deterministic (current time, random IDs, external service availability), hard to trigger into specific states (simulating a downstream service outage), or simply out of scope for the unit being tested — mocking keeps the test fast, deterministic, and focused solely on the logic of the unit under test.
3. Mockito uses Objenesis, a library specifically designed to instantiate objects bypassing their constructors (using JVM-internal mechanisms like `sun.reflect.ReflectionFactory` or serialization-based instantiation strategies), combined with a Byte Buddy-generated subclass that overrides all overridable methods — this avoids needing to know/supply valid constructor arguments and avoids executing any real constructor logic (which might have side effects or requirements the test doesn't want to satisfy).
4. Behavior verification (`verify(mock).method(x)`) checks that a specific interaction with a collaborator occurred — appropriate when the point of the unit is to trigger a side effect (e.g., "sends an email," "saves to the repository"). State verification checks the observable output/return value of the unit under test — appropriate when the point is a computed result. Overusing behavior verification for things that are really about output state makes tests brittle, coupling them to *how* a result is produced rather than *what* the result is.
5. Over-mocking every single collaborator (including simple value objects or trivial helpers) creates tests that mirror the production code's exact call sequence rather than its observable behavior; a refactor that changes *how* a method internally collaborates with others (without changing overall behavior) then breaks many tests even though nothing user-visible changed. Conversely, tests can pass despite broken code if the mock's stubbed behavior no longer matches what the real collaborator would actually do (a "mock/reality divergence"), since the mock happily returns whatever was configured regardless of whether that's realistic — mitigated by keeping a baseline of real integration/contract tests alongside heavily-mocked unit tests.
6. Mocking works by generating a subclass that overrides methods; a `final` class cannot be subclassed at all, and `final`/`static`/`private` methods cannot be overridden by any subclass, so classic proxy-based mocking has no hook point to intercept those calls. Modern Mockito (2.x+, with the `mockito-inline` module, now default since Mockito 5) works around this using the Instrumentation API (`java.lang.instrument`) to perform bytecode manipulation of the already-loaded class directly (redefining/retransforming its methods at the class level) rather than relying purely on subclass generation, enabling mocking of final classes and static methods.
7. Mocking time (via an injectable `Clock`, rather than calling `LocalDate.now()`/`Instant.now()` directly inside business logic) is a best practice, not a smell, when the goal is to deterministically test date/time-dependent branching logic (e.g., "is this invoice overdue as of a given date"). It becomes a smell only if the mock is used to fake away logic that should genuinely be verified against real behavior — e.g., mocking away the actual date arithmetic itself rather than just the "what is now" input — since that would test nothing about whether the arithmetic is correct.

### Code Examples

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private PaymentGateway paymentGateway; // real gateway would hit a real payment provider

    @Test
    void chargesCustomerAndMarksOrderPaid() {
        // Arrange: stub the collaborator's behavior deterministically
        Order order = new Order("ORD-1", 49.99);
        when(paymentGateway.charge(order)).thenReturn(PaymentResult.SUCCESS);

        OrderService service = new OrderService(paymentGateway);

        // Act
        service.checkout(order);

        // Assert: state verification (observable output)
        assertTrue(order.isPaid());

        // Assert: behavior verification (the collaborator was actually invoked correctly)
        verify(paymentGateway, times(1)).charge(order);
    }
}
```

```java
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

// Injecting Clock instead of calling Instant.now() directly makes time-dependent
// logic deterministically testable without mocking frameworks at all.
public class InvoiceOverdueChecker {
    private final Clock clock;

    public InvoiceOverdueChecker(Clock clock) {
        this.clock = clock;
    }

    public boolean isOverdue(Instant dueDate) {
        return clock.instant().isAfter(dueDate);
    }
}

// In a test: Clock.fixed(...) acts as a lightweight "fake" - no mocking framework needed.
class InvoiceOverdueCheckerTest {
    void test() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-01-15T00:00:00Z"), ZoneOffset.UTC);
        InvoiceOverdueChecker checker = new InvoiceOverdueChecker(fixedClock);
        assert checker.isOverdue(Instant.parse("2026-01-01T00:00:00Z"));
    }
}
```

## Additional Resources

### JUnit

> Watch this video [JUnit 5 Tutorial - Crash Course](https://www.youtube.com/watch?v=6uSnF6IuWIw)
>
> [github](https://github.com/marcobehlerjetbrains/junit5-tutorial)

> Official documentation for JUnit 5 [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
> [AssertJ - fluent assertions java library](https://assertj.github.io/doc/)
> [Jupiter / JUnit 5](https://java.testcontainers.org/test_framework_integration/junit_5/)
> [awaitility/awaitility](https://github.com/awaitility/awaitility)

For JUnit use this dependency strictly

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.junit</groupId>
            <artifactId>junit-bom</artifactId>
            <version>5.12.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
<dependencies>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
<build>
    <plugins>
        <plugin>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.5.2</version>
        </plugin>
        <plugin>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.10.1</version>
        </plugin>
    </plugins>
</build>
```

Maven Surefire Plugin + Compiler plugin, see [Maven surefire plugin](https://maven.apache.org/surefire/maven-surefire-plugin/usage.html):

```xml
<build>
   <pluginManagement>
       <plugins>
           <plugin>
               <artifactId>maven-surefire-plugin</artifactId>
               <version>3.0.0-M6</version>
           </plugin>
           <plugin>
               <artifactId>maven-compiler-plugin</artifactId>
               <version>3.10.1</version>
           </plugin>
       </plugins>
   </pluginManagement>
</build>
```

[AssertJ - fluent assertions java library](https://assertj.github.io/doc/)

JSON Unit dependencies:

```xml
<dependencies>
   <dependency>
       <groupId>net.javacrumbs.json-unit</groupId>
       <artifactId>json-unit-assertj</artifactId>
       <version>2.33.0</version>
       <scope>test</scope>
   </dependency>

   <dependency>
       <groupId>com.fasterxml.jackson.core</groupId>
       <artifactId>jackson-databind</artifactId>
       <version>2.13.2.2</version>
   </dependency>
   <dependency>
       <groupId>com.fasterxml.jackson.datatype</groupId>
       <artifactId>jackson-datatype-jsr310</artifactId>
       <version>2.13.2</version>
   </dependency>
</dependencies>
```

XML Unit dependencies:

```xml
<dependencies>
   <dependency>
       <groupId>org.xmlunit</groupId>  <!-- needs proper bytebuddy-->
       <artifactId>xmlunit-assertj</artifactId>
       <version>2.9.0</version>
       <scope>test</scope>
   </dependency>
</dependencies>
```

XML Unit dependency management:

```xml
<dependencyManagement>
   <dependencies>
       <dependency>
           <groupId>net.bytebuddy</groupId>
           <artifactId>byte-buddy</artifactId>
           <version>1.12.10</version>
       </dependency>

       <dependency>
           <groupId>net.bytebuddy</groupId>
           <artifactId>byte-buddy-agent</artifactId>
           <version>1.12.10</version>
           <scope>test</scope>
       </dependency>
   </dependencies>
</dependencyManagement>
```

Resources.java reference: https://raw.githubusercontent.com/marcobehlerjetbrains/junit5-tutorial/main/src/test/java/com/jetbrains/util/Resources.java

