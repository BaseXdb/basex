package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the head() function.
 *
 * @author BaseX Team 2005-12, BSD License
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
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
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
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
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
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
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }
}
