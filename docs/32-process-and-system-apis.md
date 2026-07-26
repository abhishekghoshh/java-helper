# 32. Process & System APIs *(new)*

## `ProcessBuilder` / `Process`

### Theory

**Core Concepts**
`ProcessBuilder` (`java.lang.ProcessBuilder`) is a builder API for configuring and launching native OS processes (external commands/executables) from Java, replacing the older, more limited `Runtime.exec(...)` overloads. `Process` (`java.lang.Process`) represents a running (or completed) native process, exposing its input/output/error streams, exit value, and lifecycle control (waiting, destroying). Since Java 9, the `ProcessHandle` API complements this with richer process introspection (PID, parent/children, CPU time) for both processes started by the JVM and arbitrary OS processes.

**Internal Working**
`ProcessBuilder.start()` invokes native OS process-creation calls (`fork`/`exec` on POSIX, `CreateProcess` on Windows) via JNI in the JDK's native library layer, sets up pipes connecting the child's stdin/stdout/stderr to the parent `Process` object's streams (or redirects them per configuration), and returns a `Process` handle whose `waitFor()`/`exitValue()`/`isAlive()` methods query native process state.

**When to Use It**
- Invoking external command-line tools (compilers, image/video processors, system utilities, shell scripts) from a Java application.
- Building CLI wrapper tools, build systems, or CI orchestration logic in Java.
- Capturing or piping process output for further processing within the JVM.

**Advantages**
- `ProcessBuilder` provides a fluent, mutable configuration object (command, working directory, environment variables, I/O redirection) reused across multiple `start()` calls, cleaner than juggling `Runtime.exec` overloads.
- `redirectInput/Output/Error` (including `Redirect.INHERIT`) allows efficient direct OS-level piping without the JVM copying bytes through Java-level streams.
- `Process.onExit()` (Java 9+) returns a `CompletableFuture<Process>`, integrating process completion into async/reactive pipelines.

**Limitations**
- Failing to drain a child process's stdout/stderr streams promptly can deadlock the child process if its OS pipe buffer fills up (a classic, frequently-tested pitfall) — must consume both streams (e.g. on separate threads) or redirect them, especially when also writing to stdin.
- No built-in cross-platform shell interpretation — `ProcessBuilder` executes a program directly (argv array), not a shell command line; shell features (pipes, globbing, env var expansion) require explicitly invoking a shell (`sh -c "..."` / `cmd /c "..."`).
- Process creation is relatively expensive (OS-level fork/exec) — not suitable for high-frequency, latency-sensitive invocation.
- Zombie/orphan processes can occur if a `Process` is not properly waited on or destroyed, particularly in long-running server applications spawning many children.

### Internal Working

**Step-by-Step Explanation**
1. `new ProcessBuilder(command...)` configures the command array, optionally working directory (`directory()`), environment (`environment()` map, inherited by default from the JVM's process environment), and I/O redirection (`redirectInput/Output/Error`, or `redirectErrorStream(true)` to merge stderr into stdout).
2. `start()` triggers a native call that forks/execs (POSIX) or calls `CreateProcess` (Windows), setting up OS pipes for any non-inherited/non-redirected streams.
3. The returned `Process` object exposes `getOutputStream()` (child's stdin, from the parent's perspective an `OutputStream` to write to), `getInputStream()` (child's stdout), and `getErrorStream()` (child's stderr), unless redirected, in which case those streams are simplified/inherited.
4. The parent must actively read `getInputStream()`/`getErrorStream()` (typically on separate threads) to avoid the child blocking when its OS pipe buffer fills — a full buffer with no reader causes the child to stall on its own writes.
5. `waitFor()` blocks until the process terminates (or a timeout elapses, in the overloaded form), returning/consuming the exit code; `exitValue()` throws `IllegalThreadStateException` if called while still running. `destroy()`/`destroyForcibly()` (SIGTERM/SIGKILL-equivalent) terminate the process; `Process.onExit()` (Java 9+) gives a `CompletableFuture<Process>` for async completion handling.

**Memory Layout**
Not directly applicable to JVM heap/stack in the usual sense — a `Process` represents a separate OS process with its own independent address space, heap, and stack, entirely outside the JVM's memory management; the parent JVM only holds lightweight handle/stream objects referencing OS-level pipe file descriptors.

**Diagrams**
```
Parent JVM Process                         Child OS Process
┌─────────────────────┐                    ┌─────────────────────┐
│ ProcessBuilder       │  fork/exec via JNI │                     │
│   .start() ──────────┼───────────────────►│  argv, env, cwd     │
│                       │                    │                     │
│ Process object        │◄── stdout pipe ───┤  stdout              │
│  .getInputStream()    │                    │                     │
│  .getOutputStream() ──┼─── stdin pipe ────►│  stdin               │
│  .getErrorStream()    │◄── stderr pipe ───┤  stderr              │
│  .waitFor()            │◄── exit code ─────┤  process exit        │
└─────────────────────┘                    └─────────────────────┘
```

**JVM Behaviour**
Process creation goes through native code (`ProcessImpl` on each platform) invoking OS system calls outside normal Java bytecode execution — this is a genuine OS-level context switch/new process, not a JVM thread; file descriptor limits, OS process limits, and OS scheduling all apply. The JVM must also reap terminated child processes (via `waitpid`-equivalent calls internally) to avoid leaving zombie processes on POSIX systems if `waitFor()`/`exitValue()` is never called.

### Interview Questions

**Basic**
1. What is the difference between `Runtime.exec()` and `ProcessBuilder`?
2. How do you capture the standard output of a launched process in Java?
3. What does `Process.waitFor()` do, and what does it return?

**Intermediate**
4. Why can a child process hang indefinitely if you don't read its stdout/stderr streams?
5. How do you merge a child process's stderr into its stdout stream?
6. How would you run a shell command with pipes (e.g. `ps aux | grep java`) using `ProcessBuilder`?

**Advanced**
7. How does `Process.onExit()` integrate with `CompletableFuture`-based asynchronous workflows?
8. What is `ProcessHandle`, and how does it differ from `Process` (introduced in which Java version, and why)?
9. How do you implement a timeout for a process that might hang, ensuring it's forcibly terminated and its resources cleaned up?

**Scenario-based**
10. A Java service spawns an external image-conversion tool per uploaded file and occasionally hangs under load with high CPU/thread usage. Diagnose likely causes and propose a robust fix.

### Detailed Answers

1. **Difference between `Runtime.exec()` and `ProcessBuilder`?**
   `Runtime.exec()` offers several overloaded methods taking a command string or array, with limited configurability (environment as a separate array parameter, no persistent configuration object, string-based overloads prone to incorrect manual argument splitting). `ProcessBuilder` is the modern replacement: a mutable, reusable builder object exposing `command()`, `directory()`, `environment()` (a live `Map` view), and `redirectInput/Output/Error()`, letting you configure a process launch once and `start()` it (potentially multiple times) cleanly. `Runtime.exec()` internally delegates to `ProcessBuilder` in modern JDKs.

2. **How to capture a launched process's standard output?**
   Call `process.getInputStream()` (this is the child's stdout from the parent's perspective) and read from it, typically wrapping in a `BufferedReader`, ideally on a dedicated thread (or using `process.getInputStream().readAllBytes()` for simple cases) to avoid blocking if the child produces a lot of output while the parent is also waiting on `waitFor()`.

3. **What does `Process.waitFor()` do, and what does it return?**
   It blocks the calling thread until the underlying native process terminates, then returns the process's exit code (`int`) — conventionally `0` for success, non-zero for failure/error, though the exact meaning of non-zero codes is application-defined. An overloaded `waitFor(long timeout, TimeUnit unit)` returns `boolean` (whether the process exited within the timeout) without blocking indefinitely.

4. **Why can a child hang if you don't read its stdout/stderr?**
   OS pipes connecting parent and child have a finite kernel buffer. If the child writes more output than fits in that buffer and nothing on the parent side is reading, the child's write() call blocks (the OS pipe is full), stalling the child indefinitely — and if the parent is meanwhile blocked in `waitFor()` waiting for the child to exit (which it never will, since it's stuck writing), you get a classic deadlock. The fix: always drain both `getInputStream()` and `getErrorStream()` concurrently (e.g. via dedicated reader threads or `redirectErrorStream(true)` plus one reader thread) before/while calling `waitFor()`.

5. **How to merge stderr into stdout?**
   Call `processBuilder.redirectErrorStream(true)` before `start()` — this causes the child's stderr to be combined into the same stream as stdout, so `process.getErrorStream()` becomes effectively unused (an already-closed/empty stream) and only `process.getInputStream()` needs to be read, simplifying stream-draining logic to a single reader thread.

6. **How to run a shell command with pipes using `ProcessBuilder`?**
   `ProcessBuilder` invokes a program directly with an argv array — it does not interpret shell syntax like `|`. To use shell features, explicitly invoke the shell: `new ProcessBuilder("sh", "-c", "ps aux | grep java")` on POSIX systems, or `new ProcessBuilder("cmd", "/c", "tasklist | findstr java")` on Windows — the shell itself parses and executes the pipeline; `ProcessBuilder` just launches the shell as the "program."

7. **How does `Process.onExit()` integrate with `CompletableFuture` workflows?**
   `Process.onExit()` (Java 9+) returns a `CompletableFuture<Process>` that completes when the process terminates, backed internally by a JDK-managed process-reaper thread pool rather than requiring the caller to block a dedicated thread in `waitFor()`. This lets you compose process completion with `thenApply`/`thenCompose`/`exceptionally` chains, combine it with timeouts (`orTimeout`), or run multiple external processes concurrently and react to whichever completes, fitting naturally into asynchronous/reactive service code.

8. **What is `ProcessHandle`, and how does it differ from `Process`?**
   `ProcessHandle` (Java 9+, JEP 102) is a lighter-weight, more introspective API for referring to *any* native process — not just ones started by the current JVM — providing access to PID, parent/children/descendant processes (`ProcessHandle.parent()`, `children()`), `ProcessHandle.Info` (command, arguments, start time, CPU time owner), and `ProcessHandle.current()`/`allProcesses()` for enumerating the whole system's process tree (subject to OS permissions). `Process` (the older API) is specific to child processes launched via `ProcessBuilder`/`Runtime.exec`, focused on I/O streams and exit codes rather than broad OS process introspection; `Process.toHandle()` bridges the two, and `Process` now extends functionality via default methods delegating to `ProcessHandle`.

9. **How to implement a timeout with forced termination and cleanup?**
   Use `process.waitFor(timeout, TimeUnit)`; if it returns `false` (still running), call `process.destroyForcibly()` (sends a forceful kill, e.g. `SIGKILL` on POSIX) and then call `process.waitFor()` again (or check `isAlive()`) to confirm termination before proceeding, ensuring associated stream-reader threads are also interrupted/joined and their streams closed to avoid resource/thread leaks; `Process.onExit().orTimeout(...)` combined with `.exceptionally(ex -> { process.destroyForcibly(); return process; })` offers an async-friendly equivalent.

10. **Diagnose and fix a hanging external image-conversion process under load.**
    Likely causes: (a) the parent isn't draining the child's stdout/stderr concurrently, so under higher-resolution/larger images producing more log output, the OS pipe buffer fills and the child blocks — classic pipe-deadlock, worse under load because more data is produced; (b) no timeout is enforced, so a genuinely stuck/slow conversion accumulates indefinitely-blocked threads, exhausting a thread pool; (c) too many concurrent process launches overwhelm OS process/file-descriptor limits. Fix: always spawn dedicated stream-reader threads (or `redirectErrorStream(true)` + one reader, or redirect output to a file via `redirectOutput(File)` if the output isn't needed in-process) for every launched process; enforce a `waitFor(timeout, unit)` with `destroyForcibly()` fallback; and bound concurrency (e.g. a semaphore or fixed-size executor) to cap the number of simultaneously running child processes relative to available OS resources.

### Code Examples

```java
import java.io.*;
import java.util.concurrent.*;

public class ProcessBuilderDemo {
    public static void main(String[] args) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("sh", "-c", "echo start; sleep 1; echo done; ls /nonexistent");
        pb.redirectErrorStream(true); // merge stderr into stdout to simplify draining

        Process process = pb.start();

        // Drain output on a separate thread to avoid pipe-buffer deadlock
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> outputFuture = executor.submit(() -> {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append(System.lineSeparator());
                }
            }
            return sb.toString();
        });

        boolean finished = process.waitFor(5, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
        }
        System.out.println("Exit code: " + process.exitValue());
        System.out.println("Output:\n" + outputFuture.get());
        executor.shutdown();

        // Modern async completion handling via Process.onExit()
        Process asyncProcess = new ProcessBuilder("sh", "-c", "sleep 1; echo async-done").start();
        asyncProcess.onExit().thenAccept(p -> System.out.println("Async process exited with code " + p.exitValue()));
    }
}
```

## System Properties & Environment Variables

### Theory

**Core Concepts**
System properties (`System.getProperty`/`setProperty`, backed by a `Properties` object accessible via `System.getProperties()`) are JVM-scoped, mutable key/value configuration values — some set by the JVM itself at startup (`java.version`, `os.name`, `user.home`, `file.separator`, `java.class.path`), others supplied via `-Dkey=value` command-line flags or set programmatically at runtime. Environment variables (`System.getenv()`/`System.getenv(String)`) are OS-level key/value pairs inherited from the process that launched the JVM (shell, container runtime, CI system), exposed to Java as a **read-only, immutable snapshot** taken at JVM startup.

**Internal Working**
System properties live in a `Properties` (a `Hashtable` subclass) instance held by the `System` class, mutable via `setProperty`/`clearProperty`, and consulted by many JDK/library facilities (e.g. `java.io.tmpdir`, `line.separator`, locale defaults, proxy settings) as global configuration. Environment variables are read once, at JVM startup, from the OS process environment block (`environ` on POSIX, or the Windows process environment) via native code, then copied into an internal unmodifiable `Map<String,String>` returned by `System.getenv()` — changes to the OS environment after JVM startup, or via other processes, are never reflected.

**When to Use It**
- System properties: JVM/application-level configuration toggles set at launch (`-Dspring.profiles.active=prod`), feature flags read at startup, or standard JDK behavior queries (OS detection via `os.name`, temp directory via `java.io.tmpdir`).
- Environment variables: configuration conventionally supplied by the deployment environment/container/orchestrator (e.g. `DATABASE_URL`, `PATH`, secrets injected by Kubernetes/Docker) — the "twelve-factor app" convention favors environment variables for environment-specific config over baked-in files.

**Advantages**
- System properties are mutable at runtime (`System.setProperty`), useful for tests or dynamic reconfiguration; environment variables provide a standard, language-agnostic, container/orchestrator-friendly configuration channel shared across all processes in a deployment.
- Both are simple, ubiquitous, zero-dependency mechanisms requiring no additional configuration-file parsing.

**Limitations**
- System properties are JVM-global mutable state — like `Locale.setDefault()`, indiscriminate `System.setProperty()` calls from library code can cause action-at-a-distance bugs affecting unrelated code sharing the same JVM.
- Environment variables are immutable once the JVM starts (`System.getenv()` reflects only the startup-time snapshot) — you cannot use `System.getenv()` to pick up environment changes made after the JVM launched, unlike system properties which can be changed live.
- Environment variable names are conventionally uppercase-with-underscores and may differ in casing/availability across OSes (Windows environment variable names are case-insensitive; POSIX are case-sensitive) — code assuming a specific casing can be non-portable.
- Storing secrets in either mechanism has security implications: system properties can appear in process listings (`ps -ef` may show `-D` flags) and are visible via `System.getProperties()`/heap dumps to any code in the same JVM; environment variables are visible to child processes and, on some systems, via `/proc/<pid>/environ`.

### Internal Working

**Step-by-Step Explanation**
1. At JVM startup, the launcher populates initial system properties (OS/JVM/user info) and applies any `-Dkey=value` command-line arguments into the shared `Properties` object.
2. `System.getProperty("key")` performs a straightforward `Hashtable` lookup (thread-safe due to `Hashtable`'s internal synchronization) against this shared, mutable object; `System.setProperty("key", "value")` mutates it, visible immediately to any subsequent reader in any thread of the same JVM.
3. Separately, at JVM startup, native launcher code reads the OS environment block once and populates an internal, unmodifiable map.
4. `System.getenv()` returns this unmodifiable map directly (defensive copy is unnecessary since it's immutable); `System.getenv("KEY")` performs a lookup against it. No JVM API allows setting/mutating this map at runtime (there is no supported public `System.setenv`) — the correct way to change an environment variable's effective value is to set it before the JVM starts, or use a system property/config file instead for values needing runtime mutability.
5. `ProcessBuilder.environment()` returns a mutable copy of the current process's environment (seeded from `System.getenv()`) that can be modified before `start()`, to control the environment passed specifically to a *child* process — but this doesn't affect the parent JVM's own `System.getenv()` view.

**Memory Layout**
Not directly applicable to heap generation distinctions — both are simple, small, long-lived key/value maps typically promoted to the old generation early and rarely garbage collected; there's no special JVM memory region for them (they're ordinary heap objects held as static state within the `System` class).

**Diagrams**
```
OS process environment (immutable snapshot at JVM start)
        │
        ▼
System.getenv() ──► unmodifiable Map<String,String>  (never changes during JVM lifetime)

-Dkey=value CLI flags + JVM startup defaults + System.setProperty() calls
        │
        ▼
System.getProperties() ──► mutable Properties (Hashtable)  (can change any time, any thread)
```

**JVM Behaviour**
Reading environment variables/system properties involves no bytecode-level special handling beyond ordinary field/map access; the initial population happens in native launcher code before `main()` runs, meaning by the time any Java code executes, both maps are already fully initialized and available.

### Interview Questions

**Basic**
1. What is the difference between a system property and an environment variable in Java?
2. How do you set a system property from the command line, and how do you read it in code?
3. Can you modify environment variables from within a running Java program?

**Intermediate**
4. Why might `System.getProperty("os.name")` be used for platform-specific logic, and what are its pitfalls?
5. How would you pass a different environment to a child process launched via `ProcessBuilder` without affecting the parent JVM's environment?
6. What security concerns exist around storing secrets in system properties vs. environment variables?

**Advanced**
7. Why is `System.getenv()` immutable while `System.getProperties()` is mutable, and what design rationale explains this?
8. How do containerized deployments (Docker/Kubernetes) typically leverage environment variables for configuration, and how should a Spring Boot-style application consume them?
9. How would you design a configuration-loading strategy that layers system properties, environment variables, and config files with a defined precedence order?

**Scenario-based**
10. A production incident occurs because a `-Dapp.env=staging` flag was accidentally included in a production JVM's startup script, causing it to load staging configuration. How would you redesign the configuration strategy to prevent this class of error?

### Detailed Answers

1. **Difference between a system property and an environment variable?**
   System properties are JVM-internal, mutable key/value configuration (`System.getProperty`/`setProperty`), settable via `-Dkey=value` at launch or programmatically at runtime, visible only within that JVM instance. Environment variables are OS-level key/value pairs inherited from the parent process at JVM startup, exposed read-only via `System.getenv()`, and shared with (visible to) any child processes the JVM spawns, not just Java-specific configuration.

2. **How to set a system property from the command line and read it in code?**
   `java -Dapp.mode=debug -jar app.jar`, then in code: `String mode = System.getProperty("app.mode", "default-value");` (the second argument is an optional default if unset).

3. **Can you modify environment variables from within a running Java program?**
   Not officially/portably for the current JVM's own environment — there is no supported public API like `System.setenv()`. (Reflection-based hacks against internal `ProcessEnvironment` fields exist but are fragile, unsupported, and blocked or broken by module strong encapsulation in modern JDKs.) You *can*, however, control the environment passed to a **child** process by mutating the `Map` returned by `processBuilder.environment()` before calling `start()` — that only affects the child, not the parent JVM.

4. **Why use `System.getProperty("os.name")` for platform logic, and pitfalls?**
   It's the standard, dependency-free way to branch OS-specific logic (e.g. choosing a path separator behavior, native library extension, or shell interpreter). Pitfalls: the returned string format is OS/version-dependent and not perfectly standardized (e.g. "Windows 10", "Windows 11", "Mac OS X", "Linux" — exact strings can change across JDK/OS versions), so code should use case-insensitive substring checks (`os.name`.toLowerCase().contains("win")`) rather than exact-string equality, and prefer higher-level abstractions (`java.nio.file.Path`, `File.separator`) over manual OS-name branching wherever possible.

5. **How to pass a different environment to a child `ProcessBuilder` process without affecting the parent?**
   `Map<String,String> env = processBuilder.environment();` returns a mutable copy seeded from the current process's environment; call `env.put("KEY", "value")` or `env.remove("KEY")` on it before calling `processBuilder.start()`. This modifies only the environment map that will be handed to the *child* process; the parent JVM's own `System.getenv()` view remains completely unaffected since it's a separate, immutable snapshot.

6. **Security concerns: secrets in system properties vs. environment variables?**
   Both can leak: system properties set via `-Dsecret=...` may appear in process listing tools (`ps -ef`/`ps aux` on some systems show full command lines) and are readable by any code in the same JVM via `System.getProperties()` (including third-party libraries, which could inadvertently log them, or via heap/thread dumps). Environment variables are visible to all child processes (which may log them or pass them further) and, on Linux, can be read by any process with sufficient permissions via `/proc/<pid>/environ`. Best practice: avoid passing secrets via either mechanism when possible — prefer dedicated secret managers (Vault, AWS Secrets Manager, Kubernetes Secrets mounted as files with restricted permissions) instead.

7. **Why is `System.getenv()` immutable while `System.getProperties()` is mutable?**
   Environment variables model OS-level process state, which — following POSIX/OS semantics — is fixed for the lifetime of a process (a process cannot change its own effective environment as seen by an in-process API in a way that's meaningfully "live" across languages/tools); the JDK models this faithfully by taking one immutable snapshot at startup, avoiding the complexity/inconsistency of pretending to support live OS environment mutation that the underlying platform doesn't cleanly support. System properties, by contrast, are a purely JVM-internal construct with no OS-level equivalent constraint, so the JDK made them mutable to support runtime configuration flexibility (e.g. tests dynamically toggling behavior via `System.setProperty`).

8. **How do containerized deployments use environment variables, and how should apps consume them?**
   Docker/Kubernetes conventionally inject configuration (database URLs, feature flags, secrets via mounted env from `Secret`/`ConfigMap` objects) as environment variables set on the container process, following twelve-factor-app principles so the same container image can run unmodified across environments (dev/staging/prod) by varying only its environment. Spring Boot-style applications consume these via a layered `Environment` abstraction (`@Value("${DATABASE_URL}")`, `application.yml` placeholders `${DATABASE_URL}`, or `Environment.getProperty()`), which transparently checks environment variables (with relaxed binding, e.g. `DATABASE_URL` maps to `database.url`) alongside system properties and config files, giving a unified configuration model with defined precedence.

9. **How to design a layered config-loading strategy with defined precedence?**
   A common, well-tested precedence (highest to lowest): (1) command-line arguments / explicit `-D` system properties (for ad-hoc overrides), (2) environment variables (deployment-specific, container-injected), (3) profile-specific config files (`application-prod.yml`), (4) base config file (`application.yml`), (5) hardcoded defaults in code. Implement by loading each layer into a `Map`/`Properties`-like structure and merging with later (lower-precedence) layers only filling in keys not already set by an earlier (higher-precedence) layer — frameworks like Spring's `Environment`/`PropertySource` chain or libraries like Typesafe Config implement exactly this pattern; document the precedence clearly so operators know which layer "wins" for a given key.

10. **Redesign to prevent accidental `-Dapp.env=staging` reaching production.**
    Root cause: environment-selection was controlled by an easily-copy-pasted, error-prone command-line flag with no validation guardrail. Redesign options: (a) derive the environment from a more "sticky", deployment-pipeline-controlled source that's harder to accidentally carry over — e.g. an environment variable injected exclusively by the deployment orchestrator/CI pipeline per environment (not manually maintained scripts), or better, from infrastructure metadata (cloud instance tags, Kubernetes namespace) rather than a copy-pasteable flag; (b) add a startup validation/fail-fast check that cross-verifies the declared environment against an independent signal (e.g. hostname pattern, a required-only-in-prod secret) and refuses to start (or loudly alarms) on mismatch; (c) treat environment-selection configuration as infrastructure-as-code (version-controlled, reviewed) rather than manually edited startup scripts, reducing the chance of stale/copy-pasted flags surviving a promotion to production.

### Code Examples

```java
import java.util.*;

public class SystemPropertiesEnvDemo {
    public static void main(String[] args) throws Exception {
        // Reading standard JVM-provided system properties
        System.out.println("Java version: " + System.getProperty("java.version"));
        System.out.println("OS name: " + System.getProperty("os.name"));
        System.out.println("Temp dir: " + System.getProperty("java.io.tmpdir"));

        // Custom system property with a fallback default, mutable at runtime
        String appMode = System.getProperty("app.mode", "production");
        System.out.println("App mode: " + appMode);
        System.setProperty("app.mode", "debug"); // visible immediately to any subsequent reader in this JVM
        System.out.println("App mode after override: " + System.getProperty("app.mode"));

        // Environment variables: read-only snapshot from JVM startup
        Map<String, String> env = System.getenv();
        String path = env.get("PATH");
        System.out.println("PATH starts with: " + (path != null ? path.substring(0, Math.min(40, path.length())) : "N/A"));

        // Passing a customized environment to a CHILD process only (parent's System.getenv() is untouched)
        ProcessBuilder pb = new ProcessBuilder("sh", "-c", "echo CUSTOM_VAR=$CUSTOM_VAR");
        pb.environment().put("CUSTOM_VAR", "child-only-value");
        Process p = pb.start();
        p.getInputStream().transferTo(System.out);
        p.waitFor();
    }
}
```

## `Runtime` Class

### Theory

**Core Concepts**
`java.lang.Runtime` is a singleton (one instance per JVM, obtained via `Runtime.getRuntime()`) providing access to JVM-level operational facilities: memory statistics (`totalMemory`, `freeMemory`, `maxMemory`), garbage collection hints (`gc()`), shutdown hook registration (`addShutdownHook`), available processor count (`availableProcessors()`), and legacy process launching (`exec(...)`, superseded by `ProcessBuilder`). It represents the "environment in which the application is running" as opposed to `System`, which is a related facade offering similar and additional facilities (many `System` methods actually delegate to `Runtime` internally, e.g. `System.gc()` calls `Runtime.getRuntime().gc()`).

**Internal Working**
`Runtime` is implemented as a classic singleton with a private constructor and a static `currentRuntime` field returned by `getRuntime()`; its memory-related methods query the JVM's internal heap management subsystem (the active garbage collector's memory pools) to report current/max/free memory, while `addShutdownHook`/`removeShutdownHook` register `Thread` objects with an internal JVM shutdown-sequence list that is invoked (in unspecified order, potentially concurrently) when the JVM begins an orderly shutdown.

**When to Use It**
- Registering cleanup logic (closing resources, flushing logs, releasing locks) to run on JVM shutdown via `addShutdownHook`.
- Querying available processors (`availableProcessors()`) to size thread pools appropriately for the host machine.
- Reporting heap memory diagnostics (`totalMemory`/`freeMemory`/`maxMemory`) for monitoring/health-check endpoints.
- Legacy code still calling `Runtime.getRuntime().exec(...)` (new code should prefer `ProcessBuilder`).

**Advantages**
- Provides direct, dependency-free access to JVM-level operational information without needing external monitoring libraries for basic diagnostics.
- Shutdown hooks provide a reliable (though not 100% guaranteed under all termination scenarios) mechanism for graceful cleanup on normal JVM exit or external termination signals (e.g. `SIGTERM`, `Ctrl+C`).

**Limitations**
- `Runtime.gc()` is only a *hint* to the JVM to consider running garbage collection — the JVM is free to ignore it entirely (unlike `System.gc()`'s conceptually identical, equally unreliable behavior); relying on it for deterministic memory reclamation is a well-known anti-pattern.
- Shutdown hooks are **not guaranteed to run** if the JVM is killed abruptly (`kill -9`/`SIGKILL`, power loss, `Runtime.halt()`), and hooks that throw exceptions or run indefinitely can delay or complicate shutdown; multiple hooks run concurrently with no guaranteed ordering, so they must not depend on each other's side effects.
- `Runtime.exec(...)` overloads are largely superseded by `ProcessBuilder` and are considered legacy — string-command overloads are especially error-prone (naive whitespace-based argument splitting).
- Memory figures (`totalMemory`/`freeMemory`) reflect the JVM's currently *allocated* heap from the OS, not necessarily the true "used" application memory, and can be misleading without understanding generational GC behavior (e.g. `freeMemory` can look artificially low right before a GC reclaims a lot of garbage).

### Internal Working

**Step-by-Step Explanation**
1. `Runtime.getRuntime()` returns the single per-JVM `Runtime` instance (constructed once during JVM bootstrap).
2. `runtime.availableProcessors()` queries the OS/JVM for the number of processors available to the JVM (which can change at runtime in containerized environments with dynamic CPU allocation — hence why this method's value should not be cached forever in long-running containerized applications).
3. `runtime.totalMemory()`/`freeMemory()`/`maxMemory()` consult the active garbage collector's heap accounting: `totalMemory()` is the heap currently committed from the OS, `maxMemory()` is the configured upper bound (`-Xmx`), and `freeMemory()` is the portion of `totalMemory()` not currently occupied by live+garbage objects (an approximate, GC-cycle-dependent figure).
4. `runtime.addShutdownHook(thread)` registers a `Thread` object (not yet started) into an internal JVM-managed list; when JVM shutdown begins (via normal `main` completion, explicit `System.exit()`, or external interrupt signal handling), the JVM starts all registered hook threads (concurrently, unspecified order), then waits for them all to finish (unless `Runtime.halt()` is called, which skips hooks entirely) before actually terminating the process.
5. `runtime.gc()` invokes the JVM's garbage collector as a suggestion — the actual GC implementation decides whether/when to honor this (modern low-pause collectors like G1/ZGC/Shenandoah may choose to ignore or heavily deprioritize explicit `gc()` calls).

**Memory Layout**
`Runtime`'s memory-reporting methods directly reflect heap layout: `maxMemory()` corresponds to the `-Xmx`-bounded total heap ceiling (young + old generation combined, under the default generational collectors); `totalMemory()` is the currently committed portion of that ceiling (may grow as the heap expands toward `-Xmx`); `freeMemory()` is unused space within the currently committed region. Metaspace (class metadata) is tracked separately and is **not** reflected in these heap-focused figures — `java.lang.management.MemoryMXBean`/`MemoryPoolMXBean` provide more granular, generation-aware statistics (including Metaspace) than `Runtime`'s coarse-grained methods.

**Diagrams**
```
                     Runtime.getRuntime()  (singleton)
                              │
       ┌──────────────────────┼───────────────────────┐
       ▼                      ▼                       ▼
 availableProcessors()   totalMemory()/freeMemory()   addShutdownHook(thread)
       │                 /maxMemory()                     │
       ▼                      ▼                       ▼
 sized thread pools      heap diagnostics          registered in JVM shutdown
                                                      hook list
                                                          │
                                             JVM shutdown begins (normal exit,
                                             System.exit(), or SIGTERM handling)
                                                          │
                                                          ▼
                                          all hooks started concurrently, JVM
                                          waits for all to finish, then exits
                                          (skipped entirely by Runtime.halt())
```

**JVM Behaviour**
Shutdown hooks run as real JVM threads participating in the normal shutdown sequence (finalizers, if enabled, and hook threads); if a hook thread hangs indefinitely, JVM shutdown can hang indefinitely too (there's no default hook timeout), which is a common production gotcha — hooks should have bounded, fast cleanup logic or enforce their own internal timeouts. `Runtime.gc()` calls flow into whatever GC algorithm is configured (Serial/Parallel/G1/ZGC/Shenandoah), each of which independently decides whether to actually perform a full collection in response.

### Interview Questions

**Basic**
1. How do you obtain a `Runtime` instance, and why is it a singleton?
2. What is the difference between `Runtime.totalMemory()`, `freeMemory()`, and `maxMemory()`?
3. What does `Runtime.getRuntime().gc()` actually guarantee?

**Intermediate**
4. How do you register a JVM shutdown hook, and what are its limitations?
5. Why shouldn't you cache the result of `availableProcessors()` permanently in a containerized environment?
6. What is the relationship between `System.gc()`/`System.exit()` and their `Runtime` equivalents?

**Advanced**
7. Under what circumstances will registered shutdown hooks NOT run?
8. How would you use `MemoryMXBean`/`MemoryPoolMXBean` instead of `Runtime` for more precise memory diagnostics, and why might you prefer them?
9. What are the risks of a slow or hanging shutdown hook in a production service, and how would you mitigate them?

**Scenario-based**
10. Your Kubernetes-deployed service needs to flush an in-memory metrics buffer and close a database connection pool gracefully on pod termination (`SIGTERM`), within Kubernetes's default grace period. Design the shutdown handling using `Runtime`.

### Detailed Answers

1. **How to obtain a `Runtime` instance, and why singleton?**
   Via the static factory `Runtime.getRuntime()`; there is exactly one `Runtime` object per JVM because it represents JVM-wide operational state (heap configuration, shutdown sequence, processor count) that is inherently a property of the single running JVM process, not something meaningfully instantiable per-object — a private constructor prevents external instantiation.

2. **Difference between `totalMemory()`, `freeMemory()`, and `maxMemory()`?**
   `maxMemory()` is the maximum heap size the JVM is configured to grow to (`-Xmx`, or a platform default). `totalMemory()` is the heap size currently committed/allocated from the OS (can be less than `maxMemory()` and grows toward it as needed). `freeMemory()` is the amount of the currently committed (`totalMemory()`) heap not occupied by live or as-yet-uncollected garbage objects — it's an approximate figure that changes significantly around GC cycles.

3. **What does `Runtime.getRuntime().gc()` actually guarantee?**
   Nothing guaranteed — it is merely a hint/suggestion to the JVM that now might be a good time to perform garbage collection; the JVM specification explicitly permits the implementation to do nothing in response. Modern low-pause collectors often deprioritize or reinterpret explicit `gc()` requests. It should never be relied upon for deterministic resource reclamation (e.g. don't call it expecting finalizers/cleaners to run predictably).

4. **How to register a shutdown hook, and its limitations?**
   `Runtime.getRuntime().addShutdownHook(new Thread(() -> { /* cleanup */ }));` — pass an unstarted `Thread`. Limitations: hooks are not guaranteed to run if the JVM is killed forcibly (`kill -9`, `Runtime.halt()`, abrupt power loss); multiple hooks execute concurrently with no defined ordering, so hooks must be independent and not assume another hook has/hasn't run yet; hooks that throw uncaught exceptions don't stop other hooks but the exception is essentially swallowed (printed via default uncaught exception handling); a slow/hanging hook delays JVM termination indefinitely since there's no built-in timeout.

5. **Why not permanently cache `availableProcessors()` in containers?**
   In containerized/cloud environments, CPU allocation (cgroup CPU quota/shares) can change dynamically (e.g. vertical autoscaling, Kubernetes resizing), and `availableProcessors()` reflects the JVM's current view of available CPUs, which the JDK has, since Java 10ish improvements to container-awareness, made responsive to cgroup limits rather than only host-level core counts — code that caches this value once at startup into a static field could end up sizing thread pools incorrectly if the container's CPU allocation is later changed without a JVM restart.

6. **Relationship between `System.gc()`/`System.exit()` and `Runtime` equivalents?**
   `System.gc()` is simply a convenience delegation to `Runtime.getRuntime().gc()`. `System.exit(status)` terminates the JVM by (internally) invoking the shutdown sequence — running all registered shutdown hooks, then (if security manager/shutdown state permits) actually halting — conceptually related to, though not a direct passthrough of, `Runtime.exit(status)` (in fact, `System.exit` calls `Runtime.getRuntime().exit(status)` internally); `Runtime.halt(status)` is a more abrupt variant that skips shutdown hooks and finalization entirely.

7. **Under what circumstances will registered shutdown hooks NOT run?**
   When the JVM is terminated abruptly without going through its normal shutdown sequence: `kill -9`/`SIGKILL` (cannot be intercepted by any process), `Runtime.getRuntime().halt(status)` (explicitly bypasses hooks), abrupt host power loss/crash, or a fatal JVM crash (native segfault, `OutOfMemoryError` in some catastrophic native-level cases) that prevents orderly shutdown. Hooks *do* generally run for normal `main()` completion, explicit `System.exit()`, and typically for `SIGTERM`/`SIGINT` (`Ctrl+C`) on POSIX systems, since the JVM installs signal handlers for those that trigger orderly shutdown.

8. **Why prefer `MemoryMXBean`/`MemoryPoolMXBean` over `Runtime` for diagnostics?**
   `Runtime`'s memory methods give only coarse, whole-heap figures (total/free/max), with no visibility into individual generations (young/old) or Metaspace/non-heap pools. `ManagementFactory.getMemoryMXBean()` exposes separate `MemoryUsage` for heap vs. non-heap memory, and `getMemoryPoolMXBeans()` gives per-pool detail (Eden, Survivor, Old Gen, Metaspace, Code Cache, etc.), including peak usage and (for many pools) usage thresholds/notifications — essential for building accurate monitoring/alerting rather than `Runtime`'s single aggregate heap snapshot.

9. **Risks of a slow/hanging shutdown hook, and mitigation?**
   A hanging hook can delay or entirely block JVM termination (no default timeout exists), which in orchestrated environments (Kubernetes, systemd) risks the platform's own grace period expiring and issuing a forceful `SIGKILL`, which then skips any *remaining* unfinished cleanup (including other hooks) entirely — potentially worse than not having a graceful hook at all if it consumes most of the grace period without completing meaningful work. Mitigation: keep hook logic fast and bounded; wrap cleanup steps in their own timeouts (e.g. `ExecutorService.awaitTermination(timeout, unit)`); avoid hooks that depend on other threads/hooks; log clearly at hook start/end for observability; and size the platform's grace period (e.g. Kubernetes `terminationGracePeriodSeconds`) generously enough for the hook's expected worst-case duration.

10. **Design graceful shutdown for a Kubernetes pod handling `SIGTERM` within the grace period.**
    Register a single shutdown hook via `Runtime.getRuntime().addShutdownHook(new Thread(this::gracefulShutdown))` where `gracefulShutdown()`: (a) flips an `AtomicBoolean shuttingDown` flag so health/readiness probes immediately start reporting "not ready" (causing Kubernetes to stop routing new traffic to the pod, if not already draining); (b) flushes the in-memory metrics buffer synchronously with a bounded timeout; (c) calls the database connection pool's `close()`/`shutdown()` method, again with a bounded timeout via an `ExecutorService.awaitTermination`; (d) logs completion. The whole hook's total expected duration should be comfortably under the Kubernetes pod's `terminationGracePeriodSeconds` (increase that value if the cleanup genuinely needs more time), and each step should have its own internal timeout so a single slow step (e.g. a stuck DB connection close) can't consume the entire grace period and risk a forceful `SIGKILL` truncating the remaining cleanup steps.

### Code Examples

```java
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class RuntimeDemo {
    private static final AtomicBoolean shuttingDown = new AtomicBoolean(false);

    public static void main(String[] args) throws InterruptedException {
        Runtime runtime = Runtime.getRuntime();

        // Size a thread pool based on available processors (re-checked, not permanently cached across long uptimes)
        int workers = Math.max(2, runtime.availableProcessors());
        ExecutorService pool = Executors.newFixedThreadPool(workers);
        System.out.println("Sized thread pool with " + workers + " workers");

        // Basic heap diagnostics
        System.out.printf("Max: %dMB, Total: %dMB, Free: %dMB%n",
                runtime.maxMemory() / (1024 * 1024),
                runtime.totalMemory() / (1024 * 1024),
                runtime.freeMemory() / (1024 * 1024));

        // Graceful shutdown hook: flip a flag, drain work, close resources within a bounded timeout
        runtime.addShutdownHook(new Thread(() -> {
            shuttingDown.set(true);
            System.out.println("Shutdown hook: draining in-flight work...");
            pool.shutdown();
            try {
                if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.out.println("Timed out waiting for tasks; forcing shutdown");
                    pool.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                pool.shutdownNow();
            }
            System.out.println("Shutdown hook: cleanup complete");
        }, "graceful-shutdown-hook"));

        pool.submit(() -> {
            while (!shuttingDown.get()) {
                // simulate periodic background work
                try { Thread.sleep(200); } catch (InterruptedException ignored) { return; }
            }
        });

        System.out.println("Application running. Press Ctrl+C to trigger graceful shutdown.");
        Thread.sleep(1000); // demo: exit shortly after start instead of running forever
    }
}
```
