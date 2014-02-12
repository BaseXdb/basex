package org.basex.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the string-greater-than operator (not actually defined as such in F+O).
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpStringGreaterThan extends QT3TestSet {

  /**
   *  A test whose essence is: `'abc' gt 'a'`. .
   */
  @org.junit.Test
  public void kStringGT1() {
    final XQuery query = new XQuery(
      "'abc' gt 'a'",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not('abc' gt 'abc')`. .
   */
  @org.junit.Test
  public void kStringGT2() {
    final XQuery query = new XQuery(
      "not('abc' gt 'abc')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `'abc' ge 'a'`. .
   */
  @org.junit.Test
  public void kStringGT3() {
    final XQuery query = new XQuery(
      "'abc' ge 'a'",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not('a' ge 'abc')`. .
   */
  @org.junit.Test
  public void kStringGT4() {
    final XQuery query = new XQuery(
      "not('a' ge 'abc')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `'abc' ge 'abc'`. .
   */
  @org.junit.Test
  public void kStringGT5() {
    final XQuery query = new XQuery(
      "'abc' ge 'abc'",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
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
  public void cbclStringGreaterThan001() {
    final XQuery query = new XQuery(
      "not(string(current-time()) gt \"now\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
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
  public void cbclStringGreaterThan002() {
    final XQuery query = new XQuery(
      "not(string(current-time()) le \"now\")",
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
  public void cbclStringGreaterThan003() {
    final XQuery query = new XQuery(
      "\n" +
      "        not(xs:untypedAtomic(current-time()) gt xs:untypedAtomic(\"now\"))\n" +
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

  /**
   *  test string comparison .
   */
  @org.junit.Test
  public void cbclStringGreaterThan004() {
    final XQuery query = new XQuery(
      "\n" +
      "        not(xs:untypedAtomic(current-time()) le xs:untypedAtomic(\"now\"))\n" +
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
}
