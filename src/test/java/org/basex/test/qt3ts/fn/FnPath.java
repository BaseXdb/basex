package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * tests for the fn:path() function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnPath extends QT3TestSet {

  /**
   * path() applied to empty sequence.
   */
  @org.junit.Test
  public void path001() {
    final XQuery query = new XQuery(
      "fn:path(())",
      ctx);
    query.context(node(file("fn/path/pathdata.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertEmpty()
    );
  }

  /**
   * path() applied to top-level element.
   */
  @org.junit.Test
  public void path002() {
    final XQuery query = new XQuery(
      "fn:path(/*)",
      ctx);
    query.context(node(file("fn/path/pathdata.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "/Q{http://www.w3.org/2010/09/qt-fots-catalog}test-set[1]")
      &&
        assertType("xs:string")
      )
    );
  }

  /**
   * path() applied to nested element.
   */
  @org.junit.Test
  public void path003() {
    final XQuery query = new XQuery(
      "fn:path((//*:all-of)[1])",
      ctx);
    query.context(node(file("fn/path/pathdata.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "/Q{http://www.w3.org/2010/09/qt-fots-catalog}test-set[1]/Q{http://www.w3.org/2010/09/qt-fots-catalog}test-case[1]/Q{http://www.w3.org/2010/09/qt-fots-catalog}result[1]/Q{http://www.w3.org/2010/09/qt-fots-catalog}all-of[1]")
      &&
        assertType("xs:string")
      )
    );
  }

  /**
   * path() applied to non-first sibling element.
   */
  @org.junit.Test
  public void path004() {
    final XQuery query = new XQuery(
      "fn:path(//*[@name=\"fn-absintg1args-1\"])",
      ctx);
    query.context(node(file("fn/path/pathdata.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "/Q{http://www.w3.org/2010/09/qt-fots-catalog}test-set[1]/Q{http://www.w3.org/2010/09/qt-fots-catalog}test-case[4]")
      &&
        assertType("xs:string")
      )
    );
  }

  /**
   * path() applied to no-namespace attribute.
   */
  @org.junit.Test
  public void path005() {
    final XQuery query = new XQuery(
      "fn:path((//@idref)[1])",
      ctx);
    query.context(node(file("fn/path/pathdata.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "/Q{http://www.w3.org/2010/09/qt-fots-catalog}test-set[1]/Q{http://www.w3.org/2010/09/qt-fots-catalog}link[1]/@idref")
      &&
        assertType("xs:string")
      )
    );
  }

  /**
   * path() applied to namespaced attribute.
   */
  @org.junit.Test
  public void path006() {
    final XQuery query = new XQuery(
      "fn:path((//*:source)[3]/@xml:id)",
      ctx);
    query.context(node(file("fn/path/pathdata.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "/Q{http://www.w3.org/2010/09/qt-fots-catalog}test-set[1]/Q{http://www.w3.org/2010/09/qt-fots-catalog}environment[3]/Q{http://www.w3.org/2010/09/qt-fots-catalog}source[1]/@Q{http://www.w3.org/XML/1998/namespace}id")
      &&
        assertType("xs:string")
      )
    );
  }

  /**
   * path() applied to comment.
   */
  @org.junit.Test
  public void path007() {
    final XQuery query = new XQuery(
      "fn:path((//comment())[2])",
      ctx);
    query.context(node(file("fn/path/pathdata.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "/Q{http://www.w3.org/2010/09/qt-fots-catalog}test-set[1]/Q{http://www.w3.org/2010/09/qt-fots-catalog}test-case[2]/Q{http://www.w3.org/2010/09/qt-fots-catalog}description[1]/comment()[1]")
      &&
        assertType("xs:string")
      )
    );
  }

  /**
   * path() applied to text node.
   */
  @org.junit.Test
  public void path008() {
    final XQuery query = new XQuery(
      "fn:path(//text()[.='2147483647'][1])",
      ctx);
    query.context(node(file("fn/path/pathdata.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "/Q{http://www.w3.org/2010/09/qt-fots-catalog}test-set[1]/Q{http://www.w3.org/2010/09/qt-fots-catalog}test-case[3]/Q{http://www.w3.org/2010/09/qt-fots-catalog}result[1]/Q{http://www.w3.org/2010/09/qt-fots-catalog}all-of[1]/Q{http://www.w3.org/2010/09/qt-fots-catalog}assert-eq[1]/text()[1]")
      &&
        assertType("xs:string")
      )
    );
  }

  /**
   * path() applied to PI.
   */
  @org.junit.Test
  public void path009() {
    final XQuery query = new XQuery(
      "fn:path(//processing-instruction()[1])",
      ctx);
    query.context(node(file("fn/path/pathdata.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "/processing-instruction(\"xml-stylesheet\")[1]")
      &&
        assertType("xs:string")
      )
    );
  }

  /**
   * path() applied to no-namespace element.
   */
  @org.junit.Test
  public void path010() {
    final XQuery query = new XQuery(
      "fn:path(//p)",
      ctx);
    query.context(node(file("fn/path/pathdata.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "/Q{http://www.w3.org/2010/09/qt-fots-catalog}test-set[1]/Q{}p[1]")
      &&
        assertType("xs:string")
      )
    );
  }

  /**
   * path() applied to a parentless element.
   */
  @org.junit.Test
  public void path014() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace copy=\"http://www.w3.org/QT3/copy\";\n" +
      "        fn:path(copy:copy((//employee)[1]))\n" +
      "      ",
      ctx);
    query.context(node(file("docs/works-mod.xml")));
    query.addModule("http://www.w3.org/QT3/copy", file("fn/id/copy.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      error("FODC0002")
    );
  }

  /**
   * path() applied to document node.
   */
  @org.junit.Test
  public void path015() {
    final XQuery query = new XQuery(
      "fn:path(/)",
      ctx);
    query.context(node(file("fn/path/pathdata.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "/")
      &&
        assertType("xs:string")
      )
    );
  }
}
