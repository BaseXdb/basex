package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.junit.Assert.*;

import org.basex.query.*;
import org.basex.query.ast.*;
import org.basex.query.expr.*;
import org.basex.query.func.fn.*;
import org.basex.query.func.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.junit.*;

/**
 * XQuery functions: AST tests.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Leo Woerteler
 */
public final class FnModuleTest extends QueryPlanTest {
  /** Text file. */
  private static final String TEXT = "src/test/resources/input.xml";

  /** Test method. */
  @Test public void abs() {
    final Function func = Function.ABS;
    final String name = Util.className(func.clazz);

    check(func.args(" ()"), "", empty());
    check("for $i in 1 to 2 return " + func.args(" $i"), "1\n2", type(name, "xs:integer"));
    check("for $i in (1,2.0) return " + func.args(" $i"), "1\n2", type(name, "xs:decimal"));
    check(func.args(" <a>1</a>"), "1", type(name, "xs:double"));

    check("for $i in ([],[]) return " + func.args(" $i"), "", type(name, "xs:numeric?"));
    check("for $i in (1,<a>2</a>) return " + func.args(" $i"), "1\n2", type(name, "xs:numeric?"));
  }

  /** Test method. */
  @Test public void apply() {
    final Function func = Function.APPLY;
    final String name = Util.className(func.clazz);

    query(func.args(" true#0", " []"), true);
    query(func.args(" count#1", " [(1,2,3)]"));
    query(func.args(" string-join#1", " [ reverse(1 to 5) ! string() ]"), 54321);
    query("let $func := function($a, $b,$c) { $a + $b + $c } "
        + "let $args := [ 1, 2, 3 ] "
        + "return " + func.args(" $func", " $args"), 6);
    query("for $a in 2 to 3 "
        + "let $f := function-lookup(xs:QName('fn:concat'), $a) "
        + "return " + func.args(" $f", " array { 1 to $a }"), "12\n123");
    error(func.args(" false#0", " ['x']"), APPLY_X_X);
    error(func.args(" string-length#1", " [ ('a','b') ]"), INVTYPE_X_X_X);

    // no pre-evaluation (higher-order arguments), but type adjustment
    check(func.args(" true#0", " []"), true, type(name, "xs:boolean"));
    check(func.args(" count#1", " [1]"), 1, type(name, "xs:integer"));
    check(func.args(" abs#1", " [1]"), 1, type(name, "xs:numeric?"));
    check(func.args(" reverse#1", " [()]"), "", type(name, ""));
    check("(true#0, 1)[. instance of function(*)] ! " + func.args(" .", " []"), true,
        type(name, ""));

    // code coverage tests
    query("string-length(" + func.args(" reverse#1", " ['a']") + ")", 1);
    error(func.args(" true#0", " [1]"), QueryError.APPLY_X_X);
    error(func.args(" put#2", " [<_/>,'']"), QueryError.FUNCUP_X);
  }

  /** Test method. */
  @Test public void avg() {
    final Function func = Function.AVG;
    final String name = Util.className(func.clazz);

    check(func.args(" ()"), "", empty());
    check(func.args(" prof:void('x')"), "", empty(func.clazz));

    check(func.args(" 1"), 1, empty(func.clazz));
    check(func.args(" 1.0"), 1, empty(func.clazz));
    check(func.args(" 1e0"), 1, empty(func.clazz));
    check(func.args(" xs:float('1')"), 1, empty(func.clazz));

    check(func.args(" 1[. = 1]"), 1, type(name, "xs:decimal?"));
    check(func.args(" 1.0[. = 1]"), 1, type(name, "xs:decimal?"));
    check(func.args(" 1e0[. = 1]"), 1, type(name, "xs:double?"));
    check(func.args(" xs:float('1')[. = 1]"), 1, type(name, "xs:float?"));

    check(func.args(" (1,3[. = 5])"), 1, type(name, "xs:decimal"));
    check(func.args(" (1,3.0[. = 5])"), 1, type(name, "xs:decimal"));
    check(func.args(" (<a>1</a>, <a>3</a>)"), 2, type(name, "xs:double"));

    check(func.args(" (1 to 3)"), 2, empty(func.clazz));
    check(func.args(" reverse(1 to 3)"), 2, empty(func.clazz));
    check(func.args(" (1 to <_>3</_>)"), 2, type(name, "xs:decimal?"));
    check(func.args(" (1 to <_>0</_>)"), "", type(name, "xs:decimal?"));
    check(func.args(" (1 to 999999)"), 500000, empty(func.clazz));
    check(func.args(" (1 to 999999) ! 1"), 1, empty(func.clazz));

    check(func.args(" (1 to 3) ! 1"), 1, empty(func.clazz));
    check(func.args(" (1 to 3) ! xs:untypedAtomic(1)"), 1, empty(func.clazz));

    check(func.args(" util:replicate(1.0, 3)"), 1, empty(func.clazz));
    check(func.args(" util:replicate(<_>1</_>, 3)"), 1, type(name, "xs:double"));

    error(func.args(" true#0"), FIATOM_X);
    error(func.args(" util:replicate(true#0, 2)"), FIATOM_X);
    error(func.args(" (1 to 999999) ! true#0"), FIATOM_X);
  }

  /** Test method. */
  @Test public void bool() {
    final Function func = Function.BOOLEAN;
    final String name = Util.className(func.clazz);

    // pre-evaluated expressions
    check(func.args(1), true, empty(name));
    check(func.args(" ()"), false, empty(name));

    // function is replaced with fn:exists
    check(func.args(" <a/>/self::*"), true, exists(FnExists.class));
    // function is replaced by its argument (argument yields no result)
    check("(false(), true())[" + func.args(" .") + "]", true, empty(name));
    // no replacement
    check("(false(), 1)[" + func.args(" .") + "]", 1, exists(name));

    // optimize ebv
    check("[][. instance of xs:int][" + func.args(" .") + "]", "", empty(FnExists.class));
  }

  /** Test method. */
  @Test public void count() {
    final Function func = Function.COUNT;

    query(func.args(" (1 to 100000000) ! string()"), 100000000);
    query(func.args(" for $i in 1 to 100000000 return string('x')"), 100000000);
  }

  /** Test method. */
  @Test public void distinctValues() {
    final Function func = Function.DISTINCT_VALUES;

    query(func.args(" (1 to 100000000) ! 'a'") + "", "a");
    query("count(" + func.args(" 1 to 100000000") + ')', 100000000);
  }

  /** Test method. */
  @Test public void error() {
    final Function func = Function.ERROR;

    // pre-evaluate empty sequence
    error(func.args(), FUNERR1);
    error(func.args(" ()"), FUNERR1);
    query("(1," + func.args() + ")[1]", 1);

    // errors: defer error if not requested; adjust declared sequence type of {@link TypeCheck}
    query("head((1," + func.args() + "))", 1);
    query("head((1, function() { error() }()))", 1);
    query("declare function local:e() { error() }; head((1, local:e()))", 1);
    query("declare function local:e() as empty-sequence() { error() }; head((1, local:e()))", 1);
    query("declare %basex:inline(0) function local:f() { error() }; head((1, local:f()))", 1);
  }

  /** Test method. */
  @Test public void filter() {
    final Function func = Function.FILTER;
    query(func.args(" (0, 1)", " boolean#1"), 1);

    check(func.args(" ()", " boolean#1"), "", empty());
    check(func.args(" 1 to 9", " function($_) { $_ = 0 }"), "",
        exists("FuncItem[@type = 'function(xs:integer) as xs:boolean']"));
    check(func.args(" ('a',<a/>)", " function($_ as xs:string) { $_ = 'a' }"), "a",
        exists("FuncItem[@type = 'function(xs:string) as xs:boolean']"));
    check(func.args(" ('a',<a/>)", " function($_ as xs:string) as xs:boolean? { $_ = 'a' }"), "a",
        exists("FuncItem[@type = 'function(xs:string) as xs:boolean']"));

    check(func.args(" (<a/>, <b/>)", " boolean#1"), "<a/>\n<b/>",
        exists("FnFilter[@type = 'element()*']"));
    check(func.args(" <a/>", " boolean#1"), "<a/>",
        exists("FnFilter[@type = 'element()?']"));
  }

  /** Test method. */
  @Test public void foldLeft() {
    final Function func = Function.FOLD_LEFT;

    query("(1,'a')[. instance of xs:integer] ! " +
        func.args(" .", 0, " function($a, $b) { $b }"), 1);
    query("(1, function($a, $b) { $b })[. instance of function(*)] ! " +
        func.args("A", 1, " ."), "A");

    query(func.args(" ()", 1, " function($a, $b) { $b }"), 1);
    query(func.args(" prof:void(1)", 1, " function($a, $b) { $b }"), 1);
    query(func.args(" 2", 1, " function($a, $b) { $b }"), 2);

    check(func.args(" ()", " ()", " function($a, $b) { $b }"), "", empty());

    // should be unrolled and evaluated at compile time
    check(func.args(" 2 to 10", 1, " function($a, $b) { $a + $b }"),
        55,
        empty(Util.className(FnFoldLeft.class) + "[contains(@name, 'fold-left')]"),
        exists(Int.class));
    // should be unrolled but not evaluated at compile time
    check(func.args(" 2 to 10", 1, " function($a, $b) { 0 * random:double() + $b }"),
        10,
        exists(Int.class),
        empty(Util.className(FnFoldLeft.class) + "[contains(@name, 'fold-left')]"),
        count(Util.className(Arith.class) + "[@op = '+']", 9));
    // should not be unrolled
    check(func.args(" 1 to 11", 0, " function($a, $b) { $a + $b }"),
        66,
        exists(Util.className(FnFoldLeft.class) + "[contains(@name, 'fold-left')]"));
  }

  /** Test method. */
  @Test public void foldRight() {
    final Function func = Function.FOLD_RIGHT;

    check(func.args(" ()", " ()", " function($a, $b) { $a }"), "", empty());

    // should be unrolled and evaluated at compile time
    check(func.args(" 1 to 9", 10, " function($a, $b) { $a + $b }"),
        55,
        empty(FnFoldRight.class),
        exists(Int.class));
    // should be unrolled but not evaluated at compile time
    check(func.args(" 1 to 9", 10, " function($a, $b) { 0 * random:double() + $b }"),
        10,
        empty(FnFoldRight.class),
        exists(Int.class),
        count(Util.className(Arith.class) + "[@op = '+']", 9));
    // should not be unrolled
    check(func.args(" 0 to 10", 10, " function($a, $b) { $a + $b }"),
        65,
        exists(Util.className(FnFoldRight.class) + "[contains(@name, 'fold-right')]"));
  }

  /** Test method. */
  @Test public void forEach() {
    final Function func = Function.FOR_EACH;

    query("(1,not#1)[. instance of function(*)] ! " + func.args(" 1", " ."), "false");
    query("sort(" + func.args(" (1 to 2)[. > 0]", " string#1") + ')', "1\n2");
    check(func.args(" ()", " boolean#1"), "", empty());

    // pre-compute result size
    query("count(" + func.args(" 1 to 10000000000", " string#1") + ')', 10000000000L);
    check("count(" + func.args(" 1 to 20", " function($a) { $a, $a }") + ')', 40,
        exists(FnForEach.class));

    // should be unrolled and evaluated at compile time
    check(func.args(" 0 to 8", " function($x) { $x + 1 }"),
        "1\n2\n3\n4\n5\n6\n7\n8\n9",
        empty(FnForEach.class),
        exists(IntSeq.class));
    // should be unrolled but not evaluated at compile time
    check(func.args(" 1 to 9", " function($x) { 0 * random:double() + $x }"),
        "1\n2\n3\n4\n5\n6\n7\n8\n9",
        empty(FnForEach.class),
        empty(IntSeq.class),
        count(Util.className(Arith.class) + "[@op = '+']", 9));
    // should not be unrolled
    check(func.args(" 0 to 10", " function($x) { $x + 1 }"),
        "1\n2\n3\n4\n5\n6\n7\n8\n9\n10\n11",
        exists(Util.className(FnForEach.class) + "[contains(@name, 'for-each')]"));
  }

  /** Test method. */
  @Test public void forEachPair() {
    final Function func = Function.FOR_EACH_PAIR;

    query("(0,concat#2)[. instance of function(*)] ! " +
        func.args("A", "B", " ."), "AB");

    query("sort(" + func.args(" ('aa','bb')", " (2,2)", " substring#2") + ')', "a\nb");

    // pre-compute result size
    check("count(" + func.args(" 1 to 10000000000", " 1 to 10000000000",
        " function($a,$b) { 'a' }") + ')', 10000000000L, empty(FnForEachPair.class));
    check("count(" + func.args(" 1 to 20000000000", " 1 to 10000000000",
        " function($a,$b) { 'a' }") + ')', 10000000000L, empty(FnForEachPair.class));
    check("count(" + func.args(" 1 to 10000000000", " 1 to 20000000000",
        " function($a,$b) { 'a' }") + ')', 10000000000L, empty(FnForEachPair.class));
    check("count(" + func.args(" 1 to 20", " 1 to 20", " function($a,$b) { $a,$b }") + ')', 40,
        exists(FnForEachPair.class));

    check(func.args(" ()", "a", " matches#2"), "", empty());
    check(func.args("aa", " ()", " matches#2"), "", empty());

    query(func.args("aa", "a", " matches#2"), "true");
    query(func.args(" ('aa','bb')", "a", " matches#2"), "true");
    query(func.args("aa", " ('a','b')", " matches#2"), "true");
  }

  /** Test method. */
  @Test public void functionLookup() {
    final Function func = Function.FUNCTION_LOOKUP;
    final String name = Util.className(func.clazz);

    check("for $f in ('fn:true', 'fn:position') ! function-lookup(xs:QName(.), 0) " +
        "return (8,9)[$f()]", "8\n9\n9", exists(name));
    check("for $f in xs:QName('fn:position') return (8,9)[function-lookup($f, 0)()]", "8\n9",
        exists(name));
  }

  /** Test method. */
  @Test public void head() {
    final Function func = Function.HEAD;
    final String name = Util.className(func.clazz);

    // pre-evaluate empty sequence
    check(func.args(" ()"), "", empty(name));
    check(func.args(1), 1, empty(name));
    check(func.args(" (1,2)"), 1, empty(name));
    check(func.args(" <a/>"), "<a/>", empty(name));
    check(func.args(" <a/>[name()]"), "<a/>", empty(name));
    check(func.args(" (<a/>, <b/>)[name()]"), "<a/>", exists(name));
    check(func.args(" (1,error())"), 1, exists(Int.class));
    check(func.args(" reverse((1, 2, 3)[. > 1])"), 3, exists(UtilLastFrom.class));
  }

  /** Test method. */
  @Test public void innermost() {
    final Function func = Function.INNERMOST;
    query("let $n := <li/> return " + func.args(" ($n, $n)"), "<li/>");
  }

  /** Test for namespace functions and in-scope namespaces. */
  @Test
  public void inScopePrefixes() {
    final Function func = Function.IN_SCOPE_PREFIXES;
    query("sort(<e xmlns:p='u'>{" + func.args(" <e/>") + "}</e>/text()/tokenize(.))", "p\nxml");
  }

  /** Test method. */
  @Test public void jsonDoc() {
    final Function func = Function.JSON_DOC;
    query(func.args("src/test/resources/example.json") + "('address')('state')", "NY");
    query(func.args("src/test/resources/example.json") + "?address?state", "NY");
  }

  /** Test method. */
  @Test public void jsonToXml() {
    final Function func = Function.JSON_TO_XML;
    contains(func.args("null"), "xmlns");
    contains(func.args("null") + " update ()", "xmlns");
  }

  /** Test method. */
  @Test public void min() {
    final Function func = Function.MIN;
    query(func.args(1), 1);
    query(func.args(1.1), 1.1);
    query(func.args(" 1e1"), 10);
    query(func.args(" (1, 1e1)"), 1);
    query(func.args(" (1, 1.1, 1e1)") + " instance of xs:double", true);
    query(func.args(" <x>1</x>") + " instance of xs:double", true);
    query(func.args(" [1]"), 1);
    query(func.args(" (7, 6, 6, 6.0, 5.0, 5.0, xs:float('5'), xs:float('4'), xs:float('4'), " +
        "4, 4e0, 3e0, 3e0, 2e0, 2, 2, 1, <x>0</x>, <x>0</x>)"), 0);
    query(func.args(" (xs:double('NaN'), xs:float('NaN'))") + " instance of xs:double", true);

    query(func.args(" (xs:anyURI('b'), xs:anyURI('a'))") +
        " ! (. = 'a' and . instance of xs:anyURI)", true);
    query(func.args(" (xs:anyURI('c'), xs:anyURI('b'), 'a')") +
        " ! (. = 'a' and . instance of xs:string)", true);
    query(func.args(" ('b', xs:anyURI('a'))") +
        " ! (. = 'a' and . instance of xs:string)", true);
    query(func.args(" (2,3,1)"), 1);
    query(func.args(" (xs:date('2002-01-01'), xs:date('2003-01-01'), xs:date('2001-01-01'))"),
        "2001-01-01");
    query(func.args(" (xs:dayTimeDuration('PT1S'), xs:dayTimeDuration('PT0S'))"), "PT0S");
    query(func.args(" (xs:hexBinary('42'), xs:hexBinary('43'), xs:hexBinary('41'))"), 'A');

    query("for $n in (1, 2) return " + func.args(" $n"), "1\n2");
    query("for $n in (1, 2) return " + func.args(" ($n, $n)"), "1\n2");

    query("for $s in (['a', 'b'], ['c']) return " + func.args(" ($s, $s)"), "a\nc");

    // query plan checks
    check(func.args(" ()"), "", empty());
    check(func.args(" prof:void(123)"), "", empty(func.clazz));
    check(func.args(" 123"), 123, empty(func.clazz));
    check(func.args(" <x>1</x>"), 1, exists(func.clazz));

    // errors
    error(func.args(" xs:QName('a')"), CMP_X);
    error(func.args(" ('b', 'c', 'a', 1)"), CMP_X_X_X);
    error(func.args(" (2, 3, 1, 'a')"), CMP_X_X_X);
    error(func.args(" (false(), true(), false(), 1)"), CMP_X_X_X);
    error(func.args(" 'x'", 1), INVTYPE_X_X_X);
  }

  /** Test method. */
  @Test public void namespaceUriForPrefix() {
    final Function func = Function.NAMESPACE_URI_FOR_PREFIX;
    query("sort(<e xmlns:p='u'>{" + func.args("p", " <e/>") + "}</e>/text()/tokenize(.))", "u");
  }

  /** Test method. */
  @Test public void not() {
    final Function func = Function.NOT;
    final String name = Util.className(func.clazz);

    // pre-evaluated expressions
    check(func.args(1), false, empty(name));
    check(func.args(" ()"), true,  empty(name));

    // function is replaced with fn:exists
    check(func.args(" empty(1[.=1])"), true, exists(FnExists.class));
    // function is replaced with fn:empty
    check(func.args(" exists(1[.=1])"), false, exists(FnEmpty.class));
    check(func.args(" <a/>/self::*"), false, exists(FnEmpty.class));
    // function is replaced with fn:boolean
    check(func.args(" not(1[.=1])"), true, exists(FnBoolean.class));

    // function is replaced with fn:boolean
    check("for $i in (1,2) return " + func.args(" $i = $i + 1"),
        "true\ntrue", exists("*[@op != '=']"));
    check("for $i in (1,2) return " + func.args(" $i eq $i + 1"),
        "true\ntrue", exists("*[@op != 'eq']"));
    check("for $i in (1,2) return " + func.args(" $i = ($i, $i)"),
        "false\nfalse", exists(name));
  }

  /** Test method. */
  @Test public void number() {
    final Function func = Function.NUMBER;
    final String name = Util.className(func.clazz);

    query(func.args(1), 1);
    query(func.args(" ()"), "NaN");
    query(func.args(" xs:double('NaN')"), "NaN");
    query(func.args("X"), "NaN");
    query(func.args(" <a>1</a>"), 1);

    check("for $d in (1e0, 2e-1) return " + func.args(" $d"), "1\n0.2", empty(name));
    check("for $d in (1, 2.34) return " + func.args(" $d"), "1\n2.34", exists(name));
    check("for $d in (1e0, 2e-1) return $d[" + func.args() + ']', 1, empty(name));
    check("for $d in (1e0, 2e-1) return $d[" + func.args() + " = 1]", 1, empty(name));
    check("for $d in (1, 2.34) return $d[" + func.args() + ']', 1, exists(name));

    error(func.args(), QueryError.NOCTX_X);
    error(func.args(" true#0"), QueryError.FIATOM_X);
  }

  /** Test method. */
  @Test public void outermost() {
    final Function func = Function.INNERMOST;
    query("let $n := <li/> return " + func.args(" ($n, $n)"), "<li/>");
  }

  /** Test method. */
  @Test public void parseIetfDate() {
    final Function func = Function.PARSE_IETF_DATE;

    query(func.args("Wed, 06 Jun 1994 07:29:35 GMT"), "1994-06-06T07:29:35Z");
    query(func.args("Wed, 6 Jun 94 07:29:35 GMT"), "1994-06-06T07:29:35Z");
    query(func.args("Wed Jun 06 11:54:45 EST 0090"), "0090-06-06T11:54:45-05:00");
    query(func.args("Sunday, 06-Nov-94 08:49:37 GMT"), "1994-11-06T08:49:37Z");
    query(func.args("Wed, 6 Jun 94 07:29:35 +0500"), "1994-06-06T07:29:35+05:00");
    query(func.args("1 Nov 1234 05:06:07.89 gmt"), "1234-11-01T05:06:07.89Z");

    query(func.args("01-feb-3456 07:08:09 GMT"), "3456-02-01T07:08:09Z");
    query(func.args("01-FEB-3456 07:08:09 GMT"), "3456-02-01T07:08:09Z");
    query(func.args("Wed, 06 Jun 94 07:29:35 +0000 (GMT)"), "1994-06-06T07:29:35Z");
    query(func.args("Wed, 06 Jun 94 07:29:35"), "1994-06-06T07:29:35Z");

    String s = "Wed, Jan-01 07:29:35 GMT 19";
    query(func.args(s), "1919-01-01T07:29:35Z");
    for(int i = s.length(); --i >= 0;) {
      error(func.args(s.substring(0, i)), IETF_PARSE_X_X_X);
    }

    s = "Wed, 06 Jun 1994 07:29";
    query(func.args(s), "1994-06-06T07:29:00Z");
    for(int i = s.length(); --i >= 0;) {
      error(func.args(s.substring(0, i)), IETF_PARSE_X_X_X);
    }
    error(func.args(s + "X"), IETF_PARSE_X_X_X);

    error(func.args("Wed, 99 Jun 94 07:29:35 +0000 ("), IETF_PARSE_X_X_X);
    error(func.args("Wed, 99 Jun 94 07:29:35 +0000 (GT)"), IETF_PARSE_X_X_X);
    error(func.args("Wed, 99 Jun 94 07:29:35 +0000 (GMT"), IETF_PARSE_X_X_X);

    error(func.args("Wed, 99 Jun 94 07:29:35. GMT"), IETF_PARSE_X_X_X);
    error(func.args("Wed, 99 Jun 94 07:29:35 0500"), IETF_PARSE_X_X_X);
    error(func.args("Wed, 99 Jun 94 07:29:35 +0500"), IETF_INV_X);
  }

  /** Test method. */
  @Test public void parseJson() {
    final Function func = Function.PARSE_JSON;
    query(func.args("\"x\\u0000\""), "x\uFFFD");
  }

  /** Test method. */
  @Test public void parseXML() {
    final Function func = Function.PARSE_XML;
    contains(func.args("<x>a</x>") + "//text()", "a");
    query(func.args("<a/>") + "/a[node()]", "");
    query(func.args("<a/>") + "/*[1]", "<a/>");
    query(func.args("<a/>") + "/*[self::a]", "<a/>");
    query(func.args("<a/>") + "/*[1][self::a]", "<a/>");
    query(func.args("<a/>") + "/*[1][not(child::node())]", "<a/>");
    query(func.args("<a/>") + "/*[1][self::a][not(child::node())]", "<a/>");
  }

  /** Test method. */
  @Test public void randomNumberGenerator() {
    final Function func = Function.RANDOM_NUMBER_GENERATOR;

    // ensure that the same seed will generate the same result
    final String query = func.args(123) + "?number";
    assertEquals(query(query), query(query));
    // ensure that multiple number generators in a query will generate the same result
    query("let $seq := 1 to 10 "
        + "let $m1 := " + func.args() + " "
        + "let $m2 := " + func.args() + " "
        + "return every $test in ("
        + "  $m1('number') = $m2('number'),"
        + "  $m2('next')()('number') = $m1('next')()('number'),"
        + "  deep-equal($m1('permute')($seq), $m2('permute')($seq))"
        + ") satisfies true()", true);
    // ensure that the generator has no mutable state
    query("for $i in 1 to 100 "
        + "let $rng := " + func.args() + " "
        + "where $rng?next()?number ne $rng?next()?number "
        + "return error()");
  }

  /** Test method. */
  @Test public void replace() {
    // tests for issue GH-573:
    final Function func = Function.REPLACE;
    query(func.args("aaaaa bbbbbbbb ddd ", "(.{6,15}) ", "$1@"), "aaaaa bbbbbbbb@ddd ");
    query(func.args("aaaa AAA 123", "(\\s+\\P{Ll}{3,280}?)", "$1@"), "aaaa AAA@ 123@");
    error(func.args("asdf", "a{12,3}", ""), REGPAT_X);
  }

  /** Test method. */
  @Test public void resolveQName() {
    final Function func = Function.RESOLVE_QNAME;
    query("sort(<e xmlns:p='u'>{" + func.args("p:p", " <e/>") + "}</e>/text()/tokenize(.))", "p:p");
  }

  /** Test method. */
  @Test public void serialize() {
    final Function func = Function.SERIALIZE;
    contains(func.args(" <x/>"), "<x/>");
    contains(func.args(" <x/>", " " + serialParams("")), "<x/>");
    contains(func.args(" <x>a</x>", " " + serialParams("<method value='text'/>")), "a");
  }

  /** Test method. */
  @Test public void sort() {
    final Function func = Function.SORT;
    query(func.args(" ('b','a')", "http://www.w3.org/2005/xpath-functions/collation/codepoint"),
        "a\nb");

    query(func.args(" (1, 4, 6, 5, 3)"), "1\n3\n4\n5\n6");
    query(func.args(" (1,-2,5,10,-10,10,8)", " ()", " abs#1"), "1\n-2\n5\n8\n10\n-10\n10");
    query(func.args(" ((1,0), (1,1), (0,1), (0,0))"), "0\n0\n0\n0\n1\n1\n1\n1");
    query(func.args(" ('9','8','29','310','75','85','36-37','93','72','185','188','86','87','83',"
        + "'79','82','71','67','63','58','57','53','31','26','22','21','20','15','10')", " ()",
        " function($s) { number($s) }") + "[1]",
        "36-37");
    query(func.args(" (1,2)", " ()", " function($s) { [$s] }"), "1\n2");

    query("for $i in (10000,10001) return " + func.args(" 1 to $i") + "[1]", "1\n1");
    query("for $i in (10000,10001) return " + func.args(" reverse(1 to $i)") + "[1]", "1\n1");
    query("for $i in (10000,10001) return " + func.args(func.args(" reverse(1 to $i)")) + "[1]");
    query("for $i in (1,2) return " + func.args(func.args(" (1,$i)")) + "[1]", "1\n1");

    check(func.args(" ()"), "", empty());
    check(func.args("1"), 1, empty(func.clazz));

    check(func.args(" 1 to 100000000") + "[1]", 1, empty(func.clazz));
    check(func.args(" reverse(1 to 100000000)") + "[1]", 1, empty(func.clazz));
    check(func.args(" (1 to 100000000) ! 1") + "[1]", 1, empty(func.clazz));
    check(func.args(" reverse((1 to 100000000) ! 1)") + "[1]", 1, empty(func.clazz));

    error(func.args(" true#0"), FIATOM_X);
    error(func.args(" (1 to 2) ! true#0"), FIATOM_X);
  }

  /** Test method. */
  @Test public void staticBaseUri() {
    final Function func = Function.STATIC_BASE_URI;
    query("declare base-uri 'a/'; ends-with(" + func.args() + ", '/')", true);
    query("declare base-uri '.' ; ends-with(" + func.args() + ", '/')", true);
    query("declare base-uri '..'; ends-with(" + func.args() + ", '/')", true);
  }

  /** Test method. */
  @Test public void string() {
    final Function func = Function.STRING;
    final String name = Util.className(func.clazz);

    query(func.args(" ()"), "");
    query(func.args("A"), "A");
    query(func.args(" <a>A</a>"), "A");

    check("for $s in ('a', 'b') return " + func.args(" $s"), "a\nb", empty(name));
    check("for $s in (<a/>, <b/>) return " + func.args(" $s"), "\n", exists(name));
    check("for $s in ('a', 'b') return $s[" + func.args() + ']', "a\nb", empty(name));
    check("for $s in ('a' ,'b') return $s[" + func.args() + " = 'a']", "a", empty(name));
    check("for $s in (<a/> ,<b/>) return $s[" + func.args() + ']', "", exists(name));

    error(func.args(), QueryError.NOCTX_X);
    error(func.args(" true#0"), QueryError.FISTRING_X);
  }

  /** Test method. */
  @Test public void stringLength() {
    final Function func = Function.STRING_LENGTH;
    query(func.args(" ()"), 0);
    query(func.args("A"), 1);
    query("'A'[" + func.args() + ']', "A");
    query(func.args(" ([()], 'a')"), 1);
    error("true#0[" + func.args() + ']', QueryError.FISTRING_X);
  }

  /** Test method. */
  @Test public void subsequence() {
    final Function func = Function.SUBSEQUENCE;

    // static rewrites
    query(func.args(" ()", 0), "");
    query(func.args("A", 0), "A");
    query(func.args("A", 0, 0), "");
    query(func.args("A", 1), "A");
    query(func.args("A", 1), "A");
    query(func.args("A", 1, 1), "A");
    query(func.args(" (1, 2)", 2, 0), "");
    query(func.args(" (1, 2)", 2, 1), 2);
    query(func.args(" (1 to 3)", 2, 2), "2\n3");

    // special offset and length values
    query(func.args("A", 0.5), "A");
    query(func.args("A", " xs:double('NaN')"), "");
    query(func.args("A", 1, " xs:double('NaN')"), "");

    // known result size, iterative evaluation
    query(func.args(" (1 to 2) ! (. + 1)", 2), 3);
    query(func.args(" (1 to 3) ! (. + 1)", 2, 1), 3);
    query(func.args(" (1 to 3) ! (. + 1)", 2), "3\n4");
    query(func.args(" (1 to 3) ! (. + 1)", 3), 4);
    query(func.args(" (1 to 3) ! (. + 1)", 4), "");

    // non-numeric offsets and lengths
    query(func.args(" (1 to 3)", " <_>0</_>"), "1\n2\n3");
    query(func.args(" (1 to 3)", " <_>0</_>", 10), "1\n2\n3");
    query(func.args(" (1 to 3)", " <_>0</_>", " <_>10</_>"), "1\n2\n3");
    query(func.args(" (1 to 3)", 0, " <_>10</_>"), "1\n2\n3");
    query(func.args(" (1 to 2)", " <_>2</_>"), 2);
    query(func.args(" (1 to 2)", " <_>2</_>", 1), 2);
    query(func.args(" (1 to 2)", " <_>2</_>", " <_>1</_>"), 2);
    query(func.args(" (1 to 2)", 2, " <_>1</_>"), 2);

    // known result size
    query(func.args(" <_>1</_> + 1", 1), 2);
    query(func.args(" (<_>1</_> + 1, 2)", 2), 2);
    query(func.args(" (<_>1</_> + 1, 2)", 3), "");
    query(func.args(" (<_>1</_> + 1, 2, 3)", 2) + "[2]", 3);
    query(func.args(" prof:void(())", 1), "");
    query(func.args(" prof:void(())", 2), "");

    // unknown result size
    query(func.args(" 1[. = 0]", 1), "");
    query(func.args(" 1[. = 1]", 1), 1);
    query(func.args(" 1[. = 0]", 2), "");
    query(func.args(" 1[. = 1]", 2), "");
    query(func.args(" (1 to 2)[. = 0]", 1), "");
    query(func.args(" (1 to 4)[. < 3]", 1), "1\n2");
    query(func.args(" (1 to 2)[. = 0]", 2), "");
    query(func.args(" (1 to 4)[. < 3]", 2), 2);

    // value-based iterator
    query(func.args(" tokenize(<_></_>)", 3), "");
    query(func.args(" tokenize(<_>W</_>)", 3), "");
    query(func.args(" tokenize(<_>W X</_>)", 3), "");
    query(func.args(" tokenize(<_>W X Y</_>)", 3), "Y");
    query(func.args(" tokenize(<_>W X Y Z</_>)", 3), "Y\nZ");
  }

  /** Test method. */
  @Test public void substring() {
    final Function func = Function.SUBSTRING;
    contains(func.args("'ab'", " [2]"), "b");
  }

  /** Test method. */
  @Test public void sum() {
    final Function func = Function.SUM;
    query(func.args(1), 1);
    query(func.args(" 1 to 10"), 55);
    query(func.args(" 1 to 3037000499"), 4611686016981624750L);
    query(func.args(" 1 to 3037000500"), 4611686020018625250L);
    query(func.args(" 1 to 4294967295"), 9223372034707292160L);
    query(func.args(" 1 to <x>4294967295</x>"), 9223372034707292160L);
    query(func.args(" 1 to <x>0</x>"), 0);
    query(func.args(" reverse(1 to 10)"), 55);
    query(func.args(" sort(reverse(distinct-values(1 to 4294967295)))"), 9223372034707292160L);
    error(func.args(" 1 to 10000000000000"), RANGE_X);

    query(func.args(" (1 to 10) ! 1"), 10);
    query(func.args(" (1 to 10) ! 10"), 100);
    query(func.args(" (1 to 1000000) ! 1000000"), 1000000000000L);
    query(func.args(" (1 to 10) ! xs:untypedAtomic('10')"), 100);
    error(func.args(" (1 to 10) ! 'a'"), SUM_X_X);
    error(func.args(" (1 to 1000000) ! 'b'"), SUM_X_X);

    query("for $i in 1 to 2 return " + func.args(" ()", " $i"), "1\n2");
    query(func.args(" ()", " <x>0</x>"), 0);
    query(func.args(" ()", "A"), "A");
    query(func.args(" ()", " 1"), 1);
    query(func.args(" ()", " ()"), "");
    query(func.args(" ()", " prof:void('x')"), "");

    query("for $i in 1 to 2 return " + func.args(" prof:void('x')", " $i"), "1\n2");
    query(func.args(" prof:void('x')", " <x>0</x>"), 0);
    query(func.args(" prof:void('x')", "A"), "A");
    query(func.args(" prof:void('x')", " 1"), 1);
    query(func.args(" prof:void('x')", " ()"), "");
    query(func.args(" prof:void('x')", " prof:void('x')"), "");

    query(func.args(" 2 to 10"), 54);
    query(func.args(" 9 to 10"), 19);
    query(func.args(" -3037000500 to 3037000500"), 0);
    query(func.args(" ()", " ()"), "");
    query(func.args(1, "x"), 1);
    error(func.args(" ()", " (1,2)"), SEQFOUND_X);

    query(func.args(" (1,3,5)"), 9);
    query(func.args(" (-3,-1,1,3)"), 0);
    query(func.args(" (1,1.1,1e0)"), 3.1);

    check("for $i in 1 to 2 return " + func.args(" $i"), "1\n2",
        exists("FnSum[@type = 'xs:integer']"));
    check("for $i in 1 to 2 return " + func.args(" $i", "a"), "1\n2",
        exists("FnSum[@type = 'xs:integer']"));
    check(func.args(" 1[. = 1]", " 0.0"), 1, exists("FnSum[@type = 'xs:decimal']"));
    check(func.args(" 1[. = 1]", "a"), 1, exists("FnSum[@type = 'xs:anyAtomicType']"));
    check(func.args(" 1[. = 1]", " 1[. = 1]"), 1, exists("FnSum[@type = 'xs:integer?']"));
    check(func.args(" 1[. = 1]", " 'a'[. = 'a']"), 1, exists("FnSum[@type = 'xs:anyAtomicType?']"));
  }

  /** Test method. */
  @Test public void tail() {
    final Function func = Function.TAIL;

    // static rewrites
    query(func.args(" ()"), "");
    query(func.args("A"), "");
    query(func.args(" (1,2)"), 2);
    query(func.args(" (1 to 3)"), "2\n3");

    // known result size
    query(func.args(" <_>1</_> + 1"), "");
    query(func.args(" (<_>1</_> + 1, 2)"), 2);
    query(func.args(" prof:void(())"), "");

    // unknown result size
    query(func.args(" 1[. = 0]"), "");
    query(func.args(" 1[. = 1]"), "");
    query(func.args(" (1 to 2)[. = 0]"), "");
    query(func.args(" (1 to 4)[. < 3]"), 2);

    // value-based iterator
    query(func.args(" tokenize(<_></_>)"), "");
    query(func.args(" tokenize(<_>X</_>)"), "");
    query(func.args(" tokenize(<_>X Y</_>)"), "Y");
    query(func.args(" tokenize(<_>X Y Z</_>)"), "Y\nZ");
  }

  /** Test method. */
  @Test public void unparsedText() {
    final Function func = Function.UNPARSED_TEXT;
    contains(func.args(TEXT), "<html");
    contains(func.args(TEXT, "US-ASCII"), "<html");
    error(func.args(TEXT, "xyz"), ENCODING_X);
  }

  /** Test method. */
  @Test public void unparsedTextLines() {
    final Function func = Function.UNPARSED_TEXT_LINES;
    query(func.args(" ()"), "");
  }

  /** Test method. */
  @Test public void xmlToJson() {
    final Function func = Function.XML_TO_JSON;
    query(func.args(" <map xmlns='http://www.w3.org/2005/xpath-functions'>"
        + "<string key=''>í</string></map>", " map { 'indent' : 'no' }"), "{\"\":\"\u00ed\"}");
    query(func.args(" <fn:string key='root'>X</fn:string>"), "\"X\"");
  }
}
