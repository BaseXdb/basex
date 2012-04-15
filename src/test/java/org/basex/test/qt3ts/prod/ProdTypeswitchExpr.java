package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the TypeswitchExpr production.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdTypeswitchExpr extends QT3TestSet {

  /**
   *  typeswitch test where the sequence types only differs in cardinality. .
   */
  @org.junit.Test
  public void kSequenceExprTypeswitch1() {
    final XQuery query = new XQuery(
      "(typeswitch((1, 2)) case xs:integer return -1 case xs:integer+ return 1 default return -2) eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A string literal is of type xs:string, even though it can be promoted to xs:anyURI. .
   */
  @org.junit.Test
  public void kSequenceExprTypeswitch2() {
    final XQuery query = new XQuery(
      "(typeswitch(\"a string\") case xs:anyURI return -1 case xs:string return 1 default return -2) eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  typeswitch test: A string literal is of type xs:string, even though it can be promoted to xs:untypedAtomic. .
   */
  @org.junit.Test
  public void kSequenceExprTypeswitch3() {
    final XQuery query = new XQuery(
      "(typeswitch(\"a string\") case xs:untypedAtomic return -1 case xs:string return 1 default return -2) eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A sequence of atomic items are not treated individually, but as a whole. .
   */
  @org.junit.Test
  public void kSequenceExprTypeswitch4() {
    final XQuery query = new XQuery(
      "(typeswitch((1, \"a string\")) case xs:integer return -1 case xs:string return -2 case xs:anyAtomicType+ return 1 default return -3) eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A typeswitch scenario which in some implementations trigger certain optimization code paths. .
   */
  @org.junit.Test
  public void kSequenceExprTypeswitch5() {
    final XQuery query = new XQuery(
      "(typeswitch(((1, current-time())[1])) case element() return -1 case xs:integer return 1 default return -2) eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A typeswitch scenario involving empty-sequence(). Both the 'xs:integer*' branch and the 'empty-sequnec()' branch are valid. .
   */
  @org.junit.Test
  public void kSequenceExprTypeswitch6() {
    final XQuery query = new XQuery(
      "(typeswitch(()) case xs:integer* return 1 case empty-sequence() return 1 default return -2) eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A typeswitch with an operand expression being the comma operator using no paranteses. .
   */
  @org.junit.Test
  public void kSequenceExprTypeswitch7() {
    final XQuery query = new XQuery(
      "(typeswitch(1, 2, 3) case xs:string+ return -1 case xs:integer+ return 1 default return -2) eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A typeswitch where the case clauses will never be evaluated. In some implementations this trigger optimization code paths. .
   */
  @org.junit.Test
  public void kSequenceExprTypeswitch8() {
    final XQuery query = new XQuery(
      "(typeswitch(1, 2, current-time()) case element() return -1 case document-node() return -2 default return 1) eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Extract the EBV from the result of a typeswitch. .
   */
  @org.junit.Test
  public void k2SequenceExprTypeswitch1() {
    final XQuery query = new XQuery(
      "boolean(typeswitch (current-time(), 1, 3e3, \"foo\") case node() return 0 case xs:integer return 3 case xs:anyAtomicType return true() default return -1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Typeswitch variables are not in scope outside the typeswitch expression. .
   */
  @org.junit.Test
  public void k2SequenceExprTypeswitch10() {
    final XQuery query = new XQuery(
      "typeswitch (1, 2, 3) case node() return <e/> case $i as xs:integer return 3 default return 1, $i",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0008")
    );
  }

  /**
   *  A variable declared in the default branch doesn't carry over to a subsequent typeswitch. .
   */
  @org.junit.Test
  public void k2SequenceExprTypeswitch11() {
    final XQuery query = new XQuery(
      "typeswitch (1, 2, 3) case node() return <e/> default $i return 1 , typeswitch (1, 2, 3) case xs:integer* return $i default return 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0008")
    );
  }

  /**
   *  Two nested typeswitches. .
   */
  @org.junit.Test
  public void k2SequenceExprTypeswitch12() {
    final XQuery query = new XQuery(
      "declare variable $i := (attribute name {\"content\"}, <a attr=\"content\"/>, <e/>, 1, \"str\", <!-- a comment -->); <d> { typeswitch(typeswitch($i) case $b as element(e) return concat(\"Found an element by name \", $b) case $b as element() return comment{concat(\"Found: \", $b)} case $c as attribute(doesntMatch) return $c/.. default $def return $def) case $str as xs:string return \"A string\" case $attr as attribute() return string($attr) default $def return $def } </d>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<d name=\"content\"><a attr=\"content\"/><e/>1 str<!-- a comment --></d>", false)
    );
  }

  /**
   *  A complex query that constructs nodes in the wrong order. .
   */
  @org.junit.Test
  public void k2SequenceExprTypeswitch13() {
    final XQuery query = new XQuery(
      "declare variable $i := (<e/>, attribute name {\"content\"}, <a attr=\"content\"/>, <e/>, 1, \"str\", <!-- a comment -->); <d> { typeswitch(typeswitch($i) case $b as element(e) return concat(\"Found an element by name \", $b) case $b as element() return comment{concat(\"Found: \", $b)} case $c as attribute(doesntMatch) return $c/.. default $def return $def) case $str as xs:string return \"A string\" case $attr as attribute() return string($attr) default $def return $def } </d>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQTY0024")
    );
  }

  /**
   *  Use the focus from within a typeswitch's case-branch. .
   */
  @org.junit.Test
  public void k2SequenceExprTypeswitch14() {
    final XQuery query = new XQuery(
      "<e/>/(typeswitch (self::node()) case $i as node() return . default return 1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<e/>", false)
    );
  }

  /**
   *  Use the focus from within a typeswitch's case-branch. .
   */
  @org.junit.Test
  public void k2SequenceExprTypeswitch15() {
    final XQuery query = new XQuery(
      "<e/>/(typeswitch (self::node()) case $i as xs:integer return $i default $v return $v)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<e/>", false)
    );
  }

  /**
   *  Use variables with type declarations. .
   */
  @org.junit.Test
  public void k2SequenceExprTypeswitch16() {
    final XQuery query = new XQuery(
      "typeswitch(<e/>, <e/>) case $b as element() return concat(\"\", $b treat as element()) default return 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  A default clause must be specified. .
   */
  @org.junit.Test
  public void k2SequenceExprTypeswitch2() {
    final XQuery query = new XQuery(
      "typeswitch(current-time()) case node() return 0 case xs:integer return 3 case xs:anyAtomicType return true()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Parenteses must be specified for the expression that's switched. .
   */
  @org.junit.Test
  public void k2SequenceExprTypeswitch3() {
    final XQuery query = new XQuery(
      "typeswitch 1 case node() return 0 case xs:integer return 3 default return true()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  A variable is only in scope for the case branch it is declared for. .
   */
  @org.junit.Test
  public void k2SequenceExprTypeswitch4() {
    final XQuery query = new XQuery(
      "typeswitch (1, 2, 3) case $i as node() return <e/> case xs:integer* return $i default return true()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0008")
    );
  }

  /**
   *  A variable is only in scope for the case branch it is declared for(#2). .
   */
  @org.junit.Test
  public void k2SequenceExprTypeswitch5() {
    final XQuery query = new XQuery(
      "typeswitch (1, 2, 3) case node() return $i case $i as xs:integer return 1 default return true()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0008")
    );
  }

  /**
   *  A variable is only in scope for the case branch it is declared for(#3). .
   */
  @org.junit.Test
  public void k2SequenceExprTypeswitch6() {
    final XQuery query = new XQuery(
      "typeswitch (1, 2, 3) case node() return <e/> case $i as xs:integer return 1 default return $i",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0008")
    );
  }

  /**
   *  A variable is only in scope for the case branch it is declared for(#3). .
   */
  @org.junit.Test
  public void k2SequenceExprTypeswitch7() {
    final XQuery query = new XQuery(
      "typeswitch (1, 2, 3) case node() return <e/> case xs:integer* return $i default $i return 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0008")
    );
  }

  /**
   *  A type declaration is not allowed in the default branch. .
   */
  @org.junit.Test
  public void k2SequenceExprTypeswitch8() {
    final XQuery query = new XQuery(
      "typeswitch (1, 2, 3) case node() return <e/> case xs:integer return 3 default $i as item() return 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Typeswitch variables are not in scope outside the typeswitch expression. .
   */
  @org.junit.Test
  public void k2SequenceExprTypeswitch9() {
    final XQuery query = new XQuery(
      "typeswitch (1, 2, 3) case node() return <e/> case xs:integer return 3 default $i return 1, $i",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0008")
    );
  }

  /**
   * Typeswitch with union, a match first branch only; check type of bound variable.
   */
  @org.junit.Test
  public void typeswitchUnionBranch1() {
    final XQuery query = new XQuery(
      "\n" +
      "\t typeswitch (47) \n" +
      "\t case $i as xs:integer | xs:string return $i\n" +
      "\t default $v return 0\n" +
      "\t ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("47")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Typeswitch with union, a match first branch only; check type of bound variable.
   */
  @org.junit.Test
  public void typeswitchUnionBranch1Dup() {
    final XQuery query = new XQuery(
      "\n" +
      "\t typeswitch (47) \n" +
      "\t case $i as xs:integer | xs:string return $i\n" +
      "\t case $i as xs:integer return $i\n" +
      "\t default $v return 0\n" +
      "\t ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("47")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Typeswitch with union, a match second branch only; check type of bound variable.
   */
  @org.junit.Test
  public void typeswitchUnionBranch2() {
    final XQuery query = new XQuery(
      "\n" +
      "\t typeswitch (47) \n" +
      "\t case $i as xs:string | xs:integer return $i\n" +
      "\t default $v return 0\n" +
      "\t ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("47")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Typeswitch with union, a match second branch only; check type of bound variable.
   */
  @org.junit.Test
  public void typeswitchUnionBranch2Dup() {
    final XQuery query = new XQuery(
      "\n" +
      "\t typeswitch (47) \n" +
      "\t case $i as xs:string | xs:integer return $i\n" +
      "\t case $i as xs:integer return $i\n" +
      "\t default $v return 0\n" +
      "\t ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("47")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Typeswitch with union, a match second branch only; check type of bound variable.
   */
  @org.junit.Test
  public void typeswitchUnionBranchBoth() {
    final XQuery query = new XQuery(
      "\n" +
      "\t typeswitch (xs:integer(47)) \n" +
      "\t case $i as xs:decimal | xs:integer return $i\n" +
      "\t default $v return 0\n" +
      "\t ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("47")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Typeswitch with union, a match second branch only; check type of bound variable.
   */
  @org.junit.Test
  public void typeswitchUnionBranchBothDup() {
    final XQuery query = new XQuery(
      "\n" +
      "\t typeswitch (xs:integer(47)) \n" +
      "\t case $i as xs:decimal | xs:integer return $i\n" +
      "\t case $i as xs:decimal return $i\n" +
      "\t default $v return 0\n" +
      "\t ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("47")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Typeswitch with N-way union.
   */
  @org.junit.Test
  public void typeswitchUnionMulti() {
    final XQuery query = new XQuery(
      "\n" +
      "\t for $x in (<e/>, 1, \"x\") return\n" +
      "\t typeswitch ($x) \n" +
      "\t case $i as xs:integer | xs:boolean | element() return 1\n" +
      "\t default $v return 0\n" +
      "\t ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("1, 1, 0")
    );
  }

  /**
   * Typeswitch with union, no match.
   */
  @org.junit.Test
  public void typeswitchUnionNomatch() {
    final XQuery query = new XQuery(
      "\n" +
      "\t typeswitch (<e/>) \n" +
      "\t case $i as xs:integer | xs:string return $i\n" +
      "\t default $v return 0\n" +
      "\t ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   * Typeswitch with union, no match.
   */
  @org.junit.Test
  public void typeswitchUnionNomatch2() {
    final XQuery query = new XQuery(
      "\n" +
      "\t typeswitch (<e/>) \n" +
      "\t case xs:integer | xs:string return 1\n" +
      "\t default $v return 0\n" +
      "\t ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Simple test for evaluation of atomic value (integer) and various data types for case evaluation .
   */
  @org.junit.Test
  public void typeswitchhc1() {
    final XQuery query = new XQuery(
      "typeswitch (5) case $i as xs:integer return <wrap>test passed - integer data type</wrap> case $i as xs:date return <wrap>test failed</wrap> case $i as xs:time return <wrap>test failed</wrap> case $i as xs:string return <wrap>test failed</wrap> default return <wrap>test failed</wrap>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<wrap>test passed - integer data type</wrap>", false)
    );
  }

  /**
   *  Simple test for evaluation of simple anyURI expression and various data types for case evaluation .
   */
  @org.junit.Test
  public void typeswitchhc10() {
    final XQuery query = new XQuery(
      "typeswitch(xs:anyURI(\"http://example.com\")) case $i as xs:string return <wrap>test failed</wrap> case $i as xs:integer return <wrap>test failed</wrap> case $i as xs:anyURI return <wrap>test passed - xs:anyURI(\"http://www.example.com\")is of anyURI type</wrap> default return <wrap>test failed</wrap>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<wrap>test passed - xs:anyURI(\"http://www.example.com\")is of anyURI type</wrap>", false)
    );
  }

  /**
   *  Simple test for evaluation of various expressions and should mathc the deafult value .
   */
  @org.junit.Test
  public void typeswitchhc11() {
    final XQuery query = new XQuery(
      "typeswitch(123) case $i as xs:string return <wrap>test failed</wrap> case $i as xs:double return <wrap>test failed</wrap> case $i as xs:anyURI return <wrap>test failed</wrap> default return <wrap>test passed - 123 is an integer (not an option on any cases)</wrap>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<wrap>test passed - 123 is an integer (not an option on any cases)</wrap>", false)
    );
  }

  /**
   *  Simple test for evaluating dynamic error raised by default clause (no error raised) .
   */
  @org.junit.Test
  public void typeswitchhc12() {
    final XQuery query = new XQuery(
      "typeswitch(123) case $i as xs:string return <wrap>test failed</wrap> case $i as xs:double return <wrap>test failed</wrap> case $i as xs:integer return <wrap>test passed - If a dynamic error is generated, then test failed.</wrap> default return 12 div 0",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<wrap>test passed - If a dynamic error is generated, then test failed.</wrap>", false)
    );
  }

  /**
   *  Simple test for typeswitch with operand expression (if Expression) evaluating to an integer. .
   */
  @org.junit.Test
  public void typeswitchhc13() {
    final XQuery query = new XQuery(
      "typeswitch(if (1 lt 2) then 3 else 4.5E4) case $i as xs:string return <wrap>test failed</wrap> case $i as xs:double return <wrap>test failed</wrap> case $i as xs:integer return <wrap>test passed - \"(1 lt 2) then 3 else 4.5E4\" should evaluate to an integer</wrap> default return <wrap>test failed</wrap>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<wrap>test passed - \"(1 lt 2) then 3 else 4.5E4\" should evaluate to an integer</wrap>", false)
    );
  }

  /**
   *  Simple test for typeswitch with operand expression (boolean) evaluating to boolean type .
   */
  @org.junit.Test
  public void typeswitchhc14() {
    final XQuery query = new XQuery(
      "typeswitch(fn:true() and fn:true()) case $i as xs:string return <wrap>test failed</wrap> case $i as xs:double return <wrap>test failed</wrap> case $i as xs:boolean return <wrap>test passed - \"fn:true() and fn:true()\" should evaluate to boolean type</wrap> default return <wrap>test failed</wrap>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<wrap>test passed - \"fn:true() and fn:true()\" should evaluate to boolean type</wrap>", false)
    );
  }

  /**
   *  Simple test for typeswitch with operand expression (boolean or boolean) evaluating to boolean type .
   */
  @org.junit.Test
  public void typeswitchhc15() {
    final XQuery query = new XQuery(
      "typeswitch(fn:true() or fn:false()) case $i as xs:string return <wrap>test failed</wrap> case $i as xs:double return <wrap>test failed</wrap> case $i as xs:boolean return <wrap>test passed - \"fn:true() or fn:false()\" should evaluate to boolean type</wrap> default return <wrap>test failed</wrap>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<wrap>test passed - \"fn:true() or fn:false()\" should evaluate to boolean type</wrap>", false)
    );
  }

  /**
   *  Simple test for typeswitch with operand expression, that is itself a typeswitch expression evaluating to integer type .
   */
  @org.junit.Test
  public void typeswitchhc16() {
    final XQuery query = new XQuery(
      "typeswitch(typeswitch (1) case $i as xs:integer return $i default return <a>fn:false</a> ) case $i as xs:string return <wrap>test failed</wrap> case $i as xs:double return <wrap>test failed</wrap> case $i as xs:integer return <wrap>test passed - the operand expression should evaluate to an integer type</wrap> default return <wrap>test failed</wrap>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<wrap>test passed - the operand expression should evaluate to an integer type</wrap>", false)
    );
  }

  /**
   *  Simple test for evaluation of atomic value (integer) and return contains integer expression .
   */
  @org.junit.Test
  public void typeswitchhc17() {
    final XQuery query = new XQuery(
      "typeswitch (5) case $i as xs:integer return xs:integer(1 + 1) case $i as xs:date return <wrap>test failed</wrap> case $i as xs:time return <wrap>test failed</wrap> case $i as xs:string return <wrap>test failed</wrap> default return <wrap>test failed</wrap>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("2")
    );
  }

  /**
   *  Simple test for evaluation of atomic value (decimal) and return clause conatining an arithmetic expression with decimals .
   */
  @org.junit.Test
  public void typeswitchhc18() {
    final XQuery query = new XQuery(
      "typeswitch (5.1) case $i as xs:decimal return xs:decimal(1.1 + 3.1) case $i as xs:float return <wrap>test failed</wrap> case $i as xs:integer return <wrap>test failed</wrap> case $i as xs:double return <wrap>test failed</wrap> default return <wrap>test failed</wrap>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "4.2")
    );
  }

  /**
   *  Simple test for evaluation of atomic value (double) and return clause containing a double addition operation. .
   */
  @org.junit.Test
  public void typeswitchhc19() {
    final XQuery query = new XQuery(
      "typeswitch (5.1E2) case $i as xs:integer return <wrap>test failed2</wrap> case $i as xs:double return xs:double(5.1E2 + 1.1E2) default return <wrap>test failed</wrap>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("620")
    );
  }

  /**
   *  Simple test for evaluation of atomic value (decimal) and various data types for case evaluation .
   */
  @org.junit.Test
  public void typeswitchhc2() {
    final XQuery query = new XQuery(
      "typeswitch (5.1) case $i as xs:decimal return <wrap>test passed - 5.1 is a decimal type</wrap> case $i as xs:float return <wrap>test failed</wrap> case $i as xs:integer return <wrap>test failed</wrap> case $i as xs:double return <wrap>test failed</wrap> default return <wrap>test failed</wrap>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<wrap>test passed - 5.1 is a decimal type</wrap>", false)
    );
  }

  /**
   *  Simple test for evaluation of atomic value (string) and return clause containing a string operation .
   */
  @org.junit.Test
  public void typeswitchhc20() {
    final XQuery query = new XQuery(
      "typeswitch (\"A String\") case $i as xs:decimal return <wrap>test failed</wrap> case $i as xs:integer return <wrap>test failed</wrap> case $i as xs:string return fn:string-length($i) default return <wrap>test failed</wrap>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("8")
    );
  }

  /**
   *  Simple test for evaluation of atomic value ("1") and return clause containing an fn:count() .
   */
  @org.junit.Test
  public void typeswitchhc21() {
    final XQuery query = new XQuery(
      "typeswitch (1) case $i as xs:double return <wrap>test failed</wrap> case $i as xs:integer return fn:count((1, 2, 3)) case $i as xs:string return <wrap>test failed</wrap> default return <wrap>test failed</wrap>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("3")
    );
  }

  /**
   *  Simple test for evaluation of atomic value ("1") and return clause containing a subtraction operation .
   */
  @org.junit.Test
  public void typeswitchhc22() {
    final XQuery query = new XQuery(
      "typeswitch (1) case $i as xs:double return <wrap>test failed</wrap> case $i as xs:integer return 5 - 3 case $i as xs:string return <wrap>test failed</wrap> default return <wrap>test failed</wrap>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("2")
    );
  }

  /**
   *  Simple test for evaluation of atomic value ("1") and return clause containing a multiplication operation .
   */
  @org.junit.Test
  public void typeswitchhc23() {
    final XQuery query = new XQuery(
      "typeswitch (1) case $i as xs:double return <wrap>test failed</wrap> case $i as xs:integer return 5 * 2 case $i as xs:string return <wrap>test failed</wrap> default return <wrap>test failed</wrap>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("10")
    );
  }

  /**
   *  Simple test for evaluation of atomic value ("1") and return clause containing a division (div) operation .
   */
  @org.junit.Test
  public void typeswitchhc24() {
    final XQuery query = new XQuery(
      "typeswitch (1) case $i as xs:double return <wrap>test failed</wrap> case $i as xs:integer return 10 div 2 case $i as xs:string return <wrap>test failed</wrap> default return <wrap>test failed</wrap>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("5")
    );
  }

  /**
   *  Simple test for evaluation of atomic value ("1") and return clause containing a division (idiv) operation .
   */
  @org.junit.Test
  public void typeswitchhc25() {
    final XQuery query = new XQuery(
      "typeswitch (1) case $i as xs:double return <wrap>test failed</wrap> case $i as xs:integer return 10 idiv 2 case $i as xs:string return <wrap>test failed</wrap> default return <wrap>test failed</wrap>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("5")
    );
  }

  /**
   *  Simple test for evaluation of atomic value (double) and various data types for case evaluation .
   */
  @org.junit.Test
  public void typeswitchhc3() {
    final XQuery query = new XQuery(
      "typeswitch (5.1E2) case $i as xs:integer return <wrap>test failed2</wrap> case $i as xs:double return <wrap>test passed - 5.1E2 is a double type</wrap> default return <wrap>test failed</wrap>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<wrap>test passed - 5.1E2 is a double type</wrap>", false)
    );
  }

  /**
   *  Simple test for evaluation of atomic value (string) and various data types for case evaluation .
   */
  @org.junit.Test
  public void typeswitchhc4() {
    final XQuery query = new XQuery(
      "typeswitch (\"A String\") case $i as xs:decimal return <wrap>test failed</wrap> case $i as xs:integer return <wrap>test failed</wrap> case $i as xs:string return <wrap>test passed - \"A String\" is a string type</wrap> default return <wrap>test failed</wrap>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<wrap>test passed - \"A String\" is a string type</wrap>", false)
    );
  }

  /**
   *  Simple test for evaluation of atomic value (float) and various data types for case evaluation .
   */
  @org.junit.Test
  public void typeswitchhc5() {
    final XQuery query = new XQuery(
      "typeswitch (1267.43233E12) case $i as xs:string return <wrap>test failed</wrap> case $i as xs:integer return <wrap>test failed</wrap> case $i as xs:double return <wrap>test passed - 1267.43233E12 is a double type</wrap> default return <wrap>test failed</wrap>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<wrap>test passed - 1267.43233E12 is a double type</wrap>", false)
    );
  }

  /**
   *  Simple test for evaluation of simple boolean expression and various data types for case evaluation .
   */
  @org.junit.Test
  public void typeswitchhc6() {
    final XQuery query = new XQuery(
      "typeswitch(1 > 2) case $i as xs:string return <wrap>test failed</wrap> case $i as xs:integer return <wrap>test failed</wrap> case $i as xs:boolean return <wrap>test passed - 1 > 2 is a boolean type</wrap> default return <wrap>test failed</wrap>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<wrap>test passed - 1 &gt; 2 is a boolean type</wrap>", false)
    );
  }

  /**
   *  Simple test for evaluation of simple date expression and various data types for case evaluation .
   */
  @org.junit.Test
  public void typeswitchhc7() {
    final XQuery query = new XQuery(
      "typeswitch(xs:date(\"1999-05-31\")) case $i as xs:string return <wrap>test failed</wrap> case $i as xs:integer return <wrap>test failed</wrap> case $i as xs:date return <wrap>test passed - xs:date(\"1999-05-31\")is of date type</wrap> default return <wrap>test failed</wrap>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<wrap>test passed - xs:date(\"1999-05-31\")is of date type</wrap>", false)
    );
  }

  /**
   *  Simple test for evaluation of simple time expression and various data types for case evaluation .
   */
  @org.junit.Test
  public void typeswitchhc8() {
    final XQuery query = new XQuery(
      "typeswitch(xs:time(\"12:00:00\")) case $i as xs:string return <wrap>test failed</wrap> case $i as xs:integer return <wrap>test failed</wrap> case $i as xs:time return <wrap>test passed - xs:time(\"12:00:00\")is of time type</wrap> default return <wrap>test failed</wrap>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<wrap>test passed - xs:time(\"12:00:00\")is of time type</wrap>", false)
    );
  }

  /**
   *  Simple test for evaluation of simple dateTime expression and various data types for case evaluation .
   */
  @org.junit.Test
  public void typeswitchhc9() {
    final XQuery query = new XQuery(
      "typeswitch(xs:dateTime(\"1999-12-31T19:20:00\")) case $i as xs:string return <wrap>test failed</wrap> case $i as xs:integer return <wrap>test failed</wrap> case $i as xs:dateTime return <wrap>test passed - xs:dateTime(\"1999-12-31T19:20:00\")is of dateTime type</wrap> default return <wrap>test failed</wrap>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<wrap>test passed - xs:dateTime(\"1999-12-31T19:20:00\")is of dateTime type</wrap>", false)
    );
  }
}
