package org.basex.query.parse;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import javax.tools.*;

/**
 * Updates the generated parts of the REx parser after a change of the grammar. Run it with the
 * classes of the last build and the dependencies on the class path (e.g. from the IDE). It
 * compiles what its steps need, so it works even if the parsers do not compile because of the new
 * grammar.
 *
 * <ol>
 *   <li>Run this class. It fetches the grammar and regenerates {@code XQuerySyntaxParser} with REx
 *     ({@link RExRemake}), then {@code Sym} ({@link SymGenerator}) and the syntax tree classes
 *     ({@link NodeGenerator}). It reports the nonterminals that were added, removed or changed
 *     ({@link FoldPolicyTest}) and records them in {@code fold-signatures.properties}: the changes
 *     are reported once, and the diff of that file is their record.</li>
 *   <li>Review the report. New or changed nonterminals are to be classified in {@link FoldPolicy};
 *     {@code Symbol} may need to follow renamed symbols. Rerun until no {@code REVIEW} is left. A
 *     clean rerun does not replace reviewing the original report or the signature diff.</li>
 *   <li>Build. The compile errors of {@code RExQueryParser} show where records changed. Add the
 *     handling of new nonterminals and language features, to {@code QueryParser} as well as long
 *     as it exists.</li>
 *   <li>Test: {@code SymTest}, {@code NodeGeneratorTest}, {@code FoldPolicyTest},
 *     {@code RExQueryTest}, the query tests with both parsers ({@code -Dorg.basex.QUERYPARSER=
 *     manual} and {@code rex}), and {@link ParseDiff}, which is to report no plan differences.</li>
 * </ol>
 *
 * <p>Recovery: fix the cause and rerun. If REx cannot read its saved command line, restore the
 * generated Java and XML files from version control first. To repeat the change report, restore
 * {@code fold-signatures.properties} from before the update.</p>
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class GrammarUpdate {
  /** Directory of the generated parser. */
  private static final Path GEN = Path.of("src/main/java/org/basex/query/parse/gen");
  /** Directory of the fold policy. */
  private static final Path MAIN = Path.of("src/main/java/org/basex/query/parse");
  /** Directory of the generators. */
  private static final Path TEST = Path.of("src/test/java/org/basex/query/parse");
  /** Prefix of the arguments of the child processes. */
  private static final String STEP = "step:";
  /** Exit status of a step that has found something to be reviewed. */
  private static final int REVIEW = 2;

  /** Check that can fail. */
  private interface Check {
    /**
     * Runs the check.
     * @throws Exception exception
     */
    void run() throws Exception;
  }

  /** Private constructor. */
  private GrammarUpdate() { }

  /**
   * Main method.
   * @param args not used
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    if(args.length > 0 && args[0].startsWith(STEP)) {
      System.exit(step(args[0].substring(STEP.length())));
    }
    final Path core = core();

    System.out.println("...running RExRemake: fetch the grammar, run REx");
    RExRemake.remake(core);
    child(core, "sym", "SymGenerator: generate the symbol constants (Sym.java)",
        GEN.resolve("XQuerySyntaxParser.java"), TEST.resolve("SymGenerator.java"));
    final int status = child(core, "check", "NodeGenerator: generate the syntax tree classes " +
        "(Nodes.java, NodeFactory.java)", GEN.resolve("Sym.java"),
        GEN.resolve("XQuerySyntaxParser.java"), MAIN.resolve("FoldPolicy.java"),
        TEST.resolve("Symbol.java"), TEST.resolve("NodeGenerator.java"),
        TEST.resolve("FoldPolicyTest.java"), TEST.resolve("GrammarXml.java"));
    if(status == REVIEW) {
      System.out.println("...done, but something is to be reviewed (see REVIEW above): adapt " +
          "FoldPolicy or Symbol, then run again.");
      System.exit(REVIEW);
    }
    System.out.println("...done. Next: build, adapt RExQueryParser (and QueryParser), and test " +
        "(see the Javadoc of GrammarUpdate).");
  }

  /**
   * Finds the directory of module basex-core: above the classes of this class, or else at or
   * below the working directory.
   * @return directory
   * @throws Exception exception
   */
  private static Path core() throws Exception {
    final List<Path> candidates = new ArrayList<>();
    final Path classes = Path.of(
        GrammarUpdate.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    for(Path path = classes; path != null; path = path.getParent()) candidates.add(path);
    candidates.add(Path.of("."));
    candidates.add(Path.of("basex-core"));
    for(final Path candidate : candidates) {
      if(Files.isDirectory(candidate.resolve(GEN))) return candidate;
    }
    throw new IOException("Module basex-core not found.");
  }

  /**
   * Compiles the sources of a step into a new directory, and runs the step in a new JVM with these
   * classes in front of the class path, as the classes of the last build are outdated.
   * @param core directory of the module
   * @param name name of the step
   * @param description description of the step
   * @param sources sources to be compiled, relative to the module
   * @return exit status: 0, or {@link #REVIEW}
   * @throws Exception exception
   */
  private static int child(final Path core, final String name, final String description,
      final Path... sources) throws Exception {

    System.out.println("...running " + description);
    final JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
    if(javac == null) throw new IOException("A JDK is required to compile the generators.");
    final StringJoiner entries = new StringJoiner(File.pathSeparator);
    for(final String entry : System.getProperty("java.class.path").split(File.pathSeparator)) {
      // the child runs in another directory
      entries.add(entry.isEmpty() ? entry : Path.of(entry).toAbsolutePath().toString());
    }
    final String classpath = entries.toString();
    final Path out = Files.createTempDirectory("grammar-update");
    try {
      final List<String> args = new ArrayList<>(List.of("-nowarn", "-proc:none", "-encoding",
          "UTF-8", "-d", out.toString(), "-cp", classpath));
      for(final Path source : sources) args.add(core.resolve(source).toString());
      if(javac.run(null, null, null, args.toArray(String[]::new)) != 0) {
        throw new IOException("Step " + name + " does not compile. Symbol.java names symbols " +
            "of the grammar and may need to follow renamed ones.");
      }
      final String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
      final Process process = new ProcessBuilder(java, "-cp",
          out + File.pathSeparator + classpath, GrammarUpdate.class.getName(), STEP + name).
          directory(core.toFile()).inheritIO().start();
      final int status = process.waitFor();
      if(status != 0 && status != REVIEW) throw new IOException("Step " + name + " failed.");
      return status;
    } finally {
      try(var files = Files.walk(out)) {
        files.sorted(Comparator.reverseOrder()).forEach(file -> file.toFile().delete());
      }
    }
  }

  /**
   * Runs a step in the child process.
   * @param name name of the step
   * @return exit status
   * @throws Exception exception
   */
  private static int step(final String name) throws Exception {
    switch(name) {
      case "sym" -> SymGenerator.main(new String[0]);
      default -> {
        NodeGenerator.main(new String[0]);
        System.out.println("...running FoldPolicyTest: report and record the changes, compare " +
            "FoldPolicy with the grammar");
        return fold();
      }
    }
    return 0;
  }

  /**
   * Reports what the grammar has changed, records it, and checks the fold policy against the
   * grammar.
   * @return exit status: 0, or {@link #REVIEW} if there is something to be reviewed
   * @throws Exception exception
   */
  private static int fold() throws Exception {
    int failed = 0;
    System.out.println("  Changes since the last run:");
    for(final String line : FoldPolicyTest.changes()) {
      System.out.println("    " + line);
      if(line.startsWith("REVIEW")) failed++;
    }

    final FoldPolicyTest test = new FoldPolicyTest();
    final Map<String, Check> checks = new LinkedHashMap<>();
    checks.put("signatures recorded in fold-signatures.properties", () -> {
      System.setProperty("fold.update", "true");
      try {
        test.signatures();
      } finally {
        System.clearProperty("fold.update");
      }
    });
    checks.put("grammar, parser and XML belong together", test::grammar);
    checks.put("FoldPolicy.NAMES matches the grammar", test::names);
    checks.put("FoldPolicy.LISTS matches the grammar", test::lists);
    for(final Map.Entry<String, Check> entry : checks.entrySet()) {
      try {
        entry.getValue().run();
        System.out.println("  ok      " + entry.getKey());
      } catch(final AssertionError | Exception ex) {
        failed++;
        System.out.println("  REVIEW  " + ex.getMessage());
      }
    }
    return failed > 0 ? REVIEW : 0;
  }
}
