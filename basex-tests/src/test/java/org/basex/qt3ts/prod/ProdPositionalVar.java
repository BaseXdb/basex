package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the PositionalVar production.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdPositionalVar extends QT3TestSet {

  /**
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kForExprPositionalVar1() {
    final XQuery query = new XQuery(
      "for $a at $p in (1, 2) return 1, $p",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  Verify that the position is properly computed for fn:string-to-codepoints(). .
   */
  @org.junit.Test
  public void kForExprPositionalVar10() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 3), for $i at $p in distinct-values((1, 2, 3, 1, 2)) return $p)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Verify that the position is properly computed for the empty sequence. .
   */
  @org.junit.Test
  public void kForExprPositionalVar11() {
    final XQuery query = new XQuery(
      "empty(for $i at $p in () return $p)",
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
   *  Verify that the position is properly computed for fn:insert-before(). .
   */
  @org.junit.Test
  public void kForExprPositionalVar12() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 3, 4), for $i at $p in insert-before((1, current-time()), 13, (current-date(), 3)) return $p)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Verify that the position is properly computed for fn:insert-before()(#2). .
   */
  @org.junit.Test
  public void kForExprPositionalVar13() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 3, 4), for $i at $p in insert-before((1, current-time()), 1, (current-date(), 3)) return $p)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Verify that the position is properly computed for fn:insert-before()(#3). .
   */
  @org.junit.Test
  public void kForExprPositionalVar14() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 3, 4), for $i at $p in insert-before((1, current-time()), 2, (current-date(), 3)) return $p)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Verify that the position is properly computed for the range expression. .
   */
  @org.junit.Test
  public void kForExprPositionalVar15() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 3, 4), for $i at $p in 1 to 4 return $p)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Verify that the position is properly computed for the range expression(#2). .
   */
  @org.junit.Test
  public void kForExprPositionalVar16() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 3, 4), for $i at $p in -10 to -7 return $p)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Verify that the position is properly computed for fn:remove(). .
   */
  @org.junit.Test
  public void kForExprPositionalVar17() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 3), for $i at $p in remove((1, 2, 3, current-time()), 2) return $p)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Verify that the position is properly computed for fn:remove(). .
   */
  @org.junit.Test
  public void kForExprPositionalVar18() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 3), for $i at $p in remove((1, 2, 3, current-time()), 4) return $p)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Verify that the position is properly computed for fn:remove(). .
   */
  @org.junit.Test
  public void kForExprPositionalVar19() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 3), for $i at $p in remove((1, 2, current-time()), 10) return $p)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Position variable which is not in scope. .
   */
  @org.junit.Test
  public void kForExprPositionalVar2() {
    final XQuery query = new XQuery(
      "for $a at $p1 in (1, 2), $b at $p2 in (1, 2), $c at $p3 in (1, 2) return 1, $p1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  Verify that the position is properly computed for fn:remove(). .
   */
  @org.junit.Test
  public void kForExprPositionalVar20() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 3), for $i at $p in remove((1, 2, current-time()), 0) return $p)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Verify that the position is properly computed for fn:remove(). .
   */
  @org.junit.Test
  public void kForExprPositionalVar21() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 3), for $i at $p in remove((1, 2, 3, current-time()), 1) return $p)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Verify that the position is properly computed for fn:remove(). .
   */
  @org.junit.Test
  public void kForExprPositionalVar22() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 3), for $i at $p in remove((1, 2, 3, current-time()), 3) return $p)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Verify that the position is properly computed for fn:subsequence(). .
   */
  @org.junit.Test
  public void kForExprPositionalVar23() {
    final XQuery query = new XQuery(
      "1 eq (for $i at $p in subsequence((1, 2, 3, current-time()), 1, 1) return $p)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Verify that the position is properly computed for fn:subsequence(). .
   */
  @org.junit.Test
  public void kForExprPositionalVar24() {
    final XQuery query = new XQuery(
      "empty(for $i at $p in subsequence((1, 2, 3, current-time()), 5) return $p)",
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
   *  Verify that the position is properly computed for fn:subsequence(). .
   */
  @org.junit.Test
  public void kForExprPositionalVar25() {
    final XQuery query = new XQuery(
      "empty(for $i at $p in subsequence((1, 2, 3, current-time()), 5, 8) return $p)",
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
   *  Verify that the position is properly computed for fn:subsequence(). .
   */
  @org.junit.Test
  public void kForExprPositionalVar26() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2), for $i at $p in subsequence((1, 2, 3, current-time()), 3, 2) return $p)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Verify that the position is properly computed for fn:subsequence(). .
   */
  @org.junit.Test
  public void kForExprPositionalVar27() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2), for $i at $p in subsequence((1, 2, 3, current-time()), 1, 2) return $p)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Verify that the position is properly computed for fn:subsequence(). .
   */
  @org.junit.Test
  public void kForExprPositionalVar28() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2), for $i at $p in subsequence((1, 2, 3, current-time()), 2, 2) return $p)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Verify that the position is properly computed for a singleton value. .
   */
  @org.junit.Test
  public void kForExprPositionalVar29() {
    final XQuery query = new XQuery(
      "1 eq (for $i at $p in 0 return $p)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Position variable which is not in scope. .
   */
  @org.junit.Test
  public void kForExprPositionalVar3() {
    final XQuery query = new XQuery(
      "for $a at $p1 in (1, 2), $b at $p2 in (1, 2), $c at $p3 in (1, 2) return 1, $p2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  Evaluate the positional and binding expression at the same time. .
   */
  @org.junit.Test
  public void kForExprPositionalVar30() {
    final XQuery query = new XQuery(
      "deep-equal(for $i at $p in (1, 2, 3, 4) return ($i, $p), (1, 1, 2, 2, 3, 3, 4, 4))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Position variable which is not in scope. .
   */
  @org.junit.Test
  public void kForExprPositionalVar4() {
    final XQuery query = new XQuery(
      "for $a at $p1 in (1, 2), $b at $p2 in (1, 2), $c at $p3 in (1, 2) return 1, $p3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  The name for a positional variable must be preceeded with '$'. .
   */
  @org.junit.Test
  public void kForExprPositionalVar5() {
    final XQuery query = new XQuery(
      "for $a at p1 in 1 return 1",
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
   *  Verify positional variable with fn:deep-equal(). .
   */
  @org.junit.Test
  public void kForExprPositionalVar6() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 3), for $i at $p in (1, 2, 3) return $p)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Extract the EBV from a positional variable. .
   */
  @org.junit.Test
  public void kForExprPositionalVar7() {
    final XQuery query = new XQuery(
      "deep-equal((true(), true()), for $i at $p in (1, 2) return boolean($p))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A positional variable causing a type error. .
   */
  @org.junit.Test
  public void kForExprPositionalVar8() {
    final XQuery query = new XQuery(
      "for $i at $p in (1, 2, 3) return $p + \"1\"",
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
   *  Verify that the position is properly computed for fn:string-to-codepoints(). .
   */
  @org.junit.Test
  public void kForExprPositionalVar9() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 3), for $i at $p in string-to-codepoints(\"abc\") return $p)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Combine fn:remove() and a positional for-variable. .
   */
  @org.junit.Test
  public void k2ForExprPositionalVar1() {
    final XQuery query = new XQuery(
      "for $i at $p in remove((1, 2, 3), 10) return $p",
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
   *  A query that possibly could raise a type error. .
   */
  @org.junit.Test
  public void k2ForExprPositionalVar2() {
    final XQuery query = new XQuery(
      "let $tree := <e> <a id=\"1\"/> <a id=\"2\"/> <a id=\"3\"/> </e> for $i at $pos in (\"a\", \"b\", \"c\") return ($tree/@id eq $pos, $pos)",
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
        assertStringValue(false, "1 2 3")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  Usage of two positional variable references. .
   */
  @org.junit.Test
  public void k2ForExprPositionalVar3() {
    final XQuery query = new XQuery(
      "let $tree := <e> <a id=\"1\"/> <a id=\"2\"/> <a id=\"3\"/> </e> for $i at $pos in (\"a\", \"b\", \"c\") return ($tree/a/@id = $pos, $pos)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "true 1 true 2 true 3")
    );
  }

  /**
   *  Cache a positional variable with a let-binding. .
   */
  @org.junit.Test
  public void k2ForExprPositionalVar4() {
    final XQuery query = new XQuery(
      "for $i at $pos in (3 to 6) let $let := $pos + 1 return ($let, $let - 1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2 1 3 2 4 3 5 4")
    );
  }
}
