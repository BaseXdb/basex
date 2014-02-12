package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the uri-collection function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnUriCollection extends QT3TestSet {

  /**
   *  Pass an invalid xs:anyURI to fn:uri-collection(). Inspired by K2-SeqCollectionFunc-1..
   */
  @org.junit.Test
  public void k2SeqUriCollectionFunc1() {
    final XQuery query = new XQuery(
      "uri-collection(\"http:\\\\invalidURI\\someURI%gg\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODC0004")
    );
  }

  /**
   *  ':/ is an invalid URI. Inspired by K2-SeqCollectionFunc-2..
   */
  @org.junit.Test
  public void k2SeqUriCollectionFunc2() {
    final XQuery query = new XQuery(
      "uri-collection(\":/\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODC0004")
    );
  }

  /**
   *  Evaluation of an fn:uri-collection function with wrong arity.  Inspired by fn-collection-1. .
   */
  @org.junit.Test
  public void fnUriCollection1() {
    final XQuery query = new XQuery(
      "fn:uri-collection(\"argument1\",\"argument2\")",
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
   *  Evaluation of ana fn:uri-collection, which tries to retrieve a non-existent resource. Inspired by fn-collection-2. .
   */
  @org.junit.Test
  public void fnUriCollection2() {
    final XQuery query = new XQuery(
      "fn:uri-collection(\"thisfileshouldnotexists\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODC0002")
    );
  }

  /**
   *  Evaluation of ana fn:uri-collection with argument set to an invalid URI. Inspired by fn-collection-3..
   */
  @org.junit.Test
  public void fnUriCollection3() {
    final XQuery query = new XQuery(
      "fn:uri-collection(\"invalidURI%gg\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODC0004")
    );
  }

  /**
   * Unknown default resource collection. Inspired by collection-901..
   */
  @org.junit.Test
  public void uriCollection901() {
    final XQuery query = new XQuery(
      "uri-collection()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODC0002")
    );
  }

  /**
   * Unknown default resource collection..
   */
  @org.junit.Test
  public void uriCollection903() {
    final XQuery query = new XQuery(
      "uri-collection(())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODC0002")
    );
  }
}
