package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;
import static org.junit.Assert.*;

import org.basex.query.*;
import org.junit.*;

/**
 * This class tests standard XQuery functions.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 * @author Leo Woerteler
 */
public final class FnTest extends AdvancedQueryTest {
  /** Text file. */
  private static final String TEXT = "src/test/resources/input.xml";

  /** Test method. */
  @Test
  public void unparsedText() {
    contains(UNPARSED_TEXT.args(TEXT), "<html");
    contains(UNPARSED_TEXT.args(TEXT, "US-ASCII"), "<html");
    error(UNPARSED_TEXT.args(TEXT, "xyz"), ENCODING_X);
  }

  /** Test method. */
  @Test
  public void parseXML() {
    contains(PARSE_XML.args("\"<x>a</x>\"") + "//text()", "a");
  }

  /** Test method. */
  @Test
  public void serialize() {
    contains(SERIALIZE.args("<x/>"), "<x/>");
    contains(SERIALIZE.args("<x/>", serialParams("")), "<x/>");
    contains(SERIALIZE.args("<x>a</x>", serialParams("<method value='text'/>")), "a");
  }

  /** Tests for the {@code replace} function. */
  @Test
  public void replace() {
    // tests for issue GH-573:
    query("replace('aaaa bbbbbbbb ddd ','(.{6,15}) ','$1@')", "aaaa bbbbbbbb@ddd ");
    query("replace(' aaa AAA 123','(\\s+\\P{Ll}{3,280}?)','$1@')", " aaa AAA@ 123@");
    error("replace('asdf','a{12,3}','')", REGPAT_X);
  }

  /** Tests for the {@code sum} function. */
  @Test
  public void sum() {
    query("sum(1)", "1");
    query("sum(1 to 10)", "55");
    query("sum(1 to 3037000499)", "4611686016981624750");
    query("sum(1 to 3037000500)", "4611686020018625250");
    query("sum(1 to 4294967295)", "9223372034707292160");
    query("sum(2 to 10)", "54");
    query("sum(9 to 10)", "19");
    query("sum(-3037000500 to 3037000500)", "0");
    query("sum((), ())", "");
    error("sum(1, 'x')", SUM_X_X);
    error("sum((), (1,2))", SEQFOUND_X);
  }

  /** Tests for the {@code parse-ietf-date} function. */
  @Test
  public void parseIetfDate() {
    query("parse-ietf-date('Wed, 06 Jun 1994 07:29:35 GMT')", "1994-06-06T07:29:35Z");
    query("parse-ietf-date('Wed, 6 Jun 94 07:29:35 GMT')", "1994-06-06T07:29:35Z");
    query("parse-ietf-date('Wed Jun 06 11:54:45 EST 0090')", "0090-06-06T11:54:45-05:00");
    query("parse-ietf-date('Sunday, 06-Nov-94 08:49:37 GMT')", "1994-11-06T08:49:37Z");
    query("parse-ietf-date('Wed, 6 Jun 94 07:29:35 +0500')", "1994-06-06T07:29:35+05:00");
    query("parse-ietf-date(' 1 Nov 1234 05:06:07.89 gmt')", "1234-11-01T05:06:07.89Z");

    query("parse-ietf-date(' 01-feb-3456 07:08:09 GMT')", "3456-02-01T07:08:09Z");
    query("parse-ietf-date(' 01-FEB-3456 07:08:09 GMT')", "3456-02-01T07:08:09Z");
    query("parse-ietf-date('Wed, 06 Jun 94 07:29:35 +0000 (GMT)')", "1994-06-06T07:29:35Z");
    query("parse-ietf-date('Wed, 06 Jun 94 07:29:35')", "1994-06-06T07:29:35Z");

    String s = "Wed, Jan-01 07:29:35 GMT 19";
    query("parse-ietf-date('" + s + "')", "1919-01-01T07:29:35Z");
    for(int i = s.length(); --i >= 0;) {
      error("parse-ietf-date('" + s.substring(0, i) + "')", IETF_PARSE_X_X_X);
    }

    s = "Wed, 06 Jun 1994 07:29";
    query("parse-ietf-date('" + s + "')", "1994-06-06T07:29:00Z");
    for(int i = s.length(); --i >= 0;) {
      error("parse-ietf-date('" + s.substring(0, i) + "')", IETF_PARSE_X_X_X);
    }
    error("parse-ietf-date('" + s + "X')", IETF_PARSE_X_X_X);

    error("parse-ietf-date('Wed, 99 Jun 94 07:29:35 +0000 (')", IETF_PARSE_X_X_X);
    error("parse-ietf-date('Wed, 99 Jun 94 07:29:35 +0000 (GT)')", IETF_PARSE_X_X_X);
    error("parse-ietf-date('Wed, 99 Jun 94 07:29:35 +0000 (GMT')", IETF_PARSE_X_X_X);

    error("parse-ietf-date('Wed, 99 Jun 94 07:29:35. GMT')", IETF_PARSE_X_X_X);
    error("parse-ietf-date('Wed, 99 Jun 94 07:29:35 0500')", IETF_PARSE_X_X_X);
    error("parse-ietf-date('Wed, 99 Jun 94 07:29:35 +5')", IETF_PARSE_X_X_X);
    error("parse-ietf-date('Wed, 99 Jun 94 07:29:35 -050')", IETF_PARSE_X_X_X);
    error("parse-ietf-date('Wed, 99 Jun 94 07:29:35 +0500')", IETF_INV_X);
  }

  /** Tests for the {@code sort} function. */
  @Test
  public void sort() {
    query(SORT.args("(1, 4, 6, 5, 3)"), "1\n3\n4\n5\n6");
    query(SORT.args("(1,-2,5,10,-10,10,8)", " abs#1"), "1\n-2\n5\n8\n10\n-10\n10");
    query(SORT.args("((1,0), (1,1), (0,1), (0,0))"), "0\n0\n0\n0\n1\n1\n1\n1");
  }

  /** Tests for the {@code outermost} and {@code innermost} functions. */
  @Test
  public void most() {
    query("let $n := <li/> return " + OUTERMOST.args("($n, $n)"), "<li/>");
    query("let $n := <li/> return " + INNERMOST.args("($n, $n)"), "<li/>");
  }

  /** Tests for the {@code parse-json} function. */
  @Test
  public void parseJson() {
    query(PARSE_JSON.args("\"\"\"x\\u0000\"\"\""), "x\uFFFD");
  }

  /** Tests for the {@code json-doc} function. */
  @Test
  public void jsonDoc() {
    query(JSON_DOC.args("src/test/resources/example.json") + "('address')('state')", "NY");
    query(JSON_DOC.args("src/test/resources/example.json") + "?address?state", "NY");
  }

  /** Test for namespace functions and in-scope namespaces. */
  @Test
  public void ns() {
    query("sort(<e xmlns:p='u'>{"
        + "  in-scope-prefixes(<e/>),"
        + "  namespace-uri-for-prefix('p', <e/>), "
        + "  resolve-QName('p:p', <p/>)"
        + "}</e>/text()/tokenize(.))",
        "p\np:p\nu\nxml");
  }

  /** Tests for the {@code random-number-generator} function. */
  @Test
  public void randomNumberGenerator() {
    // ensure that the same seed will generate the same result
    final String query = "random-number-generator(123)?number";
    assertEquals(query(query), query(query));
    // ensure that multiple number generators in a query will generate the same result
    query("let $seq := 1 to 10 "
        + "let $m1 := random-number-generator() "
        + "let $m2 := random-number-generator() "
        + "return every $test in ("
        + "  $m1('number') = $m2('number'),"
        + "  $m2('next')()('number') = $m1('next')()('number'),"
        + "  deep-equal($m1('permute')($seq), $m2('permute')($seq))"
        + ") satisfies true()", "true");
  }

  /** Tests for the {@code apply} function. */
  @Test
  public void apply() {
    query(APPLY.args(" true#0", " []"), "true");
    query(APPLY.args(" count#1", " [(1,2,3)]"));
    query(APPLY.args(" string-join#1", " [ reverse(1 to 5) ! string() ]"), "54321");
    query("let $func := function($a,$b,$c) { $a + $b + $c } "
        + "let $args := [ 1, 2, 3 ] "
        + "return fn:apply($func, $args)", "6");
    query("for $a in 2 to 3 "
        + "let $f := function-lookup(xs:QName('fn:concat'), $a) "
        + "return " + APPLY.args("$f", " array { 1 to $a }"), "12\n123");
    error(APPLY.args(" false#0", " ['x']"), APPLY_X_X);
    error(APPLY.args(" string-length#1", " [ ('a','b') ]"), INVTREAT_X_X_X);
  }
}
