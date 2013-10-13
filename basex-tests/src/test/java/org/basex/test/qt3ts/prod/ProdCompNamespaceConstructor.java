package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the computed namespace constructor expression added in XQuery 3.0.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdCompNamespaceConstructor extends QT3TestSet {

  /**
   * nscons-001 - dynamic namespace constructor - variable content .
   */
  @org.junit.Test
  public void nscons001() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $s := \"http://saxon.sf.net/\"; \n" +
      "        declare variable $xsl := \"http://www.w3.org/1999/XSL/Transform\"; \n" +
      "        <e>{ namespace saxon {$s}, attribute a {23}, namespace xsl {$xsl} }</e>\n" +
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
      assertSerialization("<e xmlns:saxon=\"http://saxon.sf.net/\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" a=\"23\"/>", false)
    );
  }

  /**
   * nscons-002 - dynamic namespace constructor - variable name .
   */
  @org.junit.Test
  public void nscons002() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $s := \"saxon\"; \n" +
      "        declare variable $xsl := \"xsl\"; \n" +
      "        <e>{ namespace {$s} {\"http://saxon.sf.net/\"}, \n" +
      "             attribute a {23}, \n" +
      "             namespace {$xsl} {\"http://www.w3.org/1999/XSL/Transform\"} }</e>\n" +
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
      assertSerialization("<e xmlns:saxon=\"http://saxon.sf.net/\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" a=\"23\"/>", false)
    );
  }

  /**
   * nscons-003 - dynamic namespace constructor - default namespace .
   */
  @org.junit.Test
  public void nscons003() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $s := \"saxon\"; \n" +
      "        declare variable $xsl := \"xsl\"; \n" +
      "        <out> <t:e xmlns:t=\"http://www.example.com/\">{ \n" +
      "            namespace {\"\"} {\"http://saxon.sf.net/\"}, \n" +
      "            attribute a {23}, \n" +
      "            namespace {$xsl} {\"http://www.w3.org/1999/XSL/Transform\"}, <f/> }</t:e> </out>\n" +
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
      assertSerialization("<out><t:e xmlns:t=\"http://www.example.com/\" xmlns=\"http://saxon.sf.net/\"\n        xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" a=\"23\"><f xmlns=\"\"/></t:e></out>", false)
    );
  }

  /**
   * nscons-004 - dynamic namespace constructor - xml namespace .
   */
  @org.junit.Test
  public void nscons004() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $s := \"saxon\"; \n" +
      "        declare variable $xml := \"http://www.w3.org/XML/1998/namespace\"; \n" +
      "        <out> <t:e xmlns:t=\"http://www.example.com/\" xml:space=\"preserve\">{ \n" +
      "            namespace xml {\"http://www.w3.org/XML/1998/namespace\"}, \n" +
      "            attribute a {23}, <f/> }</t:e> </out>\n" +
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
      assertSerialization("<out><t:e xmlns:t=\"http://www.example.com/\" xml:space=\"preserve\" a=\"23\"><f/></t:e></out>", false)
    );
  }

  /**
   * nscons-005 - dynamic namespace constructor - with dynamic element constructor .
   */
  @org.junit.Test
  public void nscons005() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $s := \"http://saxon.sf.net/\"; \n" +
      "        declare variable $xsl := \"http://www.w3.org/1999/XSL/Transform\"; \n" +
      "        element {QName(\"http://saxon.sf.net/\", \"saxon:extension\")} { namespace saxon {$s}, attribute a {23}, namespace xsl {$xsl}, element f {42} }\n" +
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
      assertSerialization("<saxon:extension xmlns:saxon=\"http://saxon.sf.net/\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" \n            a=\"23\"><f>42</f></saxon:extension>", false)
    );
  }

  /**
   * nscons-006 - dynamic namespace constructor - duplicates are OK .
   */
  @org.junit.Test
  public void nscons006() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $s := \"http://saxon.sf.net/\"; \n" +
      "        declare variable $xsl := \"http://www.w3.org/1999/XSL/Transform\"; \n" +
      "        element {QName(\"http://saxon.sf.net/\", \"saxon:extension\")} \n" +
      "                { namespace saxon {$s}, attribute a {23}, namespace xsl {$xsl}, namespace saxon {$s}, element f {42} }\n" +
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
      assertSerialization("<saxon:extension xmlns:saxon=\"http://saxon.sf.net/\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"\n                 a=\"23\"><f>42</f></saxon:extension>", false)
    );
  }

  /**
   * nscons-007 - dynamic namespace constructor - error, misuse of xmlns .
   */
  @org.junit.Test
  public void nscons007() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $s := \"http://saxon.sf.net/\"; \n" +
      "        declare variable $xsl := \"http://www.w3.org/1999/XSL/Transform\"; \n" +
      "        declare variable $xmlns := \"xmlns\"; \n" +
      "        <e> { namespace saxon {$s}, attribute a {23}, namespace xsl {$xsl}, namespace xmlns {$s}, element f {42} }</e>\n" +
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
      error("XQDY0101")
    );
  }

  /**
   * nscons-008 - dynamic namespace constructor - error, misuse of xml .
   */
  @org.junit.Test
  public void nscons008() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $s := \"http://saxon.sf.net/\"; \n" +
      "        declare variable $xsl := \"http://www.w3.org/1999/XSL/Transform\"; \n" +
      "        declare variable $xmlns := \"xml\"; \n" +
      "        <e> { namespace saxon {$s}, attribute a {23}, namespace xsl {$xsl}, namespace {$xmlns} {$s}, element f {42} }</e>\n" +
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
      error("XQDY0101")
    );
  }

  /**
   * nscons-009 - dynamic namespace constructor - error, two bindings of same prefix .
   */
  @org.junit.Test
  public void nscons009() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $p1 := \"http://example.com/one\"; \n" +
      "        declare variable $p2 := \"http://example.com/two\"; \n" +
      "        <e> { namespace p {$p1}, namespace p {$p2}, element f {42} }</e>\n" +
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
      error("XQDY0102")
    );
  }

  /**
   * nscons-010 - dynamic namespace constructor - requires renaming of attribute node creates an arbitrary prefix for the attribute, so this test is designed to remove the arbitrariness .
   */
  @org.junit.Test
  public void nscons010() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $p1 := \"http://example.com/one\"; \n" +
      "        declare variable $p2 := \"http://example.com/two\"; \n" +
      "        declare variable $r := <e> { namespace p {$p1}, attribute {QName($p2, \"p:att\")} {93.7}, element f {42} }</e>; \n" +
      "        <out> { exists($r/@*:att[prefix-from-QName(node-name(.))!='p']), exists(in-scope-prefixes($r)[.='p']) }</out>\n" +
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
      assertSerialization("<out>true true</out>", false)
    );
  }

  /**
   * nscons-010 - dynamic namespace constructor - requires renaming of element node creates an arbitrary prefix 
   *         for the attribute, so this test is designed to remove the arbitrariness .
   */
  @org.junit.Test
  public void nscons011() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $p1 := \"http://example.com/one\"; \n" +
      "        declare variable $p2 := \"http://example.com/two\"; \n" +
      "        declare variable $r := element {QName($p2, 'p:e')} { namespace p {$p1}, element f {42} }; \n" +
      "        <out> { exists($r[prefix-from-QName(node-name(.))!='p']), exists(in-scope-prefixes($r)[.='p']) }</out>\n" +
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
      assertSerialization("<out>true true</out>", false)
    );
  }

  /**
   * nscons-012 - dynamic namespace constructor - show some properties of the namespace node .
   */
  @org.junit.Test
  public void nscons012() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $p1 := \"http://example.com/one\"; \n" +
      "        declare variable $p2 := \"http://example.com/two\"; \n" +
      "        declare function local:f($ns as namespace-node()) as element() { \n" +
      "            <namespace name=\"{name($ns)}\" local-name=\"{local-name($ns)}\" \n" +
      "                namespace-uri=\"{namespace-uri($ns)}\" string-value=\"{string($ns)}\" typed-value=\"{data($ns)}\" \n" +
      "                is-untyped=\"{data($ns) instance of xs:untypedAtomic}\" parent-exists=\"{exists($ns/..)}\" \n" +
      "                is-namespace=\"{$ns instance of namespace-node()}\" \n" +
      "                is-node=\"{$ns instance of node()}\" is-item=\"{$ns instance of item()}\" \n" +
      "                same-as-self=\"{$ns is $ns}\"/> \n" +
      "        }; \n" +
      "        <out>{ \n" +
      "            local:f(namespace p {\"http://example.com/one\"}), \n" +
      "            local:f(namespace {\"\"} {\"http://example.com/two\"}) }</out>\n" +
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
      assertSerialization("<out><namespace \n              same-as-self=\"true\" is-namespace=\"true\" namespace-uri=\"\" is-item=\"true\"\n              typed-value=\"http://example.com/one\"\n              is-untyped=\"false\"\n              string-value=\"http://example.com/one\"\n              local-name=\"p\"\n              parent-exists=\"false\"\n              name=\"p\"\n              is-node=\"true\"/><namespace \n              same-as-self=\"true\" is-namespace=\"true\" namespace-uri=\"\" is-item=\"true\"\n              typed-value=\"http://example.com/two\"\n              is-untyped=\"false\"\n              string-value=\"http://example.com/two\"\n              local-name=\"\"\n              parent-exists=\"false\"\n              name=\"\"\n              is-node=\"true\"/></out>", false)
    );
  }

  /**
   * nscons-013 - dynamic namespace constructor - atomization of prefix expression.
   */
  @org.junit.Test
  public void nscons013() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $pre := <prefix>z</prefix>,\n" +
      "            $uri := \"http://www.zorba-xquery.com/\"\n" +
      "        return\n" +
      "          <e>{ namespace { $pre } { $uri } }</e>\n" +
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
      assertSerialization("<e xmlns:z=\"http://www.zorba-xquery.com/\"/>", false)
    );
  }

  /**
   * nscons-014 - dynamic namespace constructor - prefix from string.
   */
  @org.junit.Test
  public void nscons014() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $pre := \"z\",\n" +
      "            $uri := \"http://www.zorba-xquery.com/\"\n" +
      "        return\n" +
      "          <e>{ namespace { $pre } { $uri } }</e>\n" +
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
      assertSerialization("<e xmlns:z=\"http://www.zorba-xquery.com/\"/>", false)
    );
  }

  /**
   * nscons-015 - dynamic namespace constructor - prefix expression is empty.
   */
  @org.junit.Test
  public void nscons015() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace z=\"http://www.zorba-xquery.com/\";\n" +
      "        <z:e>{ namespace { <a/>/* } { \"http://www.w3.org/\" } }</z:e>\n" +
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
      assertSerialization("<z:e xmlns:z=\"http://www.zorba-xquery.com/\" xmlns=\"http://www.w3.org/\" />", false)
    );
  }

  /**
   * nscons-016 - dynamic namespace constructor - untypedAtomic prefix expression can not be cast to ncname.
   */
  @org.junit.Test
  public void nscons016() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $pre := <prefix>z:z</prefix>,\n" +
      "            $uri := \"http://www.zorba-xquery.com/\"\n" +
      "        return\n" +
      "          <e>{ namespace { $pre } { $uri } }</e>\n" +
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
      error("XQDY0074")
    );
  }

  /**
   * nscons-017 - dynamic namespace constructor - string prefix expression can not be cast to ncname.
   */
  @org.junit.Test
  public void nscons017() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $pre := \"z z\",\n" +
      "            $uri := \"http://www.zorba-xquery.com/\"\n" +
      "        return\n" +
      "          <e>{ namespace { $pre } { $uri } }</e>\n" +
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
      error("XQDY0074")
    );
  }

  /**
   * nscons-018 - dynamic namespace constructor - prefix expression is not string/untypedAtomic.
   */
  @org.junit.Test
  public void nscons018() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $pre := 1,\n" +
      "            $uri := \"http://www.zorba-xquery.com/\"\n" +
      "        return\n" +
      "          <e>{ namespace { $pre } { $uri } }</e>\n" +
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
      error("XPTY0004")
    );
  }

  /**
   * nscons-019 - dynamic namespace constructor - bind a prefix other than xml to the namespace URI http://www.w3.org/XML/1998/namespace.
   */
  @org.junit.Test
  public void nscons019() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $uri := \"http://www.w3.org/XML/1998/namespace\"\n" +
      "        return\n" +
      "          <e>{ namespace x { $uri } }</e>\n" +
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
      error("XQDY0101")
    );
  }

  /**
   * nscons-020 - dynamic namespace constructor - bind a prefix to the namespace URI http://www.w3.org/2000/xmlns/.
   */
  @org.junit.Test
  public void nscons020() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $uri := \"http://www.w3.org/2000/xmlns/\"\n" +
      "        return\n" +
      "          <e>{ namespace x { $uri } }</e>\n" +
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
      error("XQDY0101")
    );
  }

  /**
   * nscons-021 - dynamic namespace constructor - bind any prefix to a zero-length namespace URI.
   */
  @org.junit.Test
  public void nscons021() {
    final XQuery query = new XQuery(
      "\n" +
      "        <e>{ namespace x { \"\" } }</e>\n" +
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
      error("XQDY0101")
    );
  }

  /**
   * nscons-022 - dynamic namespace constructor - added to the element's in-scope namespaces.
   */
  @org.junit.Test
  public void nscons022() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $elem := <e>{ namespace z { \"http://www.zorba-xquery.com/\" } }</e>\n" +
      "        return\n" +
      "          element { resolve-QName(\"z:f\", $elem) } {}\n" +
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
      assertSerialization("<z:f xmlns:z=\"http://www.zorba-xquery.com/\" />", false)
    );
  }

  /**
   * nscons-023 - dynamic namespace constructor - no effect on the statically known namespaces.
   */
  @org.junit.Test
  public void nscons023() {
    final XQuery query = new XQuery(
      "\n" +
      "        <z:e>{ namespace z { \"http://www.zorba-xquery.com/\" } }</z:e>\n" +
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
   * nscons-024 - dynamic namespace constructor - no effect on the statically known namespaces.
   */
  @org.junit.Test
  public void nscons024() {
    final XQuery query = new XQuery(
      "\n" +
      "        <e>{ namespace z { \"http://www.zorba-xquery.com/\" }, element z:e {} }</e>\n" +
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
   * nscons-025 - dynamic namespace constructor - no effect on the statically known namespaces.
   */
  @org.junit.Test
  public void nscons025() {
    final XQuery query = new XQuery(
      "\n" +
      "        element e { attribute z:a {},  namespace z { \"http://www.zorba-xquery.com/\" } }\n" +
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
   * nscons-026 - dynamic namespace constructor - no effect on the statically known namespaces.
   */
  @org.junit.Test
  public void nscons026() {
    final XQuery query = new XQuery(
      "\n" +
      "        <e>{ namespace z { \"http://www.zorba-xquery.com/\" }, element { xs:QName(\"z:e\") } { } }</e>\n" +
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
      error("FONS0004")
    );
  }

  /**
   * nscons-027 - dynamic namespace constructor - return namespace node from module.
   */
  @org.junit.Test
  public void nscons027() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace mod1=\"http://www.w3.org/TestModules/cnc-module\";\n" +
      "        let $elem := <e>{ mod1:one() }</e>\n" +
      "        return\n" +
      "          element { resolve-QName(\"z:f\", $elem) } {}\n" +
      "      ",
      ctx);
    try {
      query.addModule("http://www.w3.org/TestModules/cnc-module", file("prod/CompNamespaceConstructor/cnc-module.xq"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<z:f xmlns:z=\"http://www.zorba-xquery.com/\" />", false)
    );
  }

  /**
   * nscons-028 - dynamic namespace constructor - node equality and modules.
   */
  @org.junit.Test
  public void nscons028() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace mod1=\"http://www.w3.org/TestModules/cnc-module\";\n" +
      "        let $ns := mod1:one()\n" +
      "        return ($ns is $ns, $ns is mod1:one())\n" +
      "      ",
      ctx);
    try {
      query.addModule("http://www.w3.org/TestModules/cnc-module", file("prod/CompNamespaceConstructor/cnc-module.xq"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "true false")
    );
  }

  /**
   * nscons-029 - dynamic namespace constructor - serialization of namespace node.
   */
  @org.junit.Test
  public void nscons029() {
    final XQuery query = new XQuery(
      "\n" +
      "        serialize( namespace z { \"http://www.zorba-xquery.com/\" } )\n" +
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
      error("SENR0001")
    );
  }

  /**
   * nscons-030 - dynamic namespace constructor - serialization of element with namespace node.
   */
  @org.junit.Test
  public void nscons030() {
    final XQuery query = new XQuery(
      "\n" +
      "        serialize( element e { namespace z { \"http://www.zorba-xquery.com/\" } } )\n" +
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
        assertQuery("contains($result,'xmlns:z')")
      &&
        assertQuery("contains($result,'\"http://www.zorba-xquery.com/\"')")
      )
    );
  }

  /**
   * nscons-031 - dynamic namespace constructor - copy namespaces mode.
   */
  @org.junit.Test
  public void nscons031() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare copy-namespaces preserve, inherit;\n" +
      "        let $nested := \n" +
      "            element outer { \n" +
      "              namespace out { \"http://out.zorba-xquery.com/\" },\n" +
      "              element inner {\n" +
      "                namespace in { \"http://in.zorba-xquery.com/\" }\n" +
      "              } \n" +
      "            },\n" +
      "            $elem := element e { namespace new { \"http://new.zorba-xquery.com/\" }, $nested }\n" +
      "        return\n" +
      "          $elem/outer/inner\n" +
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
      assertSerialization("<inner xmlns:new=\"http://new.zorba-xquery.com/\" xmlns:out=\"http://out.zorba-xquery.com/\" xmlns:in=\"http://in.zorba-xquery.com/\" />", false)
    );
  }

  /**
   * nscons-032 - dynamic namespace constructor - copy namespaces mode.
   */
  @org.junit.Test
  public void nscons032() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare copy-namespaces preserve, no-inherit;\n" +
      "        let $nested := \n" +
      "            element outer { \n" +
      "              namespace out { \"http://out.zorba-xquery.com/\" },\n" +
      "              element inner { namespace in { \"http://in.zorba-xquery.com/\" } } \n" +
      "            },\n" +
      "            $elem := element e { namespace new { \"http://new.zorba-xquery.com/\" }, $nested }\n" +
      "        return\n" +
      "          $elem/outer/inner\n" +
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
      assertSerialization("<inner xmlns:in=\"http://in.zorba-xquery.com/\" />", false)
    );
  }

  /**
   * nscons-033 - dynamic namespace constructor - copy namespaces mode.
   */
  @org.junit.Test
  public void nscons033() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare copy-namespaces no-preserve, inherit;\n" +
      "        let $nested := \n" +
      "            element outer { \n" +
      "              namespace out { \"http://out.zorba-xquery.com/\" },\n" +
      "              element inner { namespace in { \"http://in.zorba-xquery.com/\" } } \n" +
      "            },\n" +
      "            $elem := element e { namespace new { \"http://new.zorba-xquery.com/\" }, $nested }\n" +
      "        return\n" +
      "          $elem/outer/inner\n" +
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
      assertSerialization("<inner xmlns:new=\"http://new.zorba-xquery.com/\" />", false)
    );
  }

  /**
   * nscons-034 - dynamic namespace constructor - copy namespaces mode.
   */
  @org.junit.Test
  public void nscons034() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare copy-namespaces no-preserve, no-inherit;\n" +
      "        let $nested := \n" +
      "            element outer { \n" +
      "              namespace out { \"http://out.zorba-xquery.com/\" },\n" +
      "              element inner { namespace in { \"http://in.zorba-xquery.com/\" } } \n" +
      "            },\n" +
      "            $elem := element e { namespace new { \"http://new.zorba-xquery.com/\" }, $nested }\n" +
      "        return\n" +
      "          $elem/outer/inner\n" +
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
      assertSerialization("<inner/>", false)
    );
  }

  /**
   * nscons-035 - dynamic namespace constructor - copy namespaces mode and modules.
   */
  @org.junit.Test
  public void nscons035() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace mod1=\"http://www.w3.org/TestModules/cnc-module\";\n" +
      "        declare copy-namespaces preserve, inherit;\n" +
      "        let $nested := mod1:nested(),\n" +
      "            $elem := element e { namespace new { \"http://new.zorba-xquery.com/\" }, $nested }\n" +
      "        return\n" +
      "          $elem/outer/inner\n" +
      "      ",
      ctx);
    try {
      query.addModule("http://www.w3.org/TestModules/cnc-module", file("prod/CompNamespaceConstructor/cnc-module.xq"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<inner xmlns:new=\"http://new.zorba-xquery.com/\" xmlns:out=\"http://out.zorba-xquery.com/\" xmlns:in=\"http://in.zorba-xquery.com/\" />", false)
    );
  }

  /**
   * nscons-036 - dynamic namespace constructor - copy namespaces mode and modules.
   */
  @org.junit.Test
  public void nscons036() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace mod1=\"http://www.w3.org/TestModules/cnc-module\";\n" +
      "        declare copy-namespaces preserve, no-inherit;\n" +
      "        let $nested := mod1:nested(),\n" +
      "            $elem := element e { namespace new { \"http://new.zorba-xquery.com/\" }, $nested }\n" +
      "        return\n" +
      "          $elem/outer/inner\n" +
      "      ",
      ctx);
    try {
      query.addModule("http://www.w3.org/TestModules/cnc-module", file("prod/CompNamespaceConstructor/cnc-module.xq"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<inner xmlns:in=\"http://in.zorba-xquery.com/\" xmlns:out=\"http://out.zorba-xquery.com/\" />", false)
    );
  }

  /**
   * nscons-037 - dynamic namespace constructor - copy namespaces mode and modules.
   */
  @org.junit.Test
  public void nscons037() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace mod1=\"http://www.w3.org/TestModules/cnc-module\";\n" +
      "        declare copy-namespaces no-preserve, inherit;\n" +
      "        let $nested := mod1:nested(),\n" +
      "            $elem := element e { namespace new { \"http://new.zorba-xquery.com/\" }, $nested }\n" +
      "        return\n" +
      "          $elem/outer/inner\n" +
      "      ",
      ctx);
    try {
      query.addModule("http://www.w3.org/TestModules/cnc-module", file("prod/CompNamespaceConstructor/cnc-module.xq"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<inner xmlns:new=\"http://new.zorba-xquery.com/\" />", false)
    );
  }

  /**
   * nscons-038 - dynamic namespace constructor - copy namespaces mode and modules.
   */
  @org.junit.Test
  public void nscons038() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace mod1=\"http://www.w3.org/TestModules/cnc-module\";\n" +
      "        declare copy-namespaces no-preserve, no-inherit;\n" +
      "        let $nested := mod1:nested(),\n" +
      "            $elem := element e { namespace new { \"http://new.zorba-xquery.com/\" }, $nested }\n" +
      "        return\n" +
      "          $elem/outer/inner\n" +
      "      ",
      ctx);
    try {
      query.addModule("http://www.w3.org/TestModules/cnc-module", file("prod/CompNamespaceConstructor/cnc-module.xq"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<inner/>", false)
    );
  }

  /**
   * nscons-039 - dynamic namespace constructor - recursively inherit namespaces.
   */
  @org.junit.Test
  public void nscons039() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare copy-namespaces preserve, inherit;\n" +
      "        \n" +
      "        declare function local:rec-add($level as xs:integer) as element() {\n" +
      "          if ($level > 0) then\n" +
      "            element { concat(\"e\", $level) } { \n" +
      "              namespace { concat(\"pre\", $level) } { concat(\"uri\", $level) },\n" +
      "              local:rec-add($level - 1)\n" +
      "            }\n" +
      "          else\n" +
      "            element e0 {}       \n" +
      "        };\n" +
      "\n" +
      "        local:rec-add(2)/e1/e0\n" +
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
      assertSerialization("<e0 xmlns:pre2=\"uri2\" xmlns:pre1=\"uri1\" />", false)
    );
  }
}
