package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the local-name-from-qname() function.
 *
 * @author BaseX Team 2005-12, BSD License
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  Test function fn:local-name-from-QName. Error case - invalid parameter type (simple type) .
   */
  @org.junit.Test
  public void localNameFromQNameFunc015() {
    final XQuery query = new XQuery(
      "fn:local-name-from-QName((//Folder)[1])",
      ctx);
    query.context(node(file("prod/ForClause/fsx.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }
}
