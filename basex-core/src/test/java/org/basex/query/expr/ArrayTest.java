package org.basex.query.expr;

import static org.basex.query.QueryError.*;

import org.basex.*;
import org.basex.query.value.array.*;
import org.junit.jupiter.api.*;

/**
 * Tests for XQuery arrays.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ArrayTest extends SandboxTest {
  /** Constructor. */
  @Test public void squareConstructor() {
    query("[]", "[]");
    query("[()]", "[()]");
    query("[ 1 ]", "[1]");
    query("[ 1, 2 ]", "[1, 2]");
    query("[ 1 to 2 ]", "[(1, 2)]");
    query("[[[[[ 1 ]]]]]", "[[[[[1]]]]]");
    query("[[[[[ 1 ], 2 ], 3 ], 4 ], 5 ]", "[[[[[1], 2], 3], 4], 5]");
  }

  /** Constructor. */
  @Test public void curlyConstructor() {
    query("array {}", "[]");
    query("array { () }", "[]");
    query("array { 1 }", "[1]");
    query("array { 1, 2 }", "[1, 2]");
    query("array { 1, 2 }", "[1, 2]");
    query("array { array { 1 } }", "[[1]]");
    query("array { array { 1, 2 }, array {} }", "[[1, 2], []]");
  }

  /** Constructor. */
  @Test public void lookup() {
    query("[ 1 ](1)", 1);
    query("[ 1, 2, 3 ](2)", 2);
    query("[ 1 to 2 ](1)", "1\n2");
    query("array { 1 to 2 }(2)", 2);
    query("[[ 1 ]](1)", "[1]");
    query("[[[ 1 ]]](1)(1)(1)", 1);

    query("[ 1, 2, 3 ]?([ 1 ])", 1);
    query("[ 1, 2, 3 ]?([ 1, 2 ])", "1\n2");

    error("[](0)", ARRAYEMPTY);
    error("[](1)", ARRAYEMPTY);
    error("[ 1 ](-5000000000)", ARRAYBOUNDS_X_X);
    error("[ 1 ](-1)", ARRAYBOUNDS_X_X);
    error("[ 1 ](0)", ARRAYBOUNDS_X_X);
    error("[ 1 ](2)", ARRAYBOUNDS_X_X);
    error("[ 1 ](5000000000)", ARRAYBOUNDS_X_X);
  }

  /** Constructor. */
  @Test public void function() {
    query("declare function local:x($x as array(*)) { array:head($x) }; local:x([ 1, 2 ])", 1);
    query("declare function local:x($x as xs:integer*) { $x }; local:x([ 1, 2 ])", "1\n2");
  }

  /** Arithmetics. */
  @Test public void arithmetics() {
    query("[] + ()", "");
    query("() - []", "");
    query("[] + 1", "");
    query("2 + []", "");
    query("[[[]]] + 2", "");
    query("[ 2 ] - 1", 1);
    query("[[ 6 ]] * 2", 12);
    query("[[ 7 ]] idiv 2", 3);
    query("[[ 8 ]] div 5", 1.6);
    query("[[ 9 ]] mod [[ 5 ]]", 4);

    error("[ 1, 2 ] + 3", SEQFOUND_X);
    error("1 + [ 2, 3 ]", SEQFOUND_X);
  }

  /** Value comparison. */
  @Test public void valueComparison() {
    query("[] eq ()", "");
    query("[] eq 1", "");
    query("2 eq []", "");
    query("[[[]]] eq 2", "");
    query("[ 2 ] eq 1", false);
    query("[[ 6 ]] ne 2", true);
    query("[[ 7 ]] lt 3", false);
    query("[[ 8 ]] gt 4", true);
    query("[[ 9 ]] le 5", false);
    query("[[ 10 ]] ge 6", true);

    error("[ 1, 2 ] eq 3", SEQFOUND_X);
    error("1 eq [ 2, 3 ]", SEQFOUND_X);

    query("[] eq <a>a</a>", "");
    query("not([] eq 'a')", true);
  }

  /** General comparison. */
  @Test public void generalComparison() {
    query("[] = ()", false);
    query("() = []", false);
    query("[] != 1", false);
    query("2 = []", false);
    query("[[[]]] = 2", false);
    query("[ 2 ] = 1", false);
    query("[[ 6 ]] != 2", true);
    query("[ 7, 8 ] = 3", false);
    query("[ 8, 9, 10 ] != [ 6, 7 ]", true);

    query("[] = 'a'", false);
    query("not([] = 'a')", true);
    query("[ 'a', 'b' ] = <a>a</a>", true);
    query("not([ 'a', 'b' ] = 'a')", false);
    query("not([ 'a', 'b' ] = <a>a</a>)", false);
  }

  /** Element constructor. */
  @Test public void elementConstructor() {
    query("element a { [] }", "<a/>");
    query("element a { [()] }", "<a/>");
    query("element a { [ 1 ] }", "<a>1</a>");
    query("element a { [ <b>c</b> ] }", "<a>\n<b>c</b>\n</a>");
    query("element a { [ <b>c</b>, <b>d</b> ] }", "<a>\n<b>c</b>\n<b>d</b>\n</a>");

    query("element { [ 'a' ] } { }", "<a/>");

    error("element { [ 'a', 'b' ] } { }", SEQFOUND_X);
    error("element { [] } { }", EMPTYFOUND_X);
  }

  /** Attribute constructor. */
  @Test public void attributeConstructor() {
    query("<e a='{ [] }'/>/@a/string()", "");
    query("<e a='{ [()] }'/>/@a/string()", "");
    query("<e a='{ [ 1 ] }'/>/@a/string()", 1);
    query("<e a='{ [ 1 to 2 ] }'/>/@a/string()", "1 2");
    query("<e a='{ array { 1 to 2 } }'/>/@a/string()", "1 2");

    query("attribute a { [] }/string()", "");
    query("attribute a { [()] }/string()", "");
    query("attribute a { [ 1 ] }/string()", 1);
    query("attribute a { [ 1, 2 ] }/string()", "1 2");

    query("attribute { [ 'a' ] } { }/name()", "a");

    error("attribute { [ 'a', 'b' ] } { }", SEQFOUND_X);
    error("attribute { [] } { }", EMPTYFOUND_X);
  }

  /** Attribute constructor. */
  @Test public void namespaceConstructor() {
    query("namespace { [ 'a' ] } { 'u' }/name()", "a");
    query("namespace a { [ 'b' ] }/string()", "b");
    query("namespace a { [ 'b', 'c' ] }/string()", "b c");

    error("namespace { [ 'a', 'b' ] } { 'u' }", SEQFOUND_X);
  }

  /** Text constructor. */
  @Test public void textConstructor() {
    query("<e>{ [] }</e>/string()", "");
    query("<e>{ [()] }</e>/string()", "");
    query("<e>{ [ 1 ] }</e>/string()", 1);
    query("<e>{ [ 1, 2 ] }</e>/string()", "1 2");
  }

  /** Comment constructor. */
  @Test public void commentConstructor() {
    query("comment { [] }/string()", "");
    query("comment { [ 'a' ] }/string()", "a");
    query("comment { [ ('a', <b>c</b>) ] }/string()", "a c");
  }

  /** PI constructor. */
  @Test public void piConstructor() {
    query("processing-instruction { [ 'a' ] } { [ 'b' ] }", "<?a b?>");

    error("processing-instruction { [] } { }", STRNCN_X_X);
    error("processing-instruction { [ 'a', 'b' ] } { }", SEQFOUND_X);
  }

  /** Group by clause. */
  @Test public void groupBy() {
    query("for $a in ('A', 'B') group by $b := [ 'a' ] return $a", "A\nB");
    query("for $a in ('A', 'B') group by $b := [] return $a", "A\nB");

    error("for $a in ('A', 'B') group by $b := [ 'a', 'b' ] return $a", SEQFOUND_X);
  }

  /** Order by clause. */
  @Test public void orderBy() {
    query("for $a in ('A', 'B') order by [ 'a' ] return $a", "A\nB");
    query("for $a in ('A', 'B') order by [] return $a", "A\nB");

    error("for $a in ('A', 'B') order by [ 'a', 'b' ] return $a", SEQFOUND_X);
  }

  /** Switch expression. */
  @Test public void swtch() {
    query("switch([]) case 'a' return 'a' default return 'b'", "b");
    query("switch([ 'a' ]) case 'a' return 'a' default return 'b'", "a");
    query("for $a in ('a', [ 'a' ]) " +
        "return switch($a) case 'a' return 'a' default return 'b'", "a\na");

    error("switch([ 'a', 'b' ]) case 'a' return 'a' default return 'b'", SEQFOUND_X);
  }

  /** Cast expression. */
  @Test public void cast() {
    query("[] cast as xs:integer?", "");
    query("xs:integer([ 1 ])", 1);

    error("[] cast as xs:integer", INVTYPE_X_X_X);
    error("[ 1, 2 ] cast as xs:integer", INVTYPE_X_X_X);
  }

  /** Functions. */
  @Test public void functions() {
    error("string([ 1 ])", FISTRING_X);
    query("number([ 1 ])", 1);
    query("concat('a', [ 'b' ], [])", "ab");
    query("count(([ 1 ], [ 2 ]))", 2);
  }

  /** Type checks. */
  @Test public void typing() {
    query("count(data([ 'a', 'b' ]))", 2);
    query("count(data([ 'a', <a>b</a> ]))", 2);
    query("count(distinct-values([ 'a', 'a' ]))", 1);
    query("count(distinct-values([ 'a', <a>a</a> ]))", 1);
    query("count(distinct-values([ 'a', <a>b</a> ]))", 2);
  }

  /** Atomize key. */
  @Test public void atomKey() {
    query("[ 'x' ]([ 1 ])", "x");
  }

  /** Tests if {@link XQArray#members()} uses the array's offset correctly. */
  @Test public void gh1047() {
    query("array:head(array:for-each(array:subarray([ 1, 2, 3 ], 2), function($x) { $x }))",
        2);
    query("array:fold-left(array:tail([ 1, 2, 3 ]), (), function($res, $fn) { ($res, $fn) })",
        "2\n3");
  }
}
