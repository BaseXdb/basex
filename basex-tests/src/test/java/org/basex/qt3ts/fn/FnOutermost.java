package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the outermost() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnOutermost extends QT3TestSet {

  /**
   * Attempts to evaluate the "outermost" function with no arguments..
   */
  @org.junit.Test
  public void fnOutermost001() {
    final XQuery query = new XQuery(
      "fn:outermost()",
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
   * Attempts to reference the "outermost" function with arity zero..
   */
  @org.junit.Test
  public void fnOutermost002() {
    final XQuery query = new XQuery(
      "fn:outermost#0",
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
   * Attempts to evaluate the "outermost" function with two arguments..
   */
  @org.junit.Test
  public void fnOutermost003() {
    final XQuery query = new XQuery(
      "fn:outermost( (), 1 )",
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
   * Attempts to reference the "outermost" function with arity two..
   */
  @org.junit.Test
  public void fnOutermost004() {
    final XQuery query = new XQuery(
      "fn:outermost#2",
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
   * Attempts to reference the "outermost" function with arity one..
   */
  @org.junit.Test
  public void fnOutermost005() {
    final XQuery query = new XQuery(
      "fn:exists( fn:outermost#1 )",
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
   * Evaluates the "outermost" function with an argument of type xs:anyAtomicType..
   */
  @org.junit.Test
  public void fnOutermost006() {
    final XQuery query = new XQuery(
      "fn:outermost( 1 )",
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
   * Evaluates the "outermost" function with an argument of type function()..
   */
  @org.junit.Test
  public void fnOutermost007() {
    final XQuery query = new XQuery(
      "fn:outermost( fn:dateTime#2 )",
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
  public void fnOutermost009() {
    final XQuery query = new XQuery(
      "( fn:outermost( if (current-date() eq xs:date('1900-01-01'))\n" +
      "                                 then .\n" +
      "                                 else 1 ),\n" +
      "              fn:outermost( if (current-date() eq xs:date('1900-01-01'))\n" +
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
  public void fnOutermost011() {
    final XQuery query = new XQuery(
      "( fn:outermost( if (current-date() eq xs:date('1900-01-01'))\n" +
      "                                 then .\n" +
      "                                 else fn:dateTime#2 ),\n" +
      "              fn:outermost( if (current-date() eq xs:date('1900-01-01'))\n" +
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
   * Evaluates the "outermost" function with the argument set as follows: $nodes of type document-node() .
   */
  @org.junit.Test
  public void fnOutermost012() {
    final XQuery query = new XQuery(
      "fn:outermost( / )",
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
   * Evaluates the "outermost" function with the argument set as follows: $nodes of type document-node() .
   */
  @org.junit.Test
  public void fnOutermost013() {
    final XQuery query = new XQuery(
      "fn:deep-equal(fn:outermost( / ), / )",
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
   * Evaluates the "outermost" function with the argument set as follows: $nodes of type attribute()* .
   */
  @org.junit.Test
  public void fnOutermost014() {
    final XQuery query = new XQuery(
      "fn:outermost( //*/@* )",
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
   * Evaluates the "outermost" function with the argument set as follows: $nodes of type attribute()* .
   */
  @org.junit.Test
  public void fnOutermost015() {
    final XQuery query = new XQuery(
      "fn:outermost( //*/@* ) ! string()",
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
   * Evaluates the "outermost" function with the argument set as follows: $nodes of type attribute()* .
   */
  @org.junit.Test
  public void fnOutermost016() {
    final XQuery query = new XQuery(
      "deep-equal(fn:outermost( //*/@* ), //*/@*)",
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
   * Evaluates the "outermost" function with the argument set as follows: $nodes of type processing-instruction()* .
   */
  @org.junit.Test
  public void fnOutermost023() {
    final XQuery query = new XQuery(
      "fn:outermost( //processing-instruction() )",
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
   * Evaluates the "outermost" function with the argument set as follows: $nodes of type processing-instruction()* .
   */
  @org.junit.Test
  public void fnOutermost024() {
    final XQuery query = new XQuery(
      "fn:outermost( //processing-instruction() ) ! local-name() ",
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
   * Evaluates the "outermost" function with the argument set as follows: $nodes of type processing-instruction()* .
   */
  @org.junit.Test
  public void fnOutermost025() {
    final XQuery query = new XQuery(
      "fn:deep-equal( fn:outermost( //processing-instruction() ), \n" +
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
   * Evaluates the "outermost" function with the argument set as follows: $nodes of type comment()* .
   */
  @org.junit.Test
  public void fnOutermost026() {
    final XQuery query = new XQuery(
      "fn:outermost( //comment() )",
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
   * Evaluates the "outermost" function with the argument set as follows: $nodes of type comment()* .
   */
  @org.junit.Test
  public void fnOutermost027() {
    final XQuery query = new XQuery(
      "fn:outermost( //comment() ) ! string()",
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
   * Evaluates the "outermost" function with the argument set as follows: $nodes of type comment()* .
   */
  @org.junit.Test
  public void fnOutermost028() {
    final XQuery query = new XQuery(
      "fn:deep-equal( fn:outermost( //comment() ),\n" +
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
   * Evaluates the "outermost" function with the argument set as follows: $nodes of type text()* .
   */
  @org.junit.Test
  public void fnOutermost029() {
    final XQuery query = new XQuery(
      "fn:outermost( //text() )",
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
   * Evaluates the "outermost" function with the argument set as follows: $nodes of type text()* .
   */
  @org.junit.Test
  public void fnOutermost030() {
    final XQuery query = new XQuery(
      "fn:outermost( //text() ) ! string() ",
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
   * Evaluates the "outermost" function with the argument set as follows: $nodes of type text()* .
   */
  @org.junit.Test
  public void fnOutermost031() {
    final XQuery query = new XQuery(
      "fn:deep-equal( fn:outermost( //text() ),\n" +
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
   * Evaluates the "outermost" function with the argument set as follows: $nodes of type element()* .
   */
  @org.junit.Test
  public void fnOutermost032() {
    final XQuery query = new XQuery(
      "fn:outermost( //* )",
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
   * Evaluates the "outermost" function with the argument set as follows: $nodes of type element()* .
   */
  @org.junit.Test
  public void fnOutermost033() {
    final XQuery query = new XQuery(
      "fn:outermost( //* ) ! local-name(.)",
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
      assertEq("('root')")
    );
  }

  /**
   * Evaluates the "outermost" function with the argument set as follows: $nodes of type element()* .
   */
  @org.junit.Test
  public void fnOutermost034() {
    final XQuery query = new XQuery(
      "fn:deep-equal( fn:outermost( //* ), \n" +
      "                           let $nodes := //*\n" +
      "                           return $nodes except $nodes/descendant::node() )",
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
   * Evaluates the "outermost" function with the argument set as follows: $nodes of type node()* .
   */
  @org.junit.Test
  public void fnOutermost035() {
    final XQuery query = new XQuery(
      "fn:outermost( //node() )",
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
   * Evaluates the "outermost" function with the argument set as follows: $nodes of type node()* .
   */
  @org.junit.Test
  public void fnOutermost036() {
    final XQuery query = new XQuery(
      "fn:deep-equal( fn:outermost( //node() ), \n" +
      "                           let $nodes := //node()\n" +
      "                           return $nodes except $nodes/descendant::node() )",
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
   * Evaluates the "outermost" function with the argument set as follows: $nodes of type node()* .
   */
  @org.junit.Test
  public void fnOutermost037() {
    final XQuery query = new XQuery(
      "fn:outermost( /root/node() )",
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
   * Evaluates the "outermost" function with the argument set as follows: $nodes of type node()* .
   */
  @org.junit.Test
  public void fnOutermost038() {
    final XQuery query = new XQuery(
      "fn:deep-equal( fn:outermost( /root/node() ), \n" +
      "                           let $nodes := /root/node()\n" +
      "                           return $nodes except $nodes/descendant::node() )",
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
   * Evaluates the "outermost" function with the argument set as follows: $nodes of type node()* .
   */
  @org.junit.Test
  public void fnOutermost039() {
    final XQuery query = new XQuery(
      "fn:outermost( /root/descendant::node() )",
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
   * Evaluates the "outermost" function with the argument set as follows: $nodes of type node()* .
   */
  @org.junit.Test
  public void fnOutermost040() {
    final XQuery query = new XQuery(
      "fn:deep-equal( fn:outermost( /root/descendant::node() ), \n" +
      "                           let $nodes := /root/descendant::node()\n" +
      "                           return $nodes except $nodes/descendant::node() )",
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
   * Evaluates the "outermost" function with the argument set as follows: $nodes of type node()* .
   */
  @org.junit.Test
  public void fnOutermost041() {
    final XQuery query = new XQuery(
      "fn:outermost( /root/level[1]/level[1]/ancestor::node() )",
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
   * Evaluates the "outermost" function with the argument set as follows: $nodes of type node()* .
   */
  @org.junit.Test
  public void fnOutermost042() {
    final XQuery query = new XQuery(
      "fn:deep-equal( fn:outermost( /root/level[1]/level[1]/ancestor::node() ), \n" +
      "                           let $nodes := /root/level[1]/level[1]/ancestor::node()\n" +
      "                           return $nodes except $nodes/descendant::node() )",
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
   * Evaluates the "outermost" function with the argument set as follows: $nodes of type node()* .
   */
  @org.junit.Test
  public void fnOutermost043() {
    final XQuery query = new XQuery(
      "fn:outermost( /root/level[1]/level[last()]/preceding-sibling::node() )",
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
   * Evaluates the "outermost" function with the argument set as follows: $nodes of type node()* .
   */
  @org.junit.Test
  public void fnOutermost044() {
    final XQuery query = new XQuery(
      "fn:deep-equal( fn:outermost( /root/level[1]/level[last()]/preceding-sibling::node() ), \n" +
      "                           let $nodes := /root/level[1]/level[last()]/preceding-sibling::node()\n" +
      "                           return $nodes except $nodes/descendant::node() )",
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
   * Evaluates the "outermost" function with the argument set as follows: $nodes of type node()* .
   */
  @org.junit.Test
  public void fnOutermost045() {
    final XQuery query = new XQuery(
      "fn:outermost( /root/level[1]/level[last()]/preceding::node() )",
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
   * Evaluates the "outermost" function with the argument set as follows: $nodes of type node()* .
   */
  @org.junit.Test
  public void fnOutermost046() {
    final XQuery query = new XQuery(
      "fn:deep-equal( fn:outermost( /root/level[1]/level[last()]/preceding::node() ), \n" +
      "                           let $nodes := /root/level[1]/level[last()]/preceding::node()\n" +
      "                           return $nodes except $nodes/descendant::node() )",
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
   * Evaluates the "outermost" function with the argument set as follows: $nodes of type node()* .
   */
  @org.junit.Test
  public void fnOutermost047() {
    final XQuery query = new XQuery(
      "fn:outermost( /root/level[1]/following-sibling::node() )",
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
   * Evaluates the "outermost" function with the argument set as follows: $nodes of type node()* .
   */
  @org.junit.Test
  public void fnOutermost048() {
    final XQuery query = new XQuery(
      "fn:deep-equal( fn:outermost( /root/level[1]/following-sibling::node() ), \n" +
      "                           let $nodes := /root/level[1]/following-sibling::node()\n" +
      "                           return $nodes except $nodes/descendant::node() )",
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
   * Evaluates the "outermost" function with the argument set as follows: $nodes of type node()* .
   */
  @org.junit.Test
  public void fnOutermost049() {
    final XQuery query = new XQuery(
      "fn:outermost( /root/level[1]/level[1]/following::node() )",
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
   * Evaluates the "outermost" function with the argument set as follows: $nodes of type node()* .
   */
  @org.junit.Test
  public void fnOutermost050() {
    final XQuery query = new XQuery(
      "fn:deep-equal( fn:outermost( /root/level[1]/level[1]/following::node() ), \n" +
      "                           let $nodes := /root/level[1]/level[1]/following::node()\n" +
      "                           return $nodes except $nodes/descendant::node() )",
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
   * Evaluates the "outermost" function with the argument set as follows: $nodes of type node()* .
   */
  @org.junit.Test
  public void fnOutermost051() {
    final XQuery query = new XQuery(
      "fn:outermost( /root/node()/.. )",
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
   * Evaluates the "outermost" function with the argument set as follows: $nodes of type node()* .
   */
  @org.junit.Test
  public void fnOutermost052() {
    final XQuery query = new XQuery(
      "fn:deep-equal( fn:outermost( /root/node()/.. ), \n" +
      "                           let $nodes := /root/node()/..\n" +
      "                           return $nodes except $nodes/descendant::node() )",
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
   * Evaluates the "outermost" function with the argument set as follows: $nodes of type node()* from two documents .
   */
  @org.junit.Test
  public void fnOutermost053() {
    final XQuery query = new XQuery(
      "fn:outermost( ($doc1//node(), $doc2//node()) )",
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
   * Evaluates the "outermost" function with the argument set as follows: $nodes of type node()* from two documents .
   */
  @org.junit.Test
  public void fnOutermost054() {
    final XQuery query = new XQuery(
      "fn:deep-equal( fn:outermost( ($doc1//node(), $doc2//node()) ),\n" +
      "                           let $nodes := ($doc1//node(), $doc2//node())\n" +
      "                           return $nodes except $nodes/descendant::node() )",
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
   * Evaluates the "outermost" function with the input sequence not in document order .
   */
  @org.junit.Test
  public void fnOutermost055() {
    final XQuery query = new XQuery(
      "let $in := for $x in //* order by local-name($x) return $x\n" +
      "            return deep-equal(fn:outermost($in)/local-name(), fn:outermost(//*)/local-name())",
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
   * Evaluates the "outermost" function with the input sequence containing duplicates .
   */
  @org.junit.Test
  public void fnOutermost056() {
    final XQuery query = new XQuery(
      "let $in := for $x in //* order by local-name($x) return $x\n" +
      "            return deep-equal(fn:outermost(($in, $in))/local-name(), fn:outermost(//*)/local-name())",
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
   * Evaluates the "outermost" function with the input sequence empty .
   */
  @org.junit.Test
  public void fnOutermost057() {
    final XQuery query = new XQuery(
      "outermost(//rubbish)",
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
   * Check that "outermost" doesn't change node identity .
   */
  @org.junit.Test
  public void fnOutermost058() {
    final XQuery query = new XQuery(
      "outermost(//*) except //*",
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
