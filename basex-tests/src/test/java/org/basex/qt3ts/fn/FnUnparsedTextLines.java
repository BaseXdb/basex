package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the unparsed-text-lines() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnUnparsedTextLines extends QT3TestSet {

  /**
   * Attempts to evaluate the "unparsed-text-lines" function with no arguments..
   */
  @org.junit.Test
  public void fnUnparsedTextLines001() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines()",
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
   * Attempts to reference the "unparsed-text-lines" function with arity zero..
   */
  @org.junit.Test
  public void fnUnparsedTextLines002() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines#0",
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
   * Attempts to reference the "unparsed-text-lines" function with arity one..
   */
  @org.junit.Test
  public void fnUnparsedTextLines003() {
    final XQuery query = new XQuery(
      "fn:exists(fn:unparsed-text-lines#1)",
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
   * Attempts to reference the "unparsed-text-lines" function with arity two..
   */
  @org.junit.Test
  public void fnUnparsedTextLines004() {
    final XQuery query = new XQuery(
      "fn:exists(fn:unparsed-text-lines#2)",
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
   * Attempts to evaluate the "unparsed-text-lines" function with three arguments..
   */
  @org.junit.Test
  public void fnUnparsedTextLines005() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines(static-base-uri(), \"utf-8\", \"\")",
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
   * Attempts to reference the "unparsed-text-lines" function with arity three..
   */
  @org.junit.Test
  public void fnUnparsedTextLines006() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines#3",
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
  public void fnUnparsedTextLines008() {
    final XQuery query = new XQuery(
      "( fn:unparsed-text-lines( \n" +
      "                if (current-date() eq xs:date('1900-01-01'))\n" +
      "                then \"http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt\"\n" +
      "                else 1 ),\n" +
      "              fn:unparsed-text-lines( \n" +
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
  public void fnUnparsedTextLines010() {
    final XQuery query = new XQuery(
      "( fn:unparsed-text-lines( \n" +
      "                \"http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt\",\n" +
      "                if (current-date() eq xs:date('1900-01-01'))\n" +
      "                then \"utf-8\"\n" +
      "                else 1 ),\n" +
      "              fn:unparsed-text-lines( \n" +
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
  public void fnUnparsedTextLines012() {
    final XQuery query = new XQuery(
      "( fn:unparsed-text-lines( \n" +
      "              \"http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt\",\n" +
      "              if (current-date() eq xs:date('1900-01-01'))\n" +
      "              then \"utf-8\"\n" +
      "              else () ),\n" +
      "              fn:unparsed-text-lines( \n" +
      "              \"http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt\",\n" +
      "              if (current-date() eq xs:date('1900-01-01'))\n" +
      "              then ()\n" +
      "              else \"utf-8\") )",
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
   * Evaluates the "unparsed-text-lines" function with the argument set as follows: $href contains a fragment identifier..
   */
  @org.junit.Test
  public void fnUnparsedTextLines013() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines(\"http://www.example.org/#fragment\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOUT1170")
    );
  }

  /**
   * Evaluates the "unparsed-text-lines" function with the argument set as follows: $href contains a fragment identifier..
   */
  @org.junit.Test
  public void fnUnparsedTextLines014() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines(\"http://www.example.org/#fragment\", \"utf-8\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOUT1170")
    );
  }

  /**
   * Evaluates the "unparsed-text-lines" function with the argument set as follows: $href is an invalid xs:anyURI..
   */
  @org.junit.Test
  public void fnUnparsedTextLines015() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines(\"http://www.example.org/%gg\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOUT1170")
    );
  }

  /**
   * Evaluates the "unparsed-text-lines" function with the argument set as follows: $href is an invalid xs:anyURI..
   */
  @org.junit.Test
  public void fnUnparsedTextLines016() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines(\"http://www.example.org/%gg\", \"utf-8\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOUT1170")
    );
  }

  /**
   * Evaluates the "unparsed-text-lines" function with the argument set as follows: $href is an invalid xs:anyURI..
   */
  @org.junit.Test
  public void fnUnparsedTextLines017() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines(\":/\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOUT1170")
    );
  }

  /**
   * Evaluates the "unparsed-text-lines" function with the argument set as follows: $href is an invalid xs:anyURI..
   */
  @org.junit.Test
  public void fnUnparsedTextLines018() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines(\":/\", \"utf-8\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOUT1170")
    );
  }

  /**
   * Evaluates the "unparsed-text-lines" function with the argument set as follows: $href refers to a non-existent file..
   */
  @org.junit.Test
  public void fnUnparsedTextLines019() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines( \"http://www.w3.org/fots/unparsed-text/does-not-exist.txt\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOUT1170")
    );
  }

  /**
   * Evaluates the "unparsed-text-lines" function with the argument set as follows: $href refers to a non-existent file..
   */
  @org.junit.Test
  public void fnUnparsedTextLines020() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines( \"http://www.w3.org/fots/unparsed-text/does-not-exist.txt\", \"utf-8\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOUT1170")
    );
  }

  /**
   * Evaluates the "unparsed-text-lines" function with the argument set as follows: $href uses a (probably) unsupported URI scheme..
   */
  @org.junit.Test
  public void fnUnparsedTextLines021() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines(\"surely-nobody-supports-this:/path.txt\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOUT1170")
    );
  }

  /**
   * Evaluates the "unparsed-text-lines" function with the argument set as follows: $href uses a (probably) unsupported URI scheme..
   */
  @org.junit.Test
  public void fnUnparsedTextLines022() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines(\"surely-nobody-supports-this:/path.txt\", \"utf-8\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOUT1170")
    );
  }

  /**
   * Evaluates the "unparsed-text-lines" function with the argument set as follows: $href is a relative URI, static base URI undefined..
   */
  @org.junit.Test
  public void fnUnparsedTextLines023() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines(\"unparsed-text/text-plain-utf-8.txt\")",
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
   * Evaluates the "unparsed-text-lines" function with the argument set as follows: $href is a relative URI, static base URI undefined..
   */
  @org.junit.Test
  public void fnUnparsedTextLines024() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines(\"unparsed-text/text-plain-utf-8.txt\", \"utf-8\")",
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
   * Evaluates the "unparsed-text-lines" function with the argument set as follows: $href is a relative URI, static base is defined..
   */
  @org.junit.Test
  public void fnUnparsedTextLines025() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines(\"does-not-exist.txt\")",
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
      error("FOUT1170")
    );
  }

  /**
   * Evaluates the "unparsed-text-lines" function with the argument set as follows: $href is a relative URI, static base is defined..
   */
  @org.junit.Test
  public void fnUnparsedTextLines026() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines(\"does-not-exist.txt\", \"utf-8\")",
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
      error("FOUT1170")
    );
  }

  /**
   * Evaluates the "unparsed-text-lines" function with the argument set as follows: $href is a relative URI, static base is defined..
   */
  @org.junit.Test
  public void fnUnparsedTextLines027() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines(\"text-plain-utf-8.txt\")",
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
      assertStringValue(false, "hello world")
    );
  }

  /**
   * Evaluates the "unparsed-text-lines" function with the argument set as follows: $href is a relative URI, static base is defined..
   */
  @org.junit.Test
  public void fnUnparsedTextLines028() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines(\"text-plain-utf-8.txt\", \"utf-8\")",
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
      assertStringValue(false, "hello world")
    );
  }

  /**
   * Evaluates the "unparsed-text-lines" function with the argument set as follows: $href is a Windows file path..
   */
  @org.junit.Test
  public void fnUnparsedTextLines029() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines(\"C:\\file-might-exist.txt\")",
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
      error("FOUT1170")
    );
  }

  /**
   * Evaluates the "unparsed-text-lines" function with the argument set as follows: $href is a Windows file path..
   */
  @org.junit.Test
  public void fnUnparsedTextLines030() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines(\"C:\\file-might-exist.txt\", \"utf-8\")",
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
      error("FOUT1170")
    );
  }

  /**
   * Evaluates the "unparsed-text-lines" function with the argument set as follows: $href is an empty string..
   */
  @org.junit.Test
  public void fnUnparsedTextLines031() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines(\"\")",
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
      assertStringValue(false, "hello world")
    );
  }

  /**
   * Evaluates the "unparsed-text-lines" function with the argument set as follows: $href is an empty string..
   */
  @org.junit.Test
  public void fnUnparsedTextLines032() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines(\"\", \"utf-8\")",
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
      assertStringValue(false, "hello world")
    );
  }

  /**
   * Evaluates the "unparsed-text-lines" function with the argument set as follows: $href is an empty string..
   */
  @org.junit.Test
  public void fnUnparsedTextLines033() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines(\"\")",
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
      error("FOUT1170")
    );
  }

  /**
   * Evaluates the "unparsed-text-lines" function with the argument set as follows: $href is an empty string..
   */
  @org.junit.Test
  public void fnUnparsedTextLines034() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines(\"\", \"utf-8\")",
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
      error("FOUT1170")
    );
  }

  /**
   * Test the effect of a call to fn:unparsed-text-lines which need not be evaluated.   Since implementations are allowed to dereference the sources in any case, an error is allowed..
   */
  @org.junit.Test
  public void fnUnparsedTextLines035() {
    final XQuery query = new XQuery(
      "(1, fn:unparsed-text-lines(\"http://www.w3.org/fots/unparsed-text/does-not-exist.txt\"))[1]",
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
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-2.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-3.txt
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
        assertStringValue(false, "1")
      ||
        error("FOUT1170")
      )
    );
  }

  /**
   * Evaluates the "unparsed-text-lines" function with the argument set as follows: $encoding is invalid..
   */
  @org.junit.Test
  public void fnUnparsedTextLines036() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines(\"http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt\", \"123\")",
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
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-2.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-3.txt
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
      error("FOUT1190")
    );
  }

  /**
   * Evaluates the "unparsed-text-lines" function with the argument set as follows: $href refers to a file whose encoding cannot be determined..
   */
  @org.junit.Test
  public void fnUnparsedTextLines037() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines(\"http://www.w3.org/fots/unparsed-text/unknown-encoding.txt\")",
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
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-2.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-3.txt
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
      error("FOUT1200")
    );
  }

  /**
   * Evaluates the "unparsed-text-lines" function with the argument set as follows: $href refers to a file whose encoding is invalid for utf-8..
   */
  @org.junit.Test
  public void fnUnparsedTextLines038() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines(\"http://www.w3.org/fots/unparsed-text/text-plain-utf-8-invalid.txt\")",
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
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-2.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-3.txt
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
      error("FOUT1190")
    );
  }

  /**
   * Evaluates the "unparsed-text-lines" function with the argument set as follows: $href refers to a file which contains non-XML characters..
   */
  @org.junit.Test
  public void fnUnparsedTextLines039() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines(\"http://www.w3.org/fots/unparsed-text/non-xml-character.txt\")",
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
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-2.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-3.txt
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
      error("FOUT1190")
    );
  }

  /**
   * Evaluates the "unparsed-text-lines" function with the argument set as follows: $href refers to a file which does not contain well-formed XML.  A processor is not required to support utf-16..
   */
  @org.junit.Test
  public void fnUnparsedTextLines040() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines(\"http://www.w3.org/fots/unparsed-text/invalid-xml.xml\")",
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
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-2.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-3.txt
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
        assertDeepEq("'<?xml version=\"1.0\" encoding=\"utf-16\"?><text>hello world'")
      ||
        error("FOUT1200")
      )
    );
  }

  /**
   * Evaluates the "unparsed-text-lines" function with the argument set as follows: $href refers to a file whose encoding is valid utf-8..
   */
  @org.junit.Test
  public void fnUnparsedTextLines041() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines(\"http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt\")",
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
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-2.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-3.txt
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
      assertStringValue(false, "hello world")
    );
  }

  /**
   * Evaluates the "unparsed-text-lines" function with the argument set as follows: $href refers to a file whose encoding is valid utf-8..
   */
  @org.junit.Test
  public void fnUnparsedTextLines042() {
    final XQuery query = new XQuery(
      "fn:unparsed-text(\"http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom.txt\")",
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
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-2.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-3.txt
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
      assertStringValue(false, "hello world")
    );
  }

  /**
   * Evaluates the "unparsed-text-lines" function with the argument set as follows: $href refers to a file whose encoding is valid utf-16..
   */
  @org.junit.Test
  public void fnUnparsedTextLines043() {
    final XQuery query = new XQuery(
      "fn:unparsed-text(\"http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom.txt\")",
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
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-2.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-3.txt
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
      assertStringValue(false, "hello world")
    );
  }

  /**
   * Evaluates the "unparsed-text-lines" function with the argument set as follows: $href refers to a file whose encoding is valid utf-16..
   */
  @org.junit.Test
  public void fnUnparsedTextLines044() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines(\"http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom.txt\")",
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
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-2.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-3.txt
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
      assertStringValue(false, "hello world")
    );
  }

  /**
   * Evaluates the "unparsed-text-lines" function with the argument set as follows: $href refers to a file whose encoding is valid iso-8859-1..
   */
  @org.junit.Test
  public void fnUnparsedTextLines045() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines(\"http://www.w3.org/fots/unparsed-text/text-plain-iso-8859-1.txt\")",
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
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-2.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-3.txt
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
        assertStringValue(false, "hello world")
      ||
        error("FOUT1190")
      )
    );
  }

  /**
   * Evaluates the "unparsed-text-lines" function with the argument set as follows: $href refers to a file whose encoding is valid utf-8..
   */
  @org.junit.Test
  public void fnUnparsedTextLines046() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines(\"http://www.w3.org/fots/unparsed-text/text-xml-utf-8.xml\")",
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
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-2.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-3.txt
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
      assertStringValue(false, "<?xml version=\"1.0\" encoding=\"utf-8\"?><text>hello world</text>")
    );
  }

  /**
   * 
   *          Evaluates the "unparsed-text-lines" function with the argument set as follows: 
   *          $href refers to a file whose encoding is valid utf-16.  A processor is not required to support utf-16..
   */
  @org.junit.Test
  public void fnUnparsedTextLines047() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines(\"http://www.w3.org/fots/unparsed-text/text-xml-utf-16.xml\")",
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
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-2.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-3.txt
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
        assertStringValue(false, "<?xml version=\"1.0\" encoding=\"utf-16\"?><text>hello world</text>")
      ||
        error("FOUT1200")
      )
    );
  }

  /**
   * 
   *          Evaluates the "unparsed-text-lines" function with the argument set as follows: 
   *          $href refers to a file whose encoding is valid iso-8859-1..
   */
  @org.junit.Test
  public void fnUnparsedTextLines048() {
    final XQuery query = new XQuery(
      "fn:unparsed-text(\"http://www.w3.org/fots/unparsed-text/text-xml-iso-8859-1.xml\")",
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
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-2.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-3.txt
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
        assertStringValue(false, "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?><text>hello world</text>")
      ||
        error("FOUT1190")
      )
    );
  }

  /**
   * 
   *          Evaluates the "unparsed-text-lines" function with the argument set as follows: 
   *          $href refers to a file containing various line endings of the form 0A, x0D, and x0Dx0A..
   */
  @org.junit.Test
  public void fnUnparsedTextLines049() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines(\"http://www.w3.org/fots/unparsed-text/text-plain-utf-8-lines.txt\") ! string-length(.)",
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
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-2.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-3.txt
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
      assertDeepEq("(53, 54, 179, 77, 32)")
    );
  }

  /**
   * 
   *          Evaluates the "unparsed-text-lines" function with the argument set as follows: 
   *          $href refers to a file containing various line endings of the form 0A, x0D, and x0Dx0A.
   *       .
   */
  @org.junit.Test
  public void fnUnparsedTextLines050() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines(\"http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines.txt\") ! string-length(.)",
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
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-2.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-3.txt
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
      assertDeepEq("(53, 54, 179, 77, 32)")
    );
  }

  /**
   * 
   *          Evaluates the "unparsed-text-lines" function with the argument set as follows: 
   *          $href refers to a file containing various line endings of the form 0A, x0D, and x0Dx0A.  A processor is not required to support utf-16.
   *       .
   */
  @org.junit.Test
  public void fnUnparsedTextLines051() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines(\"http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom-lines.txt\") ! string-length(.)",
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
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-2.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-3.txt
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
        assertDeepEq("(53, 54, 179, 77, 32)")
      ||
        error("FOUT1200")
      )
    );
  }

  /**
   * 
   *          Evaluates the "unparsed-text-lines" function with the argument set as follows: 
   *          $href refers to a file containing various line endings of the form 0A, x0D, and x0Dx0A.  A processor is not required to support utf-16.
   *       .
   */
  @org.junit.Test
  public void fnUnparsedTextLines052() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines(\"http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom-lines.txt\") ! string-length(.)",
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
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-2.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-3.txt
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
        assertDeepEq("(53, 54, 179, 77, 32)")
      ||
        error("FOUT1200")
      )
    );
  }

  /**
   * 
   *          Evaluates the "unparsed-text-lines" function with the argument set as follows: 
   *          $href refers to a file containing various line endings of the form 0A, x0D, and x0Dx0A.
   *       .
   */
  @org.junit.Test
  public void fnUnparsedTextLines053() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines(\"http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-2.txt\") ! string-length(.)",
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
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-2.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-3.txt
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
      assertDeepEq("(50, 0, 0, 50, 0, 0)")
    );
  }

  /**
   * 
   *          Evaluates the "unparsed-text-lines" function with the argument set as follows: 
   *          $href refers to a file containing various line endings of the form 0A, x0D, and x0Dx0A.
   *       .
   */
  @org.junit.Test
  public void fnUnparsedTextLines054() {
    final XQuery query = new XQuery(
      "fn:unparsed-text-lines(\"http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-3.txt\") ! string-length(.)",
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
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-2.txt
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines-3.txt
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
      assertDeepEq("(46, 0, 20)")
    );
  }
}
