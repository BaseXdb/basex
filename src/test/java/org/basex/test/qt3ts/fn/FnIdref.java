package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the idref() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnIdref extends QT3TestSet {

  /**
   *  Wrong arguments to fn:idref(). .
   */
  @org.junit.Test
  public void k2SeqIDREFFunc1() {
    final XQuery query = new XQuery(
      "idref((), ())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  Wrong arguments to fn:idref(). .
   */
  @org.junit.Test
  public void k2SeqIDREFFunc2() {
    final XQuery query = new XQuery(
      "(1, 2, 3)[idref(\"ncname\", .)]",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  Wrong arguments to fn:idref(). .
   */
  @org.junit.Test
  public void k2SeqIDREFFunc3() {
    final XQuery query = new XQuery(
      "(1, 2, 3)[idref(\"ncname\")]",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * Evaluation of fn:idref with incorrect arity..
   */
  @org.junit.Test
  public void fnIdref1() {
    final XQuery query = new XQuery(
      "fn:idref(\"argument 1\", / ,\"Argument 3\")",
      ctx);
    query.context(node(file("fn/id/iddtd.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  Evaluation of fn:idref with context item not a node. .
   */
  @org.junit.Test
  public void fnIdref2() {
    final XQuery query = new XQuery(
      "(1 to 10)[fn:idref(\"argument1\")]",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  Evaluation of fn:idref with context item not a node and second argument set to ".". .
   */
  @org.junit.Test
  public void fnIdref22() {
    final XQuery query = new XQuery(
      "fn:idref(\"argument1\",.)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPDY0002")
    );
  }

  /**
   *  Evaluation of fn:idref with second argument not a node. .
   */
  @org.junit.Test
  public void fnIdref3() {
    final XQuery query = new XQuery(
      "fn:idref(\"argument1\", \"A\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  Evaluation of fn:idref with node not being from document where root is the document element. .
   */
  @org.junit.Test
  public void fnIdref4() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace copy=\"http://www.w3.org/QT3/copy\";\n" +
      "        let $var := copy:copy(/*) return fn:idref(\"argument1\", $var)\n" +
      "      ",
      ctx);
    query.context(node(file("docs/works-mod.xml")));
    query.addModule("http://www.w3.org/QT3/copy", file("fn/id/copy.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      error("FODC0001")
    );
  }

  /**
   *  Evaluation of fn:idref with multiple ID, but none matching one element. .
   */
  @org.junit.Test
  public void fnIdrefDtd10() {
    final XQuery query = new XQuery(
      "fn:count(fn:idref(\"nomatching1 nomatching2\", /IDS[1]))",
      ctx);
    query.context(node(file("fn/id/iddtd.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluation of fn:idref with ID set to empty string. .
   */
  @org.junit.Test
  public void fnIdrefDtd11() {
    final XQuery query = new XQuery(
      "fn:count(fn:idref(\"\", /IDS[1]))",
      ctx);
    query.context(node(file("fn/id/iddtd.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluation of fn:idref used as an argument to function fn:node-name() .
   */
  @org.junit.Test
  public void fnIdrefDtd12() {
    final XQuery query = new XQuery(
      "fn:node-name(fn:idref(\"id2\", /IDS[1]))",
      ctx);
    query.context(node(file("fn/id/iddtd.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "anIdRef")
    );
  }

  /**
   *  Evaluation of fn:idref used as part of a node expression ("is" operand). Compare same elements. .
   */
  @org.junit.Test
  public void fnIdrefDtd13() {
    final XQuery query = new XQuery(
      "(fn:idref(\"id1\", /IDS[1])) is (fn:idref(\"id1\", /IDS[1]))",
      ctx);
    query.context(node(file("fn/id/iddtd.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of fn:idref used as part of a node expression ("is" operand). Compare different elements. .
   */
  @org.junit.Test
  public void fnIdrefDtd14() {
    final XQuery query = new XQuery(
      "(fn:idref(\"id1\", /IDS[1])) is (fn:idref(\"id2\", /IDS[1]))",
      ctx);
    query.context(node(file("fn/id/iddtd.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of fn:idref for which ID list have the same value for all its members. .
   */
  @org.junit.Test
  public void fnIdrefDtd15() {
    final XQuery query = new XQuery(
      "count(fn:idref((\"id1\",\"id1\"), /IDS[1]))",
      ctx);
    query.context(node(file("fn/id/iddtd.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluation of fn:idref for which ID list have the same value for all its members but different case. .
   */
  @org.junit.Test
  public void fnIdrefDtd16() {
    final XQuery query = new XQuery(
      "count(fn:idref((\"id1\",\"ID1\"), /IDS[1]))",
      ctx);
    query.context(node(file("fn/id/iddtd.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluation of fn:idref for which ID uses the fn lower case function. .
   */
  @org.junit.Test
  public void fnIdrefDtd17() {
    final XQuery query = new XQuery(
      "fn:idref(fn:lower-case(\"ID1\"), /IDS[1])/name(..)",
      ctx);
    query.context(node(file("fn/id/iddtd.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "elementwithidrefattr-1")
    );
  }

  /**
   *  Evaluation of fn:idref for which ID uses the fn:upper-case function. .
   */
  @org.junit.Test
  public void fnIdrefDtd18() {
    final XQuery query = new XQuery(
      "fn:idref(fn:upper-case(\"id5\"), /IDS[1])/name(..)",
      ctx);
    query.context(node(file("fn/id/iddtd.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "elementwithidrefattr-6")
    );
  }

  /**
   *  Evaluation of fn:idref for which $arg uses the fn:concat function. .
   */
  @org.junit.Test
  public void fnIdrefDtd19() {
    final XQuery query = new XQuery(
      "fn:idref(fn:concat(\"i\",\"d1\"), /IDS[1])/name(..)",
      ctx);
    query.context(node(file("fn/id/iddtd.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "elementwithidrefattr-1")
    );
  }

  /**
   *  Evaluation of fn:idref for which $arg uses the xs:string function. .
   */
  @org.junit.Test
  public void fnIdrefDtd20() {
    final XQuery query = new XQuery(
      "fn:idref(xs:string(\"id1\"), /IDS[1])/name(..)",
      ctx);
    query.context(node(file("fn/id/iddtd.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "elementwithidrefattr-1")
    );
  }

  /**
   *  Evaluation of fn:idref for which $arg uses the fn:string-join function. .
   */
  @org.junit.Test
  public void fnIdrefDtd21() {
    final XQuery query = new XQuery(
      "fn:idref(fn:string-join((\"id\",\"1\"),\"\"), /IDS[1])/name(..)",
      ctx);
    query.context(node(file("fn/id/iddtd.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "elementwithidrefattr-1")
    );
  }

  /**
   *  Evaluation of fn:idref with declare ordering. .
   */
  @org.junit.Test
  public void fnIdrefDtd23() {
    final XQuery query = new XQuery(
      "declare ordering ordered;  \n" +
      "        <results>{fn:idref(\"id4\", /IDS[1])}</results>",
      ctx);
    query.context(node(file("fn/id/iddtd.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<results anIdRef=\"id4\"/>", false)
    );
  }

  /**
   *  Evaluation of fn:idref, where an IDREFS node contains multiple IDREFs. For errata FO.E29. .
   */
  @org.junit.Test
  public void fnIdrefDtd24() {
    final XQuery query = new XQuery(
      "<results>{fn:idref(\"language\", /)}</results>",
      ctx);
    query.context(node(file("app/FunctxFn/functx_book.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<results refs='context language'/>", false)
    );
  }

  /**
   * Evaluation of fn:idref with no second argument..
   */
  @org.junit.Test
  public void fnIdrefDtd25() {
    final XQuery query = new XQuery(
      "fn:idref(\"id1\")/name(..)",
      ctx);
    query.context(node(file("fn/id/iddtd.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "elementwithidrefattr-1")
    );
  }

  /**
   *  Evaluation of fn:idref with given ID matching a single element. .
   */
  @org.junit.Test
  public void fnIdrefDtd5() {
    final XQuery query = new XQuery(
      "fn:idref(\"id1\",/IDS[1])/name(..)",
      ctx);
    query.context(node(file("fn/id/iddtd.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "elementwithidrefattr-1")
    );
  }

  /**
   *  Evaluation of fn:idref with given ID not matching a single element. .
   */
  @org.junit.Test
  public void fnIdrefDtd6() {
    final XQuery query = new XQuery(
      "fn:idref(\"nomatchingid\", /IDS[1])",
      ctx);
    query.context(node(file("fn/id/iddtd.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertEmpty()
    );
  }

  /**
   *  Evaluation of fn:idref with given ID matching multiple elements. .
   */
  @org.junit.Test
  public void fnIdrefDtd7() {
    final XQuery query = new XQuery(
      "fn:idref(\"id4\", /IDS[1])/name(..)",
      ctx);
    query.context(node(file("fn/id/iddtd.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "elementwithidrefattr-4")
    );
  }

  /**
   *  Evaluation of fn:idref function, which attempts to create element with two attributes with same name. .
   */
  @org.junit.Test
  public void fnIdrefDtd8() {
    final XQuery query = new XQuery(
      "<results>{fn:idref((\"id1\", \"id2\"), /IDS[1])}</results>",
      ctx);
    query.context(node(file("fn/id/iddtd.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQDY0025")
    );
  }

  /**
   *  Evaluation of fn:idref with multiple ID, but only one matching one element. .
   */
  @org.junit.Test
  public void fnIdrefDtd9() {
    final XQuery query = new XQuery(
      "fn:idref((\"id1\", \"nomatching\"), /IDS[1])/name(..)",
      ctx);
    query.context(node(file("fn/id/iddtd.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "elementwithidrefattr-1")
    );
  }
}
