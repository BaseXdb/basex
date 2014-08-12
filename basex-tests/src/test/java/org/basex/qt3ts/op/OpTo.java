package org.basex.qt3ts.op;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the to() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpTo extends QT3TestSet {

  /**
   *  Since the left operand has the static cardinality zero-or-more, implementations using the static typing feature may raise XPTY0004. .
   */
  @org.junit.Test
  public void kRangeExpr1() {
    final XQuery query = new XQuery(
      "1 to 1 eq 1",
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
   *  A test whose essence is: `count((10, 1 to 4)) eq 5`. .
   */
  @org.junit.Test
  public void kRangeExpr10() {
    final XQuery query = new XQuery(
      "count((10, 1 to 4)) eq 5",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `subsequence(-3 to -1, 1, 1) eq -3`. .
   */
  @org.junit.Test
  public void kRangeExpr11() {
    final XQuery query = new XQuery(
      "subsequence(-3 to -1, 1, 1) eq -3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `subsequence(-3 to -1, 3, 1) eq -1`. .
   */
  @org.junit.Test
  public void kRangeExpr12() {
    final XQuery query = new XQuery(
      "subsequence(-3 to -1, 3, 1) eq -1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty(reverse(4 to 1))`. .
   */
  @org.junit.Test
  public void kRangeExpr13() {
    final XQuery query = new XQuery(
      "empty(reverse(4 to 1))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `subsequence(reverse(1 to 3), 1, 1) eq 3`. .
   */
  @org.junit.Test
  public void kRangeExpr14() {
    final XQuery query = new XQuery(
      "subsequence(reverse(1 to 3), 1, 1) eq 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `subsequence(reverse(1 to 3), 3, 1) eq 1`. .
   */
  @org.junit.Test
  public void kRangeExpr15() {
    final XQuery query = new XQuery(
      "subsequence(reverse(1 to 3), 3, 1) eq 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `subsequence(reverse(1 to 4), 2, 1) eq 3`. .
   */
  @org.junit.Test
  public void kRangeExpr16() {
    final XQuery query = new XQuery(
      "subsequence(reverse(1 to 4), 2, 1) eq 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `subsequence(reverse(1 to 4), 3, 1) eq 2`. .
   */
  @org.junit.Test
  public void kRangeExpr17() {
    final XQuery query = new XQuery(
      "subsequence(reverse(1 to 4), 3, 1) eq 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `subsequence(reverse(-4 to -1), 2, 1) eq -2`. .
   */
  @org.junit.Test
  public void kRangeExpr18() {
    final XQuery query = new XQuery(
      "subsequence(reverse(-4 to -1), 2, 1) eq -2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal((-1, -2, -3, -4), reverse(-4 to -1))`. .
   */
  @org.junit.Test
  public void kRangeExpr19() {
    final XQuery query = new XQuery(
      "deep-equal((-1, -2, -3, -4), reverse(-4 to -1))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty(30 to 3)`. .
   */
  @org.junit.Test
  public void kRangeExpr2() {
    final XQuery query = new XQuery(
      "empty(30 to 3)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal((), reverse(0 to -5))`. .
   */
  @org.junit.Test
  public void kRangeExpr20() {
    final XQuery query = new XQuery(
      "deep-equal((), reverse(0 to -5))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal((0, -1, -2, -3, -4, -5), reverse(-5 to 0))`. .
   */
  @org.junit.Test
  public void kRangeExpr21() {
    final XQuery query = new XQuery(
      "deep-equal((0, -1, -2, -3, -4, -5), reverse(-5 to 0))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(reverse(-5 to -2)) eq 4`. .
   */
  @org.junit.Test
  public void kRangeExpr22() {
    final XQuery query = new XQuery(
      "count(reverse(-5 to -2)) eq 4",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(reverse(-5 to -0)) eq 6`. .
   */
  @org.junit.Test
  public void kRangeExpr23() {
    final XQuery query = new XQuery(
      "count(reverse(-5 to -0)) eq 6",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(reverse(1 to 4)) eq 4`. .
   */
  @org.junit.Test
  public void kRangeExpr24() {
    final XQuery query = new XQuery(
      "count(reverse(1 to 4)) eq 4",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty(1 to 0)`. .
   */
  @org.junit.Test
  public void kRangeExpr25() {
    final XQuery query = new XQuery(
      "empty(1 to 0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty(0 to -5)`. .
   */
  @org.junit.Test
  public void kRangeExpr26() {
    final XQuery query = new XQuery(
      "empty(0 to -5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty(-4 to -5)`. .
   */
  @org.junit.Test
  public void kRangeExpr27() {
    final XQuery query = new XQuery(
      "empty(-4 to -5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty(reverse(1 to 0))`. .
   */
  @org.junit.Test
  public void kRangeExpr28() {
    final XQuery query = new XQuery(
      "empty(reverse(1 to 0))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty(reverse(0 to -5))`. .
   */
  @org.junit.Test
  public void kRangeExpr29() {
    final XQuery query = new XQuery(
      "empty(reverse(0 to -5))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty(0 to -3)`. .
   */
  @org.junit.Test
  public void kRangeExpr3() {
    final XQuery query = new XQuery(
      "empty(0 to -3)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty(reverse(-4 to -5))`. .
   */
  @org.junit.Test
  public void kRangeExpr30() {
    final XQuery query = new XQuery(
      "empty(reverse(-4 to -5))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `(remove((2.e0, 4), 1) treat as xs:integer to 4)`. .
   */
  @org.junit.Test
  public void kRangeExpr31() {
    final XQuery query = new XQuery(
      "(remove((2.e0, 4), 1) treat as xs:integer to 4)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "4")
    );
  }

  /**
   *  A test whose essence is: `(4 to remove((2e0, 4), 1) treat as xs:integer)`. .
   */
  @org.junit.Test
  public void kRangeExpr32() {
    final XQuery query = new XQuery(
      "(4 to remove((2e0, 4), 1) treat as xs:integer)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "4")
    );
  }

  /**
   *  A test whose essence is: `1.1 to 3`. .
   */
  @org.junit.Test
  public void kRangeExpr33() {
    final XQuery query = new XQuery(
      "1.1 to 3",
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
   *  A test whose essence is: `3 to 1.1`. .
   */
  @org.junit.Test
  public void kRangeExpr34() {
    final XQuery query = new XQuery(
      "3 to 1.1",
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
   *  A test whose essence is: `1.1 to 3.3`. .
   */
  @org.junit.Test
  public void kRangeExpr35() {
    final XQuery query = new XQuery(
      "1.1 to 3.3",
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
   *  A test whose essence is: `1 + 1.1 to 5`. .
   */
  @org.junit.Test
  public void kRangeExpr36() {
    final XQuery query = new XQuery(
      "1 + 1.1 to 5",
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
   *  A test whose essence is: `empty(1 to ())`. .
   */
  @org.junit.Test
  public void kRangeExpr4() {
    final XQuery query = new XQuery(
      "empty(1 to ())",
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
   *  A test whose essence is: `empty(() to 1)`. .
   */
  @org.junit.Test
  public void kRangeExpr5() {
    final XQuery query = new XQuery(
      "empty(() to 1)",
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
   *  A test whose essence is: `empty(-1 to -3)`. .
   */
  @org.junit.Test
  public void kRangeExpr6() {
    final XQuery query = new XQuery(
      "empty(-1 to -3)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(1 to 4) eq 4`. .
   */
  @org.junit.Test
  public void kRangeExpr7() {
    final XQuery query = new XQuery(
      "count(1 to 4) eq 4",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(0 to 4) eq 5`. .
   */
  @org.junit.Test
  public void kRangeExpr8() {
    final XQuery query = new XQuery(
      "count(0 to 4) eq 5",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(-5 to -0) eq 6`. .
   */
  @org.junit.Test
  public void kRangeExpr9() {
    final XQuery query = new XQuery(
      "count(-5 to -0) eq 6",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  The range expression doesn't accept xs:double as operand. .
   */
  @org.junit.Test
  public void k2RangeExpr1() {
    final XQuery query = new XQuery(
      "1e3 to 3",
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
   *  The range expression doesn't accept xs:double as operand. .
   */
  @org.junit.Test
  public void k2RangeExpr2() {
    final XQuery query = new XQuery(
      "3 to 1e3",
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
   *  Combine with a for expression. .
   */
  @org.junit.Test
  public void k2RangeExpr3() {
    final XQuery query = new XQuery(
      "for $i in 1 to 3 return $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2 3")
    );
  }

  /**
   *  Expressions that are tricky for local rewrites. .
   */
  @org.junit.Test
  public void k2RangeExpr4() {
    final XQuery query = new XQuery(
      "1 to <value>5</value>, 5 to <value>5</value>, <value>1</value> to 5, <value>1</value> to <value>5</value>, let $i := <e>5</e> return $i to $i, count(5 to 10), count(1000 to 2000), count(<e>5</e> to 10), count(3 to <e>10</e>), count(<e>3</e> to <e>10</e>), count(<e>5</e> to 10), count(3 to <e>10</e>), count(<e>3</e> to <e>10</e>), count(4294967295 to 4294967298)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2 3 4 5 5 1 2 3 4 5 1 2 3 4 5 5 6 1001 6 8 8 6 8 8 4")
    );
  }

  /**
   *  Evaluation of a single range expression using positive integers. .
   */
  @org.junit.Test
  public void rangeExpr1() {
    final XQuery query = new XQuery(
      "(10, 1 to 4)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "10 1 2 3 4")
    );
  }

  /**
   *  Evaluation of a range expression, where the second operand is "xs:integer" function. .
   */
  @org.junit.Test
  public void rangeExpr10() {
    final XQuery query = new XQuery(
      "(1 to xs:integer(5))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2 3 4 5")
    );
  }

  /**
   *  Evaluation of a range expression, where both operands are "xs:integer" functions. .
   */
  @org.junit.Test
  public void rangeExpr11() {
    final XQuery query = new XQuery(
      "(xs:integer(1) to xs:integer(5))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2 3 4 5")
    );
  }

  /**
   *  Evaluation of a range expression, using the "fn:min" function .
   */
  @org.junit.Test
  public void rangeExpr12() {
    final XQuery query = new XQuery(
      "(fn:min((1,2)) to 5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2 3 4 5")
    );
  }

  /**
   *  Evaluation of a range expression, using the "fn:max" function .
   */
  @org.junit.Test
  public void rangeExpr13() {
    final XQuery query = new XQuery(
      "(fn:max((1,2)) to 5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2 3 4 5")
    );
  }

  /**
   *  Evaluation of a range expression, using the "fn:min" and "fn:max" functions .
   */
  @org.junit.Test
  public void rangeExpr14() {
    final XQuery query = new XQuery(
      "(fn:min((1,2)) to fn:max((6,7)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2 3 4 5 6 7")
    );
  }

  /**
   *  Evaluation of a range expression as an argument to a "fn:min" function) .
   */
  @org.junit.Test
  public void rangeExpr15() {
    final XQuery query = new XQuery(
      "fn:min((1 to 5))",
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
   *  Evaluation of a range expression as an argument to a "fn:max" function) .
   */
  @org.junit.Test
  public void rangeExpr16() {
    final XQuery query = new XQuery(
      "fn:max((1 to 5))",
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
   *  Evaluation of a range expression as an argument to an "fn:avg" function) .
   */
  @org.junit.Test
  public void rangeExpr17() {
    final XQuery query = new XQuery(
      "fn:avg((1 to 5))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3")
    );
  }

  /**
   *  Evaluation of a range expression as an argument to an "fn:count" function) .
   */
  @org.junit.Test
  public void rangeExpr18() {
    final XQuery query = new XQuery(
      "fn:count((1 to 5))",
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
   *  Evaluation of a range expression, where the first operand is a multiplication operation. .
   */
  @org.junit.Test
  public void rangeExpr19() {
    final XQuery query = new XQuery(
      "((3*2) to 10)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "6 7 8 9 10")
    );
  }

  /**
   *  Evaluation of a range expression of length one containing the single integer 10. .
   */
  @org.junit.Test
  public void rangeExpr2() {
    final XQuery query = new XQuery(
      "10 to 10",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "10")
    );
  }

  /**
   *  Evaluation of a range expression, where the second operand is a multiplication operation. .
   */
  @org.junit.Test
  public void rangeExpr20() {
    final XQuery query = new XQuery(
      "(1 to (3*2))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2 3 4 5 6")
    );
  }

  /**
   *  Evaluation of a range expression, where both operands are multiplication operations. .
   */
  @org.junit.Test
  public void rangeExpr21() {
    final XQuery query = new XQuery(
      "((1*2) to (3*2))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2 3 4 5 6")
    );
  }

  /**
   *  Evaluation of a range expression, where the first operand is a subtraction operation. .
   */
  @org.junit.Test
  public void rangeExpr22() {
    final XQuery query = new XQuery(
      "((3 - 2) to 10)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2 3 4 5 6 7 8 9 10")
    );
  }

  /**
   *  Evaluation of a range expression, where the second operand is a subtraction operation. .
   */
  @org.junit.Test
  public void rangeExpr23() {
    final XQuery query = new XQuery(
      "(1 to (3 - 2))",
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
   *  Evaluation of a range expression, where both operands are subtraction operations. .
   */
  @org.junit.Test
  public void rangeExpr24() {
    final XQuery query = new XQuery(
      "((2 - 1) to (7 - 1))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2 3 4 5 6")
    );
  }

  /**
   *  Evaluation of a range expression, where the first operand is a division operation. .
   */
  @org.junit.Test
  public void rangeExpr25() {
    final XQuery query = new XQuery(
      "((6 idiv 2) to 10)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3 4 5 6 7 8 9 10")
    );
  }

  /**
   *  Evaluation of a range expression, where the second operand is a division operation. .
   */
  @org.junit.Test
  public void rangeExpr26() {
    final XQuery query = new XQuery(
      "(1 to (10 idiv 2))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2 3 4 5")
    );
  }

  /**
   *  Evaluation of a range expression, where both operands are division operations. .
   */
  @org.junit.Test
  public void rangeExpr27() {
    final XQuery query = new XQuery(
      "((5 idiv 5) to (8 idiv 2))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2 3 4")
    );
  }

  /**
   *  test op:to with large numbers .
   */
  @org.junit.Test
  public void rangeExpr28() {
    final XQuery query = new XQuery(
      "18446744073709551616 to 18446744073709551620",
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
        assertStringValue(false, "18446744073709551616 18446744073709551617 18446744073709551618 18446744073709551619 18446744073709551620")
      ||
        error("FOAR0002")
      )
    );
  }

  /**
   *  test op:to with large numbers .
   */
  @org.junit.Test
  public void rangeExpr29() {
    final XQuery query = new XQuery(
      "count(18446744073709551616 to 18446744073709551620)",
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
        assertEq("5")
      ||
        error("FOAR0002")
      )
    );
  }

  /**
   *  Evaluation of a range expression that results in a sequence of length 0. Uses "fn:count" to avoid empty file. .
   */
  @org.junit.Test
  public void rangeExpr3() {
    final XQuery query = new XQuery(
      "fn:count(15 to 10)",
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
   *  test op:to with large numbers .
   */
  @org.junit.Test
  public void rangeExpr30() {
    final XQuery query = new XQuery(
      "(28446744073709551616 to 28446744073709551620)!position()",
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
        assertStringValue(false, "1 2 3 4 5")
      ||
        error("FOAR0002")
      )
    );
  }

  /**
   *  test op:to with large numbers .
   */
  @org.junit.Test
  public void rangeExpr31() {
    final XQuery query = new XQuery(
      "((28446744073709551616 to 28446744073709551620)!last())[1]",
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
        assertEq("5")
      ||
        error("FOAR0002")
      )
    );
  }

  /**
   *  test op:to with large numbers .
   */
  @org.junit.Test
  public void rangeExpr32() {
    final XQuery query = new XQuery(
      "reverse(28446744073709551616 to 28446744073709551620)",
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
        assertStringValue(false, "28446744073709551620 28446744073709551619 28446744073709551618 28446744073709551617 28446744073709551616")
      ||
        error("FOAR0002")
      )
    );
  }

  /**
   *  Evaluation of a range expression that uses the "reverse" function. .
   */
  @org.junit.Test
  public void rangeExpr4() {
    final XQuery query = new XQuery(
      "fn:reverse(10 to 15)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "15 14 13 12 11 10")
    );
  }

  /**
   *  Evaluation of a range expression that uses the empty sequence function. Uses the count function to avoid empty file. .
   */
  @org.junit.Test
  public void rangeExpr5() {
    final XQuery query = new XQuery(
      "fn:count((1, 2 to ()))",
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
        assertStringValue(false, "1")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Evaluation of a range expression, where both operands are addition operations. .
   */
  @org.junit.Test
  public void rangeExpr6() {
    final XQuery query = new XQuery(
      "((1+2) to (2+2))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3 4")
    );
  }

  /**
   *  Evaluation of a range expression, where the first operand are negative number. .
   */
  @org.junit.Test
  public void rangeExpr7() {
    final XQuery query = new XQuery(
      "(-4,-3 to 2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-4 -3 -2 -1 0 1 2")
    );
  }

  /**
   *  Evaluation of a range expression, where both operands are negative integers. .
   */
  @org.junit.Test
  public void rangeExpr8() {
    final XQuery query = new XQuery(
      "(-4, -3 to -1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-4 -3 -2 -1")
    );
  }

  /**
   *  Evaluation of a range expression, where the first operand is "xs:integer" function. .
   */
  @org.junit.Test
  public void rangeExpr9() {
    final XQuery query = new XQuery(
      "(xs:integer(1) to 5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2 3 4 5")
    );
  }
}
