package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the VarDecl.external production.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdVarDeclExternal extends QT3TestSet {

  /**
   *  Two external variables with the same name where the first has a type declaration. .
   */
  @org.junit.Test
  public void k2ExternalVariablesWith1() {
    final XQuery query = new XQuery(
      "declare variable $input-context as item()* external; declare variable $input-context external; 1",
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
   *  An implementation may raise XPDY0002 on a declared external variable, although not necessary since it isn't used(#2). .
   */
  @org.junit.Test
  public void k2ExternalVariablesWith10() {
    final XQuery query = new XQuery(
      "declare namespace e = \"http://example.com/ANamespace\"; declare variable $e:exampleComThisVarIsNotRecognized as element(*) external; 1",
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
   *  The assignment expression is ExprSingle, not Expr. .
   */
  @org.junit.Test
  public void k2ExternalVariablesWith11() {
    final XQuery query = new XQuery(
      "declare variable $i := 1, 1; 1",
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
   *  Variable declarations doesn't cause type conversion. .
   */
  @org.junit.Test
  public void k2ExternalVariablesWith12() {
    final XQuery query = new XQuery(
      "declare variable $i as xs:integer := xs:untypedAtomic(\"1\"); $i",
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
   *  variable declarations doesn't cause numeric promotion. .
   */
  @org.junit.Test
  public void k2ExternalVariablesWith13() {
    final XQuery query = new XQuery(
      "declare variable $i as xs:float := 1.1 ; $i",
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
   *  variable declarations doesn't cause numeric promotion(#2). .
   */
  @org.junit.Test
  public void k2ExternalVariablesWith14() {
    final XQuery query = new XQuery(
      "declare variable $i as xs:double := 1.1 ; $i",
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
   *  variable declarations doesn't cause numeric promotion(#3). .
   */
  @org.junit.Test
  public void k2ExternalVariablesWith15() {
    final XQuery query = new XQuery(
      "declare variable $i as xs:float := 1 ; $i",
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
   *  variable declarations doesn't cause numeric promotion(#4). .
   */
  @org.junit.Test
  public void k2ExternalVariablesWith16() {
    final XQuery query = new XQuery(
      "declare variable $i as xs:double := 1 ; $i",
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
   *  variable declarations doesn't cause numeric promotion(#5). .
   */
  @org.junit.Test
  public void k2ExternalVariablesWith17() {
    final XQuery query = new XQuery(
      "declare variable $i as xs:double := xs:float(3) ; $i",
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
   *  variable declarations doesn't cause string promotion conversion. .
   */
  @org.junit.Test
  public void k2ExternalVariablesWith18() {
    final XQuery query = new XQuery(
      "declare variable $i as xs:string := xs:untypedAtomic(\"a string\") ; $i",
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
   *  variable declarations doesn't cause URI promotion conversion. .
   */
  @org.junit.Test
  public void k2ExternalVariablesWith19() {
    final XQuery query = new XQuery(
      "declare variable $i as xs:string := xs:anyURI(\"http://www.example.com/\") ; $i",
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
   *  Two external variables with the same name where the last has a type declaration. .
   */
  @org.junit.Test
  public void k2ExternalVariablesWith2() {
    final XQuery query = new XQuery(
      "declare variable $input-context external; declare variable $input-context as item()* external; 1",
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
   *  The name of an external variable clashing with the name of a variable declared in the query. .
   */
  @org.junit.Test
  public void k2ExternalVariablesWith20() {
    final XQuery query = new XQuery(
      "declare variable $input-context1 external; declare variable $input-context1 := 1; 1",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TopMany.xml")));
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
   *  The name of an external variable clashing with the name of a variable declared in the query(reversed order). .
   */
  @org.junit.Test
  public void k2ExternalVariablesWith21() {
    final XQuery query = new XQuery(
      "declare variable $input-context1 external; declare variable $input-context1 := 1; 1",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TopMany.xml")));
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
   *  A complex type declaration. .
   */
  @org.junit.Test
  public void k2ExternalVariablesWith22() {
    final XQuery query = new XQuery(
      "declare variable $v as element(*, xs:untyped?)+ := <e/>; 1",
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
   *  A complex type declaration. .
   */
  @org.junit.Test
  public void k2ExternalVariablesWith23() {
    final XQuery query = new XQuery(
      "declare variable $v as element(elementName, xs:anyType?)+ := <elementName/>; 1",
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
   *  '+' is not allowed for the atomic type in element(). .
   */
  @org.junit.Test
  public void k2ExternalVariablesWith24() {
    final XQuery query = new XQuery(
      "declare variable $v as element(*, xs:untyped+)+ := <e/>; 1",
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
   *  '*' is not allowed for the atomic type in element(). .
   */
  @org.junit.Test
  public void k2ExternalVariablesWith25() {
    final XQuery query = new XQuery(
      "declare variable $v as element(*, xs:untyped*)+ := <e/>; 1",
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
   *  '+' is not allowed for the atomic type in element(). .
   */
  @org.junit.Test
  public void k2ExternalVariablesWith26() {
    final XQuery query = new XQuery(
      "declare variable $v as element(notWildcard, xs:untyped+)+ := <e/>; 1",
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
   *  '*' is not allowed for the atomic type in element(). .
   */
  @org.junit.Test
  public void k2ExternalVariablesWith27() {
    final XQuery query = new XQuery(
      "declare variable $v as element(notWildcard, xs:untyped*)+ := <e/>; 1",
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
   *  Two external variables with the same name where both have a type declaration. .
   */
  @org.junit.Test
  public void k2ExternalVariablesWith3() {
    final XQuery query = new XQuery(
      "declare variable $input-context as item()* external ; declare variable $input-context as item()*external ; 1",
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
   *  Two external variables with the same name where both have a type declaration and the variable is used. .
   */
  @org.junit.Test
  public void k2ExternalVariablesWith4() {
    final XQuery query = new XQuery(
      "declare variable $input-context as item()* external ; declare variable $input-context as item()*external ; $input-context",
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
   *  Two external variables with the same name where both have a type declaration. .
   */
  @org.junit.Test
  public void k2ExternalVariablesWith5() {
    final XQuery query = new XQuery(
      "declare variable $input-context as item()* external; declare variable $input-context as item()*external; 1",
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
   *  Two external variables with the same name where both have a type declaration which are different. .
   */
  @org.junit.Test
  public void k2ExternalVariablesWith6() {
    final XQuery query = new XQuery(
      "declare variable $input-context as xs:string* external; declare variable $input-context as item()*external; 1",
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
   *  If the value for an external variable can't be supplied, XPDY0002 must be raised. .
   */
  @org.junit.Test
  public void k2ExternalVariablesWith7() {
    final XQuery query = new XQuery(
      "declare variable $exampleComThisVarIsNotRecognized as xs:string *external; $exampleComThisVarIsNotRecognized",
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
   *  If the value for an external variable can't be supplied, XPDY0002 must be raised(#2). .
   */
  @org.junit.Test
  public void k2ExternalVariablesWith8() {
    final XQuery query = new XQuery(
      "declare namespace e = \"http://example.com/ANamespace\"; declare variable $e:exampleComThisVarIsNotRecognized as comment() *external; $e:exampleComThisVarIsNotRecognized",
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
   *  An implementation may raise XPDY0002 on a declared external variable, although not necessary since it isn't used. .
   */
  @org.junit.Test
  public void k2ExternalVariablesWith9() {
    final XQuery query = new XQuery(
      "declare variable $exampleComThisVarIsNotRecognized as processing-instruction()? external; 1",
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
   *  Two external variables with the same name. .
   */
  @org.junit.Test
  public void k2ExternalVariablesWithout1() {
    final XQuery query = new XQuery(
      "declare variable $input-context external; declare variable $input-context external; 1",
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
   *  Ensure node identity is handled through several variables. .
   */
  @org.junit.Test
  public void k2ExternalVariablesWithout10() {
    final XQuery query = new XQuery(
      "declare variable $e := <e> <a/> </e>; declare variable $f := $e; <r> { $e is $e, $f is $e, $e, $f } </r>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<r>true true<e><a/></e><e><a/></e></r>", false)
    );
  }

  /**
   *  Description ensure node identity is handled through variables that has cardinality zero or more. .
   */
  @org.junit.Test
  public void k2ExternalVariablesWithout11() {
    final XQuery query = new XQuery(
      "declare variable $a as attribute()* := (attribute name1 {()}, attribute name2 {()}, attribute name3 {()}); declare variable $b as attribute()* := (attribute name1 {()}, attribute name2 {()}, attribute name3 {()}); $a/(let $p := position() return . is $b[$p])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "false false false")
    );
  }

  /**
   *  Reference a variable from two different node constructors. .
   */
  @org.junit.Test
  public void k2ExternalVariablesWithout12() {
    final XQuery query = new XQuery(
      "declare variable $a as attribute()* := (attribute name1 {()}, attribute name2 {()}, attribute name3 {()}); <r> <e> { $a } </e> <e> { $a } </e> </r>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<r><e name1=\"\" name2=\"\" name3=\"\"/><e name1=\"\" name2=\"\" name3=\"\"/></r>", false)
    );
  }

  /**
   *  Evaluate the boolean value of a variable. .
   */
  @org.junit.Test
  public void k2ExternalVariablesWithout13() {
    final XQuery query = new XQuery(
      "declare variable $e := <e>{current-time()}</e>/(string-length(.) > 0); $e, if($e) then \"SUCCESS\" else \"FAILURE\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "true SUCCESS")
    );
  }

  /**
   *  Double colon in assignment is syntactically invalid. .
   */
  @org.junit.Test
  public void k2ExternalVariablesWithout14() {
    final XQuery query = new XQuery(
      "declare variable $v ::= 1; 1",
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
   *  Space in assignment is syntactically invalid. .
   */
  @org.junit.Test
  public void k2ExternalVariablesWithout15() {
    final XQuery query = new XQuery(
      "declare variable $v : = 1; 1",
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
   *  Ensure that a start looking like a declaration, isn't treated as so. .
   */
  @org.junit.Test
  public void k2ExternalVariablesWithout16() {
    final XQuery query = new XQuery(
      "declare ne gt",
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
   *  Ensure the 'variable' keyword is parsed correctly. .
   */
  @org.junit.Test
  public void k2ExternalVariablesWithout17() {
    final XQuery query = new XQuery(
      "variable lt variable",
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
   *  XQuery 1.0 doesn't allow externals to have a default value. .
   */
  @org.junit.Test
  public void k2ExternalVariablesWithout18() {
    xquery10();
    final XQuery query = new XQuery(
      "declare variable $var external := 1; 1",
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
   *  XQuery 3.0 does allow externals to have a default value. .
   */
  @org.junit.Test
  public void k2ExternalVariablesWithout18b() {
    final XQuery query = new XQuery(
      "declare variable $var external := 1; 1",
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
   *  Ensure XPDY0002 is raised for global variables if there's no focus defined. .
   */
  @org.junit.Test
  public void k2ExternalVariablesWithout19() {
    final XQuery query = new XQuery(
      "declare variable $global := count(*); <e/>/$global",
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
   *  Two external variables with the same name and where the variable is used. .
   */
  @org.junit.Test
  public void k2ExternalVariablesWithout2() {
    final XQuery query = new XQuery(
      "declare variable $input-context external; declare variable $input-context external; $input-context",
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
   *  Ensure XPDY0002 is raised for global variables if there's no focus defined. Reference the variable twice. .
   */
  @org.junit.Test
  public void k2ExternalVariablesWithout20() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $global := count(*); \n" +
      "        <e/>/($global, $global)",
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
   *  Use the global focus in various ways three references. .
   */
  @org.junit.Test
  public void k2ExternalVariablesWithout21() {
    final XQuery query = new XQuery(
      "declare variable $global := count(*); $global, <wrongFocus> <e1/> <e2/> </wrongFocus>/$global, $global",
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
      assertStringValue(false, "1 1 1")
    );
  }

  /**
   *  Use the global focus in various ways three references. .
   */
  @org.junit.Test
  public void k2ExternalVariablesWithout22() {
    final XQuery query = new XQuery(
      "declare variable $global := count(*); <wrongFocus> <e1/> <e2/> </wrongFocus>/$global",
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
   *  An external variable using an undeclared prefix. .
   */
  @org.junit.Test
  public void k2ExternalVariablesWithout3() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default element namespace \"\"; \n" +
      "        declare namespace prefix = \"\"; \n" +
      "        declare variable $prefix:input-context external; 1",
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
   *  If the value for an external variable can't be supplied, XPDY0002 must be raised. .
   */
  @org.junit.Test
  public void k2ExternalVariablesWithout4() {
    final XQuery query = new XQuery(
      "\n" +
      "        \n" +
      "        declare variable $exampleComThisVarIsNotRecognized external; $exampleComThisVarIsNotRecognized",
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
   *  If the value for an external variable can't be supplied, XPDY0002 must be raised(#2). .
   */
  @org.junit.Test
  public void k2ExternalVariablesWithout5() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace e = \"http://example.com/ANamespace\"; \n" +
      "        declare variable $e:exampleComThisVarIsNotRecognized external; \n" +
      "        $e:exampleComThisVarIsNotRecognized",
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
   *  An implementation may raise XPDY0002 on a declared external variable, although not necessary since it isn't used. .
   */
  @org.junit.Test
  public void k2ExternalVariablesWithout6() {
    final XQuery query = new XQuery(
      "declare variable $exampleComThisVarIsNotRecognized external; 1",
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
   *  An implementation may raise XPDY0002 on a declared external variable, although not necessary since it isn't used(#2). .
   */
  @org.junit.Test
  public void k2ExternalVariablesWithout7() {
    final XQuery query = new XQuery(
      "declare namespace e = \"http://example.com/ANamespace\"; declare variable $e:exampleComThisVarIsNotRecognized external; 1",
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
   *  It is ok to have space between '$' and the name in variable names. .
   */
  @org.junit.Test
  public void k2ExternalVariablesWithout8() {
    final XQuery query = new XQuery(
      "declare variable $ name := 3; $ name",
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
   *  Ensure a variable declaration doesn't violate stability rules. .
   */
  @org.junit.Test
  public void k2ExternalVariablesWithout9() {
    final XQuery query = new XQuery(
      "declare variable $e := current-time(); let $i := ($e, 1 to 50000, $e) return $i[1] = $i[last()]",
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
   *  Evaluates an external variable declaration without type .
   */
  @org.junit.Test
  public void extvardeclwithouttype1() {
    final XQuery query = new XQuery(
      "declare variable $x external; fn:string($x)",
      ctx);
    try {
      query.bind("x", new XQuery("'abc'", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "abc")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  Evaluates an external variable that adds two values Only THIS query performs the operation. .
   */
  @org.junit.Test
  public void extvardeclwithouttype10() {
    final XQuery query = new XQuery(
      "declare variable $x external; $x + $x",
      ctx);
    try {
      query.bind("x", new XQuery("1", ctx).value());
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
        error("XPTY0004")
      )
    );
  }

  /**
   *  Evaluates an external variable that multiply two values Both queries perform the operation. .
   */
  @org.junit.Test
  public void extvardeclwithouttype11() {
    final XQuery query = new XQuery(
      "declare variable $x external; $x * $x",
      ctx);
    try {
      query.bind("x", new XQuery("2 * 2", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("16")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  Evaluates an external variable that subtract two values Both queries perform the operation. .
   */
  @org.junit.Test
  public void extvardeclwithouttype12() {
    final XQuery query = new XQuery(
      "declare variable $x external; $x - 1",
      ctx);
    try {
      query.bind("x", new XQuery("4 - 1", ctx).value());
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
        error("XPTY0004")
      )
    );
  }

  /**
   *  Evaluates an external variable that divides two values Both queries perform the operation. .
   */
  @org.junit.Test
  public void extvardeclwithouttype13() {
    final XQuery query = new XQuery(
      "declare variable $x external; $x idiv 2",
      ctx);
    try {
      query.bind("x", new XQuery("20 idiv 2", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("5")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  Evaluates an external variable that divides (div operator) two values Both queries perform the operation. .
   */
  @org.junit.Test
  public void extvardeclwithouttype14() {
    final XQuery query = new XQuery(
      "declare variable $x external; $x div 10",
      ctx);
    try {
      query.bind("x", new XQuery("40 div 2", ctx).value());
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
        error("XPTY0004")
      )
    );
  }

  /**
   *  Evaluates an external variable that perform modulus operation on two values Both queries perform the operation. .
   */
  @org.junit.Test
  public void extvardeclwithouttype15() {
    final XQuery query = new XQuery(
      "declare variable $x external; $x mod 2",
      ctx);
    try {
      query.bind("x", new XQuery("55 mod 3", ctx).value());
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
   *  Evaluates an external variable that evaluates a boolean expression Both queries perform the operation. .
   */
  @org.junit.Test
  public void extvardeclwithouttype16() {
    final XQuery query = new XQuery(
      "declare variable $x external; $x and fn:false()",
      ctx);
    try {
      query.bind("x", new XQuery("true() and true()", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertBoolean(false)
      ||
        error("FORG0006")
      )
    );
  }

  /**
   *  Evaluates an external variable that evaluates a boolean expression Both queries perform the operation. .
   */
  @org.junit.Test
  public void extvardeclwithouttype17() {
    final XQuery query = new XQuery(
      "declare variable $x external; $x or fn:false()",
      ctx);
    try {
      query.bind("x", new XQuery("true() or true()", ctx).value());
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
        error("FORG0006")
      )
    );
  }

  /**
   *  Evaluates an external variable that evaluates avg function. .
   */
  @org.junit.Test
  public void extvardeclwithouttype18() {
    final XQuery query = new XQuery(
      "declare variable $x external; $x",
      ctx);
    try {
      query.bind("x", new XQuery("avg((1,2,4))", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "2.333333333333333333")
      ||
        assertStringValue(false, "2.333333333333")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  Evaluates an external variable that attempts to cast an incompatible value. .
   */
  @org.junit.Test
  public void extvardeclwithouttype19() {
    final XQuery query = new XQuery(
      "declare variable $x external; xs:dateTime($x)",
      ctx);
    try {
      query.bind("x", new XQuery("avg((1,2,4))", ctx).value());
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
   *  Evaluates an external variable declaration without type .
   */
  @org.junit.Test
  public void extvardeclwithouttype2() {
    final XQuery query = new XQuery(
      "declare variable $x external; $x",
      ctx);
    try {
      query.bind("x", new XQuery("'2006-02-07+05:00'", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "2006-02-07+05:00")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  Evaluates an external variable whose case is changed. .
   */
  @org.junit.Test
  public void extvardeclwithouttype20() {
    final XQuery query = new XQuery(
      "declare variable $x external; fn:upper-case($x)",
      ctx);
    try {
      query.bind("x", new XQuery("lower-case('This String should be all in upper case')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "THIS STRING SHOULD BE ALL IN UPPER CASE")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  Evaluates an external variable whose case is changed. .
   */
  @org.junit.Test
  public void extvardeclwithouttype21() {
    final XQuery query = new XQuery(
      "declare variable $x external; fn:lower-case($x)",
      ctx);
    try {
      query.bind("x", new XQuery("upper-case('THIS STRING SHOULD ALL BE IN LOWER CASE')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "this string should all be in lower case")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  Evaluates an external variable with no type whose value is succesfully casted. .
   */
  @org.junit.Test
  public void extvardeclwithouttype22() {
    final XQuery query = new XQuery(
      "declare variable $x external; fn:not($x)",
      ctx);
    try {
      query.bind("x", new XQuery("0 + 1", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertBoolean(false)
      ||
        error("FORG0006")
      )
    );
  }

  /**
   *  Evaluates an external variable reference, with no assigned value. .
   */
  @org.junit.Test
  public void extvardeclwithouttype23() {
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
   *  Evaluates an external variable declaration without type Use type xs:integer. .
   */
  @org.junit.Test
  public void extvardeclwithouttype3() {
    final XQuery query = new XQuery(
      "declare variable $x external; xs:integer($x)",
      ctx);
    try {
      query.bind("x", new XQuery("2", ctx).value());
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
        error("XPTY0004")
      )
    );
  }

  /**
   *  Evaluates an external variable declaration without type .
   */
  @org.junit.Test
  public void extvardeclwithouttype4() {
    final XQuery query = new XQuery(
      "declare variable $x external; $x",
      ctx);
    try {
      query.bind("x", new XQuery("1.2E2", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("120")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  Evaluates an external variable declaration without type .
   */
  @org.junit.Test
  public void extvardeclwithouttype5() {
    final XQuery query = new XQuery(
      "declare variable $x external; $x",
      ctx);
    try {
      query.bind("x", new XQuery("let $var := 1 return $var", ctx).value());
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
   *  Evaluates an external variable declaration without type Use type xs:float. .
   */
  @org.junit.Test
  public void extvardeclwithouttype6() {
    final XQuery query = new XQuery(
      "declare variable $x external; xs:float($x)",
      ctx);
    try {
      query.bind("x", new XQuery("12.5E10", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "1.25E11")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  Evaluates an external variable declaration without type .
   */
  @org.junit.Test
  public void extvardeclwithouttype7() {
    final XQuery query = new XQuery(
      "declare variable $x external; $x",
      ctx);
    try {
      query.bind("x", new XQuery("12678967.543233", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "12678967.543233")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  Evaluates an external variable that adds two integers Both queries perform the operation .
   */
  @org.junit.Test
  public void extvardeclwithouttype8() {
    final XQuery query = new XQuery(
      "declare variable $x external; $x + $x",
      ctx);
    try {
      query.bind("x", new XQuery("1 + 1", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("4")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  Evaluates an external variable that adds two values This query does NOT performs the operation .
   */
  @org.junit.Test
  public void extvardeclwithouttype9() {
    final XQuery query = new XQuery(
      "declare variable $x external; $x",
      ctx);
    try {
      query.bind("x", new XQuery("1 + 1", ctx).value());
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
        error("XPTY0004")
      )
    );
  }

  /**
   *  Evaluates an external variable declaration with type Use type xs:string. .
   */
  @org.junit.Test
  public void extvardeclwithtype1() {
    final XQuery query = new XQuery(
      "declare variable $x as xs:string external; $x",
      ctx);
    try {
      query.bind("x", new XQuery("(: Name: extvardeclwithtypetobind-1 :) (: Description: Binding a string value for extvardeclwithtype-1.:) \"abc\"", ctx).value());
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
   *  Evaluates an external variable that adds two integers Only THIS query performs the operation. .
   */
  @org.junit.Test
  public void extvardeclwithtype10() {
    final XQuery query = new XQuery(
      "declare variable $x as xs:integer external; $x + $x",
      ctx);
    try {
      query.bind("x", new XQuery("let $var := 1 return $var", ctx).value());
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
   *  Evaluates an external variable that multiply two integers Both queries perform the operation. .
   */
  @org.junit.Test
  public void extvardeclwithtype11() {
    final XQuery query = new XQuery(
      "declare variable $x as xs:integer external; $x * $x",
      ctx);
    try {
      query.bind("x", new XQuery("let $var := 2 * 2 return $var", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("16")
    );
  }

  /**
   *  Evaluates an external variable that subtract two integers Both queries perform the operation. .
   */
  @org.junit.Test
  public void extvardeclwithtype12() {
    final XQuery query = new XQuery(
      "declare variable $x as xs:integer external; $x - xs:integer(1)",
      ctx);
    try {
      query.bind("x", new XQuery("let $var := 5 - 2 return $var", ctx).value());
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
   *  Evaluates an external variable that divides two integers Both queries perform the operation. .
   */
  @org.junit.Test
  public void extvardeclwithtype13() {
    final XQuery query = new XQuery(
      "declare variable $x as xs:integer external; $x idiv xs:integer(2)",
      ctx);
    try {
      query.bind("x", new XQuery("let $var := 20 idiv 2 return $var", ctx).value());
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
   *  Evaluates an external variable that divides (div operator) two integers Both queries perform the operation. .
   */
  @org.junit.Test
  public void extvardeclwithtype14() {
    final XQuery query = new XQuery(
      "declare variable $x as xs:integer external; $x div xs:integer(10)",
      ctx);
    try {
      query.bind("x", new XQuery("let $var := 40 div 2 return xs:integer($var)", ctx).value());
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
   *  Evaluates an external variable that perform modulus operation on two integers Both queries perform the operation. .
   */
  @org.junit.Test
  public void extvardeclwithtype15() {
    final XQuery query = new XQuery(
      "declare variable $x as xs:integer external; $x mod xs:integer(2)",
      ctx);
    try {
      query.bind("x", new XQuery("let $var := 55 mod 3 return $var", ctx).value());
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
   *  Evaluates an external variable that evaluates a boolean expression Both queries perform the operation. .
   */
  @org.junit.Test
  public void extvardeclwithtype16() {
    final XQuery query = new XQuery(
      "declare variable $x as xs:boolean external; $x and fn:false()",
      ctx);
    try {
      query.bind("x", new XQuery("let $var := true() and true() return $var", ctx).value());
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
   *  Evaluates an external variable that evaluates a boolean expression Both queries perform the operation. .
   */
  @org.junit.Test
  public void extvardeclwithtype17() {
    final XQuery query = new XQuery(
      "declare variable $x as xs:boolean external; $x or fn:false()",
      ctx);
    try {
      query.bind("x", new XQuery("let $var := true() or true() return $var", ctx).value());
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
   *  Evaluates an external variable that evaluates avg function. .
   */
  @org.junit.Test
  public void extvardeclwithtype18() {
    final XQuery query = new XQuery(
      "declare variable $x as xs:float external; $x",
      ctx);
    try {
      query.bind("x", new XQuery("let $var := avg((1,2,4)) return xs:float($var)", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "2.3333333")
      ||
        assertStringValue(false, "2.33333")
      )
    );
  }

  /**
   *  Evaluates an external variable that attempts too cast an incompatible value. .
   */
  @org.junit.Test
  public void extvardeclwithtype19() {
    final XQuery query = new XQuery(
      "declare variable $x as xs:dateTime external; $x",
      ctx);
    try {
      query.bind("x", new XQuery("let $var := avg((1,2,4)) return $var", ctx).value());
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
   *  Evaluates an external variable declaration with type Use type xs:date. .
   */
  @org.junit.Test
  public void extvardeclwithtype2() {
    final XQuery query = new XQuery(
      "declare variable $x as xs:date external; fn:string($x)",
      ctx);
    try {
      query.bind("x", new XQuery("(: Name: extvardeclwithtypetobind-2 :) (: Description: Binding a date value for extvardeclwithtype-2.:) xs:date(\"2000-01-01+05:00\")", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2000-01-01+05:00")
    );
  }

  /**
   *  Evaluates an external variable whose case is changed. .
   */
  @org.junit.Test
  public void extvardeclwithtype20() {
    final XQuery query = new XQuery(
      "declare variable $x as xs:string external; fn:upper-case($x)",
      ctx);
    try {
      query.bind("x", new XQuery("let $var := lower-case(\"This String should be all in upper case\") return $var", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "THIS STRING SHOULD BE ALL IN UPPER CASE")
    );
  }

  /**
   *  Evaluates an external variable whose case is changed. .
   */
  @org.junit.Test
  public void extvardeclwithtype21() {
    final XQuery query = new XQuery(
      "declare variable $x as xs:string external; fn:lower-case($x)",
      ctx);
    try {
      query.bind("x", new XQuery("let $var := upper-case(\"THIS STRING SHOULD ALL BE IN LOWER CASE\") return $var", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "this string should all be in lower case")
    );
  }

  /**
   *  Evaluates an external variable whose value is succesfully casted. .
   */
  @org.junit.Test
  public void extvardeclwithtype22() {
    final XQuery query = new XQuery(
      "declare variable $x as xs:boolean external; fn:not($x)",
      ctx);
    try {
      query.bind("x", new XQuery("let $var := xs:integer(0) + xs:integer(1) return xs:boolean($var)", ctx).value());
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
   * This query opens a C++ GCC-XML output file, and outputs a report describing the use of globals variables. .
   */
  @org.junit.Test
  public void extvardeclwithtype23() {
    final XQuery query = new XQuery(
      queryFile(
        file(
            "prod/VarDecl.external/extvardeclwithtype-23.xq"
        )
      ),
      ctx);
    try {
      query.context(node(file("prod/AxisStep/CPPGlobals.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<html xmlns=\"http://www.w3.org/1999/xhtml/\" xml:lang=\"en\" lang=\"en\"><head><title>Global variables report for Globals.cpp</title></head><style type=\"text/css\">\r\n        .details\r\n        {\r\n            text-align: center;\r\n            font-size: 80%;\r\n            color: gray\r\n        }\r\n        .variableName\r\n        {\r\n            font-family: courier\r\n        }\r\n    </style><body><p>The following global, complex variables were found:</p><ol><li><span class=\"variableName\">constComplex2</span> in globals.cpp at line 17</li><li><span class=\"variableName\">constComplex1</span> in globals.cpp at line 16</li><li><span class=\"variableName\">mutableComplex2</span> in globals.cpp at line 15</li><li><span class=\"variableName\">mutableComplex1</span> in globals.cpp at line 14</li></ol><p>The following mutable primitives were found:</p><ol><li><span class=\"variableName\">mutablePrimitive2</span> in globals.cpp at line 2</li><li><span class=\"variableName\">mutablePrimitive1</span> in globals.cpp at line 1</li></ol><p class=\"details\">This report was generated on</p></body></html>", false)
    );
  }

  /**
   *  Evaluates an external variable declaration with type Use type xs:ineteger. .
   */
  @org.junit.Test
  public void extvardeclwithtype3() {
    final XQuery query = new XQuery(
      "declare variable $x as xs:integer external; $x",
      ctx);
    try {
      query.bind("x", new XQuery("(: Name: extvardeclwithtypetobind-3 :) (: Description: Binding an integer value for extvardeclwithtype-3.:) xs:integer(2)", ctx).value());
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
   *  Evaluates an external variable declaration with type Use type xs:double. .
   */
  @org.junit.Test
  public void extvardeclwithtype4() {
    final XQuery query = new XQuery(
      "declare variable $x as xs:double external; $x",
      ctx);
    try {
      query.bind("x", new XQuery("xs:double(1.2E2)", ctx).value());
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
   *  Evaluates an external variable declaration with type Use type xs:boolean. .
   */
  @org.junit.Test
  public void extvardeclwithtype5() {
    final XQuery query = new XQuery(
      "declare variable $x as xs:boolean external; fn:string($x)",
      ctx);
    try {
      query.bind("x", new XQuery("true()", ctx).value());
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
   *  Evaluates an external variable declaration with type Use type xs:float. .
   */
  @org.junit.Test
  public void extvardeclwithtype6() {
    final XQuery query = new XQuery(
      "declare variable $x as xs:float external; $x",
      ctx);
    try {
      query.bind("x", new XQuery("xs:float(1267.43233E12)", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "1.2674323E15")
      ||
        assertStringValue(false, "1.2674324E15")
      )
    );
  }

  /**
   *  Evaluates an external variable declaration with type Use type xs:decimal. .
   */
  @org.junit.Test
  public void extvardeclwithtype7() {
    final XQuery query = new XQuery(
      "declare variable $x as xs:decimal external; $x",
      ctx);
    try {
      query.bind("x", new XQuery("xs:decimal(12678967.543233)", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "12678967.543233")
    );
  }

  /**
   *  Evaluates an external variable that adds two integers Both queries perform the operation .
   */
  @org.junit.Test
  public void extvardeclwithtype8() {
    final XQuery query = new XQuery(
      "declare variable $x as xs:integer external; $x + $x",
      ctx);
    try {
      query.bind("x", new XQuery("let $var := 1 + 1 return $var", ctx).value());
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
   *  Evaluates an external variable that adds two integers This query does NOT performs the operation .
   */
  @org.junit.Test
  public void extvardeclwithtype9() {
    final XQuery query = new XQuery(
      "declare variable $x as xs:integer external; $x",
      ctx);
    try {
      query.bind("x", new XQuery("let $var := 1 + 1 return $var", ctx).value());
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
}
