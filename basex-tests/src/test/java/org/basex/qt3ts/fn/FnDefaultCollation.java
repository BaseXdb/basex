package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the default-collation() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnDefaultCollation extends QT3TestSet {

  /**
   *  A test whose essence is: `default-collation(.)`. .
   */
  @org.junit.Test
  public void kContextDefaultCollationFunc1() {
    final XQuery query = new XQuery(
      "default-collation(.)",
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
   *  A test whose essence is: `default-collation(1, 2)`. .
   */
  @org.junit.Test
  public void kContextDefaultCollationFunc2() {
    final XQuery query = new XQuery(
      "default-collation(1, 2)",
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
   *  A test whose essence is: `default-collation() eq "http://www.w3.org/2005/xpath-functions/collation/codepoint"`. .
   */
  @org.junit.Test
  public void kContextDefaultCollationFunc3() {
    final XQuery query = new XQuery(
      "default-collation() eq \"http://www.w3.org/2005/xpath-functions/collation/codepoint\"",
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
   *  test evalaution of fn:default-collation Author: Tim Mills .
   */
  @org.junit.Test
  public void cbclDefaultCollation001() {
    final XQuery query = new XQuery(
      "fn:count(fn:default-collation())",
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
   *  test evalaution of fn:boolean on fn:default-collation Author: Tim Mills .
   */
  @org.junit.Test
  public void cbclDefaultCollation002() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:default-collation())",
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
   *  Evaluation of an fn:default-collation function with wrong arity. .
   */
  @org.junit.Test
  public void fnDefaultCollation1() {
    final XQuery query = new XQuery(
      "fn:default-collation(\"An Argument\")",
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
   *  Evaluation of an fn:default-collation function that retrieves the default collation. .
   */
  @org.junit.Test
  public void fnDefaultCollation2() {
    final XQuery query = new XQuery(
      "fn:default-collation()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.w3.org/2005/xpath-functions/collation/codepoint")
    );
  }
}
