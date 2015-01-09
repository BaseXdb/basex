package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the PathExpr production.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdPathExpr extends QT3TestSet {

  /**
   *  Leading lone slash syntax contraints .
   */
  @org.junit.Test
  public void pathExpr1() {
    final XQuery query = new XQuery(
      "fn:count(.[5 * /])",
      ctx);
    try {
      query.context(node(file("prod/PathExpr/OneTopElement.xml")));
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
   *  Leading lone slash syntax contraints .
   */
  @org.junit.Test
  public void pathExpr10() {
    final XQuery query = new XQuery(
      "fn:count(.[if (doclevel) then / else /*])",
      ctx);
    try {
      query.context(node(file("prod/PathExpr/OneTopElement.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  Leading lone slash syntax contraints .
   */
  @org.junit.Test
  public void pathExpr11() {
    final XQuery query = new XQuery(
      "let $a := . return fn:count(.[/ is $a])",
      ctx);
    try {
      query.context(node(file("prod/PathExpr/OneTopElement.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  Leading lone slash syntax contraints .
   */
  @org.junit.Test
  public void pathExpr12() {
    final XQuery query = new XQuery(
      "fn:count(.[/ instance of document-node(schema-element(x))])",
      ctx);
    try {
      query.context(node(file("prod/PathExpr/OneTopElement.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  Leading lone slash syntax contraints .
   */
  @org.junit.Test
  public void pathExpr13() {
    final XQuery query = new XQuery(
      "fn:count(.[let $doc := / return $doc/*])",
      ctx);
    try {
      query.context(node(file("prod/PathExpr/OneTopElement.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  Leading lone slash syntax contraints .
   */
  @org.junit.Test
  public void pathExpr14() {
    final XQuery query = new XQuery(
      "fn:count(.[/<a/>])",
      ctx);
    try {
      query.context(node(file("prod/PathExpr/OneTopElement.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Leading lone slash syntax contraints .
   */
  @org.junit.Test
  public void pathExpr15() {
    final XQuery query = new XQuery(
      "fn:count(.[/-5])",
      ctx);
    try {
      query.context(node(file("prod/PathExpr/OneTopElement.xml")));
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
   *  Leading lone slash syntax contraints .
   */
  @org.junit.Test
  public void pathExpr16() {
    final XQuery query = new XQuery(
      "let $a := . return fn:count(.[/=$a])",
      ctx);
    try {
      query.context(node(file("prod/PathExpr/OneTopElement.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Leading lone slash syntax contraints .
   */
  @org.junit.Test
  public void pathExpr2() {
    final XQuery query = new XQuery(
      "fn:count(.[(/) * 5])",
      ctx);
    try {
      query.context(node(file("prod/PathExpr/OneTopElement.xml")));
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
   *  Leading lone slash syntax contraints .
   */
  @org.junit.Test
  public void pathExpr3() {
    final XQuery query = new XQuery(
      "fn:count(.[/ * 5])",
      ctx);
    try {
      query.context(node(file("prod/PathExpr/OneTopElement.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  Leading lone slash syntax contraints .
   */
  @org.junit.Test
  public void pathExpr4() {
    final XQuery query = new XQuery(
      "fn:count(.[(/) < 5])",
      ctx);
    try {
      query.context(node(file("prod/PathExpr/OneTopElement.xml")));
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
   *  Leading lone slash syntax contraints .
   */
  @org.junit.Test
  public void pathExpr5() {
    final XQuery query = new XQuery(
      "fn:count(.[/ < 5])",
      ctx);
    try {
      query.context(node(file("prod/PathExpr/OneTopElement.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  Leading lone slash syntax contraints .
   */
  @org.junit.Test
  public void pathExpr6() {
    final XQuery query = new XQuery(
      "fn:count(.[5</])",
      ctx);
    try {
      query.context(node(file("prod/PathExpr/OneTopElement.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Leading lone slash syntax contraints .
   */
  @org.junit.Test
  public void pathExpr7() {
    final XQuery query = new XQuery(
      "fn:count(.[/ < a])",
      ctx);
    try {
      query.context(node(file("prod/PathExpr/OneTopElement.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  Leading lone slash syntax contraints .
   */
  @org.junit.Test
  public void pathExpr8() {
    final XQuery query = new XQuery(
      "fn:count(.[/ < /b])",
      ctx);
    try {
      query.context(node(file("prod/PathExpr/OneTopElement.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  Leading lone slash syntax contraints .
   */
  @org.junit.Test
  public void pathExpr9() {
    final XQuery query = new XQuery(
      "fn:count(.[/<a div 3])",
      ctx);
    try {
      query.context(node(file("prod/PathExpr/OneTopElement.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  Evaluate error condition XPTY0018 .
   */
  @org.junit.Test
  public void pathExprErr2() {
    final XQuery query = new XQuery(
      "(<a>1</a>,<b>2</b>)/(if(position() eq 1) then . else data(.))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0018")
    );
  }
}
