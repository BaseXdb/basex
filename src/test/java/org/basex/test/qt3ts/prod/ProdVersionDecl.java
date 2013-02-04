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
      "(:encoding \"UTF-8XX\":)xquery version \"1.0\" encoding \"UTF-8\"; 1 eq 1",
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
  public void kVersionProlog2V3() {
    final XQuery query = new XQuery(
      "xquery version \"3.0\" ; 1 eq 1",
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
   *  Whitespace is not allowed in EncName. .
   */
  @org.junit.Test
  public void kVersionProlog3V3() {
    final XQuery query = new XQuery(
      "xquery version \"3.0\" encoding \"UTF-8 \"; 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0087")
    );
  }

  /**
   *  Vertical bar is not allowed in EncName. .
   */
  @org.junit.Test
  public void kVersionProlog4V3() {
    final XQuery query = new XQuery(
      "xquery version \"3.0\" encoding \"ISO-8859-1|\"; 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0087")
    );
  }

  /**
   *  A prolog containing many different declarations. TODO function declarations missing TODO variable declarations missing .
   */
  @org.junit.Test
  public void kVersionProlog5V3() {
    final XQuery query = new XQuery(
      "\n" +
      "        xquery version \"3.0\" encoding \"ISO-8859-1\"; \n" +
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
   *  a string literal reminding of an xquery version declaration. .
   */
  @org.junit.Test
  public void k2VersionProlog1() {
    final XQuery query = new XQuery(
      "'xquery version \"1.0\" encoding \"UTF-8|#%\";' eq 'xquery version \"1.0\" encoding \"UTF-8|#%\";'",
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
   *  a string literal reminding of an xquery version declaration. .
   */
  @org.junit.Test
  public void k2VersionProlog2() {
    final XQuery query = new XQuery(
      "\"xquery version '1.0' encoding 'UTF-8|#%';\" eq \"xquery version '1.0' encoding 'UTF-8|#%';\"",
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
   *  Use an invalid keyword as encoding. .
   */
  @org.junit.Test
  public void k2VersionProlog3V3() {
    final XQuery query = new XQuery(
      "xquery version \"3.0\" default; 1",
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
   *  Whitespace isn't allowed after a prefix in a QName. .
   */
  @org.junit.Test
  public void k2VersionProlog4() {
    final XQuery query = new XQuery(
      "encoding :localName",
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
   *  Check that the 'xquery' name test is parsed correctly. .
   */
  @org.junit.Test
  public void k2VersionProlog5() {
    final XQuery query = new XQuery(
      "xquery gt xquery",
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
   *  An XQuery 3.0 processor might or might not accept version "1.0". .
   */
  @org.junit.Test
  public void versionDeclV3ProcessorAndV1Query() {
    final XQuery query = new XQuery(
      "xquery version \"1.0\"; <bib/>",
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
        error("XQST0031")
      ||
        assertSerialization("<bib/>", false)
      )
    );
  }

  /**
   *  Prolog version declaration with both version and encoding information (set to 3.0 and "utf-8" respectively. .
   */
  @org.junit.Test
  public void prologVersion1V3() {
    final XQuery query = new XQuery(
      "xquery version \"3.0\" encoding \"utf-8\"; 1,2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2")
    );
  }

  /**
   *  Prolog version declaration with both version and encoding information (set to 3.0 and "US-ASCII" respectively. .
   */
  @org.junit.Test
  public void prologVersion3V3() {
    final XQuery query = new XQuery(
      "xquery version \"3.0\" encoding \"US-ASCII\"; 1,2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2")
    );
  }

  /**
   *  Simple version decl. .
   */
  @org.junit.Test
  public void prologVersion4V3() {
    final XQuery query = new XQuery(
      "xquery version \"3.0\"; 1,2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2")
    );
  }

  /**
   *  An XQuery 3.0 processor must accept version "3.0". .
   */
  @org.junit.Test
  public void prologVersion5V3() {
    final XQuery query = new XQuery(
      "xquery version \"3.0\"; <bib/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<bib/>", false)
    );
  }

  /**
   *  A more realistic query with a version decl, no encoding. .
   */
  @org.junit.Test
  public void prologVersion6V3() {
    final XQuery query = new XQuery(
      "\n" +
      "        xquery version '3.0'; \n" +
      "        declare boundary-space preserve; \n" +
      "        declare default order empty greatest; \n" +
      "        declare namespace ns = \"http://www.example.org/\"; \n" +
      "        for $b in//book stable order by xs:decimal($b/price[1]) empty greatest \n" +
      "        return $b/title",
      ctx);
    try {
      query.context(node(file("op/union/bib2.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<title>Data on the Web</title><title>TCP/IP Illustrated</title><title>Advanced Programming in the Unix environment</title><title>The Economics of Technology and Content for Digital TV</title>", false)
    );
  }

  /**
   *  A more realitic query with a version decl, no encoding. .
   */
  @org.junit.Test
  public void prologVersion7V3() {
    final XQuery query = new XQuery(
      "\n" +
      "        xquery version \"3.0\"; \n" +
      "        declare boundary-space preserve; \n" +
      "        declare default order empty greatest; \n" +
      "        declare namespace ns = \"http://www.example.org/\"; \n" +
      "        for $b in //book stable order by xs:decimal($b/price[1]) empty greatest \n" +
      "        return $b/title",
      ctx);
    try {
      query.context(node(file("op/union/bib2.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<title>Data on the Web</title><title>TCP/IP Illustrated</title><title>Advanced Programming in the Unix environment</title><title>The Economics of Technology and Content for Digital TV</title>", false)
    );
  }

  /**
   * 
   *          The production for VersionDecl includes the final Separator.
   *          Thus, a Comment occurring after the bulk of a VersionDecl but before the semicolon,
   *          still "occurs before the end of the version declaration".
   *       .
   */
  @org.junit.Test
  public void versionDeclaration001() {
    final XQuery query = new XQuery(
      "xquery version \"1.0\" (:encoding \"utf-8xx\":); 1,2",
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
        assertStringValue(false, "1 2")
      ||
        error("*")
      )
    );
  }

  /**
   *  A case of Comment-before-VersionDecl, this one without an encoding decl. .
   */
  @org.junit.Test
  public void versionDeclaration002() {
    final XQuery query = new XQuery(
      "(:encoding \"utf-8xx\":)xquery version \"1.0\"; <bib/>",
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
        assertSerialization("<bib/>", false)
      ||
        error("*")
      )
    );
  }

  /**
   *  A negative version number. .
   */
  @org.junit.Test
  public void versionDeclaration007() {
    final XQuery query = new XQuery(
      "xquery version '-1.0'; 2 + 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0031")
    );
  }

  /**
   *  A non-numeric version string. .
   */
  @org.junit.Test
  public void versionDeclaration008() {
    final XQuery query = new XQuery(
      "xquery version \"abc\"; 1,2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0031")
    );
  }

  /**
   *  A VersionDecl cannot appear after a prolog decl. .
   */
  @org.junit.Test
  public void versionDeclaration009() {
    final XQuery query = new XQuery(
      "declare boundary-space preserve; xquery version \"1.0\"; 1,2",
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
   *  Asterisk is not allowed in EncName. .
   */
  @org.junit.Test
  public void versionDeclaration010V3() {
    final XQuery query = new XQuery(
      "xquery version \"3.0\" encoding \"999-UTF-8-*\"; \"ABC\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0087")
    );
  }

  /**
   *  You can't leave out the version string. .
   */
  @org.junit.Test
  public void versionDeclaration020() {
    final XQuery query = new XQuery(
      "xquery version; 1",
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
   *  "2.0" is a plausible version string, but not supported. .
   */
  @org.junit.Test
  public void versionDeclaration021() {
    final XQuery query = new XQuery(
      "xquery version \"2.0\"; 1,2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0031")
    );
  }

  /**
   *  You can't swap the order of the 'version' and 'encoding' clauses. .
   */
  @org.junit.Test
  public void versionDeclaration022V3() {
    final XQuery query = new XQuery(
      "xquery encoding \"utf-8\" version \"3.0\"; 1",
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
   *  In XQuery 3.0, you *can* omit the 'version' clause. .
   */
  @org.junit.Test
  public void versionDeclaration023V3() {
    final XQuery query = new XQuery(
      "xquery encoding \"utf-8\"; 1 eq 1",
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
}
