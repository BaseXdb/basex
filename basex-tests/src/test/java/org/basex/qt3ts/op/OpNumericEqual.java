package org.basex.qt3ts.op;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the numeric-equal() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpNumericEqual extends QT3TestSet {

  /**
   *  A test whose essence is: `xs:integer(1) eq xs:integer(1)`. .
   */
  @org.junit.Test
  public void kNumericEqual1() {
    final XQuery query = new XQuery(
      "xs:integer(1) eq xs:integer(1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `xs:decimal(1.1) ne xs:decimal(2.1)`. .
   */
  @org.junit.Test
  public void kNumericEqual10() {
    final XQuery query = new XQuery(
      "xs:decimal(1.1) ne xs:decimal(2.1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `xs:integer(2) ne xs:decimal(2.6)`. .
   */
  @org.junit.Test
  public void kNumericEqual11() {
    final XQuery query = new XQuery(
      "xs:integer(2) ne xs:decimal(2.6)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `xs:double(1.1) eq xs:double(1.1)`. .
   */
  @org.junit.Test
  public void kNumericEqual12() {
    final XQuery query = new XQuery(
      "xs:double(1.1) eq xs:double(1.1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `xs:double(-1.1) eq xs:double(-1.1)`. .
   */
  @org.junit.Test
  public void kNumericEqual13() {
    final XQuery query = new XQuery(
      "xs:double(-1.1) eq xs:double(-1.1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `xs:double(1.1) ne xs:double(2.1)`. .
   */
  @org.junit.Test
  public void kNumericEqual14() {
    final XQuery query = new XQuery(
      "xs:double(1.1) ne xs:double(2.1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `xs:double(-1) ne xs:double(1)`. .
   */
  @org.junit.Test
  public void kNumericEqual15() {
    final XQuery query = new XQuery(
      "xs:double(-1) ne xs:double(1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `xs:double("NaN") ne xs:double("NaN")`. .
   */
  @org.junit.Test
  public void kNumericEqual16() {
    final XQuery query = new XQuery(
      "xs:double(\"NaN\") ne xs:double(\"NaN\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `xs:double("NaN") ne xs:double(0)`. .
   */
  @org.junit.Test
  public void kNumericEqual17() {
    final XQuery query = new XQuery(
      "xs:double(\"NaN\") ne xs:double(0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `xs:double(0) ne xs:double("NaN")`. .
   */
  @org.junit.Test
  public void kNumericEqual18() {
    final XQuery query = new XQuery(
      "xs:double(0) ne xs:double(\"NaN\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `xs:double("-INF") eq xs:double("-INF")`. .
   */
  @org.junit.Test
  public void kNumericEqual19() {
    final XQuery query = new XQuery(
      "xs:double(\"-INF\") eq xs:double(\"-INF\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `xs:integer(1) ne xs:integer(2)`. .
   */
  @org.junit.Test
  public void kNumericEqual2() {
    final XQuery query = new XQuery(
      "xs:integer(1) ne xs:integer(2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `xs:double("INF") eq xs:double("INF")`. .
   */
  @org.junit.Test
  public void kNumericEqual20() {
    final XQuery query = new XQuery(
      "xs:double(\"INF\") eq xs:double(\"INF\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `xs:double("-INF") ne xs:double("INF")`. .
   */
  @org.junit.Test
  public void kNumericEqual21() {
    final XQuery query = new XQuery(
      "xs:double(\"-INF\") ne xs:double(\"INF\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `xs:double("INF") ne xs:double("-INF")`. .
   */
  @org.junit.Test
  public void kNumericEqual22() {
    final XQuery query = new XQuery(
      "xs:double(\"INF\") ne xs:double(\"-INF\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `xs:double("INF") ne xs:double("0")`. .
   */
  @org.junit.Test
  public void kNumericEqual23() {
    final XQuery query = new XQuery(
      "xs:double(\"INF\") ne xs:double(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `xs:double("-INF") ne xs:double("0")`. .
   */
  @org.junit.Test
  public void kNumericEqual24() {
    final XQuery query = new XQuery(
      "xs:double(\"-INF\") ne xs:double(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `xs:double("0") ne xs:double("INF")`. .
   */
  @org.junit.Test
  public void kNumericEqual25() {
    final XQuery query = new XQuery(
      "xs:double(\"0\") ne xs:double(\"INF\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `xs:double("0") ne xs:double("-INF")`. .
   */
  @org.junit.Test
  public void kNumericEqual26() {
    final XQuery query = new XQuery(
      "xs:double(\"0\") ne xs:double(\"-INF\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `xs:float(1.1) eq xs:float(1.1)`. .
   */
  @org.junit.Test
  public void kNumericEqual27() {
    final XQuery query = new XQuery(
      "xs:float(1.1) eq xs:float(1.1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `xs:float(-1.1) eq xs:float(-1.1)`. .
   */
  @org.junit.Test
  public void kNumericEqual28() {
    final XQuery query = new XQuery(
      "xs:float(-1.1) eq xs:float(-1.1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `xs:float(1.1) ne xs:float(2.1)`. .
   */
  @org.junit.Test
  public void kNumericEqual29() {
    final XQuery query = new XQuery(
      "xs:float(1.1) ne xs:float(2.1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `xs:integer(1) eq xs:integer(1)`. .
   */
  @org.junit.Test
  public void kNumericEqual3() {
    final XQuery query = new XQuery(
      "xs:integer(1) eq xs:integer(1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `xs:float("NaN") ne xs:float("NaN")`. .
   */
  @org.junit.Test
  public void kNumericEqual30() {
    final XQuery query = new XQuery(
      "xs:float(\"NaN\") ne xs:float(\"NaN\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `xs:float("NaN") ne xs:float(0)`. .
   */
  @org.junit.Test
  public void kNumericEqual31() {
    final XQuery query = new XQuery(
      "xs:float(\"NaN\") ne xs:float(0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `xs:float(0) ne xs:float("NaN")`. .
   */
  @org.junit.Test
  public void kNumericEqual32() {
    final XQuery query = new XQuery(
      "xs:float(0) ne xs:float(\"NaN\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `xs:float("-INF") eq xs:float("-INF")`. .
   */
  @org.junit.Test
  public void kNumericEqual33() {
    final XQuery query = new XQuery(
      "xs:float(\"-INF\") eq xs:float(\"-INF\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `xs:float("INF") eq xs:float("INF")`. .
   */
  @org.junit.Test
  public void kNumericEqual34() {
    final XQuery query = new XQuery(
      "xs:float(\"INF\") eq xs:float(\"INF\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `xs:float("-INF") ne xs:float("INF")`. .
   */
  @org.junit.Test
  public void kNumericEqual35() {
    final XQuery query = new XQuery(
      "xs:float(\"-INF\") ne xs:float(\"INF\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `xs:float("INF") ne xs:float("-INF")`. .
   */
  @org.junit.Test
  public void kNumericEqual36() {
    final XQuery query = new XQuery(
      "xs:float(\"INF\") ne xs:float(\"-INF\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `xs:float("INF") ne xs:float("0")`. .
   */
  @org.junit.Test
  public void kNumericEqual37() {
    final XQuery query = new XQuery(
      "xs:float(\"INF\") ne xs:float(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `xs:float("-INF") ne xs:float("0")`. .
   */
  @org.junit.Test
  public void kNumericEqual38() {
    final XQuery query = new XQuery(
      "xs:float(\"-INF\") ne xs:float(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `xs:float("0") ne xs:float("INF")`. .
   */
  @org.junit.Test
  public void kNumericEqual39() {
    final XQuery query = new XQuery(
      "xs:float(\"0\") ne xs:float(\"INF\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `xs:integer(-1) eq xs:integer(-1)`. .
   */
  @org.junit.Test
  public void kNumericEqual4() {
    final XQuery query = new XQuery(
      "xs:integer(-1) eq xs:integer(-1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `xs:float("0") ne xs:float("-INF")`. .
   */
  @org.junit.Test
  public void kNumericEqual40() {
    final XQuery query = new XQuery(
      "xs:float(\"0\") ne xs:float(\"-INF\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  An expression involving the 'eq' operator that trigger certain optimization paths in some implementations. .
   */
  @org.junit.Test
  public void kNumericEqual41() {
    final XQuery query = new XQuery(
      "count(remove(remove((current-time(), 1), 1), 1)) eq 0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test that fn:count combined with expressions that might disable compile time evaluations(optimization) as well as the 'eq' operator, is conformant. .
   */
  @org.junit.Test
  public void kNumericEqual42() {
    final XQuery query = new XQuery(
      "not(0 eq count((1, 2, timezone-from-time(current-time()))))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test that fn:count combined with expressions that might disable compile time evaluations(optimization) as well as the '=' operator, is conformant. .
   */
  @org.junit.Test
  public void kNumericEqual43() {
    final XQuery query = new XQuery(
      "0 ne count((1, 2, timezone-from-time(current-time())))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test automatic xs:untypedAtomic conversion. .
   */
  @org.junit.Test
  public void kNumericEqual44() {
    final XQuery query = new XQuery(
      "(xs:untypedAtomic(\"3\"), 3) eq 3",
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
   *  Test automatic xs:untypedAtomic conversion. .
   */
  @org.junit.Test
  public void kNumericEqual45() {
    final XQuery query = new XQuery(
      "count(xs:untypedAtomic(\"1\") to 3) eq 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test automatic xs:untypedAtomic conversion. .
   */
  @org.junit.Test
  public void kNumericEqual46() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"three\") + 3",
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
   *  A syntactically invalid test. .
   */
  @org.junit.Test
  public void kNumericEqual47() {
    final XQuery query = new XQuery(
      "1 := 3",
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
   *  A test whose essence is: `xs:integer(1) ne xs:integer(2)`. .
   */
  @org.junit.Test
  public void kNumericEqual5() {
    final XQuery query = new XQuery(
      "xs:integer(1) ne xs:integer(2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `xs:decimal(1.1) eq xs:decimal(1.1)`. .
   */
  @org.junit.Test
  public void kNumericEqual6() {
    final XQuery query = new XQuery(
      "xs:decimal(1.1) eq xs:decimal(1.1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `xs:decimal(1.1) ne xs:decimal(2.1)`. .
   */
  @org.junit.Test
  public void kNumericEqual7() {
    final XQuery query = new XQuery(
      "xs:decimal(1.1) ne xs:decimal(2.1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `xs:decimal(1.1) eq xs:decimal(1.1)`. .
   */
  @org.junit.Test
  public void kNumericEqual8() {
    final XQuery query = new XQuery(
      "xs:decimal(1.1) eq xs:decimal(1.1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `xs:decimal(-1.1) eq xs:decimal(-1.1)`. .
   */
  @org.junit.Test
  public void kNumericEqual9() {
    final XQuery query = new XQuery(
      "xs:decimal(-1.1) eq xs:decimal(-1.1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Compare an xs:integer to a processing instuction node. .
   */
  @org.junit.Test
  public void k2NumericEqual1() {
    final XQuery query = new XQuery(
      "1 eq <?target 1?>",
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
   *  Compare an xs:integer to a processing instuction node(#2). .
   */
  @org.junit.Test
  public void k2NumericEqual2() {
    final XQuery query = new XQuery(
      "<?target 1?> eq 1",
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
   *  Compare an xs:integer to a comment. .
   */
  @org.junit.Test
  public void k2NumericEqual3() {
    final XQuery query = new XQuery(
      "1 eq <!--1-->",
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
   *  Compare an xs:integer to a comment(#2). .
   */
  @org.junit.Test
  public void k2NumericEqual4() {
    final XQuery query = new XQuery(
      "<!--1--> eq 1",
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
   *  Compare an xs:integer to a text node. .
   */
  @org.junit.Test
  public void k2NumericEqual5() {
    final XQuery query = new XQuery(
      "1 eq text {\"1\"}",
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
   *  Compare an xs:integer to a text node(#2). .
   */
  @org.junit.Test
  public void k2NumericEqual6() {
    final XQuery query = new XQuery(
      "text {\"1\"} eq 1",
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
   *  test comparison of xs:double .
   */
  @org.junit.Test
  public void cbclNumericEqual001() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:double) as xs:double { $arg * $arg }; \n" +
      "      \tnot( local:square(1e0) eq local:square(2e0) )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test comparison of xs:float .
   */
  @org.junit.Test
  public void cbclNumericEqual002() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:float) as xs:float { $arg * $arg }; \n" +
      "      \tnot( local:square(xs:float(1e0)) eq local:square(xs:float(2e0)) )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test comparison of xs:decimal .
   */
  @org.junit.Test
  public void cbclNumericEqual003() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:decimal) as xs:decimal { $arg * $arg }; \n" +
      "      \tnot( local:square(1.0) eq local:square(2.0) )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test comparison of xs:integer .
   */
  @org.junit.Test
  public void cbclNumericEqual004() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:integer) as xs:integer { $arg * $arg }; \n" +
      "      \tnot( local:square(1) eq local:square(2) )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test comparison of xs:double .
   */
  @org.junit.Test
  public void cbclNumericEqual005() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:double) as xs:double { $arg * $arg }; \n" +
      "      \tnot( local:square(1e0) ne local:square(2e0) )",
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
   *  test comparison of xs:float .
   */
  @org.junit.Test
  public void cbclNumericEqual006() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:float) as xs:float { $arg * $arg }; \n" +
      "      \tnot( local:square(xs:float(1e0)) ne local:square(xs:float(2e0)) )",
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
   *  test comparison of xs:decimal .
   */
  @org.junit.Test
  public void cbclNumericEqual007() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:decimal) as xs:decimal { $arg * $arg }; \n" +
      "      \tnot( local:square(1.0) ne local:square(2.0) )",
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
   *  test comparison of xs:integer .
   */
  @org.junit.Test
  public void cbclNumericEqual008() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:integer) as xs:integer { $arg * $arg }; \n" +
      "      \tnot( local:square(1) ne local:square(2) )",
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
   *  test comparison .
   */
  @org.junit.Test
  public void cbclNumericEqual009() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:integer) as xs:integer { $arg * $arg }; \n" +
      "      \tlet $x := local:square(4) return 0 eq $x - 1",
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
   *  test comparison .
   */
  @org.junit.Test
  public void cbclNumericEqual010() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:integer) as xs:integer { $arg * $arg }; \n" +
      "      \tlet $x := local:square(4) return 0 eq $x + 1",
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
   *  test comparison .
   */
  @org.junit.Test
  public void cbclNumericEqual011() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:integer) as xs:integer { $arg * $arg }; \n" +
      "      \tlet $x := local:square(4) return 4 eq $x + 1",
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
   *  test comparison .
   */
  @org.junit.Test
  public void cbclNumericEqual012() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:integer) as xs:integer { $arg * $arg }; \n" +
      "      \tlet $x := local:square(4) return 4 eq 1 + $x",
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
   *  test comparison .
   */
  @org.junit.Test
  public void cbclNumericEqual013() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:integer) as xs:integer { $arg * $arg }; \n" +
      "      \tlet $x := local:square(4) return -52676506002282294014967032012 eq $x + 52676506002282294014967032012",
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
        assertBoolean(false)
      ||
        error("FOAR0002")
      )
    );
  }

  /**
   *  test comparison .
   */
  @org.junit.Test
  public void cbclNumericEqual014() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:integer) as xs:integer { $arg * $arg }; \n" +
      "      \tlet $x := local:square(4) return -52676506002282294014967032012 eq 52676506002282294014967032012 + $x",
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
        assertBoolean(false)
      ||
        error("FOAR0002")
      )
    );
  }

  /**
   *  test comparison .
   */
  @org.junit.Test
  public void cbclNumericEqual015() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:integer) as xs:integer { $arg * $arg }; \n" +
      "      \tlet $x := local:square(4) return 4 eq 1 - $x",
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
        assertBoolean(false)
      ||
        error("FOAR0002")
      )
    );
  }

  /**
   *  test comparison .
   */
  @org.junit.Test
  public void cbclNumericEqual016() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:integer) as xs:integer { $arg * $arg }; \n" +
      "      \tlet $x := local:square(4) return 4 eq $x - 1",
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
   *  test comparison .
   */
  @org.junit.Test
  public void cbclNumericEqual017() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:integer) as xs:integer { $arg * $arg }; \n" +
      "      \tlet $x := local:square(4) return -52676506002282294014967032012 eq 52676506002282294014967032012 - $x",
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
        assertBoolean(false)
      ||
        error("FOAR0002")
      )
    );
  }

  /**
   *  test comparison .
   */
  @org.junit.Test
  public void cbclNumericEqual018() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:square($arg as xs:integer) as xs:integer { $arg * $arg }; \n" +
      "      \tlet $x := local:square(4) return 52676506002282294014967032012 eq $x - 52676506002282294014967032012",
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
        assertBoolean(false)
      ||
        error("FOAR0002")
      )
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = () $arg2 = () .
   */
  @org.junit.Test
  public void opNumericEqualEmptyseq() {
    final XQuery query = new XQuery(
      "() eq ()",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:double(lower bound) $arg2 = xs:double(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualdbl2args1() {
    final XQuery query = new XQuery(
      "xs:double(\"-1.7976931348623157E308\") eq xs:double(\"-1.7976931348623157E308\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:double(lower bound) $arg2 = xs:double(upper bound) .
   */
  @org.junit.Test
  public void opNumericEqualdbl2args10() {
    final XQuery query = new XQuery(
      "xs:double(\"-1.7976931348623157E308\") ne xs:double(\"1.7976931348623157E308\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:double(mid range) $arg2 = xs:double(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualdbl2args2() {
    final XQuery query = new XQuery(
      "xs:double(\"0\") eq xs:double(\"-1.7976931348623157E308\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:double(upper bound) $arg2 = xs:double(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualdbl2args3() {
    final XQuery query = new XQuery(
      "xs:double(\"1.7976931348623157E308\") eq xs:double(\"-1.7976931348623157E308\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:double(lower bound) $arg2 = xs:double(mid range) .
   */
  @org.junit.Test
  public void opNumericEqualdbl2args4() {
    final XQuery query = new XQuery(
      "xs:double(\"-1.7976931348623157E308\") eq xs:double(\"0\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:double(lower bound) $arg2 = xs:double(upper bound) .
   */
  @org.junit.Test
  public void opNumericEqualdbl2args5() {
    final XQuery query = new XQuery(
      "xs:double(\"-1.7976931348623157E308\") eq xs:double(\"1.7976931348623157E308\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:double(lower bound) $arg2 = xs:double(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualdbl2args6() {
    final XQuery query = new XQuery(
      "xs:double(\"-1.7976931348623157E308\") ne xs:double(\"-1.7976931348623157E308\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:double(mid range) $arg2 = xs:double(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualdbl2args7() {
    final XQuery query = new XQuery(
      "xs:double(\"0\") ne xs:double(\"-1.7976931348623157E308\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:double(upper bound) $arg2 = xs:double(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualdbl2args8() {
    final XQuery query = new XQuery(
      "xs:double(\"1.7976931348623157E308\") ne xs:double(\"-1.7976931348623157E308\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:double(lower bound) $arg2 = xs:double(mid range) .
   */
  @org.junit.Test
  public void opNumericEqualdbl2args9() {
    final XQuery query = new XQuery(
      "xs:double(\"-1.7976931348623157E308\") ne xs:double(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:decimal(lower bound) $arg2 = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualdec2args1() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-999999999999999999\") eq xs:decimal(\"-999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:decimal(lower bound) $arg2 = xs:decimal(upper bound) .
   */
  @org.junit.Test
  public void opNumericEqualdec2args10() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-999999999999999999\") ne xs:decimal(\"999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:decimal(mid range) $arg2 = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualdec2args2() {
    final XQuery query = new XQuery(
      "xs:decimal(\"617375191608514839\") eq xs:decimal(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:decimal(upper bound) $arg2 = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualdec2args3() {
    final XQuery query = new XQuery(
      "xs:decimal(\"999999999999999999\") eq xs:decimal(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:decimal(lower bound) $arg2 = xs:decimal(mid range) .
   */
  @org.junit.Test
  public void opNumericEqualdec2args4() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-999999999999999999\") eq xs:decimal(\"617375191608514839\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:decimal(lower bound) $arg2 = xs:decimal(upper bound) .
   */
  @org.junit.Test
  public void opNumericEqualdec2args5() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-999999999999999999\") eq xs:decimal(\"999999999999999999\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:decimal(lower bound) $arg2 = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualdec2args6() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-999999999999999999\") ne xs:decimal(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:decimal(mid range) $arg2 = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualdec2args7() {
    final XQuery query = new XQuery(
      "xs:decimal(\"617375191608514839\") ne xs:decimal(\"-999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:decimal(upper bound) $arg2 = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualdec2args8() {
    final XQuery query = new XQuery(
      "xs:decimal(\"999999999999999999\") ne xs:decimal(\"-999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:decimal(lower bound) $arg2 = xs:decimal(mid range) .
   */
  @org.junit.Test
  public void opNumericEqualdec2args9() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-999999999999999999\") ne xs:decimal(\"617375191608514839\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:float(lower bound) $arg2 = xs:float(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualflt2args1() {
    final XQuery query = new XQuery(
      "xs:float(\"-3.4028235E38\") eq xs:float(\"-3.4028235E38\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:float(lower bound) $arg2 = xs:float(upper bound) .
   */
  @org.junit.Test
  public void opNumericEqualflt2args10() {
    final XQuery query = new XQuery(
      "xs:float(\"-3.4028235E38\") ne xs:float(\"3.4028235E38\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:float(mid range) $arg2 = xs:float(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualflt2args2() {
    final XQuery query = new XQuery(
      "xs:float(\"0\") eq xs:float(\"-3.4028235E38\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:float(upper bound) $arg2 = xs:float(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualflt2args3() {
    final XQuery query = new XQuery(
      "xs:float(\"3.4028235E38\") eq xs:float(\"-3.4028235E38\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:float(lower bound) $arg2 = xs:float(mid range) .
   */
  @org.junit.Test
  public void opNumericEqualflt2args4() {
    final XQuery query = new XQuery(
      "xs:float(\"-3.4028235E38\") eq xs:float(\"0\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:float(lower bound) $arg2 = xs:float(upper bound) .
   */
  @org.junit.Test
  public void opNumericEqualflt2args5() {
    final XQuery query = new XQuery(
      "xs:float(\"-3.4028235E38\") eq xs:float(\"3.4028235E38\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:float(lower bound) $arg2 = xs:float(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualflt2args6() {
    final XQuery query = new XQuery(
      "xs:float(\"-3.4028235E38\") ne xs:float(\"-3.4028235E38\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:float(mid range) $arg2 = xs:float(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualflt2args7() {
    final XQuery query = new XQuery(
      "xs:float(\"0\") ne xs:float(\"-3.4028235E38\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:float(upper bound) $arg2 = xs:float(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualflt2args8() {
    final XQuery query = new XQuery(
      "xs:float(\"3.4028235E38\") ne xs:float(\"-3.4028235E38\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:float(lower bound) $arg2 = xs:float(mid range) .
   */
  @org.junit.Test
  public void opNumericEqualflt2args9() {
    final XQuery query = new XQuery(
      "xs:float(\"-3.4028235E38\") ne xs:float(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:int(lower bound) $arg2 = xs:int(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualint2args1() {
    final XQuery query = new XQuery(
      "xs:int(\"-2147483648\") eq xs:int(\"-2147483648\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:int(lower bound) $arg2 = xs:int(upper bound) .
   */
  @org.junit.Test
  public void opNumericEqualint2args10() {
    final XQuery query = new XQuery(
      "xs:int(\"-2147483648\") ne xs:int(\"2147483647\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:int(mid range) $arg2 = xs:int(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualint2args2() {
    final XQuery query = new XQuery(
      "xs:int(\"-1873914410\") eq xs:int(\"-2147483648\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:int(upper bound) $arg2 = xs:int(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualint2args3() {
    final XQuery query = new XQuery(
      "xs:int(\"2147483647\") eq xs:int(\"-2147483648\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:int(lower bound) $arg2 = xs:int(mid range) .
   */
  @org.junit.Test
  public void opNumericEqualint2args4() {
    final XQuery query = new XQuery(
      "xs:int(\"-2147483648\") eq xs:int(\"-1873914410\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:int(lower bound) $arg2 = xs:int(upper bound) .
   */
  @org.junit.Test
  public void opNumericEqualint2args5() {
    final XQuery query = new XQuery(
      "xs:int(\"-2147483648\") eq xs:int(\"2147483647\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:int(lower bound) $arg2 = xs:int(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualint2args6() {
    final XQuery query = new XQuery(
      "xs:int(\"-2147483648\") ne xs:int(\"-2147483648\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:int(mid range) $arg2 = xs:int(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualint2args7() {
    final XQuery query = new XQuery(
      "xs:int(\"-1873914410\") ne xs:int(\"-2147483648\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:int(upper bound) $arg2 = xs:int(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualint2args8() {
    final XQuery query = new XQuery(
      "xs:int(\"2147483647\") ne xs:int(\"-2147483648\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:int(lower bound) $arg2 = xs:int(mid range) .
   */
  @org.junit.Test
  public void opNumericEqualint2args9() {
    final XQuery query = new XQuery(
      "xs:int(\"-2147483648\") ne xs:int(\"-1873914410\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:integer(lower bound) $arg2 = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualintg2args1() {
    final XQuery query = new XQuery(
      "xs:integer(\"-999999999999999999\") eq xs:integer(\"-999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:integer(lower bound) $arg2 = xs:integer(upper bound) .
   */
  @org.junit.Test
  public void opNumericEqualintg2args10() {
    final XQuery query = new XQuery(
      "xs:integer(\"-999999999999999999\") ne xs:integer(\"999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:integer(mid range) $arg2 = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualintg2args2() {
    final XQuery query = new XQuery(
      "xs:integer(\"830993497117024304\") eq xs:integer(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:integer(upper bound) $arg2 = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualintg2args3() {
    final XQuery query = new XQuery(
      "xs:integer(\"999999999999999999\") eq xs:integer(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:integer(lower bound) $arg2 = xs:integer(mid range) .
   */
  @org.junit.Test
  public void opNumericEqualintg2args4() {
    final XQuery query = new XQuery(
      "xs:integer(\"-999999999999999999\") eq xs:integer(\"830993497117024304\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:integer(lower bound) $arg2 = xs:integer(upper bound) .
   */
  @org.junit.Test
  public void opNumericEqualintg2args5() {
    final XQuery query = new XQuery(
      "xs:integer(\"-999999999999999999\") eq xs:integer(\"999999999999999999\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:integer(lower bound) $arg2 = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualintg2args6() {
    final XQuery query = new XQuery(
      "xs:integer(\"-999999999999999999\") ne xs:integer(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:integer(mid range) $arg2 = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualintg2args7() {
    final XQuery query = new XQuery(
      "xs:integer(\"830993497117024304\") ne xs:integer(\"-999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:integer(upper bound) $arg2 = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualintg2args8() {
    final XQuery query = new XQuery(
      "xs:integer(\"999999999999999999\") ne xs:integer(\"-999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:integer(lower bound) $arg2 = xs:integer(mid range) .
   */
  @org.junit.Test
  public void opNumericEqualintg2args9() {
    final XQuery query = new XQuery(
      "xs:integer(\"-999999999999999999\") ne xs:integer(\"830993497117024304\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:long(lower bound) $arg2 = xs:long(lower bound) .
   */
  @org.junit.Test
  public void opNumericEquallng2args1() {
    final XQuery query = new XQuery(
      "xs:long(\"-92233720368547758\") eq xs:long(\"-92233720368547758\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:long(lower bound) $arg2 = xs:long(upper bound) .
   */
  @org.junit.Test
  public void opNumericEquallng2args10() {
    final XQuery query = new XQuery(
      "xs:long(\"-92233720368547758\") ne xs:long(\"92233720368547758\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:long(mid range) $arg2 = xs:long(lower bound) .
   */
  @org.junit.Test
  public void opNumericEquallng2args2() {
    final XQuery query = new XQuery(
      "xs:long(\"-47175562203048468\") eq xs:long(\"-92233720368547758\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:long(upper bound) $arg2 = xs:long(lower bound) .
   */
  @org.junit.Test
  public void opNumericEquallng2args3() {
    final XQuery query = new XQuery(
      "xs:long(\"92233720368547758\") eq xs:long(\"-92233720368547758\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:long(lower bound) $arg2 = xs:long(mid range) .
   */
  @org.junit.Test
  public void opNumericEquallng2args4() {
    final XQuery query = new XQuery(
      "xs:long(\"-92233720368547758\") eq xs:long(\"-47175562203048468\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:long(lower bound) $arg2 = xs:long(upper bound) .
   */
  @org.junit.Test
  public void opNumericEquallng2args5() {
    final XQuery query = new XQuery(
      "xs:long(\"-92233720368547758\") eq xs:long(\"92233720368547758\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:long(lower bound) $arg2 = xs:long(lower bound) .
   */
  @org.junit.Test
  public void opNumericEquallng2args6() {
    final XQuery query = new XQuery(
      "xs:long(\"-92233720368547758\") ne xs:long(\"-92233720368547758\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:long(mid range) $arg2 = xs:long(lower bound) .
   */
  @org.junit.Test
  public void opNumericEquallng2args7() {
    final XQuery query = new XQuery(
      "xs:long(\"-47175562203048468\") ne xs:long(\"-92233720368547758\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:long(upper bound) $arg2 = xs:long(lower bound) .
   */
  @org.junit.Test
  public void opNumericEquallng2args8() {
    final XQuery query = new XQuery(
      "xs:long(\"92233720368547758\") ne xs:long(\"-92233720368547758\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:long(lower bound) $arg2 = xs:long(mid range) .
   */
  @org.junit.Test
  public void opNumericEquallng2args9() {
    final XQuery query = new XQuery(
      "xs:long(\"-92233720368547758\") ne xs:long(\"-47175562203048468\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:negativeInteger(lower bound) $arg2 = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualnint2args1() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-999999999999999999\") eq xs:negativeInteger(\"-999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:negativeInteger(lower bound) $arg2 = xs:negativeInteger(upper bound) .
   */
  @org.junit.Test
  public void opNumericEqualnint2args10() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-999999999999999999\") ne xs:negativeInteger(\"-1\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:negativeInteger(mid range) $arg2 = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualnint2args2() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-297014075999096793\") eq xs:negativeInteger(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:negativeInteger(upper bound) $arg2 = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualnint2args3() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-1\") eq xs:negativeInteger(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:negativeInteger(lower bound) $arg2 = xs:negativeInteger(mid range) .
   */
  @org.junit.Test
  public void opNumericEqualnint2args4() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-999999999999999999\") eq xs:negativeInteger(\"-297014075999096793\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:negativeInteger(lower bound) $arg2 = xs:negativeInteger(upper bound) .
   */
  @org.junit.Test
  public void opNumericEqualnint2args5() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-999999999999999999\") eq xs:negativeInteger(\"-1\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:negativeInteger(lower bound) $arg2 = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualnint2args6() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-999999999999999999\") ne xs:negativeInteger(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:negativeInteger(mid range) $arg2 = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualnint2args7() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-297014075999096793\") ne xs:negativeInteger(\"-999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:negativeInteger(upper bound) $arg2 = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualnint2args8() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-1\") ne xs:negativeInteger(\"-999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:negativeInteger(lower bound) $arg2 = xs:negativeInteger(mid range) .
   */
  @org.junit.Test
  public void opNumericEqualnint2args9() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-999999999999999999\") ne xs:negativeInteger(\"-297014075999096793\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:nonNegativeInteger(lower bound) $arg2 = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualnni2args1() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"0\") eq xs:nonNegativeInteger(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:nonNegativeInteger(lower bound) $arg2 = xs:nonNegativeInteger(upper bound) .
   */
  @org.junit.Test
  public void opNumericEqualnni2args10() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"0\") ne xs:nonNegativeInteger(\"999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:nonNegativeInteger(mid range) $arg2 = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualnni2args2() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"303884545991464527\") eq xs:nonNegativeInteger(\"0\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:nonNegativeInteger(upper bound) $arg2 = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualnni2args3() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"999999999999999999\") eq xs:nonNegativeInteger(\"0\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:nonNegativeInteger(lower bound) $arg2 = xs:nonNegativeInteger(mid range) .
   */
  @org.junit.Test
  public void opNumericEqualnni2args4() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"0\") eq xs:nonNegativeInteger(\"303884545991464527\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:nonNegativeInteger(lower bound) $arg2 = xs:nonNegativeInteger(upper bound) .
   */
  @org.junit.Test
  public void opNumericEqualnni2args5() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"0\") eq xs:nonNegativeInteger(\"999999999999999999\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:nonNegativeInteger(lower bound) $arg2 = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualnni2args6() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"0\") ne xs:nonNegativeInteger(\"0\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:nonNegativeInteger(mid range) $arg2 = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualnni2args7() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"303884545991464527\") ne xs:nonNegativeInteger(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:nonNegativeInteger(upper bound) $arg2 = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualnni2args8() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"999999999999999999\") ne xs:nonNegativeInteger(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:nonNegativeInteger(lower bound) $arg2 = xs:nonNegativeInteger(mid range) .
   */
  @org.junit.Test
  public void opNumericEqualnni2args9() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"0\") ne xs:nonNegativeInteger(\"303884545991464527\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:nonPositiveInteger(lower bound) $arg2 = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualnpi2args1() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"-999999999999999999\") eq xs:nonPositiveInteger(\"-999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:nonPositiveInteger(lower bound) $arg2 = xs:nonPositiveInteger(upper bound) .
   */
  @org.junit.Test
  public void opNumericEqualnpi2args10() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"-999999999999999999\") ne xs:nonPositiveInteger(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:nonPositiveInteger(mid range) $arg2 = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualnpi2args2() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"-475688437271870490\") eq xs:nonPositiveInteger(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:nonPositiveInteger(upper bound) $arg2 = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualnpi2args3() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"0\") eq xs:nonPositiveInteger(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:nonPositiveInteger(lower bound) $arg2 = xs:nonPositiveInteger(mid range) .
   */
  @org.junit.Test
  public void opNumericEqualnpi2args4() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"-999999999999999999\") eq xs:nonPositiveInteger(\"-475688437271870490\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:nonPositiveInteger(lower bound) $arg2 = xs:nonPositiveInteger(upper bound) .
   */
  @org.junit.Test
  public void opNumericEqualnpi2args5() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"-999999999999999999\") eq xs:nonPositiveInteger(\"0\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:nonPositiveInteger(lower bound) $arg2 = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualnpi2args6() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"-999999999999999999\") ne xs:nonPositiveInteger(\"-999999999999999999\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:nonPositiveInteger(mid range) $arg2 = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualnpi2args7() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"-475688437271870490\") ne xs:nonPositiveInteger(\"-999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:nonPositiveInteger(upper bound) $arg2 = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualnpi2args8() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"0\") ne xs:nonPositiveInteger(\"-999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:nonPositiveInteger(lower bound) $arg2 = xs:nonPositiveInteger(mid range) .
   */
  @org.junit.Test
  public void opNumericEqualnpi2args9() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"-999999999999999999\") ne xs:nonPositiveInteger(\"-475688437271870490\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:positiveInteger(lower bound) $arg2 = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualpint2args1() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"1\") eq xs:positiveInteger(\"1\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:positiveInteger(lower bound) $arg2 = xs:positiveInteger(upper bound) .
   */
  @org.junit.Test
  public void opNumericEqualpint2args10() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"1\") ne xs:positiveInteger(\"999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:positiveInteger(mid range) $arg2 = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualpint2args2() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"52704602390610033\") eq xs:positiveInteger(\"1\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:positiveInteger(upper bound) $arg2 = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualpint2args3() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"999999999999999999\") eq xs:positiveInteger(\"1\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:positiveInteger(lower bound) $arg2 = xs:positiveInteger(mid range) .
   */
  @org.junit.Test
  public void opNumericEqualpint2args4() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"1\") eq xs:positiveInteger(\"52704602390610033\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:positiveInteger(lower bound) $arg2 = xs:positiveInteger(upper bound) .
   */
  @org.junit.Test
  public void opNumericEqualpint2args5() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"1\") eq xs:positiveInteger(\"999999999999999999\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:positiveInteger(lower bound) $arg2 = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualpint2args6() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"1\") ne xs:positiveInteger(\"1\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:positiveInteger(mid range) $arg2 = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualpint2args7() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"52704602390610033\") ne xs:positiveInteger(\"1\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:positiveInteger(upper bound) $arg2 = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualpint2args8() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"999999999999999999\") ne xs:positiveInteger(\"1\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:positiveInteger(lower bound) $arg2 = xs:positiveInteger(mid range) .
   */
  @org.junit.Test
  public void opNumericEqualpint2args9() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"1\") ne xs:positiveInteger(\"52704602390610033\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:short(lower bound) $arg2 = xs:short(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualsht2args1() {
    final XQuery query = new XQuery(
      "xs:short(\"-32768\") eq xs:short(\"-32768\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:short(lower bound) $arg2 = xs:short(upper bound) .
   */
  @org.junit.Test
  public void opNumericEqualsht2args10() {
    final XQuery query = new XQuery(
      "xs:short(\"-32768\") ne xs:short(\"32767\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:short(mid range) $arg2 = xs:short(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualsht2args2() {
    final XQuery query = new XQuery(
      "xs:short(\"-5324\") eq xs:short(\"-32768\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:short(upper bound) $arg2 = xs:short(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualsht2args3() {
    final XQuery query = new XQuery(
      "xs:short(\"32767\") eq xs:short(\"-32768\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:short(lower bound) $arg2 = xs:short(mid range) .
   */
  @org.junit.Test
  public void opNumericEqualsht2args4() {
    final XQuery query = new XQuery(
      "xs:short(\"-32768\") eq xs:short(\"-5324\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:short(lower bound) $arg2 = xs:short(upper bound) .
   */
  @org.junit.Test
  public void opNumericEqualsht2args5() {
    final XQuery query = new XQuery(
      "xs:short(\"-32768\") eq xs:short(\"32767\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:short(lower bound) $arg2 = xs:short(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualsht2args6() {
    final XQuery query = new XQuery(
      "xs:short(\"-32768\") ne xs:short(\"-32768\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:short(mid range) $arg2 = xs:short(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualsht2args7() {
    final XQuery query = new XQuery(
      "xs:short(\"-5324\") ne xs:short(\"-32768\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:short(upper bound) $arg2 = xs:short(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualsht2args8() {
    final XQuery query = new XQuery(
      "xs:short(\"32767\") ne xs:short(\"-32768\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:short(lower bound) $arg2 = xs:short(mid range) .
   */
  @org.junit.Test
  public void opNumericEqualsht2args9() {
    final XQuery query = new XQuery(
      "xs:short(\"-32768\") ne xs:short(\"-5324\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:unsignedLong(lower bound) $arg2 = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualulng2args1() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"0\") eq xs:unsignedLong(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:unsignedLong(lower bound) $arg2 = xs:unsignedLong(upper bound) .
   */
  @org.junit.Test
  public void opNumericEqualulng2args10() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"0\") ne xs:unsignedLong(\"184467440737095516\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:unsignedLong(mid range) $arg2 = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualulng2args2() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"130747108607674654\") eq xs:unsignedLong(\"0\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:unsignedLong(upper bound) $arg2 = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualulng2args3() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"184467440737095516\") eq xs:unsignedLong(\"0\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:unsignedLong(lower bound) $arg2 = xs:unsignedLong(mid range) .
   */
  @org.junit.Test
  public void opNumericEqualulng2args4() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"0\") eq xs:unsignedLong(\"130747108607674654\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:unsignedLong(lower bound) $arg2 = xs:unsignedLong(upper bound) .
   */
  @org.junit.Test
  public void opNumericEqualulng2args5() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"0\") eq xs:unsignedLong(\"184467440737095516\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:unsignedLong(lower bound) $arg2 = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualulng2args6() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"0\") ne xs:unsignedLong(\"0\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:unsignedLong(mid range) $arg2 = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualulng2args7() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"130747108607674654\") ne xs:unsignedLong(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:unsignedLong(upper bound) $arg2 = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualulng2args8() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"184467440737095516\") ne xs:unsignedLong(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:unsignedLong(lower bound) $arg2 = xs:unsignedLong(mid range) .
   */
  @org.junit.Test
  public void opNumericEqualulng2args9() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"0\") ne xs:unsignedLong(\"130747108607674654\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:unsignedShort(lower bound) $arg2 = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualusht2args1() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"0\") eq xs:unsignedShort(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:unsignedShort(lower bound) $arg2 = xs:unsignedShort(upper bound) .
   */
  @org.junit.Test
  public void opNumericEqualusht2args10() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"0\") ne xs:unsignedShort(\"65535\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:unsignedShort(mid range) $arg2 = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualusht2args2() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"44633\") eq xs:unsignedShort(\"0\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:unsignedShort(upper bound) $arg2 = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualusht2args3() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"65535\") eq xs:unsignedShort(\"0\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:unsignedShort(lower bound) $arg2 = xs:unsignedShort(mid range) .
   */
  @org.junit.Test
  public void opNumericEqualusht2args4() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"0\") eq xs:unsignedShort(\"44633\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:unsignedShort(lower bound) $arg2 = xs:unsignedShort(upper bound) .
   */
  @org.junit.Test
  public void opNumericEqualusht2args5() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"0\") eq xs:unsignedShort(\"65535\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:unsignedShort(lower bound) $arg2 = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualusht2args6() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"0\") ne xs:unsignedShort(\"0\")",
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
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:unsignedShort(mid range) $arg2 = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualusht2args7() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"44633\") ne xs:unsignedShort(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:unsignedShort(upper bound) $arg2 = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void opNumericEqualusht2args8() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"65535\") ne xs:unsignedShort(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:numeric-equal" operator with the arguments set as follows: $arg1 = xs:unsignedShort(lower bound) $arg2 = xs:unsignedShort(mid range) .
   */
  @org.junit.Test
  public void opNumericEqualusht2args9() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"0\") ne xs:unsignedShort(\"44633\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }
}
