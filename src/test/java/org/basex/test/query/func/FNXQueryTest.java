package org.basex.test.query.func;

import static org.basex.query.func.Function.*;

import java.io.*;

import org.basex.io.out.*;
import org.basex.query.util.*;
import org.basex.test.query.*;
import org.junit.*;

/**
 * This class tests the XQuery functions prefixed with "xquery".
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNXQueryTest extends AdvancedQueryTest {
  /** Null output stream. */
  static final PrintStream NULL = new PrintStream(new NullOutput());

  /**
   * Test method for the xquery:eval() function.
   */
  @Test
  public void xqueryEval() {
    check(_XQUERY_EVAL);
    query(_XQUERY_EVAL.args("1"), 1);
    query(_XQUERY_EVAL.args("1 + 2"), 3);
    query(_XQUERY_EVAL.args("\"$a\"", " map { '$a' := 'b' }"), "b");
    query(_XQUERY_EVAL.args("\"$a\"", " map { 'a' := 'b' }"), "b");
    query(_XQUERY_EVAL.args("\"$a\"", " map { 'a' := (1,2) }"), "1 2");
    query(_XQUERY_EVAL.args("\"$local:a\"", " map { xs:QName('local:a') := 1 }"), "1");
    query(_XQUERY_EVAL.args(".", " map { '' := 1 }"), "1");
    error(_XQUERY_EVAL.args("1+"), Err.INCOMPLETE);
    error("declare variable $a:=1;" + _XQUERY_EVAL.args("\"$a\""), Err.VARUNDEF);
    error("for $a in (1,2) return " + _XQUERY_EVAL.args("\"$a\""), Err.VARUNDEF);
  }

  /**
   * Test method for the xquery:invoke() function.
   */
  @Test
  public void xqueryInvoke() {
    check(_XQUERY_INVOKE);
    query(_XQUERY_INVOKE.args("src/test/resources/input.xq"), "XML");
    error(_XQUERY_INVOKE.args("src/test/resources/xxx.xq"), Err.WHICHRES);
  }

  /**
   * Test method for the xquery:type() function.
   */
  @Test
  public void xqueryType() {
    final PrintStream err = System.err;
    try {
      System.setErr(NULL);
      check(_XQUERY_TYPE);
      query(_XQUERY_TYPE.args("()"), "");
      query(_XQUERY_TYPE.args("1"), "1");
      query(_XQUERY_TYPE.args("(1, 2, 3)"), "1 2 3");
      query(_XQUERY_TYPE.args("<x a='1' b='2' c='3'/>/@*/data()"), "1 2 3");
    } finally {
      System.setErr(err);
    }
  }
}
