package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the Predicate production.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdPredicate extends QT3TestSet {

  /**
   *  Syntactically invalid predicate. .
   */
  @org.junit.Test
  public void kFilterExpr1() {
    final XQuery query = new XQuery(
      "(1, 2, 3)[",
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
   *  A test whose essence is: `empty((1, 2, 3)[0.1])`. .
   */
  @org.junit.Test
  public void kFilterExpr10() {
    final XQuery query = new XQuery(
      "empty((1, 2, 3)[0.1])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty((1, 2, 3)[1.1])`. .
   */
  @org.junit.Test
  public void kFilterExpr11() {
    final XQuery query = new XQuery(
      "empty((1, 2, 3)[1.1])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty((1, 2, 3)[1.01])`. .
   */
  @org.junit.Test
  public void kFilterExpr12() {
    final XQuery query = new XQuery(
      "empty((1, 2, 3)[1.01])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty((1, 2, 3)[4])`. .
   */
  @org.junit.Test
  public void kFilterExpr13() {
    final XQuery query = new XQuery(
      "empty((1, 2, 3)[4])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty((1, 2, 3)[4.1])`. .
   */
  @org.junit.Test
  public void kFilterExpr14() {
    final XQuery query = new XQuery(
      "empty((1, 2, 3)[4.1])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty((1, 2, 3)[4.01])`. .
   */
  @org.junit.Test
  public void kFilterExpr15() {
    final XQuery query = new XQuery(
      "empty((1, 2, 3)[4.01])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(empty((1, 2, 3)[1]))`. .
   */
  @org.junit.Test
  public void kFilterExpr16() {
    final XQuery query = new XQuery(
      "not(empty((1, 2, 3)[1]))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(empty((1, 2, 3)[3]))`. .
   */
  @org.junit.Test
  public void kFilterExpr17() {
    final XQuery query = new XQuery(
      "not(empty((1, 2, 3)[3]))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `(1, 2, 3)[1] eq 1`. .
   */
  @org.junit.Test
  public void kFilterExpr18() {
    final XQuery query = new XQuery(
      "(1, 2, 3)[1] eq 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `(1, 2, 3)[1.0] eq 1`. .
   */
  @org.junit.Test
  public void kFilterExpr19() {
    final XQuery query = new XQuery(
      "(1, 2, 3)[1.0] eq 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Syntactically invalid predicate. .
   */
  @org.junit.Test
  public void kFilterExpr2() {
    final XQuery query = new XQuery(
      "(1, 2, 3)]",
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
   *  A test whose essence is: `(1, 2, 3)[1.0e0] eq 1`. .
   */
  @org.junit.Test
  public void kFilterExpr20() {
    final XQuery query = new XQuery(
      "(1, 2, 3)[1.0e0] eq 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `(1, 2, 3)[3] eq 3`. .
   */
  @org.junit.Test
  public void kFilterExpr21() {
    final XQuery query = new XQuery(
      "(1, 2, 3)[3] eq 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `(1, 2, 3)[3.0] eq 3`. .
   */
  @org.junit.Test
  public void kFilterExpr22() {
    final XQuery query = new XQuery(
      "(1, 2, 3)[3.0] eq 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `(1, 2, 3)[3.0e0] eq 3`. .
   */
  @org.junit.Test
  public void kFilterExpr23() {
    final XQuery query = new XQuery(
      "(1, 2, 3)[3.0e0] eq 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `(0, 1, 2)[1] eq 0`. .
   */
  @org.junit.Test
  public void kFilterExpr24() {
    final XQuery query = new XQuery(
      "(0, 1, 2)[1] eq 0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `(0, 1, 2)[2] eq 1`. .
   */
  @org.junit.Test
  public void kFilterExpr25() {
    final XQuery query = new XQuery(
      "(0, 1, 2)[2] eq 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `(0, 1, 2)[3] eq 2`. .
   */
  @org.junit.Test
  public void kFilterExpr26() {
    final XQuery query = new XQuery(
      "(0, 1, 2)[3] eq 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `(0)[1] eq 0`. .
   */
  @org.junit.Test
  public void kFilterExpr27() {
    final XQuery query = new XQuery(
      "(0)[1] eq 0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `0[1] eq 0`. .
   */
  @org.junit.Test
  public void kFilterExpr28() {
    final XQuery query = new XQuery(
      "0[1] eq 0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `(0, 1)[1] eq 0`. .
   */
  @org.junit.Test
  public void kFilterExpr29() {
    final XQuery query = new XQuery(
      "(0, 1)[1] eq 0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Syntactically invalid predicate. .
   */
  @org.junit.Test
  public void kFilterExpr3() {
    final XQuery query = new XQuery(
      "(1, 2, 3)[]",
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
   *  A test whose essence is: `empty((1, 2, 3)[false()])`. .
   */
  @org.junit.Test
  public void kFilterExpr30() {
    final XQuery query = new XQuery(
      "empty((1, 2, 3)[false()])",
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
   *  A test whose essence is: `deep-equal((1, 2, 3), (1, 2, 3)[true()])`. .
   */
  @org.junit.Test
  public void kFilterExpr31() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 3), (1, 2, 3)[true()])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal((1, 2, 3), (1, 2, 3)[. instance of xs:integer])`. .
   */
  @org.junit.Test
  public void kFilterExpr32() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 3), (1, 2, 3)[. instance of xs:integer])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  The context item is used as the predicate, leading to a truth predicate. .
   */
  @org.junit.Test
  public void kFilterExpr33() {
    final XQuery query = new XQuery(
      "deep-equal((true(), true(), true()), (false(), true(), true(), false(), true(), false())[.])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  The context item is used as the predicate, leading to a numeric predicate. .
   */
  @org.junit.Test
  public void kFilterExpr34() {
    final XQuery query = new XQuery(
      "deep-equal((2, 3, 4, 5, 7, 8, 9), (0, 2, 3, 4, 5, 5, 7, 8, 10 - 1)[.])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Source expression of a predicate doesn't have to use paranteses. .
   */
  @org.junit.Test
  public void kFilterExpr35() {
    final XQuery query = new XQuery(
      "1[true()] eq 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Use fn:current-time() inside a predicate. .
   */
  @org.junit.Test
  public void kFilterExpr36() {
    final XQuery query = new XQuery(
      "(1, current-time())[1]",
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
   *  One xs:anyURI value is a valid predicate. .
   */
  @org.junit.Test
  public void kFilterExpr37() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 3), (1, 2, 3)[xs:anyURI(\"example.com/\")])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  xs:anyURI values are invalid predicates. .
   */
  @org.junit.Test
  public void kFilterExpr38() {
    final XQuery query = new XQuery(
      "(1, 2, 3)[(xs:anyURI(\"example.com/\"), xs:anyURI(\"example.com/\"))]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0006")
    );
  }

  /**
   *  xs:untypedAtomic literal as predicate. .
   */
  @org.junit.Test
  public void kFilterExpr39() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 3), (1, 2, 3)[xs:untypedAtomic(\"content\")])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Syntactically invalid predicate. .
   */
  @org.junit.Test
  public void kFilterExpr4() {
    final XQuery query = new XQuery(
      "[true()]",
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
   *  Multiple xs:untypedAtomic values is an invalid predicate. .
   */
  @org.junit.Test
  public void kFilterExpr40() {
    final XQuery query = new XQuery(
      "(1, 2, 3)[(xs:untypedAtomic(\"content\"), xs:untypedAtomic(\"content\"))]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0006")
    );
  }

  /**
   *  A test whose essence is: `(0, 1, 2)[true()][1] eq 0`. .
   */
  @org.junit.Test
  public void kFilterExpr41() {
    final XQuery query = new XQuery(
      "(0, 1, 2)[true()][1] eq 0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `(1, 2, 3)[position() eq 2 or position() eq 3][2] eq 3`. .
   */
  @org.junit.Test
  public void kFilterExpr42() {
    final XQuery query = new XQuery(
      "(1, 2, 3)[position() eq 2 or position() eq 3][2] eq 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty((1, 2, 3)[position() eq 2 or position() eq 3][3])`. .
   */
  @org.junit.Test
  public void kFilterExpr43() {
    final XQuery query = new XQuery(
      "empty((1, 2, 3)[position() eq 2 or position() eq 3][3])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal((1, 2, 3), (1, 2, 3)[2 or 3])`. .
   */
  @org.junit.Test
  public void kFilterExpr44() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 3), (1, 2, 3)[2 or 3])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty((1, 2, 3)[3][2])`. .
   */
  @org.junit.Test
  public void kFilterExpr45() {
    final XQuery query = new XQuery(
      "empty((1, 2, 3)[3][2])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty((1, 2, 3)[3][0])`. .
   */
  @org.junit.Test
  public void kFilterExpr46() {
    final XQuery query = new XQuery(
      "empty((1, 2, 3)[3][0])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  An excessive nesting of various predicates. .
   */
  @org.junit.Test
  public void kFilterExpr47() {
    final XQuery query = new XQuery(
      "(0, 2, 4, 5)[1][1][1][true()][1][true()][1] eq 0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A large numeric xs:double predicate that evaluates to the empty sequence. .
   */
  @org.junit.Test
  public void kFilterExpr48() {
    final XQuery query = new XQuery(
      "empty((1, 2, 3)[3e8])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty((0, 1, 2)[false()][1])`. .
   */
  @org.junit.Test
  public void kFilterExpr49() {
    final XQuery query = new XQuery(
      "empty((0, 1, 2)[false()][1])",
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
   *  Syntactically invalid predicate. .
   */
  @org.junit.Test
  public void kFilterExpr5() {
    final XQuery query = new XQuery(
      "[]",
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
   *  A test whose essence is: `deep-equal(("a", "b", "c"), (0, 1, 2, "a", "b", "c")[. instance of xs:string])`. .
   */
  @org.junit.Test
  public void kFilterExpr50() {
    final XQuery query = new XQuery(
      "deep-equal((\"a\", \"b\", \"c\"), (0, 1, 2, \"a\", \"b\", \"c\")[. instance of xs:string])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Filter a sequence with instance of and a second predicate. .
   */
  @org.junit.Test
  public void kFilterExpr51() {
    final XQuery query = new XQuery(
      "((0, 1, 2, \"a\", \"b\", \"c\")[. instance of xs:string][. treat as xs:string eq \"c\"] treat as xs:string) eq \"c\"",
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
        error("XPDY0050")
      )
    );
  }

  /**
   *  Filter a sequence with instance of and a second predicate(#2). .
   */
  @org.junit.Test
  public void kFilterExpr52() {
    final XQuery query = new XQuery(
      "((0, 1, 2, \"a\", \"b\", \"c\")[. instance of xs:integer][. treat as xs:integer eq 0] treat as xs:integer) eq 0",
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
        error("XPDY0050")
      )
    );
  }

  /**
   *  Two predicates, where one leading to an invalid operator mapping in the second. .
   */
  @org.junit.Test
  public void kFilterExpr53() {
    final XQuery query = new XQuery(
      "(0, 1, 2, \"a\", \"b\", \"c\")[. instance of xs:integer][. eq \"c\"] eq 0",
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
   *  Predicates in combination with the empty sequence. .
   */
  @org.junit.Test
  public void kFilterExpr54() {
    final XQuery query = new XQuery(
      "empty(()[()])",
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
   *  Predicates in combination with the empty sequence. .
   */
  @org.junit.Test
  public void kFilterExpr55() {
    final XQuery query = new XQuery(
      "empty((1, 2, 3)[()])",
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
   *  Predicates in combination with the empty sequence. .
   */
  @org.junit.Test
  public void kFilterExpr56() {
    final XQuery query = new XQuery(
      "empty(()[last()])",
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
   *  Predicates in combination with the empty sequence. .
   */
  @org.junit.Test
  public void kFilterExpr57() {
    final XQuery query = new XQuery(
      "empty(()[1])",
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
   *  Predicates in combination with the empty sequence. .
   */
  @org.junit.Test
  public void kFilterExpr58() {
    final XQuery query = new XQuery(
      "empty(()[position()])",
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
   *  Predicates in combination with the empty sequence. .
   */
  @org.junit.Test
  public void kFilterExpr59() {
    final XQuery query = new XQuery(
      "empty(()[count(remove((current-time(), 1), 1)) eq 1])",
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
   *  A test whose essence is: `empty((1, 2, 3)[0])`. .
   */
  @org.junit.Test
  public void kFilterExpr6() {
    final XQuery query = new XQuery(
      "empty((1, 2, 3)[0])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal((1, 2, 3), (1, 2, 3)[position() >= 1])`. .
   */
  @org.junit.Test
  public void kFilterExpr60() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 3), (1, 2, 3)[position() >= 1])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal((1, 2, 3), (1, 2, 3)[3 >= position()])`. .
   */
  @org.junit.Test
  public void kFilterExpr61() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 3), (1, 2, 3)[3 >= position()])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal((1, 2, 3), (1, 2, 3)[position() ge 1])`. .
   */
  @org.junit.Test
  public void kFilterExpr62() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 3), (1, 2, 3)[position() ge 1])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal((1, 2, 3), (1, 2, 3)[3 ge position()])`. .
   */
  @org.junit.Test
  public void kFilterExpr63() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 3), (1, 2, 3)[3 ge position()])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal((0, 1, 2), (0, 1, 2)[position() eq position()])`. .
   */
  @org.junit.Test
  public void kFilterExpr64() {
    final XQuery query = new XQuery(
      "deep-equal((0, 1, 2), (0, 1, 2)[position() eq position()])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal((0, 1, 2), (0, 1, 2)[position() = position()])`. .
   */
  @org.junit.Test
  public void kFilterExpr65() {
    final XQuery query = new XQuery(
      "deep-equal((0, 1, 2), (0, 1, 2)[position() = position()])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `(0, 1, 2)[1 eq position()]`. .
   */
  @org.junit.Test
  public void kFilterExpr66() {
    final XQuery query = new XQuery(
      "(0, 1, 2)[1 eq position()]",
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
   *  A test whose essence is: `(0, 1, 2)[3 eq position()]`. .
   */
  @org.junit.Test
  public void kFilterExpr67() {
    final XQuery query = new XQuery(
      "(0, 1, 2)[3 eq position()]",
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
   *  A test whose essence is: `(0, 1, 2)[position() eq 3]`. .
   */
  @org.junit.Test
  public void kFilterExpr68() {
    final XQuery query = new XQuery(
      "(0, 1, 2)[position() eq 3]",
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
   *  A test whose essence is: `deep-equal((1, 2, 3), (1, 2, 3)[number(.)])`. .
   */
  @org.junit.Test
  public void kFilterExpr69() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 3), (1, 2, 3)[number(.)])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty((1, 2, 3)[4])`. .
   */
  @org.junit.Test
  public void kFilterExpr7() {
    final XQuery query = new XQuery(
      "empty((1, 2, 3)[4])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal((1, 2), (0, 1, 2)[if(. eq 1) then 2 else 3])`. .
   */
  @org.junit.Test
  public void kFilterExpr70() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2), (0, 1, 2)[if(. eq 1) then 2 else 3])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal((0, 1, 2), (0, 1, 2)[if(. eq 8) then "str" else position()])`. .
   */
  @org.junit.Test
  public void kFilterExpr71() {
    final XQuery query = new XQuery(
      "deep-equal((0, 1, 2), (0, 1, 2)[if(. eq 8) then \"str\" else position()])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal((0, 1, 2), (0, 1, 2)[if(. eq 8) then 0 else position()])`. .
   */
  @org.junit.Test
  public void kFilterExpr72() {
    final XQuery query = new XQuery(
      "deep-equal((0, 1, 2), (0, 1, 2)[if(. eq 8) then 0 else position()])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `(0, 1, 2)[last()]`. .
   */
  @org.junit.Test
  public void kFilterExpr73() {
    final XQuery query = new XQuery(
      "(0, 1, 2)[last()]",
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
   *  A test whose essence is: `(0, 1, "2")[last()]`. .
   */
  @org.junit.Test
  public void kFilterExpr74() {
    final XQuery query = new XQuery(
      "(0, 1, \"2\")[last()]",
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
   *  fn:last() in predicate, leading to invalid operator mapping. .
   */
  @org.junit.Test
  public void kFilterExpr75() {
    final XQuery query = new XQuery(
      "2 eq (0, 1, \"2\")[last()]",
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
   *  A test whose essence is: `empty((())[last()])`. .
   */
  @org.junit.Test
  public void kFilterExpr76() {
    final XQuery query = new XQuery(
      "empty((())[last()])",
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
   *  A test whose essence is: `empty(()[last()])`. .
   */
  @org.junit.Test
  public void kFilterExpr77() {
    final XQuery query = new XQuery(
      "empty(()[last()])",
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
   *  Predicates involving the focus' context item. .
   */
  @org.junit.Test
  public void kFilterExpr78() {
    final XQuery query = new XQuery(
      "(0, 1, 2)[. eq 0]",
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
   *  Predicates involving the focus' context item. .
   */
  @org.junit.Test
  public void kFilterExpr79() {
    final XQuery query = new XQuery(
      "(0, 1, 2)[. eq 1]",
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
   *  A test whose essence is: `empty((1, 2, 3)[0])`. .
   */
  @org.junit.Test
  public void kFilterExpr8() {
    final XQuery query = new XQuery(
      "empty((1, 2, 3)[0])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Predicates involving the focus' context item. .
   */
  @org.junit.Test
  public void kFilterExpr80() {
    final XQuery query = new XQuery(
      "(0, 1, 2)[. eq 2]",
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
   *  Predicates involving the focus' context item. .
   */
  @org.junit.Test
  public void kFilterExpr81() {
    final XQuery query = new XQuery(
      "deep-equal((0, 1, 2), (0, 1, 2)[. eq 0 or . eq 1 or . eq 2])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Type related predicate tests. .
   */
  @org.junit.Test
  public void kFilterExpr82() {
    final XQuery query = new XQuery(
      "(0, 1, 2)[remove((1, \"a string\"), 2)]",
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
        assertEq("0")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  Type related predicate tests. .
   */
  @org.junit.Test
  public void kFilterExpr83() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2), (1, 2)[remove((true(), \"a string\"), 2)])",
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
   *  Type related predicate tests. .
   */
  @org.junit.Test
  public void kFilterExpr84() {
    final XQuery query = new XQuery(
      "empty((1, 2, 3)[remove((false(), \"a string\"), 2)])",
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
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Type related predicate tests. .
   */
  @org.junit.Test
  public void kFilterExpr85() {
    final XQuery query = new XQuery(
      "empty((1, 2, 3)[remove((false(), \"a string\"), 2)])",
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
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  A predicate that never can match. .
   */
  @org.junit.Test
  public void kFilterExpr86() {
    final XQuery query = new XQuery(
      "empty(current-time()[2])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A predicate that never can match. .
   */
  @org.junit.Test
  public void kFilterExpr87() {
    final XQuery query = new XQuery(
      "empty((1, 2, 3, current-time())[0])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A truth predicate that never match. .
   */
  @org.junit.Test
  public void kFilterExpr88() {
    final XQuery query = new XQuery(
      "empty(remove((1, 2, 3, current-time()), 4)[false()])",
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
   *  A truth predicate that always match. .
   */
  @org.junit.Test
  public void kFilterExpr89() {
    final XQuery query = new XQuery(
      "deep-equal(remove((1, 2, 3, current-time()), 4)[true()], (1, 2, 3))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty((1, 2, 3)[4])`. .
   */
  @org.junit.Test
  public void kFilterExpr9() {
    final XQuery query = new XQuery(
      "empty((1, 2, 3)[4])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Nested predicate with multiple calls to fn:last(). .
   */
  @org.junit.Test
  public void kFilterExpr90() {
    final XQuery query = new XQuery(
      "(1, 2, 3)[(last(), last())[2]]",
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
   *  A predicate whose expression EBV cannot be extracted from. .
   */
  @org.junit.Test
  public void kFilterExpr91() {
    final XQuery query = new XQuery(
      "(1, 2, 3)[xs:hexBinary(\"FF\")]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0006")
    );
  }

  /**
   *  A predicate whose expression EBV cannot be extracted from. .
   */
  @org.junit.Test
  public void kFilterExpr92() {
    final XQuery query = new XQuery(
      "(1, 2, 3)[1, 2]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0006")
    );
  }

  /**
   *  A predicate whose expression EBV cannot be extracted from. .
   */
  @org.junit.Test
  public void kFilterExpr93() {
    final XQuery query = new XQuery(
      "(1, 2, 3)[1, \"a string\"]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0006")
    );
  }

  /**
   *  A predicate whose expression EBV cannot be extracted from. .
   */
  @org.junit.Test
  public void kFilterExpr94() {
    final XQuery query = new XQuery(
      "(1, 2, 3)[\"a string\", 1]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0006")
    );
  }

  /**
   *  Apply a predicate combined with last(), on a sequence constructed with an element constructor. .
   */
  @org.junit.Test
  public void k2FilterExpr1() {
    final XQuery query = new XQuery(
      "declare variable $var := (for $i in 1 to 100 return <e>{$i}</e>); $var[last()]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e>100</e>", false)
    );
  }

  /**
   *  Apply a predicate combined with a numeric literal, on a sequence constructed with an element constructor. .
   */
  @org.junit.Test
  public void k2FilterExpr2() {
    final XQuery query = new XQuery(
      "declare variable $var := (for $i in 1 to 100 return <e>{$i}</e>); $var[5]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e>5</e>", false)
    );
  }

  /**
   *  An numeric predicate that is xs:decimal. .
   */
  @org.junit.Test
  public void k2FilterExpr3() {
    final XQuery query = new XQuery(
      "empty((1,2,3,4,5)[3.4])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  An numeric predicate that is xs:decimal, as part of a path expression. .
   */
  @org.junit.Test
  public void k2FilterExpr4() {
    final XQuery query = new XQuery(
      "empty(<e><a/></e>//a[3.4])",
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
   *  Subsequent filter expressions, and node tests whose focus depends on where a variable is declared. .
   */
  @org.junit.Test
  public void k2FilterExpr5() {
    final XQuery query = new XQuery(
      "let $d := document {<root><child type=\"\"/></root>} return $d//*[let $i := @type return $d//*[$i]], (1, 2, 3)[true()], (4, 5, 6)[false()]",
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
        assertSerialization("<child type=\"\"/>1 2 3", false)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Subsequent filter expressions, and node tests whose focus depends on where a variable is declared(#2). .
   */
  @org.junit.Test
  public void k2FilterExpr6() {
    final XQuery query = new XQuery(
      "let $d := document { <root><child type=\"\"/></root> } return $d//*[let $i := @type return $d//*[$i]]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<child type=\"\"/>", false)
    );
  }

  /**
   *  Use self::processing-instruction() in a filter predicate. .
   */
  @org.junit.Test
  public void k2FilterExpr7() {
    final XQuery query = new XQuery(
      "(<x/>, <?y?>)[self::processing-instruction()]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<?y?>", false)
    );
  }

  /**
   *  Use self::processing-instruction() in a filter predicate, combined with a treat as. .
   */
  @org.junit.Test
  public void k2FilterExpr8() {
    final XQuery query = new XQuery(
      "(<?z?>, <?y?>)[self::processing-instruction(y)] treat as empty-sequence()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPDY0050")
    );
  }

  /**
   *  Predicate combined with 'treat as'. .
   */
  @org.junit.Test
  public void k2Predicates1() {
    final XQuery query = new XQuery(
      "\"c\"[. treat as xs:string]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "c")
    );
  }

  /**
   *  Apply a predicate to directly constructed nodes. .
   */
  @org.junit.Test
  public void k2Predicates2() {
    final XQuery query = new XQuery(
      "declare variable $myvar := <elem> <a/> <b/> <c/></elem>; $myvar/*[last()]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<c/>", false)
    );
  }

  /**
   *  Apply a predicate to directly constructed nodes. .
   */
  @org.junit.Test
  public void k2Predicates3() {
    final XQuery query = new XQuery(
      "declare variable $myvar := <elem> <a/> <b/> <c/></elem>; $myvar/*[last() - 1]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<b/>", false)
    );
  }

  /**
   *  Apply a predicate to directly constructed nodes. .
   */
  @org.junit.Test
  public void k2Predicates4() {
    final XQuery query = new XQuery(
      "declare variable $myvar := <elem> <a/> <b/> <c/></elem>; $myvar/*[1]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a/>", false)
    );
  }

  /**
   *  Apply two identical numeric predicates after each other. .
   */
  @org.junit.Test
  public void k2Predicates5() {
    final XQuery query = new XQuery(
      "(<a/>, <b/>, <c/>)[1][1]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a/>", false)
    );
  }

  /**
   *  Apply a predicate to a node constructor. .
   */
  @org.junit.Test
  public void k2Predicates6() {
    final XQuery query = new XQuery(
      "<b attr=\"f\"/>[1]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<b attr=\"f\"/>", false)
    );
  }

  /**
   *  Apply last() to ancestor-or-self within a predicate. .
   */
  @org.junit.Test
  public void k2Predicates7() {
    final XQuery query = new XQuery(
      "<r>{<e xml:lang=\"ene\"/>/(ancestor-or-self::*/@xml:lang)[last()]}</r>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<r xml:lang=\"ene\"/>", false)
    );
  }

  /**
   *  Use an axis step on an expression which has static type item(), from within a predicate. .
   */
  @org.junit.Test
  public void k2Predicates8() {
    final XQuery query = new XQuery(
      "declare function local:foo($arg as item()) { $arg[@arg] }; local:foo(<e arg=\"\">result</e>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e arg=\"\">result</e>", false)
    );
  }

  /**
   *  Use an axis step on an expression which has static type item(), from within a path. .
   */
  @org.junit.Test
  public void k2Predicates9() {
    final XQuery query = new XQuery(
      "declare function local:foo($arg as item()) { string($arg/@arg) }; local:foo(<e arg=\"result\"/>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "result")
    );
  }

  /**
   *  Tests various filter optimizations .
   */
  @org.junit.Test
  public void cbclFilter001() {
    final XQuery query = new XQuery(
      "let $x := exists((1 to 10)[. mod 2 = 0]) return (1 to 100)[position() mod 2 = 0 and position() mod 3 = 0 and $x]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "6 12 18 24 30 36 42 48 54 60 66 72 78 84 90 96")
    );
  }

  /**
   *  Test that negated floating point comparisons against the position variable optimize correctly. .
   */
  @org.junit.Test
  public void cbclFilterexpr001() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:nan() { xs:float(\"NaN\") }; \n" +
      "      \t(1 to 10)[not(position() < xs:float(\"NaN\"))]\n" +
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
      assertStringValue(false, "1 2 3 4 5 6 7 8 9 10")
    );
  }

  /**
   *  test fn:first-in-sequence on sub-expression where quant = 1 .
   */
  @org.junit.Test
  public void cbclFirstInSequence001() {
    final XQuery query = new XQuery(
      "declare function local:generate($arg as xs:integer?) { if ($arg = 0) then (1, 2, 3) else () }; ( local:generate(0), 1, local:generate(0) )[1]",
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
   *  test fn:first-in-sequence on a for expression .
   */
  @org.junit.Test
  public void cbclFirstInSequence002() {
    final XQuery query = new XQuery(
      "declare function local:generate($arg as xs:integer?) as xs:integer* { if ($arg = 0) then (1, 2, 3) else $arg }; ( local:generate(()), for $x in local:generate(0) return $x + 2 )[1]",
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
   *  test fn:first-in-sequence on a for expression .
   */
  @org.junit.Test
  public void cbclFirstInSequence003() {
    final XQuery query = new XQuery(
      "declare function local:generate($arg as xs:integer?) as xs:integer? { if ($arg = 0) then 0 else () }; ( local:generate(()), for $x in local:generate(0) return $x + 2 )[1]",
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
   *  test fn:first-in-sequence on a for-at expression .
   */
  @org.junit.Test
  public void cbclFirstInSequence004() {
    final XQuery query = new XQuery(
      "declare function local:generate($arg as xs:integer?) as xs:integer* { if ($arg = 0) then ( 1, 2, 3 ) else ( $arg ) }; ( local:generate(()), for $x at $p in local:generate(0) return $p + $x)[1]",
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
   *  test fn:first-in-sequence on a for-at expression .
   */
  @org.junit.Test
  public void cbclFirstInSequence005() {
    final XQuery query = new XQuery(
      "declare function local:generate($arg as xs:integer?) as xs:integer? { if ($arg = 0) then 1 else $arg }; ( local:generate(()), for $x at $p in local:generate(0) return $p + $x)[1]",
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
   *  test fn:first-in-sequence on a map expression .
   */
  @org.junit.Test
  public void cbclFirstInSequence006() {
    final XQuery query = new XQuery(
      "declare function local:generate($arg as xs:integer?) as xs:string* { if ($arg = 0) then ('a', 'b', 'c') else ('d' ) }; ( if (local:generate(1) = 'd') then () else 1, for $x in local:generate(0) return fn:lower-case($x))[1]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a")
    );
  }

  /**
   *  test fn:first-in-sequence on a map expression .
   */
  @org.junit.Test
  public void cbclFirstInSequence007() {
    final XQuery query = new XQuery(
      "declare function local:generate($arg as xs:integer?) as xs:integer? { $arg }; ( if (local:generate(0) = 0) then () else 1, for $x in local:generate(0) return -$x)[1]",
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
   *  test fn:first-in-sequence on a distinct-doc-order .
   */
  @org.junit.Test
  public void cbclFirstInSequence008() {
    final XQuery query = new XQuery(
      "( (<a><b>cheese</b></a>)/b )[1]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<b>cheese</b>", false)
    );
  }

  /**
   *  test fn:first-in-sequence on a for-each expression Author: Tim Mills .
   */
  @org.junit.Test
  public void cbclFirstInSequence009() {
    final XQuery query = new XQuery(
      "declare function local:generate($arg as xs:integer?) as xs:string* { if ($arg = 0) then ('a', 'b', 'c') else () }; ( local:generate(()), for $x in local:generate(0) return 3)[1]",
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
   *  test fn:boolean on fn:first-in-sequence .
   */
  @org.junit.Test
  public void cbclFirstInSequence010() {
    final XQuery query = new XQuery(
      "declare function local:generate($arg as xs:integer?) as xs:string* { if ($arg = 0) then ('a', 'b', 'c') else () }; boolean(local:generate(0)[1])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:boolean on fn:first-in-sequence .
   */
  @org.junit.Test
  public void cbclFirstInSequence011() {
    final XQuery query = new XQuery(
      "declare function local:generate($arg as xs:integer?) as xs:string* { if ($arg = 0) then ('a', 'b', 'c') else () }; boolean(local:generate(1)[1])",
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
   *  test fn:first-in-sequence on a for-each expression .
   */
  @org.junit.Test
  public void cbclFirstInSequence012() {
    final XQuery query = new XQuery(
      "declare function local:generate($arg as xs:boolean) as xs:string+ { if ($arg) then ('a', 'b', 'c') else ('A', 'B', 'C') }; ( for $x in local:generate(true()) return 3)[1]",
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
   *  Filter Expression. Simple Filter expression involving numeric data and (gt operator0) .
   */
  @org.junit.Test
  public void filterexpressionhc1() {
    final XQuery query = new XQuery(
      "(/works/employee[xs:integer(hours[1]) gt 20])",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<employee>\n   <empnum>E1</empnum>\n   <pnum>P1</pnum>\n   <hours>40</hours>\n  </employee><employee>\n   <empnum>E1</empnum>\n   <pnum>P3</pnum>\n   <hours>80</hours>\n  </employee><employee>\n   <empnum>E2</empnum>\n   <pnum>P1</pnum>\n   <hours>40</hours>\n  </employee><employee>\n   <empnum>E2</empnum>\n   <pnum>P2</pnum>\n   <hours>80</hours>\n  </employee><employee>\n   <empnum>E4</empnum>\n   <pnum>P4</pnum>\n   <hours>40</hours>\n  </employee><employee>\n   <empnum>E4</empnum>\n   <pnum>P5</pnum>\n   <hours>80</hours>\n  </employee>", false)
    );
  }

  /**
   *  Simple filter expression involving the ne operator .
   */
  @org.junit.Test
  public void filterexpressionhc10() {
    final XQuery query = new XQuery(
      "((1 to 11)[. ne 10])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2 3 4 5 6 7 8 9 11")
    );
  }

  /**
   *  Simple filter expression involving a boolean "and" expression .
   */
  @org.junit.Test
  public void filterexpressionhc11() {
    final XQuery query = new XQuery(
      "((1 to 11)[(. eq 10) and (. mod 5 eq 0)])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("10")
    );
  }

  /**
   *  Simple filter expression involving a boolean "or" expression .
   */
  @org.junit.Test
  public void filterexpressionhc12() {
    final XQuery query = new XQuery(
      "((1 to 11)[(. eq 10) or (. eq 5)])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "5 10")
    );
  }

  /**
   *  Simple filter expression involving a division expression .
   */
  @org.junit.Test
  public void filterexpressionhc13() {
    final XQuery query = new XQuery(
      "((1,2,4,5,6,7,8,9,10,11)[(. div 2 eq 5)])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("10")
    );
  }

  /**
   *  Simple filter expression involving a multiplication expression .
   */
  @org.junit.Test
  public void filterexpressionhc14() {
    final XQuery query = new XQuery(
      "((1,2,4,5,6,7,8,9,10,11)[(. * 2 eq 10)])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("5")
    );
  }

  /**
   *  Simple filter expression involving an addition expression .
   */
  @org.junit.Test
  public void filterexpressionhc15() {
    final XQuery query = new XQuery(
      "((1,2,4,5,6,7,8,9,10,11)[(. + 2 eq 10)])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("8")
    );
  }

  /**
   *  Simple filter expression involving a subtration expression .
   */
  @org.junit.Test
  public void filterexpressionhc16() {
    final XQuery query = new XQuery(
      "((1,2,4,5,6,7,8,9,10,11)[(. - 2 eq 6)])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("8")
    );
  }

  /**
   *  Simple filter expression involving an integer division (idiv) expression .
   */
  @org.junit.Test
  public void filterexpressionhc17() {
    final XQuery query = new XQuery(
      "((1,2,4,5,6,7,8,9,10,11)[(. idiv 2 eq 3)])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "6 7")
    );
  }

  /**
   *  Simple filter expression using an xs:string function .
   */
  @org.junit.Test
  public void filterexpressionhc18() {
    final XQuery query = new XQuery(
      "((1,2,3,4,5,6,7,8,9,10,11)[(xs:string(.) eq \"3\")])",
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
   *  Simple filter expression involving two simple predicates .
   */
  @org.junit.Test
  public void filterexpressionhc19() {
    final XQuery query = new XQuery(
      "((1,2,3,4,5,6,7,8,9,10,11)[. gt 1][. gt 5])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "6 7 8 9 10 11")
    );
  }

  /**
   *  Simple filter expression involving integers. Return integer from 1 to 25 divisible by 2 .
   */
  @org.junit.Test
  public void filterexpressionhc2() {
    final XQuery query = new XQuery(
      "((1 to 25)[. mod 2 eq 0])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2 4 6 8 10 12 14 16 18 20 22 24")
    );
  }

  /**
   *  Simple filter expression involving two complex predicates .
   */
  @org.junit.Test
  public void filterexpressionhc20() {
    final XQuery query = new XQuery(
      "((1,2,3,4,5,6,7,8,9,10,11)[(. gt 1) and (. gt 2)][(. gt 5) and (. gt 6)])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "7 8 9 10 11")
    );
  }

  /**
   *  Simple filter expression involving the "fn:true" function .
   */
  @org.junit.Test
  public void filterexpressionhc21() {
    final XQuery query = new XQuery(
      "((1,2,3,4,5,6,7,8,9,10,11)[fn:true()])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2 3 4 5 6 7 8 9 10 11")
    );
  }

  /**
   *  Simple filter expression involving the "fn:false" function Use fn;count to avoid empty sequence. .
   */
  @org.junit.Test
  public void filterexpressionhc22() {
    final XQuery query = new XQuery(
      "fn:count(((1,2,3,4,5,6,7,8,9,10,11)[fn:false()]))",
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
        assertEq("0")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Simple filter expression that returns a sigle number .
   */
  @org.junit.Test
  public void filterexpressionhc3() {
    final XQuery query = new XQuery(
      "((1 to 25)[25])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("25")
    );
  }

  /**
   *  Simple filter expression involving union operator .
   */
  @org.junit.Test
  public void filterexpressionhc4() {
    final XQuery query = new XQuery(
      "(//empnum | (/))//employee[xs:integer(hours[1]) gt 20]",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<employee>\n   <empnum>E1</empnum>\n   <pnum>P1</pnum>\n   <hours>40</hours>\n  </employee><employee>\n   <empnum>E1</empnum>\n   <pnum>P3</pnum>\n   <hours>80</hours>\n  </employee><employee>\n   <empnum>E2</empnum>\n   <pnum>P1</pnum>\n   <hours>40</hours>\n  </employee><employee>\n   <empnum>E2</empnum>\n   <pnum>P2</pnum>\n   <hours>80</hours>\n  </employee><employee>\n   <empnum>E4</empnum>\n   <pnum>P4</pnum>\n   <hours>40</hours>\n  </employee><employee>\n   <empnum>E4</empnum>\n   <pnum>P5</pnum>\n   <hours>80</hours>\n  </employee>", false)
    );
  }

  /**
   *  Simple filter expression as a stept in a path expression involving the "last" function .
   */
  @org.junit.Test
  public void filterexpressionhc5() {
    final XQuery query = new XQuery(
      "(//employee[fn:last()])",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<employee>\n   <empnum>E4</empnum>\n   <pnum>P5</pnum>\n   <hours>80</hours>\n  </employee>", false)
    );
  }

  /**
   *  Simple filter expression involving the ge operator .
   */
  @org.junit.Test
  public void filterexpressionhc6() {
    final XQuery query = new XQuery(
      "((1 to 25)[. ge 10])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25")
    );
  }

  /**
   *  Simple filter expression involving the lt opertor .
   */
  @org.junit.Test
  public void filterexpressionhc7() {
    final XQuery query = new XQuery(
      "((1 to 25)[. lt 10])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2 3 4 5 6 7 8 9")
    );
  }

  /**
   *  Simple filter expression involving the le operator .
   */
  @org.junit.Test
  public void filterexpressionhc8() {
    final XQuery query = new XQuery(
      "((1 to 25)[. le 10])",
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
   *  Simple filter expression involving the eq operator .
   */
  @org.junit.Test
  public void filterexpressionhc9() {
    final XQuery query = new XQuery(
      "((1 to 25)[. eq 10])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("10")
    );
  }

  /**
   *  Evaluation of a simple predicate with a "true" value (uses "fn:true"). .
   */
  @org.junit.Test
  public void predicates1() {
    final XQuery query = new XQuery(
      "(//integer[fn:true()])",
      ctx);
    try {
      query.context(node(file("docs/atomicns.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<integer>12678967543233</integer>", false)
    );
  }

  /**
   *  Evaluation of a simple predicate, that uses the "xs:decimal()" function. .
   */
  @org.junit.Test
  public void predicates10() {
    final XQuery query = new XQuery(
      "(/root/decimal[xs:decimal(.) = 12678967.543233])",
      ctx);
    try {
      query.context(node(file("docs/atomicns.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<decimal attr=\"12678967.543233\">12678967.543233</decimal>", false)
    );
  }

  /**
   *  Evaluation of a simple predicate, that uses the "xs:float()" function. .
   */
  @org.junit.Test
  public void predicates11() {
    final XQuery query = new XQuery(
      "(/root/float[xs:float(.) = xs:float(1267.43233E12)])",
      ctx);
    try {
      query.context(node(file("docs/atomicns.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<float>1267.43233E12</float>", false)
    );
  }

  /**
   *  Evaluation of a simple predicate, that uses the "xs:double()" function. .
   */
  @org.junit.Test
  public void predicates12() {
    final XQuery query = new XQuery(
      "(/root/double[xs:double(.) = 1267.43233E12])",
      ctx);
    try {
      query.context(node(file("docs/atomicns.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<double>1267.43233E12</double>", false)
    );
  }

  /**
   *  Evaluation of a simple predicate, that uses the "xs:boolean" function. .
   */
  @org.junit.Test
  public void predicates13() {
    final XQuery query = new XQuery(
      "(/root/boolean[xs:boolean(.) = fn:true()])",
      ctx);
    try {
      query.context(node(file("docs/atomicns.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<boolean>true</boolean>", false)
    );
  }

  /**
   *  Evaluation of a simple predicate, that uses the "xs:date()" function. .
   */
  @org.junit.Test
  public void predicates14() {
    final XQuery query = new XQuery(
      "(/root/date[xs:date(.) = xs:date(\"2000-01-01+05:00\")])",
      ctx);
    try {
      query.context(node(file("docs/atomicns.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<date>2000-01-01+05:00</date>", false)
    );
  }

  /**
   *  Evaluation of a simple predicate, that uses the "eq" operator. .
   */
  @org.junit.Test
  public void predicates17() {
    final XQuery query = new XQuery(
      "(/works/employee[@name=\"Jane Doe 11\"])",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<employee name=\"Jane Doe 11\" gender=\"female\">\n   <empnum>E4</empnum>\n   <pnum>P2</pnum>\n   <hours>20</hours>\n  </employee>", false)
    );
  }

  /**
   *  Evaluation of a simple predicate, that uses the "ne" operator. .
   */
  @org.junit.Test
  public void predicates18() {
    final XQuery query = new XQuery(
      "(/works//day[xs:string(.) ne \"Monday\"])",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<day>Tuesday</day>", false)
    );
  }

  /**
   *  Evaluation of a simple predicate, that uses the "lt" operator. .
   */
  @org.junit.Test
  public void predicates19() {
    final XQuery query = new XQuery(
      "(/works//hours[xs:integer(.) lt 13])",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<hours>12</hours>", false)
    );
  }

  /**
   *  Evaluation of a simple predicate with a "false" value (uses "fn:false"). Use of fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void predicates2() {
    final XQuery query = new XQuery(
      "fn:count((//integer[fn:false()]))",
      ctx);
    try {
      query.context(node(file("docs/atomicns.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("0")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Evaluation of a simple predicate, that uses the "le" operator. .
   */
  @org.junit.Test
  public void predicates20() {
    final XQuery query = new XQuery(
      "(/works//hours[xs:integer(.) le 12])",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<hours>12</hours>", false)
    );
  }

  /**
   *  Evaluation of a simple predicate, that uses the "gt" operator. .
   */
  @org.junit.Test
  public void predicates21() {
    final XQuery query = new XQuery(
      "(/works//hours[xs:integer(.) gt 79])",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<hours>80</hours><hours>80</hours><hours>80</hours>", false)
    );
  }

  /**
   *  Evaluation of a simple predicate, that uses the "ge" operator. .
   */
  @org.junit.Test
  public void predicates22() {
    final XQuery query = new XQuery(
      "(/works//hours[xs:integer(.) ge 80])",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<hours>80</hours><hours>80</hours><hours>80</hours>", false)
    );
  }

  /**
   *  Evaluation of a simple predicate, that uses the "=" operator. .
   */
  @org.junit.Test
  public void predicates23() {
    final XQuery query = new XQuery(
      "(/works//hours[xs:integer(.) = 12])",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<hours>12</hours>", false)
    );
  }

  /**
   *  Evaluation of a simple predicate, that uses the "!=" operator. .
   */
  @org.junit.Test
  public void predicates24() {
    final XQuery query = new XQuery(
      "(/works[1]//employee[empnum != \"E1\" and empnum != \"E4\"])",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<employee name=\"Jane Doe 7\" gender=\"female\">\n   <empnum>E2</empnum>\n   <pnum>P1</pnum>\n   <hours>40</hours>\n  </employee><employee name=\"John Doe 8\" gender=\"male\">\n   <empnum>E2</empnum>\n   <pnum>P2</pnum>\n   <hours>80</hours>\n  </employee><employee name=\"Jane Doe 9\" gender=\"female\">\n   <empnum>E3</empnum>\n   <pnum>P2</pnum>\n   <hours>20</hours>\n  </employee><employee name=\"John Doe 10\" gender=\"male\">\n   <empnum>E3</empnum>\n   <pnum>P2</pnum>\n   <hours>20</hours>\n  </employee>", false)
    );
  }

  /**
   *  Evaluation of a simple predicate, that uses the "<" operator. .
   */
  @org.junit.Test
  public void predicates25() {
    final XQuery query = new XQuery(
      "(/works//hours[xs:integer(.) < 13])",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<hours>12</hours>", false)
    );
  }

  /**
   *  Evaluation of a simple predicate, that uses the "<=" operator. .
   */
  @org.junit.Test
  public void predicates26() {
    final XQuery query = new XQuery(
      "(/works//hours[xs:integer(.) <= 12])",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<hours>12</hours>", false)
    );
  }

  /**
   *  Evaluation of a simple predicate, that uses the ">" operator. .
   */
  @org.junit.Test
  public void predicates27() {
    final XQuery query = new XQuery(
      "(/works//hours[xs:integer(.) > 79])",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<hours>80</hours><hours>80</hours><hours>80</hours>", false)
    );
  }

  /**
   *  Evaluation of a simple predicate, that uses the ">=" operator. .
   */
  @org.junit.Test
  public void predicates28() {
    final XQuery query = new XQuery(
      "(/works//hours[xs:integer(.) >= 80])",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<hours>80</hours><hours>80</hours><hours>80</hours>", false)
    );
  }

  /**
   *  A negative test for numeric range used as filter expression. .
   */
  @org.junit.Test
  public void predicates29() {
    final XQuery query = new XQuery(
      "let $foo := <element1><element2>some content</element2></element1> return $foo[(2 to 5)]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0006")
    );
  }

  /**
   *  Evaluation of a simple predicate with a "true" value (uses "fn:false" and fn:not()). .
   */
  @org.junit.Test
  public void predicates3() {
    final XQuery query = new XQuery(
      "(//integer[fn:not(fn:false())])",
      ctx);
    try {
      query.context(node(file("docs/atomicns.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<integer>12678967543233</integer>", false)
    );
  }

  /**
   *  Evaluation of predicates reordering by an implementation .
   */
  @org.junit.Test
  public void predicates30() {
    final XQuery query = new XQuery(
      "for $x in /works/employee[fn:position() lt 5][fn:position() mod 2 eq 1] return (fn:data($x/empnum), fn:data($x/pnum))",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "E1 P1 E1 P3")
    );
  }

  /**
   *  Evaluation of predicates reordering by an implementation .
   */
  @org.junit.Test
  public void predicates31() {
    final XQuery query = new XQuery(
      "for $x in /works/employee[fn:position() mod 2 eq 1][fn:position() lt 5] return (fn:data($x/empnum), fn:data($x/pnum))",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "E1 P1 E1 P3 E1 P5 E2 P1")
    );
  }

  /**
   *  Evaluation of a simple predicate set to a boolean expression ("and" operator). .
   */
  @org.junit.Test
  public void predicates4() {
    final XQuery query = new XQuery(
      "(//integer[fn:true() and fn:true()])",
      ctx);
    try {
      query.context(node(file("docs/atomicns.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<integer>12678967543233</integer>", false)
    );
  }

  /**
   *  Evaluation of a simple predicate set to a boolean expression ("or" operator). .
   */
  @org.junit.Test
  public void predicates5() {
    final XQuery query = new XQuery(
      "(//integer[fn:true() or fn:true()])",
      ctx);
    try {
      query.context(node(file("docs/atomicns.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<integer>12678967543233</integer>", false)
    );
  }

  /**
   *  Evaluation of a simple predicate set to a boolean expression ("and" operator). Returns "false" Uses "fn:count" to avoid empty file. .
   */
  @org.junit.Test
  public void predicates6() {
    final XQuery query = new XQuery(
      "fn:count((//integer[fn:false() and fn:false()]))",
      ctx);
    try {
      query.context(node(file("docs/atomicns.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("0")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Evaluation of a simple predicate set to a boolean expression ("or" operator). Returns "false" Uses "fn:count" to avoid empty file. .
   */
  @org.junit.Test
  public void predicates7() {
    final XQuery query = new XQuery(
      "fn:count((//integer[fn:false() or fn:false()]))",
      ctx);
    try {
      query.context(node(file("docs/atomicns.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("0")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Evaluation of a simple predicate, that uses the "xs:string()" function. .
   */
  @org.junit.Test
  public void predicates8() {
    final XQuery query = new XQuery(
      "(/root/string[xs:string(.) = \"A String Function\"])",
      ctx);
    try {
      query.context(node(file("docs/atomicns.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<string>A String Function</string>", false)
    );
  }

  /**
   *  Evaluation of a simple predicate, that uses the "xs:integer()" function. .
   */
  @org.junit.Test
  public void predicates9() {
    final XQuery query = new XQuery(
      "(/root/integer[xs:integer(.) = 12678967543233])",
      ctx);
    try {
      query.context(node(file("docs/atomicns.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<integer>12678967543233</integer>", false)
    );
  }

  /**
   *  Evaluation of a simple predicate with a "true" value (uses "fn:true"). Use of fn:count to avoid empty file. Not Schema dependent. .
   */
  @org.junit.Test
  public void predicatesns1() {
    final XQuery query = new XQuery(
      "fn:count((//integer[fn:true()]))",
      ctx);
    try {
      query.context(node(file("docs/atomicns.xml")));
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
   *  Evaluation of a simple predicate, that uses the "xs:decimal()" function. Not Schema dependent. .
   */
  @org.junit.Test
  public void predicatesns10() {
    final XQuery query = new XQuery(
      "(/root/decimal[(xs:decimal(.) = 12678967.543233)])",
      ctx);
    try {
      query.context(node(file("docs/atomicns.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<decimal attr=\"12678967.543233\">12678967.543233</decimal>", false)
    );
  }

  /**
   *  Evaluation of a simple predicate, that uses the "xs:float()" function. Not Schema dependent . .
   */
  @org.junit.Test
  public void predicatesns11() {
    final XQuery query = new XQuery(
      "(/root/float[xs:float(.) = xs:float(1267.43233E12)])",
      ctx);
    try {
      query.context(node(file("docs/atomicns.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<float>1267.43233E12</float>", false)
    );
  }

  /**
   *  Evaluation of a simple predicate, that uses the "xs:double()" function. Not Schema dependent. .
   */
  @org.junit.Test
  public void predicatesns12() {
    final XQuery query = new XQuery(
      "(/root/double[xs:double(.) = 1267.43233E12])",
      ctx);
    try {
      query.context(node(file("docs/atomicns.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<double>1267.43233E12</double>", false)
    );
  }

  /**
   *  Evaluation of a simple predicate, that uses the "xs:boolean" function. Not Schema dependent. .
   */
  @org.junit.Test
  public void predicatesns13() {
    final XQuery query = new XQuery(
      "(/root/boolean[xs:boolean(.) = fn:true()])",
      ctx);
    try {
      query.context(node(file("docs/atomicns.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<boolean>true</boolean>", false)
    );
  }

  /**
   *  Evaluation of a simple predicate, that uses the "xs:date()" function. Not schema dependent. .
   */
  @org.junit.Test
  public void predicatesns14() {
    final XQuery query = new XQuery(
      "(/root/date[xs:date(.) = xs:date(\"2000-01-01+05:00\")])",
      ctx);
    try {
      query.context(node(file("docs/atomicns.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<date>2000-01-01+05:00</date>", false)
    );
  }

  /**
   *  Evaluation of a simple predicate with a "false" value (uses "fn:false"). Use of fn:count to avoid empty file. Not Schema dependent. Use fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void predicatesns2() {
    final XQuery query = new XQuery(
      "fn:count((//integer[fn:false()]))",
      ctx);
    try {
      query.context(node(file("docs/atomicns.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("0")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Evaluation of a simple predicate with a "true" value (uses "fn:false" and fn:not()). Not Schema dependent. .
   */
  @org.junit.Test
  public void predicatesns3() {
    final XQuery query = new XQuery(
      "(//integer[fn:not(fn:false())])",
      ctx);
    try {
      query.context(node(file("docs/atomicns.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<integer>12678967543233</integer>", false)
    );
  }

  /**
   *  Evaluation of a simple predicate set to a boolean expression ("and" operator). Not Schema dependent. .
   */
  @org.junit.Test
  public void predicatesns4() {
    final XQuery query = new XQuery(
      "(//integer[fn:true() and fn:true()])",
      ctx);
    try {
      query.context(node(file("docs/atomicns.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<integer>12678967543233</integer>", false)
    );
  }

  /**
   *  Evaluation of a simple predicate set to a boolean expression ("or" operator). Not Schema dependent. .
   */
  @org.junit.Test
  public void predicatesns5() {
    final XQuery query = new XQuery(
      "(//integer[fn:true() or fn:true()])",
      ctx);
    try {
      query.context(node(file("docs/atomicns.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<integer>12678967543233</integer>", false)
    );
  }

  /**
   *  Evaluation of a simple predicate set to a boolean expression ("and" operator). Returns "false" Uses "fn:count" to avoid empty file. Not Schema dependent. .
   */
  @org.junit.Test
  public void predicatesns6() {
    final XQuery query = new XQuery(
      "fn:count((//integer[fn:false() and fn:false()]))",
      ctx);
    try {
      query.context(node(file("docs/atomicns.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("0")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Evaluation of a simple predicate set to a boolean expression ("or" operator). Returns "false" Uses "fn:count" to avoid empty file. Not Schema dependent. .
   */
  @org.junit.Test
  public void predicatesns7() {
    final XQuery query = new XQuery(
      "fn:count((//integer[fn:false() or fn:false()]))",
      ctx);
    try {
      query.context(node(file("docs/atomicns.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("0")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Evaluation of a simple predicate, that uses the "xs:string()" function. Not Schema dependent. .
   */
  @org.junit.Test
  public void predicatesns8() {
    final XQuery query = new XQuery(
      "(/root/string[xs:string(.) = \"A String Function\"])",
      ctx);
    try {
      query.context(node(file("docs/atomicns.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<string>A String Function</string>", false)
    );
  }

  /**
   *  Evaluation of a simple predicate, that uses the "xs:integer()" function. Not Schema dependent. .
   */
  @org.junit.Test
  public void predicatesns9() {
    final XQuery query = new XQuery(
      "(/root/integer[xs:integer(.) = 12678967543233])",
      ctx);
    try {
      query.context(node(file("docs/atomicns.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<integer>12678967543233</integer>", false)
    );
  }
}
