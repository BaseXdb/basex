package org.basex.test.query.func;

import static org.basex.query.func.Function.*;

import org.basex.query.util.*;
import org.basex.test.query.*;
import org.junit.*;

/**
 * This class tests the functions of the XQuery Module.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNXQueryTest extends AdvancedQueryTest {
  /** Test method. */
  @Test
  public void eval() {
    query(_XQUERY_EVAL.args("1"), 1);
    query(_XQUERY_EVAL.args("1 + 2"), 3);
    query(_XQUERY_EVAL.args("\"$a\"", " map { '$a' := 'b' }"), "b");
    query(_XQUERY_EVAL.args("\"$a\"", " map { 'a' := 'b' }"), "b");
    query(_XQUERY_EVAL.args("\"$a\"", " map { 'a' := (1,2) }"), "1 2");
    query(_XQUERY_EVAL.args("\"declare variable $local:a external;$local:a\"",
        " map { xs:QName('local:a') := 1 }"), "1");
    query(_XQUERY_EVAL.args(".", " map { '' := 1 }"), "1");
    error(_XQUERY_EVAL.args("1+"), Err.INCOMPLETE);
    error("declare variable $a:=1;" + _XQUERY_EVAL.args("\"$a\""), Err.VARUNDEF);
    error("for $a in (1,2) return " + _XQUERY_EVAL.args("\"$a\""), Err.VARUNDEF);
    // check updating expressions
    error(_XQUERY_EVAL.args("delete node ()"), Err.BXXQ_UPDATING);
    error(_XQUERY_EVAL.args("declare %updating function local:x() {()}; local:x()"),
        Err.BXXQ_UPDATING);
    query(_XQUERY_EVAL.args("declare %updating function local:x() {()}; 1"));
  }

  /** Test method. */
  @Test
  public void invoke() {
    query(_XQUERY_INVOKE.args("src/test/resources/input.xq"), "XML");
    error(_XQUERY_INVOKE.args("src/test/resources/xxx.xq"), Err.WHICHRES);
  }

  /** Test method. */
  @Test
  public void type() {
    try {
      System.setErr(NULL);
      query(_XQUERY_TYPE.args("()"), "");
      query(_XQUERY_TYPE.args("1"), "1");
      query(_XQUERY_TYPE.args("(1, 2, 3)"), "1 2 3");
      query(_XQUERY_TYPE.args("<x a='1' b='2' c='3'/>/@*/data()"), "1 2 3");
    } finally {
      System.setErr(ERR);
    }
  }
}
