package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the FunctionDecl production.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdFunctionDecl extends QT3TestSet {

  /**
   *  The 'XPath Data Types' namespace is not reserved anymore, although it was in older drafts. .
   */
  @org.junit.Test
  public void kFunctionProlog1() {
    final XQuery query = new XQuery(
      "declare namespace test = \"http://www.w3.org/2005/xpath-datatypes\";\n" +
      "        declare function test:myFunction() { 1};\n" +
      "        1 eq 1",
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
   *  A user declared function whose return value simply doesn't match the return type. .
   */
  @org.junit.Test
  public void kFunctionProlog10() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunction() as xs:double { \"This is not a double, it's an xs:string.\" };\n" +
      "        local:myFunction()",
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
   *  A user declared function whose return value simply doesn't match the return type(#2). .
   */
  @org.junit.Test
  public void kFunctionProlog11() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunction() as item() { () };\n" +
      "        local:myFunction()",
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
   *  XQuery 1.0: Variable appearing after a function declaration is not in scope inside the function. .
   */
  @org.junit.Test
  public void kFunctionProlog12a() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:computeSum() { $myVariable };\n" +
      "        declare variable $myVariable := 1;\n" +
      "        1",
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
   *  XQuery 3.0: Variable appearing after a function declaration is in scope inside the function. .
   */
  @org.junit.Test
  public void kFunctionProlog12b() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:computeSum() { $myVariable };\n" +
      "        declare variable $myVariable := 1;\n" +
      "        1",
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
   *  Namespaces declarations appearing after a function declaration are not in scope inside the function. .
   */
  @org.junit.Test
  public void kFunctionProlog13() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:computeSum() { $prefix:myVariable };\n" +
      "        declare namespaces prefix = \"example.com/Anamespace\";\n" +
      "        1",
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
        error("XPST0081")
      ||
        error("XPST0003")
      )
    );
  }

  /**
   *  A user function which when run doesn't match the declared returned type. .
   */
  @org.junit.Test
  public void kFunctionProlog14() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunction() as xs:integer { subsequence((1, 2, \"a string\"), 3 ,1) };\n" +
      "        fn:boolean(local:myFunction())",
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
   *  A user function whose return type doesn't match the body, which can be statically inferred. .
   */
  @org.junit.Test
  public void kFunctionProlog15() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunction() as xs:anyURI { 1 };\n" +
      "        true()",
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
        error("XPTY0004")
      )
    );
  }

  /**
   *  When declaring a function, the paranteses must be present even though it doesn't have any arguments. .
   */
  @org.junit.Test
  public void kFunctionProlog16() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunction as xs:integer { 1 };\n" +
      "        true()",
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
   *  When declaring a function, the paranteses must be present even though it doesn't have any arguments. .
   */
  @org.junit.Test
  public void kFunctionProlog17() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunction { 1 };\n" +
      "        true()",
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
   *  Two user functions using global variables. .
   */
  @org.junit.Test
  public void kFunctionProlog18() {
    final XQuery query = new XQuery(
      "declare variable $var1 := 1;\n" +
      "        declare function local:func1() as xs:integer { $var1 };\n" +
      "        declare variable $var2 := 2;\n" +
      "        declare function local:func2() as xs:integer { $var2 };\n" +
      "        1 eq local:func1() and 2 eq local:func2()",
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
   *  A user function where all its arguments are unused. .
   */
  @org.junit.Test
  public void kFunctionProlog19() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:func1($a1, $a2, $a3) { 1 };\n" +
      "        local:func1(1, 2, 3)",
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
   *  A function cannot be declared in the 'http://www.w3.org/2005/xpath-functions' namespace. .
   */
  @org.junit.Test
  public void kFunctionProlog2() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function wrongNS() { 1 };\n" +
      "        1",
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
   *  User functions which in some implementations causes constant propagation combined with function versioning. .
   */
  @org.junit.Test
  public void kFunctionProlog20() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:func($choose, $whenTrue, $whenFalse) { if($choose) then $whenTrue else $whenFalse };\n" +
      "        local:func(true(), current-time(), current-date()) instance of xs:time and local:func(false(), current-time(), current-date()) instance of xs:date",
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
   *  User functions where the middle argument of three arguments is unused. .
   */
  @org.junit.Test
  public void kFunctionProlog21() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:func($a as xs:integer, $unused, $c as xs:integer) { $a + $c };\n" +
      "        local:func(1, 2, 3)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("4")
    );
  }

  /**
   *  User functions where the first argument of three arguments is unused. .
   */
  @org.junit.Test
  public void kFunctionProlog22() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:func($unused, $b as xs:integer, $c as xs:integer) { $b + $c };\n" +
      "        local:func(1, 2, 3)",
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
   *  User functions where the last argument of three arguments is unused. .
   */
  @org.junit.Test
  public void kFunctionProlog23() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:func($a as xs:integer, $b as xs:integer, $unused) { $a + $b };\n" +
      "        local:func(1, 2, 3)",
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
   *  A function cannot be declared in the 'http://www.w3.org/2005/xpath-functions' namespace. .
   */
  @org.junit.Test
  public void kFunctionProlog24() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function wrongNS() { 1 };\n" +
      "        1",
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
   *  A function and a variable can have the same name. .
   */
  @org.junit.Test
  public void kFunctionProlog25() {
    final XQuery query = new XQuery(
      "declare variable $local:myName := 1;\n" +
      "        declare function local:myName() as xs:integer { 1 };\n" +
      "        $local:myName eq local:myName()",
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
   *  A function declaration duplicated. .
   */
  @org.junit.Test
  public void kFunctionProlog26() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myName() { 1 };\n" +
      "        declare function local:myName() { 1 };\n" +
      "        1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0034")
    );
  }

  /**
   *  A function declaration duplicated; difference in return types is insignificant. .
   */
  @org.junit.Test
  public void kFunctionProlog27() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myName() as xs:integer { 1 };\n" +
      "        declare function local:myName() as xs:nonPositiveInteger { 1 };\n" +
      "        1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0034")
    );
  }

  /**
   *  A function declaration duplicated; difference in arguments types is insignificant. .
   */
  @org.junit.Test
  public void kFunctionProlog28() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myName($myvar as xs:integer) { 1 };\n" +
      "        declare function local:myName($myvar as xs:nonPositiveInteger) { 1 };\n" +
      "        1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0034")
    );
  }

  /**
   *  A function declaration duplicated; difference in arguments name is insignificant. .
   */
  @org.junit.Test
  public void kFunctionProlog29() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myName($myvar) { 1 };\n" +
      "        declare function local:myName($myvar2) { 1 };\n" +
      "        1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0034")
    );
  }

  /**
   *  A function cannot be declared in the 'http://www.w3.org/XML/1998/namespace' namespace. .
   */
  @org.junit.Test
  public void kFunctionProlog3() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function xml:wrongNS() { 1 };\n" +
      "        1",
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
   *  Overloading user functions based on arity. .
   */
  @org.junit.Test
  public void kFunctionProlog30() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myName($var as xs:integer) as xs:integer { $var };\n" +
      "        declare function local:myName() as xs:integer { 1 };\n" +
      "        (local:myName(4) - 3) eq local:myName()",
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
   *  One cannot declare a user function as fn:count. .
   */
  @org.junit.Test
  public void kFunctionProlog31() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function fn:count($var) { fn:count($var) };\n" +
      "        1",
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
   *  One cannot declare a user function as xs:gYear. .
   */
  @org.junit.Test
  public void kFunctionProlog32() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function xs:gYear($arg as xs:anyAtomicType?) as xs:gYear? { xs:gYear($arg) };\n" +
      "        1",
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
   *  A type error inside a function. .
   */
  @org.junit.Test
  public void kFunctionProlog33() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunction() { \"a string\" + 1 };\n" +
      "        true()",
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
        error("XPTY0004")
      )
    );
  }

  /**
   *  A call to a user declared function which almost is spelled correctly(capitalization wrong). .
   */
  @org.junit.Test
  public void kFunctionProlog34() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:MyFunction() { 1 };\n" +
      "        local:myFunction()",
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
   *  A call to a user declared function which almost is spelled correctly(#2). .
   */
  @org.junit.Test
  public void kFunctionProlog35() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:MyFunction() { 1 };\n" +
      "        local:myFunctionn()",
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
   *  The variable '$myArg' is in scope inside the function, but not in the query body. .
   */
  @org.junit.Test
  public void kFunctionProlog36() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:MyFunction($myArg) { 0 };\n" +
      "        $myArg",
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
   *  The variable '$myArg' is in scope inside one function, but not the other function. .
   */
  @org.junit.Test
  public void kFunctionProlog37() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:MyFunction($myArg) { 0 };\n" +
      "        declare function local:MyFunction2($myArg2) { $myArg };\n" +
      "        1",
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
   *  The variable '$myArg2' is in scope inside one function, but not the other function. .
   */
  @org.junit.Test
  public void kFunctionProlog38() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:MyFunction($myArg) { $myArg2 };\n" +
      "        declare function local:MyFunction2($myArg2) { 0 };\n" +
      "        1",
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
   *  Function arguments shadows global variables. .
   */
  @org.junit.Test
  public void kFunctionProlog39() {
    final XQuery query = new XQuery(
      "declare variable $local:myVar := 1;\n" +
      "        declare function local:myFunction($local:myVar) { $local:myVar };\n" +
      "        $local:myVar, local:myFunction(2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2")
    );
  }

  /**
   *  A function cannot be declared in the 'http://www.w3.org/2001/XMLSchema-instance' namespace. .
   */
  @org.junit.Test
  public void kFunctionProlog4() {
    final XQuery query = new XQuery(
      "declare namespace my = \"http://www.w3.org/2001/XMLSchema-instance\";\n" +
      "        declare function my:wrongNS() { 1 };\n" +
      "        1",
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
   *  Variables declared inside functions shadow function arguments. .
   */
  @org.junit.Test
  public void kFunctionProlog40() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunction($local:myVar as xs:integer) { for $local:myVar in ($local:myVar, 3) return $local:myVar };\n" +
      "        deep-equal(local:myFunction(1), (1, 3))",
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
   *  Type error in body of user function caused by the argument value. .
   */
  @org.junit.Test
  public void kFunctionProlog41() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunction($local:myVar) { $local:myVar + 1 };\n" +
      "        local:myFunction(1), local:myFunction(\"this will fail\")",
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
   *  Type error(cardinality) in return value of user function caused by the argument value. .
   */
  @org.junit.Test
  public void kFunctionProlog42() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunction($local:arg) as item() { 1, $local:arg };\n" +
      "        local:myFunction(()), local:myFunction(1)",
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
   *  Item type error in argument value. .
   */
  @org.junit.Test
  public void kFunctionProlog43() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunction($arg as item(), $arg2 as xs:integer) { $arg, $arg2 };\n" +
      "        local:myFunction(\"3\", \"3\")",
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
   *  Cardinality error in argument value. .
   */
  @org.junit.Test
  public void kFunctionProlog44() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunction($arg as item(), $arg2 as xs:integer) { $arg, $arg2 };\n" +
      "        local:myFunction(\"3\", ())",
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
   *  Item type error in argument value. xs:decimal doesn't match xs:integer. .
   */
  @org.junit.Test
  public void kFunctionProlog45() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunction($arg as item()?, $arg2 as xs:integer) { $arg, $arg2 };\n" +
      "        local:myFunction((), 4.1)",
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
   *  '1' doesn't match the empty-sequence(). .
   */
  @org.junit.Test
  public void kFunctionProlog46() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunction($arg as empty-sequence()) { $arg };\n" +
      "        local:myFunction(1)",
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
        error("XPST0005")
      )
    );
  }

  /**
   *  'empty-sequence()+' is syntactically invalid. .
   */
  @org.junit.Test
  public void kFunctionProlog47() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunction($arg as empty-sequence()+) { $arg };\n" +
      "        local:myFunction(())",
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
   *  'empty-sequence()? is syntactically invalid. .
   */
  @org.junit.Test
  public void kFunctionProlog48() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunction($arg as empty-sequence()?) { $arg };\n" +
      "        local:myFunction(())",
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
   *  It is valid to declare an argument to be of type empty-sequence(). .
   */
  @org.junit.Test
  public void kFunctionProlog49() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunction($arg as empty-sequence()) { $arg };\n" +
      "        empty(local:myFunction(()))",
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
   *  A function cannot be declared in the 'http://www.w3.org/2001/XMLSchema' namespace. .
   */
  @org.junit.Test
  public void kFunctionProlog5() {
    final XQuery query = new XQuery(
      "declare namespace my = \"http://www.w3.org/2001/XMLSchema\";\n" +
      "        declare function my:wrongNS() { 1 };\n" +
      "        1",
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
   *  empty-sequence() as return type, but body doesn't match when run. .
   */
  @org.junit.Test
  public void kFunctionProlog50() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunction($arg) as empty-sequence() { $arg };\n" +
      "        local:myFunction(1)",
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
   *  Too few arguments passed to a user function. .
   */
  @org.junit.Test
  public void kFunctionProlog51() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunction($arg) { $arg };\n" +
      "        local:myFunction()",
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
   *  empty-sequence() as return type, and a body containing fn:error(). .
   */
  @org.junit.Test
  public void kFunctionProlog52() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunction() as empty-sequence() { fn:error() };\n" +
      "        local:myFunction()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOER0000")
    );
  }

  /**
   *  A global variable referenced from inside a function. .
   */
  @org.junit.Test
  public void kFunctionProlog53() {
    final XQuery query = new XQuery(
      "declare namespace my = \"http://example.com/MyNamespace/\";\n" +
      "        declare variable $my:error-qname := QName(\"http:example.org/\", \"prefix:ncname\");\n" +
      "        declare function my:error($choice, $msg as xs:string) as empty-sequence() { if($choice) then error($my:error-qname, concat('No luck: ', $msg)) else () };\n" +
      "        empty((my:error(false(), \"msg\"), my:error(false(), \"The message\")))",
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
   *  A more realistic case involving fn:error(). .
   */
  @org.junit.Test
  public void kFunctionProlog54() {
    final XQuery query = new XQuery(
      "declare namespace my = \"http://example.com/MyNamespace/\";\n" +
      "        declare variable $my:error-qname := QName(\"http://example.com/MyErrorNS\", \"my:qName\");\n" +
      "        declare function my:error($msg as xs:string) as empty-sequence() { error($my:error-qname, concat('No luck: ', $msg)) };\n" +
      "        my:error(\"The message\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("*")
    );
  }

  /**
   *  A more realistic case involving fn:error(), #2. .
   */
  @org.junit.Test
  public void kFunctionProlog55() {
    final XQuery query = new XQuery(
      "declare namespace my = \"http://example.com/MyNamespace/\";\n" +
      "        declare variable $my:error-qname := QName(\"http://example.com/MyErrorNS\", \"my:qName\");\n" +
      "        declare function my:error($choice, $msg as xs:string) as empty-sequence() { if($choice) then error($my:error-qname, concat('No luck: ', $msg)) else () };\n" +
      "        my:error(false(), \"msg\"), my:error(true(), \"The message\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("*")
    );
  }

  /**
   *  A more realistic case involving fn:error(), #3. .
   */
  @org.junit.Test
  public void kFunctionProlog56() {
    final XQuery query = new XQuery(
      "declare namespace my = \"http://example.com/MyNamespace/\";\n" +
      "        declare variable $my:error-qname := QName(\"http://example.com/MyErrorNS\", \"my:qName\");\n" +
      "        declare function my:error($choice, $msg as xs:string) as empty-sequence() { if($choice) then error($my:error-qname, concat('No luck: ', $msg)) else () };\n" +
      "        empty((my:error(false(), \"msg\"), my:error(false(), \"The message\")))",
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
   *  The type 'none' isn't available to users. .
   */
  @org.junit.Test
  public void kFunctionProlog57() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:error() as none { 1 };\n" +
      "        local:error()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0051")
    );
  }

  /**
   *  The type 'none()' isn't available to users. .
   */
  @org.junit.Test
  public void kFunctionProlog58() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:error() as none() { 1 };\n" +
      "        local:error()",
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
        error("XPST0051")
      )
    );
  }

  /**
   *  A call to a user function where the argument in the callsite corresponding to an unused argument contains a type error. .
   */
  @org.junit.Test
  public void kFunctionProlog59() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunction($unusedArg) { true() };\n" +
      "        local:myFunction(1 + \"a string\")",
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
        error("XPTY0004")
      )
    );
  }

  /**
   *  Arguments in functions cannot have default values initialized with '='(or in any other way). .
   */
  @org.junit.Test
  public void kFunctionProlog6() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunction($arg = 1) {1};\n" +
      "        true()",
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
   *  An argument in a user function is not in scope in the query body. .
   */
  @org.junit.Test
  public void kFunctionProlog60() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunction($arg) { 1 };\n" +
      "        $arg",
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
   *  An argument in a user function is not in scope in the query body. .
   */
  @org.junit.Test
  public void kFunctionProlog61() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunction($arg, $arg2, $arg3) { 1 };\n" +
      "        $arg3",
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
   *  Check that a global variable is in scope despite an unused function being declared. .
   */
  @org.junit.Test
  public void kFunctionProlog62() {
    final XQuery query = new XQuery(
      "declare variable $my := 3;\n" +
      "        declare function local:myFunction($my, $arg2, $arg4) { 1 };\n" +
      "        $my eq 3",
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
   *  A function call that could be a call to a hypotehical user function. .
   */
  @org.junit.Test
  public void kFunctionProlog63() {
    final XQuery query = new XQuery(
      "local:myFunction(1)",
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
   *  A function call that could be a call to a hypothetical user function(#2). .
   */
  @org.junit.Test
  public void kFunctionProlog64() {
    final XQuery query = new XQuery(
      "declare namespace my = \"http://example.com/ANamespace\";\n" +
      "        my:function(1)",
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
   *  Since the return type is empty-sequence() it is a type error(XPTY0004) to pass its return value to anything which requires cardinality exactly-one. That the function's body is of type 'none', doesn't affect that, it only adapts to the declared return type. However, there's no constraints on what is reported first, so FOER0000 can also be issued. .
   */
  @org.junit.Test
  public void kFunctionProlog65() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunction() as empty-sequence() { fn:error() };\n" +
      "        QName(\"http://example.com/ANamespace\", local:myFunction())",
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
        error("FOER0000")
      )
    );
  }

  /**
   *  If static typing is in use, XPTY004 is issued since local:myFunction() has static type item()*. However, if the function is invoked FOER0000 is issued. .
   */
  @org.junit.Test
  public void kFunctionProlog66() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunction() { fn:error() };\n" +
      "        QName(\"http://example.com/ANamespace\", local:myFunction())",
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
        error("FOER0000")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  A function call that reminds of the range expression. .
   */
  @org.junit.Test
  public void kFunctionProlog67() {
    final XQuery query = new XQuery(
      "declare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "        declare function local:is() as xs:integer { 1 };\n" +
      "        is() eq 1",
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
   *  A call to a user function where the argument in the callsite corresponding to a used argument contains a type error. .
   */
  @org.junit.Test
  public void kFunctionProlog68() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunction($usedArg) { $usedArg };\n" +
      "        local:myFunction(1 + \"a string\")",
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
   *  Arguments in functions cannot have default values initialized with '='(or in any other way). .
   */
  @org.junit.Test
  public void kFunctionProlog7() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunction($arg = 1 as xs:integer) {1};\n" +
      "        true()",
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
   *  Arguments in functions cannot have default values initialized with ':='(or in any other way). .
   */
  @org.junit.Test
  public void kFunctionProlog8() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunction($arg := 1) {1};\n" +
      "        true()",
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
   *  Arguments in functions cannot have default values initialized with ':='(or in any other way). .
   */
  @org.junit.Test
  public void kFunctionProlog9() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunction($arg := 1 as xs:integer) {1};\n" +
      "        true()",
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
   *  Have two function callsites as arguments to 'eq'. .
   */
  @org.junit.Test
  public void k2FunctionProlog1() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunction($arg as xs:integer) as xs:integer { ((if($arg eq 1) then 1 else $arg - 1), current-time())[1] treat as xs:integer };\n" +
      "        local:myFunction(1) eq local:myFunction(2)",
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
   *  A function whose name is only '_' and is declared in the default namespace. .
   */
  @org.junit.Test
  public void k2FunctionProlog10() {
    final XQuery query = new XQuery(
      "declare default function namespace \"http://example.com\";\n" +
      "        declare function _() { 1 };\n" +
      "        _()",
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
   *  A function whose name is only '_'. .
   */
  @org.junit.Test
  public void k2FunctionProlog11() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:_() { 1 };\n" +
      "        local:_()",
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
   *  An argument requiring a node, but is passed an integer. .
   */
  @org.junit.Test
  public void k2FunctionProlog12() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:_($arg as node()) { $arg };\n" +
      "        local:_(1)",
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
   *  An argument requiring a node, but is passed an integer(#2). .
   */
  @org.junit.Test
  public void k2FunctionProlog13() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:_($arg as attribute()?) { 1 };\n" +
      "        local:_(1)",
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
        error("XPTY0004")
      )
    );
  }

  /**
   *  A focus doesn't propagate into function. .
   */
  @org.junit.Test
  public void k2FunctionProlog14() {
    final XQuery query = new XQuery(
      "       declare function local:myFunc() { e };\n" +
      "        <e/>/local:myFunc()/1",
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
   *  Use a range variable inside the assignment expression of a global variable. .
   */
  @org.junit.Test
  public void k2FunctionProlog15() {
    final XQuery query = new XQuery(
      "declare variable $var1 := let $var1 := 1 return 1;\n" +
      "        $var1 eq 1",
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
   *  Ensure three subsequent stars are parsed. .
   */
  @org.junit.Test
  public void k2FunctionProlog16() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunction() as item()* {***};\n" +
      "        1",
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
        error("XPDY0002")
      )
    );
  }

  /**
   *  Conversions are invoked when an element is returned from a function with declared return type xs:integer. .
   */
  @org.junit.Test
  public void k2FunctionProlog17() {
    final XQuery query = new XQuery(
      "       declare function local:func() as xs:integer { <e>1</e> };\n" +
      "        local:func()",
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
   *  Conversions are invoked when an attribute is returned from a function with declared return type xs:integer. .
   */
  @org.junit.Test
  public void k2FunctionProlog18() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:func() as xs:integer { attribute name {\"1\"} };\n" +
      "        local:func()",
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
   *  Conversions are invoked when a document is returned from a function with declared return type xs:integer. .
   */
  @org.junit.Test
  public void k2FunctionProlog19() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:func() as xs:integer { document {\"1\"} };\n" +
      "        local:func()",
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
   *  Have two function callsites as arguments to 'ne'. .
   */
  @org.junit.Test
  public void k2FunctionProlog2() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunction($arg as xs:integer) as xs:integer { ((if($arg eq 1) then 1 else $arg - 1), current-time())[1] treat as xs:integer };\n" +
      "        not(local:myFunction(1) ne local:myFunction(2))",
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
   *  Comments cannot be converted into xs:integer. .
   */
  @org.junit.Test
  public void k2FunctionProlog20() {
    final XQuery query = new XQuery(
      "       declare function local:func() as xs:integer { <!--1--> };\n" +
      "        local:func()",
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
   *  Processing instructions cannot be converted into xs:integer. .
   */
  @org.junit.Test
  public void k2FunctionProlog21() {
    final XQuery query = new XQuery(
      "       declare function local:func() as xs:integer { <?target 1?> };\n" +
      "        local:func()",
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
   *  Conversions are invoked when a text node is returned from a function with declared return type xs:integer. .
   */
  @org.junit.Test
  public void k2FunctionProlog22() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:func() as xs:integer? { text {\"1\"} };\n" +
      "        local:func()",
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
   *  Call a function that subsequently calls a recursive function. .
   */
  @org.junit.Test
  public void k2FunctionProlog23() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:recursiveFunction($i as xs:integer) as xs:integer { if($i eq 5) then $i else local:recursiveFunction($i + 1) };\n" +
      "        declare function local:proxy() as xs:integer { local:recursiveFunction(0) + 3 };\n" +
      "        local:proxy()",
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
   *  The empty string cannot be cast to an xs:boolean. .
   */
  @org.junit.Test
  public void k2FunctionProlog24() {
    final XQuery query = new XQuery(
      "       declare function local:distinct-nodes-stable ($arg as node()*) as xs:boolean* { for $a in $arg return $a };\n" +
      "        local:distinct-nodes-stable((<element1/>,<element2/>))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0001")
    );
  }

  /**
   *  A call site that has an operand that gets treated as an xs:integer. .
   */
  @org.junit.Test
  public void k2FunctionProlog25() {
    final XQuery query = new XQuery(
      "       declare function local:myFunc($recurse as xs:integer) { attribute {concat(\"name\", $recurse)} {()} , if ($recurse = 0) then () else local:myFunc($recurse - 1) };\n" +
      "        <e> { local:myFunc((2, current-time())[1] treat as xs:integer) } </e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e name2=\"\" name1=\"\" name0=\"\"/>", false)
    );
  }

  /**
   *  A type declaration whose body doesn't match. .
   */
  @org.junit.Test
  public void k2FunctionProlog26() {
    final XQuery query = new XQuery(
      "       declare function local:myFunc() as element(foo) { <bar/> };\n" +
      "        local:myFunc()",
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
   *  A type declaration whose body doesn't match(#2). .
   */
  @org.junit.Test
  public void k2FunctionProlog27() {
    final XQuery query = new XQuery(
      "       declare function local:myFunc() as attribute(foo) { <foo/> };\n" +
      "        local:myFunc()",
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
   *  Compute the levenshtein distance between strings. .
   */
  @org.junit.Test
  public void k2FunctionProlog28() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:levenshtein($arg1 as xs:string, $arg2 as xs:string) as xs:decimal { if(string-length($arg1) = 0) then string-length($arg2) else if(string-length($arg2) = 0) then string-length($arg1) else min((local:levenshtein(substring($arg1, 2), $arg2) + 1, local:levenshtein($arg1, substring($arg2, 2)) + 1, local:levenshtein(substring($arg1, 2), substring($arg2, 2)) + (if(substring($arg1, 1, 1) = substring($arg2, 1, 1)) then 0 else 1))) };\n" +
      "        local:levenshtein(\"a\", \"a\"), local:levenshtein(\"aa\", \"aa\"), local:levenshtein(\"aaa\", \"aaa\"), local:levenshtein(\"aa a\", \"aa a\"), local:levenshtein(\"a a a\", \"aaa\"), local:levenshtein(\"aaa\", \"a a a\"), local:levenshtein(\"aaa\", \"aaab\"), local:levenshtein(\"978\", \"abc\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0 0 0 0 2 2 1 3")
    );
  }

  /**
   *  Ensure the 'function' keyword is parsed correctly. .
   */
  @org.junit.Test
  public void k2FunctionProlog29() {
    final XQuery query = new XQuery(
      "function gt function",
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
   *  Have two function callsites as arguments to '='. .
   */
  @org.junit.Test
  public void k2FunctionProlog3() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunction($arg as xs:integer) as xs:integer { ((if($arg eq 1) then 1 else $arg - 1), current-time())[1] treat as xs:integer };\n" +
      "        local:myFunction(1) = local:myFunction(2)",
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
   *  Ensure an invalid value, reached through conversions, is reported as invalid. .
   */
  @org.junit.Test
  public void k2FunctionProlog30() {
    final XQuery query = new XQuery(
      "       declare function local:foo($arg) as xs:boolean { $arg };\n" +
      "        local:foo(<e/>)",
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
        error("FORG0001")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  Pass a sequence of mixed atomic values and nodes to an argument which has no type declared. .
   */
  @org.junit.Test
  public void k2FunctionProlog31() {
    final XQuery query = new XQuery(
      "       declare function local:foo($arg) as xs:boolean* { $arg };\n" +
      "        local:foo((<e>true</e>, true(), xs:untypedAtomic(\"false\"), false(), <e> true </e>))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "true true false false true")
    );
  }

  /**
   *  Pass a value which cannot be converted to the expected type. $arg doesn't have a type declared. .
   */
  @org.junit.Test
  public void k2FunctionProlog32() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:foo($arg ) as xs:boolean* { $arg };\n" +
      "        local:foo(current-date())",
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
   *  Mix in an atomic value which matches the type it's being converted to. $arg doesn't have a type declared. .
   */
  @org.junit.Test
  public void k2FunctionProlog33() {
    final XQuery query = new XQuery(
      "       declare function local:foo($arg ) as xs:boolean* { $arg };\n" +
      "        local:foo((<e>true</e>, true(), xs:untypedAtomic(\"false\"), false(), <e> true </e>))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "true true false false true")
    );
  }

  /**
   *  Call fn:true() and fn:false() and pass to a user function. .
   */
  @org.junit.Test
  public void k2FunctionProlog34() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:foo($arg) as xs:boolean* { $arg };\n" +
      "        local:foo((true(), xs:untypedAtomic(\"false\"))), local:foo((false(), xs:untypedAtomic(\"false\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "true false false false")
    );
  }

  /**
   *  Pass an untyped value. .
   */
  @org.junit.Test
  public void k2FunctionProlog35() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:foo($arg) as xs:boolean* { $arg };\n" +
      "        local:foo(xs:untypedAtomic(\"false\"))",
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
   *  Pass an untyped value(different cardinality). .
   */
  @org.junit.Test
  public void k2FunctionProlog36() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:foo($arg) as xs:boolean { $arg };\n" +
      "        local:foo(xs:untypedAtomic(\"false\"))",
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
   *  Mix in an atomic value which matches the type it's being converted to. $arg doesn't have a type declared. .
   */
  @org.junit.Test
  public void k2FunctionProlog37() {
    final XQuery query = new XQuery(
      "       declare function local:foo($arg ) as xs:boolean* { $arg };\n" +
      "        local:foo((<e>true</e>, true(), xs:untypedAtomic(\"false\"), false(), <e> true </e>))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "true true false false true")
    );
  }

  /**
   *  Trigger a crash in an implementation by having a user declared function with type, that has a text node constructor containing a call to a nonexisting function. .
   */
  @org.junit.Test
  public void k2FunctionProlog38() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:foo() as xs:boolean { text {local:doesNotExist()} };\n" +
      "        1",
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
   *  Use complex real-world case for function calls, with automatic conversion not needed. .
   */
  @org.junit.Test
  public void k2FunctionProlog39() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $A:=(<A>{local:functionA()}</A>);\n" +
      "        declare function local:functionA() as element() { <input>testing ...</input> };\n" +
      "        declare function local:functionB ( ) as xs:string { xs:string($A) };\n" +
      "        local:functionB()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "testing ...")
    );
  }

  /**
   *  Have two function callsites as arguments to '!='. .
   */
  @org.junit.Test
  public void k2FunctionProlog4() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunction($arg as xs:integer) as xs:integer { ((if($arg eq 1) then 1 else $arg - 1), current-time())[1] treat as xs:integer };\n" +
      "        not(local:myFunction(1) != local:myFunction(2))",
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
   *  Use complex real-world case for function calls, with automatic conversion. .
   */
  @org.junit.Test
  public void k2FunctionProlog40() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $A:=(<A>{local:functionA()}</A>);\n" +
      "        declare function local:functionA() as element() { <input>testing ...</input> };\n" +
      "        declare function local:functionB ( ) as xs:string { $A };\n" +
      "        local:functionB()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "testing ...")
    );
  }

  /**
   *  A function requiring xs:integer but is passed an xs:decimal. .
   */
  @org.junit.Test
  public void k2FunctionProlog5() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunction($arg as xs:integer) { $arg };\n" +
      "        local:myFunction(1.0)",
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
   *  A function requiring xs:integer as return value, but is passed xs:decimal. .
   */
  @org.junit.Test
  public void k2FunctionProlog6() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunction() as xs:integer { 1.0 };\n" +
      "        local:myFunction()",
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
   *  A declared return value invokes numeric promotion. .
   */
  @org.junit.Test
  public void k2FunctionProlog7() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunction() as xs:float { 4.0 };\n" +
      "        (current-time(), 1, 2, \"a string\", local:myFunction(), 4.0, xs:double(\"NaN\"), current-date())[5] instance of xs:float\n" +
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
   *  A recursive function stretching through several function calls. .
   */
  @org.junit.Test
  public void k2FunctionProlog8() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunction($arg as xs:integer) as xs:integer { if($arg eq 1) then $arg else local:myFunction3($arg - 1) };\n" +
      "        declare function local:myFunction2($arg as xs:integer) as xs:integer { local:myFunction($arg) };\n" +
      "        declare function local:myFunction3($arg as xs:integer) as xs:integer { local:myFunction2($arg) };\n" +
      "        local:myFunction3(3) eq 1",
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
   *  A recursive function multiplying a sequence. .
   */
  @org.junit.Test
  public void k2FunctionProlog9() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:multiplySequence($input as xs:integer*) as xs:integer { if (empty($input)) then 1 else $input[1] * local:multiplySequence($input[position() != 1]) };\n" +
      "        local:multiplySequence((1, 2, 3, 4, 5))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("120")
    );
  }

  /**
   *  check that XPST0005 is NOT a valid response .
   */
  @org.junit.Test
  public void cbclFunctionDecl001() {
    final XQuery query = new XQuery(
      "declare function local:nothing() as empty-sequence() { () }; empty(local:nothing())",
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
   *  Test passing a large sequence to a function. .
   */
  @org.junit.Test
  public void cbclFunctionDeclaration002() {
    final XQuery query = new XQuery(
      "declare function local:count($x) { count($x) }; local:count((1 to 100000, 1 to 100000))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "200000")
    );
  }

  /**
   * Check that reserved function name attribute is handled correctly. .
   */
  @org.junit.Test
  public void functionDeclReservedFunctionNames001() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function attribute() { fn:true() };\n" +
      "\tlocal:attribute()\n" +
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
   * Check that reserved function name attribute is handled correctly. .
   */
  @org.junit.Test
  public void functionDeclReservedFunctionNames002() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function attribute() { fn:true() };\n" +
      "\tlocal:attribute()\n" +
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
  public void functionDeclReservedFunctionNames003() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function comment() { fn:true() };\n" +
      "\tlocal:comment()\n" +
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
   * Check that reserved function name comment is handled correctly. .
   */
  @org.junit.Test
  public void functionDeclReservedFunctionNames004() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function comment() { fn:true() };\n" +
      "\tlocal:comment()\n" +
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
  public void functionDeclReservedFunctionNames005() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function document-node() { fn:true() };\n" +
      "\tlocal:document-node()\n" +
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
   * Check that reserved function name document-node is handled correctly. .
   */
  @org.junit.Test
  public void functionDeclReservedFunctionNames006() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function document-node() { fn:true() };\n" +
      "\tlocal:document-node()\n" +
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
  public void functionDeclReservedFunctionNames007() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function element() { fn:true() };\n" +
      "\tlocal:element()\n" +
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
   * Check that reserved function name element is handled correctly. .
   */
  @org.junit.Test
  public void functionDeclReservedFunctionNames008() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function element() { fn:true() };\n" +
      "\tlocal:element()\n" +
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
  public void functionDeclReservedFunctionNames009() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function empty-sequence() { fn:true() };\n" +
      "\tlocal:empty-sequence()\n" +
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
   * Check that reserved function name empty-sequence is handled correctly. .
   */
  @org.junit.Test
  public void functionDeclReservedFunctionNames010() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function empty-sequence() { fn:true() };\n" +
      "\tlocal:empty-sequence()\n" +
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
  public void functionDeclReservedFunctionNames011() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function function() { fn:true() };\n" +
      "\tlocal:function()\n" +
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
  public void functionDeclReservedFunctionNames012() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function function() { fn:true() };\n" +
      "\tlocal:function()\n" +
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
  public void functionDeclReservedFunctionNames013() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function if() { fn:true() };\n" +
      "\tlocal:if()\n" +
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
   * Check that reserved function name if is handled correctly. .
   */
  @org.junit.Test
  public void functionDeclReservedFunctionNames014() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function if() { fn:true() };\n" +
      "\tlocal:if()\n" +
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
  public void functionDeclReservedFunctionNames015() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function item() { fn:true() };\n" +
      "\tlocal:item()\n" +
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
   * Check that reserved function name item is handled correctly. .
   */
  @org.junit.Test
  public void functionDeclReservedFunctionNames016() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function item() { fn:true() };\n" +
      "\tlocal:item()\n" +
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
  public void functionDeclReservedFunctionNames017() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function namespace-node() { fn:true() };\n" +
      "\tlocal:namespace-node()\n" +
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
  public void functionDeclReservedFunctionNames018() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function namespace-node() { fn:true() };\n" +
      "\tlocal:namespace-node()\n" +
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
  public void functionDeclReservedFunctionNames019() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function node() { fn:true() };\n" +
      "\tlocal:node()\n" +
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
   * Check that reserved function name node is handled correctly. .
   */
  @org.junit.Test
  public void functionDeclReservedFunctionNames020() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function node() { fn:true() };\n" +
      "\tlocal:node()\n" +
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
  public void functionDeclReservedFunctionNames021() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function processing-instruction() { fn:true() };\n" +
      "\tlocal:processing-instruction()\n" +
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
   * Check that reserved function name processing-instruction is handled correctly. .
   */
  @org.junit.Test
  public void functionDeclReservedFunctionNames022() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function processing-instruction() { fn:true() };\n" +
      "\tlocal:processing-instruction()\n" +
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
  public void functionDeclReservedFunctionNames023() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function schema-attribute() { fn:true() };\n" +
      "\tlocal:schema-attribute()\n" +
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
   * Check that reserved function name schema-attribute is handled correctly. .
   */
  @org.junit.Test
  public void functionDeclReservedFunctionNames024() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function schema-attribute() { fn:true() };\n" +
      "\tlocal:schema-attribute()\n" +
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
  public void functionDeclReservedFunctionNames025() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function schema-element() { fn:true() };\n" +
      "\tlocal:schema-element()\n" +
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
   * Check that reserved function name schema-element is handled correctly. .
   */
  @org.junit.Test
  public void functionDeclReservedFunctionNames026() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function schema-element() { fn:true() };\n" +
      "\tlocal:schema-element()\n" +
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
  public void functionDeclReservedFunctionNames027() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function switch() { fn:true() };\n" +
      "\tlocal:switch()\n" +
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
  public void functionDeclReservedFunctionNames028() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function switch() { fn:true() };\n" +
      "\tlocal:switch()\n" +
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
  public void functionDeclReservedFunctionNames029() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function text() { fn:true() };\n" +
      "\tlocal:text()\n" +
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
   * Check that reserved function name text is handled correctly. .
   */
  @org.junit.Test
  public void functionDeclReservedFunctionNames030() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function text() { fn:true() };\n" +
      "\tlocal:text()\n" +
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
  public void functionDeclReservedFunctionNames031() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function typeswitch() { fn:true() };\n" +
      "\tlocal:typeswitch()\n" +
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
   * Check that reserved function name typeswitch is handled correctly. .
   */
  @org.junit.Test
  public void functionDeclReservedFunctionNames032() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare default function namespace \"http://www.w3.org/2005/xquery-local-functions\";\n" +
      "\tdeclare function typeswitch() { fn:true() };\n" +
      "\tlocal:typeswitch()\n" +
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
   *  Demonstrate function declaration in different combination .
   */
  @org.junit.Test
  public void functionDeclaration001() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace foo = \"http://www..oracle.com/xquery/test\";\n" +
      "        declare function foo:price ($b as element()) as element()* { $b/price };\n" +
      "        1\n" +
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
   *  Demonstrate function declaration in different combination .
   */
  @org.junit.Test
  public void functionDeclaration002() {
    final XQuery query = new XQuery(
      "        \n" +
      "        declare function local:foo($n as xs:integer) { <tr> {$n} </tr> };\n" +
      "        local:foo(4)\n" +
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
      assertSerialization("<tr>4</tr>", false)
    );
  }

  /**
   *  Demonstrate function declaration in different combination .
   */
  @org.junit.Test
  public void functionDeclaration003() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:price ($i as element()) as element()? { $i/price };\n" +
      "        for $j in /bib/book return local:price($j)",
      ctx);
    try {
      query.context(node(file("docs/bib.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<price>65.95</price><price>65.95</price><price>39.95</price><price>129.95</price>", false)
    );
  }

  /**
   *  Demonstrate function declaration in different combination .
   */
  @org.junit.Test
  public void functionDeclaration004() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:summary($emps as element(employee)*) as element(dept)* { \n" +
      "            for $d in distinct-values($emps/deptno) \n" +
      "            let $e := $emps[deptno = $d] \n" +
      "            return <dept> \n" +
      "                      <deptno>{$d}</deptno> \n" +
      "                      <headcount> {count($e)} </headcount> \n" +
      "                      <payroll> {sum($e/salary)} </payroll> \n" +
      "                   </dept> \n" +
      "        };\n" +
      "        local:summary(//employee[location = \"Denver\"])\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("op/union/acme_corp.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<dept><deptno>1</deptno><headcount>2</headcount><payroll>130000</payroll></dept><dept><deptno>2</deptno><headcount>1</headcount><payroll>80000</payroll></dept>", false)
    );
  }

  /**
   *  Demonstrate function declaration in different combination .
   */
  @org.junit.Test
  public void functionDeclaration005() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:mysum($i as xs:integer, $j as xs:integer) { let $j := $i + $j return $j };\n" +
      "        declare function local:invoke_mysum() { let $s := 1 for $d in (1,2,3,4,5) let $s := local:mysum($s, $d) return $s };\n" +
      "        local:invoke_mysum()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2 3 4 5 6")
    );
  }

  /**
   *  Demonstrate function declaration in different combination .
   */
  @org.junit.Test
  public void functionDeclaration006() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:mysum($i as xs:integer, $j as xs:integer) { let $j := $i + $j return $j };\n" +
      "        declare function local:invoke_mysum($st as xs:integer) { for $d in (1,2,3,4,5) let $st := local:mysum($d, $st) return $st };\n" +
      "        local:invoke_mysum(0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2 3 4 5")
    );
  }

  /**
   *  Demonstrate function declaration in different combination .
   */
  @org.junit.Test
  public void functionDeclaration007() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $a := 1;\n" +
      "        declare function local:foo($a as xs:integer) { \n" +
      "            if ($a > 100) then $a else let $a := $a + 1 return local:foo($a) };\n" +
      "        local:foo($a)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("101")
    );
  }

  /**
   *  Demonstrate function declaration in different combination .
   */
  @org.junit.Test
  public void functionDeclaration008() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace my = \"urn:foo\";\n" +
      "        declare function my:fact($n as xs:integer) as xs:integer { if ($n < 2) then 1 else $n * my:fact($n - 1) };\n" +
      "        declare variable $my:ten := my:fact(10);\n" +
      "        <table> { for $i in 1 to 10 return <tr> <td>10!/{$i}! = {$my:ten div my:fact($i)}</td> </tr> } </table>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<table><tr><td>10!/1! = 3628800</td></tr><tr><td>10!/2! = 1814400</td></tr><tr><td>10!/3! = 604800</td></tr><tr><td>10!/4! = 151200</td></tr><tr><td>10!/5! = 30240</td></tr><tr><td>10!/6! = 5040</td></tr><tr><td>10!/7! = 720</td></tr><tr><td>10!/8! = 90</td></tr><tr><td>10!/9! = 10</td></tr><tr><td>10!/10! = 1</td></tr></table>", false)
    );
  }

  /**
   *  Demonstrate function declaration in different combination .
   */
  @org.junit.Test
  public void functionDeclaration009() {
    final XQuery query = new XQuery(
      "       \n" +
      "        declare function local:fact($n as xs:integer) as xs:integer { if ($n < 2) then 1 else $n * local:fact(($n)-1) };\n" +
      "        <table> { for $i in 1 to 10 return <tr> <td>{$i}! = {local:fact($i)}</td> </tr> } </table>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<table><tr><td>1! = 1</td></tr><tr><td>2! = 2</td></tr><tr><td>3! = 6</td></tr><tr><td>4! = 24</td></tr><tr><td>5! = 120</td></tr><tr><td>6! = 720</td></tr><tr><td>7! = 5040</td></tr><tr><td>8! = 40320</td></tr><tr><td>9! = 362880</td></tr><tr><td>10! = 3628800</td></tr></table>", false)
    );
  }

  /**
   *  Demonstrate function declaration in different combination .
   */
  @org.junit.Test
  public void functionDeclaration010() {
    final XQuery query = new XQuery(
      "       \n" +
      "        declare function local:prnt($n as xs:integer,$n2 as xs:string, $n3 as xs:date, $n4 as xs:long, $n5 as xs:string, $n6 as xs:decimal) { if ($n < 2) then 1 else concat($n, \" \",$n2,\" \",$n3,\" \",$n4,\" \",$n5,\" \",$n6) };\n" +
      "        <table> { <td>Value is = {local:prnt(4,xs:string(\"hello\"),xs:date(\"2005-02-22\"), xs:long(5),xs:string(\"well\"),xs:decimal(1.2))}</td> } </table>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<table><td>Value is = 4 hello 2005-02-22 5 well 1.2</td></table>", false)
    );
  }

  /**
   *  Demonstrate function declaration - forward declaration .
   */
  @org.junit.Test
  public void functionDeclaration011() {
    final XQuery query = new XQuery(
      "       \n" +
      "        declare function local:fn1 ($n as xs:integer) as xs:integer { local:fn2($n) };\n" +
      "        declare function local:fn2 ($n as xs:integer) as xs:integer { if ($n = 1) then 1 else $n + local:fn1($n - 1) };\n" +
      "        local:fn1(4)",
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
   *  Demonstrate function declaration in different combination .
   */
  @org.junit.Test
  public void functionDeclaration012() {
    final XQuery query = new XQuery(
      "       \n" +
      "        declare function local:fn1 ($n as xs:integer) as xs:integer { local:fn2($n) };\n" +
      "        declare function local:fn2 ($n as xs:integer) as xs:integer { if ($n = 1) then 1 else $n + local:fn1($n - 1) };\n" +
      "        local:fn1(4)",
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
   *  Demonstrate function declaration in different combination .
   */
  @org.junit.Test
  public void functionDeclaration013() {
    final XQuery query = new XQuery(
      "       \n" +
      "        declare function local:foo2($i as xs:string) as xs:string {local:foo($i)};\n" +
      "        declare function local:foo($i as xs:string) as xs:string {$i};\n" +
      "        local:foo2(\"abc\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abc")
    );
  }

  /**
   *  Demonstrate function declaration in different combination .
   */
  @org.junit.Test
  public void functionDeclaration014() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:odd($x as xs:integer) as xs:boolean {if ($x = 0) then false() else local:even($x - 1)};\n" +
      "        declare function local:even($x as xs:integer) as xs:boolean {if ($x = 0) then true() else local:odd($x - 1)};\n" +
      "        local:even(4)",
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
   *  Demonstrate function declaration in different combination .
   */
  @org.junit.Test
  public void functionDeclaration015() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:odd($x as xs:integer) as xs:boolean {if ($x = 0) then false() else local:even($x - 1)};\n" +
      "        declare function local:even($x as xs:integer) as xs:boolean {if ($x = 0) then true() else local:odd($x - 1)};\n" +
      "        local:even(3)",
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
   *  Demonstrate function declaration in different combination .
   */
  @org.junit.Test
  public void functionDeclaration016() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:title($a_book as element()) { for $i in $a_book return $i/title };\n" +
      "        /bib/book/(local:title(.))",
      ctx);
    try {
      query.context(node(file("op/union/bib2.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<title>TCP/IP Illustrated</title><title>Advanced Programming in the Unix environment</title><title>Data on the Web</title><title>The Economics of Technology and Content for Digital TV</title>", false)
    );
  }

  /**
   *  Demonstrate function declaration in different combination .
   */
  @org.junit.Test
  public void functionDeclaration017() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default element namespace \"http://www.example.com/filesystem\";\n" +
      "        declare variable $v as xs:integer := 100;\n" +
      "        declare function local:udf1 ($CUSTNO as xs:integer) { <empty> {$CUSTNO*$v} </empty> };\n" +
      "        local:udf1(10)\n" +
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
      assertSerialization("<empty xmlns=\"http://www.example.com/filesystem\">1000</empty>", false)
    );
  }

  /**
   *  Demonstrate function declaration in different combination .
   */
  @org.junit.Test
  public void functionDeclaration018() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default element namespace \"http://www.example.com/filesystem\";\n" +
      "        declare function local:udf1 () { <empty> {10*10} </empty> };\n" +
      "        local:udf1 ()\n" +
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
      assertSerialization("<empty xmlns=\"http://www.example.com/filesystem\">100</empty>", false)
    );
  }

  /**
   *  Demonstrate function declaration in different combination .
   */
  @org.junit.Test
  public void functionDeclaration019() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default element namespace \"http://www.example.com/def\";\n" +
      "        declare namespace test=\"http://www.example.com/test\";\n" +
      "        declare namespace test2=\"http://www.example.com/test2\";\n" +
      "        declare function test:udf1() { <empty> {10*10} </empty> };\n" +
      "        declare function test2:udf1() { <empty/> };\n" +
      "        <A> {test:udf1()} {test2:udf1()} </A>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<A xmlns=\"http://www.example.com/def\"><empty>100</empty><empty/></A>", false)
    );
  }

  /**
   *  Demonstrate function declaration - overloading .
   */
  @org.junit.Test
  public void functionDeclaration020() {
    final XQuery query = new XQuery(
      "declare namespace foo = \"http://www..oracle.com/xquery/test\";\n" +
      "        declare function foo:price () as xs:integer+ { 100 };\n" +
      "        declare function foo:price ($z as xs:integer) as xs:integer+ { $z };\n" +
      "        declare function foo:price ($x as xs:integer, $y as xs:integer) as xs:integer+ { $x, $y };\n" +
      "        declare function foo:price ($x as xs:integer, $y as xs:integer, $z as xs:integer) as xs:integer+ { $x+$y+$z };\n" +
      "        foo:price(), foo:price(1), foo:price(2,3), foo:price(4,5,6)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "100 1 2 3 15")
    );
  }

  /**
   *  Demonstrate function declaration - negative tests .
   */
  @org.junit.Test
  public void functionDeclaration021() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:foo ($n as xs:integer) as xs:string { $n };\n" +
      "        local:foo(4)",
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
   *  Demonstrate function declaration - negative tests .
   */
  @org.junit.Test
  public void functionDeclaration022() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:foo ($m as xs:integer) { $m };\n" +
      "        declare function local:foo ($n as xs:integer) { $n };\n" +
      "        local:foo(4)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0034")
    );
  }

  /**
   *  Demonstrate function declaration - negative tests .
   */
  @org.junit.Test
  public void functionDeclaration023() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function foo ($n as xs:integer) { $n };\n" +
      "        foo(4)",
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
        error("XQST0045")
      ||
        error("XPST0017")
      )
    );
  }

  /**
   *  Demonstrate function declaration - negative tests .
   */
  @org.junit.Test
  public void functionDeclaration024() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:foo ($n as xs:integer, $n as xs:integer) { $n };\n" +
      "        local:foo(4, 1)",
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
   *  Function Declaration with no namespace. .
   */
  @org.junit.Test
  public void functionDeclaration025() {
    final XQuery query = new XQuery(
      "declare default function namespace \"\";\n" +
      "        declare function foo ($n as xs:integer, $m as xs:integer) { $n };\n" +
      "        foo(4, 1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0060")
    );
  }

  /**
   *  Function Declaration using global variable. A global node test, which uses the focus in the dynamic context. .
   */
  @org.junit.Test
  public void functionDeclaration026() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $global := node();\n" +
      "        declare function local:function() { exists($global) };\n" +
      "        local:function()",
      ctx);
    try {
      query.context(node(file("op/union/bib2.xml")));
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
}
