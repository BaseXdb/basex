package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the year-from-date() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnYearFromDate extends QT3TestSet {

  /**
   *  A test whose essence is: `year-from-date()`. .
   */
  @org.junit.Test
  public void kYearFromDateFunc1() {
    final XQuery query = new XQuery(
      "year-from-date()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `year-from-date((), "Wrong param")`. .
   */
  @org.junit.Test
  public void kYearFromDateFunc2() {
    final XQuery query = new XQuery(
      "year-from-date((), \"Wrong param\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `empty(year-from-date(()))`. .
   */
  @org.junit.Test
  public void kYearFromDateFunc3() {
    final XQuery query = new XQuery(
      "empty(year-from-date(()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `year-from-date(()) instance of xs:integer?`. .
   */
  @org.junit.Test
  public void kYearFromDateFunc4() {
    final XQuery query = new XQuery(
      "year-from-date(()) instance of xs:integer?",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `year-from-date(xs:date("2000-02-03")) eq 2000`. .
   */
  @org.junit.Test
  public void kYearFromDateFunc5() {
    final XQuery query = new XQuery(
      "year-from-date(xs:date(\"2000-02-03\")) eq 2000",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "year-from-date" function As per example 1 of the F&O specs .
   */
  @org.junit.Test
  public void fnYearFromDate1() {
    final XQuery query = new XQuery(
      "fn:year-from-date(xs:date(\"1999-05-31Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1999")
    );
  }

  /**
   *  Evaluates The "year-from-date" function as part of a "*" expression. .
   */
  @org.junit.Test
  public void fnYearFromDate10() {
    final XQuery query = new XQuery(
      "fn:year-from-date(xs:date(\"1970-01-01Z\")) * fn:year-from-date(xs:date(\"0002-01-01Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "3940")
    );
  }

  /**
   *  Evaluates The "year-from-date" function as part of a "div" expression. .
   */
  @org.junit.Test
  public void fnYearFromDate11() {
    final XQuery query = new XQuery(
      "fn:year-from-date(xs:date(\"1970-01-01Z\")) div fn:year-from-date(xs:date(\"1970-01-01Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  Evaluates The "year-from-date" function as part of a "idiv" expression. .
   */
  @org.junit.Test
  public void fnYearFromDate12() {
    final XQuery query = new XQuery(
      "fn:year-from-date(xs:date(\"1970-01-01Z\")) idiv fn:year-from-date(xs:date(\"1970-01-01Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  Evaluates The "year-from-date" function as part of a "mod" expression. .
   */
  @org.junit.Test
  public void fnYearFromDate13() {
    final XQuery query = new XQuery(
      "fn:year-from-date(xs:date(\"1970-01-01Z\")) mod fn:year-from-date(xs:date(\"1970-01-01Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluates The "year-from-date" function as part of a "numeric-unary-plus" expression. .
   */
  @org.junit.Test
  public void fnYearFromDate14() {
    final XQuery query = new XQuery(
      "+fn:year-from-date(xs:date(\"1970-01-01Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1970")
    );
  }

  /**
   *  Evaluates The "year-from-date" function as part of a "numeric-unary-minus" expression. .
   */
  @org.junit.Test
  public void fnYearFromDate15() {
    final XQuery query = new XQuery(
      "-fn:year-from-date(xs:date(\"1970-01-01Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-1970")
    );
  }

  /**
   *  Evaluates The "year-from-date" function as part of a "numeric-equal" expression (eq operator) .
   */
  @org.junit.Test
  public void fnYearFromDate16() {
    final XQuery query = new XQuery(
      "fn:year-from-date(xs:date(\"1970-01-01Z\")) eq fn:year-from-date(xs:date(\"1970-01-01Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "year-from-date" function as part of a "numeric-equal" expression (ne operator) .
   */
  @org.junit.Test
  public void fnYearFromDate17() {
    final XQuery query = new XQuery(
      "fn:year-from-date(xs:date(\"1970-01-01Z\")) ne fn:year-from-date(xs:date(\"1970-01-01Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "year-from-date" function as part of a "numeric-equal" expression (le operator) .
   */
  @org.junit.Test
  public void fnYearFromDate18() {
    final XQuery query = new XQuery(
      "fn:year-from-date(xs:date(\"1970-01-01Z\")) le fn:year-from-date(xs:date(\"1970-01-01Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "year-from-date" function as part of a "numeric-equal" expression (ge operator) .
   */
  @org.junit.Test
  public void fnYearFromDate19() {
    final XQuery query = new XQuery(
      "fn:year-from-date(xs:date(\"1970-01-01Z\")) ge fn:year-from-date(xs:date(\"1970-01-01Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "year-from-date" function As per example 2 of the F&O specs .
   */
  @org.junit.Test
  public void fnYearFromDate2() {
    final XQuery query = new XQuery(
      "fn:year-from-date(xs:date(\"2000-01-01Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2000")
    );
  }

  /**
   *  Evaluates The "year-from-date" function involving a "numeric-less-than" operation (lt operator) .
   */
  @org.junit.Test
  public void fnYearFromDate3() {
    final XQuery query = new XQuery(
      "fn:year-from-date(xs:date(\"1999-12-31Z\")) lt fn:year-from-date(xs:date(\"1999-12-31Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "year-from-date" function involving a "numeric-less-than" operation (le operator) .
   */
  @org.junit.Test
  public void fnYearFromDate4() {
    final XQuery query = new XQuery(
      "fn:year-from-date(xs:date(\"1999-12-31Z\")) le fn:year-from-date(xs:date(\"1999-12-31Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "year-from-date" function using the empty sequence as an argument. Use count function to avoid empty file. .
   */
  @org.junit.Test
  public void fnYearFromDate5() {
    final XQuery query = new XQuery(
      "fn:count(fn:year-from-date(()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluates The "year-from-date" function that returns 1. .
   */
  @org.junit.Test
  public void fnYearFromDate6() {
    final XQuery query = new XQuery(
      "fn:year-from-date(xs:date(\"0001-05-31Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  Evaluates The "year-from-date" function that returns a negative number .
   */
  @org.junit.Test
  public void fnYearFromDate7() {
    final XQuery query = new XQuery(
      "fn:year-from-date(xs:date(\"-1999-05-31Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-1999")
    );
  }

  /**
   *  Evaluates The "year-from-date" function as part of a "+" expression. .
   */
  @org.junit.Test
  public void fnYearFromDate8() {
    final XQuery query = new XQuery(
      "fn:year-from-date(xs:date(\"1970-01-01Z\")) + fn:year-from-date(xs:date(\"1970-01-01Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "3940")
    );
  }

  /**
   *  Evaluates The "year-from-date" function as part of a "-" expression. .
   */
  @org.junit.Test
  public void fnYearFromDate9() {
    final XQuery query = new XQuery(
      "fn:year-from-date(xs:date(\"1970-01-01Z\")) - fn:year-from-date(xs:date(\"1970-01-01Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluates The "year-from-date" function with the arguments set as follows: $arg = xs:date(lower bound) .
   */
  @org.junit.Test
  public void fnYearFromDate1args1() {
    final XQuery query = new XQuery(
      "fn:year-from-date(xs:date(\"1970-01-01Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1970")
    );
  }

  /**
   *  Evaluates The "year-from-date" function with the arguments set as follows: $arg = xs:date(mid range) .
   */
  @org.junit.Test
  public void fnYearFromDate1args2() {
    final XQuery query = new XQuery(
      "fn:year-from-date(xs:date(\"1983-11-17Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1983")
    );
  }

  /**
   *  Evaluates The "year-from-date" function with the arguments set as follows: $arg = xs:date(upper bound) .
   */
  @org.junit.Test
  public void fnYearFromDate1args3() {
    final XQuery query = new XQuery(
      "fn:year-from-date(xs:date(\"2030-12-31Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2030")
    );
  }
}
