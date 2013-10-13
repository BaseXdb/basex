package org.basex.test.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the numeric-integer-divide() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpNumericIntegerDivide extends QT3TestSet {

  /**
   *  A test whose essence is: `(xs:decimal(6) idiv xs:integer(2)) instance of xs:integer`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide1() {
    final XQuery query = new XQuery(
      "(xs:decimal(6) idiv xs:integer(2)) instance of xs:integer",
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
   *  A test whose essence is: `(xs:float(6) idiv xs:float(2)) instance of xs:integer`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide10() {
    final XQuery query = new XQuery(
      "(xs:float(6) idiv xs:float(2)) instance of xs:integer",
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
   *  A test whose essence is: `(xs:decimal(6) idiv xs:double(2)) instance of xs:integer`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide11() {
    final XQuery query = new XQuery(
      "(xs:decimal(6) idiv xs:double(2)) instance of xs:integer",
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
   *  A test whose essence is: `(xs:double(6) idiv xs:decimal(2)) instance of xs:integer`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide12() {
    final XQuery query = new XQuery(
      "(xs:double(6) idiv xs:decimal(2)) instance of xs:integer",
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
   *  A test whose essence is: `(xs:double(6) idiv xs:float(2)) instance of xs:integer`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide13() {
    final XQuery query = new XQuery(
      "(xs:double(6) idiv xs:float(2)) instance of xs:integer",
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
   *  A test whose essence is: `(xs:float(6) idiv xs:double(2)) instance of xs:integer`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide14() {
    final XQuery query = new XQuery(
      "(xs:float(6) idiv xs:double(2)) instance of xs:integer",
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
   *  A test whose essence is: `(xs:double(6) idiv xs:integer(2)) instance of xs:integer`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide15() {
    final XQuery query = new XQuery(
      "(xs:double(6) idiv xs:integer(2)) instance of xs:integer",
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
   *  A test whose essence is: `(xs:integer(6) idiv xs:double(2)) instance of xs:integer`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide16() {
    final XQuery query = new XQuery(
      "(xs:integer(6) idiv xs:double(2)) instance of xs:integer",
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
   *  A test whose essence is: `(xs:double(6) idiv xs:double(2)) instance of xs:integer`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide17() {
    final XQuery query = new XQuery(
      "(xs:double(6) idiv xs:double(2)) instance of xs:integer",
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
   *  A test whose essence is: `10 idiv 3 eq 3`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide18() {
    final XQuery query = new XQuery(
      "10 idiv 3 eq 3",
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
   *  A test whose essence is: `3 idiv -2 eq -1`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide19() {
    final XQuery query = new XQuery(
      "3 idiv -2 eq -1",
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
   *  A test whose essence is: `(xs:integer(6) idiv xs:decimal(2)) instance of xs:integer`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide2() {
    final XQuery query = new XQuery(
      "(xs:integer(6) idiv xs:decimal(2)) instance of xs:integer",
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
   *  A test whose essence is: `-3 idiv 2 eq -1`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide20() {
    final XQuery query = new XQuery(
      "-3 idiv 2 eq -1",
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
   *  A test whose essence is: `-3 idiv -2 eq 1`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide21() {
    final XQuery query = new XQuery(
      "-3 idiv -2 eq 1",
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
   *  A test whose essence is: `5.0 idiv 2.0 eq 2`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide22() {
    final XQuery query = new XQuery(
      "5.0 idiv 2.0 eq 2",
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
   *  A test whose essence is: `9.0 idiv 3 eq 3`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide23() {
    final XQuery query = new XQuery(
      "9.0 idiv 3 eq 3",
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
   *  A test whose essence is: `-3.5 idiv 3 eq -1`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide24() {
    final XQuery query = new XQuery(
      "-3.5 idiv 3 eq -1",
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
   *  A test whose essence is: `3.0 idiv 4 eq 0`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide25() {
    final XQuery query = new XQuery(
      "3.0 idiv 4 eq 0",
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
   *  A test whose essence is: `3.1E1 idiv 6 eq 5`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide26() {
    final XQuery query = new XQuery(
      "3.1E1 idiv 6 eq 5",
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
   *  A test whose essence is: `3.1E1 idiv 7 eq 4`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide27() {
    final XQuery query = new XQuery(
      "3.1E1 idiv 7 eq 4",
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
   *  A test whose essence is: `(1.1 idiv 1) instance of xs:integer`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide28() {
    final XQuery query = new XQuery(
      "(1.1 idiv 1) instance of xs:integer",
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
   *  A test whose essence is: `(xs:double(1.1) idiv 1.1) instance of xs:integer`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide29() {
    final XQuery query = new XQuery(
      "(xs:double(1.1) idiv 1.1) instance of xs:integer",
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
   *  A test whose essence is: `(xs:integer(6) idiv xs:integer(2)) instance of xs:integer`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide3() {
    final XQuery query = new XQuery(
      "(xs:integer(6) idiv xs:integer(2)) instance of xs:integer",
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
   *  A test whose essence is: `3 idiv 1.1 eq 2`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide30() {
    final XQuery query = new XQuery(
      "3 idiv 1.1 eq 2",
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
   *  A test whose essence is: `1 idiv xs:float("NaN")`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide31() {
    final XQuery query = new XQuery(
      "1 idiv xs:float(\"NaN\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOAR0002")
    );
  }

  /**
   *  A test whose essence is: `xs:float("NaN") idiv 1`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide32() {
    final XQuery query = new XQuery(
      "xs:float(\"NaN\") idiv 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOAR0002")
    );
  }

  /**
   *  A test whose essence is: `xs:float("INF") idiv xs:float(3)`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide33() {
    final XQuery query = new XQuery(
      "xs:float(\"INF\") idiv xs:float(3)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOAR0002")
    );
  }

  /**
   *  A test whose essence is: `xs:float("-INF") idiv xs:float(3)`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide34() {
    final XQuery query = new XQuery(
      "xs:float(\"-INF\") idiv xs:float(3)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOAR0002")
    );
  }

  /**
   *  A test whose essence is: `xs:float("3") idiv xs:float("INF") eq xs:float(0)`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide35() {
    final XQuery query = new XQuery(
      "xs:float(\"3\") idiv xs:float(\"INF\") eq xs:float(0)",
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
   *  A test whose essence is: `xs:float("3") idiv xs:float("-INF") eq xs:float(0)`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide36() {
    final XQuery query = new XQuery(
      "xs:float(\"3\") idiv xs:float(\"-INF\") eq xs:float(0)",
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
   *  A test whose essence is: `1 idiv xs:double("NaN")`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide37() {
    final XQuery query = new XQuery(
      "1 idiv xs:double(\"NaN\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOAR0002")
    );
  }

  /**
   *  A test whose essence is: `xs:double("NaN") idiv 1`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide38() {
    final XQuery query = new XQuery(
      "xs:double(\"NaN\") idiv 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOAR0002")
    );
  }

  /**
   *  A test whose essence is: `xs:double("INF") idiv xs:double(3)`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide39() {
    final XQuery query = new XQuery(
      "xs:double(\"INF\") idiv xs:double(3)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOAR0002")
    );
  }

  /**
   *  A test whose essence is: `(xs:decimal(6) idiv xs:decimal(2)) instance of xs:integer`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide4() {
    final XQuery query = new XQuery(
      "(xs:decimal(6) idiv xs:decimal(2)) instance of xs:integer",
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
   *  A test whose essence is: `xs:double("-INF") idiv xs:double(3)`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide40() {
    final XQuery query = new XQuery(
      "xs:double(\"-INF\") idiv xs:double(3)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOAR0002")
    );
  }

  /**
   *  A test whose essence is: `xs:double("3") idiv xs:double("INF") eq xs:double(0)`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide41() {
    final XQuery query = new XQuery(
      "xs:double(\"3\") idiv xs:double(\"INF\") eq xs:double(0)",
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
   *  A test whose essence is: `xs:double("3") idiv xs:double("-INF") eq xs:double(0)`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide42() {
    final XQuery query = new XQuery(
      "xs:double(\"3\") idiv xs:double(\"-INF\") eq xs:double(0)",
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
   *  Invalid whitespace for the 'idiv' operator. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide43() {
    final XQuery query = new XQuery(
      "10idiv 3",
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
   *  Invalid whitespace for the 'idiv' operator. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide44() {
    final XQuery query = new XQuery(
      "10 idiv3",
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
   *  Invalid whitespace for the 'idiv' operator. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide45() {
    final XQuery query = new XQuery(
      "10idiv3",
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
   *  Invoke the 'idiv operator with invalid operands. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide46() {
    final XQuery query = new XQuery(
      "\"3\" idiv \"3\"",
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
   *  Invoke the 'idiv' operator with invalid operands. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide47() {
    final XQuery query = new XQuery(
      "\"3\" idiv xs:float(3)",
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
   *  A test whose essence is: `(xs:untypedAtomic("9") idiv xs:float(5)) eq 1`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide48() {
    final XQuery query = new XQuery(
      "(xs:untypedAtomic(\"9\") idiv xs:float(5)) eq 1",
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
   *  A test whose essence is: `(xs:float(9) idiv xs:untypedAtomic("5")) eq 1`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide49() {
    final XQuery query = new XQuery(
      "(xs:float(9) idiv xs:untypedAtomic(\"5\")) eq 1",
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
   *  A test whose essence is: `(xs:decimal(6) idiv xs:decimal(2)) instance of xs:integer`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide5() {
    final XQuery query = new XQuery(
      "(xs:decimal(6) idiv xs:decimal(2)) instance of xs:integer",
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
   *  Invoke 'idiv' where an untypedAtomic conversion fails. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide50() {
    final XQuery query = new XQuery(
      "(xs:untypedAtomic(\"nine\") idiv xs:float(5)) eq 1",
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
   *  Invoke 'idiv' where an untypedAtomic conversion fails. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide51() {
    final XQuery query = new XQuery(
      "(xs:float(9) idiv xs:untypedAtomic(\"five\")) eq 1",
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
   *  Operand(s) which are the empty sequence. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide52() {
    final XQuery query = new XQuery(
      "empty(() idiv ())",
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
  public void kNumericIntegerDivide53() {
    final XQuery query = new XQuery(
      "empty(() idiv xs:decimal(1))",
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
   *  A test whose essence is: `(xs:float(6) idiv xs:decimal(2)) instance of xs:integer`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide6() {
    final XQuery query = new XQuery(
      "(xs:float(6) idiv xs:decimal(2)) instance of xs:integer",
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
   *  A test whose essence is: `(xs:decimal(6) idiv xs:float(2)) instance of xs:integer`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide7() {
    final XQuery query = new XQuery(
      "(xs:decimal(6) idiv xs:float(2)) instance of xs:integer",
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
   *  A test whose essence is: `(xs:float(6) idiv xs:integer(2)) instance of xs:integer`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide8() {
    final XQuery query = new XQuery(
      "(xs:float(6) idiv xs:integer(2)) instance of xs:integer",
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
   *  A test whose essence is: `(xs:integer(6) idiv xs:float(2)) instance of xs:integer`. .
   */
  @org.junit.Test
  public void kNumericIntegerDivide9() {
    final XQuery query = new XQuery(
      "(xs:integer(6) idiv xs:float(2)) instance of xs:integer",
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
   *  Integer divide by 0. .
   */
  @org.junit.Test
  public void k2NumericIntegerDivide1() {
    final XQuery query = new XQuery(
      "1 idiv 0",
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
   *  Integer divide by 0.0. .
   */
  @org.junit.Test
  public void k2NumericIntegerDivide2() {
    final XQuery query = new XQuery(
      "1 idiv 0.0",
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
   *  Integer divide by +0.0. .
   */
  @org.junit.Test
  public void k2NumericIntegerDivide3() {
    final XQuery query = new XQuery(
      "1 idiv +0.0",
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
   *  Integer divide by -0.0. .
   */
  @org.junit.Test
  public void k2NumericIntegerDivide4() {
    final XQuery query = new XQuery(
      "1 idiv -0.0",
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
   *  Integer divide by 0e0. .
   */
  @org.junit.Test
  public void k2NumericIntegerDivide5() {
    final XQuery query = new XQuery(
      "1 idiv 0e0",
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
   *  Integer divide -1 by 0e0. .
   */
  @org.junit.Test
  public void k2NumericIntegerDivide6() {
    final XQuery query = new XQuery(
      "-1 idiv 0e0",
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
   *  Integer divide by xs:float(0). .
   */
  @org.junit.Test
  public void k2NumericIntegerDivide7() {
    final XQuery query = new XQuery(
      "1 idiv xs:float(0)",
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
   *  Integer divide -1 by xs:float(0). .
   */
  @org.junit.Test
  public void k2NumericIntegerDivide8() {
    final XQuery query = new XQuery(
      "-1 idiv xs:float(0)",
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
   *  test possible overflow of xs:decimal .
   */
  @org.junit.Test
  public void cbclNumericIdivide001() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:decimal) as xs:decimal { $arg * $arg }; \n" +
      "      \tlocal:square(4294967296.0) idiv 0.0000000000005 gt 0",
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
        error("FOAR0002")
      )
    );
  }

  /**
   *  test possible overflow of xs:double .
   */
  @org.junit.Test
  public void cbclNumericIdivide002() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:double) as xs:double { $arg * $arg }; \n" +
      "      \tlocal:square(1e100) idiv 5e-100",
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
        (
          assertType("xs:integer")
        &&
          assertQuery("string-length(string($result)) gt 299")
        )
      )
    );
  }

  /**
   *  test possible overflow of xs:float .
   */
  @org.junit.Test
  public void cbclNumericIdivide003() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:float) as xs:float { $arg * $arg }; \n" +
      "      \tlocal:square(xs:float(1e30)) idiv xs:float(5e-30)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOAR0002")
    );
  }

  /**
   *  ensure that idividing xs:integers returns an xs:integer .
   */
  @org.junit.Test
  public void cbclNumericIdivide004() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:integer($x as xs:decimal) { if ($x mod 1 eq 0) then xs:integer($x) else $x };\n" +
      "      \t (local:integer(2) idiv local:integer(2)) instance of xs:integer",
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
   *  Test idiv by one on an xs:integer .
   */
  @org.junit.Test
  public void cbclNumericIdivide005() {
    final XQuery query = new XQuery(
      "xs:integer(2) idiv xs:decimal(1)",
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
   *  Test idiv by one on an xs:float .
   */
  @org.junit.Test
  public void cbclNumericIdivide006() {
    final XQuery query = new XQuery(
      "xs:float(1.5) idiv xs:decimal(1)",
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
   *  Test idiv by one on an xs:double .
   */
  @org.junit.Test
  public void cbclNumericIdivide007() {
    final XQuery query = new XQuery(
      "xs:double(1.5) idiv xs:decimal(1)",
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
   *  Test idiv by one on an xs:double .
   */
  @org.junit.Test
  public void cbclNumericIdivide008() {
    final XQuery query = new XQuery(
      "xs:float('1e38') idiv xs:float('1e-37')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOAR0002")
    );
  }

  /**
   *  Test numeric idiv with a potential type check error .
   */
  @org.junit.Test
  public void cbclNumericIdivide009() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:value($number as xs:boolean) { if ($number) then 1 else xs:string('1') }; \n" +
      "      \tlocal:value(true()) idiv local:value(true())\n" +
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
        assertEq("1")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  Evaluates The "op:numeric-integer-divide" operator for error condition. .
   */
  @org.junit.Test
  public void opNumericIntegerDivide1() {
    final XQuery query = new XQuery(
      "(0 div 0E0) idiv xs:integer(2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOAR0002")
    );
  }

  /**
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:decimal(lower bound) $arg2 = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void opNumericIntegerDividedec2args1() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-999999999999999999\") idiv xs:decimal(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:decimal(mid range) $arg2 = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void opNumericIntegerDividedec2args2() {
    final XQuery query = new XQuery(
      "xs:decimal(\"617375191608514839\") idiv xs:decimal(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:decimal(upper bound) $arg2 = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void opNumericIntegerDividedec2args3() {
    final XQuery query = new XQuery(
      "xs:decimal(\"999999999999999999\") idiv xs:decimal(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:decimal(lower bound) $arg2 = xs:decimal(mid range) .
   */
  @org.junit.Test
  public void opNumericIntegerDividedec2args4() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-999999999999999999\") idiv xs:decimal(\"617375191608514839\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:decimal(lower bound) $arg2 = xs:decimal(upper bound) .
   */
  @org.junit.Test
  public void opNumericIntegerDividedec2args5() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-999999999999999999\") idiv xs:decimal(\"999999999999999999\")",
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
   *  "op:numeric-integer-divide" with both arguments of type xs:double. Raise [err:FOAR0002] .
   */
  @org.junit.Test
  public void opNumericIntegerDividedouble2args1() {
    final XQuery query = new XQuery(
      "xs:double(\"-INF\") idiv xs:double(\"1\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOAR0002")
    );
  }

  /**
   *  "op:numeric-integer-divide" with both arguments of type xs:double. Raise [err:FOAR0002] .
   */
  @org.junit.Test
  public void opNumericIntegerDividedouble2args2() {
    final XQuery query = new XQuery(
      "xs:double(\"1\") idiv xs:double(\"NaN\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOAR0002")
    );
  }

  /**
   *  "op:numeric-integer-divide" with both arguments of type xs:double. Raise [err:FOAR0001] .
   */
  @org.junit.Test
  public void opNumericIntegerDividedouble2args3() {
    final XQuery query = new XQuery(
      "xs:double(\"1\") idiv xs:double(\"0.0E0\")",
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
   *  "op:numeric-integer-divide" with both arguments of type xs:double. Raise [err:FOAR0001] .
   */
  @org.junit.Test
  public void opNumericIntegerDividedouble2args4() {
    final XQuery query = new XQuery(
      "xs:double(\"INF\") idiv xs:double(\"0\")",
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
   *  "op:numeric-integer-divide" with both arguments of type xs:double. .
   */
  @org.junit.Test
  public void opNumericIntegerDividedouble2args5() {
    final XQuery query = new XQuery(
      "xs:double(\"12.78e-2\") idiv xs:double(\"3\")",
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
   *  Integer divide with operands of type xs:float. Raise [err:FOAR0002]. .
   */
  @org.junit.Test
  public void opNumericIntegerDividefloat2args1() {
    final XQuery query = new XQuery(
      "xs:float(\"-INF\") idiv xs:float(\"1\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOAR0002")
    );
  }

  /**
   *  Integer divide with operands of type xs:float. Raise [err:FOAR0002]. .
   */
  @org.junit.Test
  public void opNumericIntegerDividefloat2args2() {
    final XQuery query = new XQuery(
      "xs:float(\"1\") idiv xs:float(\"NaN\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOAR0002")
    );
  }

  /**
   *  Integer divide with operands of type xs:float. Raise [err:FOAR0001]. .
   */
  @org.junit.Test
  public void opNumericIntegerDividefloat2args3() {
    final XQuery query = new XQuery(
      "xs:float(\"1\") idiv xs:float(\"0\")",
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
   *  Integer divide with operands of type xs:float. Raise [err:FOAR0001]. .
   */
  @org.junit.Test
  public void opNumericIntegerDividefloat2args4() {
    final XQuery query = new XQuery(
      "xs:float(\"INF\") idiv xs:float(\"0\")",
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
   *  "op:numeric-integer-divide" with both arguments of type xs:float. .
   */
  @org.junit.Test
  public void opNumericIntegerDividefloat2args5() {
    final XQuery query = new XQuery(
      "xs:float(\"-3.5\") idiv xs:float(\"3\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:int(lower bound) $arg2 = xs:int(lower bound) .
   */
  @org.junit.Test
  public void opNumericIntegerDivideint2args1() {
    final XQuery query = new XQuery(
      "xs:int(\"-2147483648\") idiv xs:int(\"-2147483648\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:int(mid range) $arg2 = xs:int(lower bound) .
   */
  @org.junit.Test
  public void opNumericIntegerDivideint2args2() {
    final XQuery query = new XQuery(
      "xs:int(\"-1873914410\") idiv xs:int(\"-2147483648\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:int(upper bound) $arg2 = xs:int(lower bound) .
   */
  @org.junit.Test
  public void opNumericIntegerDivideint2args3() {
    final XQuery query = new XQuery(
      "xs:int(\"2147483647\") idiv xs:int(\"-2147483648\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:int(lower bound) $arg2 = xs:int(mid range) .
   */
  @org.junit.Test
  public void opNumericIntegerDivideint2args4() {
    final XQuery query = new XQuery(
      "xs:int(\"-2147483648\") idiv xs:int(\"-1873914410\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:int(lower bound) $arg2 = xs:int(upper bound) .
   */
  @org.junit.Test
  public void opNumericIntegerDivideint2args5() {
    final XQuery query = new XQuery(
      "xs:int(\"-2147483648\") idiv xs:int(\"2147483647\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:integer(lower bound) $arg2 = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void opNumericIntegerDivideintg2args1() {
    final XQuery query = new XQuery(
      "xs:integer(\"-999999999999999999\") idiv xs:integer(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:integer(mid range) $arg2 = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void opNumericIntegerDivideintg2args2() {
    final XQuery query = new XQuery(
      "xs:integer(\"830993497117024304\") idiv xs:integer(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:integer(upper bound) $arg2 = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void opNumericIntegerDivideintg2args3() {
    final XQuery query = new XQuery(
      "xs:integer(\"999999999999999999\") idiv xs:integer(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:integer(lower bound) $arg2 = xs:integer(mid range) .
   */
  @org.junit.Test
  public void opNumericIntegerDivideintg2args4() {
    final XQuery query = new XQuery(
      "xs:integer(\"-999999999999999999\") idiv xs:integer(\"830993497117024304\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:integer(lower bound) $arg2 = xs:integer(upper bound) .
   */
  @org.junit.Test
  public void opNumericIntegerDivideintg2args5() {
    final XQuery query = new XQuery(
      "xs:integer(\"-999999999999999999\") idiv xs:integer(\"999999999999999999\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:long(lower bound) $arg2 = xs:long(lower bound) .
   */
  @org.junit.Test
  public void opNumericIntegerDividelng2args1() {
    final XQuery query = new XQuery(
      "xs:long(\"-92233720368547758\") idiv xs:long(\"-92233720368547758\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:long(mid range) $arg2 = xs:long(lower bound) .
   */
  @org.junit.Test
  public void opNumericIntegerDividelng2args2() {
    final XQuery query = new XQuery(
      "xs:long(\"-47175562203048468\") idiv xs:long(\"-92233720368547758\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:long(upper bound) $arg2 = xs:long(lower bound) .
   */
  @org.junit.Test
  public void opNumericIntegerDividelng2args3() {
    final XQuery query = new XQuery(
      "xs:long(\"92233720368547758\") idiv xs:long(\"-92233720368547758\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:long(lower bound) $arg2 = xs:long(mid range) .
   */
  @org.junit.Test
  public void opNumericIntegerDividelng2args4() {
    final XQuery query = new XQuery(
      "xs:long(\"-92233720368547758\") idiv xs:long(\"-47175562203048468\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:long(lower bound) $arg2 = xs:long(upper bound) .
   */
  @org.junit.Test
  public void opNumericIntegerDividelng2args5() {
    final XQuery query = new XQuery(
      "xs:long(\"-92233720368547758\") idiv xs:long(\"92233720368547758\")",
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
   *  Simple integer division test with () as one operand should return null .
   */
  @org.junit.Test
  public void opNumericIntegerDividemix2args1() {
    final XQuery query = new XQuery(
      "() idiv 1",
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
   *  If the divisor is zer0, then an error is raised .
   */
  @org.junit.Test
  public void opNumericIntegerDividemix2args10() {
    final XQuery query = new XQuery(
      "1 idiv 0",
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
   *  Simple integer division test pass string for second operator .
   */
  @org.junit.Test
  public void opNumericIntegerDividemix2args2() {
    final XQuery query = new XQuery(
      "1 idiv '1'",
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
   *  Simple integer division test, second operator cast string to integer .
   */
  @org.junit.Test
  public void opNumericIntegerDividemix2args3() {
    final XQuery query = new XQuery(
      "1 idiv xs:integer('1')",
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
   *  Simple integer division test, second operator is a node, atomization applied .
   */
  @org.junit.Test
  public void opNumericIntegerDividemix2args4() {
    final XQuery query = new XQuery(
      "3 idiv <a> 2 </a>",
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
   *  Simple integer division test, second operator is a node, atomization applied .
   */
  @org.junit.Test
  public void opNumericIntegerDividemix2args5() {
    final XQuery query = new XQuery(
      "1 idiv <a> <b> 2 </b> </a>",
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
   *  Simple integer division test, second operator node which is not atomizable .
   */
  @org.junit.Test
  public void opNumericIntegerDividemix2args6() {
    final XQuery query = new XQuery(
      "3 idiv <a> <b> 2</b> <c> 2</c> </a>",
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
   *  Simple integer division test, two operands are nodes .
   */
  @org.junit.Test
  public void opNumericIntegerDividemix2args7() {
    final XQuery query = new XQuery(
      "<a> 1 </a> idiv <b> 2 </b>",
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
   *  Simple integer division test, second operator is a node, atomizable but not castable to integer .
   */
  @org.junit.Test
  public void opNumericIntegerDividemix2args8() {
    final XQuery query = new XQuery(
      "1 idiv <a> x </a>",
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
   *  Simple integer division test pass an empty node for second operator .
   */
  @org.junit.Test
  public void opNumericIntegerDividemix2args9() {
    final XQuery query = new XQuery(
      "1 idiv <a/>",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:negativeInteger(lower bound) $arg2 = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericIntegerDividenint2args1() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-999999999999999999\") idiv xs:negativeInteger(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:negativeInteger(mid range) $arg2 = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericIntegerDividenint2args2() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-297014075999096793\") idiv xs:negativeInteger(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:negativeInteger(upper bound) $arg2 = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericIntegerDividenint2args3() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-1\") idiv xs:negativeInteger(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:negativeInteger(lower bound) $arg2 = xs:negativeInteger(mid range) .
   */
  @org.junit.Test
  public void opNumericIntegerDividenint2args4() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-999999999999999999\") idiv xs:negativeInteger(\"-297014075999096793\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:negativeInteger(lower bound) $arg2 = xs:negativeInteger(upper bound) .
   */
  @org.junit.Test
  public void opNumericIntegerDividenint2args5() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-999999999999999999\") idiv xs:negativeInteger(\"-1\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:nonNegativeInteger(lower bound) $arg2 = xs:nonNegativeInteger(mid range) .
   */
  @org.junit.Test
  public void opNumericIntegerDividenni2args1() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"0\") idiv xs:nonNegativeInteger(\"303884545991464527\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:nonNegativeInteger(lower bound) $arg2 = xs:nonNegativeInteger(upper bound) .
   */
  @org.junit.Test
  public void opNumericIntegerDividenni2args2() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"0\") idiv xs:nonNegativeInteger(\"999999999999999999\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:nonPositiveInteger(lower bound) $arg2 = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericIntegerDividenpi2args1() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"-999999999999999999\") idiv xs:nonPositiveInteger(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:nonPositiveInteger(mid range) $arg2 = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericIntegerDividenpi2args2() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"-475688437271870490\") idiv xs:nonPositiveInteger(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:nonPositiveInteger(upper bound) $arg2 = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericIntegerDividenpi2args3() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"0\") idiv xs:nonPositiveInteger(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:nonPositiveInteger(lower bound) $arg2 = xs:nonPositiveInteger(mid range) .
   */
  @org.junit.Test
  public void opNumericIntegerDividenpi2args4() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"-999999999999999999\") idiv xs:nonPositiveInteger(\"-475688437271870490\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:positiveInteger(lower bound) $arg2 = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericIntegerDividepint2args1() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"1\") idiv xs:positiveInteger(\"1\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:positiveInteger(mid range) $arg2 = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericIntegerDividepint2args2() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"52704602390610033\") idiv xs:positiveInteger(\"1\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:positiveInteger(upper bound) $arg2 = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericIntegerDividepint2args3() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"999999999999999999\") idiv xs:positiveInteger(\"1\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:positiveInteger(lower bound) $arg2 = xs:positiveInteger(mid range) .
   */
  @org.junit.Test
  public void opNumericIntegerDividepint2args4() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"1\") idiv xs:positiveInteger(\"52704602390610033\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:positiveInteger(lower bound) $arg2 = xs:positiveInteger(upper bound) .
   */
  @org.junit.Test
  public void opNumericIntegerDividepint2args5() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"1\") idiv xs:positiveInteger(\"999999999999999999\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:short(lower bound) $arg2 = xs:short(lower bound) .
   */
  @org.junit.Test
  public void opNumericIntegerDividesht2args1() {
    final XQuery query = new XQuery(
      "xs:short(\"-32768\") idiv xs:short(\"-32768\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:short(mid range) $arg2 = xs:short(lower bound) .
   */
  @org.junit.Test
  public void opNumericIntegerDividesht2args2() {
    final XQuery query = new XQuery(
      "xs:short(\"-5324\") idiv xs:short(\"-32768\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:short(upper bound) $arg2 = xs:short(lower bound) .
   */
  @org.junit.Test
  public void opNumericIntegerDividesht2args3() {
    final XQuery query = new XQuery(
      "xs:short(\"32767\") idiv xs:short(\"-32768\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:short(lower bound) $arg2 = xs:short(mid range) .
   */
  @org.junit.Test
  public void opNumericIntegerDividesht2args4() {
    final XQuery query = new XQuery(
      "xs:short(\"-32768\") idiv xs:short(\"-5324\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("6")
    );
  }

  /**
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:short(lower bound) $arg2 = xs:short(upper bound) .
   */
  @org.junit.Test
  public void opNumericIntegerDividesht2args5() {
    final XQuery query = new XQuery(
      "xs:short(\"-32768\") idiv xs:short(\"32767\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:unsignedLong(lower bound) $arg2 = xs:unsignedLong(mid range) .
   */
  @org.junit.Test
  public void opNumericIntegerDivideulng2args1() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"0\") idiv xs:unsignedLong(\"130747108607674654\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:unsignedLong(lower bound) $arg2 = xs:unsignedLong(upper bound) .
   */
  @org.junit.Test
  public void opNumericIntegerDivideulng2args2() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"0\") idiv xs:unsignedLong(\"184467440737095516\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:unsignedShort(lower bound) $arg2 = xs:unsignedShort(mid range) .
   */
  @org.junit.Test
  public void opNumericIntegerDivideusht2args1() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"0\") idiv xs:unsignedShort(\"44633\")",
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
   *  Evaluates The "op:numeric-integer-divide" operator with the arguments set as follows: $arg1 = xs:unsignedShort(lower bound) $arg2 = xs:unsignedShort(upper bound) .
   */
  @org.junit.Test
  public void opNumericIntegerDivideusht2args2() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"0\") idiv xs:unsignedShort(\"65535\")",
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
