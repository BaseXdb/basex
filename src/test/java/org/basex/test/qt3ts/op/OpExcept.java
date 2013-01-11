package org.basex.test.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the except() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpExcept extends QT3TestSet {

  /**
   *  Only nodes are allowed. .
   */
  @org.junit.Test
  public void k2SeqExcept1() {
    final XQuery query = new XQuery(
      "(1, 2, 3) except (1, 2, 3)",
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
   *  Use a node kind keyword, document, to test query parsing. .
   */
  @org.junit.Test
  public void k2SeqExcept10() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author except document))",
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
   *  Use a node kind keyword, if, to test query parsing. .
   */
  @org.junit.Test
  public void k2SeqExcept11() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author except if))",
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
   *  Use a node kind keyword, then, to test query parsing. .
   */
  @org.junit.Test
  public void k2SeqExcept12() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author except then))",
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
   *  Use a node kind keyword, mod, to test query parsing. .
   */
  @org.junit.Test
  public void k2SeqExcept13() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author except mod))",
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
   *  Use a node kind keyword, div, to test query parsing. .
   */
  @org.junit.Test
  public void k2SeqExcept14() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author except div))",
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
   *  Use a node kind keyword, empty-sequence, to test query parsing. .
   */
  @org.junit.Test
  public void k2SeqExcept15() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author except empty-sequence))",
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
   *  Use a node kind keyword, schema-attribute, to test query parsing. .
   */
  @org.junit.Test
  public void k2SeqExcept16() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author except schema-attribute))",
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
   *  Use a node kind keyword, schema-element, to test query parsing. .
   */
  @org.junit.Test
  public void k2SeqExcept17() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author except schema-element))",
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
   *  Use a computed attribute constructor as right operand. .
   */
  @org.junit.Test
  public void k2SeqExcept18() {
    final XQuery query = new XQuery(
      "count(<e/>/(a except attribute {\"name\"} {()}))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Use a computed attribute constructor as right operand(#2). .
   */
  @org.junit.Test
  public void k2SeqExcept19() {
    final XQuery query = new XQuery(
      "count(<e/>/(a except attribute name {()}))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Use a node kind keyword, text, to test query parsing. .
   */
  @org.junit.Test
  public void k2SeqExcept2() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author except text))",
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
   *  Use a computed attribute constructor as right operand. .
   */
  @org.junit.Test
  public void k2SeqExcept20() {
    final XQuery query = new XQuery(
      "count(<e/>/(a except element {\"name\"} {()}))",
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
        assertStringValue(false, "0")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Use a computed attribute constructor as right operand(#2). .
   */
  @org.junit.Test
  public void k2SeqExcept21() {
    final XQuery query = new XQuery(
      "count(<e/>/(a except element name {()}))",
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
        assertStringValue(false, "0")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Use a computed attribute constructor as right operand. .
   */
  @org.junit.Test
  public void k2SeqExcept22() {
    final XQuery query = new XQuery(
      "count(<e/>/(a except processing-instruction {\"name\"} {()}))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Use a computed attribute constructor as right operand. .
   */
  @org.junit.Test
  public void k2SeqExcept23() {
    final XQuery query = new XQuery(
      "count(<e/>/(a except processing-instruction name {}))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Use a computed attribute constructor as right operand. .
   */
  @org.junit.Test
  public void k2SeqExcept24() {
    final XQuery query = new XQuery(
      "count(<e/>/(a except comment {()}))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Use a computed attribute constructor as right operand. .
   */
  @org.junit.Test
  public void k2SeqExcept25() {
    final XQuery query = new XQuery(
      "count(<e/>/(a except text {()}))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Use a name test that is the descendant axis as right operand. .
   */
  @org.junit.Test
  public void k2SeqExcept26() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a except descendant))",
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
   *  Use a name test that is the attribute axis as right operand. .
   */
  @org.junit.Test
  public void k2SeqExcept27() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a except attribute))",
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
   *  Use a name test that is the self axis as right operand. .
   */
  @org.junit.Test
  public void k2SeqExcept28() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a except self))",
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
   *  Use a name test that is the descendant-or-self axis as right operand. .
   */
  @org.junit.Test
  public void k2SeqExcept29() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a except descendant-or-self))",
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
   *  Use a node kind keyword, node, to test query parsing. .
   */
  @org.junit.Test
  public void k2SeqExcept3() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author except node))",
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
   *  Use a name test that is the following-sibling axis as right operand. .
   */
  @org.junit.Test
  public void k2SeqExcept30() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a except following-sibling))",
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
   *  Use a name test that is the following axis as right operand. .
   */
  @org.junit.Test
  public void k2SeqExcept31() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a except following))",
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
   *  Use a name test that is the preceding-sibling axis as right operand. .
   */
  @org.junit.Test
  public void k2SeqExcept32() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a except preceding-sibling))",
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
   *  Use a name test that is the preceding axis as right operand. .
   */
  @org.junit.Test
  public void k2SeqExcept33() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a except preceding))",
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
   *  Use a name test that is the parent axis as right operand. .
   */
  @org.junit.Test
  public void k2SeqExcept34() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a except parent))",
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
   *  Use a name test that is the ancestor axis as right operand. .
   */
  @org.junit.Test
  public void k2SeqExcept35() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a except ancestor))",
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
   *  Use a name test that is the ancestor axis as right operand. .
   */
  @org.junit.Test
  public void k2SeqExcept36() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a except ancestor))",
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
   *  Use a name test that is the ancestor-or-self axis as right operand. .
   */
  @org.junit.Test
  public void k2SeqExcept37() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a except ancestor-or-self))",
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
   *  Use 'declare as right operand. .
   */
  @org.junit.Test
  public void k2SeqExcept38() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a except declare))",
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
   *  Extract the boolean value from an except expression. .
   */
  @org.junit.Test
  public void k2SeqExcept39() {
    final XQuery query = new XQuery(
      "boolean(//employee[location = \"Denver\"] except //employee[last()])",
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
      assertBoolean(true)
    );
  }

  /**
   *  Use a node kind keyword, element, to test query parsing. .
   */
  @org.junit.Test
  public void k2SeqExcept4() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author except element))",
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
   *  Explicitly sort the result of an except expression. .
   */
  @org.junit.Test
  public void k2SeqExcept40() {
    final XQuery query = new XQuery(
      "<r> { //(employee[location = \"Denver\"] except //employee[last()])/./location } </r>",
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
      assertSerialization("<r><location>Denver</location><location>Denver</location><location>Denver</location></r>", false)
    );
  }

  /**
   *  Compare two nodes from different trees. .
   */
  @org.junit.Test
  public void k2SeqExcept41() {
    final XQuery query = new XQuery(
      "<a/> except <b/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a/>", false)
    );
  }

  /**
   *  Ensure node deduplication is done. .
   */
  @org.junit.Test
  public void k2SeqExcept42() {
    final XQuery query = new XQuery(
      "<r> { let $i := <e> <a/> <b/> <c/> </e>/a , $t := $i/following-sibling::b return (($i except ($i, $i)), (($t, $t) except $t)) } </r>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<r/>", false)
    );
  }

  /**
   *  Use a node kind keyword, attribute, to test query parsing. .
   */
  @org.junit.Test
  public void k2SeqExcept5() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author except attribute))",
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
   *  Use a node kind keyword, document-node, to test query parsing. .
   */
  @org.junit.Test
  public void k2SeqExcept6() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author except document-node))",
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
   *  Use a node kind keyword, comment, to test query parsing. .
   */
  @org.junit.Test
  public void k2SeqExcept7() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author except comment))",
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
   *  Use a node kind keyword, processing-instruction, to test query parsing. .
   */
  @org.junit.Test
  public void k2SeqExcept8() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author except processing-instruction))",
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
   *  Use a node kind keyword, item, to test query parsing. .
   */
  @org.junit.Test
  public void k2SeqExcept9() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author except item))",
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
   *  Simple combination of node sequences involving node types with integer data. Use "except" operator. .
   */
  @org.junit.Test
  public void combiningnodeseqexcepthc1() {
    final XQuery query = new XQuery(
      "(<a>0</a>,<a>1</a>) except (<a>3</a>,<a>4</a>)",
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
        assertSerialization("<a>0</a><a>1</a>", false)
      ||
        assertSerialization("<a>1</a><a>0</a>", false)
      )
    );
  }

  /**
   *  Simple combination of node sequences involving repetition of the same element data. Use "except" operator. .
   */
  @org.junit.Test
  public void combiningnodeseqexcepthc2() {
    final XQuery query = new XQuery(
      "(<a>0</a>,<a>1</a>) except (<a>3</a>,<a>4</a>,<a>0</a>)",
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
        assertSerialization("<a>0</a><a>1</a>", false)
      ||
        assertSerialization("<a>1</a><a>0</a>", false)
      )
    );
  }

  /**
   *  Simple combination of node sequences involving the empty sequence. Use "except" operator. .
   */
  @org.junit.Test
  public void combiningnodeseqexcepthc3() {
    final XQuery query = new XQuery(
      "count(() except (<a>3</a>,<a>4</a>,<a>0</a>))",
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
   *  Simple combination of node sequences that evaluates to the empty sequence. Use "except" operator. Use count to avoid empty file. .
   */
  @org.junit.Test
  public void combiningnodeseqexcepthc4() {
    final XQuery query = new XQuery(
      "for $h in ( count((//hours) except (//hours))) order by number($h) return $h",
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
      assertEq("0")
    );
  }

  /**
   *  Simple combination of node sequences involving multiple xml data sources and repetition of a sequence. Use "except" operator. Use count to avoid empty file results .
   */
  @org.junit.Test
  public void combiningnodeseqexcepthc5() {
    final XQuery query = new XQuery(
      "for $h in ( count(($works//hours) except ($staff//grade,$works//hours))) order by number($h) return $h",
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
      assertEq("0")
    );
  }

  /**
   *  Simple combination of node sequences involving multiple xml data sources. Use "except" operator. .
   */
  @org.junit.Test
  public void combiningnodeseqexcepthc6() {
    final XQuery query = new XQuery(
      "for $h in ( ($works//hours) except ($staff//grade)) order by number($h) return $h",
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
   *  arg: node .
   */
  @org.junit.Test
  public void fnExceptNodeArgs001() {
    final XQuery query = new XQuery(
      "/bib/book[1]/title except /bib/book[1]/title",
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
      assertStringValue(false, "")
    );
  }

  /**
   *  arg: incorrect nodes .
   */
  @org.junit.Test
  public void fnExceptNodeArgs002() {
    final XQuery query = new XQuery(
      "/bib/book/title except /bib/book[1]/title",
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
      assertSerialization("<title>Advanced Programming in the Unix environment</title><title>Data on the Web</title><title>The Economics of Technology and Content for Digital TV</title>", false)
    );
  }

  /**
   *  arg: node .
   */
  @org.junit.Test
  public void fnExceptNodeArgs003() {
    final XQuery query = new XQuery(
      "(/bib/book[3] except root(exactly-one(/bib/book[3]/title)))/string(@year)",
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
      assertStringValue(false, "2000")
    );
  }

  /**
   *  arg: text node & node .
   */
  @org.junit.Test
  public void fnExceptNodeArgs004() {
    final XQuery query = new XQuery(
      "/bib/book[3]/title/text() except /bib/book/title/text()",
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
   *  arg: processing-instruction node & node .
   */
  @org.junit.Test
  public void fnExceptNodeArgs005() {
    final XQuery query = new XQuery(
      "/processing-instruction() except /processing-instruction()",
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
   *  arg: processing-instruction node & node .
   */
  @org.junit.Test
  public void fnExceptNodeArgs006() {
    final XQuery query = new XQuery(
      "(/processing-instruction() except /processing-instruction(PI1))/name()",
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
      assertStringValue(false, "PI2")
    );
  }

  /**
   *  arg: comment node & node .
   */
  @org.junit.Test
  public void fnExceptNodeArgs007() {
    final XQuery query = new XQuery(
      "/comment() except /comment()",
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
   *  arg: text node & node .
   */
  @org.junit.Test
  public void fnExceptNodeArgs008() {
    final XQuery query = new XQuery(
      "string-join((for $node in /bib/book/title/text() except /bib/book[3]/title/text() return $node/string()), \"|\")",
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
      assertStringValue(false, "TCP/IP Illustrated|Advanced Programming in the Unix environment|The Economics of Technology and Content for Digital TV")
    );
  }

  /**
   *  arg: processing-instruction node & node .
   */
  @org.junit.Test
  public void fnExceptNodeArgs009() {
    final XQuery query = new XQuery(
      "(/processing-instruction() except /bib/book[2]/title)/name()",
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
   *  arg: processing-instruction node & node .
   */
  @org.junit.Test
  public void fnExceptNodeArgs010() {
    final XQuery query = new XQuery(
      "(/processing-instruction(PI1) except /bib/book)/name()",
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
   *  arg: comment node & node .
   */
  @org.junit.Test
  public void fnExceptNodeArgs011() {
    final XQuery query = new XQuery(
      "/bib/book except /bib/book",
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
      (
        assertEmpty()
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  arg: node & non existing node .
   */
  @org.junit.Test
  public void fnExceptNodeArgs012() {
    final XQuery query = new XQuery(
      "//author except //nonexisting",
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
      assertSerialization("<author><last>Stevens</last><first>W.</first></author><author><last>Stevens</last><first>W.</first></author><author><last>Abiteboul</last><first>Serge</first></author><author><last>Buneman</last><first>Peter</first></author><author><last>Suciu</last><first>Dan</first></author>", false)
    );
  }

  /**
   *  arg: node & empty sequence .
   */
  @org.junit.Test
  public void fnExceptNodeArgs013() {
    final XQuery query = new XQuery(
      "//author except ()",
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
      assertSerialization("<author><last>Stevens</last><first>W.</first></author><author><last>Stevens</last><first>W.</first></author><author><last>Abiteboul</last><first>Serge</first></author><author><last>Buneman</last><first>Peter</first></author><author><last>Suciu</last><first>Dan</first></author>", false)
    );
  }

  /**
   *  arg: empty sequence & empty sequence .
   */
  @org.junit.Test
  public void fnExceptNodeArgs014() {
    final XQuery query = new XQuery(
      "() except ()",
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
      (
        assertEmpty()
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  arg: node & node .
   */
  @org.junit.Test
  public void fnExceptNodeArgs022() {
    final XQuery query = new XQuery(
      "string-join((for $node in ((//price/text()) , (//price/text())) except (//price) return $node)/string(), \"|\")",
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
      assertStringValue(false, " 65.95|65.95| 39.95|129.95")
    );
  }

  /**
   *  arg: node & node .
   */
  @org.junit.Test
  public void fnExceptNodeArgs023() {
    final XQuery query = new XQuery(
      "((//price/text()) , (//price/text())) except (//price/text())",
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
}
