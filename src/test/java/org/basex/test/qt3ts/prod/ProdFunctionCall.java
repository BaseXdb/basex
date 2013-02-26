package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the FunctionCall production.
 *     Contains tests verifying the function calling mechanism; not a particular function implementation..
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdFunctionCall extends QT3TestSet {

  /**
   * Test that arguments are atomized - built in function.
   */
  @org.junit.Test
  public void functionCall001() {
    final XQuery query = new XQuery(
      "concat(<a>X</a>, <a>Y</a>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "XY")
    );
  }

  /**
   * Test that arguments are atomized - constructor function.
   */
  @org.junit.Test
  public void functionCall002() {
    final XQuery query = new XQuery(
      "xs:boolean(<a>0</a>)",
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
   * Test that arguments are atomized - user-defined function.
   */
  @org.junit.Test
  public void functionCall004() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare function local:f($in as xs:boolean) as xs:boolean { $in };\n" +
      "         local:f(<a>0</a>)",
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
   * Test that arguments are atomized - anonymous inline function.
   */
  @org.junit.Test
  public void functionCall006() {
    final XQuery query = new XQuery(
      "\n" +
      "         let $f := function ($in as xs:boolean) as xs:boolean { $in }\n" +
      "         return $f(<a>0</a>)\n" +
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
   * Test that a sequence of untypedAtomic values is cast to the target type: user-defined function.
   *          Also tests XPath 3.0 casting to list, and bang operator..
   */
  @org.junit.Test
  public void functionCall009() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($in as xs:decimal*) as xs:decimal {sum($in, 0.0)};\n" +
      "        local:f(xs:NMTOKENS('1 1.2 1.3 1.4')!xs:untypedAtomic(.))\n" +
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
        assertType("xs:decimal")
      &&
        assertEq("4.9")
      )
    );
  }

  /**
   * Test that the untypedAtomic result of a user-defined function is cast to the declared type..
   */
  @org.junit.Test
  public void functionCall013() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($a as xs:integer, $b as xs:integer) as xs:integer {\n" +
      "          data(<a>{$a}{$b}</a>)\n" +
      "        };\n" +
      "        local:f(12, 34)\n" +
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
        assertType("xs:integer")
      &&
        assertEq("1234")
      )
    );
  }

  /**
   * Test that the untypedAtomic result of an inline function is cast to the declared type..
   */
  @org.junit.Test
  public void functionCall014() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $f := function($a as xs:integer, $b as xs:integer) as xs:integer {\n" +
      "          data(<a>{$a}{$b}</a>)\n" +
      "        }\n" +
      "        return $f(12, 34)\n" +
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
        assertType("xs:integer")
      &&
        assertEq("1234")
      )
    );
  }

  /**
   * Test that a sequence of arguments is atomized.
   */
  @org.junit.Test
  public void functionCall023() {
    final XQuery query = new XQuery(
      "string-join((<a>X</a>, <a>Y</a>, <a>Z</a>), '')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "XYZ")
    );
  }

  /**
   *  A test whose essence is: `func-does-not-exist(1, 2, 3)`. .
   */
  @org.junit.Test
  public void kFunctionCallExpr1() {
    final XQuery query = new XQuery(
      "func-does-not-exist(1, 2, 3)",
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
   *  A function call containing an invalid QName as name. .
   */
  @org.junit.Test
  public void kFunctionCallExpr10() {
    final XQuery query = new XQuery(
      "p:f:lname()",
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
        error("XPST0081")
      )
    );
  }

  /**
   *  No function by name fn:document() exists(although one does in XSL-T). .
   */
  @org.junit.Test
  public void kFunctionCallExpr11() {
    final XQuery query = new XQuery(
      "document(\"example.com/file.ext\")",
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
   *  No function by name fn:unparsed-text() exists(although one does in XSL-T). .
   */
  @org.junit.Test
  public void kFunctionCallExpr12() {
    final XQuery query = new XQuery(
      "unparsed-text(\"example.com/file.ext\")",
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
   *  No function by name fn:unparsed-text() exists(although one does in XSL-T). .
   */
  @org.junit.Test
  public void kFunctionCallExpr13() {
    final XQuery query = new XQuery(
      "unparsed-text-available(\"example.com/file.ext\")",
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
   *  No function by name fn:key() exists(although one does in XSL-T). .
   */
  @org.junit.Test
  public void kFunctionCallExpr14() {
    final XQuery query = new XQuery(
      "key('func', \"a value\")",
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
   *  No function by name fn:format-number() exists in XQuery 1.0 (although one does in XSLT and in XQuery 3.0). .
   */
  @org.junit.Test
  public void kFunctionCallExpr15() {
    final XQuery query = new XQuery(
      "format-number(3, \"0000\")",
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
   *  No function by name fn:format-number() exists in XQuery 1.0 (although one does in XSLT and in XQuery 3.0). .
   */
  @org.junit.Test
  public void kFunctionCallExpr15a() {
    final XQuery query = new XQuery(
      "format-number(3, \"0000\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("\"0003\"")
    );
  }

  /**
   *  No function by name fn:format-time() exists in XQuery 1.0 (although one does in XSLT). .
   */
  @org.junit.Test
  public void kFunctionCallExpr16() {
    final XQuery query = new XQuery(
      "matches(format-time(current-time(), \"[H01]:[m01]\"), \"[0-2][0-9]:[0-5][0-9]\")",
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
   *  No function by name fn:format-time() exists in XQuery 1.0 (although one does in XSLT). .
   */
  @org.junit.Test
  public void kFunctionCallExpr16a() {
    final XQuery query = new XQuery(
      "matches(format-time(current-time(), \"[H01]:[m01]\"), \"[0-2][0-9]:[0-5][0-9]\")",
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
   *  No function by name fn:format-time() exists in XQuery 1.0 (although one does in XSLT). .
   */
  @org.junit.Test
  public void kFunctionCallExpr17() {
    final XQuery query = new XQuery(
      "matches(format-time(current-time(), \"[H01]:[m01]\", (), (), ()), \"..:..\")",
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
   *  No function by name fn:format-time() exists in XQuery 1.0 (although one does in XSLT). .
   */
  @org.junit.Test
  public void kFunctionCallExpr17a() {
    final XQuery query = new XQuery(
      "matches(format-time(current-time(), \"[H01]:[m01]\", (), (), ()), \"..:..\")",
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
   *  No function by name fn:format-date() exists in XQuery 1.0 (although one does in XSLT and XQuery 1.1). .
   */
  @org.junit.Test
  public void kFunctionCallExpr18() {
    final XQuery query = new XQuery(
      "matches(format-dateTime(current-dateTime(), \"[Y0001]-[M01]-[D01]\"), \"[0-9]{4}-[0-9]{2}-[0-9]{2}\")",
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
        error("XPST0017")
      )
    );
  }

  /**
   *  No function by name fn:format-dateTime() exists in XQuery 1.0 (although one does in XSLT and XQuery 1.1). .
   */
  @org.junit.Test
  public void kFunctionCallExpr19() {
    final XQuery query = new XQuery(
      "matches(format-dateTime(current-dateTime(), \"[Y0001]-[M01]-[D01]\", (), (), ()), \"....-..-..\")",
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
        error("XPST0017")
      )
    );
  }

  /**
   *  A test whose essence is: `fn:func-does-not-exist(1, 2, 3)`. .
   */
  @org.junit.Test
  public void kFunctionCallExpr2() {
    final XQuery query = new XQuery(
      "fn:func-does-not-exist(1, 2, 3)",
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
   *  No function by name fn:format-dateTime() exists in XQuery 1.0 (although one does in XSLT and XQuery 1.1). .
   */
  @org.junit.Test
  public void kFunctionCallExpr20() {
    final XQuery query = new XQuery(
      "matches(format-dateTime(current-dateTime(), \"[M01]/[D01]/[Y0001] at [H01]:[m01]:[s01]\"), \"[0-1][0-9]/[0-3][0-9]/[0-9]{4} at [0-9]{2}:[0-9]{2}:[0-9]{2}\")",
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
        error("XPST0017")
      )
    );
  }

  /**
   *  No function by name fn:format-dateTime() exists in XQuery 1.0 (although it does exist in XSLT and XQuery 1.1). .
   */
  @org.junit.Test
  public void kFunctionCallExpr21() {
    final XQuery query = new XQuery(
      "matches(format-dateTime(current-dateTime(), \"[M01]/[D01]/[Y0001] at [H01]:[m01]:[s01]\", (), (), ()), \"../../.... at ..:..:..\")",
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
        error("XPST0017")
      )
    );
  }

  /**
   *  No function by name fn:current() exists(although one does in XSLT). .
   */
  @org.junit.Test
  public void kFunctionCallExpr22() {
    final XQuery query = new XQuery(
      "current()",
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
   *  No function by name fn:unparsed-entity-uri() exists(although one does in XSLT). .
   */
  @org.junit.Test
  public void kFunctionCallExpr23() {
    final XQuery query = new XQuery(
      "unparsed-entity-uri(\"example.com/file.ext\")",
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
   *  No function by name fn:unparsed-entity-public-id() exists(although one does in XSL-T). .
   */
  @org.junit.Test
  public void kFunctionCallExpr24() {
    final XQuery query = new XQuery(
      "unparsed-entity-public-id(\"entity\")",
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
   *  No function by name fn:generate-id() exists(although one does in XSLT). .
   */
  @org.junit.Test
  public void kFunctionCallExpr25() {
    final XQuery query = new XQuery(
      "generate-id(<a/>) castable as xs:NCName",
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
   *  Function by name fn:generate-id() does exist in XQuery 3.0. .
   */
  @org.junit.Test
  public void kFunctionCallExpr25a() {
    final XQuery query = new XQuery(
      "generate-id(<a/>) castable as xs:NCName",
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
   *  No function by name fn:system-property() exists(although one does in XSL-T). .
   */
  @org.junit.Test
  public void kFunctionCallExpr26() {
    final XQuery query = new XQuery(
      "system-property(\"xsl:vendor\")",
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
   *  No function by name fn:escape-uri() exists(although one did in older 'Functions & Operators' drafts). .
   */
  @org.junit.Test
  public void kFunctionCallExpr27() {
    final XQuery query = new XQuery(
      "fn:escape-uri(\"http:/example.com/\")",
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
   *  No function by name fn:sub-sequence() exists(although one by name fn:subsequence does). .
   */
  @org.junit.Test
  public void kFunctionCallExpr28() {
    final XQuery query = new XQuery(
      "fn:sub-sequence(\"http:/example.com/\", 1, 1)",
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
   *  A test whose essence is: `xs:func-does-not-exist(1, 2, 3)`. .
   */
  @org.junit.Test
  public void kFunctionCallExpr3() {
    final XQuery query = new XQuery(
      "xs:func-does-not-exist(1, 2, 3)",
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
   *  A test whose essence is: `local:func-does-not-exist(1, 2, 3)`. .
   */
  @org.junit.Test
  public void kFunctionCallExpr4() {
    final XQuery query = new XQuery(
      "local:func-does-not-exist(1, 2, 3)",
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
   *  A test whose essence is: `prefix-does-not-exist:func-does-not-exist(1, 2, 3)`. .
   */
  @org.junit.Test
  public void kFunctionCallExpr5() {
    final XQuery query = new XQuery(
      "prefix-does-not-exist:func-does-not-exist(1, 2, 3)",
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
   *  A function call containing an invalid QName as name. .
   */
  @org.junit.Test
  public void kFunctionCallExpr6() {
    final XQuery query = new XQuery(
      "f:f:()",
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
        error("XPST0081")
      )
    );
  }

  /**
   *  A function call containing an invalid QName as name. .
   */
  @org.junit.Test
  public void kFunctionCallExpr7() {
    final XQuery query = new XQuery(
      ":f()",
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
   *  A function call containing an invalid QName as name. .
   */
  @org.junit.Test
  public void kFunctionCallExpr8() {
    final XQuery query = new XQuery(
      ":f()",
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
   *  A function call containing an invalid QName as name. .
   */
  @org.junit.Test
  public void kFunctionCallExpr9() {
    final XQuery query = new XQuery(
      "1fd()",
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
   *  Two user functions with many arguments. .
   */
  @org.junit.Test
  public void k2FunctionCallExpr1() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:bar($c, $d, $e, $f, $g, $h, $i, $j, $a, $b) { 1 }; \n" +
      "        declare function local:moo($k) { $k }; \n" +
      "        local:moo(1)",
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
   *  Check that nodes, when passed through function arguments, have proper node identities. .
   */
  @org.junit.Test
  public void k2FunctionCallExpr10() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $a := <a/>; \n" +
      "        declare function local:testSingleNodeIdentity($node as node()) { $node is $node }; \n" +
      "        declare function local:testDoubleNodeIdentity($a as node(), $b as node()) { $a is $b }; \n" +
      "        local:testSingleNodeIdentity(<a/>), local:testDoubleNodeIdentity(<a/>, <b/>), local:testDoubleNodeIdentity($a, $a)\n" +
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
      assertStringValue(false, "true false true")
    );
  }

  /**
   *  A very simple string-difference function. .
   */
  @org.junit.Test
  public void k2FunctionCallExpr11() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:compare($arg1 as xs:string, $arg2 as xs:string) { \n" +
      "            let $cps1 := string-to-codepoints($arg1), \n" +
      "            $cps2 := string-to-codepoints($arg2) \n" +
      "            return abs(count($cps1) - count($cps2)) + sum(for $x in 1 to min((count($cps1), count($cps2))) \n" +
      "                                                          return if ($cps1[$x] ne $cps2[$x]) then 1 else ()) }; \n" +
      "        local:compare(\"\", \"\"), \n" +
      "        local:compare(\"a\", \"\"), \n" +
      "        local:compare(\"\", \"a\"), \n" +
      "        local:compare(\"a\", \"a\"), \n" +
      "        local:compare(\"\", \"aa\"), \n" +
      "        local:compare(\"aa\", \"ab\"), \n" +
      "        local:compare(\"ba\", \"ba\"), \n" +
      "        local:compare(\"bab\", \"bbb\"), \n" +
      "        local:compare(\"aba\", \"bab\")\n" +
      "     ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0 1 1 0 2 1 0 1 3")
    );
  }

  /**
   *  Trigger an infinite recursion in one implementation. .
   */
  @org.junit.Test
  public void k2FunctionCallExpr12() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:func1() { if(local:func2('b')) then 3 else local:func1() }; \n" +
      "        declare function local:func2($a) { if(matches(\"\",$a)) then () else 4 }; \n" +
      "        local:func1()",
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
   *  Have a function which recurses infintely, but which never is called. .
   */
  @org.junit.Test
  public void k2FunctionCallExpr13() {
    final XQuery query = new XQuery(
      "declare function local:foo($arg) { local:foo(local:foo(1)) }; 1",
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
   *  Function current-grouping-key() is not available in XQuery. .
   */
  @org.junit.Test
  public void k2FunctionCallExpr2() {
    final XQuery query = new XQuery(
      "current-grouping-key()",
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
   *  Function current() is not available in XQuery. .
   */
  @org.junit.Test
  public void k2FunctionCallExpr3() {
    final XQuery query = new XQuery(
      "current()",
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
   *  Function unparsed-entity-uri() is not available in XQuery. .
   */
  @org.junit.Test
  public void k2FunctionCallExpr4() {
    final XQuery query = new XQuery(
      "unparsed-entity-uri(\"str\")",
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
   *  Function unparsed-entity-public-id() is not available in XQuery. .
   */
  @org.junit.Test
  public void k2FunctionCallExpr5() {
    final XQuery query = new XQuery(
      "unparsed-entity-public-id(\"str\")",
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
   *  Function generate-id() is not available in XQuery. .
   */
  @org.junit.Test
  public void k2FunctionCallExpr6() {
    final XQuery query = new XQuery(
      "generate-id(\"str\")",
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
   *  Function generate-id() is not available in XQuery(#2). .
   */
  @org.junit.Test
  public void k2FunctionCallExpr7() {
    final XQuery query = new XQuery(
      "generate-id()",
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
   *  Function system-property() is not available in XQuery(#2). .
   */
  @org.junit.Test
  public void k2FunctionCallExpr8() {
    final XQuery query = new XQuery(
      "system-property(\"property\")",
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
   *  Function key() is not available in XQuery. .
   */
  @org.junit.Test
  public void k2FunctionCallExpr9() {
    final XQuery query = new XQuery(
      "key(\"id\")",
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
   *  Ensure sequence types are promoted correctly .
   */
  @org.junit.Test
  public void cbclPromotion001() {
    final XQuery query = new XQuery(
      "\n" +
      "        string-join( (xs:anyURI('http://www.microsoft.com'), xs:anyURI('http://www.google.com/')), ' ')\n" +
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
      assertStringValue(false, "http://www.microsoft.com http://www.google.com/")
    );
  }

  /**
   *  Test that type promotion occurs correctly for values passed to user-defined functions .
   */
  @org.junit.Test
  public void cbclPromotion002() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:index-of($seq as xs:double*, $item as xs:double) { for $x at $p in $seq return if ($x eq $item) then $p else () };\n" +
      "        declare function local:sequence($x as xs:integer) { (\"string\", 1, 2.0, xs:float(3))[$x] };\n" +
      "        local:index-of(for $x in (2,3,4) return local:sequence($x), 2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2")
    );
  }

  /**
   *  Test that type promotion occurs correctly for values returned from user-defined functions .
   */
  @org.junit.Test
  public void cbclPromotion003() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:index-of($seq as xs:integer*, $item as xs:integer?) as xs:float* { \n" +
      "          if (empty($item)) \n" +
      "            then -1\n" +
      "            else for $x at $p in $seq return if ($x eq $item) then $p else () \n" +
      "        };\n" +
      "        local:index-of(1 to 10, 3)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3")
    );
  }

  /**
   *  Test that type promotion occurs correctly for sequence values returned from user-defined functions .
   */
  @org.junit.Test
  public void cbclPromotion004() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f() as xs:double* { \n" +
      "          if (day-from-date(current-date()) < 32) then xs:integer(3) else -1\n" +
      "        };\n" +
      "        local:f() + 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "4")
    );
  }

  /**
   *  Test a function declared to return optional returning more than one value .
   */
  @org.junit.Test
  public void cbclPromotion005() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:index-of($seq, $item) as xs:double? { for $x at $p in $seq return if ($x eq $item) then $p else () };\n" +
      "        local:index-of((1, 2.0, xs:float(3), 2), 2)",
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
   *  Test a function taking optional argument being passed more than one .
   */
  @org.junit.Test
  public void cbclPromotion006() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($arg as xs:anyAtomicType?) { $arg };\n" +
      "        local:f(index-of((1,2,3,2),2))",
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
   *  Check that correct type constraint is inserted. .
   */
  @org.junit.Test
  public void cbclPromotion007() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($v as xs:double*) as xs:double+ { if (empty($v)) then 0 else $v };\n" +
      "        declare function local:g($v as xs:double*) as xs:double+ { local:f($v) };\n" +
      "        local:g((1,2,3))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2 3")
    );
  }

  /**
   * Check that reserved function name attribute is handled correctly. .
   */
  @org.junit.Test
  public void functionCallReservedFunctionNames001() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function local:attribute($arg) { fn:true() };\n" +
      "\tattribute(1)\n" +
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
   * Check that reserved function name comment is handled correctly. .
   */
  @org.junit.Test
  public void functionCallReservedFunctionNames002() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function local:comment($arg) { fn:true() };\n" +
      "\tcomment(1)\n" +
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
   * Check that reserved function name document-node is handled correctly. .
   */
  @org.junit.Test
  public void functionCallReservedFunctionNames003() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function local:document-node($arg) { fn:true() };\n" +
      "\tdocument-node(1)\n" +
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
   * Check that reserved function name element is handled correctly. .
   */
  @org.junit.Test
  public void functionCallReservedFunctionNames004() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function local:element($arg) { fn:true() };\n" +
      "\telement(1)\n" +
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
   * Check that reserved function name empty-sequence is handled correctly. .
   */
  @org.junit.Test
  public void functionCallReservedFunctionNames005() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function local:empty-sequence() { fn:true() };\n" +
      "\tempty-sequence()\n" +
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
   * Check that reserved function name if is handled correctly. .
   */
  @org.junit.Test
  public void functionCallReservedFunctionNames006() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function local:if() { fn:true() };\n" +
      "\tif()\n" +
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
   * Check that reserved function name item is handled correctly. .
   */
  @org.junit.Test
  public void functionCallReservedFunctionNames007() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function local:item($arg) { fn:true() };\n" +
      "\titem(1)\n" +
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
   * Check that reserved function name node is handled correctly. .
   */
  @org.junit.Test
  public void functionCallReservedFunctionNames008() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function local:node($arg) { fn:true() };\n" +
      "\tnode(1)\n" +
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
   * Check that reserved function name processing-instruction is handled correctly. .
   */
  @org.junit.Test
  public void functionCallReservedFunctionNames009() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function local:processing-instruction($arg) { fn:true() };\n" +
      "\tprocessing-instruction(1)\n" +
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
   * Check that reserved function name schema-attribute is handled correctly. .
   */
  @org.junit.Test
  public void functionCallReservedFunctionNames010() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function local:schema-attribute() { fn:true() };\n" +
      "\tschema-attribute()\n" +
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
   * Check that reserved function name schema-element is handled correctly. .
   */
  @org.junit.Test
  public void functionCallReservedFunctionNames011() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function local:schema-element() { fn:true() };\n" +
      "\tschema-element()\n" +
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
   * Check that reserved function name text is handled correctly. .
   */
  @org.junit.Test
  public void functionCallReservedFunctionNames012() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function local:text($arg) { fn:true() };\n" +
      "\ttext(1)\n" +
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
   * Check that reserved function name typeswitch is handled correctly. .
   */
  @org.junit.Test
  public void functionCallReservedFunctionNames013() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function local:typeswitch() { fn:true() };\n" +
      "\ttypeswitch()\n" +
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
   * Check that reserved function name function is handled correctly. .
   */
  @org.junit.Test
  public void functionCallReservedFunctionNames014() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function local:function() { fn:true() };\n" +
      "\tfunction()\n" +
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
   * Check that reserved function name namespace-node is handled correctly. .
   */
  @org.junit.Test
  public void functionCallReservedFunctionNames015() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function namespace-node($arg) { fn:true() };\n" +
      "\tnamespace-node(1)\n" +
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
   * Check that reserved function name switch is handled correctly. .
   */
  @org.junit.Test
  public void functionCallReservedFunctionNames016() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function local:switch() { fn:true() };\n" +
      "\tswitch()\n" +
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
   * Check that reserved function name function is handled correctly. .
   */
  @org.junit.Test
  public void functionCallReservedFunctionNames017() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function local:function() { fn:true() };\n" +
      "\tfunction()\n" +
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
   * Check that reserved function name namespace-node is handled correctly. .
   */
  @org.junit.Test
  public void functionCallReservedFunctionNames018() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function local:namespace-node($arg) { fn:true() };\n" +
      "\tnamespace-node(1)\n" +
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
   * Check that reserved function name switch is handled correctly. .
   */
  @org.junit.Test
  public void functionCallReservedFunctionNames019() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function local:switch() { fn:true() };\n" +
      "\tswitch()\n" +
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
}
