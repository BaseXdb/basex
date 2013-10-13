package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the unparsed-text-available() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnUnparsedTextAvailable extends QT3TestSet {

  /**
   * Attempts to evaluate the "unparsed-text-available" function with no arguments..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable001() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available()",
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
   * Attempts to reference the "unparsed-text-available" function with arity zero..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable002() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available#0",
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
   * Attempts to reference the "unparsed-text-available" function with arity one..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable003() {
    final XQuery query = new XQuery(
      "fn:exists( fn:unparsed-text-available#1 )",
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
   * Attempts to reference the "unparsed-text-available" function with arity two..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable004() {
    final XQuery query = new XQuery(
      "fn:exists( fn:unparsed-text-available#2 )",
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
   * Attempts to evaluate the "unparsed-text-available" function with three arguments..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable005() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available(static-base-uri(), \"utf-8\", \"\")",
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
   * Attempts to reference the "unparsed-text-available" function with arity three..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable006() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available#3",
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
   * Tests the type checking of the $href argument..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable008() {
    final XQuery query = new XQuery(
      "( fn:unparsed-text-available(\n" +
      "                if (current-date() eq xs:date('1900-01-01'))\n" +
      "                then \"http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt\"\n" +
      "                else 1 ),\n" +
      "              fn:unparsed-text-available( \n" +
      "                if (current-date() eq xs:date('1900-01-01'))\n" +
      "                then 1\n" +
      "                else \"http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt\") )",
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
   * Tests the type checking of the $encoding argument..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable010() {
    final XQuery query = new XQuery(
      "( fn:unparsed-text-available( \n" +
      "                \"http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt\",\n" +
      "                if (current-date() eq xs:date('1900-01-01'))\n" +
      "                then \"utf-8\"\n" +
      "                else 1 ),\n" +
      "              fn:unparsed-text-available( \n" +
      "                \"http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt\",\n" +
      "                if (current-date() eq xs:date('1900-01-01'))\n" +
      "                then 1\n" +
      "                else \"utf-8\") )",
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
   * Tests the type checking of the $encoding argument..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable012() {
    final XQuery query = new XQuery(
      "( fn:unparsed-text-available( \n" +
      "                \"http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt\",\n" +
      "                if (current-date() eq xs:date('1900-01-01'))\n" +
      "                then \"utf-8\"\n" +
      "                else () ),\n" +
      "              fn:unparsed-text-available( \n" +
      "                \"http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt\",\n" +
      "                if (current-date() eq xs:date('1900-01-01'))\n" +
      "                then ()\n" +
      "                else \"utf-8\") )",
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
   * Evaluates the "unparsed-text-available" function with the argument set as follows: $href contains a fragment identifier..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable013() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available(\"http://www.example.org/#fragment\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * Evaluates the "unparsed-text-available" function with the argument set as follows: $href contains a fragment identifier..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable014() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available(\"http://www.example.org/#fragment\", \"utf-8\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * Evaluates the "unparsed-text-available" function with the argument set as follows: $href is an invalid xs:anyURI..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable015() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available(\"http://www.example.org/%gg\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * Evaluates the "unparsed-text-available" function with the argument set as follows: $href is an invalid xs:anyURI..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable016() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available(\"http://www.example.org/%gg\", \"utf-8\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * Evaluates the "unparsed-text-available" function with the argument set as follows: $href is an invalid xs:anyURI..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable017() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available(\":/\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * Evaluates the "unparsed-text-available" function with the argument set as follows: $href is an invalid xs:anyURI..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable018() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available(\":/\", \"utf-8\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * Evaluates the "unparsed-text-available" function with the argument set as follows: $href refers to a non-existent file..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable019() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available( \"http://www.w3.org/fots/unparsed-text/does-not-exist.txt\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * Evaluates the "unparsed-text-available" function with the argument set as follows: $href refers to a non-existent file..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable020() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available( \"http://www.w3.org/fots/unparsed-text/does-not-exist.txt\", \"utf-8\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * Evaluates the "unparsed-text-available" function with the argument set as follows: $href uses a (probably) unsupported URI scheme..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable021() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available(\"surely-nobody-supports-this:/path.txt\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * Evaluates the "unparsed-text-available" function with the argument set as follows: $href uses a (probably) unsupported URI scheme..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable022() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available(\"surely-nobody-supports-this:/path.txt\", \"utf-8\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * Evaluates the "unparsed-text-available" function with the argument set as follows: $href is a relative URI, static base URI undefined..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable023() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available(\"unparsed-text/text-plain-utf-8.txt\")",
      ctx);
    try {
      query.baseURI("#UNDEFINED");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0001")
    );
  }

  /**
   * Evaluates the "unparsed-text-available" function with the argument set as follows: $href is a relative URI, static base URI undefined..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable024() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available(\"unparsed-text/text-plain-utf-8.txt\", \"utf-8\")",
      ctx);
    try {
      query.baseURI("#UNDEFINED");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0001")
    );
  }

  /**
   * Evaluates the "unparsed-text-available" function with the argument set as follows: $href is a relative URI, static base is defined..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable025() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available(\"does-not-exist.txt\")",
      ctx);
    try {
      query.baseURI("http://www.w3.org/fots/unparsed-text/");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * Evaluates the "unparsed-text-available" function with the argument set as follows: $href is a relative URI, static base is defined..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable026() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available(\"does-not-exist.txt\", \"utf-8\")",
      ctx);
    try {
      query.baseURI("http://www.w3.org/fots/unparsed-text/");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * Evaluates the "unparsed-text-available" function with the argument set as follows: $href is a relative URI, static base is defined..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable027() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available(\"text-plain-utf-8.txt\")",
      ctx);
    try {
      query.baseURI("http://www.w3.org/fots/unparsed-text/");
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
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
   * Evaluates the "unparsed-text-available" function with the argument set as follows: $href is a relative URI, static base is defined..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable028() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available(\"text-plain-utf-8.txt\", \"utf-8\")",
      ctx);
    try {
      query.baseURI("http://www.w3.org/fots/unparsed-text/");
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
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
   * Evaluates the "unparsed-text-available" function with the argument set as follows: $href is a Windows file path..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable029() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available(\"C:\\file-might-exist.txt\")",
      ctx);
    try {
      query.baseURI("http://www.w3.org/fots/unparsed-text/");
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * Evaluates the "unparsed-text-available" function with the argument set as follows: $href is a Windows file path..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable030() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available(\"C:\\file-might-exist.txt\", \"utf-8\")",
      ctx);
    try {
      query.baseURI("http://www.w3.org/fots/unparsed-text/");
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * Evaluates the "unparsed-text-available" function with the argument set as follows: $href is an empty string..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable031() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available(\"\")",
      ctx);
    try {
      query.baseURI("http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt");
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
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
   * Evaluates the "unparsed-text-available" function with the argument set as follows: $href is an empty string..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable032() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available(\"\", \"utf-8\")",
      ctx);
    try {
      query.baseURI("http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt");
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
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
   * Evaluates the "unparsed-text-available" function with the argument set as follows: $href is an empty string..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable033() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available(\"\")",
      ctx);
    try {
      query.baseURI("http://www.w3.org/fots/unparsed-text/does-not-exists.txt");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * Evaluates the "unparsed-text-available" function with the argument set as follows: $href is an empty string..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable034() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available(\"\", \"utf-8\")",
      ctx);
    try {
      query.baseURI("http://www.w3.org/fots/unparsed-text/does-not-exists.txt");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * Evaluates the "unparsed-text-available" function with the argument set as follows: $encoding is invalid..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable035() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available(\"http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt\", \"123\")",
      ctx);
    try {
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-iso-8859-1.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-invalid.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-utf-8.xml
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-utf-16.xml
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-iso-8859-1.xml
      // resource: http://www.w3.org/fots/unparsed-text/non-xml-character.txt
      // resource: http://www.w3.org/fots/unparsed-text/invalid-xml.xml
      // resource: http://www.w3.org/fots/unparsed-text/unknown-encoding.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom-lines.txt
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * Evaluates the "unparsed-text-available" function with the argument set as follows: $href refers to a file whose encoding cannot be determined..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable036() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available(\"http://www.w3.org/fots/unparsed-text/unknown-encoding.txt\")",
      ctx);
    try {
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-iso-8859-1.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-invalid.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-utf-8.xml
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-utf-16.xml
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-iso-8859-1.xml
      // resource: http://www.w3.org/fots/unparsed-text/non-xml-character.txt
      // resource: http://www.w3.org/fots/unparsed-text/invalid-xml.xml
      // resource: http://www.w3.org/fots/unparsed-text/unknown-encoding.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom-lines.txt
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * Evaluates the "unparsed-text-available" function with the argument set as follows: $href refers to a file whose encoding is invalid for utf-8..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable037() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available(\"http://www.w3.org/fots/unparsed-text/text-plain-utf-8-invalid.txt\")",
      ctx);
    try {
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-iso-8859-1.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-invalid.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-utf-8.xml
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-utf-16.xml
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-iso-8859-1.xml
      // resource: http://www.w3.org/fots/unparsed-text/non-xml-character.txt
      // resource: http://www.w3.org/fots/unparsed-text/invalid-xml.xml
      // resource: http://www.w3.org/fots/unparsed-text/unknown-encoding.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom-lines.txt
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * Evaluates the "unparsed-text-available" function with the argument set as follows: $href refers to a file which contains non-XML characters..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable038() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available(\"http://www.w3.org/fots/unparsed-text/non-xml-character.txt\")",
      ctx);
    try {
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-iso-8859-1.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-invalid.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-utf-8.xml
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-utf-16.xml
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-iso-8859-1.xml
      // resource: http://www.w3.org/fots/unparsed-text/non-xml-character.txt
      // resource: http://www.w3.org/fots/unparsed-text/invalid-xml.xml
      // resource: http://www.w3.org/fots/unparsed-text/unknown-encoding.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom-lines.txt
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * Evaluates the "unparsed-text-available" function with the argument set as follows: $href refers to a file which does not contain well-formed XML..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable039() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available(\"http://www.w3.org/fots/unparsed-text/invalid-xml.xml\")",
      ctx);
    try {
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-iso-8859-1.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-invalid.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-utf-8.xml
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-utf-16.xml
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-iso-8859-1.xml
      // resource: http://www.w3.org/fots/unparsed-text/non-xml-character.txt
      // resource: http://www.w3.org/fots/unparsed-text/invalid-xml.xml
      // resource: http://www.w3.org/fots/unparsed-text/unknown-encoding.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom-lines.txt
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
   * Evaluates the "unparsed-text-available" function with the argument set as follows: $href refers to a file whose encoding is valid utf-8..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable040() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available(\"http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt\")",
      ctx);
    try {
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-iso-8859-1.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-invalid.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-utf-8.xml
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-utf-16.xml
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-iso-8859-1.xml
      // resource: http://www.w3.org/fots/unparsed-text/non-xml-character.txt
      // resource: http://www.w3.org/fots/unparsed-text/invalid-xml.xml
      // resource: http://www.w3.org/fots/unparsed-text/unknown-encoding.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom-lines.txt
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
   * Evaluates the "unparsed-text-available" function with the argument set as follows: $href refers to a file whose encoding is valid utf-8..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable041() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available(\"http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom.txt\")",
      ctx);
    try {
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-iso-8859-1.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-invalid.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-utf-8.xml
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-utf-16.xml
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-iso-8859-1.xml
      // resource: http://www.w3.org/fots/unparsed-text/non-xml-character.txt
      // resource: http://www.w3.org/fots/unparsed-text/invalid-xml.xml
      // resource: http://www.w3.org/fots/unparsed-text/unknown-encoding.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom-lines.txt
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
   * Evaluates the "unparsed-text-available" function with the argument set as follows: $href refers to a file whose encoding is valid utf-16..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable042() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available(\"http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom.txt\")",
      ctx);
    try {
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-iso-8859-1.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-invalid.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-utf-8.xml
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-utf-16.xml
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-iso-8859-1.xml
      // resource: http://www.w3.org/fots/unparsed-text/non-xml-character.txt
      // resource: http://www.w3.org/fots/unparsed-text/invalid-xml.xml
      // resource: http://www.w3.org/fots/unparsed-text/unknown-encoding.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom-lines.txt
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
   * Evaluates the "unparsed-text-available" function with the argument set as follows: $href refers to a file whose encoding is valid utf-16..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable043() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available(\"http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom.txt\")",
      ctx);
    try {
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-iso-8859-1.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-invalid.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-utf-8.xml
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-utf-16.xml
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-iso-8859-1.xml
      // resource: http://www.w3.org/fots/unparsed-text/non-xml-character.txt
      // resource: http://www.w3.org/fots/unparsed-text/invalid-xml.xml
      // resource: http://www.w3.org/fots/unparsed-text/unknown-encoding.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom-lines.txt
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
   * Evaluates the "unparsed-text-available" function with the argument set as follows: $href refers to a file whose encoding is valid iso-8859-1..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable044() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available(\"http://www.w3.org/fots/unparsed-text/text-plain-iso-8859-1.txt\")",
      ctx);
    try {
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-iso-8859-1.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-invalid.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-utf-8.xml
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-utf-16.xml
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-iso-8859-1.xml
      // resource: http://www.w3.org/fots/unparsed-text/non-xml-character.txt
      // resource: http://www.w3.org/fots/unparsed-text/invalid-xml.xml
      // resource: http://www.w3.org/fots/unparsed-text/unknown-encoding.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom-lines.txt
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
        assertBoolean(false)
      )
    );
  }

  /**
   * Evaluates the "unparsed-text-available" function with the argument set as follows: $href refers to a file whose encoding is valid utf-8..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable045() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available(\"http://www.w3.org/fots/unparsed-text/text-xml-utf-8.xml\")",
      ctx);
    try {
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-iso-8859-1.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-invalid.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-utf-8.xml
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-utf-16.xml
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-iso-8859-1.xml
      // resource: http://www.w3.org/fots/unparsed-text/non-xml-character.txt
      // resource: http://www.w3.org/fots/unparsed-text/invalid-xml.xml
      // resource: http://www.w3.org/fots/unparsed-text/unknown-encoding.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom-lines.txt
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
   * Evaluates the "unparsed-text-available" function with the argument set as follows: $href refers to a file whose encoding is valid utf-16..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable046() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available(\"http://www.w3.org/fots/unparsed-text/text-xml-utf-16.xml\")",
      ctx);
    try {
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-iso-8859-1.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-invalid.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-utf-8.xml
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-utf-16.xml
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-iso-8859-1.xml
      // resource: http://www.w3.org/fots/unparsed-text/non-xml-character.txt
      // resource: http://www.w3.org/fots/unparsed-text/invalid-xml.xml
      // resource: http://www.w3.org/fots/unparsed-text/unknown-encoding.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom-lines.txt
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
   * Evaluates the "unparsed-text-available" function with the argument set as follows: $href refers to a file whose encoding is valid iso-8859-1..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable047() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available(\"http://www.w3.org/fots/unparsed-text/text-xml-iso-8859-1.xml\")",
      ctx);
    try {
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-iso-8859-1.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-invalid.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-utf-8.xml
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-utf-16.xml
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-iso-8859-1.xml
      // resource: http://www.w3.org/fots/unparsed-text/non-xml-character.txt
      // resource: http://www.w3.org/fots/unparsed-text/invalid-xml.xml
      // resource: http://www.w3.org/fots/unparsed-text/unknown-encoding.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom-lines.txt
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
        assertBoolean(false)
      )
    );
  }

  /**
   * Evaluates the "unparsed-text-available" function with the argument set as follows: $href refers to a file containing various line endings of the form 0A, x0D, and x0Dx0A..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable048() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available(\"http://www.w3.org/fots/unparsed-text/text-plain-utf-8-lines.txt\")",
      ctx);
    try {
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-iso-8859-1.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-invalid.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-utf-8.xml
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-utf-16.xml
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-iso-8859-1.xml
      // resource: http://www.w3.org/fots/unparsed-text/non-xml-character.txt
      // resource: http://www.w3.org/fots/unparsed-text/invalid-xml.xml
      // resource: http://www.w3.org/fots/unparsed-text/unknown-encoding.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom-lines.txt
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
   * Evaluates the "unparsed-text-available" function with the argument set as follows: $href refers to a file containing various line endings of the form 0A, x0D, and x0Dx0A..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable049() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available(\"http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines.txt\")",
      ctx);
    try {
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-iso-8859-1.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-invalid.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-utf-8.xml
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-utf-16.xml
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-iso-8859-1.xml
      // resource: http://www.w3.org/fots/unparsed-text/non-xml-character.txt
      // resource: http://www.w3.org/fots/unparsed-text/invalid-xml.xml
      // resource: http://www.w3.org/fots/unparsed-text/unknown-encoding.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom-lines.txt
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
   * Evaluates the "unparsed-text-available" function with the argument set as follows: $href refers to a file containing various line endings of the form 0A, x0D, and x0Dx0A..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable050() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available(\"http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom-lines.txt\")",
      ctx);
    try {
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-iso-8859-1.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-invalid.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-utf-8.xml
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-utf-16.xml
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-iso-8859-1.xml
      // resource: http://www.w3.org/fots/unparsed-text/non-xml-character.txt
      // resource: http://www.w3.org/fots/unparsed-text/invalid-xml.xml
      // resource: http://www.w3.org/fots/unparsed-text/unknown-encoding.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom-lines.txt
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
   * Evaluates the "unparsed-text-available" function with the argument set as follows: $href refers to a file containing various line endings of the form 0A, x0D, and x0Dx0A..
   */
  @org.junit.Test
  public void fnUnparsedTextAvailable051() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-available(\"http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom-lines.txt\")",
      ctx);
    try {
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-iso-8859-1.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-invalid.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-utf-8.xml
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-utf-16.xml
      // resource: http://www.w3.org/fots/unparsed-text/text-xml-iso-8859-1.xml
      // resource: http://www.w3.org/fots/unparsed-text/non-xml-character.txt
      // resource: http://www.w3.org/fots/unparsed-text/invalid-xml.xml
      // resource: http://www.w3.org/fots/unparsed-text/unknown-encoding.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom-lines.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom-lines.txt
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
