package org.basex.test.qt3ts.misc;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for features associated with higher order functions.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class MiscHigherOrderFunctions extends QT3TestSet {

  /**
   * A function item is a value that represents a function..
   */
  @org.junit.Test
  public void functionItem1() {
    final XQuery query = new XQuery(
      "concat#64 instance of function(*)",
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
   * A function item consists of the following information: The signature of the function item..
   */
  @org.junit.Test
  public void functionItem10() {
    final XQuery query = new XQuery(
      "(let $a := 92, $b := true() return function($c) { $a, $b, $c }) instance of function(item()*) as item()*",
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
   * A function item consists of the following information: The function implementation..
   */
  @org.junit.Test
  public void functionItem11() {
    final XQuery query = new XQuery(
      "(let $a := 92, $b := true() return function($c) { $a, $b, $c })((xs:QName(\"foo\"), 5.0e3))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("92, true(), fn:QName(\"\",\"foo\"), 5000")
    );
  }

  /**
   * function(item()) as item() is a subtype of function(*).
   */
  @org.junit.Test
  public void functionItem12() {
    final XQuery query = new XQuery(
      "function($a as item()) as item() { $a } instance of function(*)",
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
   * function(item()) as xs:integer is a subtype of function(item()) as item().
   */
  @org.junit.Test
  public void functionItem13() {
    final XQuery query = new XQuery(
      "function($a as item()) as xs:integer { $a } instance of function(item()) as item()",
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
   * function(xs:string) as item() is a subtype of function(item()) as item().
   */
  @org.junit.Test
  public void functionItem14() {
    final XQuery query = new XQuery(
      "function($a as item()) as item() { $a } instance of function(xs:string) as item()",
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
   * function(item()) as item() is not a subtype of function() as item().
   */
  @org.junit.Test
  public void functionItem15() {
    final XQuery query = new XQuery(
      "function($a as item()) as item() { $a } instance of function() as item()",
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
   * function(item()) as xs:integer is no a subtype of function(item(), item()) as item().
   */
  @org.junit.Test
  public void functionItem16() {
    final XQuery query = new XQuery(
      "function($a as item()) as xs:integer { $a } instance of function(item(), item()) as item()",
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
   * function(item()) as item() is not a subtype of function(xs:string) as item().
   */
  @org.junit.Test
  public void functionItem17() {
    final XQuery query = new XQuery(
      "function($a as xs:string) as item() { $a } instance of function(item()) as item()",
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
   * Function items can be invoked, which is the act of calling the function that the function item represents..
   */
  @org.junit.Test
  public void functionItem2() {
    final XQuery query = new XQuery(
      "string-join#1((\"a\", \"b\", \"c\", \"d\", \"e\", \"f\", \"g\", \"h\", \"i\", \"j\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abcdefghij")
    );
  }

  /**
   * Function items have no identity, cannot be compared, and have no serialization..
   */
  @org.junit.Test
  public void functionItem3() {
    final XQuery query = new XQuery(
      "string-join#1 is string-join#1",
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
   * Function items have no identity, cannot be compared, and have no serialization..
   */
  @org.junit.Test
  public void functionItem4() {
    final XQuery query = new XQuery(
      "string-join#1 eq string-join#1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOTY0013")
    );
  }

  /**
   * Function items have no identity, cannot be compared, and have no serialization..
   */
  @org.junit.Test
  public void functionItem5() {
    final XQuery query = new XQuery(
      "element a { avg#1 }",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQTY0105")
    );
  }

  /**
   * Function items have no identity, cannot be compared, and have no serialization..
   */
  @org.junit.Test
  public void functionItem6() {
    final XQuery query = new XQuery(
      "attribute a { avg#1 }",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOTY0013")
    );
  }

  /**
   * A function item consists of the following information: The set of variable values for the variables referenced by the function..
   */
  @org.junit.Test
  public void functionItem7() {
    final XQuery query = new XQuery(
      "(let $a := 92, $b := true() return function($c) { $a, $b, $c })(\"lala\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("92, true(), \"lala\"")
    );
  }

  /**
   * A function item consists of the following information: The name of the function as a xs:QName..
   */
  @org.junit.Test
  public void functionItem8() {
    final XQuery query = new XQuery(
      "function-name(function-name#1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("xs:QName(\"fn:function-name\")")
    );
  }

  /**
   * A function item consists of the following information: The name of the function as a xs:QName. This is potentially absent..
   */
  @org.junit.Test
  public void functionItem9() {
    final XQuery query = new XQuery(
      "function-name(let $a := 92, $b := true() return function($c) { $a, $b, $c })",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertCount(0)
    );
  }

  /**
   *  named function reference, user-defined function  .
   */
  @org.junit.Test
  public void hof001() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x as xs:integer) as xs:integer { $x + 1 }; \n" +
      "        let $f := local:f#1 return $f(2)",
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
   *  named function reference, user-defined function  .
   */
  @org.junit.Test
  public void hof002() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:f() as xs:integer { 42 }; \n" +
      "      \tdeclare function local:f($x as xs:integer) as xs:integer { $x + 1 }; \n" +
      "      \tlet $f := local:f#0 return $f()\n" +
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
      assertEq("42")
    );
  }

  /**
   *  named function reference, imported user-defined function  .
   */
  @org.junit.Test
  public void hof003() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace m=\"http://example.com/hof-003\"; \n" +
      "        let $f := m:f#1 return $f(17)",
      ctx);
    try {
      query.addModule("http://example.com/hof-003", file("misc/HigherOrderFunctions/module-hof-003.xq"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("18")
    );
  }

  /**
   *  named function reference, system function  .
   */
  @org.junit.Test
  public void hof004() {
    final XQuery query = new XQuery(
      "let $f := fn:round#1 return $f(1.2345)",
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
   *  named function reference, system function  .
   */
  @org.junit.Test
  public void hof005() {
    final XQuery query = new XQuery(
      "let $f := concat#8 return $f('a','b','c','d','e','f','g','h')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abcdefgh")
    );
  }

  /**
   *  named function reference, user-defined function, default function namespace  .
   */
  @org.junit.Test
  public void hof006() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare default function namespace \"http://example.com/hof-006\"; \n" +
      "      declare function g($x as xs:integer) as xs:integer { $x + 1 }; \n" +
      "      let $f := g#1 return $f(21)\n" +
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
   *  named function reference, constructor function, default namespace  .
   */
  @org.junit.Test
  public void hof007() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare default function namespace \"http://www.w3.org/2001/XMLSchema\"; \n" +
      "      let $f := date#1 return $f('2008-01-31')\n" +
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
      assertStringValue(false, "2008-01-31")
    );
  }

  /**
   *  named function reference, constructor function, non default namespace  .
   */
  @org.junit.Test
  public void hof008() {
    final XQuery query = new XQuery(
      "let $f := xs:date#1 return $f('2008-01-31')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2008-01-31")
    );
  }

  /**
   *  SequenceType function()  .
   */
  @org.junit.Test
  public void hof010() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:f($x as xs:integer) as xs:integer { $x + 1 }; \n" +
      "      let $f as function(*) := local:f#1 return $f(2)\n" +
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
   *  SequenceType function(x) as z  .
   */
  @org.junit.Test
  public void hof011() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:f($x as xs:integer) as xs:integer { $x + 3 }; \n" +
      "      let $f as function(xs:integer) as xs:integer := local:f#1 \n" +
      "      return $f(2)\n" +
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
   *  SequenceType function(x,y) as z  .
   */
  @org.junit.Test
  public void hof012() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:f($x as xs:integer, $y as xs:long) as xs:integer { $x + $y }; \n" +
      "      let $f as function(xs:integer, xs:long) as xs:integer := local:f#2 \n" +
      "      return $f(2, xs:long(5))\n" +
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
   *  Selecting from a sequence of functions  .
   */
  @org.junit.Test
  public void hof013() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:f($x as xs:integer) as xs:integer { $x + 3 }; \n" +
      "      declare function local:g($x as xs:integer) as xs:integer { $x + 4 }; \n" +
      "      declare function local:h($x as xs:integer) as xs:integer { $x + 5 }; \n" +
      "      let $f as (function(xs:integer) as xs:integer)* := (local:f#1, local:g#1, local:h#1) return $f[3](2)[1]\n" +
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
   *  Function returning a function  .
   */
  @org.junit.Test
  public void hof014() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:case($x as xs:boolean) as function(*) \n" +
      "      \t{ if ($x) then fn:upper-case#1 else fn:lower-case#1 }; \n" +
      "      local:case(true())(\"Mike\"), local:case(false())(\"Mike\")\n" +
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
      assertStringValue(false, "MIKE mike")
    );
  }

  /**
   *  Function returning a function  .
   */
  @org.junit.Test
  public void hof015() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:case($x as xs:boolean) as function(xs:string?) as xs:string \n" +
      "      \t{ if ($x) then fn:upper-case#1 else fn:lower-case#1 }; \n" +
      "      local:case(true())(\"Mike\"), local:case(false())(\"Mike\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "MIKE mike")
    );
  }

  /**
   *  Function expecting a function  .
   */
  @org.junit.Test
  public void hof016() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:case($x as function(*), $y as xs:string) as xs:string { $x($y) }; \n" +
      "      local:case(upper-case#1, \"Mike\"), local:case(lower-case#1, \"Mike\")\n" +
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
      assertStringValue(false, "MIKE mike")
    );
  }

  /**
   *  Function expecting a function, full signature  .
   */
  @org.junit.Test
  public void hof017() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:case($x as function(xs:string?) as xs:string, $y as xs:string) as xs:string { $x($y) }; \n" +
      "      local:case(upper-case#1, \"Mike\"), local:case(lower-case#1, \"Mike\")\n" +
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
      assertStringValue(false, "MIKE mike")
    );
  }

  /**
   *  Function expecting a function, caller supplies local function  .
   */
  @org.junit.Test
  public void hof018() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:scramble($x as function(xs:string) as xs:string, $y as xs:string) as xs:string \n" +
      "      \t{ $x($y) }; \n" +
      "      declare function local:rot13($x as xs:string) as xs:string \n" +
      "      \t{ translate($x, \"abcdefghijklmnopqrstuvwxyz\", \"nopqrstuvwxyzabcdefghijklm\") }; \n" +
      "      local:scramble(local:rot13#1, \"mike\")\n" +
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
      assertStringValue(false, "zvxr")
    );
  }

  /**
   *  Function expecting a function, caller supplies local function  .
   */
  @org.junit.Test
  public void hof019() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:scramble($x as function(*), $y as xs:string) as xs:string { $x($y) }; \n" +
      "      declare function local:rot13($x as xs:string) as xs:string { translate($x, \"abcdefghijklmnopqrstuvwxyz\", \"nopqrstuvwxyzabcdefghijklm\") }; \n" +
      "      local:scramble(local:rot13#1, \"mike\")\n" +
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
      assertStringValue(false, "zvxr")
    );
  }

  /**
   *  Function expecting a function, caller supplies inline function  .
   */
  @org.junit.Test
  public void hof020() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:scramble($x as function(*), $y as xs:string) as xs:string { $x($y) }; \n" +
      "      local:scramble(function($x){translate($x, \"abcdefghijklmnopqrstuvwxyz\", \"nopqrstuvwxyzabcdefghijklm\")}, \"john\")\n" +
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
      assertStringValue(false, "wbua")
    );
  }

  /**
   *  Function expecting a function, caller supplies inline function. Needs function coercion  .
   */
  @org.junit.Test
  public void hof021() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:scramble($x as function(xs:string) as xs:string, $y as xs:string) as xs:string { $x($y) }; \n" +
      "      local:scramble(function($x){translate($x, \"abcdefghijklmnopqrstuvwxyz\", \"nopqrstuvwxyzabcdefghijklm\")}, \"john\")\n" +
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
      assertStringValue(false, "wbua")
    );
  }

  /**
   *  Name and arity of a user-defined function  .
   */
  @org.junit.Test
  public void hof022() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:scramble($x as function(xs:string) as xs:string, $y as xs:string) as xs:string { $x($y) }; \n" +
      "      let $n := function-name(local:scramble#2) \n" +
      "      return (local-name-from-QName($n), namespace-uri-from-QName($n), function-arity(local:scramble#2))\n" +
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
      assertStringValue(false, "scramble http://www.w3.org/2005/xquery-local-functions 2")
    );
  }

  /**
   *  Name and arity of a system function  .
   */
  @org.junit.Test
  public void hof023() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $f := fn:function-name#1, $n := function-name($f) \n" +
      "        return (local-name-from-QName($n), namespace-uri-from-QName($n), function-arity($f))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "function-name http://www.w3.org/2005/xpath-functions 1")
    );
  }

  /**
   *  Name and arity of a constructor function  .
   */
  @org.junit.Test
  public void hof024() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $f := xs:dateTime#1, $n := function-name($f) \n" +
      "        return (local-name-from-QName($n), namespace-uri-from-QName($n), function-arity($f))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "dateTime http://www.w3.org/2001/XMLSchema 1")
    );
  }

  /**
   *  Name and arity of a concat function  .
   */
  @org.junit.Test
  public void hof025() {
    final XQuery query = new XQuery(
      "let $f := concat#123456, $n := function-name($f) \n" +
      "        return (local-name-from-QName($n), namespace-uri-from-QName($n), function-arity($f))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "concat http://www.w3.org/2005/xpath-functions 123456")
    );
  }

  /**
   *  Name and arity of an inline function  .
   */
  @org.junit.Test
  public void hof026() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $f := function($x as xs:string) as xs:string { upper-case($x) } \n" +
      "        let $n := function-name($f) \n" +
      "        return <a loc=\"{local-name-from-QName($n)}\" uri=\"{namespace-uri-from-QName($n)}\"\n" +
      "        \t\t\tarity=\"{function-arity($f)}\" eloc=\"{empty(local-name-from-QName($n))}\" euri=\"{empty(namespace-uri-from-QName($n))}\"/>\n" +
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
      assertSerialization("<a uri=\"\" loc=\"\" euri=\"true\" eloc=\"true\" arity=\"1\"/>", false)
    );
  }

  /**
   *  Curry a system function  .
   */
  @org.junit.Test
  public void hof027() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $f := fn:contains(?, \"e\") \n" +
      "        return for $s in (\"Mike\", \"John\", \"Dave\", \"Mary\", \"Jane\") return $f($s)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "true false true false true")
    );
  }

  /**
   *  Return an inline function from a user-defined function. Needs function coercion  .
   */
  @org.junit.Test
  public void hof028() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:splitter() as (function(xs:string) as xs:string*) { function($x as xs:string) { tokenize($x, '\\s') } }; \n" +
      "      string-join(local:splitter()(\"A nice cup of tea\"), '|')\n" +
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
      assertStringValue(false, "A|nice|cup|of|tea")
    );
  }

  /**
   *  Return an inline function that uses internal variables. Needs function coercion  .
   */
  @org.junit.Test
  public void hof029() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:splitter() as (function(xs:string) as xs:string+)? \n" +
      "      { function($x as xs:string) { for $i in tokenize($x, '\\s') return upper-case($i)} }; \n" +
      "      string-join(local:splitter()(\"A nice cup of tea\"), '|')\n" +
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
      assertStringValue(false, "A|NICE|CUP|OF|TEA")
    );
  }

  /**
   *  Return an inline function that uses global variables  .
   */
  @org.junit.Test
  public void hof030() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare variable $sep as xs:string := \"\\s\"; \n" +
      "      declare function local:splitter() as (function(xs:string) as xs:string*)? \n" +
      "      \t{ function($x as xs:string) { for $i in tokenize($x, $sep) return upper-case($i)} }; \n" +
      "      string-join(local:splitter()(\"A nice cup of tea\"), '|')\n" +
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
      assertStringValue(false, "A|NICE|CUP|OF|TEA")
    );
  }

  /**
   *  Return an inline function that uses local parameters  .
   */
  @org.junit.Test
  public void hof031() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:splitter($sep as xs:string) as (function(xs:string) as xs:string*) \n" +
      "      \t{ function($x as xs:string) { for $i in tokenize($x, $sep) return upper-case($i)} }; \n" +
      "      string-join(local:splitter(\"\\s\")(\"A nice cup of tea\"), '|')\n" +
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
      assertStringValue(false, "A|NICE|CUP|OF|TEA")
    );
  }

  /**
   *  Parenthesized expression in a function call  .
   */
  @org.junit.Test
  public void hof032() {
    final XQuery query = new XQuery(
      "(if (current-date() gt xs:date('2000-12-31')) then upper-case#1 else lower-case#1)(\"Mike\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "MIKE")
    );
  }

  /**
   *  Context item is a function item  .
   */
  @org.junit.Test
  public void hof033() {
    final XQuery query = new XQuery(
      "local-name-from-QName(function-name((upper-case#1, lower-case#1)[.(\"Mike\") = \"MIKE\"]))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "upper-case")
    );
  }

  /**
   *  ordered{} applied to a function item  .
   */
  @org.junit.Test
  public void hof034() {
    final XQuery query = new XQuery(
      "local-name-from-QName(function-name((upper-case#1, lower-case#1)[ordered{.}(\"Mike\") = \"MIKE\"]))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "upper-case")
    );
  }

  /**
   *  unordered{} applied to a function item  .
   */
  @org.junit.Test
  public void hof035() {
    final XQuery query = new XQuery(
      "local-name-from-QName(function-name((upper-case#1, lower-case#1)[ordered{.}(\"Mike\") = \"MIKE\"]))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "upper-case")
    );
  }

  /**
   *  Heterogeneous sequence of atomics and functions on rhs of "/"   .
   */
  @org.junit.Test
  public void hof036() {
    final XQuery query = new XQuery(
      "(<a b=\"3\"/>/(string(@b), upper-case#1, 17))[. instance of xs:anyAtomicType]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3 17")
    );
  }

  /**
   *  instance-of tests on user-defined function, varying the argument types - all true  .
   */
  @org.junit.Test
  public void hof037() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x as xs:long, $y as xs:NCName) as element(e) { \n" +
      "            <e x=\"{$x}\" y=\"{$y}\"/> \n" +
      "        }; \n" +
      "        local:f#2 instance of function(*), \n" +
      "        local:f#2 instance of function(xs:long, xs:NCName) as element(e), \n" +
      "        local:f#2 instance of function(xs:anyAtomicType?, xs:anyAtomicType?) as element(e), \n" +
      "        local:f#2 instance of function(item()*, item()*) as element(e)\n" +
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
      assertStringValue(false, "true true false false")
    );
  }

  /**
   *  instance-of tests on user-defined function, varying the argument types - all false  .
   */
  @org.junit.Test
  public void hof038() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x as xs:long?, $y as xs:NCName?) as element(e) { \n" +
      "            <e x=\"{$x}\" y=\"{$y}\"/> \n" +
      "        }; \n" +
      "        local:f#2 instance of function(xs:int?, xs:NCName?) as element(e), \n" +
      "        local:f#2 instance of function(xs:long?) as element(e), \n" +
      "        local:f#2 instance of function(xs:long?, xs:NCName?, item()*) as element(e), \n" +
      "        local:f#2 instance of function(xs:long, xs:anyAtomicType?) as element(e), \n" +
      "        local:f#2 instance of function(item()+, item()+) as element(e)\n" +
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
      assertStringValue(false, "true false false false false")
    );
  }

  /**
   *  instance-of tests on user-defined function, varying the result types  .
   */
  @org.junit.Test
  public void hof039() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:f($x as xs:long, $y as xs:NCName) as element(e)? { <e x=\"{$x}\" y=\"{$y}\"/> }; \n" +
      "      \tlocal:f#2 instance of function(xs:long, xs:NCName) as element()?, \n" +
      "      \tlocal:f#2 instance of function(xs:long, xs:NCName) as element()*, \n" +
      "      \tlocal:f#2 instance of function(xs:long, xs:NCName) as element(e)*, \n" +
      "      \tlocal:f#2 instance of function(xs:long, xs:NCName) as element(e, xs:anyType?)*, \n" +
      "      \tlocal:f#2 instance of function(xs:long, xs:NCName) as element(*, xs:anyType?)?, \n" +
      "      \tlocal:f#2 instance of function(xs:long, xs:NCName) as element(*, xs:untyped)?\n" +
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
      assertStringValue(false, "true true true true true false")
    );
  }

  /**
   *  Pass a sequence of functions that require coercion in different ways  .
   */
  @org.junit.Test
  public void hof040() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:apply($fns as (function(xs:string) as xs:string)*, $s as xs:string) as xs:string* { for $f in $fns return $f($s) }; \n" +
      "      let $ops := (upper-case#1, lower-case#1, function($x){translate($x, 'e', 'i')}, substring-before(?, ' ')) \n" +
      "      return string-join(local:apply($ops, 'Michael Kay'), '~')\n" +
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
      assertStringValue(false, "MICHAEL KAY~michael kay~Michail Kay~Michael")
    );
  }

  /**
   *  Return a sequence of functions that require coercion in different ways  .
   */
  @org.junit.Test
  public void hof041() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:ops() as (function(xs:string) as xs:string)*\n" +
      "      \t{ (upper-case#1, lower-case#1, function($x){translate($x, 'e', 'i')}, substring-before(?, ' ')) }; \n" +
      "      string-join(for $f in local:ops() return $f('Michael Kay'), '~')\n" +
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
      assertStringValue(false, "MICHAEL KAY~michael kay~Michail Kay~Michael")
    );
  }

  /**
   *  Implicit atomization works for various kinds of functions  .
   */
  @org.junit.Test
  public void hof042() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:lower-case($x as xs:string) as xs:string { concat(\"'\", fn:lower-case($x), \"'\") }; \n" +
      "        declare function local:ops() as (function(xs:string) as xs:string)* \n" +
      "        \t{ (upper-case#1, local:lower-case#1, function($x){translate($x, 'e', 'i')}, substring-before(?, ' ')) }; \n" +
      "        string-join(for $f in local:ops() return $f(<a name=\"Michael Kay\"/>/@name), '~')\n" +
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
      assertStringValue(false, "MICHAEL KAY~'michael kay'~Michail Kay~Michael")
    );
  }

  /**
   *  untypedAtomic conversion works for various kinds of functions  .
   */
  @org.junit.Test
  public void hof043() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:round($x as xs:double) as xs:double \n" +
      "      \t{ fn:floor($x) }; \n" +
      "      declare function local:ops() as (function(xs:double) as xs:double)* \n" +
      "      \t{ (abs#1, local:round#1, function($x){$x+1}, round-half-to-even(?, 2)) }; \n" +
      "      string-join(for $f in local:ops() return string($f(xs:untypedAtomic('123.456'))), '~')\n" +
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
      assertStringValue(false, "123.456~123~124.456~123.46")
    );
  }

  /**
   *  numeric promotion works for various kinds of functions  .
   */
  @org.junit.Test
  public void hof044() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:round($x as xs:double) as xs:double { fn:floor($x) }; \n" +
      "      declare function local:ops() as (function(xs:double) as xs:double)* \n" +
      "      \t{ (abs#1, local:round#1, function($x as xs:double){$x+1}, round-half-to-even(?, 2)) }; \n" +
      "      string-join(for $f in local:ops() return string(round-half-to-even($f(xs:decimal('123.456')), 4)), '~')\n" +
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
      assertStringValue(false, "123.456~123~124.456~123.46")
    );
  }

  /**
   * partial-apply supplying a function parameter .
   */
  @org.junit.Test
  public void hof045() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:round($x as xs:double, $algorithm as (function(xs:double) as xs:double)) as xs:double { $algorithm($x) }; \n" +
      "      declare variable $roundToCeiling := local:round(?, ceiling#1); $roundToCeiling(12.4)",
      ctx);
    try {
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
   * Return an inline function that uses inner and outer local variables.
   */
  @org.junit.Test
  public void hof046() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:splitter($x as xs:string) as (function() as xs:string*)* { \n" +
      "            for $sep in ('\\s', ',', '!') \n" +
      "            return function() { for $i in tokenize($x, $sep) return upper-case($i) } \n" +
      "        }; \n" +
      "        <out>{ \n" +
      "            for $f as function(*) in local:splitter(\"How nice! Thank you, I enjoyed that.\") \n" +
      "            return <tokens>{ for $t in $f() \n" +
      "                             return <t>{$t}</t> \n" +
      "                   }</tokens> \n" +
      "       }</out>\n" +
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
      assertSerialization("<out><tokens><t>HOW</t><t>NICE!</t><t>THANK</t><t>YOU,</t><t>I</t><t>ENJOYED</t><t>THAT.</t></tokens><tokens><t>HOW NICE! THANK YOU</t><t> I ENJOYED THAT.</t></tokens><tokens><t>HOW NICE</t><t> THANK YOU, I ENJOYED THAT.</t></tokens></out>", false)
    );
  }

  /**
   *  Nested inline functions referencing grandfather local variables .
   */
  @org.junit.Test
  public void hof047() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:splitter($x as xs:string) as (function() as xs:string*)* { \n" +
      "            for $sep in ('\\s', ',', '!') return function() { \n" +
      "                for $i in tokenize($x, $sep) return \n" +
      "                    let $f := function(){ concat($sep, ':', upper-case($i)) } \n" +
      "                    return $f() } \n" +
      "        }; \n" +
      "        <out>{ \n" +
      "            for $f as function(*) in local:splitter(\"How nice! Thank you, I enjoyed that.\") \n" +
      "            return <tokens>{ for $t in $f() \n" +
      "                             return <t>{$t}</t> }</tokens> \n" +
      "        }</out>\n" +
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
      assertSerialization("<out><tokens><t>\\s:HOW</t><t>\\s:NICE!</t><t>\\s:THANK</t><t>\\s:YOU,</t><t>\\s:I</t><t>\\s:ENJOYED</t><t>\\s:THAT.</t></tokens><tokens><t>,:HOW NICE! THANK YOU</t><t>,: I ENJOYED THAT.</t></tokens><tokens><t>!:HOW NICE</t><t>!: THANK YOU, I ENJOYED THAT.</t></tokens></out>", false)
    );
  }

  /**
   *  Forwards reference to a literal function item  .
   */
  @org.junit.Test
  public void hof048() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:do() as xs:integer { (local:f#1)(5) }; \n" +
      "        declare function local:f($x as xs:integer) as xs:integer { $x + 1 }; \n" +
      "        local:do()",
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
   *  true#0 and false# as literal function items  .
   */
  @org.junit.Test
  public void hof049() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:tf($i as xs:integer) as function () as xs:boolean { \n" +
      "            if ($i) then true#0 else false#0 \n" +
      "        }; \n" +
      "        <out>{(local:tf(0)(), local:tf(1)())}</out>\n" +
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
      assertSerialization("<out>false true</out>", false)
    );
  }

  /**
   *  Partial application of a literal function item  .
   */
  @org.junit.Test
  public void hof050() {
    final XQuery query = new XQuery(
      "let $f := fn:substring-before#2(?, '-') return <out>{$f('the-end-of-the-world')}</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<out>the</out>", false)
    );
  }

  /**
   *  Partial application of an inline function item  .
   */
  @org.junit.Test
  public void hof051() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $f := function($a as xs:string, $b as xs:string) { \n" +
      "            starts-with($a, $b) and ends-with($a, $b)}(?, 'a') \n" +
      "        return <out>{$f('abracadabra')}</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<out>true</out>", false)
    );
  }

  /**
   *  Repeated partial application of a function  .
   */
  @org.junit.Test
  public void hof052() {
    final XQuery query = new XQuery(
      "let $f := fn:concat#3(?, '*', ?) let $g := $f('[', ?) return <out>{$g(']')}</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<out>[*]</out>", false)
    );
  }

  /**
   *  instance-of tests on user-defined function, varying the result types  .
   */
  @org.junit.Test
  public void hof053() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:f($x as xs:long, $y as xs:NCName) as element(e, xs:anyAtomicType) { <e x=\"{$x}\" y=\"{$y}\"/> }; \n" +
      "      \tlocal:f#2 instance of function(xs:long, xs:NCName) as element(), \n" +
      "      \tlocal:f#2 instance of function(xs:long, xs:NCName) as element()+, \n" +
      "      \tlocal:f#2 instance of function(xs:long, xs:NCName) as element()?, \n" +
      "      \tlocal:f#2 instance of function(xs:long, xs:NCName) as element()*, \n" +
      "      \tlocal:f#2 instance of function(xs:long, xs:NCName) as element(e)*, \n" +
      "      \tlocal:f#2 instance of function(xs:long, xs:NCName) as element(e, xs:anyType?)*, \n" +
      "      \tlocal:f#2 instance of function(xs:long, xs:NCName) as element(*, xs:anyType?)?, \n" +
      "      \tlocal:f#2 instance of function(xs:long, xs:NCName) as element(e, xs:anyType)*, \n" +
      "      \tlocal:f#2 instance of function(xs:long, xs:NCName) as element(*, xs:anyType)?, \n" +
      "      \tlocal:f#2 instance of function(xs:long, xs:NCName) as element(*, xs:untyped)?\n" +
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
      assertStringValue(false, "true true true true true true true true true false")
    );
  }

  /**
   *  inline function literal, unknown user-defined function  .
   */
  @org.junit.Test
  public void hof901() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:f($x as xs:integer) as xs:integer { $x + 1 }; \n" +
      "      \tlet $f := local:g#1 return $f(2)",
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
   *  inline function literal, unknown user-defined function  .
   */
  @org.junit.Test
  public void hof902() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:f($x as xs:integer) as xs:integer { $x + 1 }; \n" +
      "      \tlet $f := local:f#3 return $f(2)\n" +
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
      error("XPST0017")
    );
  }

  /**
   *  inline function literal, unknown function  .
   */
  @org.junit.Test
  public void hof903() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:f($x as xs:integer) as xs:integer { $x + 1 }; \n" +
      "      \tlet $f := xs:date#2 return $f('2008-03-01')\n" +
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
      error("XPST0017")
    );
  }

  /**
   *  inline function literal, unknown function  .
   */
  @org.junit.Test
  public void hof904() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:f($x as xs:integer) as xs:integer { $x + 1 }; \n" +
      "      \tlet $f := concat#1 return $f('2008-03-01')\n" +
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
      error("XPST0017")
    );
  }

  /**
   *  apply string() to a function item  .
   */
  @org.junit.Test
  public void hof905() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:f($x as xs:integer) as xs:integer { $x + 1 }; \n" +
      "      \tstring(local:f#1)\n" +
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
      error("FOTY0014")
    );
  }

  /**
   *  apply data() to a function item  .
   */
  @org.junit.Test
  public void hof906() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:f($x as xs:integer) as xs:integer { $x + 1 }; \n" +
      "      \texists(data(local:f#1))\n" +
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
      error("FOTY0013")
    );
  }

  /**
   *  apply deep-equal() to a function item  .
   */
  @org.junit.Test
  public void hof907() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:f($x as xs:integer) as xs:integer { $x + 1 }; \n" +
      "      \tdeep-equal((1,2,3,4,local:f#1), (1,2,3,4,local:f#1))\n" +
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
      error("FOTY0015")
    );
  }

  /**
   *  atomize a function item implicitly  .
   */
  @org.junit.Test
  public void hof908() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:f($x as xs:integer) as xs:integer { $x + 1 }; \n" +
      "      \tlocal:f#1 eq 3\n" +
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
      error("FOTY0013")
    );
  }

  /**
   *  atomize a function item implicitly  .
   */
  @org.junit.Test
  public void hof909() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:f($x as xs:integer) as xs:integer { $x + 1 }; \n" +
      "      \tnumber(local:f#1)\n" +
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
      error("FOTY0013")
    );
  }

  /**
   *  In SequenceType syntax, Result type required if argument type given  .
   */
  @org.junit.Test
  public void hof910() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x as xs:integer) as xs:integer {\n" +
      "            $x + 1\n" +
      "        };\n" +
      "        let $f as function(xs:integer) := local:f#1\n" +
      "        return $f(3)\n" +
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
   *  Heterogeneous sequence on rhs of "/" .
   */
  @org.junit.Test
  public void hof911() {
    final XQuery query = new XQuery(
      "<a b=\"3\"/>/(@b, upper-case#1)",
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
   *  Pass a sequence of functions that cannot be coerced to the required type  .
   */
  @org.junit.Test
  public void hof912() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:apply($fns as (function(xs:string) as xs:string)*, $s as xs:string) as xs:string* \n" +
      "        { for $f in $fns return $f($s) };\n" +
      "        let $ops := (upper-case#1, lower-case#1, function($x){translate($x, 'e', 'i')}, \n" +
      "            substring-before(?, ' ', ?)) \n" +
      "        return string-join(local:apply($ops, 'Michael Kay'), '~')",
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
   *  Pass a sequence of functions that cannot be coerced to the required type  .
   */
  @org.junit.Test
  public void hof913() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:apply($fns as (function(xs:string) as xs:string)*, $s as xs:string) as xs:string* \n" +
      "        { for $f in $fns return $f($s) }; \n" +
      "        let $ops := (upper-case#1, lower-case#1, function($x){translate($x, 'e', 'i')}, \n" +
      "            string-length#1) \n" +
      "        return string-join(local:apply($ops, 'Michael Kay'), '~')",
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
   *  Pass a sequence of functions that cannot be coerced to the required type  .
   */
  @org.junit.Test
  public void hof914() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:apply($fns as (function(xs:string) as xs:string)*, $s as xs:string) as xs:string* \n" +
      "        { for $f in $fns return $f($s) }; \n" +
      "        let $ops := (upper-case#1, lower-case#1, function($x as xs:double){string($x)}) \n" +
      "        return string-join(local:apply($ops, 'Michael Kay'), '~')",
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
   *  partial-apply, argument number out of range  .
   */
  @org.junit.Test
  public void hof915() {
    final XQuery query = new XQuery(
      "let $ops := substring-before('abc', ' ', (), ?) return $ops('Michael Kay')",
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
   *  partial-apply, argument number out of range  .
   */
  @org.junit.Test
  public void hof916() {
    final XQuery query = new XQuery(
      "let $ops := substring-before(?, ?) return $ops('Michael Kay')",
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
   *  partial-apply, argument value invalid for target function  .
   */
  @org.junit.Test
  public void hof917() {
    final XQuery query = new XQuery(
      "let $ops := substring-before(?, 2) return $ops('Michael Kay')",
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
   *  partial-apply supplying an incorrect function parameter  .
   */
  @org.junit.Test
  public void hof918() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:round($x as xs:double, $algorithm as (function(xs:double) as xs:double)) as xs:double \n" +
      "        { $algorithm($x) }; \n" +
      "        declare variable $roundToCeiling := local:round(?, upper-case#1); \n" +
      "        $roundToCeiling(12.4)\n" +
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
   *  Was hof-044: test that numeric promotion works for various kinds of functions. But there's
   *       an error - the function item function($x as xs:float){$x+1} doesn't satisfy the required type
   *       (function(xs:double) as xs:double) because it doesn't accept a double as an argument.
   *       .
   */
  @org.junit.Test
  public void hof919() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:round($x as xs:double) as xs:double { fn:floor($x) }; \n" +
      "      declare function local:ops() as (function(xs:double) as xs:double)* \n" +
      "      \t{ (abs#1, local:round#1, function($x as xs:float){$x+1}, round-half-to-even(?, 2)) }; \n" +
      "      string-join(for $f in local:ops() return string(round-half-to-even($f(xs:decimal('123.456')), 4)), '~')\n" +
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
   * An inline function expression creates a function.
   */
  @org.junit.Test
  public void inlineFunction1() {
    final XQuery query = new XQuery(
      "function() { 5 } instance of function(*)",
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
   * The parameters of a function declaration are considered to be variables whose scope is the function body..
   */
  @org.junit.Test
  public void inlineFunction10() {
    final XQuery query = new XQuery(
      "function($a) { \"lala\", $a }, $a",
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
   * The parameters of a function declaration are considered to be variables whose scope is the function body..
   */
  @org.junit.Test
  public void inlineFunction11() {
    final XQuery query = new XQuery(
      "let $a := \"monkey\" return function($a) { \"lala\", $a }(\"gibbon\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"lala\", \"gibbon\"")
    );
  }

  /**
   * The parameters of a function declaration are considered to be variables whose scope is the function body..
   */
  @org.junit.Test
  public void inlineFunction11a() {
    final XQuery query = new XQuery(
      "function($a) { let $a := \"monkey\" return (\"lala\", $a) }(\"gibbon\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"lala\", \"monkey\"")
    );
  }

  /**
   * The parameters of a function declaration are considered to be variables whose scope is the function body..
   */
  @org.junit.Test
  public void inlineFunction12() {
    final XQuery query = new XQuery(
      "$a, function($a) { \"lala\", $a }",
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
   * It is a static error [err:XQST0039] for a function declaration to have more than one parameter with the same name..
   */
  @org.junit.Test
  public void inlineFunction12a() {
    final XQuery query = new XQuery(
      "function($a, $a) { \"lala\", $a }(\"gibbon\", \"monkey\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0039")
    );
  }

  /**
   * It is a static error [err:XQST0039] for a function declaration to have more than one parameter with the same name..
   */
  @org.junit.Test
  public void inlineFunction13() {
    final XQuery query = new XQuery(
      "function($local:foo, $local:bar, $local:foo) { \"lala\", $local:foo, $local:bar }(\"gibbon\", \"monkey\", \"ape\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0039")
    );
  }

  /**
   * It is a static error [err:XQST0039] for a function declaration to have more than one parameter with the same name..
   */
  @org.junit.Test
  public void inlineFunction14() {
    final XQuery query = new XQuery(
      "function($local:foo, $local:bar, $fn:foo) { \"lala\", $local:foo, $local:bar }(\"gibbon\", \"monkey\", \"ape\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"lala\", \"gibbon\", \"monkey\"")
    );
  }

  /**
   * It is a static error [err:XQST0039] for a function declaration to have more than one parameter with the same name..
   */
  @org.junit.Test
  public void inlineFunction15() {
    final XQuery query = new XQuery(
      "function($Q{http://local/}foo, $Q{http://local/}bar, $Q{http://local/}foo) { \n" +
      "              \"lala\", $Q{http://local/}foo, $Q{http://local/}bar }(\"gibbon\", \"monkey\", \"ape\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0039")
    );
  }

  /**
   * It is a static error [err:XQST0039] for a function declaration to have more than one parameter with the same name..
   */
  @org.junit.Test
  public void inlineFunction16() {
    final XQuery query = new XQuery(
      "function($Q{http://local/}foo, $Q{http://local/}bar, $fn:foo) { \n" +
      "               \"lala\", $Q{http://local/}foo, $Q{http://local/}bar }(\"gibbon\", \"monkey\", \"ape\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"lala\", \"gibbon\", \"monkey\"")
    );
  }

  /**
   * that represents an anonymous function.
   */
  @org.junit.Test
  public void inlineFunction2() {
    final XQuery query = new XQuery(
      "function-name(function() { 5 })",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertCount(0)
    );
  }

  /**
   * An inline function specifies the names and SequenceTypes of the parameters to the function, the SequenceType of the result, and the body of the function..
   */
  @org.junit.Test
  public void inlineFunction3() {
    final XQuery query = new XQuery(
      "function() as xs:integer { 5 }()",
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
   * An inline function specifies the names and SequenceTypes of the parameters to the function, the SequenceType of the result, and the body of the function..
   */
  @org.junit.Test
  public void inlineFunction4() {
    final XQuery query = new XQuery(
      "function($a as xs:integer) as xs:integer { $a + 5 }(3)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("8")
    );
  }

  /**
   * An inline function specifies the names and SequenceTypes of the parameters to the function, the SequenceType of the result, and the body of the function..
   */
  @org.junit.Test
  public void inlineFunction5() {
    final XQuery query = new XQuery(
      "function($a as xs:integer, $b as xs:double) as xs:double { $a * $b + 5 }(3, 2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("11")
    );
  }

  /**
   * If a function parameter is declared using a name but no type, its default type is item()*..
   */
  @org.junit.Test
  public void inlineFunction6() {
    final XQuery query = new XQuery(
      "function($a, $b as xs:double) as xs:double { $a * $b + 5 } instance of function(item()*, xs:double) as xs:double",
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
   * If a function parameter is declared using a name but no type, its default type is item()*..
   */
  @org.junit.Test
  public void inlineFunction7() {
    final XQuery query = new XQuery(
      "function($a as node()+, $b) as xs:double { $a * $b + 5 } instance of function(node(), item()*) as xs:double",
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
   * If the result type is omitted from a function declaration, its default result type is item()*..
   */
  @org.junit.Test
  public void inlineFunction8() {
    final XQuery query = new XQuery(
      "function($a as node()+) { $a + 5 } instance of function(node()) as item()*",
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
   * If the result type is omitted from a function declaration, its default result type is item()*..
   */
  @org.junit.Test
  public void inlineFunction9() {
    final XQuery query = new XQuery(
      "function() { true() } instance of function() as item()*",
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
.
   */
  @org.junit.Test
  public void xqhof1() {
    final XQuery query = new XQuery(
      "\n" +
      "declare namespace map = \"http://snelson.org.uk/functions/map\";\n" +
      "\n" +
      "declare function map:key($pair as function() as item()+) as item()\n" +
      "{\n" +
      "  $pair()[1]\n" +
      "};\n" +
      "\n" +
      "declare function map:value($pair as function() as item()+) as item()*\n" +
      "{\n" +
      "  subsequence($pair(), 2)\n" +
      "};\n" +
      "\n" +
      "declare function map:contains($map as (function() as item()+)*, $key as item())\n" +
      "  as xs:boolean\n" +
      "{\n" +
      "  map:process($map, $key, function($a) { true() }, false(),\n" +
      "    function($a) { () })\n" +
      "};\n" +
      "\n" +
      "declare function map:get($map as (function() as item()+)*, $key as item())\n" +
      "  as item()*\n" +
      "{\n" +
      "  map:process($map, $key, map:value#1, (), function($a) { () })\n" +
      "};\n" +
      "\n" +
      "declare function map:process(\n" +
      "  $map as (function() as item()+)*,\n" +
      "  $key as item(),\n" +
      "  $found as function(function() as item()+) as item()*,\n" +
      "  $notfound as item()*,\n" +
      "  $unused as function((function() as item()+)*) as item()*\n" +
      ") as item()*\n" +
      "{\n" +
      "  if(empty($map)) then $notfound\n" +
      "  else\n" +
      "\n" +
      "  let $length := count($map)\n" +
      "  let $middle := $length idiv 2 + 1\n" +
      "  let $pair := $map[$middle]\n" +
      "  let $pair_key := $pair()[1]\n" +
      "  return\n" +
      "    if($pair_key eq $key) then (\n" +
      "      $unused(subsequence($map, 1, $middle - 1)),\n" +
      "      $found($pair),\n" +
      "      $unused(subsequence($map, $middle + 1))\n" +
      "    )\n" +
      "    else if($pair_key gt $key) then (\n" +
      "      map:process(subsequence($map, 1, $middle - 1), $key,\n" +
      "        $found, $notfound, $unused),\n" +
      "      $unused(subsequence($map, $middle))\n" +
      "    )\n" +
      "    else (\n" +
      "      $unused(subsequence($map, 1, $middle)),\n" +
      "      map:process(subsequence($map, $middle + 1), $key,\n" +
      "        $found, $notfound, $unused)\n" +
      "    )\n" +
      "};\n" +
      "\n" +
      "declare function map:pair($key as item(), $value as item()*)\n" +
      "  as function() as item()+\n" +
      "{\n" +
      "  function() { $key, $value }\n" +
      "};\n" +
      "\n" +
      "declare function map:put(\n" +
      "  $map as (function() as item()+)*,\n" +
      "  $key as item(),\n" +
      "  $value as item()*\n" +
      ") as (function() as item()+)+\n" +
      "{\n" +
      "  let $pair := map:pair($key, $value)\n" +
      "  return\n" +
      "    map:process($map, $key, function($a) { $pair }, $pair,\n" +
      "      function($a) { $a })\n" +
      "};\n" +
      "\n" +
      "string-join(let $map := map:put(map:put(map:put(map:put(map:put(map:put((),\n" +
      "  \"a\", \"aardvark\"),\n" +
      "  \"z\", \"zebra\"),\n" +
      "  \"e\", (\"elephant\", \"eagle\")),\n" +
      "  \"o\", \"osterich\"),\n" +
      "  \"t\", \"terrapin\"),\n" +
      "  \"a\", \"antelope\")\n" +
      "return (\n" +
      "  map:get($map, \"o\"),\n" +
      "\n" +
      "  for $m in $map\n" +
      "  return concat(\"key: \", map:key($m), \", value: (\",\n" +
      "    string-join(map:value($m), \", \"), \")\"))\n" +
      ", \"\n" +
      "\")\n" +
      "",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("\"osterich\nkey: a, value: (antelope)\nkey: e, value: (elephant, eagle)\nkey: o, value: (osterich)\nkey: t, value: (terrapin)\nkey: z, value: (zebra)\"")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void xqhof10() {
    final XQuery query = new XQuery(
      "\n" +
      "         for $f in (concat(\"one \", ?, \" three\"), substring-before(\"one two three\", ?), matches(?, \"t.*o\"), xs:NCName(?))\n" +
      "         return $f(\"two\")\n" +
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
      assertDeepEq("\"one two three\", \"one \", true(), xs:NCName(\"two\")")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void xqhof11() {
    final XQuery query = new XQuery(
      "()(\"two\")",
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
.
   */
  @org.junit.Test
  public void xqhof12() {
    final XQuery query = new XQuery(
      "(concat(\"one \", ?, \" three\"), substring-before(\"one two three\", ?), matches(?, \"t.*o\"), xs:NCName(?))(\"two\")",
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
.
   */
  @org.junit.Test
  public void xqhof13() {
    final XQuery query = new XQuery(
      "\n" +
      "         let $f := function($a) { node-name(.), $a }\n" +
      "         return <a/>/$f(5)\n" +
      "      \n" +
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
      error("XPDY0002")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void xqhof14() {
    final XQuery query = new XQuery(
      "\n" +
      "         let $f := name#0\n" +
      "         return <a/>/$f()\n" +
      "      \n" +
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
      error("XPDY0002")
    );
  }

  /**
   * Test closure over context.
   */
  @org.junit.Test
  public void xqhof15() {
    final XQuery query = new XQuery(
      "\n" +
      "         let $f := <b/>/name#0\n" +
      "         return <a/>/$f()\n" +
      "      \n" +
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
      assertEq("\"b\"")
    );
  }

  /**
   * Test closure over context.
   */
  @org.junit.Test
  public void xqhof16() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare base-uri \"main\";\n" +
      "         import module namespace lib = \"lib\";\n" +
      "         \n" +
      "         lib:getfun()(),\n" +
      "         fn:static-base-uri#0(),\n" +
      "         fn:static-base-uri()\n" +
      "      ",
      ctx);
    try {
      query.addModule("lib", file("misc/HigherOrderFunctions/module-xqhof16.xq"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertQuery("fn:ends-with($result[1], \"lib\")")
      &&
        assertQuery("fn:ends-with($result[2], \"main\")")
      &&
        assertQuery("fn:ends-with($result[3], \"main\")")
      )
    );
  }

  /**
   * Test closure over context.
   */
  @org.junit.Test
  public void xqhof17() {
    final XQuery query = new XQuery(
      "\n" +
      "         import module namespace lib = \"lib\";\n" +
      "         \n" +
      "         <main/>/lib:getfun2()(),\n" +
      "         <main/>/name#0(),\n" +
      "         <main/>/name()\n" +
      "      ",
      ctx);
    try {
      query.addModule("lib", file("misc/HigherOrderFunctions/module-xqhof16.xq"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"lib\", \"main\", \"main\"")
    );
  }

  /**
   * Test closure over context.
   */
  @org.junit.Test
  public void xqhof18() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare base-uri \"main\";\n" +
      "         import module namespace lib = \"lib\";\n" +
      "         \n" +
      "         lib:getfun3()(xs:QName(\"fn:static-base-uri\"),0)(),\n" +
      "         function-lookup#2(xs:QName(\"fn:static-base-uri\"),0)(),\n" +
      "         function-lookup(xs:QName(\"fn:static-base-uri\"),0)()\n" +
      "      ",
      ctx);
    try {
      query.addModule("lib", file("misc/HigherOrderFunctions/module-xqhof16.xq"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertQuery("fn:ends-with($result[1], \"lib\")")
      &&
        assertQuery("fn:ends-with($result[2], \"main\")")
      &&
        assertQuery("fn:ends-with($result[3], \"main\")")
      )
    );
  }

  /**
   * Test closure over context.
   */
  @org.junit.Test
  public void xqhof19() {
    final XQuery query = new XQuery(
      "\n" +
      "         import module namespace lib = \"lib\";\n" +
      "         \n" +
      "         <main/>/lib:getfun3()(xs:QName(\"fn:name\"),0)(),\n" +
      "         <main/>/function-lookup#2(xs:QName(\"fn:name\"),0)(),\n" +
      "         <main/>/function-lookup(xs:QName(\"fn:name\"),0)()\n" +
      "      ",
      ctx);
    try {
      query.addModule("lib", file("misc/HigherOrderFunctions/module-xqhof16.xq"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"lib\", \"main\", \"main\"")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void xqhof2() {
    final XQuery query = new XQuery(
      "\n" +
      "         import module namespace func = \"http://snelson.org.uk/functions/functional\";\n" +
      "         \n" +
      "         let $f := func:curry(concat#5)\n" +
      "         return $f(\"foo\")(\" bar\")(\" baz\")(\" what's\")(\" next?\")\n" +
      "      ",
      ctx);
    try {
      query.addModule("http://snelson.org.uk/functions/functional", file("misc/HigherOrderFunctions/functional.xq"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("\"foo bar baz what's next?\"")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void xqhof3() {
    final XQuery query = new XQuery(
      "\n" +
      "import module namespace func = \"http://snelson.org.uk/functions/functional\";\n" +
      "\n" +
      "declare function local:fib2_aux($result as xs:integer, $next as xs:integer, $n) as xs:integer*\n" +
      "{\n" +
      "  if($n eq 0) then () else (\n" +
      "  $result, local:fib2_aux($next, $next + $result, $n - 1))\n" +
      "};\n" +
      "\n" +
      "declare function local:fib2($n) as xs:integer*\n" +
      "{\n" +
      "  local:fib2_aux(0, 1, $n)\n" +
      "};\n" +
      "\n" +
      "string-join(\n" +
      "for $a in subsequence(\n" +
      "\n" +
      "let $interleave := func:curry(map-pairs#3)(function($a, $b) { $a, $b })\n" +
      "let $enumerate := $interleave(0 to 49)\n" +
      "return\n" +
      "$enumerate(local:fib2(50))\n" +
      "\n" +
      ", 1, 100)\n" +
      "return string($a)\n" +
      ", \"\n" +
      "\")\n" +
      "      ",
      ctx);
    try {
      query.addModule("http://snelson.org.uk/functions/functional", file("misc/HigherOrderFunctions/functional.xq"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("\"0\n0\n1\n1\n2\n1\n3\n2\n4\n3\n5\n5\n6\n8\n7\n13\n8\n21\n9\n34\n10\n55\n11\n89\n12\n144\n13\n233\n14\n377\n15\n610\n16\n987\n17\n1597\n18\n2584\n19\n4181\n20\n6765\n21\n10946\n22\n17711\n23\n28657\n24\n46368\n25\n75025\n26\n121393\n27\n196418\n28\n317811\n29\n514229\n30\n832040\n31\n1346269\n32\n2178309\n33\n3524578\n34\n5702887\n35\n9227465\n36\n14930352\n37\n24157817\n38\n39088169\n39\n63245986\n40\n102334155\n41\n165580141\n42\n267914296\n43\n433494437\n44\n701408733\n45\n1134903170\n46\n1836311903\n47\n2971215073\n48\n4807526976\n49\n7778742049\"")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void xqhof4() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare function local:hof($s, $f as function(*)) {\n" +
      "           $f($s[1], $s[2])\n" +
      "         };\n" +
      "         \n" +
      "         local:hof(('1', '2'), concat#2)\n" +
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
      assertEq("\"12\"")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void xqhof5() {
    final XQuery query = new XQuery(
      "\n" +
      "         let $a := string-join(?, \"\")\n" +
      "         return $a((\"foo\", \"bar\", \"baz\"))\n" +
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
      assertEq("\"foobarbaz\"")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void xqhof6() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare function local:curry($f as function(item()*, item()*) as item()*) as function(item()*) as function(item()*) as item()*\n" +
      "         {\n" +
      "           function($a) { $f($a, ?) }\n" +
      "         };\n" +
      "         \n" +
      "         local:curry(substring-after#2)(\"foobar\")(\"foo\")\n" +
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
.
   */
  @org.junit.Test
  public void xqhof7() {
    final XQuery query = new XQuery(
      "concat#3(\"one\", \"two\")",
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
.
   */
  @org.junit.Test
  public void xqhof8() {
    final XQuery query = new XQuery(
      "concat#4(\"one\", ?, \"three\")",
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
.
   */
  @org.junit.Test
  public void xqhof9() {
    final XQuery query = new XQuery(
      "concat#2(\"one\", ?, \"three\")",
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
}
