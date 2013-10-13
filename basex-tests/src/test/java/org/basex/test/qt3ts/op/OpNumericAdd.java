package org.basex.test.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the numeric-add() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpNumericAdd extends QT3TestSet {

  /**
   *  A test whose essence is: `xs:double(6) + xs:double(2) eq 8`. .
   */
  @org.junit.Test
  public void kNumericAdd1() {
    final XQuery query = new XQuery(
      "xs:double(6) + xs:double(2) eq 8",
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
   *  A test whose essence is: `string(xs:double("NaN") + 3) eq "NaN"`. .
   */
  @org.junit.Test
  public void kNumericAdd10() {
    final XQuery query = new XQuery(
      "string(xs:double(\"NaN\") + 3) eq \"NaN\"",
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
   *  A test whose essence is: `string(3 + xs:float("NaN")) eq "NaN"`. .
   */
  @org.junit.Test
  public void kNumericAdd11() {
    final XQuery query = new XQuery(
      "string(3 + xs:float(\"NaN\")) eq \"NaN\"",
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
   *  A test whose essence is: `string(3 + xs:double("NaN")) eq "NaN"`. .
   */
  @org.junit.Test
  public void kNumericAdd12() {
    final XQuery query = new XQuery(
      "string(3 + xs:double(\"NaN\")) eq \"NaN\"",
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
   *  A test whose essence is: `(xs:decimal(6) + xs:integer(2)) instance of xs:decimal`. .
   */
  @org.junit.Test
  public void kNumericAdd13() {
    final XQuery query = new XQuery(
      "(xs:decimal(6) + xs:integer(2)) instance of xs:decimal",
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
   *  A test whose essence is: `(xs:integer(6) + xs:decimal(2)) instance of xs:decimal`. .
   */
  @org.junit.Test
  public void kNumericAdd14() {
    final XQuery query = new XQuery(
      "(xs:integer(6) + xs:decimal(2)) instance of xs:decimal",
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
   *  A test whose essence is: `(xs:integer(6) + xs:integer(2)) instance of xs:integer`. .
   */
  @org.junit.Test
  public void kNumericAdd15() {
    final XQuery query = new XQuery(
      "(xs:integer(6) + xs:integer(2)) instance of xs:integer",
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
   *  A test whose essence is: `(xs:decimal(6) + xs:decimal(2)) instance of xs:decimal`. .
   */
  @org.junit.Test
  public void kNumericAdd16() {
    final XQuery query = new XQuery(
      "(xs:decimal(6) + xs:decimal(2)) instance of xs:decimal",
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
   *  A test whose essence is: `(xs:decimal(6) + xs:float(2)) instance of xs:float`. .
   */
  @org.junit.Test
  public void kNumericAdd17() {
    final XQuery query = new XQuery(
      "(xs:decimal(6) + xs:float(2)) instance of xs:float",
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
   *  A test whose essence is: `(xs:float(6) + xs:decimal(2)) instance of xs:float`. .
   */
  @org.junit.Test
  public void kNumericAdd18() {
    final XQuery query = new XQuery(
      "(xs:float(6) + xs:decimal(2)) instance of xs:float",
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
   *  A test whose essence is: `(xs:float(6) + xs:integer(2)) instance of xs:float`. .
   */
  @org.junit.Test
  public void kNumericAdd19() {
    final XQuery query = new XQuery(
      "(xs:float(6) + xs:integer(2)) instance of xs:float",
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
   *  A test whose essence is: `xs:decimal(6) + xs:decimal(2) eq 8`. .
   */
  @org.junit.Test
  public void kNumericAdd2() {
    final XQuery query = new XQuery(
      "xs:decimal(6) + xs:decimal(2) eq 8",
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
   *  A test whose essence is: `(xs:integer(6) + xs:float(2)) instance of xs:float`. .
   */
  @org.junit.Test
  public void kNumericAdd20() {
    final XQuery query = new XQuery(
      "(xs:integer(6) + xs:float(2)) instance of xs:float",
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
   *  A test whose essence is: `(xs:float(6) + xs:float(2)) instance of xs:float`. .
   */
  @org.junit.Test
  public void kNumericAdd21() {
    final XQuery query = new XQuery(
      "(xs:float(6) + xs:float(2)) instance of xs:float",
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
   *  A test whose essence is: `(xs:decimal(6) + xs:double(2)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNumericAdd22() {
    final XQuery query = new XQuery(
      "(xs:decimal(6) + xs:double(2)) instance of xs:double",
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
   *  A test whose essence is: `(xs:double(6) + xs:decimal(2)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNumericAdd23() {
    final XQuery query = new XQuery(
      "(xs:double(6) + xs:decimal(2)) instance of xs:double",
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
   *  A test whose essence is: `(xs:double(6) + xs:float(2)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNumericAdd24() {
    final XQuery query = new XQuery(
      "(xs:double(6) + xs:float(2)) instance of xs:double",
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
   *  A test whose essence is: `(xs:float(6) + xs:double(2)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNumericAdd25() {
    final XQuery query = new XQuery(
      "(xs:float(6) + xs:double(2)) instance of xs:double",
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
   *  A test whose essence is: `(xs:double(6) + xs:integer(2)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNumericAdd26() {
    final XQuery query = new XQuery(
      "(xs:double(6) + xs:integer(2)) instance of xs:double",
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
   *  A test whose essence is: `(xs:integer(6) + xs:double(2)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNumericAdd27() {
    final XQuery query = new XQuery(
      "(xs:integer(6) + xs:double(2)) instance of xs:double",
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
   *  A test whose essence is: `(xs:double(6) + xs:double(2)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNumericAdd28() {
    final XQuery query = new XQuery(
      "(xs:double(6) + xs:double(2)) instance of xs:double",
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
   *  Test parser handles '+' operator with critical whitespace. .
   */
  @org.junit.Test
  public void kNumericAdd29() {
    final XQuery query = new XQuery(
      "1+1 eq 2",
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
   *  A test whose essence is: `xs:decimal(6.1) + xs:decimal(2.1) eq 8.2`. .
   */
  @org.junit.Test
  public void kNumericAdd3() {
    final XQuery query = new XQuery(
      "xs:decimal(6.1) + xs:decimal(2.1) eq 8.2",
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
   *  Test parser handles '+' operator with critical whitespace(#2). .
   */
  @org.junit.Test
  public void kNumericAdd30() {
    final XQuery query = new XQuery(
      "2 eq 1+1",
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
   *  Invoke operator '+' where one of the operands is of type none. .
   */
  @org.junit.Test
  public void kNumericAdd31() {
    final XQuery query = new XQuery(
      "3 + error()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOER0000")
    );
  }

  /**
   *  Invoke operator '+' where one of the operands is of type none. .
   */
  @org.junit.Test
  public void kNumericAdd32() {
    final XQuery query = new XQuery(
      "error() + 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOER0000")
    );
  }

  /**
   *  Invoke operator '+' where one of the operands is of type none. .
   */
  @org.junit.Test
  public void kNumericAdd33() {
    final XQuery query = new XQuery(
      "3 + (error(), 4)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOER0000")
    );
  }

  /**
   *  Invoke operator '+' where one of the operands is of type none. .
   */
  @org.junit.Test
  public void kNumericAdd34() {
    final XQuery query = new XQuery(
      "(4, error()) + 3",
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
        error("FOER0000")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Invoke operator '+' where one of the operands is of type none. .
   */
  @org.junit.Test
  public void kNumericAdd35() {
    final XQuery query = new XQuery(
      "3 + (4, error())",
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
        error("FOER0000")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Invoke operator '+' where one of the operands is of type none. .
   */
  @org.junit.Test
  public void kNumericAdd36() {
    final XQuery query = new XQuery(
      "(error(), 4) + 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOER0000")
    );
  }

  /**
   *  Arithmethics involving operands of integer type but with wrong cardinality. .
   */
  @org.junit.Test
  public void kNumericAdd37() {
    final XQuery query = new XQuery(
      "(1, 2) + 1",
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
   *  Arithmethics involving operands of integer type but with wrong cardinality. .
   */
  @org.junit.Test
  public void kNumericAdd38() {
    final XQuery query = new XQuery(
      "empty((1, 2) + ())",
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
        error("XPTY0004")
      )
    );
  }

  /**
   *  Arithmethics involving operands of integer type but with wrong cardinality. .
   */
  @org.junit.Test
  public void kNumericAdd39() {
    final XQuery query = new XQuery(
      "1 + (1, 2)",
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
        error("XPTY0004")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  A test whose essence is: `1.1 + 2.2 eq 3.3`. .
   */
  @org.junit.Test
  public void kNumericAdd4() {
    final XQuery query = new XQuery(
      "1.1 + 2.2 eq 3.3",
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
   *  Arithmethics involving operands of integer type but with wrong cardinality. .
   */
  @org.junit.Test
  public void kNumericAdd40() {
    final XQuery query = new XQuery(
      "empty(() + (1, 2))",
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
        error("XPTY0004")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Number addition with error() as the left operand. .
   */
  @org.junit.Test
  public void kNumericAdd41() {
    final XQuery query = new XQuery(
      "error() + 3",
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
        error("FOER0000")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Number addition with error() as the right operand. .
   */
  @org.junit.Test
  public void kNumericAdd42() {
    final XQuery query = new XQuery(
      "3 + error()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOER0000")
    );
  }

  /**
   *  Invoke the '+' operator with invalid operands. .
   */
  @org.junit.Test
  public void kNumericAdd43() {
    final XQuery query = new XQuery(
      "\"3\" + \"3\"",
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
   *  Invoke the '+' operator with invalid operands. .
   */
  @org.junit.Test
  public void kNumericAdd44() {
    final XQuery query = new XQuery(
      "\"3\" + xs:untypedAtomic(\"3\")",
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
   *  A test whose essence is: `(3 + xs:untypedAtomic("3")) eq 6`. .
   */
  @org.junit.Test
  public void kNumericAdd45() {
    final XQuery query = new XQuery(
      "(3 + xs:untypedAtomic(\"3\")) eq 6",
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
   *  A test whose essence is: `(xs:untypedAtomic("3") + 3.0) eq 6`. .
   */
  @org.junit.Test
  public void kNumericAdd46() {
    final XQuery query = new XQuery(
      "(xs:untypedAtomic(\"3\") + 3.0) eq 6",
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
   *  Complex combination of numeric arithmetics in order to stress operator precedence. .
   */
  @org.junit.Test
  public void kNumericAdd47() {
    final XQuery query = new XQuery(
      "(2 + 4) * 5 eq 30",
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
   *  Complex combination of numeric arithmetics in order to stress operator precedence. .
   */
  @org.junit.Test
  public void kNumericAdd48() {
    final XQuery query = new XQuery(
      "2 + 4 * 5 eq 22",
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
   *  Complex combination of numeric arithmetics in order to stress operator precedence. .
   */
  @org.junit.Test
  public void kNumericAdd49() {
    final XQuery query = new XQuery(
      "1 + 2 * 4 + (1 + 2 + 3 * 4) eq 24",
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
   *  A test whose essence is: `xs:double(1.1) + xs:double(2.2) ne 3.3`. .
   */
  @org.junit.Test
  public void kNumericAdd5() {
    final XQuery query = new XQuery(
      "xs:double(1.1) + xs:double(2.2) ne 3.3",
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
  public void kNumericAdd50() {
    final XQuery query = new XQuery(
      "empty(() + ())",
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
   *  Implementations supporting the static typing feature may raise XPTy0004. .
   */
  @org.junit.Test
  public void kNumericAdd51() {
    final XQuery query = new XQuery(
      "(remove((1, \"two\"), 2) + 1) eq 2",
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
        error("XPTY0004")
      )
    );
  }

  /**
   *  Implementations supporting the static typing feature may raise XPTy0004. .
   */
  @org.junit.Test
  public void kNumericAdd52() {
    final XQuery query = new XQuery(
      "2 eq (1 + remove((1, \"two\"), 2))",
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
        error("XPTY0004")
      )
    );
  }

  /**
   *  Implementations supporting the static typing feature may raise XPTy0004. .
   */
  @org.junit.Test
  public void kNumericAdd53() {
    final XQuery query = new XQuery(
      "(remove((1, \"two\"), 2) + xs:untypedAtomic(\"1\")) eq 2",
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
        error("XPTY0004")
      )
    );
  }

  /**
   *  Implementations supporting the static typing feature may raise XPTy0004. .
   */
  @org.junit.Test
  public void kNumericAdd54() {
    final XQuery query = new XQuery(
      "2 eq (xs:untypedAtomic(\"1\") + remove((1, \"two\"), 2))",
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
        error("XPTY0004")
      )
    );
  }

  /**
   *  Implementations supporting the static typing feature may raise XPTy0004. .
   */
  @org.junit.Test
  public void kNumericAdd55() {
    final XQuery query = new XQuery(
      "(remove((xs:untypedAtomic(\"1\"), \"two\"), 2) + 1) eq 2",
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
        error("XPTY0004")
      )
    );
  }

  /**
   *  Implementations supporting the static typing feature may raise XPTy0004. .
   */
  @org.junit.Test
  public void kNumericAdd56() {
    final XQuery query = new XQuery(
      "2 eq (1 + remove((xs:untypedAtomic(\"1\"), \"two\"), 2))",
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
        error("XPTY0004")
      )
    );
  }

  /**
   *  Implementations supporting the static typing feature may raise XPTy0004. .
   */
  @org.junit.Test
  public void kNumericAdd57() {
    final XQuery query = new XQuery(
      "(remove((1, \"two\"), 2) + xs:untypedAtomic(\"1\")) eq 2",
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
        error("XPTY0004")
      )
    );
  }

  /**
   *  Implementations supporting the static typing feature may raise XPTy0004. .
   */
  @org.junit.Test
  public void kNumericAdd58() {
    final XQuery query = new XQuery(
      "2 eq (xs:untypedAtomic(\"1\") + remove((1, \"two\"), 2))",
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
        error("XPTY0004")
      )
    );
  }

  /**
   *  A test whose essence is: `xs:untypedAtomic("3") + 3 eq 6`. .
   */
  @org.junit.Test
  public void kNumericAdd59() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"3\") + 3 eq 6",
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
   *  A test whose essence is: `xs:float(1.1) + xs:float(2.2) ne 3.3`. .
   */
  @org.junit.Test
  public void kNumericAdd6() {
    final XQuery query = new XQuery(
      "xs:float(1.1) + xs:float(2.2) ne 3.3",
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
   *  A test whose essence is: `(xs:untypedAtomic("3") + 3) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNumericAdd60() {
    final XQuery query = new XQuery(
      "(xs:untypedAtomic(\"3\") + 3) instance of xs:double",
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
   *  A test whose essence is: `(remove((5, 1e0), 2) + 1) eq 6`. .
   */
  @org.junit.Test
  public void kNumericAdd61() {
    final XQuery query = new XQuery(
      "(remove((5, 1e0), 2) + 1) eq 6",
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
        error("XPTY0004")
      )
    );
  }

  /**
   *  A test whose essence is: `(1 + remove((5, 1e0), 2)) eq 6`. .
   */
  @org.junit.Test
  public void kNumericAdd62() {
    final XQuery query = new XQuery(
      "(1 + remove((5, 1e0), 2)) eq 6",
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
        error("XPTY0004")
      )
    );
  }

  /**
   *  Invoke operator '+' where one of the operands, using subsequence(), evaluates to an invalid cardinality. .
   */
  @org.junit.Test
  public void kNumericAdd63() {
    final XQuery query = new XQuery(
      "1 + subsequence(\"a string\", 1, 1)",
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
   *  Invoke operator '+' where one of the operands, using subsequence(), evaluates to an invalid cardinality. .
   */
  @org.junit.Test
  public void kNumericAdd64() {
    final XQuery query = new XQuery(
      "subsequence(\"a string\", 1, 1) + 1",
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
   *  Invoke operator '+' where one of the operands is a string. .
   */
  @org.junit.Test
  public void kNumericAdd65() {
    final XQuery query = new XQuery(
      "\"foo\" + 1",
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
   *  Invoke operator '+' where one of the operands is a string. .
   */
  @org.junit.Test
  public void kNumericAdd66() {
    final XQuery query = new XQuery(
      "1 + \"foo\"",
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
   *  A test whose essence is: `xs:integer(6) + xs:integer(2) eq 8`. .
   */
  @org.junit.Test
  public void kNumericAdd7() {
    final XQuery query = new XQuery(
      "xs:integer(6) + xs:integer(2) eq 8",
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
   *  A test whose essence is: `xs:float(6) + xs:float(2) eq 8`. .
   */
  @org.junit.Test
  public void kNumericAdd8() {
    final XQuery query = new XQuery(
      "xs:float(6) + xs:float(2) eq 8",
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
   *  A test whose essence is: `string(xs:float("NaN") + 3) eq "NaN"`. .
   */
  @org.junit.Test
  public void kNumericAdd9() {
    final XQuery query = new XQuery(
      "string(xs:float(\"NaN\") + 3) eq \"NaN\"",
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
   *  Arithmethics involving operands of integer type but where both have wrong cardinality. .
   */
  @org.junit.Test
  public void k2NumericAdd1() {
    final XQuery query = new XQuery(
      "(1, 2) + (1, 2)",
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
   *  Arithmetics where the operands are directly constructed nodes. .
   */
  @org.junit.Test
  public void k2NumericAdd2() {
    final XQuery query = new XQuery(
      "<a>1</a> + <b>2</b>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("3")
    );
  }

  /**
   *  Arithmetics where the operands are directly constructed attributes. .
   */
  @org.junit.Test
  public void k2NumericAdd3() {
    final XQuery query = new XQuery(
      "<a foo=\"1\"/>/@foo + <b foo=\"2\"/>/@foo",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("3")
    );
  }

  /**
   *  Check dynamic type of numeric add on arguments of union of numeric types and untypedAtomic. .
   */
  @org.junit.Test
  public void opNumericAdd1() {
    final XQuery query = new XQuery(
      "for $x in (1, xs:decimal(2), xs:float(3), xs:double(4), xs:untypedAtomic(5)) for $y in (1, xs:decimal(2), xs:float(3), xs:double(4), xs:untypedAtomic(5)) return typeswitch ($x + $y) case xs:integer return \"integer\" case xs:decimal return \"decimal\" case xs:float return \"float\" case xs:double return \"double\" default return error()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "integer decimal float double double decimal decimal float double double float float float double double double double double double double double double double double double")
    );
  }

  /**
   *  Evaluates The "op:numeric-add" operator with the operators set as follows $arg1 = xs:long $arg2 = xs:unsignedLong .
   */
  @org.junit.Test
  public void opNumericAddDerived1() {
    final XQuery query = new XQuery(
      "xs:long(10) + xs:unsignedLong(35)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("45")
    );
  }

  /**
   *  Evaluates The "op:numeric-add" operator with the operators set as follows $arg1 = xs:positiveInteger $arg2 = xs:nonPositiveInteger .
   */
  @org.junit.Test
  public void opNumericAddDerived2() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(10) + xs:nonPositiveInteger(-15)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-5")
    );
  }

  /**
   *  Evaluates The "op:numeric-add" operator with the operators set as follows $arg1 = xs:nonNegativeInteger $arg2 = xs:short .
   */
  @org.junit.Test
  public void opNumericAddDerived3() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(10) + xs:short(15)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("25")
    );
  }

  /**
   *  Evaluates The "op:numeric-add" operator with the operators set as follows $arg1 = xs:short $arg2 = xs:long .
   */
  @org.junit.Test
  public void opNumericAddDerived4() {
    final XQuery query = new XQuery(
      "xs:short(10) + xs:long(145)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("155")
    );
  }

  /**
   *  Evaluates The "op:numeric-add" operator with the operators set as follows $arg1 = xs:positiveInteger $arg2 = xs:negativeInteger .
   */
  @org.junit.Test
  public void opNumericAddDerived5() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(10) + xs:negativeInteger(-5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("5")
    );
  }

  /**
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:double(mid range) $arg2 = xs:double(lower bound) .
   */
  @org.junit.Test
  public void opNumericAdddbl2args1() {
    final XQuery query = new XQuery(
      "xs:double(\"0\") + xs:double(\"-1.7976931348623157E308\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:double(upper bound) $arg2 = xs:double(lower bound) .
   */
  @org.junit.Test
  public void opNumericAdddbl2args2() {
    final XQuery query = new XQuery(
      "xs:double(\"1.7976931348623157E308\") + xs:double(\"-1.7976931348623157E308\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:double(lower bound) $arg2 = xs:double(mid range) .
   */
  @org.junit.Test
  public void opNumericAdddbl2args3() {
    final XQuery query = new XQuery(
      "xs:double(\"-1.7976931348623157E308\") + xs:double(\"0\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:double(lower bound) $arg2 = xs:double(upper bound) .
   */
  @org.junit.Test
  public void opNumericAdddbl2args4() {
    final XQuery query = new XQuery(
      "xs:double(\"-1.7976931348623157E308\") + xs:double(\"1.7976931348623157E308\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:decimal(mid range) $arg2 = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void opNumericAdddec2args1() {
    final XQuery query = new XQuery(
      "xs:decimal(\"617375191608514839\") + xs:decimal(\"-999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-382624808391485160")
    );
  }

  /**
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:decimal(upper bound) $arg2 = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void opNumericAdddec2args2() {
    final XQuery query = new XQuery(
      "xs:decimal(\"999999999999999999\") + xs:decimal(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:decimal(lower bound) $arg2 = xs:decimal(mid range) .
   */
  @org.junit.Test
  public void opNumericAdddec2args3() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-999999999999999999\") + xs:decimal(\"617375191608514839\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-382624808391485160")
    );
  }

  /**
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:decimal(lower bound) $arg2 = xs:decimal(upper bound) .
   */
  @org.junit.Test
  public void opNumericAdddec2args4() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-999999999999999999\") + xs:decimal(\"999999999999999999\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:float(mid range) $arg2 = xs:float(lower bound) .
   */
  @org.junit.Test
  public void opNumericAddflt2args1() {
    final XQuery query = new XQuery(
      "xs:float(\"0\") + xs:float(\"-3.4028235E38\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:float(upper bound) $arg2 = xs:float(lower bound) .
   */
  @org.junit.Test
  public void opNumericAddflt2args2() {
    final XQuery query = new XQuery(
      "xs:float(\"3.4028235E38\") + xs:float(\"-3.4028235E38\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:float(lower bound) $arg2 = xs:float(mid range) .
   */
  @org.junit.Test
  public void opNumericAddflt2args3() {
    final XQuery query = new XQuery(
      "xs:float(\"-3.4028235E38\") + xs:float(\"0\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:float(lower bound) $arg2 = xs:float(upper bound) .
   */
  @org.junit.Test
  public void opNumericAddflt2args4() {
    final XQuery query = new XQuery(
      "xs:float(\"-3.4028235E38\") + xs:float(\"3.4028235E38\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:int(mid range) $arg2 = xs:int(lower bound) .
   */
  @org.junit.Test
  public void opNumericAddint2args1() {
    final XQuery query = new XQuery(
      "xs:int(\"-1873914410\") + xs:int(\"-273569238\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:int(upper bound) $arg2 = xs:int(lower bound) .
   */
  @org.junit.Test
  public void opNumericAddint2args2() {
    final XQuery query = new XQuery(
      "xs:int(\"2147483647\") + xs:int(\"-2147483648\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:int(lower bound) $arg2 = xs:int(mid range) .
   */
  @org.junit.Test
  public void opNumericAddint2args3() {
    final XQuery query = new XQuery(
      "xs:int(\"-273569238\") + xs:int(\"-1873914410\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:int(lower bound) $arg2 = xs:int(upper bound) .
   */
  @org.junit.Test
  public void opNumericAddint2args4() {
    final XQuery query = new XQuery(
      "xs:int(\"-2147483648\") + xs:int(\"2147483647\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:integer(mid range) $arg2 = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void opNumericAddintg2args1() {
    final XQuery query = new XQuery(
      "xs:integer(\"830993497117024304\") + xs:integer(\"-999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-169006502882975695")
    );
  }

  /**
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:integer(upper bound) $arg2 = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void opNumericAddintg2args2() {
    final XQuery query = new XQuery(
      "xs:integer(\"999999999999999999\") + xs:integer(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:integer(lower bound) $arg2 = xs:integer(mid range) .
   */
  @org.junit.Test
  public void opNumericAddintg2args3() {
    final XQuery query = new XQuery(
      "xs:integer(\"-999999999999999999\") + xs:integer(\"830993497117024304\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-169006502882975695")
    );
  }

  /**
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:integer(lower bound) $arg2 = xs:integer(upper bound) .
   */
  @org.junit.Test
  public void opNumericAddintg2args4() {
    final XQuery query = new XQuery(
      "xs:integer(\"-999999999999999999\") + xs:integer(\"999999999999999999\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:long(mid range) $arg2 = xs:long(lower bound) .
   */
  @org.junit.Test
  public void opNumericAddlng2args1() {
    final XQuery query = new XQuery(
      "xs:long(\"-47175562203048468\") + xs:long(\"-45058158165499290\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:long(upper bound) $arg2 = xs:long(lower bound) .
   */
  @org.junit.Test
  public void opNumericAddlng2args2() {
    final XQuery query = new XQuery(
      "xs:long(\"92233720368547758\") + xs:long(\"-92233720368547758\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:long(lower bound) $arg2 = xs:long(mid range) .
   */
  @org.junit.Test
  public void opNumericAddlng2args3() {
    final XQuery query = new XQuery(
      "xs:long(\"-45058158165499290\") + xs:long(\"-47175562203048468\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:long(lower bound) $arg2 = xs:long(upper bound) .
   */
  @org.junit.Test
  public void opNumericAddlng2args4() {
    final XQuery query = new XQuery(
      "xs:long(\"-92233720368547758\") + xs:long(\"92233720368547758\")",
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
   *  Simple addition test with () as one operand should return null .
   */
  @org.junit.Test
  public void opNumericAddmix2args1() {
    final XQuery query = new XQuery(
      "1 + ()",
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
   *  Simple addition test pass string for second operator .
   */
  @org.junit.Test
  public void opNumericAddmix2args2() {
    final XQuery query = new XQuery(
      "1 + '1'",
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
   *  Simple addition test, second operator cast string to integer .
   */
  @org.junit.Test
  public void opNumericAddmix2args3() {
    final XQuery query = new XQuery(
      "1 + xs:integer('1')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("2")
    );
  }

  /**
   *  Simple addition test, second operator is a node, atomization applied .
   */
  @org.junit.Test
  public void opNumericAddmix2args4() {
    final XQuery query = new XQuery(
      "1 + <a> 2 </a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("3")
    );
  }

  /**
   *  Simple addition test, second operator is a node, atomization applied .
   */
  @org.junit.Test
  public void opNumericAddmix2args5() {
    final XQuery query = new XQuery(
      "1+<a> <b> 2 </b> </a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("3")
    );
  }

  /**
   *  Simple addition test, second operator node which is not atomizable .
   */
  @org.junit.Test
  public void opNumericAddmix2args6() {
    final XQuery query = new XQuery(
      "1 + <a> <b> 2</b> <c> 2</c> </a>",
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
   *  Simple addition test, two operands are nodes .
   */
  @org.junit.Test
  public void opNumericAddmix2args7() {
    final XQuery query = new XQuery(
      "<a> 1 </a> + <b> 2 </b>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("3")
    );
  }

  /**
   *  Simple addition test, second operator is a node, atomizable but not castable to integer .
   */
  @org.junit.Test
  public void opNumericAddmix2args8() {
    final XQuery query = new XQuery(
      "1 + <a> x </a>",
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
   *  Simple addition test pass an empty node for second operator .
   */
  @org.junit.Test
  public void opNumericAddmix2args9() {
    final XQuery query = new XQuery(
      "1 + <a/>",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:negativeInteger(mid range) $arg2 = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericAddnint2args1() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-297014075999096793\") + xs:negativeInteger(\"-702985924000903206\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:negativeInteger(upper bound) $arg2 = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericAddnint2args2() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-1\") + xs:negativeInteger(\"-999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1000000000000000000")
    );
  }

  /**
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:negativeInteger(lower bound) $arg2 = xs:negativeInteger(mid range) .
   */
  @org.junit.Test
  public void opNumericAddnint2args3() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-702985924000903206\") + xs:negativeInteger(\"-297014075999096793\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:negativeInteger(lower bound) $arg2 = xs:negativeInteger(upper bound) .
   */
  @org.junit.Test
  public void opNumericAddnint2args4() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-999999999999999999\") + xs:negativeInteger(\"-1\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1000000000000000000")
    );
  }

  /**
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:nonNegativeInteger(lower bound) $arg2 = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericAddnni2args1() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"0\") + xs:nonNegativeInteger(\"0\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:nonNegativeInteger(mid range) $arg2 = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericAddnni2args2() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"303884545991464527\") + xs:nonNegativeInteger(\"0\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:nonNegativeInteger(upper bound) $arg2 = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericAddnni2args3() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"999999999999999999\") + xs:nonNegativeInteger(\"0\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:nonNegativeInteger(lower bound) $arg2 = xs:nonNegativeInteger(mid range) .
   */
  @org.junit.Test
  public void opNumericAddnni2args4() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"0\") + xs:nonNegativeInteger(\"303884545991464527\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:nonNegativeInteger(lower bound) $arg2 = xs:nonNegativeInteger(upper bound) .
   */
  @org.junit.Test
  public void opNumericAddnni2args5() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"0\") + xs:nonNegativeInteger(\"999999999999999999\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:nonPositiveInteger(mid range) $arg2 = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericAddnpi2args1() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"-475688437271870490\") + xs:nonPositiveInteger(\"-524311562728129509\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:nonPositiveInteger(upper bound) $arg2 = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericAddnpi2args2() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"0\") + xs:nonPositiveInteger(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:nonPositiveInteger(lower bound) $arg2 = xs:nonPositiveInteger(mid range) .
   */
  @org.junit.Test
  public void opNumericAddnpi2args3() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"-524311562728129509\") + xs:nonPositiveInteger(\"-475688437271870490\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:nonPositiveInteger(lower bound) $arg2 = xs:nonPositiveInteger(upper bound) .
   */
  @org.junit.Test
  public void opNumericAddnpi2args4() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"-999999999999999999\") + xs:nonPositiveInteger(\"0\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:positiveInteger(lower bound) $arg2 = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericAddpint2args1() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"1\") + xs:positiveInteger(\"1\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("2")
    );
  }

  /**
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:positiveInteger(mid range) $arg2 = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericAddpint2args2() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"52704602390610033\") + xs:positiveInteger(\"1\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("52704602390610034")
    );
  }

  /**
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:positiveInteger(upper bound) $arg2 = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericAddpint2args3() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"999999999999999998\") + xs:positiveInteger(\"1\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:positiveInteger(lower bound) $arg2 = xs:positiveInteger(mid range) .
   */
  @org.junit.Test
  public void opNumericAddpint2args4() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"1\") + xs:positiveInteger(\"52704602390610033\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("52704602390610034")
    );
  }

  /**
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:positiveInteger(lower bound) $arg2 = xs:positiveInteger(upper bound) .
   */
  @org.junit.Test
  public void opNumericAddpint2args5() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"1\") + xs:positiveInteger(\"999999999999999998\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:short(mid range) $arg2 = xs:short(lower bound) .
   */
  @org.junit.Test
  public void opNumericAddsht2args1() {
    final XQuery query = new XQuery(
      "xs:short(\"-5324\") + xs:short(\"-27444\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:short(upper bound) $arg2 = xs:short(lower bound) .
   */
  @org.junit.Test
  public void opNumericAddsht2args2() {
    final XQuery query = new XQuery(
      "xs:short(\"32767\") + xs:short(\"-32768\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:short(lower bound) $arg2 = xs:short(mid range) .
   */
  @org.junit.Test
  public void opNumericAddsht2args3() {
    final XQuery query = new XQuery(
      "xs:short(\"-27444\") + xs:short(\"-5324\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:short(lower bound) $arg2 = xs:short(upper bound) .
   */
  @org.junit.Test
  public void opNumericAddsht2args4() {
    final XQuery query = new XQuery(
      "xs:short(\"-32768\") + xs:short(\"32767\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:unsignedLong(lower bound) $arg2 = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void opNumericAddulng2args1() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"0\") + xs:unsignedLong(\"0\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:unsignedLong(mid range) $arg2 = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void opNumericAddulng2args2() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"130747108607674654\") + xs:unsignedLong(\"0\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:unsignedLong(upper bound) $arg2 = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void opNumericAddulng2args3() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"184467440737095516\") + xs:unsignedLong(\"0\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:unsignedLong(lower bound) $arg2 = xs:unsignedLong(mid range) .
   */
  @org.junit.Test
  public void opNumericAddulng2args4() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"0\") + xs:unsignedLong(\"130747108607674654\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:unsignedLong(lower bound) $arg2 = xs:unsignedLong(upper bound) .
   */
  @org.junit.Test
  public void opNumericAddulng2args5() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"0\") + xs:unsignedLong(\"184467440737095516\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:unsignedShort(lower bound) $arg2 = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void opNumericAddusht2args1() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"0\") + xs:unsignedShort(\"0\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:unsignedShort(mid range) $arg2 = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void opNumericAddusht2args2() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"44633\") + xs:unsignedShort(\"0\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:unsignedShort(upper bound) $arg2 = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void opNumericAddusht2args3() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"65535\") + xs:unsignedShort(\"0\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:unsignedShort(lower bound) $arg2 = xs:unsignedShort(mid range) .
   */
  @org.junit.Test
  public void opNumericAddusht2args4() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"0\") + xs:unsignedShort(\"44633\")",
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
   *  Evaluates The "op:numeric-add" operator with the arguments set as follows: $arg1 = xs:unsignedShort(lower bound) $arg2 = xs:unsignedShort(upper bound) .
   */
  @org.junit.Test
  public void opNumericAddusht2args5() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"0\") + xs:unsignedShort(\"65535\")",
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
