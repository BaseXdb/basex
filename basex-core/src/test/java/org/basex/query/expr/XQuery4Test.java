package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.query.ast.*;
import org.basex.query.value.item.*;
import org.junit.jupiter.api.*;

/**
 * XQuery 4.0 tests.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class XQuery4Test extends QueryPlanTest {
  /** Version declaration. */
  @Test public void version40() {
    query("xquery version '1.0'; ()", "");
    query("xquery version '3.0'; ()", "");
    query("xquery version '3.1'; ()", "");
    query("xquery version '4.0'; ()", "");
    error("xquery version '0.0'; ()", XQUERYVER_X);
  }

  /** Lookup operator. */
  @Test public void lookup() {
    query("map { } ? ''", "");
    query("map { '': 1 } ? ''", 1);

    query("let $m := map { '': 1 } return $m?''", 1);
    query("let $_ := '' return map { '': 1 } ? $_", 1);
    query("let $_ := '' let $m := map { '': 1 } return $m?$_", 1);

    query("declare variable $_ := 1; [ 9 ]?$_", 9);
  }

  /** Otherwise expression. */
  @Test public void otherwise() {
    query("() otherwise ()", "");
    query("() otherwise 2", 2);
    query("1 otherwise ()", 1);
    query("1 otherwise 2", 1);
    query("1 otherwise 2 otherwise 3", 1);

    query("(1 to 10)[. = 0] otherwise (1 to 10)[. = 0]", "");
    query("(1 to 10)[. = 0] otherwise (1 to 10)[. = 2]", 2);
    query("(1 to 10)[. = 1] otherwise (1 to 10)[. = 0]", 1);
    query("(1 to 10)[. = 1] otherwise (1 to 10)[. = 2]", 1);

    query("(1 to 10)[. = 0] otherwise (1 to 10)[. = 0]", "");

    check("1 otherwise prof:void(2)", 1, root(Int.class));
    check("prof:void(1) otherwise 2", 2, root(Otherwise.class));
    check("prof:void(1) otherwise prof:void(2)", "", root(Otherwise.class));

    check("count(prof:void(1) otherwise prof:void(2))", 0, root(COUNT));
    query("count((1 to 10)[. = 0] otherwise (1 to 10)[. = 2])", 1);
    query("count((1 to 10)[. = 1] otherwise (1 to 10)[. = 2])", 1);

    query("(1 to 10)[. = 0] otherwise (1 to 6) ! string()", "1\n2\n3\n4\n5\n6");
    query("count((1 to 10)[. = 0] otherwise (1 to 6) ! string())", 6);
    query("count((1 to 10)[. = 0] otherwise sort((1 to 6)[. = 3]) otherwise 1)", 1);
  }

  /** Function item, arrow. */
  @Test public void functionItemArrow() {
    query("->() { }()", "");
    query("->($a) { $a }(())", "");
    query("->($a) { $a }(1)", 1);
    query("->($a) { $a }(1 to 2)", "1\n2");
    query("->($a, $b) { $a + $b }(1, 2)", 3);
    query("sum((1 to 6) ! ->($a) { $a * $a }(.))", 91);
    query("sum(for $i in 1 to 6  return ->($a) { $a * $a }($i))", 91);

    query("- ->(){ 1 }()", -1);
    query("--->(){ 1 }()", 1);
    query("1-->(){ 2 }()", -1);
    query("1--->(){ 2 }()", 3);
  }

  /** Function item, no parameter list. */
  @Test public void functionContextItem() {
    query("function { . + 1 }(1)", 2);
    query("-> { . + 1 }(1)", 2);
    query("-> { . + . }(1)", 2);
    query("sum((1 to 6) ! -> { . * . }(.))", 91);
    query("sum(for $i in 1 to 6  return -> { . * . }($i))", 91);

    query("- ->{ . }(1)", -1);
    query("--->{ . }(1)", 1);
    query("1-->{ . }(2)", -1);
    query("1--->{ . }(2)", 3);

    error("-> { . }(())", INVPROMOTE_X_X_X);
    error("-> { . }(1 to 5)", INVPROMOTE_X_X_X);
  }
}
