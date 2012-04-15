package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the deep-equal() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnDeepEqual extends QT3TestSet {

  /**
   *  A test whose essence is: `deep-equal()`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc1() {
    final XQuery query = new XQuery(
      "deep-equal()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `deep-equal(xs:float("NaN"), xs:double("NaN"))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc10() {
    final XQuery query = new XQuery(
      "deep-equal(xs:float(\"NaN\"), xs:double(\"NaN\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal(xs:double("NaN"), xs:float("NaN"))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc11() {
    final XQuery query = new XQuery(
      "deep-equal(xs:double(\"NaN\"), xs:float(\"NaN\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(deep-equal(xs:float("NaN"), xs:float(0)))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc12() {
    final XQuery query = new XQuery(
      "not(deep-equal(xs:float(\"NaN\"), xs:float(0)))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(deep-equal(xs:float(0), xs:float("NaN")))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc13() {
    final XQuery query = new XQuery(
      "not(deep-equal(xs:float(0), xs:float(\"NaN\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(deep-equal(xs:double(0), xs:double("NaN")))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc14() {
    final XQuery query = new XQuery(
      "not(deep-equal(xs:double(0), xs:double(\"NaN\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(deep-equal(xs:double("NaN"), xs:double(0)))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc15() {
    final XQuery query = new XQuery(
      "not(deep-equal(xs:double(\"NaN\"), xs:double(0)))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(deep-equal(xs:decimal("1"), xs:anyURI("example.com")))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc16() {
    final XQuery query = new XQuery(
      "not(deep-equal(xs:decimal(\"1\"), xs:anyURI(\"example.com\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(deep-equal(QName("example.com", "ncname"), 3e2))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc17() {
    final XQuery query = new XQuery(
      "not(deep-equal(QName(\"example.com\", \"ncname\"), 3e2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(deep-equal((1, 2, xs:anyURI("example.com")), (1, 2, 3)))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc18() {
    final XQuery query = new XQuery(
      "not(deep-equal((1, 2, xs:anyURI(\"example.com\")), (1, 2, 3)))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(deep-equal((1, xs:decimal("2.2"), 3), (1, 2, 3)))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc19() {
    final XQuery query = new XQuery(
      "not(deep-equal((1, xs:decimal(\"2.2\"), 3), (1, 2, 3)))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal("a string")`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc2() {
    final XQuery query = new XQuery(
      "deep-equal(\"a string\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `not(deep-equal((true(), 2, 3), (1, 2, 3)))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc20() {
    final XQuery query = new XQuery(
      "not(deep-equal((true(), 2, 3), (1, 2, 3)))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(deep-equal((1, 2, 3), (1, 2, QName("example.com", "ncname"))))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc21() {
    final XQuery query = new XQuery(
      "not(deep-equal((1, 2, 3), (1, 2, QName(\"example.com\", \"ncname\"))))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(deep-equal((1, 2, 3), (1, xs:hexBinary("FF"), 3)))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc22() {
    final XQuery query = new XQuery(
      "not(deep-equal((1, 2, 3), (1, xs:hexBinary(\"FF\"), 3)))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(deep-equal((1, 2, 3), (xs:base64Binary("FFFF"), 2, 3)))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc23() {
    final XQuery query = new XQuery(
      "not(deep-equal((1, 2, 3), (xs:base64Binary(\"FFFF\"), 2, 3)))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal((1, 2, xs:base64Binary("FFFF")), (1, 2, xs:base64Binary("FFFF")))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc24() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, xs:base64Binary(\"FFFF\")), (1, 2, xs:base64Binary(\"FFFF\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal((1, 1, 1), (1, 1, 1))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc25() {
    final XQuery query = new XQuery(
      "deep-equal((1, 1, 1), (1, 1, 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(deep-equal((1, 1, 3), (1, 1, 1)))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc26() {
    final XQuery query = new XQuery(
      "not(deep-equal((1, 1, 3), (1, 1, 1)))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(deep-equal((1, 3, 1), (1, 1, 1)))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc27() {
    final XQuery query = new XQuery(
      "not(deep-equal((1, 3, 1), (1, 1, 1)))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(deep-equal((3, 1, 1), (1, 1, 1)))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc28() {
    final XQuery query = new XQuery(
      "not(deep-equal((3, 1, 1), (1, 1, 1)))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(deep-equal((1, 1, 1), (3, 1, 1)))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc29() {
    final XQuery query = new XQuery(
      "not(deep-equal((1, 1, 1), (3, 1, 1)))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal("a string", "a string", "http://www.w3.org/2005/xpath-functions/collation/codepoint", "wrong param")`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc3() {
    final XQuery query = new XQuery(
      "deep-equal(\"a string\", \"a string\", \"http://www.w3.org/2005/xpath-functions/collation/codepoint\", \"wrong param\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `not(deep-equal((1, 1, 1), (1, 3, 1)))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc30() {
    final XQuery query = new XQuery(
      "not(deep-equal((1, 1, 1), (1, 3, 1)))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(deep-equal((1, 1, 1), (1, 1, 3)))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc31() {
    final XQuery query = new XQuery(
      "not(deep-equal((1, 1, 1), (1, 1, 3)))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal(index-of(20, 20), (1))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc32() {
    final XQuery query = new XQuery(
      "deep-equal(index-of(20, 20), (1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal(index-of((20, 40), 20), (1))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc33() {
    final XQuery query = new XQuery(
      "deep-equal(index-of((20, 40), 20), (1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal(index-of((20, 20), 20), (1, 2))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc34() {
    final XQuery query = new XQuery(
      "deep-equal(index-of((20, 20), 20), (1, 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal(index-of((20, 40, 20), 20), (1, 3))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc35() {
    final XQuery query = new XQuery(
      "deep-equal(index-of((20, 40, 20), 20), (1, 3))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal((1, 1, "str"), (1, 1, "str"))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc36() {
    final XQuery query = new XQuery(
      "deep-equal((1, 1, \"str\"), (1, 1, \"str\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal((1, "str", 1), (1, "str", 1))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc37() {
    final XQuery query = new XQuery(
      "deep-equal((1, \"str\", 1), (1, \"str\", 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal(("str", 1, 1), ("str", 1, 1))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc38() {
    final XQuery query = new XQuery(
      "deep-equal((\"str\", 1, 1), (\"str\", 1, 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal((1, 1, ()), (1, 1, ()))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc39() {
    final XQuery query = new XQuery(
      "deep-equal((1, 1, ()), (1, 1, ()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal("a string", "a string", "http://www.example.com/COLLATION/NOT/SUPPORTED")`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc4() {
    final XQuery query = new XQuery(
      "deep-equal(\"a string\", \"a string\", \"http://www.example.com/COLLATION/NOT/SUPPORTED\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOCH0002")
    );
  }

  /**
   *  A test whose essence is: `deep-equal((1, (), 1), (1, (), 1))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc40() {
    final XQuery query = new XQuery(
      "deep-equal((1, (), 1), (1, (), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal(((), 1, 1), ((), 1, 1))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc41() {
    final XQuery query = new XQuery(
      "deep-equal(((), 1, 1), ((), 1, 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(deep-equal((1, 1, ()), (1, 1, 1)))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc42() {
    final XQuery query = new XQuery(
      "not(deep-equal((1, 1, ()), (1, 1, 1)))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(deep-equal((1, (), 1), (1, 1, 1)))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc43() {
    final XQuery query = new XQuery(
      "not(deep-equal((1, (), 1), (1, 1, 1)))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(deep-equal(((), 1, 1), (1, 1, 1)))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc44() {
    final XQuery query = new XQuery(
      "not(deep-equal(((), 1, 1), (1, 1, 1)))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(deep-equal((1, 1, 1), (1, 1, ())))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc45() {
    final XQuery query = new XQuery(
      "not(deep-equal((1, 1, 1), (1, 1, ())))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(deep-equal((1, 1, 1), (1, (), 1)))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc46() {
    final XQuery query = new XQuery(
      "not(deep-equal((1, 1, 1), (1, (), 1)))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(deep-equal((1, 1, 1), ((), 1, 1)))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc47() {
    final XQuery query = new XQuery(
      "not(deep-equal((1, 1, 1), ((), 1, 1)))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(deep-equal((1, 1), (1, 1, 1)))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc48() {
    final XQuery query = new XQuery(
      "not(deep-equal((1, 1), (1, 1, 1)))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(deep-equal((1, 1, 1), (1, 1)))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc49() {
    final XQuery query = new XQuery(
      "not(deep-equal((1, 1, 1), (1, 1)))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal("a string", "a string", ())`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc5() {
    final XQuery query = new XQuery(
      "deep-equal(\"a string\", \"a string\", ())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  A test whose essence is: `not(deep-equal((), (1, 1, 1)))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc50() {
    final XQuery query = new XQuery(
      "not(deep-equal((), (1, 1, 1)))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(deep-equal((1, 1, 1), ()))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc51() {
    final XQuery query = new XQuery(
      "not(deep-equal((1, 1, 1), ()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal((), reverse(0 to -5))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc52() {
    final XQuery query = new XQuery(
      "deep-equal((), reverse(0 to -5))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal((), 0 to -5)`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc53() {
    final XQuery query = new XQuery(
      "deep-equal((), 0 to -5)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal(reverse(0 to -5), ())`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc54() {
    final XQuery query = new XQuery(
      "deep-equal(reverse(0 to -5), ())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal(0 to -5, ())`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc55() {
    final XQuery query = new XQuery(
      "deep-equal(0 to -5, ())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal("a string", "a string", "http://www.w3.org/2005/xpath-functions/collation/codepoint")`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc6() {
    final XQuery query = new XQuery(
      "deep-equal(\"a string\", \"a string\", \"http://www.w3.org/2005/xpath-functions/collation/codepoint\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal((), ()) eq true()`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc7() {
    final XQuery query = new XQuery(
      "deep-equal((), ()) eq true()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal(xs:float("NaN"), xs:float("NaN"))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc8() {
    final XQuery query = new XQuery(
      "deep-equal(xs:float(\"NaN\"), xs:float(\"NaN\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal(xs:double("NaN"), xs:double("NaN"))`. .
   */
  @org.junit.Test
  public void kSeqDeepEqualFunc9() {
    final XQuery query = new XQuery(
      "deep-equal(xs:double(\"NaN\"), xs:double(\"NaN\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Two date/time values that never will compare equal. .
   */
  @org.junit.Test
  public void k2SeqDeepEqualFunc1() {
    final XQuery query = new XQuery(
      "not(deep-equal(current-time(), current-date()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  fn:deep-equal() with two sequence of nodes. .
   */
  @org.junit.Test
  public void k2SeqDeepEqualFunc10() {
    final XQuery query = new XQuery(
      "deep-equal((<a/>, <b/>, <c/>), (<a/>, <b/>, <c/>))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  fn:deep-equal() with two sequence of nodes(#2). .
   */
  @org.junit.Test
  public void k2SeqDeepEqualFunc11() {
    final XQuery query = new XQuery(
      "deep-equal((<a/>, <b/>, <c/>), (<a/>, <b/>, <a/>))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  fn:deep-equal() with two sequence of nodes(#3). .
   */
  @org.junit.Test
  public void k2SeqDeepEqualFunc12() {
    final XQuery query = new XQuery(
      "deep-equal((<a/>, <b/>), (<a/>, <b/>, <c/>))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  fn:deep-equal() with two sequence of nodes(#4). .
   */
  @org.junit.Test
  public void k2SeqDeepEqualFunc13() {
    final XQuery query = new XQuery(
      "deep-equal((<a/>, <b/>, <c/>), (<a/>, <b/>))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Ensure processing instructions are ignored if children of a document node. .
   */
  @org.junit.Test
  public void k2SeqDeepEqualFunc14() {
    final XQuery query = new XQuery(
      "declare variable $d1 := document { <?target data?>, text{\"some text\"}}; declare variable $d2 := document {text{\"some text\"}}; deep-equal($d1, $d2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Ensure processing instructions are ignored if children of a document node(#2). .
   */
  @org.junit.Test
  public void k2SeqDeepEqualFunc15() {
    final XQuery query = new XQuery(
      "declare variable $d1 := document {()}; declare variable $d2 := document {<?target data?>}; deep-equal($d1, $d2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Ensure processing instructions are ignored if children of a document node. .
   */
  @org.junit.Test
  public void k2SeqDeepEqualFunc16() {
    final XQuery query = new XQuery(
      "declare variable $d1 := document { <?target data?>, text{\"some text\"}}; declare variable $d2 := document {text{\"some text\"}}; deep-equal($d1, $d2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Ensure processing instructions are ignored if children of a document node(#2). .
   */
  @org.junit.Test
  public void k2SeqDeepEqualFunc17() {
    final XQuery query = new XQuery(
      "declare variable $d1 := document {()}; declare variable $d2 := document {<?target data?>}; deep-equal($d1, $d2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  fn:deep-equal() with mixed content. .
   */
  @org.junit.Test
  public void k2SeqDeepEqualFunc18() {
    final XQuery query = new XQuery(
      "deep-equal(<e>1</e>, 1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  fn:deep-equal() with mixed content(#2). .
   */
  @org.junit.Test
  public void k2SeqDeepEqualFunc19() {
    final XQuery query = new XQuery(
      "deep-equal(1, <e>1</e>)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  A date and node value that never will compare equal. .
   */
  @org.junit.Test
  public void k2SeqDeepEqualFunc2() {
    final XQuery query = new XQuery(
      "deep-equal(current-time(), <e/>)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  One of the operands has two text nodes, and hence it evaluate to false. .
   */
  @org.junit.Test
  public void k2SeqDeepEqualFunc20() {
    final XQuery query = new XQuery(
      "declare variable $d1 := <e a=\"1\" b=\"2\">te<?target data?>xt</e>; declare variable $d2 := <e b=\"2\" a=\"1\">text</e>; deep-equal($d1, $d2), deep-equal($d2, $d1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "false false")
    );
  }

  /**
   *  Processing instructions inside elements are ignored. .
   */
  @org.junit.Test
  public void k2SeqDeepEqualFunc21() {
    final XQuery query = new XQuery(
      "declare variable $d1 := <e a=\"1\" b=\"2\"><?target data?>text</e>; declare variable $d2 := <e b=\"2\" a=\"1\">text</e>; deep-equal($d1, $d2), deep-equal($d2, $d1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "true true")
    );
  }

  /**
   *  One of the operands has two text nodes besides the comment, and hence it evaluate to false. .
   */
  @org.junit.Test
  public void k2SeqDeepEqualFunc22() {
    final XQuery query = new XQuery(
      "declare variable $d1 := <e a=\"1\" b=\"2\">te<!-- content -->xt</e>; declare variable $d2 := <e b=\"2\" a=\"1\">text</e>; deep-equal($d1, $d2), deep-equal($d2, $d1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "false false")
    );
  }

  /**
   *  Comments inside elements are ignored. .
   */
  @org.junit.Test
  public void k2SeqDeepEqualFunc23() {
    final XQuery query = new XQuery(
      "declare variable $d1 := <e a=\"1\" b=\"2\"><!-- content -->text</e>; declare variable $d2 := <e b=\"2\" a=\"1\">text</e>; deep-equal($d1, $d2), deep-equal($d2, $d1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "true true")
    );
  }

  /**
   *  Compare attribute nodes. .
   */
  @org.junit.Test
  public void k2SeqDeepEqualFunc24() {
    final XQuery query = new XQuery(
      "deep-equal(attribute name {\"content\"}, attribute name {\"content\"})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Compare attribute nodes in a weird order. .
   */
  @org.junit.Test
  public void k2SeqDeepEqualFunc25() {
    final XQuery query = new XQuery(
      "deep-equal((attribute name2 {\"content\"}, attribute name {\"content\"}), (attribute name {\"content\"}, attribute name2 {\"content\"}))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Compare operands with attributes nodes of different size. .
   */
  @org.junit.Test
  public void k2SeqDeepEqualFunc26() {
    final XQuery query = new XQuery(
      "deep-equal((attribute name {\"content\"}, attribute name {\"content\"}), (attribute name {\"content\"}))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Compare operands with attributes nodes of different size(#2). .
   */
  @org.junit.Test
  public void k2SeqDeepEqualFunc27() {
    final XQuery query = new XQuery(
      "deep-equal((attribute name {\"content\"}, attribute name {\"content\"}), attribute name {\"content\"})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test deep-equalness of two attribute nodes. .
   */
  @org.junit.Test
  public void k2SeqDeepEqualFunc28() {
    final XQuery query = new XQuery(
      "deep-equal(attribute name {}, attribute name {})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test deep-equalness of two attribute nodes. .
   */
  @org.junit.Test
  public void k2SeqDeepEqualFunc29() {
    final XQuery query = new XQuery(
      "deep-equal(attribute name {\"content\"}, attribute name {\"content\"})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A date and node value that never will compare equal(#2). .
   */
  @org.junit.Test
  public void k2SeqDeepEqualFunc3() {
    final XQuery query = new XQuery(
      "deep-equal(<e/>, current-time())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test deep-equalness of two attribute nodes that has the same name, but different string values. .
   */
  @org.junit.Test
  public void k2SeqDeepEqualFunc30() {
    final XQuery query = new XQuery(
      "deep-equal(attribute name {\"content\"}, attribute name {})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test deep-equalness of two attribute nodes that has same text content but different names. .
   */
  @org.junit.Test
  public void k2SeqDeepEqualFunc31() {
    final XQuery query = new XQuery(
      "deep-equal(attribute name {}, attribute name2 {})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test deep-equalness of two attribute nodes that has identical text content but different names. .
   */
  @org.junit.Test
  public void k2SeqDeepEqualFunc32() {
    final XQuery query = new XQuery(
      "deep-equal(attribute name {\"content \"}, attribute name2 {\"content \"})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Time values that never will compare equal. .
   */
  @org.junit.Test
  public void k2SeqDeepEqualFunc4() {
    final XQuery query = new XQuery(
      "deep-equal((current-time(), current-time(), current-time()), (current-time(), current-time()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Comparing mixed content. .
   */
  @org.junit.Test
  public void k2SeqDeepEqualFunc5() {
    final XQuery query = new XQuery(
      "deep-equal((1, <e/>, 2), (1, <e/>, 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Comparing mixed content(#2). .
   */
  @org.junit.Test
  public void k2SeqDeepEqualFunc6() {
    final XQuery query = new XQuery(
      "deep-equal((1, <a/>, 2), (1, <b/>, 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Comparing mixed content(#3). .
   */
  @org.junit.Test
  public void k2SeqDeepEqualFunc7() {
    final XQuery query = new XQuery(
      "deep-equal((<a/>, <b/>, 4), (<a/>, <b/>, 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  fn:deep-equal() takes two arguments, not zero. .
   */
  @org.junit.Test
  public void k2SeqDeepEqualFunc8() {
    final XQuery query = new XQuery(
      "deep-equal()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  fn:deep-equal() takes two arguments, not one. .
   */
  @org.junit.Test
  public void k2SeqDeepEqualFunc9() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 3))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  The order of elements in sequence is important .
   */
  @org.junit.Test
  public void fnDeepEqualMixArgs001() {
    final XQuery query = new XQuery(
      "fn:deep-equal( (1,2) , (2,1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Two sequences are fn:deep-equal if items have same value and same order .
   */
  @org.junit.Test
  public void fnDeepEqualMixArgs002() {
    final XQuery query = new XQuery(
      "fn:deep-equal( (1,2) , (1,2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  comparing arguments of type string .
   */
  @org.junit.Test
  public void fnDeepEqualMixArgs003() {
    final XQuery query = new XQuery(
      "fn:deep-equal( xs:string(\"A\") , \"A\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  arg of type string, but case is different .
   */
  @org.junit.Test
  public void fnDeepEqualMixArgs004() {
    final XQuery query = new XQuery(
      "fn:deep-equal( xs:string(\"A\") , \"a\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  arg1: string, arg2: sequence with 1 element of type string .
   */
  @org.junit.Test
  public void fnDeepEqualMixArgs005() {
    final XQuery query = new XQuery(
      "fn:deep-equal( xs:string(\"A\") , (\"A\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Comparing null strings .
   */
  @org.junit.Test
  public void fnDeepEqualMixArgs006() {
    final XQuery query = new XQuery(
      "fn:deep-equal( xs:string(\"\") , (\"\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Comparing null sequences .
   */
  @org.junit.Test
  public void fnDeepEqualMixArgs007() {
    final XQuery query = new XQuery(
      "fn:deep-equal( () , ())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Comparing nested null & a null sequence .
   */
  @org.junit.Test
  public void fnDeepEqualMixArgs008() {
    final XQuery query = new XQuery(
      "fn:deep-equal( (()) , ())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Comparing arg1: sequence with a space with agr2: empty sequence .
   */
  @org.junit.Test
  public void fnDeepEqualMixArgs009() {
    final XQuery query = new XQuery(
      "fn:deep-equal( ( ) , ())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Comparing strings with different value .
   */
  @org.junit.Test
  public void fnDeepEqualMixArgs010() {
    final XQuery query = new XQuery(
      "fn:deep-equal(xs:string(\"abc\"), xs:string(\"cba\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Comparing arg1: anyURI, arg2: string .
   */
  @org.junit.Test
  public void fnDeepEqualMixArgs011() {
    final XQuery query = new XQuery(
      "fn:deep-equal( xs:anyURI(\"www.example.com\") , \"www.example.com\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Comparing arg1:anyURI and arg2:string .
   */
  @org.junit.Test
  public void fnDeepEqualMixArgs012() {
    final XQuery query = new XQuery(
      "fn:deep-equal( xs:anyURI(\"www.example.com\") , xs:string(\"www.example.com\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Comparing arg1:integer and arg2:decimal .
   */
  @org.junit.Test
  public void fnDeepEqualMixArgs013() {
    final XQuery query = new XQuery(
      "fn:deep-equal( xs:integer(1) , xs:decimal(1.0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Comapring integer args with different values .
   */
  @org.junit.Test
  public void fnDeepEqualMixArgs014() {
    final XQuery query = new XQuery(
      "fn:deep-equal( xs:integer(1) , xs:integer(-1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Comparing arg1:integer and arg2:float .
   */
  @org.junit.Test
  public void fnDeepEqualMixArgs015() {
    final XQuery query = new XQuery(
      "fn:deep-equal( xs:integer(1) , xs:float(1.0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Comparing arg1:integer, arg2:double .
   */
  @org.junit.Test
  public void fnDeepEqualMixArgs016() {
    final XQuery query = new XQuery(
      "fn:deep-equal( xs:integer(1) , xs:double(1.0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Comparing arg1:decimal , arg2:float .
   */
  @org.junit.Test
  public void fnDeepEqualMixArgs017() {
    final XQuery query = new XQuery(
      "fn:deep-equal( xs:decimal(1.01) , xs:float(1.01))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Comparing arg1:decimal, arg2:double .
   */
  @org.junit.Test
  public void fnDeepEqualMixArgs018() {
    final XQuery query = new XQuery(
      "fn:deep-equal( xs:decimal(1.01) , xs:double(1.01))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Comparing arg1:float , arg2:double .
   */
  @org.junit.Test
  public void fnDeepEqualMixArgs019() {
    final XQuery query = new XQuery(
      "fn:deep-equal( xs:float(1.01) , xs:double(1.01))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Comparing arg1:float, arg2:double values INF .
   */
  @org.junit.Test
  public void fnDeepEqualMixArgs020() {
    final XQuery query = new XQuery(
      "fn:deep-equal( xs:float(\"INF\") , xs:double(\"INF\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Comparing arg1:float, arg2:double values -INF .
   */
  @org.junit.Test
  public void fnDeepEqualMixArgs021() {
    final XQuery query = new XQuery(
      "fn:deep-equal( xs:float(\"-INF\") , xs:double(\"-INF\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Comparing arg1:float, arg2:double values NaN .
   */
  @org.junit.Test
  public void fnDeepEqualMixArgs022() {
    final XQuery query = new XQuery(
      "fn:deep-equal( xs:float(\"NaN\") , xs:double(\"NaN\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Comparing two boolean args .
   */
  @org.junit.Test
  public void fnDeepEqualMixArgs023() {
    final XQuery query = new XQuery(
      "fn:deep-equal( xs:boolean(\"1\") , xs:boolean(\"true\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Comparing two boolean args .
   */
  @org.junit.Test
  public void fnDeepEqualMixArgs024() {
    final XQuery query = new XQuery(
      "fn:deep-equal( xs:boolean(\"true\") , xs:boolean(\"true\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Comparing arg1:boolean arg2:value returned by true fn .
   */
  @org.junit.Test
  public void fnDeepEqualMixArgs025() {
    final XQuery query = new XQuery(
      "fn:deep-equal( xs:boolean(\"true\") , true())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Comparing two boolean args .
   */
  @org.junit.Test
  public void fnDeepEqualMixArgs026() {
    final XQuery query = new XQuery(
      "fn:deep-equal( xs:boolean(\"0\") , xs:boolean(\"false\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Comparing arg1:boolean arg2:value of false fn .
   */
  @org.junit.Test
  public void fnDeepEqualMixArgs027() {
    final XQuery query = new XQuery(
      "fn:deep-equal( xs:boolean(\"false\") , false())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Comparing arg1:date, arg2: string .
   */
  @org.junit.Test
  public void fnDeepEqualMixArgs028() {
    final XQuery query = new XQuery(
      "fn:deep-equal( xs:date(\"1993-03-31\") , \"1993-03-31\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Comparing arg1:date, arg2: string .
   */
  @org.junit.Test
  public void fnDeepEqualMixArgs029() {
    final XQuery query = new XQuery(
      "fn:deep-equal( xs:date(\"1993-03-31\") , xs:string(\"1993-03-31\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Comparing arg1:dateTime, arg2: string .
   */
  @org.junit.Test
  public void fnDeepEqualMixArgs030() {
    final XQuery query = new XQuery(
      "fn:deep-equal(xs:dateTime(\"1972-12-31T00:00:00\"), \"1972-12-31T00:00:00\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Comparing arg1:time, arg2: string .
   */
  @org.junit.Test
  public void fnDeepEqualMixArgs031() {
    final XQuery query = new XQuery(
      "fn:deep-equal(xs:time(\"12:30:00\"), \"12:30:00\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Apply deep-equal to nodes .
   */
  @org.junit.Test
  public void fnDeepEqualNodeArgs1() {
    final XQuery query = new XQuery(
      "fn:deep-equal(<a> {/bib/node()} </a>/node(), <b> {/bib/node()} </b>/node() )",
      ctx);
    query.context(node(file("docs/bib.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Apply deep-equal to nodes .
   */
  @org.junit.Test
  public void fnDeepEqualNodeArgs2() {
    final XQuery query = new XQuery(
      "fn:deep-equal(<a> {/bib/node(), /bib/node()} </a>/node(), <b> {/bib/node(), <difference/>, /bib/node()} </b>/node() )",
      ctx);
    query.context(node(file("docs/bib.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Apply deep-equal to nodes .
   */
  @org.junit.Test
  public void fnDeepEqualNodeArgs3() {
    final XQuery query = new XQuery(
      "fn:deep-equal(<a> {/node(), /node()} </a>/node(), <b> {/node(), 'difference', /node()} </b>/node() )",
      ctx);
    query.context(node(file("docs/bib.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Apply deep-equal to nodes .
   */
  @org.junit.Test
  public void fnDeepEqualNodeArgs4() {
    final XQuery query = new XQuery(
      "fn:deep-equal(<a> {/node(), <diff x='1'/>, /node()} </a>/node(), <b> {/node(), <diff x='2'/>, /node()} </b>/node() )",
      ctx);
    query.context(node(file("docs/bib.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Apply deep-equal to nodes .
   */
  @org.junit.Test
  public void fnDeepEqualNodeArgs5() {
    final XQuery query = new XQuery(
      "fn:deep-equal(<a> {/node(), <diff x='1'/>, /node()} </a>/node(), <b> {/node(), <diff xx='1'/>, /node()} </b>/node() )",
      ctx);
    query.context(node(file("docs/bib.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:double(lower bound) $parameter2 = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnDeepEqualdbl2args1() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:double(\"-1.7976931348623157E308\")),(xs:double(\"-1.7976931348623157E308\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:double(mid range) $parameter2 = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnDeepEqualdbl2args2() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:double(\"0\")),(xs:double(\"-1.7976931348623157E308\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:double(upper bound) $parameter2 = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnDeepEqualdbl2args3() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:double(\"1.7976931348623157E308\")),(xs:double(\"-1.7976931348623157E308\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:double(lower bound) $parameter2 = xs:double(mid range) .
   */
  @org.junit.Test
  public void fnDeepEqualdbl2args4() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:double(\"-1.7976931348623157E308\")),(xs:double(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:double(lower bound) $parameter2 = xs:double(upper bound) .
   */
  @org.junit.Test
  public void fnDeepEqualdbl2args5() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:double(\"-1.7976931348623157E308\")),(xs:double(\"1.7976931348623157E308\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:decimal(lower bound) $parameter2 = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnDeepEqualdec2args1() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:decimal(\"-999999999999999999\")),(xs:decimal(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:decimal(mid range) $parameter2 = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnDeepEqualdec2args2() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:decimal(\"617375191608514839\")),(xs:decimal(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:decimal(upper bound) $parameter2 = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnDeepEqualdec2args3() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:decimal(\"999999999999999999\")),(xs:decimal(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:decimal(lower bound) $parameter2 = xs:decimal(mid range) .
   */
  @org.junit.Test
  public void fnDeepEqualdec2args4() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:decimal(\"-999999999999999999\")),(xs:decimal(\"617375191608514839\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:decimal(lower bound) $parameter2 = xs:decimal(upper bound) .
   */
  @org.junit.Test
  public void fnDeepEqualdec2args5() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:decimal(\"-999999999999999999\")),(xs:decimal(\"999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:float(lower bound) $parameter2 = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnDeepEqualflt2args1() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:float(\"-3.4028235E38\")),(xs:float(\"-3.4028235E38\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:float(mid range) $parameter2 = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnDeepEqualflt2args2() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:float(\"0\")),(xs:float(\"-3.4028235E38\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:float(upper bound) $parameter2 = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnDeepEqualflt2args3() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:float(\"3.4028235E38\")),(xs:float(\"-3.4028235E38\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:float(lower bound) $parameter2 = xs:float(mid range) .
   */
  @org.junit.Test
  public void fnDeepEqualflt2args4() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:float(\"-3.4028235E38\")),(xs:float(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:float(lower bound) $parameter2 = xs:float(upper bound) .
   */
  @org.junit.Test
  public void fnDeepEqualflt2args5() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:float(\"-3.4028235E38\")),(xs:float(\"3.4028235E38\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:int(lower bound) $parameter2 = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnDeepEqualint2args1() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:int(\"-2147483648\")),(xs:int(\"-2147483648\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:int(mid range) $parameter2 = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnDeepEqualint2args2() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:int(\"-1873914410\")),(xs:int(\"-2147483648\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:int(upper bound) $parameter2 = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnDeepEqualint2args3() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:int(\"2147483647\")),(xs:int(\"-2147483648\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:int(lower bound) $parameter2 = xs:int(mid range) .
   */
  @org.junit.Test
  public void fnDeepEqualint2args4() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:int(\"-2147483648\")),(xs:int(\"-1873914410\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:int(lower bound) $parameter2 = xs:int(upper bound) .
   */
  @org.junit.Test
  public void fnDeepEqualint2args5() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:int(\"-2147483648\")),(xs:int(\"2147483647\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:integer(lower bound) $parameter2 = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnDeepEqualintg2args1() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:integer(\"-999999999999999999\")),(xs:integer(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:integer(mid range) $parameter2 = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnDeepEqualintg2args2() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:integer(\"830993497117024304\")),(xs:integer(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:integer(upper bound) $parameter2 = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnDeepEqualintg2args3() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:integer(\"999999999999999999\")),(xs:integer(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:integer(lower bound) $parameter2 = xs:integer(mid range) .
   */
  @org.junit.Test
  public void fnDeepEqualintg2args4() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:integer(\"-999999999999999999\")),(xs:integer(\"830993497117024304\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:integer(lower bound) $parameter2 = xs:integer(upper bound) .
   */
  @org.junit.Test
  public void fnDeepEqualintg2args5() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:integer(\"-999999999999999999\")),(xs:integer(\"999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:long(lower bound) $parameter2 = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnDeepEquallng2args1() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:long(\"-92233720368547758\")),(xs:long(\"-92233720368547758\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:long(mid range) $parameter2 = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnDeepEquallng2args2() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:long(\"-47175562203048468\")),(xs:long(\"-92233720368547758\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:long(upper bound) $parameter2 = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnDeepEquallng2args3() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:long(\"92233720368547758\")),(xs:long(\"-92233720368547758\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:long(lower bound) $parameter2 = xs:long(mid range) .
   */
  @org.junit.Test
  public void fnDeepEquallng2args4() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:long(\"-92233720368547758\")),(xs:long(\"-47175562203048468\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:long(lower bound) $parameter2 = xs:long(upper bound) .
   */
  @org.junit.Test
  public void fnDeepEquallng2args5() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:long(\"-92233720368547758\")),(xs:long(\"92233720368547758\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:negativeInteger(lower bound) $parameter2 = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnDeepEqualnint2args1() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:negativeInteger(\"-999999999999999999\")),(xs:negativeInteger(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:negativeInteger(mid range) $parameter2 = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnDeepEqualnint2args2() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:negativeInteger(\"-297014075999096793\")),(xs:negativeInteger(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:negativeInteger(upper bound) $parameter2 = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnDeepEqualnint2args3() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:negativeInteger(\"-1\")),(xs:negativeInteger(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:negativeInteger(lower bound) $parameter2 = xs:negativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnDeepEqualnint2args4() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:negativeInteger(\"-999999999999999999\")),(xs:negativeInteger(\"-297014075999096793\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:negativeInteger(lower bound) $parameter2 = xs:negativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnDeepEqualnint2args5() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:negativeInteger(\"-999999999999999999\")),(xs:negativeInteger(\"-1\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:nonNegativeInteger(lower bound) $parameter2 = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnDeepEqualnni2args1() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:nonNegativeInteger(\"0\")),(xs:nonNegativeInteger(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:nonNegativeInteger(mid range) $parameter2 = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnDeepEqualnni2args2() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:nonNegativeInteger(\"303884545991464527\")),(xs:nonNegativeInteger(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:nonNegativeInteger(upper bound) $parameter2 = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnDeepEqualnni2args3() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:nonNegativeInteger(\"999999999999999999\")),(xs:nonNegativeInteger(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:nonNegativeInteger(lower bound) $parameter2 = xs:nonNegativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnDeepEqualnni2args4() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:nonNegativeInteger(\"0\")),(xs:nonNegativeInteger(\"303884545991464527\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:nonNegativeInteger(lower bound) $parameter2 = xs:nonNegativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnDeepEqualnni2args5() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:nonNegativeInteger(\"0\")),(xs:nonNegativeInteger(\"999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:nonPositiveInteger(lower bound) $parameter2 = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnDeepEqualnpi2args1() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:nonPositiveInteger(\"-999999999999999999\")),(xs:nonPositiveInteger(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:nonPositiveInteger(mid range) $parameter2 = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnDeepEqualnpi2args2() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:nonPositiveInteger(\"-475688437271870490\")),(xs:nonPositiveInteger(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:nonPositiveInteger(upper bound) $parameter2 = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnDeepEqualnpi2args3() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:nonPositiveInteger(\"0\")),(xs:nonPositiveInteger(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:nonPositiveInteger(lower bound) $parameter2 = xs:nonPositiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnDeepEqualnpi2args4() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:nonPositiveInteger(\"-999999999999999999\")),(xs:nonPositiveInteger(\"-475688437271870490\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:nonPositiveInteger(lower bound) $parameter2 = xs:nonPositiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnDeepEqualnpi2args5() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:nonPositiveInteger(\"-999999999999999999\")),(xs:nonPositiveInteger(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:positiveInteger(lower bound) $parameter2 = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnDeepEqualpint2args1() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:positiveInteger(\"1\")),(xs:positiveInteger(\"1\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:positiveInteger(mid range) $parameter2 = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnDeepEqualpint2args2() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:positiveInteger(\"52704602390610033\")),(xs:positiveInteger(\"1\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:positiveInteger(upper bound) $parameter2 = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnDeepEqualpint2args3() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:positiveInteger(\"999999999999999999\")),(xs:positiveInteger(\"1\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:positiveInteger(lower bound) $parameter2 = xs:positiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnDeepEqualpint2args4() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:positiveInteger(\"1\")),(xs:positiveInteger(\"52704602390610033\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:positiveInteger(lower bound) $parameter2 = xs:positiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnDeepEqualpint2args5() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:positiveInteger(\"1\")),(xs:positiveInteger(\"999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:short(lower bound) $parameter2 = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnDeepEqualsht2args1() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:short(\"-32768\")),(xs:short(\"-32768\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:short(mid range) $parameter2 = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnDeepEqualsht2args2() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:short(\"-5324\")),(xs:short(\"-32768\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:short(upper bound) $parameter2 = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnDeepEqualsht2args3() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:short(\"32767\")),(xs:short(\"-32768\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:short(lower bound) $parameter2 = xs:short(mid range) .
   */
  @org.junit.Test
  public void fnDeepEqualsht2args4() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:short(\"-32768\")),(xs:short(\"-5324\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:short(lower bound) $parameter2 = xs:short(upper bound) .
   */
  @org.junit.Test
  public void fnDeepEqualsht2args5() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:short(\"-32768\")),(xs:short(\"32767\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:unsignedLong(lower bound) $parameter2 = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnDeepEqualulng2args1() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:unsignedLong(\"0\")),(xs:unsignedLong(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:unsignedLong(mid range) $parameter2 = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnDeepEqualulng2args2() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:unsignedLong(\"130747108607674654\")),(xs:unsignedLong(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:unsignedLong(upper bound) $parameter2 = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnDeepEqualulng2args3() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:unsignedLong(\"184467440737095516\")),(xs:unsignedLong(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:unsignedLong(lower bound) $parameter2 = xs:unsignedLong(mid range) .
   */
  @org.junit.Test
  public void fnDeepEqualulng2args4() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:unsignedLong(\"0\")),(xs:unsignedLong(\"130747108607674654\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:unsignedLong(lower bound) $parameter2 = xs:unsignedLong(upper bound) .
   */
  @org.junit.Test
  public void fnDeepEqualulng2args5() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:unsignedLong(\"0\")),(xs:unsignedLong(\"184467440737095516\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:unsignedShort(lower bound) $parameter2 = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnDeepEqualusht2args1() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:unsignedShort(\"0\")),(xs:unsignedShort(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:unsignedShort(mid range) $parameter2 = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnDeepEqualusht2args2() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:unsignedShort(\"44633\")),(xs:unsignedShort(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:unsignedShort(upper bound) $parameter2 = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnDeepEqualusht2args3() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:unsignedShort(\"65535\")),(xs:unsignedShort(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:unsignedShort(lower bound) $parameter2 = xs:unsignedShort(mid range) .
   */
  @org.junit.Test
  public void fnDeepEqualusht2args4() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:unsignedShort(\"0\")),(xs:unsignedShort(\"44633\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "deep-equal" function with the arguments set as follows: $parameter1 = xs:unsignedShort(lower bound) $parameter2 = xs:unsignedShort(upper bound) .
   */
  @org.junit.Test
  public void fnDeepEqualusht2args5() {
    final XQuery query = new XQuery(
      "fn:deep-equal((xs:unsignedShort(\"0\")),(xs:unsignedShort(\"65535\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }
}
