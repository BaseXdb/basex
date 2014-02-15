package org.basex.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the intersect() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpIntersect extends QT3TestSet {

  /**
   * Only nodes are allowed..
   */
  @org.junit.Test
  public void k2SeqIntersect1() {
    final XQuery query = new XQuery(
      "(1, 2, 3) intersect (1, 2, 3)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * Use a node kind keyword, document, to test query parsing..
   */
  @org.junit.Test
  public void k2SeqIntersect10() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author intersect document))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Use a node kind keyword, if, to test query parsing..
   */
  @org.junit.Test
  public void k2SeqIntersect11() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author intersect if))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Use a node kind keyword, then, to test query parsing..
   */
  @org.junit.Test
  public void k2SeqIntersect12() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author intersect then))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Use a node kind keyword, mod, to test query parsing..
   */
  @org.junit.Test
  public void k2SeqIntersect13() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author intersect mod))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Use a node kind keyword, div, to test query parsing..
   */
  @org.junit.Test
  public void k2SeqIntersect14() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author intersect div))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Use a node kind keyword, empty-sequence, to test query parsing..
   */
  @org.junit.Test
  public void k2SeqIntersect15() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author intersect empty-sequence))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Use a node kind keyword, schema-attribute, to test query parsing..
   */
  @org.junit.Test
  public void k2SeqIntersect16() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author intersect schema-attribute))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Use a node kind keyword, schema-element, to test query parsing..
   */
  @org.junit.Test
  public void k2SeqIntersect17() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author intersect schema-element))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Use a computed attribute constructor as right operand..
   */
  @org.junit.Test
  public void k2SeqIntersect18() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a intersect attribute {\"name\"} {()}))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Use a computed attribute constructor as right operand(#2)..
   */
  @org.junit.Test
  public void k2SeqIntersect19() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a intersect attribute name {()}))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Use a node kind keyword, text, to test query parsing..
   */
  @org.junit.Test
  public void k2SeqIntersect2() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author intersect text))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Use a computed attribute constructor as right operand..
   */
  @org.junit.Test
  public void k2SeqIntersect20() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a intersect element {\"name\"} {()}))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertBoolean(true)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   * Use a computed attribute constructor as right operand(#2)..
   */
  @org.junit.Test
  public void k2SeqIntersect21() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a intersect element name {()}))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertBoolean(true)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   * Use a computed attribute constructor as right operand..
   */
  @org.junit.Test
  public void k2SeqIntersect22() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a intersect processing-instruction {\"name\"} {()}))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Use a computed attribute constructor as right operand..
   */
  @org.junit.Test
  public void k2SeqIntersect23() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a intersect processing-instruction name {}))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Use a computed attribute constructor as right operand..
   */
  @org.junit.Test
  public void k2SeqIntersect24() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a intersect comment {()}))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Use a computed attribute constructor as right operand..
   */
  @org.junit.Test
  public void k2SeqIntersect25() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a intersect text {()}))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Use a name test that is the descendant axis as right operand..
   */
  @org.junit.Test
  public void k2SeqIntersect26() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a intersect descendant))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Use a name test that is the attribute axis as right operand..
   */
  @org.junit.Test
  public void k2SeqIntersect27() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a intersect attribute))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Use a name test that is the self axis as right operand..
   */
  @org.junit.Test
  public void k2SeqIntersect28() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a intersect self))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Use a name test that is the descendant-or-self axis as right operand..
   */
  @org.junit.Test
  public void k2SeqIntersect29() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a intersect descendant-or-self))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Use a node kind keyword, node, to test query parsing..
   */
  @org.junit.Test
  public void k2SeqIntersect3() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author intersect node))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Use a name test that is the following-sibling axis as right operand..
   */
  @org.junit.Test
  public void k2SeqIntersect30() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a intersect following-sibling))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Use a name test that is the following axis as right operand..
   */
  @org.junit.Test
  public void k2SeqIntersect31() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a intersect following))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Use a name test that is the preceding-sibling axis as right operand..
   */
  @org.junit.Test
  public void k2SeqIntersect32() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a intersect preceding-sibling))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Use a name test that is the preceding axis as right operand..
   */
  @org.junit.Test
  public void k2SeqIntersect33() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a intersect preceding))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Use a name test that is the parent axis as right operand..
   */
  @org.junit.Test
  public void k2SeqIntersect34() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a intersect parent))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Use a name test that is the ancestor axis as right operand..
   */
  @org.junit.Test
  public void k2SeqIntersect35() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a intersect ancestor))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Use a name test that is the ancestor axis as right operand..
   */
  @org.junit.Test
  public void k2SeqIntersect36() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a intersect ancestor))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Use a name test that is the ancestor-or-self axis as right operand..
   */
  @org.junit.Test
  public void k2SeqIntersect37() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a intersect ancestor-or-self))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Use 'declare as right operand..
   */
  @org.junit.Test
  public void k2SeqIntersect38() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a intersect declare))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Extract the boolean value from a intersect expression..
   */
  @org.junit.Test
  public void k2SeqIntersect39() {
    final XQuery query = new XQuery(
      "boolean(//employee[location = \"Denver\"] intersect //employee[last()])",
      ctx);
    try {
      query.context(node(file("op/union/acme_corp.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * Use a node kind keyword, element, to test query parsing..
   */
  @org.junit.Test
  public void k2SeqIntersect4() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author intersect element))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Explicitly sort the result of an intersect expression..
   */
  @org.junit.Test
  public void k2SeqIntersect40() {
    final XQuery query = new XQuery(
      "//(employee[location = \"Denver\"] intersect //employee[last()])/./location",
      ctx);
    try {
      query.context(node(file("op/union/acme_corp.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   * Ensure node deduplication is done..
   */
  @org.junit.Test
  public void k2SeqIntersect41() {
    final XQuery query = new XQuery(
      "<r> { let $i := <e> <a/> <b/> <c/> </e>/a , $t := $i/following-sibling::b return (($i intersect ($i, $i)), (($t, $t) intersect $t)) } </r>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<r><a/><b/></r>", false)
    );
  }

  /**
   * Perform intersection between two singleton document nodes..
   */
  @org.junit.Test
  public void k2SeqIntersect42() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare function local:function ($c as node()) { $c intersect $c }; \n" +
      "         empty(local:function(document{()}))\n" +
      "      ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * Ensure two sequential union operators can be parsed(intersect)..
   */
  @org.junit.Test
  public void k2SeqIntersect43() {
    final XQuery query = new XQuery(
      "1 intersect 2 intersect 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * Ensure two sequential union operators can be parsed(except)..
   */
  @org.junit.Test
  public void k2SeqIntersect44() {
    final XQuery query = new XQuery(
      "1 except 2 except 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * Use a node kind keyword, attribute, to test query parsing..
   */
  @org.junit.Test
  public void k2SeqIntersect5() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author intersect attribute))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Use a node kind keyword, document-node, to test query parsing..
   */
  @org.junit.Test
  public void k2SeqIntersect6() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author intersect document-node))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Use a node kind keyword, comment, to test query parsing..
   */
  @org.junit.Test
  public void k2SeqIntersect7() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author intersect comment))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Use a node kind keyword, processing-instruction, to test query parsing..
   */
  @org.junit.Test
  public void k2SeqIntersect8() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author intersect processing-instruction))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Use a node kind keyword, item, to test query parsing..
   */
  @org.junit.Test
  public void k2SeqIntersect9() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author intersect item))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple combination of node sequences involving different node sequences that results in empty sequence . Use "intersect" operator. Uses the count function to avoid the empty file for results. .
   */
  @org.junit.Test
  public void combiningnodeseqintersecthc1() {
    final XQuery query = new XQuery(
      "count((<a>0</a>,<a>1</a>) intersect (<a>3</a>,<a>4</a>))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Simple combination of node sequences involving two empty sequences. Use "intersect" operator. .
   */
  @org.junit.Test
  public void combiningnodeseqintersecthc2() {
    final XQuery query = new XQuery(
      "count(() intersect ())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("0")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Simple combination of sequences, where the two sequences are the same. Use "intersect" operator. .
   */
  @org.junit.Test
  public void combiningnodeseqintersecthc3() {
    final XQuery query = new XQuery(
      "for $h in ( (//hours) intersect (//hours[xs:integer(.) gt 12])) order by number($h) return $h",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>40</hours><hours>40</hours><hours>40</hours><hours>80</hours><hours>80</hours><hours>80</hours>", false)
    );
  }

  /**
   *  Simple combination of sequences, where second sequence intersect first sequence (multiple data sources). Use "intersect" operator. .
   */
  @org.junit.Test
  public void combiningnodeseqintersecthc4() {
    final XQuery query = new XQuery(
      "for $h in ( ($works//hours) intersect ($works//hours, $staff//grade)) order by number($h) return $h",
      ctx);
    try {
      query.bind("$works", node(file("docs/works.xml")));
      query.bind("$staff", node(file("docs/staff.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<hours>12</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>40</hours><hours>40</hours><hours>40</hours><hours>80</hours><hours>80</hours><hours>80</hours>", false)
    );
  }

  /**
   * arg: node.
   */
  @org.junit.Test
  public void fnIntersectNodeArgs001() {
    final XQuery query = new XQuery(
      "(/bib/book[1]/title intersect /bib/book[1]/title)/string()",
      ctx);
    try {
      query.context(node(file("op/union/bib2.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "TCP/IP Illustrated")
    );
  }

  /**
   * arg: incorrect nodes.
   */
  @org.junit.Test
  public void fnIntersectNodeArgs002() {
    final XQuery query = new XQuery(
      "/bib/book/title intersect /bib/book[1]/title",
      ctx);
    try {
      query.context(node(file("op/union/bib2.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "TCP/IP Illustrated")
    );
  }

  /**
   * arg: node.
   */
  @org.junit.Test
  public void fnIntersectNodeArgs003() {
    final XQuery query = new XQuery(
      "/bib/book[3]/title intersect root(exactly-one(/bib/book[3]/title))",
      ctx);
    try {
      query.context(node(file("op/union/bib2.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   * arg: text node & node.
   */
  @org.junit.Test
  public void fnIntersectNodeArgs004() {
    final XQuery query = new XQuery(
      "/bib/book[3]/title/text() intersect /bib/book/title/text()",
      ctx);
    try {
      query.context(node(file("op/union/bib2.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Data on the Web")
    );
  }

  /**
   * arg: processing-instruction node & node.
   */
  @org.junit.Test
  public void fnIntersectNodeArgs005() {
    final XQuery query = new XQuery(
      "(/processing-instruction() intersect /processing-instruction())/name()",
      ctx);
    try {
      query.context(node(file("op/union/bib2.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "PI1 PI2")
    );
  }

  /**
   * arg: processing-instruction node & node.
   */
  @org.junit.Test
  public void fnIntersectNodeArgs006() {
    final XQuery query = new XQuery(
      "(/processing-instruction(PI1) intersect /processing-instruction())/name()",
      ctx);
    try {
      query.context(node(file("op/union/bib2.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "PI1")
    );
  }

  /**
   * arg: comment node & node.
   */
  @org.junit.Test
  public void fnIntersectNodeArgs007() {
    final XQuery query = new XQuery(
      "string-join((/comment() intersect /comment()), \"|\")",
      ctx);
    try {
      query.context(node(file("op/union/bib2.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, " this file is a copy of bib.xml; just adds a few comments and PI nodes for testing | Comment 1 | Comment 2 ")
    );
  }

  /**
   * arg: text node & node.
   */
  @org.junit.Test
  public void fnIntersectNodeArgs008() {
    final XQuery query = new XQuery(
      "/bib/book[3]/title/text() intersect /bib/book/title/text()",
      ctx);
    try {
      query.context(node(file("op/union/bib2.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Data on the Web")
    );
  }

  /**
   * arg: processing-instruction node & node.
   */
  @org.junit.Test
  public void fnIntersectNodeArgs009() {
    final XQuery query = new XQuery(
      "/processing-instruction() intersect /bib/book[2]/title",
      ctx);
    try {
      query.context(node(file("op/union/bib2.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   * arg: processing-instruction node & node.
   */
  @org.junit.Test
  public void fnIntersectNodeArgs010() {
    final XQuery query = new XQuery(
      "/processing-instruction(PI1) intersect /bib/book",
      ctx);
    try {
      query.context(node(file("op/union/bib2.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   * arg: comment node & node.
   */
  @org.junit.Test
  public void fnIntersectNodeArgs011() {
    final XQuery query = new XQuery(
      "(/bib/book intersect /bib/book)/string(@year)",
      ctx);
    try {
      query.context(node(file("op/union/bib2.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1994 1992 2000 1999")
    );
  }

  /**
   * arg: node & non existing node.
   */
  @org.junit.Test
  public void fnIntersectNodeArgs012() {
    final XQuery query = new XQuery(
      "//author intersect //nonexisting",
      ctx);
    try {
      query.context(node(file("op/union/bib2.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   * arg: node & empty sequence.
   */
  @org.junit.Test
  public void fnIntersectNodeArgs013() {
    final XQuery query = new XQuery(
      "//author intersect ()",
      ctx);
    try {
      query.context(node(file("op/union/bib2.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   * arg: empty sequence & empty sequence.
   */
  @org.junit.Test
  public void fnIntersectNodeArgs014() {
    final XQuery query = new XQuery(
      "() intersect ()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEmpty()
      ||
        error("XPST0005")
      )
    );
  }

  /**
   * arg: node & node.
   */
  @org.junit.Test
  public void fnIntersectNodeArgs023() {
    final XQuery query = new XQuery(
      "for $node in ((//price/text()) , (//price/text())) intersect ((//price/text()) , (//price/text())) return <a> {$node} </a>",
      ctx);
    try {
      query.context(node(file("op/union/bib2.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a> 65.95</a><a>65.95</a><a> 39.95</a><a>129.95</a>", false)
    );
  }
}
