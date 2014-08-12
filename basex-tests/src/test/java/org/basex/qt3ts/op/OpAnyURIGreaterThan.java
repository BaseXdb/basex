package org.basex.qt3ts.op;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the anyURI-greater-than operator (not actually defined as such in F+O).
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpAnyURIGreaterThan extends QT3TestSet {

  /**
   *  Invoked 'le' on xs:anyURI values. .
   */
  @org.junit.Test
  public void k2AnyURILeGe1() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://example.com/A\") le xs:anyURI(\"http://example.com/B\")",
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
  public void k2AnyURILeGe10() {
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

  /**
   *  Invoked 'le' on xs:anyURI values. .
   */
  @org.junit.Test
  public void k2AnyURILeGe2() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://example.com/A\") le xs:anyURI(\"http://example.com/A\")",
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
   *  Invoked 'le' on xs:anyURI values. .
   */
  @org.junit.Test
  public void k2AnyURILeGe3() {
    final XQuery query = new XQuery(
      "not(xs:anyURI(\"http://example.com/B\") le xs:anyURI(\"http://example.com/A\"))",
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
  public void k2AnyURILeGe4() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://example.com/B\") ge xs:anyURI(\"http://example.com/A\")",
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
  public void k2AnyURILeGe5() {
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
  public void k2AnyURILeGe6() {
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
   *  Invoked 'le' on xs:anyURI values. .
   */
  @org.junit.Test
  public void k2AnyURILeGe7() {
    final XQuery query = new XQuery(
      "xs:string(\"http://example.com/A\") le xs:anyURI(\"http://example.com/A\")",
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
   *  Invoked 'le' on xs:anyURI values. .
   */
  @org.junit.Test
  public void k2AnyURILeGe8() {
    final XQuery query = new XQuery(
      "not(xs:anyURI(\"http://example.com/B\") le xs:string(\"http://example.com/A\"))",
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
  public void k2AnyURILeGe9() {
    final XQuery query = new XQuery(
      "xs:string(\"http://example.com/B\") gt xs:anyURI(\"http://example.com/A\")",
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
