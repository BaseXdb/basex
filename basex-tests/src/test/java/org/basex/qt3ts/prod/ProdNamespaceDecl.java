package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the NamespaceDecl production.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdNamespaceDecl extends QT3TestSet {

  /**
   *  A 'declare namespace' expression containing many comments, using apostrophes for the URILiteral. .
   */
  @org.junit.Test
  public void kNamespaceProlog1() {
    final XQuery query = new XQuery(
      "(::)declare(::)namespace(::)ncname(::)=(::)'http://example.com/';(::)1(::)eq(::)1(::)",
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
   *  A 'declare namespace' expression containing many comments, using quotes for the URILiteral. .
   */
  @org.junit.Test
  public void kNamespaceProlog2() {
    final XQuery query = new XQuery(
      "(::)declare(::)namespace(::)ncname(::)=(::)\"http://example.com/\"(::);(::)1(::)eq(::)1(::)",
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
   *  ':=' can't be used in 'declare namespace, '=' must be used. .
   */
  @org.junit.Test
  public void kNamespaceProlog3() {
    final XQuery query = new XQuery(
      "declare namespace NCName := \"http://example.com/\";",
      ctx);
    try {
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
   *  A namespace declaration cannot occur twice for the same prefix, no matter what. .
   */
  @org.junit.Test
  public void k2NamespaceProlog1() {
    final XQuery query = new XQuery(
      "declare namespace myPrefix = \"http://example.com/\"; declare namespace myPrefix = \"\"; 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0033")
    );
  }

  /**
   *  Undeclare the 'local' prefix. .
   */
  @org.junit.Test
  public void k2NamespaceProlog10() {
    final XQuery query = new XQuery(
      "declare namespace local = \"\"; local:untypedAtomic(\"string\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0081")
    );
  }

  /**
   *  Undeclare the 'fn' prefix. .
   */
  @org.junit.Test
  public void k2NamespaceProlog11() {
    final XQuery query = new XQuery(
      "declare namespace fn = \"\"; fn:untypedAtomic(\"string\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0081")
    );
  }

  /**
   *  Undeclare the 'xsi' prefix. .
   */
  @org.junit.Test
  public void k2NamespaceProlog12() {
    final XQuery query = new XQuery(
      "declare namespace xsi = \"\"; xsi:untypedAtomic(\"string\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0081")
    );
  }

  /**
   *  Use the focus from within a attribute value template. .
   */
  @org.junit.Test
  public void k2NamespaceProlog13() {
    final XQuery query = new XQuery(
      "<e/>/<e a=\"{p:asd}\" xmlns:p=\"http://example.com/asd\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e xmlns:p=\"http://example.com/asd\" a=\"\"/>", false)
    );
  }

  /**
   *  Namespace declaration must appear before a variable declaration. .
   */
  @org.junit.Test
  public void k2NamespaceProlog14() {
    final XQuery query = new XQuery(
      "declare variable $inputDoc := 2; declare namespace x = \"http://example.com/\"; 1",
      ctx);
    try {
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
   *  Bind the prefix 'xml' to an invalid namespace. .
   */
  @org.junit.Test
  public void k2NamespaceProlog15() {
    final XQuery query = new XQuery(
      "declare namespace xml = \"http://example.com/\"; 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0070")
    );
  }

  /**
   *  XQuery has no such expression. .
   */
  @org.junit.Test
  public void k2NamespaceProlog16() {
    final XQuery query = new XQuery(
      "declare namespace prefix = \"http://example.com/\" { 1 }",
      ctx);
    try {
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
   *  A namespace declaration cannot occur twice for the same prefix, no matter what. .
   */
  @org.junit.Test
  public void k2NamespaceProlog2() {
    final XQuery query = new XQuery(
      "declare namespace myPrefix = \"\"; declare namespace myPrefix = \"http://example.com/\"; 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0033")
    );
  }

  /**
   *  A namespace declaration cannot occur twice for the same prefix, no matter what. .
   */
  @org.junit.Test
  public void k2NamespaceProlog3() {
    final XQuery query = new XQuery(
      "declare namespace myPrefix = \"http://example.com/\"; declare namespace myPrefix = \"http://example.com/TheSecondOne\"; declare namespace myPrefix = \"\"; 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0033")
    );
  }

  /**
   *  When a pre-declared namespace prefix has been undeclared, it is not available. .
   */
  @org.junit.Test
  public void k2NamespaceProlog4() {
    final XQuery query = new XQuery(
      "declare namespace xs = \"\"; xs:integer(1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0081")
    );
  }

  /**
   *  It is ok to undeclare a non-bound namespace. .
   */
  @org.junit.Test
  public void k2NamespaceProlog5() {
    final XQuery query = new XQuery(
      "declare namespace thisPrefixIsNotBoundExampleCom = \"\"; true()",
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
   *  It is not possible to undeclare the 'xml' prefix. .
   */
  @org.junit.Test
  public void k2NamespaceProlog6() {
    final XQuery query = new XQuery(
      "declare namespace xml = \"\"; 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0070")
    );
  }

  /**
   *  It is not possible to undeclare the 'xmlns' prefix. .
   */
  @org.junit.Test
  public void k2NamespaceProlog7() {
    final XQuery query = new XQuery(
      "declare namespace xmlns = \"\"; 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0070")
    );
  }

  /**
   *  Undeclare the 'xdt' prefix. .
   */
  @org.junit.Test
  public void k2NamespaceProlog8() {
    final XQuery query = new XQuery(
      "declare namespace xdt = \"\"; xdt:untypedAtomic(\"string\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0081")
    );
  }

  /**
   *  Undeclare the 'xs' prefix. .
   */
  @org.junit.Test
  public void k2NamespaceProlog9() {
    final XQuery query = new XQuery(
      "declare namespace xs = \"\"; xs:untypedAtomic(\"string\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0081")
    );
  }

  /**
   *  Test that the empty namespace can be bound to a prefix. .
   */
  @org.junit.Test
  public void cbclDeclareNamespace001() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare default element namespace \"http://www.example.org\"; \n" +
      "      \tdeclare namespace test=\"\"; \n" +
      "      \t<test:a />\n" +
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
      error("XPST0081")
    );
  }

  /**
   *  Evaluation of multiple namespace declarations with same prefix. Should raise static error. .
   */
  @org.junit.Test
  public void namespaceDecl1() {
    final XQuery query = new XQuery(
      "declare namespace foo = \"http://www.example.com/examples\"; declare namespace foo = \"http://www.example.com/examples\"; a",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0033")
    );
  }

  /**
   *  verify that the "xsi" prefix can be redefined and used. .
   */
  @org.junit.Test
  public void namespaceDecl10() {
    final XQuery query = new XQuery(
      "declare namespace xsi = \"http://www.example.com/examples\"; let $var := <xsi:someElement>some context</xsi:someElement> return $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<xsi:someElement xmlns:xsi=\"http://www.example.com/examples\">some context</xsi:someElement>", false)
    );
  }

  /**
   *  verify that the "fn" prefix can be redefined and used. .
   */
  @org.junit.Test
  public void namespaceDecl11() {
    final XQuery query = new XQuery(
      "declare namespace fn = \"http://www.example.com/examples\"; let $var := <fn:someElement>some context</fn:someElement> return $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<fn:someElement xmlns:fn=\"http://www.example.com/examples\">some context</fn:someElement>", false)
    );
  }

  /**
   *  verify that the "xdt" prefix can be redefined and used. .
   */
  @org.junit.Test
  public void namespaceDecl12() {
    final XQuery query = new XQuery(
      "declare namespace xdt = \"http://www.example.com/examples\"; let $var := <xdt:someElement>some context</xdt:someElement> return $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<xdt:someElement xmlns:xdt=\"http://www.example.com/examples\">some context</xdt:someElement>", false)
    );
  }

  /**
   *  verify that the "local" prefix can be redefined and used. .
   */
  @org.junit.Test
  public void namespaceDecl13() {
    final XQuery query = new XQuery(
      "declare namespace local = \"http://www.example.com/examples\"; let $var := <local:someElement>some context</local:someElement> return $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<local:someElement xmlns:local=\"http://www.example.com/examples\">some context</local:someElement>", false)
    );
  }

  /**
   *  verify that upper case "XML" is different from lower case "xml". .
   */
  @org.junit.Test
  public void namespaceDecl14() {
    final XQuery query = new XQuery(
      "declare namespace XML = \"http://www.example.com/examples\"; let $var := <XML:someElement>some context</XML:someElement> return $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<XML:someElement xmlns:XML=\"http://www.example.com/examples\">some context</XML:someElement>", false)
    );
  }

  /**
   *  verify that upper case "XMLNS" is different from lower case "xmlns". .
   */
  @org.junit.Test
  public void namespaceDecl15() {
    final XQuery query = new XQuery(
      "declare namespace XMLNS = \"http://www.example.com/examples\"; let $var := <XMLNS:someElement>some context</XMLNS:someElement> return $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<XMLNS:someElement xmlns:XMLNS=\"http://www.example.com/examples\">some context</XMLNS:someElement>", false)
    );
  }

  /**
   *  verify that a local namespace declaration overrides a querywide declaration. .
   */
  @org.junit.Test
  public void namespaceDecl16() {
    final XQuery query = new XQuery(
      "declare namespace px = \"http://www.example.com/examples\"; let $var := <px:someElement xmlns:px = \"http://www.examples.com/localexamples\">some context</px:someElement> return $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<px:someElement xmlns:px=\"http://www.examples.com/localexamples\">some context</px:someElement>", false)
    );
  }

  /**
   *  Verify that "abc" is a valid namespace declaration Test was modified on 07/06/06 in order to avoid serialization of relative URI's as part of the result. .
   */
  @org.junit.Test
  public void namespaceDecl17() {
    final XQuery query = new XQuery(
      "declare namespace px = \"http://www.example.com/abc\"; let $var := <px:someElement>some context</px:someElement> return namespace-uri-from-QName(node-name($var)) eq xs:anyURI(\"http://www.example.com/abc\")",
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
   *  Verify that "ftp://ftp.is.co.za/rfc/somefile.txt" is a vaild namespace declaration .
   */
  @org.junit.Test
  public void namespaceDecl18() {
    final XQuery query = new XQuery(
      "declare namespace px = \"ftp://ftp.is.co.za/rfc/somefile.txt\"; let $var := <px:someElement>some context</px:someElement> return $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<px:someElement xmlns:px=\"ftp://ftp.is.co.za/rfc/somefile.txt\">some context</px:someElement>", false)
    );
  }

  /**
   *  Verify that the same namespace URI can be bound to different prefixes. .
   */
  @org.junit.Test
  public void namespaceDecl19() {
    final XQuery query = new XQuery(
      "declare namespace px1 = \"http://www.example.com/examples\"; declare namespace px2 = \"http://www.example.com/examples\"; let $var := <px1:someElement>some context</px1:someElement> return $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<px1:someElement xmlns:px1=\"http://www.example.com/examples\">some context</px1:someElement>", false)
    );
  }

  /**
   *  Evaluation of usage of prefix with no declaration. Should raise static error. .
   */
  @org.junit.Test
  public void namespaceDecl2() {
    final XQuery query = new XQuery(
      "element foo:anElement {\"Element content\"}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0081")
    );
  }

  /**
   *  Verify that namespace URI may contain a number. Test was modified on 07/06/06 in order to avoid serialization of relative URI's as part of the result. .
   */
  @org.junit.Test
  public void namespaceDecl20() {
    final XQuery query = new XQuery(
      "declare namespace xx = \"http://www.example.com/abc123\"; let $var := <xx:someElement>some content</xx:someElement> return namespace-uri-from-QName(node-name($var)) eq xs:anyURI(\"http://www.example.com/abc123\")",
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
   *  Verify that namespace URI and prefix can contain the same value. Test was modified on 07/06/06 in order to avoid serialization of relative URI's as part of the result. .
   */
  @org.junit.Test
  public void namespaceDecl21() {
    final XQuery query = new XQuery(
      "declare namespace abc = \"http://www.example.com/abc\"; let $var := <abc:someElement>some content</abc:someElement> return namespace-uri-from-QName(node-name($var)) eq xs:anyURI(\"http://www.example.com/abc\")",
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
   *  Verify that "gopher://spinaltap.micro.umn.edu/00/Weather/California/somefile" is a valid namespace URI during namespace declaration. .
   */
  @org.junit.Test
  public void namespaceDecl22() {
    final XQuery query = new XQuery(
      "declare namespace abc = \"gopher://spinaltap.micro.umn.edu/00/Weather/California/somefile\"; <abc:someElement>some content</abc:someElement>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<abc:someElement xmlns:abc=\"gopher://spinaltap.micro.umn.edu/00/Weather/California/somefile\">some content</abc:someElement>", false)
    );
  }

  /**
   *  Verify that a namespace URI conatining "abc&amp;" is a valid namespace URI during namespace declaration. Test was modified on 07/06/06 in order to avoid serialization of relative URI's as part of the result. .
   */
  @org.junit.Test
  public void namespaceDecl23() {
    final XQuery query = new XQuery(
      "declare namespace abc = \"http://www.example.com/abc&amp;\"; let $var := <abc:someElement>some content</abc:someElement> return namespace-uri-from-QName(node-name($var)) eq xs:anyURI(\"http://www.example.com/abc&amp;\")",
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
   *  Verify that "mailto:someuser@someserver.com" is a valid namespace URI during namespace declaration. .
   */
  @org.junit.Test
  public void namespaceDecl24() {
    final XQuery query = new XQuery(
      "declare namespace abc = \"mailto:someuser@someserver.com\"; <abc:someElement>some content</abc:someElement>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<abc:someElement xmlns:abc=\"mailto:someuser@someserver.com\">some content</abc:someElement>", false)
    );
  }

  /**
   *  Evaluation of usage of predefined namespace xml = "http://www.w3.org/XML/1998/namespace" .
   */
  @org.junit.Test
  public void namespaceDecl3() {
    final XQuery query = new XQuery(
      "declare namespace xml = \"http://www.w3.org/XML/1998/namespace\"; \"a\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0070")
    );
  }

  /**
   *  Evaluation of redefinition of namespace associated with xml. .
   */
  @org.junit.Test
  public void namespaceDecl4() {
    final XQuery query = new XQuery(
      "declare namespace foo = \"http://www.w3.org/XML/1998/namespace\"; \"a\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0070")
    );
  }

  /**
   *  Evaluation of redefinition xmls prefix. .
   */
  @org.junit.Test
  public void namespaceDecl5() {
    final XQuery query = new XQuery(
      "declare namespace xmlns = \"http://example.com/examples\"; \"a\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0070")
    );
  }

  /**
   *  Evaluation of typical usage of namespace declaration as per example 1 in section 4.7 of the query specs. .
   */
  @org.junit.Test
  public void namespaceDecl6() {
    final XQuery query = new XQuery(
      "declare namespace foo = \"http://example.org\"; <foo:bar> Lentils </foo:bar>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<foo:bar xmlns:foo=\"http://example.org\"> Lentils </foo:bar>", false)
    );
  }

  /**
   *  Evaluation usage of namespace declaration, different prefix bounded to same namespace uri and use of same local name (example 2 of this section from the query specs. .
   */
  @org.junit.Test
  public void namespaceDecl7() {
    final XQuery query = new XQuery(
      "declare namespace xx = \"http://example.org\"; let $i := <foo:bar xmlns:foo = \"http://example.org\"> <foo:bing> Lentils </foo:bing> </foo:bar> return $i/xx:bing",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<foo:bing xmlns:foo=\"http://example.org\"> Lentils </foo:bing>", false)
    );
  }

  /**
   *  Evaluates that at a namespace delcaration the prefix name is an NCName. from the query specs. .
   */
  @org.junit.Test
  public void namespaceDecl8() {
    final XQuery query = new XQuery(
      "declare namespace foo:bar = \"http://www.example.com/examples\"; \"aa\"",
      ctx);
    try {
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
   *  verify that the "xs" prefix can be redefined and used. .
   */
  @org.junit.Test
  public void namespaceDecl9() {
    final XQuery query = new XQuery(
      "declare namespace xs = \"http://www.example.com/examples\"; let $var := <xs:someElement>some context</xs:someElement> return $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<xs:someElement xmlns:xs=\"http://www.example.com/examples\">some context</xs:someElement>", false)
    );
  }
}
