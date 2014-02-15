package org.basex.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the union() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpUnion extends QT3TestSet {

  /**
   *  Ensure nodes are deduplicated and sorted even though one of the operands is the empty sequence. .
   */
  @org.junit.Test
  public void k2SeqUnion1() {
    final XQuery query = new XQuery(
      "<r> { let $i := <e> <a/> <b/> </e> return ($i/b, $i/a, $i/b, $i/a) | () } </r>",
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
   *  Use a node kind keyword, document-node, to test query parsing. .
   */
  @org.junit.Test
  public void k2SeqUnion10() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author union document-node))",
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
  public void k2SeqUnion11() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author union comment))",
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
  public void k2SeqUnion12() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author union processing-instruction))",
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
  public void k2SeqUnion13() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author union item))",
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
   *  Use a node kind keyword, document, to test query parsing. .
   */
  @org.junit.Test
  public void k2SeqUnion14() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author union document))",
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
  public void k2SeqUnion15() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author union if))",
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
  public void k2SeqUnion16() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author union then))",
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
  public void k2SeqUnion17() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author union mod))",
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
  public void k2SeqUnion18() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author union div))",
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
  public void k2SeqUnion19() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author union empty-sequence))",
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
   *  Ensure nodes are deduplicated and sorted even though one of the operands is the empty sequence(#2). .
   */
  @org.junit.Test
  public void k2SeqUnion2() {
    final XQuery query = new XQuery(
      "<r> { let $i := <e> <a/> <b/> </e> return () | ($i/b, $i/a, $i/b, $i/a) } </r>",
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
   *  Use a node kind keyword, schema-attribute, to test query parsing. .
   */
  @org.junit.Test
  public void k2SeqUnion20() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author union schema-attribute))",
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
  public void k2SeqUnion21() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author union schema-element))",
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
  public void k2SeqUnion22() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a union attribute {\"name\"} {()}))",
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
   *  Use a computed attribute constructor as right operand(#2). .
   */
  @org.junit.Test
  public void k2SeqUnion23() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a union attribute name {()}))",
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
   *  Use a computed attribute constructor as right operand. .
   */
  @org.junit.Test
  public void k2SeqUnion24() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a union element {\"name\"} {()}))",
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
        assertBoolean(false)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Use an element constructor as right operand(#2). .
   */
  @org.junit.Test
  public void k2SeqUnion25() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a union element name {()}))",
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
        assertBoolean(false)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Use a processing instruction constructor as right operand. .
   */
  @org.junit.Test
  public void k2SeqUnion26() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a union processing-instruction {\"name\"} {()}))",
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
   *  Use a processing instruction constructor as right operand. .
   */
  @org.junit.Test
  public void k2SeqUnion27() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a union processing-instruction name {}))",
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
   *  Use a comment constructor as right operand. .
   */
  @org.junit.Test
  public void k2SeqUnion28() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a union comment {()}))",
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
   *  Use a text node constructor as right operand. .
   */
  @org.junit.Test
  public void k2SeqUnion29() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a union text {()}))",
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
   *  Use 'comment' as right operand. .
   */
  @org.junit.Test
  public void k2SeqUnion3() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author union comment))",
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
   *  Use a name test that resembles the descendant axis as right operand. .
   */
  @org.junit.Test
  public void k2SeqUnion30() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a union descendant))",
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
   *  Use a name test that resembles the attribute axis as right operand. .
   */
  @org.junit.Test
  public void k2SeqUnion31() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a union attribute))",
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
   *  Use a name test that resembles the self axis as right operand. .
   */
  @org.junit.Test
  public void k2SeqUnion32() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a union self))",
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
   *  Use a name test that resembles the descendant-or-self axis as right operand. .
   */
  @org.junit.Test
  public void k2SeqUnion33() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a union descendant-or-self))",
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
   *  Use a name test that resembles the following-sibling axis as right operand. .
   */
  @org.junit.Test
  public void k2SeqUnion34() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a union following-sibling))",
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
   *  Use a name test that resembles the following axis as right operand. .
   */
  @org.junit.Test
  public void k2SeqUnion35() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a union following))",
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
   *  Use a name test that resembles the preceding-sibling axis as right operand. .
   */
  @org.junit.Test
  public void k2SeqUnion36() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a union preceding-sibling))",
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
   *  Use a name test that resembles the preceding axis as right operand. .
   */
  @org.junit.Test
  public void k2SeqUnion37() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a union preceding))",
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
   *  Use a name test that resembles the parent axis as right operand. .
   */
  @org.junit.Test
  public void k2SeqUnion38() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a union parent))",
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
   *  Use a name test that resembles the ancestor axis as right operand. .
   */
  @org.junit.Test
  public void k2SeqUnion39() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a union ancestor))",
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
   *  Ensure node deduplication is done on the operands. .
   */
  @org.junit.Test
  public void k2SeqUnion4() {
    final XQuery query = new XQuery(
      "let $i := <e> <a/> <b/> <c/> </e>/a , $t := $i/following-sibling::b return (($i union ($i, $i)), (($t, $t) union $t))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a/><b/>", false)
    );
  }

  /**
   *  Use a name test that resembles the ancestor axis as right operand. .
   */
  @org.junit.Test
  public void k2SeqUnion40() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a union ancestor))",
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
   *  Use a name test that resembles the ancestor-or-self axis as right operand. .
   */
  @org.junit.Test
  public void k2SeqUnion41() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a union ancestor-or-self))",
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
  public void k2SeqUnion42() {
    final XQuery query = new XQuery(
      "empty(<e/>/(a union declare))",
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
   *  Extract the boolean value from a union expression. .
   */
  @org.junit.Test
  public void k2SeqUnion43() {
    final XQuery query = new XQuery(
      "boolean(//employee[location = \"Denver\"] union //employee[last()])",
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
   *  Extract the boolean value from a union expression(different keyword). .
   */
  @org.junit.Test
  public void k2SeqUnion44() {
    final XQuery query = new XQuery(
      "boolean(//employee[location = \"Denver\"] | //employee[last()])",
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
   *  Explicitly sort the result of a union expression. .
   */
  @org.junit.Test
  public void k2SeqUnion45() {
    final XQuery query = new XQuery(
      " <r> { //(employee[location = \"Denver\"] union //employee[last()])/./location } </r>",
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
      assertSerialization("<r><location>Denver</location><location>Denver</location><location>Denver</location><location>Boston</location></r>", false)
    );
  }

  /**
   *  Ensure two sequential union operators can be parsed(|). .
   */
  @org.junit.Test
  public void k2SeqUnion46() {
    final XQuery query = new XQuery(
      "1|2|3",
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
   *  Ensure two sequential union operators can be parsed(union). .
   */
  @org.junit.Test
  public void k2SeqUnion47() {
    final XQuery query = new XQuery(
      "1 union 2 union 3",
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
   *  Only nodes are allowed. .
   */
  @org.junit.Test
  public void k2SeqUnion5() {
    final XQuery query = new XQuery(
      "(1, 2, 3) union (1, 2, 3)",
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
   *  Use a node kind keyword, text, to test query parsing. .
   */
  @org.junit.Test
  public void k2SeqUnion6() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author union text))",
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
  public void k2SeqUnion7() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author union node))",
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
   *  Use a node kind keyword, element, to test query parsing. .
   */
  @org.junit.Test
  public void k2SeqUnion8() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author union element))",
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
   *  Use a node kind keyword, attribute, to test query parsing. .
   */
  @org.junit.Test
  public void k2SeqUnion9() {
    final XQuery query = new XQuery(
      "empty(<e/>/(author union attribute))",
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
   *  Tests various optimizations on op:union .
   */
  @org.junit.Test
  public void cbclFnUnion001() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $x := <b/>;\n" +
      "        declare variable $y := <a/>;\n" +
      "        ($x union $y) union $x, ($x intersect $y) union $x, ($x except $y) union $x, ($y except $x) union $x, $x union ($x union $y), $x union ($x intersect $y), $x union ($x except $y), $x union ($y except $x)",
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
        assertSerialization("<b/><a/><b/><b/><b/><a/><b/><a/><b/><b/><b/><a/>", false)
      ||
        assertSerialization("<a/><b/><b/><b/><a/><b/><a/><b/><b/><b/><a/><b/>", false)
      )
    );
  }

  /**
   *  Tests optimization of union of path expressions .
   */
  @org.junit.Test
  public void cbclFnUnion002() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $doc := <root><test><x/><y/></test></root>;\n" +
      "        $doc/test[x] union $doc/test[y]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<test><x/><y/></test>", false)
    );
  }

  /**
   *  Simple combination of node sequences involving integers. uses "|" operator .
   */
  @org.junit.Test
  public void combiningnodeseqhc1() {
    final XQuery query = new XQuery(
      "for $h in ( (<hours>0</hours>,<hours>1</hours>) | //hours) order by number($h) return $h",
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
      assertSerialization("<hours>0</hours><hours>1</hours><hours>12</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>40</hours><hours>40</hours><hours>40</hours><hours>80</hours><hours>80</hours><hours>80</hours>", false)
    );
  }

  /**
   *  Simple combination of node sequences involving multiple xml data sources. Uses "union" operator .
   */
  @org.junit.Test
  public void combiningnodeseqhc10() {
    final XQuery query = new XQuery(
      "for $h in ( ($works//hours) union ($staff//grade[xs:integer(.) gt 12])) order by number($h) return $h",
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
      assertSerialization("<hours>12</hours><grade>13</grade><grade>13</grade><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>40</hours><hours>40</hours><hours>40</hours><hours>80</hours><hours>80</hours><hours>80</hours>", false)
    );
  }

  /**
   *  Simple combination of node sequences involving integers. uses "union" operator .
   */
  @org.junit.Test
  public void combiningnodeseqhc2() {
    final XQuery query = new XQuery(
      "for $h in ( (<hours>0</hours>,<hours>1</hours>) union (//hours)) order by number($h) return $h",
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
      assertSerialization("<hours>0</hours><hours>1</hours><hours>12</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>40</hours><hours>40</hours><hours>40</hours><hours>80</hours><hours>80</hours><hours>80</hours>", false)
    );
  }

  /**
   *  Simple combination of node sequences involving integers and repetition. uses "|" operator .
   */
  @org.junit.Test
  public void combiningnodeseqhc3() {
    final XQuery query = new XQuery(
      "for $h in ( (<hours>0</hours>,<hours>40</hours>) | (//hours)) order by number($h) return $h",
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
      assertSerialization("<hours>0</hours><hours>12</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>40</hours><hours>40</hours><hours>40</hours><hours>40</hours><hours>80</hours><hours>80</hours><hours>80</hours>", false)
    );
  }

  /**
   *  Simple combination of node sequences involving integers and repetition. uses "union" operator .
   */
  @org.junit.Test
  public void combiningnodeseqhc4() {
    final XQuery query = new XQuery(
      "for $h in ( (<hours>0</hours>,<hours>40</hours>) union (//hours)) order by number($h) return $h",
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
      assertSerialization("<hours>0</hours><hours>12</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>40</hours><hours>40</hours><hours>40</hours><hours>40</hours><hours>80</hours><hours>80</hours><hours>80</hours>", false)
    );
  }

  /**
   *  Simple combination of node sequences involving integers and the empty sequence. Uses "|" operator .
   */
  @org.junit.Test
  public void combiningnodeseqhc5() {
    final XQuery query = new XQuery(
      "for $h in ( () | (//hours)) order by number($h) return $h",
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
      assertSerialization("<hours>12</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>40</hours><hours>40</hours><hours>40</hours><hours>80</hours><hours>80</hours><hours>80</hours>", false)
    );
  }

  /**
   *  Simple combination of node sequences involving integers and the empty sequence. Uses "union" operator .
   */
  @org.junit.Test
  public void combiningnodeseqhc6() {
    final XQuery query = new XQuery(
      "for $h in ( () union (//hours)) order by number($h) return $h",
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
      assertSerialization("<hours>12</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>40</hours><hours>40</hours><hours>40</hours><hours>80</hours><hours>80</hours><hours>80</hours>", false)
    );
  }

  /**
   *  Simple combination of node sequences involving different children of xml data source. Uses "|" operator .
   */
  @org.junit.Test
  public void combiningnodeseqhc7() {
    final XQuery query = new XQuery(
      "for $h in ( (//hours[xs:integer(.) le 20]) | (//hours[xs:integer(.) gt 20])) order by number($h) return $h",
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
      assertSerialization("<hours>12</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>40</hours><hours>40</hours><hours>40</hours><hours>80</hours><hours>80</hours><hours>80</hours>", false)
    );
  }

  /**
   *  Simple combination of node sequences involving xml data source. Uses "union" operator .
   */
  @org.junit.Test
  public void combiningnodeseqhc8() {
    final XQuery query = new XQuery(
      "for $h in ( (//hours[xs:integer(.) le 20]) union (//hours[xs:integer(.) gt 20])) order by number($h) return $h",
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
      assertSerialization("<hours>12</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>40</hours><hours>40</hours><hours>40</hours><hours>80</hours><hours>80</hours><hours>80</hours>", false)
    );
  }

  /**
   *  Simple combination of node sequences involving multiple xml data sources. Uses "|" operator .
   */
  @org.junit.Test
  public void combiningnodeseqhc9() {
    final XQuery query = new XQuery(
      "for $h in ( ($works//hours) | ($staff//grade[xs:integer(.) gt 12])) order by number($h) return $h",
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
      assertSerialization("<hours>12</hours><grade>13</grade><grade>13</grade><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>40</hours><hours>40</hours><hours>40</hours><hours>80</hours><hours>80</hours><hours>80</hours>", false)
    );
  }

  /**
   *  arg: node .
   */
  @org.junit.Test
  public void fnUnionNodeArgs001() {
    final XQuery query = new XQuery(
      "/bib/book[3]/title union /bib/book[1]/title",
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
      assertSerialization("<title>TCP/IP Illustrated</title><title>Data on the Web</title>", false)
    );
  }

  /**
   *  arg: incorrect nodes .
   */
  @org.junit.Test
  public void fnUnionNodeArgs002() {
    final XQuery query = new XQuery(
      "(/bib/book/title | /bib/book)/local-name()",
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
      assertStringValue(false, "book title book title book title book title")
    );
  }

  /**
   *  arg: node .
   */
  @org.junit.Test
  public void fnUnionNodeArgs003() {
    final XQuery query = new XQuery(
      "/bib/book[3]/title | root(fn:exactly-one(/bib/book[3]/title))",
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
      assertSerialization("<!-- this file is a copy of bib.xml; just adds a few comments and PI nodes for testing --><!-- Comment 1 --><?PI1 Processing Instruction 1?><bib>\n    <book year=\"1994\">\n        <title>TCP/IP Illustrated</title>\n        <author><last>Stevens</last><first>W.</first></author>\n        <publisher>Addison-Wesley</publisher>\n        <price> 65.95</price>\n    </book>\n \n    <book year=\"1992\">\n        <title>Advanced Programming in the Unix environment</title>\n        <author><last>Stevens</last><first>W.</first></author>\n        <publisher>Addison-Wesley</publisher>\n        <price>65.95</price>\n    </book>\n \n    <book year=\"2000\">\n        <title>Data on the Web</title>\n        <author><last>Abiteboul</last><first>Serge</first></author>\n        <author><last>Buneman</last><first>Peter</first></author>\n        <author><last>Suciu</last><first>Dan</first></author>\n        <publisher>Morgan Kaufmann Publishers</publisher>\n        <price> 39.95</price>\n    </book>\n \n    <book year=\"1999\">\n        <title>The Economics of Technology and Content for Digital TV</title>\n        <editor>\n               <last>Gerbarg</last><first>Darcy</first>\n                <affiliation>CITI</affiliation>\n        </editor>\n            <publisher>Kluwer Academic Publishers</publisher>\n        <price>129.95</price>\n    </book>\n</bib><!-- Comment 2 --><?PI2 Processing Instruction 2?><title>Data on the Web</title>", false)
    );
  }

  /**
   *  arg: text node & node .
   */
  @org.junit.Test
  public void fnUnionNodeArgs004() {
    final XQuery query = new XQuery(
      "/bib/book[3]/title/text() union /bib/book[1]/title",
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
      assertSerialization("<title>TCP/IP Illustrated</title>Data on the Web", false)
    );
  }

  /**
   *  arg: processing-instruction node & node .
   */
  @org.junit.Test
  public void fnUnionNodeArgs005() {
    final XQuery query = new XQuery(
      "/processing-instruction() union /bib/book[2]/title",
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
      assertSerialization("<?PI1 Processing Instruction 1?><title>Advanced Programming in the Unix environment</title><?PI2 Processing Instruction 2?>", false)
    );
  }

  /**
   *  arg: processing-instruction node & node .
   */
  @org.junit.Test
  public void fnUnionNodeArgs006() {
    final XQuery query = new XQuery(
      "/processing-instruction(PI1) union /bib/book[3]/title",
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
      assertSerialization("<?PI1 Processing Instruction 1?><title>Data on the Web</title>", false)
    );
  }

  /**
   *  arg: comment node & node .
   */
  @org.junit.Test
  public void fnUnionNodeArgs007() {
    final XQuery query = new XQuery(
      "/comment() union /bib/book[1]/title",
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
      assertSerialization("<!-- this file is a copy of bib.xml; just adds a few comments and PI nodes for testing --><!-- Comment 1 --><title>TCP/IP Illustrated</title><!-- Comment 2 -->", false)
    );
  }

  /**
   *  arg: text node & node .
   */
  @org.junit.Test
  public void fnUnionNodeArgs008() {
    final XQuery query = new XQuery(
      "/bib/book[3]/title/text() | /bib/book[1]/title",
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
      assertSerialization("<title>TCP/IP Illustrated</title>Data on the Web", false)
    );
  }

  /**
   *  arg: processing-instruction node & node .
   */
  @org.junit.Test
  public void fnUnionNodeArgs009() {
    final XQuery query = new XQuery(
      "/processing-instruction() | /bib/book[2]/title",
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
      assertSerialization("<?PI1 Processing Instruction 1?><title>Advanced Programming in the Unix environment</title><?PI2 Processing Instruction 2?>", false)
    );
  }

  /**
   *  arg: processing-instruction node & node .
   */
  @org.junit.Test
  public void fnUnionNodeArgs010() {
    final XQuery query = new XQuery(
      "/processing-instruction(PI1) | /bib/book[3]/title",
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
      assertSerialization("<?PI1 Processing Instruction 1?><title>Data on the Web</title>", false)
    );
  }

  /**
   *  arg: comment node & node .
   */
  @org.junit.Test
  public void fnUnionNodeArgs011() {
    final XQuery query = new XQuery(
      "/comment() | /bib/book[1]/title",
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
      assertSerialization("<!-- this file is a copy of bib.xml; just adds a few comments and PI nodes for testing --><!-- Comment 1 --><title>TCP/IP Illustrated</title><!-- Comment 2 -->", false)
    );
  }

  /**
   *  arg: node & non existing node .
   */
  @org.junit.Test
  public void fnUnionNodeArgs012() {
    final XQuery query = new XQuery(
      "//author union //nonexisting",
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
  public void fnUnionNodeArgs013() {
    final XQuery query = new XQuery(
      "//author | ()",
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
  public void fnUnionNodeArgs014() {
    final XQuery query = new XQuery(
      "() | ()",
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
        assertStringValue(false, "")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  arg: node & node .
   */
  @org.junit.Test
  public void fnUnionNodeArgs023() {
    final XQuery query = new XQuery(
      "string-join(for $node in ((//price/text()) , (//price/text())) union ((//price/text()) , (//price/text())) return $node, \"|\")",
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
}
