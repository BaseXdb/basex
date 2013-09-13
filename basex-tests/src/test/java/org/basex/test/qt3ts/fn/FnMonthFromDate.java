package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the month-from-date() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnMonthFromDate extends QT3TestSet {

  /**
   *  A test whose essence is: `month-from-date()`. .
   */
  @org.junit.Test
  public void kMonthFromDateFunc1() {
    final XQuery query = new XQuery(
      "month-from-date()",
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
   *  A test whose essence is: `month-from-date((), "Wrong param")`. .
   */
  @org.junit.Test
  public void kMonthFromDateFunc2() {
    final XQuery query = new XQuery(
      "month-from-date((), \"Wrong param\")",
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
   *  A test whose essence is: `empty(month-from-date(()))`. .
   */
  @org.junit.Test
  public void kMonthFromDateFunc3() {
    final XQuery query = new XQuery(
      "empty(month-from-date(()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `month-from-date(()) instance of xs:integer?`. .
   */
  @org.junit.Test
  public void kMonthFromDateFunc4() {
    final XQuery query = new XQuery(
      "month-from-date(()) instance of xs:integer?",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `month-from-date(xs:date("2000-02-03")) eq 2`. .
   */
  @org.junit.Test
  public void kMonthFromDateFunc5() {
    final XQuery query = new XQuery(
      "month-from-date(xs:date(\"2000-02-03\")) eq 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "month-from-date" function As per example 1 of the F&O specs .
   */
  @org.junit.Test
  public void fnMonthFromDate1() {
    final XQuery query = new XQuery(
      "fn:month-from-date(xs:date(\"1999-05-31-05:00\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "5")
    );
  }

  /**
   *  Evaluates The "month-from-date" function as part of a "*" expression. .
   */
  @org.junit.Test
  public void fnMonthFromDate10() {
    final XQuery query = new XQuery(
      "fn:month-from-date(xs:date(\"1970-01-01Z\")) * fn:month-from-date(xs:date(\"0002-01-01Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  Evaluates The "month-from-date" function as part of a "div" expression. .
   */
  @org.junit.Test
  public void fnMonthFromDate11() {
    final XQuery query = new XQuery(
      "fn:month-from-date(xs:date(\"1970-01-01Z\")) div fn:month-from-date(xs:date(\"1970-01-01Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  Evaluates The "month-from-date" function as part of a "idiv" expression. .
   */
  @org.junit.Test
  public void fnMonthFromDate12() {
    final XQuery query = new XQuery(
      "fn:month-from-date(xs:date(\"1970-01-01Z\")) idiv fn:month-from-date(xs:date(\"1970-01-01Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  Evaluates The "month-from-date" function as part of a "mod" expression. .
   */
  @org.junit.Test
  public void fnMonthFromDate13() {
    final XQuery query = new XQuery(
      "fn:month-from-date(xs:date(\"1970-01-01Z\")) mod fn:month-from-date(xs:date(\"1970-01-01Z\"))",
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
   *  Evaluates The "month-from-date" function as part of a "numeric-unary-plus" expression. .
   */
  @org.junit.Test
  public void fnMonthFromDate14() {
    final XQuery query = new XQuery(
      "+fn:month-from-date(xs:date(\"1970-01-01Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  Evaluates The "month-from-date" function as part of a "numeric-unary-minus" expression. .
   */
  @org.junit.Test
  public void fnMonthFromDate15() {
    final XQuery query = new XQuery(
      "-fn:month-from-date(xs:date(\"1970-01-01Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-1")
    );
  }

  /**
   *  Evaluates The "month-from-date" function as part of a "numeric-equal" expression (eq operator) .
   */
  @org.junit.Test
  public void fnMonthFromDate16() {
    final XQuery query = new XQuery(
      "fn:month-from-date(xs:date(\"1970-01-01Z\")) eq fn:month-from-date(xs:date(\"1970-01-01Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "month-from-date" function as part of a "numeric-equal" expression (ne operator) .
   */
  @org.junit.Test
  public void fnMonthFromDate17() {
    final XQuery query = new XQuery(
      "fn:month-from-date(xs:date(\"1970-01-01Z\")) ne fn:month-from-date(xs:date(\"1970-01-01Z\"))",
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
   *  Evaluates The "month-from-date" function as part of a "numeric-equal" expression (le operator) .
   */
  @org.junit.Test
  public void fnMonthFromDate18() {
    final XQuery query = new XQuery(
      "fn:month-from-date(xs:date(\"1970-01-01Z\")) le fn:month-from-date(xs:date(\"1970-01-01Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "month-from-date" function as part of a "numeric-equal" expression (ge operator) .
   */
  @org.junit.Test
  public void fnMonthFromDate19() {
    final XQuery query = new XQuery(
      "fn:month-from-date(xs:date(\"1970-01-01Z\")) ge fn:month-from-date(xs:date(\"1970-01-01Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "month-from-date" function As per example 2 of the F&O specs .
   */
  @org.junit.Test
  public void fnMonthFromDate2() {
    final XQuery query = new XQuery(
      "fn:month-from-date(xs:date(\"2000-01-01+05:00\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  Evaluates The "month-from-date" function involving a "numeric-less-than" operation (lt operator) .
   */
  @org.junit.Test
  public void fnMonthFromDate3() {
    final XQuery query = new XQuery(
      "fn:month-from-date(xs:date(\"1999-12-31Z\")) lt fn:month-from-date(xs:date(\"1999-12-31Z\"))",
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
   *  Evaluates The "month-from-date" function involving a "numeric-less-than" operation (le operator) .
   */
  @org.junit.Test
  public void fnMonthFromDate4() {
    final XQuery query = new XQuery(
      "fn:month-from-date(xs:date(\"1999-12-31Z\")) le fn:month-from-date(xs:date(\"1999-12-31Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "month-from-date" function using the empty sequence as an argument. Use count function to avoid empty file. .
   */
  @org.junit.Test
  public void fnMonthFromDate5() {
    final XQuery query = new XQuery(
      "fn:count(fn:month-from-date(()))",
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
   *  Evaluates The "month-from-date" function that returns 1. .
   */
  @org.junit.Test
  public void fnMonthFromDate6() {
    final XQuery query = new XQuery(
      "fn:month-from-date(xs:date(\"1970-01-31Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  Evaluates The "month-from-date" function that returns 12. .
   */
  @org.junit.Test
  public void fnMonthFromDate7() {
    final XQuery query = new XQuery(
      "fn:month-from-date(xs:date(\"1999-12-31Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "12")
    );
  }

  /**
   *  Evaluates The "month-from-date" function as part of a "+" expression. .
   */
  @org.junit.Test
  public void fnMonthFromDate8() {
    final XQuery query = new XQuery(
      "fn:month-from-date(xs:date(\"1970-01-01Z\")) + fn:month-from-date(xs:date(\"1970-01-01Z\"))",
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
   *  Evaluates The "month-from-date" function as part of a "-" expression. .
   */
  @org.junit.Test
  public void fnMonthFromDate9() {
    final XQuery query = new XQuery(
      "fn:month-from-date(xs:date(\"1970-01-01Z\")) - fn:month-from-date(xs:date(\"1970-01-01Z\"))",
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
   *  Evaluates The "month-from-date" function with the arguments set as follows: $arg = xs:date(lower bound) .
   */
  @org.junit.Test
  public void fnMonthFromDate1args1() {
    final XQuery query = new XQuery(
      "fn:month-from-date(xs:date(\"1970-01-01Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  Evaluates The "month-from-date" function with the arguments set as follows: $arg = xs:date(mid range) .
   */
  @org.junit.Test
  public void fnMonthFromDate1args2() {
    final XQuery query = new XQuery(
      "fn:month-from-date(xs:date(\"1983-11-17Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "11")
    );
  }

  /**
   *  Evaluates The "month-from-date" function with the arguments set as follows: $arg = xs:date(upper bound) .
   */
  @org.junit.Test
  public void fnMonthFromDate1args3() {
    final XQuery query = new XQuery(
      "fn:month-from-date(xs:date(\"2030-12-31Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "12")
    );
  }
}
