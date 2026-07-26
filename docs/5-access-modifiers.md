# 5. Access Modifiers

## `private`

**Theory**

- **Core Concepts**: `private` is the most restrictive access modifier in Java. A member (field, method, constructor, or nested class) declared `private` is visible only within the physical source file (top-level class body, including all nested/inner classes) where it is declared — not to subclasses, not to other classes in the same package, and not to any code outside that file. It is the cornerstone of *encapsulation*: the outside world interacts with an object only through its public/protected API, while internal representation stays free to change.
- **Internal Working**: Enforced entirely at compile time by the Java compiler and re-verified at class-load time by the JVM's bytecode verifier and `Reflection`/`invokedynamic` access-checking machinery; there is no separate runtime access-control mechanism beyond that (reflection can bypass it via `setAccessible(true)` unless a `SecurityManager`/module restriction blocks it).
- **When to Use It**: Default choice for all fields (favor getters/setters or immutability over public fields), and for helper methods/implementation details that must not become part of a class's public contract.
- **Advantages**: Maximizes encapsulation; allows internal refactoring without breaking callers; prevents invariant-breaking external mutation; reduces the API surface that must be kept backward compatible.
- **Limitations**: Not visible to subclasses (unlike `protected`), so it cannot be used for extension points; before Java 11, nested classes accessing each other's `private` members required the compiler to generate synthetic bridge/accessor methods since the classfile format itself only supports package-level `private` access checks at the JVM level.

**Internal Working**

- **Step-by-Step Explanation**: (1) Compiler records the member's access flag as `ACC_PRIVATE` in the generated `.class` file. (2) Any access from outside the top-level class is rejected at *compile time* with "has private access in". (3) For legitimate access from a sibling nested class within the same top-level class, pre-Java 11 compilers generated a synthetic package-private bridge method (e.g., `access$000`) because the JVM itself only understood private-to-the-exact-class visibility, not private-to-the-enclosing-top-level-class. (4) Since Java 11, `NestMates` (`NestHost`/`NestMembers` classfile attributes) let the JVM itself recognize nest membership, so the JVM performs the access check directly without synthetic bridges.
- **Memory Layout**: Not directly applicable — access modifiers affect compile-time/verification checks, not the runtime memory layout of the object itself; a `private` field occupies the same slot in the object's memory layout as any other field of the same type.
- **Diagrams**:
```text
Outer.java (single source/class file boundary)
┌─────────────────────────────────────────┐
│ class Outer {                            │
│   private int secret;   <-- accessible   │
│   class Inner {                          │
│     void peek() { secret++; } <-- OK     │
│   }                                       │
│ }                                         │
└─────────────────────────────────────────┘
         ^ NOT visible outside this box (no subclass/package access)
```
- **JVM Behaviour**: The `invokespecial` bytecode instruction is used (not `invokevirtual`) for private instance method calls because private methods cannot be overridden — the JVM can resolve the exact target method statically, enabling more aggressive JIT inlining since there's no virtual dispatch ambiguity.

**Interview Questions**

*Basic*
1. What does `private` mean, and can a private member be accessed from a subclass?
2. Can a top-level (outer) class be declared `private`?

*Intermediate*
3. How can two nested classes within the same outer class access each other's `private` members if private means "encapsulated to that class"?
4. Why does Java use `invokespecial` for calls to private instance methods instead of `invokevirtual`?

*Advanced*
5. How does Java 11's Nestmates feature change the bytecode generated for private member access between nested classes?
6. Can reflection bypass `private` access, and what stops it in a modular (JPMS) application?

*Scenario-based*
7. You need a field to be settable only through validated setters but readable efficiently by many internal helper methods in the same class. How do you design this using `private`?

**Detailed Answers**

1. `private` restricts visibility to the declaring top-level class (including all its nested classes); it is not inherited/visible in subclasses at all — a subclass cannot even see that the member exists via `super`, reflection aside. This is the strongest encapsulation level, used for internal state and helper logic.
2. No — a *top-level* class can only be `public` or package-private (no modifier); `private` (and `protected`) are only legal on *nested* (member) classes, not top-level ones, because "private to itself" would make a top-level class unusable from anywhere, including its own file's other classes.
3. Historically (pre-Java 11) the compiler generated synthetic package-private static accessor methods (visible in bytecode as `access$100`, etc.) in the outer class so the inner class could call them — the JVM's classfile-level access rules only understood exact class-to-class or package private, not "private to the enclosing top-level class" as a concept. Since Java 11, the `NestHost`/`NestMembers` attributes group all nested classes of a top-level class into one "nest", and the JVM's own access-control logic natively permits private access between nestmates without any synthetic bridge methods, reducing bytecode bloat and improving reflection performance (`Lookup.privateLookupIn`).
4. Because a `private` method can never be overridden by a subclass, the compiler knows the exact target at compile time — there's no polymorphic dispatch to resolve. `invokespecial` performs direct, non-virtual dispatch (like constructors and `super.method()` calls), letting the JIT compiler skip the virtual method table (vtable) lookup entirely and often inline the call.
5. Nestmates introduce two new classfile attributes: `NestHost` (on member classes, pointing to the top-level class) and `NestMembers` (on the top-level class, listing all its nested classes). The JVM's `JVM_AccessControl` (Reflection.java / VM access checks) recognizes classes sharing the same nest host and permits `private` member access between them directly — no synthetic `access$` bridge methods are emitted, shrinking generated bytecode and simplifying stack traces/debugging.
6. Yes, via `Field.setAccessible(true)` or `Method.setAccessible(true)`, reflection can bypass compile-time `private` checks by disabling Java language access checks at runtime. However, in a modularized application (JPMS), a module must explicitly `opens` a package (or `opens ... to specificModule`) for reflective deep access to be permitted; otherwise `setAccessible` throws `InaccessibleObjectException`, and a `SecurityManager` (deprecated for removal since Java 17) could historically also block it.
7. Declare the field `private`, expose a validated `public`/`protected` setter that enforces invariants (e.g., range checks, non-null checks) before assignment, and let all other methods within the same class read/write the field directly (bypassing the setter internally is fine since the class itself is trusted to maintain its own invariants). This keeps validation centralized in one place while internal code retains fast, direct field access.

**Code Examples**

```java
public class BankAccount {
    private double balance; // encapsulated: no direct external mutation

    public BankAccount(double initialBalance) {
        if (initialBalance < 0) throw new IllegalArgumentException("Negative balance");
        this.balance = initialBalance;
    }

    public void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        balance += amount; // internal code can touch the field directly
    }

    public double getBalance() {
        return balance; // controlled read-only exposure
    }
}
```

```java
// Nestmates example: Inner accesses Outer's private field directly (Java 11+)
public class Outer {
    private int secret = 42;

    class Inner {
        void reveal() {
            System.out.println("Secret: " + secret); // no synthetic accessor needed
        }
    }

    public static void main(String[] args) {
        Outer outer = new Outer();
        outer.new Inner().reveal();
    }
}
```

## `protected`

**Theory**

- **Core Concepts**: `protected` grants access to the declaring class, all classes in the same package (like package-private), *and* subclasses in other packages — but for subclasses in different packages, access is only permitted through a reference of the subclass's own type (or a further subtype), not through an arbitrary superclass reference. It sits between package-private and `public` in strictness, designed specifically to support inheritance-based extension.
- **Internal Working**: Enforced by the compiler via the `ACC_PROTECTED` access flag and a special JLS rule (§6.6.2) restricting cross-package subclass access to instances of the accessing subclass type itself, re-checked by the JVM verifier at link time.
- **When to Use It**: Members meant to be used/overridden by subclasses as extension points (template method hooks, protected fields in abstract base classes) but hidden from unrelated external callers.
- **Advantages**: Enables the *Template Method* and other inheritance-based design patterns while still hiding implementation details from non-subclass consumers; documents clear "this is for subclassing" intent.
- **Limitations**: Leaks implementation details to every subclass, potentially in other packages, making it harder to change later than `private`; package-private classes can effectively expose `protected` members within the package to any code there, which is sometimes broader than intended; module system (JPMS) further restricts protected access across module boundaries unless properly `exports`/`opens`.

**Internal Working**

- **Step-by-Step Explanation**: (1) Same-package access: treated identically to package-private — any class in the package can access it. (2) Cross-package access: only permitted from within the body of a subclass, and only via an expression whose static type is that subclass or a further subtype (the JLS's "protected access to instance members" rule) — you cannot do `superRef.protectedField` on an arbitrary `Super` instance from outside the package, only `this.protectedField` or via a `Sub`-typed reference. (3) The compiler enforces this restriction at compile time; the JVM verifier double-checks at class-loading/linking time.
- **Memory Layout**: Not directly applicable — identical field/method memory layout regardless of modifier; only the access-check phase differs.
- **Diagrams**:
```text
package a;                     package b;
class Super {                  class Sub extends Super {
  protected int x;               void m(Super other) {
}                                   other.x;      // COMPILE ERROR (not Sub-typed)
                                    this.x;       // OK (via subclass 'this')
                                    ((Sub) other).x; // OK if 'other' really is a Sub
                                  }
                                }
```
- **JVM Behaviour**: Bytecode verification includes protected-access checks at `getfield`/`putfield`/`invokevirtual` resolution time; the JIT does not treat protected differently from public once access is verified — no runtime performance penalty compared to public members.

**Interview Questions**

*Basic*
1. How does `protected` differ from package-private (default) access?
2. Can a `protected` member be accessed from a completely unrelated class in a different package?

*Intermediate*
3. Why can't a subclass in another package access a `protected` member through a plain superclass-typed reference?
4. Is a `protected` member of a class visible to other classes in the same package, even if they don't extend it?

*Advanced*
5. How does the Java Platform Module System (JPMS) interact with `protected` access across module boundaries?
6. Why do many framework base classes (e.g., abstract classes meant for extension) favor `protected` fields/methods over `public`?

*Scenario-based*
7. You are designing an abstract `PaymentProcessor` base class with a hook method that subclasses in third-party packages must override, but that should not be callable by arbitrary external code. Which modifier do you use and why?

**Detailed Answers**

1. Package-private (no modifier) is visible only within the declaring package; `protected` includes that same-package visibility *plus* visibility to subclasses located in other packages (with the restricted access rule described below). So `protected` is strictly broader than package-private.
2. No — an unrelated class in a different package that is not a subclass has zero access to a `protected` member, exactly as if it were package-private from its point of view.
3. The JLS restricts this to prevent a subclass from using inherited access to peek into *unrelated* sibling instances of the superclass that happen to be passed to it. Since the subclass only truly "owns" the protected member through its own inheritance chain, access is allowed only when the compiler can prove the object is (statically) of the accessing subclass type or further subtype — protecting encapsulation of arbitrary same-superclass objects it didn't create.
4. Yes — within the same package, `protected` behaves exactly like package-private; any class in that package, subclass or not, can access it directly.
5. In JPMS, `protected` (and public) members are only accessible outside their module if the module `exports` the declaring package (or `exports ... to` the consuming module for qualified exports); reflection-based deep access additionally requires `opens`. So modularization adds a second, coarser-grained gate on top of the language-level `protected` rule — even a public/protected member is inaccessible to a non-exported package.
6. Because framework base classes want extension points (`protected` hooks, template methods, or state) to be usable by subclasses that customize behavior, but not to be part of the stable public API called directly by arbitrary client code — this keeps the "contract with subclassers" separate from and more flexible than the "contract with API consumers," letting the internal hook signatures evolve more freely.
7. Use `protected` for the hook method: it allows any subclass (including ones in third-party packages, assuming the package is exported) to override and be invoked polymorphically via the base class's public template method, but prevents arbitrary unrelated code from calling the hook directly, preserving the intended call sequence defined by the base class's public API.

**Code Examples**

```java
public abstract class PaymentProcessor {
    public final void process(double amount) { // public entry point (Template Method)
        validate(amount);
        charge(amount); // subclasses supply the actual charging logic
    }

    protected void validate(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Invalid amount");
    }

    protected abstract void charge(double amount); // extension point for subclasses
}

package com.example.gateways;
public class StripeProcessor extends PaymentProcessor {
    @Override
    protected void charge(double amount) {
        System.out.println("Charging $" + amount + " via Stripe");
    }
}
```

## `public`

**Theory**

- **Core Concepts**: `public` is the least restrictive access level — a `public` type or member is visible from any other class, in any package, provided the containing package/module is itself reachable (imported and, under JPMS, exported). It defines the true, stable API contract of a class or library.
- **Internal Working**: Marked with the `ACC_PUBLIC` flag in the classfile; the compiler performs no restriction on the accessing class's location, though JPMS module boundaries can still block access to a public member if its package isn't exported.
- **When to Use It**: For the intentional, documented API surface — public classes, interface methods, constructors meant to be instantiated externally, and constants (`public static final`) meant for broad reuse.
- **Advantages**: Maximum reusability and interoperability; necessary for any cross-package/cross-module collaboration.
- **Limitations**: Once published (especially in a versioned library), a `public` member becomes part of a binary/source compatibility contract — removing or changing its signature is a breaking change; overuse of `public` fields breaks encapsulation and invariant protection.

**Internal Working**

- **Step-by-Step Explanation**: (1) Compiler tags the member/class with `ACC_PUBLIC`. (2) Any class that can load the containing class (via classpath or, in JPMS, via an `exports` declaration) can access the public member without further restriction, subject only to normal type-checking. (3) In modular applications, even `public` members in a package that is *not* exported by its module are inaccessible outside that module (`IllegalAccessError`/compile error) — public is necessary but not sufficient for cross-module visibility.
- **Memory Layout**: Not directly applicable — access modifier has no bearing on field size/alignment/placement in the object header or instance data layout.
- **Diagrams**:
```text
            module com.lib { exports com.lib.api; }
┌────────────────────────────────────────────────────┐
│ package com.lib.api (exported)                      │
│   public class Widget { public void render(){} }     │  <-- reachable externally
│ package com.lib.internal (NOT exported)              │
│   public class Helper { ... }                        │  <-- public but UNREACHABLE
└────────────────────────────────────────────────────┘
```
- **JVM Behaviour**: Public member access compiles to ordinary `invokevirtual`/`getfield`/`putfield` instructions with no extra verification cost versus other modifiers (aside from the module-export check, which happens at class-loading/linking time, not per-call).

**Interview Questions**

*Basic*
1. What does declaring a class or member `public` guarantee?
2. Can a `.java` file contain more than one `public` top-level class?

*Intermediate*
3. If a class and its method are both `public`, can external code still fail to access that method? Under what circumstance?
4. Why is it considered bad practice to expose mutable `public` fields directly?

*Advanced*
5. How does the Java Platform Module System change the meaning of "public" for cross-module access?
6. What is the difference between binary compatibility and source compatibility when evolving a `public` API?

*Scenario-based*
7. You maintain a widely used library and need to remove a `public` method that's no longer needed. What's the safe migration path?

**Detailed Answers**

1. It guarantees the type/member is accessible from any other class that can see the containing class — i.e., there is no package or inheritance restriction at the language level; it's the broadest access modifier.
2. No — a `.java` source file can declare at most one top-level `public` class, and its name must match the filename exactly; other top-level classes in the same file must be package-private.
3. Yes: under JPMS, if the method's package isn't `exported` by its module, external modules get `IllegalAccessError` at runtime (or a compile error) even though the class and method are both `public` — public visibility is necessary but not sufficient across module boundaries.
4. Public mutable fields bypass all validation/invariant-checking and expose implementation details directly, so any external code can put the object into an inconsistent state, and the class can never later change its internal representation (e.g., switch a field's type or add derived computation) without breaking every caller — violating encapsulation and the Open/Closed principle.
5. Pre-JPMS, `public` meant "accessible anywhere on the classpath." With JPMS, `public` types/members are only accessible outside their own module if the containing package is explicitly `exports`-ed (optionally qualified to specific modules with `exports ... to`), and reflective access additionally requires `opens`. This adds a second layer of encapsulation ("strong encapsulation") on top of the language-level `public` keyword.
6. Source compatibility means existing *source code* still compiles unchanged against the new API version (e.g., adding an overloaded method is source-compatible). Binary compatibility means existing *compiled bytecode* still links and runs correctly without recompilation (e.g., adding a new interface method with a default implementation is binary-compatible, but removing a method is neither). Library maintainers must consider both when evolving public APIs.
7. Follow a deprecate-then-remove cycle: annotate the method `@Deprecated(since="x.y", forRemoval=true)` with Javadoc pointing to the replacement, keep it functional for at least one or more minor/major release cycles to give consumers time to migrate, monitor usage/communicate via release notes, and only remove it in a subsequent major version bump (following semantic versioning) to avoid silently breaking downstream consumers.

**Code Examples**

```java
// File: Widget.java — the public top-level class must match the filename
public class Widget {
    private final String id; // encapsulated state, not exposed directly

    public Widget(String id) {
        this.id = id;
    }

    public String getId() { // public accessor instead of a public field
        return id;
    }

    @Deprecated(since = "2.0", forRemoval = true)
    public void legacyRender() {
        render(); // delegate to the new method during the migration window
    }

    public void render() {
        System.out.println("Rendering widget " + id);
    }
}
```

## package-private (default)

**Theory**

- **Core Concepts**: When no access modifier is written, a class/member gets *package-private* (also called default) access: visible only to other classes within the exact same package, not to subclasses in other packages and not to unrelated external code. It is Java's original mechanism for package-level encapsulation, predating JPMS modules.
- **Internal Working**: The compiler omits any `ACC_PUBLIC`/`ACC_PRIVATE`/`ACC_PROTECTED` flag (or sets none of them), and the JVM verifier checks that the accessing class's package name exactly matches the declaring class's package name (and, historically, the same classloader — two classes with the same package name loaded by different classloaders are treated as different packages for access purposes, a subtlety exploited in classloader-isolation designs).
- **When to Use It**: Package-internal helper classes/utility methods that implement a package's public API but shouldn't leak outside it; useful for organizing a cohesive package-level module before JPMS existed (and still useful for intra-package collaboration even with modules).
- **Advantages**: Lets multiple classes within a package collaborate closely (share helpers, internal state) while presenting a clean, restricted public surface to the rest of the application; no boilerplate needed (it's the absence of a keyword).
- **Limitations**: Easy to trigger accidentally by forgetting the `public` keyword; provides only package-level granularity, not the finer per-class control `private` gives nor the fine module-level control JPMS gives; two same-named packages in different JARs/classloaders can silently collide or fail to see each other depending on classloader delegation.

**Internal Working**

- **Step-by-Step Explanation**: (1) Compiler emits the class/member without a public/private/protected flag. (2) At compile time, the compiler checks that the accessing source file's package declaration textually matches. (3) At class-loading/runtime (e.g., via reflection or dynamically loaded classes), the JVM checks that both classes were defined by runtime-compatible classloaders in the same named package — the *same named package loaded by different classloaders* is treated as a *different* runtime package, blocking access even though the textual package name matches.
- **Memory Layout**: Not directly applicable.
- **Diagrams**:
```text
package com.app.service;      (same package, different file — OK)
class Helper { void assist(){} }
class Service { void run() { new Helper().assist(); } } // accessible

package com.other;            (different package — COMPILE ERROR)
class Client { void x() { new Helper(); } } // Helper not visible
```
- **JVM Behaviour**: Package-private access checks occur during bytecode verification/linking (`resolveClassAccess` style checks in the JVM spec); at runtime, `ClassLoader` identity plus package name jointly determine the "runtime package," which matters heavily for correct behavior in OSGi/plugin systems and JPMS split-package prevention.

**Interview Questions**

*Basic*
1. What is the visibility scope of a class or member with no explicit access modifier?
2. How does package-private differ from `protected`?

*Intermediate*
3. If you forget to write `public` on a top-level class meant to be part of your library's API, what happens to external callers?
4. Can two classes named identically and declared in the same-named package but loaded by different classloaders access each other's package-private members?

*Advanced*
5. How does the JVM determine "same package" for access-control purposes, and why does classloader identity matter?
6. How does the Java Platform Module System's "split package" restriction relate to package-private visibility?

*Scenario-based*
7. You're building an internal utility class used by several classes in one package but that must never be used outside it, even accidentally by a teammate importing it from another package. How do you enforce this, and does JPMS add anything beyond package-private?

**Detailed Answers**

1. It is visible to, and only to, other classes located in the exact same package (same package declaration and, at runtime, same classloader-defined package) — not to subclasses elsewhere, not to unrelated packages.
2. `protected` extends package-private visibility further to subclasses in other packages (with the restricted "accessible via subclass-typed reference" rule); package-private grants no access whatsoever outside the package, regardless of inheritance.
3. External callers in other packages simply cannot see the class at all — a compile-time error ("X is not public in Y; cannot be accessed from outside package") if referenced directly, since without `public` the class defaults to package-private and is invisible outside its package.
4. No — the JVM defines "same package" for access-control purposes as the same package name *and* the same defining classloader (or classloaders in a delegation relationship that resolve to the same class objects); two classes with an identical package name but loaded by unrelated classloaders are considered to be in different runtime packages and cannot access each other's package-private members. This underlies classloader-based isolation (e.g., application server module isolation, plugin sandboxes).
5. The JVM tracks each loaded class's defining classloader plus its package name as a compound key; access checks for package-private/protected same-package cases compare both the package name string and confirm the two classes share a compatible defining classloader per the JVM specification's package access rules. This is deliberate: it lets two "same-named" packages loaded in different isolated classloader realms (e.g., two versions of a plugin) coexist without accidentally granting each other implicit access.
6. JPMS disallows "split packages" — the same package must not be provided by two different modules on the module path — precisely because package-private (and protected same-package) access assumes a single, coherent definition of a package's membership; if a package were split across modules, package-private access semantics and internal consistency would break down, so the module system enforces one package = one module at readability-resolution time (a `LayerInstantiationException` results otherwise).
7. Declare the utility class and its methods without any modifier (package-private), placing it in the same package as its collaborators — this is compile-time-enforced and requires no extra tooling. For an additional, stronger guarantee under JPMS, do NOT `exports` that package from the module at all (keep it unexported); this makes the package completely unreachable to any other module even via reflection (unless explicitly `opens`), providing defense-in-depth beyond plain package-private visibility.

**Code Examples**

```java
// File: OrderValidator.java — package-private helper, package com.shop.order
package com.shop.order;

class OrderValidator { // no modifier: only visible within com.shop.order
    boolean isValid(Order order) {
        return order.getItems() != null && !order.getItems().isEmpty();
    }
}

package com.shop.order;
public class OrderService { // public API entry point
    private final OrderValidator validator = new OrderValidator();

    public void submit(Order order) {
        if (!validator.isValid(order)) { // package-private collaboration
            throw new IllegalStateException("Invalid order");
        }
        System.out.println("Order submitted: " + order);
    }
}
```
