package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the prod-ContextItemDecl production introduced in XQuery 3.0.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdContextItemDecl extends QT3TestSet {

  /**
   * Forwards reference to context item. .
   */
  @org.junit.Test
  public void contextDecl014() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $x := . + 5;\n" +
      "        declare context item := 17;\n" +
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
      assertEq("22")
    );
  }

  /**
   * It is a static error if the initializer of the context item depends on the context item. .
   */
  @org.junit.Test
  public void contextDecl015() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $y := /works/employee;\n" +
      "        declare context item := $y[9];\n" +
      "        declare variable $x external := if (./*) then fn:position() else 0;\n" +
      "        ($x, $y)\n" +
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
  public void contextDecl016() {
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
  public void contextDecl017() {
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
   * Context last in default value for context item. (Debatable!).
   */
  @org.junit.Test
  public void contextDecl018() {
    final XQuery query = new XQuery(
      " declare context item := last() + 1; .",
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
        assertEq("2")
      ||
        error("XPDY0002")
      )
    );
  }

  /**
   * Context position in default value for context item. (Debatable!).
   */
  @org.junit.Test
  public void contextDecl019() {
    final XQuery query = new XQuery(
      " declare context item := position() + 1; .",
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
        assertEq("2")
      ||
        error("XPDY0002")
      )
    );
  }

  /**
   *  External context item has wrong type .
   */
  @org.junit.Test
  public void contextDecl020() {
    final XQuery query = new XQuery(
      " declare context item as xs:integer external; . ",
      ctx);
    try {
      query.context(new XQuery("'London'", ctx).value());
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
   *  Internal context item has wrong type .
   */
  @org.junit.Test
  public void contextDecl021() {
    final XQuery query = new XQuery(
      " declare context item as xs:integer := 'London'; . ",
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
   *  Default context item has wrong type; error is optional? .
   */
  @org.junit.Test
  public void contextDecl022() {
    final XQuery query = new XQuery(
      " declare context item as xs:string := 2; . ",
      ctx);
    try {
      query.context(new XQuery("'London'", ctx).value());
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
        assertEq("'London'")
      )
    );
  }

  /**
   * Context item type must be an item type (no occurrence indicator).
   */
  @org.junit.Test
  public void contextDecl023() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare context item as xs:integer+ := (1 to 17)[position() = 5];\n" +
      "        .\n" +
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
   * Context item bound to an atomic value .
   */
  @org.junit.Test
  public void contextDecl028() {
    final XQuery query = new XQuery(
      " declare context item := 3; . + 4 ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("7")
    );
  }

  /**
   * Context item bound to a node .
   */
  @org.junit.Test
  public void contextDecl029() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare context item := <a>bananas</a>;\n" +
      "        string-length()\n" +
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
      assertEq("7")
    );
  }

  /**
   * Context item bound to a node .
   */
  @org.junit.Test
  public void contextDecl030() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare context item := <a id=\"qwerty\">bananas</a>;\n" +
      "        string-length(@id)\n" +
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
      assertEq("6")
    );
  }

  /**
   * Context item bound to a function item .
   */
  @org.junit.Test
  public void contextDecl031() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare context item := contains(?, \"e\");\n" +
      "        .(\"raspberry\")\n" +
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
   * Context item bound to an empty sequence. Spec unclear, see bug 19257.
   */
  @org.junit.Test
  public void contextDecl032() {
    final XQuery query = new XQuery(
      "declare context item := (1 to 17)[20]; .",
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
   * Context item bound to a non-singleton sequence. Spec unclear, see bug 19257.
   */
  @org.junit.Test
  public void contextDecl033() {
    final XQuery query = new XQuery(
      "declare context item := (1 to 17)[position() gt 5]; .",
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
   * Context item successfully matches its type.
   */
  @org.junit.Test
  public void contextDecl034() {
    final XQuery query = new XQuery(
      "declare context item as xs:integer := (1 to 17)[position() = 5]; .",
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

  /**
   * Context item successfully matches its type.
   */
  @org.junit.Test
  public void contextDecl035() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare context item as xs:integer := (1 to 17)[position() = 5];\n" +
      "        .\n" +
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

  /**
   * Context item fails to match its type.
   */
  @org.junit.Test
  public void contextDecl036() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare context item as xs:integer := current-date();\n" +
      "        .\n" +
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
   * Function conversion rules not applied to context item.
   */
  @org.junit.Test
  public void contextDecl037() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare context item as xs:integer := <a>23</a>;\n" +
      "        .\n" +
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
   * Function conversion rules not applied to context item.
   */
  @org.junit.Test
  public void contextDecl038() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare context item as xs:anyURI := \"http://www.w3.org/\";\n" +
      "        .\n" +
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
   * Function conversion rules not applied to context item.
   */
  @org.junit.Test
  public void contextDecl039() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare context item as xs:double := 1.234;\n" +
      "        .\n" +
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
   * Require an external context item, no required type.
   */
  @org.junit.Test
  public void contextDecl040() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare context item external;\n" +
      "        . instance of document-node()\n" +
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
      assertBoolean(true)
    );
  }

  /**
   * Require an external context item, with required type.
   */
  @org.junit.Test
  public void contextDecl041() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare context item as document-node() external;\n" +
      "        name(/*)\n" +
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
      assertEq("\"works\"")
    );
  }

  /**
   * Require an external context item, with default (no type).
   */
  @org.junit.Test
  public void contextDecl042() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare context item external := 17;\n" +
      "        . = 17\n" +
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
   * Require an external context item, with default (with type).
   */
  @org.junit.Test
  public void contextDecl043() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare context item as xs:integer external := 17;\n" +
      "        . = 17\n" +
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
   * Require an external context item, with default (with wrong type).
   */
  @org.junit.Test
  public void contextDecl044() {
    final XQuery query = new XQuery(
      " declare context item as xs:double external := 17; . = 17",
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
   * Require an external context item but none supplied.
   */
  @org.junit.Test
  public void contextDecl045() {
    final XQuery query = new XQuery(
      " declare context item as xs:double external; . = 17",
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
   * More than one context item declaration.
   */
  @org.junit.Test
  public void contextDecl046() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare context item as xs:double external;\n" +
      "        declare context item as xs:integer := 15;\n" +
      "        . = 17\n" +
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
      error("XQST0099")
    );
  }

  /**
   * Context item declaration constraining type appears in library module.
   */
  @org.junit.Test
  public void contextDecl047() {
    final XQuery query = new XQuery(
      "\n" +
      "      \timport module namespace m=\"http://www.w3.org/TestModules/libmodule2\"; \n" +
      "        . gt xs:date('1900-01-01')\n" +
      "      ",
      ctx);
    try {
      query.context(new XQuery("current-date()", ctx).value());
      query.addModule("http://www.w3.org/TestModules/libmodule2", file("prod/ContextItemDecl/libmodule-2.xq"));
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
   * Context item declaration with default value appears in library module.
   */
  @org.junit.Test
  public void contextDecl048() {
    final XQuery query = new XQuery(
      "\n" +
      "      \timport module namespace m=\"http://www.w3.org/TestModules/libmodule1\"; \n" +
      "      \t. = 17\n" +
      "      ",
      ctx);
    try {
      query.addModule("http://www.w3.org/TestModules/libmodule1", file("prod/ContextItemDecl/libmodule-1.xq"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0113")
    );
  }

  /**
   * Context item declaration with type appears in library module.
   */
  @org.junit.Test
  public void contextDecl049() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace m=\"http://www.w3.org/TestModules/libmodule2\"; \n" +
      "        declare context item as xs:date := current-date();\n" +
      "        . gt xs:date('1900-01-01')\n" +
      "      ",
      ctx);
    try {
      query.addModule("http://www.w3.org/TestModules/libmodule2", file("prod/ContextItemDecl/libmodule-2.xq"));
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
   * Context item declaration with different type appears in library module.
   */
  @org.junit.Test
  public void contextDecl050() {
    final XQuery query = new XQuery(
      "\n" +
      "      \timport module namespace m=\"http://www.w3.org/TestModules/libmodule2\"; \n" +
      "        declare context item as xs:integer := 23;\n" +
      "        . eq 23\n" +
      "      ",
      ctx);
    try {
      query.addModule("http://www.w3.org/TestModules/libmodule2", file("prod/ContextItemDecl/libmodule-2.xq"));
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
   * Context item declaration with different type appears in library module.
   */
  @org.junit.Test
  public void contextDecl051() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace m=\"http://www.w3.org/TestModules/libmodule2\"; \n" +
      "        declare context item as node() external;\n" +
      "        . instance of element()\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      query.addModule("http://www.w3.org/TestModules/libmodule2", file("prod/ContextItemDecl/libmodule-2.xq"));
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
   * Context item declaration in library module with initial value.
   */
  @org.junit.Test
  public void contextDecl052() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace m=\"http://www.w3.org/TestModules/libmodule3\"; \n" +
      "        . eq 23\n" +
      "      ",
      ctx);
    try {
      query.addModule("http://www.w3.org/TestModules/libmodule3", file("prod/ContextItemDecl/libmodule-3.xq"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0113")
    );
  }

  /**
   * Dynamic circularity involving function lookup.
   */
  @org.junit.Test
  public void contextDecl053() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $p := \"base-uri\";\n" +
      "        declare variable $f := function-lookup(xs:QName(\"fn:\"||$p), 0);\n" +
      "        declare context item := $f();\n" +
      "        .\n" +
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
   * Context item declaration constraining type appears in library module.
   */
  @org.junit.Test
  public void contextDecl054() {
    final XQuery query = new XQuery(
      "\n" +
      "      \timport module namespace m=\"http://www.w3.org/TestModules/libmodule2\"; \n" +
      "        xs:date(.) gt xs:date('1900-01-01')\n" +
      "      ",
      ctx);
    try {
      query.context(new XQuery("current-dateTime()", ctx).value());
      query.addModule("http://www.w3.org/TestModules/libmodule2", file("prod/ContextItemDecl/libmodule-2.xq"));
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
   * Forwards reference to context item involving function lookup.
   */
  @org.junit.Test
  public void contextDecl055() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare variable $f := function-lookup(xs:QName(\"fn:\"||$p), 0);\n" +
      "      declare context item := <e/>;\n" +
      "      declare variable $p := \"local-name\";\n" +
      "      $f()\n" +
      "    ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("\"e\"")
    );
  }
}
