package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the namespace-uri-from-qname() function.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnNamespaceUriFromQName extends QT3TestSet {

  /**
   *  A test whose essence is: `namespace-uri-from-QName()`. .
   */
  @org.junit.Test
  public void kNamespaceURIFromQNameFunc1() {
    final XQuery query = new XQuery(
      "namespace-uri-from-QName()",
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
   *  A test whose essence is: `namespace-uri-from-QName(1, 2)`. .
   */
  @org.junit.Test
  public void kNamespaceURIFromQNameFunc2() {
    final XQuery query = new XQuery(
      "namespace-uri-from-QName(1, 2)",
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
   *  A test whose essence is: `empty(namespace-uri-from-QName( () ))`. .
   */
  @org.junit.Test
  public void kNamespaceURIFromQNameFunc3() {
    final XQuery query = new XQuery(
      "empty(namespace-uri-from-QName( () ))",
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
   *  A test whose essence is: `namespace-uri-from-QName( QName("example.com/", "pre:lname")) eq xs:anyURI("example.com/")`. .
   */
  @org.junit.Test
  public void kNamespaceURIFromQNameFunc4() {
    final XQuery query = new XQuery(
      "namespace-uri-from-QName( QName(\"example.com/\", \"pre:lname\")) eq xs:anyURI(\"example.com/\")",
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
   *  A test whose essence is: `namespace-uri-from-QName( QName("example.com/", "pre:lname")) instance of xs:anyURI`. .
   */
  @org.junit.Test
  public void kNamespaceURIFromQNameFunc5() {
    final XQuery query = new XQuery(
      "namespace-uri-from-QName( QName(\"example.com/\", \"pre:lname\")) instance of xs:anyURI",
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
   *  Test that xmlns declarations in direct element constructors are honored in embedded expressions. .
   */
  @org.junit.Test
  public void k2NamespaceURIFromQNameFunc1() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace ns = \"http://example.com/ANamespace\"; \n" +
      "        string(<name xmlns:ns=\"http://example.com/BNamespace\">{namespace-uri-from-QName(\"ns:foo\" cast as xs:QName)}</name>)\n" +
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
      assertStringValue(false, "http://example.com/BNamespace")
    );
  }

  /**
   *  Check that the default element namespace is picked up with computed element constructors. .
   */
  @org.junit.Test
  public void k2NamespaceURIFromQNameFunc2() {
    final XQuery query = new XQuery(
      "\n" +
      "        <e xmlns=\"http://example.com/\"> {namespace-uri-from-QName(node-name(element anElement{\"text\"}))} </e>/string()\n" +
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
      assertStringValue(false, "http://example.com/")
    );
  }

  /**
   *  Check that xmlns declarations overrides. .
   */
  @org.junit.Test
  public void k2NamespaceURIFromQNameFunc3() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default element namespace \"http://example.com/\"; \n" +
      "        <e xmlns=\"\">{namespace-uri-from-QName(xs:QName(\"l\"))}</e>/string()\n" +
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
      assertEq("\"\"")
    );
  }

  /**
   *  Check that xmlns declarations overrides(#2). .
   */
  @org.junit.Test
  public void k2NamespaceURIFromQNameFunc4() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default element namespace \"http://example.com/\"; \n" +
      "        declare namespace p = \"http://example.com/2\"; \n" +
      "        <e xmlns=\"\" xmlns:p=\"http://example.com/3\">[{namespace-uri-from-QName(xs:QName(\"n1\"))}|{namespace-uri-from-QName(xs:QName(\"p:n2\"))}]</e>/text()\n" +
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
      assertStringValue(false, "[|http://example.com/3]")
    );
  }

  /**
   *  Test function fn:namespace-uri-from-QName. Empty sequence literal as input .
   */
  @org.junit.Test
  public void namespaceURIFromQNameFunc006() {
    final XQuery query = new XQuery(
      "fn:namespace-uri-from-QName(())",
      ctx);
    try {
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
   *  Test function fn:namespace-uri-from-QName. Empty sequence literal as input .
   */
  @org.junit.Test
  public void namespaceURIFromQNameFunc007() {
    final XQuery query = new XQuery(
      "fn:namespace-uri-from-QName(((),()))",
      ctx);
    try {
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
   *  Test function fn:namespace-uri-from-QName. Error case - invalid parameter type (string) .
   */
  @org.junit.Test
  public void namespaceURIFromQNameFunc009() {
    final XQuery query = new XQuery(
      "fn:namespace-uri-from-QName(\"\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  Test function fn:namespace-uri-from-QName. Error case - no input parameter .
   */
  @org.junit.Test
  public void namespaceURIFromQNameFunc011() {
    final XQuery query = new XQuery(
      "fn:namespace-uri-from-QName()",
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
   *  Test function fn:namespace-uri-from-QName.
   *       				Error case - invalid parameter type (simple type) .
   */
  @org.junit.Test
  public void namespaceURIFromQNameFunc015() {
    final XQuery query = new XQuery(
      "fn:namespace-uri-from-QName((//Folder)[1])",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  Test function fn:namespace-uri-from-QName.
   *       				Error case - invalid parameter type (simple type) .
   */
  @org.junit.Test
  public void namespaceURIFromQNameFunc015a() {
    final XQuery query = new XQuery(
      "fn:namespace-uri-from-QName((//Folder)[1])",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0117")
    );
  }

  /**
   *  Test function fn:namespace-uri-from-QName. Error case - invalid parameter type (integer) .
   */
  @org.junit.Test
  public void namespaceURIFromQNameFunc016() {
    final XQuery query = new XQuery(
      "fn:namespace-uri-from-QName(xs:integer(\"100\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  Test function fn:namespace-uri-from-QName. Error case - invalid parameter type (time) .
   */
  @org.junit.Test
  public void namespaceURIFromQNameFunc017() {
    final XQuery query = new XQuery(
      "fn:namespace-uri-from-QName(xs:time(\"12:00:00Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }
}
