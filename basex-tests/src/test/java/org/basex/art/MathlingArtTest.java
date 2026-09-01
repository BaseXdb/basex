package org.basex.art;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;
import java.util.zip.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

/**
 * Runs the main modules of Mary Holstege's "art" XQuery library
 * (<a href="https://mathling.com/">mathling.com</a>) through every available
 * {@link XQueryProcessor}: BaseX is the reference, further engines are discovered via
 * {@link ServiceLoader}, and their results are compared against BaseX. Run times are reported.
 * Skipped unless {@code MATHLING_ART} points at the (self-downloaded) {@code art.zip}.
 * Artifacts are written to {@code target/mathling}.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public class MathlingArtTest {
  /** Environment variable naming the art.zip download. */
  private static final String ART_ZIP_ENV = "MATHLING_ART";
  /** Fixes applied to Mary's sources after unpacking, as {@code {file, from, to}} replacements. */
  private static final String[][] PATCHES = {
    // import location fixes
    { "core/vector.xqy", "at \"../math.xqy\"", "at \"../math/math.xqy\"" },
    { "geo/affine.xqy", "at \"../math.xqy\"", "at \"../math/math.xqy\"" },
    { "geo/point.xqy", "at \"../math.xqy\"", "at \"../math/math.xqy\"" },
    { "noise/value.xqy", "at \"../modifiers.xqy\"", "at \"../noise/modifiers.xqy\"" },
    // util:assert()'s empty-sequence() return type lets an optimizer deduce a switch
    // default returning it is empty, then reject the enclosing function (XPTY0004, perlin.xqy).
    { "core/utilities.xqy",
      "function this:assert($that as xs:boolean, $complaint as xs:string) as empty-sequence()",
      "function this:assert($that as xs:boolean, $complaint as xs:string)"},
    // observed wrong results for `instance of map(xs:string,xs:string)`, so test the values
    // explicitly.
    { "shapes/systems.xqy", "$rules instance of map(xs:string,xs:string)",
      "every $k in map:keys($rules) satisfies $rules($k) instance of xs:string"},
    // assertFails: optimization drops evaluation of single-valued $f() in ($f(),false())=>tail(),
    // so detect a caught error via a sentinel node instead.
    { "tests/testlib.xqy",
      "  let $ok :=\n"
      + "    try {\n"
      + "      ($f(), false())=>tail()\n"
      + "    } catch * {\n"
      + "      true()\n"
      + "    }\n"
      + "  return (\n"
      + "    if ($ok) then ()",
      "  let $caught := text { 'caught' }\n"
      + "  let $result :=\n"
      + "    try {\n"
      + "      $f()\n"
      + "    } catch * {\n"
      + "      $caught\n"
      + "    }\n"
      + "  return (\n"
      + "    if ($result instance of text() and $result is $caught) then ()" },
    // XQuery 4.0: a computed xs:double no longer equals a decimal literal by value. Numeric-
    // promoting assertEquals fallback for Mary's 3.1 tests.
    { "tests/testlib.xqy",
      "if (deep-equal($this,$that) or this:same($this,$that,$unordered)) then ()",
      "if (deep-equal($this,$that) or this:same($this,$that,$unordered) "
       + "or this:num-equal($this,$that)) then ()" },
    { "tests/testlib.xqy",
      "declare function this:assertEquals($message as xs:string, $this as item()*, "
        + "$that as item()*) as empty-sequence()",
      "(:~ Numeric-promoting equality (3.1-style): fallback for XQuery 4.0. :)\n"
      + "declare function this:num-equal($this as item()*, $that as item()*) as xs:boolean\n"
      + "{\n"
      + "  count($this) eq count($that) and\n"
      + "  (every $i in 1 to count($this) satisfies this:num-equal-item($this[$i], $that[$i]))\n"
      + "};\n"
      + "\n"
      + "declare function this:num-equal-item($a as item(), $b as item()) as xs:boolean\n"
      + "{\n"
      + "  if ($a instance of xs:decimal or $a instance of xs:double "
      + "or $a instance of xs:float)\n"
      + "  then (($b instance of xs:decimal or $b instance of xs:double "
      + "or $b instance of xs:float)\n"
      + "        and xs:double($a) eq xs:double($b))\n"
      + "  else if ($a instance of map(*) and $b instance of map(*))\n"
      + "  then (count(map:keys($a)) eq count(map:keys($b))\n"
      + "        and (every $k in map:keys($a)\n"
      + "             satisfies map:contains($b, $k) and "
      + "this:num-equal($a=>map:get($k), $b=>map:get($k))))\n"
      + "  else if ($a instance of array(*) and $b instance of array(*))\n"
      + "  then (array:size($a) eq array:size($b)\n"
      + "        and (every $i in 1 to array:size($a) "
      + "satisfies this:num-equal($a($i), $b($i))))\n"
      + "  else deep-equal($a, $b)\n"
      + "};\n"
      + "\n"
      + "declare function this:assertEquals($message as xs:string, $this as item()*, "
      + "$that as item()*) as empty-sequence()" },
    { "tests/test-math.xqy", "test:assertEquals(mmath:logK(81, 3), 4)",
      "test:assertClose(mmath:logK(81, 3), 4, 1E-6)" },
    // xs:integer is a 64-bit long, so 2^64 overflows the MULTIPLIERS64 table. xs:decimal is
    // arbitrary-precision and holds it; length (65) and values unchanged.
    { "core/binary.xqy", "declare variable $this:MULTIPLIERS64 as xs:integer* :=",
      "declare variable $this:MULTIPLIERS64 as xs:decimal* :=" },
    { "core/binary.xqy", "return fn:round(math:pow(2, $i)) cast as xs:integer)",
      "return fn:round(math:pow(2, $i)) cast as xs:decimal)" },
    // "?void=this" to makes the void setEntry() chainable is not implemented. Drop it and
    // return the mutated matrix explicitly. Note: commons-math3 must be on the classpath.
    { "math/eigen.xqy",
      "java:org.apache.commons.math3.linear.Array2DRowRealMatrix?void=this",
      "java:org.apache.commons.math3.linear.Array2DRowRealMatrix" },
    { "math/eigen.xqy",
      "$java-matrix=>Array2DRowRealMatrix:setEntry($row - 1, $col - 1, "
      + "xs:double($matrix($row)($col)))",
      "($java-matrix=>Array2DRowRealMatrix:setEntry($row - 1, $col - 1, "
      + "xs:double($matrix($row)($col))), $java-matrix)" },
    // No working branch for function annotations; use standard fn:function-annotations.
    { "core/utilities.xqy",
      "    ) else (\n"
      + "      function($f as function(*)) as map(*) {\n"
      + "        map {}\n"
      + "      }\n"
      + "    )",
      "    ) else (\n"
      + "      function($f as function(*)) as map(*) {\n"
      + "        map:merge(\n"
      + "          for $a in function-annotations($f)\n"
      + "          let $k := map:keys($a)\n"
      + "          return map { $k : if (empty($a($k))) then true() else $a($k) },\n"
      + "          map {\"duplicates\": \"combine\"}\n"
      + "        )\n"
      + "      }\n"
      + "    )" },
    { "core/utilities.xqy",
      "    ) else (\n"
      + "      function($f as function(*), $name as xs:QName) as item()* {\n"
      + "        ()\n"
      + "      }\n"
      + "    )",
      "    ) else (\n"
      + "      function($f as function(*), $name as xs:QName) as item()* {\n"
      + "        map:merge(function-annotations($f), map {\"duplicates\": \"combine\"})($name)\n"
      + "      }\n"
      + "    )" },
  };
  /** Modules skipped via an assumption, as {@code id -> reason} (kept beside {@link #PATCHES}). */
  private static final Map<String, String> SKIP = Map.of(
    "tests/test-fsa.xqy", "depends on CoffeeSacks, prefer fn:invisible-xml",
    "tests/test-ixml.xqy", "depends on CoffeeSacks, prefer fn:invisible-xml",
    "tests/testrand.xqy", "unreliable: RNG variance");

  /** Whether the test data was located and unpacked. */
  private static boolean available;
  /** Working directory: holds the extracted archive and all test artifacts. */
  private static Path mathlingDir;
  /** Unpacked "art" directory (the archive's own leading folder). */
  private static Path artDir;

  /** Available engines, reference first. */
  private static List<XQueryProcessor> processors;
  /** Engine lookup by id. */
  private static Map<String, XQueryProcessor> byId;
  /** Per-engine output directory. */
  private static Map<String, Path> outputs;
  /** The reference engine (BaseX). */
  private static XQueryProcessor reference;

  /**
   * Finds the art.zip named by {@code MATHLING_ART}, or {@code null} if unset/missing.
   * @return path to art.zip or {@code null}
   */
  private static Path locateZip() {
    final String var = System.getenv(ART_ZIP_ENV);
    if(var == null || var.isBlank()) return null;
    final Path p = Path.of(var);
    return Files.isRegularFile(p) ? p : null;
  }

  /**
   * Applies the {@link #PATCHES} to the unpacked sources so they run on BaseX.
   * @throws IOException I/O exception
   */
  private static void applyPatches() throws IOException {
    for(final String[] patch : PATCHES) {
      final Path file = artDir.resolve(patch[0]);
      if(!Files.exists(file)) continue;
      final String text = Files.readString(file);
      if(text.contains(patch[1])) Files.writeString(file, text.replace(patch[1], patch[2]));
    }
  }

  /**
   * Discovers the available engines (reference first) and prepares an output directory for each.
   * @throws Exception setup exception
   */
  private static void initProcessors() throws Exception {
    final List<XQueryProcessor> found = new ArrayList<>();
    for(final XQueryProcessor p : ServiceLoader.load(XQueryProcessor.class)) {
      if(p.available()) found.add(p);
    }
    reference = found.stream().filter(XQueryProcessor::reference).findFirst().orElseThrow(
        () -> new IllegalStateException("No reference XQueryProcessor found."));
    // reference first, remaining engines in id order for stable output
    processors = new ArrayList<>();
    processors.add(reference);
    found.stream().filter(p -> p != reference).sorted(
        Comparator.comparing(XQueryProcessor::id)).forEach(processors::add);

    byId = new LinkedHashMap<>();
    outputs = new LinkedHashMap<>();
    for(final XQueryProcessor p : processors) {
      byId.put(p.id(), p);
      final Path out = mathlingDir.resolve(p.id() + "-output");
      Files.createDirectories(out);
      Files.writeString(out.resolve("timing.log"), "module,seconds,compile,eval,output\n");
      outputs.put(p.id(), out);
      p.init();
    }
  }

  /**
   * Locates and unpacks the test data and initialises the engines; skips all tests if the data
   * is missing.
   * @throws Exception setup exception
   */
  @BeforeAll
  public static void setUp() throws Exception {
    final Path zip = locateZip();
    if(zip != null) {
      mathlingDir = Path.of("target", "mathling");
      new IOFile(mathlingDir).delete();
      Files.createDirectories(mathlingDir);
      unzip(zip, mathlingDir);
      // the archive is accepted as-is, with its leading "art/" directory
      artDir = mathlingDir.resolve("art");
      available = Files.isDirectory(artDir);
    }
    assumeTrue(available, "art.zip not found.\n"
      + "Download from https://mathling.com/code/art/art.zip and set the environment variable\n"
      + ART_ZIP_ENV + "=<path-to-art.zip> (in the shell, or the Environment tab of the Eclipse\n"
      + "run configuration).");
    applyPatches();
    initProcessors();
  }

  /**
   * Provides every (engine, main module) pair as {@code (engine id, module id, module path)}
   * arguments, the module id being its path relative to {@code art} (e.g. {@code examples/x.xqy}).
   * @return stream of arguments
   * @throws IOException I/O exception
   */
  private static Stream<Arguments> runs() throws IOException {
    final List<String> ids = moduleIds();
    return processors.stream().flatMap(
        p -> ids.stream().map(id -> Arguments.of(p.id(), id, artDir.resolve(id))));
  }

  /**
   * Lists the ids (paths relative to {@code art}) of every main module in the tree.
   * @return sorted module ids
   * @throws IOException I/O exception
   */
  private static List<String> moduleIds() throws IOException {
    try(Stream<Path> files = Files.walk(artDir)) {
      return files.filter(Files::isRegularFile).filter(
          p -> p.getFileName().toString().matches(".*\\.xq[^.]*")).filter(
          MathlingArtTest::isMainModule).map(
          p -> artDir.relativize(p).toString().replace('\\', '/')).sorted().toList();
    }
  }

  /**
   * A main module is an {@code .xq*} file with no {@code module namespace} declaration.
   * @param module candidate file
   * @return whether it is a main module
   */
  private static boolean isMainModule(final Path module) {
    try {
      return !Files.readString(module).contains("\nmodule namespace");
    } catch(final IOException ex) {
      return false;
    }
  }

  /**
   * Runs one module on one engine (its own test; results compared in {@link #report()}).
   * @param engineId engine id
   * @param id module id (path relative to {@code art}, e.g. {@code examples/tree.xqy})
   * @param module module path
   */
  @ParameterizedTest(name = "{1} [{0}]")
  @MethodSource("runs")
  public void run(final String engineId, final String id, final Path module) {
    assumeTrue(!SKIP.containsKey(id), SKIP.get(id));
    assertDoesNotThrow(() -> runModule(byId.get(engineId), id, module), engineId + " " + id);
  }

  /**
   * Runs a module on one engine, writing its result and timing; errors fail the test.
   * @param proc engine
   * @param id module id (path relative to {@code art})
   * @param module module path
   * @throws Exception execution exception
   */
  private static void runModule(final XQueryProcessor proc, final String id, final Path module)
      throws Exception {
    final String moduleText = Files.readString(module);
    final Path engineOut = outputs.get(proc.id());
    final Path modDir = moduleDir(engineOut, id);
    Files.createDirectories(modDir.resolve("examples"));
    final Path tmp = modDir.resolve("system.out");
    final ByteArrayOutputStream errBuf = new ByteArrayOutputStream();
    final Map<String, String> bindings = Map.of("TMPDIR", forward(modDir));

    final long t0 = System.nanoTime();
    final double[] phases;
    try {
      phases = proc.run(module, moduleText, modDir, tmp, bindings, errBuf);
    } catch(final Exception ex) {
      writeErr(modDir, errBuf.toString(StandardCharsets.UTF_8));
      throw ex;
    }
    final double secs = (System.nanoTime() - t0) / 1e9;
    finish(id, moduleText, modDir, tmp, errBuf.toString(StandardCharsets.UTF_8), secs, phases[0],
        phases[1], engineOut);
  }

  /**
   * Maps a module id to its result folder under an engine's output.
   * @param engineOut engine output directory
   * @param id module id (path relative to {@code art})
   * @return the module's result folder
   */
  private static Path moduleDir(final Path engineOut, final String id) {
    return engineOut.resolve(id.substring(0, id.lastIndexOf('.')));
  }

  /**
   * Names the result, writes {@code system.err}, and appends a CSV timing row.
   * @param id module id (path relative to {@code art})
   * @param moduleText module source
   * @param modDir the module's result folder
   * @param tmp temporary result file
   * @param err captured trace / error output
   * @param secs total run time in seconds
   * @param compile compile time in seconds
   * @param eval evaluation time in seconds
   * @param engineOut the engine's output directory (holding timing.log)
   * @throws IOException I/O exception
   */
  private static void finish(final String id, final String moduleText, final Path modDir,
      final Path tmp, final String err, final double secs, final double compile, final double eval,
      final Path engineOut) throws IOException {
    final String content = Files.exists(tmp) ? Files.readString(tmp) : "";
    final String ext = extension(moduleText, content);
    final Path result = modDir.resolve("system.out." + ext);
    Files.deleteIfExists(result);
    if(Files.exists(tmp)) Files.move(tmp, result);
    else Files.writeString(result, "");
    writeErr(modDir, err);

    final String output = engineOut.relativize(result).toString().replace('\\', '/');
    final String row = String.format(Locale.US, "%s,%.3f,%.3f,%.3f,%s%n", id, secs, compile, eval,
        output);
    Files.writeString(engineOut.resolve("timing.log"), row, StandardOpenOption.APPEND);
  }

  /**
   * Chooses a file extension from the module's serialization method or its content.
   * @param moduleText module source
   * @param content produced output
   * @return extension without dot
   */
  private static String extension(final String moduleText, final String content) {
    String method = null;
    final Matcher m = Pattern.compile("output:method\\s+\"(\\w+)\"").matcher(moduleText);
    if(m.find()) method = m.group(1).toLowerCase(Locale.ENGLISH);

    final String lead = content.stripLeading();
    final char c0 = lead.isEmpty() ? 0 : lead.charAt(0);
    final boolean hasSvg = content.toLowerCase(Locale.ENGLISH).contains("<svg");
    final boolean hasElem = Pattern.compile("<[A-Za-z]").matcher(content).find();

    if("json".equals(method)) return "json";
    if("html".equals(method)) return "html";
    if("xhtml".equals(method)) return "xhtml";
    if("xml".equals(method)) return hasSvg ? "svg" : "xml";
    if(c0 == '{' || c0 == '[') return "json";
    if(hasSvg) return "svg";
    if(hasElem) return "xml";
    return "txt";
  }

  /**
   * Compares two module result folders (results via {@code compare.xq}, plus side-files).
   * @param baseDir reference result folder
   * @param otherDir other engine's result folder
   * @return combined verdict string
   * @throws Exception query / I/O exception
   */
  private static String compare(final Path baseDir, final Path otherDir) throws Exception {
    final String primary = xquery(resource("/mathling/compare.xq"),
        Map.of("a", forward(resultFile(baseDir)), "b", forward(resultFile(otherDir)))).trim();
    final String sides = compareSides(baseDir.resolve("examples"), otherDir.resolve("examples"));
    return sides.isEmpty() ? primary : primary + "; " + sides;
  }

  /**
   * Returns the module's {@code system.out.*} result file, or {@code null} if none.
   * @param modDir module folder
   * @return the result file, or {@code null}
   * @throws IOException I/O exception
   */
  private static Path resultFile(final Path modDir) throws IOException {
    if(!Files.isDirectory(modDir)) return null;
    try(Stream<Path> s = Files.list(modDir)) {
      return s.filter(
          p -> p.getFileName().toString().startsWith("system.out.")).findFirst().orElse(null);
    }
  }

  /**
   * Summarizes the side-files (count and total bytes) of two runs.
   * @param baseEx reference {@code examples} folder
   * @param otherEx other engine's {@code examples} folder
   * @return summary, or the empty string if neither run wrote side-files
   * @throws IOException I/O exception
   */
  private static String compareSides(final Path baseEx, final Path otherEx) throws IOException {
    final long[] b = sideStats(baseEx);
    final long[] s = sideStats(otherEx);
    if(b[0] == 0 && s[0] == 0) return "";
    return String.format(Locale.US, "side-files %s: reference %d/%dB, other %d/%dB",
        b[0] == s[0] ? "match" : "DIFFER", b[0], b[1], s[0], s[1]);
  }

  /**
   * Counts the regular files in a folder and their total size.
   * @param dir folder (may not exist)
   * @return {@code {count, totalBytes}}
   * @throws IOException I/O exception
   */
  private static long[] sideStats(final Path dir) throws IOException {
    if(!Files.isDirectory(dir)) return new long[] { 0, 0 };
    try(Stream<Path> s = Files.list(dir)) {
      long count = 0, bytes = 0;
      for(final Path f : s.filter(Files::isRegularFile).toList()) {
        count++;
        bytes += Files.size(f);
      }
      return new long[] { count, bytes };
    }
  }

  /**
   * Writes captured trace/error output as {@code system.err} (or removes a stale file).
   * @param modDir module folder
   * @param err captured output
   * @throws IOException I/O exception
   */
  private static void writeErr(final Path modDir, final String err) throws IOException {
    final Path e = modDir.resolve("system.err");
    if(err.isBlank()) Files.deleteIfExists(e);
    else Files.writeString(e, err);
  }

  /**
   * Compares every non-reference engine against the reference, then renders and writes
   * {@code timing.md}.
   * @throws Exception query / I/O exception
   */
  @AfterAll
  public static void report() throws Exception {
    if(!available) return;

    final List<String> ids = moduleIds();
    for(final XQueryProcessor p : processors) {
      if(p == reference) continue;
      for(final String id : ids) {
        final Path baseDir = moduleDir(outputs.get(reference.id()), id);
        final Path otherDir = moduleDir(outputs.get(p.id()), id);
        // compare only where both engines produced a result
        if(resultFile(baseDir) != null && resultFile(otherDir) != null) {
          System.out.println("[compare " + p.id() + "] " + id + " : " + compare(baseDir, otherDir));
        }
      }
      System.out.println();
    }

    // one line per engine ("name<TAB>timing-log-URI"), reference first
    final StringBuilder engines = new StringBuilder();
    for(final XQueryProcessor p : processors) {
      engines.append(p.name()).append('\t').append(
          outputs.get(p.id()).resolve("timing.log").toUri()).append('\n');
    }
    final String md = xquery(resource("/mathling/timing.xq"),
        Map.of("engines", engines.toString()));

    final Path out = mathlingDir.resolve("timing.md");
    Files.writeString(out, md + '\n');

    System.out.println("Timing (also written to " + out + ")");
    System.out.println();
    System.out.println(md);
  }

  /**
   * Closes every engine.
   * @throws Exception teardown exception
   */
  @AfterAll public static void closeProcessors() throws Exception {
    if(processors == null) return;
    for(final XQueryProcessor p : processors) p.close();
    processors = null;
  }

  // helpers ------------------------------------------------------------------

  /**
   * Runs an XQuery and returns its serialized (text) result.
   * @param query query string
   * @param vars external variable bindings
   * @return serialized result
   * @throws Exception query exception
   */
  private static String xquery(final String query, final Map<String, Object> vars)
      throws Exception {
    final Context ctx = new Context();
    try(QueryProcessor qp = new QueryProcessor(query, ctx)) {
      for(final Map.Entry<String, Object> e : vars.entrySet())
        qp.variable(e.getKey(), e.getValue());
      final ByteArrayOutputStream bos = new ByteArrayOutputStream();
      // close (flush) the serializer before reading the buffer
      try(Serializer ser = qp.serializer(bos)) {
        for(final Item item : qp.value()) ser.serialize(item);
      }
      return bos.toString(StandardCharsets.UTF_8);
    } finally {
      ctx.close();
    }
  }

  /**
   * Reads a class-path resource as UTF-8 text.
   * @param path resource path
   * @return resource contents
   * @throws IOException I/O exception
   */
  private static String resource(final String path) throws IOException {
    try(InputStream in = MathlingArtTest.class.getResourceAsStream(path)) {
      if(in == null) throw new FileNotFoundException("Missing resource: " + path);
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  /**
   * Returns a path with forward slashes (for binding into XQuery).
   * @param p path
   * @return path string with forward slashes
   */
  private static String forward(final Path p) {
    return p.toAbsolutePath().toString().replace('\\', '/');
  }

  /**
   * Extracts a zip archive, guarding against path traversal.
   * @param zip archive
   * @param dest destination directory
   * @throws IOException I/O exception
   */
  private static void unzip(final Path zip, final Path dest) throws IOException {
    final Path root = dest.toAbsolutePath().normalize();
    try(var in = new ZipInputStream(Files.newInputStream(zip))) {
      for(ZipEntry e; (e = in.getNextEntry()) != null;) {
        final Path target = root.resolve(e.getName()).normalize();
        if(!target.startsWith(root)) throw new IOException("Bad zip entry: " + e.getName());
        if(e.isDirectory()) {
          Files.createDirectories(target);
        } else {
          Files.createDirectories(target.getParent());
          Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
      }
    }
  }
}
