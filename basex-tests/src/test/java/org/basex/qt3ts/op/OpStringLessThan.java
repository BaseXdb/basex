package org.basex.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the string-less-than operator (not actually defined as such in F+O).
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpStringLessThan extends QT3TestSet {

  /**
   *  A test whose essence is: `'a' lt 'abc'`. .
   */
  @org.junit.Test
  public void kStringLT1() {
    final XQuery query = new XQuery(
      "'a' lt 'abc'",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not('abc' lt 'a')`. .
   */
  @org.junit.Test
  public void kStringLT2() {
    final XQuery query = new XQuery(
      "not('abc' lt 'a')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `'a' le 'abc'`. .
   */
  @org.junit.Test
  public void kStringLT3() {
    final XQuery query = new XQuery(
      "'a' le 'abc'",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not('abc' le 'a')`. .
   */
  @org.junit.Test
  public void kStringLT4() {
    final XQuery query = new XQuery(
      "not('abc' le 'a')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `'abc' le 'abc'`. .
   */
  @org.junit.Test
  public void kStringLT5() {
    final XQuery query = new XQuery(
      "'abc' le 'abc'",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Compare two large codepoints. .
   */
  @org.junit.Test
  public void k2StringLT1() {
    final XQuery query = new XQuery(
      "\"\uea60\" lt \"\ud804\udd70\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test string comparison .
   */
  @org.junit.Test
  public void cbclStringLessThan001() {
    final XQuery query = new XQuery(
      "not(string(current-time()) lt \"now\")",
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
   *  test string comparison .
   */
  @org.junit.Test
  public void cbclStringLessThan002() {
    final XQuery query = new XQuery(
      "not(string(current-time()) ge \"now\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test string comparison .
   */
  @org.junit.Test
  public void cbclStringLessThan003() {
    final XQuery query = new XQuery(
      "\n" +
      "        not(xs:untypedAtomic(current-time()) lt xs:untypedAtomic(\"now\"))\n" +
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
      assertBoolean(false)
    );
  }

  /**
   *  test string comparison .
   */
  @org.junit.Test
  public void cbclStringLessThan004() {
    final XQuery query = new XQuery(
      "\n" +
      "        not(xs:untypedAtomic(current-time()) ge xs:untypedAtomic(\"now\"))\n" +
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
      assertBoolean(true)
    );
  }
}
