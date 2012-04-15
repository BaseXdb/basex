package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the id() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnId extends QT3TestSet {

  /**
   * Wrong arguments to fn:id()..
   */
  @org.junit.Test
  public void k2SeqIDFunc1() {
    final XQuery query = new XQuery(
      "id((), ())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * Select elements with xml:id, and trigger node sorting..
   */
  @org.junit.Test
  public void k2SeqIDFunc10() {
    final XQuery query = new XQuery(
      "for $i in id((\"short\", \"positiveInteger\")) return $i/@name/string()",
      ctx);
    query.context(node(file("fn/id/UsingXMLId.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "positiveInteger short")
    );
  }

  /**
   * Select elements with xml:id, and trigger node sorting..
   */
  @org.junit.Test
  public void k2SeqIDFunc11() {
    final XQuery query = new XQuery(
      "id((\"short\"), //xs:element/@name[. = \"positiveInteger\"])/@name",
      ctx);
    query.context(node(file("fn/id/UsingXMLId.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "short")
    );
  }

  /**
   * Select elements with xml:id, trigger node sorting, and use an invalid NCName in the lookup..
   */
  @org.junit.Test
  public void k2SeqIDFunc12() {
    final XQuery query = new XQuery(
      "id((\".\", \"short\", \"123\"), //xs:element/@name[. = \"positiveInteger\"])/@name",
      ctx);
    query.context(node(file("fn/id/UsingXMLId.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "short")
    );
  }

  /**
   * Use on xml:id with input strings which contains many IDREFs, and invalid ones too..
   */
  @org.junit.Test
  public void k2SeqIDFunc13() {
    final XQuery query = new XQuery(
      "fn:id(//b/@ref)/data(exactly-one(@*))",
      ctx);
    query.context(node(file("fn/id/XMLIDMany.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "a b c d e f i")
    );
  }

  /**
   * Select elements with xml:id, and trigger node sorting: space-separated id list.
   */
  @org.junit.Test
  public void k2SeqIDFunc14() {
    final XQuery query = new XQuery(
      "for $i in id((\"short positiveInteger\")) return $i/@name/string()",
      ctx);
    query.context(node(file("fn/id/UsingXMLId.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "positiveInteger short")
    );
  }

  /**
   * Use on xml:id with input strings which contains many IDREFs, and invalid ones too; tab-separated id list.
   */
  @org.junit.Test
  public void k2SeqIDFunc15() {
    final XQuery query = new XQuery(
      "fn:id(string-join(reverse(//b/@ref), '\t'))/data(exactly-one(@*))",
      ctx);
    query.context(node(file("fn/id/XMLIDMany.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "a b c d e f i")
    );
  }

  /**
   * Wrong arguments to fn:id()..
   */
  @org.junit.Test
  public void k2SeqIDFunc2() {
    final XQuery query = new XQuery(
      "(1, 2, 3)[id(\"ncname\", .)]",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * Wrong arguments to fn:id()..
   */
  @org.junit.Test
  public void k2SeqIDFunc3() {
    final XQuery query = new XQuery(
      "(1, 2, 3)[id(\"ncname\")]",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * Use an invalid target node..
   */
  @org.junit.Test
  public void k2SeqIDFunc4() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace copy=\"http://www.w3.org/QT3/copy\";\n" +
      "        id(\"id\", copy:copy((//comment())[1]))\n" +
      "      ",
      ctx);
    query.context(node(file("docs/auction.xml")));
    query.addModule("http://www.w3.org/QT3/copy", file("fn/id/copy.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      error("FODC0001")
    );
  }

  /**
   * Use an invalid target node..
   */
  @org.junit.Test
  public void k2SeqIDFunc5() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace copy=\"http://www.w3.org/QT3/copy\";\n" +
      "        id(\"id\", copy:copy((//processing-instruction())[1]))\n" +
      "      ",
      ctx);
    query.context(node(file("docs/auction.xml")));
    query.addModule("http://www.w3.org/QT3/copy", file("fn/id/copy.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      error("FODC0001")
    );
  }

  /**
   * Use an invalid target node.
   */
  @org.junit.Test
  public void k2SeqIDFunc6() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace copy=\"http://www.w3.org/QT3/copy\";\n" +
      "        id(\"id\", copy:copy(/*))\n" +
      "      ",
      ctx);
    query.context(node(file("docs/auction.xml")));
    query.addModule("http://www.w3.org/QT3/copy", file("fn/id/copy.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      error("FODC0001")
    );
  }

  /**
   * Use an invalid target node.
   */
  @org.junit.Test
  public void k2SeqIDFunc7() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace copy=\"http://www.w3.org/QT3/copy\";\n" +
      "        id(\"id\", (copy:copy(/*)//*:NegativeComments)[last()])\n" +
      "      ",
      ctx);
    query.context(node(file("docs/auction.xml")));
    query.addModule("http://www.w3.org/QT3/copy", file("fn/id/copy.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      error("FODC0001")
    );
  }

  /**
   * Use an invalid target node..
   */
  @org.junit.Test
  public void k2SeqIDFunc8() {
    final XQuery query = new XQuery(
      "let $i := <e><e/><e/><e/><e/><e/><e/><e/><b xml:id=\"foo\"/><e/></e>return id(\"foo\", $i)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FODC0001")
    );
  }

  /**
   * Use a constructed document node with xml:id..
   */
  @org.junit.Test
  public void k2SeqIDFunc9() {
    final XQuery query = new XQuery(
      "let $i := document {<e> <e/> <e/> <e/> <e/> <e/> <e/> <e/> <b xml:id=\"foo\"/> <e/> </e>} return id(\"foo\", $i)/name()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "b")
    );
  }

  /**
   * Evaluation of fn:id with incorrect arity..
   */
  @org.junit.Test
  public void fnId1() {
    final XQuery query = new XQuery(
      "fn:id(\"argument 1\", / ,\"Argument 3\")",
      ctx);
    query.context(node(file("fn/id/iddtd.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   * Evaluation of fn:id with context item not a node..
   */
  @org.junit.Test
  public void fnId2() {
    final XQuery query = new XQuery(
      "(1 to 5)[fn:id(\"argument1\")]",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * Evaluation of fn:id with context item not a node and second argument set to "."..
   */
  @org.junit.Test
  public void fnId22() {
    final XQuery query = new XQuery(
      "(1 to 5)[ fn:id(\"argument1\",.)]",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * Evaluation of fn:id with second argument not a node..
   */
  @org.junit.Test
  public void fnId3() {
    final XQuery query = new XQuery(
      "fn:id(\"argument1\", \"A\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * Evaluation of fn:id with node not being from document where root is the document element..
   */
  @org.junit.Test
  public void fnId4() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace copy=\"http://www.w3.org/QT3/copy\";\n" +
      "        let $var := copy:copy(/*) return fn:id(\"argument1\", $var)\n" +
      "      ",
      ctx);
    query.context(node(file("docs/auction.xml")));
    query.addModule("http://www.w3.org/QT3/copy", file("fn/id/copy.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      error("FODC0001")
    );
  }

  /**
   * Evaluation of fn:id with multiple IDREF, but none matching one element..
   */
  @org.junit.Test
  public void fnIdDtd10() {
    final XQuery query = new XQuery(
      "fn:count(fn:id(\"nomatching1 nomatching2\", /IDS[1]))",
      ctx);
    query.context(node(file("fn/id/iddtd.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   * Evaluation of fn:id with IDREF set to empty string..
   */
  @org.junit.Test
  public void fnIdDtd11() {
    final XQuery query = new XQuery(
      "fn:id(\"\", /IDS[1])",
      ctx);
    query.context(node(file("fn/id/iddtd.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertEmpty()
    );
  }

  /**
   * Evaluation of fn:id function that give first argument as argument to fn:substring..
   */
  @org.junit.Test
  public void fnIdDtd12() {
    final XQuery query = new XQuery(
      "fn:id(fn:substring(\"1id3\",2), /IDS[1])/name()",
      ctx);
    query.context(node(file("fn/id/iddtd.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "elementwithid-3")
    );
  }

  /**
   * Evaluation of fn:id, where the same IDREF makes reference to the same element..
   */
  @org.junit.Test
  public void fnIdDtd13() {
    final XQuery query = new XQuery(
      "fn:id(\"id4\", /IDS[1])/name()",
      ctx);
    query.context(node(file("fn/id/iddtd.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "elementwithid-4")
    );
  }

  /**
   * Evaluation of fn:id for which the given IDREF contains a prefix..
   */
  @org.junit.Test
  public void fnIdDtd14() {
    final XQuery query = new XQuery(
      "fn:id(\"p1:id5\", /IDS[1])",
      ctx);
    query.context(node(file("fn/id/iddtd.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertEmpty()
    );
  }

  /**
   * Evaluation of fn:id for which IDREF list have the same value for all its members..
   */
  @org.junit.Test
  public void fnIdDtd15() {
    final XQuery query = new XQuery(
      "fn:id(\"id1 id1\", /IDS[1])/name()",
      ctx);
    query.context(node(file("fn/id/iddtd.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "elementwithid-1")
    );
  }

  /**
   * Evaluation of fn:id for which IDREF list have the same value for all its members but different case..
   */
  @org.junit.Test
  public void fnIdDtd16() {
    final XQuery query = new XQuery(
      "fn:id(\"id1 ID1\", /IDS[1])/name()",
      ctx);
    query.context(node(file("fn/id/iddtd.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "elementwithid-1")
    );
  }

  /**
   * Evaluation of fn:id for which IDREF uses the fn lower case function..
   */
  @org.junit.Test
  public void fnIdDtd17() {
    final XQuery query = new XQuery(
      "fn:id(fn:lower-case(\"ID1\"), /IDS[1])/name()",
      ctx);
    query.context(node(file("fn/id/iddtd.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "elementwithid-1")
    );
  }

  /**
   * Evaluation of fn:id for which IDREF uses the fn:upper-case function..
   */
  @org.junit.Test
  public void fnIdDtd18() {
    final XQuery query = new XQuery(
      "fn:id(fn:upper-case(\"id5\"), /IDS[1])/name()",
      ctx);
    query.context(node(file("fn/id/iddtd.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "elementwithid-6")
    );
  }

  /**
   * Evaluation of fn:id for which $arg uses the fn:concat function..
   */
  @org.junit.Test
  public void fnIdDtd19() {
    final XQuery query = new XQuery(
      "fn:id(fn:concat(\"i\",\"d1\"), /IDS[1])/name()",
      ctx);
    query.context(node(file("fn/id/iddtd.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "elementwithid-1")
    );
  }

  /**
   * Evaluation of fn:id for which $arg uses the xs:string function..
   */
  @org.junit.Test
  public void fnIdDtd20() {
    final XQuery query = new XQuery(
      "fn:id(xs:string(\"id1\"), /IDS[1])/name()",
      ctx);
    query.context(node(file("fn/id/iddtd.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "elementwithid-1")
    );
  }

  /**
   * Evaluation of fn:id for which $arg uses the fn:string-join function..
   */
  @org.junit.Test
  public void fnIdDtd21() {
    final XQuery query = new XQuery(
      "fn:id(fn:string-join((\"id\",\"1\"),\"\"), /IDS[1])/name()",
      ctx);
    query.context(node(file("fn/id/iddtd.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "elementwithid-1")
    );
  }

  /**
   * Evaluation of fn:id together with declare ordering..
   */
  @org.junit.Test
  public void fnIdDtd23() {
    final XQuery query = new XQuery(
      "declare ordering ordered; fn:id(\"id1 id2\", /IDS[1])/name()",
      ctx);
    query.context(node(file("fn/id/iddtd.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "elementwithid-1 elementwithid-2")
    );
  }

  /**
   * Evaluation of fn:id with given IDREF matching a single element..
   */
  @org.junit.Test
  public void fnIdDtd5() {
    final XQuery query = new XQuery(
      "fn:id(\"id1\", /IDS[1])/string(@anId)",
      ctx);
    query.context(node(file("fn/id/iddtd.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "id1")
    );
  }

  /**
   * Evaluation of fn:id with given IDREF not matching a single element..
   */
  @org.junit.Test
  public void fnIdDtd6() {
    final XQuery query = new XQuery(
      "fn:count(fn:id(\"nomatchingid\", /IDS[1]))",
      ctx);
    query.context(node(file("fn/id/iddtd.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   * Evaluation of fn:id with given IDREF matching same element..
   */
  @org.junit.Test
  public void fnIdDtd7() {
    final XQuery query = new XQuery(
      "fn:id(\"id2 id2\", /IDS[1])/name()",
      ctx);
    query.context(node(file("fn/id/iddtd.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "elementwithid-2")
    );
  }

  /**
   * Evaluation of fn:id with multiple IDREF matching multiple elements..
   */
  @org.junit.Test
  public void fnIdDtd8() {
    final XQuery query = new XQuery(
      "fn:id(\"id1 id2\", /IDS[1])/name()",
      ctx);
    query.context(node(file("fn/id/iddtd.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "elementwithid-1 elementwithid-2")
    );
  }

  /**
   * Evaluation of fn:id with multiple IDREF, but only one matching one element..
   */
  @org.junit.Test
  public void fnIdDtd9() {
    final XQuery query = new XQuery(
      "fn:id(\"id1 nomatching\", /IDS[1])/name()",
      ctx);
    query.context(node(file("fn/id/iddtd.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "elementwithid-1")
    );
  }
}
