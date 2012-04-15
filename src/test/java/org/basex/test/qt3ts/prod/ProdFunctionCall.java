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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * Test that a sequence of untypedAtomic values is cast to the target type: user-defined function.
   *          Also tests XPath 3.0 inline functions, casting to list, and bang operator..
   */
  @org.junit.Test
  public void functionCall009() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($in as xs:decimal*) as xs:decimal {sum($in, 0.0)};\n" +
      "        local:f(xs:NMTOKENS('1 1.2 1.3 1.4')!xs:untypedAtomic(.))\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("\"0003\"")
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

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertBoolean(true)
      ||
        error("XPST0017")
      )
    );
  }

  /**
   *  No function by name fn:current() exists(although one does in XSL-T). .
   */
  @org.junit.Test
  public void kFunctionCallExpr22() {
    final XQuery query = new XQuery(
      "current()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  No function by name fn:unparsed-entity-uri() exists(although one does in XSL-T). .
   */
  @org.junit.Test
  public void kFunctionCallExpr23() {
    final XQuery query = new XQuery(
      "unparsed-entity-uri(\"example.com/file.ext\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }
}
