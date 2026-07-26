# 12. File I/O

## File API

### Theory

- **Core Concepts**: `java.io.File` (since Java 1.0) is an abstract representation of a file or directory *pathname* — it does **not** represent the file's content, and importantly, a `File` object can validly refer to a path that doesn't exist on disk at all. It provides metadata operations (`exists()`, `isDirectory()`, `length()`, `lastModified()`, `list()`, `mkdirs()`, `delete()`, `renameTo()`).
- **Internal Working**: Internally wraps a normalized pathname string and delegates OS-level filesystem operations to a platform-specific `FileSystem` implementation (`java.io.FileSystem`, an internal, package-private abstraction with Unix/Windows subclasses) via native calls.
- **When to Use It**: Legacy codebases, or simple cases needing just a `File` handle for APIs that still require it (some older third-party libraries and `java.io` stream constructors accept `File` directly); for new code, `java.nio.file.Path`/`Files` (NIO.2) is strongly preferred.
- **Advantages**: Simple, familiar API; broadly supported across the whole JDK history and countless libraries built against it.
- **Limitations**: Many methods (e.g., `delete()`, `mkdir()`, `renameTo()`) return a `boolean` on failure instead of throwing a descriptive exception, making error diagnosis difficult; no built-in support for symbolic links, file attributes beyond the basics, or efficient directory-tree walking; not designed with NIO's more powerful, exception-rich, attribute-aware model in mind — largely superseded by `java.nio.file.Path`/`Files` since Java 7.

### Internal Working

- **Step-by-Step Explanation**: 1) `new File(pathname)` simply parses and normalizes the given string into a `File` object — no filesystem I/O occurs at construction time at all. 2) Query methods like `exists()`, `isFile()`, `length()` each trigger a fresh native filesystem call (via `FileSystem`/`FileSystemProvider`-equivalent internals) every time they're invoked, since `File` caches nothing about the underlying filesystem state. 3) Mutating operations (`delete()`, `mkdirs()`, `createNewFile()`) invoke native OS calls and report success/failure as a `boolean`, swallowing any underlying OS error detail (e.g., permission denied versus disk full both simply return `false`).
- **Memory Layout**: A `File` instance is a small, immutable heap object wrapping a normalized path `String` — no file descriptor or OS handle is held by a `File` object itself (unlike an open `FileInputStream`, which does hold a native file descriptor); Not directly applicable beyond this.
- **Diagrams**:

```
new File("/data/reports/2026.csv")
        |
        v (just parses/normalizes the path string; no I/O yet)
File{path="/data/reports/2026.csv"}
        |
        v .exists() / .length() / .delete()  -- each triggers a fresh native syscall
```

- **JVM Behaviour**: File I/O operations invoke native code (via JNI-backed internal classes) that ultimately calls OS system calls (`stat`, `unlink`, `mkdir`, etc. on POSIX systems); these are blocking calls from the calling Java thread's perspective — a thread performing `File` operations occupies an OS thread for the duration of the syscall, which is one of the underlying motivations for asynchronous/non-blocking I/O alternatives introduced later in NIO.

### Interview Questions

**Basic**
1. Does creating a `new File("foo.txt")` create an actual file on disk?
2. What kind of information does `java.io.File` represent?

**Intermediate**
3. Why is `File.delete()` considered a poor API design choice compared to modern alternatives?
4. What is the relationship between `File` and the newer `java.nio.file.Path`?

**Advanced**
5. Why can't `File`-based code efficiently walk very large directory trees compared to NIO.2's `Files.walk`/`DirectoryStream`?

**Scenario-based**
6. A legacy codebase uses `file.delete()` and logs "delete failed" whenever it returns `false`, but the actual reason (permission denied vs. file in use vs. file doesn't exist) is never clear from the logs. How would migrating to NIO.2 improve this?

### Detailed Answers

1. No — constructing a `File` object is purely an in-memory operation that parses and normalizes a pathname string; it performs no filesystem I/O and does not require the path to exist. The file (or lack thereof) is only checked/affected when you call query methods (`exists()`) or mutating methods (`createNewFile()`, `mkdirs()`).
2. `java.io.File` represents an abstract pathname to a file or directory location — essentially, a structured wrapper around a path string, plus metadata-query and basic manipulation methods — but not the file's actual content, which requires opening a stream (`FileInputStream`, etc.) separately.
3. `File.delete()` returns a plain `boolean` (`true`/`false`) to indicate success or failure, giving no information about *why* it failed — permission denied, the file being held open by another process, the path not existing, or a directory being non-empty all collapse into the same `false` result, forcing callers to guess or perform extra checks to diagnose real problems.
4. `java.nio.file.Path` (introduced in Java 7 as part of NIO.2) is the modern replacement for `File`, offering a richer, more capable API (symbolic link support, rich file attribute views, exception-based error reporting, efficient directory streaming). `File` provides a `toPath()` method to obtain an equivalent `Path`, and `Path` provides `toFile()` for the reverse, easing interoperability between legacy code using `File` and modern code using NIO.2.
5. `File.list()`/`listFiles()` eagerly load the *entire* directory listing into an array in one call, which is memory-intensive and slow for directories with huge numbers of entries; NIO.2's `Files.newDirectoryStream(path)`/`Files.walk(path)` instead provide lazy, streaming iteration over directory entries (backed by OS-level directory-reading APIs that fetch entries incrementally), allowing very large directory trees to be processed without loading the full listing into memory at once, and integrate naturally with the Streams API for filtering/short-circuiting.
6. NIO.2's `Files.delete(path)` throws a specific, descriptive exception on failure — e.g., `NoSuchFileException` if the path doesn't exist, `DirectoryNotEmptyException` if attempting to delete a non-empty directory, or `AccessDeniedException`/`IOException` for permission issues — allowing logs and error-handling code to clearly distinguish these cases instead of a single opaque `false`. Migrating the legacy code to `Files.delete`/`Files.deleteIfExists` (catching and logging the specific exception type/message) would immediately surface the true root cause of each deletion failure.

### Code Examples

```java
import java.io.File;
import java.util.Arrays;

public class LegacyFileApiDemo {
    public static void main(String[] args) {
        File reportsDir = new File("reports");

        if (!reportsDir.exists()) {
            boolean created = reportsDir.mkdirs(); // boolean result: no detail on failure reason
            System.out.println("Directory created: " + created);
        }

        File[] entries = reportsDir.listFiles((dir, name) -> name.endsWith(".csv"));
        if (entries != null) {
            Arrays.stream(entries)
                  .forEach(f -> System.out.println(f.getName() + " - " + f.length() + " bytes"));
        }
    }
}
```

```java
import java.io.File;
import java.io.IOException;

public class FileInteropDemo {
    // Common pattern: legacy API still requires java.io.File
    static void processLegacy(File file) throws IOException {
        if (!file.exists()) {
            throw new IOException("File not found: " + file.getAbsolutePath());
        }
        System.out.println("Processing legacy file handle for: " + file.getName());
    }

    public static void main(String[] args) throws IOException {
        // Bridge from modern NIO.2 Path back to legacy File for interop with old APIs
        java.nio.file.Path modernPath = java.nio.file.Path.of("data", "input.txt");
        processLegacy(modernPath.toFile());
    }
}
```

## NIO

### Theory

- **Core Concepts**: `java.nio` (New I/O, Java 1.4+) introduced a fundamentally different I/O model built around **buffers** (`ByteBuffer` and friends — fixed-capacity, position/limit-tracked containers for primitive data) and **channels** (`FileChannel`, `SocketChannel` — bidirectional conduits to an I/O resource that read/write data into/from buffers), plus **selectors** for readiness-based multiplexed non-blocking I/O across many channels with a single thread.
- **Internal Working**: Buffers can be heap-allocated (`ByteBuffer.allocate`, backed by a JVM `byte[]`) or direct/native (`ByteBuffer.allocateDirect`, backed by memory allocated outside the Java heap via `malloc`-equivalent native calls), the latter enabling zero-copy-style transfers directly to/from OS I/O calls without an intermediate JVM-heap-to-native-buffer copy.
- **When to Use It**: High-throughput or scalable network/file I/O scenarios needing non-blocking, multiplexed operation (many connections handled by few threads via `Selector`), or when direct buffer/zero-copy transfer performance matters (e.g., `FileChannel.transferTo`).
- **Advantages**: Non-blocking mode with `Selector`-based readiness selection scales to large numbers of concurrent connections without one-thread-per-connection overhead; direct buffers avoid extra copying for native I/O operations; `FileChannel` supports efficient file-to-file/file-to-socket transfers and file locking.
- **Limitations**: Considerably more complex API than classic streams (manual buffer flip/clear/compact management is a common source of bugs); non-blocking `Selector`-based code is harder to reason about and debug than simple blocking stream code; largely superseded for file-path/metadata operations by NIO.2 (`java.nio.file`), while the buffer/channel/selector machinery remains the low-level foundation used by networking frameworks (Netty, etc.).

### Internal Working

- **Step-by-Step Explanation**: 1) A `Buffer` (e.g., `ByteBuffer`) tracks four key properties: capacity (fixed size), position (next read/write index), limit (boundary of valid data), and mark (a saved position) — operations like `put`/`get` advance `position`, and `flip()` prepares a buffer that was just written to for subsequent reading by setting `limit = position` and `position = 0`. 2) A `Channel` (e.g., `FileChannel`) reads bytes from/writes bytes to an underlying OS resource directly into/from a supplied `Buffer`'s backing storage — for a direct buffer, this can be a true zero-copy operation at the OS level (`read()`/`write()` syscalls operate straight on the native buffer memory). 3) A `Selector` allows a single thread to monitor multiple registered non-blocking channels for readiness (`OP_READ`, `OP_WRITE`, `OP_ACCEPT`, `OP_CONNECT`) via `select()`, which blocks only until at least one registered channel becomes ready, then returns the ready set for the thread to service — the core mechanism enabling scalable, few-threads-many-connections servers.
- **Memory Layout**: Heap buffers (`allocate`) are backed by ordinary `byte[]` arrays on the Java heap, subject to normal GC. Direct buffers (`allocateDirect`) are backed by native (off-heap) memory obtained outside normal heap allocation, tracked by a small on-heap `DirectByteBuffer` wrapper object; the native memory is reclaimed via the buffer object's cleaner/`Cleaner`-based mechanism when the wrapper becomes unreachable and is garbage-collected — meaning direct buffer native memory release is tied to (and can lag behind) GC timing, a common source of native-memory "leak" symptoms if large numbers of direct buffers are allocated without explicit disposal.
- **Diagrams**:

```
write phase:  buffer.put(data)     -- position advances toward limit(=capacity)
              buffer.flip()        -- limit=position, position=0  (switch to read mode)
read phase:   buffer.get()         -- position advances toward limit
              buffer.clear()/compact() -- reset for reuse

Selector.select() blocks until >=1 registered channel is ready
        |
        v
selectedKeys() -> iterate ready channels -> handle read/write without one-thread-per-connection
```

- **JVM Behaviour**: Direct buffers bypass the JVM heap entirely for their storage, reducing GC scanning overhead for large I/O buffers but shifting memory accounting to native/off-heap space (visible via `-XX:MaxDirectMemorySize` and monitorable separately from heap usage); `Selector`-based non-blocking I/O relies on OS-level readiness-notification primitives (`epoll` on Linux, `kqueue` on macOS/BSD, IOCP-adjacent mechanisms on Windows) under the hood via native JVM code.

### Interview Questions

**Basic**
1. What are the three core abstractions introduced by `java.nio`?
2. What is the difference between a heap `ByteBuffer` and a direct `ByteBuffer`?

**Intermediate**
3. What does calling `buffer.flip()` do, and why is it necessary after writing data into a buffer before reading it back?
4. What problem does a `Selector` solve that plain blocking I/O with one thread per connection does not?

**Advanced**
5. Why can allocating many direct buffers lead to native memory pressure even while heap usage looks fine in a profiler?

**Scenario-based**
6. You're building a server that needs to handle 50,000 concurrent (mostly idle) client connections. Explain why classic blocking `java.io` streams with one thread per connection would struggle here, and how NIO's selector model addresses it.

### Detailed Answers

1. The three core `java.nio` abstractions are: **Buffers** (fixed-capacity containers tracking position/limit/capacity for reading/writing primitive data), **Channels** (bidirectional connections to I/O resources like files or sockets that transfer data to/from buffers), and **Selectors** (a readiness-multiplexing mechanism letting one thread monitor many non-blocking channels simultaneously).
2. A heap `ByteBuffer` (`allocate(n)`) is backed by a regular `byte[]` on the Java heap, subject to normal garbage collection, but requires an extra copy into native memory when actually performing OS-level I/O. A direct `ByteBuffer` (`allocateDirect(n)`) is backed by memory allocated outside the JVM heap, which OS I/O calls can operate on directly without an intermediate copy — faster for I/O-heavy workloads but with allocation/deallocation overhead that makes it less suitable for frequently created-and-discarded small buffers.
3. `flip()` sets the buffer's `limit` to the current `position` and resets `position` to 0, switching the buffer from "write mode" (where position marks how much has been written) to "read mode" (where position now marks how much has been read, up to the limit which marks how much valid data exists). It's necessary because without flipping, attempting to read immediately after writing would use the wrong position/limit values, either reading garbage past the written data or reading nothing at all.
4. A `Selector` allows a single (or small pool of) thread(s) to monitor readiness across potentially thousands of channels simultaneously, servicing only those that are actually ready for I/O at any given moment. Classic blocking I/O requires one dedicated thread per connection (since a blocking read/write ties up the calling thread until data is available), which becomes prohibitively expensive in memory (each thread's stack) and context-switching overhead once connection counts reach into the thousands or tens of thousands — the "C10K problem" that NIO's selector model was specifically designed to address.
5. Direct buffers' actual storage lives in native (off-heap) memory, which is only reclaimed when the small on-heap `DirectByteBuffer` wrapper object becomes unreachable and is collected, triggering its cleaner to free the native memory. If direct buffers are allocated frequently but the wrapper objects happen to be long-lived, rarely-collected, or the GC simply hasn't run a full collection recently, the native memory they hold can accumulate well beyond what heap-focused profiling tools show, since standard heap profilers don't account for off-heap native allocations tied to buffer cleaners — dedicated monitoring of `-XX:MaxDirectMemorySize` usage or explicit buffer lifecycle management is needed to catch this.
6. With one thread per connection, 50,000 mostly-idle connections would require 50,000 OS threads, each consuming its own stack memory (often 512KB-1MB by default) and adding scheduler/context-switch overhead, quickly exhausting memory and CPU resources even though almost all threads are blocked doing nothing most of the time. NIO's `Selector` lets a small, fixed number of threads register all 50,000 channels in non-blocking mode and call `select()`, which efficiently (via OS primitives like `epoll`) blocks until at least one channel actually has data ready, letting the same handful of threads service whichever connections are currently active without dedicating a full OS thread to each idle connection.

### Code Examples

```java
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class NioBufferChannelDemo {
    public static void main(String[] args) throws Exception {
        try (RandomAccessFile raf = new RandomAccessFile("nio-demo.txt", "rw");
             FileChannel channel = raf.getChannel()) {

            ByteBuffer buffer = ByteBuffer.allocate(64);
            buffer.put("Hello, NIO Channels!".getBytes());

            buffer.flip();              // switch from write mode to read mode
            channel.write(buffer);      // write buffer's readable content to the file

            channel.position(0);
            buffer.clear();             // reset for reading from the channel
            int bytesRead = channel.read(buffer);
            buffer.flip();

            byte[] data = new byte[bytesRead];
            buffer.get(data);
            System.out.println(new String(data));
        }
    }
}
```

```java
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

public class SelectorSkeletonDemo {
    public static void main(String[] args) throws Exception {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(8080));
        serverChannel.configureBlocking(false);

        Selector selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        // A single thread can now efficiently multiplex many client connections
        while (selector.select() > 0) {
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                keys.remove();
                if (key.isAcceptable()) {
                    // handle new connection acceptance without blocking other channels
                }
            }
        }
    }
}
```

## NIO2

### Path

#### Theory

- **Core Concepts**: `java.nio.file.Path` (Java 7+, NIO.2) is an immutable, filesystem-abstraction representation of a file-system path, obtained via `Path.of(...)` (or the older `Paths.get(...)`) or `FileSystem.getPath(...)`. Unlike `File`, `Path` is tied to a specific `FileSystem` provider, enabling it to represent paths in non-default filesystems too (e.g., an in-memory filesystem, or paths inside a ZIP file via `FileSystems.newFileSystem`).
- **Internal Working**: A `Path` stores a normalized sequence of path elements (root, plus name components) and delegates actual filesystem interaction to the associated `FileSystemProvider`; operations like `resolve`, `relativize`, and `normalize` are pure string/segment manipulations with no I/O involved, while `toRealPath()` and most `Files` methods that accept a `Path` do perform actual I/O.
- **When to Use It**: The modern default for representing any filesystem location in new code — pairs with the `Files` utility class for actual I/O operations.
- **Advantages**: Rich, chainable path manipulation API (`resolve`, `relativize`, `normalize`, `getParent`, `getFileName`), pluggable filesystem support (works with non-default `FileSystem`s, e.g., ZIP filesystems), integrates cleanly with `Files`' exception-rich, attribute-aware I/O methods.
- **Limitations**: Being tied to a specific `FileSystem`, comparing or combining `Path` instances from different filesystem providers can throw `ProviderMismatchException`; still requires care around platform-specific path syntax (though `Path.of` accepts platform-appropriate separators).

#### Internal Working

- **Step-by-Step Explanation**: 1) `Path.of("data", "reports", "2026.csv")` (or a single string with separators) builds an immutable `Path` by delegating to the default `FileSystemProvider`'s path-parsing logic, normalizing separators for the current platform. 2) Pure path-manipulation methods (`resolve`, `relativize`, `normalize`, `getParent`) operate purely on the in-memory sequence of path segments — no filesystem access occurs. 3) I/O-triggering methods, whether directly on `Path` (`toRealPath()`, which resolves symbolic links and requires the path to actually exist) or via the companion `Files` class, delegate to the `FileSystemProvider`, which performs the actual native syscalls.
- **Memory Layout**: An immutable, lightweight heap object holding normalized path segment data plus a reference to its owning `FileSystem`; Not directly applicable beyond ordinary object allocation.
- **Diagrams**:

```
Path base = Path.of("/data/reports");
Path file = base.resolve("2026.csv");          // /data/reports/2026.csv (no I/O)
Path rel  = base.relativize(file);              // 2026.csv (no I/O)
Path real = file.toRealPath();                  // resolves symlinks, REQUIRES file to exist (I/O)
```

- **JVM Behaviour**: Path manipulation methods are pure in-memory computation with no syscalls, JIT-friendly and allocation-light; I/O-triggering operations invoke native filesystem calls through the `FileSystemProvider` SPI, identical in cost/blocking nature to any other native I/O call.

#### Interview Questions

**Basic**
1. What is `java.nio.file.Path`, and how do you create one?
2. Does calling `path.resolve("subdir")` touch the filesystem?

**Intermediate**
3. What is the difference between `Path.resolve()` and `Path.relativize()`?
4. What does `Path.normalize()` do, and when is it useful?

**Advanced**
5. Why can combining two `Path` instances from different `FileSystem`s throw `ProviderMismatchException`, and what does this reveal about `Path`'s design?

**Scenario-based**
6. You need to read files stored inside a ZIP archive using the same `Files`/`Path` APIs used for regular filesystem access. How does NIO.2 support this?

#### Detailed Answers

1. `Path` is NIO.2's abstraction of a filesystem location (file or directory), tied to a specific `FileSystem`. You create one via the static factory `Path.of("some", "path", "segments")` (Java 11+) or the older `Paths.get(...)`, both of which delegate to the default filesystem's provider to parse and normalize the given path string(s).
2. No — `resolve()` is a pure in-memory path-combination operation (appending the given relative path to the current path's segments) and performs no filesystem access; the resulting `Path` may or may not correspond to anything that actually exists on disk.
3. `resolve(other)` combines the current path with another (typically relative) path, producing a longer, combined path (e.g., `/data` resolved with `reports/x.csv` gives `/data/reports/x.csv`). `relativize(other)` does the inverse conceptually — given two paths, it computes the relative path needed to navigate from the current path to the other path (e.g., relativizing `/data` against `/data/reports/x.csv` gives `reports/x.csv`).
4. `normalize()` removes redundant path elements like `.` (current directory) and resolves `..` (parent directory) segments where possible, producing a cleaner, canonical-looking path string without touching the filesystem or resolving symbolic links (unlike `toRealPath()`). It's useful for cleaning up paths built programmatically through concatenation/resolution before displaying them or using them in contexts sensitive to redundant segments.
5. Different `FileSystem` instances (e.g., the default local filesystem versus a ZIP-file-backed filesystem opened via `FileSystems.newFileSystem`) may use entirely different internal path representations and semantics; NIO.2 deliberately prevents mixing `Path` instances from different providers in operations like `resolve`/`equals` that assume a common filesystem context, throwing `ProviderMismatchException` to catch such logically invalid combinations early. This reveals that `Path` is fundamentally an abstraction over a *pluggable* filesystem model (the `FileSystemProvider` SPI), not merely a string wrapper for the local disk.
6. NIO.2's `FileSystemProvider` SPI allows third-party (or JDK-bundled, e.g., `jdk.zipfs`) providers to implement an entirely custom `FileSystem` backed by a ZIP archive; calling `FileSystems.newFileSystem(zipPath, ...)` returns a `FileSystem` instance whose `Path`s refer to entries inside the ZIP, and the exact same `Files` methods (`Files.readAllBytes`, `Files.newInputStream`, `Files.walk`, etc.) work transparently against these ZIP-backed paths, since they all operate through the common `Path`/`FileSystemProvider` abstraction rather than assuming a real local disk.

#### Code Examples

```java
import java.nio.file.Path;

public class PathManipulationDemo {
    public static void main(String[] args) {
        Path base = Path.of("/data/reports");
        Path file = base.resolve("2026").resolve("summary.csv");

        System.out.println(file);                       // /data/reports/2026/summary.csv
        System.out.println(base.relativize(file));       // 2026/summary.csv
        System.out.println(file.getParent());            // /data/reports/2026
        System.out.println(file.getFileName());          // summary.csv

        Path messy = Path.of("/data/./reports/../reports/2026/summary.csv");
        System.out.println(messy.normalize());           // /data/reports/2026/summary.csv
    }
}
```

```java
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class ZipFileSystemDemo {
    public static void main(String[] args) throws Exception {
        Path zipPath = Path.of("archive.zip");
        URI zipUri = URI.create("jar:" + zipPath.toUri());

        try (FileSystem zipFs = FileSystems.newFileSystem(zipUri, Map.of("create", "true"))) {
            Path entry = zipFs.getPath("/notes.txt");
            Files.writeString(entry, "Stored inside the zip via the Path/Files API");
            System.out.println(Files.readString(entry));
        }
    }
}
```

### Files

#### Theory

- **Core Concepts**: `java.nio.file.Files` is a final utility class of static methods providing the actual I/O operations that pair with `Path` objects — reading/writing whole files (`readAllBytes`, `readString`, `writeString`), streaming line-by-line (`lines()`), copying/moving/deleting (`copy`, `move`, `delete`/`deleteIfExists`), directory traversal (`walk`, `list`, `newDirectoryStream`), and rich attribute queries (`size`, `getLastModifiedTime`, `readAttributes` for POSIX/DOS-specific attribute views).
- **Internal Working**: Every `Files` method delegates to the `Path` argument's associated `FileSystemProvider`, which performs the actual native filesystem calls; methods that return a `Stream` (like `Files.lines`, `Files.walk`) are backed by lazily-evaluated, closeable resources that must be used within try-with-resources to avoid leaking underlying file handles/directory streams.
- **When to Use It**: The default choice for essentially all file I/O in modern Java code — reading/writing files, walking directory trees, checking/copying/moving files, querying detailed file attributes.
- **Advantages**: Descriptive, specific exceptions (`NoSuchFileException`, `FileAlreadyExistsException`, `DirectoryNotEmptyException`) instead of opaque booleans; convenient one-liner whole-file read/write helpers; efficient lazy streaming for large files/directory trees; rich, extensible file-attribute-view support (POSIX permissions, DOS attributes, owner information) via `readAttributes`/`setAttribute`.
- **Limitations**: Stream-returning methods (`lines()`, `walk()`, `list()`) must be explicitly closed (try-with-resources) since they hold underlying OS resources (file handles/directory streams) that aren't released just because the terminal stream operation completes normally in all cases — a commonly-missed resource leak if `.close()`/try-with-resources is forgotten.

#### Internal Working

- **Step-by-Step Explanation**: 1) `Files.readAllBytes(path)` opens the file, reads its entire content into a single `byte[]` in one operation, and closes the file — suitable only for files that comfortably fit in memory. 2) `Files.lines(path)` opens the file and returns a lazily-evaluated `Stream<String>` backed by a `BufferedReader` under the hood; content is read incrementally as the stream is consumed, and the underlying reader is only closed when the stream itself is closed (hence the try-with-resources requirement). 3) `Files.walk(path)` similarly returns a lazily-evaluated `Stream<Path>` backed by an internal directory-tree iterator that opens/reads OS directory handles incrementally as the stream is traversed, again requiring proper closing to release those handles. 4) Attribute methods like `Files.readAttributes(path, PosixFileAttributes.class)` query a specific attribute-view SPI implementation registered by the filesystem provider, returning a rich, platform-appropriate view object instead of forcing a lowest-common-denominator generic model.
- **Memory Layout**: Whole-file read methods allocate a single `byte[]`/`String` sized to the file's content on the heap (a potential `OutOfMemoryError` risk for very large files, contrasted with `lines()`/`newBufferedReader` streaming approaches which only hold a small buffer's worth in memory at a time); Not otherwise directly applicable.
- **Diagrams**:

```
Files.readAllBytes(path)      -- one-shot: loads entire file into heap byte[]

try (Stream<String> lines = Files.lines(path)) {   -- lazy: BufferedReader underneath
    lines.filter(...).forEach(...);                 --  reads incrementally as consumed
}   // stream.close() -> closes underlying reader -> releases file handle
```

- **JVM Behaviour**: Underlying native syscalls are blocking from the calling thread's perspective, exactly like classic `java.io`; the key JVM-relevant risk is native file-descriptor/handle exhaustion if lazily-streamed `Files` results aren't properly closed, since the JVM (and OS) impose limits on the number of concurrently open file descriptors per process.

#### Interview Questions

**Basic**
1. What is `java.nio.file.Files`, and how does it relate to `Path`?
2. What's the difference between `Files.readAllBytes` and `Files.lines` in terms of memory usage?

**Intermediate**
3. Why must `Files.lines(path)` and `Files.walk(path)` be used inside a try-with-resources block?
4. Name three specific exceptions `Files` methods can throw and what each indicates.

**Advanced**
5. How does `Files.readAttributes` support platform-specific metadata (like POSIX permissions) without a bloated, lowest-common-denominator `File`-style API?

**Scenario-based**
6. A batch job calls `Files.walk(rootDir).filter(...).forEach(...)` in a loop processing thousands of directory trees per run, and after a while it starts throwing "Too many open files." What is the likely bug, and how do you fix it?

#### Detailed Answers

1. `Files` is a static-method utility class providing the actual filesystem operations (reading, writing, copying, deleting, attribute queries, directory traversal) that operate on `Path` objects, which by themselves only represent a location, not perform I/O. Nearly every `Files` method takes one or more `Path` arguments and delegates to the appropriate `FileSystemProvider` to perform the operation.
2. `Files.readAllBytes` loads the file's **entire** content into a single `byte[]` in memory in one call — appropriate only for files known to be reasonably small. `Files.lines` returns a lazily-evaluated `Stream<String>` that reads the file incrementally, line by line, as the stream is consumed, keeping memory usage bounded regardless of the file's total size — appropriate for large files or line-by-line processing pipelines.
3. Both methods return a `Stream` that is backed by an open OS-level resource (a `BufferedReader`/file handle for `lines()`, a directory-traversal handle for `walk()`) that is only released when the stream's `close()` method is called. Since `Stream` implements `AutoCloseable` specifically for this reason, failing to close it (e.g., not using try-with-resources) leaks the underlying file handle or directory stream, which can eventually exhaust the process's file descriptor limit under sustained use.
4. `NoSuchFileException` — thrown when an operation targets a path that doesn't exist but was expected to. `FileAlreadyExistsException` — thrown when an operation like `Files.createFile`/`Files.copy` (without `REPLACE_EXISTING`) targets a path that already exists. `DirectoryNotEmptyException` — thrown when attempting to delete a directory that still contains entries, via `Files.delete`.
5. `Files.readAttributes` is generic over `BasicFileAttributes` subinterfaces/classes (`PosixFileAttributes`, `DosFileAttributes`), each provided by a corresponding `FileAttributeView` SPI implementation registered by the underlying `FileSystemProvider` for that platform. This lets the API expose rich, platform-specific metadata (POSIX owner/group/permission bits on Unix-like systems, hidden/archive/system flags on Windows) through strongly-typed, purpose-built attribute classes, rather than cramming every possible platform's metadata into one bloated, mostly-irrelevant-per-platform generic `File`-style method set.
6. The likely bug is that the code isn't wrapping `Files.walk(rootDir)` in a try-with-resources block (or is otherwise failing to call `.close()` on the returned stream) for every directory tree processed — each call leaves the underlying directory-traversal OS handles open, and across thousands of iterations, the process eventually exhausts its available file descriptors. The fix is to always wrap `Files.walk(...)` (and any other `Files` method returning a `Stream`, like `Files.lines`/`Files.list`) in try-with-resources: `try (Stream<Path> paths = Files.walk(rootDir)) { paths.filter(...).forEach(...); }`, ensuring the underlying resource is released deterministically after each use regardless of whether the stream operation completes normally or throws.

#### Code Examples

```java
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class FilesBasicOpsDemo {
    public static void main(String[] args) throws Exception {
        Path source = Path.of("input.csv");
        Files.writeString(source, "id,name\n1,widget\n2,gadget\n");

        List<String> lines = Files.readAllLines(source);
        System.out.println("Lines read: " + lines.size());

        Path backup = Path.of("input.csv.bak");
        Files.copy(source, backup, StandardCopyOption.REPLACE_EXISTING);

        System.out.println("Backup exists: " + Files.exists(backup));
        System.out.println("Backup size: " + Files.size(backup) + " bytes");
    }
}
```

```java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class LargeDirectoryScanDemo {
    static long countJavaFiles(Path root) throws IOException {
        // try-with-resources is mandatory: Files.walk holds an open directory-traversal handle
        try (Stream<Path> paths = Files.walk(root)) {
            return paths.filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".java"))
                        .count();
        }
    }

    public static void main(String[] args) throws IOException {
        long count = countJavaFiles(Path.of("."));
        System.out.println("Java files found: " + count);
    }
}
```

### Channels

#### Theory

- **Core Concepts**: A `Channel` (`java.nio.channels`) represents an open connection to an I/O source/destination — a file (`FileChannel`), a TCP socket (`SocketChannel`/`ServerSocketChannel`), a UDP socket (`DatagramChannel`) — capable of reading data into a `Buffer` and/or writing data from a `Buffer`. Unlike a classic `InputStream`/`OutputStream` (which are inherently one-directional), many channels are bidirectional, and network channels support non-blocking mode for use with a `Selector`.
- **Internal Working**: A channel wraps a native OS file descriptor/socket handle; `read(buffer)`/`write(buffer)` calls translate to native read/write syscalls operating on the buffer's backing memory (with direct buffers enabling the OS to read/write straight into off-heap memory, avoiding an extra JVM-heap copy).
- **When to Use It**: `FileChannel` for advanced file operations — memory-mapped file access (`map()`), efficient file-to-file/file-to-socket transfer (`transferTo`/`transferFrom`), file locking (`lock()`/`tryLock()`), and precise positional read/write. `SocketChannel`/`ServerSocketChannel`/`DatagramChannel` for non-blocking, selector-multiplexed network I/O.
- **Advantages**: Supports zero-copy-style transfers (`transferTo` can, on many OS/JVM combinations, avoid copying data through user-space entirely for file-to-socket transfers — the basis of high-performance file-serving code), memory-mapped I/O for very fast random access to large files, native non-blocking mode integration with `Selector`.
- **Limitations**: Lower-level and more complex than classic streams; memory-mapped files (`MappedByteBuffer`) have historically had no reliable, guaranteed way to force unmapping before GC (`Cleaner`-dependent), which could cause file-locking issues on Windows until addressed by newer JDK APIs; file locks obtained via `FileChannel.lock()` are JVM/OS advisory locks in many cases and are not automatically reentrant across multiple channels in the same JVM in all platforms' semantics.

#### Internal Working

- **Step-by-Step Explanation**: 1) `FileChannel.open(path, options)` (or `RandomAccessFile.getChannel()`/`FileInputStream.getChannel()`) opens a native file handle and wraps it as a `FileChannel`. 2) `channel.read(buffer)` fills the given buffer from the current file position (advancing it), while `channel.write(buffer)` writes the buffer's remaining readable bytes to the file at the current position — both delegate to native read/write syscalls. 3) `transferTo(position, count, targetChannel)` (commonly used for file-to-socket transfer, e.g., serving a static file over HTTP) can, depending on OS and channel types, be implemented via a kernel-level `sendfile`-equivalent syscall that copies data directly within the OS without ever bringing it into JVM (or even user-space) memory at all — a true zero-copy transfer. 4) `map(mode, position, size)` creates a `MappedByteBuffer` backed directly by the OS's memory-mapping of the file, allowing the file's content to be accessed as if it were an in-memory array, with the OS's virtual memory system handling page-in/page-out transparently.
- **Memory Layout**: Memory-mapped file regions (`MappedByteBuffer`) occupy address space in the process's virtual memory outside the normal Java heap, backed directly by the OS page cache for that file; direct `ByteBuffer`s used with channels similarly live in native/off-heap memory, distinct from heap-allocated buffers.
- **Diagrams**:

```
Classic copy: file A -> read into JVM heap buffer -> write from buffer -> socket B  (2 copies, crosses user-space)

transferTo:   file A ------------------------------------------------> socket B
              (potential kernel-level zero-copy path, bypassing JVM heap/user-space entirely)
```

- **JVM Behaviour**: Channel operations invoke native code; memory-mapped buffers rely on OS virtual memory / page fault mechanisms rather than explicit read/write syscalls for each access, meaning access patterns can trigger page faults handled transparently by the OS; unmapping a `MappedByteBuffer`'s underlying memory has historically depended on GC-driven cleaner mechanisms (improved via `sun.misc.Unsafe`/newer JDK APIs), which is why memory-mapped files on some platforms (notably Windows) could remain locked/undeletable until the mapping was collected.

#### Interview Questions

**Basic**
1. What is a `Channel`, and how is it different from an `InputStream`/`OutputStream`?
2. Give one example each of a file-oriented and a network-oriented channel.

**Intermediate**
3. What does `FileChannel.transferTo` do, and why can it be significantly faster than a manual read/write loop for file-to-socket copying?
4. What is a memory-mapped file (`MappedByteBuffer`), and what's the main advantage over regular buffered reads?

**Advanced**
5. Why has unmapping memory-mapped files historically been tricky in Java, and what problem did this cause on Windows specifically?

**Scenario-based**
6. You're building a file-download server that streams large static files to clients. How would using `FileChannel.transferTo` improve performance compared to a loop of `InputStream.read`/`OutputStream.write` calls?

#### Detailed Answers

1. A `Channel` is a bidirectional (in many cases) conduit to an I/O resource that transfers data to/from `Buffer` objects, whereas `InputStream`/`OutputStream` are inherently one-directional, byte-at-a-time (or small-array-at-a-time) abstractions with no direct buffer-management concept and no native support for non-blocking mode or `Selector`-based multiplexing.
2. `FileChannel` is the file-oriented example, supporting positional read/write, memory-mapping, locking, and efficient transfers. `SocketChannel` (or `DatagramChannel` for UDP) is the network-oriented example, supporting both blocking and non-blocking modes and integration with a `Selector` for scalable multiplexed I/O.
3. `transferTo(position, count, target)` hands off the data-transfer responsibility to the underlying OS/JVM native implementation, which — for suitable channel type combinations (e.g., a `FileChannel` source and a `SocketChannel` target) — can leverage a kernel-level zero-copy mechanism (like the `sendfile` syscall on Linux) that moves data directly within the OS/kernel space without ever copying it into JVM heap memory or even user-space buffers at all. A manual read/write loop, by contrast, must always copy data from the OS into a JVM buffer (read) and then from that buffer back out to the OS (write) — at least one extra full data copy, and typically more with intermediate buffering.
4. A memory-mapped file is a region of a file made directly accessible in a process's virtual address space via the OS's memory-mapping facility (`FileChannel.map()` producing a `MappedByteBuffer`), so that reading/writing to the buffer directly reads/writes the underlying file content, with the OS's virtual memory system handling the actual disk I/O transparently (via page faults) as needed. The main advantage is very fast random access to large files — there's no need to explicitly seek/read specific byte ranges via syscalls; instead, you simply index into the buffer as if it were an in-memory array, and the OS's page cache handles bringing the relevant portions into physical memory on demand.
5. Because `MappedByteBuffer` doesn't provide a public, direct "unmap now" method, the underlying native memory mapping has historically only been released when the buffer object itself becomes unreachable and is garbage-collected, triggering an internal cleaner. This unpredictable timing (dependent on GC running) meant that on Windows, whose filesystem semantics prevent deleting or fully modifying a file that still has an active memory mapping, a program could fail to delete/rename a file it had previously memory-mapped, until an unpredictable future GC cycle finally released the mapping — a long-standing, well-known Java pain point (partially mitigated in newer JDKs via APIs allowing more explicit control).
6. Using `FileChannel.transferTo(position, count, socketChannel)` allows the OS to potentially move the file's bytes directly to the socket via a kernel-level zero-copy path, avoiding the overhead of repeatedly copying data into and out of JVM-managed buffers that a manual `InputStream.read`/`OutputStream.write` loop would incur. For a server serving large static files to many concurrent clients, this reduces CPU usage (fewer copies, less garbage generated) and can substantially increase achievable throughput compared to naive stream-based copying.

#### Code Examples

```java
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

public class ZeroCopyTransferDemo {
    static void serveFile(String filePath, SocketChannel clientChannel) throws Exception {
        try (RandomAccessFile file = new RandomAccessFile(filePath, "r");
             FileChannel fileChannel = file.getChannel()) {

            long size = fileChannel.size();
            long position = 0;

            // Potential kernel-level zero-copy transfer, avoiding JVM-heap buffering entirely
            while (position < size) {
                position += fileChannel.transferTo(position, size - position, clientChannel);
            }
        }
    }
}
```

```java
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class MemoryMappedFileDemo {
    public static void main(String[] args) throws Exception {
        Path path = Path.of("large-index.dat");

        try (FileChannel channel = FileChannel.open(path,
                StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE)) {

            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, 1024);

            // Direct indexed access - the OS handles paging the underlying file transparently
            buffer.putInt(0, 42);
            buffer.putInt(4, 100);

            System.out.println("Value at offset 0: " + buffer.getInt(0));
            System.out.println("Value at offset 4: " + buffer.getInt(4));
        }
    }
}
```

### Buffers

#### Theory

- **Core Concepts**: `java.nio.Buffer` and its typed subclasses (`ByteBuffer`, `CharBuffer`, `IntBuffer`, `LongBuffer`, `DoubleBuffer`, etc.) are fixed-capacity, sequential containers for primitive data, characterized by four key state properties: **capacity** (total fixed size, set at creation and never changed), **position** (index of the next element to read/write), **limit** (index of the first element that should not be read/written), and **mark** (a saved position for later reset). `ByteBuffer` additionally supports viewing its content as other primitive-typed buffers and reading/writing multi-byte primitives with a configurable `ByteOrder` (endianness).
- **Internal Working**: A heap buffer wraps a JVM-managed primitive array (`byte[]`, `int[]`, etc.); a direct buffer wraps a block of natively-allocated (off-heap) memory referenced via an internal address, avoiding the extra host-to-native copy step required when heap buffers are used in native I/O calls.
- **When to Use It**: Anywhere you interact with `Channel`-based I/O (which requires buffers, not byte arrays directly), or need efficient, allocation-light manipulation of primitive sequences with explicit position/limit tracking.
- **Advantages**: Precise control over exactly which portion of data has been read/written via position/limit, supports efficient bulk transfer operations (`put(byte[])`, `get(byte[])`), direct buffers enable zero-copy-style native I/O.
- **Limitations**: The position/limit/flip/clear/compact protocol is a common source of subtle bugs for developers new to NIO (forgetting to `flip()` before reading, or `clear()`/`compact()` before reusing for writing); direct buffer allocation/deallocation has higher fixed overhead than heap buffers, making them a poor choice for many small, short-lived buffers.

#### Internal Working

- **Step-by-Step Explanation**: 1) `ByteBuffer.allocate(n)` creates a heap buffer with `capacity = limit = n` and `position = 0`, ready for writing. 2) As data is `put()` into the buffer, `position` advances toward `limit` (initially equal to `capacity`); attempting to `put()` beyond `limit` throws `BufferOverflowException`. 3) To then read what was just written, `flip()` sets `limit = position` and `position = 0`, defining exactly the previously-written region as now readable; `get()` calls advance `position` toward `limit`, and reading past `limit` throws `BufferUnderflowException`. 4) `clear()` resets `position = 0` and `limit = capacity`, discarding any notion of previously written data (ready for fresh writing, but not erasing old bytes still physically present) — `compact()` instead shifts any unread remaining bytes (between the current `position` and `limit`) to the beginning of the buffer and adjusts `position`/`limit` accordingly, preserving unconsumed data for buffers used in partial-read/partial-write loops.
- **Memory Layout**: Heap buffers are backed by ordinary GC-managed primitive arrays; direct buffers are backed by natively allocated (`malloc`-equivalent) off-heap memory, tracked via a small heap-resident wrapper object whose garbage collection triggers eventual native memory release through an internal cleaner mechanism.
- **Diagrams**:

```
allocate(10):  capacity=10 limit=10 position=0   [_ _ _ _ _ _ _ _ _ _]
put(5 bytes):  position=5                        [X X X X X _ _ _ _ _]
flip():        limit=5 position=0                [X X X X X] (readable region)
get(3 bytes):  position=3                         reads X X X
compact():     shifts unread [X](1 byte) to front, position=1, limit=capacity=10
```

- **JVM Behaviour**: Ordinary heap-buffer operations are plain array indexing, fully JIT-optimizable bounds-checked array access; direct-buffer operations go through `sun.nio.ch`/`Unsafe`-backed native memory access paths, bypassing normal heap array bounds-check elimination but avoiding GC scanning of that memory region entirely since it isn't part of the managed heap.

#### Interview Questions

**Basic**
1. What four state properties does a `Buffer` track, and what does each mean?
2. What is the difference between `clear()` and `compact()`?

**Intermediate**
3. Why is calling `flip()` necessary after writing to a buffer and before reading from it?
4. What exception is thrown if you try to `put()` more data than a buffer's remaining capacity (between position and limit) allows?

**Advanced**
5. When would you prefer `compact()` over `clear()` in a read/process/write loop?

**Scenario-based**
6. A developer writes data into a `ByteBuffer` via `put()` in a loop, then immediately tries to `get()` the same buffer to send it over a channel, but nothing is read (0 bytes). What mistake did they make?

#### Detailed Answers

1. **Capacity** is the buffer's fixed total size, set once at creation and never changed thereafter. **Position** is the index of the next element to be read or written, advancing with each `get()`/`put()`. **Limit** is the index of the first element that should not be read/written — for writing, it's typically the capacity; for reading (after `flip()`), it marks how much valid data was actually written. **Mark** is an optional saved position value, set via `mark()` and restorable via `reset()`, useful for "peek ahead then rewind" patterns.
2. `clear()` resets `position` to 0 and `limit` to `capacity`, preparing the buffer for a fresh round of writing — it does **not** erase or zero out the buffer's actual underlying data, it merely resets the tracking indices, meaning old data is still physically present (though logically considered "not yet written" until overwritten or read past the old position). `compact()` instead preserves any *unread* remaining data (between the current `position` and `limit`) by shifting it to the start of the buffer, then sets `position` just after that preserved data and `limit` to `capacity` — used when a buffer is being incrementally drained and refilled without losing not-yet-consumed bytes.
3. After writing, `position` marks how much has been written and `limit` still equals `capacity` (which doesn't reflect what's actually valid data). `flip()` converts these to reading-mode semantics by setting `limit = position` (marking exactly where the valid, previously-written data ends) and resetting `position = 0` (so reading starts from the beginning of that valid data) — without this, reads would either return zero bytes (since `position` already equals where writing left off, right at `limit == capacity`) or read uninitialized/garbage data beyond the actually-written region.
4. `BufferOverflowException` is thrown if you attempt to `put()` more data than fits in the remaining space between the current `position` and `limit`.
5. `compact()` is preferable in a read/process/write loop scenario where a buffer may contain a partially-consumed message or record — e.g., reading network data where a complete "frame" might not yet be fully received, and you need to preserve the unconsumed trailing bytes at the start of the buffer for the next read call to append to, rather than discarding them as `clear()` implicitly does by resetting position to 0 without preserving trailing unread content.
6. The developer forgot to call `flip()` (or otherwise reset `position`/`limit` appropriately) after writing to the buffer via `put()` and before attempting to `get()`/write it out via a channel. After the `put()` loop, `position` sits right at the end of the written data and `limit` still equals `capacity`, so immediately calling `get()` or `channel.write(buffer)` starts reading from `position` (right after the written data) up to `limit` (capacity) — since nothing was written in that trailing region, effectively zero meaningful bytes are read/transferred. Calling `flip()` before reading fixes this by correctly redefining the readable region as exactly what was just written.

#### Code Examples

```java
import java.nio.ByteBuffer;
import java.nio.BufferOverflowException;

public class BufferProtocolDemo {
    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(10);

        buffer.put((byte) 1).put((byte) 2).put((byte) 3);
        System.out.println("After put - position: " + buffer.position() + ", limit: " + buffer.limit());

        buffer.flip(); // switch to read mode: limit=position(3), position=0
        System.out.println("After flip - position: " + buffer.position() + ", limit: " + buffer.limit());

        while (buffer.hasRemaining()) {
            System.out.println("Read byte: " + buffer.get());
        }

        try {
            ByteBuffer small = ByteBuffer.allocate(2);
            small.put((byte) 1).put((byte) 2).put((byte) 3); // exceeds capacity
        } catch (BufferOverflowException e) {
            System.out.println("Caught expected overflow: buffer is full");
        }
    }
}
```

```java
import java.nio.ByteBuffer;

public class CompactVsClearDemo {
    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put(new byte[]{1, 2, 3, 4, 5});
        buffer.flip(); // read mode: position=0, limit=5

        buffer.get(); // consume one byte (value 1); position now 1, three bytes (2,3,4,5 minus one) remain... 
        buffer.get(); // consume another (value 2); position now 2

        // Preserve the still-unread bytes (3,4,5) by shifting them to the front
        buffer.compact();
        System.out.println("After compact - position: " + buffer.position()); // 3 (unread bytes preserved)

        // Buffer is now ready to accept more writes after the preserved unread data
        buffer.put((byte) 6);
        buffer.flip();
        while (buffer.hasRemaining()) {
            System.out.println(buffer.get()); // prints 3, 4, 5, 6 in order
        }
    }
}
```

## Streams *(new)*

### Theory

- **Core Concepts**: The classic `java.io` streaming model splits I/O into four root abstractions: `InputStream`/`OutputStream` for **byte-oriented** data (raw binary, e.g., images, serialized data), and `Reader`/`Writer` for **character-oriented** data (text, with charset decoding/encoding built in). Both families support a decorator-pattern-based composition model (`BufferedInputStream(new FileInputStream(...))`, `InputStreamReader(inputStream, charset)`) allowing behavior (buffering, charset conversion, compression, etc.) to be layered as needed.
- **Internal Working**: `InputStream`/`OutputStream` define a minimal contract centered on `read()`/`write()` operating on `byte`/`byte[]`; `Reader`/`Writer` mirror this for `char`/`char[]`, with `InputStreamReader`/`OutputStreamWriter` acting as the bridge that decodes/encodes bytes to/from characters using a specified (or platform-default) `Charset`.
- **When to Use It**: `InputStream`/`OutputStream` for any binary data (images, serialized objects, network protocols, compressed data); `Reader`/`Writer` for any text data where correct character-encoding handling matters (avoiding the classic mojibake/garbled-text bug class from treating text as raw bytes with an implicit or wrong charset assumption).
- **Advantages**: Simple, blocking, easy-to-reason-about sequential API; broad decorator ecosystem (`GZIPInputStream`, `ObjectInputStream`, `BufferedReader`, `PrintWriter`) for layering functionality; universally supported across the entire JDK and ecosystem.
- **Limitations**: Purely blocking (a `read()` call ties up the calling thread until data is available or EOF), no built-in non-blocking or readiness-multiplexing support (that's what NIO's channels/selectors address); unbuffered raw streams performing one syscall per single-byte read are extremely slow, requiring explicit wrapping in `Buffered*` decorators for reasonable performance.

### Internal Working

- **Step-by-Step Explanation**: 1) A concrete `InputStream` (e.g., `FileInputStream`) wraps a native file descriptor; each `read()`/`read(byte[])` call performs (or triggers, if buffered) an underlying native read syscall. 2) Decorators wrap an existing stream and add behavior transparently — `BufferedInputStream` interposes an internal byte array, satisfying many small `read()` calls from that in-memory buffer and only issuing a native syscall to refill it when exhausted, dramatically reducing syscall overhead for byte-at-a-time consumption patterns. 3) For character streams, `InputStreamReader` wraps a byte-oriented `InputStream` and a `Charset`, using a `CharsetDecoder` internally to convert incoming byte sequences into `char` sequences correctly (handling multi-byte encodings like UTF-8 properly, including partial multi-byte sequences split across buffer-fill boundaries). 4) Every stream/reader implements `Closeable`/`AutoCloseable`, and must be closed (ideally via try-with-resources) to release the underlying native file descriptor/socket handle.
- **Memory Layout**: Buffered decorators hold an internal heap-allocated `byte[]`/`char[]` buffer (typically a few KB by default) that is filled/drained incrementally; unbuffered raw streams hold minimal state beyond the native handle itself.
- **Diagrams**:

```
FileInputStream (raw)
       |
       v  wrap
BufferedInputStream (adds internal buffer, reduces syscalls)
       |
       v  wrap (for text)
InputStreamReader(charset)  -- decodes bytes -> chars using CharsetDecoder
       |
       v  wrap
BufferedReader  -- adds readLine(), char-buffering
```

- **JVM Behaviour**: Stream I/O calls are blocking native calls from the calling Java thread's perspective, occupying an OS thread for the syscall's duration; buffering decorators reduce the *number* of such native calls (a major real-world performance factor) but don't change their blocking nature — true non-blocking behavior requires the NIO channel/selector model instead.

### Interview Questions

**Basic**
1. What is the fundamental difference between the `InputStream`/`OutputStream` hierarchy and the `Reader`/`Writer` hierarchy?
2. Why is wrapping a raw `FileInputStream` in a `BufferedInputStream` almost always recommended?

**Intermediate**
3. What role does `InputStreamReader` play, and why can specifying the wrong `Charset` cause garbled text?
4. What design pattern do `BufferedInputStream`, `GZIPInputStream`, and similar wrapper classes implement, and what benefit does this provide?

**Advanced**
5. Why can decoding UTF-8 text incrementally from a byte stream be tricky if not handled carefully (e.g., naive fixed-size byte-array reads combined with manual `new String(bytes)` conversion per chunk)?

**Scenario-based**
6. A developer reads a large text file one byte at a time using a raw (unbuffered) `FileInputStream`, converting each byte to a `char` manually, and the code is extremely slow, plus produces garbled output for non-ASCII characters. Diagnose both problems and the fix.

### Detailed Answers

1. `InputStream`/`OutputStream` operate on raw `byte`/`byte[]` data with no inherent understanding of text encoding — appropriate for arbitrary binary data. `Reader`/`Writer` operate on `char`/`char[]` data, with built-in (or explicitly specified) character-encoding conversion when bridging to/from an underlying byte source/sink — appropriate specifically for text, since it correctly handles multi-byte character encodings like UTF-8 rather than treating text as raw, encoding-agnostic bytes.
2. A raw `FileInputStream` (or any unbuffered stream) typically issues one native read syscall per `read()` call (or per small `read(byte[])` call sized smaller than the OS's efficient block size), which is extremely inefficient for byte-at-a-time or small-chunk consumption patterns due to syscall overhead. `BufferedInputStream` interposes an internal buffer that reads a larger chunk from the OS in one syscall and then satisfies many subsequent small `read()` calls directly from that in-memory buffer, dramatically reducing the number of actual syscalls and improving throughput.
3. `InputStreamReader` bridges a byte-oriented `InputStream` to a character-oriented `Reader`, using a specified (or platform-default, which is best avoided for portability) `Charset` to decode the incoming bytes into the correct `char` sequence. If the wrong charset is specified (e.g., decoding UTF-8-encoded bytes as if they were ISO-8859-1), multi-byte character sequences get misinterpreted as multiple separate single-byte characters (or vice versa), producing garbled/incorrect text output — the classic "mojibake" bug.
4. These classes implement the **Decorator** design pattern — each wrapper class implements the same base interface/class (`InputStream`) while wrapping another instance of that same type and adding its own behavior (buffering, decompression, object deserialization) before/after delegating to the wrapped instance. This allows arbitrary combinations of behavior to be composed by nesting wrappers (e.g., `new GZIPInputStream(new BufferedInputStream(new FileInputStream(...)))`) without requiring a combinatorial explosion of subclasses for every possible combination of features.
5. A multi-byte UTF-8 character sequence can be split across the boundary of two separate fixed-size byte-array reads (e.g., a 3-byte UTF-8 character where the first 2 bytes land in one `read()` call's buffer and the last byte lands in the next). Naively converting each chunk independently via `new String(bytesChunk, "UTF-8")` would then incorrectly decode the split character (producing replacement characters or corrupted text at chunk boundaries), because `String`'s constructor has no memory of a partial sequence from the previous chunk. Correct incremental decoding requires a stateful `CharsetDecoder` (which `InputStreamReader` uses internally) that can carry over partial multi-byte sequences across read calls.
6. Reading one byte at a time via a raw, unbuffered `FileInputStream` triggers a native syscall (or close to it) per byte, which is extremely slow due to per-call overhead multiplied across the entire file's byte count — the fix is wrapping the stream in a `BufferedInputStream` (or better, using a `Reader`-based approach entirely) to batch reads into much larger chunks. Separately, manually converting individual bytes to `char` values (e.g., via a direct numeric cast) completely ignores character encoding — for any non-ASCII/multi-byte-encoded text (like UTF-8), this produces garbled output, because a proper `char` can require assembling multiple bytes together; the fix for both problems simultaneously is to use `new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))`, which handles both efficient buffering and correct, stateful multi-byte character decoding.

### Code Examples

```java
import java.io.*;
import java.nio.charset.StandardCharsets;

public class LayeredStreamDemo {
    public static void main(String[] args) throws IOException {
        byte[] rawData = "id,amount\n1,199.99\n2,49.50\n".getBytes(StandardCharsets.UTF_8);

        // Binary stream layer: decorator pattern composing behavior
        try (InputStream raw = new ByteArrayInputStream(rawData);
             BufferedInputStream buffered = new BufferedInputStream(raw);
             Reader charReader = new InputStreamReader(buffered, StandardCharsets.UTF_8);
             BufferedReader lineReader = new BufferedReader(charReader)) {

            String line;
            while ((line = lineReader.readLine()) != null) {
                System.out.println("Row: " + line);
            }
        }
    }
}
```

```java
import java.io.*;
import java.util.zip.GZIPOutputStream;
import java.nio.charset.StandardCharsets;

public class CompressedWriteDemo {
    public static void main(String[] args) throws IOException {
        File output = new File("audit-log.gz");

        // Composing buffering + compression + character encoding via decorator layering
        try (OutputStream fileOut = new FileOutputStream(output);
             OutputStream gzipOut = new GZIPOutputStream(fileOut);
             Writer writer = new OutputStreamWriter(gzipOut, StandardCharsets.UTF_8);
             BufferedWriter bufferedWriter = new BufferedWriter(writer)) {

            bufferedWriter.write("2026-07-26T10:00:00Z - user 42 logged in");
            bufferedWriter.newLine();
        }

        System.out.println("Compressed size: " + output.length() + " bytes");
    }
}
```

## Standard I/O Streams (`System.in`, `System.out`, `System.err`) *(new)*

### Theory

- **Core Concepts**: Every JVM process automatically has three pre-connected standard I/O streams, mirroring the OS-level standard streams: `System.in` (an `InputStream` for standard input, typically the keyboard/terminal or a redirected file/pipe), `System.out` (a `PrintStream` for standard output, typically the terminal or a redirected file/pipe), and `System.err` (a `PrintStream` for standard error, conventionally used for diagnostic/error messages, kept separate from `System.out` so error output can be redirected independently).
- **Internal Working**: These are `static final` fields on `java.lang.System`, initialized by the JVM at startup from the underlying OS-provided file descriptors (0, 1, 2 on POSIX systems); `System.setOut`/`setErr`/`setIn` allow reassigning them at runtime (used, for example, by testing frameworks to capture console output).
- **When to Use It**: `System.out`/`System.err` for command-line tool output and diagnostic messages (though structured logging frameworks like SLF4J/Logback are strongly preferred for real applications); `System.in` for simple interactive command-line input (often wrapped in a `Scanner` or `BufferedReader` for convenient parsing).
- **Advantages**: Zero setup required — always available in every JVM process; conventional separation of `out` (normal output) versus `err` (diagnostics) enables shell-level redirection (`program 1>out.log 2>err.log`) without any application code changes.
- **Limitations**: `System.out.println` is unbuffered-by-default in terms of behavior expectations (it auto-flushes on newline, which is convenient for interactivity but can be a performance bottleneck for high-volume output — wrapping in an explicit `BufferedOutputStream`/using a `PrintWriter` with buffering disabled auto-flush can help); using `System.out` directly for application logging (instead of a proper logging framework) loses features like log levels, structured output, rotation, and centralized configuration.

### Internal Working

- **Step-by-Step Explanation**: 1) At JVM startup, native bootstrap code opens/wraps the OS's inherited standard file descriptors (stdin=0, stdout=1, stderr=2) as Java-level stream objects, assigned to the `System.in`/`out`/`err` static fields before any application code (including `main`) runs. 2) `System.out`/`System.err` are actually `PrintStream` instances (a subclass of `FilterOutputStream` adding convenience `print`/`println` methods that never throw checked `IOException`, instead setting an internal error flag checkable via `checkError()`), typically wrapping a `BufferedOutputStream` internally with auto-flush enabled on newline characters for `println`. 3) `System.setOut(PrintStream)`/`setErr`/`setIn` perform a privileged, security-manager-checked (historically) reassignment of these static fields, commonly used by test frameworks to redirect and capture console output for assertions.
- **Memory Layout**: Not directly applicable beyond ordinary object references; these are simply long-lived static objects referencing OS-level file descriptors for the life of the JVM process.
- **Diagrams**:

```
OS process: fd 0 (stdin) --- fd 1 (stdout) --- fd 2 (stderr)
                |                  |                  |
                v                  v                  v
          System.in          System.out          System.err
        (InputStream)       (PrintStream)       (PrintStream)

shell redirection: java MyApp 1>output.log 2>errors.log
   -> stdout/stderr transparently redirected at the OS level, no code change needed
```

- **JVM Behaviour**: Writes to `System.out`/`err` ultimately invoke native write syscalls against the underlying file descriptor (terminal, redirected file, or pipe); when stdout is redirected to a file/pipe rather than an interactive terminal, the OS/JVM I/O behavior around buffering can differ subtly (fully-buffered versus line-buffered), which is why explicit flushing matters more in some contexts.

### Interview Questions

**Basic**
1. What are `System.in`, `System.out`, and `System.err`, and how are they initialized?
2. Why does Java provide a separate `System.err` instead of just using `System.out` for everything?

**Intermediate**
3. What type of object is `System.out`, and why do its `print`/`println` methods never throw a checked `IOException`?
4. How would you redirect what `System.out.println` writes to, purely within Java code (not via shell redirection), and why might a test framework need this?

**Advanced**
5. Why is using `System.out.println` for application logging in production code generally discouraged in favor of a logging framework?

**Scenario-based**
6. A command-line tool prints large volumes of output very slowly to the terminal, and a developer suspects flushing behavior. What would you check/change to improve throughput?

### Detailed Answers

1. `System.in`, `System.out`, and `System.err` are `static final` fields on `java.lang.System` representing the JVM process's standard input, standard output, and standard error streams respectively. They are initialized by the JVM's native startup code before `main()` runs, wrapping the OS-level standard file descriptors (0, 1, 2) inherited from the process that launched the JVM.
2. Separating diagnostic/error output (`System.err`) from normal program output (`System.out`) allows each to be independently redirected at the shell/OS level (e.g., `program > output.txt 2> errors.txt`), so that normal output can be piped into another program or saved to a file while error messages remain visible on the terminal (or vice versa) — an important convention for well-behaved command-line tools.
3. `System.out` (and `System.err`) is a `PrintStream`, a convenience wrapper designed specifically to make console-style output easier by catching any underlying `IOException` internally (setting an internal error flag, checkable via `checkError()`) rather than forcing every `print`/`println` call site to handle a checked exception — a deliberate ergonomic tradeoff since console output failures are rare and usually not meaningfully recoverable at the call site anyway.
4. You can call `System.setOut(new PrintStream(customOutputStream))` to redirect all subsequent `System.out` writes to a different destination (e.g., a `ByteArrayOutputStream` for in-memory capture). Test frameworks commonly do this to capture console output produced by the code under test, allowing assertions against exactly what was printed without needing to actually display it on a real console or modify the production code to accept an injectable output destination.
5. `System.out.println` provides none of the features expected of production logging: no log levels (DEBUG/INFO/WARN/ERROR) to filter verbosity, no structured/contextual metadata (timestamps, thread names, correlation IDs) automatically attached, no configurable output destinations/rotation/retention policies, and no centralized runtime control over verbosity across a large codebase. Logging frameworks (SLF4J with Logback/Log4j2, java.util.logging) provide all of this in a consistent, configurable way, making `System.out.println` acceptable only for quick debugging or genuinely simple command-line tool output, not production application logging.
6. The developer should check whether `System.out` is wrapped with excessive per-line auto-flushing overhead for high-volume output — `println` on the default `PrintStream` auto-flushes after each newline, which can be a bottleneck when printing large volumes of lines rapidly, since each flush may trigger a system call. A common fix is to wrap standard output in an explicit `PrintStream` backed by a `BufferedOutputStream` with auto-flush disabled (flushing manually or only periodically/at the end), significantly reducing the number of underlying flush/write syscalls for bulk output scenarios.

### Code Examples

```java
import java.io.PrintStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

public class StandardStreamRedirectionDemo {
    public static void main(String[] args) throws Exception {
        System.out.println("This goes to the console (or wherever stdout is redirected).");
        System.err.println("This goes to stderr - separately redirectable.");

        // Redirect subsequent System.out writes to a file, entirely within Java code
        PrintStream fileOut = new PrintStream(new BufferedOutputStream(new FileOutputStream("app.log")), true);
        System.setOut(fileOut);
        System.out.println("This line is written to app.log instead of the console.");
    }
}
```

```java
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class InteractiveInputDemo {
    public static void main(String[] args) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.print("Enter your name: ");
            String name = reader.readLine();
            System.out.println("Hello, " + name + "!");
        }
    }
}
```

## `Scanner`, `BufferedReader`, `BufferedWriter` *(new)*

### Theory

- **Core Concepts**: `Scanner` (`java.util.Scanner`) is a token-parsing utility that reads and converts primitive types and strings from a text source (a `Reader`, `InputStream`, `String`, or `File`) using configurable delimiters (whitespace by default) and regular-expression-based pattern matching. `BufferedReader`/`BufferedWriter` are the efficient, buffered wrappers around character `Reader`/`Writer` streams, providing `readLine()`/line-oriented I/O plus reduced syscall overhead versus unbuffered character streams.
- **Internal Working**: `Scanner` internally uses a `Pattern`/`Matcher`-based tokenizer over a buffered input source, which makes it flexible but comparatively slow for large-volume parsing compared to direct `BufferedReader.readLine()` combined with manual `split`/`parse` calls. `BufferedReader` maintains an internal `char[]` buffer, refilling it from the underlying `Reader` in larger chunks and satisfying `read()`/`readLine()` calls from that buffer.
- **When to Use It**: `Scanner` for convenient, readable parsing of loosely-structured or interactive input (competitive programming, simple CLI tools, quick prototyping) where performance is not critical. `BufferedReader`/`BufferedWriter` for performance-sensitive or large-volume text I/O, and whenever you need simple, fast line-by-line reading/writing.
- **Advantages**: `Scanner` offers a very convenient, type-safe API (`nextInt()`, `nextDouble()`, `hasNext()`) with configurable delimiters/locales for numeric parsing. `BufferedReader`/`BufferedWriter` offer significantly better raw throughput for large text files and a simple, minimal API (`readLine()`, `write(String)`, `newLine()`).
- **Limitations**: `Scanner` is measurably slower than `BufferedReader` for large-volume parsing (its regex-based tokenizing has real overhead) and its exception model (`InputMismatchException`, requiring `hasNextX()` checks to avoid) can be awkward for robust production parsing. `BufferedReader`/`BufferedWriter` provide no built-in type conversion — parsing/formatting must be done manually (e.g., `Integer.parseInt(line)`), and `BufferedWriter` requires an explicit `newLine()` call (platform-appropriate) rather than a convenient `println`.

### Internal Working

- **Step-by-Step Explanation**: 1) `new Scanner(source)` wraps the input source and lazily reads/buffers text as needed; each `next()`/`nextInt()`/etc. call advances an internal matcher against the configured delimiter pattern (default: whitespace regex) to extract the next token, then attempts to parse/convert it to the requested type, throwing `InputMismatchException` if the token doesn't match the expected format. 2) `hasNext()`/`hasNextInt()` perform a non-consuming lookahead check against the same tokenizing logic, letting callers safely test before consuming. 3) `BufferedReader.readLine()` scans its internal buffer for a line terminator (`\n`, `\r`, or `\r\n`, normalizing all three), refilling the buffer from the underlying `Reader` via a native/decoding read when exhausted, and returns the line content without the terminator (or `null` at end-of-stream). 4) `BufferedWriter.write(String)` appends characters to its internal buffer, only flushing to the underlying `Writer` (and ultimately performing a native write) when the buffer fills or `flush()`/`close()` is explicitly called.
- **Memory Layout**: Both `Scanner` and `BufferedReader`/`BufferedWriter` maintain internal heap-allocated `char[]` buffers (a default size on the order of a few KB, though `Scanner`'s buffer management for large tokens can grow as needed); no off-heap/native memory is involved for these classes themselves (they sit atop whatever underlying stream — which might itself involve native I/O — they wrap).
- **Diagrams**:

```
Scanner:  raw text -> Pattern/Matcher tokenizer (regex-based) -> next()/nextInt()/hasNext()

BufferedReader:  underlying Reader -> internal char[] buffer -> readLine() scans buffer for \n/\r
                 (refills buffer via underlying Reader only when exhausted)

BufferedWriter:  write(String) -> appended to internal char[] buffer -> flush() when full/closed
                 (reduces number of actual native writes to the underlying Writer/stream)
```

- **JVM Behaviour**: Both are ordinary buffered decorators over character streams with no special JVM-level treatment; `Scanner`'s regex-based tokenizing does incur real CPU cost (compiled `Pattern` matching per token) that shows up meaningfully in profiles for very large-volume text parsing, which is why performance-critical or competitive-programming-style large-input parsing favors `BufferedReader` with manual `split`/`parse` logic instead.

### Interview Questions

**Basic**
1. What is the main purpose of `Scanner`, and what is the default delimiter it uses between tokens?
2. What does `BufferedReader.readLine()` return at the end of the stream?

**Intermediate**
3. Why is `BufferedReader` generally faster than `Scanner` for parsing large text files?
4. Why must you call `newLine()` (or manually append a line separator) with `BufferedWriter`, rather than relying on an automatic newline like `println`?

**Advanced**
5. What exception does `Scanner.nextInt()` throw if the next token isn't a valid integer, and how would you use `hasNextInt()` to avoid it?

**Scenario-based**
6. You're writing a program to process a multi-gigabyte log file line by line, extracting a few numeric fields per line. Would you choose `Scanner` or `BufferedReader`, and why?

### Detailed Answers

1. `Scanner`'s main purpose is convenient parsing of primitive types and tokens from a text source, using a configurable delimiter (whitespace by default, via a regex pattern) to split the input into tokens that can then be consumed via type-specific methods like `nextInt()`, `nextDouble()`, or `next()` for plain strings.
2. `BufferedReader.readLine()` returns `null` once the end of the underlying stream has been reached and there is no more content to read, which is the conventional way to detect end-of-stream in a `while ((line = reader.readLine()) != null)` loop.
3. `BufferedReader.readLine()` performs straightforward buffer scanning for line-terminator characters, which is computationally cheap. `Scanner`, by contrast, uses `Pattern`/`Matcher`-based regular expression tokenizing for every token extraction (even for simple whitespace-delimited tokens), which carries meaningfully higher CPU overhead per token when processing large volumes of text, making `BufferedReader` (combined with manual parsing like `Integer.parseInt` on split tokens) the faster choice for large-scale text processing.
4. `BufferedWriter` is a low-level, general-purpose buffered character writer with no inherent knowledge of "print a value followed by a line break" semantics the way `PrintWriter.println` provides; it exposes `newLine()` specifically to write the platform-appropriate line separator (respecting the `line.separator` system property, which differs between Windows `\r\n` and Unix `\n`) consistently, rather than requiring the caller to hardcode a specific separator that might be wrong for the target platform.
5. `Scanner.nextInt()` throws `InputMismatchException` if the next token cannot be parsed as an integer (e.g., it's plain text or a decimal number). `hasNextInt()` performs a non-consuming lookahead to check whether the next token *would* successfully parse as an integer, letting you write `if (scanner.hasNextInt()) { int value = scanner.nextInt(); } else { /* handle unexpected input */ }`, avoiding the exception entirely for expected "wrong format" cases rather than relying on exception-based control flow.
6. `BufferedReader` is the better choice for processing a multi-gigabyte log file line by line with numeric field extraction, since `Scanner`'s regex-based tokenizing overhead becomes significant at that scale, and simple line-based reading combined with manual `String.split(...)` plus `Integer.parseInt`/`Long.parseLong` on the relevant fields is both faster and gives more explicit control over parsing/error handling for malformed lines, which is important when processing large real-world log data that may contain occasional unexpected formats.

### Code Examples

```java
import java.util.Scanner;

public class ScannerParsingDemo {
    public static void main(String[] args) {
        String input = "widget 3 19.99 gadget 1 49.50";
        Scanner scanner = new Scanner(input);

        double total = 0;
        while (scanner.hasNext()) {
            String name = scanner.next();
            if (scanner.hasNextInt()) {
                int quantity = scanner.nextInt();
                if (scanner.hasNextDouble()) {
                    double price = scanner.nextDouble();
                    total += quantity * price;
                    System.out.printf("%s: %d x $%.2f%n", name, quantity, price);
                }
            }
        }
        System.out.printf("Total: $%.2f%n", total);
    }
}
```

```java
import java.io.*;
import java.nio.charset.StandardCharsets;

public class LargeFileLineProcessingDemo {
    static long sumFirstNumericField(String path) throws IOException {
        long sum = 0;
        // BufferedReader + manual parsing: fastest approach for large-volume line processing
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length > 0) {
                    try {
                        sum += Long.parseLong(fields[0].trim());
                    } catch (NumberFormatException ignored) {
                        // skip malformed lines rather than aborting the whole batch
                    }
                }
            }
        }
        return sum;
    }

    public static void main(String[] args) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("nums.csv"))) {
            for (int i = 1; i <= 5; i++) {
                writer.write(i + ",note-" + i);
                writer.newLine(); // platform-correct line separator, unlike a hardcoded "\n"
            }
        }
        System.out.println("Sum: " + sumFirstNumericField("nums.csv"));
    }
}
```
