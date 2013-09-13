package org.basex.test.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the anyURI-less-than operator (not actually defined as such in F+O).
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpAnyURILessThan extends QT3TestSet {

  /**
   *  Invoked 'lt' on xs:anyURI values. .
   */
  @org.junit.Test
  public void k2AnyURILtGt1() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://example.com/A\") lt xs:anyURI(\"http://example.com/B\")",
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
   *  Invoked 'lt' on xs:anyURI values. .
   */
  @org.junit.Test
  public void k2AnyURILtGt2() {
    final XQuery query = new XQuery(
      "not(xs:anyURI(\"http://example.com/B\") lt xs:anyURI(\"http://example.com/A\"))",
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
   *  Invoked 'lt' on xs:anyURI values. .
   */
  @org.junit.Test
  public void k2AnyURILtGt3() {
    final XQuery query = new XQuery(
      "xs:string(\"http://example.com/A\") lt xs:anyURI(\"http://example.com/B\")",
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
   *  Invoked 'lt' on xs:anyURI values. .
   */
  @org.junit.Test
  public void k2AnyURILtGt4() {
    final XQuery query = new XQuery(
      "not(xs:anyURI(\"http://example.com/B\") lt xs:string(\"http://example.com/A\"))",
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
   *  Invoked 'gt' on xs:anyURI values. .
   */
  @org.junit.Test
  public void k2AnyURILtGt5() {
    final XQuery query = new XQuery(
      "not(xs:anyURI(\"http://example.com/A\") gt xs:anyURI(\"http://example.com/B\"))",
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
   *  Invoked 'gt' on xs:anyURI values. .
   */
  @org.junit.Test
  public void k2AnyURILtGt6() {
    final XQuery query = new XQuery(
      "not(xs:string(\"http://example.com/A\") gt xs:anyURI(\"http://example.com/B\"))",
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
   *  Invoked 'gt' on xs:anyURI values. .
   */
  @org.junit.Test
  public void k2AnyURILtGt7() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://example.com/B\") gt xs:anyURI(\"http://example.com/A\")",
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
   *  Invoked 'gt' on xs:anyURI values. .
   */
  @org.junit.Test
  public void k2AnyURILtGt8() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://example.com/B\") gt xs:string(\"http://example.com/A\")",
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
