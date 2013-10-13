package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the subsequence() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnSubsequence extends QT3TestSet {

  /**
   *  A test whose essence is: `subsequence()`. .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc1() {
    final XQuery query = new XQuery(
      "subsequence()",
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
   *  A test whose essence is: `subsequence((1, 2, 3.1, "four"), 3, 1) instance of xs:decimal`. .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc10() {
    final XQuery query = new XQuery(
      "subsequence((1, 2, 3.1, \"four\"), 3, 1) instance of xs:decimal",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `subsequence((1, 2, 3.1, "four"), 1, 1)`. .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc11() {
    final XQuery query = new XQuery(
      "subsequence((1, 2, 3.1, \"four\"), 1, 1)",
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
   *  A test whose essence is: `empty(subsequence((1, 2, 3, "four"), 4, -3))`. .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc12() {
    final XQuery query = new XQuery(
      "empty(subsequence((1, 2, 3, \"four\"), 4, -3))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty(subsequence((1, 2, 3, "four"), -4, -3))`. .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc13() {
    final XQuery query = new XQuery(
      "empty(subsequence((1, 2, 3, \"four\"), -4, -3))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `subsequence((1, 2, 3), 1, 1) eq 1`. .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc14() {
    final XQuery query = new XQuery(
      "subsequence((1, 2, 3), 1, 1) eq 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `subsequence((1, 2, 3), 1, 1) eq 1`. .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc15() {
    final XQuery query = new XQuery(
      "subsequence((1, 2, 3), 1, 1) eq 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `subsequence((1, 2, 3), 3) eq 3`. .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc16() {
    final XQuery query = new XQuery(
      "subsequence((1, 2, 3), 3) eq 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(subsequence((1, 2, 3), 1, 1)) eq 1`. .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc17() {
    final XQuery query = new XQuery(
      "count(subsequence((1, 2, 3), 1, 1)) eq 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(subsequence((1, 2, 3), 1, 3)) eq 3`. .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc18() {
    final XQuery query = new XQuery(
      "count(subsequence((1, 2, 3), 1, 3)) eq 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(subsequence((1, 2, 3, "four"), 4)) eq 1`. .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc19() {
    final XQuery query = new XQuery(
      "count(subsequence((1, 2, 3, \"four\"), 4)) eq 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `subsequence(1)`. .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc2() {
    final XQuery query = new XQuery(
      "subsequence(1)",
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
   *  A test whose essence is: `count(subsequence(1 to 3, 1, 1)) eq 1`. .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc20() {
    final XQuery query = new XQuery(
      "count(subsequence(1 to 3, 1, 1)) eq 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty(subsequence((1, 2), 4))`. .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc21() {
    final XQuery query = new XQuery(
      "empty(subsequence((1, 2), 4))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `subsequence((5, 6, 7, 8), 2, 1) eq 6`. .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc22() {
    final XQuery query = new XQuery(
      "subsequence((5, 6, 7, 8), 2, 1) eq 6",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(subsequence((1, 2), 2)) eq 1`. .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc23() {
    final XQuery query = new XQuery(
      "count(subsequence((1, 2), 2)) eq 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(subsequence((1, 2, 3, "four"), 2)) eq 3`. .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc24() {
    final XQuery query = new XQuery(
      "count(subsequence((1, 2, 3, \"four\"), 2)) eq 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(subsequence((1, 2, 3, "four"), 2, 2)) eq 2`. .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc25() {
    final XQuery query = new XQuery(
      "count(subsequence((1, 2, 3, \"four\"), 2, 2)) eq 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `subsequence((1, 2, 3.1, "four"), 1, 1)`. .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc26() {
    final XQuery query = new XQuery(
      "subsequence((1, 2, 3.1, \"four\"), 1, 1)",
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
   *  A test whose essence is: `1 eq "a string"`. .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc27() {
    final XQuery query = new XQuery(
      "1 eq \"a string\"",
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
   *  A test whose essence is: `1 eq subsequence(("1", 2, 3.1, "four"), 1, 1)`. .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc28() {
    final XQuery query = new XQuery(
      "1 eq subsequence((\"1\", 2, 3.1, \"four\"), 1, 1)",
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
   *  A test whose essence is: `subsequence(("1", 2, 3.1, "four"), 1, 1) eq 1`. .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc29() {
    final XQuery query = new XQuery(
      "subsequence((\"1\", 2, 3.1, \"four\"), 1, 1) eq 1",
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
   *  A test whose essence is: `subsequence(1, 1, 1, 1)`. .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc3() {
    final XQuery query = new XQuery(
      "subsequence(1, 1, 1, 1)",
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
   *  A test whose essence is: `subsequence(error(), 1, 1)`. .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc30() {
    final XQuery query = new XQuery(
      "subsequence(error(), 1, 1)",
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
   *  A test whose essence is: `count(subsequence((1, 2, 2, current-time()), 2, 2)) eq 2`. .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc31() {
    final XQuery query = new XQuery(
      "count(subsequence((1, 2, 2, current-time()), 2, 2)) eq 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(subsequence(remove(current-time(), 1), 1, 1)) eq 0`. .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc32() {
    final XQuery query = new XQuery(
      "count(subsequence(remove(current-time(), 1), 1, 1)) eq 0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal(1, subsequence((1, 2, current-time()), 1, 1))`. .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc33() {
    final XQuery query = new XQuery(
      "deep-equal(1, subsequence((1, 2, current-time()), 1, 1))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Using subsequence inside a predicate. .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc34() {
    final XQuery query = new XQuery(
      "(1)[deep-equal(1, subsequence((1, 2, current-time()), 1, 1))] eq 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty(subsequence((current-time(), 1), 4))`. .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc35() {
    final XQuery query = new XQuery(
      "empty(subsequence((current-time(), 1), 4))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(subsequence((current-time(), 1), 4)) eq 0`. .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc36() {
    final XQuery query = new XQuery(
      "count(subsequence((current-time(), 1), 4)) eq 0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(subsequence((current-time(), 2 , 3), 1)) eq 3`. .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc37() {
    final XQuery query = new XQuery(
      "count(subsequence((current-time(), 2 , 3), 1)) eq 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(subsequence((current-time(), 2 , 3), 3)) eq 1`. .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc38() {
    final XQuery query = new XQuery(
      "count(subsequence((current-time(), 2 , 3), 3)) eq 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(subsequence((current-time(), 2 , 3, 4), 2, 2)) eq 2`. .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc39() {
    final XQuery query = new XQuery(
      "count(subsequence((current-time(), 2 , 3, 4), 2, 2)) eq 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty(subsequence((), 2, 3))`. .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc4() {
    final XQuery query = new XQuery(
      "empty(subsequence((), 2, 3))",
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
   *  Apply a predicate to the result of fn:subsequence(). .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc40() {
    final XQuery query = new XQuery(
      "subsequence((1, 2, 3, current-time(), 5, 6, 7), 1, 1)[1]",
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
   *  Apply a predicate to the result of fn:subsequence(). .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc41() {
    final XQuery query = new XQuery(
      "subsequence((1, 2, 3, current-time(), 5, 6, 9), 7)[last()]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("9")
    );
  }

  /**
   *  Apply a predicate to the result of fn:subsequence(). .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc42() {
    final XQuery query = new XQuery(
      "subsequence((1, 2, 3, current-time(), 5, 6, 7), 4)[last() - 1]",
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
   *  Apply a predicate to the result of fn:subsequence(). .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc43() {
    final XQuery query = new XQuery(
      "empty(subsequence((1, 2, 3, current-time(), 5, 6, 7), 4, 1)[last() - 10])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Apply a predicate to the result of fn:subsequence(). .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc44() {
    final XQuery query = new XQuery(
      "empty(subsequence((1, 2, 3, current-time(), 5, 6, 7), 1, 1)[2])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty(subsequence((1, 2, 3), 2, -10))`. .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc5() {
    final XQuery query = new XQuery(
      "empty(subsequence((1, 2, 3), 2, -10))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(subsequence((1, 2, 3, "four"), 4)) eq 1`. .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc6() {
    final XQuery query = new XQuery(
      "count(subsequence((1, 2, 3, \"four\"), 4)) eq 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(subsequence((1, 2, 3, "four"), 4, 1)) eq 1`. .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc7() {
    final XQuery query = new XQuery(
      "count(subsequence((1, 2, 3, \"four\"), 4, 1)) eq 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `subsequence((1, 2, 3.1, "four"), 4)`. .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc8() {
    final XQuery query = new XQuery(
      "subsequence((1, 2, 3.1, \"four\"), 4)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("\"four\"")
    );
  }

  /**
   *  A test whose essence is: `subsequence((1, 2, 3.1, "four"), 4, 1)`. .
   */
  @org.junit.Test
  public void kSeqSubsequenceFunc9() {
    final XQuery query = new XQuery(
      "subsequence((1, 2, 3.1, \"four\"), 4, 1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("\"four\"")
    );
  }

  /**
   *  Use fn:subsequence where the input is via variable references. .
   */
  @org.junit.Test
  public void k2SeqSubsequenceFunc1() {
    final XQuery query = new XQuery(
      "let $start := (current-time(), 2)[2] treat as xs:integer,\n" +
      "                $len := (current-time(), 1)[2] treat as xs:integer\n" +
      "                return subsequence((1, 2, 3), $start, $len)",
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
   *  Have a call to subsequence that triggers a type error in an expression which can be const folded(derived from functionbc20_037). .
   */
  @org.junit.Test
  public void k2SeqSubsequenceFunc10() {
    final XQuery query = new XQuery(
      "subsequence((1, 2, 3), 1, \"string\")",
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
   *  A zero length. .
   */
  @org.junit.Test
  public void k2SeqSubsequenceFunc2() {
    final XQuery query = new XQuery(
      "subsequence((1, 2, 3), 1, 0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   *  A negative length. .
   */
  @org.junit.Test
  public void k2SeqSubsequenceFunc3() {
    final XQuery query = new XQuery(
      "subsequence((1, 2, 3, 4, 5), 4, -1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   *  Ensure rounding is done properly, 1.1. .
   */
  @org.junit.Test
  public void k2SeqSubsequenceFunc4() {
    final XQuery query = new XQuery(
      "fn:subsequence((1,2,3), 1.1, 1)",
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
   *  Ensure rounding is done properly, 1.8. .
   */
  @org.junit.Test
  public void k2SeqSubsequenceFunc5() {
    final XQuery query = new XQuery(
      "fn:subsequence((1,2,3), 1.8, 1)",
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
   *  Ensure rounding is done properly, 1.4. .
   */
  @org.junit.Test
  public void k2SeqSubsequenceFunc6() {
    final XQuery query = new XQuery(
      "fn:subsequence((1,2,3), 1.4, 1.4)",
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
   *  Ensure rounding is done properly, 1.5. .
   */
  @org.junit.Test
  public void k2SeqSubsequenceFunc7() {
    final XQuery query = new XQuery(
      "fn:subsequence((1,2,3), 1.5, 1.5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("2, 3")
    );
  }

  /**
   *  Have subsequence inside a function body, to trap bugs related to inference, rewrites and function call sites. This expression typically constant propagates to a sequence of integers. .
   */
  @org.junit.Test
  public void k2SeqSubsequenceFunc8() {
    final XQuery query = new XQuery(
      "let $f :=function() { subsequence(subsequence((1, 2, 3, 4), 3, 1), 1, 4) } return $f()",
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
   *  Have subsequence inside a function body, to trap bugs related to inference, rewrites and function call sites(#2). This expression typically constant propagates to a sequence of integers. .
   */
  @org.junit.Test
  public void k2SeqSubsequenceFunc9() {
    final XQuery query = new XQuery(
      "let $f :=function() { subsequence((1, 2, 3), 1) } return $f()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("1, 2, 3")
    );
  }

  /**
   *  Test fn:subsequence on -INF and INF .
   */
  @org.junit.Test
  public void cbclSubsequence001() {
    final XQuery query = new XQuery(
      "\n" +
      "        count(subsequence(1 to 10, xs:double(\"-INF\"), xs:double(\"INF\")))\n" +
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
      assertStringValue(false, "0")
    );
  }

  /**
   *  Test fn:subsequence with length NaN .
   */
  @org.junit.Test
  public void cbclSubsequence002() {
    final XQuery query = new XQuery(
      "count(subsequence(1 to 10, 2, xs:double(\"NaN\")))",
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
   *  Test fn:subsequence with starting position NaN .
   */
  @org.junit.Test
  public void cbclSubsequence003() {
    final XQuery query = new XQuery(
      "count(subsequence(1 to 10, xs:double(\"NaN\"), 4))",
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
   *  Test unbounded fn:subsequence with starting position -INF .
   */
  @org.junit.Test
  public void cbclSubsequence004() {
    final XQuery query = new XQuery(
      "count(subsequence(1 to 10, xs:double(\"-INF\")))",
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
   *  Test unbounded fn:subsequence with starting position NaN .
   */
  @org.junit.Test
  public void cbclSubsequence005() {
    final XQuery query = new XQuery(
      "count(subsequence(1 to 10, xs:double(\"NaN\")))",
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
   *  Test subsequence optimizations. .
   */
  @org.junit.Test
  public void cbclSubsequence006() {
    final XQuery query = new XQuery(
      "\n" +
      "        subsequence((1, 2, for $x in 1 to 10 return 2*$x), 2, year-from-date(current-date()))\n" +
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
      assertStringValue(false, "2 2 4 6 8 10 12 14 16 18 20")
    );
  }

  /**
   *  Test subsequence optimizations. .
   */
  @org.junit.Test
  public void cbclSubsequence007() {
    final XQuery query = new XQuery(
      "\n" +
      "        subsequence((1, 2, for $x in 1 to 10 return 2*$x), -1, sum((1 to 10)[. mod 10 = 3]))\n" +
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
      assertStringValue(false, "1")
    );
  }

  /**
   *  Test subsequence on length out of int range. .
   */
  @org.junit.Test
  public void cbclSubsequence008() {
    final XQuery query = new XQuery(
      "subsequence(1 to 100, 99, 2147483648)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "99 100")
    );
  }

  /**
   *  Test subsequence on starting value out of int range. .
   */
  @org.junit.Test
  public void cbclSubsequence009() {
    final XQuery query = new XQuery(
      "count(subsequence(1 to 100, -2147483648, 20))",
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
   *  Test subsequence on largest supported (non-infinite) range. .
   */
  @org.junit.Test
  public void cbclSubsequence010() {
    final XQuery query = new XQuery(
      "count(subsequence(1 to 3000000000, -2147483648, 2147483647))",
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
        error("*")
      ||
        assertEq("0")
      )
    );
  }

  /**
   *  Test unbounded subsequence with starting position out of int range. .
   */
  @org.junit.Test
  public void cbclSubsequence011() {
    final XQuery query = new XQuery(
      "count(subsequence(1 to 3000000000, -2147483649))",
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
        assertEq("3000000000")
      ||
        error("*")
      )
    );
  }

  /**
   *  Test unbounded subsequence with starting position out of int range. .
   */
  @org.junit.Test
  public void cbclSubsequence012() {
    final XQuery query = new XQuery(
      "count(subsequence(1 to 3000000000, 2147483648))",
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
        assertEq("852516353")
      ||
        error("*")
      )
    );
  }

  /**
   *  Test unbounded subsequence with largest allowed starting position. .
   */
  @org.junit.Test
  public void cbclSubsequence013() {
    final XQuery query = new XQuery(
      "count(subsequence(1 to 3000000000, 2147483647))",
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
        assertEq("852516354")
      ||
        error("*")
      )
    );
  }

  /**
   *  Test unbounded subsequence with largest allowed starting position. .
   */
  @org.junit.Test
  public void cbclSubsequence014() {
    final XQuery query = new XQuery(
      "subsequence(1 to 3000000000, 2147483647, 5)",
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
        assertStringValue(false, "2147483647 2147483648 2147483649 2147483650 2147483651")
      ||
        error("*")
      )
    );
  }

  /**
   *  Test subsequence on map expression. .
   */
  @org.junit.Test
  public void cbclSubsequence015() {
    final XQuery query = new XQuery(
      "subsequence(for $x in (1 to 100) return -$x, 3, 5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-3 -4 -5 -6 -7")
    );
  }

  /**
   *  Test subsequence on something that looks a bit like last but isn't .
   */
  @org.junit.Test
  public void cbclSubsequence016() {
    final XQuery query = new XQuery(
      "\n" +
      "        subsequence( for $x in 1 to 10 return 1 to $x, count(for $x in 0 to 10 return 1 to $x), 1)\n" +
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
      assertStringValue(false, "10")
    );
  }

  /**
   *  Test subsequence with infinite length starting at first item .
   */
  @org.junit.Test
  public void cbclSubsequence017() {
    final XQuery query = new XQuery(
      "\n" +
      "        subsequence(for $x in 1 to 10 return 1 to $x, 1.2, xs:double(\"INF\"))\n" +
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
      assertStringValue(false, "1 1 2 1 2 3 1 2 3 4 1 2 3 4 5 1 2 3 4 5 6 1 2 3 4 5 6 7 1 2 3 4 5 6 7 8 1 2 3 4 5 6 7 8 9 1 2 3 4 5 6 7 8 9 10")
    );
  }

  /**
   *  Test subsequence with infinite length starting after first item .
   */
  @org.junit.Test
  public void cbclSubsequence018() {
    final XQuery query = new XQuery(
      "\n" +
      "        subsequence(for $x in 1 to 10 return 1 to $x, 4.2, xs:double(\"INF\"))\n" +
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
      assertStringValue(false, "1 2 3 1 2 3 4 1 2 3 4 5 1 2 3 4 5 6 1 2 3 4 5 6 7 1 2 3 4 5 6 7 8 1 2 3 4 5 6 7 8 9 1 2 3 4 5 6 7 8 9 10")
    );
  }

  /**
   *  Test subsequence that gets the last item .
   */
  @org.junit.Test
  public void cbclSubsequence019() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $y := for $x in 1 to 10 return $x * $x return subsequence($y, count($y), 3)\n" +
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
      assertStringValue(false, "100")
    );
  }

  /**
   *  Tests subsequence with static start and length .
   */
  @org.junit.Test
  public void cbclSubsequence020() {
    final XQuery query = new XQuery(
      "subsequence(for $x in 1 to 10 return $x[. mod 2 = 0],2,4)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "4 6 8 10")
    );
  }

  /**
   *  Tests an unbounded subsequence with a static start .
   */
  @org.junit.Test
  public void cbclSubsequence021() {
    final XQuery query = new XQuery(
      "subsequence(for $x in 1 to 10 return $x[. mod 2 = 0],2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "4 6 8 10")
    );
  }

  /**
   *  Test skip on initial subsequence enumerator .
   */
  @org.junit.Test
  public void cbclSubsequence022() {
    final XQuery query = new XQuery(
      "subsequence((1 to 20)[. mod 2 = 0][position() < 5],2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "4 6 8")
    );
  }

  /**
   *  Test subsequence(for .....) .
   */
  @org.junit.Test
  public void cbclSubsequence023() {
    final XQuery query = new XQuery(
      "subsequence(for $x in 1 to 10 return $x * $x,2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "4 9 16 25 36 49 64 81 100")
    );
  }

  /**
   *  Test subsequence(...,1,INF) .
   */
  @org.junit.Test
  public void cbclSubsequence024() {
    final XQuery query = new XQuery(
      "\n" +
      "        subsequence((1 to 100)[. mod 2 = 0],1,xs:double(string-join(('I','N','F')[position() mod 2 >= 0],'')))\n" +
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
      assertStringValue(false, "2 4 6 8 10 12 14 16 18 20 22 24 26 28 30 32 34 36 38 40 42 44 46 48 50 52 54 56 58 60 62 64 66 68 70 72 74 76 78 80 82 84 86 88 90 92 94 96 98 100")
    );
  }

  /**
   *  Tries to call GetExpressionProperties on static-subsequence. In fact shows an error in variable binding when expanding path expressions .
   */
  @org.junit.Test
  public void cbclSubsequence025() {
    final XQuery query = new XQuery(
      "count(<a><b/></a>/*/subsequence(.,1,1)/..)",
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
   *  Tests for subsequence on divided sequence .
   */
  @org.junit.Test
  public void cbclSubsequence026() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $x := (1 to 10)[. mod 2 = 0] return subsequence((0,$x),3,count($x) div 2)\n" +
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
      assertStringValue(false, "4 6 8")
    );
  }

  /**
   *  Tests for subsequence on a map expression .
   */
  @org.junit.Test
  public void cbclSubsequence027() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $z := (1 to 10)[. mod 2 = 0] return subsequence(for $x in $z return floor($x),2,4)\n" +
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
      assertStringValue(false, "4 6 8 10")
    );
  }

  /**
   *  arg1: sequence of string, arg2 & arg3: integer .
   */
  @org.junit.Test
  public void fnSubsequenceMixArgs001() {
    final XQuery query = new XQuery(
      "fn:subsequence( (\"a\", \"b\", \"c\"), 1, 1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"a\"")
    );
  }

  /**
   *  arg1: sequence of string, arg2 & arg3: integer .
   */
  @org.junit.Test
  public void fnSubsequenceMixArgs002() {
    final XQuery query = new XQuery(
      "fn:subsequence( (\"a\", \"b\", \"c\"), 3, 12)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"c\"")
    );
  }

  /**
   *  arg1: sequence of string, arg2 & arg3: integer .
   */
  @org.junit.Test
  public void fnSubsequenceMixArgs003() {
    final XQuery query = new XQuery(
      "fn:subsequence( (\"a\", xs:string(\"\"),\"b\", \"c\"), 1, 3)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"a\", \"\", \"b\"")
    );
  }

  /**
   *  arg1: sequence of string, arg2 & arg3: integer .
   */
  @org.junit.Test
  public void fnSubsequenceMixArgs004() {
    final XQuery query = new XQuery(
      "fn:subsequence( (\"a\", xs:string(\"hello\"),\"b\", \"c\"), 1, 3)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"a\", \"hello\", \"b\"")
    );
  }

  /**
   *  arg1: sequence of string & anyURI, arg2 & arg3: integer .
   */
  @org.junit.Test
  public void fnSubsequenceMixArgs005() {
    final XQuery query = new XQuery(
      "fn:subsequence( (\"a\", xs:anyURI(\"www.example.com\"),\"b\", \"c\"), 1, 3)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"a\", \"www.example.com\", \"b\"")
    );
  }

  /**
   *  arg1: sequence of string, arg2 & arg3: integer .
   */
  @org.junit.Test
  public void fnSubsequenceMixArgs006() {
    final XQuery query = new XQuery(
      "fn:subsequence( (\"a\", (), (), \"b\", \"c\"), 1, 3)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"a\", \"b\", \"c\"")
    );
  }

  /**
   *  arg1: sequence of string & integer, arg2 & arg3: integer .
   */
  @org.junit.Test
  public void fnSubsequenceMixArgs007() {
    final XQuery query = new XQuery(
      "fn:subsequence( (\"a\", xs:integer(\"100\"), xs:integer(\"-100\"), \"b\", \"c\"),2,4)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("100, -100, \"b\", \"c\"")
    );
  }

  /**
   *  arg1: sequence of string,decimal & integer, arg2 & arg3: integer .
   */
  @org.junit.Test
  public void fnSubsequenceMixArgs008() {
    final XQuery query = new XQuery(
      "fn:subsequence( (\"a\", xs:decimal(\"-1.000000000001\"), xs:integer(\"-100\"), \"b\", \"c\"), 2,3)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("-1.000000000001, -100, \"b\"")
    );
  }

  /**
   *  arg1: sequence of string & float , arg2 & arg3: integer .
   */
  @org.junit.Test
  public void fnSubsequenceMixArgs009() {
    final XQuery query = new XQuery(
      "fn:subsequence( (\"a\", xs:float(\"INF\"), \"b\", \"c\"),-2,3)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   *  arg1: sequence of string & float , arg2 & arg3: integer .
   */
  @org.junit.Test
  public void fnSubsequenceMixArgs010() {
    final XQuery query = new XQuery(
      "fn:subsequence ( (\"a\", xs:float(\"-INF\"), \"b\", \"c\"), 1,2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"a\", xs:float(\"-INF\")")
    );
  }

  /**
   *  arg1: sequence of string & float , arg2 & arg3: integer .
   */
  @org.junit.Test
  public void fnSubsequenceMixArgs011() {
    final XQuery query = new XQuery(
      "fn:subsequence ( (\"a\", xs:float(\"NaN\"), \"b\", \"c\"), 0, 2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("\"a\"")
    );
  }

  /**
   *  arg1: sequence of string & float , arg2 & arg3: integer .
   */
  @org.junit.Test
  public void fnSubsequenceMixArgs012() {
    final XQuery query = new XQuery(
      "fn:subsequence ( (\"a\", xs:float(\"1.01\"), \"b\", \"c\"), 2,4)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("1.01, \"b\", \"c\"")
    );
  }

  /**
   *  arg1: sequence of string & double , arg2 & arg3: integer .
   */
  @org.junit.Test
  public void fnSubsequenceMixArgs013() {
    final XQuery query = new XQuery(
      "fn:subsequence ( (\"a\", xs:double(\"NaN\"), \"b\", \"c\"), 2, 20)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("xs:double('NaN'), \"b\", \"c\"")
    );
  }

  /**
   *  arg1: sequence of string & double , arg2 & arg3: integer .
   */
  @org.junit.Test
  public void fnSubsequenceMixArgs014() {
    final XQuery query = new XQuery(
      "fn:subsequence ( (\"a\", xs:double(\"1.01\"), \"b\", \"c\"), 2,3)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("1.01, \"b\", \"c\"")
    );
  }

  /**
   *  arg1: sequence of string & double , arg2 & arg3: integer .
   */
  @org.junit.Test
  public void fnSubsequenceMixArgs015() {
    final XQuery query = new XQuery(
      "fn:subsequence ( (\"a\", xs:double(\"-INF\"), \"b\", \"c\"), 2,2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("xs:double('-INF'), \"b\"")
    );
  }

  /**
   *  arg1: sequence of string & double , arg2 & arg3: integer .
   */
  @org.junit.Test
  public void fnSubsequenceMixArgs016() {
    final XQuery query = new XQuery(
      "fn:subsequence ( (\"a\", xs:double(\"INF\"), \"b\", \"c\"), 2, 1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("xs:double(\"INF\")")
    );
  }

  /**
   *  arg1: sequence of string & boolean, arg2 & arg3: integer .
   */
  @org.junit.Test
  public void fnSubsequenceMixArgs017() {
    final XQuery query = new XQuery(
      "fn:subsequence ( (\"a\", xs:boolean(\"1\"), \"b\", \"c\"), 1,2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"a\", true()")
    );
  }

  /**
   *  arg1: sequence of string & boolean, arg2 & arg3: integer .
   */
  @org.junit.Test
  public void fnSubsequenceMixArgs018() {
    final XQuery query = new XQuery(
      "fn:subsequence ( (\"a\", xs:boolean(\"0\"), \"b\", \"c\"), 2,1)",
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
   *  arg1: sequence of string & boolean, arg2 & arg3: integer .
   */
  @org.junit.Test
  public void fnSubsequenceMixArgs019() {
    final XQuery query = new XQuery(
      "fn:subsequence ( (\"a\", xs:boolean(\"true\"), \"b\", \"c\"), 1,2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"a\", true()")
    );
  }

  /**
   *  arg1: sequence of string & boolean, arg2 & arg3: integer .
   */
  @org.junit.Test
  public void fnSubsequenceMixArgs020() {
    final XQuery query = new XQuery(
      "fn:subsequence ( (\"a\", xs:boolean(\"false\"), \"b\", \"c\"), 1, 3)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"a\", false(), \"b\"")
    );
  }

  /**
   *  arg1: sequence of string & date , arg2 & arg3: integer .
   */
  @org.junit.Test
  public void fnSubsequenceMixArgs021() {
    final XQuery query = new XQuery(
      "fn:subsequence ( (\"a\", xs:date(\"1993-03-31\"), \"b\", \"c\"), 1,2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"a\", xs:date('1993-03-31')")
    );
  }

  /**
   *  arg1: sequence of string & dateTime, arg2 & arg3: integer .
   */
  @org.junit.Test
  public void fnSubsequenceMixArgs022() {
    final XQuery query = new XQuery(
      "fn:subsequence ( (\"a\", xs:dateTime(\"1972-12-31T00:00:00\"), \"b\", \"c\"), 0,2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("\"a\"")
    );
  }

  /**
   *  arg1: sequence of string & time , arg2 & arg3: integer .
   */
  @org.junit.Test
  public void fnSubsequenceMixArgs023() {
    final XQuery query = new XQuery(
      "fn:subsequence ( (\"a\", xs:time(\"12:30:00\"), \"b\", \"c\"), 1, 2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"a\", xs:time('12:30:00')")
    );
  }

  /**
   *  subsequence(X, -INF, +INF Returns () because -INF + INF = NaN, and position() lt NaN is false. See XSLT test case bug 837 (member only bugzilla) .
   */
  @org.junit.Test
  public void fnSubsequenceMixArgs024() {
    final XQuery query = new XQuery(
      "fn:subsequence (1 to 10, xs:double('-INF'), xs:double('INF'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }
}
