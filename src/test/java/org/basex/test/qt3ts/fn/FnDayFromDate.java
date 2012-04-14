package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the day-from-date() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnDayFromDate extends QT3TestSet {

  /**
   *  A test whose essence is: `day-from-date()`. .
   */
  @org.junit.Test
  public void kDayFromDateFunc1() {
    final XQuery query = new XQuery(
      "day-from-date()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `day-from-date((), "Wrong param")`. .
   */
  @org.junit.Test
  public void kDayFromDateFunc2() {
    final XQuery query = new XQuery(
      "day-from-date((), \"Wrong param\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `empty(day-from-date(()))`. .
   */
  @org.junit.Test
  public void kDayFromDateFunc3() {
    final XQuery query = new XQuery(
      "empty(day-from-date(()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `day-from-date(()) instance of xs:integer?`. .
   */
  @org.junit.Test
  public void kDayFromDateFunc4() {
    final XQuery query = new XQuery(
      "day-from-date(()) instance of xs:integer?",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `day-from-date(xs:date("2000-02-03")) eq 3`. .
   */
  @org.junit.Test
  public void kDayFromDateFunc5() {
    final XQuery query = new XQuery(
      "day-from-date(xs:date(\"2000-02-03\")) eq 3",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "day-from-date" function As per example 1 of the F&O specs .
   */
  @org.junit.Test
  public void fnDayFromDate1() {
    final XQuery query = new XQuery(
      "fn:day-from-date(xs:date(\"1999-05-31-05:00\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "31")
    );
  }

  /**
   *  Evaluates The "day-from-date" function as part of a "*" expression. .
   */
  @org.junit.Test
  public void fnDayFromDate10() {
    final XQuery query = new XQuery(
      "fn:day-from-date(xs:date(\"1970-01-03Z\")) * fn:day-from-date(xs:date(\"0002-01-01Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "3")
    );
  }

  /**
   *  Evaluates The "day-from-date" function as part of a "div" expression. .
   */
  @org.junit.Test
  public void fnDayFromDate11() {
    final XQuery query = new XQuery(
      "fn:day-from-date(xs:date(\"1970-01-02Z\")) div fn:day-from-date(xs:date(\"1970-01-01Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2")
    );
  }

  /**
   *  Evaluates The "day-from-date" function as part of a "idiv" expression. .
   */
  @org.junit.Test
  public void fnDayFromDate12() {
    final XQuery query = new XQuery(
      "fn:day-from-date(xs:date(\"1970-01-02Z\")) idiv fn:day-from-date(xs:date(\"1970-01-01Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2")
    );
  }

  /**
   *  Evaluates The "day-from-date" function as part of a "mod" expression. .
   */
  @org.junit.Test
  public void fnDayFromDate13() {
    final XQuery query = new XQuery(
      "fn:day-from-date(xs:date(\"1970-01-02Z\")) mod fn:day-from-date(xs:date(\"1970-01-01Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluates The "day-from-date" function as part of a "numeric-unary-plus" expression. .
   */
  @org.junit.Test
  public void fnDayFromDate14() {
    final XQuery query = new XQuery(
      "+fn:day-from-date(xs:date(\"1970-01-01Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  Evaluates The "day-from-date" function as part of a "numeric-unary-minus" expression. .
   */
  @org.junit.Test
  public void fnDayFromDate15() {
    final XQuery query = new XQuery(
      "-fn:day-from-date(xs:date(\"1970-01-01Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-1")
    );
  }

  /**
   *  Evaluates The "day-from-date" function as part of a "numeric-equal" expression (eq operator) .
   */
  @org.junit.Test
  public void fnDayFromDate16() {
    final XQuery query = new XQuery(
      "fn:day-from-date(xs:date(\"1970-01-02Z\")) eq fn:day-from-date(xs:date(\"1970-01-01Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "month-from-date" function as part of a "numeric-equal" expression (ne operator) .
   */
  @org.junit.Test
  public void fnDayFromDate17() {
    final XQuery query = new XQuery(
      "fn:month-from-date(xs:date(\"1970-01-01Z\")) ne fn:month-from-date(xs:date(\"1970-01-03Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "day-from-date" function as part of a "numeric-equal" expression (le operator) .
   */
  @org.junit.Test
  public void fnDayFromDate18() {
    final XQuery query = new XQuery(
      "fn:day-from-date(xs:date(\"1970-01-01Z\")) le fn:day-from-date(xs:date(\"1970-01-02Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "day-from-date" function as part of a "numeric-equal" expression (ge operator) .
   */
  @org.junit.Test
  public void fnDayFromDate19() {
    final XQuery query = new XQuery(
      "fn:day-from-date(xs:date(\"1970-01-03Z\")) ge fn:day-from-date(xs:date(\"1970-01-01Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "day-from-date" function As per example 2 of the F&O specs .
   */
  @org.junit.Test
  public void fnDayFromDate2() {
    final XQuery query = new XQuery(
      "fn:day-from-date(xs:date(\"2000-01-01+05:00\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  Evaluates The "day-from-date" function involving a "numeric-less-than" operation (lt operator) .
   */
  @org.junit.Test
  public void fnDayFromDate3() {
    final XQuery query = new XQuery(
      "fn:day-from-date(xs:date(\"1999-12-31Z\")) lt fn:day-from-date(xs:date(\"1999-12-31Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "day-from-date" function involving a "numeric-less-than" operation (le operator) .
   */
  @org.junit.Test
  public void fnDayFromDate4() {
    final XQuery query = new XQuery(
      "fn:day-from-date(xs:date(\"1999-12-31Z\")) le fn:day-from-date(xs:date(\"1999-12-31Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "day-from-date" function using the empty sequence as an argument. Use count function to avoid empty file. .
   */
  @org.junit.Test
  public void fnDayFromDate5() {
    final XQuery query = new XQuery(
      "fn:count(fn:day-from-date(()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluates The "day-from-date" function that returns 31. .
   */
  @org.junit.Test
  public void fnDayFromDate6() {
    final XQuery query = new XQuery(
      "fn:day-from-date(xs:date(\"1999-05-31Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "31")
    );
  }

  /**
   *  Evaluates The "day-from-date" function used as arguments to an avg function . .
   */
  @org.junit.Test
  public void fnDayFromDate7() {
    final XQuery query = new XQuery(
      "fn:avg((fn:day-from-date(xs:date(\"1999-12-31Z\")),fn:day-from-date(xs:date(\"1999-12-29Z\"))))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "30")
    );
  }

  /**
   *  Evaluates The "day-from-date" function as part of a "+" expression. .
   */
  @org.junit.Test
  public void fnDayFromDate8() {
    final XQuery query = new XQuery(
      "fn:day-from-date(xs:date(\"1970-01-01Z\")) + fn:day-from-date(xs:date(\"1970-01-01Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2")
    );
  }

  /**
   *  Evaluates The "day-from-date" function as part of a "-" expression. .
   */
  @org.junit.Test
  public void fnDayFromDate9() {
    final XQuery query = new XQuery(
      "fn:day-from-date(xs:date(\"1970-01-01Z\")) - fn:day-from-date(xs:date(\"1970-01-01Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluates The "day-from-date" function with the arguments set as follows: $arg = xs:date(lower bound) .
   */
  @org.junit.Test
  public void fnDayFromDate1args1() {
    final XQuery query = new XQuery(
      "fn:day-from-date(xs:date(\"1970-01-01Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  Evaluates The "day-from-date" function with the arguments set as follows: $arg = xs:date(mid range) .
   */
  @org.junit.Test
  public void fnDayFromDate1args2() {
    final XQuery query = new XQuery(
      "fn:day-from-date(xs:date(\"1983-11-17Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "17")
    );
  }

  /**
   *  Evaluates The "day-from-date" function with the arguments set as follows: $arg = xs:date(upper bound) .
   */
  @org.junit.Test
  public void fnDayFromDate1args3() {
    final XQuery query = new XQuery(
      "fn:day-from-date(xs:date(\"2030-12-31Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "31")
    );
  }
}
