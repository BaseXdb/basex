package org.basex.query.func.unit;

import static org.basex.query.func.unit.Constants.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.jobs.*;
import org.basex.io.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * XQUnit tests: Testing multiple modules.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Suite {
  /** Failures. */
  public int failures;
  /** Errors. */
  public int errors;
  /** Skipped. */
  public int skipped;
  /** Tests. */
  public int tests;

  /**
   * Tests all test functions in the specified path.
   * @param ctx database context
   * @param root path to test modules
   * @param job calling job
   * @return resulting value
   * @throws IOException I/O exception
   */
  public FElem test(final IOFile root, final Context ctx, final Job job) throws IOException {
    final ArrayList<IOFile> files = new ArrayList<>();

    final Performance perf = new Performance();
    final FElem suites = new FElem(TESTSUITES);
    if(root.isDir()) {
      for(final String path : root.descendants()) {
        final IOFile file = new IOFile(root, path);
        if(file.hasSuffix(IO.XQSUFFIXES)) files.add(file);
      }
    } else {
      files.add(root);
    }

    for(final IOFile file : files) {
      final Unit unit = new Unit(file, ctx, job);
      unit.test(suites);
      errors += unit.errors;
      failures += unit.failures;
      skipped += unit.skipped;
      tests += unit.tests;
    }

    suites.add(TIME, Unit.time(perf));
    return suites;
  }
}
