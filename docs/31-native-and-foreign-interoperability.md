# 31. Native & Foreign Interoperability *(new)*

## Java Native Interface (JNI)

**Theory**

- **Core Concepts**: JNI is the original, standard Java framework (present since JDK 1.1) that lets Java code call native methods implemented in C/C++ (and vice versa — native code calling back into the JVM). A Java method is declared `native`, has no body, and its implementation is provided by a compiled shared library (`.so`/`.dll`/`.dylib`) loaded via `System.loadLibrary()`.
- **Internal Working**: The JVM generates a specific C function signature (name-mangled from the Java package/class/method name) that the native library must implement; the JVM passes a `JNIEnv*` pointer giving the native code controlled access back into the JVM (creating objects, calling methods, throwing exceptions).
- **When to Use It**: Legacy integration with existing C/C++ codebases, OS-specific system calls not exposed by the JDK, or performance-critical native libraries where FFM API isn't yet a viable replacement (e.g., needing to call back extensively into complex Java object graphs).
- **Advantages**: Mature, universally supported across all JVMs; full two-way interoperability (native-to-Java callbacks); works with existing C/C++ toolchains and headers via `javac -h` generated headers.
- **Limitations**: Extremely verbose and error-prone (manual reference management, name-mangled signatures); a single native bug (buffer overrun, bad pointer) can crash the entire JVM process with no safety net; requires separate compilation per target platform/architecture; local/global reference leaks are a common source of native memory leaks.

**Internal Working**

- **Step-by-Step Explanation**: (1) Declare a Java method `native`, e.g. `private native int add(int a, int b);`. (2) Run `javac -h .` to generate a C header (`Class_method` signature) with the JNI-mangled function name. (3) Implement that function in C/C++, receiving `JNIEnv*` and `jobject`/`jclass` as the first two parameters. (4) Compile the C code into a shared library. (5) At runtime, `System.loadLibrary("mylib")` maps the native method table so the JVM can resolve and invoke the native symbol when the Java method is called. (6) The native function uses `JNIEnv*` function pointers (e.g., `GetIntField`, `NewObject`, `CallVoidMethod`) to interact back with JVM-managed objects, and must correctly manage local/global references to avoid leaks or dangling handles.
- **Memory Layout**: Native code operates on raw off-heap memory (malloc'd buffers, pointers) entirely outside the Java heap and GC's control; Java objects passed to native code are represented as opaque `jobject` handles (local references), and the GC can move/collect the underlying object once no reference (local or global) is held, so native code must pin/hold a JNI reference for as long as it needs the object.
- **Diagrams**:
```text
Java Heap                       Native Heap (malloc/C runtime)
┌───────────────┐               ┌──────────────────┐
│ MyClass obj   │  jobject ref  │ native buffers,  │
│  (GC managed) │◄─────────────►│ raw pointers     │
└───────────────┘  JNIEnv* calls └──────────────────┘
     ^ managed by GC                   ^ manually managed (malloc/free)
```
- **JVM Behaviour**: Calling a native method uses the JVM's native method dispatch table; a crash inside native code (segfault, bad pointer) terminates the entire JVM process (`SIGSEGV` -> fatal error log, `hs_err_pid*.log`) since there's no JVM-level memory safety around raw native pointers; JIT compilation does not optimize across the JNI boundary — native calls act as an opaque barrier, and JNI calls carry non-trivial per-call overhead (argument marshaling, reference table management) compared to a pure Java call.

**Interview Questions**

*Basic*
1. What is JNI, and what keyword marks a Java method as implemented natively?
2. How does the JVM locate the native implementation of a `native` method at runtime?

*Intermediate*
3. What is the difference between a JNI local reference and a global reference?
4. Why can a bug in native code crash the entire JVM, unlike a bug in pure Java code?

*Advanced*
5. How does the JIT compiler treat calls across the JNI boundary, and what performance implications does this have?
6. What replacement has the JDK introduced for JNI, and what safety guarantees does it add?

*Scenario-based*
7. Your team needs to call into an existing high-performance C image-processing library from Java. What are the tradeoffs of using JNI versus the newer Foreign Function & Memory API?

**Detailed Answers**

1. JNI (Java Native Interface) is the standard bridge letting Java call native (C/C++) code and vice versa; a Java method is marked `native`, has no body in Java, and its actual implementation lives in a compiled shared library loaded at runtime.
2. When `System.loadLibrary("name")` (or `System.load(path)`) is called, the JVM loads the shared library and, on first invocation of a `native` method, resolves the corresponding exported C symbol using JNI's name-mangling convention (`Java_package_Class_method`), then binds it into the method's internal native function pointer for subsequent calls.
3. A local reference is valid only for the duration of the current native method call (and is automatically freed when that native frame returns, or manually via `DeleteLocalRef`); a global reference (`NewGlobalRef`) persists across native calls and must be explicitly released with `DeleteGlobalRef`, used when native code needs to retain a Java object reference beyond a single call (e.g., a callback registered for later invocation).
4. Native code operates with raw pointers and no JVM-enforced memory safety (bounds checking, type safety, or automatic memory management) — a buffer overrun, null/dangling pointer dereference, or stack corruption in native code triggers an OS-level fault (e.g., SIGSEGV) that the OS delivers to the whole process, and the JVM has no way to recover or isolate this since it's running as native machine code within the same process address space, unlike a Java `NullPointerException` which the JVM safely intercepts and turns into a catchable exception.
5. The JIT treats native method calls as an opaque boundary it cannot see into or optimize across — it cannot inline the native call, cannot perform escape analysis on objects passed to native code (since it can't prove the native code won't retain a reference), and each JNI call incurs fixed overhead for transitioning CPU/stack state between Java and native calling conventions plus JNI reference-table bookkeeping, making JNI calls significantly more expensive per-invocation than a pure Java method call — batching work per JNI call is a common mitigation.
6. The Foreign Function & Memory (FFM) API (finalized in Java 22, JEP 454) replaces JNI for most use cases: it provides `MemorySegment` for bounds-checked, type-safe access to off-heap memory and `Linker`/`FunctionDescriptor` for calling native functions directly from Java without writing any C glue code or generating headers. `Arena` ties memory/segment lifetimes to well-defined scopes, preventing use-after-free, and access is bounds-checked at the API level, eliminating whole classes of native-memory bugs that plague hand-written JNI code.
7. JNI offers maximum compatibility (works on all JDK versions, arbitrarily old) and full bidirectional callback support, but requires writing/maintaining glue C code, per-platform compilation, and offers no memory safety — a defect can crash the whole JVM. The FFM API (Java 22+) lets you call the C library's functions directly from Java (given its symbols/headers), with bounds-checked `MemorySegment` access and confined `Arena` lifetimes, drastically reducing boilerplate and the risk surface, at the cost of requiring a recent JDK version and less maturity/tooling than decades-old JNI. For a modern codebase already on JDK 22+, FFM API is generally preferred; for legacy support or complex bidirectional callback-heavy integration, JNI may still be necessary.

**Code Examples**

```java
// Java side: declare the native method and load the library
public class NativeMath {
    static {
        System.loadLibrary("nativemath"); // loads libnativemath.so/.dylib/.dll
    }

    public native int add(int a, int b);

    public static void main(String[] args) {
        System.out.println(new NativeMath().add(2, 3));
    }
}
```

```c
// C side (generated via `javac -h .`): NativeMath.c
#include "NativeMath.h"

JNIEXPORT jint JNICALL Java_NativeMath_add(JNIEnv *env, jobject obj, jint a, jint b) {
    return a + b; // trivial native computation, real code would call into a C library
}
```

## Foreign Function & Memory API (Project Panama)

**Theory**

- **Core Concepts**: The FFM API (standardized in Java 22 via JEP 454, evolved through JEPs 412/419/424/434/442/454) is Project Panama's replacement for JNI, providing a pure-Java way to call native functions (`Linker`, `FunctionDescriptor`) and to allocate/access off-heap memory safely (`MemorySegment`, `Arena`) without writing any native glue code.
- **Internal Working**: `Arena` establishes a memory lifetime scope; `MemorySegment` is a bounds-checked, typed view over a contiguous region of memory (on- or off-heap); `Linker.nativeLinker().downcallHandle(...)` builds a `MethodHandle` that directly invokes a native function by address and calling-convention descriptor, without any generated C shim.
- **When to Use It**: Calling native libraries (image processing, compression, ML runtimes, OS APIs) from modern JDKs (22+), replacing hand-written JNI, or manipulating large off-heap buffers (e.g., memory-mapped files, native interop buffers) with safety guarantees.
- **Advantages**: No native glue code (no headers, no C compiler step) needed for typical calls; deterministic, scope-bound memory lifetime management via `Arena` prevents use-after-free; bounds-checked `MemorySegment` access prevents buffer overruns; typically much lower overhead than JNI per call.
- **Limitations**: Requires JDK 22+ for the standardized API (earlier versions had it as a preview feature under different JEP numbers); still requires understanding native ABI details (struct layouts, calling conventions) for complex native APIs; `Arena.ofConfined()` restricts segment access to the owning thread, which needs care in multi-threaded designs (use `ofShared()` when cross-thread access is required, understanding its weaker safety guarantees).

**Internal Working**

- **Step-by-Step Explanation**: (1) Obtain a `Linker` (`Linker.nativeLinker()`) representing the platform's native calling convention (e.g., System V AMD64 or Windows x64). (2) Look up the native function's symbol via a `SymbolLookup` (e.g., `SymbolLookup.libraryLookup("libm", arena)` for a shared library, or the default lookup for `libc`). (3) Describe the function's native signature with a `FunctionDescriptor` (e.g., `FunctionDescriptor.of(JAVA_DOUBLE, JAVA_DOUBLE)` for `double sqrt(double)`). (4) Build a `MethodHandle` via `linker.downcallHandle(symbol, descriptor)`. (5) Invoke the handle like any other `MethodHandle.invoke(...)`, and the JVM marshals arguments/return values across the native ABI boundary directly. (6) For memory, open an `Arena` (`ofConfined`, `ofShared`, `ofAuto`, or `global()`), allocate `MemorySegment`s from it, and rely on the arena's `close()` (or GC, for `ofAuto`) to deterministically free the memory, with all accesses bounds-checked against the segment's declared size.
- **Memory Layout**: `MemorySegment`s can wrap on-heap arrays (heap segments) or off-heap native memory (native segments, outside GC-managed heap, outside Metaspace); a native segment is essentially a `(base address, byte size, confinement thread, associated Arena scope)` tuple that every access is checked against — out-of-bounds or use-after-close accesses throw `IndexOutOfBoundsException`/`IllegalStateException` instead of corrupting memory.
- **Diagrams**:
```text
try (Arena arena = Arena.ofConfined()) {
   ┌───────────────────────────────┐
   │ Off-heap native memory region │ <-- MemorySegment (bounds-checked view)
   └───────────────────────────────┘
   Java heap: MethodHandle, MemorySegment object (metadata only)
} // arena.close() -> segment invalidated, native memory freed deterministically
```
- **JVM Behaviour**: Downcalls compiled through `Linker` are backed by a JVM intrinsic/stub generator that produces a tight native trampoline (far cheaper than JNI's generic dispatch path); the JIT can inline and optimize around `MethodHandle` invocations of downcall handles in hot loops once warmed up; segment bounds/confinement checks are inserted as ordinary bytecode checks that the JIT can often hoist or eliminate after escape/bounds analysis.

**Interview Questions**

*Basic*
1. What two core capabilities does the FFM API provide?
2. What is an `Arena`, and why is it needed?

*Intermediate*
3. How does a `MemorySegment` prevent the kind of buffer-overrun bugs common in raw JNI/native code?
4. What's the difference between `Arena.ofConfined()` and `Arena.ofShared()`?

*Advanced*
5. How does a downcall via `Linker` avoid the overhead typically associated with JNI?
6. What happens if you access a `MemorySegment` after its owning `Arena` has been closed?

*Scenario-based*
7. You need to call a native `libm` math function (e.g., `sqrt`) from Java using the FFM API without writing any C code. Outline the steps.

**Detailed Answers**

1. It provides (a) safe, efficient access to off-heap (and on-heap) memory via `MemorySegment`/`MemoryLayout`, and (b) the ability to call native functions directly from Java via `Linker`/`FunctionDescriptor`/`MethodHandle`, without needing JNI glue code or a native compiler step.
2. An `Arena` is a controller of memory segment lifetime — all segments allocated from an arena become invalid once the arena is closed, giving deterministic, explicit control over when native memory is freed (similar in spirit to try-with-resources), which prevents both premature frees (use-after-free) and memory leaks (forgotten frees) that are common in manual native memory management.
3. Every `MemorySegment` carries its declared size and (for confined arenas) owning thread; every read/write/slice operation is checked against these bounds and against whether the segment's arena is still open, throwing `IndexOutOfBoundsException` or `IllegalStateException` on violation instead of silently reading/writing adjacent memory or freed memory, which is exactly the class of bug (buffer overrun, use-after-free) that plagues raw pointer-based JNI/C code.
4. `Arena.ofConfined()` restricts all access to segments it allocates to the single thread that created the arena (fast, no synchronization overhead, but throws `WrongThreadException` if accessed elsewhere); `Arena.ofShared()` allows access from any thread (useful for producer/consumer patterns across threads) at the cost of additional internal synchronization and a different, careful closing protocol since any thread might be using the memory concurrently.
5. `Linker.downcallHandle()` generates a specialized `MethodHandle` backed by a JVM-level stub tailored to the target function's exact calling convention and argument layout, computed once at handle-creation time; there's no per-call name lookup, generic argument boxing/marshaling framework, or JNI reference-table bookkeeping as in classic JNI — the JIT can treat the resulting call similarly to any other `MethodHandle` invocation and inline/optimize it in hot paths, giving near-native call overhead after warm-up.
6. Any subsequent access throws `IllegalStateException` ("already closed") — the FFM API deliberately fails fast rather than allowing a dangling-pointer-style use-after-free, which is a major safety improvement over raw JNI/native pointer code where such an access would silently read/write freed or reused memory.
7. (1) Create an `Arena` (e.g., `Arena.ofConfined()`); (2) obtain a `Linker` via `Linker.nativeLinker()`; (3) look up the `sqrt` symbol from the platform math library via `linker.defaultLookup().find("sqrt")` (or a specific `SymbolLookup`); (4) describe its signature with `FunctionDescriptor.of(ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE)`; (5) create a downcall `MethodHandle` via `linker.downcallHandle(symbol, descriptor)`; (6) invoke it with `(double) sqrtHandle.invoke(2.0)` inside a try/catch for `Throwable` (since `MethodHandle.invoke` declares `throws Throwable`).

**Code Examples**

```java
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

public class NativeSqrt {
    public static void main(String[] args) throws Throwable {
        Linker linker = Linker.nativeLinker();
        SymbolLookup stdlib = linker.defaultLookup();

        // Locate the native 'sqrt' symbol and describe its C signature: double sqrt(double)
        MemorySegment sqrtAddr = stdlib.find("sqrt").orElseThrow();
        FunctionDescriptor descriptor = FunctionDescriptor.of(
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE);
        MethodHandle sqrtHandle = linker.downcallHandle(sqrtAddr, descriptor);

        double result = (double) sqrtHandle.invoke(2.0);
        System.out.println("sqrt(2.0) = " + result); // 1.4142135623730951
    }
}
```

```java
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class OffHeapBuffer {
    public static void main(String[] args) {
        try (Arena arena = Arena.ofConfined()) {
            // Allocate a native (off-heap) buffer of 4 ints, bounds-checked
            MemorySegment segment = arena.allocate(4 * ValueLayout.JAVA_INT.byteSize());
            for (int i = 0; i < 4; i++) {
                segment.setAtIndex(ValueLayout.JAVA_INT, i, i * i);
            }
            for (int i = 0; i < 4; i++) {
                System.out.println(segment.getAtIndex(ValueLayout.JAVA_INT, i));
            }
        } // Native memory deterministically freed here
    }
}
```

## Vector API

**Theory**

- **Core Concepts**: The Vector API (incubating since JDK 16, JEP 338/414/417/426/438/460) provides a portable, explicit API (`jdk.incubator.vector`) for expressing SIMD (Single Instruction, Multiple Data) computations in pure Java, letting a single Java expression operate on a whole *vector lane* of primitive values (e.g., 4/8/16 `float`s at once) that compiles down to hardware vector instructions (SSE/AVX on x86, NEON/SVE on ARM) at runtime.
- **Internal Working**: A `VectorSpecies<T>` describes the element type and the preferred (hardware-optimal) vector length for the current CPU; operations like `add`, `mul`, `fma` are performed on `Vector<T>` values representing a full lane, and the JIT's C2 compiler recognizes these API calls and emits actual SIMD machine instructions instead of a Java-level loop.
- **When to Use It**: CPU-bound numerical workloads that are naturally data-parallel — image/signal processing, linear algebra, physics simulations, ML inference kernels — where auto-vectorization by the JIT is unreliable or insufficient and explicit control over lane width/masking is needed.
- **Advantages**: Portable across CPU architectures (the same Java source adapts to the widest vector width available, via `VectorSpecies.preferred()`), far more predictable than relying on JIT auto-vectorization of scalar loops, can yield multi-x speedups for suitable numeric kernels.
- **Limitations**: Still an incubator API (`jdk.incubator.vector`, requires `--add-modules jdk.incubator.vector`) as of recent JDKs, so its API can change between releases and isn't yet recommended for long-term stable production APIs; only benefits genuinely data-parallel numeric code; requires careful handling of tail/remainder elements when array length isn't a multiple of the vector length.

**Internal Working**

- **Step-by-Step Explanation**: (1) Choose a `VectorSpecies<Float>` (e.g., `FloatVector.SPECIES_PREFERRED`) representing the optimal lane width for the running CPU (e.g., 8 floats for 256-bit AVX2). (2) Loop over the array in steps of `species.length()`, loading a `FloatVector` lane via `FloatVector.fromArray(species, array, offset)`. (3) Perform vectorized arithmetic (`va.add(vb)`, `va.fma(vb, vc)`) which represents an entire lane's worth of parallel scalar operations as a single API call. (4) Store the result back via `.intoArray(...)`. (5) Handle the "tail" — remaining elements fewer than one full lane — either with a masked operation (`species.indexInRange`) or a scalar fallback loop. (6) At JIT compile time (C2), these vector API calls are recognized as intrinsics and lowered directly to the CPU's native SIMD instructions (e.g., `VADDPS` on AVX).
- **Memory Layout**: Vector values are typically loaded from contiguous primitive arrays (which are already contiguous in the Java heap) into CPU vector registers (XMM/YMM/ZMM on x86, or NEON/SVE registers on ARM) — the JVM heap array layout is unaffected, but the computation itself briefly resides in wide hardware registers rather than iterating scalar-by-scalar.
- **Diagrams**:
```text
Scalar loop:              Vector API (lane width = 8):
for i in 0..N:            for i in 0..N step 8:
  c[i]=a[i]+b[i]            va = load a[i..i+8]
  (N iterations)            vb = load b[i..i+8]
                            vc = va.add(vb)   // 1 SIMD instruction, 8 adds
                            store vc -> c[i..i+8]
                          (N/8 iterations)
```
- **JVM Behaviour**: The Vector API relies on C2 JIT intrinsics rather than bytecode-level special-casing — methods like `FloatVector.add` are recognized by the compiler and replaced with direct SIMD machine code once the method is JIT-compiled (so performance benefits only appear after warm-up, not in interpreted/C1 execution); species/lane width chosen via `SPECIES_PREFERRED` is resolved based on the actual CPU's supported instruction set (e.g., AVX-512 vs AVX2 vs SSE) detected by the JVM at startup.

**Interview Questions**

*Basic*
1. What problem does the Vector API solve that a normal Java `for` loop over an array doesn't reliably achieve?
2. What module must be added to compile/run code using the Vector API, and why?

*Intermediate*
3. What is a `VectorSpecies`, and why would you use `SPECIES_PREFERRED` instead of a hardcoded lane width?
4. Why does code using the Vector API need a scalar "tail loop" for handling remaining elements?

*Advanced*
5. How does the JIT compiler turn Vector API calls into actual SIMD CPU instructions?
6. Why is the Vector API still an incubator/preview feature after several JDK releases, and what risk does that pose for production adoption?

*Scenario-based*
7. You have a hot loop computing element-wise `c[i] = a[i] * b[i] + d[i]` over large `float` arrays and profiling shows it's CPU-bound. How would you use the Vector API to speed it up, and what pitfalls should you watch for?

**Detailed Answers**

1. A normal scalar loop processes one array element per iteration and relies entirely on the JIT's *auto-vectorization* to opportunistically batch operations into SIMD instructions — this is heuristic, fragile (small code changes can silently disable it), and not portable/predictable across JVM versions or CPUs. The Vector API expresses the parallelism explicitly in the source code, so a whole lane's worth of elements is processed with one guaranteed SIMD operation per call, making the parallelism reliable and portable rather than dependent on compiler auto-detection.
2. You must add `--add-modules jdk.incubator.vector` (and the corresponding `--add-exports`/`--enable-preview` flags on some JDKs) because the API lives in `jdk.incubator.vector`, an *incubator module* — the JDK's mechanism for shipping APIs that aren't yet finalized/standardized, signaling that its surface may still change between releases.
3. A `VectorSpecies<T>` combines an element type (e.g., `float`) with a concrete vector shape/lane count matched to the underlying hardware's vector register width. Using `SPECIES_PREFERRED` (rather than a fixed shape like `SPECIES_256`) lets the same source code automatically use the widest vector width the current CPU actually supports (e.g., AVX-512's 512-bit registers vs. an older CPU's 128-bit SSE registers), maximizing throughput portably without recompilation.
4. Array lengths are rarely exact multiples of the vector lane width (e.g., a 1000-element array with an 8-wide lane leaves 1000 mod 8 = 0 in this case, but often there's a remainder); the Vector API cannot safely process a partial lane the same way as a full one without either a mask (a per-lane boolean predicate limiting which elements are read/written) or falling back to a plain scalar loop for the last few elements, to avoid reading/writing past the array bounds.
5. Vector API methods are implemented as thin Java wrappers whose bytecode the JIT (specifically the server/C2 compiler) recognizes as *intrinsics* — special-cased method signatures that the compiler replaces wholesale with hand-tuned SIMD machine code sequences (e.g., mapping `FloatVector.add` to the AVX `VADDPS` instruction) rather than compiling the Java method body literally; this recognition only kicks in once the method is hot enough to be JIT-compiled by C2, so cold/interpreted execution does not get SIMD speedups.
6. Vector API's exact class/method shapes, species definitions, and masking semantics have continued to evolve release-over-release (it has been re-previewed/incubated across many JDK versions, e.g. JEP 338 through 460) as the JDK team gathers feedback and refines semantics for platform portability (especially ARM SVE's variable-length vectors vs. x86's fixed-width vectors); shipping it as incubator avoids committing to a binary/source-compatible contract prematurely, but it means production code depending on it must track API changes across JDK upgrades and cannot rely on long-term stability guarantees until it's finalized.
7. Pick `FloatVector.SPECIES_PREFERRED`, loop over the arrays in steps of `species.length()`, load `a`/`b`/`d` lanes via `FloatVector.fromArray`, compute `va.fma(vb, vd)` (fused multiply-add, often mapping to a single hardware FMA instruction), and store into `c` via `intoArray`; after the main loop, handle any remaining tail elements (`length - length % species.length()` onward) with either a masked final iteration (`species.indexInRange`) or a simple scalar loop. Pitfalls: forgetting the tail loop causes `ArrayIndexOutOfBoundsException` or dropped elements; expecting speedup on cold code (JIT warm-up needed); and false assumptions that lane width is fixed across all deployment hardware (always use `SPECIES_PREFERRED`, not a hardcoded shape).

**Code Examples**

```java
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorSpecies;

public class VectorFma {
    static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;

    // Computes c[i] = a[i] * b[i] + d[i] using SIMD lanes, with a scalar tail loop
    static void fma(float[] a, float[] b, float[] d, float[] c) {
        int i = 0;
        int upperBound = SPECIES.loopBound(a.length);
        for (; i < upperBound; i += SPECIES.length()) {
            FloatVector va = FloatVector.fromArray(SPECIES, a, i);
            FloatVector vb = FloatVector.fromArray(SPECIES, b, i);
            FloatVector vd = FloatVector.fromArray(SPECIES, d, i);
            va.fma(vb, vd).intoArray(c, i); // one SIMD instruction per lane-width chunk
        }
        for (; i < a.length; i++) { // scalar tail loop for remaining elements
            c[i] = a[i] * b[i] + d[i];
        }
    }
}
```
