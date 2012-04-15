package org.basex.test.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the string-less-than operator (not actually defined as such in F+O).
 *
 * @author BaseX Team 2005-12, BSD License
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }
}
