package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the innermost() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnInnermost extends QT3TestSet {

  /**
   * Attempts to evaluate the "innermost" function with no arguments..
   */
  @org.junit.Test
  public void fnInnermost001() {
    final XQuery query = new XQuery(
      "fn:innermost()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0017")
    );
  }

  /**
   * Attempts to reference the "innermost" function with arity zero..
   */
  @org.junit.Test
  public void fnInnermost002() {
    final XQuery query = new XQuery(
      "fn:innermost#0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0017")
    );
  }

  /**
   * Attempts to evaluate the "innermost" function with two arguments..
   */
  @org.junit.Test
  public void fnInnermost003() {
    final XQuery query = new XQuery(
      "fn:innermost( (), 1 )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0017")
    );
  }

  /**
   * Attempts to reference the "innermost" function with arity two..
   */
  @org.junit.Test
  public void fnInnermost004() {
    final XQuery query = new XQuery(
      "fn:innermost#2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0017")
    );
  }

  /**
   * Attempts to reference the "innermost" function with arity one..
   */
  @org.junit.Test
  public void fnInnermost005() {
    final XQuery query = new XQuery(
      "fn:exists( fn:innermost#1 )",
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
   * Evaluates the "innermost" function with an argument of type xs:anyAtomicType..
   */
  @org.junit.Test
  public void fnInnermost006() {
    final XQuery query = new XQuery(
      "fn:innermost( 1 )",
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
   * Evaluates the "innermost" function with an argument of type function()..
   */
  @org.junit.Test
  public void fnInnermost007() {
    final XQuery query = new XQuery(
      "fn:innermost( fn:dateTime#2 )",
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
   * Tests the type checking of the argument..
   */
  @org.junit.Test
  public void fnInnermost009() {
    final XQuery query = new XQuery(
      "( fn:innermost( if (current-date() eq xs:date('1900-01-01'))\n" +
      "                                 then .\n" +
      "                                 else 1 ),\n" +
      "              fn:innermost( if (current-date() eq xs:date('1900-01-01'))\n" +
      "                                 then 1\n" +
      "                                 else . ) )",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
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
   * Tests the type checking of the argument..
   */
  @org.junit.Test
  public void fnInnermost011() {
    final XQuery query = new XQuery(
      "( fn:innermost( if (current-date() eq xs:date('1900-01-01'))\n" +
      "                                 then .\n" +
      "                                 else fn:dateTime#2 ),\n" +
      "              fn:innermost( if (current-date() eq xs:date('1900-01-01'))\n" +
      "                                 then fn:dateTime#2\n" +
      "                                 else . ) )",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
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
   * Evaluates the "innermost" function with the argument set as follows: $nodes of type document-node() .
   */
  @org.junit.Test
  public void fnInnermost012() {
    final XQuery query = new XQuery(
      "fn:innermost( / )",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertCount(1)
    );
  }

  /**
   * Evaluates the "innermost" function with the argument set as follows: $nodes of type document-node() .
   */
  @org.junit.Test
  public void fnInnermost013() {
    final XQuery query = new XQuery(
      "fn:deep-equal(fn:innermost( / ), / )",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
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
   * Evaluates the "innermost" function with the argument set as follows: $nodes of type attribute()* .
   */
  @org.junit.Test
  public void fnInnermost014() {
    final XQuery query = new XQuery(
      "fn:innermost( //*/@* )",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertCount(7)
    );
  }

  /**
   * Evaluates the "innermost" function with the argument set as follows: $nodes of type attribute()* .
   */
  @org.junit.Test
  public void fnInnermost015() {
    final XQuery query = new XQuery(
      "fn:innermost( //*/@* ) ! string()",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("('0a','00a','000a','01a','010a','02a','020a')")
    );
  }

  /**
   * Evaluates the "innermost" function with the argument set as follows: $nodes of type attribute()* .
   */
  @org.junit.Test
  public void fnInnermost016() {
    final XQuery query = new XQuery(
      "deep-equal(fn:innermost( //*/@* ), //*/@*)",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
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
   * Evaluates the "innermost" function with the argument set as follows: $nodes of type processing-instruction()* .
   */
  @org.junit.Test
  public void fnInnermost023() {
    final XQuery query = new XQuery(
      "fn:innermost( //processing-instruction() )",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertCount(7)
    );
  }

  /**
   * Evaluates the "innermost" function with the argument set as follows: $nodes of type processing-instruction()* .
   */
  @org.junit.Test
  public void fnInnermost024() {
    final XQuery query = new XQuery(
      "fn:innermost( //processing-instruction() ) ! local-name() ",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("('level-0','level-00','level-000','level-01','level-010','level-02','level-020')")
    );
  }

  /**
   * Evaluates the "innermost" function with the argument set as follows: $nodes of type processing-instruction()* .
   */
  @org.junit.Test
  public void fnInnermost025() {
    final XQuery query = new XQuery(
      "fn:deep-equal( fn:innermost( //processing-instruction() ), \n" +
      "                          //processing-instruction() )",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
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
   * Evaluates the "innermost" function with the argument set as follows: $nodes of type comment()* .
   */
  @org.junit.Test
  public void fnInnermost026() {
    final XQuery query = new XQuery(
      "fn:innermost( //comment() )",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertCount(7)
    );
  }

  /**
   * Evaluates the "innermost" function with the argument set as follows: $nodes of type comment()* .
   */
  @org.junit.Test
  public void fnInnermost027() {
    final XQuery query = new XQuery(
      "fn:innermost( //comment() ) ! string()",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("('0c','00c','000c','01c','010c','02c','020c')")
    );
  }

  /**
   * Evaluates the "innermost" function with the argument set as follows: $nodes of type comment()* .
   */
  @org.junit.Test
  public void fnInnermost028() {
    final XQuery query = new XQuery(
      "fn:deep-equal( fn:innermost( //comment() ),\n" +
      "                           //comment() )",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
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
   * Evaluates the "innermost" function with the argument set as follows: $nodes of type text()* .
   */
  @org.junit.Test
  public void fnInnermost029() {
    final XQuery query = new XQuery(
      "fn:innermost( //text() )",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertCount(14)
    );
  }

  /**
   * Evaluates the "innermost" function with the argument set as follows: $nodes of type text()* .
   */
  @org.junit.Test
  public void fnInnermost030() {
    final XQuery query = new XQuery(
      "fn:innermost( //text() ) ! string() ",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("('0t',' ',\n                         '00t',' ','000t',' ',\n                         '01t',' ','010t',' ',\n                         '02t',' ','020t',' ')")
    );
  }

  /**
   * Evaluates the "innermost" function with the argument set as follows: $nodes of type text()* .
   */
  @org.junit.Test
  public void fnInnermost031() {
    final XQuery query = new XQuery(
      "fn:deep-equal( fn:innermost( //text() ),\n" +
      "                           //text() )",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
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
   * Evaluates the "innermost" function with the argument set as follows: $nodes of type element()* .
   */
  @org.junit.Test
  public void fnInnermost032() {
    final XQuery query = new XQuery(
      "fn:innermost( //* )",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertCount(14)
    );
  }

  /**
   * Evaluates the "innermost" function with the argument set as follows: $nodes of type element()* .
   */
  @org.junit.Test
  public void fnInnermost033() {
    final XQuery query = new XQuery(
      "fn:innermost( //* ) ! local-name(.)",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("('empty-level-0',   'non-empty-level-0',\n                         'empty-level-00',  'non-empty-level-00',\n                         'empty-level-000', 'non-empty-level-000',\n                         'empty-level-01',  'non-empty-level-01',\n                         'empty-level-010', 'non-empty-level-010',\n                         'empty-level-02',  'non-empty-level-02',\n                         'empty-level-020', 'non-empty-level-020')")
    );
  }

  /**
   * Evaluates the "innermost" function with the argument set as follows: $nodes of type element()* .
   */
  @org.junit.Test
  public void fnInnermost034() {
    final XQuery query = new XQuery(
      "fn:deep-equal( fn:innermost( //* ), \n" +
      "                           let $nodes := //*\n" +
      "                           return $nodes except $nodes/ancestor::node() )",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
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
   * Evaluates the "innermost" function with the argument set as follows: $nodes of type node()* .
   */
  @org.junit.Test
  public void fnInnermost035() {
    final XQuery query = new XQuery(
      "fn:innermost( //node() )",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertCount(35)
    );
  }

  /**
   * Evaluates the "innermost" function with the argument set as follows: $nodes of type node()* .
   */
  @org.junit.Test
  public void fnInnermost036() {
    final XQuery query = new XQuery(
      "fn:deep-equal( fn:innermost( //node() ), \n" +
      "                           let $nodes := //node()\n" +
      "                           return $nodes except $nodes/ancestor::node() )",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
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
   * Evaluates the "innermost" function with the argument set as follows: $nodes of type node()* .
   */
  @org.junit.Test
  public void fnInnermost037() {
    final XQuery query = new XQuery(
      "fn:innermost( /root/node() )",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertCount(8)
    );
  }

  /**
   * Evaluates the "innermost" function with the argument set as follows: $nodes of type node()* .
   */
  @org.junit.Test
  public void fnInnermost038() {
    final XQuery query = new XQuery(
      "fn:deep-equal( fn:innermost( /root/node() ), \n" +
      "                           let $nodes := /root/node()\n" +
      "                           return $nodes except $nodes/ancestor::node() )",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
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
   * Evaluates the "innermost" function with the argument set as follows: $nodes of type node()* .
   */
  @org.junit.Test
  public void fnInnermost039() {
    final XQuery query = new XQuery(
      "fn:innermost( /root/descendant::node() )",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertCount(35)
    );
  }

  /**
   * Evaluates the "innermost" function with the argument set as follows: $nodes of type node()* .
   */
  @org.junit.Test
  public void fnInnermost040() {
    final XQuery query = new XQuery(
      "fn:deep-equal( fn:innermost( /root/descendant::node() ), \n" +
      "                           let $nodes := /root/descendant::node()\n" +
      "                           return $nodes except $nodes/ancestor::node() )",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
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
   * Evaluates the "innermost" function with the argument set as follows: $nodes of type node()* .
   */
  @org.junit.Test
  public void fnInnermost041() {
    final XQuery query = new XQuery(
      "fn:innermost( /root/level[1]/level[1]/ancestor::node() )",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertCount(1)
    );
  }

  /**
   * Evaluates the "innermost" function with the argument set as follows: $nodes of type node()* .
   */
  @org.junit.Test
  public void fnInnermost042() {
    final XQuery query = new XQuery(
      "fn:deep-equal( fn:innermost( /root/level[1]/level[1]/ancestor::node() ), \n" +
      "                           let $nodes := /root/level[1]/level[1]/ancestor::node()\n" +
      "                           return $nodes except $nodes/ancestor::node() )",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
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
   * Evaluates the "innermost" function with the argument set as follows: $nodes of type node()* .
   */
  @org.junit.Test
  public void fnInnermost043() {
    final XQuery query = new XQuery(
      "fn:innermost( /root/level[1]/level[last()]/preceding-sibling::node() )",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertCount(5)
    );
  }

  /**
   * Evaluates the "innermost" function with the argument set as follows: $nodes of type node()* .
   */
  @org.junit.Test
  public void fnInnermost044() {
    final XQuery query = new XQuery(
      "fn:deep-equal( fn:innermost( /root/level[1]/level[last()]/preceding-sibling::node() ), \n" +
      "                           let $nodes := /root/level[1]/level[last()]/preceding-sibling::node()\n" +
      "                           return $nodes except $nodes/ancestor::node() )",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
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
   * Evaluates the "innermost" function with the argument set as follows: $nodes of type node()* .
   */
  @org.junit.Test
  public void fnInnermost045() {
    final XQuery query = new XQuery(
      "fn:innermost( /root/level[1]/level[last()]/preceding::node() )",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertCount(10)
    );
  }

  /**
   * Evaluates the "innermost" function with the argument set as follows: $nodes of type node()* .
   */
  @org.junit.Test
  public void fnInnermost046() {
    final XQuery query = new XQuery(
      "fn:deep-equal( fn:innermost( /root/level[1]/level[last()]/preceding::node() ), \n" +
      "                           let $nodes := /root/level[1]/level[last()]/preceding::node()\n" +
      "                           return $nodes except $nodes/ancestor::node() )",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
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
   * Evaluates the "innermost" function with the argument set as follows: $nodes of type node()* .
   */
  @org.junit.Test
  public void fnInnermost047() {
    final XQuery query = new XQuery(
      "fn:innermost( /root/level[1]/following-sibling::node() )",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertCount(2)
    );
  }

  /**
   * Evaluates the "innermost" function with the argument set as follows: $nodes of type node()* .
   */
  @org.junit.Test
  public void fnInnermost048() {
    final XQuery query = new XQuery(
      "fn:deep-equal( fn:innermost( /root/level[1]/following-sibling::node() ), \n" +
      "                           let $nodes := /root/level[1]/following-sibling::node()\n" +
      "                           return $nodes except $nodes/ancestor::node() )",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
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
   * Evaluates the "innermost" function with the argument set as follows: $nodes of type node()* .
   */
  @org.junit.Test
  public void fnInnermost049() {
    final XQuery query = new XQuery(
      "fn:innermost( /root/level[1]/level[1]/following::node() )",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertCount(20)
    );
  }

  /**
   * Evaluates the "innermost" function with the argument set as follows: $nodes of type node()* .
   */
  @org.junit.Test
  public void fnInnermost050() {
    final XQuery query = new XQuery(
      "fn:deep-equal( fn:innermost( /root/level[1]/level[1]/following::node() ), \n" +
      "                           let $nodes := /root/level[1]/level[1]/following::node()\n" +
      "                           return $nodes except $nodes/ancestor::node() )",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
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
   * Evaluates the "innermost" function with the argument set as follows: $nodes of type node()* .
   */
  @org.junit.Test
  public void fnInnermost051() {
    final XQuery query = new XQuery(
      "fn:innermost( /root/node()/.. )",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertCount(1)
    );
  }

  /**
   * Evaluates the "innermost" function with the argument set as follows: $nodes of type node()* .
   */
  @org.junit.Test
  public void fnInnermost052() {
    final XQuery query = new XQuery(
      "fn:deep-equal( fn:innermost( /root/node()/.. ), \n" +
      "                           let $nodes := /root/node()/..\n" +
      "                           return $nodes except $nodes/ancestor::node() )",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
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
   * Evaluates the "innermost" function with the argument set as follows: $nodes of type node()* from two documents .
   */
  @org.junit.Test
  public void fnInnermost053() {
    final XQuery query = new XQuery(
      "fn:innermost( ($doc1//node(), $doc2//node()) )",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertCount(26)
    );
  }

  /**
   * Evaluates the "innermost" function with the argument set as follows: $nodes of type node()* from two documents .
   */
  @org.junit.Test
  public void fnInnermost054() {
    final XQuery query = new XQuery(
      "fn:deep-equal( fn:innermost( ($doc1//node(), $doc2//node()) ),\n" +
      "                           let $nodes := ($doc1//node(), $doc2//node())\n" +
      "                           return $nodes except $nodes/ancestor::node() )",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
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
   * Evaluates the "innermost" function with the input sequence not in document order .
   */
  @org.junit.Test
  public void fnInnermost055() {
    final XQuery query = new XQuery(
      "let $in := for $x in //* order by local-name($x) return $x\n" +
      "            return deep-equal(fn:innermost($in)/local-name(), fn:innermost(//*)/local-name())",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
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
   * Evaluates the "innermost" function with the input sequence containing duplicates .
   */
  @org.junit.Test
  public void fnInnermost056() {
    final XQuery query = new XQuery(
      "let $in := for $x in //* order by local-name($x) return $x\n" +
      "            return deep-equal(fn:innermost(($in, $in))/local-name(), fn:innermost(//*)/local-name())",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
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
   * Evaluates the "innermost" function with the input sequence empty .
   */
  @org.junit.Test
  public void fnInnermost057() {
    final XQuery query = new XQuery(
      "innermost(//rubbish)",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
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
   * Check that "innermost" doesn't change node identity .
   */
  @org.junit.Test
  public void fnInnermost058() {
    final XQuery query = new XQuery(
      "innermost(//*) except //*",
      ctx);
    try {
      query.context(node(file("fn/innermost/innermost.xml")));
      query.bind("$doc1", node(file("fn/innermost/doc1.xml")));
      query.bind("$doc2", node(file("fn/innermost/doc2.xml")));
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
