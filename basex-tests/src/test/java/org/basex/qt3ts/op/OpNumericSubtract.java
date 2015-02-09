package org.basex.qt3ts.op;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the numeric-subtract() function.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpNumericSubtract extends QT3TestSet {

  /**
   *  A test whose essence is: `xs:double(6) - xs:double(2) eq 4`. .
   */
  @org.junit.Test
  public void kNumericSubtract1() {
    final XQuery query = new XQuery(
      "xs:double(6) - xs:double(2) eq 4",
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
   *  A test whose essence is: `(xs:float(6) - xs:decimal(2)) instance of xs:float`. .
   */
  @org.junit.Test
  public void kNumericSubtract10() {
    final XQuery query = new XQuery(
      "(xs:float(6) - xs:decimal(2)) instance of xs:float",
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
   *  A test whose essence is: `(xs:float(6) - xs:integer(2)) instance of xs:float`. .
   */
  @org.junit.Test
  public void kNumericSubtract11() {
    final XQuery query = new XQuery(
      "(xs:float(6) - xs:integer(2)) instance of xs:float",
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
   *  A test whose essence is: `(xs:integer(6) - xs:float(2)) instance of xs:float`. .
   */
  @org.junit.Test
  public void kNumericSubtract12() {
    final XQuery query = new XQuery(
      "(xs:integer(6) - xs:float(2)) instance of xs:float",
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
   *  A test whose essence is: `(xs:float(6) - xs:float(2)) instance of xs:float`. .
   */
  @org.junit.Test
  public void kNumericSubtract13() {
    final XQuery query = new XQuery(
      "(xs:float(6) - xs:float(2)) instance of xs:float",
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
   *  A test whose essence is: `(xs:decimal(6) - xs:double(2)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNumericSubtract14() {
    final XQuery query = new XQuery(
      "(xs:decimal(6) - xs:double(2)) instance of xs:double",
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
   *  A test whose essence is: `(xs:double(6) - xs:decimal(2)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNumericSubtract15() {
    final XQuery query = new XQuery(
      "(xs:double(6) - xs:decimal(2)) instance of xs:double",
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
   *  A test whose essence is: `(xs:double(6) - xs:float(2)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNumericSubtract16() {
    final XQuery query = new XQuery(
      "(xs:double(6) - xs:float(2)) instance of xs:double",
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
   *  A test whose essence is: `(xs:float(6) - xs:double(2)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNumericSubtract17() {
    final XQuery query = new XQuery(
      "(xs:float(6) - xs:double(2)) instance of xs:double",
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
   *  A test whose essence is: `(xs:double(6) - xs:integer(2)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNumericSubtract18() {
    final XQuery query = new XQuery(
      "(xs:double(6) - xs:integer(2)) instance of xs:double",
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
   *  A test whose essence is: `(xs:integer(6) - xs:double(2)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNumericSubtract19() {
    final XQuery query = new XQuery(
      "(xs:integer(6) - xs:double(2)) instance of xs:double",
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
   *  A test whose essence is: `xs:decimal(6) - xs:decimal(2) eq 4`. .
   */
  @org.junit.Test
  public void kNumericSubtract2() {
    final XQuery query = new XQuery(
      "xs:decimal(6) - xs:decimal(2) eq 4",
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
   *  A test whose essence is: `(xs:double(6) - xs:double(2)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNumericSubtract20() {
    final XQuery query = new XQuery(
      "(xs:double(6) - xs:double(2)) instance of xs:double",
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
   *  A test whose essence is: `string(xs:float("NaN") - 3) eq "NaN"`. .
   */
  @org.junit.Test
  public void kNumericSubtract21() {
    final XQuery query = new XQuery(
      "string(xs:float(\"NaN\") - 3) eq \"NaN\"",
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
   *  A test whose essence is: `string(xs:double("NaN") - 3) eq "NaN"`. .
   */
  @org.junit.Test
  public void kNumericSubtract22() {
    final XQuery query = new XQuery(
      "string(xs:double(\"NaN\") - 3) eq \"NaN\"",
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
   *  A test whose essence is: `string(3 - xs:float("NaN")) eq "NaN"`. .
   */
  @org.junit.Test
  public void kNumericSubtract23() {
    final XQuery query = new XQuery(
      "string(3 - xs:float(\"NaN\")) eq \"NaN\"",
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
   *  A test whose essence is: `string(3 - xs:double("NaN")) eq "NaN"`. .
   */
  @org.junit.Test
  public void kNumericSubtract24() {
    final XQuery query = new XQuery(
      "string(3 - xs:double(\"NaN\")) eq \"NaN\"",
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
   *  Invoke the '-' operator with invalid operands. .
   */
  @org.junit.Test
  public void kNumericSubtract25() {
    final XQuery query = new XQuery(
      "\"3\" - \"3\"",
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
   *  Invoke the '-' operator with invalid operands. .
   */
  @org.junit.Test
  public void kNumericSubtract26() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"3\") - \"3\"",
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
   *  A test whose essence is: `(xs:untypedAtomic("3") - 3) eq 0`. .
   */
  @org.junit.Test
  public void kNumericSubtract27() {
    final XQuery query = new XQuery(
      "(xs:untypedAtomic(\"3\") - 3) eq 0",
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
   *  A test whose essence is: `(3 - xs:untypedAtomic("3")) eq 0`. .
   */
  @org.junit.Test
  public void kNumericSubtract28() {
    final XQuery query = new XQuery(
      "(3 - xs:untypedAtomic(\"3\")) eq 0",
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
  public void kNumericSubtract29() {
    final XQuery query = new XQuery(
      "empty(() - ())",
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
   *  A test whose essence is: `xs:integer(6) - xs:integer(2) eq 4`. .
   */
  @org.junit.Test
  public void kNumericSubtract3() {
    final XQuery query = new XQuery(
      "xs:integer(6) - xs:integer(2) eq 4",
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
  public void kNumericSubtract30() {
    final XQuery query = new XQuery(
      "empty(() - xs:float(3))",
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
  public void kNumericSubtract31() {
    final XQuery query = new XQuery(
      "empty(() - xs:double(3))",
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
   *  A test whose essence is: `(xs:untypedAtomic("3") - xs:untypedAtomic("3")) eq 0`. .
   */
  @org.junit.Test
  public void kNumericSubtract32() {
    final XQuery query = new XQuery(
      "(xs:untypedAtomic(\"3\") - xs:untypedAtomic(\"3\")) eq 0",
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
   *  A test whose essence is: `(xs:untypedAtomic("3") - xs:untypedAtomic("3")) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNumericSubtract33() {
    final XQuery query = new XQuery(
      "(xs:untypedAtomic(\"3\") - xs:untypedAtomic(\"3\")) instance of xs:double",
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
   *  A test whose essence is: `(xs:untypedAtomic("3") - 1.1) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNumericSubtract34() {
    final XQuery query = new XQuery(
      "(xs:untypedAtomic(\"3\") - 1.1) instance of xs:double",
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
   *  A test whose essence is: `(1.1 - xs:untypedAtomic("3")) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNumericSubtract35() {
    final XQuery query = new XQuery(
      "(1.1 - xs:untypedAtomic(\"3\")) instance of xs:double",
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
   *  Substracting zero, with complex operands. Implementations supporting the static typing feature may raise XPTY0004. .
   */
  @org.junit.Test
  public void kNumericSubtract36() {
    final XQuery query = new XQuery(
      "1 eq (remove((current-time(), 1), 1) - 0)",
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
   *  Adding zero, with complex operands. Implementations supporting the static typing feature may raise XPTY0004. .
   */
  @org.junit.Test
  public void kNumericSubtract37() {
    final XQuery query = new XQuery(
      "1 eq (remove((current-time(), 1), 1) + 0)",
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
   *  Adding zero, with complex operands. Implementations supporting the static typing feature may raise XPTY0004. .
   */
  @org.junit.Test
  public void kNumericSubtract38() {
    final XQuery query = new XQuery(
      "1 eq (0 + remove((current-time(), 1), 1))",
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
   *  A test whose essence is: `xs:float(5) - xs:float(1) eq 4`. .
   */
  @org.junit.Test
  public void kNumericSubtract4() {
    final XQuery query = new XQuery(
      "xs:float(5) - xs:float(1) eq 4",
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
   *  A test whose essence is: `(xs:decimal(6) - xs:integer(2)) instance of xs:decimal`. .
   */
  @org.junit.Test
  public void kNumericSubtract5() {
    final XQuery query = new XQuery(
      "(xs:decimal(6) - xs:integer(2)) instance of xs:decimal",
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
   *  A test whose essence is: `(xs:integer(6) - xs:decimal(2)) instance of xs:decimal`. .
   */
  @org.junit.Test
  public void kNumericSubtract6() {
    final XQuery query = new XQuery(
      "(xs:integer(6) - xs:decimal(2)) instance of xs:decimal",
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
   *  A test whose essence is: `(xs:integer(6) - xs:integer(2)) instance of xs:integer`. .
   */
  @org.junit.Test
  public void kNumericSubtract7() {
    final XQuery query = new XQuery(
      "(xs:integer(6) - xs:integer(2)) instance of xs:integer",
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
   *  A test whose essence is: `(xs:decimal(6) - xs:decimal(2)) instance of xs:decimal`. .
   */
  @org.junit.Test
  public void kNumericSubtract8() {
    final XQuery query = new XQuery(
      "(xs:decimal(6) - xs:decimal(2)) instance of xs:decimal",
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
   *  A test whose essence is: `(xs:decimal(6) - xs:float(2)) instance of xs:float`. .
   */
  @org.junit.Test
  public void kNumericSubtract9() {
    final XQuery query = new XQuery(
      "(xs:decimal(6) - xs:float(2)) instance of xs:float",
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
   *  Substract xs:double(0) from xs:integer(0). .
   */
  @org.junit.Test
  public void k2NumericSubtract1() {
    final XQuery query = new XQuery(
      "0 - xs:double(0)",
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
   *  Substract xs:double(0) from xs:integer(0). .
   */
  @org.junit.Test
  public void k2NumericSubtract2() {
    final XQuery query = new XQuery(
      "0 - xs:float(0)",
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
   *  test subtraction of large xs:decimal .
   */
  @org.junit.Test
  public void cbclNumericSubtract001() {
    final XQuery query = new XQuery(
      "-79228162514264337593543950335.0 - 1.0 lt 0.0",
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
   *  test subtraction of large xs:integer .
   */
  @org.junit.Test
  public void cbclNumericSubtract002() {
    final XQuery query = new XQuery(
      "-79228162514264337593543950335 - 1 lt 0",
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
   *  test subtraction of xs:decimal 0 .
   */
  @org.junit.Test
  public void cbclNumericSubtract003() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:decimal) as xs:decimal { $arg * $arg }; \n" +
      "      \tlocal:square(7.5) - 0.0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "56.25")
    );
  }

  /**
   *  test subtraction of xs:integer 0 .
   */
  @org.junit.Test
  public void cbclNumericSubtract004() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:integer) as xs:integer { $arg * $arg }; \n" +
      "      \tlocal:square(7) - 0",
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
   *  ensure that subtracting xs:integers returns an xs:integer .
   */
  @org.junit.Test
  public void cbclNumericSubtract005() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:integer($x as xs:decimal) { if ($x mod 1 eq 0) then xs:integer($x) else $x }; \n" +
      "      \t(local:integer(2) - local:integer(2)) instance of xs:integer",
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
   *  test subtraction of negated integers .
   */
  @org.junit.Test
  public void cbclNumericSubtract006() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:integer($x as xs:decimal) { if ($x mod 1 eq 0) then xs:integer($x) else $x }; \n" +
      "      \t-(local:integer(2)) - -(local:integer(4))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2")
    );
  }

  /**
   *  test subtraction of negated integers .
   */
  @org.junit.Test
  public void cbclNumericSubtract007() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:integer($x as xs:decimal) { if ($x mod 1 eq 0) then xs:integer($x) else $x }; \n" +
      "      \tlocal:integer(2) - -(local:integer(4))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "6")
    );
  }

  /**
   *  test subtraction of negated integers .
   */
  @org.junit.Test
  public void cbclNumericSubtract008() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:integer($x as xs:decimal) { if ($x mod 1 eq 0) then xs:integer($x) else $x }; \n" +
      "      \t-(local:integer(2)) - local:integer(4)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-6")
    );
  }

  /**
   *  Check dynamic type of numeric subtract on arguments of union of numeric types and untypedAtomic. .
   */
  @org.junit.Test
  public void opNumericSubtract1() {
    final XQuery query = new XQuery(
      "\n" +
      "        for $x in (1, xs:decimal(2), xs:float(3), xs:double(4), xs:untypedAtomic(5)) \n" +
      "        for $y in (1, xs:decimal(2), xs:float(3), xs:double(4), xs:untypedAtomic(5)) \n" +
      "        return typeswitch ($x - $y) \n" +
      "        case xs:integer return \"integer\" \n" +
      "        case xs:decimal return \"decimal\" \n" +
      "        case xs:float return \"float\" \n" +
      "        case xs:double return \"double\" \n" +
      "        default return error()",
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
   *  test subtraction of large xs:decimal .
   */
  @org.junit.Test
  public void opNumericSubtractBig01() {
    final XQuery query = new XQuery(
      "(-79228162514264337593543950335.0 - 1.0) cast as xs:string",
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
        assertStringValue(false, "-79228162514264337593543950336")
      ||
        error("FOAR0002")
      )
    );
  }

  /**
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:double(lower bound) $arg2 = xs:double(lower bound) .
   */
  @org.junit.Test
  public void opNumericSubtractdbl2args1() {
    final XQuery query = new XQuery(
      "xs:double(\"-1.7976931348623157E308\") - xs:double(\"-1.7976931348623157E308\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:double(mid range) $arg2 = xs:double(lower bound) .
   */
  @org.junit.Test
  public void opNumericSubtractdbl2args2() {
    final XQuery query = new XQuery(
      "xs:double(\"0\") - xs:double(\"-1.7976931348623157E308\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:double(upper bound) $arg2 = xs:double(lower bound) .
   */
  @org.junit.Test
  public void opNumericSubtractdbl2args3() {
    final XQuery query = new XQuery(
      "xs:double(\"0\") - xs:double(\"-1.7976931348623157E308\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:double(lower bound) $arg2 = xs:double(mid range) .
   */
  @org.junit.Test
  public void opNumericSubtractdbl2args4() {
    final XQuery query = new XQuery(
      "xs:double(\"-1.7976931348623157E308\") - xs:double(\"0\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:double(lower bound) $arg2 = xs:double(upper bound) .
   */
  @org.junit.Test
  public void opNumericSubtractdbl2args5() {
    final XQuery query = new XQuery(
      "xs:double(\"0\") - xs:double(\"1.7976931348623157E308\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:decimal(lower bound) $arg2 = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void opNumericSubtractdec2args1() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-999999999999999999\") - xs:decimal(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:decimal(mid range) $arg2 = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void opNumericSubtractdec2args2() {
    final XQuery query = new XQuery(
      "xs:decimal(\"0\") - xs:decimal(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:decimal(upper bound) $arg2 = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void opNumericSubtractdec2args3() {
    final XQuery query = new XQuery(
      "xs:decimal(\"0\") - xs:decimal(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:decimal(lower bound) $arg2 = xs:decimal(mid range) .
   */
  @org.junit.Test
  public void opNumericSubtractdec2args4() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-382624808391485160\") - xs:decimal(\"617375191608514839\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:decimal(lower bound) $arg2 = xs:decimal(upper bound) .
   */
  @org.junit.Test
  public void opNumericSubtractdec2args5() {
    final XQuery query = new XQuery(
      "xs:decimal(\"0\") - xs:decimal(\"999999999999999999\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:float(lower bound) $arg2 = xs:float(lower bound) .
   */
  @org.junit.Test
  public void opNumericSubtractflt2args1() {
    final XQuery query = new XQuery(
      "xs:float(\"-3.4028235E38\") - xs:float(\"-3.4028235E38\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:float(mid range) $arg2 = xs:float(lower bound) .
   */
  @org.junit.Test
  public void opNumericSubtractflt2args2() {
    final XQuery query = new XQuery(
      "xs:float(\"0\") - xs:float(\"-3.4028235E38\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:float(upper bound) $arg2 = xs:float(lower bound) .
   */
  @org.junit.Test
  public void opNumericSubtractflt2args3() {
    final XQuery query = new XQuery(
      "xs:float(\"0\") - xs:float(\"-3.4028235E38\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:float(lower bound) $arg2 = xs:float(mid range) .
   */
  @org.junit.Test
  public void opNumericSubtractflt2args4() {
    final XQuery query = new XQuery(
      "xs:float(\"-3.4028235E38\") - xs:float(\"0\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:float(lower bound) $arg2 = xs:float(upper bound) .
   */
  @org.junit.Test
  public void opNumericSubtractflt2args5() {
    final XQuery query = new XQuery(
      "xs:float(\"0\") - xs:float(\"3.4028235E38\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:int(lower bound) $arg2 = xs:int(lower bound) .
   */
  @org.junit.Test
  public void opNumericSubtractint2args1() {
    final XQuery query = new XQuery(
      "xs:int(\"-2147483648\") - xs:int(\"-2147483648\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:int(mid range) $arg2 = xs:int(lower bound) .
   */
  @org.junit.Test
  public void opNumericSubtractint2args2() {
    final XQuery query = new XQuery(
      "xs:int(\"-1873914410\") - xs:int(\"-2147483648\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("273569238")
    );
  }

  /**
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:int(upper bound) $arg2 = xs:int(lower bound) .
   */
  @org.junit.Test
  public void opNumericSubtractint2args3() {
    final XQuery query = new XQuery(
      "xs:int(\"-1\") - xs:int(\"-2147483648\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:int(lower bound) $arg2 = xs:int(mid range) .
   */
  @org.junit.Test
  public void opNumericSubtractint2args4() {
    final XQuery query = new XQuery(
      "xs:int(\"-2147483648\") - xs:int(\"-1873914410\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-273569238")
    );
  }

  /**
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:int(lower bound) $arg2 = xs:int(upper bound) .
   */
  @org.junit.Test
  public void opNumericSubtractint2args5() {
    final XQuery query = new XQuery(
      "xs:int(\"-1\") - xs:int(\"2147483647\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:integer(lower bound) $arg2 = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void opNumericSubtractintg2args1() {
    final XQuery query = new XQuery(
      "xs:integer(\"-999999999999999999\") - xs:integer(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:integer(mid range) $arg2 = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void opNumericSubtractintg2args2() {
    final XQuery query = new XQuery(
      "xs:integer(\"0\") - xs:integer(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:integer(upper bound) $arg2 = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void opNumericSubtractintg2args3() {
    final XQuery query = new XQuery(
      "xs:integer(\"0\") - xs:integer(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:integer(lower bound) $arg2 = xs:integer(mid range) .
   */
  @org.junit.Test
  public void opNumericSubtractintg2args4() {
    final XQuery query = new XQuery(
      "xs:integer(\"-169006502882975695\") - xs:integer(\"830993497117024304\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:integer(lower bound) $arg2 = xs:integer(upper bound) .
   */
  @org.junit.Test
  public void opNumericSubtractintg2args5() {
    final XQuery query = new XQuery(
      "xs:integer(\"0\") - xs:integer(\"999999999999999999\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:long(lower bound) $arg2 = xs:long(lower bound) .
   */
  @org.junit.Test
  public void opNumericSubtractlng2args1() {
    final XQuery query = new XQuery(
      "xs:long(\"-92233720368547758\") - xs:long(\"-92233720368547758\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:long(mid range) $arg2 = xs:long(lower bound) .
   */
  @org.junit.Test
  public void opNumericSubtractlng2args2() {
    final XQuery query = new XQuery(
      "xs:long(\"-47175562203048468\") - xs:long(\"-92233720368547758\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("45058158165499290")
    );
  }

  /**
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:long(upper bound) $arg2 = xs:long(lower bound) .
   */
  @org.junit.Test
  public void opNumericSubtractlng2args3() {
    final XQuery query = new XQuery(
      "xs:long(\"0\") - xs:long(\"-92233720368547758\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:long(lower bound) $arg2 = xs:long(mid range) .
   */
  @org.junit.Test
  public void opNumericSubtractlng2args4() {
    final XQuery query = new XQuery(
      "xs:long(\"-92233720368547758\") - xs:long(\"-47175562203048468\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-45058158165499290")
    );
  }

  /**
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:long(lower bound) $arg2 = xs:long(upper bound) .
   */
  @org.junit.Test
  public void opNumericSubtractlng2args5() {
    final XQuery query = new XQuery(
      "xs:long(\"0\") - xs:long(\"92233720368547758\")",
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
   *  Simple subtraction test with () as one operand should return null .
   */
  @org.junit.Test
  public void opNumericSubtractmix2args1() {
    final XQuery query = new XQuery(
      "1 - ()",
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
   *  Simple subtraction test pass string for second operator .
   */
  @org.junit.Test
  public void opNumericSubtractmix2args2() {
    final XQuery query = new XQuery(
      "1 - '1'",
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
   *  Simple subtraction test, second operator cast string to integer .
   */
  @org.junit.Test
  public void opNumericSubtractmix2args3() {
    final XQuery query = new XQuery(
      "1 - xs:integer('1')",
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
   *  Simple subtraction test, second operator is a node, atomization applied .
   */
  @org.junit.Test
  public void opNumericSubtractmix2args4() {
    final XQuery query = new XQuery(
      "3 - <a> 2 </a>",
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
   *  Simple subtraction test, second operator is a node, atomization applied .
   */
  @org.junit.Test
  public void opNumericSubtractmix2args5() {
    final XQuery query = new XQuery(
      "3 - <a> <b> 2 </b> </a>",
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
   *  Simple subtraction test, second operator node which is not atomizable .
   */
  @org.junit.Test
  public void opNumericSubtractmix2args6() {
    final XQuery query = new XQuery(
      "3 - <a> <b> 2</b> <c> 2</c> </a>",
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
   *  Simple subtraction test, two operands are nodes .
   */
  @org.junit.Test
  public void opNumericSubtractmix2args7() {
    final XQuery query = new XQuery(
      "<a> 2 </a> - <b> 1 </b>",
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
   *  Simple subtraction test, second operator is a node, atomizable but not castable to integer .
   */
  @org.junit.Test
  public void opNumericSubtractmix2args8() {
    final XQuery query = new XQuery(
      "1 - <a> x </a>",
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
   *  Simple subtraction test pass an empty node for second operator .
   */
  @org.junit.Test
  public void opNumericSubtractmix2args9() {
    final XQuery query = new XQuery(
      "1 - <a/>",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:negativeInteger(lower bound) $arg2 = xs:negativeInteger(mid range) .
   */
  @org.junit.Test
  public void opNumericSubtractnint2args1() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-999999999999999999\") - xs:negativeInteger(\"-297014075999096793\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-702985924000903206")
    );
  }

  /**
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:negativeInteger(lower bound) $arg2 = xs:negativeInteger(upper bound) .
   */
  @org.junit.Test
  public void opNumericSubtractnint2args2() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-999999999999999999\") - xs:negativeInteger(\"-1\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999998")
    );
  }

  /**
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:nonNegativeInteger(lower bound) $arg2 = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericSubtractnni2args1() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"0\") - xs:nonNegativeInteger(\"0\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:nonNegativeInteger(mid range) $arg2 = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericSubtractnni2args2() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"303884545991464527\") - xs:nonNegativeInteger(\"0\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:nonNegativeInteger(upper bound) $arg2 = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericSubtractnni2args3() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"999999999999999999\") - xs:nonNegativeInteger(\"0\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:nonNegativeInteger(lower bound) $arg2 = xs:nonNegativeInteger(mid range) .
   */
  @org.junit.Test
  public void opNumericSubtractnni2args4() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"303884545991464527\") - xs:nonNegativeInteger(\"303884545991464527\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:nonNegativeInteger(lower bound) $arg2 = xs:nonNegativeInteger(upper bound) .
   */
  @org.junit.Test
  public void opNumericSubtractnni2args5() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"999999999999999999\") - xs:nonNegativeInteger(\"999999999999999999\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:nonPositiveInteger(lower bound) $arg2 = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericSubtractnpi2args1() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"-999999999999999999\") - xs:nonPositiveInteger(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:nonPositiveInteger(mid range) $arg2 = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericSubtractnpi2args2() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"-999999999999999999\") - xs:nonPositiveInteger(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:nonPositiveInteger(upper bound) $arg2 = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericSubtractnpi2args3() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"-999999999999999999\") - xs:nonPositiveInteger(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:nonPositiveInteger(lower bound) $arg2 = xs:nonPositiveInteger(mid range) .
   */
  @org.junit.Test
  public void opNumericSubtractnpi2args4() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"-999999999999999999\") - xs:nonPositiveInteger(\"-475688437271870490\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-524311562728129509")
    );
  }

  /**
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:nonPositiveInteger(lower bound) $arg2 = xs:nonPositiveInteger(upper bound) .
   */
  @org.junit.Test
  public void opNumericSubtractnpi2args5() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"-999999999999999999\") - xs:nonPositiveInteger(\"0\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:positiveInteger(mid range) $arg2 = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericSubtractpint2args1() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"52704602390610033\") - xs:positiveInteger(\"1\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("52704602390610032")
    );
  }

  /**
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:positiveInteger(upper bound) $arg2 = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericSubtractpint2args2() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"999999999999999999\") - xs:positiveInteger(\"1\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999998")
    );
  }

  /**
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:positiveInteger(lower bound) $arg2 = xs:positiveInteger(mid range) .
   */
  @org.junit.Test
  public void opNumericSubtractpint2args3() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"52704602390610034\") - xs:positiveInteger(\"52704602390610033\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:short(lower bound) $arg2 = xs:short(lower bound) .
   */
  @org.junit.Test
  public void opNumericSubtractsht2args1() {
    final XQuery query = new XQuery(
      "xs:short(\"-32768\") - xs:short(\"-32768\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:short(mid range) $arg2 = xs:short(lower bound) .
   */
  @org.junit.Test
  public void opNumericSubtractsht2args2() {
    final XQuery query = new XQuery(
      "xs:short(\"-5324\") - xs:short(\"-32768\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("27444")
    );
  }

  /**
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:short(upper bound) $arg2 = xs:short(lower bound) .
   */
  @org.junit.Test
  public void opNumericSubtractsht2args3() {
    final XQuery query = new XQuery(
      "xs:short(\"-1\") - xs:short(\"-32768\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:short(lower bound) $arg2 = xs:short(mid range) .
   */
  @org.junit.Test
  public void opNumericSubtractsht2args4() {
    final XQuery query = new XQuery(
      "xs:short(\"-32768\") - xs:short(\"-5324\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-27444")
    );
  }

  /**
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:short(lower bound) $arg2 = xs:short(upper bound) .
   */
  @org.junit.Test
  public void opNumericSubtractsht2args5() {
    final XQuery query = new XQuery(
      "xs:short(\"-1\") - xs:short(\"32767\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:unsignedLong(lower bound) $arg2 = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void opNumericSubtractulng2args1() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"0\") - xs:unsignedLong(\"0\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:unsignedLong(mid range) $arg2 = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void opNumericSubtractulng2args2() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"130747108607674654\") - xs:unsignedLong(\"0\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:unsignedLong(upper bound) $arg2 = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void opNumericSubtractulng2args3() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"184467440737095516\") - xs:unsignedLong(\"0\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:unsignedLong(lower bound) $arg2 = xs:unsignedLong(mid range) .
   */
  @org.junit.Test
  public void opNumericSubtractulng2args4() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"130747108607674654\") - xs:unsignedLong(\"130747108607674654\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:unsignedLong(lower bound) $arg2 = xs:unsignedLong(upper bound) .
   */
  @org.junit.Test
  public void opNumericSubtractulng2args5() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"184467440737095516\") - xs:unsignedLong(\"184467440737095516\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:unsignedShort(lower bound) $arg2 = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void opNumericSubtractusht2args1() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"0\") - xs:unsignedShort(\"0\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:unsignedShort(mid range) $arg2 = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void opNumericSubtractusht2args2() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"44633\") - xs:unsignedShort(\"0\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:unsignedShort(upper bound) $arg2 = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void opNumericSubtractusht2args3() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"65535\") - xs:unsignedShort(\"0\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:unsignedShort(lower bound) $arg2 = xs:unsignedShort(mid range) .
   */
  @org.junit.Test
  public void opNumericSubtractusht2args4() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"44633\") - xs:unsignedShort(\"44633\")",
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
   *  Evaluates The "op:numeric-subtract" operator with the arguments set as follows: $arg1 = xs:unsignedShort(lower bound) $arg2 = xs:unsignedShort(upper bound) .
   */
  @org.junit.Test
  public void opNumericSubtractusht2args5() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"65535\") - xs:unsignedShort(\"65535\")",
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
