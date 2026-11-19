package org.basex.query.parse;

import java.io.*;
import java.nio.file.*;

/**
 * Locates the QT4 test suite for the tools of this package, in the same way as the QT4 test
 * runner ({@code QT4Test} in module basex-tests): the suite is taken from the environment variable
 * {@code QT4_PATH}, or from a {@code qt4tests} directory next to the root project.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
final class QT4Suite {
  /** Environment variable with the suite path. */
  private static final String PATH_ENV = "QT4_PATH";
  /** Name of the suite directory next to the root project. */
  private static final String SUITE_DIR = "qt4tests";
  /** Name of the catalog file. */
  private static final String CATALOG = "catalog.xml";

  /** Private constructor. */
  private QT4Suite() { }

  /**
   * Returns the directory of the test suite.
   * @return directory
   * @throws IOException if the suite cannot be found
   */
  static Path find() throws IOException {
    final String env = System.getenv(PATH_ENV);
    if(env != null && !env.isBlank()) {
      final Path path = Path.of(env);
      if(!isSuite(path)) throw new IOException(PATH_ENV + " does not point to the QT4 test " +
          "suite: " + path);
      return path;
    }
    for(Path dir = Path.of("").toAbsolutePath().getParent(); dir != null; dir = dir.getParent()) {
      final Path path = dir.resolve(SUITE_DIR);
      if(isSuite(path)) return path;
    }
    throw new IOException("QT4 test suite not found. Set " + PATH_ENV + ", or clone " +
        "https://github.com/qt4cg/qt4tests next to the root project.");
  }

  /**
   * Checks if a directory contains the test suite.
   * @param dir directory
   * @return result of check
   */
  private static boolean isSuite(final Path dir) {
    return Files.isRegularFile(dir.resolve(CATALOG));
  }
}
