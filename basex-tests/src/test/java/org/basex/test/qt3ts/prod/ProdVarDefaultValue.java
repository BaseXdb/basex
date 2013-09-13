package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the prod-VarDefaultValue operator.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdVarDefaultValue extends QT3TestSet {

  /**
   * external variable with a default value .
   */
  @org.junit.Test
  public void extvardef001() {
    final XQuery query = new XQuery(
      "declare variable $ext external := 0; <a>{$ext}</a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a>0</a>", false)
    );
  }

  /**
   * external variable with a default value .
   */
  @org.junit.Test
  public void extvardef001a() {
    final XQuery query = new XQuery(
      "declare variable $ext external := 0; $ext",
      ctx);
    try {
      query.bind("ext", new XQuery("5", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("5")
    );
  }

  /**
   * external variable with a default value and a required type .
   */
  @org.junit.Test
  public void extvardef002() {
    final XQuery query = new XQuery(
      "declare variable $ext as xs:integer external := 0; <a>{$ext}</a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a>0</a>", false)
    );
  }

  /**
   * external variable with a default value and a required type .
   */
  @org.junit.Test
  public void extvardef002a() {
    final XQuery query = new XQuery(
      "declare variable $ext as xs:integer external := 0; <a>{$ext}</a>",
      ctx);
    try {
      query.bind("ext", new XQuery("5", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a>5</a>", false)
    );
  }

  /**
   * external variable with a default value and a required type .
   */
  @org.junit.Test
  public void extvardef002b() {
    final XQuery query = new XQuery(
      "declare variable $ext as xs:integer external := 0; <a>{$ext}</a>",
      ctx);
    try {
      query.bind("ext", new XQuery("xs:date('2008-12-01')", ctx).value());
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
   * external variable with a default value and a required type .
   */
  @org.junit.Test
  public void extvardef003() {
    final XQuery query = new XQuery(
      "declare variable $ext as xs:integer* external := (0,1,2); <a>{sum($ext)}</a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a>3</a>", false)
    );
  }

  /**
   * external variable with a default value and a required type .
   */
  @org.junit.Test
  public void extvardef003a() {
    final XQuery query = new XQuery(
      "declare variable $ext as xs:integer* external := (0,1,2); <a>{sum($ext)}</a>",
      ctx);
    try {
      query.bind("ext", new XQuery("4,5,6", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a>15</a>", false)
    );
  }

  /**
   * external variable with a default value and a required type .
   */
  @org.junit.Test
  public void extvardef003b() {
    final XQuery query = new XQuery(
      "declare variable $ext as xs:integer* external := (0,1,2); <a>{sum($ext)}</a>",
      ctx);
    try {
      query.bind("ext", new XQuery("42", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a>42</a>", false)
    );
  }

  /**
   * external variable with a default value - static error - it must be an ExprSingle .
   */
  @org.junit.Test
  public void extvardef004() {
    final XQuery query = new XQuery(
      "declare variable $ext as xs:integer* external := 0,1,2; <a>{sum($ext)}</a>",
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
   * external variable with a default value - static error - it must be an ExprSingle .
   */
  @org.junit.Test
  public void extvardef005() {
    final XQuery query = new XQuery(
      "declare variable $ext as xs:integer* external := ; <a></a>",
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
   * external variable with a default value that depends on other vars .
   */
  @org.junit.Test
  public void extvardef006() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare variable $var as xs:integer := 17; \n" +
      "         declare variable $ext as element(a) external := <a>{$var}</a>; \n" +
      "         <out>{$ext}</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<out><a>17</a></out>", false)
    );
  }

  /**
   * external variable with a default value that depends on other vars .
   */
  @org.junit.Test
  public void extvardef006a() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $var as xs:integer := 17; \n" +
      "        declare variable $ext as xs:integer external := $var; \n" +
      "        <out>{$ext}</out>\n" +
      "      ",
      ctx);
    try {
      query.bind("ext", new XQuery("862", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<out>862</out>", false)
    );
  }

  /**
   * external variable with a default value that depends on other vars .
   */
  @org.junit.Test
  public void extvardef006b() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $var as xs:integer := 17; \n" +
      "        declare variable $ext as xs:integer external := <a>{$var}</a>; \n" +
      "        <out>{$ext}</out>\n" +
      "      ",
      ctx);
    try {
      query.bind("ext", new XQuery("862", ctx).value());
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
   * external variable with a default value that depends on context .
   */
  @org.junit.Test
  public void extvardef007() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $ext as xs:date external := current-date() + xs:dayTimeDuration('P30D'); \n" +
      "        $ext gt xs:date('2008-12-30')\n" +
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
      assertBoolean(true)
    );
  }

  /**
   * If no value is provided for the variable by the external enviroment, and no default value specified -> err:XPDY0002. .
   */
  @org.junit.Test
  public void extvardef008() {
    final XQuery query = new XQuery(
      "declare variable $x external; $x",
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
   * It is implementation-dependent whether an error is raised if no value is provided by the external environment, no default value is specified, and the evaluation of the query does not reference the value of the variable. .
   */
  @org.junit.Test
  public void extvardef009() {
    final XQuery query = new XQuery(
      "declare variable $x external; \"result\"",
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
        assertStringValue(false, "result")
      ||
        error("XPDY0002")
      )
    );
  }

  /**
   * The variable values contain the values of all variables present in the static context (in this case, forward references). .
   */
  @org.junit.Test
  public void extvardef010() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $a := 1;\n" +
      "        declare variable $x external := $a + $b;\n" +
      "        declare variable $b external := 2;\n" +
      "        $x\n" +
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
      assertEq("3")
    );
  }

  /**
   * Functions contain the values of all variables present in the static context. .
   */
  @org.junit.Test
  public void extvardef010a() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $a external := 6 + local:foo();\n" +
      "        declare variable $b external := 12;\n" +
      "        declare function local:foo() { $b + 10 };\n" +
      "        $a\n" +
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
      assertEq("28")
    );
  }

  /**
   * Cycles are forbidden. .
   */
  @org.junit.Test
  public void extvardef011() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $a := $x;\n" +
      "        declare variable $x external := $a + 2;\n" +
      "        $x\n" +
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
      error("XQDY0054")
    );
  }

  /**
   * Cycles are forbidden. .
   */
  @org.junit.Test
  public void extvardef011a() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $x external := 3 + local:foo();\n" +
      "        declare variable $b external := 2 + local:foo();\n" +
      "\n" +
      "        declare function local:foo()\n" +
      "        {\n" +
      "         $b\n" +
      "         };\n" +
      "\n" +
      "         $x\n" +
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
      error("XQDY0054")
    );
  }

  /**
   * The named functions component contains a function for each statically known function signature present in the static context. .
   */
  @org.junit.Test
  public void extvardef012() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:aaa() { 1 };\n" +
      "        declare variable $x external := local:bbb() + local:aaa();\n" +
      "        declare function local:bbb() { 2 };\n" +
      "        $x \n" +
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
      assertEq("3")
    );
  }

  /**
   * The named functions component contains a function for each statically known function signature present in the static context. .
   */
  @org.junit.Test
  public void extvardef013() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $y external;\n" +
      "        declare variable $z external := 10;\n" +
      "        declare function local:aaa() { $z };\n" +
      "        declare variable $x external := local:bbb() + local:aaa() + 2;\n" +
      "        declare function local:bbb() { $y };\n" +
      "        $x \n" +
      "      ",
      ctx);
    try {
      query.bind("y", new XQuery("16", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("28")
    );
  }

  /**
   * Context item in default value for external variable. .
   */
  @org.junit.Test
  public void extvardef014() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $x external := /works/employee[@name eq \"Jane Doe 1\"];\n" +
      "        fn:count($x)\n" +
      "      ",
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
      assertEq("1")
    );
  }

  /**
   * It is a static error if the initializer of the context item depends on the context item. .
   */
  @org.junit.Test
  public void extvardef015() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $y := /works/employee;\n" +
      "        declare context item := $y[9];\n" +
      "        declare variable $x external := fn:position();\n" +
      "        $x\n" +
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
      error("XQDY0054")
    );
  }

  /**
   * Context position in default value for external variable. .
   */
  @org.junit.Test
  public void extvardef016a() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $y := (<a>1</a>,<a>2</a>,<a>3</a>,<a>4</a>,<a>5</a>,<a>6</a>,<a>7</a>,<a>8</a>,<a>9</a>,<a>10</a>);\n" +
      "        declare context item := $y[3];\n" +
      "        declare variable $x external := fn:position();\n" +
      "        $x\n" +
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
      assertEq("1")
    );
  }

  /**
   * Context last in default value for external variable. .
   */
  @org.junit.Test
  public void extvardef016b() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $y := <root><a>1</a>,<a>2</a>,<a>3</a>,<a>4</a>,<a>5</a>,<a>6</a>,<a>7</a>,<a>8</a>,<a>9</a>,<a>10</a></root>;\n" +
      "        declare context item := $y;\n" +
      "        declare variable $x external := fn:last();\n" +
      "        $x\n" +
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
      assertEq("1")
    );
  }

  /**
   * Implicit cast of external variable default. .
   */
  @org.junit.Test
  public void extvardef017() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $x as xs:decimal external := xs:integer(10);\n" +
      "        $x\n" +
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
      assertEq("10")
    );
  }

  /**
   * Undeclared variable in external variable default value. .
   */
  @org.junit.Test
  public void extvardef018() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $x external := $a + 10;\n" +
      "        $x\n" +
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
      error("XPST0008")
    );
  }

  /**
   * External default variable used in external default variable. .
   */
  @org.junit.Test
  public void extvardef019() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $x external := 10;\n" +
      "        declare variable $y external := 18 + $x;\n" +
      "        $y\n" +
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
      assertEq("28")
    );
  }

  /**
   * External default variable used indirectly in external default variable. .
   */
  @org.junit.Test
  public void extvardef020() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $x external := 5;\n" +
      "        declare variable $a := $x + 2;\n" +
      "        declare function local:foo() {$x +$a};\n" +
      "        declare variable $y external := 11 + local:foo() + $x;\n" +
      "        $y\n" +
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
      assertEq("28")
    );
  }

  /**
   * External default variable used indirectly in external default variable, with implicit cast. .
   */
  @org.junit.Test
  public void extvardef021() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $x as xs:integer external := xs:int(5);\n" +
      "        declare variable $y as xs:decimal external := $x;\n" +
      "        $y instance of xs:decimal\n" +
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
      assertEq("fn:true()")
    );
  }

  /**
   * Using external default variable in a function declared before the variable. .
   */
  @org.junit.Test
  public void extvardef022() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:foo() {\n" +
      "          $x\n" +
      "        };\n" +
      "        declare variable $x external := 5;\n" +
      "        local:foo()\n" +
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
      assertEq("5")
    );
  }
}
