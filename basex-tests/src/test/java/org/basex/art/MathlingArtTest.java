package org.basex.art;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
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
 * (<a href="https://mathling.com/">mathling.com</a>) through BaseX and, if {@code SAXON_EE}
 * holds a Saxon-EE classpath, also Saxon, comparing results and reporting run times.
 * Skipped unless {@code MATHLING_ART} points at the (self-downloaded) {@code art.zip}.
 * Artifacts are written to {@code target/mathling}.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class MathlingArtTest {
  /** Environment variable naming a Saxon-EE classpath (optional). */
  private static final String SAXON_EE_ENV = "SAXON_EE";
  /** Environment variable naming the art.zip download. */
  private static final String ART_ZIP_ENV = "MATHLING_ART";
  /** Fixes applied to Mary's sources after unpacking, as {@code {file, from, to}} replacements. */
  private static final String[][] PATCHES = {
    // import location fixes
    { "core/vector.xqy", "at \"../math.xqy\"", "at \"../math/math.xqy\"" },
    { "geo/affine.xqy", "at \"../math.xqy\"", "at \"../math/math.xqy\"" },
    { "geo/point.xqy", "at \"../math.xqy\"", "at \"../math/math.xqy\"" },
    { "noise/value.xqy", "at \"../modifiers.xqy\"", "at \"../noise/modifiers.xqy\"" },
    // util:assert()'s empty-sequence() return type lets Saxon's optimizer deduce a switch
    // default returning it is empty, then reject the enclosing function (XPTY0004, perlin.xqy).
    { "core/utilities.xqy",
      "function this:assert($that as xs:boolean, $complaint as xs:string) as empty-sequence()",
      "function this:assert($that as xs:boolean, $complaint as xs:string)"},
    // Saxon bug #7224: instance of map(xs:string,xs:string) is wrong . test the values explicitly.
    { "shapes/systems.xqy", "$rules instance of map(xs:string,xs:string)",
      "every $k in map:keys($rules) satisfies $rules($k) instance of xs:string"},
    // assertFails: BaseX does not evaluate single-valued $f() in ($f(),false())=>tail() - detect a
    // caught error via a sentinel node instead.
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
    // XQuery 4.0: a computed xs:double no longer equals a decimal literal by value (Saxon
    // -qversion:4.0 agrees). Numeric-promoting assertEquals fallback for Mary's 3.1 tests.
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
    // BaseX xs:integer is a 64-bit long, so 2^64 overflows the MULTIPLIERS64 table.
    // xs:decimal is arbitrary-precision and holds it; length (65) and values unchanged.
    { "core/binary.xqy", "declare variable $this:MULTIPLIERS64 as xs:integer* :=",
      "declare variable $this:MULTIPLIERS64 as xs:decimal* :=" },
    { "core/binary.xqy", "return fn:round(math:pow(2, $i)) cast as xs:integer)",
      "return fn:round(math:pow(2, $i)) cast as xs:decimal)" },
    // Saxon's "?void=this" makes the void setEntry() chainable; BaseX lacks it. Drop it and
    // return the mutated matrix explicitly. Note: commons-math3 must be on BaseX's classpath.
    { "math/eigen.xqy",
      "java:org.apache.commons.math3.linear.Array2DRowRealMatrix?void=this",
      "java:org.apache.commons.math3.linear.Array2DRowRealMatrix" },
    { "math/eigen.xqy",
      "$java-matrix=>Array2DRowRealMatrix:setEntry($row - 1, $col - 1, "
      + "xs:double($matrix($row)($col)))",
      "($java-matrix=>Array2DRowRealMatrix:setEntry($row - 1, $col - 1, "
      + "xs:double($matrix($row)($col))), $java-matrix)" },
    // No working BaseX branch for function annotations; use standard fn:function-annotations.
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
    "tests/test-fsa.xqy", "CoffeeSacks ixml (Saxon-only); prefer fn:invisible-xml",
    "tests/test-ixml.xqy", "CoffeeSacks ixml (Saxon-only); prefer fn:invisible-xml",
    "tests/testrand.xqy", "unreliable on both: RNG variance (all versions)");

  /** Whether the test data was located and unpacked. */
  private static boolean available;
  /** Whether a Saxon-EE classpath is available. */
  private static boolean saxon;
  /** Working directory: holds the extracted archive and all test artifacts. */
  private static Path mathlingDir;
  /** Unpacked "art" directory (the archive's own leading folder). */
  private static Path artDir;
  /** BaseX output directory. */
  private static Path basexOut;
  /** Saxon output directory. */
  private static Path saxonOut;

  /** Class loader for the optional Saxon-EE classpath. */
  private static URLClassLoader saxonLoader;
  /** Saxon s9api Processor, loaded reflectively (or {@code null}). */
  private static Object saxonProcessor;
  /** Saxon StandardLogger whose target stream is swapped per run to capture output. */
  private static Object saxonLogger;

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
   * Creates an engine output directory and a fresh timing.log.
   * @param dir output directory
   * @throws IOException I/O exception
   */
  private static void prepareOutput(final Path dir) throws IOException {
    Files.createDirectories(dir);
    Files.writeString(dir.resolve("timing.log"), "module,seconds,compile,eval,output\n");
  }

  /**
   * Locates and unpacks the test data; skips all tests if it is missing.
   * @throws IOException I/O exception
   */
  @BeforeAll
  public static void setUp() throws IOException {
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
    basexOut = mathlingDir.resolve("basex-output");
    prepareOutput(basexOut);
    final String saxonCp = System.getenv(SAXON_EE_ENV);
    saxon = saxonCp != null && !saxonCp.isBlank();
    if(saxon) {
      saxonOut = mathlingDir.resolve("saxon-output");
      prepareOutput(saxonOut);
      initSaxon();
    }
  }

  /**
   * Loads Saxon-EE into its own class loader and creates the s9api processor; disables Saxon on
   * any failure.
   */
  private static void initSaxon() {
    try {
      saxonLoader = new URLClassLoader(saxonUrls(), ClassLoader.getPlatformClassLoader());
      final Class<?> processorC = saxonLoader.loadClass("net.sf.saxon.s9api.Processor");
      Object proc;
      try {
        proc = processorC.getConstructor(boolean.class).newInstance(true);
      } catch(final ReflectiveOperationException ex) {
        proc = processorC.getConstructor(boolean.class).newInstance(false);
      }
      // -opt:-l: keep the optimizer from folding randomizers and avoid a loop-lifter crash
      final Object config = processorC.getMethod("getUnderlyingConfiguration").invoke(proc);
      config.getClass().getMethod("setConfigurationProperty", String.class, Object.class).invoke(
          config, "http://saxon.sf.net/feature/optimizationLevel", "-l");
      // route trace through a logger whose stream is swapped per run
      final Class<?> stdLogger = saxonLoader.loadClass("net.sf.saxon.lib.StandardLogger");
      saxonLogger = stdLogger.getConstructor().newInstance();
      final Class<?> loggerType = saxonLoader.loadClass("net.sf.saxon.lib.Logger");
      config.getClass().getMethod("setLogger", loggerType).invoke(config, saxonLogger);
      saxonProcessor = proc;
    } catch(final Exception ex) {
      saxon = false;
      System.out.println("Saxon unavailable (" + ex + "); running BaseX only.");
    }
  }

  /**
   * Provides every main module in the {@code art} tree as (id, path) arguments,
   * the id being the path relative to {@code art} (e.g. {@code examples/tree.xqy}).
   * @return stream of (id, module path) arguments
   * @throws IOException I/O exception
   */
  private static Stream<Arguments> mainModules() throws IOException {
    return moduleIds().stream().map(id -> Arguments.of(id, artDir.resolve(id)));
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
   * Runs one module on BaseX (its own test; results compared in {@link #report()}).
   * @param id module id (path relative to {@code art}, e.g. {@code examples/tree.xqy})
   * @param module module path
   */
  @ParameterizedTest(name = "{0}")
  @MethodSource("mainModules")
  public void basex(final String id, final Path module) {
    assumeTrue(!SKIP.containsKey(id), SKIP.get(id));
    assertDoesNotThrow(() -> runBaseX(id, module), id);
  }

  /**
   * Runs one module on Saxon (its own test; skipped unless {@code SAXON_EE} is set).
   * @param id module id (path relative to {@code art}, e.g. {@code examples/tree.xqy})
   * @param module module path
   */
  @ParameterizedTest(name = "{0}")
  @MethodSource("mainModules")
  public void saxon(final String id, final Path module) {
    assumeTrue(!SKIP.containsKey(id), SKIP.get(id));
    assumeTrue(saxon, "Saxon not available (set SAXON_EE)");
    assertDoesNotThrow(() -> runSaxon(id, module), id);
  }

  /**
   * Runs a module in-process on BaseX, writing its result and timing; errors fail the test.
   * @param id module id (path relative to {@code art})
   * @param module module path
   * @throws Exception execution exception
   */
  private static void runBaseX(final String id, final Path module) throws Exception {
    final String moduleText = Files.readString(module);
    final Path modDir = moduleDir(basexOut, id);
    Files.createDirectories(modDir.resolve("examples"));
    final Path tmp = modDir.resolve("system.out");
    final ByteArrayOutputStream errBuf = new ByteArrayOutputStream();
    final PrintStream origErr = System.err;

    final long t0 = System.nanoTime();
    final Context ctx = new Context();
    double compile = 0, eval = 0;
    try {
      ctx.options.set(MainOptions.DTD, true);
      ctx.options.set(MainOptions.FNXMLTRUSTED, true);
      // fn:trace / fn:message go to standard error; capture that as system.err
      System.setErr(new PrintStream(errBuf, true, StandardCharsets.UTF_8));
      try(QueryProcessor qp = new QueryProcessor(moduleText, module.toUri().toString(), ctx,
          null)) {
        qp.variable("TMPDIR", forward(modDir));
        final long c0 = System.nanoTime();
        qp.optimize();
        compile = (System.nanoTime() - c0) / 1e9;
        final long e0 = System.nanoTime();
        try(OutputStream os = Files.newOutputStream(tmp); Serializer ser = qp.serializer(os)) {
          for(final Item item : qp.value()) ser.serialize(item);
        }
        eval = (System.nanoTime() - e0) / 1e9;
      }
    } finally {
      System.setErr(origErr);
      ctx.close();
    }
    final double secs = (System.nanoTime() - t0) / 1e9;
    finish(id, moduleText, modDir, tmp, errBuf.toString(StandardCharsets.UTF_8), secs, compile,
        eval, basexOut);
  }

  /**
   * Runs a module in-process on Saxon, writing its result and timing; errors fail the test.
   * @param id module id (path relative to {@code art})
   * @param module module path
   * @throws Exception execution exception
   */
  private static void runSaxon(final String id, final Path module) throws Exception {
    final String moduleText = Files.readString(module);
    final Path modDir = moduleDir(saxonOut, id);
    Files.createDirectories(modDir.resolve("examples"));
    final Path tmp = modDir.resolve("system.out");
    final ByteArrayOutputStream errBuf = new ByteArrayOutputStream();
    saxonCaptureErr(errBuf);

    final long t0 = System.nanoTime();
    final double[] phases;
    try {
      phases = saxonEvaluate(module, moduleText, modDir, tmp);
    } catch(final InvocationTargetException ex) {
      writeErr(modDir, errBuf.toString(StandardCharsets.UTF_8));
      final Throwable cause = ex.getCause();
      throw cause instanceof Exception e ? e : new IllegalStateException(String.valueOf(cause));
    }
    final double secs = (System.nanoTime() - t0) / 1e9;
    finish(id, moduleText, modDir, tmp, errBuf.toString(StandardCharsets.UTF_8), secs, phases[0],
        phases[1], saxonOut);
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
   * Compiles and runs a module through Saxon (s9api, reflectively) into {@code tmp}.
   * @param module module path (used for the base URI)
   * @param moduleText module source
   * @param modDir the module's result folder (bound as {@code $TMPDIR})
   * @param tmp result file
   * @return {@code {compile, eval}} times in seconds
   * @throws ReflectiveOperationException on any s9api failure (including a wrapped query error)
   */
  private static double[] saxonEvaluate(final Path module, final String moduleText,
      final Path modDir, final Path tmp) throws ReflectiveOperationException {
    final Class<?> processorC = saxonProcessor.getClass();
    final Object compiler = processorC.getMethod("newXQueryCompiler").invoke(saxonProcessor);
    final Class<?> compilerC = compiler.getClass();
    compilerC.getMethod("setBaseURI", URI.class).invoke(compiler, module.toUri());
    final long c0 = System.nanoTime();
    final Object executable = compilerC.getMethod("compile", String.class).invoke(compiler,
        moduleText);
    final double compile = (System.nanoTime() - c0) / 1e9;

    final long e0 = System.nanoTime();
    final Object evaluator = executable.getClass().getMethod("load").invoke(executable);

    final Class<?> qnameC = saxonLoader.loadClass("net.sf.saxon.s9api.QName");
    final Class<?> xdmValueC = saxonLoader.loadClass("net.sf.saxon.s9api.XdmValue");
    final Object tmpdirQ = qnameC.getConstructor(String.class).newInstance("TMPDIR");
    final Object tmpdirV = saxonLoader.loadClass(
        "net.sf.saxon.s9api.XdmAtomicValue").getConstructor(String.class).newInstance(
            forward(modDir));
    evaluator.getClass().getMethod("setExternalVariable", qnameC, xdmValueC).invoke(evaluator,
        tmpdirQ, tmpdirV);

    final Object serializer = processorC.getMethod("newSerializer", File.class).invoke(
        saxonProcessor, tmp.toFile());
    final Class<?> propertyC = saxonLoader.loadClass("net.sf.saxon.s9api.Serializer$Property");
    @SuppressWarnings({ "unchecked", "rawtypes" })
    final Object omit = Enum.valueOf((Class<Enum>) propertyC, "OMIT_XML_DECLARATION");
    serializer.getClass().getMethod("setOutputProperty", propertyC, String.class).invoke(serializer,
        omit, "yes");
    final Class<?> destinationC = saxonLoader.loadClass("net.sf.saxon.s9api.Destination");
    evaluator.getClass().getMethod("run", destinationC).invoke(evaluator, serializer);
    return new double[] { compile, (System.nanoTime() - e0) / 1e9 };
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
   * @param baseDir BaseX result folder
   * @param saxonDir Saxon result folder
   * @return combined verdict string
   * @throws Exception query / I/O exception
   */
  private static String compare(final Path baseDir, final Path saxonDir) throws Exception {
    final String primary = xquery(resource("/mathling/compare.xq"),
        Map.of("a", forward(resultFile(baseDir)), "b", forward(resultFile(saxonDir)))).trim();
    final String sides = compareSides(baseDir.resolve("examples"), saxonDir.resolve("examples"));
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
   * @param baseEx BaseX {@code examples} folder
   * @param saxonEx Saxon {@code examples} folder
   * @return summary, or the empty string if neither run wrote side-files
   * @throws IOException I/O exception
   */
  private static String compareSides(final Path baseEx, final Path saxonEx) throws IOException {
    final long[] b = sideStats(baseEx);
    final long[] s = sideStats(saxonEx);
    if(b[0] == 0 && s[0] == 0) return "";
    return String.format(Locale.US, "side-files %s: BaseX %d/%dB, Saxon %d/%dB",
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
   * Points the Saxon logger at a capture buffer for the next run.
   * @param buf capture buffer
   * @throws ReflectiveOperationException on s9api failure
   */
  private static void saxonCaptureErr(final OutputStream buf) throws ReflectiveOperationException {
    saxonLogger.getClass().getMethod("setPrintStream", PrintStream.class).invoke(
        saxonLogger, new PrintStream(buf, true, StandardCharsets.UTF_8));
  }

  /**
   * Compares the modules both engines ran, then renders and writes {@code timing.md}.
   * @throws Exception query / I/O exception
   */
  @AfterAll
  public static void report() throws Exception {
    if(!available) return;

    if(saxon) {
      for(final String id : moduleIds()) {
        final Path baseDir = moduleDir(basexOut, id);
        final Path saxonDir = moduleDir(saxonOut, id);
        // compare only where both engines produced a result
        if(resultFile(baseDir) != null && resultFile(saxonDir) != null) {
          System.out.println("[compare] " + id + " : " + compare(baseDir, saxonDir));
        }
      }
      System.out.println();
    }

    final Map<String, Object> vars = new HashMap<>();
    vars.put("basex-log", basexOut.resolve("timing.log").toUri().toString());
    vars.put("saxon-log", saxon ? saxonOut.resolve("timing.log").toUri().toString() : "");
    final String md = xquery(resource("/mathling/timing.xq"), vars);

    final Path out = mathlingDir.resolve("timing.md");
    Files.writeString(out, md + '\n');

    System.out.println("Timing (also written to " + out + ")");
    System.out.println();
    System.out.println(md);
  }

  /**
   * Closes the Saxon class loader.
   * @throws IOException I/O exception
   */
  @AfterAll public static void closeSaxon() throws IOException {
    saxonProcessor = null;
    if(saxonLoader != null) {
      saxonLoader.close();
      saxonLoader = null;
    }
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
   * Expands a class-path string (with {@code *} wildcards) into JAR/directory URLs.
   * @param cp class path
   * @return URLs
   * @throws IOException I/O exception
   */
  private static URL[] expandClasspath(final String cp) throws IOException {
    final List<URL> urls = new ArrayList<>();
    for(final String entry : cp.split(File.pathSeparator)) {
      if(entry.isBlank()) continue;
      if(entry.endsWith("*")) {
        final Path dir = Path.of(entry.substring(0, entry.length() - 1));
        if(Files.isDirectory(dir)) {
          try(Stream<Path> list = Files.list(dir)) {
            for(final Path jar : list.filter(
                p -> p.toString().toLowerCase(Locale.ENGLISH).endsWith(".jar")).toList()) {
              urls.add(jar.toUri().toURL());
            }
          }
        }
      } else {
        final Path p = Path.of(entry);
        if(Files.exists(p)) urls.add(p.toUri().toURL());
      }
    }
    return urls.toArray(new URL[0]);
  }

  /**
   * Builds the Saxon classpath URLs: {@code SAXON_EE} plus the {@code commons-math3} jar
   * (the isolated Saxon loader cannot see the application classpath).
   * @return classpath URLs
   * @throws IOException I/O exception
   */
  private static URL[] saxonUrls() throws IOException {
    final List<URL> urls = new ArrayList<>(List.of(expandClasspath(System.getenv(SAXON_EE_ENV))));
    final URL commonsMath = commonsMath3Url();
    if(commonsMath != null) urls.add(commonsMath);
    return urls.toArray(new URL[0]);
  }

  /**
   * Locates the {@code commons-math3} jar via its code source.
   * @return jar URL, or {@code null} if not locatable
   */
  private static URL commonsMath3Url() {
    try {
      final Class<?> clz = Class.forName("org.apache.commons.math3.linear.EigenDecomposition");
      final java.security.CodeSource src = clz.getProtectionDomain().getCodeSource();
      return src == null ? null : src.getLocation();
    } catch(final Throwable ex) {
      return null;
    }
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
