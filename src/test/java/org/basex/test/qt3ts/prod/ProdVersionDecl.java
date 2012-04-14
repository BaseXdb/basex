package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the VersionDecl production.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdVersionDecl extends QT3TestSet {

  /**
   *  A simple version declaration including encoding. Since it's implementation defined how comments 
   *         before the version declaration is handled, any error is allowed. .
   */
  @org.junit.Test
  public void kVersionProlog1() {
    final XQuery query = new XQuery(
      "xquery version \"1.0\" encoding \"UTF-8\"; 1 eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertBoolean(true)
      ||
        error("*")
      )
    );
  }

  /**
   *  A simple version declaration excluding encoding. .
   */
  @org.junit.Test
  public void kVersionProlog2() {
    final XQuery query = new XQuery(
      "xquery version \"1.0\" ; 1 eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A version declaration with an encoding containing whitespace. .
   */
  @org.junit.Test
  public void kVersionProlog3() {
    final XQuery query = new XQuery(
      "xquery version \"1.0\" encoding \"UTF-8 \"; 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0087")
    );
  }

  /**
   *  A version declaration with an encoding containing an disallowed character. .
   */
  @org.junit.Test
  public void kVersionProlog4() {
    final XQuery query = new XQuery(
      "xquery version \"1.0\" encoding \"ISO-8859-1|\"; 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0087")
    );
  }

  /**
   *  A prolog containing many different declarations. TODO function declarations missing TODO variable declarations missing .
   */
  @org.junit.Test
  public void kVersionProlog5() {
    final XQuery query = new XQuery(
      "\n" +
      "        xquery version \"1.0\" encoding \"ISO-8859-1\"; \n" +
      "        declare boundary-space preserve; \n" +
      "        declare default collation \"http://www.w3.org/2005/xpath-functions/collation/codepoint\"; \n" +
      "        declare base-uri \"http://example.com/\"; \n" +
      "        declare construction strip; \n" +
      "        declare ordering ordered; \n" +
      "        declare default order empty greatest; \n" +
      "        declare copy-namespaces no-preserve, no-inherit; \n" +
      "        declare namespace ex = \"http://example.com/a/Namespace\"; \n" +
      "        declare default element namespace \"http://example.com/\"; \n" +
      "        declare default function namespace \"http://example.com/\"; \n" +
      "        declare option fn:x-notRecognized \"option content\"; \n" +
      "        1 eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  a string literal reminding of an xquery version declaration. .
   */
  @org.junit.Test
  public void k2VersionProlog1() {
    final XQuery query = new XQuery(
      "'xquery version \"1.0\" encoding \"UTF-8|#%\";' eq 'xquery version \"1.0\" encoding \"UTF-8|#%\";'",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  a string literal reminding of an xquery version declaration. .
   */
  @org.junit.Test
  public void k2VersionProlog2() {
    final XQuery query = new XQuery(
      "\"xquery version '1.0' encoding 'UTF-8|#%';\" eq \"xquery version '1.0' encoding 'UTF-8|#%';\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Use an invalid keyword as encoding. .
   */
  @org.junit.Test
  public void k2VersionProlog3() {
    final XQuery query = new XQuery(
      "xquery version \"1.0\" default; 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Whitespace isn't allowed after a prefix in a QName. .
   */
  @org.junit.Test
  public void k2VersionProlog4() {
    final XQuery query = new XQuery(
      "encoding :localName",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Check that the 'xquery' name test is parsed correctly. .
   */
  @org.junit.Test
  public void k2VersionProlog5() {
    final XQuery query = new XQuery(
      "xquery gt xquery",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPDY0002")
    );
  }

  /**
   *  Prolog version declaration with both version and encoding information (set to 1.0 and "utf-8" respectively. .
   */
  @org.junit.Test
  public void prologVersion1() {
    final XQuery query = new XQuery(
      "xquery version \"1.0\" encoding \"utf-8\"; 1,2",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1 2")
    );
  }

  /**
   * Written By: Carmelo Montanez  Demonstrates version declaration,the version declaration occurs at 
   *         the beginning of the module and identifies the applicable XQuery syntax and semantics for the module. .
   */
  @org.junit.Test
  public void prologVersion10() {
    final XQuery query = new XQuery(
      "xquery version '-1.0'; 2 + 2",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0031")
    );
  }

  /**
   * Written By: Carmelo Montanez  Demonstrates version declaration,the version declaration occurs at 
   *         the beginning of the module and identifies the applicable XQuery syntax and semantics for the module. .
   */
  @org.junit.Test
  public void prologVersion11() {
    final XQuery query = new XQuery(
      "xquery version \"abc\"; 1,2",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0031")
    );
  }

  /**
   * Written By: Carmelo Montanez  Demonstrates version declaration,the version declaration occurs at 
   *         the beginning of the module and identifies the applicable XQuery syntax and semantics for the module. .
   */
  @org.junit.Test
  public void prologVersion12() {
    final XQuery query = new XQuery(
      "declare boundary-space preserve; xquery version \"1.0\"; 1,2",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Prolog version declaration with both version and encoding information (set to 1.0 and "US-ASCII" respectively. .
   */
  @org.junit.Test
  public void prologVersion3() {
    final XQuery query = new XQuery(
      "xquery version \"1.0\" encoding \"US-ASCII\"; 1,2",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1 2")
    );
  }

  /**
   *  Demonstrates version declaration,the version declaration occurs at the beginning of the module 
   *         and identifies the applicable XQuery syntax and semantics for the module. .
   */
  @org.junit.Test
  public void prologVersion4() {
    final XQuery query = new XQuery(
      "xquery version \"1.0\"; 1,2",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1 2")
    );
  }

  /**
   *  Demonstrates version declaration,the version declaration occurs at the beginning of the module 
   *         and identifies the applicable XQuery syntax and semantics for the module. .
   */
  @org.junit.Test
  public void prologVersion5() {
    final XQuery query = new XQuery(
      "xquery version \"1.0\"; <bib/>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<bib/>", false)
    );
  }

  /**
   *  Demonstrates version declaration,the version declaration occurs at the beginning of the module 
   *         and identifies the applicable XQuery syntax and semantics for the module. .
   */
  @org.junit.Test
  public void prologVersion6() {
    final XQuery query = new XQuery(
      "\n" +
      "        xquery version '1.0'; \n" +
      "        declare boundary-space preserve; \n" +
      "        declare default order empty greatest; \n" +
      "        declare namespace ns = \"http://www.example.org/\"; \n" +
      "        for $b in//book stable order by xs:decimal($b/price[1]) empty greatest \n" +
      "        return $b/title",
      ctx);
    query.context(node(file("op/union/bib2.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<title>Data on the Web</title><title>TCP/IP Illustrated</title><title>Advanced Programming in the Unix environment</title><title>The Economics of Technology and Content for Digital TV</title>", false)
    );
  }

  /**
   *  Demonstrates version declaration,the version declaration occurs at the beginning of the module 
   *         and identifies the applicable XQuery syntax and semantics for the module. .
   */
  @org.junit.Test
  public void prologVersion7() {
    final XQuery query = new XQuery(
      "\n" +
      "        xquery version \"1.0\"; \n" +
      "        declare boundary-space preserve; \n" +
      "        declare default order empty greatest; \n" +
      "        declare namespace ns = \"http://www.example.org/\"; \n" +
      "        for $b in //book stable order by xs:decimal($b/price[1]) empty greatest \n" +
      "        return $b/title",
      ctx);
    query.context(node(file("op/union/bib2.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<title>Data on the Web</title><title>TCP/IP Illustrated</title><title>Advanced Programming in the Unix environment</title><title>The Economics of Technology and Content for Digital TV</title>", false)
    );
  }

  /**
   * Written By: Carmelo Montanez  
   *         Demonstrates version declaration,the version declaration occurs at the beginning of the module 
   *         and identifies the applicable XQuery syntax and semantics for the module. .
   */
  @org.junit.Test
  public void prologVersion9() {
    final XQuery query = new XQuery(
      "xquery version \"3.0\"; <bib/>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertSerialization("<bib/>", false)
      ||
        error("XQST0031")
      )
    );
  }

  /**
   *  Demonstrates version declaration,the version declaration occurs at the beginning of the module 
   *         and identifies the applicable XQuery syntax and semantics for the module .
   */
  @org.junit.Test
  public void versionDeclaration001() {
    final XQuery query = new XQuery(
      "xquery version \"1.0\"; 1,2",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "1 2")
      ||
        error("*")
      )
    );
  }

  /**
   *  Demonstrates version declaration,the version declaration occurs at the beginning of the module 
   *         and identifies the applicable XQuery syntax and semantics for the module .
   */
  @org.junit.Test
  public void versionDeclaration002() {
    final XQuery query = new XQuery(
      "xquery version \"1.0\"; <bib/>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertSerialization("<bib/>", false)
      ||
        error("*")
      )
    );
  }

  /**
   *  Demonstrates version declaration,the version declaration occurs at the beginning of the module 
   *         and identifies the applicable XQuery syntax and semantics for the module .
   */
  @org.junit.Test
  public void versionDeclaration003() {
    final XQuery query = new XQuery(
      "xquery version '1.0'; declare boundary-space preserve; declare default order empty greatest; declare namespace ns = \"http://www.example.org/\"; declare variable $input-context external; for $b in $input-context//book stable order by xs:decimal($b/price) empty greatest return $b/title",
      ctx);
    query.context(node(file("op/union/bib2.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertSerialization("<title>Data on the Web</title><title>TCP/IP Illustrated</title><title>Advanced Programming in the Unix environment</title><title>The Economics of Technology and Content for Digital TV</title>", false)
      ||
        error("*")
      )
    );
  }

  /**
   *  Demonstrates version declaration,the version declaration occurs at the beginning of the module 
   *         and identifies the applicable XQuery syntax and semantics for the module .
   */
  @org.junit.Test
  public void versionDeclaration004() {
    final XQuery query = new XQuery(
      "xquery version \"1.0\"; declare boundary-space preserve; declare default order empty greatest; declare namespace ns = \"http://www.example.org/\"; declare variable $input-context external; for $b in $input-context//book stable order by xs:decimal($b/price) empty greatest return $b/title",
      ctx);
    query.context(node(file("op/union/bib2.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertSerialization("<title>Data on the Web</title><title>TCP/IP Illustrated</title><title>Advanced Programming in the Unix environment</title><title>The Economics of Technology and Content for Digital TV</title>", false)
      ||
        error("*")
      )
    );
  }

  /**
   *  Demonstrates version declaration,the version declaration occurs at the beginning of the module 
   *         and identifies the applicable XQuery syntax and semantics for the module .
   */
  @org.junit.Test
  public void versionDeclaration006() {
    final XQuery query = new XQuery(
      "xquery version \"3.0\"; <bib/>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertSerialization("<bib/>", false)
      ||
        error("XQST0031")
      )
    );
  }

  /**
   *  Demonstrates version declaration,the version declaration occurs at the beginning of the module 
   *         and identifies the applicable XQuery syntax and semantics for the module .
   */
  @org.junit.Test
  public void versionDeclaration007() {
    final XQuery query = new XQuery(
      "xquery version '-1.0'; 2 + 2",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0031")
    );
  }

  /**
   *  Demonstrates version declaration,the version declaration occurs at the beginning of the module 
   *         and identifies the applicable XQuery syntax and semantics for the module .
   */
  @org.junit.Test
  public void versionDeclaration008() {
    final XQuery query = new XQuery(
      "xquery version \"abc\"; 1,2",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0031")
    );
  }

  /**
   *  Demonstrates version declaration,the version declaration occurs at the beginning of the module 
   *         and identifies the applicable XQuery syntax and semantics for the module .
   */
  @org.junit.Test
  public void versionDeclaration009() {
    final XQuery query = new XQuery(
      "declare boundary-space preserve; xquery version \"1.0\"; 1,2",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   * Evaluation of version declaration, for which the encoding does not conforms to "encName" from XML 1.0. .
   */
  @org.junit.Test
  public void versionDeclaration010() {
    final XQuery query = new XQuery(
      "xquery version \"1.0\" encoding \"999-UTF-8-*\"; \"ABC\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0087")
    );
  }
}
