package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the StepExpr production.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdStepExpr extends QT3TestSet {

  /**
   *  A 'first-item' predicate combined with a name test inside a function. .
   */
  @org.junit.Test
  public void k2Steps1() {
    final XQuery query = new XQuery(
      "declare function local:myFunc() { e[1] }; local:myFunc()",
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
   *  The last step containing a mixture of nodes and atomic values. .
   */
  @org.junit.Test
  public void k2Steps10() {
    final XQuery query = new XQuery(
      "declare variable $myVar := <e>text</e>; $myVar/text()/(<e/>, (), 1, <e/>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0018")
    );
  }

  /**
   *  '///' is an invalid expression. .
   */
  @org.junit.Test
  public void k2Steps11() {
    final XQuery query = new XQuery(
      "///",
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
   *  Combine variables, the context item and path expressions. .
   */
  @org.junit.Test
  public void k2Steps12() {
    final XQuery query = new XQuery(
      "declare variable $e := ()/.; declare variable $b := <b/>/.; $e, <b/>",
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
        assertSerialization("<b/>", false)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Ensure the focus is available through a for-expression. .
   */
  @org.junit.Test
  public void k2Steps13() {
    final XQuery query = new XQuery(
      "empty(<e/>/(for $i in e return $i))",
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
   *  Ensure sorting and de-duplication is applied to variables when appearing in paths. .
   */
  @org.junit.Test
  public void k2Steps14() {
    final XQuery query = new XQuery(
      "<r> { let $i := <e> <a/> <b/> </e> let $b := ($i/b, $i/a, $i/b, $i/a) return ()/$b } </r>",
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
        assertSerialization("<r/>", false)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Ensure sorting and de-duplication is applied to variables when appearing in paths(#2). .
   */
  @org.junit.Test
  public void k2Steps15() {
    final XQuery query = new XQuery(
      "<r> { let $i := <e> <a/> <b/> </e> let $b := ($i/b, $i/a, $i/b, $i/a) return <e/>/$b } </r>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<r><a/><b/></r>", false)
    );
  }

  /**
   *  Ensure sorting and de-duplication is applied to variables when appearing in paths(#3). .
   */
  @org.junit.Test
  public void k2Steps16() {
    final XQuery query = new XQuery(
      "<r> { let $i := <e> <a/> <b/> </e> let $b := ($i/b, $i/a, $i/b, $i/a) return <e/>/./$b } </r>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<r><a/><b/></r>", false)
    );
  }

  /**
   *  Ensure sorting and de-duplication is applied to variables when appearing in paths(#4). .
   */
  @org.junit.Test
  public void k2Steps17() {
    final XQuery query = new XQuery(
      "<r> { let $i := <e> <a/> <b/> </e> let $b := ($i/b, $i/a, $i/b, $i/a) return $b/. } </r>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<r><a/><b/></r>", false)
    );
  }

  /**
   *  Trigger node sorting of a peculiar case. .
   */
  @org.junit.Test
  public void k2Steps18() {
    final XQuery query = new XQuery(
      "<e> <a/> </e>/*/(., .)/.",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a/>", false)
    );
  }

  /**
   *  Trigger node sorting of a peculiar case(#2). .
   */
  @org.junit.Test
  public void k2Steps19() {
    final XQuery query = new XQuery(
      "<e> <a/> </e>/(., .)/.",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e><a/></e>", false)
    );
  }

  /**
   *  A numeric predicate combined with a name test inside a function. .
   */
  @org.junit.Test
  public void k2Steps2() {
    final XQuery query = new XQuery(
      "declare function local:myFunc() { e[928] }; local:myFunc()",
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
        error("XPDY0002")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Use nested name tests. .
   */
  @org.junit.Test
  public void k2Steps20() {
    final XQuery query = new XQuery(
      "<e> <a/> <b/> </e>/((b, a)/., (.), (*, *))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e><a/><b/></e><a/><b/>", false)
    );
  }

  /**
   *  Tricky combination of focuses and function calls. .
   */
  @org.junit.Test
  public void k2Steps21() {
    final XQuery query = new XQuery(
      "declare variable $root := <a> <b e=\"B\"/> <c e=\"B\"/> </a>; declare function local:function($arg) { $root[\"B\" eq $arg/@e] }; $root/local:function(c)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a><b e=\"B\"/><c e=\"B\"/></a>", false)
    );
  }

  /**
   *  Tricky combination of focuses, function calls, and a cardinality check. .
   */
  @org.junit.Test
  public void k2Steps22() {
    final XQuery query = new XQuery(
      "declare variable $root := <a> <b e=\"B\"/> <c e=\"B\"/> </a>; declare function local:function($arg) { $root[exactly-one($arg/@e)] }; $root/local:function(c)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a><b e=\"B\"/><c e=\"B\"/></a>", false)
    );
  }

  /**
   *  Tricky combination of focuses and function calls(#2). .
   */
  @org.junit.Test
  public void k2Steps23() {
    final XQuery query = new XQuery(
      "declare variable $root := <a><c e=\"\"/></a>; declare function local:function($arg) { $root[$arg/@e] }; $root/local:function(c)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a><c e=\"\"/></a>", false)
    );
  }

  /**
   *  Tricky combination of focuses and function calls(#2). .
   */
  @org.junit.Test
  public void k2Steps24() {
    final XQuery query = new XQuery(
      "declare variable $root := <root> <b d=\"\"/> <c> <c d=\"\"/> <c/> </c> </root>; declare function local:function($object) { $root/b[@d = $object/@d] }; $root/c/c/local:function(.)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<b d=\"\"/>", false)
    );
  }

  /**
   *  Tricky combination of focuses and function calls(#3). .
   */
  @org.junit.Test
  public void k2Steps25() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $root := <root> <b d=\"\"/> <c> <c d=\"\"/> <c/> </c> </root>; \n" +
      "        declare function local:function($object) { $root/b[@d = $object/@d] }; \n" +
      "        $root//local:function(c)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<b d=\"\"/>", false)
    );
  }

  /**
   *  Tricky combination of focuses and function calls(#4). .
   */
  @org.junit.Test
  public void k2Steps26() {
    final XQuery query = new XQuery(
      "declare variable $root := <root> <b d=\"\"/> <c> <c d=\"\"/> <c/> </c> </root>; declare function local:function($object) { $root/b[$object/@d] }; $root//local:function(c)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<b d=\"\"/>", false)
    );
  }

  /**
   *  Tricky combination of focuses and function calls(#5). .
   */
  @org.junit.Test
  public void k2Steps27() {
    final XQuery query = new XQuery(
      "declare variable $root := <root> <b d=\"\"/> <c> <c d=\"\"/> <c/> </c> </root>; declare function local:function($object) { $root[$object/@d] }; $root//local:function(c)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<root><b d=\"\"/><c><c d=\"\"/><c/></c></root>", false)
    );
  }

  /**
   *  Tricky combination of focuses and function calls(#6). .
   */
  @org.junit.Test
  public void k2Steps28() {
    final XQuery query = new XQuery(
      "declare variable $root := <root> <b d=\"\"/> <c d=\"\"/> </root>; declare function local:function($object) { $root[$object/@d] }; $root//local:function(c)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<root><b d=\"\"/><c d=\"\"/></root>", false)
    );
  }

  /**
   *  Tricky combination of focuses and function calls(#7). .
   */
  @org.junit.Test
  public void k2Steps29() {
    final XQuery query = new XQuery(
      "declare variable $root := <root> <c d=\"\"/> </root>; declare function local:function($object) { $root[$object/@d] }; $root//local:function(c)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<root><c d=\"\"/></root>", false)
    );
  }

  /**
   *  A truth predicate combined with a name test inside a function. .
   */
  @org.junit.Test
  public void k2Steps3() {
    final XQuery query = new XQuery(
      "declare function local:myFunc() { e[true()] }; local:myFunc()",
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
        error("XPDY0002")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Tricky combination of focuses and function calls(#8). .
   */
  @org.junit.Test
  public void k2Steps30() {
    final XQuery query = new XQuery(
      "declare variable $root := <root> <c d=\"\"/> </root>; declare function local:function($object) { $root[$object] }; $root//local:function(c)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<root><c d=\"\"/></root>", false)
    );
  }

  /**
   *  Tricky combination of focuses and function calls(#9). .
   */
  @org.junit.Test
  public void k2Steps31() {
    final XQuery query = new XQuery(
      "declare variable $root := <root><c/></root>; declare function local:function($arg) { $root[$arg] }; $root//local:function(.)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<root><c/></root>", false)
    );
  }

  /**
   *  Tricky combination of focus and a recursive function call. .
   */
  @org.junit.Test
  public void k2Steps32() {
    final XQuery query = new XQuery(
      "declare variable $root := <root/>; declare function local:function($arg, $count as xs:integer) { $arg, $root, if($count eq 2) then $root else local:function($arg, $count + 1) }; $root/local:function(., 1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<root/>", false)
    );
  }

  /**
   *  Tricky combination of focus and a recursive function call(#2). .
   */
  @org.junit.Test
  public void k2Steps33() {
    final XQuery query = new XQuery(
      "declare variable $root := ( <b d=\"\"/>, <c> <c d=\"\"/> </c> ); declare function local:function($object) { $root[@d eq $object/@d] }; $root/local:function(c)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<b d=\"\"/>", false)
    );
  }

  /**
   *  Tricky combination of focus and a recursive function call(#3). .
   */
  @org.junit.Test
  public void k2Steps34() {
    final XQuery query = new XQuery(
      "declare variable $root := ( <b d=\"\"/>, <c d=\"\"> <c d=\"\"/> </c> ); declare function local:function($object) { $root[@d eq $object/@d] }; $root/local:function(c)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<b d=\"\"/><c d=\"\"><c d=\"\"/></c>", false)
    );
  }

  /**
   *  Combine predicate with an element and text node constructor. .
   */
  @org.junit.Test
  public void k2Steps35() {
    final XQuery query = new XQuery(
      "<e/>[1]/text{string-join(., \" \")}, 1",
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
        assertCount(2)
      &&
        assertQuery("string($result[1]) eq \"\"")
      &&
        assertQuery("$result[2] eq 1")
      )
    );
  }

  /**
   *  A predicate with last() combined with a name test inside a function. .
   */
  @org.junit.Test
  public void k2Steps4() {
    final XQuery query = new XQuery(
      "declare function local:myFunc() { e[last()] }; local:myFunc()",
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
   *  A direct element constructor as step, followed by a name test. .
   */
  @org.junit.Test
  public void k2Steps5() {
    final XQuery query = new XQuery(
      "empty(<a/>/a)",
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
   *  A direct element constructor as step, followed by the context item. .
   */
  @org.junit.Test
  public void k2Steps6() {
    final XQuery query = new XQuery(
      "<a/>/.",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a/>", false)
    );
  }

  /**
   *  Apply fn:count to an atomic step. .
   */
  @org.junit.Test
  public void k2Steps7() {
    final XQuery query = new XQuery(
      "count((<a/>, <!--comment-->)/3)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("2")
    );
  }

  /**
   *  '//' by itself is a syntax error. .
   */
  @org.junit.Test
  public void k2Steps8() {
    final XQuery query = new XQuery(
      "(/)/(//)/foo",
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
        error("XPST0003")
      ||
        error("XPDY0002")
      )
    );
  }

  /**
   *  A syntax error in a path step. .
   */
  @org.junit.Test
  public void k2Steps9() {
    final XQuery query = new XQuery(
      "child::local:b(:ada",
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
   *  Verify xgc:leading-lone-slash implementation This expression is a syntax error: /*5 .
   */
  @org.junit.Test
  public void stepsLeadingLoneSlash1() {
    final XQuery query = new XQuery(
      "declare variable $var := document {<a>123</a>}; $var[/*5]",
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
   *  Verify xgc:leading-lone-slash implementation This expression is a valid path expression /* .
   */
  @org.junit.Test
  public void stepsLeadingLoneSlash10() {
    final XQuery query = new XQuery(
      "declare variable $var := document {<a>123</a>}; $var[/*]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a>123</a>", false)
    );
  }

  /**
   *  Verify xgc:leading-lone-slash implementation This expression is a valid path expression /<a/> .
   */
  @org.junit.Test
  public void stepsLeadingLoneSlash11() {
    final XQuery query = new XQuery(
      "declare variable $var := document {<a>123</a>}; $var[/<a/>]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a>123</a>", false)
    );
  }

  /**
   *  Verify xgc:leading-lone-slash implementation This expression is a valid path expression /<a div="3"/> .
   */
  @org.junit.Test
  public void stepsLeadingLoneSlash12() {
    final XQuery query = new XQuery(
      "declare variable $var := document {<a>123</a>}; $var[/<a div=\"3\"/>]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a>123</a>", false)
    );
  }

  /**
   *  Verify xgc:leading-lone-slash implementation This expression is a valid path expression /unordered{a} .
   */
  @org.junit.Test
  public void stepsLeadingLoneSlash13() {
    final XQuery query = new XQuery(
      "declare variable $var := document {<a>123</a>}; $var[/unordered{a}]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a>123</a>", false)
    );
  }

  /**
   *  Verify xgc:leading-lone-slash implementation This expression is a valid path expression /max(a) .
   */
  @org.junit.Test
  public void stepsLeadingLoneSlash14() {
    final XQuery query = new XQuery(
      "declare variable $var := document {<a>123</a>}; $var[/max(a)]",
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
   *  Verify xgc:leading-lone-slash implementation This expression is a valid expression /* .
   */
  @org.junit.Test
  public void stepsLeadingLoneSlash15() {
    final XQuery query = new XQuery(
      "declare variable $var := document {<a>123</a>}; $var[/-5]",
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
   *  Verify xgc:leading-lone-slash implementation This expression is a valid expression /=$a .
   */
  @org.junit.Test
  public void stepsLeadingLoneSlash16() {
    final XQuery query = new XQuery(
      "declare variable $var := document {<a>123</a>}; declare variable $a := document {<a>123</a>}; $var[/=$a]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a>123</a>", false)
    );
  }

  /**
   *  Verify xgc:leading-lone-slash implementation This expression is a valid expression 5* / .
   */
  @org.junit.Test
  public void stepsLeadingLoneSlash17() {
    final XQuery query = new XQuery(
      "declare variable $var := document {<a>123</a>}; declare variable $a := document {<a>123</a>}; $var[5*/]",
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
   *  Verify xgc:leading-lone-slash implementation This expression is a valid expression (/)*5 .
   */
  @org.junit.Test
  public void stepsLeadingLoneSlash1a() {
    final XQuery query = new XQuery(
      "declare variable $var := document {<a>123</a>}; $var[(/)*5]",
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
   *  Verify xgc:leading-lone-slash implementation This expression is a syntax error: /<a .
   */
  @org.junit.Test
  public void stepsLeadingLoneSlash2() {
    final XQuery query = new XQuery(
      "declare variable $var := document {<a>123</a>}; $var[/<a]",
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
   *  Verify xgc:leading-lone-slash implementation This expression is a valid expression (/)<a .
   */
  @org.junit.Test
  public void stepsLeadingLoneSlash2a() {
    final XQuery query = new XQuery(
      "declare variable $var := document {<a>123</a>}; $var[(/)<a]",
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
   *  Verify xgc:leading-lone-slash implementation This expression is a syntax error: /<5 .
   */
  @org.junit.Test
  public void stepsLeadingLoneSlash3() {
    final XQuery query = new XQuery(
      "declare variable $var := document {<a>123</a>}; $var[/<5]",
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
   *  Verify xgc:leading-lone-slash implementation This expression is a valid expression (/)<5 .
   */
  @org.junit.Test
  public void stepsLeadingLoneSlash3a() {
    final XQuery query = new XQuery(
      "declare variable $var := document {<a>123</a>}; $var[(/)<5]",
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
   *  Verify xgc:leading-lone-slash implementation This expression is a syntax error: /</b .
   */
  @org.junit.Test
  public void stepsLeadingLoneSlash4() {
    final XQuery query = new XQuery(
      "declare variable $var := document {<a>123</a>}; $var[/</b]",
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
   *  Verify xgc:leading-lone-slash implementation This expression is a syntax error: /<a div 3 .
   */
  @org.junit.Test
  public void stepsLeadingLoneSlash5() {
    final XQuery query = new XQuery(
      "declare variable $var := document {<a>123</a>}; $var[/<a div 3]",
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
   *  Verify xgc:leading-lone-slash implementation This expression is a valid expression (/)<a div 3 .
   */
  @org.junit.Test
  public void stepsLeadingLoneSlash5a() {
    final XQuery query = new XQuery(
      "declare variable $var := document {<a>123</a>}; $var[(/)<a div 3]",
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
   *  Verify xgc:leading-lone-slash implementation This expression is a syntax error: /if ($doclevel) then / else /* .
   */
  @org.junit.Test
  public void stepsLeadingLoneSlash6() {
    final XQuery query = new XQuery(
      "declare variable $var := document {<a>123</a>}; $var[/if ($doclevel) then / else /*]",
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
   *  Verify xgc:leading-lone-slash implementation This expression is a syntax error: / is $a .
   */
  @org.junit.Test
  public void stepsLeadingLoneSlash7() {
    final XQuery query = new XQuery(
      "declare variable $var := document {<a>123</a>}; declare variable $a := document {<a>123</a>}; $var[/ is $a]",
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
   *  Verify xgc:leading-lone-slash implementation This expression is a valid expression (/) is $a .
   */
  @org.junit.Test
  public void stepsLeadingLoneSlash7a() {
    final XQuery query = new XQuery(
      "declare variable $var := document {<a>123</a>}; declare variable $a := document {<a>123</a>}; $var[(/) is $a]",
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
   *  Verify xgc:leading-lone-slash implementation This expression is a syntax error: / instance of document-node(element(x)) .
   */
  @org.junit.Test
  public void stepsLeadingLoneSlash8() {
    final XQuery query = new XQuery(
      "declare variable $var := document {<a>123</a>}; $var[/ instance of document-node(element(x))]",
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
   *  Verify xgc:leading-lone-slash implementation This expression is a valid expression (/) instance of document-node(element(x)) .
   */
  @org.junit.Test
  public void stepsLeadingLoneSlash8a() {
    final XQuery query = new XQuery(
      "declare variable $var := document {<a>123</a>}; $var[(/) instance of document-node(element(x))]",
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
   *  Verify xgc:leading-lone-slash implementation This expression is a syntax error: let $doc := / return $doc/* .
   */
  @org.junit.Test
  public void stepsLeadingLoneSlash9() {
    final XQuery query = new XQuery(
      "declare variable $var := document {<a>123</a>}; let $doc := / return $doc/*",
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
}
