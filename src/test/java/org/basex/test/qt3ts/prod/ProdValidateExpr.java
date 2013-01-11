package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the ValidateExpr production.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdValidateExpr extends QT3TestSet {

  /**
   *  Ensure the validate keyword is parsed correctly. .
   */
  @org.junit.Test
  public void k2ValidateExpression1() {
    final XQuery query = new XQuery(
      "validate gt validate",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPDY0002")
    );
  }

  /**
   *  Test validation on a document with a single element with extra nodes .
   */
  @org.junit.Test
  public void cbclValidateexpr11() {
    final XQuery query = new XQuery(
      "validate { document { (<e/>, \"text\") } }",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0084")
    );
  }

  /**
   *  Test validation on a document with no element nodes. .
   */
  @org.junit.Test
  public void cbclValidateexpr12() {
    final XQuery query = new XQuery(
      "validate{ document { \"text\" } }",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0061")
    );
  }

  /**
   *  Test validation on a document with more than one element nodes. .
   */
  @org.junit.Test
  public void cbclValidateexpr13() {
    final XQuery query = new XQuery(
      "validate lax { document { (<a/>, <b/>) } }",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0061")
    );
  }

  /**
   *  Test For error condition XQDY0061 using a document node. .
   */
  @org.junit.Test
  public void validateexpr26() {
    final XQuery query = new XQuery(
      "\n" +
      "        validate { document { <a/>, <b/> } }\n" +
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
      (
        error("XQDY0061")
      ||
        error("XQDY0084")
      )
    );
  }

  /**
   *  namespace not declared: see bug 17040 (cezar.andrei@gmail.com). Also tests lax validation using xsi:type .
   */
  @org.junit.Test
  public void validateexpr34() {
    final XQuery query = new XQuery(
      "\n" +
      "        validate lax { <a xsi:type='xs:integer'>42</a> }\n" +
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
      error("XQDY0027")
    );
  }

  /**
   *  Lax validation using xsi:type .
   */
  @org.junit.Test
  public void validateexpr35() {
    final XQuery query = new XQuery(
      "\n" +
      "        validate lax { <a xsi:type='xs:integer' xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">42</a> }\n" +
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
      assertSerialization("<a xsi:type='xs:integer' \n           xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\n           xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">42</a>", false)
    );
  }
}
