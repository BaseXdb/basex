package org.basex.test.query.func;

import static org.basex.query.func.Function.*;

import java.io.*;

import org.basex.io.out.*;
import org.basex.test.query.*;
import org.junit.*;

/**
 * This class tests the XQuery utility functions prefixed with "util".
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNProfTest extends AdvancedQueryTest {
  /** Null output stream. */
  static final PrintStream NULL = new PrintStream(new NullOutput());

  /**
   * Test method for the prof:mem() function.
   */
  @Test
  public void utilMem() {
    final PrintStream err = System.err;
    try {
      System.setErr(NULL);
      check(_PROF_MEM);
      query(_PROF_MEM.args("()"));
      query(COUNT.args(_PROF_MEM.args(" 1 to 100 ", false)), "100");
      query(COUNT.args(_PROF_MEM.args(" 1 to 100 ", true)), "100");
      query(COUNT.args(_PROF_MEM.args(" 1 to 100 ", true, "label")), "100");
    } finally {
      System.setErr(err);
    }
  }

  /**
   * Test method for the prof:time() function.
   */
  @Test
  public void utilTime() {
    final PrintStream err = System.err;
    try {
      System.setErr(NULL);
      check(_PROF_TIME);
      query(_PROF_TIME.args("()"));
      query(COUNT.args(_PROF_TIME.args(" 1 to 100 ", false)), "100");
      query(COUNT.args(_PROF_TIME.args(" 1 to 100 ", true)), "100");
      query(COUNT.args(_PROF_TIME.args(" 1 to 100 ", true, "label")), "100");
    } finally {
      System.setErr(err);
    }
  }

  /**
   * Test method for the prof:sleep() function.
   */
  @Test
  public void utilSleep() {
    check(_PROF_SLEEP);
    query(_PROF_SLEEP.args(" 10"));
    query(_PROF_SLEEP.args(" 1"));
    query(_PROF_SLEEP.args(" 0"));
    query(_PROF_SLEEP.args(" -1"));
  }
}
