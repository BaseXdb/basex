package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the VarDecl (variable declaration) production.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdVarDecl extends QT3TestSet {

  /**
   *  A variable declaration involving assignment and type declaration, and many comments. .
   */
  @org.junit.Test
  public void kInternalVariablesWith1() {
    final XQuery query = new XQuery(
      "declare(::)variable(::)$local:var(::)as(::)item((: :))* :=3(::);(::)1(::)eq(::)1",
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
   *  A namespace declaration must appear before a variable declaration, and its prefix is not inscope for the variable declaration. .
   */
  @org.junit.Test
  public void kInternalVariablesWith10() {
    final XQuery query = new XQuery(
      "declare variable $prefix:var1 := 2; declare namespace prefix = \"http://example.com/myNamespace\"; true()",
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
   *  One prolog variable initialized via another. .
   */
  @org.junit.Test
  public void kInternalVariablesWith11() {
    final XQuery query = new XQuery(
      "declare variable $var1 := 2; declare variable $var2 := $var1; $var2 eq 2",
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
   *  One prolog variable depending on a user function appearing after it. .
   */
  @org.junit.Test
  public void kInternalVariablesWith12() {
    final XQuery query = new XQuery(
      "declare variable $var1 := local:myFunc(); declare function local:myFunc() { 1 }; $var1 eq 1",
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
   *  One prolog variable depending on a user function, but where the user-specified types doesn't match. .
   */
  @org.junit.Test
  public void kInternalVariablesWith13() {
    final XQuery query = new XQuery(
      "declare variable $var1 as xs:string := local:myFunc(); declare function local:myFunc() as xs:integer { 1 }; $var1 eq 1",
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
   *  One prolog variable depending on a user function, but where the types doesn't match. .
   */
  @org.junit.Test
  public void kInternalVariablesWith14() {
    final XQuery query = new XQuery(
      "declare variable $var1 as xs:string := local:myFunc(); declare function local:myFunc() { 1 }; $var1 eq 1",
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
   *  One prolog variable depending on itself. .
   */
  @org.junit.Test
  public void kInternalVariablesWith15a() {
    final XQuery query = new XQuery(
      "declare variable $var1 := $var1; true()",
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
   *  One prolog variable depending on itself. .
   */
  @org.junit.Test
  public void kInternalVariablesWith15b() {
    final XQuery query = new XQuery(
      "declare variable $var1 := $var1; true()",
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
   *  A prolog variable having a circular dependency, stretching through many functions. .
   */
  @org.junit.Test
  public void kInternalVariablesWith16() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare variable $var := local:func1(); \n" +
      "      declare function local:func1() { local:func2() }; \n" +
      "      declare function local:func2() { local:func3() }; \n" +
      "      declare function local:func3() { local:func4() }; \n" +
      "      declare function local:func4() { $var }; \n" +
      "      boolean($var)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0054")
    );
  }

  /**
   * Test case based on K-InternalVariablesWith-16. XQuery 3.0 error code..
   */
  @org.junit.Test
  public void kInternalVariablesWith16a() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare variable $var := local:func1(); \n" +
      "      declare function local:func1() { local:func2() }; \n" +
      "      declare function local:func2() { local:func3() }; \n" +
      "      declare function local:func3() { local:func4() }; \n" +
      "      declare function local:func4() { $var }; \n" +
      "      boolean($var)",
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
   *  A prolog variable having a circular dependency, by having a variable reference in a call site argument. This is an error even though the variable isn't used, because implementations cannot skip reporting static errors. .
   */
  @org.junit.Test
  public void kInternalVariablesWith17() {
    final XQuery query = new XQuery(
      "declare variable $var := local:func1(); declare function local:func1() { local:func2($var) }; declare function local:func2($arg2) { 1 }; true()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0054")
    );
  }

  /**
   * Test case based on K-InternalVariablesWith-17. XQuery 3.0 error code..
   */
  @org.junit.Test
  public void kInternalVariablesWith17a() {
    final XQuery query = new XQuery(
      "declare variable $var := local:func1(); declare function local:func1() { local:func2($var) }; declare function local:func2($arg2) { 1 }; true()",
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
   *  A prolog variable having a circular dependency, by having a variable reference in a call site argument. .
   */
  @org.junit.Test
  public void kInternalVariablesWith18() {
    final XQuery query = new XQuery(
      "declare variable $var := local:func1(); declare function local:func1() { local:func2($var) }; declare function local:func2($arg2) { $arg2 }; $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0054")
    );
  }

  /**
   * Test case based on K-InternalVariablesWith-18 with XQuery 3.0 error code..
   */
  @org.junit.Test
  public void kInternalVariablesWith18a() {
    final XQuery query = new XQuery(
      "declare variable $var := local:func1(); declare function local:func1() { local:func2($var) }; declare function local:func2($arg2) { $arg2 }; $var",
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
   *  A prolog variable having a circular dependency, stretching through functions and variables. .
   */
  @org.junit.Test
  public void kInternalVariablesWith19() {
    final XQuery query = new XQuery(
      "declare variable $var2 := local:func1(); declare variable $var := ($var2 treat as xs:integer) + 1; declare function local:func1() { local:func2() }; declare function local:func2() { local:func3() }; declare function local:func3() { local:func4() }; declare function local:func4() { $var }; boolean($var)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0054")
    );
  }

  /**
   * Test case based on K-InternalVariablesWith-19 with XQuery 3.0 error code..
   */
  @org.junit.Test
  public void kInternalVariablesWith19a() {
    final XQuery query = new XQuery(
      "declare variable $var2 := local:func1(); declare variable $var := ($var2 treat as xs:integer) + 1; declare function local:func1() { local:func2() }; declare function local:func2() { local:func3() }; declare function local:func3() { local:func4() }; declare function local:func4() { $var }; boolean($var)",
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
   *  A variable declaration involving assignment, and many comments. .
   */
  @org.junit.Test
  public void kInternalVariablesWith2() {
    final XQuery query = new XQuery(
      "(::)declare(::)variable(::)$local:var(::):=(::)3;(::)1(::)eq(::)1(::)",
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
   *  A prolog variable having a circular dependency, stretching through functions and variables(#2). .
   */
  @org.junit.Test
  public void kInternalVariablesWith20() {
    final XQuery query = new XQuery(
      "declare variable $var := local:func1(); declare function local:func1() { local:func2() }; declare function local:func2() { local:func3() }; declare variable $var2 := local:func2(); declare function local:func3() { $var2 }; boolean($var)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0054")
    );
  }

  /**
   * Test case based on K-InternalVariablesWith-20 with XQuery 3.0 error code..
   */
  @org.junit.Test
  public void kInternalVariablesWith20a() {
    final XQuery query = new XQuery(
      "declare variable $var := local:func1(); declare function local:func1() { local:func2() }; declare function local:func2() { local:func3() }; declare variable $var2 := local:func2(); declare function local:func3() { $var2 }; boolean($var)",
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
   *  A variable declaration involving assignment and type declaration, demonstrating a parsing problem. .
   */
  @org.junit.Test
  public void kInternalVariablesWith21() {
    final XQuery query = new XQuery(
      "declare variable $local:var as item() *:=3; true()",
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
   *  A variable declaration whose source expression doesn't match the declared type, and where it typically is difficult to deduce statically. .
   */
  @org.junit.Test
  public void kInternalVariablesWith3() {
    final XQuery query = new XQuery(
      "declare variable $myVar as xs:integer := subsequence((1, 2, \"a string\"), 3, 1); $myVar eq 3",
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
   *  A variable declaration whose source expression doesn't match the declared type, and where it can be deduced statically. .
   */
  @org.junit.Test
  public void kInternalVariablesWith4() {
    final XQuery query = new XQuery(
      "declare variable $myVar as xs:gYear := 2006; true()",
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
   *  A variable declaration whose source expression doesn't match the declared type, and where it can be deduced statically. .
   */
  @org.junit.Test
  public void kInternalVariablesWith5() {
    final XQuery query = new XQuery(
      "declare variable $myVar as xs:gYear := 2006; $myVar",
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
   *  A prolog variable depending on a variable which is not in scope,
   *       and the variable is not used. .
   */
  @org.junit.Test
  public void kInternalVariablesWith6a() {
    final XQuery query = new XQuery(
      "declare variable $var1 := $var2; declare variable $var2 := 2; true()",
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
   *  A prolog variable depending on a variable which is (in XQuery 3.0) in scope,
   *       and the variable is not used. .
   */
  @org.junit.Test
  public void kInternalVariablesWith6b() {
    final XQuery query = new XQuery(
      "declare variable $var1 := $var2; declare variable $var2 := 2; true()",
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
   *  'declare variable' must be followed by '$'. .
   */
  @org.junit.Test
  public void kInternalVariablesWith7() {
    final XQuery query = new XQuery(
      "declare variable var1 := 1; 1",
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
   *  '=' cannot be used to assign values in 'declare variable', it must be ':='. .
   */
  @org.junit.Test
  public void kInternalVariablesWith8() {
    final XQuery query = new XQuery(
      "declare variable $var1 = 1; 1",
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
   *  A prolog variable depending on a variable which is not in scope. .
   */
  @org.junit.Test
  public void kInternalVariablesWith9a() {
    final XQuery query = new XQuery(
      "declare variable $var1 := $var2; declare variable $var2 := 2; $var1",
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
   *  A prolog variable depending on a variable which is (in XQuery 3.0) in scope. .
   */
  @org.junit.Test
  public void kInternalVariablesWith9b() {
    final XQuery query = new XQuery(
      "declare variable $var1 := $var2; declare variable $var2 := 2; $var1",
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
   *  The query contains a type error despite the 'treat as' declaration. .
   */
  @org.junit.Test
  public void k2InternalVariablesWith1() {
    final XQuery query = new XQuery(
      "declare variable $var1 as xs:string := 1 treat as item(); $var1",
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
   *  A variable depending on a recursive function. .
   */
  @org.junit.Test
  public void k2InternalVariablesWithout1() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $local:myVar := local:myFunction(); \n" +
      "        declare function local:myFunction() { local:myFunction(), 1, $local:myVar }; \n" +
      "        $local:myVar\n" +
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
      error("XQST0054")
    );
  }

  /**
   *  Function arguments shadow global variables. .
   */
  @org.junit.Test
  public void k2InternalVariablesWithout10() {
    final XQuery query = new XQuery(
      "declare variable $local:myVar := local:myFunc(3); declare function local:myFunc($local:myVar) { $local:myVar }; local:myFunc(6)",
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
   *  A variable initialized with a function that doesn't exist. .
   */
  @org.junit.Test
  public void k2InternalVariablesWithout11() {
    final XQuery query = new XQuery(
      "declare variable $local:myVar := local:thisFunctionDoesNotExist(); 1",
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
        error("XPST0017")
      )
    );
  }

  /**
   *  A variable initialized with a function that doesn't exist. .
   */
  @org.junit.Test
  public void k2InternalVariablesWithout12() {
    final XQuery query = new XQuery(
      "declare variable $local:myVar := local:thisFunctionDoesNotExist(); $local:myVar",
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
   *  A prolog containing 20 variable declarations. .
   */
  @org.junit.Test
  public void k2InternalVariablesWithout13() {
    final XQuery query = new XQuery(
      "declare variable $local:var1 := 1; declare variable $local:var2 := 2; declare variable $local:var3 := 3; declare variable $local:var4 := 4; declare variable $local:var5 := 5; declare variable $local:var6 := 6; declare variable $local:var7 := 7; declare variable $local:var8 := 8; declare variable $local:var9 := 9; declare variable $local:var10 := 10; declare variable $local:var11 := 11; declare variable $local:var12 := 12; declare variable $local:var13 := 13; declare variable $local:var14 := 14; declare variable $local:var15 := 15; declare variable $local:var16 := 16; declare variable $local:var17 := 17; declare variable $local:var18 := 18; declare variable $local:var19 := 19; declare variable $local:var20 := 20; deep-equal((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20), ($local:var1, $local:var2, $local:var3, $local:var4, $local:var5, $local:var6, $local:var7, $local:var8, $local:var9, $local:var10, $local:var11, $local:var12, $local:var13, $local:var14, $local:var15, $local:var16, $local:var17, $local:var18, $local:var19, $local:var20))",
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
   *  A focus doesn't propagate through variable references. .
   */
  @org.junit.Test
  public void k2InternalVariablesWithout14() {
    final XQuery query = new XQuery(
      "declare variable $myVar := <e>{nametest}</e>; <e/>/$myVar",
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
   *  Multiple assignments is invalid. .
   */
  @org.junit.Test
  public void k2InternalVariablesWithout15() {
    final XQuery query = new XQuery(
      "declare variable $var := 1 := 2; 3",
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
   * Test case based on K2-InterVariablesWithout-1 with XQuery 3.0 error code.
   */
  @org.junit.Test
  public void k2InternalVariablesWithout1a() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $local:myVar := local:myFunction();\n" +
      "        declare function local:myFunction() { local:myFunction(), 1, $local:myVar };\n" +
      "        $local:myVar\n" +
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
   *  A variable depending on a recursive function. .
   */
  @org.junit.Test
  public void k2InternalVariablesWithout2() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $local:myVar := local:myFunction(); \n" +
      "        declare function local:myFunction() { $local:myVar, 1, local:myFunction() }; \n" +
      "        $local:myVar\n" +
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
      error("XQST0054")
    );
  }

  /**
   * Test case based on K2-InterVariablesWithout-2 with XQuery 3.0 error code.
   */
  @org.junit.Test
  public void k2InternalVariablesWithout2a() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $local:myVar := local:myFunction();\n" +
      "        declare function local:myFunction() { $local:myVar, 1, local:myFunction() };\n" +
      "        $local:myVar\n" +
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
   *  A variable depending on a recursive function. .
   */
  @org.junit.Test
  public void k2InternalVariablesWithout3() {
    final XQuery query = new XQuery(
      "declare variable $local:myVar := local:myFunction(); declare function local:myFunction() { $local:myVar, 1, local:myFunction() }; $local:myVar",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0054")
    );
  }

  /**
   * Test case based on K2-InterVariablesWithout-3 with XQuery 3.0 error code.
   */
  @org.junit.Test
  public void k2InternalVariablesWithout3a() {
    final XQuery query = new XQuery(
      "declare variable $local:myVar := local:myFunction(); declare function local:myFunction() { $local:myVar, 1, local:myFunction() }; $local:myVar",
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
   *  A variable depending on a recursive function. .
   */
  @org.junit.Test
  public void k2InternalVariablesWithout4() {
    final XQuery query = new XQuery(
      "declare variable $local:myVar := local:myFunction(); declare function local:myFunction() { $local:myVar, 1, local:myFunction() }; $local:myVar",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0054")
    );
  }

  /**
   * Test case based on K2-InterVariablesWithout-4 with XQuery 3.0 error code.
   */
  @org.junit.Test
  public void k2InternalVariablesWithout4a() {
    final XQuery query = new XQuery(
      "declare variable $local:myVar := local:myFunction(); declare function local:myFunction() { $local:myVar, 1, local:myFunction() }; $local:myVar",
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
   *  A variable depending indirectly on a recursive function. .
   */
  @org.junit.Test
  public void k2InternalVariablesWithout5() {
    final XQuery query = new XQuery(
      "declare variable $local:myVar := local:myFunction(); declare function local:myFunction2() { $local:myVar, 1, local:myFunction() }; declare function local:myFunction() { local:myFunction2() }; $local:myVar",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0054")
    );
  }

  /**
   * Test case based on K2-InterVariablesWithout-5 with XQuery 3.0 error code.
   */
  @org.junit.Test
  public void k2InternalVariablesWithout5a() {
    final XQuery query = new XQuery(
      "declare variable $local:myVar := local:myFunction(); declare function local:myFunction2() { $local:myVar, 1, local:myFunction() }; declare function local:myFunction() { local:myFunction2() }; $local:myVar",
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
   *  A variable depending indirectly on a recursive function. .
   */
  @org.junit.Test
  public void k2InternalVariablesWithout6() {
    final XQuery query = new XQuery(
      "declare variable $local:myVar := local:myFunction(); declare function local:myFunction2() { local:myFunction(), $local:myVar }; declare function local:myFunction() { local:myFunction2() }; local:myFunction()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0054")
    );
  }

  /**
   * Test case based on K2-InterVariablesWithout-6 with XQuery 3.0 error code.
   */
  @org.junit.Test
  public void k2InternalVariablesWithout6a() {
    final XQuery query = new XQuery(
      "declare variable $local:myVar := local:myFunction(); declare function local:myFunction2() { local:myFunction(), $local:myVar }; declare function local:myFunction() { local:myFunction2() }; local:myFunction()",
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
   *  A variable depending indirectly on a recursive function. .
   */
  @org.junit.Test
  public void k2InternalVariablesWithout7() {
    final XQuery query = new XQuery(
      "declare variable $local:myVar := local:myFunction(); declare function local:myFunction2() { local:myFunction(), $local:myVar }; declare function local:myFunction4() { local:myFunction2() }; declare function local:myFunction3() { local:myFunction4() }; declare function local:myFunction() { local:myFunction3() }; local:myFunction()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0054")
    );
  }

  /**
   * Test case based on K2-InterVariablesWithout-7 with XQuery 3.0 error code.
   */
  @org.junit.Test
  public void k2InternalVariablesWithout7a() {
    final XQuery query = new XQuery(
      "declare variable $local:myVar := local:myFunction(); declare function local:myFunction2() { local:myFunction(), $local:myVar }; declare function local:myFunction4() { local:myFunction2() }; declare function local:myFunction3() { local:myFunction4() }; declare function local:myFunction() { local:myFunction3() }; local:myFunction()",
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
   *  A variable depending indirectly on a recursive function. .
   */
  @org.junit.Test
  public void k2InternalVariablesWithout8() {
    final XQuery query = new XQuery(
      "declare variable $local:myVar := local:myFunction(); declare function local:myFunction2() { local:myFunction4() }; declare function local:myFunction4() { local:myFunction2(), $local:myVar }; declare function local:myFunction3() { local:myFunction4() }; declare function local:myFunction() { local:myFunction3() }; local:myFunction()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0054")
    );
  }

  /**
   * Test case based on K2-InterVariablesWithout-8 with XQuery 3.0 error code.
   */
  @org.junit.Test
  public void k2InternalVariablesWithout8a() {
    final XQuery query = new XQuery(
      "declare variable $local:myVar := local:myFunction(); declare function local:myFunction2() { local:myFunction4() }; declare function local:myFunction4() { local:myFunction2(), $local:myVar }; declare function local:myFunction3() { local:myFunction4() }; declare function local:myFunction() { local:myFunction3() }; local:myFunction()",
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
   *  A variable depending on its self through the argument of a user function callsite. .
   */
  @org.junit.Test
  public void k2InternalVariablesWithout9() {
    final XQuery query = new XQuery(
      "declare variable $local:myVar := local:myFunc(3); declare function local:myFunc($arg) { local:myFunc($local:myVar) }; $local:myVar",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0054")
    );
  }

  /**
   * Test case based on K2-InterVariablesWithout-9 with XQuery 3.0 error code.
   */
  @org.junit.Test
  public void k2InternalVariablesWithout9a() {
    final XQuery query = new XQuery(
      "declare variable $local:myVar := local:myFunc(3); declare function local:myFunc($arg) { local:myFunc($local:myVar) }; $local:myVar",
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
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl001() {
    final XQuery query = new XQuery(
      "declare variable $x := \"\" ; $x",
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
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl002() {
    final XQuery query = new XQuery(
      "declare variable $x := '' ; $x",
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
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl003() {
    final XQuery query = new XQuery(
      "declare variable $x := 'a string' ; $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a string")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl004() {
    final XQuery query = new XQuery(
      "declare variable $x := \"a string\" ; $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a string")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl005() {
    final XQuery query = new XQuery(
      "declare variable $x := \"This is a string, isn't it?\" ; $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "This is a string, isn't it?")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl006() {
    final XQuery query = new XQuery(
      "declare variable $x := 'This is a \"String\"' ; $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "This is a \"String\"")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl007() {
    final XQuery query = new XQuery(
      "declare variable $x := \"a \"\" or a ' delimits a string literal\" ; $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a \" or a ' delimits a string literal")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl008() {
    final XQuery query = new XQuery(
      "declare variable $x := 'a \" or a '' delimits a string literal' ; $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a \" or a ' delimits a string literal")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl009() {
    final XQuery query = new XQuery(
      "declare variable $x := '&lt;bold&gt;A sample element.&lt;/bold&gt;' ; $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "<bold>A sample element.</bold>")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl010() {
    final XQuery query = new XQuery(
      "declare variable $x := 0 ; $x",
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

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl011() {
    final XQuery query = new XQuery(
      "declare variable $x := 1 ; $x",
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
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl012() {
    final XQuery query = new XQuery(
      "declare variable $x := -1 ; $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl013() {
    final XQuery query = new XQuery(
      "declare variable $x := +1 ; $x",
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
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl014() {
    final XQuery query = new XQuery(
      "declare variable $x := 1.23 ; $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1.23")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl015() {
    final XQuery query = new XQuery(
      "declare variable $x := -1.23 ; $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-1.23")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl016() {
    final XQuery query = new XQuery(
      "declare variable $x := 1.2e5 ; $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("120000")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl017() {
    final XQuery query = new XQuery(
      "declare variable $x := -1.2E5 ; $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-120000")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl018() {
    final XQuery query = new XQuery(
      "declare variable $x := 0.0E0 ; $x",
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

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl019() {
    final XQuery query = new XQuery(
      "declare variable $x := 1e-5 ; $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0.00001")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl020() {
    final XQuery query = new XQuery(
      "declare variable $x := 9.999999999999999; $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "9.999999999999999")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl021() {
    final XQuery query = new XQuery(
      "declare variable $x := -10000000 ; $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-10000000")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl022() {
    final XQuery query = new XQuery(
      "declare variable $x := 1 to 10 ; $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2 3 4 5 6 7 8 9 10")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl023() {
    final XQuery query = new XQuery(
      "declare variable $x := xs:double('NaN'); $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl024() {
    final XQuery query = new XQuery(
      "declare variable $x := xs:double('INF'); $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "INF")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl025() {
    final XQuery query = new XQuery(
      "declare variable $x := xs:double('-INF'); $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-INF")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl026() {
    final XQuery query = new XQuery(
      "declare variable $x := fn:false() ; $x",
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
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl027() {
    final XQuery query = new XQuery(
      "declare variable $x := false(); $x",
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
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl028() {
    final XQuery query = new XQuery(
      "declare variable $x := fn:true() ; $x",
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
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl029() {
    final XQuery query = new XQuery(
      "declare variable $x := true() ; $x",
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
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl030() {
    final XQuery query = new XQuery(
      "declare variable $x := true(); $x",
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
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl031() {
    final XQuery query = new XQuery(
      "declare variable $x := 2+2 ; $x",
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
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl032() {
    final XQuery query = new XQuery(
      "declare variable $x := 2*2 ; $x",
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
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl033() {
    final XQuery query = new XQuery(
      "declare variable $x := 3-2 ; $x",
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
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl034() {
    final XQuery query = new XQuery(
      "declare variable $x := 3 div 2 ; $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1.5")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl035() {
    final XQuery query = new XQuery(
      "declare variable $x := 3 mod 2 ; $x",
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
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl036() {
    final XQuery query = new XQuery(
      "declare variable $x := 3 idiv 2 ; $x",
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
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl037() {
    final XQuery query = new XQuery(
      "declare variable $x := -1.7976931348623157E308 ; $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-1.7976931348623157E308")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl038() {
    final XQuery query = new XQuery(
      "declare variable $x := xs:double(\"-1.7976931348623157E308\") ; $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-1.7976931348623157E308")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl039() {
    final XQuery query = new XQuery(
      "declare variable $x := -999999999999999999 ; $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl040() {
    final XQuery query = new XQuery(
      "declare variable $x := xs:decimal(\"-999999999999999999\") ; $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl041() {
    final XQuery query = new XQuery(
      "declare variable $x := 999999999999999999 ; $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl042() {
    final XQuery query = new XQuery(
      "declare variable $x := xs:decimal(\"999999999999999999\") ; $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl043() {
    final XQuery query = new XQuery(
      "declare variable $x := -3.4028235E38 ; $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-3.4028235E38")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl044() {
    final XQuery query = new XQuery(
      "declare variable $x := xs:float(\"-3.4028235E38\") ; $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-3.4028235E38")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl045() {
    final XQuery query = new XQuery(
      "declare variable $x := xs:date(\"1970-01-01Z\") ; $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1970-01-01Z")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl046() {
    final XQuery query = new XQuery(
      "declare variable $x := xs:date(\"1970-01-01Z\") + xs:dayTimeDuration(\"P31DT23H59M59S\") ; $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1970-02-01Z")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl047() {
    final XQuery query = new XQuery(
      "declare variable $x := xs:time(\"08:03:35Z\") ; $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "08:03:35Z")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl048() {
    final XQuery query = new XQuery(
      "declare variable $x := xs:time(\"08:03:35Z\") + xs:dayTimeDuration(\"P0DT0H0M0S\") ; $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "08:03:35Z")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl049() {
    final XQuery query = new XQuery(
      "declare variable $x := xs:dateTime(\"2030-12-31T23:59:59Z\") ; $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2030-12-31T23:59:59Z")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl050() {
    final XQuery query = new XQuery(
      "declare variable $x := (1,2,3) ; $x",
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
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl051() {
    final XQuery query = new XQuery(
      "declare variable $x := (xs:string(\"a\") , (), \"xyz\") ; $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a xyz")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl052() {
    final XQuery query = new XQuery(
      "declare variable $x := (xs:string(\"a\") , xs:anyURI(\"www.example.com\")) ; $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a www.example.com")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl053() {
    final XQuery query = new XQuery(
      "declare variable $x := (xs:float(\"INF\") , xs:double(\"NaN\")) ; $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "INF NaN")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl054() {
    final XQuery query = new XQuery(
      "declare variable $x := (xs:boolean(\"true\") , xs:boolean(\"0\"), xs:integer(\"0\")) ; $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "true false 0")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl055() {
    final XQuery query = new XQuery(
      "declare variable $x := (xs:date(\"1993-03-31\") , xs:boolean(\"true\"), xs:string(\"abc\")) ; $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1993-03-31 true abc")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl056() {
    final XQuery query = new XQuery(
      "declare variable $x := (xs:time(\"12:30:00\") , xs:string(\" \") , xs:decimal(\"2.000000000000002\")) ; $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "12:30:00   2.000000000000002")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl057() {
    final XQuery query = new XQuery(
      "declare variable $x := ((1+1), (2-2)) ; $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2 0")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl058() {
    final XQuery query = new XQuery(
      "declare variable $x := ((1,2,2),(1,2,3),(123,\"\"),(),(\"\")) ; $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2 2 1 2 3 123  ")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl059() {
    final XQuery query = new XQuery(
      "declare variable $x := (//book/price, (), (1)) ; $x",
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
      assertSerialization("<price>65.95</price><price>65.95</price><price>39.95</price><price>129.95</price>1", false)
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl060() {
    final XQuery query = new XQuery(
      "declare variable $x := //Price/text() ; $x",
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
      assertStringValue(false, "")
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl061() {
    final XQuery query = new XQuery(
      "declare variable $x := /comment() ; $x",
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
      assertSerialization("<!-- this file is a copy of bib.xml; just adds a few comments and PI nodes for testing --><!-- Comment 1 --><!-- Comment 2 -->", false)
    );
  }

  /**
   * Purpose - Variable with no type definition .
   */
  @org.junit.Test
  public void varDecl062() {
    final XQuery query = new XQuery(
      "declare variable $x := /processing-instruction() ; $x",
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
      assertSerialization("<?PI1 Processing Instruction 1?><?PI2 Processing Instruction 2?>", false)
    );
  }

  /**
   * Purpose - forwards references to global variables allowed in XQuery 3.0 .
   */
  @org.junit.Test
  public void varDecl063() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $x := $y + 3;\n" +
      "        declare variable $y := 17;\n" +
      "        $x + 5\n" +
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
      assertEq("25")
    );
  }

  /**
   *  Evaluates an external variable named "$local:var" declared as "item()*" and multiple embedded comments. .
   */
  @org.junit.Test
  public void internalvar1() {
    final XQuery query = new XQuery(
      "declare(::)variable(::)$var(::)as(::)item((: :))*(::):=(::)1(::);(::) 1(::)eq(::)1(::)",
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
   *  Evaluates an external variable named "$local:var" declared with no type and multiple embedded comments. .
   */
  @org.junit.Test
  public void internalvar2() {
    final XQuery query = new XQuery(
      "(::)declare(::)variable(::)$var(::):=(::)1(::);(::) 1(::)eq(::)1",
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
   *  Test circularity on variable/function declaration . .
   */
  @org.junit.Test
  public void vardeclerr() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare namespace foo = \"http://www..oracle.com/xquery/test\"; \n" +
      "      declare variable $var1 as xs:integer := foo:price(xs:integer(2)); \n" +
      "      declare function foo:price ($b as xs:integer) as xs:integer { $var1 + 1 }; \n" +
      "      declare variable $input-context1 external; \n" +
      "      $var1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0054")
    );
  }

  /**
   *  Test circularity on variable/function declaration . .
   */
  @org.junit.Test
  public void vardeclerr1() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare namespace foo = \"http://www..oracle.com/xquery/test\"; \n" +
      "      declare variable $var1 as xs:integer := foo:price(xs:integer(2)); \n" +
      "      declare function foo:price ($b as xs:integer) as xs:integer { $var1 + 1 }; \n" +
      "      declare variable $input-context1 external; \n" +
      "      $var1",
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
   *  Evaluates an internal variable declaration with type Use type xs:string. .
   */
  @org.junit.Test
  public void vardeclwithtype1() {
    final XQuery query = new XQuery(
      "declare variable $var as xs:string := \"abc\"; declare variable $input-context1 external; $var",
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
   *  Evaluates an internal variable declaration with type Use type xs:time .
   */
  @org.junit.Test
  public void vardeclwithtype10() {
    final XQuery query = new XQuery(
      "declare variable $var as xs:time := xs:time(\"11:12:00Z\"); declare variable $input-context1 external; $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "11:12:00Z")
    );
  }

  /**
   *  Evaluates an internal variable declaration with type Use type xs:date .
   */
  @org.junit.Test
  public void vardeclwithtype11() {
    final XQuery query = new XQuery(
      "declare variable $var as xs:date := xs:date(\"1999-11-28Z\"); declare variable $input-context1 external; $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1999-11-28Z")
    );
  }

  /**
   *  Evaluates an internal variable declaration with type that is declared twice .
   */
  @org.junit.Test
  public void vardeclwithtype12() {
    final XQuery query = new XQuery(
      "declare variable $var as xs:date := xs:date(\"1999-11-28Z\"); declare variable $var as xs:date := xs:date(\"1999-11-28Z\"); declare variable $input-context1 external; $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0049")
    );
  }

  /**
   *  Evaluates an internal variable declaration with type and expression. Expression result does not match given type. .
   */
  @org.junit.Test
  public void vardeclwithtype13() {
    final XQuery query = new XQuery(
      "declare variable $var as xs:date := fn:true() and fn:true(); declare variable $input-context1 external; $var",
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
   *  Evaluates an internal variable declaration with type and expression. Expression uses a previously defined variable. .
   */
  @org.junit.Test
  public void vardeclwithtype14() {
    final XQuery query = new XQuery(
      "declare variable $x as xs:integer := 10; declare variable $var as xs:integer := $x +1; declare variable $input-context1 external; $var",
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
   *  Evaluates an internal variable declaration with type and expression.
   *       Test usage of variable with no assigned value at time of expression definition. .
   */
  @org.junit.Test
  public void vardeclwithtype15a() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare variable $var as xs:integer := $e +1; \n" +
      "      declare variable $e as xs:integer := 10;  \n" +
      "      $var",
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
   *  Evaluates an internal variable declaration with type and expression.
   *       Test usage of variable with no assigned value at time of expression definition. .
   */
  @org.junit.Test
  public void vardeclwithtype15b() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare variable $var as xs:integer := $e +1; \n" +
      "      declare variable $e as xs:integer := 10;  \n" +
      "      $var",
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
   *  Evaluates an internal variable declaration with type that uses a declared namesapce. .
   */
  @org.junit.Test
  public void vardeclwithtype16() {
    final XQuery query = new XQuery(
      "declare namespace p1 = \"http://www.example.com\"; declare variable $p1:var as xs:integer := 10; declare variable $input-context1 external; $p1:var",
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
   *  Evaluates an internal variable declaration with type that uses a declared namespace. Two variable with same name declared in two deferent namespaces. .
   */
  @org.junit.Test
  public void vardeclwithtype17() {
    final XQuery query = new XQuery(
      "declare namespace p1 = \"http://www.example.com\"; declare namespace p2 = \"http://www.example.com/examples\"; declare variable $p1:var as xs:integer := 10; declare variable $p2:var as xs:integer := 20; declare variable $input-context1 external; $p2:var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("20")
    );
  }

  /**
   *  Evaluates an internal variable declaration with type that uses a declared namespace. Two variable with same name declared in namespaces with same namespace uri. .
   */
  @org.junit.Test
  public void vardeclwithtype18() {
    final XQuery query = new XQuery(
      "declare namespace p1 = \"http://www.example.com\"; declare namespace p2 = \"http://www.example.com\"; declare variable $p1:var as xs:integer := 10; declare variable $p2:var as xs:integer := 20; declare variable $input-context1 external; $p2:var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0049")
    );
  }

  /**
   *  Evaluates an internal variable declaration with type that uses the fn:count function . .
   */
  @org.junit.Test
  public void vardeclwithtype19() {
    final XQuery query = new XQuery(
      "declare variable $var as xs:integer := fn:count((10,2)); declare variable $input-context1 external; $var",
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
   *  Evaluates an internal variable declaration with type Use type xs:integer. .
   */
  @org.junit.Test
  public void vardeclwithtype2() {
    final XQuery query = new XQuery(
      "declare variable $var as xs:integer := 100; declare variable $input-context1 external; $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("100")
    );
  }

  /**
   *  Evaluates an internal variable declaration with type that uses the fn:string-length function . .
   */
  @org.junit.Test
  public void vardeclwithtype20() {
    final XQuery query = new XQuery(
      "declare variable $var as xs:integer := fn:string-length(\"ABC\"); declare variable $input-context1 external; $var",
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
   *  Evaluates an internal variable declaration with type that uses the fn:not function . .
   */
  @org.junit.Test
  public void vardeclwithtype21() {
    final XQuery query = new XQuery(
      "declare variable $var as xs:boolean := fn:not(fn:true()); declare variable $input-context1 external; $var",
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
   *  Evaluates an internal variable declaration with type that uses the fn:empty function . .
   */
  @org.junit.Test
  public void vardeclwithtype22() {
    final XQuery query = new XQuery(
      "declare variable $var as xs:boolean := fn:empty((1,2,3)); declare variable $input-context1 external; $var",
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
   *  Evaluates an internal variable declaration with type Use type xs:decimal. .
   */
  @org.junit.Test
  public void vardeclwithtype3() {
    final XQuery query = new XQuery(
      "declare variable $var as xs:decimal := 100; declare variable $input-context1 external; $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("100")
    );
  }

  /**
   *  Evaluates an internal variable declaration with type Use type xs:boolean (true value). .
   */
  @org.junit.Test
  public void vardeclwithtype4() {
    final XQuery query = new XQuery(
      "declare variable $var as xs:boolean := fn:true(); declare variable $input-context1 external; $var",
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
   *  Evaluates an internal variable declaration with type Use type xs:boolean (false value). .
   */
  @org.junit.Test
  public void vardeclwithtype5() {
    final XQuery query = new XQuery(
      "declare variable $var as xs:boolean := fn:false(); declare variable $input-context1 external; $var",
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
   *  Evaluates an internal variable declaration with type Use type xs:float .
   */
  @org.junit.Test
  public void vardeclwithtype6() {
    final XQuery query = new XQuery(
      "declare variable $var as xs:float := xs:float(12.5E10); declare variable $input-context1 external; $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1.25E11")
    );
  }

  /**
   *  Evaluates an internal variable declaration with type Use type xs:double .
   */
  @org.junit.Test
  public void vardeclwithtype7() {
    final XQuery query = new XQuery(
      "declare variable $var as xs:double := xs:double(1267.43233E12); declare variable $input-context1 external; $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1.26743233E15")
    );
  }

  /**
   *  Evaluates an internal variable declaration with type Use type xs:anyURI .
   */
  @org.junit.Test
  public void vardeclwithtype8() {
    final XQuery query = new XQuery(
      "declare variable $var as xs:anyURI := xs:anyURI(\"http://example.com\"); declare variable $input-context1 external; $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://example.com")
    );
  }

  /**
   *  Evaluates an internal variable declaration with type Use type xs:dateTime .
   */
  @org.junit.Test
  public void vardeclwithtype9() {
    final XQuery query = new XQuery(
      "declare variable $var as xs:dateTime := xs:dateTime(\"1999-11-28T09:00:00Z\"); declare variable $input-context1 external; $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1999-11-28T09:00:00Z")
    );
  }
}
