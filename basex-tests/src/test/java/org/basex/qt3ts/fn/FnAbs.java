package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnAbs extends QT3TestSet {

  /**
   * Test: K-ABSFunc-1 `abs()`. .
   */
  @org.junit.Test
  public void kABSFunc1() {
    final XQuery query = new XQuery(
      "abs()",
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
   * Test: K-ABSFunc-2 `abs(1, 2)`. .
   */
  @org.junit.Test
  public void kABSFunc2() {
    final XQuery query = new XQuery(
      "abs(1, 2)",
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
   * Test: K-ABSFunc-3 `empty(abs(()))`. .
   */
  @org.junit.Test
  public void kABSFunc3() {
    final XQuery query = new XQuery(
      "empty(abs(()))",
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
   * Test: K-ABSFunc-4 `abs(10.5) eq 10.5`. .
   */
  @org.junit.Test
  public void kABSFunc4() {
    final XQuery query = new XQuery(
      "abs(10.5) eq 10.5",
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
   * Test: K-ABSFunc-5 `abs(-10.5) eq 10.5`. .
   */
  @org.junit.Test
  public void kABSFunc5() {
    final XQuery query = new XQuery(
      "abs(-10.5) eq 10.5",
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
   * Test: K-ABSFunc-6 `abs("a string")`. .
   */
  @org.junit.Test
  public void kABSFunc6() {
    final XQuery query = new XQuery(
      "abs(\"a string\")",
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
   * Test: K2-ABSFunc-1 type xs:integer. .
   */
  @org.junit.Test
  public void k2ABSFunc1() {
    final XQuery query = new XQuery(
      "fn:abs(xs:integer(-4))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "4")
    );
  }

  /**
   * Test: K2-ABSFunc-10 type xs:unsignedInt. .
   */
  @org.junit.Test
  public void k2ABSFunc10() {
    final XQuery query = new XQuery(
      "fn:abs(xs:unsignedInt(4))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "4")
    );
  }

  /**
   * Test: K2-ABSFunc-11 type xs:unsignedShort. .
   */
  @org.junit.Test
  public void k2ABSFunc11() {
    final XQuery query = new XQuery(
      "fn:abs(xs:unsignedShort(4))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "4")
    );
  }

  /**
   * Test: K2-ABSFunc-12 type xs:unsignedByte. .
   */
  @org.junit.Test
  public void k2ABSFunc12() {
    final XQuery query = new XQuery(
      "fn:abs(xs:unsignedByte(4))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "4")
    );
  }

  /**
   * Test: K2-ABSFunc-13 type xs:positiveInteger. .
   */
  @org.junit.Test
  public void k2ABSFunc13() {
    final XQuery query = new XQuery(
      "fn:abs(xs:positiveInteger(4))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "4")
    );
  }

  /**
   * Test: K2-ABSFunc-14 xs:integer and check the return type. .
   */
  @org.junit.Test
  public void k2ABSFunc14() {
    final XQuery query = new XQuery(
      "fn:abs(xs:integer(-4)) instance of xs:integer",
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
   * Test: K2-ABSFunc-15 xs:nonPositiveInteger and check the return type. .
   */
  @org.junit.Test
  public void k2ABSFunc15() {
    final XQuery query = new XQuery(
      "fn:abs(xs:nonPositiveInteger(-4)) instance of xs:integer",
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
   * Test: K2-ABSFunc-16 xs:negativeInteger and check the return type. .
   */
  @org.junit.Test
  public void k2ABSFunc16() {
    final XQuery query = new XQuery(
      "fn:abs(xs:negativeInteger(-4)) instance of xs:integer",
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
   * Test: K2-ABSFunc-17 xs:long and check the return type. .
   */
  @org.junit.Test
  public void k2ABSFunc17() {
    final XQuery query = new XQuery(
      "fn:abs(xs:long(-4)) instance of xs:integer",
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
   * Test: K2-ABSFunc-18 xs:int and check the return type. .
   */
  @org.junit.Test
  public void k2ABSFunc18() {
    final XQuery query = new XQuery(
      "fn:abs(xs:int(-4)) instance of xs:integer",
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
   * Test: K2-ABSFunc-19 xs:short and check the return type. .
   */
  @org.junit.Test
  public void k2ABSFunc19() {
    final XQuery query = new XQuery(
      "fn:abs(xs:short(-4)) instance of xs:integer",
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
   * Test: K2-ABSFunc-2 type xs:nonPositiveInteger. .
   */
  @org.junit.Test
  public void k2ABSFunc2() {
    final XQuery query = new XQuery(
      "fn:abs(xs:nonPositiveInteger(-4))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "4")
    );
  }

  /**
   * Test: K2-ABSFunc-20 xs:byte and check the return type. .
   */
  @org.junit.Test
  public void k2ABSFunc20() {
    final XQuery query = new XQuery(
      "fn:abs(xs:byte(-4)) instance of xs:integer",
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
   * Test: K2-ABSFunc-21 xs:nonNegativeInteger and check the return type. .
   */
  @org.junit.Test
  public void k2ABSFunc21() {
    final XQuery query = new XQuery(
      "fn:abs(xs:nonNegativeInteger(4)) instance of xs:integer",
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
   * Test: K2-ABSFunc-22 xs:unsignedLong and check the return type. .
   */
  @org.junit.Test
  public void k2ABSFunc22() {
    final XQuery query = new XQuery(
      "fn:abs(xs:unsignedLong(4)) instance of xs:integer",
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
   * Test: K2-ABSFunc-23 xs:unsignedInt and check the return type. .
   */
  @org.junit.Test
  public void k2ABSFunc23() {
    final XQuery query = new XQuery(
      "fn:abs(xs:unsignedInt(4)) instance of xs:integer",
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
   * Test: K2-ABSFunc-24 xs:unsignedShort and check the return type. .
   */
  @org.junit.Test
  public void k2ABSFunc24() {
    final XQuery query = new XQuery(
      "fn:abs(xs:unsignedShort(4)) instance of xs:integer",
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
   * Test: K2-ABSFunc-25 xs:unsignedByte and check the return type. .
   */
  @org.junit.Test
  public void k2ABSFunc25() {
    final XQuery query = new XQuery(
      "fn:abs(xs:unsignedByte(4)) instance of xs:integer",
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
   * Test: K2-ABSFunc-26 xs:positiveInteger and check the return type. .
   */
  @org.junit.Test
  public void k2ABSFunc26() {
    final XQuery query = new XQuery(
      "fn:abs(xs:positiveInteger(4)) instance of xs:integer",
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
   * Test: K2-ABSFunc-27 xs:decimal and check the return type(negative test). .
   */
  @org.junit.Test
  public void k2ABSFunc27() {
    final XQuery query = new XQuery(
      "fn:abs(1.1) instance of xs:integer",
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
   * Test: K2-ABSFunc-28 xs:double and check the return type(negative test). .
   */
  @org.junit.Test
  public void k2ABSFunc28() {
    final XQuery query = new XQuery(
      "fn:abs(1e1) instance of xs:integer",
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
   * Test: K2-ABSFunc-29 xs:float and check the return type(negative test). .
   */
  @org.junit.Test
  public void k2ABSFunc29() {
    final XQuery query = new XQuery(
      "fn:abs(xs:float(1)) instance of xs:double",
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
   * Test: K2-ABSFunc-3 type xs:negativeInteger. .
   */
  @org.junit.Test
  public void k2ABSFunc3() {
    final XQuery query = new XQuery(
      "fn:abs(xs:negativeInteger(-4))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "4")
    );
  }

  /**
   * Test: K2-ABSFunc-30 xs:integer and check the return type(negative test). .
   */
  @org.junit.Test
  public void k2ABSFunc30() {
    final XQuery query = new XQuery(
      "fn:abs(xs:float(1)) instance of xs:double",
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
   * Test: K2-ABSFunc-31 xs:decimal and check the return type. .
   */
  @org.junit.Test
  public void k2ABSFunc31() {
    final XQuery query = new XQuery(
      "fn:abs(1.1) instance of xs:decimal",
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
   * Test: K2-ABSFunc-32 xs:double and check the return type. .
   */
  @org.junit.Test
  public void k2ABSFunc32() {
    final XQuery query = new XQuery(
      "fn:abs(1e1) instance of xs:double",
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
   * Test: K2-ABSFunc-33 xs:float and check the return type. .
   */
  @org.junit.Test
  public void k2ABSFunc33() {
    final XQuery query = new XQuery(
      "fn:abs(xs:float(1)) instance of xs:float",
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
   * Test: K2-ABSFunc-34 xs:integer and check the return type. .
   */
  @org.junit.Test
  public void k2ABSFunc34() {
    final XQuery query = new XQuery(
      "fn:abs(xs:integer(1)) instance of xs:integer",
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
   * Test: K2-ABSFunc-35 is properly inferred for unsignedShort. .
   */
  @org.junit.Test
  public void k2ABSFunc35() {
    final XQuery query = new XQuery(
      "abs(xs:unsignedShort(.)) instance of xs:unsignedShort",
      ctx);
    try {
      query.context(node(file("fn/abs/e0.xml")));
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
        assertBoolean(true)
      )
    );
  }

  /**
   * Test: K2-ABSFunc-36 is properly inferred for unsignedLong. .
   */
  @org.junit.Test
  public void k2ABSFunc36() {
    final XQuery query = new XQuery(
      "abs(xs:unsignedLong(.)) instance of xs:unsignedLong",
      ctx);
    try {
      query.context(node(file("fn/abs/e0.xml")));
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
        assertBoolean(true)
      )
    );
  }

  /**
   * Test: K2-ABSFunc-37 is properly inferred for unsignedInt. .
   */
  @org.junit.Test
  public void k2ABSFunc37() {
    final XQuery query = new XQuery(
      "abs(xs:unsignedInt(.)) instance of xs:unsignedInt",
      ctx);
    try {
      query.context(node(file("fn/abs/e0.xml")));
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
        assertBoolean(true)
      )
    );
  }

  /**
   * Test: K2-ABSFunc-38 is properly inferred for unsignedByte. .
   */
  @org.junit.Test
  public void k2ABSFunc38() {
    final XQuery query = new XQuery(
      "abs(xs:unsignedByte(.)) instance of xs:unsignedByte",
      ctx);
    try {
      query.context(node(file("fn/abs/e0.xml")));
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
        assertBoolean(true)
      )
    );
  }

  /**
   * Test: K2-ABSFunc-39 is properly inferred for positiveInteger. .
   */
  @org.junit.Test
  public void k2ABSFunc39() {
    final XQuery query = new XQuery(
      "abs(xs:positiveInteger(.)) instance of xs:positiveInteger",
      ctx);
    try {
      query.context(node(file("fn/abs/e1.xml")));
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
        assertBoolean(true)
      )
    );
  }

  /**
   * Test: K2-ABSFunc-4 type xs:long. .
   */
  @org.junit.Test
  public void k2ABSFunc4() {
    final XQuery query = new XQuery(
      "fn:abs(xs:long(-4))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "4")
    );
  }

  /**
   * Test: K2-ABSFunc-40 is properly inferred for nonPositiveInteger. .
   */
  @org.junit.Test
  public void k2ABSFunc40() {
    final XQuery query = new XQuery(
      "abs(xs:nonPositiveInteger(.)) instance of\n" +
      "         xs:nonPositiveInteger",
      ctx);
    try {
      query.context(node(file("fn/abs/e0.xml")));
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
        assertBoolean(true)
      )
    );
  }

  /**
   * Test: K2-ABSFunc-41 is properly inferred for nonNegativeInteger. .
   */
  @org.junit.Test
  public void k2ABSFunc41() {
    final XQuery query = new XQuery(
      "abs(xs:nonNegativeInteger(.)) instance of\n" +
      "         xs:nonNegativeInteger",
      ctx);
    try {
      query.context(node(file("fn/abs/e0.xml")));
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
        assertBoolean(true)
      )
    );
  }

  /**
   * Test: K2-ABSFunc-42 is properly inferred for negativeInteger. .
   */
  @org.junit.Test
  public void k2ABSFunc42() {
    final XQuery query = new XQuery(
      "abs(xs:negativeInteger(.)) instance of xs:negativeInteger",
      ctx);
    try {
      query.context(node(file("fn/abs/e-1.xml")));
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
        assertBoolean(true)
      )
    );
  }

  /**
   * Test: K2-ABSFunc-43 is properly inferred for long. .
   */
  @org.junit.Test
  public void k2ABSFunc43() {
    final XQuery query = new XQuery(
      "abs(xs:long(.)) instance of xs:long",
      ctx);
    try {
      query.context(node(file("fn/abs/e0.xml")));
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
        assertBoolean(true)
      )
    );
  }

  /**
   * Test: K2-ABSFunc-44 is properly inferred for int. .
   */
  @org.junit.Test
  public void k2ABSFunc44() {
    final XQuery query = new XQuery(
      "abs(xs:int(.)) instance of xs:int",
      ctx);
    try {
      query.context(node(file("fn/abs/e0.xml")));
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
        assertBoolean(true)
      )
    );
  }

  /**
   * Test: K2-ABSFunc-45 is properly inferred for short. .
   */
  @org.junit.Test
  public void k2ABSFunc45() {
    final XQuery query = new XQuery(
      "abs(xs:short(.)) instance of xs:short",
      ctx);
    try {
      query.context(node(file("fn/abs/e0.xml")));
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
        assertBoolean(true)
      )
    );
  }

  /**
   * Test: K2-ABSFunc-46 is properly inferred for byte. .
   */
  @org.junit.Test
  public void k2ABSFunc46() {
    final XQuery query = new XQuery(
      "abs(xs:byte(.)) instance of xs:byte",
      ctx);
    try {
      query.context(node(file("fn/abs/e0.xml")));
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
        assertBoolean(true)
      )
    );
  }

  /**
   * Test: K2-ABSFunc-47 .
   */
  @org.junit.Test
  public void k2ABSFunc47() {
    final XQuery query = new XQuery(
      "fn:abs(xs:float(\"-INF\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "INF")
    );
  }

  /**
   * Test: K2-ABSFunc-48 -INF/xs:double. .
   */
  @org.junit.Test
  public void k2ABSFunc48() {
    final XQuery query = new XQuery(
      "fn:abs(xs:double(\"-INF\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "INF")
    );
  }

  /**
   * Test: K2-ABSFunc-49 .
   */
  @org.junit.Test
  public void k2ABSFunc49() {
    final XQuery query = new XQuery(
      "fn:abs(xs:float(\"INF\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "INF")
    );
  }

  /**
   * Test: K2-ABSFunc-5 type xs:int. .
   */
  @org.junit.Test
  public void k2ABSFunc5() {
    final XQuery query = new XQuery(
      "fn:abs(xs:int(-4))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "4")
    );
  }

  /**
   * Test: K2-ABSFunc-50 -INF/xs:double. .
   */
  @org.junit.Test
  public void k2ABSFunc50() {
    final XQuery query = new XQuery(
      "fn:abs(xs:double(\"INF\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "INF")
    );
  }

  /**
   * Test: K2-ABSFunc-6 type xs:short. .
   */
  @org.junit.Test
  public void k2ABSFunc6() {
    final XQuery query = new XQuery(
      "fn:abs(xs:short(-4))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "4")
    );
  }

  /**
   * Test: K2-ABSFunc-7 type xs:byte. .
   */
  @org.junit.Test
  public void k2ABSFunc7() {
    final XQuery query = new XQuery(
      "fn:abs(xs:byte(-4))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "4")
    );
  }

  /**
   * Test: K2-ABSFunc-8 type xs:nonNegativeInteger. .
   */
  @org.junit.Test
  public void k2ABSFunc8() {
    final XQuery query = new XQuery(
      "fn:abs(xs:nonNegativeInteger(4))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "4")
    );
  }

  /**
   * Test: K2-ABSFunc-9 type xs:unsignedLong. .
   */
  @org.junit.Test
  public void k2ABSFunc9() {
    final XQuery query = new XQuery(
      "fn:abs(xs:unsignedLong(4))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "4")
    );
  }

  /**
   * test fn:abs where input type is uncertain.
   */
  @org.junit.Test
  public void cbclAbs001() {
    final XQuery query = new XQuery(
      "fn:abs( fn:reverse( (1, xs:decimal(2.2), xs:float(3.3), xs:double(4.4)) )[2] )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3.3")
    );
  }

  /**
   * numeric types. Author: Oliver Hallam Date: 2010-03-15.
   */
  @org.junit.Test
  public void fnAbs1() {
    final XQuery query = new XQuery(
      "string-join(for $x in (1, xs:decimal(2), xs:float(3), xs:double(4)) return \n" +
      "           if ((abs($x)) instance of xs:integer) then \"integer\" \n" +
      "           else if ((abs($x)) instance of xs:decimal) then \"decimal\" \n" +
      "           else if ((abs($x)) instance of xs:float) then \"float\"\n" +
      "           else if ((abs($x)) instance of xs:double) then \"double\" else error(), \" \")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("\"integer decimal float double\"")
    );
  }

  /**
   * Test: fn-abs-more-args-001.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: arg: double .
   */
  @org.junit.Test
  public void fnAbsMoreArgs001() {
    final XQuery query = new XQuery(
      "fn:abs(xs:double(\"-0\"))",
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
        assertEq("0")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Test: fn-abs-more-args-002.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: arg: double .
   */
  @org.junit.Test
  public void fnAbsMoreArgs002() {
    final XQuery query = new XQuery(
      "fn:abs(xs:double(\"-INF\"))",
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
        assertStringValue(false, "INF")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Test: fn-abs-more-args-003.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: arg: double .
   */
  @org.junit.Test
  public void fnAbsMoreArgs003() {
    final XQuery query = new XQuery(
      "fn:abs(xs:double(\"INF\"))",
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
        assertStringValue(false, "INF")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Test: fn-abs-more-args-004.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: arg: double .
   */
  @org.junit.Test
  public void fnAbsMoreArgs004() {
    final XQuery query = new XQuery(
      "fn:abs(xs:double(\"NaN\"))",
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
        assertStringValue(false, "NaN")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Test: fn-abs-more-args-005.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: arg: double .
   */
  @org.junit.Test
  public void fnAbsMoreArgs005() {
    final XQuery query = new XQuery(
      "fn:abs(xs:double(\"NaN\"))",
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
        assertStringValue(false, "NaN")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Test: fn-abs-more-args-006.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: arg: decimal .
   */
  @org.junit.Test
  public void fnAbsMoreArgs006() {
    final XQuery query = new XQuery(
      "fn:abs(xs:decimal(\"-0\"))",
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
        assertEq("0")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Test: fn-abs-more-args-007.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: arg: decimal .
   */
  @org.junit.Test
  public void fnAbsMoreArgs007() {
    final XQuery query = new XQuery(
      "fn:abs(xs:decimal(\"0\"))",
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
        assertEq("0")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Test: fn-abs-more-args-008.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: arg: float .
   */
  @org.junit.Test
  public void fnAbsMoreArgs008() {
    final XQuery query = new XQuery(
      "fn:abs(xs:float(\"-0\"))",
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
        assertEq("0")
      &&
        assertType("xs:float")
      )
    );
  }

  /**
   * Test: fn-abs-more-args-009.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: arg: float .
   */
  @org.junit.Test
  public void fnAbsMoreArgs009() {
    final XQuery query = new XQuery(
      "fn:abs(xs:float(\"-INF\"))",
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
        assertStringValue(false, "INF")
      &&
        assertType("xs:float")
      )
    );
  }

  /**
   * Test: fn-abs-more-args-010.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: arg: float .
   */
  @org.junit.Test
  public void fnAbsMoreArgs010() {
    final XQuery query = new XQuery(
      "fn:abs(xs:float(\"INF\"))",
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
        assertStringValue(false, "INF")
      &&
        assertType("xs:float")
      )
    );
  }

  /**
   * Test: fn-abs-more-args-011.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: arg: float .
   */
  @org.junit.Test
  public void fnAbsMoreArgs011() {
    final XQuery query = new XQuery(
      "fn:abs(xs:float(\"NaN\"))",
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
        assertStringValue(false, "NaN")
      &&
        assertType("xs:float")
      )
    );
  }

  /**
   * Test: fn-abs-more-args-012.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: arg: float .
   */
  @org.junit.Test
  public void fnAbsMoreArgs012() {
    final XQuery query = new XQuery(
      "fn:abs(xs:float(\"NaN\"))",
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
        assertStringValue(false, "NaN")
      &&
        assertType("xs:float")
      )
    );
  }

  /**
   * Test: fn-abs-more-args-013.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: arg: int .
   */
  @org.junit.Test
  public void fnAbsMoreArgs013() {
    final XQuery query = new XQuery(
      "fn:abs(xs:int(\"0\"))",
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
        assertEq("0")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: fn-abs-more-args-014.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: arg: int .
   */
  @org.junit.Test
  public void fnAbsMoreArgs014() {
    final XQuery query = new XQuery(
      "fn:abs(xs:int(\"-0\"))",
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
        assertEq("0")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: fn-abs-more-args-015.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: arg: integer .
   */
  @org.junit.Test
  public void fnAbsMoreArgs015() {
    final XQuery query = new XQuery(
      "fn:abs(xs:integer(\"0\"))",
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
        assertEq("0")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: fn-abs-more-args-016.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: arg: integer .
   */
  @org.junit.Test
  public void fnAbsMoreArgs016() {
    final XQuery query = new XQuery(
      "fn:abs(xs:integer(\"-0\"))",
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
        assertEq("0")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: fn-abs-more-args-017.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: arg: long .
   */
  @org.junit.Test
  public void fnAbsMoreArgs017() {
    final XQuery query = new XQuery(
      "fn:abs(xs:long(\"0\"))",
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
        assertEq("0")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: fn-abs-more-args-018.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: arg: long .
   */
  @org.junit.Test
  public void fnAbsMoreArgs018() {
    final XQuery query = new XQuery(
      "fn:abs(xs:long(\"-0\"))",
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
        assertEq("0")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: fn-abs-more-args-019.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: arg: nonNegativeInteger .
   */
  @org.junit.Test
  public void fnAbsMoreArgs019() {
    final XQuery query = new XQuery(
      "fn:abs(xs:nonNegativeInteger(\"-0\"))",
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
        assertEq("0")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: fn-abs-more-args-020.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: arg: nonPositiveIntege .
   */
  @org.junit.Test
  public void fnAbsMoreArgs020() {
    final XQuery query = new XQuery(
      "fn:abs(xs:nonPositiveInteger(\"-0\"))",
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
        assertEq("0")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: fn-abs-more-args-021.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: arg: short .
   */
  @org.junit.Test
  public void fnAbsMoreArgs021() {
    final XQuery query = new XQuery(
      "fn:abs(xs:short(\"0\"))",
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
        assertEq("0")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: fn-abs-more-args-022.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: arg: short .
   */
  @org.junit.Test
  public void fnAbsMoreArgs022() {
    final XQuery query = new XQuery(
      "fn:abs(xs:short(\"-0\"))",
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
        assertEq("0")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: fn-abs-more-args-023.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: arg: unsignedLong .
   */
  @org.junit.Test
  public void fnAbsMoreArgs023() {
    final XQuery query = new XQuery(
      "fn:abs(xs:unsignedLong(\"-0\"))",
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
        assertEq("0")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: fn-abs-more-args-024.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: arg: unsignedShort .
   */
  @org.junit.Test
  public void fnAbsMoreArgs024() {
    final XQuery query = new XQuery(
      "fn:abs(xs:unsignedShort(\"-0\"))",
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
        assertEq("0")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: fn-abs-more-args-025.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs025() {
    final XQuery query = new XQuery(
      "fn:abs(xs:decimal(\"-INF\"))",
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
   * Test: fn-abs-more-args-026.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs026() {
    final XQuery query = new XQuery(
      "fn:abs(xs:decimal(\"INF\"))",
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
   * Test: fn-abs-more-args-027.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs027() {
    final XQuery query = new XQuery(
      "fn:abs(xs:decimal(\"-NaN\"))",
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
   * Test: fn-abs-more-args-028.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs028() {
    final XQuery query = new XQuery(
      "fn:abs(xs:decimal(\"NaN\"))",
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
   * Test: fn-abs-more-args-029.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs029() {
    final XQuery query = new XQuery(
      "fn:abs(xs:int(\"-INF\"))",
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
   * Test: fn-abs-more-args-030.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs030() {
    final XQuery query = new XQuery(
      "fn:abs(xs:int(\"INF\"))",
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
   * Test: fn-abs-more-args-031.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs031() {
    final XQuery query = new XQuery(
      "fn:abs(xs:int(\"-NaN\"))",
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
   * Test: fn-abs-more-args-032.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs032() {
    final XQuery query = new XQuery(
      "fn:abs(xs:int(\"NaN\"))",
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
   * Test: fn-abs-more-args-033.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs033() {
    final XQuery query = new XQuery(
      "fn:abs(xs:integer(\"-INF\"))",
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
   * Test: fn-abs-more-args-034.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs034() {
    final XQuery query = new XQuery(
      "fn:abs(xs:integer(\"INF\"))",
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
   * Test: fn-abs-more-args-035.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs035() {
    final XQuery query = new XQuery(
      "fn:abs(xs:integer(\"-NaN\"))",
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
   * Test: fn-abs-more-args-036.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs036() {
    final XQuery query = new XQuery(
      "fn:abs(xs:integer(\"NaN\"))",
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
   * Test: fn-abs-more-args-037.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs037() {
    final XQuery query = new XQuery(
      "fn:abs(xs:long(\"-INF\"))",
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
   * Test: fn-abs-more-args-038.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs038() {
    final XQuery query = new XQuery(
      "fn:abs(xs:long(\"INF\"))",
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
   * Test: fn-abs-more-args-039.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs039() {
    final XQuery query = new XQuery(
      "fn:abs(xs:long(\"-NaN\"))",
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
   * Test: fn-abs-more-args-040.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs040() {
    final XQuery query = new XQuery(
      "fn:abs(xs:long(\"NaN\"))",
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
   * Test: fn-abs-more-args-041.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs041() {
    final XQuery query = new XQuery(
      "fn:abs(xs:negativeInteger(\"-0\"))",
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
   * Test: fn-abs-more-args-042.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs042() {
    final XQuery query = new XQuery(
      "fn:abs(xs:negativeInteger(\"0\"))",
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
   * Test: fn-abs-more-args-043.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs043() {
    final XQuery query = new XQuery(
      "fn:abs(xs:negativeInteger(\"2\"))",
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
   * Test: fn-abs-more-args-044.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs044() {
    final XQuery query = new XQuery(
      "fn:abs(xs:negativeInteger(\"-INF\"))",
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
   * Test: fn-abs-more-args-045.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs045() {
    final XQuery query = new XQuery(
      "fn:abs(xs:negativeInteger(\"INF\"))",
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
   * Test: fn-abs-more-args-046.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs046() {
    final XQuery query = new XQuery(
      "fn:abs(xs:negativeInteger(\"-NaN\"))",
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
   * Test: fn-abs-more-args-047.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs047() {
    final XQuery query = new XQuery(
      "fn:abs(xs:negativeInteger(\"NaN\"))",
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
   * Test: fn-abs-more-args-048.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs048() {
    final XQuery query = new XQuery(
      "fn:abs(xs:nonNegativeInteger(\"-2\"))",
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
   * Test: fn-abs-more-args-049.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs049() {
    final XQuery query = new XQuery(
      "fn:abs(xs:nonNegativeInteger(\"-INF\"))",
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
   * Test: fn-abs-more-args-050.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs050() {
    final XQuery query = new XQuery(
      "fn:abs(xs:nonNegativeInteger(\"INF\"))",
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
   * Test: fn-abs-more-args-051.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs051() {
    final XQuery query = new XQuery(
      "fn:abs(xs:nonNegativeInteger(\"-NaN\"))",
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
   * Test: fn-abs-more-args-052.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs052() {
    final XQuery query = new XQuery(
      "fn:abs(xs:nonNegativeInteger(\"NaN\"))",
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
   * Test: fn-abs-more-args-053.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs053() {
    final XQuery query = new XQuery(
      "fn:abs(xs:nonPositiveInteger(\"2\"))",
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
   * Test: fn-abs-more-args-054.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs054() {
    final XQuery query = new XQuery(
      "fn:abs(xs:nonPositiveInteger(\"-INF\"))",
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
   * Test: fn-abs-more-args-055.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs055() {
    final XQuery query = new XQuery(
      "fn:abs(xs:nonPositiveInteger(\"INF\"))",
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
   * Test: fn-abs-more-args-056.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs056() {
    final XQuery query = new XQuery(
      "fn:abs(xs:nonPositiveInteger(\"-NaN\"))",
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
   * Test: fn-abs-more-args-057.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs057() {
    final XQuery query = new XQuery(
      "fn:abs(xs:nonPositiveInteger(\"NaN\"))",
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
   * Test: fn-abs-more-args-058.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs058() {
    final XQuery query = new XQuery(
      "fn:abs(xs:positiveInteger(\"0\"))",
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
   * Test: fn-abs-more-args-059.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs059() {
    final XQuery query = new XQuery(
      "fn:abs(xs:positiveInteger(\"-0\"))",
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
   * Test: fn-abs-more-args-060.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs060() {
    final XQuery query = new XQuery(
      "fn:abs(xs:positiveInteger(\"-2\"))",
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
   * Test: fn-abs-more-args-061.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs061() {
    final XQuery query = new XQuery(
      "fn:abs(xs:positiveInteger(\"-INF\"))",
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
   * Test: fn-abs-more-args-062.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs062() {
    final XQuery query = new XQuery(
      "fn:abs(xs:positiveInteger(\"INF\"))",
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
   * Test: fn-abs-more-args-063.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs063() {
    final XQuery query = new XQuery(
      "fn:abs(xs:positiveInteger(\"-NaN\"))",
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
   * Test: fn-abs-more-args-064.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs064() {
    final XQuery query = new XQuery(
      "fn:abs(xs:positiveInteger(\"NaN\"))",
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
   * Test: fn-abs-more-args-065.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs065() {
    final XQuery query = new XQuery(
      "fn:abs(xs:short(\"-INF\"))",
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
   * Test: fn-abs-more-args-066.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs066() {
    final XQuery query = new XQuery(
      "fn:abs(xs:short(\"INF\"))",
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
   * Test: fn-abs-more-args-067.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs067() {
    final XQuery query = new XQuery(
      "fn:abs(xs:short(\"-NaN\"))",
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
   * Test: fn-abs-more-args-068.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs068() {
    final XQuery query = new XQuery(
      "fn:abs(xs:short(\"NaN\"))",
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
   * Test: fn-abs-more-args-069.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs069() {
    final XQuery query = new XQuery(
      "fn:abs(xs:unsignedLong(\"-INF\"))",
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
   * Test: fn-abs-more-args-070.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs070() {
    final XQuery query = new XQuery(
      "fn:abs(xs:unsignedLong(\"INF\"))",
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
   * Test: fn-abs-more-args-071.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs071() {
    final XQuery query = new XQuery(
      "fn:abs(xs:unsignedLong(\"-NaN\"))",
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
   * Test: fn-abs-more-args-072.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs072() {
    final XQuery query = new XQuery(
      "fn:abs(xs:unsignedLong(\"NaN\"))",
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
   * Test: fn-abs-more-args-073.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs073() {
    final XQuery query = new XQuery(
      "fn:abs(xs:unsignedShort(\"-INF\"))",
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
   * Test: fn-abs-more-args-074.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs074() {
    final XQuery query = new XQuery(
      "fn:abs(xs:unsignedShort(\"INF\"))",
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
   * Test: fn-abs-more-args-075.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs075() {
    final XQuery query = new XQuery(
      "fn:abs(xs:unsignedShort(\"-NaN\"))",
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
   * Test: fn-abs-more-args-076.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs076() {
    final XQuery query = new XQuery(
      "fn:abs(xs:unsignedShort(\"NaN\"))",
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
   * Test: fn-abs-more-args-077.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives XPTY0004 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs077() {
    final XQuery query = new XQuery(
      "fn:abs(xs:string(\"1\"))",
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
   * Test: fn-abs-more-args-078.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives XPTY0004 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs078() {
    final XQuery query = new XQuery(
      "fn:abs(xs:string(\"hello\"))",
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
   * Test: fn-abs-more-args-079.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives XPTY0004 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs079() {
    final XQuery query = new XQuery(
      "fn:abs(xs:boolean(\"1\"))",
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
   * Test: fn-abs-more-args-080.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives XPTY0004 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs080() {
    final XQuery query = new XQuery(
      "fn:abs(xs:boolean(\"0\"))",
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
   * Test: fn-abs-more-args-081.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives XPTY0004 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs081() {
    final XQuery query = new XQuery(
      "fn:abs(xs:boolean(fn:true()))",
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
   * Test: fn-abs-more-args-082.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives XPTY0004 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs082() {
    final XQuery query = new XQuery(
      "fn:abs(xs:boolean(fn:false()))",
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
   * Test: fn-abs-more-args-083.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives XPTY0004 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs083() {
    final XQuery query = new XQuery(
      "fn:abs(xs:date(\"2002-10-09\"))",
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
   * Test: fn-abs-more-args-084.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives XPTY0004 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs084() {
    final XQuery query = new XQuery(
      "fn:abs(xs:time(\"13:20:00-05:00\"))",
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
   * Test: fn-abs-more-args-085.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives XPTY0004 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs085() {
    final XQuery query = new XQuery(
      "fn:abs(xs:dateTime(\"2002-10-10T12:00:00-05:00\"))",
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
   * Test: fn-abs-more-args-086.xq Written By: Pulkita Tyagi Date: Thu Oct 27 03:56:31 2005 Purpose: Negative Test gives XPTY0004 .
   */
  @org.junit.Test
  public void fnAbsMoreArgs086() {
    final XQuery query = new XQuery(
      "fn:abs(xs:anyURI(\"www.examples.org\"))",
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
   * Test: absdbl1args-1 The "abs" function with the arguments set as follows: $arg = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnAbsdbl1args1() {
    final XQuery query = new XQuery(
      "fn:abs(xs:double(\"-1.7976931348623157E308\"))",
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
        assertEq("1.7976931348623157E308")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Test: absdbl1args-2 The "abs" function with the arguments set as follows: $arg = xs:double(mid range) .
   */
  @org.junit.Test
  public void fnAbsdbl1args2() {
    final XQuery query = new XQuery(
      "fn:abs(xs:double(\"0\"))",
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
        assertEq("0.0e0")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Test: absdbl1args-3 The "abs" function with the arguments set as follows: $arg = xs:double(upper bound) .
   */
  @org.junit.Test
  public void fnAbsdbl1args3() {
    final XQuery query = new XQuery(
      "fn:abs(xs:double(\"1.7976931348623157E308\"))",
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
        assertEq("1.7976931348623157E308")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Test: absdec1args-1 The "abs" function with the arguments set as follows: $arg = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnAbsdec1args1() {
    final XQuery query = new XQuery(
      "fn:abs(xs:decimal(\"-999999999999999999\"))",
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
        assertEq("999999999999999999")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Test: absdec1args-2 The "abs" function with the arguments set as follows: $arg = xs:decimal(mid range) .
   */
  @org.junit.Test
  public void fnAbsdec1args2() {
    final XQuery query = new XQuery(
      "fn:abs(xs:decimal(\"617375191608514839\"))",
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
        assertEq("617375191608514839")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Test: absdec1args-3 The "abs" function with the arguments set as follows: $arg = xs:decimal(upper bound) .
   */
  @org.junit.Test
  public void fnAbsdec1args3() {
    final XQuery query = new XQuery(
      "fn:abs(xs:decimal(\"999999999999999999\"))",
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
        assertEq("999999999999999999")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Test: absflt1args-1 The "abs" function with the arguments set as follows: $arg = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnAbsflt1args1() {
    final XQuery query = new XQuery(
      "fn:abs(xs:float(\"-3.4028235E38\"))",
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
        assertEq("xs:float(3.4028235E38)")
      &&
        assertType("xs:float")
      )
    );
  }

  /**
   * Test: absflt1args-2 The "abs" function with the arguments set as follows: $arg = xs:float(mid range) .
   */
  @org.junit.Test
  public void fnAbsflt1args2() {
    final XQuery query = new XQuery(
      "fn:abs(xs:float(\"0\"))",
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
        assertEq("0.0e0")
      &&
        assertType("xs:float")
      )
    );
  }

  /**
   * Test: absflt1args-3 The "abs" function with the arguments set as follows: $arg = xs:float(upper bound) .
   */
  @org.junit.Test
  public void fnAbsflt1args3() {
    final XQuery query = new XQuery(
      "fn:abs(xs:float(\"3.4028235E38\"))",
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
        assertEq("xs:float(3.4028235E38)")
      &&
        assertType("xs:float")
      )
    );
  }

  /**
   * Test: absint1args-1 The "abs" function with the arguments set as follows: $arg = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnAbsint1args1() {
    final XQuery query = new XQuery(
      "fn:abs(xs:int(\"-2147483648\"))",
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
        assertEq("2147483648")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: absint1args-2 The "abs" function with the arguments set as follows: $arg = xs:int(mid range) .
   */
  @org.junit.Test
  public void fnAbsint1args2() {
    final XQuery query = new XQuery(
      "fn:abs(xs:int(\"-1873914410\"))",
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
        assertEq("1873914410")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: absint1args-3 The "abs" function with the arguments set as follows: $arg = xs:int(upper bound) .
   */
  @org.junit.Test
  public void fnAbsint1args3() {
    final XQuery query = new XQuery(
      "fn:abs(xs:int(\"2147483647\"))",
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
        assertEq("2147483647")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: absintg1args-1 The "abs" function with the arguments set as follows: $arg = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnAbsintg1args1() {
    final XQuery query = new XQuery(
      "fn:abs(xs:integer(\"-999999999999999999\"))",
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
        assertEq("999999999999999999")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: absintg1args-2 The "abs" function with the arguments set as follows: $arg = xs:integer(mid range) .
   */
  @org.junit.Test
  public void fnAbsintg1args2() {
    final XQuery query = new XQuery(
      "fn:abs(xs:integer(\"830993497117024304\"))",
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
        assertEq("830993497117024304")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: absintg1args-3 The "abs" function with the arguments set as follows: $arg = xs:integer(upper bound) .
   */
  @org.junit.Test
  public void fnAbsintg1args3() {
    final XQuery query = new XQuery(
      "fn:abs(xs:integer(\"999999999999999999\"))",
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
        assertEq("999999999999999999")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: abslng1args-1 The "abs" function with the arguments set as follows: $arg = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnAbslng1args1() {
    final XQuery query = new XQuery(
      "fn:abs(xs:long(\"-92233720368547758\"))",
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
        assertEq("92233720368547758")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: abslng1args-2 The "abs" function with the arguments set as follows: $arg = xs:long(mid range) .
   */
  @org.junit.Test
  public void fnAbslng1args2() {
    final XQuery query = new XQuery(
      "fn:abs(xs:long(\"-47175562203048468\"))",
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
        assertEq("47175562203048468")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: abslng1args-3 The "abs" function with the arguments set as follows: $arg = xs:long(upper bound) .
   */
  @org.junit.Test
  public void fnAbslng1args3() {
    final XQuery query = new XQuery(
      "fn:abs(xs:long(\"92233720368547758\"))",
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
        assertEq("92233720368547758")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: absnint1args-1 The "abs" function with the arguments set as follows: $arg = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnAbsnint1args1() {
    final XQuery query = new XQuery(
      "fn:abs(xs:negativeInteger(\"-999999999999999999\"))",
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
        assertEq("999999999999999999")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: absnint1args-2 The "abs" function with the arguments set as follows: $arg = xs:negativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnAbsnint1args2() {
    final XQuery query = new XQuery(
      "fn:abs(xs:negativeInteger(\"-297014075999096793\"))",
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
        assertEq("297014075999096793")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: absnint1args-3 The "abs" function with the arguments set as follows: $arg = xs:negativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnAbsnint1args3() {
    final XQuery query = new XQuery(
      "fn:abs(xs:negativeInteger(\"-1\"))",
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
        assertEq("1")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: absnni1args-1 The "abs" function with the arguments set as follows: $arg = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnAbsnni1args1() {
    final XQuery query = new XQuery(
      "fn:abs(xs:nonNegativeInteger(\"0\"))",
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
        assertEq("0")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: absnni1args-2 The "abs" function with the arguments set as follows: $arg = xs:nonNegativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnAbsnni1args2() {
    final XQuery query = new XQuery(
      "fn:abs(xs:nonNegativeInteger(\"303884545991464527\"))",
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
        assertEq("303884545991464527")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: absnni1args-3 The "abs" function with the arguments set as follows: $arg = xs:nonNegativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnAbsnni1args3() {
    final XQuery query = new XQuery(
      "fn:abs(xs:nonNegativeInteger(\"999999999999999999\"))",
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
        assertEq("999999999999999999")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: absnpi1args-1 The "abs" function with the arguments set as follows: $arg = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnAbsnpi1args1() {
    final XQuery query = new XQuery(
      "fn:abs(xs:nonPositiveInteger(\"-999999999999999999\"))",
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
        assertEq("999999999999999999")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: absnpi1args-2 The "abs" function with the arguments set as follows: $arg = xs:nonPositiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnAbsnpi1args2() {
    final XQuery query = new XQuery(
      "fn:abs(xs:nonPositiveInteger(\"-475688437271870490\"))",
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
        assertEq("475688437271870490")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: absnpi1args-3 The "abs" function with the arguments set as follows: $arg = xs:nonPositiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnAbsnpi1args3() {
    final XQuery query = new XQuery(
      "fn:abs(xs:nonPositiveInteger(\"0\"))",
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
        assertEq("0")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: abspint1args-1 The "abs" function with the arguments set as follows: $arg = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnAbspint1args1() {
    final XQuery query = new XQuery(
      "fn:abs(xs:positiveInteger(\"1\"))",
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
        assertEq("1")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: abspint1args-2 The "abs" function with the arguments set as follows: $arg = xs:positiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnAbspint1args2() {
    final XQuery query = new XQuery(
      "fn:abs(xs:positiveInteger(\"52704602390610033\"))",
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
        assertEq("52704602390610033")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: abspint1args-3 The "abs" function with the arguments set as follows: $arg = xs:positiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnAbspint1args3() {
    final XQuery query = new XQuery(
      "fn:abs(xs:positiveInteger(\"999999999999999999\"))",
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
        assertEq("999999999999999999")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: abssht1args-1 The "abs" function with the arguments set as follows: $arg = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnAbssht1args1() {
    final XQuery query = new XQuery(
      "fn:abs(xs:short(\"-32768\"))",
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
        assertEq("32768")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: abssht1args-2 The "abs" function with the arguments set as follows: $arg = xs:short(mid range) .
   */
  @org.junit.Test
  public void fnAbssht1args2() {
    final XQuery query = new XQuery(
      "fn:abs(xs:short(\"-5324\"))",
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
        assertEq("5324")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: abssht1args-3 The "abs" function with the arguments set as follows: $arg = xs:short(upper bound) .
   */
  @org.junit.Test
  public void fnAbssht1args3() {
    final XQuery query = new XQuery(
      "fn:abs(xs:short(\"32767\"))",
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
        assertEq("32767")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: absulng1args-1 The "abs" function with the arguments set as follows: $arg = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnAbsulng1args1() {
    final XQuery query = new XQuery(
      "fn:abs(xs:unsignedLong(\"0\"))",
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
        assertEq("0")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: absulng1args-2 The "abs" function with the arguments set as follows: $arg = xs:unsignedLong(mid range) .
   */
  @org.junit.Test
  public void fnAbsulng1args2() {
    final XQuery query = new XQuery(
      "fn:abs(xs:unsignedLong(\"130747108607674654\"))",
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
        assertEq("130747108607674654")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: absulng1args-3 The "abs" function with the arguments set as follows: $arg = xs:unsignedLong(upper bound) .
   */
  @org.junit.Test
  public void fnAbsulng1args3() {
    final XQuery query = new XQuery(
      "fn:abs(xs:unsignedLong(\"184467440737095516\"))",
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
        assertEq("184467440737095516")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: absusht1args-1 The "abs" function with the arguments set as follows: $arg = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnAbsusht1args1() {
    final XQuery query = new XQuery(
      "fn:abs(xs:unsignedShort(\"0\"))",
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
        assertEq("0")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: absusht1args-2 The "abs" function with the arguments set as follows: $arg = xs:unsignedShort(mid range) .
   */
  @org.junit.Test
  public void fnAbsusht1args2() {
    final XQuery query = new XQuery(
      "fn:abs(xs:unsignedShort(\"44633\"))",
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
        assertEq("44633")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Test: absusht1args-3 The "abs" function with the arguments set as follows: $arg = xs:unsignedShort(upper bound) .
   */
  @org.junit.Test
  public void fnAbsusht1args3() {
    final XQuery query = new XQuery(
      "fn:abs(xs:unsignedShort(\"65535\"))",
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
        assertEq("65535")
      &&
        assertType("xs:integer")
      )
    );
  }
}
