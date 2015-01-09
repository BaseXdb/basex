package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the escape-html-uri() function.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnEscapeHtmlUri extends QT3TestSet {

  /**
   *  A test whose essence is: `escape-html-uri()`. .
   */
  @org.junit.Test
  public void kEscapeHTMLURIFunc1() {
    final XQuery query = new XQuery(
      "escape-html-uri()",
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
   *  A test whose essence is: `escape-html-uri("http://example.com/", "wrong param")`. .
   */
  @org.junit.Test
  public void kEscapeHTMLURIFunc2() {
    final XQuery query = new XQuery(
      "escape-html-uri(\"http://example.com/\", \"wrong param\")",
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
   *  A test whose essence is: `escape-html-uri(()) eq ""`. .
   */
  @org.junit.Test
  public void kEscapeHTMLURIFunc3() {
    final XQuery query = new XQuery(
      "escape-html-uri(()) eq \"\"",
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
   *  Combine fn:concat and fn:escape-html-uri. .
   */
  @org.junit.Test
  public void kEscapeHTMLURIFunc4() {
    final XQuery query = new XQuery(
      "escape-html-uri(\"http://www.example.com/00/Weather/CA/Los Angeles#ocean\") eq \"http://www.example.com/00/Weather/CA/Los Angeles#ocean\"",
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
   *  Combine fn:concat and fn:escape-html-uri. .
   */
  @org.junit.Test
  public void kEscapeHTMLURIFunc5() {
    final XQuery query = new XQuery(
      "escape-html-uri(\"javascript:if (navigator.browserLanguage == 'fr') window.open('http://www.example.com/~bébé');\") eq \"javascript:if (navigator.browserLanguage == 'fr') window.open('http://www.example.com/~b%C3%A9b%C3%A9');\"",
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
   *  Invoke fn:normalize-space() on the return value of fn:escape-html-uri(). .
   */
  @org.junit.Test
  public void kEscapeHTMLURIFunc6() {
    final XQuery query = new XQuery(
      "normalize-space(iri-to-uri((\"example.com\", current-time())[1] treat as xs:string))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "example.com")
    );
  }

  /**
   *  test fn:escape-html-uri with a variety of characters .
   */
  @org.junit.Test
  public void cbclEscapeHtmlUri001() {
    final XQuery query = new XQuery(
      "fn:escape-html-uri(codepoints-to-string((9, 65, 128)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "%09A%C2%80")
    );
  }

  /**
   *  Examines that the fn:escape-html-uri function does not escape the lower cases letters. .
   */
  @org.junit.Test
  public void fnEscapeHtmlUri1() {
    final XQuery query = new XQuery(
      "fn:escape-html-uri(\"abcdedfghijklmnopqrstuvwxyz\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abcdedfghijklmnopqrstuvwxyz")
    );
  }

  /**
   *  Examines that the fn:escape-html-uri function does not escape the ")" symbol. .
   */
  @org.junit.Test
  public void fnEscapeHtmlUri10() {
    final XQuery query = new XQuery(
      "fn:escape-html-uri(\"example)example\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "example)example")
    );
  }

  /**
   *  Examines that the fn:escape-html-uri function does not escape the "*" symbol. .
   */
  @org.junit.Test
  public void fnEscapeHtmlUri11() {
    final XQuery query = new XQuery(
      "fn:escape-html-uri(\"example*example\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "example*example")
    );
  }

  /**
   *  Examines that the fn:escape-html-uri function does not escape the "+" symbol. .
   */
  @org.junit.Test
  public void fnEscapeHtmlUri12() {
    final XQuery query = new XQuery(
      "fn:escape-html-uri(\"example+example\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "example+example")
    );
  }

  /**
   *  Examines that the fn:escape-html-uri function does not escape the "," symbol. .
   */
  @org.junit.Test
  public void fnEscapeHtmlUri13() {
    final XQuery query = new XQuery(
      "fn:escape-html-uri(\"example,example\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "example,example")
    );
  }

  /**
   *  Examines that the fn:escape-html-uri function does not escape the "-" symbol. .
   */
  @org.junit.Test
  public void fnEscapeHtmlUri14() {
    final XQuery query = new XQuery(
      "fn:escape-html-uri(\"example-example\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "example-example")
    );
  }

  /**
   *  Examines that the fn:escape-html-uri function does not escape the "." symbol. .
   */
  @org.junit.Test
  public void fnEscapeHtmlUri15() {
    final XQuery query = new XQuery(
      "fn:escape-html-uri(\"example.example\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "example.example")
    );
  }

  /**
   *  Examines that the fn:escape-html-uri function does not escape the "/" symbol. .
   */
  @org.junit.Test
  public void fnEscapeHtmlUri16() {
    final XQuery query = new XQuery(
      "fn:escape-html-uri(\"example/example\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "example/example")
    );
  }

  /**
   *  Examines that the fn:escape-html-uri function does not escape the ";" symbol. .
   */
  @org.junit.Test
  public void fnEscapeHtmlUri17() {
    final XQuery query = new XQuery(
      "fn:escape-html-uri(\"example;example\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "example;example")
    );
  }

  /**
   *  Examines that the fn:escape-html-uri function does not escape the ":" symbol. .
   */
  @org.junit.Test
  public void fnEscapeHtmlUri18() {
    final XQuery query = new XQuery(
      "fn:escape-html-uri(\"example:example\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "example:example")
    );
  }

  /**
   *  Examines that the fn:escape-html-uri function does not escape the "@" symbol. .
   */
  @org.junit.Test
  public void fnEscapeHtmlUri19() {
    final XQuery query = new XQuery(
      "fn:escape-html-uri(\"example@example\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "example@example")
    );
  }

  /**
   *  Examines that the fn:escape-html-uri function does not escape the upper cases letters. .
   */
  @org.junit.Test
  public void fnEscapeHtmlUri2() {
    final XQuery query = new XQuery(
      "fn:escape-html-uri(\"ABCDEFGHIJKLMNOPQRSTUVWXYZ\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ABCDEFGHIJKLMNOPQRSTUVWXYZ")
    );
  }

  /**
   *  Examines that the fn:escape-html-uri function does escape the euro symbol. .
   */
  @org.junit.Test
  public void fnEscapeHtmlUri20() {
    final XQuery query = new XQuery(
      "fn:escape-html-uri(\"exampleé€example\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "example%C3%A9%E2%82%ACexample")
    );
  }

  /**
   *  Examines that the fn:escape-html-uri function does escape the euro symbol. .
   */
  @org.junit.Test
  public void fnEscapeHtmlUri21() {
    final XQuery query = new XQuery(
      "fn:escape-html-uri(\"example€example\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "example%E2%82%ACexample")
    );
  }

  /**
   *  Examines that the fn:escape-html-uri function does not escape digits. .
   */
  @org.junit.Test
  public void fnEscapeHtmlUri3() {
    final XQuery query = new XQuery(
      "fn:escape-html-uri(\"a0123456789\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a0123456789")
    );
  }

  /**
   *  Examines that the fn:escape-html-uri function does not escape the space. .
   */
  @org.junit.Test
  public void fnEscapeHtmlUri4() {
    final XQuery query = new XQuery(
      "fn:escape-html-uri(\"example example\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "example example")
    );
  }

  /**
   *  Examines that the fn:escape-html-uri function does not escape the "!" symbol. .
   */
  @org.junit.Test
  public void fnEscapeHtmlUri5() {
    final XQuery query = new XQuery(
      "fn:escape-html-uri(\"example!example\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "example!example")
    );
  }

  /**
   *  Examines that the fn:escape-html-uri function does not escape the "#" symbol. .
   */
  @org.junit.Test
  public void fnEscapeHtmlUri6() {
    final XQuery query = new XQuery(
      "fn:escape-html-uri(\"example#example\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "example#example")
    );
  }

  /**
   *  Examines that the fn:escape-html-uri function does not escape the "$" symbol. .
   */
  @org.junit.Test
  public void fnEscapeHtmlUri7() {
    final XQuery query = new XQuery(
      "fn:escape-html-uri(\"example$example\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "example$example")
    );
  }

  /**
   *  Examines that the fn:escape-html-uri function does not escape the "'" symbol. .
   */
  @org.junit.Test
  public void fnEscapeHtmlUri8() {
    final XQuery query = new XQuery(
      "fn:escape-html-uri(\"example'example\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "example'example")
    );
  }

  /**
   *  Examines that the fn:escape-html-uri function does not escape the "(" symbol. .
   */
  @org.junit.Test
  public void fnEscapeHtmlUri9() {
    final XQuery query = new XQuery(
      "fn:escape-html-uri(\"example(example\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "example(example")
    );
  }

  /**
   * Test escape-html-uri from example defined in functions and operators specification .
   */
  @org.junit.Test
  public void fnEscapeHtmlUri1args1() {
    final XQuery query = new XQuery(
      "escape-html-uri(\"http://www.example.com/00/Weather/CA/Los Angeles#ocean\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.com/00/Weather/CA/Los Angeles#ocean")
    );
  }

  /**
   * Test escape-html-uri from example defined in functions and operators specification .
   */
  @org.junit.Test
  public void fnEscapeHtmlUri1args2() {
    final XQuery query = new XQuery(
      "escape-html-uri(\"javascript:if (navigator.browserLanguage == 'fr') window.open('http://www.example.com/~bébé');\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "javascript:if (navigator.browserLanguage == 'fr') window.open('http://www.example.com/~b%C3%A9b%C3%A9');")
    );
  }

  /**
   * Test escape-html-uri with zero-length string argument .
   */
  @org.junit.Test
  public void fnEscapeHtmlUri1args3() {
    final XQuery query = new XQuery(
      "escape-html-uri('')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
   * Test escape-html-uri with empty sequence argument .
   */
  @org.junit.Test
  public void fnEscapeHtmlUri1args4() {
    final XQuery query = new XQuery(
      "escape-html-uri(())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
   * Test escape-html-uri with invalid argument types .
   */
  @org.junit.Test
  public void fnEscapeHtmlUri1args5() {
    final XQuery query = new XQuery(
      "escape-html-uri(12)",
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
   * Test escape-html-uri with incorrect arity .
   */
  @org.junit.Test
  public void fnEscapeHtmlUri1args6() {
    final XQuery query = new XQuery(
      "escape-html-uri('',())",
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
}
