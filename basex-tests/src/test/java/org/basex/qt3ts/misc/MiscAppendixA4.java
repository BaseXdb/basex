package org.basex.qt3ts.misc;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the AppendixA4 operator.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class MiscAppendixA4 extends QT3TestSet {

  /**
   *  Precedence order for "+" and "*". .
   */
  @org.junit.Test
  public void appendixA41() {
    final XQuery query = new XQuery(
      "-3 + 5 * 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("12")
    );
  }

  /**
   *  Precedence order for "-" and "*". .
   */
  @org.junit.Test
  public void appendixA42() {
    final XQuery query = new XQuery(
      "3 - 5 * 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-12")
    );
  }

  /**
   *  Precedence order for "+" and "div". .
   */
  @org.junit.Test
  public void appendixA43() {
    final XQuery query = new XQuery(
      "3 + 10 div 2",
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
   *  Precedence order for "-" and "div". .
   */
  @org.junit.Test
  public void appendixA44() {
    final XQuery query = new XQuery(
      "5 - 10 div 2",
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
   *  Precedence order for "+" and "idiv". .
   */
  @org.junit.Test
  public void appendixA45() {
    final XQuery query = new XQuery(
      "5 + 10 idiv 4",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("7")
    );
  }

  /**
   *  Precedence order for "-" and "idiv". .
   */
  @org.junit.Test
  public void appendixA46() {
    final XQuery query = new XQuery(
      "5 - 10 idiv 4",
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
   *  Precedence order for "+" and "mod". .
   */
  @org.junit.Test
  public void appendixA47() {
    final XQuery query = new XQuery(
      "5 + 10 mod 4",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("7")
    );
  }

  /**
   *  Precedence order for "-" and "mod". .
   */
  @org.junit.Test
  public void appendixA48() {
    final XQuery query = new XQuery(
      "5 - 10 mod 4",
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
   *  Precedence order for unary "-" and unary "+". .
   */
  @org.junit.Test
  public void appendixA49() {
    final XQuery query = new XQuery(
      "-+7",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-7")
    );
  }
}
