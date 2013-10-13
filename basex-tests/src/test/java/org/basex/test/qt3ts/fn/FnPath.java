package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * tests for the fn:path() function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-13, BSD License
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
    try {
      query.context(node(file("fn/path/pathdata.xml")));
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
   * path() applied to top-level element.
   */
  @org.junit.Test
  public void path002() {
    final XQuery query = new XQuery(
      "fn:path(/*)",
      ctx);
    try {
      query.context(node(file("fn/path/pathdata.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.context(node(file("fn/path/pathdata.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.context(node(file("fn/path/pathdata.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.context(node(file("fn/path/pathdata.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.context(node(file("fn/path/pathdata.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.context(node(file("fn/path/pathdata.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.context(node(file("fn/path/pathdata.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.context(node(file("fn/path/pathdata.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "/processing-instruction(xml-stylesheet)[1]")
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
    try {
      query.context(node(file("fn/path/pathdata.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
      "        fn:path(copy:copy((//employee)[1])/pnum)\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      query.addModule("http://www.w3.org/QT3/copy", file("fn/id/copy.xq"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Q{http://www.w3.org/2005/xpath-functions}root()/Q{}pnum[1]")
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
    try {
      query.context(node(file("fn/path/pathdata.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "/")
      &&
        assertType("xs:string")
      )
    );
  }

  /**
   * path() applied to a parentless attribute node.
   */
  @org.junit.Test
  public void path016() {
    final XQuery query = new XQuery(
      "fn:path(attribute name {\"fred\"})",
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
        assertStringValue(false, "Q{http://www.w3.org/2005/xpath-functions}root()")
      &&
        assertType("xs:string")
      )
    );
  }

  /**
   * path() applied to a parentless text node.
   */
  @org.junit.Test
  public void path017() {
    final XQuery query = new XQuery(
      "fn:path(text{\"fred\"})",
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
        assertStringValue(false, "Q{http://www.w3.org/2005/xpath-functions}root()")
      &&
        assertType("xs:string")
      )
    );
  }

  /**
   * path() applied to an attribute of an element root.
   */
  @org.junit.Test
  public void path018() {
    final XQuery query = new XQuery(
      "fn:path(<a b=\"c\"/>/@b)",
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
        assertStringValue(false, "Q{http://www.w3.org/2005/xpath-functions}root()/@b")
      &&
        assertType("xs:string")
      )
    );
  }

  /**
   * path() applied to an element child of an element root.
   */
  @org.junit.Test
  public void path019() {
    final XQuery query = new XQuery(
      "fn:path(<a><b/><b/></a>/(b[2]))",
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
        assertStringValue(false, "Q{http://www.w3.org/2005/xpath-functions}root()/Q{}b[2]")
      &&
        assertType("xs:string")
      )
    );
  }

  /**
   * path() with no arguments.
   */
  @org.junit.Test
  public void path020() {
    final XQuery query = new XQuery(
      "(//*:all-of)[1] ! fn:path()",
      ctx);
    try {
      query.context(node(file("fn/path/pathdata.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "/Q{http://www.w3.org/2010/09/qt-fots-catalog}test-set[1]/Q{http://www.w3.org/2010/09/qt-fots-catalog}test-case[1]/Q{http://www.w3.org/2010/09/qt-fots-catalog}result[1]/Q{http://www.w3.org/2010/09/qt-fots-catalog}all-of[1]")
      &&
        assertType("xs:string")
      )
    );
  }
}
