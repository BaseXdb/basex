package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the id() function.
 *
 * @author BaseX Team 2005-14, BSD License
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
   * Select elements with xml:id, and trigger node sorting..
   */
  @org.junit.Test
  public void k2SeqIDFunc10() {
    final XQuery query = new XQuery(
      "for $i in id((\"short\", \"positiveInteger\")) return $i/@name/string()",
      ctx);
    try {
      query.context(node(file("fn/id/UsingXMLId.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.context(node(file("fn/id/UsingXMLId.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.context(node(file("fn/id/UsingXMLId.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.context(node(file("fn/id/XMLIDMany.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.context(node(file("fn/id/UsingXMLId.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.context(node(file("fn/id/XMLIDMany.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
   * Wrong arguments to fn:id()..
   */
  @org.junit.Test
  public void k2SeqIDFunc3() {
    final XQuery query = new XQuery(
      "(1, 2, 3)[id(\"ncname\")]",
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
    try {
      query.context(node(file("docs/auction.xml")));
      query.addModule("http://www.w3.org/QT3/copy", file("fn/id/copy.xq"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.context(node(file("docs/auction.xml")));
      query.addModule("http://www.w3.org/QT3/copy", file("fn/id/copy.xq"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.context(node(file("docs/auction.xml")));
      query.addModule("http://www.w3.org/QT3/copy", file("fn/id/copy.xq"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.context(node(file("docs/auction.xml")));
      query.addModule("http://www.w3.org/QT3/copy", file("fn/id/copy.xq"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "b")
    );
  }

  /**
   *  test fn:id on the empty sequence .
   */
  @org.junit.Test
  public void cbclId001() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:generate($arg as xs:integer?) as xs:string* { if ($arg = 0) then () else 'id1', 'id2' }; \n" +
      "      \tlet $doc := document { <root /> } return fn:empty( fn:id( local:generate(0), $doc) )\n" +
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
      assertBoolean(true)
    );
  }

  /**
   *  test fn:id on the empty sequence .
   */
  @org.junit.Test
  public void cbclId002() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tlet $doc := document { <root /> } return fn:empty( fn:id( (), $doc) )\n" +
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
      (
        assertBoolean(true)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  test fn:id in an axis expression .
   */
  @org.junit.Test
  public void cbclId003() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:generate($arg as xs:integer?) as xs:string* { if ($arg = 0) then () else 'id1', 'id2' }; \n" +
      "      \tlet $doc := document { <root /> } return fn:empty( $doc/fn:id( local:generate(0)) )\n" +
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
      assertBoolean(true)
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
    try {
      query.context(node(file("fn/id/iddtd.xml")));
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
   * Evaluation of fn:id with context item not a node..
   */
  @org.junit.Test
  public void fnId2() {
    final XQuery query = new XQuery(
      "(1 to 5)[fn:id(\"argument1\")]",
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
   * Evaluation of fn:id with context item not a node and second argument set to "."..
   */
  @org.junit.Test
  public void fnId22() {
    final XQuery query = new XQuery(
      "(1 to 5)[ fn:id(\"argument1\",.)]",
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
   * Evaluation of fn:id with second argument not a node..
   */
  @org.junit.Test
  public void fnId3() {
    final XQuery query = new XQuery(
      "fn:id(\"argument1\", \"A\")",
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
    try {
      query.context(node(file("docs/auction.xml")));
      query.addModule("http://www.w3.org/QT3/copy", file("fn/id/copy.xq"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.context(node(file("fn/id/iddtd.xml")));
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
   * Evaluation of fn:id with IDREF set to empty string..
   */
  @org.junit.Test
  public void fnIdDtd11() {
    final XQuery query = new XQuery(
      "fn:id(\"\", /IDS[1])",
      ctx);
    try {
      query.context(node(file("fn/id/iddtd.xml")));
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
   * Evaluation of fn:id function that give first argument as argument to fn:substring..
   */
  @org.junit.Test
  public void fnIdDtd12() {
    final XQuery query = new XQuery(
      "fn:id(fn:substring(\"1id3\",2), /IDS[1])/name()",
      ctx);
    try {
      query.context(node(file("fn/id/iddtd.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.context(node(file("fn/id/iddtd.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.context(node(file("fn/id/iddtd.xml")));
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
   * Evaluation of fn:id for which IDREF list have the same value for all its members..
   */
  @org.junit.Test
  public void fnIdDtd15() {
    final XQuery query = new XQuery(
      "fn:id(\"id1 id1\", /IDS[1])/name()",
      ctx);
    try {
      query.context(node(file("fn/id/iddtd.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.context(node(file("fn/id/iddtd.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.context(node(file("fn/id/iddtd.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.context(node(file("fn/id/iddtd.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.context(node(file("fn/id/iddtd.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.context(node(file("fn/id/iddtd.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.context(node(file("fn/id/iddtd.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.context(node(file("fn/id/iddtd.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.context(node(file("fn/id/iddtd.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.context(node(file("fn/id/iddtd.xml")));
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
   * Evaluation of fn:id with given IDREF matching same element..
   */
  @org.junit.Test
  public void fnIdDtd7() {
    final XQuery query = new XQuery(
      "fn:id(\"id2 id2\", /IDS[1])/name()",
      ctx);
    try {
      query.context(node(file("fn/id/iddtd.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.context(node(file("fn/id/iddtd.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.context(node(file("fn/id/iddtd.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "elementwithid-1")
    );
  }
}
