package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the min() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnMin extends QT3TestSet {

  /**
   *  A test whose essence is: `min()`. .
   */
  @org.junit.Test
  public void kSeqMINFunc1() {
    final XQuery query = new XQuery(
      "min()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `min(xs:untypedAtomic("3")) eq 3`. .
   */
  @org.junit.Test
  public void kSeqMINFunc10() {
    final XQuery query = new XQuery(
      "min(xs:untypedAtomic(\"3\")) eq 3",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `min((xs:untypedAtomic("1"), 3, 2)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kSeqMINFunc11() {
    final XQuery query = new XQuery(
      "min((xs:untypedAtomic(\"1\"), 3, 2)) instance of xs:double",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `min((3, xs:float(2), xs:untypedAtomic("1"))) eq 1`. .
   */
  @org.junit.Test
  public void kSeqMINFunc12() {
    final XQuery query = new XQuery(
      "min((3, xs:float(2), xs:untypedAtomic(\"1\"))) eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `min((3, xs:float(2), xs:untypedAtomic("1"))) instance of xs:double`. .
   */
  @org.junit.Test
  public void kSeqMINFunc13() {
    final XQuery query = new XQuery(
      "min((3, xs:float(2), xs:untypedAtomic(\"1\"))) instance of xs:double",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `min((1, xs:float(2), xs:decimal(3))) instance of xs:float`. .
   */
  @org.junit.Test
  public void kSeqMINFunc14() {
    final XQuery query = new XQuery(
      "min((1, xs:float(2), xs:decimal(3))) instance of xs:float",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `min((3, xs:untypedAtomic("1"), xs:float(2))) instance of xs:double`. .
   */
  @org.junit.Test
  public void kSeqMINFunc15() {
    final XQuery query = new XQuery(
      "min((3, xs:untypedAtomic(\"1\"), xs:float(2))) instance of xs:double",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `string(min((1, xs:untypedAtomic("NaN"), xs:float(2)))) eq "NaN"`. .
   */
  @org.junit.Test
  public void kSeqMINFunc16() {
    final XQuery query = new XQuery(
      "string(min((1, xs:untypedAtomic(\"NaN\"), xs:float(2)))) eq \"NaN\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `string(min((xs:float("NaN"), xs:untypedAtomic("3"), xs:float(2)))) eq "NaN"`. .
   */
  @org.junit.Test
  public void kSeqMINFunc17() {
    final XQuery query = new XQuery(
      "string(min((xs:float(\"NaN\"), xs:untypedAtomic(\"3\"), xs:float(2)))) eq \"NaN\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `min((xs:float("NaN"), xs:untypedAtomic("3"), xs:double(2))) instance of xs:double`. .
   */
  @org.junit.Test
  public void kSeqMINFunc18() {
    final XQuery query = new XQuery(
      "min((xs:float(\"NaN\"), xs:untypedAtomic(\"3\"), xs:double(2))) instance of xs:double",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `min((xs:float("NaN"), 1, 1, 2, xs:double("NaN"))) instance of xs:double`. .
   */
  @org.junit.Test
  public void kSeqMINFunc19() {
    final XQuery query = new XQuery(
      "min((xs:float(\"NaN\"), 1, 1, 2, xs:double(\"NaN\"))) instance of xs:double",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `min("a string", "http://www.w3.org/2005/xpath-functions/collation/codepoint", "wrong param")`. .
   */
  @org.junit.Test
  public void kSeqMINFunc2() {
    final XQuery query = new XQuery(
      "min(\"a string\", \"http://www.w3.org/2005/xpath-functions/collation/codepoint\", \"wrong param\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `min((xs:double("NaN"), 1, 1, 2, xs:float("NaN"))) instance of xs:double`. .
   */
  @org.junit.Test
  public void kSeqMINFunc20() {
    final XQuery query = new XQuery(
      "min((xs:double(\"NaN\"), 1, 1, 2, xs:float(\"NaN\"))) instance of xs:double",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  An xs:string cannot be compared to a numeric, even if a value is NaN. .
   */
  @org.junit.Test
  public void kSeqMINFunc21() {
    final XQuery query = new XQuery(
      "min((xs:float(\"NaN\"), 1, \"a string\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FORG0006")
    );
  }

  /**
   *  An xs:string cannot be compared to a numeric, even if a value is NaN. .
   */
  @org.junit.Test
  public void kSeqMINFunc22() {
    final XQuery query = new XQuery(
      "min((\"a string\", 1, xs:float(\"NaN\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FORG0006")
    );
  }

  /**
   *  A type error must be reported even if one of other values is NaN. .
   */
  @org.junit.Test
  public void kSeqMINFunc23() {
    final XQuery query = new XQuery(
      "max((xs:float(\"NaN\"), 1, xs:untypedAtomic(\"one\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FORG0001")
    );
  }

  /**
   *  A type error must be reported even if one of other values is NaN. .
   */
  @org.junit.Test
  public void kSeqMINFunc24() {
    final XQuery query = new XQuery(
      "max((xs:untypedAtomic(\"one\"), 1, xs:float(\"NaN\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FORG0001")
    );
  }

  /**
   *  A test whose essence is: `string(min((xs:double("NaN"), xs:double("NaN")))) eq "NaN"`. .
   */
  @org.junit.Test
  public void kSeqMINFunc25() {
    final XQuery query = new XQuery(
      "string(min((xs:double(\"NaN\"), xs:double(\"NaN\")))) eq \"NaN\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `string(min((xs:float("NaN"), xs:float("NaN")))) eq "NaN"`. .
   */
  @org.junit.Test
  public void kSeqMINFunc26() {
    final XQuery query = new XQuery(
      "string(min((xs:float(\"NaN\"), xs:float(\"NaN\")))) eq \"NaN\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `string(min((3, xs:double("NaN")))) eq "NaN"`. .
   */
  @org.junit.Test
  public void kSeqMINFunc27() {
    final XQuery query = new XQuery(
      "string(min((3, xs:double(\"NaN\")))) eq \"NaN\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `string(min((3, xs:float("NaN")))) eq "NaN"`. .
   */
  @org.junit.Test
  public void kSeqMINFunc28() {
    final XQuery query = new XQuery(
      "string(min((3, xs:float(\"NaN\")))) eq \"NaN\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `min((3, xs:double("NaN"))) instance of xs:double`. .
   */
  @org.junit.Test
  public void kSeqMINFunc29() {
    final XQuery query = new XQuery(
      "min((3, xs:double(\"NaN\"))) instance of xs:double",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty(min(()))`. .
   */
  @org.junit.Test
  public void kSeqMINFunc3() {
    final XQuery query = new XQuery(
      "empty(min(()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `min((3, xs:float("NaN"))) instance of xs:float`. .
   */
  @org.junit.Test
  public void kSeqMINFunc30() {
    final XQuery query = new XQuery(
      "min((3, xs:float(\"NaN\"))) instance of xs:float",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `string(min((xs:float(-3), xs:untypedAtomic("3"), xs:double("NaN")))) eq "NaN"`. .
   */
  @org.junit.Test
  public void kSeqMINFunc31() {
    final XQuery query = new XQuery(
      "string(min((xs:float(-3), xs:untypedAtomic(\"3\"), xs:double(\"NaN\")))) eq \"NaN\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `min((xs:float(-3), xs:untypedAtomic("3"), xs:double("NaN"))) instance of xs:double`. .
   */
  @org.junit.Test
  public void kSeqMINFunc32() {
    final XQuery query = new XQuery(
      "min((xs:float(-3), xs:untypedAtomic(\"3\"), xs:double(\"NaN\"))) instance of xs:double",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `string(min(xs:float("NaN"))) eq "NaN"`. .
   */
  @org.junit.Test
  public void kSeqMINFunc33() {
    final XQuery query = new XQuery(
      "string(min(xs:float(\"NaN\"))) eq \"NaN\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `string(min(xs:double("NaN"))) eq "NaN"`. .
   */
  @org.junit.Test
  public void kSeqMINFunc34() {
    final XQuery query = new XQuery(
      "string(min(xs:double(\"NaN\"))) eq \"NaN\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `min(xs:untypedAtomic("three"))`. .
   */
  @org.junit.Test
  public void kSeqMINFunc35() {
    final XQuery query = new XQuery(
      "min(xs:untypedAtomic(\"three\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FORG0001")
    );
  }

  /**
   *  A test whose essence is: `min((xs:untypedAtomic("3"), "a string"))`. .
   */
  @org.junit.Test
  public void kSeqMINFunc36() {
    final XQuery query = new XQuery(
      "min((xs:untypedAtomic(\"3\"), \"a string\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FORG0006")
    );
  }

  /**
   *  A test whose essence is: `min(("a string", xs:untypedAtomic("3")))`. .
   */
  @org.junit.Test
  public void kSeqMINFunc37() {
    final XQuery query = new XQuery(
      "min((\"a string\", xs:untypedAtomic(\"3\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FORG0006")
    );
  }

  /**
   *  A test whose essence is: `min(QName("example.com/", "ncname"))`. .
   */
  @org.junit.Test
  public void kSeqMINFunc38() {
    final XQuery query = new XQuery(
      "min(QName(\"example.com/\", \"ncname\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FORG0006")
    );
  }

  /**
   *  A test whose essence is: `min(xs:anyURI("example.com/")) eq xs:anyURI("example.com/")`. .
   */
  @org.junit.Test
  public void kSeqMINFunc39() {
    final XQuery query = new XQuery(
      "min(xs:anyURI(\"example.com/\")) eq xs:anyURI(\"example.com/\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `min((3, 3, 3, 3, 3, 3)) eq 3`. .
   */
  @org.junit.Test
  public void kSeqMINFunc4() {
    final XQuery query = new XQuery(
      "min((3, 3, 3, 3, 3, 3)) eq 3",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `min((xs:anyURI("example.com/"), xs:anyURI("example.com/"))) eq xs:anyURI("example.com/")`. .
   */
  @org.junit.Test
  public void kSeqMINFunc40() {
    final XQuery query = new XQuery(
      "min((xs:anyURI(\"example.com/\"), xs:anyURI(\"example.com/\"))) eq xs:anyURI(\"example.com/\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `min(("a string")) eq "a string"`. .
   */
  @org.junit.Test
  public void kSeqMINFunc41() {
    final XQuery query = new XQuery(
      "min((\"a string\")) eq \"a string\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `min(("a string", QName("example.com/", "ncname")))`. .
   */
  @org.junit.Test
  public void kSeqMINFunc42() {
    final XQuery query = new XQuery(
      "min((\"a string\", QName(\"example.com/\", \"ncname\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FORG0006")
    );
  }

  /**
   *  A test whose essence is: `min((5, 5.0e0)) eq 5.0e0`. .
   */
  @org.junit.Test
  public void kSeqMINFunc43() {
    final XQuery query = new XQuery(
      "min((5, 5.0e0)) eq 5.0e0",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `min((5.0e0, 5)) eq 5.0e0`. .
   */
  @org.junit.Test
  public void kSeqMINFunc44() {
    final XQuery query = new XQuery(
      "min((5.0e0, 5)) eq 5.0e0",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `min((3, 5.0e0)) eq 3`. .
   */
  @org.junit.Test
  public void kSeqMINFunc45() {
    final XQuery query = new XQuery(
      "min((3, 5.0e0)) eq 3",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `min((5.0e0, 3)) eq 3`. .
   */
  @org.junit.Test
  public void kSeqMINFunc46() {
    final XQuery query = new XQuery(
      "min((5.0e0, 3)) eq 3",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple test for min() involving xs:date. .
   */
  @org.junit.Test
  public void kSeqMINFunc47() {
    final XQuery query = new XQuery(
      "min((xs:date(\"2005-01-01\"), xs:date(\"2001-01-01\"))) eq xs:date(\"2001-01-01\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `min((3,4,5)) eq 3`. .
   */
  @org.junit.Test
  public void kSeqMINFunc48() {
    final XQuery query = new XQuery(
      "min((3,4,5)) eq 3",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `min((5, 5.0e0)) eq 5.0e0`. .
   */
  @org.junit.Test
  public void kSeqMINFunc49() {
    final XQuery query = new XQuery(
      "min((5, 5.0e0)) eq 5.0e0",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `min((3, 1, 1, 1, 1, 1)) eq 1`. .
   */
  @org.junit.Test
  public void kSeqMINFunc5() {
    final XQuery query = new XQuery(
      "min((3, 1, 1, 1, 1, 1)) eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `min((3,4, "Zero"))`. .
   */
  @org.junit.Test
  public void kSeqMINFunc50() {
    final XQuery query = new XQuery(
      "min((3,4, \"Zero\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FORG0006")
    );
  }

  /**
   *  A test whose essence is: `min((xs:float(0.0E0), xs:float(-0.0E0))) eq xs:float("-0")`. .
   */
  @org.junit.Test
  public void kSeqMINFunc51() {
    final XQuery query = new XQuery(
      "min((xs:float(0.0E0), xs:float(-0.0E0))) eq xs:float(\"-0\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `min((1, 1, 1, 1, 1, 3)) eq 1`. .
   */
  @org.junit.Test
  public void kSeqMINFunc6() {
    final XQuery query = new XQuery(
      "min((1, 1, 1, 1, 1, 3)) eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `min((3, 1, 5, 1, 1, 3)) eq 1`. .
   */
  @org.junit.Test
  public void kSeqMINFunc7() {
    final XQuery query = new XQuery(
      "min((3, 1, 5, 1, 1, 3)) eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `min((3, -5.0, 5, 1, -3, 3)) eq -5.0`. .
   */
  @org.junit.Test
  public void kSeqMINFunc8() {
    final XQuery query = new XQuery(
      "min((3, -5.0, 5, 1, -3, 3)) eq -5.0",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `min(xs:untypedAtomic("3")) instance of xs:double`. .
   */
  @org.junit.Test
  public void kSeqMINFunc9() {
    final XQuery query = new XQuery(
      "min(xs:untypedAtomic(\"3\")) instance of xs:double",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Invoke fn:min() on two identical xs:anyURI values. .
   */
  @org.junit.Test
  public void k2SeqMINFunc1() {
    final XQuery query = new XQuery(
      "min((xs:anyURI(\"http://example.com/A\"), xs:anyURI(\"http://example.com/A\"))) eq xs:anyURI(\"http://example.com/A\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  fn:min() and xs:double promotion. .
   */
  @org.junit.Test
  public void k2SeqMINFunc10() {
    final XQuery query = new XQuery(
      "min((5.0e0, 3)) instance of xs:double",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  fn:min() and xs:double promotion. .
   */
  @org.junit.Test
  public void k2SeqMINFunc11() {
    final XQuery query = new XQuery(
      "min((1, 1, 1, 1, 1.0)) instance of xs:decimal",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  fn:min() and xs:double promotion. .
   */
  @org.junit.Test
  public void k2SeqMINFunc12() {
    final XQuery query = new XQuery(
      "min((1.0, 1, 1, 1, 1)) instance of xs:decimal",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  fn:min() and xs:double promotion. .
   */
  @org.junit.Test
  public void k2SeqMINFunc13() {
    final XQuery query = new XQuery(
      "min((1.0, 1, 1.0, 1, 1)) instance of xs:decimal",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  fn:min() and xs:double promotion. .
   */
  @org.junit.Test
  public void k2SeqMINFunc14() {
    final XQuery query = new XQuery(
      "min((\"a\", \"b\", \"c\")) eq \"a\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Ensure the return type is properly inferred. .
   */
  @org.junit.Test
  public void k2SeqMINFunc15() {
    final XQuery query = new XQuery(
      "min(xs:unsignedShort(\"1\")) instance of xs:unsignedShort",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Invoke fn:min() on two different xs:anyURI values. .
   */
  @org.junit.Test
  public void k2SeqMINFunc2() {
    final XQuery query = new XQuery(
      "min((xs:anyURI(\"http://example.com/B\"), xs:anyURI(\"http://example.com/A\"))) eq xs:anyURI(\"http://example.com/A\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Invoke fn:min() on two different xs:anyURI values(#2). .
   */
  @org.junit.Test
  public void k2SeqMINFunc3() {
    final XQuery query = new XQuery(
      "min((xs:anyURI(\"http://example.com/8\"), xs:anyURI(\"http://example.com/4\"))) eq xs:anyURI(\"http://example.com/4\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Invoke fn:min() with an unsupported collation. .
   */
  @org.junit.Test
  public void k2SeqMINFunc4() {
    final XQuery query = new XQuery(
      "min((\"str1\", \"str2\"), \"http://example.com/UNSUPPORTED_COLLATION\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOCH0002")
    );
  }

  /**
   *  Invoke fn:min() with an unsupported collation and one xs:anyURI value. .
   */
  @org.junit.Test
  public void k2SeqMINFunc5() {
    final XQuery query = new XQuery(
      "min(xs:anyURI(\"str1\"), \"http://example.com/UNSUPPORTED_COLLATION\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("str1")
      ||
        error("FOCH0002")
      )
    );
  }

  /**
   *  Invoke fn:min() with too many arguments. .
   */
  @org.junit.Test
  public void k2SeqMINFunc6() {
    final XQuery query = new XQuery(
      "min((\"str1\", \"str2\"), \"http://www.w3.org/2005/xpath-functions/collation/codepoint\", ())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  fn:min() and xs:double promotion. .
   */
  @org.junit.Test
  public void k2SeqMINFunc7() {
    final XQuery query = new XQuery(
      "min((5, 5.0e0)) instance of xs:double",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  fn:min() and xs:double promotion. .
   */
  @org.junit.Test
  public void k2SeqMINFunc8() {
    final XQuery query = new XQuery(
      "min((5.0e0, 5)) instance of xs:double",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  fn:min() and xs:double promotion. .
   */
  @org.junit.Test
  public void k2SeqMINFunc9() {
    final XQuery query = new XQuery(
      "min((3, 5.0e0)) instance of xs:double",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of the fn:min function with argument to sequence of different types. .
   */
  @org.junit.Test
  public void fnMin1() {
    final XQuery query = new XQuery(
      "fn:min((3,4,\"Zero\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FORG0006")
    );
  }

  /**
   * Test fn:min on a sequence of numeric arguments.
   */
  @org.junit.Test
  public void fnMin10() {
    final XQuery query = new XQuery(
      "for $p in 1 to 4 let $x := (xs:integer(4), xs:decimal(3), xs:float(2), xs:double(1))[position() le $p] return typeswitch (min($x)) case xs:integer return \"integer\" case xs:decimal return \"decimal\" case xs:float return \"float\" case xs:double return \"double\" default return error()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "integer decimal float double")
    );
  }

  /**
   * Test fn:min on a sequence of strings.
   */
  @org.junit.Test
  public void fnMin11() {
    final XQuery query = new XQuery(
      "min((\"a\", \"b\", \"c\", \"d\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "a")
    );
  }

  /**
   * Test fn:min on a sequence of dates.
   */
  @org.junit.Test
  public void fnMin12() {
    final XQuery query = new XQuery(
      "min((xs:date('1066-10-02'), xs:date('1588-08-08'), xs:date('2011-06-29')))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1066-10-02")
    );
  }

  /**
   *  Evaluation of type promotion when using mixed typed with fn:min function .
   */
  @org.junit.Test
  public void fnMin2() {
    final XQuery query = new XQuery(
      "fn:min((xs:integer(5000000),xs:double(3e8)))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("5.0E6")
    );
  }

  /**
   * Evaluation of type promotion when using mixed typed with fn:min function (used derived types).
   */
  @org.junit.Test
  public void fnMin3() {
    final XQuery query = new XQuery(
      "let $var := fn:min((xs:long(22),xs:short(10))) return $var instance of xs:integer",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Test fn:min on a sequence of xs:dayTimeDuration arguments.
   */
  @org.junit.Test
  public void fnMin4() {
    final XQuery query = new XQuery(
      "min((xs:dayTimeDuration(\"P1D\"), xs:dayTimeDuration(\"PT2H\"))) instance of xs:dayTimeDuration",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Test fn:min on a sequence of xs:dayTimeDuration arguments.
   */
  @org.junit.Test
  public void fnMin5() {
    final XQuery query = new XQuery(
      "min(for $x in 1 to 10 return xs:dayTimeDuration(concat(\"PT\",$x,\"H\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "PT1H")
    );
  }

  /**
   * Test fn:min on a sequence of xs:yearMonthDuration arguments.
   */
  @org.junit.Test
  public void fnMin6() {
    final XQuery query = new XQuery(
      "min((xs:yearMonthDuration(\"P1Y\"), xs:yearMonthDuration(\"P1M\"))) instance of xs:yearMonthDuration",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Test fn:min on a sequence of xs:yearMonthDuration arguments.
   */
  @org.junit.Test
  public void fnMin7() {
    final XQuery query = new XQuery(
      "min(for $x in 1 to 10 return xs:yearMonthDuration(concat(\"P\",$x,\"M\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "P1M")
    );
  }

  /**
   * Test fn:min on a sequence of xs:yearMonthDuration and xs:dayTimeDuration arguments.
   */
  @org.junit.Test
  public void fnMin8() {
    final XQuery query = new XQuery(
      "min((xs:yearMonthDuration(\"P1Y\"), xs:dayTimeDuration(\"P1D\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        error("FORG0006")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   * Test fn:min on a sequence of xs:duration arguments.
   */
  @org.junit.Test
  public void fnMin9() {
    final XQuery query = new XQuery(
      "min(xs:duration(\"P1Y1M1D\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        error("FORG0006")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnMindbl1args1() {
    final XQuery query = new XQuery(
      "fn:min((xs:double(\"-1.7976931348623157E308\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg = xs:double(mid range) .
   */
  @org.junit.Test
  public void fnMindbl1args2() {
    final XQuery query = new XQuery(
      "fn:min((xs:double(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg = xs:double(upper bound) .
   */
  @org.junit.Test
  public void fnMindbl1args3() {
    final XQuery query = new XQuery(
      "fn:min((xs:double(\"1.7976931348623157E308\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:double(lower bound) $arg2 = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnMindbl2args1() {
    final XQuery query = new XQuery(
      "fn:min((xs:double(\"-1.7976931348623157E308\"),xs:double(\"-1.7976931348623157E308\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:double(mid range) $arg2 = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnMindbl2args2() {
    final XQuery query = new XQuery(
      "fn:min((xs:double(\"0\"),xs:double(\"-1.7976931348623157E308\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:double(upper bound) $arg2 = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnMindbl2args3() {
    final XQuery query = new XQuery(
      "fn:min((xs:double(\"1.7976931348623157E308\"),xs:double(\"-1.7976931348623157E308\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:double(lower bound) $arg2 = xs:double(mid range) .
   */
  @org.junit.Test
  public void fnMindbl2args4() {
    final XQuery query = new XQuery(
      "fn:min((xs:double(\"-1.7976931348623157E308\"),xs:double(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:double(lower bound) $arg2 = xs:double(upper bound) .
   */
  @org.junit.Test
  public void fnMindbl2args5() {
    final XQuery query = new XQuery(
      "fn:min((xs:double(\"-1.7976931348623157E308\"),xs:double(\"1.7976931348623157E308\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnMindec1args1() {
    final XQuery query = new XQuery(
      "fn:min((xs:decimal(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg = xs:decimal(mid range) .
   */
  @org.junit.Test
  public void fnMindec1args2() {
    final XQuery query = new XQuery(
      "fn:min((xs:decimal(\"617375191608514839\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("617375191608514839")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg = xs:decimal(upper bound) .
   */
  @org.junit.Test
  public void fnMindec1args3() {
    final XQuery query = new XQuery(
      "fn:min((xs:decimal(\"999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:decimal(lower bound) $arg2 = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnMindec2args1() {
    final XQuery query = new XQuery(
      "fn:min((xs:decimal(\"-999999999999999999\"),xs:decimal(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:decimal(mid range) $arg2 = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnMindec2args2() {
    final XQuery query = new XQuery(
      "fn:min((xs:decimal(\"617375191608514839\"),xs:decimal(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:decimal(upper bound) $arg2 = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnMindec2args3() {
    final XQuery query = new XQuery(
      "fn:min((xs:decimal(\"999999999999999999\"),xs:decimal(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:decimal(lower bound) $arg2 = xs:decimal(mid range) .
   */
  @org.junit.Test
  public void fnMindec2args4() {
    final XQuery query = new XQuery(
      "fn:min((xs:decimal(\"-999999999999999999\"),xs:decimal(\"617375191608514839\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:decimal(lower bound) $arg2 = xs:decimal(upper bound) .
   */
  @org.junit.Test
  public void fnMindec2args5() {
    final XQuery query = new XQuery(
      "fn:min((xs:decimal(\"-999999999999999999\"),xs:decimal(\"999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnMinflt1args1() {
    final XQuery query = new XQuery(
      "fn:min((xs:float(\"-3.4028235E38\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("xs:float(\"-3.4028235E38\")")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg = xs:float(mid range) .
   */
  @org.junit.Test
  public void fnMinflt1args2() {
    final XQuery query = new XQuery(
      "fn:min((xs:float(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg = xs:float(upper bound) .
   */
  @org.junit.Test
  public void fnMinflt1args3() {
    final XQuery query = new XQuery(
      "fn:min((xs:float(\"3.4028235E38\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("xs:float(\"3.4028235E38\")")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:float(lower bound) $arg2 = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnMinflt2args1() {
    final XQuery query = new XQuery(
      "fn:min((xs:float(\"-3.4028235E38\"),xs:float(\"-3.4028235E38\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("xs:float(\"-3.4028235E38\")")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:float(mid range) $arg2 = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnMinflt2args2() {
    final XQuery query = new XQuery(
      "fn:min((xs:float(\"0\"),xs:float(\"-3.4028235E38\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("xs:float(\"-3.4028235E38\")")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:float(upper bound) $arg2 = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnMinflt2args3() {
    final XQuery query = new XQuery(
      "fn:min((xs:float(\"3.4028235E38\"),xs:float(\"-3.4028235E38\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("xs:float(\"-3.4028235E38\")")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:float(lower bound) $arg2 = xs:float(mid range) .
   */
  @org.junit.Test
  public void fnMinflt2args4() {
    final XQuery query = new XQuery(
      "fn:min((xs:float(\"-3.4028235E38\"),xs:float(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("xs:float(\"-3.4028235E38\")")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:float(lower bound) $arg2 = xs:float(upper bound) .
   */
  @org.junit.Test
  public void fnMinflt2args5() {
    final XQuery query = new XQuery(
      "fn:min((xs:float(\"-3.4028235E38\"),xs:float(\"3.4028235E38\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("xs:float(\"-3.4028235E38\")")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnMinint1args1() {
    final XQuery query = new XQuery(
      "fn:min((xs:int(\"-2147483648\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-2147483648")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg = xs:int(mid range) .
   */
  @org.junit.Test
  public void fnMinint1args2() {
    final XQuery query = new XQuery(
      "fn:min((xs:int(\"-1873914410\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-1873914410")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg = xs:int(upper bound) .
   */
  @org.junit.Test
  public void fnMinint1args3() {
    final XQuery query = new XQuery(
      "fn:min((xs:int(\"2147483647\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("2147483647")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:int(lower bound) $arg2 = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnMinint2args1() {
    final XQuery query = new XQuery(
      "fn:min((xs:int(\"-2147483648\"),xs:int(\"-2147483648\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-2147483648")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:int(mid range) $arg2 = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnMinint2args2() {
    final XQuery query = new XQuery(
      "fn:min((xs:int(\"-1873914410\"),xs:int(\"-2147483648\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-2147483648")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:int(upper bound) $arg2 = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnMinint2args3() {
    final XQuery query = new XQuery(
      "fn:min((xs:int(\"2147483647\"),xs:int(\"-2147483648\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-2147483648")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:int(lower bound) $arg2 = xs:int(mid range) .
   */
  @org.junit.Test
  public void fnMinint2args4() {
    final XQuery query = new XQuery(
      "fn:min((xs:int(\"-2147483648\"),xs:int(\"-1873914410\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-2147483648")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:int(lower bound) $arg2 = xs:int(upper bound) .
   */
  @org.junit.Test
  public void fnMinint2args5() {
    final XQuery query = new XQuery(
      "fn:min((xs:int(\"-2147483648\"),xs:int(\"2147483647\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-2147483648")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnMinintg1args1() {
    final XQuery query = new XQuery(
      "fn:min((xs:integer(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg = xs:integer(mid range) .
   */
  @org.junit.Test
  public void fnMinintg1args2() {
    final XQuery query = new XQuery(
      "fn:min((xs:integer(\"830993497117024304\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("830993497117024304")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg = xs:integer(upper bound) .
   */
  @org.junit.Test
  public void fnMinintg1args3() {
    final XQuery query = new XQuery(
      "fn:min((xs:integer(\"999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:integer(lower bound) $arg2 = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnMinintg2args1() {
    final XQuery query = new XQuery(
      "fn:min((xs:integer(\"-999999999999999999\"),xs:integer(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:integer(mid range) $arg2 = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnMinintg2args2() {
    final XQuery query = new XQuery(
      "fn:min((xs:integer(\"830993497117024304\"),xs:integer(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:integer(upper bound) $arg2 = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnMinintg2args3() {
    final XQuery query = new XQuery(
      "fn:min((xs:integer(\"999999999999999999\"),xs:integer(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:integer(lower bound) $arg2 = xs:integer(mid range) .
   */
  @org.junit.Test
  public void fnMinintg2args4() {
    final XQuery query = new XQuery(
      "fn:min((xs:integer(\"-999999999999999999\"),xs:integer(\"830993497117024304\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:integer(lower bound) $arg2 = xs:integer(upper bound) .
   */
  @org.junit.Test
  public void fnMinintg2args5() {
    final XQuery query = new XQuery(
      "fn:min((xs:integer(\"-999999999999999999\"),xs:integer(\"999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnMinlng1args1() {
    final XQuery query = new XQuery(
      "fn:min((xs:long(\"-92233720368547758\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-92233720368547758")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg = xs:long(mid range) .
   */
  @org.junit.Test
  public void fnMinlng1args2() {
    final XQuery query = new XQuery(
      "fn:min((xs:long(\"-47175562203048468\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-47175562203048468")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg = xs:long(upper bound) .
   */
  @org.junit.Test
  public void fnMinlng1args3() {
    final XQuery query = new XQuery(
      "fn:min((xs:long(\"92233720368547758\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("92233720368547758")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:long(lower bound) $arg2 = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnMinlng2args1() {
    final XQuery query = new XQuery(
      "fn:min((xs:long(\"-92233720368547758\"),xs:long(\"-92233720368547758\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-92233720368547758")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:long(mid range) $arg2 = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnMinlng2args2() {
    final XQuery query = new XQuery(
      "fn:min((xs:long(\"-47175562203048468\"),xs:long(\"-92233720368547758\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-92233720368547758")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:long(upper bound) $arg2 = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnMinlng2args3() {
    final XQuery query = new XQuery(
      "fn:min((xs:long(\"92233720368547758\"),xs:long(\"-92233720368547758\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-92233720368547758")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:long(lower bound) $arg2 = xs:long(mid range) .
   */
  @org.junit.Test
  public void fnMinlng2args4() {
    final XQuery query = new XQuery(
      "fn:min((xs:long(\"-92233720368547758\"),xs:long(\"-47175562203048468\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-92233720368547758")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:long(lower bound) $arg2 = xs:long(upper bound) .
   */
  @org.junit.Test
  public void fnMinlng2args5() {
    final XQuery query = new XQuery(
      "fn:min((xs:long(\"-92233720368547758\"),xs:long(\"92233720368547758\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-92233720368547758")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnMinnint1args1() {
    final XQuery query = new XQuery(
      "fn:min((xs:negativeInteger(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg = xs:negativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnMinnint1args2() {
    final XQuery query = new XQuery(
      "fn:min((xs:negativeInteger(\"-297014075999096793\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-297014075999096793")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg = xs:negativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnMinnint1args3() {
    final XQuery query = new XQuery(
      "fn:min((xs:negativeInteger(\"-1\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-1")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:negativeInteger(lower bound) $arg2 = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnMinnint2args1() {
    final XQuery query = new XQuery(
      "fn:min((xs:negativeInteger(\"-999999999999999999\"),xs:negativeInteger(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:negativeInteger(mid range) $arg2 = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnMinnint2args2() {
    final XQuery query = new XQuery(
      "fn:min((xs:negativeInteger(\"-297014075999096793\"),xs:negativeInteger(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:negativeInteger(upper bound) $arg2 = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnMinnint2args3() {
    final XQuery query = new XQuery(
      "fn:min((xs:negativeInteger(\"-1\"),xs:negativeInteger(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:negativeInteger(lower bound) $arg2 = xs:negativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnMinnint2args4() {
    final XQuery query = new XQuery(
      "fn:min((xs:negativeInteger(\"-999999999999999999\"),xs:negativeInteger(\"-297014075999096793\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:negativeInteger(lower bound) $arg2 = xs:negativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnMinnint2args5() {
    final XQuery query = new XQuery(
      "fn:min((xs:negativeInteger(\"-999999999999999999\"),xs:negativeInteger(\"-1\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnMinnni1args1() {
    final XQuery query = new XQuery(
      "fn:min((xs:nonNegativeInteger(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg = xs:nonNegativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnMinnni1args2() {
    final XQuery query = new XQuery(
      "fn:min((xs:nonNegativeInteger(\"303884545991464527\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("303884545991464527")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg = xs:nonNegativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnMinnni1args3() {
    final XQuery query = new XQuery(
      "fn:min((xs:nonNegativeInteger(\"999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:nonNegativeInteger(lower bound) $arg2 = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnMinnni2args1() {
    final XQuery query = new XQuery(
      "fn:min((xs:nonNegativeInteger(\"0\"),xs:nonNegativeInteger(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:nonNegativeInteger(mid range) $arg2 = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnMinnni2args2() {
    final XQuery query = new XQuery(
      "fn:min((xs:nonNegativeInteger(\"303884545991464527\"),xs:nonNegativeInteger(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:nonNegativeInteger(upper bound) $arg2 = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnMinnni2args3() {
    final XQuery query = new XQuery(
      "fn:min((xs:nonNegativeInteger(\"999999999999999999\"),xs:nonNegativeInteger(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:nonNegativeInteger(lower bound) $arg2 = xs:nonNegativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnMinnni2args4() {
    final XQuery query = new XQuery(
      "fn:min((xs:nonNegativeInteger(\"0\"),xs:nonNegativeInteger(\"303884545991464527\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:nonNegativeInteger(lower bound) $arg2 = xs:nonNegativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnMinnni2args5() {
    final XQuery query = new XQuery(
      "fn:min((xs:nonNegativeInteger(\"0\"),xs:nonNegativeInteger(\"999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnMinnpi1args1() {
    final XQuery query = new XQuery(
      "fn:min((xs:nonPositiveInteger(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg = xs:nonPositiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnMinnpi1args2() {
    final XQuery query = new XQuery(
      "fn:min((xs:nonPositiveInteger(\"-475688437271870490\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-475688437271870490")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg = xs:nonPositiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnMinnpi1args3() {
    final XQuery query = new XQuery(
      "fn:min((xs:nonPositiveInteger(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:nonPositiveInteger(lower bound) $arg2 = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnMinnpi2args1() {
    final XQuery query = new XQuery(
      "fn:min((xs:nonPositiveInteger(\"-999999999999999999\"),xs:nonPositiveInteger(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:nonPositiveInteger(mid range) $arg2 = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnMinnpi2args2() {
    final XQuery query = new XQuery(
      "fn:min((xs:nonPositiveInteger(\"-475688437271870490\"),xs:nonPositiveInteger(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:nonPositiveInteger(upper bound) $arg2 = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnMinnpi2args3() {
    final XQuery query = new XQuery(
      "fn:min((xs:nonPositiveInteger(\"0\"),xs:nonPositiveInteger(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:nonPositiveInteger(lower bound) $arg2 = xs:nonPositiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnMinnpi2args4() {
    final XQuery query = new XQuery(
      "fn:min((xs:nonPositiveInteger(\"-999999999999999999\"),xs:nonPositiveInteger(\"-475688437271870490\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:nonPositiveInteger(lower bound) $arg2 = xs:nonPositiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnMinnpi2args5() {
    final XQuery query = new XQuery(
      "fn:min((xs:nonPositiveInteger(\"-999999999999999999\"),xs:nonPositiveInteger(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnMinpint1args1() {
    final XQuery query = new XQuery(
      "fn:min((xs:positiveInteger(\"1\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg = xs:positiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnMinpint1args2() {
    final XQuery query = new XQuery(
      "fn:min((xs:positiveInteger(\"52704602390610033\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("52704602390610033")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg = xs:positiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnMinpint1args3() {
    final XQuery query = new XQuery(
      "fn:min((xs:positiveInteger(\"999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:positiveInteger(lower bound) $arg2 = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnMinpint2args1() {
    final XQuery query = new XQuery(
      "fn:min((xs:positiveInteger(\"1\"),xs:positiveInteger(\"1\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:positiveInteger(mid range) $arg2 = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnMinpint2args2() {
    final XQuery query = new XQuery(
      "fn:min((xs:positiveInteger(\"52704602390610033\"),xs:positiveInteger(\"1\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:positiveInteger(upper bound) $arg2 = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnMinpint2args3() {
    final XQuery query = new XQuery(
      "fn:min((xs:positiveInteger(\"999999999999999999\"),xs:positiveInteger(\"1\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:positiveInteger(lower bound) $arg2 = xs:positiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnMinpint2args4() {
    final XQuery query = new XQuery(
      "fn:min((xs:positiveInteger(\"1\"),xs:positiveInteger(\"52704602390610033\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:positiveInteger(lower bound) $arg2 = xs:positiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnMinpint2args5() {
    final XQuery query = new XQuery(
      "fn:min((xs:positiveInteger(\"1\"),xs:positiveInteger(\"999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnMinsht1args1() {
    final XQuery query = new XQuery(
      "fn:min((xs:short(\"-32768\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-32768")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg = xs:short(mid range) .
   */
  @org.junit.Test
  public void fnMinsht1args2() {
    final XQuery query = new XQuery(
      "fn:min((xs:short(\"-5324\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-5324")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg = xs:short(upper bound) .
   */
  @org.junit.Test
  public void fnMinsht1args3() {
    final XQuery query = new XQuery(
      "fn:min((xs:short(\"32767\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("32767")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:short(lower bound) $arg2 = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnMinsht2args1() {
    final XQuery query = new XQuery(
      "fn:min((xs:short(\"-32768\"),xs:short(\"-32768\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-32768")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:short(mid range) $arg2 = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnMinsht2args2() {
    final XQuery query = new XQuery(
      "fn:min((xs:short(\"-5324\"),xs:short(\"-32768\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-32768")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:short(upper bound) $arg2 = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnMinsht2args3() {
    final XQuery query = new XQuery(
      "fn:min((xs:short(\"32767\"),xs:short(\"-32768\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-32768")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:short(lower bound) $arg2 = xs:short(mid range) .
   */
  @org.junit.Test
  public void fnMinsht2args4() {
    final XQuery query = new XQuery(
      "fn:min((xs:short(\"-32768\"),xs:short(\"-5324\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-32768")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:short(lower bound) $arg2 = xs:short(upper bound) .
   */
  @org.junit.Test
  public void fnMinsht2args5() {
    final XQuery query = new XQuery(
      "fn:min((xs:short(\"-32768\"),xs:short(\"32767\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-32768")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnMinulng1args1() {
    final XQuery query = new XQuery(
      "fn:min((xs:unsignedLong(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg = xs:unsignedLong(mid range) .
   */
  @org.junit.Test
  public void fnMinulng1args2() {
    final XQuery query = new XQuery(
      "fn:min((xs:unsignedLong(\"130747108607674654\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("130747108607674654")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg = xs:unsignedLong(upper bound) .
   */
  @org.junit.Test
  public void fnMinulng1args3() {
    final XQuery query = new XQuery(
      "fn:min((xs:unsignedLong(\"184467440737095516\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("184467440737095516")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:unsignedLong(lower bound) $arg2 = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnMinulng2args1() {
    final XQuery query = new XQuery(
      "fn:min((xs:unsignedLong(\"0\"),xs:unsignedLong(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:unsignedLong(mid range) $arg2 = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnMinulng2args2() {
    final XQuery query = new XQuery(
      "fn:min((xs:unsignedLong(\"130747108607674654\"),xs:unsignedLong(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:unsignedLong(upper bound) $arg2 = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnMinulng2args3() {
    final XQuery query = new XQuery(
      "fn:min((xs:unsignedLong(\"184467440737095516\"),xs:unsignedLong(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:unsignedLong(lower bound) $arg2 = xs:unsignedLong(mid range) .
   */
  @org.junit.Test
  public void fnMinulng2args4() {
    final XQuery query = new XQuery(
      "fn:min((xs:unsignedLong(\"0\"),xs:unsignedLong(\"130747108607674654\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:unsignedLong(lower bound) $arg2 = xs:unsignedLong(upper bound) .
   */
  @org.junit.Test
  public void fnMinulng2args5() {
    final XQuery query = new XQuery(
      "fn:min((xs:unsignedLong(\"0\"),xs:unsignedLong(\"184467440737095516\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnMinusht1args1() {
    final XQuery query = new XQuery(
      "fn:min((xs:unsignedShort(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg = xs:unsignedShort(mid range) .
   */
  @org.junit.Test
  public void fnMinusht1args2() {
    final XQuery query = new XQuery(
      "fn:min((xs:unsignedShort(\"44633\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("44633")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg = xs:unsignedShort(upper bound) .
   */
  @org.junit.Test
  public void fnMinusht1args3() {
    final XQuery query = new XQuery(
      "fn:min((xs:unsignedShort(\"65535\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("65535")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:unsignedShort(lower bound) $arg2 = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnMinusht2args1() {
    final XQuery query = new XQuery(
      "fn:min((xs:unsignedShort(\"0\"),xs:unsignedShort(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:unsignedShort(mid range) $arg2 = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnMinusht2args2() {
    final XQuery query = new XQuery(
      "fn:min((xs:unsignedShort(\"44633\"),xs:unsignedShort(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:unsignedShort(upper bound) $arg2 = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnMinusht2args3() {
    final XQuery query = new XQuery(
      "fn:min((xs:unsignedShort(\"65535\"),xs:unsignedShort(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:unsignedShort(lower bound) $arg2 = xs:unsignedShort(mid range) .
   */
  @org.junit.Test
  public void fnMinusht2args4() {
    final XQuery query = new XQuery(
      "fn:min((xs:unsignedShort(\"0\"),xs:unsignedShort(\"44633\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "min" function with the arguments set as follows: $arg1 = xs:unsignedShort(lower bound) $arg2 = xs:unsignedShort(upper bound) .
   */
  @org.junit.Test
  public void fnMinusht2args5() {
    final XQuery query = new XQuery(
      "fn:min((xs:unsignedShort(\"0\"),xs:unsignedShort(\"65535\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }
}
