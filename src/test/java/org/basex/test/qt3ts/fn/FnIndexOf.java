package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the index-of function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnIndexOf extends QT3TestSet {

  /**
   *  A test whose essence is: `index-of()`. .
   */
  @org.junit.Test
  public void kSeqIndexOfFunc1() {
    final XQuery query = new XQuery(
      "index-of()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `empty(index-of(xs:double("NaN"), xs:double("NaN")))`. .
   */
  @org.junit.Test
  public void kSeqIndexOfFunc10() {
    final XQuery query = new XQuery(
      "empty(index-of(xs:double(\"NaN\"), xs:double(\"NaN\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty(index-of(4, "4"))`. .
   */
  @org.junit.Test
  public void kSeqIndexOfFunc11() {
    final XQuery query = new XQuery(
      "empty(index-of(4, \"4\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `index-of(4, 4)`. .
   */
  @org.junit.Test
  public void kSeqIndexOfFunc12() {
    final XQuery query = new XQuery(
      "index-of(4, 4)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  A test whose essence is: `empty(index-of((), 4))`. .
   */
  @org.junit.Test
  public void kSeqIndexOfFunc13() {
    final XQuery query = new XQuery(
      "empty(index-of((), 4))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `index-of(4, 4)`. .
   */
  @org.junit.Test
  public void kSeqIndexOfFunc14() {
    final XQuery query = new XQuery(
      "index-of(4, 4)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  A test whose essence is: `deep-equal((1, 2, 3, 4, 5, 6), index-of((4, 4, 4, 4, 4, 4), 4))`. .
   */
  @org.junit.Test
  public void kSeqIndexOfFunc15() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 3, 4, 5, 6), index-of((4, 4, 4, 4, 4, 4), 4))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty(index-of(xs:anyURI("example.com/"), xs:hexBinary("FF")))`. .
   */
  @org.junit.Test
  public void kSeqIndexOfFunc16() {
    final XQuery query = new XQuery(
      "empty(index-of(xs:anyURI(\"example.com/\"), xs:hexBinary(\"FF\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `index-of(xs:untypedAtomic("example.com/"), xs:anyURI("example.com/"))`. .
   */
  @org.junit.Test
  public void kSeqIndexOfFunc17() {
    final XQuery query = new XQuery(
      "index-of(xs:untypedAtomic(\"example.com/\"), xs:anyURI(\"example.com/\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  A test whose essence is: `deep-equal(index-of((1, 2, "three", 5, 5, 6), 5), (4, 5))`. .
   */
  @org.junit.Test
  public void kSeqIndexOfFunc18() {
    final XQuery query = new XQuery(
      "deep-equal(index-of((1, 2, \"three\", 5, 5, 6), 5), (4, 5))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty(index-of((10, 20, 30, 40), 35))`. .
   */
  @org.junit.Test
  public void kSeqIndexOfFunc19() {
    final XQuery query = new XQuery(
      "empty(index-of((10, 20, 30, 40), 35))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `index-of(1)`. .
   */
  @org.junit.Test
  public void kSeqIndexOfFunc2() {
    final XQuery query = new XQuery(
      "index-of(1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `deep-equal(index-of((10, 20, 30, 30, 20, 10), 20), (2, 5))`. .
   */
  @org.junit.Test
  public void kSeqIndexOfFunc20() {
    final XQuery query = new XQuery(
      "deep-equal(index-of((10, 20, 30, 30, 20, 10), 20), (2, 5))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal(index-of(("a", "sport", "and", "a", "pastime"), "a"), (1, 4))`. .
   */
  @org.junit.Test
  public void kSeqIndexOfFunc21() {
    final XQuery query = new XQuery(
      "deep-equal(index-of((\"a\", \"sport\", \"and\", \"a\", \"pastime\"), \"a\"), (1, 4))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(index-of((1, 2, 3, 2, 1), 2)) eq 2`. .
   */
  @org.junit.Test
  public void kSeqIndexOfFunc22() {
    final XQuery query = new XQuery(
      "count(index-of((1, 2, 3, 2, 1), 2)) eq 2",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(index-of((1, 2, 3, 2, 1), 1)) eq 2`. .
   */
  @org.junit.Test
  public void kSeqIndexOfFunc23() {
    final XQuery query = new XQuery(
      "count(index-of((1, 2, 3, 2, 1), 1)) eq 2",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(index-of((1, 2, 3, 2, 1), 3)) eq 1`. .
   */
  @org.junit.Test
  public void kSeqIndexOfFunc24() {
    final XQuery query = new XQuery(
      "count(index-of((1, 2, 3, 2, 1), 3)) eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(index-of((1, 2, 3, 2, 1), 4)) eq 0`. .
   */
  @org.junit.Test
  public void kSeqIndexOfFunc25() {
    final XQuery query = new XQuery(
      "count(index-of((1, 2, 3, 2, 1), 4)) eq 0",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `index-of((1, 2, 3), 1, ())`. .
   */
  @org.junit.Test
  public void kSeqIndexOfFunc3() {
    final XQuery query = new XQuery(
      "index-of((1, 2, 3), 1, ())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  A test whose essence is: `index-of("a string", "a string", "http://www.example.com/COLLATION/NOT/SUPPORTED")`. .
   */
  @org.junit.Test
  public void kSeqIndexOfFunc4() {
    final XQuery query = new XQuery(
      "index-of(\"a string\", \"a string\", \"http://www.example.com/COLLATION/NOT/SUPPORTED\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOCH0002")
    );
  }

  /**
   *  A test whose essence is: `index-of("a string", "a string", "http://www.w3.org/2005/xpath-functions/collation/codepoint", "wrong param")`. .
   */
  @org.junit.Test
  public void kSeqIndexOfFunc5() {
    final XQuery query = new XQuery(
      "index-of(\"a string\", \"a string\", \"http://www.w3.org/2005/xpath-functions/collation/codepoint\", \"wrong param\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `index-of("a string", "a string", "http://www.w3.org/2005/xpath-functions/collation/codepoint")`. .
   */
  @org.junit.Test
  public void kSeqIndexOfFunc6() {
    final XQuery query = new XQuery(
      "index-of(\"a string\", \"a string\", \"http://www.w3.org/2005/xpath-functions/collation/codepoint\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  A test whose essence is: `empty(index-of(xs:double("NaN"), xs:float("NaN")))`. .
   */
  @org.junit.Test
  public void kSeqIndexOfFunc7() {
    final XQuery query = new XQuery(
      "empty(index-of(xs:double(\"NaN\"), xs:float(\"NaN\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty(index-of(xs:float("NaN"), xs:double("NaN")))`. .
   */
  @org.junit.Test
  public void kSeqIndexOfFunc8() {
    final XQuery query = new XQuery(
      "empty(index-of(xs:float(\"NaN\"), xs:double(\"NaN\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty(index-of(xs:float("NaN"), xs:float("NaN")))`. .
   */
  @org.junit.Test
  public void kSeqIndexOfFunc9() {
    final XQuery query = new XQuery(
      "empty(index-of(xs:float(\"NaN\"), xs:float(\"NaN\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  arg1: Sequence of integers , arg2: integer .
   */
  @org.junit.Test
  public void fnIndexofMixArgs001() {
    final XQuery query = new XQuery(
      "fn:index-of ((10, 20, 30, 30, 20, 10), 20)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2 5")
    );
  }

  /**
   *  arg1: Sequence of integers , arg2: integer .
   */
  @org.junit.Test
  public void fnIndexofMixArgs002() {
    final XQuery query = new XQuery(
      "fn:index-of ((10, 20, 30, 40), 35)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "")
    );
  }

  /**
   *  arg1: Sequence of string , arg2: string .
   */
  @org.junit.Test
  public void fnIndexofMixArgs003() {
    final XQuery query = new XQuery(
      "fn:index-of ((\"a\", \"sport\", \"and\", \"a\", \"pastime\"), \"a\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1 4")
    );
  }

  /**
   *  arg1: Sequence of string , arg2: string .
   */
  @org.junit.Test
  public void fnIndexofMixArgs004() {
    final XQuery query = new XQuery(
      "fn:index-of((\"sport\", \"\", \"and\", \"\", \"\", \"pastime\"), \"\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2 4 5")
    );
  }

  /**
   *  arg1: empty Sequence , arg2: string .
   */
  @org.junit.Test
  public void fnIndexofMixArgs005() {
    final XQuery query = new XQuery(
      "fn:index-of((),\"\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "")
    );
  }

  /**
   *  arg1: Sequence of string , arg2: string .
   */
  @org.junit.Test
  public void fnIndexofMixArgs006() {
    final XQuery query = new XQuery(
      "fn:index-of((\"sport\"), \"\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "")
    );
  }

  /**
   *  arg1: empty Sequence , arg2: untypedAtomic .
   */
  @org.junit.Test
  public void fnIndexofMixArgs007() {
    final XQuery query = new XQuery(
      "fn:index-of((),fn:exactly-one(xs:untypedAtomic('')))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "")
    );
  }

  /**
   *  arg1: Sequence of float , arg2: float .
   */
  @org.junit.Test
  public void fnIndexofMixArgs008() {
    final XQuery query = new XQuery(
      "fn:index-of((xs:float('NaN')), fn:exactly-one(xs:float('NaN')))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "")
    );
  }

  /**
   *  arg1: Sequence of double , arg2: double .
   */
  @org.junit.Test
  public void fnIndexofMixArgs009() {
    final XQuery query = new XQuery(
      "fn:index-of((xs:double('NaN')), fn:exactly-one(xs:double('NaN')))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "")
    );
  }

  /**
   *  arg1: Sequence of double, arg2: double .
   */
  @org.junit.Test
  public void fnIndexofMixArgs010() {
    final XQuery query = new XQuery(
      "fn:index-of((xs:double('INF')), fn:exactly-one(xs:double('INF')))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  arg1: Sequence of double, arg2: double .
   */
  @org.junit.Test
  public void fnIndexofMixArgs011() {
    final XQuery query = new XQuery(
      "fn:index-of((xs:double('-INF')), fn:exactly-one(xs:double('-INF')))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  arg1: Sequence of decimal, arg2: decimal .
   */
  @org.junit.Test
  public void fnIndexofMixArgs012() {
    final XQuery query = new XQuery(
      "fn:index-of((xs:decimal('9.99999999999999999999999999')), fn:exactly-one(xs:decimal('9.99999999999999999999999999')))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  arg1: Sequence of decimal, arg2: decimal .
   */
  @org.junit.Test
  public void fnIndexofMixArgs013() {
    final XQuery query = new XQuery(
      "fn:index-of((xs:decimal('9.99999999999999999999999999')), fn:exactly-one(xs:decimal('9.9999999999999999999999999')))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "")
    );
  }

  /**
   *  arg1: Sequence of positiveInteger, arg2: positiveInteger .
   */
  @org.junit.Test
  public void fnIndexofMixArgs014() {
    final XQuery query = new XQuery(
      "fn:index-of((xs:positiveInteger('1'),xs:positiveInteger('2')), fn:exactly-one(xs:positiveInteger('2')))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("2")
    );
  }

  /**
   *  arg1: Sequence of negativeInteger, arg2: negativeInteger .
   */
  @org.junit.Test
  public void fnIndexofMixArgs015() {
    final XQuery query = new XQuery(
      "fn:index-of((xs:negativeInteger('-2'), xs:negativeInteger('-1')), fn:exactly-one(xs:negativeInteger('-1')))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("2")
    );
  }

  /**
   *  Use a nested sequence in the sequence to search .
   */
  @org.junit.Test
  public void fnIndexofMixArgs016() {
    final XQuery query = new XQuery(
      "fn:index-of((1, (1,2,3)),1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1 2")
    );
  }

  /**
   *  Use multiple nested sequence in the sequence to search .
   */
  @org.junit.Test
  public void fnIndexofMixArgs017() {
    final XQuery query = new XQuery(
      "fn:index-of(((1),(1), (2,1), (0,1)),1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1 2 4 6")
    );
  }

  /**
   *  Use external variable for the sequence parameter .
   */
  @org.junit.Test
  public void fnIndexofMixArgs018() {
    final XQuery query = new XQuery(
      "fn:index-of((/bib/book/publisher), \"Addison-Wesley\")",
      ctx);
    query.context(node(file("docs/bib.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1 2")
    );
  }

  /**
   *  Use external variable both in sequence and search parameter .
   */
  @org.junit.Test
  public void fnIndexofMixArgs019() {
    final XQuery query = new XQuery(
      "fn:index-of((/bib/book/publisher), /bib/book[1]/publisher[1]/text() cast as xs:string)",
      ctx);
    query.context(node(file("docs/bib.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1 2")
    );
  }

  /**
   *  Use empty string with other strings in sequence .
   */
  @org.junit.Test
  public void fnIndexofMixArgs020() {
    final XQuery query = new XQuery(
      "fn:index-of((\"sport\", \"\", \"and\", \"\", \"\", \"pastime\"), \"\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2 4 5")
    );
  }

  /**
   *  Use empty string with integers in the sequence parameter .
   */
  @org.junit.Test
  public void fnIndexofMixArgs021() {
    final XQuery query = new XQuery(
      "fn:index-of( (\"\", 1, \"\"), \"\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1 3")
    );
  }

  /**
   *  Use no search parameter .
   */
  @org.junit.Test
  public void fnIndexofMixArgs022() {
    final XQuery query = new XQuery(
      "fn:index-of((1,2,3,4))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }
}
