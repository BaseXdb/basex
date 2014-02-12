package org.basex.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the numeric-mod() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpNumericMod extends QT3TestSet {

  /**
   *  A test whose essence is: `(xs:decimal(6) mod xs:integer(2)) instance of xs:decimal`. .
   */
  @org.junit.Test
  public void kNumericMod1() {
    final XQuery query = new XQuery(
      "(xs:decimal(6) mod xs:integer(2)) instance of xs:decimal",
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
   *  A test whose essence is: `(xs:decimal(6) mod xs:double(2)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNumericMod10() {
    final XQuery query = new XQuery(
      "(xs:decimal(6) mod xs:double(2)) instance of xs:double",
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
   *  A test whose essence is: `(xs:double(6) mod xs:decimal(2)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNumericMod11() {
    final XQuery query = new XQuery(
      "(xs:double(6) mod xs:decimal(2)) instance of xs:double",
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
   *  A test whose essence is: `(xs:double(6) mod xs:float(2)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNumericMod12() {
    final XQuery query = new XQuery(
      "(xs:double(6) mod xs:float(2)) instance of xs:double",
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
   *  A test whose essence is: `(xs:float(6) mod xs:double(2)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNumericMod13() {
    final XQuery query = new XQuery(
      "(xs:float(6) mod xs:double(2)) instance of xs:double",
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
   *  A test whose essence is: `(xs:double(6) mod xs:integer(2)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNumericMod14() {
    final XQuery query = new XQuery(
      "(xs:double(6) mod xs:integer(2)) instance of xs:double",
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
   *  A test whose essence is: `(xs:integer(6) mod xs:double(2)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNumericMod15() {
    final XQuery query = new XQuery(
      "(xs:integer(6) mod xs:double(2)) instance of xs:double",
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
   *  A test whose essence is: `(xs:double(6) mod xs:double(2)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNumericMod16() {
    final XQuery query = new XQuery(
      "(xs:double(6) mod xs:double(2)) instance of xs:double",
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
   *  A test whose essence is: `10 mod 3 eq 1`. .
   */
  @org.junit.Test
  public void kNumericMod17() {
    final XQuery query = new XQuery(
      "10 mod 3 eq 1",
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
   *  A test whose essence is: `6 mod -2 eq 0`. .
   */
  @org.junit.Test
  public void kNumericMod18() {
    final XQuery query = new XQuery(
      "6 mod -2 eq 0",
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
   *  A test whose essence is: `4.5 mod 1.2 eq 0.9`. .
   */
  @org.junit.Test
  public void kNumericMod19() {
    final XQuery query = new XQuery(
      "4.5 mod 1.2 eq 0.9",
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
   *  A test whose essence is: `(xs:integer(6) mod xs:decimal(2)) instance of xs:decimal`. .
   */
  @org.junit.Test
  public void kNumericMod2() {
    final XQuery query = new XQuery(
      "(xs:integer(6) mod xs:decimal(2)) instance of xs:decimal",
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
   *  A test whose essence is: `1.23E2 mod 0.6E1 eq 3.0E0`. .
   */
  @org.junit.Test
  public void kNumericMod20() {
    final XQuery query = new XQuery(
      "1.23E2 mod 0.6E1 eq 3.0E0",
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
   *  Invalid whitespace for the 'mod' operator. .
   */
  @org.junit.Test
  public void kNumericMod21() {
    final XQuery query = new XQuery(
      "10 mod3",
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
   *  Invalid whitespace for the 'mod' operator. .
   */
  @org.junit.Test
  public void kNumericMod22() {
    final XQuery query = new XQuery(
      "10mod 3",
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
   *  Invalid whitespace for the 'mod' operator. .
   */
  @org.junit.Test
  public void kNumericMod23() {
    final XQuery query = new XQuery(
      "10mod3",
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
   *  Invoke the 'mod operator with invalid operands. .
   */
  @org.junit.Test
  public void kNumericMod24() {
    final XQuery query = new XQuery(
      "\"3\" mod \"3\"",
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
   *  Invoke the 'mod operator with invalid operands. .
   */
  @org.junit.Test
  public void kNumericMod25() {
    final XQuery query = new XQuery(
      "\"3\" mod 1.1",
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
   *  A test whose essence is: `(xs:untypedAtomic("5") mod xs:double(3)) eq 2`. .
   */
  @org.junit.Test
  public void kNumericMod26() {
    final XQuery query = new XQuery(
      "(xs:untypedAtomic(\"5\") mod xs:double(3)) eq 2",
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
   *  A test whose essence is: `(xs:decimal(5) mod xs:untypedAtomic("3")) eq 2`. .
   */
  @org.junit.Test
  public void kNumericMod27() {
    final XQuery query = new XQuery(
      "(xs:decimal(5) mod xs:untypedAtomic(\"3\")) eq 2",
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
  public void kNumericMod28() {
    final XQuery query = new XQuery(
      "empty(() mod ())",
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
  public void kNumericMod29() {
    final XQuery query = new XQuery(
      "empty(xs:float(3) mod ())",
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
   *  A test whose essence is: `(xs:integer(6) mod xs:integer(2)) instance of xs:integer`. .
   */
  @org.junit.Test
  public void kNumericMod3() {
    final XQuery query = new XQuery(
      "(xs:integer(6) mod xs:integer(2)) instance of xs:integer",
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
   *  A test whose essence is: `(xs:decimal(6) mod xs:decimal(2)) instance of xs:decimal`. .
   */
  @org.junit.Test
  public void kNumericMod4() {
    final XQuery query = new XQuery(
      "(xs:decimal(6) mod xs:decimal(2)) instance of xs:decimal",
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
   *  A test whose essence is: `(xs:decimal(6) mod xs:float(2)) instance of xs:float`. .
   */
  @org.junit.Test
  public void kNumericMod5() {
    final XQuery query = new XQuery(
      "(xs:decimal(6) mod xs:float(2)) instance of xs:float",
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
   *  A test whose essence is: `(xs:float(6) mod xs:decimal(2)) instance of xs:float`. .
   */
  @org.junit.Test
  public void kNumericMod6() {
    final XQuery query = new XQuery(
      "(xs:float(6) mod xs:decimal(2)) instance of xs:float",
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
   *  A test whose essence is: `(xs:float(6) mod xs:integer(2)) instance of xs:float`. .
   */
  @org.junit.Test
  public void kNumericMod7() {
    final XQuery query = new XQuery(
      "(xs:float(6) mod xs:integer(2)) instance of xs:float",
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
   *  A test whose essence is: `(xs:integer(6) mod xs:float(2)) instance of xs:float`. .
   */
  @org.junit.Test
  public void kNumericMod8() {
    final XQuery query = new XQuery(
      "(xs:integer(6) mod xs:float(2)) instance of xs:float",
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
   *  A test whose essence is: `(xs:float(6) mod xs:float(2)) instance of xs:float`. .
   */
  @org.junit.Test
  public void kNumericMod9() {
    final XQuery query = new XQuery(
      "(xs:float(6) mod xs:float(2)) instance of xs:float",
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
   *  Divisor is small negative xs:integer. .
   */
  @org.junit.Test
  public void k2NumericMod1() {
    final XQuery query = new XQuery(
      "-1 mod -1",
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
   *  Right op is xs:float/NaN. .
   */
  @org.junit.Test
  public void k2NumericMod10() {
    final XQuery query = new XQuery(
      "3 mod xs:float(\"NaN\")",
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
   *  Both ops are xs:float/NaN. .
   */
  @org.junit.Test
  public void k2NumericMod11() {
    final XQuery query = new XQuery(
      "xs:float(\"NaN\") mod xs:float(\"NaN\")",
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
   *  Left op is xs:double/NaN. .
   */
  @org.junit.Test
  public void k2NumericMod12() {
    final XQuery query = new XQuery(
      "xs:double(\"NaN\") mod 3",
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
   *  Right op is xs:double/NaN. .
   */
  @org.junit.Test
  public void k2NumericMod13() {
    final XQuery query = new XQuery(
      "3 mod xs:double(\"NaN\")",
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
   *  Both ops are xs:double/NaN. .
   */
  @org.junit.Test
  public void k2NumericMod14() {
    final XQuery query = new XQuery(
      "xs:double(\"NaN\") mod xs:double(\"NaN\")",
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
   *  Left op is xs:double/positive INF. .
   */
  @org.junit.Test
  public void k2NumericMod15() {
    final XQuery query = new XQuery(
      "xs:double(\"INF\") mod 3",
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
   *  Right op is xs:double/positive INF. .
   */
  @org.junit.Test
  public void k2NumericMod16() {
    final XQuery query = new XQuery(
      "3 mod xs:double(\"INF\")",
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
   *  Both ops are xs:double/positive INF. .
   */
  @org.junit.Test
  public void k2NumericMod17() {
    final XQuery query = new XQuery(
      "xs:double(\"INF\") mod xs:double(\"INF\")",
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
   *  Left op is xs:double/-INF. .
   */
  @org.junit.Test
  public void k2NumericMod18() {
    final XQuery query = new XQuery(
      "xs:double(\"-INF\") mod 3",
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
   *  Right op is xs:double/-INF. .
   */
  @org.junit.Test
  public void k2NumericMod19() {
    final XQuery query = new XQuery(
      "3 mod xs:double(\"-INF\")",
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
   *  Divisor is small negative xs:decimal. .
   */
  @org.junit.Test
  public void k2NumericMod2() {
    final XQuery query = new XQuery(
      "-1.0 mod -1.0",
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
   *  Both ops are xs:double/-INF. .
   */
  @org.junit.Test
  public void k2NumericMod20() {
    final XQuery query = new XQuery(
      "xs:double(\"-INF\") mod xs:double(\"-INF\")",
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
   *  Left op is xs:double/positive 0. .
   */
  @org.junit.Test
  public void k2NumericMod21() {
    final XQuery query = new XQuery(
      "xs:double(\"0\") mod 3",
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
   *  Right op is xs:double/positive 0. .
   */
  @org.junit.Test
  public void k2NumericMod22() {
    final XQuery query = new XQuery(
      "3 mod xs:double(\"0\")",
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
   *  Both ops are xs:double/positive 0. .
   */
  @org.junit.Test
  public void k2NumericMod23() {
    final XQuery query = new XQuery(
      "xs:double(\"0\") mod xs:double(\"INF\")",
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
   *  Left op is xs:double/-0. .
   */
  @org.junit.Test
  public void k2NumericMod24() {
    final XQuery query = new XQuery(
      "xs:double(\"-0\") mod 3",
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
   *  Right op is xs:double/-0. .
   */
  @org.junit.Test
  public void k2NumericMod25() {
    final XQuery query = new XQuery(
      "3 mod xs:double(\"-0\")",
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
   *  Both ops are xs:double/-0. .
   */
  @org.junit.Test
  public void k2NumericMod26() {
    final XQuery query = new XQuery(
      "xs:double(\"-0\") mod xs:double(\"-0\")",
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
   *  Left op is xs:float/positive INF. .
   */
  @org.junit.Test
  public void k2NumericMod27() {
    final XQuery query = new XQuery(
      "xs:float(\"INF\") mod 3",
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
   *  Right op is xs:float/positive INF. .
   */
  @org.junit.Test
  public void k2NumericMod28() {
    final XQuery query = new XQuery(
      "3 mod xs:float(\"INF\")",
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
   *  Both ops are xs:float/positive INF. .
   */
  @org.junit.Test
  public void k2NumericMod29() {
    final XQuery query = new XQuery(
      "xs:float(\"INF\") mod xs:float(\"INF\")",
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
   *  Divisor is small negative xs:double. .
   */
  @org.junit.Test
  public void k2NumericMod3() {
    final XQuery query = new XQuery(
      "-1.0e0 mod -1.0e0",
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
   *  Left op is xs:float/-INF. .
   */
  @org.junit.Test
  public void k2NumericMod30() {
    final XQuery query = new XQuery(
      "xs:float(\"-INF\") mod 3",
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
   *  Right op is xs:float/-INF. .
   */
  @org.junit.Test
  public void k2NumericMod31() {
    final XQuery query = new XQuery(
      "3 mod xs:float(\"-INF\")",
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
   *  Both ops are xs:float/-INF. .
   */
  @org.junit.Test
  public void k2NumericMod32() {
    final XQuery query = new XQuery(
      "xs:float(\"-INF\") mod xs:float(\"-INF\")",
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
   *  Left op is xs:float/positive 0. .
   */
  @org.junit.Test
  public void k2NumericMod33() {
    final XQuery query = new XQuery(
      "xs:float(\"0\") mod 3",
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
   *  Right op is xs:float/positive 0. .
   */
  @org.junit.Test
  public void k2NumericMod34() {
    final XQuery query = new XQuery(
      "3 mod xs:float(\"0\")",
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
   *  Both ops are xs:float/positive 0. .
   */
  @org.junit.Test
  public void k2NumericMod35() {
    final XQuery query = new XQuery(
      "xs:float(\"0\") mod xs:float(\"INF\")",
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
   *  Left op is xs:float/-0. .
   */
  @org.junit.Test
  public void k2NumericMod36() {
    final XQuery query = new XQuery(
      "xs:float(\"-0\") mod 3",
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
   *  Right op is xs:float/-0. .
   */
  @org.junit.Test
  public void k2NumericMod37() {
    final XQuery query = new XQuery(
      "3 mod xs:float(\"-0\")",
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
   *  Both ops are xs:float/-0. .
   */
  @org.junit.Test
  public void k2NumericMod38() {
    final XQuery query = new XQuery(
      "xs:float(\"-0\") mod xs:float(\"-0\")",
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
   *  Left op is xs:float/-0. .
   */
  @org.junit.Test
  public void k2NumericMod39() {
    final XQuery query = new XQuery(
      "xs:float(\"-0\") mod xs:float(\"4\")",
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
   *  Divisor is small negative xs:float. .
   */
  @org.junit.Test
  public void k2NumericMod4() {
    final XQuery query = new XQuery(
      "-1.0e0 mod xs:float(-1.0e0)",
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
   *  Left op is xs:float/0. .
   */
  @org.junit.Test
  public void k2NumericMod40() {
    final XQuery query = new XQuery(
      "xs:float(\"0\") mod xs:float(\"4\")",
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
   *  Left op is xs:double/-0. .
   */
  @org.junit.Test
  public void k2NumericMod41() {
    final XQuery query = new XQuery(
      "xs:double(\"-0\") mod xs:double(\"4\")",
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
   *  Left op is xs:double/0. .
   */
  @org.junit.Test
  public void k2NumericMod42() {
    final XQuery query = new XQuery(
      "xs:double(\"0\") mod xs:double(\"4\")",
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
   *  Left op is xs:float/-0 with INF as divisor. .
   */
  @org.junit.Test
  public void k2NumericMod43() {
    final XQuery query = new XQuery(
      "xs:float(\"-0\") mod xs:float(\"INF\")",
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
   *  Left op is xs:float/0 with INF as divisor. .
   */
  @org.junit.Test
  public void k2NumericMod44() {
    final XQuery query = new XQuery(
      "xs:float(\"0\") mod xs:float(\"INF\")",
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
   *  Left op is xs:double/-0 with INF as divisor. .
   */
  @org.junit.Test
  public void k2NumericMod45() {
    final XQuery query = new XQuery(
      "xs:double(\"-0\") mod xs:double(\"INF\")",
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
   *  Left op is xs:double/0 with INF as divisor. .
   */
  @org.junit.Test
  public void k2NumericMod46() {
    final XQuery query = new XQuery(
      "xs:double(\"0\") mod xs:double(\"INF\")",
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
   *  Left op is xs:float/-0 with -INF as divisor. .
   */
  @org.junit.Test
  public void k2NumericMod47() {
    final XQuery query = new XQuery(
      "xs:float(\"-0\") mod xs:float(\"-INF\")",
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
   *  Left op is xs:float/0 with -INF as divisor. .
   */
  @org.junit.Test
  public void k2NumericMod48() {
    final XQuery query = new XQuery(
      "xs:float(\"0\") mod xs:float(\"-INF\")",
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
   *  Left op is xs:double/-0 with -INF as divisor. .
   */
  @org.junit.Test
  public void k2NumericMod49() {
    final XQuery query = new XQuery(
      "xs:double(\"-0\") mod xs:double(\"-INF\")",
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
   *  Divisor is large negative xs:integer. .
   */
  @org.junit.Test
  public void k2NumericMod5() {
    final XQuery query = new XQuery(
      "-1 mod -9223372036854775808",
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
        assertEq("-1")
      ||
        error("FOAR0002")
      )
    );
  }

  /**
   *  Left op is xs:double/0 with -INF as divisor. .
   */
  @org.junit.Test
  public void k2NumericMod50() {
    final XQuery query = new XQuery(
      "xs:double(\"0\") mod xs:double(\"-INF\")",
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
   *  Trival mod with xs:double. .
   */
  @org.junit.Test
  public void k2NumericMod51() {
    final XQuery query = new XQuery(
      "xs:double(\"10000000\") mod xs:double(\"10000000\")",
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
   *  Trival mod with xs:double(#2). .
   */
  @org.junit.Test
  public void k2NumericMod52() {
    final XQuery query = new XQuery(
      "xs:double(\"-10000000\") mod xs:double(\"-10000000\")",
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
   *  Trival mod with xs:double(#3). .
   */
  @org.junit.Test
  public void k2NumericMod53() {
    final XQuery query = new XQuery(
      "xs:double(\"10000000\") mod xs:double(\"-10000000\")",
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
   *  Trival mod with xs:double(#4). .
   */
  @org.junit.Test
  public void k2NumericMod54() {
    final XQuery query = new XQuery(
      "xs:double(\"-10000000\") mod xs:double(\"10000000\")",
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
   *  Trival mod with xs:float. .
   */
  @org.junit.Test
  public void k2NumericMod55() {
    final XQuery query = new XQuery(
      "xs:float(\"10000000\") mod xs:float(\"10000000\")",
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
   *  Trival mod with xs:float(#2). .
   */
  @org.junit.Test
  public void k2NumericMod56() {
    final XQuery query = new XQuery(
      "xs:float(\"-10000000\") mod xs:float(\"-10000000\")",
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
   *  Trival mod with xs:float(#3). .
   */
  @org.junit.Test
  public void k2NumericMod57() {
    final XQuery query = new XQuery(
      "xs:float(\"10000000\") mod xs:float(\"-10000000\")",
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
   *  Trival mod with xs:float(#4). .
   */
  @org.junit.Test
  public void k2NumericMod58() {
    final XQuery query = new XQuery(
      "xs:float(\"-10000000\") mod xs:float(\"10000000\")",
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
   *  Divide xs:integer by 0. .
   */
  @org.junit.Test
  public void k2NumericMod59() {
    final XQuery query = new XQuery(
      "3 mod 0",
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
   *  Divisor is large negative xs:decimal. .
   */
  @org.junit.Test
  public void k2NumericMod6() {
    final XQuery query = new XQuery(
      "-1.0 mod -9223372036854775808.0",
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
   *  Divide an xs:decimal by 0. .
   */
  @org.junit.Test
  public void k2NumericMod60() {
    final XQuery query = new XQuery(
      "3.0 mod 0",
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
   *  Divide xs:integer by 0.0. .
   */
  @org.junit.Test
  public void k2NumericMod61() {
    final XQuery query = new XQuery(
      "3 mod 0.0",
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
   *  Ensure the 'div' keyword is parsed correctly. .
   */
  @org.junit.Test
  public void k2NumericMod62() {
    final XQuery query = new XQuery(
      "empty(<e/>/(mod mod mod))",
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
   *  Divisor is large negative xs:double. .
   */
  @org.junit.Test
  public void k2NumericMod7() {
    final XQuery query = new XQuery(
      "-1.0e0 mod xs:double(-9223372036854775808)",
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
        assertEq("-1")
      ||
        error("FOAR0002")
      )
    );
  }

  /**
   *  Divisor is large negative xs:float. .
   */
  @org.junit.Test
  public void k2NumericMod8() {
    final XQuery query = new XQuery(
      "xs:float(-1.0e0) mod xs:float(-9223372036854775808)",
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
        assertEq("-1")
      ||
        error("FOAR0002")
      )
    );
  }

  /**
   *  Left op is xs:float/NaN. .
   */
  @org.junit.Test
  public void k2NumericMod9() {
    final XQuery query = new XQuery(
      "xs:float(\"NaN\") mod 3",
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
   *  ensure that taking the modulo xs:integers returns an xs:integer .
   */
  @org.junit.Test
  public void cbclNumericMod001() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:integer($x as xs:decimal) { if ($x mod 1 eq 0) then xs:integer($x) else $x }; \n" +
      "      \t(local:integer(3) mod local:integer(2)) instance of xs:integer",
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
   *  ensure that taking the modulo xs:integers returns an xs:integer .
   */
  @org.junit.Test
  public void cbclNumericMod002() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:integer($x as xs:decimal) { if ($x mod 1 eq 0) then xs:integer($x) else $x }; \n" +
      "      \t(local:integer(3.5) mod local:integer(2)) instance of xs:integer",
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
   *  Test numeric mod with a potential type check error .
   */
  @org.junit.Test
  public void cbclNumericMod003() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:value($number as xs:boolean) { if ($number) then 1 else xs:string('1') }; \n" +
      "      \tlocal:value(true()) mod local:value(true())",
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
        assertStringValue(false, "0")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  Check dynamic type of numeric mod on arguments of union of numeric types and untypedAtomic. .
   */
  @org.junit.Test
  public void opNumericMod1() {
    final XQuery query = new XQuery(
      "for $x in (1, xs:decimal(2), xs:float(3), xs:double(4), xs:untypedAtomic(5)) for $y in (1, xs:decimal(2), xs:float(3), xs:double(4), xs:untypedAtomic(5)) return typeswitch ($x mod $y) case xs:integer return \"integer\" case xs:decimal return \"decimal\" case xs:float return \"float\" case xs:double return \"double\" default return error()",
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
   *  Evaluates The "op:numeric-mod" operator with the arguments set as follows: $arg1 = xs:double(lower bound) $arg2 = xs:double(upper bound) .
   */
  @org.junit.Test
  public void opNumericModdbl2args1() {
    final XQuery query = new XQuery(
      "xs:double(\"-1.7976931348623157E308\") mod xs:double(\"1.7976931348623157E308\")",
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
   *  Evaluates The "op:numeric-mod" operator with the arguments set as follows: $arg1 = xs:decimal(lower bound) $arg2 = xs:decimal(mid range) .
   */
  @org.junit.Test
  public void opNumericModdec2args1() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-999999999999999999\") mod xs:decimal(\"617375191608514839\")",
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
   *  Evaluates The "op:numeric-mod" operator with the arguments set as follows: $arg1 = xs:decimal(lower bound) $arg2 = xs:decimal(upper bound) .
   */
  @org.junit.Test
  public void opNumericModdec2args2() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-999999999999999999\") mod xs:decimal(\"999999999999999999\")",
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
   *  Evaluates The "op:numeric-mod" operator with the arguments set as follows: $arg1 = xs:float(lower bound) $arg2 = xs:float(upper bound) .
   */
  @org.junit.Test
  public void opNumericModflt2args1() {
    final XQuery query = new XQuery(
      "xs:float(\"-3.4028235E38\") mod xs:float(\"3.4028235E38\")",
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
   *  Evaluates The "op:numeric-mod" operator with the arguments set as follows: $arg1 = xs:int(lower bound) $arg2 = xs:int(upper bound) .
   */
  @org.junit.Test
  public void opNumericModint2args1() {
    final XQuery query = new XQuery(
      "xs:int(\"-2147483648\") mod xs:int(\"2147483647\")",
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
   *  Evaluates The "op:numeric-mod" operator with the arguments set as follows: $arg1 = xs:integer(lower bound) $arg2 = xs:integer(mid range) .
   */
  @org.junit.Test
  public void opNumericModintg2args1() {
    final XQuery query = new XQuery(
      "xs:integer(\"-999999999999999999\") mod xs:integer(\"830993497117024304\")",
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
   *  Evaluates The "op:numeric-mod" operator with the arguments set as follows: $arg1 = xs:integer(lower bound) $arg2 = xs:integer(upper bound) .
   */
  @org.junit.Test
  public void opNumericModintg2args2() {
    final XQuery query = new XQuery(
      "xs:integer(\"-999999999999999999\") mod xs:integer(\"999999999999999999\")",
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
   *  Evaluates The "op:numeric-mod" operator with the arguments set as follows: $arg1 = xs:long(lower bound) $arg2 = xs:long(upper bound) .
   */
  @org.junit.Test
  public void opNumericModlng2args1() {
    final XQuery query = new XQuery(
      "xs:long(\"-92233720368547758\") mod xs:long(\"92233720368547758\")",
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
   *  Simple mod test with () as one operand should return null .
   */
  @org.junit.Test
  public void opNumericModmix2args1() {
    final XQuery query = new XQuery(
      "() mod 1",
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
   *  Simple mod test pass string for second operator .
   */
  @org.junit.Test
  public void opNumericModmix2args2() {
    final XQuery query = new XQuery(
      "1 mod '1'",
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
   *  Simple mod test, second operator cast string to integer .
   */
  @org.junit.Test
  public void opNumericModmix2args3() {
    final XQuery query = new XQuery(
      "1 mod xs:integer('1')",
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
   *  Simple mod test, second operator is a node, atomization applied .
   */
  @org.junit.Test
  public void opNumericModmix2args4() {
    final XQuery query = new XQuery(
      "1 mod <a> 2 </a>",
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
   *  Simple mod test, second operator is a node, atomization applied .
   */
  @org.junit.Test
  public void opNumericModmix2args5() {
    final XQuery query = new XQuery(
      "1 mod <a> <b> 2 </b> </a>",
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
   *  Simple mod test, second operator node which is not atomizable .
   */
  @org.junit.Test
  public void opNumericModmix2args6() {
    final XQuery query = new XQuery(
      "1 mod <a> <b> 2</b> <c> 2</c> </a>",
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
   *  Simple mod test, two operands are nodes .
   */
  @org.junit.Test
  public void opNumericModmix2args7() {
    final XQuery query = new XQuery(
      "<a> 1 </a> mod <b> 2 </b>",
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
   *  Simple mod test, second operator is a node, atomizable but not castable to integer .
   */
  @org.junit.Test
  public void opNumericModmix2args8() {
    final XQuery query = new XQuery(
      "1 mod <a> x </a>",
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
   *  Simple mod test pass an empty node for second operator .
   */
  @org.junit.Test
  public void opNumericModmix2args9() {
    final XQuery query = new XQuery(
      "1 mod <a/>",
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
   *  Evaluates The "op:numeric-mod" operator with the arguments set as follows: $arg1 = xs:nonNegativeInteger(lower bound) $arg2 = xs:nonNegativeInteger(mid range) .
   */
  @org.junit.Test
  public void opNumericModnni2args1() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"0\") mod xs:nonNegativeInteger(\"303884545991464527\")",
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
   *  Evaluates The "op:numeric-mod" operator with the arguments set as follows: $arg1 = xs:nonNegativeInteger(lower bound) $arg2 = xs:nonNegativeInteger(upper bound) .
   */
  @org.junit.Test
  public void opNumericModnni2args2() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"0\") mod xs:nonNegativeInteger(\"999999999999999999\")",
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
   *  Evaluates The "op:numeric-mod" operator with the arguments set as follows: $arg1 = xs:positiveInteger(lower bound) $arg2 = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericModpint2args1() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"1\") mod xs:positiveInteger(\"1\")",
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
   *  Evaluates The "op:numeric-mod" operator with the arguments set as follows: $arg1 = xs:positiveInteger(mid range) $arg2 = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericModpint2args2() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"52704602390610033\") mod xs:positiveInteger(\"1\")",
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
   *  Evaluates The "op:numeric-mod" operator with the arguments set as follows: $arg1 = xs:positiveInteger(upper bound) $arg2 = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericModpint2args3() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"999999999999999999\") mod xs:positiveInteger(\"1\")",
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
   *  Evaluates The "op:numeric-mod" operator with the arguments set as follows: $arg1 = xs:positiveInteger(lower bound) $arg2 = xs:positiveInteger(mid range) .
   */
  @org.junit.Test
  public void opNumericModpint2args4() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"1\") mod xs:positiveInteger(\"52704602390610033\")",
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
   *  Evaluates The "op:numeric-mod" operator with the arguments set as follows: $arg1 = xs:positiveInteger(lower bound) $arg2 = xs:positiveInteger(upper bound) .
   */
  @org.junit.Test
  public void opNumericModpint2args5() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"1\") mod xs:positiveInteger(\"999999999999999999\")",
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
   *  Evaluates The "op:numeric-mod" operator with the arguments set as follows: $arg1 = xs:short(lower bound) $arg2 = xs:short(upper bound) .
   */
  @org.junit.Test
  public void opNumericModsht2args1() {
    final XQuery query = new XQuery(
      "xs:short(\"-32768\") mod xs:short(\"32767\")",
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
   *  Evaluates The "op:numeric-mod" operator with the arguments set as follows: $arg1 = xs:unsignedLong(lower bound) $arg2 = xs:unsignedLong(mid range) .
   */
  @org.junit.Test
  public void opNumericModulng2args1() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"0\") mod xs:unsignedLong(\"130747108607674654\")",
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
   *  Evaluates The "op:numeric-mod" operator with the arguments set as follows: $arg1 = xs:unsignedLong(lower bound) $arg2 = xs:unsignedLong(upper bound) .
   */
  @org.junit.Test
  public void opNumericModulng2args2() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"0\") mod xs:unsignedLong(\"184467440737095516\")",
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
   *  Evaluates The "op:numeric-mod" operator with the arguments set as follows: $arg1 = xs:unsignedShort(lower bound) $arg2 = xs:unsignedShort(mid range) .
   */
  @org.junit.Test
  public void opNumericModusht2args1() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"0\") mod xs:unsignedShort(\"44633\")",
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
   *  Evaluates The "op:numeric-mod" operator with the arguments set as follows: $arg1 = xs:unsignedShort(lower bound) $arg2 = xs:unsignedShort(upper bound) .
   */
  @org.junit.Test
  public void opNumericModusht2args2() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"0\") mod xs:unsignedShort(\"65535\")",
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
