package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.junit.Assert.*;

import org.basex.query.*;
import org.basex.query.ast.*;
import org.basex.query.expr.*;
import org.basex.query.func.fn.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.junit.*;

/**
 * XQuery functions: AST tests.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Leo Woerteler
 */
public final class FnModuleTest extends QueryPlanTest {
  /** Text file. */
  private static final String TEXT = "src/test/resources/input.xml";

  /** Tests functions with first argument that can be empty ({@link StandardFunc#optFirst}). */
  @Test public void optFirst() {
    // use fn:abs as candidate
    final Function func = Function.ABS;
    final String name = Util.className(func.clazz);

    // pre-evaluate empty sequence
    check(func.args(" ()"), "", "empty(//" + name + ')');
    // pre-evaluate argument
    check(func.args(1), 1, "empty(//" + name + ')');

    // function is replaced by its argument (argument yields no result)
    check(func.args(" prof:void(())"), "", "empty(//" + name + ')');
    // but type is adjusted
    check(func.args(" <_>1</_>"), 1, "//" + name + "/(@type = 'xs:numeric' and @size = 1)");
    // no adjustment of type
    check(func.args(" 1 ! array { . }"), 1,
        "//" + name + "/(@type = 'xs:numeric?' and @size = -1)");
  }

  /** Test method. */
  @Test public void apply() {
    final Function func = Function.APPLY;
    final String name = Util.className(func.clazz);

    query(func.args(" true#0", " []"), true);
    query(func.args(" count#1", " [(1,2,3)]"));
    query(func.args(" string-join#1", " [ reverse(1 to 5) ! string() ]"), 54321);
    query("let $func := function($a,$b,$c) { $a + $b + $c } "
        + "let $args := [ 1, 2, 3 ] "
        + "return " + func.args(" $func", " $args"), 6);
    query("for $a in 2 to 3 "
        + "let $f := function-lookup(xs:QName('fn:concat'), $a) "
        + "return " + func.args(" $f", " array { 1 to $a }"), "12\n123");
    error(func.args(" false#0", " ['x']"), APPLY_X_X);
    error(func.args(" string-length#1", " [ ('a','b') ]"), INVPROMOTE_X);

    // no pre-evaluation (higher-order arguments), but type adjustment
    check(func.args(" true#0", " []"), true,
        "//" + name + "/(@type = 'xs:boolean' and @size = 1)");
    check(func.args(" count#1", " [1]"), 1,
        "//" + name + "/(@type = 'xs:integer' and @size = 1)");
    check(func.args(" abs#1", " [1]"), 1,
        "//" + name + "/(@type = 'xs:numeric?' and @size = -1)");
    check(func.args(" reverse#1", " [()]"), "",
        "//" + name + "/(@type = 'item()*' and @size = -1)");
    check("(true#0, 1)[. instance of function(*)] ! " + func.args(" .", " []"), true,
        "//" + name + "/(@type = 'item()*' and @size = -1)");

    // code coverage tests
    query("string-length(" + func.args(" reverse#1", " ['a']") + ")", 1);
    error(func.args(" true#0", " [1]"), QueryError.APPLY_X_X);
    error(func.args(" put#2", " [<_/>,'']"), QueryError.FUNCUP_X);
  }

  /** Test method. */
  @Test public void bool() {
    final Function func = Function.BOOLEAN;
    final String name = Util.className(func.clazz);

    // pre-evaluated expressions
    check(func.args(1), true, "empty(//" + name + ")");
    check(func.args(" ()"), false, "empty(//" + name + ")");

    // function is replaced with fn:exists
    check(func.args(" <a/>/self::*"), true, "exists(//FnExists)");
    // function is replaced by its argument (argument yields no result)
    check("(false(), true())[" + func.args(" .") + "]", true, "empty(//" + name + ")");
    // no replacement
    check("(false(), 1)[" + func.args(" .") + "]", 1, "exists(//" + name + ")");

    // optimize ebv
    check("[][. instance of xs:int][" + func.args(" .") + "]", "", "empty(//FnExists)");
  }


  /** Test method. */
  @Test public void count() {
    final Function func = Function.COUNT;

    query(func.args(" (1 to 100000000)") + " ! string()", 100000000);
    query(func.args(" for $i in 1 to 100000000 return string('x')"), 100000000);
  }

  /** Test method. */
  @Test public void foldLeft() {
    final Function func = Function.FOLD_LEFT;

    // should be unrolled and evaluated at compile time
    check(func.args(" 2 to 10", 1, " function($a,$b) {$a+$b}"),
        55,
        "empty(//" + Util.className(FnFoldLeft.class) + "[contains(@name, 'fold-left')])",
        "exists(*/" + Util.className(Int.class) + ')');
    // should be unrolled but not evaluated at compile time
    check(func.args(" 2 to 10", 1, " function($a,$b) {0*random:integer($a)+$b}"),
        10,
        "empty(//" + Util.className(FnFoldLeft.class) + "[contains(@name, 'fold-left')])",
        "empty(*/" + Util.className(Int.class) + ')',
        "count(//" + Util.className(Arith.class) + "[@op = '+']) eq 9");
    // should not be unrolled
    check(func.args(" 1 to 10", 0, " function($a,$b) {$a+$b}"),
        55,
        "exists(//" + Util.className(FnFoldLeft.class) + "[contains(@name, 'fold-left')])");
  }

  /** Test method. */
  @Test public void foldRight() {
    final Function func = Function.FOLD_RIGHT;

    // should be unrolled and evaluated at compile time
    check(func.args(" 1 to 9", 10, " function($a,$b) {$a+$b}"),
        55,
        "empty(//" + Util.className(FnFoldRight.class) + ')',
        "exists(*/" + Util.className(Int.class) + ')');
    // should be unrolled but not evaluated at compile time
    check(func.args(" 1 to 9", 10, " function($a,$b) {0*random:integer($a)+$b}"),
        10,
        "empty(//" + Util.className(FnFoldRight.class) + ')',
        "empty(*/" + Util.className(Int.class) + ')',
        "count(//" + Util.className(Arith.class) + "[@op = '+']) eq 9");
    // should not be unrolled
    check(func.args(" 0 to 9", 10, " function($a,$b) {$a+$b}"),
        55,
        "exists(//" + Util.className(FnFoldRight.class) + "[contains(@name, 'fold-right')])");
  }

  /** Test method. */
  @Test public void forEach() {
    final Function func = Function.FOR_EACH;

    // should be unrolled and evaluated at compile time
    check(func.args(" 0 to 8", " function($x) {$x+1}"),
        "1\n2\n3\n4\n5\n6\n7\n8\n9",
        "empty(//" + Util.className(FnForEach.class) + ')',
        "exists(*/" + Util.className(IntSeq.class) + ')');
    // should be unrolled but not evaluated at compile time
    check(func.args(" 1 to 9", " function($x) {0*random:integer()+$x}"),
        "1\n2\n3\n4\n5\n6\n7\n8\n9",
        "empty(//" + Util.className(FnForEach.class) + ')',
        "empty(*/" + Util.className(IntSeq.class) + ')',
        "count(//" + Util.className(Arith.class) + "[@op = '+']) eq 9");
    // should not be unrolled
    check(func.args(" 0 to 9", " function($x) {$x+1}"),
        "1\n2\n3\n4\n5\n6\n7\n8\n9\n10",
        "exists(//" + Util.className(FnForEach.class) + "[contains(@name, 'for-each')])");
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
  @Test public void namespaceUriForPrefix() {
    final Function func = Function.NAMESPACE_URI_FOR_PREFIX;
    query("sort(<e xmlns:p='u'>{" + func.args("p", " <e/>") + "}</e>/text()/tokenize(.))", "u");
  }

  /** Test method. */
  @Test public void not() {
    final Function func = Function.NOT;
    final String name = Util.className(func.clazz);

    // pre-evaluated expressions
    check(func.args(1), false, "empty(//" + name + ")");
    check(func.args(" ()"), true,  "empty(//" + name + ")");

    // function is replaced with fn:exists
    check(func.args(" empty(1[.=1])"), true, "exists(//FnExists)");
    // function is replaced with fn:empty
    check(func.args(" exists(1[.=1])"), false, "exists(//FnEmpty)");
    check(func.args(" <a/>/self::*"), false, "exists(//FnEmpty)");
    // function is replaced with fn:boolean
    check(func.args(" not(1[.=1])"), true, "exists(//FnBoolean)");

    // function is replaced with fn:boolean
    check("for $i in (1,2) return " + func.args(" $i = $i + 1"),
        "true\ntrue", "exists(//*[@op = '!='])");
    check("for $i in (1,2) return " + func.args(" $i eq $i + 1"),
        "true\ntrue", "exists(//*[@op = 'ne'])");
    check("for $i in (1,2) return " + func.args(" $i = ($i, $i)"),
        "false\nfalse", "exists(//" + name + ")");
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
    query(func.args(" (1, 4, 6, 5, 3)"), "1\n3\n4\n5\n6");
    query(func.args(" (1,-2,5,10,-10,10,8)", " ()", " abs#1"), "1\n-2\n5\n8\n10\n-10\n10");
    query(func.args(" ((1,0), (1,1), (0,1), (0,0))"), "0\n0\n0\n0\n1\n1\n1\n1");
    query(func.args(" ('9','8','29','310','75','85','36-37','93','72','185','188','86','87','83',"
        + "'79','82','71','67','63','58','57','53','31','26','22','21','20','15','10')", " ()",
        " function($s) { number($s) }") + "[1]",
        "36-37");
    query(func.args(" (1,2)", " ()", " function($s) { [$s] }"), "1\n2");
  }

  /** Test method. */
  @Test public void staticBaseUri() {
    final Function func = Function.STATIC_BASE_URI;
    query("declare base-uri 'a/'; ends-with(" + func.args() + ", '/')", true);
    query("declare base-uri '.' ; ends-with(" + func.args() + ", '/')", true);
    query("declare base-uri '..'; ends-with(" + func.args() + ", '/')", true);
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
    query(func.args(" 2 to 10"), 54);
    query(func.args(" 9 to 10"), 19);
    query(func.args(" -3037000500 to 3037000500"), 0);
    query(func.args(" ()", " ()"), "");
    query(func.args(1, "x"), 1);
    error(func.args(" ()", " (1,2)"), SEQFOUND_X);
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
        + "<string key=''>í</string></map>", " map{'indent':'no'}"), "{\"\":\"\u00ed\"}");
    query(func.args(" <fn:string key='root'>X</fn:string>"), "\"X\"");
  }
}
