package org.basex.query;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.junit.jupiter.api.*;

/**
 * Tests if the string representation of optimized queries can be parsed and evaluated again.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class QueryStringTest extends SandboxTest {
  /** Non-foldable zero. */
  private static final String ZERO = "random:integer(1)";

  /** Direct element constructors. */
  @Test public void elementConstructor() {
    roundtrip("<a><b/>x<c/></a>");
    roundtrip("<a>x{ " + ZERO + " }y</a>");
    roundtrip("<a>{{x}}</a>");
    roundtrip("<a b='{{'/>");
    roundtrip("<a b='}}'>{ " + ZERO + " }</a>");
    roundtrip("<a>&amp;&lt;</a>");
    roundtrip("<a><![CDATA[<x>]]></a>");
    roundtrip("<a>{ '{' }</a>");
    roundtrip("<a>&#x20;<b/></a>");
    roundtrip("<a>{ ' ' }</a>");
    roundtrip("<a>&#xA;<b/>{ " + ZERO + " }</a>");
    roundtrip("<a b='x{ " + ZERO + " }y'/>");
    roundtrip("<a b='&lt;'>{ " + ZERO + " }</a>");
    roundtrip("<a b='&#x9;&quot;'>{ " + ZERO + " }</a>");
  }

  /** Comments and processing instructions: contents are not escaped. */
  @Test public void commentsPIs() {
    roundtrip("<!--a&amp;b-->");
    roundtrip("<?pi a&amp;b?>");
    roundtrip("comment { 'a&amp;b' }");
    roundtrip("processing-instruction pi { 'a&amp;b' }");
    roundtrip("comment { 'a&#xA;b' }");
    roundtrip("<a>{ " + ZERO + " }<!--a&amp;b--><?pi a&amp;b?></a>");
    roundtrip("parse-xml('<x><!--a&amp;b--><?pi a&amp;b?></x>')/x/node()");
  }

  /** Atomic items. */
  @Test public void atomics() {
    roundtrip("xs:date('2020-01-01')");
    roundtrip("xs:dateTime('2020-01-01T00:00:00Z')");
    roundtrip("xs:duration('P1Y')");
    roundtrip("xs:dayTimeDuration('PT1S')");
    roundtrip("xs:hexBinary('AB')");
    roundtrip("xs:base64Binary('AAAA')");
    roundtrip("{ xs:date('2020-01-01'): 1 }");
    roundtrip("[ xs:time('10:00:00') ]");
    roundtrip("xs:double('INF')");
    roundtrip("xs:double('-INF')");
    roundtrip("xs:double('NaN')");
    roundtrip("xs:float('-INF')");
    roundtrip("xs:float('NaN')");
    roundtrip("xs:double(1) div " + ZERO);
    roundtrip("xs:double(-0e0)");
    for(final String item : new String[] { "1", "-1", "1.5", "xs:decimal(1)", "xs:double(1)",
        "1e100", "xs:float(1)", "xs:byte(1)", "xs:untypedAtomic('a')", "xs:token('a')",
        "xs:anyURI('a')", "xs:NCName('a')", "'a' cast as enum('a', 'b')" }) {
      roundtrip("(" + item + ", " + ZERO + ")");
    }
  }

  /** Error variables. */
  @Test public void errorVariables() {
    roundtrip("try { 1 div " + ZERO + " } catch * { $err:description }");
    roundtrip("try { 1 div " + ZERO + " } catch * { $err:code, $err:value, $err:module }");
  }

  /** Expressions that must be parenthesized as operands. */
  @Test public void parentheses() {
    final String cond = "(if(" + ZERO + ") then 1 else 2)";
    roundtrip(cond + " + 1");
    roundtrip("-" + cond);
    roundtrip(cond + " ! string()");
    roundtrip("(switch(" + ZERO + ") case 1 return 1 default return 2) * 2");
    roundtrip("(switch(" + ZERO + ") case 1 return 1 case 2 return 3 default return 2) * 2");
    roundtrip("(typeswitch(" + ZERO + ") case xs:string return 1 default return 2) + 1");
    roundtrip("(typeswitch(" + ZERO + ") case xs:string return 1 case xs:integer return 3 "
        + "default return 2) + 1");
    roundtrip("(1 to 5)[position() = (" + ZERO + " + 1, 3)]");
    roundtrip("(" + ZERO + " instance of xs:integer) ! string()");
    roundtrip("(-" + ZERO + ") ! string()");
    roundtrip("(-1) ! (. + " + ZERO + ") ! string()");
    roundtrip("(-1.5)[" + ZERO + " = 0] ! string()");
    roundtrip("(" + ZERO + " >= 1 and " + ZERO + " <= 3) ! string()");
    roundtrip("(" + ZERO + " * 1.5e0 >= 1 and " + ZERO + " * 1.5e0 <= 3) ! string()");
    roundtrip("(copy $c := <a>{ " + ZERO + " }</a> modify delete node $c/text() return $c) ! "
        + "name()");
    roundtrip("(<a>{ " + ZERO + " }</a> transform with { delete node text() }) ! name()");
  }

  /** Expressions that need no parentheses in their context. */
  @Test public void noParentheses() {
    roundtrip("count(for $i in 1 to " + ZERO + " + 2 return $i * 2)");
    roundtrip("let $a := for $i in 1 to 3 return $i * " + ZERO + " let $b := $a return $b");
    roundtrip("for $i in 1 to 3 where $i > " + ZERO + " and $i < 3 return $i || 'x'");
    roundtrip("(if(" + ZERO + ") then 1 else 2, 3)");
    roundtrip("(1 to 3)[. = " + ZERO + " + 1]");
    roundtrip("<a>{ " + ZERO + " + 1 }</a>");
    roundtrip("switch(" + ZERO + ") case 1 return 2 + 3 default return 4 * 5");
    roundtrip("(" + ZERO + " cast as xs:string, " + ZERO + " castable as xs:double)");
    roundtrip("string(" + ZERO + " otherwise 1) || (" + ZERO + " to 2)");
    roundtrip("for $i in 1 to 4 order by $i mod 2 + " + ZERO + " descending return $i");
    roundtrip("for $i in 1 to 4 group by $g := $i mod 2 + " + ZERO + " return $g");
    roundtrip("for tumbling window $w in 1 to 4 + " + ZERO + " start when true() "
        + "end $e when $e mod 2 = " + ZERO + " return count($w)");

    final String query = "(" + ZERO + " + 1) * " + ZERO + " = " + ZERO + " and " + ZERO + " = 4";
    assertEquals("count(((((" + ZERO + " + 1) * " + ZERO + ") = " + ZERO + ") and (" + ZERO
        + " = 4), 1))", roundtrip("count((" + query + ", 1))").trim());
  }

  /** Full-text expressions. */
  @Test public void fullText() {
    final String input = "'a b c' contains text 'a' ftand 'c' ";
    roundtrip(input + "distance exactly 1 words");
    roundtrip(input + "distance at least 1 words");
    roundtrip(input + "distance at most 1 words");
    roundtrip(input + "distance from 1 to 3 words");
    roundtrip(input + "window 3 words");
    roundtrip("'a b c' contains text 'a' occurs exactly 1 times");
    roundtrip("'a b c' contains text 'a' occurs at least 1 times");
    roundtrip("'a b c' contains text 'a' at start");
    roundtrip("'a b c' contains text 'a' at end");
    roundtrip("'a b c' contains text 'a b c' entire content");
    roundtrip("'a b c' contains text 'a' using stop words ('x')");
    roundtrip("'a b c' contains text 'a' using thesaurus default");
    roundtrip("'a b c' contains text 'a' using no thesaurus");
    final String thes = "at 'src/test/resources/thesaurus.xml'";
    roundtrip("'lucky' contains text 'happy' using thesaurus " + thes);
    roundtrip("'lucky' contains text 'happy' using thesaurus " + thes + " relationship 'RT'");
    roundtrip("'lucky' contains text 'happy' using thesaurus " + thes + " from 0 to 2 levels");
    roundtrip("'lucky' contains text 'happy' using thesaurus (" + thes + ", " + thes
        + " relationship 'XY')");
  }

  /**
   * Checks if the optimized query yields the same result as the original query.
   * @param query query
   * @return optimized query
   */
  private static String roundtrip(final String query) {
    final String optimized;
    try(QueryProcessor qp = new QueryProcessor(query, context)) {
      qp.optimize();
      optimized = qp.qc.toString();
    } catch(final QueryException ex) {
      return fail("Optimization failed: " + query, ex);
    }
    final String info = "\nQuery: " + query + "\nOptimized: " + optimized;
    try(QueryProcessor qp1 = new QueryProcessor(query, context);
        QueryProcessor qp2 = new QueryProcessor(optimized, context)) {
      final Value expected = qp1.value(), returned = qp2.value();
      final DeepEqualOptions options = new DeepEqualOptions();
      options.set(DeepEqualOptions.TYPE_ANNOTATIONS, true);
      assertTrue(new DeepEqual(null, null, null, options).equal(expected, returned),
          info + "\nExpected: " + expected + "\nReturned: " + returned);
    } catch(final QueryException ex) {
      fail(info, ex);
    }
    return optimized;
  }
}
