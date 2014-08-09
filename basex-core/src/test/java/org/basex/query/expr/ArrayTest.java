package org.basex.query.expr;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.junit.*;

/**
 * Tests for XQuery arrays.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class ArrayTest extends AdvancedQueryTest {
  /** Constructor. */
  @Test public void squareConstructor() {
    array("[]", "[]");
    array("[()]", "[()]");
    array("[1]", "[1]");
    array("[1,2]", "[1, 2]");
    array("[1 to 2]", "[(1, 2)]");
    array("[[[[[1]]]]]", "[[[[[1]]]]]");
    array("[[[[[1],2],3],4],5]", "[[[[[1], 2], 3], 4], 5]");
  }

  /** Constructor. */
  @Test public void curlyConstructor() {
    array("array {}", "[]");
    array("array { () }", "[]");
    array("array { 1 }", "[1]");
    array("array { 1, 2 }", "[1, 2]");
    array("array { 1, 2 }", "[1, 2]");
    array("array {array { 1 } }", "[[1]]");
    array("array {array { 1,2 }, array {} }", "[[1, 2], []]");
  }

  /** Constructor. */
  @Test public void lookup() {
    query("[1](1)", "1");
    query("[1, 2, 3](2)", "2");
    query("[1 to 2](1)", "1 2");
    query("array { 1 to 2 }(2)", "2");
    array("[[1]](1)", "[1]");
    query("[[[1]]](1)(1)(1)", "1");

    error("[](0)", Err.ARRAYPOS);
    error("[](1)", Err.ARRAYPOS);
    error("[1](-5000000000)", Err.ARRAYPOS);
    error("[1](-1)", Err.ARRAYPOS);
    error("[1](0)", Err.ARRAYPOS);
    error("[1](2)", Err.ARRAYPOS);
    error("[1](5000000000)", Err.ARRAYPOS);
  }

  /** Constructor. */
  @Test public void function() {
    query("declare function local:x($x as array(*)) { array:head($x) }; local:x([1, 2])", "1");
    query("declare function local:x($x as xs:integer*) { $x }; local:x([1, 2])", "1 2");
  }

  /** Arithmetics. */
  @Test public void arithmetics() {
    query("[] + ()", "");
    query("() - []", "");
    query("[] + 1", "");
    query("2 + []", "");
    query("[[[]]] + 2", "");
    query("[2] - 1", "1");
    query("[[6]] * 2", "12");
    query("[[7]] idiv 2", "3");
    query("[[8]] div 5", "1.6");
    query("[[9]] mod [[5]]", "4");

    error("[1,2] + 3", Err.SEQFOUND);
    error("1 + [2,3]", Err.SEQFOUND);
  }

  /** Value comparison. */
  @Test public void valueComparison() {
    query("[] eq ()", "");
    query("[] eq 1", "");
    query("2 eq []", "");
    query("[[[]]] eq 2", "");
    query("[2] eq 1", "false");
    query("[[6]] ne 2", "true");
    query("[[7]] lt 3", "false");
    query("[[8]] gt 4", "true");
    query("[[9]] le 5", "false");
    query("[[10]] ge 6", "true");

    error("[1,2] eq 3", Err.SEQFOUND);
    error("1 eq [2,3]", Err.SEQFOUND);
  }

  /** General comparison. */
  @Test public void generalComparison() {
    query("[] = ()", "false");
    query("[] != 1", "false");
    query("2 = []", "false");
    query("[[[]]] = 2", "false");
    query("[2] = 1", "false");
    query("[[6]] != 2", "true");
    query("[7,8] = 3", "false");
    query("[8,9,10] != [6,7]", "true");
  }

  /** General comparison. */
  @Test public void elementConstructor() {
    query("element a { [] }", "<a/>");
    query("element a { [()] }", "<a/>");
    query("element a { [1] }", "<a>1</a>");
    query("element a { [ <b>c</b> ] }", "<a>c</a>");
    query("element a { [ <b>c</b>, <b>d</b> ] }", "<a>c d</a>");
  }

  /** General comparison. */
  @Test public void attributeConstructor() {
    query("<e a='{ [] }'/>/@a/string()", "");
    query("<e a='{ [()] }'/>/@a/string()", "");
    query("<e a='{ [1] }'/>/@a/string()", "1");
    query("<e a='{ [1 to 2] }'/>/@a/string()", "1 2");
    query("<e a='{ array { 1 to 2 } }'/>/@a/string()", "1 2");

    query("attribute a { [] }/string()", "");
    query("attribute a { [()] }/string()", "");
    query("attribute a { [1] }/string()", "1");
    query("attribute a { [1, 2] }/string()", "1 2");
  }

  /** General comparison. */
  @Test public void textConstructor() {
    query("<e>{ [] }</e>/string()", "");
    query("<e>{ [()] }</e>/string()", "");
    query("<e>{ [1] }</e>/string()", "1");
    query("<e>{ [1, 2] }</e>/string()", "1 2");
  }

  /**
   * Compares the serialized version of an array.
   * @param query query string
   * @param exp expected result
   */
  private static void array(final String query, final String exp) {
    query(_ARRAY_SERIALIZE.args(" " + query), exp);
  }
}
