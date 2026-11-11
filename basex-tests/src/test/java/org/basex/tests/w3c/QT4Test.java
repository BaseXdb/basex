package org.basex.tests.w3c;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import org.basex.tests.bxapi.xdm.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.*;

/**
 * Runs the QT4 test suite: one dynamic container per test set, one test per test case.
 * The suite is taken from {@code QT4_PATH}, or from a {@code qt4tests} directory next to the
 * root project (the tests are skipped if neither exists).
 * For XSLT 4.0 and XSD 1.1, put Saxon-EE before Saxon-HE on the run configuration's classpath.
 * All tests share one driver and must run sequentially.
 *
 * <p>Run with {@code mvn -f basex-tests/pom.xml -Dtest=QT4Test test}.
 * Use {@code -Dqt4.test-set=fn/concat.xml} to select a test set, or a glob such as
 * {@code -Dqt4.test-set=fn/*.xml} to select several. Paths are relative to the suite directory.
 * A filter that matches no catalog entries fails the test factory.</p>
 *
 * @author BaseX Team, BSD License
 */
@Execution(ExecutionMode.SAME_THREAD)
public class QT4Test {
  /** Environment variable with the suite path. */
  private static final String PATH_ENV = "QT4_PATH";
  /** Name of the suite directory next to the root project. */
  private static final String SUITE_DIR = "qt4tests";
  /** System property with a test set glob. */
  private static final String TEST_SET = "qt4.test-set";

  /** Test suite driver. */
  private static QT3TS driver;

  /**
   * Creates the driver; skips all tests if the suite is missing.
   * @throws IOException I/O exception
   */
  @BeforeAll
  public static void setUp() throws IOException {
    final String env = System.getenv(PATH_ENV);
    final Path path;
    if(env != null && !env.isBlank()) {
      path = Path.of(env);
      assertTrue(isSuite(path), PATH_ENV + " does not point to the QT4 test suite: " + path);
    } else {
      path = sibling();
      assumeTrue(path != null, "QT4 test suite not found. Set " + PATH_ENV + ", or clone "
          + "https://github.com/qt4cg/qt4tests next to the root project.");
    }
    driver = new QT3TS(path.toString(), Path.of("target", "qt4").toAbsolutePath().toString());
  }

  /**
   * Checks if a directory contains the test suite.
   * @param dir directory
   * @return result of check
   */
  private static boolean isSuite(final Path dir) {
    return Files.isRegularFile(dir.resolve("catalog.xml"));
  }

  /**
   * Looks for the suite in the ancestors of the working directory (the working directory is
   * the root project or a module; worktrees inside the project are covered as well).
   * @return suite directory, or {@code null}
   */
  private static Path sibling() {
    for(Path dir = Path.of("").toAbsolutePath().getParent(); dir != null; dir = dir.getParent()) {
      final Path path = dir.resolve(SUITE_DIR);
      if(isSuite(path)) return path;
    }
    return null;
  }

  /**
   * Closes the driver.
   */
  @AfterAll
  public static void tearDown() {
    if(driver != null) {
      driver.close();
      driver = null;
    }
  }

  /**
   * Creates a container per selected test set. Children are read when the container is executed.
   * @return containers
   */
  @TestFactory
  public Stream<DynamicContainer> tests() {
    final String filter = System.getProperty(TEST_SET, "").trim();
    final PathMatcher matcher = filter.isEmpty() ? null :
      FileSystems.getDefault().getPathMatcher("glob:" + filter);
    final List<String> files = driver.testSetFiles().stream().
        filter(file -> matcher == null || matcher.matches(Path.of(file))).sorted().toList();
    assertFalse(files.isEmpty(), "No QT4 test sets match " + TEST_SET + '=' + filter);
    return files.stream().map(file -> DynamicContainer.dynamicContainer(file,
        Path.of(driver.path(), file).toUri(), Stream.of(file).flatMap(QT4Test::cases)));
  }

  /**
   * Creates the tests of a test set, including unsupported cases (aborted via an assumption).
   * @param file test set file
   * @return tests
   */
  private static Stream<DynamicTest> cases(final String file) {
    try {
      final QT3TS.TestSet set = driver.open(file);
      return set.cases().stream().map(item -> DynamicTest.dynamicTest(QT3TS.name(item),
          () -> run(set, item)));
    } catch(final IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  /**
   * Runs a test case.
   * @param set test set
   * @param item test case
   * @throws Exception exception
   */
  private static void run(final QT3TS.TestSet set, final XdmItem item) throws Exception {
    assumeFalse(set.ignored(item), "Not supported by BaseX.");
    final QT3TS.Outcome outcome = set.run(item);
    if(outcome.expected() != null) fail(outcome.text() + "File: " + set.uri);
  }

}
