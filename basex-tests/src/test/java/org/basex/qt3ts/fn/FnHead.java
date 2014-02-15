package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the head() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnHead extends QT3TestSet {

  /**
   *  head() of a simple sequence .
   */
  @org.junit.Test
  public void head001() {
    final XQuery query = new XQuery(
      "head(3 to 10)",
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
   *  head() of a simple sequence .
   */
  @org.junit.Test
  public void head002() {
    final XQuery query = new XQuery(
      "head((\"a\", \"b\", current-dateTime()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("\"a\"")
    );
  }

  /**
   *  head() of a node sequence .
   */
  @org.junit.Test
  public void head003() {
    final XQuery query = new XQuery(
      "let $a := /works return head($a/*)/string(@name)",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Jane Doe 1")
    );
  }

  /**
   *  head() of a singleton sequence .
   */
  @org.junit.Test
  public void head004() {
    final XQuery query = new XQuery(
      "let $a := /works return name(head($a))",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "works")
    );
  }

  /**
   *  head() of an empty sequence .
   */
  @org.junit.Test
  public void head005() {
    final XQuery query = new XQuery(
      "head(/works/cucumber)",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
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
   *  head() preserves identity .
   */
  @org.junit.Test
  public void head006() {
    final XQuery query = new XQuery(
      "let $a := /works/* return (head($a) is $a[1])",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
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
