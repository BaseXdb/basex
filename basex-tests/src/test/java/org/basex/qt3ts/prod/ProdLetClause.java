package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the LetClause production (or the LetExpr in XPath 3.0).
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdLetClause extends QT3TestSet {

  /**
   *  A 'let' variable is out-of-scope due to FLWOR has higher precendence than the comma operator. .
   */
  @org.junit.Test
  public void kLetExprWithout1() {
    final XQuery query = new XQuery(
      "let $i := 5, $j := 20 * $i return $i, $j",
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
   *  A value in a 'let' variable cannot be assigned to with '=', it must be ':='. .
   */
  @org.junit.Test
  public void kLetExprWithout2() {
    final XQuery query = new XQuery(
      "let $i = 5 return 3",
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
   *  A value in a 'let' variable cannot be assigned to with 'in', it must be ':='. .
   */
  @org.junit.Test
  public void kLetExprWithout3() {
    final XQuery query = new XQuery(
      "let $i in 5 return 3",
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
   *  A binding in a let-expression shadows global variables. .
   */
  @org.junit.Test
  public void kLetExprWithout4() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $i := false(); \n" +
      "        declare variable $t := false(); \n" +
      "        deep-equal((let $i := true(), $t := true() return ($i, $t)), (true(), true()))",
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
   *  fn:deep-equal combined with a for/let expression. .
   */
  @org.junit.Test
  public void k2LetExprWithout1() {
    final XQuery query = new XQuery(
      "deep-equal((<b/>, <b/>, <b/>, <b/>), (for $v1 in (1, 2, 3, 4) let $v2 := <b/> return ($v2))/.)",
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
   *  let-declarations doesn't cause numeric promotion(#2). .
   */
  @org.junit.Test
  public void k2LetExprWithout10() {
    final XQuery query = new XQuery(
      "let $i as xs:double := 1.1 return $i",
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
   *  let-declarations doesn't cause numeric promotion(#3). .
   */
  @org.junit.Test
  public void k2LetExprWithout11() {
    final XQuery query = new XQuery(
      "let $i as xs:float := 1 return $i",
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
   *  let-declarations doesn't cause numeric promotion(#4). .
   */
  @org.junit.Test
  public void k2LetExprWithout12() {
    final XQuery query = new XQuery(
      "let $i as xs:double := 1 return $i",
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
   *  let-declarations doesn't cause numeric promotion(#5). .
   */
  @org.junit.Test
  public void k2LetExprWithout13() {
    final XQuery query = new XQuery(
      "let $i as xs:double := xs:float(3) return $i",
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
   *  let-declarations doesn't cause string promotion conversion. .
   */
  @org.junit.Test
  public void k2LetExprWithout14() {
    final XQuery query = new XQuery(
      "let $i as xs:string := xs:untypedAtomic(\"a string\") return $i",
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
   *  let-declarations doesn't cause URI promotion conversion. .
   */
  @org.junit.Test
  public void k2LetExprWithout15() {
    final XQuery query = new XQuery(
      "let $i as xs:string := xs:anyURI(\"http://www.example.com/\") return $i",
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
   *  Ensure node identity is properly handled through a for binding. .
   */
  @org.junit.Test
  public void k2LetExprWithout16() {
    final XQuery query = new XQuery(
      "declare variable $e := <e/>; for $i in (<a/>, $e, <c/>) return $i is $e",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "false true false")
    );
  }

  /**
   *  Ensure a name test is reported for using the undefined focus, when appearing as a child of a let binding. However, since the let body is the empty sequence, it does not have to be evaluated. .
   */
  @org.junit.Test
  public void k2LetExprWithout17() {
    final XQuery query = new XQuery(
      "declare function local:function() { let $b := (i/a) return () }; empty(local:function())",
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
        error("XPDY0002")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Use a let-variable that acts as an alias for a function argument. .
   */
  @org.junit.Test
  public void k2LetExprWithout18() {
    final XQuery query = new XQuery(
      "declare function local:foo($a as xs:integer) { if($a = 3) then $a else let $a := $a return local:foo($a + 1) }; local:foo(1)",
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
   *  Use an undefined focus in a let binding. .
   */
  @org.junit.Test
  public void k2LetExprWithout19() {
    final XQuery query = new XQuery(
      "let $v := . return 1",
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
   *  fn:deep-equal combined with a for/let expression(#2). .
   */
  @org.junit.Test
  public void k2LetExprWithout2() {
    final XQuery query = new XQuery(
      "deep-equal((<b/>, <b/>, <b/>, <b/>), (for $v1 in (1, 2, 3, 4) let $v2 := <b/> return ($v2)))",
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
   *  Bind to let and subsequently use the for expression. .
   */
  @org.junit.Test
  public void k2LetExprWithout20() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $emps := //employee[location = \"Denver\"] \n" +
      "        for $d in distinct-values($emps/deptno) \n" +
      "        let $e := $emps[deptno = $d] \n" +
      "        return <dept> <deptno>{$d}</deptno> <headcount> {count($e)} </headcount> <payroll> {sum($e/salary)} </payroll> </dept>\n" +
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
   *  Use a focus within a LET tail expression. .
   */
  @org.junit.Test
  public void k2LetExprWithout21() {
    final XQuery query = new XQuery(
      "<e/>/(for $b in 1, $i in self::node() return $i)",
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
   *  Use a focus within a LET expression. .
   */
  @org.junit.Test
  public void k2LetExprWithout22() {
    final XQuery query = new XQuery(
      "<e/>/(for $i in self::node() return $i)",
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
   *  Bind the context item to a variable. .
   */
  @org.junit.Test
  public void k2LetExprWithout23() {
    final XQuery query = new XQuery(
      "count(<e/>/(let $i := . return (string($i), data($i))))",
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
   *  Ensure a name test is reported for using the undefined focus, when appearing as a child of a let binding. .
   */
  @org.junit.Test
  public void k2LetExprWithout24() {
    final XQuery query = new XQuery(
      "declare function local:function() { let $b := (i/a) return 1 }; local:function()",
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
   *  A combination of expressions that triggers a bug in some parsers. .
   */
  @org.junit.Test
  public void k2LetExprWithout25() {
    final XQuery query = new XQuery(
      "let $a := 1 return \n" +
      "                for $b in 1 return \n" +
      "                    if ($b) then 1 \n" +
      "                            else if ($b) then 1 \n" +
      "                            else if ($b) then 1 \n" +
      "                            else if ($b) then 1 \n" +
      "                            else if ($b) then 1 \n" +
      "                            else if ($b) then 1 \n" +
      "                            else if ($b) then 1 \n" +
      "                            else if ($b) then 1 \n" +
      "                            else if ($b) then 1 \n" +
      "                            else if ($b) then 1 \n" +
      "                            else if ($b) then 1 \n" +
      "                            else if ($b) then 1 \n" +
      "                            else if ($b) then 1 \n" +
      "                            else if ($b) then 1 \n" +
      "                            else if ($b) then 1 \n" +
      "                            else if ($b) then 1 \n" +
      "                            else if ($b) then 1 \n" +
      "                            else if ($b) then 1 \n" +
      "                            else if ($b) then 1 \n" +
      "                            else if ($b) then 1 \n" +
      "                            else if ($b) then 1 \n" +
      "                            else if ($b) then 1 \n" +
      "                            else if ($b) then 1 \n" +
      "                            else if ($b) then 1 \n" +
      "                            else if ($b) then 1 \n" +
      "                            else ()",
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
   *  An element() test whose cardinality doesn't match. .
   */
  @org.junit.Test
  public void k2LetExprWithout26() {
    final XQuery query = new XQuery(
      "let $e := <element/>, $outer as element() := $e/element() return $outer",
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
   *  The focus is undefined inside the initializing expression for a 'let' variable. .
   */
  @org.junit.Test
  public void k2LetExprWithout3() {
    final XQuery query = new XQuery(
      "declare function local:myFunc() { let $v := . return $v }; local:myFunc()",
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
   *  The focus is undefined inside the initializing expression for a 'let' variable(#2). .
   */
  @org.junit.Test
  public void k2LetExprWithout4() {
    final XQuery query = new XQuery(
      "declare function local:myFunc($arg as node()) { let $v := . return $arg/$v }; local:myFunc(<e/>)",
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
   *  The focus is undefined inside the initializing expression for a 'let' variable(#3). .
   */
  @org.junit.Test
  public void k2LetExprWithout5() {
    final XQuery query = new XQuery(
      "declare function local:myFunc($arg as node()) { let $v := aNameTest return $arg/$v }; local:myFunc(<e/>)",
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
   *  The focus is undefined inside the initializing expression for a 'let' variable(#4). .
   */
  @org.junit.Test
  public void k2LetExprWithout6() {
    final XQuery query = new XQuery(
      "declare function local:myFunc($arg as node()) { let $v := aNameTest return $arg/$v }; 1",
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
   *  let-declarations doesn't cause type conversion. .
   */
  @org.junit.Test
  public void k2LetExprWithout7() {
    final XQuery query = new XQuery(
      "let $i as xs:integer := xs:untypedAtomic(\"1\") return $i",
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
   *  A single return statement is syntactically incorrect. .
   */
  @org.junit.Test
  public void k2LetExprWithout8() {
    final XQuery query = new XQuery(
      "return 1",
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
   *  let-declarations doesn't cause numeric promotion. .
   */
  @org.junit.Test
  public void k2LetExprWithout9() {
    final XQuery query = new XQuery(
      "let $i as xs:float := 1.1 return $i",
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
   * Purpose : Assign a simple string using let .
   */
  @org.junit.Test
  public void letExpr001() {
    final XQuery query = new XQuery(
      "let $x := \"92233720368547758\" return $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "92233720368547758")
    );
  }

  /**
   * Purpose : Assign a simple integer using let .
   */
  @org.junit.Test
  public void letExpr002() {
    final XQuery query = new XQuery(
      "let $x := 92233720368547758 return $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("92233720368547758")
    );
  }

  /**
   * Purpose : Use an arithmetic operator with let .
   */
  @org.junit.Test
  public void letExpr003() {
    final XQuery query = new XQuery(
      "let $x := 92233720368547758+1 return $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("92233720368547759")
    );
  }

  /**
   * Purpose : Use xs:long(upper bound) .
   */
  @org.junit.Test
  public void letExpr004() {
    final XQuery query = new XQuery(
      "let $x := xs:long(\"92233720368547758\") return $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("92233720368547758")
    );
  }

  /**
   * Purpose : Use xs:long(lower bound) +1 .
   */
  @org.junit.Test
  public void letExpr005() {
    final XQuery query = new XQuery(
      "let $x := xs:long(\"-92233720368547758\")+1 return $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-92233720368547757")
    );
  }

  /**
   * Purpose : Use xs:double(upper bound) .
   */
  @org.junit.Test
  public void letExpr006() {
    final XQuery query = new XQuery(
      "let $x := xs:double(\"1.7976931348623157E308\") return $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1.7976931348623157E308")
    );
  }

  /**
   * Purpose : A node with an expression in curly braces .
   */
  @org.junit.Test
  public void letExpr007() {
    final XQuery query = new XQuery(
      "let $x:=<a>{1+1}</a> return $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a>2</a>", false)
    );
  }

  /**
   * Purpose : More than one variable cross referencing compatible values .
   */
  @org.junit.Test
  public void letExpr008() {
    final XQuery query = new XQuery(
      "let $x:=1, $y:=$x+1 return $x",
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
   * Purpose : More than one variable cross referencing compatible values .
   */
  @org.junit.Test
  public void letExpr009() {
    final XQuery query = new XQuery(
      "let $x:=1, $y:=<a>{$x+1}</a> return $y",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a>2</a>", false)
    );
  }

  /**
   * Purpose : More than one variable cross referencing incompatible values .
   */
  @org.junit.Test
  public void letExpr010() {
    final XQuery query = new XQuery(
      "let $x:=(1,2,3), $y:=$x+1 return $y",
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
   * Purpose : Use sequences with filter expressions .
   */
  @org.junit.Test
  public void letExpr011() {
    final XQuery query = new XQuery(
      "let $x :=(1 to 100)[. mod 5 eq 0] return $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "5 10 15 20 25 30 35 40 45 50 55 60 65 70 75 80 85 90 95 100")
    );
  }

  /**
   * Purpose : Use sequences with filter expressions with more than one variable .
   */
  @org.junit.Test
  public void letExpr012() {
    final XQuery query = new XQuery(
      "let $x :=(1 to 100)[. mod 5 eq 0], $y := $x[. mod 10 eq 0] return $y",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "10 20 30 40 50 60 70 80 90 100")
    );
  }

  /**
   * Purpose : Use a function in the let .
   */
  @org.junit.Test
  public void letExpr013() {
    final XQuery query = new XQuery(
      "let $x:=\"hello\", $y:=concat($x,\" there\") return $y",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "hello there")
    );
  }

  /**
   * Purpose : Use incompatible types in let .
   */
  @org.junit.Test
  public void letExpr014() {
    final XQuery query = new XQuery(
      "let $x := \"1\", $y := $x+1 return $y",
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
   * Purpose : Use a sequence of different types of scalars .
   */
  @org.junit.Test
  public void letExpr015() {
    final XQuery query = new XQuery(
      "let $x := (0,0.1e-1,2.0,'a',\"cat\",'',true()) return $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0 0.01 2 a cat  true")
    );
  }

  /**
   * Purpose : Use a sequence of nodes .
   */
  @org.junit.Test
  public void letExpr016() {
    final XQuery query = new XQuery(
      "let $a := (<elem1/>, <elem2/>, <elem3 att=\"test\"/>) return <root>{$a}</root>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<root><elem1/><elem2/><elem3 att=\"test\"/></root>", false)
    );
  }

  /**
   * Purpose : Use a sequence with step .
   */
  @org.junit.Test
  public void letExpr017() {
    final XQuery query = new XQuery(
      "let $x := (<a> <b> <c> 123 </c> </b> </a>) return $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a><b><c> 123 </c></b></a>", false)
    );
  }

  /**
   * Purpose : Use a sequence of different values .
   */
  @org.junit.Test
  public void letExpr018() {
    final XQuery query = new XQuery(
      "let $x := (0,0.1e-1,2.0,'a',\"cat\",'',true(), ('<a> <b> <c> 123 </c> </b> </a>')/a/b) return $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0019")
    );
  }

  /**
   * Purpose : combine two 'Let' exprs in one 'Return' .
   */
  @org.junit.Test
  public void letExpr019() {
    final XQuery query = new XQuery(
      "let $a := <elem/> let $b := <elem2/> return ($a,$b)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem/><elem2/>", false)
    );
  }

  /**
   * Purpose : Use three variables .
   */
  @org.junit.Test
  public void letExpr020() {
    final XQuery query = new XQuery(
      "let $a := 1 let $b := $a let $c := $a+$b return ($c)",
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
   * Purpose : Refer to an undefined variable .
   */
  @org.junit.Test
  public void letExpr021() {
    final XQuery query = new XQuery(
      "let $a := $b return ($a)",
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
   *  Evaluation of let clause bound to variable used in expression that raises error. .
   */
  @org.junit.Test
  public void letexprwith1() {
    final XQuery query = new XQuery(
      "let $salary as xs:decimal := \"cat\" return $salary * 2",
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
   *  Let clause using dateTime type. .
   */
  @org.junit.Test
  public void letexprwith10() {
    final XQuery query = new XQuery(
      "let $var as xs:dateTime := xs:dateTime(\"1999-05-31T13:20:00Z\") return $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1999-05-31T13:20:00Z")
    );
  }

  /**
   *  Let clause using float type. .
   */
  @org.junit.Test
  public void letexprwith11() {
    final XQuery query = new XQuery(
      "let $var as xs:float := xs:float(100) return $var",
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
   *  Let clause using an ineteger expression type. .
   */
  @org.junit.Test
  public void letexprwith12() {
    final XQuery query = new XQuery(
      "let $var as xs:integer := 100+200 return $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("300")
    );
  }

  /**
   *  Let clause using the fn:count function. .
   */
  @org.junit.Test
  public void letexprwith13() {
    final XQuery query = new XQuery(
      "let $var as xs:integer := fn:count((100,200)) return $var",
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
   *  Let clause using the fn:not function. .
   */
  @org.junit.Test
  public void letexprwith14() {
    final XQuery query = new XQuery(
      "let $var as xs:boolean := fn:not(fn:true()) return $var",
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
   *  Let clause using the "and" operator function. .
   */
  @org.junit.Test
  public void letexprwith15() {
    final XQuery query = new XQuery(
      "let $var as xs:boolean := fn:true() and fn:true() return $var",
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
   *  Let clause using the "or" operator function. .
   */
  @org.junit.Test
  public void letexprwith16() {
    final XQuery query = new XQuery(
      "let $var as xs:boolean := fn:true() and fn:true() return $var",
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
   *  Let clause using the "string-length" function. .
   */
  @org.junit.Test
  public void letexprwith17() {
    final XQuery query = new XQuery(
      "let $var as xs:integer := fn:string-length(\"A String\") return $var",
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
   *  Let clause using a casting from integer to string .
   */
  @org.junit.Test
  public void letexprwith18() {
    final XQuery query = new XQuery(
      "let $var as xs:string := xs:string((xs:integer(100))) return $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "100")
    );
  }

  /**
   *  Let clause using a casting from decimal to string .
   */
  @org.junit.Test
  public void letexprwith19() {
    final XQuery query = new XQuery(
      "let $var as xs:string := xs:string((xs:decimal(100))) return $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "100")
    );
  }

  /**
   *  Let clause used decimal type. .
   */
  @org.junit.Test
  public void letexprwith2() {
    final XQuery query = new XQuery(
      "let $var as xs:decimal := 100 return $var",
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
   *  Let clause using a casting from double to string .
   */
  @org.junit.Test
  public void letexprwith20() {
    final XQuery query = new XQuery(
      "let $var as xs:string := xs:string((xs:double(100E2))) return $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "10000")
    );
  }

  /**
   *  Let clause using a casting from boolean to string .
   */
  @org.junit.Test
  public void letexprwith21() {
    final XQuery query = new XQuery(
      "let $var as xs:string := xs:string(fn:true()) return $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "true")
    );
  }

  /**
   *  Let clause using a integer type and addition expression on return clause .
   */
  @org.junit.Test
  public void letexprwith22() {
    final XQuery query = new XQuery(
      "let $var as xs:integer := 100 return $var + 1",
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
   *  Let clause using an "if" expression .
   */
  @org.junit.Test
  public void letexprwith23() {
    final XQuery query = new XQuery(
      "let $var as xs:boolean := if (fn:true()) then fn:true() else fn:false() return $var",
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
   *  Let clause using a "typeswitch" expression .
   */
  @org.junit.Test
  public void letexprwith24() {
    final XQuery query = new XQuery(
      "let $var as xs:string := typeswitch(fn:true()) case $i as xs:boolean return \"Test Passed\" default return \"Test failed\" return $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Test Passed")
    );
  }

  /**
   *  Let clause using integer type. .
   */
  @org.junit.Test
  public void letexprwith3() {
    final XQuery query = new XQuery(
      "let $var as xs:integer := 100 return $var",
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
   *  Let clause using double type. .
   */
  @org.junit.Test
  public void letexprwith4() {
    final XQuery query = new XQuery(
      "let $var as xs:double := 100E1 return $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1000")
    );
  }

  /**
   *  Let clause using double type. .
   */
  @org.junit.Test
  public void letexprwith5() {
    final XQuery query = new XQuery(
      "let $var as xs:string := \"A String\" return $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "A String")
    );
  }

  /**
   *  Let clause using boolean (true) type. .
   */
  @org.junit.Test
  public void letexprwith6() {
    final XQuery query = new XQuery(
      "let $var as xs:boolean := fn:true() return $var",
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
   *  Let clause using boolean (false) type. .
   */
  @org.junit.Test
  public void letexprwith7() {
    final XQuery query = new XQuery(
      "let $var as xs:boolean := fn:false() return $var",
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
   *  Let clause using date type. .
   */
  @org.junit.Test
  public void letexprwith8() {
    final XQuery query = new XQuery(
      "let $var as xs:date := xs:date(\"1999-05-31Z\") return $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1999-05-31Z")
    );
  }

  /**
   *  Let clause using time type. .
   */
  @org.junit.Test
  public void letexprwith9() {
    final XQuery query = new XQuery(
      "let $var as xs:time := xs:time(\"21:23:00Z\") return $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "21:23:00Z")
    );
  }

  /**
   * Purpose : keywords can be used as variable names .
   */
  @org.junit.Test
  public void xquery10keywords() {
    final XQuery query = new XQuery(
      "let $ancestor-or-self := 1 \n" +
      "      let $ancestor := 1 \n" +
      "      let $and := 1 \n" +
      "      let $as := 1 \n" +
      "      let $ascending := 1 \n" +
      "      let $at := 1 \n" +
      "      let $attribute := 1 \n" +
      "      let $base-uri := 1 \n" +
      "      let $boundary-space := 1 \n" +
      "      let $by := 1 \n" +
      "      let $case := 1 \n" +
      "      let $cast := 1 \n" +
      "      let $castable := 1 \n" +
      "      let $child := 1 \n" +
      "      let $collation := 1 \n" +
      "      let $comment := 1 \n" +
      "      let $construction := 1 \n" +
      "      let $copy-namespaces := 1 \n" +
      "      let $declare := 1 \n" +
      "      let $default := 1 \n" +
      "      let $descendant-or-self := 1 \n" +
      "      let $descendant := 1 \n" +
      "      let $descending := 1 \n" +
      "      let $div := 1 \n" +
      "      let $document-node := 1 \n" +
      "      let $document := 1 \n" +
      "      let $element := 1 \n" +
      "      let $else := 1 \n" +
      "      let $empty-sequence := 1 \n" +
      "      let $empty := 1 \n" +
      "      let $encoding := 1 \n" +
      "      let $eq := 1 let $every := 1 let $except := 1 let $external := 1 let $following-sibling := 1 \n" +
      "      let $following := 1 let $for := 1 let $function := 1 let $ge := 1 let $greatest := 1 \n" +
      "      let $gt := 1 let $idiv := 1 let $if := 1 let $import := 1 let $in := 1 let $inherit := 1 \n" +
      "      let $instance := 1 let $intersect := 1 let $is := 1 let $item := 1 let $lax := 1 \n" +
      "      let $le := 1 let $least := 1 let $let := 1 let $lt := 1 let $mod := 1 let $module := 1 \n" +
      "      let $module := 1 let $namespace := 1 let $ne := 1 let $no-inherit := 1 \n" +
      "      let $no-preserve := 1 let $node := 1 let $of := 1 let $option := 1 let $or := 1 \n" +
      "      let $order := 1 let $ordered := 1 let $ordering := 1 let $parent := 1 let $preceding-sibling := 1 \n" +
      "      let $preceding := 1 let $preserve := 1 let $processing-instruction := 1 let $return := 1 \n" +
      "      let $satisfies := 1 let $schema-attribute := 1 let $schema-element := 1 let $schema := 1 \n" +
      "      let $self := 1 let $some := 1 let $stable := 1 let $strict := 1 let $strip := 1 \n" +
      "      let $text := 1 let $then := 1 let $to := 1 let $treat := 1 let $typeswitch := 1 \n" +
      "      let $union := 1 let $unordered := 1 let $validate := 1 let $variable := 1 let $version := 1 \n" +
      "      let $where := 1 let $xquery := 1 return 2",
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
   * Purpose : keywords can be used as element name tests .
   */
  @org.junit.Test
  public void xquery10keywords2() {
    final XQuery query = new XQuery(
      "\n" +
      "      let $x := (/) \n" +
      "      return $x /ancestor-or-self /ancestor /and /as /ascending /at /attribute \n" +
      "      /base-uri /boundary-space /by \n" +
      "      /case /cast /castable /child /collation /comment /construction /copy-namespaces \n" +
      "      /declare /default /descendant-or-self /descendant /descending /div /document-node /document \n" +
      "      /element /else /empty-sequence /empty /encoding /eq /every /except /external \n" +
      "      /following-sibling /following /for /function \n" +
      "      /ge /greatest /gt \n" +
      "      /idiv /if /import /in /inherit /instance /intersect /is /item\n" +
      "       /lax /le /least /let /lt \n" +
      "       /mod /module /module \n" +
      "       /namespace /ne /no-inherit /no-preserve /node \n" +
      "       /of /option /or /order /ordered /ordering \n" +
      "       /parent /preceding-sibling /preceding /preserve /processing-instruction \n" +
      "       /return \n" +
      "       /satisfies /schema-attribute /schema-element /schema /self /some /stable /strict /strip \n" +
      "       /text /then /to /treat /typeswitch \n" +
      "       /union /unordered \n" +
      "       /validate /variable /version \n" +
      "       /where\n" +
      "       /xquery",
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
      assertEmpty()
    );
  }

  /**
   * Purpose : keywords can be used as element names .
   */
  @org.junit.Test
  public void xquery10keywords3() {
    final XQuery query = new XQuery(
      "\n" +
      "      <keywords> <ancestor-or-self/> <ancestor/> <and/> <as/> <ascending/> <at/> <attribute/> \n" +
      "      <base-uri/> <boundary-space/> <by/> \n" +
      "      <case/> <cast/> <castable/> <child/> <collation/> <comment/> <construction/> <copy-namespaces/> \n" +
      "      <declare/> <default/> <descendant-or-self/> <descendant/> <descending/> <div/> <document-node/> <document/> \n" +
      "      <element/> <else/> <empty-sequence/> <empty/> <encoding/> <eq/> <every/> <except/> <external/> \n" +
      "      <following-sibling/> <following/> <for/> <function/> \n" +
      "      <ge/> <greatest/> <gt/> \n" +
      "      <idiv/> <if/> <import/> <in/> <inherit/> <instance/> <intersect/> <is/> <item/> \n" +
      "      <lax/> <le/> <least/> <let/> <lt/> \n" +
      "      <mod/> <module/> <module/> \n" +
      "      <namespace/> <ne/> <no-inherit/> <no-preserve/> <node/> \n" +
      "      <of/> <option/> <or/> <order/> <ordered/> <ordering/> \n" +
      "      <parent/> <preceding-sibling/> <preceding/> <preserve/> <processing-instruction/> \n" +
      "      <return/> \n" +
      "      <satisfies/> <schema-attribute/> <schema-element/> <schema/> <self/> <some/> <stable/> <strict/> <strip/> \n" +
      "      <text/> <then/> <to/> <treat/> <typeswitch/> \n" +
      "      <union/> <unordered/> \n" +
      "      <validate/> <variable/> <version/> \n" +
      "      <where/> \n" +
      "      <xquery/> </keywords>/name()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "keywords")
    );
  }

  /**
   * Purpose : keywords can be used as variable names .
   */
  @org.junit.Test
  public void xquery30keywords() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $NaN := 1\n" +
      "        let $allowing := 1\n" +
      "        let $ancestor-or-self := 1\n" +
      "        let $ancestor := 1\n" +
      "        let $and := 1\n" +
      "        let $as := 1\n" +
      "        let $ascending := 1\n" +
      "        let $at := 1\n" +
      "        let $attribute := 1\n" +
      "        let $base-uri := 1\n" +
      "        let $boundary-space := 1\n" +
      "        let $by := 1\n" +
      "        let $case := 1\n" +
      "        let $cast := 1\n" +
      "        let $castable := 1\n" +
      "        let $catch := 1\n" +
      "        let $child := 1\n" +
      "        let $collation := 1\n" +
      "        let $comment := 1\n" +
      "        let $construction := 1\n" +
      "        let $context := 1\n" +
      "        let $copy-namespaces := 1\n" +
      "        let $count := 1\n" +
      "        let $decimal-format := 1\n" +
      "        let $decimal-separator := 1\n" +
      "        let $declare := 1\n" +
      "        let $default := 1\n" +
      "        let $descendant-or-self := 1\n" +
      "        let $descendant := 1\n" +
      "        let $descending := 1\n" +
      "        let $digit := 1\n" +
      "        let $div := 1\n" +
      "        let $document-node := 1\n" +
      "        let $document := 1\n" +
      "        let $element := 1\n" +
      "        let $else := 1\n" +
      "        let $empty-sequence := 1\n" +
      "        let $empty := 1\n" +
      "        let $encoding := 1\n" +
      "        let $end := 1\n" +
      "        let $eq := 1\n" +
      "        let $every := 1\n" +
      "        let $except := 1\n" +
      "        let $external := 1\n" +
      "        let $following-sibling := 1\n" +
      "        let $following := 1\n" +
      "        let $for := 1\n" +
      "        let $function := 1\n" +
      "        let $ge := 1\n" +
      "        let $greatest := 1\n" +
      "        let $group := 1\n" +
      "        let $grouping-separator := 1\n" +
      "        let $gt := 1\n" +
      "        let $idiv := 1\n" +
      "        let $if := 1\n" +
      "        let $import := 1\n" +
      "        let $in := 1\n" +
      "        let $infinity := 1\n" +
      "        let $inherit := 1\n" +
      "        let $instance := 1\n" +
      "        let $intersect := 1\n" +
      "        let $is := 1\n" +
      "        let $item := 1\n" +
      "        let $lax := 1\n" +
      "        let $le := 1\n" +
      "        let $least := 1\n" +
      "        let $let := 1\n" +
      "        let $lt := 1\n" +
      "        let $minus-sign := 1\n" +
      "        let $mod := 1\n" +
      "        let $module := 1\n" +
      "        let $namespace-node := 1\n" +
      "        let $namespace := 1\n" +
      "        let $ne := 1\n" +
      "        let $next := 1\n" +
      "        let $no-inherit := 1\n" +
      "        let $no-preserve := 1\n" +
      "        let $node := 1\n" +
      "        let $of := 1\n" +
      "        let $only := 1\n" +
      "        let $option := 1\n" +
      "        let $or := 1\n" +
      "        let $order := 1\n" +
      "        let $ordered := 1\n" +
      "        let $ordering := 1\n" +
      "        let $parent := 1\n" +
      "        let $pattern-separator := 1\n" +
      "        let $per-mille := 1\n" +
      "        let $percent := 1\n" +
      "        let $preceding-sibling := 1\n" +
      "        let $preceding := 1\n" +
      "        let $preserve := 1\n" +
      "        let $previous := 1\n" +
      "        let $processing-instruction := 1\n" +
      "        let $return := 1\n" +
      "        let $satisfies := 1\n" +
      "        let $schema-attribute := 1\n" +
      "        let $schema-element := 1\n" +
      "        let $schema := 1\n" +
      "        let $self := 1\n" +
      "        let $sliding := 1\n" +
      "        let $some := 1\n" +
      "        let $stable := 1\n" +
      "        let $start := 1\n" +
      "        let $strict := 1\n" +
      "        let $strip := 1\n" +
      "        let $switch := 1\n" +
      "        let $text := 1\n" +
      "        let $then := 1\n" +
      "        let $to := 1\n" +
      "        let $treat := 1\n" +
      "        let $try := 1\n" +
      "        let $tumbling := 1\n" +
      "        let $type := 1\n" +
      "        let $typeswitch := 1\n" +
      "        let $union := 1\n" +
      "        let $unordered := 1\n" +
      "        let $validate := 1\n" +
      "        let $variable := 1\n" +
      "        let $version := 1\n" +
      "        let $when := 1\n" +
      "        let $where := 1\n" +
      "        let $window := 1\n" +
      "        let $xquery := 1\n" +
      "        let $zero-digit := 1\n" +
      "        return 2\n" +
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
      assertEq("2")
    );
  }

  /**
   * Purpose : keywords can be used as element name tests .
   */
  @org.junit.Test
  public void xquery30keywords2() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $x := (/)\n" +
      "        return $x\n" +
      "        /NaN\n" +
      "        /allowing\n" +
      "        /ancestor-or-self\n" +
      "        /ancestor\n" +
      "        /and\n" +
      "        /as\n" +
      "        /ascending\n" +
      "        /at\n" +
      "        /attribute\n" +
      "        /base-uri\n" +
      "        /boundary-space\n" +
      "        /by\n" +
      "        /case\n" +
      "        /cast\n" +
      "        /castable\n" +
      "        /catch\n" +
      "        /child\n" +
      "        /collation\n" +
      "        /comment\n" +
      "        /construction\n" +
      "        /context\n" +
      "        /copy-namespaces\n" +
      "        /count\n" +
      "        /decimal-format\n" +
      "        /decimal-separator\n" +
      "        /declare\n" +
      "        /default\n" +
      "        /descendant-or-self\n" +
      "        /descendant\n" +
      "        /descending\n" +
      "        /digit\n" +
      "        /div\n" +
      "        /document-node\n" +
      "        /document\n" +
      "        /element\n" +
      "        /else\n" +
      "        /empty-sequence\n" +
      "        /empty\n" +
      "        /encoding\n" +
      "        /end\n" +
      "        /eq\n" +
      "        /every\n" +
      "        /except\n" +
      "        /external\n" +
      "        /following-sibling\n" +
      "        /following\n" +
      "        /for\n" +
      "        /function\n" +
      "        /ge\n" +
      "        /greatest\n" +
      "        /group\n" +
      "        /grouping-separator\n" +
      "        /gt\n" +
      "        /idiv\n" +
      "        /if\n" +
      "        /import\n" +
      "        /in\n" +
      "        /infinity\n" +
      "        /inherit\n" +
      "        /instance\n" +
      "        /intersect\n" +
      "        /is\n" +
      "        /item\n" +
      "        /lax\n" +
      "        /le\n" +
      "        /least\n" +
      "        /let\n" +
      "        /lt\n" +
      "        /minus-sign\n" +
      "        /mod\n" +
      "        /module\n" +
      "        /namespace-node\n" +
      "        /namespace\n" +
      "        /ne\n" +
      "        /next\n" +
      "        /no-inherit\n" +
      "        /no-preserve\n" +
      "        /node\n" +
      "        /of\n" +
      "        /only\n" +
      "        /option\n" +
      "        /or\n" +
      "        /order\n" +
      "        /ordered\n" +
      "        /ordering\n" +
      "        /parent\n" +
      "        /pattern-separator\n" +
      "        /per-mille\n" +
      "        /percent\n" +
      "        /preceding-sibling\n" +
      "        /preceding\n" +
      "        /preserve\n" +
      "        /previous\n" +
      "        /processing-instruction\n" +
      "        /return\n" +
      "        /satisfies\n" +
      "        /schema-attribute\n" +
      "        /schema-element\n" +
      "        /schema\n" +
      "        /self\n" +
      "        /sliding\n" +
      "        /some\n" +
      "        /stable\n" +
      "        /start\n" +
      "        /strict\n" +
      "        /strip\n" +
      "        /switch\n" +
      "        /text\n" +
      "        /then\n" +
      "        /to\n" +
      "        /treat\n" +
      "        /try\n" +
      "        /tumbling\n" +
      "        /type\n" +
      "        /typeswitch\n" +
      "        /union\n" +
      "        /unordered\n" +
      "        /validate\n" +
      "        /variable\n" +
      "        /version\n" +
      "        /when\n" +
      "        /where\n" +
      "        /window\n" +
      "        /xquery\n" +
      "        /zero-digit\n" +
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
      assertEmpty()
    );
  }

  /**
   * Purpose : keywords can be used as element names .
   */
  @org.junit.Test
  public void xquery30keywords3() {
    final XQuery query = new XQuery(
      "\n" +
      "        <keywords>\n" +
      "           <NaN/>\n" +
      "           <allowing/>\n" +
      "           <ancestor-or-self/>\n" +
      "           <ancestor/>\n" +
      "           <and/>\n" +
      "           <as/>\n" +
      "           <ascending/>\n" +
      "           <at/>\n" +
      "           <attribute/>\n" +
      "           <base-uri/>\n" +
      "           <boundary-space/>\n" +
      "           <by/>\n" +
      "           <case/>\n" +
      "           <cast/>\n" +
      "           <castable/>\n" +
      "           <catch/>\n" +
      "           <child/>\n" +
      "           <collation/>\n" +
      "           <comment/>\n" +
      "           <construction/>\n" +
      "           <context/>\n" +
      "           <copy-namespaces/>\n" +
      "           <count/>\n" +
      "           <decimal-format/>\n" +
      "           <decimal-separator/>\n" +
      "           <declare/>\n" +
      "           <default/>\n" +
      "           <descendant-or-self/>\n" +
      "           <descendant/>\n" +
      "           <descending/>\n" +
      "           <digit/>\n" +
      "           <div/>\n" +
      "           <document-node/>\n" +
      "           <document/>\n" +
      "           <element/>\n" +
      "           <else/>\n" +
      "           <empty-sequence/>\n" +
      "           <empty/>\n" +
      "           <encoding/>\n" +
      "           <end/>\n" +
      "           <eq/>\n" +
      "           <every/>\n" +
      "           <except/>\n" +
      "           <external/>\n" +
      "           <following-sibling/>\n" +
      "           <following/>\n" +
      "           <for/>\n" +
      "           <function/>\n" +
      "           <ge/>\n" +
      "           <greatest/>\n" +
      "           <group/>\n" +
      "           <grouping-separator/>\n" +
      "           <gt/>\n" +
      "           <idiv/>\n" +
      "           <if/>\n" +
      "           <import/>\n" +
      "           <in/>\n" +
      "           <infinity/>\n" +
      "           <inherit/>\n" +
      "           <instance/>\n" +
      "           <intersect/>\n" +
      "           <is/>\n" +
      "           <item/>\n" +
      "           <lax/>\n" +
      "           <le/>\n" +
      "           <least/>\n" +
      "           <let/>\n" +
      "           <lt/>\n" +
      "           <minus-sign/>\n" +
      "           <mod/>\n" +
      "           <module/>\n" +
      "           <namespace-node/>\n" +
      "           <namespace/>\n" +
      "           <ne/>\n" +
      "           <next/>\n" +
      "           <no-inherit/>\n" +
      "           <no-preserve/>\n" +
      "           <node/>\n" +
      "           <of/>\n" +
      "           <only/>\n" +
      "           <option/>\n" +
      "           <or/>\n" +
      "           <order/>\n" +
      "           <ordered/>\n" +
      "           <ordering/>\n" +
      "           <parent/>\n" +
      "           <pattern-separator/>\n" +
      "           <per-mille/>\n" +
      "           <percent/>\n" +
      "           <preceding-sibling/>\n" +
      "           <preceding/>\n" +
      "           <preserve/>\n" +
      "           <previous/>\n" +
      "           <processing-instruction/>\n" +
      "           <return/>\n" +
      "           <satisfies/>\n" +
      "           <schema-attribute/>\n" +
      "           <schema-element/>\n" +
      "           <schema/>\n" +
      "           <self/>\n" +
      "           <sliding/>\n" +
      "           <some/>\n" +
      "           <stable/>\n" +
      "           <start/>\n" +
      "           <strict/>\n" +
      "           <strip/>\n" +
      "           <switch/>\n" +
      "           <text/>\n" +
      "           <then/>\n" +
      "           <to/>\n" +
      "           <treat/>\n" +
      "           <try/>\n" +
      "           <tumbling/>\n" +
      "           <type/>\n" +
      "           <typeswitch/>\n" +
      "           <union/>\n" +
      "           <unordered/>\n" +
      "           <validate/>\n" +
      "           <variable/>\n" +
      "           <version/>\n" +
      "           <when/>\n" +
      "           <where/>\n" +
      "           <window/>\n" +
      "           <xquery/>\n" +
      "           <zero-digit/>\n" +
      "        </keywords>/name()\n" +
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
      assertStringValue(false, "keywords")
    );
  }
}
