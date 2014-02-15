package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the local-name-from-qname() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnLocalNameFromQName extends QT3TestSet {

  /**
   *  A test whose essence is: `local-name-from-QName()`. .
   */
  @org.junit.Test
  public void kLocalNameFromQNameFunc1() {
    final XQuery query = new XQuery(
      "local-name-from-QName()",
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
   *  A test whose essence is: `local-name-from-QName(1, 2)`. .
   */
  @org.junit.Test
  public void kLocalNameFromQNameFunc2() {
    final XQuery query = new XQuery(
      "local-name-from-QName(1, 2)",
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
   *  A test whose essence is: `empty(local-name-from-QName( () ))`. .
   */
  @org.junit.Test
  public void kLocalNameFromQNameFunc3() {
    final XQuery query = new XQuery(
      "empty(local-name-from-QName( () ))",
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
   *  A test whose essence is: `local-name-from-QName( QName("example.com/", "pre:lname")) eq "lname"`. .
   */
  @org.junit.Test
  public void kLocalNameFromQNameFunc4() {
    final XQuery query = new XQuery(
      "local-name-from-QName( QName(\"example.com/\", \"pre:lname\")) eq \"lname\"",
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
   *  Test function fn:local-name-from-QName. Empty sequence literal as input .
   */
  @org.junit.Test
  public void localNameFromQNameFunc006() {
    final XQuery query = new XQuery(
      "fn:local-name-from-QName(())",
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
   *  Test function fn:local-name-from-QName. Empty sequence literal as input .
   */
  @org.junit.Test
  public void localNameFromQNameFunc007() {
    final XQuery query = new XQuery(
      "fn:local-name-from-QName(((),()))",
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
   *  Test function fn:local-name-from-QName. Error case - invalid parameter type (string) .
   */
  @org.junit.Test
  public void localNameFromQNameFunc009() {
    final XQuery query = new XQuery(
      "fn:local-name-from-QName(\"\")",
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
   *  Test function fn:local-name-from-QName. Error case - no input parameter .
   */
  @org.junit.Test
  public void localNameFromQNameFunc011() {
    final XQuery query = new XQuery(
      "fn:local-name-from-QName()",
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
   *  Test function fn:local-name-from-QName. Error case - invalid parameter type (simple type) .
   */
  @org.junit.Test
  public void localNameFromQNameFunc015() {
    xquery10();
    final XQuery query = new XQuery(
      "fn:local-name-from-QName((//Folder)[1])",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx.xml")));
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
   *  Test function fn:local-name-from-QName. Error case - invalid parameter type (simple type) .
   */
  @org.junit.Test
  public void localNameFromQNameFunc015a() {
    final XQuery query = new XQuery(
      "fn:local-name-from-QName((//Folder)[1])",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0117")
    );
  }

  /**
   *  Test function fn:local-name-from-QName. Error case - invalid parameter type (integer) .
   */
  @org.junit.Test
  public void localNameFromQNameFunc016() {
    final XQuery query = new XQuery(
      "fn:local-name-from-QName(xs:integer(\"100\"))",
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
   *  Test function fn:local-name-from-QName. Error case - invalid parameter type (time) .
   */
  @org.junit.Test
  public void localNameFromQNameFunc017() {
    final XQuery query = new XQuery(
      "fn:local-name-from-QName(xs:time(\"12:00:00Z\"))",
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
}
