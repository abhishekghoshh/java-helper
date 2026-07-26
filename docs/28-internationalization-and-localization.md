# 28. Internationalization & Localization *(new)*

## `Locale`

### Theory

**Core Concepts**
A `Locale` (`java.util.Locale`) represents a specific geographical, political, or cultural region — typically a combination of a language code (ISO 639, e.g. `en`), a country/region code (ISO 3166, e.g. `US`), and optionally a variant, script, or extension tags (BCP 47, e.g. `en-US-u-nu-thai`). It does not itself perform any formatting or translation — it is a *key* that other i18n classes (`ResourceBundle`, `NumberFormat`, `DateFormat`, `Collator`, `MessageFormat`) use to decide which culturally-specific data or algorithm to apply. Java's internationalization model is built around the separation of "what to display" (locale-sensitive data) from "how to compute it" (locale-neutral logic).

**Internal Working**
`Locale` instances are effectively immutable value objects built from language/country/variant/script fields plus optional Unicode BCP 47 extensions; the JDK caches common locales and uses them as lookup keys into `ResourceBundle.Control` and provider SPIs (`LocaleServiceProvider`) that supply locale-specific formatting data from CLDR (Common Locale Data Repository).

**When to Use It**
- Any application that must present dates, numbers, currency, or text in a way appropriate to the end user's culture/region.
- Selecting the correct `ResourceBundle` for translated strings.
- Passing to `String.format(Locale, ...)`, `Collator.getInstance(Locale)`, `NumberFormat.getInstance(Locale)`, etc., instead of relying on the JVM default locale (which can vary across environments and is not thread-safe to rely on implicitly in server applications).

**Advantages**
- Standardized (BCP 47 / IANA) representation, interoperable with HTTP `Accept-Language` headers, OS locale settings, and other systems.
- Extensible via `Locale.Builder` and extension tags (e.g. calendar type, numbering system) without inventing custom formats.
- Backed by CLDR, so formatting rules stay current with real-world locale conventions without embedding logic in application code.

**Limitations**
- `Locale.getDefault()` is a JVM-wide (or thread-category-specific since Java 7 with `Locale.Category`) mutable global — relying on it implicitly in multi-tenant server code is a common source of subtle bugs (one request's locale leaking into another's response).
- Constructing arbitrary `new Locale("xx", "YY")` strings does not validate that the language/country codes are real; `Locale.Builder` performs stricter validation.
- Locale matching (e.g. via `Locale.filter`/`Locale.lookup` with language ranges) has non-trivial fallback rules that are easy to get wrong (e.g. `en_US` not automatically falling back to `en` unless you build the fallback chain yourself or use `ResourceBundle.Control`).

### Internal Working

**Step-by-Step Explanation**
1. A `Locale` is obtained either via a constant (`Locale.US`, `Locale.JAPAN`), `Locale.forLanguageTag("en-US")`, the multi-part constructor, or `new Locale.Builder().setLanguage("en").setRegion("US").build()`.
2. The locale is passed into a locale-sensitive factory method, e.g. `NumberFormat.getCurrencyInstance(locale)`.
3. Internally, the factory consults an ordered list of `LocaleServiceProvider` implementations (JDK's built-in CLDR-based provider by default, or a custom SPI registered via `META-INF/services`) to find a matching implementation for that locale.
4. If no exact match exists, the JDK applies locale fallback (stripping variant, then country, then falling back to the default locale's resources or root/invariant data).
5. The resulting formatter/collator object encapsulates locale-specific rules (digit symbols, currency symbol placement, sort order, etc.) and is used repeatedly — these objects are generally **not thread-safe** (`NumberFormat`, `DateFormat`), so they should be created per-use or per-thread (e.g. `ThreadLocal`), unlike `Locale` itself which is immutable and thread-safe.

**Memory Layout**
Not directly applicable to heap generations — a `Locale` object is a small, typically long-lived, immutable object; the JVM interns/caches common instances (`Locale.US`, etc.) as static final fields, so they behave like other long-lived objects promoted to the old generation, with negligible GC pressure.

**Diagrams**
```
Request "Accept-Language: fr-CA"          Locale.forLanguageTag("fr-CA")
        │                                          │
        ▼                                          ▼
  Locale{language=fr, country=CA} ───► NumberFormat.getInstance(locale)
        │                                          │
        ▼                                          ▼
  Fallback chain: fr_CA → fr → root(invariant) ──► CLDR data lookup ──► formatted output
```

**JVM Behaviour**
No special bytecode or JIT behavior — `Locale` is ordinary immutable object usage. The JVM default locale is initialized once at startup from OS/`user.language`/`user.country` system properties and cached in a static field; `Locale.setDefault()` mutates this global cache and is visible to all threads (a potential concurrency hazard in servers handling multiple locales concurrently — prefer passing `Locale` explicitly rather than mutating the default).

### Interview Questions

**Basic**
1. What is a `Locale` and what three main components can it be composed of?
2. How do you obtain a `Locale` for "German as spoken in Switzerland"?
3. What is the difference between `Locale.getDefault()` and passing an explicit `Locale` to a formatter?

**Intermediate**
4. How does the JDK perform locale fallback when an exact match isn't available?
5. Why is `Locale.setDefault()` risky in a multi-tenant web server?
6. What is the difference between `new Locale("en", "US")` and `Locale.forLanguageTag("en-US")`?

**Advanced**
7. How would you implement a custom `LocaleServiceProvider` to supply formatting data for a locale not covered by the JDK?
8. Explain `Locale.Category` (`DISPLAY` vs `FORMAT`) and why it was introduced in Java 7.
9. How does `Locale.filter`/`Locale.lookup` (RFC 4647 language range matching) differ from simple fallback in `ResourceBundle`?

**Scenario-based**
10. A Spring Boot REST API serving users worldwide currently calls `Locale.setDefault()` per request based on the `Accept-Language` header. What bugs could this cause under concurrent load, and how would you redesign it?

### Detailed Answers

1. **What is a `Locale` and what three main components can it be composed of?**
   A `Locale` represents a specific linguistic/geographic/cultural context, used as a lookup key by i18n-aware classes. It is primarily composed of a language code (ISO 639, lower-case, e.g. `en`), an optional country/region code (ISO 3166, upper-case, e.g. `US`), and an optional variant, plus modern BCP 47 support for script and Unicode extension subtags (e.g. numbering system).

2. **How do you obtain a `Locale` for "German as spoken in Switzerland"?**
   `Locale swissGerman = new Locale.Builder().setLanguage("de").setRegion("CH").build();` or equivalently `Locale.forLanguageTag("de-CH")`. The older `new Locale("de", "CH")` constructor still works but performs no tag validation.

3. **What is the difference between `Locale.getDefault()` and passing an explicit `Locale` to a formatter?**
   `Locale.getDefault()` reads a JVM-wide (or category-scoped) mutable static field set at startup from OS/system properties, which can be changed at runtime by any code calling `setDefault()` — dangerous in concurrent contexts. Passing an explicit `Locale` (e.g. resolved per-request from an `Accept-Language` header) makes locale selection deterministic, thread-safe, and testable, decoupled from global JVM state.

4. **How does the JDK perform locale fallback when an exact match isn't available?**
   The lookup/fallback algorithm progressively strips specificity: `language_COUNTRY_VARIANT` → `language_COUNTRY` → `language` → root/invariant bundle. `ResourceBundle.Control` (or the default `ResourceBundle.Control.getControl`) implements this candidate-locale list generation; formatters use similar CLDR data fallback internally.

5. **Why is `Locale.setDefault()` risky in a multi-tenant web server?**
   It mutates a single JVM-wide static (or, since Java 7, per-`Category` static) value shared by all threads. In a server handling concurrent requests for different users/locales, one thread calling `setDefault()` can change formatting/parsing behavior seen by other in-flight requests, causing intermittent, hard-to-reproduce bugs (e.g. wrong currency symbol shown to a different user). The fix is to thread the `Locale` explicitly through method parameters rather than relying on global default state.

6. **What is the difference between `new Locale("en", "US")` and `Locale.forLanguageTag("en-US")`?**
   Functionally they produce an equivalent locale for simple cases, but `forLanguageTag` parses a standards-compliant BCP 47 tag (validating structure, normalizing case, supporting script/extension subtags, and mapping legacy tags), while the constructor does no validation and predates BCP 47 support — it simply stores whatever strings are passed. `Locale.Builder`/`forLanguageTag` is the modern, recommended approach.

7. **How would you implement a custom `LocaleServiceProvider` to supply formatting data for a locale not covered by the JDK?**
   Implement one of the SPI subclasses (e.g. `java.text.spi.NumberFormatProvider`, `DateFormatProvider`, `BreakIteratorProvider`), override `getAvailableLocales()` and the relevant factory methods to return locale-specific implementations, then register the provider class name in `META-INF/services/java.text.spi.NumberFormatProvider` (or use `java.util.ServiceLoader` semantics under the module system with a `provides ... with ...` directive in `module-info.java`). The JDK's `NumberFormat.getInstance(locale)` will discover and delegate to it via `ServiceLoader` when no built-in provider matches.

8. **Explain `Locale.Category` (`DISPLAY` vs `FORMAT`) and why it was introduced in Java 7.**
   `Locale.Category` lets an application maintain two independent default locales: `DISPLAY` (used for menus, dialogs, UI labels) and `FORMAT` (used for parsing/formatting dates, numbers, currency). This separation allows, for example, a UI localized in English while numeric/date formatting still follows the user's regional (e.g. French) conventions — `Locale.setDefault(Locale.Category.FORMAT, frenchLocale)` without affecting the UI language default.

9. **How does `Locale.filter`/`Locale.lookup` (RFC 4647 language range matching) differ from simple fallback in `ResourceBundle`?**
   `ResourceBundle`'s fallback is a fixed truncation chain from the most specific requested locale down to root. `Locale.filter`/`Locale.lookup` implement RFC 4647 "basic filtering"/"lookup" semantics against a *list of user-supplied priority ranges* (e.g. parsed straight from an HTTP `Accept-Language` header with `Locale.LanguageRange.parse`), returning the best-matching locale(s) from a candidate set — useful for content-negotiation scenarios where you must pick the best of several *available* locales, not just fall back within one bundle family.

10. **A Spring Boot REST API currently calls `Locale.setDefault()` per request — what bugs and redesign?**
    Because `Locale.setDefault()` mutates shared JVM state, concurrent requests can interleave: request A sets default to `fr_FR`, request B (different thread, same time window) sets it to `ja_JP`, and depending on scheduling, thread A's response may be formatted using thread B's locale — a classic race condition, worse under a shared thread pool with request pipelining. The fix: never call `setDefault()` per request; instead resolve the desired `Locale` from `Accept-Language` (e.g. via `LocaleResolver`/`LocaleContextHolder` in Spring, which is backed by a `ThreadLocal`, not a JVM-global) and pass it explicitly to every `NumberFormat`/`DateTimeFormatter`/`MessageSource` call for that request.

### Code Examples

```java
import java.util.Locale;
import java.text.NumberFormat;

public class LocaleDemo {
    public static void main(String[] args) {
        // Build locales explicitly rather than relying on JVM default
        Locale us = Locale.forLanguageTag("en-US");
        Locale germany = Locale.forLanguageTag("de-DE");
        Locale swissGerman = new Locale.Builder().setLanguage("de").setRegion("CH").build();

        double price = 1234.5;
        for (Locale locale : new Locale[] { us, germany, swissGerman }) {
            NumberFormat currency = NumberFormat.getCurrencyInstance(locale);
            System.out.println(locale.toLanguageTag() + " -> " + currency.format(price));
        }

        // RFC 4647 style negotiation: pick best match from what the app supports
        var supported = java.util.List.of(Locale.forLanguageTag("en"), Locale.forLanguageTag("fr"), Locale.forLanguageTag("de"));
        var ranges = Locale.LanguageRange.parse("fr-CA,fr;q=0.9,en;q=0.5");
        Locale best = Locale.lookup(ranges, supported);
        System.out.println("Best match for Accept-Language header: " + best);
    }
}
```

## `ResourceBundle`

### Theory

**Core Concepts**
`ResourceBundle` (`java.util.ResourceBundle`) is an abstraction for loading locale-specific key/value data — typically translated UI strings — decoupled from application logic. Concrete forms are `PropertyResourceBundle` (backed by `.properties` files, e.g. `Messages_en_US.properties`) and `ListResourceBundle`/programmatic subclasses (backed by Java code returning an `Object[][]`). At runtime, `ResourceBundle.getBundle(baseName, locale)` resolves the most specific bundle file available for the requested locale, falling back progressively toward a base/default bundle.

**Internal Working**
`getBundle` builds a candidate list of locales (most specific → least specific) via `ResourceBundle.Control`, attempts to load each corresponding `.properties`/class resource from the classpath (or a custom source if a `Control`/`ResourceBundleProvider` SPI is supplied), caches successfully loaded bundles in an internal soft-reference cache keyed by (baseName, locale, classloader), and chains bundles together via `setParent()` so `getString()` transparently searches parent bundles for missing keys.

**When to Use It**
- Externalizing all user-facing text so translators can work on `.properties` files without touching code.
- Applications supporting multiple languages/regions where the same key set needs locale-specific values (error messages, labels, email templates).
- Combined with `MessageFormat` for parameterized, grammatically-correct translated messages (plurals, ordering of substituted values).

**Advantages**
- Automatic fallback chain reduces the need for translators to duplicate every key in every locale variant.
- Pluggable loading strategy (custom `Control`, or since Java 9 `ResourceBundleProvider` SPI, module-friendly) allows bundles to come from databases, remote services, or non-classpath sources.
- Built-in caching avoids repeated file parsing.

**Limitations**
- Default `.properties` files are read as ISO-8859-1 by legacy `PropertyResourceBundle` unless using `PropertyResourceBundle`'s UTF-8-aware constructor/`Properties.load(Reader)` path (Java 9+ default encoding for bundles changed to UTF-8, but be careful with older toolchains and `native2ascii` workflows).
- No built-in pluralization/grammar handling — `MessageFormat.choice`/`ChoiceFormat` or a library like ICU4J is needed for complex plural rules.
- The bundle cache is keyed partly by classloader and can cause stale-bundle issues in hot-reload/plugin environments unless `ResourceBundle.clearCache()` is used.
- Missing-key lookups throw `MissingResourceException` at runtime rather than compile time — there is no static verification that all locales define all keys.

### Internal Working

**Step-by-Step Explanation**
1. `ResourceBundle.getBundle("Messages", locale)` is called.
2. The default `Control` generates a candidate locale list, e.g. for `Locale("fr", "CA")`: `fr_CA`, `fr`, root (empty).
3. For each candidate, the loader looks for `Messages_fr_CA.class`, then `Messages_fr_CA.properties`, then repeats for `fr`, then for the base `Messages.properties`.
4. The first bundle found becomes the primary; subsequent, less-specific bundles found are linked as parents via `setParent()`, forming a chain (`fr_CA` → `fr` → root).
5. `bundle.getString("greeting")` looks up the key in the primary bundle; if absent, delegates to `parent.getString(...)` recursively until found or `MissingResourceException` is thrown.
6. Loaded bundles are cached (soft references) keyed by base name, locale, and the calling classloader, so repeated `getBundle` calls are cheap.

**Memory Layout**
Not directly applicable to stack/heap generation distinctions beyond ordinary object lifetime; the internal bundle cache uses `SoftReference`s so cached bundles can be reclaimed under memory pressure before an `OutOfMemoryError`, then reloaded on next access.

**Diagrams**
```
getBundle("Messages", fr_CA)
        │
        ▼
 candidate locales: [fr_CA, fr, ""(root)]
        │
        ▼
 Messages_fr_CA.properties  found?  → primary bundle
        │ no
        ▼
 Messages_fr.properties      found?  → primary bundle, parent = fr_CA (if partially found) 
        │ no
        ▼
 Messages.properties (root)  → base bundle, parent chain terminus

Lookup chain for getString("key"):
 fr_CA bundle --(miss)--> fr bundle --(miss)--> root bundle --(miss)--> MissingResourceException
```

**JVM Behaviour**
Loading `.properties`-backed bundles goes through the classloader's resource-loading path (`getResourceAsStream`), which is subject to normal classpath/module visibility rules; under the Java Platform Module System, resource bundles must be either open packages or accessed via the `ResourceBundleProvider` SPI (`module-info.java` `uses`/`provides`) since Java 9, because reflective/resource access to non-open packages is restricted.

### Interview Questions

**Basic**
1. What is the difference between `PropertyResourceBundle` and `ListResourceBundle`?
2. How does `ResourceBundle.getBundle` decide which file to load for a given locale?
3. What exception is thrown when a key is missing from all bundles in the fallback chain?

**Intermediate**
4. How would you structure `.properties` files to support English (default), French, and French-Canadian variants efficiently?
5. Why might `.properties` file encoding matter, and how do you handle non-ASCII translated text?
6. How does `ResourceBundle` caching work, and how can stale cached bundles be invalidated?

**Advanced**
7. How do you supply resource bundles from a non-classpath source (e.g. a database) using a custom `Control` or `ResourceBundleProvider`?
8. How does the Java Platform Module System change resource bundle loading, and what is `ResourceBundleProvider` used for?
9. How would you integrate `ResourceBundle` with `MessageFormat` to handle parameterized, pluralized messages correctly across locales?

**Scenario-based**
10. Your team ships translated `.properties` files that are frequently out of sync (some locales missing new keys added for a feature). How would you catch this at build time rather than in production?

### Detailed Answers

1. **Difference between `PropertyResourceBundle` and `ListResourceBundle`?**
   `PropertyResourceBundle` loads key/value pairs from a `.properties` text file (simple, translator-friendly, but string-only values). `ListResourceBundle` is a Java class you write that returns an `Object[][]` of key/value pairs — compiled, type-safe, can hold arbitrary objects (not just strings), but requires a code change/recompile/redeploy to update translations, making it less suitable for external translator workflows.

2. **How does `getBundle` decide which file to load?**
   It builds an ordered candidate-locale list from most to least specific (language+country+variant → language+country → language → root), using the `ResourceBundle.Control` in effect (default or custom), and returns the first bundle found on the classpath/module path for those candidates, linking any additional found bundles as a fallback parent chain.

3. **What exception when a key is missing everywhere in the chain?**
   `MissingResourceException` (unchecked, extends `RuntimeException`), thrown by `getString`/`getObject` when the key isn't found in the bundle or any of its parents.

4. **How to structure `.properties` files for English default + French + French-Canadian?**
   `Messages.properties` (root/default, typically English), `Messages_fr.properties` (French overrides/additions), `Messages_fr_CA.properties` (only the handful of Québec-specific overrides). Because of fallback, `fr_CA` need only contain keys that differ from `fr`, and `fr` need only contain keys that differ from the root — reducing translation duplication.

5. **Why does `.properties` encoding matter, and how to handle non-ASCII text?**
   Traditionally `.properties` files were read as ISO-8859-1, requiring non-Latin-1 characters to be encoded as `\uXXXX` escapes (often via the `native2ascii` tool). Since Java 9, `PropertyResourceBundle`/`Properties` default to reading `.properties` files as UTF-8, simplifying this — but teams must ensure their build tooling and `Properties.load` calls agree on encoding to avoid mojibake, especially in mixed old/new toolchains.

6. **How does bundle caching work, and how do you invalidate stale bundles?**
   `getBundle` caches loaded bundles in an internal cache (soft-referenced) keyed by base name, locale, and classloader, avoiding repeated I/O/parsing. To force a reload (e.g. after hot-swapping translation files in a plugin system), call `ResourceBundle.clearCache()` (clears entries for the caller's classloader) or `clearCache(ClassLoader)`.

7. **How to supply bundles from a non-classpath source using `Control` or `ResourceBundleProvider`?**
   Pre-Java 9: subclass `ResourceBundle.Control`, override `newBundle(baseName, locale, format, loader, reload)` to fetch data (e.g. from a database) and construct/return a `ResourceBundle` (often a custom subclass wrapping a `Map`), then call `ResourceBundle.getBundle(baseName, locale, control)`. Java 9+ modular approach: implement `ResourceBundleProvider`, declare `uses java.util.spi.ResourceBundleProvider;`/`provides ... with ...;` in `module-info.java`, and the module system uses `ServiceLoader` to discover the provider, which is the required approach for named modules since resource lookups across module boundaries are otherwise restricted.

8. **How does JPMS change resource bundle loading, and what is `ResourceBundleProvider` for?**
   Under JPMS, non-open packages are not reflectively/resource-accessible across module boundaries, so classpath-style `.properties` lookup inside another module's package may fail. `ResourceBundleProvider` (and its subtypes for properties/class-based bundles) lets a module explicitly export bundle-providing services via `provides`/`uses` directives, giving controlled, module-aware access instead of relying on open packages.

9. **How to integrate `ResourceBundle` with `MessageFormat` for pluralized/parameterized messages?**
   Store message *patterns* (not final strings) in the bundle, e.g. `itemCount={0,choice,0#no items|1#one item|1<{0,number} items}`, then at runtime do `new MessageFormat(bundle.getString("itemCount"), locale).format(new Object[]{count})`. This lets each locale's translator adjust word order, gender agreement, or plural rules within the pattern itself, rather than concatenating translated fragments in code (which breaks across languages with different grammar).

10. **How to catch out-of-sync translation keys at build time?**
    Write a build-time (Maven/Gradle) unit test or annotation-processor step that loads the root bundle plus every locale variant bundle, computes the key set for each, and asserts that every locale's key set is a superset (accounting for intentional fallback) of the keys added in the current release, failing the build if any translated bundle is missing new keys — often paired with a CI report of missing/extra keys per locale.

### Code Examples

```java
import java.util.*;

public class ResourceBundleDemo {
    public static void main(String[] args) {
        // Loads Messages_fr_CA.properties, falls back to Messages_fr.properties, then Messages.properties
        Locale frCA = Locale.forLanguageTag("fr-CA");
        ResourceBundle bundle = ResourceBundle.getBundle("Messages", frCA);

        System.out.println(bundle.getString("greeting"));

        // Parameterized, locale-aware message using a MessageFormat pattern stored in the bundle
        String pattern = bundle.getString("itemCount"); // e.g. "{0,choice,0#no items|1#one item|1<{0,number} items}"
        String formatted = new java.text.MessageFormat(pattern, frCA).format(new Object[]{3});
        System.out.println(formatted);

        // Programmatic bundle for cases without external files (e.g. small embedded default set)
        ResourceBundle fallback = new ListResourceBundle() {
            protected Object[][] getContents() {
                return new Object[][] { {"greeting", "Hello (fallback)"} };
            }
        };
        System.out.println(fallback.getString("greeting"));
    }
}
```

## `NumberFormat` / `DateFormat`

### Theory

**Core Concepts**
`NumberFormat` and `DateFormat` (`java.text` package) are abstract base classes providing locale-sensitive formatting and parsing of numbers/currency/percentages and dates/times respectively. `NumberFormat.getInstance()/getCurrencyInstance()/getPercentInstance()` and `DateFormat.getDateInstance()/getTimeInstance()/getDateTimeInstance()` return locale-appropriate concrete implementations (`DecimalFormat`, `SimpleDateFormat`) without the caller needing to know locale-specific patterns. Since Java 8, the modern `java.time` package's `DateTimeFormatter` is preferred over `DateFormat`/`SimpleDateFormat` for new code, but `DateFormat` remains prevalent in legacy codebases and APIs using `java.util.Date`.

**Internal Working**
These factory methods consult CLDR-backed locale data to select decimal/grouping separators, currency symbols and their placement, calendar systems, and month/day names, then construct a mutable formatter instance (`DecimalFormat` or `SimpleDateFormat`) preconfigured with a locale-specific pattern string; formatting/parsing subsequently uses that pattern with `java.math`/character-level algorithms.

**When to Use It**
- Formatting numeric/currency/percentage values for display to end users in their own locale conventions (`1,234.56` vs `1.234,56`).
- Parsing user-entered locale-formatted numbers/dates back into `double`/`Date` values.
- Legacy codebases still centered on `java.util.Date`/`Calendar` rather than `java.time`.

**Advantages**
- Removes the need to hand-write locale-specific formatting patterns (currency symbol position, thousands separators, calendar name translations).
- Parsing is symmetric with formatting — the same formatter instance can parse text it would have produced.

**Limitations**
- **Not thread-safe** — `DecimalFormat`/`SimpleDateFormat` instances mutate internal state during `format`/`parse`; sharing one instance across threads without synchronization causes data corruption. Common mitigations: create a new instance per use, or wrap in `ThreadLocal<DateFormat>`.
- `DateFormat`/`SimpleDateFormat` operate on the legacy `Date`/`Calendar` API, which has well-known design flaws (mutability, poor time-zone handling, 0-based months); `java.time.format.DateTimeFormatter` (immutable, thread-safe) is the modern replacement.
- Lenient parsing by default (`setLenient(true)`) can silently accept malformed dates (e.g. "Feb 30" rolling over to March 2) unless explicitly disabled.

### Internal Working

**Step-by-Step Explanation**
1. `NumberFormat.getCurrencyInstance(locale)` (or `DateFormat.getDateInstance(style, locale)`) looks up CLDR pattern/symbol data for the locale.
2. A concrete formatter (`DecimalFormat` with a `DecimalFormatSymbols`, or `SimpleDateFormat` with locale-specific month/day names) is constructed and returned.
3. `format(value)` walks the pattern, substituting digits/separators (or date/time fields) according to locale symbols, producing a `String`.
4. `parse(text)` does the reverse: it scans the input string according to the same pattern/symbols, accumulating a numeric or date value; malformed input throws `ParseException` (checked) unless lenient parsing "recovers" by reinterpreting it.
5. Because the returned formatter instance holds mutable internal parsing/formatting state (e.g. a shared `Calendar` field in `SimpleDateFormat`), concurrent calls to `format`/`parse` on the same instance from multiple threads can produce corrupted results without external synchronization.

**Memory Layout**
Not directly applicable beyond standard object lifetimes; formatter instances are typically short-lived (created per formatting operation or cached per-thread) — creating one per call in a hot loop can create GC churn, which is why per-thread caching (`ThreadLocal`) is a common performance optimization for high-throughput formatting code.

**Diagrams**
```
                 locale=de_DE                          locale=en_US
                     │                                      │
                     ▼                                      ▼
      DecimalFormat: "#,##0.00 €" style          DecimalFormat: "$#,##0.00" style
                     │                                      │
             format(1234.5)                          format(1234.5)
                     │                                      │
                 "1.234,50 €"                           "$1,234.50"
```

**JVM Behaviour**
No special JIT/GC-specific behavior beyond ordinary object allocation; heavy use of `SimpleDateFormat`/`DecimalFormat` in tight loops without reuse can increase allocation rate and minor GC frequency — profiling hot formatting code paths sometimes reveals this as a target for caching formatter instances per thread.

### Interview Questions

**Basic**
1. What is the difference between `NumberFormat.getInstance()` and `getCurrencyInstance()`?
2. Why is `SimpleDateFormat` considered unsafe to share across threads?
3. What checked exception does `parse()` throw, and why is it checked rather than unchecked?

**Intermediate**
4. How would you safely reuse a `SimpleDateFormat` instance across multiple threads?
5. What is the effect of `setLenient(false)` on `DateFormat`, and when would you use it?
6. How do you format a number as a percentage or in a specific currency for a given locale?

**Advanced**
7. Why does the JDK recommend migrating from `DateFormat`/`SimpleDateFormat` to `java.time.format.DateTimeFormatter`?
8. How does `DecimalFormat` support custom pattern symbols (grouping size, multiplier, custom prefix/suffix) beyond simple locale defaults?
9. How would you implement round-trip-safe parsing (parse(format(x)) == x) for currency values, considering rounding and locale-specific grouping?

**Scenario-based**
10. A high-throughput report-generation service formats millions of dates per minute using a single shared `static final SimpleDateFormat`. Diagnose the likely bug and propose a fix with minimal performance regression.

### Detailed Answers

1. **Difference between `getInstance()` and `getCurrencyInstance()`?**
   `NumberFormat.getInstance(locale)` returns a general-purpose decimal formatter (grouping separators, locale-appropriate decimal point) suitable for plain numbers. `getCurrencyInstance(locale)` returns a formatter preconfigured with the locale's currency symbol, correct symbol placement (prefix/suffix), and the currency's standard number of fraction digits (e.g. 2 for USD, 0 for JPY).

2. **Why is `SimpleDateFormat` unsafe to share across threads?**
   It's a mutable class — internally it uses a shared `Calendar` instance and mutable working buffers during `format`/`parse` calls. If two threads call `format`/`parse` concurrently on the same instance, their operations can interleave and corrupt each other's intermediate state, producing wrong or inconsistent results (not always an exception — often silently wrong output, which is more dangerous).

3. **What checked exception does `parse()` throw, and why checked?**
   `java.text.ParseException`. It's checked because malformed user input (e.g. free-text date entry) is an expected, recoverable failure mode that callers are forced to explicitly handle (show a validation error, retry, etc.) rather than let propagate as an unchecked runtime failure.

4. **How to safely reuse `SimpleDateFormat` across threads?**
   Options: (a) create a new instance per use (simplest, some allocation overhead); (b) wrap it in a `ThreadLocal<SimpleDateFormat>` so each thread gets its own instance, reused across calls without contention; (c) migrate to `java.time.format.DateTimeFormatter`, which is immutable and inherently thread-safe, eliminating the problem entirely — the recommended modern solution.

5. **Effect of `setLenient(false)`, and when to use it?**
   By default, `DateFormat` is lenient — it "corrects" invalid field combinations by rolling them over (e.g. parsing "February 30" as March 1/2). `setLenient(false)` makes parsing strict, throwing `ParseException` for such invalid dates instead of silently normalizing them — important for validating genuine user input (e.g. birthdate forms) where silent correction could mask user error.

6. **How to format a number as a percentage or specific currency for a locale?**
   Percentage: `NumberFormat.getPercentInstance(locale).format(0.42)` → e.g. `"42%"`. Specific (non-default) currency for a locale: use `DecimalFormat` with `setCurrency(Currency.getInstance("EUR"))` on a `NumberFormat.getCurrencyInstance(locale)` instance, allowing the locale's grouping/placement conventions to apply to a currency other than the locale's default.

7. **Why migrate from `DateFormat`/`SimpleDateFormat` to `DateTimeFormatter`?**
   `java.time` classes (`LocalDate`, `LocalDateTime`, `ZonedDateTime`) and `DateTimeFormatter` are immutable and thread-safe by design, have clearer semantics for time zones and offsets, avoid the notorious `Calendar`/`Date` pitfalls (0-based months, mutdaily state, ambiguous time-zone handling), and provide a much richer, more precise formatting/parsing API (ISO-8601 support, `DateTimeFormatterBuilder` for custom patterns, better locale-aware era/eratext handling).

8. **How does `DecimalFormat` support custom pattern symbols beyond locale defaults?**
   Via `DecimalFormatSymbols` (grouping/decimal separators, currency symbol, digit characters, exponent symbol) which can be customized independent of the pattern, and via `DecimalFormat` methods like `setGroupingSize`, `setMultiplier` (e.g. ×100 for percentages/×1000 for per-mille), `setPositivePrefix/Suffix`/`setNegativePrefix/Suffix` for custom formatting beyond what locale defaults provide, while still layering on top of a locale's base symbol set if desired.

9. **How to implement round-trip-safe parsing for currency values?**
   Configure the formatter's rounding mode explicitly (`DecimalFormat.setRoundingMode(RoundingMode.HALF_EVEN)` or appropriate for the domain), fix the fraction digits to the currency's defined scale (`Currency.getDefaultFractionDigits()`), and use `BigDecimal` (via `setParseBigDecimal(true)`) rather than `double` for both formatting and parsing to avoid binary floating-point representation error; then verify `parse(format(x)).equals(x)` holds for the fixed scale, since values beyond that scale should have been rejected/rounded at input time.

10. **Diagnose bug in shared `static final SimpleDateFormat` under high throughput.**
    The shared instance is mutated concurrently by many threads calling `format`, causing race conditions that silently corrupt output (garbled or wrong dates under load — often intermittent and hard to reproduce, worse under high concurrency/throughput as described). Fix with minimal performance regression: replace with a `ThreadLocal<SimpleDateFormat>` (or better, migrate the whole pipeline to `java.time.format.DateTimeFormatter`, which is thread-safe and can remain a single shared `static final` instance with zero contention and no per-thread allocation overhead).

### Code Examples

```java
import java.text.*;
import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class NumberDateFormatDemo {
    public static void main(String[] args) throws ParseException {
        Locale germany = Locale.GERMANY;
        Locale us = Locale.US;

        // Currency formatting differs by locale (symbol placement, separators)
        NumberFormat deCurrency = NumberFormat.getCurrencyInstance(germany);
        NumberFormat usCurrency = NumberFormat.getCurrencyInstance(us);
        System.out.println(deCurrency.format(19999.5)); // e.g. "19.999,50 €"
        System.out.println(usCurrency.format(19999.5)); // e.g. "$19,999.50"

        // Strict parsing to reject invalid user-entered dates
        DateFormat strictDate = DateFormat.getDateInstance(DateFormat.SHORT, us);
        strictDate.setLenient(false);
        try {
            strictDate.parse("13/40/2024"); // invalid month/day
        } catch (ParseException e) {
            System.out.println("Rejected invalid date: " + e.getMessage());
        }

        // Modern, thread-safe alternative: java.time + DateTimeFormatter
        DateTimeFormatter isoFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US);
        LocalDate today = LocalDate.now();
        System.out.println(today.format(isoFormatter));
    }
}
```

## `Collator`

### Theory

**Core Concepts**
`Collator` (`java.text.Collator`) performs locale-sensitive **string comparison and sorting**, respecting linguistic ordering rules that differ from naive Unicode code-point comparison (`String.compareTo`). For example, in Swedish, "ö" sorts after "z", while in German it sorts near "o"; accented characters, case, and combining marks all require locale-aware rules to sort "correctly" from a human reader's perspective. `Collator.getInstance(locale)` returns a concrete instance (typically `RuleBasedCollator`) configured with CLDR collation rules for that locale.

**Internal Working**
`RuleBasedCollator` converts each string into a multi-level **sort key** (primary: base letter differences, secondary: accents/diacritics, tertiary: case, and sometimes quaternary/identical levels) according to locale collation rules, then compares strings level-by-level so that base-letter differences dominate accent differences, which dominate case differences — matching human intuition about alphabetical order.

**When to Use It**
- Sorting user-facing lists of names, product titles, or any text that must appear in "natural" alphabetical order for a given locale/language.
- Locale-aware, accent-insensitive or case-insensitive searching/matching (via `Collator.setStrength()`).
- Any comparison logic that must be culturally correct rather than based on raw UTF-16 code unit values.

**Advantages**
- Produces linguistically correct ordering that `String.compareTo` (raw code point/UTF-16 comparison) cannot, especially for accented and non-Latin scripts.
- Configurable strength levels (`PRIMARY`, `SECONDARY`, `TERTIARY`, `IDENTICAL`) allow flexible "fuzzy" matching (e.g. treat "café" and "CAFE" as equal at `PRIMARY` strength).
- `getCollationKey()` produces a precomputed, comparable `CollationKey` for efficient repeated sorting (avoids re-deriving sort rules on every comparison in large sorts).

**Limitations**
- Collation rules can be surprisingly locale-specific and non-obvious (e.g. Lithuanian and Czech have unusual sort orders for certain digraphs/diacritics) — assuming one universal "correct" order is wrong.
- `Collator` instances are **not guaranteed thread-safe** (like `DecimalFormat`/`SimpleDateFormat`) — the Javadoc explicitly warns against sharing them across threads without synchronization; clone per thread if needed.
- Performance: calling `compare()` repeatedly with the same strings in large sorts is more expensive than pre-computing `CollationKey`s once and sorting those.

### Internal Working

**Step-by-Step Explanation**
1. `Collator.getInstance(locale)` loads CLDR collation rules for the locale and returns a `RuleBasedCollator` (or a subclass) instance.
2. `collator.setStrength(Collator.SECONDARY)` (or `PRIMARY`/`TERTIARY`/`IDENTICAL`) configures how many "levels" of difference matter for equality/ordering.
3. `collator.compare(s1, s2)` internally derives (or uses cached) multi-level sort keys for each string and compares them level by level: primary (base letters) first; only if primary levels tie does it examine secondary (diacritics), then tertiary (case), etc.
4. For repeated sorting of the same strings (e.g. sorting a list multiple times, or binary-searching), `collator.getCollationKey(s)` precomputes a `CollationKey`, whose `compareTo` is a fast byte-array comparison — avoiding recomputation of collation rules on each comparison.
5. `Collections.sort(list, collator)` or `list.sort(collator)` applies this comparator across the whole collection.

**Memory Layout**
Not directly applicable to heap generations beyond normal object lifetime; `CollationKey` objects hold a byte-array representation of the multi-level sort key and are typically short-lived, scoped to a sort operation, so they are cheap, generation-young allocations that are collected quickly after sorting completes.

**Diagrams**
```
Strings: ["café", "cafe", "Cafe"]           Strength = PRIMARY (base letter only, ignore accents/case)
        │
        ▼
 Sort keys (primary level only): "cafe" == "cafe" == "cafe"  → all considered equal at PRIMARY
        │
        ▼
Strength = TERTIARY (base + accent + case)
        │
        ▼
 Sort keys distinguish: "cafe" < "cafe with accent" < "Cafe"(case difference) → distinct ordering
```

**JVM Behaviour**
No special bytecode/JIT considerations; collation rule tables are loaded from CLDR data bundled in the JDK (or supplied via a `java.text.spi.CollatorProvider` SPI for custom locales) at class-load/first-use time and cached for reuse — first invocation for a given locale may be marginally slower due to rule-table initialization.

### Interview Questions

**Basic**
1. Why does `String.compareTo()` sometimes produce "wrong" alphabetical order for accented text, and how does `Collator` fix this?
2. What are the four `Collator` strength levels, and what does each control?
3. How do you get a locale-aware collator instance?

**Intermediate**
4. What is a `CollationKey`, and when should you prefer it over calling `compare()` repeatedly?
5. How would you implement case-insensitive, accent-insensitive search using `Collator`?
6. Is `Collator` thread-safe? How do you use it safely in a multi-threaded sort?

**Advanced**
7. How does `RuleBasedCollator` internally represent multi-level sort keys, and why does level ordering (primary before secondary before tertiary) matter?
8. How would you customize collation rules for a domain-specific ordering requirement (e.g. treating certain punctuation as ignorable)?
9. How does `Collator` interact with Unicode normalization forms (NFC/NFD) when comparing strings with combining characters?

**Scenario-based**
10. Your application sorts a list of 10 million international customer names for a report, currently calling `collator.compare()` directly in `Collections.sort`. The sort is too slow. How would you optimize it?

### Detailed Answers

1. **Why does `String.compareTo()` give "wrong" order, and how does `Collator` fix it?**
   `String.compareTo()` compares strings by raw UTF-16 code unit values, which does not reflect linguistic alphabetical order — e.g. it would sort "Z" before "é" purely because of code point value, and it does not account for locale-specific rules (e.g. Swedish placing "ö" after "z"). `Collator` uses CLDR-derived, locale-specific collation rules to compare strings the way a native reader of that locale would expect them ordered, handling accents, case, and script-specific conventions correctly.

2. **What are the four `Collator` strength levels, and what does each control?**
   `PRIMARY` — only base letter differences matter (accents/case ignored, e.g. "a" == "á" == "A"). `SECONDARY` — base letters plus accents/diacritics matter (case still ignored, e.g. "a" == "A" but "a" != "á"). `TERTIARY` (default) — base letters, accents, and case all matter (e.g. "a" != "A" != "á"). `IDENTICAL` — full binary distinction including things like combining character sequence differences, for cases needing exact reproducibility.

3. **How do you get a locale-aware collator instance?**
   `Collator collator = Collator.getInstance(locale);` — e.g. `Collator.getInstance(Locale.forLanguageTag("sv"))` for Swedish-specific ordering, or `Collator.getInstance()` for the JVM default locale.

4. **What is a `CollationKey`, and when to prefer it over repeated `compare()`?**
   A `CollationKey` is a precomputed representation of a string's multi-level sort key (`collator.getCollationKey(str)`), whose `compareTo` performs a fast byte-array comparison. When sorting or comparing the same set of strings many times (e.g. large dataset sorts, repeated binary search), computing each string's `CollationKey` once up front and comparing keys is significantly faster than calling `collator.compare(s1, s2)` repeatedly, which re-derives collation elements from scratch on every call.

5. **How to implement case-insensitive, accent-insensitive search using `Collator`?**
   Create a `Collator` for the target locale and call `collator.setStrength(Collator.PRIMARY)`, then use `collator.compare(a, b) == 0` (or `collator.equals(a, b)`) as the equality test — at `PRIMARY` strength, base-letter matches are considered equal regardless of case or diacritics, e.g. matching "resume", "résumé", and "RESUME" as equivalent for search purposes.

6. **Is `Collator` thread-safe? How to use it safely in multi-threaded sorts?**
   No — per the Javadoc, `Collator` instances are not guaranteed thread-safe for concurrent use, similar to `DateFormat`. Safe usage patterns: create a separate `Collator` instance per thread (or clone via `collator.clone()`, since `Collator` implements `Cloneable`), or synchronize access to a shared instance, or — best for performance — precompute `CollationKey`s (immutable, safely shareable) once per string and let threads compare/sort using those keys without touching the mutable collator at all.

7. **How does `RuleBasedCollator` represent multi-level sort keys, and why does level order matter?**
   It decomposes each character/character-sequence into weighted components across levels (primary weight per base letter or "collation element", secondary weight for combining diacritics, tertiary for case/variant forms), producing an ordered sequence of these weights per string. Comparison proceeds level by level — all primary weights are compared across the full string first; only if every primary weight is equal does the comparison proceed to secondary weights, and so on. This ordering matches linguistic intuition: "resume" vs "Résumé" should first be judged on their base letters (identical), and only distinguished by accents/case as tie-breakers, not have a case difference outweigh a base letter difference.

8. **How to customize collation rules for domain-specific ordering (e.g. ignorable punctuation)?**
   Construct a `RuleBasedCollator` directly from a custom rule string (`new RuleBasedCollator(ruleExpression)`), which uses a mini rule syntax (`<`, `<<`, `<<<`, `=` operators to define relative primary/secondary/tertiary/equal orderings, and `&` to reset a reference point) to express things like "ignore hyphens" or "treat 'ä' as equivalent to 'ae'" for domain-specific needs not covered by the standard CLDR rules for a locale.

9. **How does `Collator` interact with Unicode normalization forms when comparing combining characters?**
   `Collator`'s default decomposition mode (`Collator.CANONICAL_DECOMPOSITION`, the default in Java) normalizes strings so that canonically equivalent sequences (e.g. precomposed "é" [U+00E9] vs. "e" + combining acute accent [U+0065 U+0301]) compare as equal, since both decompose to the same canonical form before collation element derivation. `Collator.NO_DECOMPOSITION` skips this normalization for performance when input is already known to be normalized, trading correctness on non-normalized input for speed.

10. **Optimize sorting 10 million names currently using `collator.compare()` directly.**
    Precompute a `CollationKey` for every name once (`Map<String, CollationKey>` or store the key alongside each record), then sort using `Comparator.comparing(record -> record.collationKey)` so the expensive rule-based comparison work happens once per string (O(n)) instead of on every comparison during the sort (O(n log n) comparisons, each re-deriving collation elements). Additionally, ensure a single `Collator` instance (or one per worker thread if parallelizing key computation) is reused rather than repeatedly calling `getInstance()`, and consider parallelizing the key-computation phase (e.g. via parallel streams) before the final sequential (or parallel) sort on the precomputed keys.

### Code Examples

```java
import java.text.Collator;
import java.text.CollationKey;
import java.util.*;

public class CollatorDemo {
    public static void main(String[] args) {
        List<String> names = new ArrayList<>(List.of("Österberg", "Andersson", "Zimmer", "Öberg"));

        // Swedish collation: ö sorts after z, unlike naive Unicode order
        Collator swedish = Collator.getInstance(Locale.forLanguageTag("sv"));
        names.sort(swedish);
        System.out.println("Swedish order: " + names);

        // Accent/case-insensitive matching via PRIMARY strength
        Collator primary = Collator.getInstance(Locale.US);
        primary.setStrength(Collator.PRIMARY);
        System.out.println(primary.equals("resume", "RÉSUMÉ")); // true at PRIMARY strength

        // Efficient repeated sorting via precomputed CollationKeys
        List<String> largeList = new ArrayList<>(List.of("café", "cafe", "Cafe", "zebra"));
        Map<String, CollationKey> keys = new HashMap<>();
        for (String s : largeList) {
            keys.put(s, swedish.getCollationKey(s));
        }
        largeList.sort(Comparator.comparing(keys::get));
        System.out.println("Sorted via precomputed keys: " + largeList);
    }
}
```
