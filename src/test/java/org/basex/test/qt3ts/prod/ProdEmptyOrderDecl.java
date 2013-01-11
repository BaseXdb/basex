package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the EmptyOrderDecl production.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdEmptyOrderDecl extends QT3TestSet {

  /**
   *  A simple 'declare default order empty' declaration, specifying 'greatest'. .
   */
  @org.junit.Test
  public void kEmptyOrderProlog1() {
    final XQuery query = new XQuery(
      "declare(::)default order empty(::)greatest; 1 eq 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A simple 'declare default order empty' declaration, specifying 'least'. .
   */
  @org.junit.Test
  public void kEmptyOrderProlog2() {
    final XQuery query = new XQuery(
      "declare(::)default order empty(::)least; 1 eq 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Two 'declare default order empty' declarations are invalid. .
   */
  @org.junit.Test
  public void kEmptyOrderProlog3() {
    final XQuery query = new XQuery(
      "declare(::)default order empty(::)greatest; declare(::)default order empty(::)least; 1 eq 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0069")
    );
  }

  /**
   *  Evaluation of a prolog with two empty order declarations. .
   */
  @org.junit.Test
  public void emptyorderdecl1() {
    final XQuery query = new XQuery(
      "declare default order empty least; declare default order empty greatest;  \"aaa\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0069")
    );
  }

  /**
   *  Evaluation of empty order declaration set to "empty greatest" and a set of nodes (one empty) sorted in descending order. .
   */
  @org.junit.Test
  public void emptyorderdecl10() {
    final XQuery query = new XQuery(
      "declare default order empty greatest;  for $i in (<a>1</a>,<a>4</a>,<a></a>,<a>7</a>) order by zero-or-one($i/text()) descending return $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a/><a>7</a><a>4</a><a>1</a>", false)
    );
  }

  /**
   *  Evaluation of empty order declaration set to "empty greatest" and a set of nodes (two of them empty) sorted in descending order. .
   */
  @org.junit.Test
  public void emptyorderdecl11() {
    final XQuery query = new XQuery(
      "declare default order empty greatest;  for $i in (<a>1</a>,<a>4</a>,<a></a>,<a></a>,<a>7</a>) order by zero-or-one($i/text()) descending return $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a/><a/><a>7</a><a>4</a><a>1</a>", false)
    );
  }

  /**
   *  Evaluation of empty order declaration set to "empty greatest" and a set of numbers (one set to Nan Expression) sorted in descending order. .
   */
  @org.junit.Test
  public void emptyorderdecl12() {
    final XQuery query = new XQuery(
      "declare default order empty greatest;  for $i in (1,4,0 div 0E0,7) order by $i descending return $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NaN 7 4 1")
    );
  }

  /**
   *  Evaluation of empty order declaration set to "empty greatest" and a set of numbers (two of them set to Nan Expression) sorted in descending order. .
   */
  @org.junit.Test
  public void emptyorderdecl13() {
    final XQuery query = new XQuery(
      "declare default order empty greatest;  for $i in (1,4,0 div 0E0,0 div 0E0,7) order by $i descending return $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NaN NaN 7 4 1")
    );
  }

  /**
   *  Evaluation of empty order declaration set to "empty least" and a set of nodes (one empty) sorted in ascening order. .
   */
  @org.junit.Test
  public void emptyorderdecl14() {
    final XQuery query = new XQuery(
      "declare default order empty least;  for $i in (<a>1</a>,<a>4</a>,<a></a>,<a>7</a>) order by zero-or-one($i/text()) ascending return $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a/><a>1</a><a>4</a><a>7</a>", false)
    );
  }

  /**
   *  Evaluation of empty order declaration set to "empty least" and a set of nodes (two of them empty) sorted in ascending order. .
   */
  @org.junit.Test
  public void emptyorderdecl15() {
    final XQuery query = new XQuery(
      "declare default order empty least;  for $i in (<a>1</a>,<a>4</a>,<a></a>,<a></a>,<a>7</a>) order by zero-or-one($i/text()) ascending return $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a/><a/><a>1</a><a>4</a><a>7</a>", false)
    );
  }

  /**
   *  Evaluation of empty order declaration set to "empty least" and a set of numbers (one set to Nan Expression) sorted in ascending order. .
   */
  @org.junit.Test
  public void emptyorderdecl16() {
    final XQuery query = new XQuery(
      "declare default order empty least;  for $i in (1,4,0 div 0E0,7) order by $i ascending return $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NaN 1 4 7")
    );
  }

  /**
   *  Evaluation of empty order declaration set to "empty least" and a set of numbers (two of them set to Nan Expression) sorted in ascending order. .
   */
  @org.junit.Test
  public void emptyorderdecl17() {
    final XQuery query = new XQuery(
      "declare default order empty least;  for $i in (1,4,0 div 0E0,0 div 0E0,7) order by $i ascending return $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NaN NaN 1 4 7")
    );
  }

  /**
   *  Evaluation of empty order declaration set to "empty greatest" and a set of nodes (one empty) sorted in descending order. .
   */
  @org.junit.Test
  public void emptyorderdecl18() {
    final XQuery query = new XQuery(
      "declare default order empty least;  for $i in (<a>1</a>,<a>4</a>,<a></a>,<a>7</a>) order by zero-or-one($i/text()) descending return $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a>7</a><a>4</a><a>1</a><a/>", false)
    );
  }

  /**
   *  Evaluation of empty order declaration set to "empty greatest" and a set of nodes (two of them empty) sorted in descending order. .
   */
  @org.junit.Test
  public void emptyorderdecl19() {
    final XQuery query = new XQuery(
      "declare default order empty least;  for $i in (<a>1</a>,<a>4</a>,<a></a>,<a></a>,<a>7</a>) order by zero-or-one($i/text()) descending return $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a>7</a><a>4</a><a>1</a><a/><a/>", false)
    );
  }

  /**
   *  Evaluation of a prolog that specifies "empty greatest" with a set of nodes (one empty) and sort them in ascending order .
   */
  @org.junit.Test
  public void emptyorderdecl2() {
    final XQuery query = new XQuery(
      "declare default order empty greatest;  for $i in (<a>1</a>,<a>4</a>,<a></a>,<a>7</a>) order by zero-or-one($i/text()) ascending return $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a>1</a><a>4</a><a>7</a><a/>", false)
    );
  }

  /**
   *  Evaluation of empty order declaration set to "empty least" and a set of numbers (one set to Nan Expression) sorted in descending order. .
   */
  @org.junit.Test
  public void emptyorderdecl20() {
    final XQuery query = new XQuery(
      "declare default order empty least;  for $i in (1,4,0 div 0E0,7) order by $i descending return $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "7 4 1 NaN")
    );
  }

  /**
   *  Evaluation of empty order declaration set to "empty least" and a set of numbers (two of them set to Nan Expression) sorted in descending order. .
   */
  @org.junit.Test
  public void emptyorderdecl21() {
    final XQuery query = new XQuery(
      "declare default order empty least;  for $i in (1,4,0 div 0E0,0 div 0E0,7) order by $i descending return $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "7 4 1 NaN NaN")
    );
  }

  /**
   *  Evaluation of empty order declaration set to "empty greatest" and a set of nodes (one of them set to empty) sorted in ascending order and local order by overrides empty order declaration in prolog (empty least. .
   */
  @org.junit.Test
  public void emptyorderdecl22() {
    final XQuery query = new XQuery(
      "declare default order empty greatest;  for $i in (<a>1</a>,<a>4</a>,<a></a>,<a>7</a>) order by zero-or-one($i/text()) ascending empty least return $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a/><a>1</a><a>4</a><a>7</a>", false)
    );
  }

  /**
   *  Evaluation of empty order declaration set to "empty greatest" and a set of numbers (one of them set to a NaN expression) sorted in ascending order and local order by overrides empty order declaration in prolog (empty least. .
   */
  @org.junit.Test
  public void emptyorderdecl23() {
    final XQuery query = new XQuery(
      "declare default order empty greatest;  for $i in (1,4,3,0 div 0E0,7) order by $i ascending empty least return $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NaN 1 3 4 7")
    );
  }

  /**
   *  Evaluation of empty order declaration set to "empty greatest" and a set of nodes (one of them set to empty) sorted in ascending order and local order by overrides empty order declaration in prolog (empty greatest. .
   */
  @org.junit.Test
  public void emptyorderdecl24() {
    final XQuery query = new XQuery(
      "declare default order empty greatest;  for $i in (<a>1</a>,<a>4</a>,<a></a>,<a>7</a>) order by zero-or-one($i/text()) ascending empty greatest return $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a>1</a><a>4</a><a>7</a><a/>", false)
    );
  }

  /**
   *  Evaluation of empty order declaration set to "empty greatest" and a set of numbers (one of them set to a NaN expression) sorted in ascending order and local order by clause set to empty greatest. .
   */
  @org.junit.Test
  public void emptyorderdecl25() {
    final XQuery query = new XQuery(
      "declare default order empty greatest;  for $i in (1,4,3,0 div 0E0,7) order by $i ascending empty greatest return $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 3 4 7 NaN")
    );
  }

  /**
   *  Evaluation of empty order declaration set to "empty least" and a set of nodes (one of them set to empty) sorted in ascending order and a local order by clause that overriddes empty order declaration in prolog. .
   */
  @org.junit.Test
  public void emptyorderdecl26() {
    final XQuery query = new XQuery(
      "declare default order empty least;  for $i in (<a>1</a>,<a>4</a>,<a></a>,<a>7</a>) order by $i/text() ascending empty greatest return $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a>1</a><a>4</a><a>7</a><a/>", false)
    );
  }

  /**
   *  Evaluation of empty order declaration set to "empty least" and a set of numbers (one of them set to a NaN expression) sorted in ascending order and local order by that overriddes empty order declaration in prolog. .
   */
  @org.junit.Test
  public void emptyorderdecl27() {
    final XQuery query = new XQuery(
      "declare default order empty least;  for $i in (1,4,3,0 div 0E0,7) order by $i ascending empty greatest return $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 3 4 7 NaN")
    );
  }

  /**
   *  Evaluation of empty order declaration set to "empty least" and a set of nodes (one of them set to empty) sorted in ascending order and local order by clause set to the same value. .
   */
  @org.junit.Test
  public void emptyorderdecl28() {
    final XQuery query = new XQuery(
      "declare default order empty least;  for $i in (<a>1</a>,<a>4</a>,<a></a>,<a>7</a>) order by $i/text() ascending empty least return $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a/><a>1</a><a>4</a><a>7</a>", false)
    );
  }

  /**
   *  Evaluation of empty order declaration set to "empty least" and a set of numbers (one of them set to a NaN expression) sorted in ascending order and local order by clause set to the same value. .
   */
  @org.junit.Test
  public void emptyorderdecl29() {
    final XQuery query = new XQuery(
      "declare default order empty least;  for $i in (1,4,3,0 div 0E0,7) order by $i ascending empty least return $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NaN 1 3 4 7")
    );
  }

  /**
   *  Evaluation of a prolog that specifies "empty greatest" with a set of nodes (two empty) and sort them in ascending order .
   */
  @org.junit.Test
  public void emptyorderdecl3() {
    final XQuery query = new XQuery(
      "declare default order empty greatest;  for $i in (<a>1</a>,<a>4</a>,<a></a>,<a></a>,<a>7</a>) order by zero-or-one($i/text()) ascending return $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a>1</a><a>4</a><a>7</a><a/><a/>", false)
    );
  }

  /**
   *  Evaluation of a prolog that specifies "empty greatest" with a set of numbers (one that results in NaN) and sort them in ascending order .
   */
  @org.junit.Test
  public void emptyorderdecl4() {
    final XQuery query = new XQuery(
      "declare default order empty greatest;  for $i in (1,4,0 div 0E0,7) order by $i ascending return $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 4 7 NaN")
    );
  }

  /**
   *  Evaluation of a prolog that specifies "empty greatest" with a set of numbers (two that results in NaN) and sort them in ascending order .
   */
  @org.junit.Test
  public void emptyorderdecl5() {
    final XQuery query = new XQuery(
      "declare default order empty greatest;  for $i in (1,4,0 div 0E0,0 div 0E0,7) order by $i ascending return $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 4 7 NaN NaN")
    );
  }

  /**
   *  Evaluation of a prolog that specifies "empty greatest" with a set of nodes (one empty) and no order by clause .
   */
  @org.junit.Test
  public void emptyorderdecl6() {
    final XQuery query = new XQuery(
      "declare default order empty greatest;  for $i in (<a>1</a>,<a>4</a>,<a></a>,<a>7</a>) return $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a>1</a><a>4</a><a/><a>7</a>", false)
    );
  }

  /**
   *  Evaluation of a prolog that specifies "empty least" with a set of nodes (one empty) and no order by clause .
   */
  @org.junit.Test
  public void emptyorderdecl7() {
    final XQuery query = new XQuery(
      "declare default order empty least;  for $i in (<a>1</a>,<a>4</a>,<a></a>,<a>7</a>) return $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a>1</a><a>4</a><a/><a>7</a>", false)
    );
  }

  /**
   *  Evaluation of a prolog that specifies "empty greatest" with a set of numbers (one empty) and no order by clause .
   */
  @org.junit.Test
  public void emptyorderdecl8() {
    final XQuery query = new XQuery(
      "declare default order empty greatest;  for $i in (1,4,0 div 0E0,7) return $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 4 NaN 7")
    );
  }

  /**
   *  Evaluation of a prolog that specifies "empty least" with a set of numbers (one empty) and no order by clause .
   */
  @org.junit.Test
  public void emptyorderdecl9() {
    final XQuery query = new XQuery(
      "declare default order empty least;  for $i in (1,4,0 div 0E0,7) return $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 4 NaN 7")
    );
  }
}
