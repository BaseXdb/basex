package org.basex.query.func;

import static org.basex.query.func.Function.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the Output Module.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class OutModuleTest extends SandboxTest {
  /** Test method. */
  @Test public void cr() {
    final Function func = _OUT_CR;
    query("string-to-codepoints(" + func.args() + ')', 13);
  }

  /** Test method. */
  @Test public void format() {
    final Function func = _OUT_FORMAT;
    query(func.args("x", "x"), "x");
    query(func.args("%d", " 1"), "1");
    query(func.args("%2d", " 1"), " 1");
    query(func.args("%05d", " 123"), "00123");
  }

  /** Test method. */
  @Test public void nl() {
    final Function func = _OUT_NL;
    query(func.args(), "\n");
    query("string-to-codepoints(" + func.args() + ')', 10);
  }

  /** Test method. */
  @Test public void tab() {
    final Function func = _OUT_TAB;
    query(func.args(), "\t");
    query("string-to-codepoints(" + func.args() + ')', 9);
  }
}
