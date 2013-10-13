package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the max() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnMax extends QT3TestSet {

  /**
   *  A test whose essence is: `max()`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc1() {
    final XQuery query = new XQuery(
      "max()",
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
   *  A test whose essence is: `max(xs:untypedAtomic("3")) eq 3`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc10() {
    final XQuery query = new XQuery(
      "max(xs:untypedAtomic(\"3\")) eq 3",
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
   *  A test whose essence is: `max((xs:untypedAtomic("3"), 1, 2)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc11() {
    final XQuery query = new XQuery(
      "max((xs:untypedAtomic(\"3\"), 1, 2)) instance of xs:double",
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
   *  A test whose essence is: `max((1, xs:float(2), xs:untypedAtomic("3"))) eq 3`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc12() {
    final XQuery query = new XQuery(
      "max((1, xs:float(2), xs:untypedAtomic(\"3\"))) eq 3",
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
   *  A test whose essence is: `max((1, xs:float(2), xs:untypedAtomic("3"))) instance of xs:double`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc13() {
    final XQuery query = new XQuery(
      "max((1, xs:float(2), xs:untypedAtomic(\"3\"))) instance of xs:double",
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
   *  A test whose essence is: `max((1, xs:float(2), xs:decimal(3))) instance of xs:float`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc14() {
    final XQuery query = new XQuery(
      "max((1, xs:float(2), xs:decimal(3))) instance of xs:float",
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
   *  A test whose essence is: `max((1, xs:untypedAtomic("3"), xs:float(2))) instance of xs:double`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc15() {
    final XQuery query = new XQuery(
      "max((1, xs:untypedAtomic(\"3\"), xs:float(2))) instance of xs:double",
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
   *  A test whose essence is: `string(max((1, xs:untypedAtomic("NaN"), xs:float(2)))) eq "NaN"`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc16() {
    final XQuery query = new XQuery(
      "string(max((1, xs:untypedAtomic(\"NaN\"), xs:float(2)))) eq \"NaN\"",
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
   *  A test whose essence is: `string(max((xs:float("NaN"), xs:untypedAtomic("3"), xs:float(2)))) eq "NaN"`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc17() {
    final XQuery query = new XQuery(
      "string(max((xs:float(\"NaN\"), xs:untypedAtomic(\"3\"), xs:float(2)))) eq \"NaN\"",
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
   *  A test whose essence is: `max((xs:float("NaN"), xs:untypedAtomic("3"), xs:float(2))) instance of xs:double`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc18() {
    final XQuery query = new XQuery(
      "max((xs:float(\"NaN\"), xs:untypedAtomic(\"3\"), xs:float(2))) instance of xs:double",
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
   *  A test whose essence is: `max((xs:float("NaN"), 1, 1, 2, xs:double("NaN"))) instance of xs:double`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc19() {
    final XQuery query = new XQuery(
      "max((xs:float(\"NaN\"), 1, 1, 2, xs:double(\"NaN\"))) instance of xs:double",
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
   *  A test whose essence is: `max("a string", "http://www.w3.org/2005/xpath-functions/collation/codepoint", "wrong param")`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc2() {
    final XQuery query = new XQuery(
      "max(\"a string\", \"http://www.w3.org/2005/xpath-functions/collation/codepoint\", \"wrong param\")",
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
   *  A test whose essence is: `max((xs:double("NaN"), 1, 1, 2, xs:float("NaN"))) instance of xs:double`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc20() {
    final XQuery query = new XQuery(
      "max((xs:double(\"NaN\"), 1, 1, 2, xs:float(\"NaN\"))) instance of xs:double",
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
   *  An xs:string cannot be compared to a numeric, even if a value is NaN. .
   */
  @org.junit.Test
  public void kSeqMAXFunc21() {
    final XQuery query = new XQuery(
      "max((xs:float(\"NaN\"), 1, \"a string\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0006")
    );
  }

  /**
   *  An xs:string cannot be compared to a numeric, even if a value is NaN. .
   */
  @org.junit.Test
  public void kSeqMAXFunc22() {
    final XQuery query = new XQuery(
      "max((\"a string\", 1, xs:float(\"NaN\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0006")
    );
  }

  /**
   *  A type error must be reported even if one of other values is NaN. .
   */
  @org.junit.Test
  public void kSeqMAXFunc23() {
    final XQuery query = new XQuery(
      "max((xs:float(\"NaN\"), 1, xs:untypedAtomic(\"one\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0001")
    );
  }

  /**
   *  A type error must be reported even if one of other values is NaN. .
   */
  @org.junit.Test
  public void kSeqMAXFunc24() {
    final XQuery query = new XQuery(
      "max((xs:untypedAtomic(\"one\"), 1, xs:float(\"NaN\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0001")
    );
  }

  /**
   *  A test whose essence is: `string(max((xs:double("NaN"), xs:double("NaN")))) eq "NaN"`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc25() {
    final XQuery query = new XQuery(
      "string(max((xs:double(\"NaN\"), xs:double(\"NaN\")))) eq \"NaN\"",
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
   *  A test whose essence is: `string(max((xs:float("NaN"), xs:float("NaN")))) eq "NaN"`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc26() {
    final XQuery query = new XQuery(
      "string(max((xs:float(\"NaN\"), xs:float(\"NaN\")))) eq \"NaN\"",
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
   *  A test whose essence is: `string(max((3, xs:double("NaN")))) eq "NaN"`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc27() {
    final XQuery query = new XQuery(
      "string(max((3, xs:double(\"NaN\")))) eq \"NaN\"",
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
   *  A test whose essence is: `string(max((3, xs:float("NaN")))) eq "NaN"`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc28() {
    final XQuery query = new XQuery(
      "string(max((3, xs:float(\"NaN\")))) eq \"NaN\"",
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
   *  A test whose essence is: `max((3, xs:double("NaN"))) instance of xs:double`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc29() {
    final XQuery query = new XQuery(
      "max((3, xs:double(\"NaN\"))) instance of xs:double",
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
   *  A test whose essence is: `empty(max(()))`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc3() {
    final XQuery query = new XQuery(
      "empty(max(()))",
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
   *  A test whose essence is: `max((3, xs:float("NaN"))) instance of xs:float`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc30() {
    final XQuery query = new XQuery(
      "max((3, xs:float(\"NaN\"))) instance of xs:float",
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
   *  A test whose essence is: `string(max((xs:float(-3), xs:untypedAtomic("3"), xs:double("NaN")))) eq "NaN"`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc31() {
    final XQuery query = new XQuery(
      "string(max((xs:float(-3), xs:untypedAtomic(\"3\"), xs:double(\"NaN\")))) eq \"NaN\"",
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
   *  A test whose essence is: `max((xs:float(-3), xs:untypedAtomic("3"), xs:double("NaN"))) instance of xs:double`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc32() {
    final XQuery query = new XQuery(
      "max((xs:float(-3), xs:untypedAtomic(\"3\"), xs:double(\"NaN\"))) instance of xs:double",
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
   *  A test whose essence is: `string(max(xs:float("NaN"))) eq "NaN"`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc33() {
    final XQuery query = new XQuery(
      "string(max(xs:float(\"NaN\"))) eq \"NaN\"",
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
   *  A test whose essence is: `string(max(xs:double("NaN"))) eq "NaN"`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc34() {
    final XQuery query = new XQuery(
      "string(max(xs:double(\"NaN\"))) eq \"NaN\"",
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
   *  A test whose essence is: `max(xs:untypedAtomic("three"))`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc35() {
    final XQuery query = new XQuery(
      "max(xs:untypedAtomic(\"three\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0001")
    );
  }

  /**
   *  A test whose essence is: `max((xs:untypedAtomic("3"), "a string"))`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc36() {
    final XQuery query = new XQuery(
      "max((xs:untypedAtomic(\"3\"), \"a string\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0006")
    );
  }

  /**
   *  A test whose essence is: `max(("a string", xs:untypedAtomic("3")))`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc37() {
    final XQuery query = new XQuery(
      "max((\"a string\", xs:untypedAtomic(\"3\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0006")
    );
  }

  /**
   *  A test whose essence is: `max(QName("example.com/", "ncname"))`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc38() {
    final XQuery query = new XQuery(
      "max(QName(\"example.com/\", \"ncname\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0006")
    );
  }

  /**
   *  A test whose essence is: `max(xs:anyURI("example.com/")) eq xs:anyURI("example.com/")`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc39() {
    final XQuery query = new XQuery(
      "max(xs:anyURI(\"example.com/\")) eq xs:anyURI(\"example.com/\")",
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
   *  A test whose essence is: `max((3, 3, 3, 3, 3, 3)) eq 3`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc4() {
    final XQuery query = new XQuery(
      "max((3, 3, 3, 3, 3, 3)) eq 3",
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
   *  A test whose essence is: `max((xs:anyURI("example.com/"), xs:anyURI("example.com/"))) eq xs:anyURI("example.com/")`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc40() {
    final XQuery query = new XQuery(
      "max((xs:anyURI(\"example.com/\"), xs:anyURI(\"example.com/\"))) eq xs:anyURI(\"example.com/\")",
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
   *  A test whose essence is: `max(("a string")) eq "a string"`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc41() {
    final XQuery query = new XQuery(
      "max((\"a string\")) eq \"a string\"",
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
   *  A test whose essence is: `max(("a string", QName("example.com/", "ncname")))`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc42() {
    final XQuery query = new XQuery(
      "max((\"a string\", QName(\"example.com/\", \"ncname\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0006")
    );
  }

  /**
   *  A test whose essence is: `max((5, 5.0e0)) eq 5.0e0`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc43() {
    final XQuery query = new XQuery(
      "max((5, 5.0e0)) eq 5.0e0",
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
   *  A test whose essence is: `max((5, 5.0e0)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc44() {
    final XQuery query = new XQuery(
      "max((5, 5.0e0)) instance of xs:double",
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
   *  A test whose essence is: `max((5, 3.0e0)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc45() {
    final XQuery query = new XQuery(
      "max((5, 3.0e0)) instance of xs:double",
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
   *  A test whose essence is: `max((5.0e0, 5)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc46() {
    final XQuery query = new XQuery(
      "max((5.0e0, 5)) instance of xs:double",
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
   *  A test whose essence is: `max((3, 5.0e0)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc47() {
    final XQuery query = new XQuery(
      "max((3, 5.0e0)) instance of xs:double",
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
   *  A test whose essence is: `max((5.0e0, 3)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc48() {
    final XQuery query = new XQuery(
      "max((5.0e0, 3)) instance of xs:double",
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
   *  A test whose essence is: `max((1, 1, 1, 1, 1.0)) instance of xs:decimal`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc49() {
    final XQuery query = new XQuery(
      "max((1, 1, 1, 1, 1.0)) instance of xs:decimal",
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
   *  A test whose essence is: `max((3, 1, 1, 1, 1, 1)) eq 3`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc5() {
    final XQuery query = new XQuery(
      "max((3, 1, 1, 1, 1, 1)) eq 3",
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
   *  A test whose essence is: `max((1.0, 1, 1, 1, 1)) instance of xs:decimal`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc50() {
    final XQuery query = new XQuery(
      "max((1.0, 1, 1, 1, 1)) instance of xs:decimal",
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
   *  A test whose essence is: `max((1.0, 1, 1.0, 1, 1)) instance of xs:decimal`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc51() {
    final XQuery query = new XQuery(
      "max((1.0, 1, 1.0, 1, 1)) instance of xs:decimal",
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
   *  A test whose essence is: `max((5.0e0, 5)) eq 5.0e0`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc52() {
    final XQuery query = new XQuery(
      "max((5.0e0, 5)) eq 5.0e0",
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
   *  A test whose essence is: `max((3, 5.0e0)) eq 5.0e0`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc53() {
    final XQuery query = new XQuery(
      "max((3, 5.0e0)) eq 5.0e0",
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
   *  A test whose essence is: `max((5.0e0, 3)) eq 5.0e0`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc54() {
    final XQuery query = new XQuery(
      "max((5.0e0, 3)) eq 5.0e0",
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
   *  Testing max() with current-date(). .
   */
  @org.junit.Test
  public void kSeqMAXFunc55() {
    final XQuery query = new XQuery(
      "max((current-date(), xs:date(\"1999-01-01\"))) eq current-date()",
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
   *  A test whose essence is: `max((3,4,5)) eq 5`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc56() {
    final XQuery query = new XQuery(
      "max((3,4,5)) eq 5",
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
   *  A test whose essence is: `max((5, 5.0e0)) eq 5.0e0`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc57() {
    final XQuery query = new XQuery(
      "max((5, 5.0e0)) eq 5.0e0",
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
   *  A test whose essence is: `max((3,4, "Zero"))`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc58() {
    final XQuery query = new XQuery(
      "max((3,4, \"Zero\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0006")
    );
  }

  /**
   *  A test whose essence is: `max(("a", "b", "c")) eq "c"`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc59() {
    final XQuery query = new XQuery(
      "max((\"a\", \"b\", \"c\")) eq \"c\"",
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
   *  A test whose essence is: `max((1, 1, 1, 1, 1, 3)) eq 3`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc6() {
    final XQuery query = new XQuery(
      "max((1, 1, 1, 1, 1, 3)) eq 3",
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
   *  A test whose essence is: `max((3, 1, 5, 1, 1, 3)) eq 5`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc7() {
    final XQuery query = new XQuery(
      "max((3, 1, 5, 1, 1, 3)) eq 5",
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
   *  A test whose essence is: `max((3, -5.0, 5, 1, -3, 3)) eq 5`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc8() {
    final XQuery query = new XQuery(
      "max((3, -5.0, 5, 1, -3, 3)) eq 5",
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
   *  A test whose essence is: `max(xs:untypedAtomic("3")) instance of xs:double`. .
   */
  @org.junit.Test
  public void kSeqMAXFunc9() {
    final XQuery query = new XQuery(
      "max(xs:untypedAtomic(\"3\")) instance of xs:double",
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
   *  Invoke fn:max() on two identical xs:anyURI values. .
   */
  @org.junit.Test
  public void k2SeqMAXFunc1() {
    final XQuery query = new XQuery(
      "max((xs:anyURI(\"http://example.com/A\"), xs:anyURI(\"http://example.com/A\"))) eq xs:anyURI(\"http://example.com/A\")",
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
   *  Invoke fn:max() on two different xs:anyURI values. .
   */
  @org.junit.Test
  public void k2SeqMAXFunc2() {
    final XQuery query = new XQuery(
      "max((xs:anyURI(\"http://example.com/B\"), xs:anyURI(\"http://example.com/A\"))) eq xs:anyURI(\"http://example.com/B\")",
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
   *  Invoke fn:max() on two different xs:anyURI values(#2). .
   */
  @org.junit.Test
  public void k2SeqMAXFunc3() {
    final XQuery query = new XQuery(
      "max((xs:anyURI(\"http://example.com/8\"), xs:anyURI(\"http://example.com/4\"))) eq xs:anyURI(\"http://example.com/8\")",
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
   *  Invoke fn:max() with an unsupported collation. .
   */
  @org.junit.Test
  public void k2SeqMAXFunc4() {
    final XQuery query = new XQuery(
      "max((\"str1\", \"str2\"), \"http://example.com/UNSUPPORTED_COLLATION\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCH0002")
    );
  }

  /**
   *  Invoke fn:max() with too many arguments. .
   */
  @org.junit.Test
  public void k2SeqMAXFunc5() {
    final XQuery query = new XQuery(
      "max((\"str1\", \"str2\"), \"http://www.w3.org/2005/xpath-functions/collation/codepoint\", ())",
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
   *  Invoke fn:max() with an unsupported collation and one xs:anyURI value. .
   */
  @org.junit.Test
  public void k2SeqMAXFunc6() {
    final XQuery query = new XQuery(
      "max(xs:anyURI(\"str1\"), \"max://example.com/UNSUPPORTED_COLLATION\")",
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
        assertEq("str1")
      ||
        error("FOCH0002")
      )
    );
  }

  /**
   *  Ensure the return type is properly inferred. .
   */
  @org.junit.Test
  public void k2SeqMAXFunc7() {
    final XQuery query = new XQuery(
      "max(xs:unsignedShort(\"1\")) instance of xs:unsignedShort",
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
   *  test fn:max on xs:boolean arguments .
   */
  @org.junit.Test
  public void cbclMax001() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:f($x as xs:integer) { \n" +
      "      \t\tif ($x = 0) then true() else false() \n" +
      "      \t}; \n" +
      "      \tmax(for $x in (1,2,3) return local:f($x))\n" +
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
      assertBoolean(false)
    );
  }

  /**
   *  test fn:max on xs:boolean arguments .
   */
  @org.junit.Test
  public void cbclMax002() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:f($x as xs:integer) { \n" +
      "      \t\tif ($x = 2) then true() else false() \n" +
      "      \t}; \n" +
      "      \tmax(for $x in (1,2,3) return local:f($x))\n" +
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
   *  test fn:max on xs:boolean arguments .
   */
  @org.junit.Test
  public void cbclMax003() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:f($x as xs:integer) { \n" +
      "      \t\tif ($x = 3) then $x else false() \n" +
      "      \t}; \n" +
      "      \tmax(for $x in (1,2,3) return local:f($x))\n" +
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
      error("FORG0006")
    );
  }

  /**
   *  test fn:max with xs:date argument causing type error .
   */
  @org.junit.Test
  public void cbclMax004() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:f($x as xs:integer) {\n" +
      "      \t\t if ($x < 3) then current-date() else current-time() \n" +
      "      \t}; \n" +
      "      \tmax(for $x in (1,2,3) return local:f($x))\n" +
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
      error("FORG0006")
    );
  }

  /**
   *  test fn:max with xs:dateTime arguments .
   */
  @org.junit.Test
  public void cbclMax005() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:f($x as xs:integer) { \n" +
      "      \t\tif ($x = 2) then xs:dateTime(\"3000-12-01T12:00:00\") else current-dateTime() \n" +
      "      \t}; \n" +
      "      \tmax(for $x in (1,2,3) return local:f($x))\n" +
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
      assertStringValue(false, "3000-12-01T12:00:00")
    );
  }

  /**
   *  test fn:max with xs:dateTime argument causing type error .
   */
  @org.junit.Test
  public void cbclMax006() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:f($x as xs:integer) { \n" +
      "      \t\tif ($x < 3) then current-dateTime() else xs:dayTimeDuration(\"PT3S\") \n" +
      "      \t}; \n" +
      "      \tmax(for $x in (1,2,3) return local:f($x))\n" +
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
      error("FORG0006")
    );
  }

  /**
   *  Test fn:max with xs:dayTimeDuration arguments .
   */
  @org.junit.Test
  public void cbclMax007() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:f($x as xs:integer) { \n" +
      "      \t\tif ($x = 2) then xs:dayTimeDuration(\"P1D\") else xs:dayTimeDuration(\"PT3S\") \n" +
      "      \t}; \n" +
      "      \tmax(for $x in (1,2,3) return local:f($x))\n" +
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
      assertStringValue(false, "P1D")
    );
  }

  /**
   *  Test fn:max with xs:dayTimeDuration argument causing type error .
   */
  @org.junit.Test
  public void cbclMax008() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:f($x as xs:integer) {\n" +
      "      \t\t if ($x = 3) then xs:duration(\"P1D\") else xs:dayTimeDuration(\"PT3S\") \n" +
      "      \t}; \n" +
      "      \tmax(for $x in (1,2,3) return local:f($x))\n" +
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
      error("FORG0006")
    );
  }

  /**
   *  Test fn:max with numeric arguments .
   */
  @org.junit.Test
  public void cbclMax009() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:f($x as xs:integer) {\n" +
      "      \t\t (xs:decimal(1.1), xs:float(2.2), xs:double(1.4), xs:integer(2))[$x] \n" +
      "      \t}; \n" +
      "      \tmax(for $x in (1,2,3) return local:f($x)) instance of xs:double\n" +
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
   *  Test fn:max with numeric arguments .
   */
  @org.junit.Test
  public void cbclMax010() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:f($x as xs:integer) { \n" +
      "      \t\t(xs:decimal(1.3), xs:float(1.2), xs:double(1.4), xs:integer(2))[$x] \n" +
      "      \t}; \n" +
      "      \tmax(for $x in (1,2,3) return local:f($x)) instance of xs:double\n" +
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
   *  Test fn:max with numeric arguments rasing error .
   */
  @org.junit.Test
  public void cbclMax011() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:f($x as xs:integer) {\n" +
      "      \t\t (xs:decimal(1.1), xs:float(1.2), xs:double(0.4), xs:string(\"2\"))[$x] \n" +
      "      \t}; \n" +
      "      \tmax(for $x in (1,4,2,3) return local:f($x))\n" +
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
      error("FORG0006")
    );
  }

  /**
   *  Test fn:max with numeric arguments .
   */
  @org.junit.Test
  public void cbclMax012() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:f($x as xs:integer) { \n" +
      "      \t\t(xs:decimal(1.1), xs:float(1.2), xs:double(0.4), xs:integer(\"-3\"))[$x] \n" +
      "      \t}; \n" +
      "      \tmax(for $x in (4,2,1,3) return local:f($x)) instance of xs:double\n" +
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
   *  test fn:max with xs:time arguments .
   */
  @org.junit.Test
  public void cbclMax013() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:f($x as xs:integer) { \n" +
      "      \t\tif ($x = 2) then xs:time(\"12:00:00-01:00\") else xs:time(\"12:00:00+01:00\") \n" +
      "      \t}; \n" +
      "      \tmax(for $x in (1,2,3) return local:f($x))\n" +
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
      assertStringValue(false, "12:00:00-01:00")
    );
  }

  /**
   *  test fn:max with xs:time argument causing type error .
   */
  @org.junit.Test
  public void cbclMax014() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:f($x as xs:integer) { \n" +
      "      \t\tif ($x < 3) then current-time() else xs:dayTimeDuration(\"PT3S\") \n" +
      "      \t}; \n" +
      "      \tmax(for $x in (1,2,3) return local:f($x))\n" +
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
      error("FORG0006")
    );
  }

  /**
   *  Test fn:max with xs:yearMonthDuration arguments .
   */
  @org.junit.Test
  public void cbclMax015() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:f($x as xs:integer) { \n" +
      "      \t\tif ($x = 2) then xs:yearMonthDuration(\"P13M\") else xs:yearMonthDuration(\"P1Y\") \n" +
      "      \t};\n" +
      "      \tmax(for $x in (1,2,3) return local:f($x))\n" +
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
      assertStringValue(false, "P1Y1M")
    );
  }

  /**
   *  Test fn:max with xs:yearMonthDuration argument causing type error .
   */
  @org.junit.Test
  public void cbclMax016() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:f($x as xs:integer) { \n" +
      "      \t\tif ($x = 3) then xs:duration(\"P1Y\") else xs:yearMonthDuration(\"P11M\") \n" +
      "      \t}; \n" +
      "      \tmax(for $x in (1,2,3) return local:f($x))\n" +
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
      error("FORG0006")
    );
  }

  /**
   *  Test fn:max with invalid type for first argument .
   */
  @org.junit.Test
  public void cbclMax017() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:f($x as xs:integer) as xs:duration { \n" +
      "      \t\tif ($x = 1) then xs:duration(\"P1Y\") else xs:yearMonthDuration(\"P11M\") \n" +
      "      \t}; \n" +
      "      \tmax(for $x in (1,2,3) return local:f($x))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0006")
    );
  }

  /**
   *  Test fn:max on a range expression .
   */
  @org.junit.Test
  public void cbclMax018() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:f($x as xs:integer) as xs:integer { $x }; \n" +
      "      \tmax(local:f(4) to local:f(10))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "10")
    );
  }

  /**
   *  Test evaluating effective boolean value of max .
   */
  @org.junit.Test
  public void cbclMax019() {
    final XQuery query = new XQuery(
      "if (max(for $x in 1 to 10 return $x mod 9 = 0)) then true() else false()",
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
   *  Evaluation of the fn:max function with argument to sequence of different types. .
   */
  @org.junit.Test
  public void fnMax1() {
    final XQuery query = new XQuery(
      "fn:max((3,4,\"Zero\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0006")
    );
  }

  /**
   *  Test fn:max on a sequence of numeric arguments .
   */
  @org.junit.Test
  public void fnMax10() {
    final XQuery query = new XQuery(
      "for $p in 1 to 4 let $x := (xs:integer(1), xs:decimal(2), xs:float(3), xs:double(4))[position() le $p] return typeswitch (max($x)) case xs:integer return \"integer\" case xs:decimal return \"decimal\" case xs:float return \"float\" case xs:double return \"double\" default return error()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "integer decimal float double")
    );
  }

  /**
   *  Test fn:max on a sequence of strings .
   */
  @org.junit.Test
  public void fnMax11() {
    final XQuery query = new XQuery(
      "max((\"a\", \"b\", \"c\", \"d\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "d")
    );
  }

  /**
   *  Test fn:max on a sequence of dates .
   */
  @org.junit.Test
  public void fnMax12() {
    final XQuery query = new XQuery(
      "max((xs:date('1066-10-02'), xs:date('1588-08-08'), xs:date('2011-06-29')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2011-06-29")
    );
  }

  /**
   *  Evaluation of type promotion when using mixed typed with fn:max function .
   */
  @org.junit.Test
  public void fnMax2() {
    final XQuery query = new XQuery(
      "fn:max((xs:integer(5000000000),xs:double(3e0)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("5.0E9")
    );
  }

  /**
   *  Evaluation of type promotion when using mixed typed with fn:max function Uses drived types promoted to least common type. .
   */
  @org.junit.Test
  public void fnMax3() {
    final XQuery query = new XQuery(
      "let $var := fn:max((xs:long(20),xs:short(13))) return $var instance of xs:integer",
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
   *  Test fn:max on a sequence of xs:dayTimeDuration arguments .
   */
  @org.junit.Test
  public void fnMax4() {
    final XQuery query = new XQuery(
      "max((xs:dayTimeDuration(\"P1D\"), xs:dayTimeDuration(\"PT2H\"))) instance of xs:dayTimeDuration",
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
   *  Test fn:max on a sequence of xs:dayTimeDuration arguments .
   */
  @org.junit.Test
  public void fnMax5() {
    final XQuery query = new XQuery(
      "max(for $x in 1 to 10 return xs:dayTimeDuration(concat(\"PT\",$x,\"H\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "PT10H")
    );
  }

  /**
   *  Test fn:max on a sequence of xs:yearMonthDuration arguments .
   */
  @org.junit.Test
  public void fnMax6() {
    final XQuery query = new XQuery(
      "max((xs:yearMonthDuration(\"P1Y\"), xs:yearMonthDuration(\"P1M\"))) instance of xs:yearMonthDuration",
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
   *  Test fn:max on a sequence of xs:yearMonthDuration arguments .
   */
  @org.junit.Test
  public void fnMax7() {
    final XQuery query = new XQuery(
      "max(for $x in 1 to 10 return xs:yearMonthDuration(concat(\"P\",$x,\"M\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P10M")
    );
  }

  /**
   *  Test fn:max on a sequence of xs:yearMonthDuration and xs:dayTimeDuration arguments .
   */
  @org.junit.Test
  public void fnMax8() {
    final XQuery query = new XQuery(
      "max((xs:yearMonthDuration(\"P1Y\"), xs:dayTimeDuration(\"P1D\")))",
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
        error("FORG0006")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  Test fn:max on a sequence of xs:duration arguments .
   */
  @org.junit.Test
  public void fnMax9() {
    final XQuery query = new XQuery(
      "max(xs:duration(\"P1Y1M1D\"))",
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
        error("FORG0006")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnMaxdbl1args1() {
    final XQuery query = new XQuery(
      "fn:max((xs:double(\"-1.7976931348623157E308\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg = xs:double(mid range) .
   */
  @org.junit.Test
  public void fnMaxdbl1args2() {
    final XQuery query = new XQuery(
      "fn:max((xs:double(\"0\")))",
      ctx);
    try {
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
   *  Evaluates The "max" function with the arguments set as follows: $arg = xs:double(upper bound) .
   */
  @org.junit.Test
  public void fnMaxdbl1args3() {
    final XQuery query = new XQuery(
      "fn:max((xs:double(\"1.7976931348623157E308\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:double(lower bound) $arg2 = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnMaxdbl2args1() {
    final XQuery query = new XQuery(
      "fn:max((xs:double(\"-1.7976931348623157E308\"),xs:double(\"-1.7976931348623157E308\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:double(mid range) $arg2 = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnMaxdbl2args2() {
    final XQuery query = new XQuery(
      "fn:max((xs:double(\"0\"),xs:double(\"-1.7976931348623157E308\")))",
      ctx);
    try {
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
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:double(upper bound) $arg2 = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnMaxdbl2args3() {
    final XQuery query = new XQuery(
      "fn:max((xs:double(\"1.7976931348623157E308\"),xs:double(\"-1.7976931348623157E308\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:double(lower bound) $arg2 = xs:double(mid range) .
   */
  @org.junit.Test
  public void fnMaxdbl2args4() {
    final XQuery query = new XQuery(
      "fn:max((xs:double(\"-1.7976931348623157E308\"),xs:double(\"0\")))",
      ctx);
    try {
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
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:double(lower bound) $arg2 = xs:double(upper bound) .
   */
  @org.junit.Test
  public void fnMaxdbl2args5() {
    final XQuery query = new XQuery(
      "fn:max((xs:double(\"-1.7976931348623157E308\"),xs:double(\"1.7976931348623157E308\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnMaxdec1args1() {
    final XQuery query = new XQuery(
      "fn:max((xs:decimal(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg = xs:decimal(mid range) .
   */
  @org.junit.Test
  public void fnMaxdec1args2() {
    final XQuery query = new XQuery(
      "fn:max((xs:decimal(\"617375191608514839\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("617375191608514839")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg = xs:decimal(upper bound) .
   */
  @org.junit.Test
  public void fnMaxdec1args3() {
    final XQuery query = new XQuery(
      "fn:max((xs:decimal(\"999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:decimal(lower bound) $arg2 = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnMaxdec2args1() {
    final XQuery query = new XQuery(
      "fn:max((xs:decimal(\"-999999999999999999\"),xs:decimal(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:decimal(mid range) $arg2 = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnMaxdec2args2() {
    final XQuery query = new XQuery(
      "fn:max((xs:decimal(\"617375191608514839\"),xs:decimal(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("617375191608514839")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:decimal(upper bound) $arg2 = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnMaxdec2args3() {
    final XQuery query = new XQuery(
      "fn:max((xs:decimal(\"999999999999999999\"),xs:decimal(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:decimal(lower bound) $arg2 = xs:decimal(mid range) .
   */
  @org.junit.Test
  public void fnMaxdec2args4() {
    final XQuery query = new XQuery(
      "fn:max((xs:decimal(\"-999999999999999999\"),xs:decimal(\"617375191608514839\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("617375191608514839")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:decimal(lower bound) $arg2 = xs:decimal(upper bound) .
   */
  @org.junit.Test
  public void fnMaxdec2args5() {
    final XQuery query = new XQuery(
      "fn:max((xs:decimal(\"-999999999999999999\"),xs:decimal(\"999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnMaxflt1args1() {
    final XQuery query = new XQuery(
      "fn:max((xs:float(\"-3.4028235E38\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("xs:float(\"-3.4028235E38\")")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg = xs:float(mid range) .
   */
  @org.junit.Test
  public void fnMaxflt1args2() {
    final XQuery query = new XQuery(
      "fn:max((xs:float(\"0\")))",
      ctx);
    try {
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
   *  Evaluates The "max" function with the arguments set as follows: $arg = xs:float(upper bound) .
   */
  @org.junit.Test
  public void fnMaxflt1args3() {
    final XQuery query = new XQuery(
      "fn:max((xs:float(\"3.4028235E38\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("xs:float(\"3.4028235E38\")")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:float(lower bound) $arg2 = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnMaxflt2args1() {
    final XQuery query = new XQuery(
      "fn:max((xs:float(\"-3.4028235E38\"),xs:float(\"-3.4028235E38\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("xs:float(-3.4028235E38)")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:float(mid range) $arg2 = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnMaxflt2args2() {
    final XQuery query = new XQuery(
      "fn:max((xs:float(\"0\"),xs:float(\"-3.4028235E38\")))",
      ctx);
    try {
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
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:float(upper bound) $arg2 = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnMaxflt2args3() {
    final XQuery query = new XQuery(
      "fn:max((xs:float(\"3.4028235E38\"),xs:float(\"-3.4028235E38\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("xs:float(3.4028235E38)")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:float(lower bound) $arg2 = xs:float(mid range) .
   */
  @org.junit.Test
  public void fnMaxflt2args4() {
    final XQuery query = new XQuery(
      "fn:max((xs:float(\"-3.4028235E38\"),xs:float(\"0\")))",
      ctx);
    try {
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
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:float(lower bound) $arg2 = xs:float(upper bound) .
   */
  @org.junit.Test
  public void fnMaxflt2args5() {
    final XQuery query = new XQuery(
      "fn:max((xs:float(\"-3.4028235E38\"),xs:float(\"3.4028235E38\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("xs:float(3.4028235E38)")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnMaxint1args1() {
    final XQuery query = new XQuery(
      "fn:max((xs:int(\"-2147483648\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-2147483648")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg = xs:int(mid range) .
   */
  @org.junit.Test
  public void fnMaxint1args2() {
    final XQuery query = new XQuery(
      "fn:max((xs:int(\"-1873914410\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1873914410")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg = xs:int(upper bound) .
   */
  @org.junit.Test
  public void fnMaxint1args3() {
    final XQuery query = new XQuery(
      "fn:max((xs:int(\"2147483647\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("2147483647")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:int(lower bound) $arg2 = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnMaxint2args1() {
    final XQuery query = new XQuery(
      "fn:max((xs:int(\"-2147483648\"),xs:int(\"-2147483648\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-2147483648")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:int(mid range) $arg2 = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnMaxint2args2() {
    final XQuery query = new XQuery(
      "fn:max((xs:int(\"-1873914410\"),xs:int(\"-2147483648\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1873914410")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:int(upper bound) $arg2 = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnMaxint2args3() {
    final XQuery query = new XQuery(
      "fn:max((xs:int(\"2147483647\"),xs:int(\"-2147483648\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("2147483647")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:int(lower bound) $arg2 = xs:int(mid range) .
   */
  @org.junit.Test
  public void fnMaxint2args4() {
    final XQuery query = new XQuery(
      "fn:max((xs:int(\"-2147483648\"),xs:int(\"-1873914410\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1873914410")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:int(lower bound) $arg2 = xs:int(upper bound) .
   */
  @org.junit.Test
  public void fnMaxint2args5() {
    final XQuery query = new XQuery(
      "fn:max((xs:int(\"-2147483648\"),xs:int(\"2147483647\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("2147483647")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnMaxintg1args1() {
    final XQuery query = new XQuery(
      "fn:max((xs:integer(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg = xs:integer(mid range) .
   */
  @org.junit.Test
  public void fnMaxintg1args2() {
    final XQuery query = new XQuery(
      "fn:max((xs:integer(\"830993497117024304\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("830993497117024304")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg = xs:integer(upper bound) .
   */
  @org.junit.Test
  public void fnMaxintg1args3() {
    final XQuery query = new XQuery(
      "fn:max((xs:integer(\"999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:integer(lower bound) $arg2 = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnMaxintg2args1() {
    final XQuery query = new XQuery(
      "fn:max((xs:integer(\"-999999999999999999\"),xs:integer(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:integer(mid range) $arg2 = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnMaxintg2args2() {
    final XQuery query = new XQuery(
      "fn:max((xs:integer(\"830993497117024304\"),xs:integer(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("830993497117024304")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:integer(upper bound) $arg2 = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnMaxintg2args3() {
    final XQuery query = new XQuery(
      "fn:max((xs:integer(\"999999999999999999\"),xs:integer(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:integer(lower bound) $arg2 = xs:integer(mid range) .
   */
  @org.junit.Test
  public void fnMaxintg2args4() {
    final XQuery query = new XQuery(
      "fn:max((xs:integer(\"-999999999999999999\"),xs:integer(\"830993497117024304\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("830993497117024304")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:integer(lower bound) $arg2 = xs:integer(upper bound) .
   */
  @org.junit.Test
  public void fnMaxintg2args5() {
    final XQuery query = new XQuery(
      "fn:max((xs:integer(\"-999999999999999999\"),xs:integer(\"999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnMaxlng1args1() {
    final XQuery query = new XQuery(
      "fn:max((xs:long(\"-92233720368547758\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-92233720368547758")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg = xs:long(mid range) .
   */
  @org.junit.Test
  public void fnMaxlng1args2() {
    final XQuery query = new XQuery(
      "fn:max((xs:long(\"-47175562203048468\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-47175562203048468")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg = xs:long(upper bound) .
   */
  @org.junit.Test
  public void fnMaxlng1args3() {
    final XQuery query = new XQuery(
      "fn:max((xs:long(\"92233720368547758\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("92233720368547758")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:long(lower bound) $arg2 = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnMaxlng2args1() {
    final XQuery query = new XQuery(
      "fn:max((xs:long(\"-92233720368547758\"),xs:long(\"-92233720368547758\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-92233720368547758")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:long(mid range) $arg2 = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnMaxlng2args2() {
    final XQuery query = new XQuery(
      "fn:max((xs:long(\"-47175562203048468\"),xs:long(\"-92233720368547758\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-47175562203048468")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:long(upper bound) $arg2 = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnMaxlng2args3() {
    final XQuery query = new XQuery(
      "fn:max((xs:long(\"92233720368547758\"),xs:long(\"-92233720368547758\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("92233720368547758")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:long(lower bound) $arg2 = xs:long(mid range) .
   */
  @org.junit.Test
  public void fnMaxlng2args4() {
    final XQuery query = new XQuery(
      "fn:max((xs:long(\"-92233720368547758\"),xs:long(\"-47175562203048468\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-47175562203048468")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:long(lower bound) $arg2 = xs:long(upper bound) .
   */
  @org.junit.Test
  public void fnMaxlng2args5() {
    final XQuery query = new XQuery(
      "fn:max((xs:long(\"-92233720368547758\"),xs:long(\"92233720368547758\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("92233720368547758")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnMaxnint1args1() {
    final XQuery query = new XQuery(
      "fn:max((xs:negativeInteger(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg = xs:negativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnMaxnint1args2() {
    final XQuery query = new XQuery(
      "fn:max((xs:negativeInteger(\"-297014075999096793\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-297014075999096793")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg = xs:negativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnMaxnint1args3() {
    final XQuery query = new XQuery(
      "fn:max((xs:negativeInteger(\"-1\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:negativeInteger(lower bound) $arg2 = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnMaxnint2args1() {
    final XQuery query = new XQuery(
      "fn:max((xs:negativeInteger(\"-999999999999999999\"),xs:negativeInteger(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:negativeInteger(mid range) $arg2 = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnMaxnint2args2() {
    final XQuery query = new XQuery(
      "fn:max((xs:negativeInteger(\"-297014075999096793\"),xs:negativeInteger(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-297014075999096793")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:negativeInteger(upper bound) $arg2 = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnMaxnint2args3() {
    final XQuery query = new XQuery(
      "fn:max((xs:negativeInteger(\"-1\"),xs:negativeInteger(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:negativeInteger(lower bound) $arg2 = xs:negativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnMaxnint2args4() {
    final XQuery query = new XQuery(
      "fn:max((xs:negativeInteger(\"-999999999999999999\"),xs:negativeInteger(\"-297014075999096793\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-297014075999096793")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:negativeInteger(lower bound) $arg2 = xs:negativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnMaxnint2args5() {
    final XQuery query = new XQuery(
      "fn:max((xs:negativeInteger(\"-999999999999999999\"),xs:negativeInteger(\"-1\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnMaxnni1args1() {
    final XQuery query = new XQuery(
      "fn:max((xs:nonNegativeInteger(\"0\")))",
      ctx);
    try {
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
   *  Evaluates The "max" function with the arguments set as follows: $arg = xs:nonNegativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnMaxnni1args2() {
    final XQuery query = new XQuery(
      "fn:max((xs:nonNegativeInteger(\"303884545991464527\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("303884545991464527")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg = xs:nonNegativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnMaxnni1args3() {
    final XQuery query = new XQuery(
      "fn:max((xs:nonNegativeInteger(\"999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:nonNegativeInteger(lower bound) $arg2 = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnMaxnni2args1() {
    final XQuery query = new XQuery(
      "fn:max((xs:nonNegativeInteger(\"0\"),xs:nonNegativeInteger(\"0\")))",
      ctx);
    try {
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
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:nonNegativeInteger(mid range) $arg2 = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnMaxnni2args2() {
    final XQuery query = new XQuery(
      "fn:max((xs:nonNegativeInteger(\"303884545991464527\"),xs:nonNegativeInteger(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("303884545991464527")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:nonNegativeInteger(upper bound) $arg2 = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnMaxnni2args3() {
    final XQuery query = new XQuery(
      "fn:max((xs:nonNegativeInteger(\"999999999999999999\"),xs:nonNegativeInteger(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:nonNegativeInteger(lower bound) $arg2 = xs:nonNegativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnMaxnni2args4() {
    final XQuery query = new XQuery(
      "fn:max((xs:nonNegativeInteger(\"0\"),xs:nonNegativeInteger(\"303884545991464527\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("303884545991464527")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:nonNegativeInteger(lower bound) $arg2 = xs:nonNegativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnMaxnni2args5() {
    final XQuery query = new XQuery(
      "fn:max((xs:nonNegativeInteger(\"0\"),xs:nonNegativeInteger(\"999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnMaxnpi1args1() {
    final XQuery query = new XQuery(
      "fn:max((xs:nonPositiveInteger(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg = xs:nonPositiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnMaxnpi1args2() {
    final XQuery query = new XQuery(
      "fn:max((xs:nonPositiveInteger(\"-475688437271870490\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-475688437271870490")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg = xs:nonPositiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnMaxnpi1args3() {
    final XQuery query = new XQuery(
      "fn:max((xs:nonPositiveInteger(\"0\")))",
      ctx);
    try {
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
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:nonPositiveInteger(lower bound) $arg2 = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnMaxnpi2args1() {
    final XQuery query = new XQuery(
      "fn:max((xs:nonPositiveInteger(\"-999999999999999999\"),xs:nonPositiveInteger(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:nonPositiveInteger(mid range) $arg2 = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnMaxnpi2args2() {
    final XQuery query = new XQuery(
      "fn:max((xs:nonPositiveInteger(\"-475688437271870490\"),xs:nonPositiveInteger(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-475688437271870490")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:nonPositiveInteger(upper bound) $arg2 = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnMaxnpi2args3() {
    final XQuery query = new XQuery(
      "fn:max((xs:nonPositiveInteger(\"0\"),xs:nonPositiveInteger(\"-999999999999999999\")))",
      ctx);
    try {
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
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:nonPositiveInteger(lower bound) $arg2 = xs:nonPositiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnMaxnpi2args4() {
    final XQuery query = new XQuery(
      "fn:max((xs:nonPositiveInteger(\"-999999999999999999\"),xs:nonPositiveInteger(\"-475688437271870490\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-475688437271870490")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:nonPositiveInteger(lower bound) $arg2 = xs:nonPositiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnMaxnpi2args5() {
    final XQuery query = new XQuery(
      "fn:max((xs:nonPositiveInteger(\"-999999999999999999\"),xs:nonPositiveInteger(\"0\")))",
      ctx);
    try {
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
   *  Evaluates The "max" function with the arguments set as follows: $arg = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnMaxpint1args1() {
    final XQuery query = new XQuery(
      "fn:max((xs:positiveInteger(\"1\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg = xs:positiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnMaxpint1args2() {
    final XQuery query = new XQuery(
      "fn:max((xs:positiveInteger(\"52704602390610033\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("52704602390610033")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg = xs:positiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnMaxpint1args3() {
    final XQuery query = new XQuery(
      "fn:max((xs:positiveInteger(\"999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:positiveInteger(lower bound) $arg2 = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnMaxpint2args1() {
    final XQuery query = new XQuery(
      "fn:max((xs:positiveInteger(\"1\"),xs:positiveInteger(\"1\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:positiveInteger(mid range) $arg2 = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnMaxpint2args2() {
    final XQuery query = new XQuery(
      "fn:max((xs:positiveInteger(\"52704602390610033\"),xs:positiveInteger(\"1\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("52704602390610033")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:positiveInteger(upper bound) $arg2 = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnMaxpint2args3() {
    final XQuery query = new XQuery(
      "fn:max((xs:positiveInteger(\"999999999999999999\"),xs:positiveInteger(\"1\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:positiveInteger(lower bound) $arg2 = xs:positiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnMaxpint2args4() {
    final XQuery query = new XQuery(
      "fn:max((xs:positiveInteger(\"1\"),xs:positiveInteger(\"52704602390610033\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("52704602390610033")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:positiveInteger(lower bound) $arg2 = xs:positiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnMaxpint2args5() {
    final XQuery query = new XQuery(
      "fn:max((xs:positiveInteger(\"1\"),xs:positiveInteger(\"999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnMaxsht1args1() {
    final XQuery query = new XQuery(
      "fn:max((xs:short(\"-32768\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-32768")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg = xs:short(mid range) .
   */
  @org.junit.Test
  public void fnMaxsht1args2() {
    final XQuery query = new XQuery(
      "fn:max((xs:short(\"-5324\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-5324")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg = xs:short(upper bound) .
   */
  @org.junit.Test
  public void fnMaxsht1args3() {
    final XQuery query = new XQuery(
      "fn:max((xs:short(\"32767\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("32767")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:short(lower bound) $arg2 = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnMaxsht2args1() {
    final XQuery query = new XQuery(
      "fn:max((xs:short(\"-32768\"),xs:short(\"-32768\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-32768")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:short(mid range) $arg2 = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnMaxsht2args2() {
    final XQuery query = new XQuery(
      "fn:max((xs:short(\"-5324\"),xs:short(\"-32768\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-5324")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:short(upper bound) $arg2 = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnMaxsht2args3() {
    final XQuery query = new XQuery(
      "fn:max((xs:short(\"32767\"),xs:short(\"-32768\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("32767")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:short(lower bound) $arg2 = xs:short(mid range) .
   */
  @org.junit.Test
  public void fnMaxsht2args4() {
    final XQuery query = new XQuery(
      "fn:max((xs:short(\"-32768\"),xs:short(\"-5324\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-5324")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:short(lower bound) $arg2 = xs:short(upper bound) .
   */
  @org.junit.Test
  public void fnMaxsht2args5() {
    final XQuery query = new XQuery(
      "fn:max((xs:short(\"-32768\"),xs:short(\"32767\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("32767")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnMaxulng1args1() {
    final XQuery query = new XQuery(
      "fn:max((xs:unsignedLong(\"0\")))",
      ctx);
    try {
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
   *  Evaluates The "max" function with the arguments set as follows: $arg = xs:unsignedLong(mid range) .
   */
  @org.junit.Test
  public void fnMaxulng1args2() {
    final XQuery query = new XQuery(
      "fn:max((xs:unsignedLong(\"130747108607674654\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("130747108607674654")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg = xs:unsignedLong(upper bound) .
   */
  @org.junit.Test
  public void fnMaxulng1args3() {
    final XQuery query = new XQuery(
      "fn:max((xs:unsignedLong(\"184467440737095516\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("184467440737095516")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:unsignedLong(lower bound) $arg2 = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnMaxulng2args1() {
    final XQuery query = new XQuery(
      "fn:max((xs:unsignedLong(\"0\"),xs:unsignedLong(\"0\")))",
      ctx);
    try {
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
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:unsignedLong(mid range) $arg2 = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnMaxulng2args2() {
    final XQuery query = new XQuery(
      "fn:max((xs:unsignedLong(\"130747108607674654\"),xs:unsignedLong(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("130747108607674654")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:unsignedLong(upper bound) $arg2 = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnMaxulng2args3() {
    final XQuery query = new XQuery(
      "fn:max((xs:unsignedLong(\"184467440737095516\"),xs:unsignedLong(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("184467440737095516")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:unsignedLong(lower bound) $arg2 = xs:unsignedLong(mid range) .
   */
  @org.junit.Test
  public void fnMaxulng2args4() {
    final XQuery query = new XQuery(
      "fn:max((xs:unsignedLong(\"0\"),xs:unsignedLong(\"130747108607674654\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("130747108607674654")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:unsignedLong(lower bound) $arg2 = xs:unsignedLong(upper bound) .
   */
  @org.junit.Test
  public void fnMaxulng2args5() {
    final XQuery query = new XQuery(
      "fn:max((xs:unsignedLong(\"0\"),xs:unsignedLong(\"184467440737095516\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("184467440737095516")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnMaxusht1args1() {
    final XQuery query = new XQuery(
      "fn:max((xs:unsignedShort(\"0\")))",
      ctx);
    try {
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
   *  Evaluates The "max" function with the arguments set as follows: $arg = xs:unsignedShort(mid range) .
   */
  @org.junit.Test
  public void fnMaxusht1args2() {
    final XQuery query = new XQuery(
      "fn:max((xs:unsignedShort(\"44633\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("44633")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg = xs:unsignedShort(upper bound) .
   */
  @org.junit.Test
  public void fnMaxusht1args3() {
    final XQuery query = new XQuery(
      "fn:max((xs:unsignedShort(\"65535\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("65535")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:unsignedShort(lower bound) $arg2 = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnMaxusht2args1() {
    final XQuery query = new XQuery(
      "fn:max((xs:unsignedShort(\"0\"),xs:unsignedShort(\"0\")))",
      ctx);
    try {
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
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:unsignedShort(mid range) $arg2 = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnMaxusht2args2() {
    final XQuery query = new XQuery(
      "fn:max((xs:unsignedShort(\"44633\"),xs:unsignedShort(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("44633")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:unsignedShort(upper bound) $arg2 = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnMaxusht2args3() {
    final XQuery query = new XQuery(
      "fn:max((xs:unsignedShort(\"65535\"),xs:unsignedShort(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("65535")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:unsignedShort(lower bound) $arg2 = xs:unsignedShort(mid range) .
   */
  @org.junit.Test
  public void fnMaxusht2args4() {
    final XQuery query = new XQuery(
      "fn:max((xs:unsignedShort(\"0\"),xs:unsignedShort(\"44633\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("44633")
    );
  }

  /**
   *  Evaluates The "max" function with the arguments set as follows: $arg1 = xs:unsignedShort(lower bound) $arg2 = xs:unsignedShort(upper bound) .
   */
  @org.junit.Test
  public void fnMaxusht2args5() {
    final XQuery query = new XQuery(
      "fn:max((xs:unsignedShort(\"0\"),xs:unsignedShort(\"65535\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("65535")
    );
  }
}
