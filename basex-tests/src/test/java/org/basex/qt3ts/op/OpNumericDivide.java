package org.basex.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the numeric-divide() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpNumericDivide extends QT3TestSet {

  /**
   *  A test whose essence is: `xs:double(6) div xs:double(2) eq 3`. .
   */
  @org.junit.Test
  public void kNumericDivide1() {
    final XQuery query = new XQuery(
      "xs:double(6) div xs:double(2) eq 3",
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
   *  A test whose essence is: `xs:integer(5) div xs:integer(2) ne 2.6`. .
   */
  @org.junit.Test
  public void kNumericDivide10() {
    final XQuery query = new XQuery(
      "xs:integer(5) div xs:integer(2) ne 2.6",
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
   *  A test whose essence is: `xs:decimal(5) div xs:decimal(2) eq 2.5`. .
   */
  @org.junit.Test
  public void kNumericDivide11() {
    final XQuery query = new XQuery(
      "xs:decimal(5) div xs:decimal(2) eq 2.5",
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
   *  A test whose essence is: `xs:decimal(5) div xs:decimal(2) ne 2.6`. .
   */
  @org.junit.Test
  public void kNumericDivide12() {
    final XQuery query = new XQuery(
      "xs:decimal(5) div xs:decimal(2) ne 2.6",
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
   *  A test whose essence is: `5 div 2 ne 2.6`. .
   */
  @org.junit.Test
  public void kNumericDivide13() {
    final XQuery query = new XQuery(
      "5 div 2 ne 2.6",
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
   *  A test whose essence is: `5.0 div 2.0 eq 2.5`. .
   */
  @org.junit.Test
  public void kNumericDivide14() {
    final XQuery query = new XQuery(
      "5.0 div 2.0 eq 2.5",
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
   *  A test whose essence is: `5.0 div 2.0 ne 2.6`. .
   */
  @org.junit.Test
  public void kNumericDivide15() {
    final XQuery query = new XQuery(
      "5.0 div 2.0 ne 2.6",
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
   *  A test whose essence is: `(xs:decimal(6) div xs:integer(2)) instance of xs:decimal`. .
   */
  @org.junit.Test
  public void kNumericDivide16() {
    final XQuery query = new XQuery(
      "(xs:decimal(6) div xs:integer(2)) instance of xs:decimal",
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
   *  A test whose essence is: `(xs:integer(6) div xs:decimal(2)) instance of xs:decimal`. .
   */
  @org.junit.Test
  public void kNumericDivide17() {
    final XQuery query = new XQuery(
      "(xs:integer(6) div xs:decimal(2)) instance of xs:decimal",
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
   *  A test whose essence is: `(xs:integer(6) div xs:integer(2)) instance of xs:decimal`. .
   */
  @org.junit.Test
  public void kNumericDivide18() {
    final XQuery query = new XQuery(
      "(xs:integer(6) div xs:integer(2)) instance of xs:decimal",
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
   *  A test whose essence is: `(xs:decimal(6) div xs:decimal(2)) instance of xs:decimal`. .
   */
  @org.junit.Test
  public void kNumericDivide19() {
    final XQuery query = new XQuery(
      "(xs:decimal(6) div xs:decimal(2)) instance of xs:decimal",
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
   *  A test whose essence is: `xs:decimal(6) div xs:decimal(2) eq 3`. .
   */
  @org.junit.Test
  public void kNumericDivide2() {
    final XQuery query = new XQuery(
      "xs:decimal(6) div xs:decimal(2) eq 3",
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
   *  A test whose essence is: `(xs:float(6) div xs:decimal(2)) instance of xs:float`. .
   */
  @org.junit.Test
  public void kNumericDivide20() {
    final XQuery query = new XQuery(
      "(xs:float(6) div xs:decimal(2)) instance of xs:float",
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
   *  A test whose essence is: `(xs:decimal(6) div xs:float(2)) instance of xs:float`. .
   */
  @org.junit.Test
  public void kNumericDivide21() {
    final XQuery query = new XQuery(
      "(xs:decimal(6) div xs:float(2)) instance of xs:float",
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
   *  A test whose essence is: `(xs:float(6) div xs:integer(2)) instance of xs:float`. .
   */
  @org.junit.Test
  public void kNumericDivide22() {
    final XQuery query = new XQuery(
      "(xs:float(6) div xs:integer(2)) instance of xs:float",
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
   *  A test whose essence is: `(xs:integer(6) div xs:float(2)) instance of xs:float`. .
   */
  @org.junit.Test
  public void kNumericDivide23() {
    final XQuery query = new XQuery(
      "(xs:integer(6) div xs:float(2)) instance of xs:float",
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
   *  A test whose essence is: `(xs:float(6) div xs:float(2)) instance of xs:float`. .
   */
  @org.junit.Test
  public void kNumericDivide24() {
    final XQuery query = new XQuery(
      "(xs:float(6) div xs:float(2)) instance of xs:float",
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
   *  A test whose essence is: `(xs:decimal(6) div xs:double(2)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNumericDivide25() {
    final XQuery query = new XQuery(
      "(xs:decimal(6) div xs:double(2)) instance of xs:double",
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
   *  A test whose essence is: `(xs:double(6) div xs:decimal(2)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNumericDivide26() {
    final XQuery query = new XQuery(
      "(xs:double(6) div xs:decimal(2)) instance of xs:double",
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
   *  A test whose essence is: `(xs:double(6) div xs:float(2)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNumericDivide27() {
    final XQuery query = new XQuery(
      "(xs:double(6) div xs:float(2)) instance of xs:double",
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
   *  A test whose essence is: `(xs:float(6) div xs:double(2)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNumericDivide28() {
    final XQuery query = new XQuery(
      "(xs:float(6) div xs:double(2)) instance of xs:double",
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
   *  A test whose essence is: `(xs:double(6) div xs:integer(2)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNumericDivide29() {
    final XQuery query = new XQuery(
      "(xs:double(6) div xs:integer(2)) instance of xs:double",
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
   *  A test whose essence is: `xs:integer(6) div xs:integer(2) eq 3`. .
   */
  @org.junit.Test
  public void kNumericDivide3() {
    final XQuery query = new XQuery(
      "xs:integer(6) div xs:integer(2) eq 3",
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
   *  A test whose essence is: `(xs:integer(6) div xs:double(2)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNumericDivide30() {
    final XQuery query = new XQuery(
      "(xs:integer(6) div xs:double(2)) instance of xs:double",
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
   *  A test whose essence is: `(xs:double(6) div xs:double(2)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNumericDivide31() {
    final XQuery query = new XQuery(
      "(xs:double(6) div xs:double(2)) instance of xs:double",
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
   *  Invoke the 'div operator with invalid operands. .
   */
  @org.junit.Test
  public void kNumericDivide32() {
    final XQuery query = new XQuery(
      "\"3\" div \"3\"",
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
   *  Invoke the 'div' operator with invalid operands. .
   */
  @org.junit.Test
  public void kNumericDivide33() {
    final XQuery query = new XQuery(
      "xs:double(3) div \"3\"",
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
   *  A test whose essence is: `(xs:double(3) div xs:untypedAtomic("3")) eq 1`. .
   */
  @org.junit.Test
  public void kNumericDivide34() {
    final XQuery query = new XQuery(
      "(xs:double(3) div xs:untypedAtomic(\"3\")) eq 1",
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
   *  A test whose essence is: `(xs:untypedAtomic("3") div xs:double(3)) eq 1`. .
   */
  @org.junit.Test
  public void kNumericDivide35() {
    final XQuery query = new XQuery(
      "(xs:untypedAtomic(\"3\") div xs:double(3)) eq 1",
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
   *  Invalid whitespace for the 'div' operator. .
   */
  @org.junit.Test
  public void kNumericDivide36() {
    final XQuery query = new XQuery(
      "10 div3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  Invalid whitespace for the 'div' operator. .
   */
  @org.junit.Test
  public void kNumericDivide37() {
    final XQuery query = new XQuery(
      "10div 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  Invalid whitespace for the 'div' operator. .
   */
  @org.junit.Test
  public void kNumericDivide38() {
    final XQuery query = new XQuery(
      "10div3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  Operand(s) which are the empty sequence. .
   */
  @org.junit.Test
  public void kNumericDivide39() {
    final XQuery query = new XQuery(
      "empty(() div ())",
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
   *  A test whose essence is: `xs:float(6) div xs:float(2) eq 3`. .
   */
  @org.junit.Test
  public void kNumericDivide4() {
    final XQuery query = new XQuery(
      "xs:float(6) div xs:float(2) eq 3",
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
   *  Operand(s) which are the empty sequence. .
   */
  @org.junit.Test
  public void kNumericDivide40() {
    final XQuery query = new XQuery(
      "empty(1 div ())",
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
   *  Operand(s) which are the empty sequence. .
   */
  @org.junit.Test
  public void kNumericDivide41() {
    final XQuery query = new XQuery(
      "empty(() div 1)",
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
   *  Operand(s) which are the empty sequence. .
   */
  @org.junit.Test
  public void kNumericDivide42() {
    final XQuery query = new XQuery(
      "empty(xs:double(3) div ())",
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
   *  Operand(s) which are the empty sequence. .
   */
  @org.junit.Test
  public void kNumericDivide43() {
    final XQuery query = new XQuery(
      "empty(() div xs:decimal(1))",
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
   *  A test whose essence is: `xs:double(5) div xs:double(2) eq 2.5`. .
   */
  @org.junit.Test
  public void kNumericDivide5() {
    final XQuery query = new XQuery(
      "xs:double(5) div xs:double(2) eq 2.5",
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
   *  A test whose essence is: `xs:double(5) div xs:double(2) ne 2.6`. .
   */
  @org.junit.Test
  public void kNumericDivide6() {
    final XQuery query = new XQuery(
      "xs:double(5) div xs:double(2) ne 2.6",
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
   *  A test whose essence is: `xs:float(5) div xs:float(2) eq 2.5`. .
   */
  @org.junit.Test
  public void kNumericDivide7() {
    final XQuery query = new XQuery(
      "xs:float(5) div xs:float(2) eq 2.5",
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
   *  A test whose essence is: `xs:float(5) div xs:float(2) ne 2.6`. .
   */
  @org.junit.Test
  public void kNumericDivide8() {
    final XQuery query = new XQuery(
      "xs:float(5) div xs:float(2) ne 2.6",
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
   *  A test whose essence is: `xs:integer(5) div xs:integer(2) eq 2.5`. .
   */
  @org.junit.Test
  public void kNumericDivide9() {
    final XQuery query = new XQuery(
      "xs:integer(5) div xs:integer(2) eq 2.5",
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
   *  Divide by 0. .
   */
  @org.junit.Test
  public void k2NumericDivide1() {
    final XQuery query = new XQuery(
      "1 div 0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOAR0001")
    );
  }

  /**
   *  Divide +0e0 with +0e0. .
   */
  @org.junit.Test
  public void k2NumericDivide10() {
    final XQuery query = new XQuery(
      "+0e0 div +0e0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   *  Divide -0e0 with -0e0. .
   */
  @org.junit.Test
  public void k2NumericDivide11() {
    final XQuery query = new XQuery(
      "-0e0 div -0e0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   *  Divide -0e0 with +0e0. .
   */
  @org.junit.Test
  public void k2NumericDivide12() {
    final XQuery query = new XQuery(
      "-0e0 div +0e0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   *  Divide +3 with +0e0. .
   */
  @org.junit.Test
  public void k2NumericDivide13() {
    final XQuery query = new XQuery(
      "+3 div +0e0",
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
   *  Divide -3 with +0e0. .
   */
  @org.junit.Test
  public void k2NumericDivide14() {
    final XQuery query = new XQuery(
      "-3 div +0e0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-INF")
    );
  }

  /**
   *  Divide +3 with -0e0. .
   */
  @org.junit.Test
  public void k2NumericDivide15() {
    final XQuery query = new XQuery(
      "+3 div -0e0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-INF")
    );
  }

  /**
   *  Divide -3 with -0e0. .
   */
  @org.junit.Test
  public void k2NumericDivide16() {
    final XQuery query = new XQuery(
      "-3 div -0e0",
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
   *  Ensure the 'div' keyword is parsed correctly. .
   */
  @org.junit.Test
  public void k2NumericDivide17() {
    final XQuery query = new XQuery(
      "empty(<e/>/(div div div))",
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
   *  Divide by 0.0. .
   */
  @org.junit.Test
  public void k2NumericDivide2() {
    final XQuery query = new XQuery(
      "1 div 0.0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOAR0001")
    );
  }

  /**
   *  Divide by +0.0. .
   */
  @org.junit.Test
  public void k2NumericDivide3() {
    final XQuery query = new XQuery(
      "1 div +0.0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOAR0001")
    );
  }

  /**
   *  Divide by -0.0. .
   */
  @org.junit.Test
  public void k2NumericDivide4() {
    final XQuery query = new XQuery(
      "1 div -0.0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOAR0001")
    );
  }

  /**
   *  Divide by 0e0. .
   */
  @org.junit.Test
  public void k2NumericDivide5() {
    final XQuery query = new XQuery(
      "1 div 0e0",
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
   *  Divide -1 by 0e0. .
   */
  @org.junit.Test
  public void k2NumericDivide6() {
    final XQuery query = new XQuery(
      "-1 div 0e0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-INF")
    );
  }

  /**
   *  Divide by xs:float(0). .
   */
  @org.junit.Test
  public void k2NumericDivide7() {
    final XQuery query = new XQuery(
      "1 div xs:float(0)",
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
   *  Divide -1 by xs:float(0). .
   */
  @org.junit.Test
  public void k2NumericDivide8() {
    final XQuery query = new XQuery(
      "-1 div xs:float(0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-INF")
    );
  }

  /**
   *  Divide +0e0 with -0e0. .
   */
  @org.junit.Test
  public void k2NumericDivide9() {
    final XQuery query = new XQuery(
      "+0e0 div -0e0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   *  test division of xs:double NaN .
   */
  @org.junit.Test
  public void cbclNumericDivide001() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:double) as xs:double { $arg * $arg }; \n" +
      "      \txs:double('NaN') div local:square(7)\n" +
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
      assertStringValue(false, "NaN")
    );
  }

  /**
   *  test division by xs:double NaN .
   */
  @org.junit.Test
  public void cbclNumericDivide002() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:double) as xs:double { $arg * $arg }; \n" +
      "      \tlocal:square(7) div xs:double('NaN')\n" +
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
      assertStringValue(false, "NaN")
    );
  }

  /**
   *  test division by xs:double 1 .
   */
  @org.junit.Test
  public void cbclNumericDivide003() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:double) as xs:double { $arg * $arg }; \n" +
      "      \tlocal:square(7) div 1e0\n" +
      "      \t",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "49")
    );
  }

  /**
   *  test division by xs:double -1 .
   */
  @org.junit.Test
  public void cbclNumericDivide004() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:double) as xs:double { $arg * $arg }; \n" +
      "      \tlocal:square(7) div -1e0\n" +
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
      assertStringValue(false, "-49")
    );
  }

  /**
   *  test division of xs:float NaN .
   */
  @org.junit.Test
  public void cbclNumericDivide005() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:float) as xs:float { $arg * $arg }; \n" +
      "      \txs:float('NaN') div local:square(7)\n" +
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
      assertStringValue(false, "NaN")
    );
  }

  /**
   *  test division by xs:float NaN .
   */
  @org.junit.Test
  public void cbclNumericDivide006() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:float) as xs:float { $arg * $arg }; \n" +
      "      \tlocal:square(7) div xs:float('NaN')\n" +
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
      assertStringValue(false, "NaN")
    );
  }

  /**
   *  test division by xs:float 1 .
   */
  @org.junit.Test
  public void cbclNumericDivide007() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:float) as xs:float { $arg * $arg }; \n" +
      "      \tlocal:square(7) div xs:float(1)\n" +
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
      assertStringValue(false, "49")
    );
  }

  /**
   *  test division by xs:float -1 .
   */
  @org.junit.Test
  public void cbclNumericDivide008() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:float) as xs:float { $arg * $arg }; \n" +
      "      \tlocal:square(7) div xs:float(-1)\n" +
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
      assertStringValue(false, "-49")
    );
  }

  /**
   *  test division by xs:decimal 0 .
   */
  @org.junit.Test
  public void cbclNumericDivide009() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:decimal) as xs:decimal { $arg * $arg }; \n" +
      "      \tlocal:square(2.0) div 0.0\n" +
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
      error("FOAR0001")
    );
  }

  /**
   *  test division by xs:decimal 1 .
   */
  @org.junit.Test
  public void cbclNumericDivide010() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:decimal) as xs:decimal { $arg * $arg }; \n" +
      "      \tlocal:square(7.0) div 1.0\n" +
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
      assertStringValue(false, "49")
    );
  }

  /**
   *  test division by xs:decimal -1 .
   */
  @org.junit.Test
  public void cbclNumericDivide011() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:decimal) as xs:decimal { $arg * $arg }; \n" +
      "      \tlocal:square(7.0) div -1.0\n" +
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
      assertStringValue(false, "-49")
    );
  }

  /**
   *  test division by xs:integer 0 .
   */
  @org.junit.Test
  public void cbclNumericDivide012() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:integer) as xs:integer { $arg * $arg }; \n" +
      "      \tlocal:square(2) div 0\n" +
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
      error("FOAR0001")
    );
  }

  /**
   *  test division by xs:integer 1 .
   */
  @org.junit.Test
  public void cbclNumericDivide013() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:integer) as xs:integer { $arg * $arg }; \n" +
      "      \tlocal:square(7) div 1\n" +
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
      assertStringValue(false, "49")
    );
  }

  /**
   *  test division by xs:integer -1 .
   */
  @org.junit.Test
  public void cbclNumericDivide014() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:integer) as xs:integer { $arg * $arg }; \n" +
      "      \tlocal:square(7) div -1\n" +
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
      assertStringValue(false, "-49")
    );
  }

  /**
   *  check that overflow occurs correctly when adding values of type xs:decimal .
   */
  @org.junit.Test
  public void cbclNumericDivide015() {
    final XQuery query = new XQuery(
      "10000000000000000000000000000.0 div 0.1 gt 0",
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
        error("FOAR0002")
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  Check dynamic type of numeric divide on arguments of union of numeric types and untypedAtomic. .
   */
  @org.junit.Test
  public void opNumericDivide1() {
    final XQuery query = new XQuery(
      "for $x in (1, xs:decimal(2), xs:float(3), xs:double(4), xs:untypedAtomic(5)), \n" +
      "                $y in (6, xs:decimal(6), xs:float(6), xs:double(6), xs:untypedAtomic(6)),\n" +
      "                $result in ($x div $y)\n" +
      "            return \n" +
      "                if ($result instance of xs:integer) then \"integer\"\n" +
      "                else if ($result instance of xs:decimal) then \"decimal\" \n" +
      "                else if ($result instance of xs:double) then \"double\"\n" +
      "                else if ($result instance of xs:float) then \"float\"\n" +
      "                else error() \n" +
      "       ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "decimal decimal float double double decimal decimal float double double float float float double double double double double double double double double double double double")
    );
  }

  /**
   *  check that overflow occurs correctly when dividing values of type xs:decimal .
   */
  @org.junit.Test
  public void opNumericDivideBig01() {
    final XQuery query = new XQuery(
      "(10000000000000000000000000000.0 div 0.1) cast as xs:string",
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
        error("FOAR0002")
      ||
        assertStringValue(false, "100000000000000000000000000000")
      )
    );
  }

  /**
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:double(lower bound) $arg2 = xs:double(lower bound) .
   */
  @org.junit.Test
  public void opNumericDividedbl2args1() {
    final XQuery query = new XQuery(
      "xs:double(\"-1.7976931348623157E308\") div xs:double(\"-1.7976931348623157E308\")",
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
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:double(mid range) $arg2 = xs:double(lower bound) .
   */
  @org.junit.Test
  public void opNumericDividedbl2args2() {
    final XQuery query = new XQuery(
      "xs:double(\"0\") div xs:double(\"-1.7976931348623157E308\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-0")
    );
  }

  /**
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:double(upper bound) $arg2 = xs:double(lower bound) .
   */
  @org.junit.Test
  public void opNumericDividedbl2args3() {
    final XQuery query = new XQuery(
      "xs:double(\"1.7976931348623157E308\") div xs:double(\"-1.7976931348623157E308\")",
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
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:double(lower bound) $arg2 = xs:double(upper bound) .
   */
  @org.junit.Test
  public void opNumericDividedbl2args4() {
    final XQuery query = new XQuery(
      "xs:double(\"-1.7976931348623157E308\") div xs:double(\"1.7976931348623157E308\")",
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
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:decimal(lower bound) $arg2 = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void opNumericDividedec2args1() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-999999999999999999\") div xs:decimal(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:decimal(mid range) $arg2 = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void opNumericDividedec2args2() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even((xs:decimal(\"617375191608514839\") div xs:decimal(\"-999999999999999999\")),18)",
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
        assertEq("-0.61737519160851484")
      ||
        assertEq("-0.6173751916085")
      )
    );
  }

  /**
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:decimal(upper bound) $arg2 = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void opNumericDividedec2args3() {
    final XQuery query = new XQuery(
      "xs:decimal(\"999999999999999999\") div xs:decimal(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:decimal(lower bound) $arg2 = xs:decimal(mid range) .
   */
  @org.junit.Test
  public void opNumericDividedec2args4() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even((xs:decimal(\"-999999999999999999\") div xs:decimal(\"617375191608514839\")),18)",
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
        assertEq("-1.619760582531006901")
      ||
        assertEq("-1.619760582531")
      )
    );
  }

  /**
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:decimal(lower bound) $arg2 = xs:decimal(upper bound) .
   */
  @org.junit.Test
  public void opNumericDividedec2args5() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-999999999999999999\") div xs:decimal(\"999999999999999999\")",
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
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:float(lower bound) $arg2 = xs:float(lower bound) .
   */
  @org.junit.Test
  public void opNumericDivideflt2args1() {
    final XQuery query = new XQuery(
      "xs:float(\"-3.4028235E38\") div xs:float(\"-3.4028235E38\")",
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
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:float(mid range) $arg2 = xs:float(lower bound) .
   */
  @org.junit.Test
  public void opNumericDivideflt2args2() {
    final XQuery query = new XQuery(
      "xs:float(\"0\") div xs:float(\"-3.4028235E38\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-0")
    );
  }

  /**
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:float(upper bound) $arg2 = xs:float(lower bound) .
   */
  @org.junit.Test
  public void opNumericDivideflt2args3() {
    final XQuery query = new XQuery(
      "xs:float(\"3.4028235E38\") div xs:float(\"-3.4028235E38\")",
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
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:float(lower bound) $arg2 = xs:float(upper bound) .
   */
  @org.junit.Test
  public void opNumericDivideflt2args4() {
    final XQuery query = new XQuery(
      "xs:float(\"-3.4028235E38\") div xs:float(\"3.4028235E38\")",
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
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:int(lower bound) $arg2 = xs:int(lower bound) .
   */
  @org.junit.Test
  public void opNumericDivideint2args1() {
    final XQuery query = new XQuery(
      "xs:int(\"-2147483648\") div xs:int(\"-2147483648\")",
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
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:int(mid range) $arg2 = xs:int(lower bound) .
   */
  @org.junit.Test
  public void opNumericDivideint2args2() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even((xs:int(\"-1873914410\") div xs:int(\"-2147483648\")),10)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0.8726093965")
    );
  }

  /**
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:int(upper bound) $arg2 = xs:int(lower bound) .
   */
  @org.junit.Test
  public void opNumericDivideint2args3() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even((xs:int(\"2147483647\") div xs:int(\"-2147483648\")),10)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-0.9999999995")
    );
  }

  /**
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:int(lower bound) $arg2 = xs:int(mid range) .
   */
  @org.junit.Test
  public void opNumericDivideint2args4() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even((xs:int(\"-2147483648\") div xs:int(\"-1873914410\")),10)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1.145988118")
    );
  }

  /**
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:int(lower bound) $arg2 = xs:int(upper bound) .
   */
  @org.junit.Test
  public void opNumericDivideint2args5() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even((xs:int(\"-2147483648\") div xs:int(\"2147483647\")),10)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1.0000000005")
    );
  }

  /**
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:integer(lower bound) $arg2 = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void opNumericDivideintg2args1() {
    final XQuery query = new XQuery(
      "xs:integer(\"-999999999999999999\") div xs:integer(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:integer(mid range) $arg2 = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void opNumericDivideintg2args2() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even((xs:integer(\"830993497117024304\") div xs:integer(\"-999999999999999999\")),18)",
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
        assertEq("-0.830993497117024305")
      ||
        assertEq("-0.830993497117")
      )
    );
  }

  /**
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:integer(upper bound) $arg2 = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void opNumericDivideintg2args3() {
    final XQuery query = new XQuery(
      "xs:integer(\"999999999999999999\") div xs:integer(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:integer(lower bound) $arg2 = xs:integer(mid range) .
   */
  @org.junit.Test
  public void opNumericDivideintg2args4() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even((xs:integer(\"-999999999999999999\") div xs:integer(\"830993497117024304\")),18)",
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
        assertEq("-1.203378851301859738")
      ||
        assertEq("-1.203378851301")
      )
    );
  }

  /**
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:integer(lower bound) $arg2 = xs:integer(upper bound) .
   */
  @org.junit.Test
  public void opNumericDivideintg2args5() {
    final XQuery query = new XQuery(
      "xs:integer(\"-999999999999999999\") div xs:integer(\"999999999999999999\")",
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
   *  Evaluates the type of the result of "op:numeric-integer-divide" operator with both arguments of type xs:integer .
   */
  @org.junit.Test
  public void opNumericDivideintg2args6() {
    final XQuery query = new XQuery(
      "(xs:integer(\"-999999999999999999\") div xs:integer(\"999999999999999999\")) instance of xs:decimal",
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
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:long(lower bound) $arg2 = xs:long(lower bound) .
   */
  @org.junit.Test
  public void opNumericDividelng2args1() {
    final XQuery query = new XQuery(
      "xs:long(\"-92233720368547758\") div xs:long(\"-92233720368547758\")",
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
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:long(mid range) $arg2 = xs:long(lower bound) .
   */
  @org.junit.Test
  public void opNumericDividelng2args2() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even((xs:long(\"-47175562203048468\") div xs:long(\"-92233720368547758\")),17)",
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
        assertEq("0.51147847028770199")
      ||
        assertEq("0.511478470287")
      )
    );
  }

  /**
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:long(upper bound) $arg2 = xs:long(lower bound) .
   */
  @org.junit.Test
  public void opNumericDividelng2args3() {
    final XQuery query = new XQuery(
      "xs:long(\"92233720368547758\") div xs:long(\"-92233720368547758\")",
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
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:long(lower bound) $arg2 = xs:long(mid range) .
   */
  @org.junit.Test
  public void opNumericDividelng2args4() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even((xs:long(\"-92233720368547758\") div xs:long(\"-47175562203048468\")),17)",
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
        assertEq("1.95511650654133906")
      ||
        assertEq("1.955116506541")
      )
    );
  }

  /**
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:long(lower bound) $arg2 = xs:long(upper bound) .
   */
  @org.junit.Test
  public void opNumericDividelng2args5() {
    final XQuery query = new XQuery(
      "xs:long(\"-92233720368547758\") div xs:long(\"92233720368547758\")",
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
   *  Simple division test with () as one operand should return null .
   */
  @org.junit.Test
  public void opNumericDividemix2args1() {
    final XQuery query = new XQuery(
      "() div 1",
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
        assertEmpty()
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Simple division test pass string for second operator .
   */
  @org.junit.Test
  public void opNumericDividemix2args2() {
    final XQuery query = new XQuery(
      "1 div '1'",
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
   *  Simple division test, second operator cast string to integer .
   */
  @org.junit.Test
  public void opNumericDividemix2args3() {
    final XQuery query = new XQuery(
      "1 div xs:integer('1')",
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
   *  Simple division test, second operator is a node, atomization applied .
   */
  @org.junit.Test
  public void opNumericDividemix2args4() {
    final XQuery query = new XQuery(
      "1 div <a> 2 </a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0.5")
    );
  }

  /**
   *  Simple division test, second operator is a node, atomization applied .
   */
  @org.junit.Test
  public void opNumericDividemix2args5() {
    final XQuery query = new XQuery(
      "1 div <a> <b> 2 </b> </a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0.5")
    );
  }

  /**
   *  Simple division test, second operator node which is not atomizable .
   */
  @org.junit.Test
  public void opNumericDividemix2args6() {
    final XQuery query = new XQuery(
      "1 div <a> <b> 2</b> <c> 2</c> </a>",
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
   *  Simple division test, two operands are nodes .
   */
  @org.junit.Test
  public void opNumericDividemix2args7() {
    final XQuery query = new XQuery(
      "<a> 1 </a> div <b> 2 </b>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0.5")
    );
  }

  /**
   *  Simple division test, second operator is a node, atomizable but not castable to integer .
   */
  @org.junit.Test
  public void opNumericDividemix2args8() {
    final XQuery query = new XQuery(
      "1 div <a> x </a>",
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
   *  Simple division test pass an empty node for second operator .
   */
  @org.junit.Test
  public void opNumericDividemix2args9() {
    final XQuery query = new XQuery(
      "1 div <a/>",
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
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:negativeInteger(lower bound) $arg2 = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericDividenint2args1() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-999999999999999999\") div xs:negativeInteger(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:negativeInteger(mid range) $arg2 = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericDividenint2args2() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even((xs:negativeInteger(\"-297014075999096793\") div xs:negativeInteger(\"-999999999999999999\")),18)",
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
        assertEq("0.297014075999096793")
      ||
        assertEq("0.297014075999")
      )
    );
  }

  /**
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:negativeInteger(upper bound) $arg2 = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericDividenint2args3() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-1\") div xs:negativeInteger(\"-999999999999999999\")",
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
        assertEq("0.000000000000000001")
      ||
        assertEq("0.000000000000000001000000000000000001")
      ||
        assertEq("0")
      )
    );
  }

  /**
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:negativeInteger(lower bound) $arg2 = xs:negativeInteger(mid range) .
   */
  @org.junit.Test
  public void opNumericDividenint2args4() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even((xs:negativeInteger(\"-999999999999999999\") div xs:negativeInteger(\"-297014075999096793\")),18)",
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
        assertEq("3.366843799022646172")
      ||
        assertEq("3.366843799022")
      )
    );
  }

  /**
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:negativeInteger(lower bound) $arg2 = xs:negativeInteger(upper bound) .
   */
  @org.junit.Test
  public void opNumericDividenint2args5() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-999999999999999999\") div xs:negativeInteger(\"-1\")",
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
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:nonNegativeInteger(lower bound) $arg2 = xs:nonNegativeInteger(mid range) .
   */
  @org.junit.Test
  public void opNumericDividenni2args1() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"0\") div xs:nonNegativeInteger(\"303884545991464527\")",
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
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:nonNegativeInteger(lower bound) $arg2 = xs:nonNegativeInteger(upper bound) .
   */
  @org.junit.Test
  public void opNumericDividenni2args2() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"0\") div xs:nonNegativeInteger(\"999999999999999999\")",
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
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:nonPositiveInteger(lower bound) $arg2 = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericDividenpi2args1() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"-999999999999999999\") div xs:nonPositiveInteger(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:nonPositiveInteger(mid range) $arg2 = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericDividenpi2args2() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even((xs:nonPositiveInteger(\"-475688437271870490\") div xs:nonPositiveInteger(\"-999999999999999999\")),18)",
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
        assertEq("0.47568843727187049")
      ||
        assertEq("0.475688437271")
      )
    );
  }

  /**
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:nonPositiveInteger(upper bound) $arg2 = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericDividenpi2args3() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"0\") div xs:nonPositiveInteger(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:nonPositiveInteger(lower bound) $arg2 = xs:nonPositiveInteger(mid range) .
   */
  @org.junit.Test
  public void opNumericDividenpi2args4() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even((xs:nonPositiveInteger(\"-999999999999999999\") div xs:nonPositiveInteger(\"-475688437271870490\")),18)",
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
        assertEq("2.102216328265447024")
      ||
        assertEq("2.102216328265")
      )
    );
  }

  /**
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:positiveInteger(lower bound) $arg2 = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericDividepint2args1() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"1\") div xs:positiveInteger(\"1\")",
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
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:positiveInteger(mid range) $arg2 = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericDividepint2args2() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"52704602390610033\") div xs:positiveInteger(\"1\")",
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
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:positiveInteger(upper bound) $arg2 = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericDividepint2args3() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"999999999999999999\") div xs:positiveInteger(\"1\")",
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
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:positiveInteger(lower bound) $arg2 = xs:positiveInteger(mid range) .
   */
  @org.junit.Test
  public void opNumericDividepint2args4() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even((xs:positiveInteger(\"1\") div xs:positiveInteger(\"52704602390610033\")),17)",
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
        assertEq("0.00000000000000002")
      ||
        assertEq("0")
      )
    );
  }

  /**
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:positiveInteger(lower bound) $arg2 = xs:positiveInteger(upper bound) .
   */
  @org.junit.Test
  public void opNumericDividepint2args5() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"1\") div xs:positiveInteger(\"999999999999999999\")",
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
        assertEq("0.000000000000000001")
      ||
        assertEq("0.000000000000000001000000000000000001")
      ||
        assertEq("0")
      )
    );
  }

  /**
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:short(lower bound) $arg2 = xs:short(lower bound) .
   */
  @org.junit.Test
  public void opNumericDividesht2args1() {
    final XQuery query = new XQuery(
      "xs:short(\"-32768\") div xs:short(\"-32768\")",
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
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:short(mid range) $arg2 = xs:short(lower bound) .
   */
  @org.junit.Test
  public void opNumericDividesht2args2() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even((xs:short(\"-5324\") div xs:short(\"-32768\")),5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0.16248")
    );
  }

  /**
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:short(upper bound) $arg2 = xs:short(lower bound) .
   */
  @org.junit.Test
  public void opNumericDividesht2args3() {
    final XQuery query = new XQuery(
      "xs:short(\"32767\") div xs:short(\"-32768\")",
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
        assertEq("-0.999969482421875")
      ||
        assertEq("-0.999969482421")
      )
    );
  }

  /**
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:short(lower bound) $arg2 = xs:short(mid range) .
   */
  @org.junit.Test
  public void opNumericDividesht2args4() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even((xs:short(\"-32768\") div xs:short(\"-5324\")),5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("6.15477")
    );
  }

  /**
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:short(lower bound) $arg2 = xs:short(upper bound) .
   */
  @org.junit.Test
  public void opNumericDividesht2args5() {
    final XQuery query = new XQuery(
      "xs:short(\"-32768\") div xs:short(\"32767\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertQuery("abs($result + 1.000030518509475997) lt 1e-12")
    );
  }

  /**
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:unsignedLong(lower bound) $arg2 = xs:unsignedLong(mid range) .
   */
  @org.junit.Test
  public void opNumericDivideulng2args1() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"0\") div xs:unsignedLong(\"130747108607674654\")",
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
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:unsignedLong(lower bound) $arg2 = xs:unsignedLong(upper bound) .
   */
  @org.junit.Test
  public void opNumericDivideulng2args2() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"0\") div xs:unsignedLong(\"184467440737095516\")",
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
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:unsignedShort(lower bound) $arg2 = xs:unsignedShort(mid range) .
   */
  @org.junit.Test
  public void opNumericDivideusht2args1() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"0\") div xs:unsignedShort(\"44633\")",
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
   *  Evaluates The "op:numeric-divide" operator with the arguments set as follows: $arg1 = xs:unsignedShort(lower bound) $arg2 = xs:unsignedShort(upper bound) .
   */
  @org.junit.Test
  public void opNumericDivideusht2args2() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"0\") div xs:unsignedShort(\"65535\")",
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
}
