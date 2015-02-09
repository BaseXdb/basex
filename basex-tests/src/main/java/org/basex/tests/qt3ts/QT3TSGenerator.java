package org.basex.tests.qt3ts;

import java.io.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.util.*;

/**
 * Generates the QT3TS JUnit tests from XML files.
 * For the resource files to be collected, the tests have to be run once with the
 * {@code QT3TS} property set to the path of the test suite files, e.g. by setting
 * {@code -DQT3TS=...} on the JVM.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
public final class QT3TSGenerator {
  /** Path to the XQuery files. */
  private static final String XQ_PATH = "src/main/xquery";
  /** Private default constructor. */
  private QT3TSGenerator() { /* hidden */ }

  /**
   * Main method, initiates the generation of the test cases.
   * @param args command-line arguments
   * @throws BaseXException database exception
   */
  public static void main(final String[] args) throws BaseXException {
    final File qt3ts = args.length > 0 ? new File(args[0]) : null;
    if(qt3ts == null || !qt3ts.isDirectory()) {
      Util.errln("Usage: java " + QT3TSGenerator.class.getName() + " [<path>]\n" +
          "    - <path>: Path to the QT3 test suite files");
      System.exit(1);
    }

    System.setProperty("QT3TS", qt3ts.getAbsolutePath());
    Util.outln(qt3ts.getAbsolutePath());

    final Context ctx = new Context();
    new Set("QUERYPATH", XQ_PATH).execute(ctx);
    new Set("BINDINGS", "QT3TS=" + qt3ts).execute(ctx);
    new Run(new File(XQ_PATH, "qt3ts.xq").getPath()).execute(ctx);
  }
}
