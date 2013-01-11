package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the doc() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnDoc extends QT3TestSet {

  /**
   *  Invoke fn:doc() on the empty sequence. .
   */
  @org.junit.Test
  public void k2SeqDocFunc1() {
    final XQuery query = new XQuery(
      "empty(fn:doc(()))",
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
   *  Invoke fn:doc() with a static base-uri that is complete, but doesn't point to a file. .
   */
  @org.junit.Test
  public void k2SeqDocFunc10() {
    final XQuery query = new XQuery(
      "doc(\"\")",
      ctx);
    try {
      query.baseURI("file:///directory/directory/example.com/www.example.com/thisFileDoesNotExist.xml");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODC0002")
    );
  }

  /**
   *  Invoke fn:doc() with a static base-uri that points to a non-existent directory. .
   */
  @org.junit.Test
  public void k2SeqDocFunc11() {
    final XQuery query = new XQuery(
      "doc(\"\")",
      ctx);
    try {
      query.baseURI("file:///directory/directory/example.com/");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODC0002")
    );
  }

  /**
   *  General query complexity, combined with a call to fn:doc() that is never evaluated. .
   */
  @org.junit.Test
  public void k2SeqDocFunc12() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $fileToOpen := <Variable id=\"_7\" name=\"constComplex2\" type=\"_11c\" context=\"_1\" location=\"f0:17\" file=\"f0\" line=\"17\"/>; \n" +
      "        empty($fileToOpen//*[let $i := @type return doc($fileToOpen)//*[$i]])\n" +
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
      (
        assertBoolean(true)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Never use the result of calls to fn:doc. Since implementations are allowed to dereference the sources in any case, an error is allowed. .
   */
  @org.junit.Test
  public void k2SeqDocFunc13() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $doc1 := doc(\"http://example.com\"); \n" +
      "        let $doc2 := doc(\"ftp://example.com/\") \n" +
      "        for $i in doc(\"localFile.xml\") \n" +
      "        return (1, 2, 3)",
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
        assertStringValue(false, "1 2 3")
      ||
        error("FODC0002")
      )
    );
  }

  /**
   *  ':/' is an invalid URI, no scheme. .
   */
  @org.junit.Test
  public void k2SeqDocFunc14() {
    final XQuery query = new XQuery(
      "doc(':/')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODC0005")
    );
  }

  /**
   *  Invoke fn:doc() on the empty sequence(#2). .
   */
  @org.junit.Test
  public void k2SeqDocFunc2() {
    final XQuery query = new XQuery(
      "<e>{fn:doc(())}</e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e/>", false)
    );
  }

  /**
   *  Load an unexisting file via the file:// protocol. .
   */
  @org.junit.Test
  public void k2SeqDocFunc3() {
    final XQuery query = new XQuery(
      "fn:doc(xs:untypedAtomic(\"file:///example.com/does/not/exist/xqts-testing.xml\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODC0002")
    );
  }

  /**
   *  Invoke fn:doc with a relative, Unix-like URI. Since it's relative, and the base-uri may be undefined, XPST0001 is allowed. .
   */
  @org.junit.Test
  public void k2SeqDocFunc4() {
    final XQuery query = new XQuery(
      "doc(\"/example.com/example.org/does/not/exist/doesNotExist/works-mod.xml\")",
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
        error("FODC0002")
      ||
        error("XPST0001")
      )
    );
  }

  /**
   *  Invoke fn:doc with a relative, Unix-like URI and a declared base-uri. .
   */
  @org.junit.Test
  public void k2SeqDocFunc5() {
    final XQuery query = new XQuery(
      "doc(\"/example.com/example.org/does/not/exist/doesNotExist/works-mod.xml\")",
      ctx);
    try {
      query.baseURI("http://www.example.invalid");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODC0002")
    );
  }

  /**
   *  Invoke fn:doc with a relative, Unix-like URI and a declared base-uri. .
   */
  @org.junit.Test
  public void k2SeqDocFunc6() {
    final XQuery query = new XQuery(
      "doc(\"/example.com/example.org/does/not/exist/doesNotExist/works-mod.xml\")",
      ctx);
    try {
      query.baseURI("file:///");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODC0002")
    );
  }

  /**
   *  A windows file path is an invalid URI to fn:doc(). .
   */
  @org.junit.Test
  public void k2SeqDocFunc7() {
    final XQuery query = new XQuery(
      "doc(\"example.com\\example.org\\does\\not\\exist\\doesNotExist\\works-mod.xml\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODC0002")
    );
  }

  /**
   *  A windows file path is an invalid URI to fn:doc()(#2). .
   */
  @org.junit.Test
  public void k2SeqDocFunc8() {
    final XQuery query = new XQuery(
      "doc(\"\\example.com\\example.org\\does\\not\\exist\\doesNotExist\\works-mod.xml\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODC0002")
    );
  }

  /**
   *  A windows file path is an invalid URI to fn:doc()(#3). .
   */
  @org.junit.Test
  public void k2SeqDocFunc9() {
    final XQuery query = new XQuery(
      "doc(\"C:\\example.com\\example.org\\does\\not\\exist\\doesNotExist\\works-mod.xml\")",
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
        error("FODC0002")
      ||
        error("FODC0005")
      )
    );
  }

  /**
   *  Evaluation of fn:doc function with an invalid argument. .
   */
  @org.junit.Test
  public void fnDoc1() {
    final XQuery query = new XQuery(
      "fn:doc(\"http:\\\\invalid>URI\\someURI\")",
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
        error("FODC0005")
      ||
        error("FODC0002")
      )
    );
  }

  /**
   *  Evaluation of fn:doc as an argument to the fn:nilled function. .
   */
  @org.junit.Test
  public void fnDoc15() {
    final XQuery query = new XQuery(
      "fn:count(fn:nilled(fn:doc($uri)))",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      query.bind("uri", new XQuery("'http://www.w3.org/fots/docs/works-mod.xml'", ctx).value());
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
   *  Evaluation of fn:doc as an argument to the fn:node-name function. Use fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void fnDoc16() {
    final XQuery query = new XQuery(
      "fn:count(fn:node-name(fn:doc($uri)))",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      query.bind("uri", new XQuery("'http://www.w3.org/fots/docs/works-mod.xml'", ctx).value());
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
   *  Evaluation of fn:doc with argument set to an invalid URI. .
   */
  @org.junit.Test
  public void fnDoc17() {
    final XQuery query = new XQuery(
      "fn:doc(\"%gg\")",
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
        error("FODC0002")
      ||
        error("FODC0005")
      )
    );
  }

  /**
   *  Evaluation of fn:doc used with "is" operator and the fn:not function. .
   */
  @org.junit.Test
  public void fnDoc18() {
    final XQuery query = new XQuery(
      "fn:not(fn:doc($uri) is fn:doc($uri))",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      query.bind("uri", new XQuery("'http://www.w3.org/fots/docs/works-mod.xml'", ctx).value());
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
   *  Evaluation of fn:doc used with "is" operator and the fn:true function with "and" operator. .
   */
  @org.junit.Test
  public void fnDoc19() {
    final XQuery query = new XQuery(
      "(fn:doc($uri) is fn:doc($uri)) and fn:true()",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      query.bind("uri", new XQuery("'http://www.w3.org/fots/docs/works-mod.xml'", ctx).value());
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
   *  Evaluation of fn:doc function with wrong arity. .
   */
  @org.junit.Test
  public void fnDoc2() {
    final XQuery query = new XQuery(
      "fn:doc(\"argument1\",\"argument2\")",
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
   *  Evaluation of fn:doc used with "is" operator and the fn:false function with "and" operator. .
   */
  @org.junit.Test
  public void fnDoc20() {
    final XQuery query = new XQuery(
      "(fn:doc($uri) is fn:doc($uri)) and fn:false()",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      query.bind("uri", new XQuery("'http://www.w3.org/fots/docs/works-mod.xml'", ctx).value());
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
   *  Evaluation of fn:doc used with "is" operator and the fn:true function with "or" operator. .
   */
  @org.junit.Test
  public void fnDoc21() {
    final XQuery query = new XQuery(
      "(fn:doc($uri) is fn:doc($uri)) or fn:true()",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      query.bind("uri", new XQuery("'http://www.w3.org/fots/docs/works-mod.xml'", ctx).value());
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
   *  Evaluation of fn:doc used with "is" operator and the fn:false function with "or" operator. .
   */
  @org.junit.Test
  public void fnDoc22() {
    final XQuery query = new XQuery(
      "(fn:doc($uri) is fn:doc($uri)) or fn:false()",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      query.bind("uri", new XQuery("'http://www.w3.org/fots/docs/works-mod.xml'", ctx).value());
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
   *  Open a document that has a text node containing ' ]'. Use a relative URI..
   */
  @org.junit.Test
  public void fnDoc24() {
    final XQuery query = new XQuery(
      "fn:doc($uri)",
      ctx);
    try {
      query.addDocument("id/SpaceBracket.xml", file("fn/id/SpaceBracket.xml"));
      query.bind("uri", new XQuery("'id/SpaceBracket.xml'", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<p> ]</p>", false)
    );
  }

  /**
   *  Open a document that has an element node with an unusual name. .
   */
  @org.junit.Test
  public void fnDoc25() {
    final XQuery query = new XQuery(
      "fn:doc($uri)",
      ctx);
    try {
      query.addDocument("id/HighUnicode.xml", file("fn/id/HighUnicode.xml"));
      query.bind("uri", new XQuery("'id/HighUnicode.xml'", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<เจมส์></เจมส์>", false)
    );
  }

  /**
   *  Open a document that has a text node containing ' ]'. .
   */
  @org.junit.Test
  public void fnDoc26() {
    final XQuery query = new XQuery(
      "fn:doc($uri)",
      ctx);
    try {
      query.addDocument("id/HighUnicode2.xml", file("fn/id/HighUnicode2.xml"));
      query.bind("uri", new XQuery("'id/HighUnicode2.xml'", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<เจมส์/>", false)
    );
  }

  /**
   *  Use a series of corner case Unicode codepoints. .
   */
  @org.junit.Test
  public void fnDoc27() {
    final XQuery query = new XQuery(
      "fn:doc($uri)",
      ctx);
    try {
      query.addDocument("id/BCisInvalid.xml", file("fn/id/BCisInvalid.xml"));
      query.bind("uri", new XQuery("'id/BCisInvalid.xml'", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODC0002")
    );
  }

  /**
   *  Open a document which contains the codepoint 188 in an NCName. .
   */
  @org.junit.Test
  public void fnDoc28() {
    final XQuery query = new XQuery(
      "fn:doc($uri)",
      ctx);
    try {
      query.addDocument("id/InvalidUmlaut.xml", file("fn/id/InvalidUmlaut.xml"));
      query.bind("uri", new XQuery("'id/InvalidUmlaut.xml'", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODC0002")
    );
  }

  /**
   *  Use a namespace declaration which is available in the subset. .
   */
  @org.junit.Test
  public void fnDoc29() {
    final XQuery query = new XQuery(
      "fn:doc($uri)",
      ctx);
    try {
      query.addDocument("id/NamespaceSuppliedInternally.xml", file("fn/id/NamespaceSuppliedInternally.xml"));
      query.bind("uri", new XQuery("'id/NamespaceSuppliedInternally.xml'", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"/>", false)
    );
  }

  /**
   *  Evaluation of fn:doc, which tries to retrieve a non-existent resourse. .
   */
  @org.junit.Test
  public void fnDoc3() {
    final XQuery query = new XQuery(
      "fn:doc(\"thisfileshouldnotexists.xml\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODC0002")
    );
  }

  /**
   *  Open an XML document that has an xml:id attribute duplicated. .
   */
  @org.junit.Test
  public void fnDoc30() {
    final XQuery query = new XQuery(
      "local-name(fn:doc($uri)/*)",
      ctx);
    try {
      query.addDocument("id/XMLIdDuplicated.xml", file("fn/id/XMLIdDuplicated.xml"));
      query.bind("uri", new XQuery("'id/XMLIdDuplicated.xml'", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "schema")
      ||
        error("FODC0002")
      )
    );
  }

  /**
   *  Open an XML document that has an invald xml:id attribute. .
   */
  @org.junit.Test
  public void fnDoc31() {
    final XQuery query = new XQuery(
      "local-name(fn:doc($uri)/*)",
      ctx);
    try {
      query.addDocument("id/InvalidXMLId.xml", file("fn/id/InvalidXMLId.xml"));
      query.bind("uri", new XQuery("'id/InvalidXMLId.xml'", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "schema")
      ||
        error("FODC0002")
      )
    );
  }

  /**
   *  Open an XML document that has ISO-8859-1 as encoding. .
   */
  @org.junit.Test
  public void fnDoc32() {
    final XQuery query = new XQuery(
      "string(fn:doc($uri)), string(exactly-one(fn:doc($uri)/*))",
      ctx);
    try {
      query.addDocument("id/Books2.xml", file("fn/id/Books2.xml"));
      query.bind("uri", new XQuery("'id/Books2.xml'", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "\n  Everyday Italian\n  Giada De Laurentiis\n \n  Everyday Italian\n  Giada De Laurentiis\n")
    );
  }

  /**
   *  A moderately complex query. , local:recurseMetaObject($metaObjects[@superClass = $object/@className], $count + 1) Output an xml:id attribute on each group element, if we have a name. .
   */
  @org.junit.Test
  public void fnDoc33() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace s = \"http://www.w3.org/2000/svg\"; \n" +
      "        declare variable $uri external; \n" +
      "        declare variable $root := doc($uri)/root/QObject; \n" +
      "        declare variable $metaObjects := $root/preceding-sibling::metaObjects/metaObject; \n" +
      "        declare function local:recurseMetaObject($object as element(metaObject), $count as xs:integer) { \n" +
      "            <s:text x =\"{10 * $count}\" y=\"10\" font-family=\"Verdana\" font-size=\"55\" fill=\"blue\" >{\n" +
      "                string($object/@className)}</s:text> \n" +
      "        }; \n" +
      "        declare function local:drawDiagram($object as element(QObject)) as element(s:g) { \n" +
      "            <s:g> { if(string($object/@objectName)) \n" +
      "                    then attribute xml:id {$object/@objectName} \n" +
      "                    else (), \n" +
      "                    $metaObjects[@className = $object/@className]/local:recurseMetaObject(., 1) } </s:g> \n" +
      "        }; \n" +
      "        <s:svg> { \n" +
      "            <s:rect x=\"1\" y=\"1\" width=\"500\" height=\"300\" fill=\"none\" stroke=\"blue\" stroke-width=\"2\"/>, \n" +
      "            <s:rect x=\"400\" y=\"100\" width=\"400\" height=\"200\" fill=\"yellow\" stroke=\"navy\" stroke-width=\"10\" />, \n" +
      "            for $object in $root//QObject for $i in local:drawDiagram($object) stable order by string($i) return $i \n" +
      "        } </s:svg>\n" +
      "     ",
      ctx);
    try {
      query.addDocument("id/QObject.xml", file("fn/id/QObject.xml"));
      query.bind("uri", new XQuery("'id/QObject.xml'", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<s:svg xmlns:s=\"http://www.w3.org/2000/svg\"><s:rect stroke-width=\"2\" width=\"500\" fill=\"none\" height=\"300\" stroke=\"blue\" y=\"1\" x=\"1\"/><s:rect stroke-width=\"10\" width=\"400\" fill=\"yellow\" height=\"200\" stroke=\"navy\" y=\"100\" x=\"400\"/><s:g xml:id=\"_layout\"/><s:g/><s:g xml:id=\"qt_tabwidget_stackedwidget\"/><s:g xml:id=\"verticalLayout_2\"/><s:g xml:id=\"htmlQueryEdit\"/><s:g/><s:g/><s:g/><s:g/><s:g/><s:g/><s:g/><s:g xml:id=\"htmlOutput\"/><s:g/><s:g/><s:g/><s:g/><s:g xml:id=\"verticalLayout\"/><s:g xml:id=\"wholeTree\"/><s:g/><s:g/><s:g/><s:g/><s:g/><s:g/><s:g/><s:g xml:id=\"wholeTreeOutput\"/><s:g/><s:g/><s:g/><s:g/><s:g/><s:g/><s:g/><s:g xml:id=\"verticalLayout_3\"/><s:g xml:id=\"diagramQuery\"/><s:g/><s:g/><s:g/><s:g/><s:g/><s:g/><s:g/><s:g xml:id=\"qt_tabwidget_tabbar\"/><s:g xml:id=\"menubar\"/><s:g xml:id=\"statusbar\"/><s:g/><s:g><s:text font-family=\"Verdana\" fill=\"blue\" font-size=\"55\" y=\"10\" x=\"10\">QBoxLayout</s:text></s:g><s:g><s:text font-family=\"Verdana\" fill=\"blue\" font-size=\"55\" y=\"10\" x=\"10\">QBoxLayout</s:text></s:g><s:g><s:text font-family=\"Verdana\" fill=\"blue\" font-size=\"55\" y=\"10\" x=\"10\">QBoxLayout</s:text></s:g><s:g><s:text font-family=\"Verdana\" fill=\"blue\" font-size=\"55\" y=\"10\" x=\"10\">QBoxLayout</s:text></s:g><s:g><s:text font-family=\"Verdana\" fill=\"blue\" font-size=\"55\" y=\"10\" x=\"10\">QBoxLayout</s:text></s:g><s:g><s:text font-family=\"Verdana\" fill=\"blue\" font-size=\"55\" y=\"10\" x=\"10\">QBoxLayout</s:text></s:g><s:g><s:text font-family=\"Verdana\" fill=\"blue\" font-size=\"55\" y=\"10\" x=\"10\">QBoxLayout</s:text></s:g><s:g><s:text font-family=\"Verdana\" fill=\"blue\" font-size=\"55\" y=\"10\" x=\"10\">QBoxLayout</s:text></s:g><s:g xml:id=\"horizontalLayout\"><s:text font-family=\"Verdana\" fill=\"blue\" font-size=\"55\" y=\"10\" x=\"10\">QHBoxLayout</s:text></s:g><s:g><s:text font-family=\"Verdana\" fill=\"blue\" font-size=\"55\" y=\"10\" x=\"10\">QHBoxLayout</s:text></s:g><s:g><s:text font-family=\"Verdana\" fill=\"blue\" font-size=\"55\" y=\"10\" x=\"10\">QHBoxLayout</s:text></s:g><s:g><s:text font-family=\"Verdana\" fill=\"blue\" font-size=\"55\" y=\"10\" x=\"10\">QSizeGrip</s:text></s:g><s:g xml:id=\"inheritanceTab\"><s:text font-family=\"Verdana\" fill=\"blue\" font-size=\"55\" y=\"10\" x=\"10\">QTabWidget</s:text></s:g><s:g><s:text font-family=\"Verdana\" fill=\"blue\" font-size=\"55\" y=\"10\" x=\"10\">QTimer</s:text></s:g><s:g><s:text font-family=\"Verdana\" fill=\"blue\" font-size=\"55\" y=\"10\" x=\"10\">QTimer</s:text></s:g><s:g><s:text font-family=\"Verdana\" fill=\"blue\" font-size=\"55\" y=\"10\" x=\"10\">QToolButton</s:text></s:g><s:g><s:text font-family=\"Verdana\" fill=\"blue\" font-size=\"55\" y=\"10\" x=\"10\">QToolButton</s:text></s:g><s:g xml:id=\"qt_menubar_ext_button\"><s:text font-family=\"Verdana\" fill=\"blue\" font-size=\"55\" y=\"10\" x=\"10\">QToolButton</s:text></s:g><s:g xml:id=\"centralwidget\"><s:text font-family=\"Verdana\" fill=\"blue\" font-size=\"55\" y=\"10\" x=\"10\">QWidget</s:text></s:g><s:g xml:id=\"htmlTab\"><s:text font-family=\"Verdana\" fill=\"blue\" font-size=\"55\" y=\"10\" x=\"10\">QWidget</s:text></s:g><s:g xml:id=\"qt_scrollarea_hcontainer\"><s:text font-family=\"Verdana\" fill=\"blue\" font-size=\"55\" y=\"10\" x=\"10\">QWidget</s:text></s:g><s:g xml:id=\"qt_scrollarea_vcontainer\"><s:text font-family=\"Verdana\" fill=\"blue\" font-size=\"55\" y=\"10\" x=\"10\">QWidget</s:text></s:g><s:g xml:id=\"qt_scrollarea_viewport\"><s:text font-family=\"Verdana\" fill=\"blue\" font-size=\"55\" y=\"10\" x=\"10\">QWidget</s:text></s:g><s:g xml:id=\"wholeTreeTab\"><s:text font-family=\"Verdana\" fill=\"blue\" font-size=\"55\" y=\"10\" x=\"10\">QWidget</s:text></s:g><s:g xml:id=\"qt_scrollarea_hcontainer\"><s:text font-family=\"Verdana\" fill=\"blue\" font-size=\"55\" y=\"10\" x=\"10\">QWidget</s:text></s:g><s:g xml:id=\"qt_scrollarea_vcontainer\"><s:text font-family=\"Verdana\" fill=\"blue\" font-size=\"55\" y=\"10\" x=\"10\">QWidget</s:text></s:g><s:g xml:id=\"qt_scrollarea_viewport\"><s:text font-family=\"Verdana\" fill=\"blue\" font-size=\"55\" y=\"10\" x=\"10\">QWidget</s:text></s:g><s:g xml:id=\"qt_scrollarea_hcontainer\"><s:text font-family=\"Verdana\" fill=\"blue\" font-size=\"55\" y=\"10\" x=\"10\">QWidget</s:text></s:g><s:g xml:id=\"qt_scrollarea_vcontainer\"><s:text font-family=\"Verdana\" fill=\"blue\" font-size=\"55\" y=\"10\" x=\"10\">QWidget</s:text></s:g><s:g xml:id=\"qt_scrollarea_viewport\"><s:text font-family=\"Verdana\" fill=\"blue\" font-size=\"55\" y=\"10\" x=\"10\">QWidget</s:text></s:g><s:g xml:id=\"svgTab\"><s:text font-family=\"Verdana\" fill=\"blue\" font-size=\"55\" y=\"10\" x=\"10\">QWidget</s:text></s:g><s:g xml:id=\"qt_scrollarea_hcontainer\"><s:text font-family=\"Verdana\" fill=\"blue\" font-size=\"55\" y=\"10\" x=\"10\">QWidget</s:text></s:g><s:g xml:id=\"qt_scrollarea_vcontainer\"><s:text font-family=\"Verdana\" fill=\"blue\" font-size=\"55\" y=\"10\" x=\"10\">QWidget</s:text></s:g><s:g xml:id=\"qt_scrollarea_viewport\"><s:text font-family=\"Verdana\" fill=\"blue\" font-size=\"55\" y=\"10\" x=\"10\">QWidget</s:text></s:g><s:g><s:text font-family=\"Verdana\" fill=\"blue\" font-size=\"55\" y=\"10\" x=\"10\">QWidgetAnimator</s:text></s:g></s:svg>", false)
    );
  }

  /**
   *  A moderately complex query. .
   */
  @org.junit.Test
  public void fnDoc34() {
    final XQuery query = new XQuery(
      "doc($uri)",
      ctx);
    try {
      query.addDocument("id/0x010D.xml", file("fn/id/0x010D.xml"));
      query.bind("uri", new XQuery("'id/0x010D.xml'", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<project čreated-by=\"{{build_number}}\"/>", false)
    );
  }

  /**
   *  Load a not-wellformed XML file. .
   */
  @org.junit.Test
  public void fnDoc35() {
    final XQuery query = new XQuery(
      "doc('id/badxml.xml')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODC0002")
    );
  }

  /**
   *  Count text nodes in a document containing character references. .
   */
  @org.junit.Test
  public void fnDoc36() {
    final XQuery query = new XQuery(
      "count(doc($uri)//text())",
      ctx);
    try {
      query.addDocument("id/builtinEntities.xml", file("fn/id/builtinEntities.xml"));
      query.bind("uri", new XQuery("'id/builtinEntities.xml'", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("9")
    );
  }

  /**
   *  Use an xml:id attribute that has whitespace, whitespace normalization is performed. .
   */
  @org.junit.Test
  public void fnDoc37() {
    final XQuery query = new XQuery(
      "let $result := id(\"idABC\", doc($uri)) return (empty($result), $result)",
      ctx);
    try {
      query.addDocument("id/XMLIdWhitespace.xml", file("fn/id/XMLIdWhitespace.xml"));
      query.bind("uri", new XQuery("'id/XMLIdWhitespace.xml'", ctx).value());
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
        assertSerialization("false<xs:attribute xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:atomic=\"http://www.w3.org/XQueryTest\" name=\"attr\" type=\"xs:IDREFS\" use=\"required\" xml:id=\"idABC\"/>", false)
      )
    );
  }

  /**
   *  Evaluation of fn:doc with argument set to empty sequence. .
   */
  @org.junit.Test
  public void fnDoc4() {
    final XQuery query = new XQuery(
      "fn:doc(())",
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
   *  Evaluation of fn:doc as per example 1 from the functions and Ops. for this function. .
   */
  @org.junit.Test
  public void fnDoc5() {
    final XQuery query = new XQuery(
      "fn:doc($uri) is fn:doc($uri)",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      query.bind("uri", new XQuery("'http://www.w3.org/fots/docs/works-mod.xml'", ctx).value());
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
   *  Evaluation of fn:doc used with different resourses and the "is" operator. .
   */
  @org.junit.Test
  public void fnDoc6() {
    final XQuery query = new XQuery(
      "fn:doc($works) is fn:doc($staff)",
      ctx);
    try {
      query.addDocument("http://www.w3.org/fots/docs/works.xml", file("docs/works.xml"));
      query.addDocument("http://www.w3.org/fots/docs/staff.xml", file("docs/staff.xml"));
      query.bind("works", new XQuery("'http://www.w3.org/fots/docs/works.xml'", ctx).value());
      query.bind("staff", new XQuery("'http://www.w3.org/fots/docs/staff.xml'", ctx).value());
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
   *  Typical usage of fn:doc. Retrieve a part of the resources. .
   */
  @org.junit.Test
  public void fnDoc7() {
    final XQuery query = new XQuery(
      "fn:doc($uri)//day/string()",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      query.bind("uri", new XQuery("'http://www.w3.org/fots/docs/works-mod.xml'", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"Monday\", \"Tuesday\"")
    );
  }
}
