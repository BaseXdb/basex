package org.basex.test.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the string-greater-than operator (not actually defined as such in F+O).
 *
 * @author BaseX Team 2005-12, BSD License
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }
}
