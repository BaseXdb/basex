package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for function and variable annotations.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdAnnotation extends QT3TestSet {

  /**
   * A function annotation.
   */
  @org.junit.Test
  public void annotation1() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare namespace eg = \"http://example.com\";\n" +
      "         declare %eg:sequential function local:foo() {\n" +
      "            \"bar\"\n" +
      "         };\n" +
      "         local:foo()\n" +
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
      assertSerialization("bar", false)
    );
  }

  /**
   * Annotations with expanded QNames.
   */
  @org.junit.Test
  public void annotation10() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare %Q{http://example.com}bar variable $foo := 0;\n" +
      "\n" +
      "         declare %Q{http://example.com}bar function local:foo($arg) { $arg }; \n" +
      "\n" +
      "         local:foo($foo)\n" +
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
      assertSerialization("0", false)
    );
  }

  /**
   * Multiple annotations.
   */
  @org.junit.Test
  public void annotation11() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare namespace eg = \"http://example.com\";\n" +
      "\n" +
      "         declare %eg:one %eg:two %eg:three variable $foo := 0;\n" +
      "\n" +
      "         declare %eg:one %eg:two %eg:three function local:foo($arg) { $arg }; \n" +
      "\n" +
      "         local:foo($foo)\n" +
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
      assertSerialization("0", false)
    );
  }

  /**
   * Multiple annotations without whitespace ("%" is a delimiting terminal symbol).
   */
  @org.junit.Test
  public void annotation12() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare namespace eg = \"http://example.com\";\n" +
      "\n" +
      "         declare %eg:one%eg:two%eg:three(1)%eg:four variable $foo := 0;\n" +
      "\n" +
      "         declare %eg:one%eg:two%eg:three(1)%eg:four function local:foo($arg) { $arg }; \n" +
      "\n" +
      "         local:foo($foo)\n" +
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
      assertSerialization("0", false)
    );
  }

  /**
   * Multiple annotations with extra whitespace .
   */
  @org.junit.Test
  public void annotation13() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare namespace eg = \"http://example.com\";\n" +
      "\n" +
      "         declare \n" +
      "            %eg:one\n" +
      "            %eg:two\n" +
      "            (: Lorem ipsum dolor sit amet. :)\n" +
      "            %eg:three(1)\n" +
      "            %Q{http://example.com}four\n" +
      "            variable $foo := 0;\n" +
      "\n" +
      "         declare \n" +
      "            %eg:one\n" +
      "            %eg:two\n" +
      "            (: Lorem ipsum dolor sit amet. :)\n" +
      "            %eg:three(1)\n" +
      "            %Q{http://example.com}four\n" +
      "            function local:foo($arg) { $arg }; \n" +
      "\n" +
      "         local:foo($foo)\n" +
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
      assertSerialization("0", false)
    );
  }

  /**
   * Multiple annotations, multiple parameters.
   */
  @org.junit.Test
  public void annotation14() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare namespace eg = \"http://example.com\";\n" +
      "\n" +
      "         declare %eg:one(1, 2, 3) %eg:two(\"a\", \"b\") %eg:three(1.234) variable $foo := 0;\n" +
      "\n" +
      "         declare  %eg:one(1, 2, 3) %eg:two(\"a\", \"b\") %eg:three(1.234) function local:foo($arg) { $arg }; \n" +
      "\n" +
      "         local:foo($foo)\n" +
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
      assertSerialization("0", false)
    );
  }

  /**
   * Annotation in a reserved namespace.
   */
  @org.junit.Test
  public void annotation15() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare %xml:x function local:foo() {\n" +
      "            \"bar\"\n" +
      "         };\n" +
      "         local:foo()\n" +
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
      error("XQST0045")
    );
  }

  /**
   * Annotation in a reserved namespace.
   */
  @org.junit.Test
  public void annotation16() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare %Q{http://www.w3.org/XML/1998/namespace}x variable $foo := \"bar\";\n" +
      "         $foo\n" +
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
      error("XQST0045")
    );
  }

  /**
   * Annotation in a reserved namespace.
   */
  @org.junit.Test
  public void annotation17() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare %xs:x function local:foo() {\n" +
      "            \"bar\"\n" +
      "         };\n" +
      "         local:foo()\n" +
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
      error("XQST0045")
    );
  }

  /**
   * Annotation in a reserved namespace.
   */
  @org.junit.Test
  public void annotation18() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare %Q{http://www.w3.org/2001/XMLSchema}x variable $foo := \"bar\";\n" +
      "         $foo\n" +
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
      error("XQST0045")
    );
  }

  /**
   * Annotation in a reserved namespace.
   */
  @org.junit.Test
  public void annotation19() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare %xsi:x function local:foo() {\n" +
      "            \"bar\"\n" +
      "         };\n" +
      "         local:foo()\n" +
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
      error("XQST0045")
    );
  }

  /**
   * A variable annotation.
   */
  @org.junit.Test
  public void annotation2() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare namespace eg = \"http://example.com\";\n" +
      "         declare %eg:sequential variable $foo := \"bar\";\n" +
      "         $foo\n" +
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
      assertSerialization("bar", false)
    );
  }

  /**
   * Annotation in a reserved namespace.
   */
  @org.junit.Test
  public void annotation20() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare %Q{http://www.w3.org/2001/XMLSchema-instance}x variable $foo := \"bar\";\n" +
      "         $foo\n" +
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
      error("XQST0045")
    );
  }

  /**
   * Annotation in a reserved namespace.
   */
  @org.junit.Test
  public void annotation21() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare %fn:x function local:foo() {\n" +
      "            \"bar\"\n" +
      "         };\n" +
      "         local:foo()\n" +
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
      error("XQST0045")
    );
  }

  /**
   * Annotation in a reserved namespace.
   */
  @org.junit.Test
  public void annotation22() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare %Q{http://www.w3.org/2005/xpath-functions}x variable $foo := \"bar\";\n" +
      "         $foo\n" +
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
      error("XQST0045")
    );
  }

  /**
   * Annotation in no namespace.
   */
  @org.junit.Test
  public void annotation23() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare %x variable $foo := \"bar\";\n" +
      "         $foo\n" +
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
      error("XQST0045")
    );
  }

  /**
   * Annotation in a reserved namespace.
   */
  @org.junit.Test
  public void annotation24() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare namespace math = \"http://www.w3.org/2005/xpath-functions/math\";\n" +
      "         declare %math:x function local:foo() {\n" +
      "            \"bar\"\n" +
      "         };\n" +
      "         local:foo()\n" +
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
      error("XQST0045")
    );
  }

  /**
   * Annotation in a reserved namespace.
   */
  @org.junit.Test
  public void annotation25() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare %Q{http://www.w3.org/2005/xpath-functions/math}x variable $foo := \"bar\";\n" +
      "         $foo\n" +
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
      error("XQST0045")
    );
  }

  /**
   * Annotation in a reserved namespace.
   */
  @org.junit.Test
  public void annotation26() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare namespace opts = \"http://www.w3.org/2012/xquery\";\n" +
      "         declare %opts:x function local:foo() {\n" +
      "            \"bar\"\n" +
      "         };\n" +
      "         local:foo()\n" +
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
      error("XQST0045")
    );
  }

  /**
   * Annotation in a reserved namespace.
   */
  @org.junit.Test
  public void annotation27() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare %Q{http://www.w3.org/2012/xquery}x variable $foo := \"bar\";\n" +
      "         $foo\n" +
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
      error("XQST0045")
    );
  }

  /**
   * A function annotation (default function namespace does not affect annotations).
   */
  @org.junit.Test
  public void annotation28() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare default function namespace \"http://example.com\";\n" +
      "         declare %x variable $foo := \"bar\";\n" +
      "         $foo\n" +
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
      error("XQST0045")
    );
  }

  /**
   * A variable annotation (per bug 16199).
   */
  @org.junit.Test
  public void annotation29() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare %local:x variable $foo := \"bar\";\n" +
      "         $foo\n" +
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
      assertEq("\"bar\"")
    );
  }

  /**
   * An inline function annotation.
   */
  @org.junit.Test
  public void annotation3() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare namespace eg = \"http://example.com\";\n" +
      "         %eg:sequential function () { \"bar\" } ()\n" +
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
      assertSerialization("bar", false)
    );
  }

  /**
   * An inline function annotation with parameters.
   */
  @org.junit.Test
  public void annotation30() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare namespace eg = \"http://example.com\";\n" +
      "         %eg:sequential(\"abc\", 3) function () { \"bar\" } ()\n" +
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
      assertSerialization("bar", false)
    );
  }

  /**
   * An inline function annotation using an EQName.
   */
  @org.junit.Test
  public void annotation31() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare namespace eg = \"http://example.com\";\n" +
      "         % Q{http://example.com}sequential(\"abc\", 3) function () { \"bar\" } ()\n" +
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
      assertSerialization("bar", false)
    );
  }

  /**
   * An inline function with multiple annotations .
   */
  @org.junit.Test
  public void annotation32() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare namespace eg = \"http://example.com\";\n" +
      "         %eg:sequential(\"abc\", 3) %eg:memo-function function () { \"bar\" } ()\n" +
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
      assertSerialization("bar", false)
    );
  }

  /**
   * An inline function using true() as an annotation parameter .
   */
  @org.junit.Test
  public void annotation33() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare namespace eg = \"http://example.com\";\n" +
      "         %eg:sequential(true())  function () { \"bar\" } ()\n" +
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
      error("XPST0003")
    );
  }

  /**
   * String literal annotation parameter.
   */
  @org.junit.Test
  public void annotation4() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare namespace java = \"http://example.com\";\n" +
      "\n" +
      "         declare %java:variable(\"java.lang.Integer.MAX_VALUE\") variable $max := 0;\n" +
      "\n" +
      "         declare %java:method(\"java.lang.Math.sin\") function local:sin($arg) { 0 }; \n" +
      "\n" +
      "         local:sin($max)\n" +
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
      assertSerialization("0", false)
    );
  }

  /**
   * Integer literal annotation parameter.
   */
  @org.junit.Test
  public void annotation5() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare namespace eg = \"http://example.com\";\n" +
      "\n" +
      "         declare %eg:integer(1234) variable $foo := 0;\n" +
      "\n" +
      "         declare %eg:integer(1234) function local:foo($arg) { $arg }; \n" +
      "\n" +
      "         local:foo($foo)\n" +
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
      assertSerialization("0", false)
    );
  }

  /**
   * Decimal literal annotation parameter.
   */
  @org.junit.Test
  public void annotation6() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare namespace eg = \"http://example.com\";\n" +
      "\n" +
      "         declare %eg:integer(12.34) variable $foo := 0;\n" +
      "\n" +
      "         declare %eg:integer(12.34) function local:foo($arg) { $arg }; \n" +
      "\n" +
      "         local:foo($foo)\n" +
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
      assertSerialization("0", false)
    );
  }

  /**
   * Double literal annotation parameter.
   */
  @org.junit.Test
  public void annotation7() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare namespace eg = \"http://example.com\";\n" +
      "\n" +
      "         declare %eg:integer(12e34) variable $foo := 0;\n" +
      "\n" +
      "         declare %eg:integer(12e34) function local:foo($arg) { $arg }; \n" +
      "\n" +
      "         local:foo($foo)\n" +
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
      assertSerialization("0", false)
    );
  }

  /**
   * Non-literal annotation parameter.
   */
  @org.junit.Test
  public void annotation8() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare namespace eg = \"http://example.com\";\n" +
      "\n" +
      "         declare %eg:integer(1+2) function local:foo() { 0 }; \n" +
      "\n" +
      "         local:foo()\n" +
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
      error("XPST0003")
    );
  }

  /**
   * Multiple annotation parameters.
   */
  @org.junit.Test
  public void annotation9() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare namespace eg = \"http://example.com\";\n" +
      "\n" +
      "         declare %eg:many(12e34,\"abc\",1234) variable $foo := 0;\n" +
      "\n" +
      "         declare %eg:many(\"xyz\", 987, 12.3) function local:foo($arg) { $arg }; \n" +
      "\n" +
      "         local:foo($foo)\n" +
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
      assertSerialization("0", false)
    );
  }

  /**
   * 
   *          Tests that a function test with an annotation assertion
   *          parses. The behavior of annotation assertions is
   *          implementation defined.  Annotation assertions can only
   *          further restrict the set of functions matched by a function
   *          test.
   *       .
   */
  @org.junit.Test
  public void annotationAssertion1() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare namespace eg = \"http://example.com\";\n" +
      "         () instance of %eg:x function(*)\n" +
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
      assertBoolean(false)
    );
  }

  /**
   * 
   *           An annotation assertion on a typed function test.
   *           Tests the parsing of annotation assertions. 
   *       .
   */
  @org.junit.Test
  public void annotationAssertion10() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare namespace eg = \"http://example.com\";\n" +
      "         () instance of %eg:x function(xs:integer) as xs:string\n" +
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
      assertBoolean(false)
    );
  }

  /**
   * An annotation assertion in a reserved namespace..
   */
  @org.junit.Test
  public void annotationAssertion11() {
    final XQuery query = new XQuery(
      "\n" +
      "         () instance of %xml:x function(*) \n" +
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
      error("XQST0045")
    );
  }

  /**
   * An annotation assertion in a reserved namespace..
   */
  @org.junit.Test
  public void annotationAssertion12() {
    final XQuery query = new XQuery(
      "\n" +
      "         () instance of %Q{http://www.w3.org/XML/1998/namespace}x function(*) \n" +
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
      error("XQST0045")
    );
  }

  /**
   * An annotation assertion in a reserved namespace..
   */
  @org.junit.Test
  public void annotationAssertion13() {
    final XQuery query = new XQuery(
      "\n" +
      "         () instance of %xs:x function(*) \n" +
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
      error("XQST0045")
    );
  }

  /**
   * An annotation assertion in a reserved namespace..
   */
  @org.junit.Test
  public void annotationAssertion14() {
    final XQuery query = new XQuery(
      "\n" +
      "         () instance of %Q{http://www.w3.org/2001/XMLSchema}x function(*) \n" +
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
      error("XQST0045")
    );
  }

  /**
   * An annotation assertion in a reserved namespace..
   */
  @org.junit.Test
  public void annotationAssertion15() {
    final XQuery query = new XQuery(
      "\n" +
      "         () instance of %xsi:x function(*) \n" +
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
      error("XQST0045")
    );
  }

  /**
   * An annotation assertion in a reserved namespace..
   */
  @org.junit.Test
  public void annotationAssertion16() {
    final XQuery query = new XQuery(
      "\n" +
      "         () instance of %fn:x function(*) \n" +
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
      error("XQST0045")
    );
  }

  /**
   * An annotation assertion in a reserved namespace..
   */
  @org.junit.Test
  public void annotationAssertion17() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare namespace math = \"http://www.w3.org/2005/xpath-functions/math\";\n" +
      "         () instance of %math:x function(*) \n" +
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
      error("XQST0045")
    );
  }

  /**
   * An annotation assertion in a reserved namespace..
   */
  @org.junit.Test
  public void annotationAssertion18() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare namespace opts = \"http://www.w3.org/2012/xquery\";\n" +
      "         () instance of %opts:x function(*) \n" +
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
      error("XQST0045")
    );
  }

  /**
   * 
   *           An annotation assertion with a string literal parameter.  
   *           Tests the parsing of annotation assertions. 
   *       .
   */
  @org.junit.Test
  public void annotationAssertion2() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare namespace eg = \"http://example.com\";\n" +
      "         () instance of %eg:x(\"foo\") function(*)\n" +
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
      assertBoolean(false)
    );
  }

  /**
   * 
   *           An annotation assertion with an integer literal parameter.
   *           Tests the parsing of annotation assertions. 
   *       .
   */
  @org.junit.Test
  public void annotationAssertion3() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare namespace eg = \"http://example.com\";\n" +
      "         () instance of %eg:x(1234) function(*)\n" +
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
      assertBoolean(false)
    );
  }

  /**
   * 
   *           An annotation assertion with a decimal literal parameter.
   *           Tests the parsing of annotation assertions. 
   *       .
   */
  @org.junit.Test
  public void annotationAssertion4() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare namespace eg = \"http://example.com\";\n" +
      "         () instance of %eg:x(12.34) function(*)\n" +
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
      assertBoolean(false)
    );
  }

  /**
   * 
   *           An annotation assertion with a double literal parameter
   *           Tests the parsing of annotation assertions. 
   *       .
   */
  @org.junit.Test
  public void annotationAssertion5() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare namespace eg = \"http://example.com\";\n" +
      "         () instance of %eg:x(12e34) function(*)\n" +
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
      assertBoolean(false)
    );
  }

  /**
   * 
   *           An annotation assertion with multiple parameters
   *           Tests the parsing of annotation assertions. 
   *       .
   */
  @org.junit.Test
  public void annotationAssertion6() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare namespace eg = \"http://example.com\";\n" +
      "         () instance of %eg:x(\"abc\", 12e34, 567) function(*)\n" +
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
      assertBoolean(false)
    );
  }

  /**
   * 
   *           An annotation assertion with an expanded QName
   *           Tests the parsing of annotation assertions. 
   *       .
   */
  @org.junit.Test
  public void annotationAssertion7() {
    final XQuery query = new XQuery(
      "\n" +
      "         () instance of %Q{http://example.com}x function(*)\n" +
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
      assertBoolean(false)
    );
  }

  /**
   * 
   *           An annotation assertion with an expanded QName.
   *           Tests the parsing of annotation assertions. 
   *       .
   */
  @org.junit.Test
  public void annotationAssertion8() {
    final XQuery query = new XQuery(
      "\n" +
      "         () instance of %Q{http://example.com}x function(*)\n" +
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
      assertBoolean(false)
    );
  }

  /**
   * 
   *           Multiple annotation assertions.
   *           Tests the parsing of annotation assertions. 
   *       .
   */
  @org.junit.Test
  public void annotationAssertion9() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare namespace eg = \"http://example.com\";\n" +
      "         () instance of %eg:x %eg:y%eg:z %eg:w(1) function(*)\n" +
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
      assertBoolean(false)
    );
  }
}
