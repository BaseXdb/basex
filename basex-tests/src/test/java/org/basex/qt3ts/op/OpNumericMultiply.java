package org.basex.qt3ts.op;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the numeric-multiply() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpNumericMultiply extends QT3TestSet {

  /**
   *  A test whose essence is: `xs:double(6) * xs:double(2) eq 12`. .
   */
  @org.junit.Test
  public void kNumericMultiply1() {
    final XQuery query = new XQuery(
      "xs:double(6) * xs:double(2) eq 12",
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
   *  A test whose essence is: `(xs:float(6) * xs:decimal(2)) instance of xs:float`. .
   */
  @org.junit.Test
  public void kNumericMultiply10() {
    final XQuery query = new XQuery(
      "(xs:float(6) * xs:decimal(2)) instance of xs:float",
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
   *  A test whose essence is: `(xs:float(6) * xs:integer(2)) instance of xs:float`. .
   */
  @org.junit.Test
  public void kNumericMultiply11() {
    final XQuery query = new XQuery(
      "(xs:float(6) * xs:integer(2)) instance of xs:float",
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
   *  A test whose essence is: `(xs:integer(6) * xs:float(2)) instance of xs:float`. .
   */
  @org.junit.Test
  public void kNumericMultiply12() {
    final XQuery query = new XQuery(
      "(xs:integer(6) * xs:float(2)) instance of xs:float",
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
   *  A test whose essence is: `(xs:float(6) * xs:float(2)) instance of xs:float`. .
   */
  @org.junit.Test
  public void kNumericMultiply13() {
    final XQuery query = new XQuery(
      "(xs:float(6) * xs:float(2)) instance of xs:float",
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
   *  A test whose essence is: `(xs:decimal(6) * xs:double(2)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNumericMultiply14() {
    final XQuery query = new XQuery(
      "(xs:decimal(6) * xs:double(2)) instance of xs:double",
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
   *  A test whose essence is: `(xs:double(6) * xs:decimal(2)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNumericMultiply15() {
    final XQuery query = new XQuery(
      "(xs:double(6) * xs:decimal(2)) instance of xs:double",
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
   *  A test whose essence is: `(xs:double(6) * xs:float(2)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNumericMultiply16() {
    final XQuery query = new XQuery(
      "(xs:double(6) * xs:float(2)) instance of xs:double",
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
   *  A test whose essence is: `(xs:float(6) * xs:double(2)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNumericMultiply17() {
    final XQuery query = new XQuery(
      "(xs:float(6) * xs:double(2)) instance of xs:double",
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
   *  A test whose essence is: `(xs:double(6) * xs:integer(2)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNumericMultiply18() {
    final XQuery query = new XQuery(
      "(xs:double(6) * xs:integer(2)) instance of xs:double",
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
   *  A test whose essence is: `(xs:integer(6) * xs:double(2)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNumericMultiply19() {
    final XQuery query = new XQuery(
      "(xs:integer(6) * xs:double(2)) instance of xs:double",
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
   *  A test whose essence is: `xs:decimal(6) * xs:decimal(2) eq 12`. .
   */
  @org.junit.Test
  public void kNumericMultiply2() {
    final XQuery query = new XQuery(
      "xs:decimal(6) * xs:decimal(2) eq 12",
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
   *  A test whose essence is: `(xs:double(6) * xs:double(2)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNumericMultiply20() {
    final XQuery query = new XQuery(
      "(xs:double(6) * xs:double(2)) instance of xs:double",
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
   *  A test whose essence is: `string(xs:float("NaN") * 3) eq "NaN"`. .
   */
  @org.junit.Test
  public void kNumericMultiply21() {
    final XQuery query = new XQuery(
      "string(xs:float(\"NaN\") * 3) eq \"NaN\"",
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
   *  A test whose essence is: `string(xs:double("NaN") * 3) eq "NaN"`. .
   */
  @org.junit.Test
  public void kNumericMultiply22() {
    final XQuery query = new XQuery(
      "string(xs:double(\"NaN\") * 3) eq \"NaN\"",
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
   *  A test whose essence is: `string(3 * xs:float("NaN")) eq "NaN"`. .
   */
  @org.junit.Test
  public void kNumericMultiply23() {
    final XQuery query = new XQuery(
      "string(3 * xs:float(\"NaN\")) eq \"NaN\"",
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
   *  A test whose essence is: `string(3 * xs:double("NaN")) eq "NaN"`. .
   */
  @org.junit.Test
  public void kNumericMultiply24() {
    final XQuery query = new XQuery(
      "string(3 * xs:double(\"NaN\")) eq \"NaN\"",
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
   *  Invalid whitespace involving multiplication operator and '/'. .
   */
  @org.junit.Test
  public void kNumericMultiply25() {
    final XQuery query = new XQuery(
      "/*5",
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
   *  Invalid whitespace involving multiplication operator and '/'. .
   */
  @org.junit.Test
  public void kNumericMultiply26() {
    final XQuery query = new XQuery(
      "/ * 5",
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
   *  Invalid whitespace involving multiplication operator and '/'. .
   */
  @org.junit.Test
  public void kNumericMultiply27() {
    final XQuery query = new XQuery(
      "4 + / * 5",
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
   *  Invoke the '*' operator with invalid operands. .
   */
  @org.junit.Test
  public void kNumericMultiply28() {
    final XQuery query = new XQuery(
      "\"3\" * \"3\"",
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
   *  Invoke the '*' operator with invalid operands. .
   */
  @org.junit.Test
  public void kNumericMultiply29() {
    final XQuery query = new XQuery(
      "1 * \"3\"",
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
   *  A test whose essence is: `xs:integer(6) * xs:integer(2) eq 12`. .
   */
  @org.junit.Test
  public void kNumericMultiply3() {
    final XQuery query = new XQuery(
      "xs:integer(6) * xs:integer(2) eq 12",
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
   *  A test whose essence is: `(3 * xs:untypedAtomic(3)) eq 9`. .
   */
  @org.junit.Test
  public void kNumericMultiply30() {
    final XQuery query = new XQuery(
      "(3 * xs:untypedAtomic(3)) eq 9",
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
   *  A test whose essence is: `(xs:untypedAtomic(3) * 3) eq 9`. .
   */
  @org.junit.Test
  public void kNumericMultiply31() {
    final XQuery query = new XQuery(
      "(xs:untypedAtomic(3) * 3) eq 9",
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
  public void kNumericMultiply32() {
    final XQuery query = new XQuery(
      "empty(() * ())",
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
  public void kNumericMultiply33() {
    final XQuery query = new XQuery(
      "empty(() * 1)",
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
   *  A test whose essence is: `xs:float(6) * xs:float(2) eq 12`. .
   */
  @org.junit.Test
  public void kNumericMultiply4() {
    final XQuery query = new XQuery(
      "xs:float(6) * xs:float(2) eq 12",
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
   *  A test whose essence is: `(xs:decimal(6) * xs:integer(2)) instance of xs:decimal`. .
   */
  @org.junit.Test
  public void kNumericMultiply5() {
    final XQuery query = new XQuery(
      "(xs:decimal(6) * xs:integer(2)) instance of xs:decimal",
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
   *  A test whose essence is: `(xs:integer(6) * xs:decimal(2)) instance of xs:decimal`. .
   */
  @org.junit.Test
  public void kNumericMultiply6() {
    final XQuery query = new XQuery(
      "(xs:integer(6) * xs:decimal(2)) instance of xs:decimal",
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
   *  A test whose essence is: `(xs:integer(6) * xs:integer(2)) instance of xs:integer`. .
   */
  @org.junit.Test
  public void kNumericMultiply7() {
    final XQuery query = new XQuery(
      "(xs:integer(6) * xs:integer(2)) instance of xs:integer",
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
   *  A test whose essence is: `(xs:decimal(6) * xs:decimal(2)) instance of xs:decimal`. .
   */
  @org.junit.Test
  public void kNumericMultiply8() {
    final XQuery query = new XQuery(
      "(xs:decimal(6) * xs:decimal(2)) instance of xs:decimal",
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
   *  A test whose essence is: `(xs:decimal(6) * xs:float(2)) instance of xs:float`. .
   */
  @org.junit.Test
  public void kNumericMultiply9() {
    final XQuery query = new XQuery(
      "(xs:decimal(6) * xs:float(2)) instance of xs:float",
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
   *  test multiplication by xs:double NaN .
   */
  @org.junit.Test
  public void cbclNumericMultiply001() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:double) as xs:double { $arg * $arg }; \n" +
      "      \txs:double('NaN') * local:square(7)",
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
   *  test multiplication by xs:double NaN .
   */
  @org.junit.Test
  public void cbclNumericMultiply002() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:double) as xs:double { $arg * $arg }; \n" +
      "      \tlocal:square(7) * xs:double('NaN')",
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
   *  test multiplication by xs:double 1 .
   */
  @org.junit.Test
  public void cbclNumericMultiply003() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:double) as xs:double { $arg * $arg }; \n" +
      "      \tlocal:square(7) * 1e0",
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
   *  test multiplication by xs:double 1 .
   */
  @org.junit.Test
  public void cbclNumericMultiply004() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:double) as xs:double { $arg * $arg }; \n" +
      "      \t1e0 * local:square(7)",
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
   *  test multiplication by xs:double -1 .
   */
  @org.junit.Test
  public void cbclNumericMultiply005() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:double) as xs:double { $arg * $arg }; \n" +
      "      \tlocal:square(7) * -1e0",
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
   *  test multiplication by xs:double -1 .
   */
  @org.junit.Test
  public void cbclNumericMultiply006() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:double) as xs:double { $arg * $arg }; \n" +
      "      \t-1e0 * local:square(7)",
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
   *  test multiplication by xs:float NaN .
   */
  @org.junit.Test
  public void cbclNumericMultiply007() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:float) as xs:float { $arg * $arg }; \n" +
      "      \txs:float('NaN') * local:square(7)",
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
   *  test multiplication by xs:float NaN .
   */
  @org.junit.Test
  public void cbclNumericMultiply008() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:float) as xs:float { $arg * $arg }; \n" +
      "      \tlocal:square(7) * xs:float('NaN')",
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
   *  test multiplication by xs:float 1 .
   */
  @org.junit.Test
  public void cbclNumericMultiply009() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:float) as xs:float { $arg * $arg }; \n" +
      "      \tlocal:square(7) * xs:float(1)",
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
   *  test multiplication by xs:float 1 .
   */
  @org.junit.Test
  public void cbclNumericMultiply010() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:float) as xs:float { $arg * $arg }; \n" +
      "      \txs:float(1) * local:square(7)",
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
   *  test multiplication by xs:float -1 .
   */
  @org.junit.Test
  public void cbclNumericMultiply011() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:float) as xs:float { $arg * $arg }; \n" +
      "      \tlocal:square(7) * xs:float(-1)",
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
   *  test multiplication by xs:float -1 .
   */
  @org.junit.Test
  public void cbclNumericMultiply012() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:float) as xs:float { $arg * $arg }; \n" +
      "      \txs:float(-1) * local:square(7)",
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
   *  test multiplication by xs:decimal 0 .
   */
  @org.junit.Test
  public void cbclNumericMultiply013() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:decimal) as xs:decimal { $arg * $arg }; \n" +
      "      \tlocal:square(2.0) * 0.0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  test multiplication by xs:decimal 0 .
   */
  @org.junit.Test
  public void cbclNumericMultiply014() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:decimal) as xs:decimal { $arg * $arg }; \n" +
      "      \t0.0 * local:square(7.0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  test multiplication by xs:decimal 1 .
   */
  @org.junit.Test
  public void cbclNumericMultiply015() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:decimal) as xs:decimal { $arg * $arg }; \n" +
      "      \tlocal:square(7.0) * 1.0",
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
   *  test multiplication by xs:decimal 1 .
   */
  @org.junit.Test
  public void cbclNumericMultiply016() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:decimal) as xs:decimal { $arg * $arg }; \n" +
      "      \t1.0 * local:square(7.0)",
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
   *  test multiplication by xs:decimal -1 .
   */
  @org.junit.Test
  public void cbclNumericMultiply017() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:decimal) as xs:decimal { $arg * $arg }; \n" +
      "      \tlocal:square(7.0) * -1.0",
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
   *  test multiplication by xs:decimal -1 .
   */
  @org.junit.Test
  public void cbclNumericMultiply018() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:decimal) as xs:decimal { $arg * $arg }; \n" +
      "      \t-1.0 * local:square(7.0)",
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
   *  test multiplication by xs:integer 0 .
   */
  @org.junit.Test
  public void cbclNumericMultiply019() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:integer) as xs:integer { $arg * $arg }; \n" +
      "      \tlocal:square(2) * 0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  test multiplication by xs:integer 0 .
   */
  @org.junit.Test
  public void cbclNumericMultiply020() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:integer) as xs:integer { $arg * $arg }; \n" +
      "      \t0 * local:square(2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  test multiplication by xs:integer 1 .
   */
  @org.junit.Test
  public void cbclNumericMultiply021() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:integer) as xs:integer { $arg * $arg }; \n" +
      "      \tlocal:square(7) * 1",
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
   *  test multiplication by xs:integer 1 .
   */
  @org.junit.Test
  public void cbclNumericMultiply022() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:integer) as xs:integer { $arg * $arg }; \n" +
      "      \t1 * local:square(7)",
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
   *  test multiplication by xs:integer -1 .
   */
  @org.junit.Test
  public void cbclNumericMultiply023() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:integer) as xs:integer { $arg * $arg }; \n" +
      "      \tlocal:square(7) * -1",
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
   *  test multiplication by xs:integer -1 .
   */
  @org.junit.Test
  public void cbclNumericMultiply024() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:integer) as xs:integer { $arg * $arg }; \n" +
      "      \t-1 * local:square(7)",
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
   *  test multiplication of large xs:decimal .
   */
  @org.junit.Test
  public void cbclNumericMultiply025() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:decimal) as xs:decimal { $arg * $arg }; \n" +
      "      \tlocal:square(9223372036854775808.1) gt 0",
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
   *  test multiplication of large xs:integer .
   */
  @org.junit.Test
  public void cbclNumericMultiply026() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:integer) as xs:integer { $arg * $arg }; \n" +
      "      \tlocal:square(9223372036854775808) gt 0",
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
   *  ensure that multiplying xs:integers returns an xs:integer .
   */
  @org.junit.Test
  public void cbclNumericMultiply027() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:integer($x as xs:decimal) { if ($x mod 1 eq 0) then xs:integer($x) else $x }; \n" +
      "      \t(local:integer(2) * local:integer(2)) instance of xs:integer",
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
   *  Check dynamic type of numeric multiply on arguments of union of numeric types and untypedAtomic. .
   */
  @org.junit.Test
  public void opNumericMultiply1() {
    final XQuery query = new XQuery(
      "for $x in (1, xs:decimal(2), xs:float(3), xs:double(4), xs:untypedAtomic(5)) for $y in (1, xs:decimal(2), xs:float(3), xs:double(4), xs:untypedAtomic(5)) return typeswitch ($x * $y) case xs:integer return \"integer\" case xs:decimal return \"decimal\" case xs:float return \"float\" case xs:double return \"double\" default return error()",
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
   *  Evaluates The "op:numeric-multiply" operator with the arguments set as follows: $arg1 = xs:double(lower bound) $arg2 = xs:double(lower bound) .
   */
  @org.junit.Test
  public void opNumericMultiplydbl2args1() {
    final XQuery query = new XQuery(
      "xs:double(\"-1.0000000000\") * xs:double(\"-1.7976931348623157E308\")",
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
   *  Evaluates The "op:numeric-multiply" operator with the arguments set as follows: $arg1 = xs:double(mid range) $arg2 = xs:double(lower bound) .
   */
  @org.junit.Test
  public void opNumericMultiplydbl2args2() {
    final XQuery query = new XQuery(
      "xs:double(\"0\") * xs:double(\"-1.7976931348623157E308\")",
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
   *  Evaluates The "op:numeric-multiply" operator with the arguments set as follows: $arg1 = xs:double(upper bound) $arg2 = xs:double(lower bound) .
   */
  @org.junit.Test
  public void opNumericMultiplydbl2args3() {
    final XQuery query = new XQuery(
      "xs:double(\"1.7976931348623157E308\") * xs:double(\"-1.0000000000\")",
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
   *  Evaluates The "op:numeric-multiply" operator with the arguments set as follows: $arg1 = xs:double(lower bound) $arg2 = xs:double(mid range) .
   */
  @org.junit.Test
  public void opNumericMultiplydbl2args4() {
    final XQuery query = new XQuery(
      "xs:double(\"-1.7976931348623157E308\") * xs:double(\"0\")",
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
   *  Evaluates The "op:numeric-multiply" operator with the arguments set as follows: $arg1 = xs:double(lower bound) $arg2 = xs:double(upper bound) .
   */
  @org.junit.Test
  public void opNumericMultiplydbl2args5() {
    final XQuery query = new XQuery(
      "xs:double(\"-1.0000000000\") * xs:double(\"1.7976931348623157E308\")",
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
   *  Evaluates The "op:numeric-multiply" operator with the arguments set as follows: $arg1 = xs:decimal(lower bound) $arg2 = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void opNumericMultiplydec2args1() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-1.0000000000\") * xs:decimal(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-multiply" operator with the arguments set as follows: $arg1 = xs:decimal(mid range) $arg2 = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void opNumericMultiplydec2args2() {
    final XQuery query = new XQuery(
      "xs:decimal(\"617375191608514839\") * xs:decimal(\"0\")",
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
   *  Evaluates The "op:numeric-multiply" operator with the arguments set as follows: $arg1 = xs:decimal(upper bound) $arg2 = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void opNumericMultiplydec2args3() {
    final XQuery query = new XQuery(
      "xs:decimal(\"999999999999999999\") * xs:decimal(\"-1.0000000000\")",
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
   *  Evaluates The "op:numeric-multiply" operator with the arguments set as follows: $arg1 = xs:decimal(lower bound) $arg2 = xs:decimal(mid range) .
   */
  @org.junit.Test
  public void opNumericMultiplydec2args4() {
    final XQuery query = new XQuery(
      "xs:decimal(\"0\") * xs:decimal(\"617375191608514839\")",
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
   *  Evaluates The "op:numeric-multiply" operator with the arguments set as follows: $arg1 = xs:decimal(lower bound) $arg2 = xs:decimal(upper bound) .
   */
  @org.junit.Test
  public void opNumericMultiplydec2args5() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-1.0000000000\") * xs:decimal(\"999999999999999999\")",
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
   *  Evaluates The "op:numeric-multiply" operator with the arguments set as follows: $arg1 = xs:float(lower bound) $arg2 = xs:float(lower bound) .
   */
  @org.junit.Test
  public void opNumericMultiplyflt2args1() {
    final XQuery query = new XQuery(
      "xs:float(\"-1.0000000000\") * xs:float(\"-3.4028235E38\")",
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
   *  Evaluates The "op:numeric-multiply" operator with the arguments set as follows: $arg1 = xs:float(mid range) $arg2 = xs:float(lower bound) .
   */
  @org.junit.Test
  public void opNumericMultiplyflt2args2() {
    final XQuery query = new XQuery(
      "xs:float(\"0\") * xs:float(\"-3.4028235E38\")",
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
   *  Evaluates The "op:numeric-multiply" operator with the arguments set as follows: $arg1 = xs:float(upper bound) $arg2 = xs:float(lower bound) .
   */
  @org.junit.Test
  public void opNumericMultiplyflt2args3() {
    final XQuery query = new XQuery(
      "xs:float(\"3.4028235E38\") * xs:float(\"-1.0000000000\")",
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
   *  Evaluates The "op:numeric-multiply" operator with the arguments set as follows: $arg1 = xs:float(lower bound) $arg2 = xs:float(mid range) .
   */
  @org.junit.Test
  public void opNumericMultiplyflt2args4() {
    final XQuery query = new XQuery(
      "xs:float(\"-3.4028235E38\") * xs:float(\"0\")",
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
   *  Evaluates The "op:numeric-multiply" operator with the arguments set as follows: $arg1 = xs:float(lower bound) $arg2 = xs:float(upper bound) .
   */
  @org.junit.Test
  public void opNumericMultiplyflt2args5() {
    final XQuery query = new XQuery(
      "xs:float(\"-1.0000000000\") * xs:float(\"3.4028235E38\")",
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
   *  Simple multiplication test with () as one operand should return null .
   */
  @org.junit.Test
  public void opNumericMultiplymix2args1() {
    final XQuery query = new XQuery(
      "1 * ()",
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
   *  Simple multiplication test pass string for second operator .
   */
  @org.junit.Test
  public void opNumericMultiplymix2args2() {
    final XQuery query = new XQuery(
      "1 * '1'",
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
   *  Simple multiplication test, second operator cast string to integer .
   */
  @org.junit.Test
  public void opNumericMultiplymix2args3() {
    final XQuery query = new XQuery(
      "1 * xs:integer('1')",
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
   *  Simple multiplication test, second operator is a node, atomization applied .
   */
  @org.junit.Test
  public void opNumericMultiplymix2args4() {
    final XQuery query = new XQuery(
      "1 * <a> 2 </a>",
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
   *  Simple multiplication test, second operator is a node, atomization applied .
   */
  @org.junit.Test
  public void opNumericMultiplymix2args5() {
    final XQuery query = new XQuery(
      "1 * <a> <b> 2 </b> </a>",
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
   *  Simple multiplication test, second operator node which is not atomizable .
   */
  @org.junit.Test
  public void opNumericMultiplymix2args6() {
    final XQuery query = new XQuery(
      "1 * <a> <b> 2</b> <c> 2</c> </a>",
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
   *  Simple multiplication test, two operands are nodes .
   */
  @org.junit.Test
  public void opNumericMultiplymix2args7() {
    final XQuery query = new XQuery(
      "<a> 1 </a> * <b> 2 </b>",
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
   *  Simple multiplication test, second operator is a node, atomizable but not castable to integer .
   */
  @org.junit.Test
  public void opNumericMultiplymix2args8() {
    final XQuery query = new XQuery(
      "1 * <a> x </a>",
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
   *  Simple multiplication test, pass an empty node for second operator .
   */
  @org.junit.Test
  public void opNumericMultiplymix2args9() {
    final XQuery query = new XQuery(
      "1 * <a/>",
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
   *  Evaluates The "op:numeric-multiply" operator with the arguments set as follows: $arg1 = xs:nonNegativeInteger(lower bound) $arg2 = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericMultiplynni2args1() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"0\") * xs:nonNegativeInteger(\"0\")",
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
   *  Evaluates The "op:numeric-multiply" operator with the arguments set as follows: $arg1 = xs:nonNegativeInteger(mid range) $arg2 = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericMultiplynni2args2() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"303884545991464527\") * xs:nonNegativeInteger(\"0\")",
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
   *  Evaluates The "op:numeric-multiply" operator with the arguments set as follows: $arg1 = xs:nonNegativeInteger(upper bound) $arg2 = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericMultiplynni2args3() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"999999999999999999\") * xs:nonNegativeInteger(\"0\")",
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
   *  Evaluates The "op:numeric-multiply" operator with the arguments set as follows: $arg1 = xs:nonNegativeInteger(lower bound) $arg2 = xs:nonNegativeInteger(mid range) .
   */
  @org.junit.Test
  public void opNumericMultiplynni2args4() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"0\") * xs:nonNegativeInteger(\"303884545991464527\")",
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
   *  Evaluates The "op:numeric-multiply" operator with the arguments set as follows: $arg1 = xs:nonNegativeInteger(lower bound) $arg2 = xs:nonNegativeInteger(upper bound) .
   */
  @org.junit.Test
  public void opNumericMultiplynni2args5() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"0\") * xs:nonNegativeInteger(\"999999999999999999\")",
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
   *  Evaluates The "op:numeric-multiply" operator with the arguments set as follows: $arg1 = xs:nonPositiveInteger(upper bound) $arg2 = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericMultiplynpi2args1() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"0\") * xs:nonPositiveInteger(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-multiply" operator with the arguments set as follows: $arg1 = xs:nonPositiveInteger(lower bound) $arg2 = xs:nonPositiveInteger(upper bound) .
   */
  @org.junit.Test
  public void opNumericMultiplynpi2args2() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"-999999999999999999\") * xs:nonPositiveInteger(\"0\")",
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
   *  Evaluates The "op:numeric-multiply" operator with the arguments set as follows: $arg1 = xs:positiveInteger(lower bound) $arg2 = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericMultiplypint2args1() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"1\") * xs:positiveInteger(\"1\")",
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
   *  Evaluates The "op:numeric-multiply" operator with the arguments set as follows: $arg1 = xs:positiveInteger(mid range) $arg2 = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericMultiplypint2args2() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"52704602390610033\") * xs:positiveInteger(\"1\")",
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
   *  Evaluates The "op:numeric-multiply" operator with the arguments set as follows: $arg1 = xs:positiveInteger(upper bound) $arg2 = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericMultiplypint2args3() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"999999999999999999\") * xs:positiveInteger(\"1\")",
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
   *  Evaluates The "op:numeric-multiply" operator with the arguments set as follows: $arg1 = xs:positiveInteger(lower bound) $arg2 = xs:positiveInteger(mid range) .
   */
  @org.junit.Test
  public void opNumericMultiplypint2args4() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"1\") * xs:positiveInteger(\"52704602390610033\")",
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
   *  Evaluates The "op:numeric-multiply" operator with the arguments set as follows: $arg1 = xs:positiveInteger(lower bound) $arg2 = xs:positiveInteger(upper bound) .
   */
  @org.junit.Test
  public void opNumericMultiplypint2args5() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"1\") * xs:positiveInteger(\"999999999999999999\")",
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
   *  Evaluates The "op:numeric-multiply" operator with the arguments set as follows: $arg1 = xs:unsignedLong(lower bound) $arg2 = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void opNumericMultiplyulng2args1() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"0\") * xs:unsignedLong(\"0\")",
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
   *  Evaluates The "op:numeric-multiply" operator with the arguments set as follows: $arg1 = xs:unsignedLong(mid range) $arg2 = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void opNumericMultiplyulng2args2() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"130747108607674654\") * xs:unsignedLong(\"0\")",
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
   *  Evaluates The "op:numeric-multiply" operator with the arguments set as follows: $arg1 = xs:unsignedLong(upper bound) $arg2 = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void opNumericMultiplyulng2args3() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"184467440737095516\") * xs:unsignedLong(\"0\")",
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
   *  Evaluates The "op:numeric-multiply" operator with the arguments set as follows: $arg1 = xs:unsignedLong(lower bound) $arg2 = xs:unsignedLong(mid range) .
   */
  @org.junit.Test
  public void opNumericMultiplyulng2args4() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"0\") * xs:unsignedLong(\"130747108607674654\")",
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
   *  Evaluates The "op:numeric-multiply" operator with the arguments set as follows: $arg1 = xs:unsignedLong(lower bound) $arg2 = xs:unsignedLong(upper bound) .
   */
  @org.junit.Test
  public void opNumericMultiplyulng2args5() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"0\") * xs:unsignedLong(\"184467440737095516\")",
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
   *  Evaluates The "op:numeric-multiply" operator with the arguments set as follows: $arg1 = xs:unsignedShort(lower bound) $arg2 = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void opNumericMultiplyusht2args1() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"0\") * xs:unsignedShort(\"0\")",
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
   *  Evaluates The "op:numeric-multiply" operator with the arguments set as follows: $arg1 = xs:unsignedShort(mid range) $arg2 = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void opNumericMultiplyusht2args2() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"44633\") * xs:unsignedShort(\"0\")",
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
   *  Evaluates The "op:numeric-multiply" operator with the arguments set as follows: $arg1 = xs:unsignedShort(upper bound) $arg2 = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void opNumericMultiplyusht2args3() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"65535\") * xs:unsignedShort(\"0\")",
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
   *  Evaluates The "op:numeric-multiply" operator with the arguments set as follows: $arg1 = xs:unsignedShort(lower bound) $arg2 = xs:unsignedShort(mid range) .
   */
  @org.junit.Test
  public void opNumericMultiplyusht2args4() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"0\") * xs:unsignedShort(\"44633\")",
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
   *  Evaluates The "op:numeric-multiply" operator with the arguments set as follows: $arg1 = xs:unsignedShort(lower bound) $arg2 = xs:unsignedShort(upper bound) .
   */
  @org.junit.Test
  public void opNumericMultiplyusht2args5() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"0\") * xs:unsignedShort(\"65535\")",
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
