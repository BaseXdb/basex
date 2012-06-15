package org.basex.test.query.func;

import static org.basex.query.func.Function.*;

import java.io.*;

import org.basex.io.out.*;
import org.basex.query.util.*;
import org.basex.test.query.*;
import org.junit.*;

/**
 * This class tests the XQuery utility functions prefixed with "util".
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNXQueryTest extends AdvancedQueryTest {
  /** Null output stream. */
  static final PrintStream NULL = new PrintStream(new NullOutput());

  /**
   * Test method for the util:eval() function.
   */
  @Test
  public void utilEval() {
    check(_XQUERY_EVAL);
    query(_XQUERY_EVAL.args("1"), 1);
    query(_XQUERY_EVAL.args("1 + 2"), 3);
    error(_XQUERY_EVAL.args("1+"), Err.INCOMPLETE);
    error("declare variable $a:=1;" + _XQUERY_EVAL.args("\"$a\""), Err.VARUNDEF);
    error("for $a in (1,2) return " + _XQUERY_EVAL.args("\"$a\""), Err.VARUNDEF);
  }

  /**
   * Test method for the util:run() function.
   */
  @Test
  public void utilRun() {
    check(_XQUERY_INVOKE);
    query(_XQUERY_INVOKE.args("src/test/resources/input.xq"), "XML");
    error(_XQUERY_INVOKE.args("src/test/resources/xxx.xq"), Err.FILE_IO);
  }

  /**
   * Test method for the util:type() function.
   */
  @Test
  public void utilType() {
    final PrintStream err = System.err;
    System.setErr(NULL);
    check(_XQUERY_TYPE);
    query(_XQUERY_TYPE.args("()"), "");
    query(_XQUERY_TYPE.args("1"), "1");
    query(_XQUERY_TYPE.args("(1, 2, 3)"), "1 2 3");
    query(_XQUERY_TYPE.args("<x a='1' b='2' c='3'/>/@*/data()"), "1 2 3");
    System.setErr(err);
  }
}
