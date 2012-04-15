package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the unparsed-text() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnUnparsedText extends QT3TestSet {

  /**
   * Attempts to evaluate the "unparsed-text" function with no arguments..
   */
  @org.junit.Test
  public void fnUnparsedText001() {
    final XQuery query = new XQuery(
      "fn:unparsed-text()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   * Attempts to reference the "unparsed-text" function with arity zero..
   */
  @org.junit.Test
  public void fnUnparsedText002() {
    final XQuery query = new XQuery(
      "fn:unparsed-text#0",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   * Attempts to reference the "unparsed-text" function with arity one..
   */
  @org.junit.Test
  public void fnUnparsedText003() {
    final XQuery query = new XQuery(
      "fn:unparsed-text#1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to reference the "unparsed-text" function with arity two..
   */
  @org.junit.Test
  public void fnUnparsedText004() {
    final XQuery query = new XQuery(
      "fn:unparsed-text#2",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to evaluate the "unparsed-text" function with three arguments..
   */
  @org.junit.Test
  public void fnUnparsedText005() {
    final XQuery query = new XQuery(
      "fn:unparsed-text(static-base-uri(), \"utf-8\", \"\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   * Attempts to reference the "unparsed-text" function with arity three..
   */
  @org.junit.Test
  public void fnUnparsedText006() {
    final XQuery query = new XQuery(
      "fn:unparsed-text#3",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   * Tests the type checking of the $href argument..
   */
  @org.junit.Test
  public void fnUnparsedText008() {
    final XQuery query = new XQuery(
      "( fn:unparsed-text( if (current-date() eq xs:date('1900-01-01'))\n" +
      "                                then \"http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt\"\n" +
      "                                else 1 ),\n" +
      "              fn:unparsed-text( if (current-date() eq xs:date('1900-01-01'))\n" +
      "                                then 1\n" +
      "                                else \"http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt\") )",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * Tests the type checking of the $encoding argument..
   */
  @org.junit.Test
  public void fnUnparsedText010() {
    final XQuery query = new XQuery(
      "( fn:unparsed-text( \"http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt\",\n" +
      "                                if (current-date() eq xs:date('1900-01-01'))\n" +
      "                                then \"utf-8\"\n" +
      "                                else 1 ),\n" +
      "              fn:unparsed-text( \"http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt\",\n" +
      "                                if (current-date() eq xs:date('1900-01-01'))\n" +
      "                                then 1\n" +
      "                                else \"utf-8\") )",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * Tests the type checking of the $encoding argument..
   */
  @org.junit.Test
  public void fnUnparsedText012() {
    final XQuery query = new XQuery(
      "( fn:unparsed-text( \"http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt\",\n" +
      "                                if (current-date() eq xs:date('1900-01-01'))\n" +
      "                                then \"utf-8\"\n" +
      "                                else () ),\n" +
      "              fn:unparsed-text( \"http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt\",\n" +
      "                                if (current-date() eq xs:date('1900-01-01'))\n" +
      "                                then ()\n" +
      "                                else \"utf-8\") )",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * Evaluates the "unparsed-text" function with the argument set as follows: $href contains a fragment identifier..
   */
  @org.junit.Test
  public void fnUnparsedText013() {
    final XQuery query = new XQuery(
      "fn:unparsed-text(\"http://www.example.org/#fragment\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOUT1170")
    );
  }

  /**
   * Evaluates the "unparsed-text" function with the argument set as follows: $href contains a fragment identifier..
   */
  @org.junit.Test
  public void fnUnparsedText014() {
    final XQuery query = new XQuery(
      "fn:unparsed-text(\"http://www.example.org/#fragment\", \"utf-8\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOUT1170")
    );
  }

  /**
   * Evaluates the "unparsed-text" function with the argument set as follows: $href is an invalid xs:anyURI..
   */
  @org.junit.Test
  public void fnUnparsedText015() {
    final XQuery query = new XQuery(
      "fn:unparsed-text(\"http://www.example.org/%gg\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOUT1170")
    );
  }

  /**
   * Evaluates the "unparsed-text" function with the argument set as follows: $href is an invalid xs:anyURI..
   */
  @org.junit.Test
  public void fnUnparsedText016() {
    final XQuery query = new XQuery(
      "fn:unparsed-text(\"http://www.example.org/%gg\", \"utf-8\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOUT1170")
    );
  }

  /**
   * Evaluates the "unparsed-text" function with the argument set as follows: $href is an invalid xs:anyURI..
   */
  @org.junit.Test
  public void fnUnparsedText017() {
    final XQuery query = new XQuery(
      "fn:unparsed-text(\":/\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOUT1170")
    );
  }

  /**
   * Evaluates the "unparsed-text" function with the argument set as follows: $href is an invalid xs:anyURI..
   */
  @org.junit.Test
  public void fnUnparsedText018() {
    final XQuery query = new XQuery(
      "fn:unparsed-text(\":/\", \"utf-8\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOUT1170")
    );
  }

  /**
   * Evaluates the "unparsed-text" function with the argument set as follows: $href refers to a non-existent file..
   */
  @org.junit.Test
  public void fnUnparsedText019() {
    final XQuery query = new XQuery(
      "fn:unparsed-text( \"http://www.w3.org/fots/unparsed-text/does-not-exist.txt\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOUT1170")
    );
  }

  /**
   * Evaluates the "unparsed-text" function with the argument set as follows: $href refers to a non-existent file..
   */
  @org.junit.Test
  public void fnUnparsedText020() {
    final XQuery query = new XQuery(
      "fn:unparsed-text( \"http://www.w3.org/fots/unparsed-text/does-not-exist.txt\", \"utf-8\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOUT1170")
    );
  }

  /**
   * Evaluates the "unparsed-text" function with the argument set as follows: $href uses a (probably) unsupported URI scheme..
   */
  @org.junit.Test
  public void fnUnparsedText021() {
    final XQuery query = new XQuery(
      "fn:unparsed-text(\"surely-nobody-supports-this:/path.txt\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOUT1170")
    );
  }

  /**
   * Evaluates the "unparsed-text" function with the argument set as follows: $href uses a (probably) unsupported URI scheme..
   */
  @org.junit.Test
  public void fnUnparsedText022() {
    final XQuery query = new XQuery(
      "fn:unparsed-text(\"surely-nobody-supports-this:/path.txt\", \"utf-8\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOUT1170")
    );
  }

  /**
   * Evaluates the "unparsed-text" function with the argument set as follows: $href is a relative URI, static base URI undefined..
   */
  @org.junit.Test
  public void fnUnparsedText023() {
    final XQuery query = new XQuery(
      "fn:unparsed-text(\"unparsed-text/text-plain-utf-8.txt\")",
      ctx);
    query.baseURI("#UNDEFINED");

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0001")
    );
  }

  /**
   * Evaluates the "unparsed-text" function with the argument set as follows: $href is a relative URI, static base URI undefined..
   */
  @org.junit.Test
  public void fnUnparsedText024() {
    final XQuery query = new XQuery(
      "fn:unparsed-text(\"unparsed-text/text-plain-utf-8.txt\", \"utf-8\")",
      ctx);
    query.baseURI("#UNDEFINED");

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0001")
    );
  }

  /**
   * Evaluates the "unparsed-text" function with the argument set as follows: $href is a relative URI, static base is defined..
   */
  @org.junit.Test
  public void fnUnparsedText025() {
    final XQuery query = new XQuery(
      "fn:unparsed-text(\"does-not-exist.txt\")",
      ctx);
    query.baseURI("http://www.w3.org/fots/unparsed-text/");

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOUT1170")
    );
  }

  /**
   * Evaluates the "unparsed-text" function with the argument set as follows: $href is a relative URI, static base is defined..
   */
  @org.junit.Test
  public void fnUnparsedText026() {
    final XQuery query = new XQuery(
      "fn:unparsed-text(\"does-not-exist.txt\", \"utf-8\")",
      ctx);
    query.baseURI("http://www.w3.org/fots/unparsed-text/");

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOUT1170")
    );
  }

  /**
   * Evaluates the "unparsed-text" function with the argument set as follows: $href is a relative URI, static base is defined..
   */
  @org.junit.Test
  public void fnUnparsedText027() {
    final XQuery query = new XQuery(
      "fn:unparsed-text(\"text-plain-utf-8.txt\")",
      ctx);
    query.baseURI("http://www.w3.org/fots/unparsed-text/");
    // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "hello world")
    );
  }

  /**
   * Evaluates the "unparsed-text" function with the argument set as follows: $href is a relative URI, static base is defined..
   */
  @org.junit.Test
  public void fnUnparsedText028() {
    final XQuery query = new XQuery(
      "fn:unparsed-text(\"text-plain-utf-8.txt\", \"utf-8\")",
      ctx);
    query.baseURI("http://www.w3.org/fots/unparsed-text/");
    // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "hello world")
    );
  }

  /**
   * Evaluates the "unparsed-text" function with the argument set as follows: $href is a Windows file path..
   */
  @org.junit.Test
  public void fnUnparsedText029() {
    final XQuery query = new XQuery(
      "fn:unparsed-text(\"C:\\file-might-exist.txt\")",
      ctx);
    query.baseURI("http://www.w3.org/fots/unparsed-text/");
    // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOUT1170")
    );
  }

  /**
   * Evaluates the "unparsed-text" function with the argument set as follows: $href is a Windows file path..
   */
  @org.junit.Test
  public void fnUnparsedText030() {
    final XQuery query = new XQuery(
      "fn:unparsed-text(\"C:\\file-might-exist.txt\", \"utf-8\")",
      ctx);
    query.baseURI("http://www.w3.org/fots/unparsed-text/");
    // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOUT1170")
    );
  }

  /**
   * Evaluates the "unparsed-text" function with the argument set as follows: $href is an empty string..
   */
  @org.junit.Test
  public void fnUnparsedText031() {
    final XQuery query = new XQuery(
      "fn:unparsed-text(\"\")",
      ctx);
    query.baseURI("http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt");
    // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "hello world")
    );
  }

  /**
   * Evaluates the "unparsed-text" function with the argument set as follows: $href is an empty string..
   */
  @org.junit.Test
  public void fnUnparsedText032() {
    final XQuery query = new XQuery(
      "fn:unparsed-text(\"\", \"utf-8\")",
      ctx);
    query.baseURI("http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt");
    // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "hello world")
    );
  }

  /**
   * Evaluates the "unparsed-text" function with the argument set as follows: $href is an empty string..
   */
  @org.junit.Test
  public void fnUnparsedText033() {
    final XQuery query = new XQuery(
      "fn:unparsed-text(\"\")",
      ctx);
    query.baseURI("http://www.w3.org/fots/unparsed-text/does-not-exists.txt");

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOUT1170")
    );
  }

  /**
   * Evaluates the "unparsed-text" function with the argument set as follows: $href is an empty string..
   */
  @org.junit.Test
  public void fnUnparsedText034() {
    final XQuery query = new XQuery(
      "fn:unparsed-text(\"\", \"utf-8\")",
      ctx);
    query.baseURI("http://www.w3.org/fots/unparsed-text/does-not-exists.txt");

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOUT1170")
    );
  }

  /**
   * Test the effect of a call to fn:unparsed-text which need not be evaluated.   Since implementations are allowed to dereference the sources in any case, an error is allowed..
   */
  @org.junit.Test
  public void fnUnparsedText035() {
    final XQuery query = new XQuery(
      "(1, fn:unparsed-text(\"http://www.w3.org/fots/unparsed-text/does-not-exist.txt\"))[1]",
      ctx);
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

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "1")
      ||
        error("FOUT1170")
      )
    );
  }

  /**
   * Evaluates the "unparsed-text" function with the argument set as follows: $encoding is invalid..
   */
  @org.junit.Test
  public void fnUnparsedText036() {
    final XQuery query = new XQuery(
      "fn:unparsed-text(\"http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt\", \"123\")",
      ctx);
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

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOUT1190")
    );
  }

  /**
   * Evaluates the "unparsed-text" function with the argument set as follows: $href refers to a file whose encoding cannot be determined..
   */
  @org.junit.Test
  public void fnUnparsedText037() {
    final XQuery query = new XQuery(
      "fn:unparsed-text(\"http://www.w3.org/fots/unparsed-text/unknown-encoding.txt\")",
      ctx);
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

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOUT1200")
    );
  }

  /**
   * Evaluates the "unparsed-text" function with the argument set as follows: $href refers to a file whose encoding is invalid for utf-8..
   */
  @org.junit.Test
  public void fnUnparsedText038() {
    final XQuery query = new XQuery(
      "fn:unparsed-text(\"http://www.w3.org/fots/unparsed-text/text-plain-utf-8-invalid.txt\")",
      ctx);
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

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOUT1190")
    );
  }

  /**
   * Evaluates the "unparsed-text" function with the argument set as follows: $href refers to a file which contains non-XML characters..
   */
  @org.junit.Test
  public void fnUnparsedText039() {
    final XQuery query = new XQuery(
      "fn:unparsed-text(\"http://www.w3.org/fots/unparsed-text/non-xml-character.txt\")",
      ctx);
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

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOUT1190")
    );
  }

  /**
   * Evaluates the "unparsed-text" function with the argument set as follows: $href refers to a file which does not contain well-formed XML..
   */
  @org.junit.Test
  public void fnUnparsedText040() {
    final XQuery query = new XQuery(
      "fn:unparsed-text(\"http://www.w3.org/fots/unparsed-text/invalid-xml.xml\")",
      ctx);
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

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "<?xml version=\"1.0\" encoding=\"utf-16\"?><text>hello world\r\n")
    );
  }

  /**
   * Evaluates the "unparsed-text" function with the argument set as follows: $href refers to a file whose encoding is valid utf-8..
   */
  @org.junit.Test
  public void fnUnparsedText041() {
    final XQuery query = new XQuery(
      "fn:unparsed-text(\"http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt\")",
      ctx);
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

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "hello world")
    );
  }

  /**
   * Evaluates the "unparsed-text" function with the argument set as follows: $href refers to a file whose encoding is valid utf-8..
   */
  @org.junit.Test
  public void fnUnparsedText042() {
    final XQuery query = new XQuery(
      "fn:unparsed-text(\"http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom.txt\")",
      ctx);
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

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "hello world")
    );
  }

  /**
   * Evaluates the "unparsed-text" function with the argument set as follows: $href refers to a file whose encoding is valid utf-16..
   */
  @org.junit.Test
  public void fnUnparsedText043() {
    final XQuery query = new XQuery(
      "fn:unparsed-text(\"http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom.txt\")",
      ctx);
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

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "hello world")
    );
  }

  /**
   * Evaluates the "unparsed-text" function with the argument set as follows: $href refers to a file whose encoding is valid utf-16..
   */
  @org.junit.Test
  public void fnUnparsedText044() {
    final XQuery query = new XQuery(
      "fn:unparsed-text(\"http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom.txt\")",
      ctx);
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

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "hello world")
    );
  }

  /**
   * Evaluates the "unparsed-text" function with the argument set as follows: $href refers to a file whose encoding is valid iso-8859-1..
   */
  @org.junit.Test
  public void fnUnparsedText045() {
    final XQuery query = new XQuery(
      "fn:unparsed-text(\"http://www.w3.org/fots/unparsed-text/text-plain-iso-8859-1.txt\")",
      ctx);
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

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "hello world")
      ||
        error("FOUT1190")
      )
    );
  }

  /**
   * Evaluates the "unparsed-text" function with the argument set as follows: $href refers to a file whose encoding is valid utf-8..
   */
  @org.junit.Test
  public void fnUnparsedText046() {
    final XQuery query = new XQuery(
      "fn:unparsed-text(\"http://www.w3.org/fots/unparsed-text/text-xml-utf-8.xml\")",
      ctx);
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

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "<?xml version=\"1.0\" encoding=\"utf-8\"?><text>hello world</text>")
    );
  }

  /**
   * Evaluates the "unparsed-text" function with the argument set as follows: $href refers to a file whose encoding is valid utf-16..
   */
  @org.junit.Test
  public void fnUnparsedText047() {
    final XQuery query = new XQuery(
      "fn:unparsed-text(\"http://www.w3.org/fots/unparsed-text/text-xml-utf-16.xml\")",
      ctx);
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

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "<?xml version=\"1.0\" encoding=\"utf-16\"?><text>hello world</text>")
    );
  }

  /**
   * Evaluates the "unparsed-text" function with the argument set as follows: $href refers to a file whose encoding is valid iso-8859-1..
   */
  @org.junit.Test
  public void fnUnparsedText048() {
    final XQuery query = new XQuery(
      "fn:unparsed-text(\"http://www.w3.org/fots/unparsed-text/text-xml-iso-8859-1.xml\")",
      ctx);
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

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?><text>hello world</text>")
      ||
        error("FOUT1190")
      )
    );
  }

  /**
   * Evaluates the "unparsed-text" function with the argument set as follows: $href refers to a file containing various line endings of the form 0A, x0D, and x0Dx0A..
   */
  @org.junit.Test
  public void fnUnparsedText049() {
    final XQuery query = new XQuery(
      "string-length(fn:unparsed-text(\"http://www.w3.org/fots/unparsed-text/text-plain-utf-8-lines.txt\"))",
      ctx);
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

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "400")
    );
  }

  /**
   * Evaluates the "unparsed-text" function with the argument set as follows: $href refers to a file containing various line endings of the form 0A, x0D, and x0Dx0A..
   */
  @org.junit.Test
  public void fnUnparsedText050() {
    final XQuery query = new XQuery(
      "string-length(fn:unparsed-text(\"http://www.w3.org/fots/unparsed-text/text-plain-utf-8-bom-lines.txt\"))",
      ctx);
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

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "400")
    );
  }

  /**
   * Evaluates the "unparsed-text" function with the argument set as follows: $href refers to a file containing various line endings of the form 0A, x0D, and x0Dx0A..
   */
  @org.junit.Test
  public void fnUnparsedText051() {
    final XQuery query = new XQuery(
      "string-length(fn:unparsed-text(\"http://www.w3.org/fots/unparsed-text/text-plain-utf-16le-bom-lines.txt\"))",
      ctx);
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

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "400")
    );
  }

  /**
   * Evaluates the "unparsed-text" function with the argument set as follows: $href refers to a file containing various line endings of the form 0A, x0D, and x0Dx0A..
   */
  @org.junit.Test
  public void fnUnparsedText052() {
    final XQuery query = new XQuery(
      "string-length(fn:unparsed-text(\"http://www.w3.org/fots/unparsed-text/text-plain-utf-16be-bom-lines.txt\"))",
      ctx);
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

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "400")
    );
  }
}
