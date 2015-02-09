package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the NameTest production.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdNameTest extends QT3TestSet {

  /**
   *  A nodetest appearing in a wrong place, leading to syntax error. .
   */
  @org.junit.Test
  public void kNameTest1() {
    final XQuery query = new XQuery(
      "1 + remove((\"foo\", 2), 2)asdasdad",
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
   *  Invalid string literals and nametests mixed, stressing tokenizer and parser code. .
   */
  @org.junit.Test
  public void kNameTest10() {
    final XQuery query = new XQuery(
      "\"f oo\" eq \"f oo",
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
   *  Invalid string literals and nametests mixed, stressing tokenizer and parser code. .
   */
  @org.junit.Test
  public void kNameTest11() {
    final XQuery query = new XQuery(
      "normalize-unicode(\"f oo\") eq \"f oo",
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
   *  '---..---' is an invalid nodetest. .
   */
  @org.junit.Test
  public void kNameTest2() {
    final XQuery query = new XQuery(
      "---..---",
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
   *  'foo-' is an invalid nametest. Whitespace is wrong. .
   */
  @org.junit.Test
  public void kNameTest3() {
    final XQuery query = new XQuery(
      "foo- foo",
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
   *  'nametest : nametest' is a syntactically invalid expression. .
   */
  @org.junit.Test
  public void kNameTest4() {
    final XQuery query = new XQuery(
      "nametest : nametest",
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
   *  'nametest: nametest' is a syntactically invalid expression. .
   */
  @org.junit.Test
  public void kNameTest5() {
    final XQuery query = new XQuery(
      "nametest: nametest",
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
   *  'nametest :nametest' is a syntactically invalid expression. .
   */
  @org.junit.Test
  public void kNameTest6() {
    final XQuery query = new XQuery(
      "nametest :nametest",
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
   *  Invalid string literals and nametests mixed, stressing tokenizer and parser code. .
   */
  @org.junit.Test
  public void kNameTest7() {
    final XQuery query = new XQuery(
      "f oo\" eq \"f oo\"",
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
   *  Invalid string literals and nametests mixed, stressing tokenizer and parser code. .
   */
  @org.junit.Test
  public void kNameTest8() {
    final XQuery query = new XQuery(
      "\"f oo eq \"f oo\"",
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
   *  Invalid string literals and nametests mixed, stressing tokenizer and parser code. .
   */
  @org.junit.Test
  public void kNameTest9() {
    final XQuery query = new XQuery(
      "\"f oo\" eq f oo\"",
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
   *  "//" by itself is not a valid path expression. .
   */
  @org.junit.Test
  public void k2NameTest1() {
    final XQuery query = new XQuery(
      "//",
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
   *  A node test must follow "parent::" is used. .
   */
  @org.junit.Test
  public void k2NameTest10() {
    final XQuery query = new XQuery(
      "parent::",
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
   *  No binding existing for a "prefix:*" test. .
   */
  @org.junit.Test
  public void k2NameTest11() {
    final XQuery query = new XQuery(
      "no-binding:*",
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
   *  A nametest being "xml:space". .
   */
  @org.junit.Test
  public void k2NameTest12() {
    final XQuery query = new XQuery(
      "declare variable $var := <elem xml:space=\"default\"/>; $var/@xml:space eq \"default\"",
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
   *  A nametest being "xml:*". .
   */
  @org.junit.Test
  public void k2NameTest13() {
    final XQuery query = new XQuery(
      "declare variable $var := <elem xml:space=\"preserve\"/>; string(($var/@xml:*)[1])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "preserve")
    );
  }

  /**
   *  A nametest being "child::*". .
   */
  @org.junit.Test
  public void k2NameTest14() {
    final XQuery query = new XQuery(
      "declare variable $var := <elem>text<a/><!-- a comment --><b/><?target data?><c/><![CDATA[more text]]></elem>; $var/child::*",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a/><b/><c/>", false)
    );
  }

  /**
   *  A name test matching many different kinds of nodes. .
   */
  @org.junit.Test
  public void k2NameTest15() {
    final XQuery query = new XQuery(
      "declare variable $e := <a b =\"content\"><?b asd?><b/><c b=\"content\"/></a>; $e/b",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<b/>", false)
    );
  }

  /**
   *  A wild-card name test matching only elements. .
   */
  @org.junit.Test
  public void k2NameTest16() {
    final XQuery query = new XQuery(
      "declare variable $e := <a b =\"content\"><?b asd?><b/></a>; $e/*",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<b/>", false)
    );
  }

  /**
   *  A name test matching only attributes. .
   */
  @org.junit.Test
  public void k2NameTest17() {
    final XQuery query = new XQuery(
      "declare variable $e := <a b =\"content\"><?b asd?><b/></a>; <a>{$e/@b}</a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a b=\"content\"/>", false)
    );
  }

  /**
   *  A wild-card name test matching only attributes. .
   */
  @org.junit.Test
  public void k2NameTest18() {
    final XQuery query = new XQuery(
      "declare variable $e := <a b =\"content\"><?b asd?><b/></a>; <a>{$e/@*}</a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a b=\"content\"/>", false)
    );
  }

  /**
   *  A name test matching only processing-instructions. .
   */
  @org.junit.Test
  public void k2NameTest19() {
    final XQuery query = new XQuery(
      "declare variable $e := <a b =\"content\"><?b asd?><b/></a>; $e/processing-instruction(b)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<?b asd?>", false)
    );
  }

  /**
   *  "/*5" is a syntax error. .
   */
  @org.junit.Test
  public void k2NameTest2() {
    final XQuery query = new XQuery(
      "/*5",
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
   *  A wild-card name test matching only processing-instructions. .
   */
  @org.junit.Test
  public void k2NameTest20() {
    final XQuery query = new XQuery(
      "declare variable $e := <a b =\"content\"><?b asd?><b/></a>; <a>{$e/processing-instruction()}</a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a><?b asd?></a>", false)
    );
  }

  /**
   *  A processing-instruction() with an invalid NCName in a string literal. .
   */
  @org.junit.Test
  public void k2NameTest21() {
    final XQuery query = new XQuery(
      "empty(let $e := <a b =\"content\"><?b asd?><b/></a> return $e/processing-instruction(\"123ncname\"))",
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
   *  A processing-instruction() with an invalid NCName in a string literal. .
   */
  @org.junit.Test
  public void k2NameTest22() {
    final XQuery query = new XQuery(
      "empty(let $e := <a b =\"content\"><?b asd?><b/></a> return $e/processing-instruction(\"b \"))",
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
   *  A processing-instruction() with an invalid NCName in a string literal. .
   */
  @org.junit.Test
  public void k2NameTest23() {
    final XQuery query = new XQuery(
      "empty(let $e := <a b =\"content\"><?b asd?><b/></a> return $e/processing-instruction(\"prefix:b\"))",
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
   *  An integer literal cannot be specified as PI name. .
   */
  @org.junit.Test
  public void k2NameTest24() {
    final XQuery query = new XQuery(
      "<e/>/processing-instruction(1))",
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
   *  A QName cannot be specified as PI name. .
   */
  @org.junit.Test
  public void k2NameTest25() {
    final XQuery query = new XQuery(
      "<e/>/processing-instruction(prefix:ncname))",
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
   *  A variable reference cannot be specified as PI name. .
   */
  @org.junit.Test
  public void k2NameTest26() {
    final XQuery query = new XQuery(
      "let $name := \"ncname\" return <e/>/processing-instruction($name))",
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
   *  A variable reference cannot be specified as PI name. .
   */
  @org.junit.Test
  public void k2NameTest27() {
    final XQuery query = new XQuery(
      "let $name := \"ncname\" return <e/>/processing-instruction($name))",
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
   *  A element name cannot be a string literal, inside element(). .
   */
  @org.junit.Test
  public void k2NameTest28() {
    final XQuery query = new XQuery(
      "<e/>/element(\"any\"))",
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
   *  A element name cannot be a string literal, inside element(). .
   */
  @org.junit.Test
  public void k2NameTest29() {
    final XQuery query = new XQuery(
      "<e/>/attribute(\"any\"))",
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
   *  "/*5" is a syntax error. .
   */
  @org.junit.Test
  public void k2NameTest3() {
    final XQuery query = new XQuery(
      "/ * 5",
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
   *  A element name cannot be a string literal, inside element(). .
   */
  @org.junit.Test
  public void k2NameTest30() {
    final XQuery query = new XQuery(
      "declare namespace a = \"http://example.com/1\"; declare namespace b = \"http://example.com/2\"; let $e := <e a:n1=\"content\" b:n1=\"content\"> <a:n1/> <b:n1/> <?n1 ?> <n1/> </e> return $e/*:n1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a:n1 xmlns:b=\"http://example.com/2\" xmlns:a=\"http://example.com/1\"/><b:n1 xmlns:b=\"http://example.com/2\" xmlns:a=\"http://example.com/1\"/><n1 xmlns:b=\"http://example.com/2\" xmlns:a=\"http://example.com/1\"/>", false)
    );
  }

  /**
   *  A element name cannot be a string literal, inside element(). .
   */
  @org.junit.Test
  public void k2NameTest31() {
    final XQuery query = new XQuery(
      "declare namespace a = \"http://example.com/1\"; declare namespace b = \"http://example.com/2\"; let $e := <e a:n1=\"content\" b:n1=\"content\"> <a:n1/> <b:n1/> <?n1 ?> <n1/> </e> return $e/a:*",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a:n1 xmlns:b=\"http://example.com/2\" xmlns:a=\"http://example.com/1\"/>", false)
    );
  }

  /**
   *  attribute(name) as part of a step. .
   */
  @org.junit.Test
  public void k2NameTest32() {
    final XQuery query = new XQuery(
      "<a>{<e foo=\"content2\" bar=\"content1\"/>/attribute(foo)}</a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a foo=\"content2\"/>", false)
    );
  }

  /**
   *  String literals aren't allowed in schema-element(). .
   */
  @org.junit.Test
  public void k2NameTest33() {
    final XQuery query = new XQuery(
      "schema-element(\"quotesAreNotAllowed\")",
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
   *  String literals aren't allowed in schema-attribute(). .
   */
  @org.junit.Test
  public void k2NameTest34() {
    final XQuery query = new XQuery(
      "schema-attribute(\"quotesAreNotAllowed\")",
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
   *  An undeclared prefix inside a name in schema-element() is an error. a little comment .
   */
  @org.junit.Test
  public void k2NameTest35() {
    final XQuery query = new XQuery(
      "schema-element(notDeclared:ncname)",
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
   *  An undeclared prefix inside a name in schema-attribute() is an error. a little comment .
   */
  @org.junit.Test
  public void k2NameTest36() {
    final XQuery query = new XQuery(
      "schema-attribute(notDeclared:ncname)",
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
   *  Specifying an unknown type in schema-element() is an error. .
   */
  @org.junit.Test
  public void k2NameTest37() {
    final XQuery query = new XQuery(
      "schema-element(thisTypeDoesNotExistExample.Com)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  Specifying an unknown type in schema-attribute() is an error. .
   */
  @org.junit.Test
  public void k2NameTest38() {
    final XQuery query = new XQuery(
      "schema-attribute(thisTypeDoesNotExistExample.Com)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  Specifying an unknown type in schema-element() is an error(with namespace). .
   */
  @org.junit.Test
  public void k2NameTest39() {
    final XQuery query = new XQuery(
      "declare namespace e = \"http://www.example.com/\"; schema-element(e:thisTypeDoesNotExistExample.Com)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  "4 + / * 5" is a syntax error. .
   */
  @org.junit.Test
  public void k2NameTest4() {
    final XQuery query = new XQuery(
      "4 + / * 5",
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
   *  Specifying an unknown type in schema-attribute() is an error(with namespace). .
   */
  @org.junit.Test
  public void k2NameTest40() {
    final XQuery query = new XQuery(
      "declare namespace e = \"http://www.example.com/\"; schema-attribute(e:thisTypeDoesNotExistExample.Com)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  Specifying an unknown prefix raises an error. .
   */
  @org.junit.Test
  public void k2NameTest41() {
    final XQuery query = new XQuery(
      "unknownprefix:*",
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
   *  Specifying an unknown prefix raises an error. .
   */
  @org.junit.Test
  public void k2NameTest42() {
    final XQuery query = new XQuery(
      "@unknownprefix:*",
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
   *  The prefix 'xmlns' is not in-scope. .
   */
  @org.junit.Test
  public void k2NameTest43() {
    final XQuery query = new XQuery(
      "@xmlns:*",
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
   *  The prefix 'xmlns' is not in-scope(#2). .
   */
  @org.junit.Test
  public void k2NameTest44() {
    final XQuery query = new XQuery(
      "@xmlns:ncname",
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
   *  The prefix 'xmlns' is not in-scope(#3). .
   */
  @org.junit.Test
  public void k2NameTest45() {
    final XQuery query = new XQuery(
      "xmlns:ncname",
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
   *  The prefix 'xmlns' is not in-scope(#4). .
   */
  @org.junit.Test
  public void k2NameTest46() {
    final XQuery query = new XQuery(
      "xmlns:*",
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
   *  Use 'xmlns' as an element name test. .
   */
  @org.junit.Test
  public void k2NameTest47() {
    final XQuery query = new XQuery(
      "declare variable $i := <e > <xmlns/> <xmlns/> <xmlns/> <xmlns/> </e>; $i/xmlns",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<xmlns/><xmlns/><xmlns/><xmlns/>", false)
    );
  }

  /**
   *  Use 'xmlns' as an attribute name test. .
   */
  @org.junit.Test
  public void k2NameTest48() {
    final XQuery query = new XQuery(
      "declare variable $i := <e xmlns=\"http://example.com/\"/>; empty($i/@xmlns)",
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
   *  An child axis applies on a sequence of attributes. .
   */
  @org.junit.Test
  public void k2NameTest49() {
    final XQuery query = new XQuery(
      "declare namespace p = \"http://example.com/\"; <a>{<e p:a=\"1\" p:b=\"2\" p:c=\"3\"/>/attribute::*/p:*}</a>",
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
        assertSerialization("<a></a>", false)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  A complex expression to parse, taken from W3C's (obsolete) 'Building a Tokenizer for XPath or XQuery' document. The query naturally contains XPTY0004. XPDY0002 is allowed since an implementation may change the default focus from being 'none' to being undefined. .
   */
  @org.junit.Test
  public void k2NameTest5() {
    final XQuery query = new XQuery(
      "declare namespace namespace = \"http://example.com\"; declare union <union>for gibberish { for $for in for return <for>***div div</for> }</union>, if(if) then then else else- +-++-**-* instance of element(*)* * * **---++div- div -div",
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
        error("XPTY0004")
      ||
        error("XPDY0002")
      )
    );
  }

  /**
   *  A '@prefix:*'-test doesn't match element nodes. .
   */
  @org.junit.Test
  public void k2NameTest50() {
    final XQuery query = new XQuery(
      "declare namespace p = \"http://example.com/\"; <a>{document {<p:e/>}/@p:*}</a>",
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
        assertSerialization("<a></a>", false)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Navigate the child axis of a computed attribute. .
   */
  @org.junit.Test
  public void k2NameTest51() {
    final XQuery query = new XQuery(
      "<a>{attribute name{\"content\"}/*}</a>",
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
        assertSerialization("<a></a>", false)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Navigate the child axis of a computed comment. .
   */
  @org.junit.Test
  public void k2NameTest52() {
    final XQuery query = new XQuery(
      "<a>{comment {\"content\"}/*}</a>",
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
        assertSerialization("<a/>", false)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Navigate the child axis of a direct comment. .
   */
  @org.junit.Test
  public void k2NameTest53() {
    final XQuery query = new XQuery(
      "<a>{<!-- a comment -->/*}</a>",
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
        assertSerialization("<a/>", false)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Navigate the child axis of a computed PI. .
   */
  @org.junit.Test
  public void k2NameTest54() {
    final XQuery query = new XQuery(
      "<a>{processing-instruction name {\"content\"}/*}</a>",
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
        assertSerialization("<a/>", false)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Navigate the child axis of a direct PI. .
   */
  @org.junit.Test
  public void k2NameTest55() {
    final XQuery query = new XQuery(
      "<a>{<?target data?>/*}</a>",
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
        assertSerialization("<a/>", false)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  A '*:name' as operand to 'eq'. .
   */
  @org.junit.Test
  public void k2NameTest56() {
    final XQuery query = new XQuery(
      "declare variable $a := <e><a/><b/><c/></e>; <a>{$a/*:ncname eq 1}</a>",
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
        assertSerialization("<a/>", false)
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  A '@*:name' as operand to 'eq'. .
   */
  @org.junit.Test
  public void k2NameTest57() {
    final XQuery query = new XQuery(
      "declare variable $a := <e><a/><b/><c/></e>; <a>{$a/*:ncname eq 1}</a>",
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
        assertSerialization("<a/>", false)
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  A 'prefix:*' as operand to 'eq'. .
   */
  @org.junit.Test
  public void k2NameTest58() {
    final XQuery query = new XQuery(
      "declare namespace prefix = \"http://example.com/\"; declare variable $a := <e><a/><b/><c/></e>; <a>{$a/prefix:* eq 1}</a>",
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
        assertSerialization("<a/>", false)
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  A '@*prefix:*' as operand to 'eq'. .
   */
  @org.junit.Test
  public void k2NameTest59() {
    final XQuery query = new XQuery(
      "declare namespace prefix = \"http://example.com/\"; declare variable $a := <e><a/><b/><c/></e>; <a>{$a/@prefix:* eq 1}</a>",
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
        assertSerialization("<a/>", false)
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  An axis must be specified when ".." is used. .
   */
  @org.junit.Test
  public void k2NameTest6() {
    final XQuery query = new XQuery(
      "::ncname",
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
   *  Combine the descendant-or-self axis with a processing-instruction test. .
   */
  @org.junit.Test
  public void k2NameTest60() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $input := <e/>; \n" +
      "        empty(for $PI as processing-instruction() in $input//processing-instruction() return $PI)",
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
        error("XPST0005")
      )
    );
  }

  /**
   *  Apply processing-instruction() to the empty sequence. .
   */
  @org.junit.Test
  public void k2NameTest61() {
    final XQuery query = new XQuery(
      "<e>{for $PI in ()/processing-instruction() return ()}</e>",
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
        assertSerialization("<e/>", false)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Apply processing-instruction() to the result of an element constructor. .
   */
  @org.junit.Test
  public void k2NameTest62() {
    final XQuery query = new XQuery(
      "<e>{for $PI in <e/>/processing-instruction() return ()}</e>",
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
        assertSerialization("<e/>", false)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Combine the descendant-or-self axis with the child axis. .
   */
  @org.junit.Test
  public void k2NameTest63() {
    final XQuery query = new XQuery(
      "<x> <x> <y id=\"0\"/> </x> <y id=\"1\"/> </x>/descendant-or-self::x/child::y",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<y id=\"0\"/><y id=\"1\"/>", false)
    );
  }

  /**
   *  Ensure the axis is correct when using an attribute(*, type) test in the abbreviated axis. .
   */
  @org.junit.Test
  public void k2NameTest64() {
    final XQuery query = new XQuery(
      "<e>{<e foo=\"asd\"/>/attribute(*, xs:untypedAtomic)}</e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e foo=\"asd\"/>", false)
    );
  }

  /**
   *  Ensure the axis is correct when using an attribute(name, type) test in the abbreviated axis. .
   */
  @org.junit.Test
  public void k2NameTest65() {
    final XQuery query = new XQuery(
      "<e>{<e foo=\"asd\"/>/attribute(foo, xs:untypedAtomic)}</e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e foo=\"asd\"/>", false)
    );
  }

  /**
   *  Use an unbound prefix inside attribute(). .
   */
  @org.junit.Test
  public void k2NameTest66() {
    final XQuery query = new XQuery(
      "<e>{<e foo=\"asd\"/>/attribute(notBound:foo, xs:untypedAtomic)}</e>",
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
   *  Use an unbound prefix inside attribute()(#2). .
   */
  @org.junit.Test
  public void k2NameTest67() {
    final XQuery query = new XQuery(
      "<e>{<e foo=\"asd\"/>/attribute(foo, notBound:untypedAtomic)}</e>",
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
   *  Use a type with attribute() that doesn't match. .
   */
  @org.junit.Test
  public void k2NameTest68() {
    final XQuery query = new XQuery(
      "<e>{<e foo=\"1\"/>/attribute(foo, xs:integer)}</e>",
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
        assertSerialization("<e/>", false)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Use a type with attribute() that doesn't exist. .
   */
  @org.junit.Test
  public void k2NameTest69() {
    final XQuery query = new XQuery(
      "<e>{<e foo=\"1\"/>/attribute(foo, doesNotExistExampleCom)}</e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  An axis must be specified when ".." is used(#2). .
   */
  @org.junit.Test
  public void k2NameTest7() {
    final XQuery query = new XQuery(
      "::local:ncname",
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
   *  Use a type with attribute() that doesn't exist(#2). .
   */
  @org.junit.Test
  public void k2NameTest70() {
    final XQuery query = new XQuery(
      "<e>{<e foo=\"1\"/>/attribute(foo, xs:doesNotExistExampleCom)}</e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  Use a complex type with attribute(). .
   */
  @org.junit.Test
  public void k2NameTest71() {
    final XQuery query = new XQuery(
      "<e>{<b foo=\"1\"/>/attribute(foo, xs:anyType)}</e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e foo=\"1\"/>", false)
    );
  }

  /**
   *  Use element() with an unbound prefix. .
   */
  @org.junit.Test
  public void k2NameTest72() {
    final XQuery query = new XQuery(
      "<e>{<e><b/></e>/element(p:foo)}</e>",
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
   *  Use element() with an unbound prefix(#2). .
   */
  @org.junit.Test
  public void k2NameTest73() {
    final XQuery query = new XQuery(
      "<e>{<e><b/></e>/element(foo, notBound:type)}</e>",
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
   *  Use element() with a type that doesn't exist. .
   */
  @org.junit.Test
  public void k2NameTest74() {
    final XQuery query = new XQuery(
      "<e>{<e><b/></e>/element(foo, xs:doesNotExist)}</e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  Use element() with a type that doesn't exist. .
   */
  @org.junit.Test
  public void k2NameTest75() {
    final XQuery query = new XQuery(
      "<e>{<e><b/></e>/element(foo, doesNotExist)}</e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  Use element() with xs:anyType. .
   */
  @org.junit.Test
  public void k2NameTest76() {
    final XQuery query = new XQuery(
      "<e><b/></e>/element(b, xs:anyType)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<b/>", false)
    );
  }

  /**
   *  Use element() with xs:anyType. .
   */
  @org.junit.Test
  public void k2NameTest77() {
    final XQuery query = new XQuery(
      "<e><b/></e>/element(b, xs:anyType)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<b/>", false)
    );
  }

  /**
   *  Use 'let' as a single name test. .
   */
  @org.junit.Test
  public void k2NameTest78() {
    final XQuery query = new XQuery(
      "let",
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
   *  Use 'for' as a single name test. .
   */
  @org.junit.Test
  public void k2NameTest79() {
    final XQuery query = new XQuery(
      "let",
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
   *  "@" by itself is syntactically invalid. .
   */
  @org.junit.Test
  public void k2NameTest8() {
    final XQuery query = new XQuery(
      "@",
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
   *  Use 'as' as a single name test. .
   */
  @org.junit.Test
  public void k2NameTest80() {
    final XQuery query = new XQuery(
      "as",
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
   *  Use 'of' as a single name test. .
   */
  @org.junit.Test
  public void k2NameTest81() {
    final XQuery query = new XQuery(
      "of",
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
   *  A name cannot end with a colon. .
   */
  @org.junit.Test
  public void k2NameTest82() {
    final XQuery query = new XQuery(
      "child:",
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
   *  A name test that is equal to a node type name. .
   */
  @org.junit.Test
  public void k2NameTest83() {
    final XQuery query = new XQuery(
      "child::element",
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
   *  Axis names are reserved function names. .
   */
  @org.junit.Test
  public void k2NameTest84() {
    final XQuery query = new XQuery(
      "declare default function namespace \"http://www.example.com/\"; declare namespace e = \"http://www.example.com/\"; declare function element() { 1 }; e:element()",
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
        assertEq("1")
      ||
        error("XPST0003")
      )
    );
  }

  /**
   *  A single '_' is a valid name test. .
   */
  @org.junit.Test
  public void k2NameTest85() {
    final XQuery query = new XQuery(
      "_",
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
   *  A '_' between letters is a valid name test. .
   */
  @org.junit.Test
  public void k2NameTest86() {
    final XQuery query = new XQuery(
      "pod_pod",
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
   *  Use a type with attribute() that doesn't exist(#3). .
   */
  @org.junit.Test
  public void k2NameTest87() {
    final XQuery query = new XQuery(
      "<e>{<e foo=\"1\"/>/attribute(*, doesNotExistExampleCom)}</e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  Use a type with attribute() that doesn't exist(#4). .
   */
  @org.junit.Test
  public void k2NameTest88() {
    final XQuery query = new XQuery(
      "<e>{<e foo=\"1\"/>/attribute(*, xs:doesNotExistExampleCom)}</e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  Use a type with element() that doesn't exist(#3). .
   */
  @org.junit.Test
  public void k2NameTest89() {
    final XQuery query = new XQuery(
      "<e>{<e foo=\"1\"/>/attribute(*, doesNotExistExampleCom)}</e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  "@" with space is syntactically invalid. .
   */
  @org.junit.Test
  public void k2NameTest9() {
    final XQuery query = new XQuery(
      "@",
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
   *  Use a type with element() that doesn't exist(#4). .
   */
  @org.junit.Test
  public void k2NameTest90() {
    final XQuery query = new XQuery(
      "<e>{<e foo=\"1\"/>/attribute(*, xs:doesNotExistExampleCom)}</e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  Simple test for * node test .
   */
  @org.junit.Test
  public void nodeTest003() {
    final XQuery query = new XQuery(
      "/*/*[1]/name()",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/nw_Customers.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Customers")
    );
  }

  /**
   *  Simple test for . node test .
   */
  @org.junit.Test
  public void nodeTest004() {
    final XQuery query = new XQuery(
      "/.",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/nw_Customers.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertType("document-node(element(Root))")
      &&
        assertQuery("exists($result//Country[.='Poland'])")
      )
    );
  }

  /**
   *  Simple test for . and * node test .
   */
  @org.junit.Test
  public void nodeTest005() {
    final XQuery query = new XQuery(
      "/*/.",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/nw_Customers.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertType("element(Root)")
      &&
        assertQuery("exists($result//Country[.='Poland'])")
      )
    );
  }

  /**
   *  Name test that evaluates a child "b" of a newly construted node. .
   */
  @org.junit.Test
  public void nametest1() {
    final XQuery query = new XQuery(
      "let $var := <a attr1 = \"abc1\"><b attr2 = \"abc2\">context2</b></a> return $var/child::*",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<b attr2=\"abc2\">context2</b>", false)
    );
  }

  /**
   *  Name test that selects an "child::b" of a newly created element node and whose namespace URI is declared as the default namespace. .
   */
  @org.junit.Test
  public void nametest10() {
    final XQuery query = new XQuery(
      "declare default element namespace \"http://www.example.org/examples\"; let $var := <a attr1 = \"abc1\"><b attr2 = \"abc2\">context2</b></a> return fn:namespace-uri(exactly-one($var/child::b))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.org/examples")
    );
  }

  /**
   *  Name test that selects an "child::b" of a newly created element node and whose namespace URI is in no namespace. .
   */
  @org.junit.Test
  public void nametest11() {
    final XQuery query = new XQuery(
      "let $var := <a attr1 = \"abc1\"><b attr2 = \"abc2\">context2</b></a> return fn:count(fn:namespace-uri(exactly-one($var/child::b)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Name test that selects an "child::*:day" of an element node used as part of an union operation. .
   */
  @org.junit.Test
  public void nametest12() {
    final XQuery query = new XQuery(
      "let $var := /works/employee[12]/overtime return $var/child::*:day[1] | $var/child::*:day[2]",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<day>Monday</day><day>Tuesday</day>", false)
    );
  }

  /**
   *  Name test that selects an "child::*" and "child::day" (same nodes) of an element node used as part of an intersect operation. .
   */
  @org.junit.Test
  public void nametest13() {
    final XQuery query = new XQuery(
      "let $var := /works[1]/child::employee[12]/overtime return $var/child::* intersect $var/child::day",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<day>Monday</day><day>Tuesday</day>", false)
    );
  }

  /**
   *  Name test that selects an "child::*" and "child::day" (same nodes) of an element node used as part of an except operation. Use fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void nametest14() {
    final XQuery query = new XQuery(
      "let $var := /works[1]/child::employee[12]/overtime return fn:count($var/child::* except $var/child::day)",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Name test that selects all the children of the context node with "child::*". .
   */
  @org.junit.Test
  public void nametest15() {
    final XQuery query = new XQuery(
      "let $var := /works return fn:count($var/child::*)",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("13")
    );
  }

  /**
   *  Name test that selects all the children of the context node with "child::employee". .
   */
  @org.junit.Test
  public void nametest16() {
    final XQuery query = new XQuery(
      "let $var := /works return fn:count($var/child::employee)",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("13")
    );
  }

  /**
   *  Name test that selects all the children of the context node with "child::*:employee". .
   */
  @org.junit.Test
  public void nametest17() {
    final XQuery query = new XQuery(
      "let $var := /works return fn:count($var/child::*:employee)",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("13")
    );
  }

  /**
   *  Evaluation of a name test, which selects a child that was created using a declared namespace (qualified name). Use "child::*:b" syntax. .
   */
  @org.junit.Test
  public void nametest18() {
    final XQuery query = new XQuery(
      "declare namespace ns1 = \"http://example.org\"; let $var := <a attr1 = \"abc1\"><ns1:b attr2 = \"abc2\">context2</ns1:b></a> return $var/child::*:b",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<ns1:b xmlns:ns1=\"http://example.org\" attr2=\"abc2\">context2</ns1:b>", false)
    );
  }

  /**
   *  Name test that test for "child::b" of a newly construted node. .
   */
  @org.junit.Test
  public void nametest2() {
    final XQuery query = new XQuery(
      "let $var := <a attr1 = \"abc1\"><b attr2 = \"abc2\">context2</b></a> return $var/child::b",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<b attr2=\"abc2\">context2</b>", false)
    );
  }

  /**
   *  Name test that uses an unknown prefix. .
   */
  @org.junit.Test
  public void nametest3() {
    final XQuery query = new XQuery(
      "let $var := <a attr1 = \"abc1\"><b attr2 = \"abc2\">context2</b></a> return $var/child::pr:b",
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
   *  Name test that uses an unknown prefix. Uses "*". .
   */
  @org.junit.Test
  public void nametest4() {
    final XQuery query = new XQuery(
      "let $var := <a attr1 = \"abc1\"><b attr2 = \"abc2\">context2</b></a> return $var/child::pr:*",
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
   *  Name test that examines "*:b" for a newly constructed element. .
   */
  @org.junit.Test
  public void nametest5() {
    final XQuery query = new XQuery(
      "let $var := <a attr1 = \"abc1\"><b attr2 = \"abc2\">context2</b></a> return $var/child::*:b",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<b attr2=\"abc2\">context2</b>", false)
    );
  }

  /**
   *  Name test that examines "ns1:b" for a newly constructed element that uses a declared namespace. .
   */
  @org.junit.Test
  public void nametest6() {
    final XQuery query = new XQuery(
      "declare namespace ns1 = \"http://example.org\"; let $var := <a attr1 = \"abc1\"><ns1:b attr2 = \"abc2\">context2</ns1:b></a> return $var/child::ns1:b",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<ns1:b xmlns:ns1=\"http://example.org\" attr2=\"abc2\">context2</ns1:b>", false)
    );
  }

  /**
   *  Name test that examines "b" for a newly constructed element. .
   */
  @org.junit.Test
  public void nametest7() {
    final XQuery query = new XQuery(
      "let $var := <a attr1 = \"abc1\"><b attr2 = \"abc2\">context2</b></a> return $var/b",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<b attr2=\"abc2\">context2</b>", false)
    );
  }

  /**
   *  Name test that examines "b" for a newly constructed element and used as argument to "node-name". .
   */
  @org.junit.Test
  public void nametest8() {
    final XQuery query = new XQuery(
      "let $var := <a attr1 = \"abc1\"><b attr2 = \"abc2\">context2</b></a> return fn:node-name(exactly-one($var/b))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "b")
    );
  }

  /**
   *  Name test that attempts to select non-existent nodes from a newly constructed. Use fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void nametest9() {
    final XQuery query = new XQuery(
      "let $var := <a attr1 = \"abc1\"><b attr2 = \"abc2\">context2</b></a> return fn:count(($var/empty-node-list)[1])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }
}
