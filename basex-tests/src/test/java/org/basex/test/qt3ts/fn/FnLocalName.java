package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnLocalName extends QT3TestSet {

  /**
   *  A test whose essence is: `local-name((), "wrong param")`..
   */
  @org.junit.Test
  public void kNodeLocalNameFunc1() {
    final XQuery query = new XQuery(
      "local-name((), \"wrong param\")",
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
   * A test whose essence is: `if(false()) then local-name() else true()`..
   */
  @org.junit.Test
  public void kNodeLocalNameFunc2() {
    final XQuery query = new XQuery(
      "if(false()) then local-name() else true()",
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
      )
    );
  }

  /**
   * A test whose essence is: `local-name(()) eq ""`..
   */
  @org.junit.Test
  public void kNodeLocalNameFunc3() {
    final XQuery query = new XQuery(
      "local-name(()) eq \"\"",
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
   *  Call fn:local-name() on an attribute node..
   */
  @org.junit.Test
  public void k2NodeLocalNameFunc1() {
    final XQuery query = new XQuery(
      "local-name(/works/employee[1]/@name)",
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
      assertStringValue(false, "name")
    );
  }

  /**
   *  Evaluation of the fn:local-name function with an undefined context node and no argument. .
   */
  @org.junit.Test
  public void fnLocalName1() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace eg = \"http://example.org\"; \n" +
      "        declare function eg:noContextFunction() { fn:local-name() }; \n" +
      "        eg:noContextFunction()\n" +
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
   *  Evaluation of the fn:local-name function argument set to a directly constructed element node with no prefix. Use fn:string. .
   */
  @org.junit.Test
  public void fnLocalName10() {
    final XQuery query = new XQuery(
      "fn:string(fn:local-name(<anElement>Some content</anElement>))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "anElement")
    );
  }

  /**
   *  Evaluation of the fn:local-name function argument set to a directly constructed element node with a prefix. Use fn:string. .
   */
  @org.junit.Test
  public void fnLocalName11() {
    final XQuery query = new XQuery(
      "fn:string(fn:local-name(<p1:anElement xmlns:p1=\"http://example.com\">Some content</p1:anElement>))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "anElement")
    );
  }

  /**
   *  Evaluation of the fn:local-name function argument set to a computed attribute node with no prefix. Use fn:string. .
   */
  @org.junit.Test
  public void fnLocalName12() {
    final XQuery query = new XQuery(
      "fn:string(fn:local-name(attribute anAttribute {\"Attribute Value\"}))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "anAttribute")
    );
  }

  /**
   *  Evaluation of the fn:local-name function argument set to a computed attribute node with a prefix. Use fn:string. .
   */
  @org.junit.Test
  public void fnLocalName13() {
    final XQuery query = new XQuery(
      "declare namespace p1 = \"http://example.org\"; \n" +
      "        fn:string(fn:local-name(attribute p1:anAttribute {\"Attribute Value\"}))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "anAttribute")
    );
  }

  /**
   *  Evaluation of the fn:local-name function argument set to a computed PI node with no prefix. Use fn:string. .
   */
  @org.junit.Test
  public void fnLocalName14() {
    final XQuery query = new XQuery(
      "fn:string(fn:local-name(processing-instruction PITarget {\"PIcontent\"}))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "PITarget")
    );
  }

  /**
   *  Evaluation of the fn:local-name function argument set to a directly constructed PI node. Use fn:string. .
   */
  @org.junit.Test
  public void fnLocalName15() {
    final XQuery query = new XQuery(
      "fn:string(fn:local-name(<?format role=\"output\" ?>))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "format")
    );
  }

  /**
   *  Evaluation of the fn:local-name function argument set to a directly constructed element node with prefix and a declare namespace declaration. Use fn:string. .
   */
  @org.junit.Test
  public void fnLocalName16() {
    final XQuery query = new XQuery(
      "declare namespace p1 = \"http://example.org\"; fn:string(fn:local-name(<p1:anElement>Some content</p1:anElement>))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "anElement")
    );
  }

  /**
   *  Evaluation of the fn:local-name function argument set to a directly constructed element node with no prefix used as argument to string-length function. Use fn:string. .
   */
  @org.junit.Test
  public void fnLocalName17() {
    final XQuery query = new XQuery(
      "fn:string-length(fn:string(fn:local-name(<anElement>Some content</anElement>)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("9")
    );
  }

  /**
   *  Evaluation of the fn:local-name function argument set to a computed element node with prefix and a declare namespace declaration. Use fn:string. .
   */
  @org.junit.Test
  public void fnLocalName18() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace p1 = \"http://example.org\"; \n" +
      "        fn:string(fn:local-name(element p1:anElement{\"Some content\"}))\n" +
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
      assertStringValue(false, "anElement")
    );
  }

  /**
   *  Evaluation of the fn:local-name function argument set to a directly constructed element node with no prefix used as argument to upper-case function. Use fn:string. .
   */
  @org.junit.Test
  public void fnLocalName19() {
    final XQuery query = new XQuery(
      "fn:upper-case(fn:string(fn:local-name(<anElement>Some content</anElement>)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ANELEMENT")
    );
  }

  /**
   *  Name: fn-local-name-1 Description: Evaluation of the fn:local-name function with
   *          an undefined context node and no argument. .
   */
  @org.junit.Test
  public void fnLocalName1a() {
    final XQuery query = new XQuery(
      "fn:local-name()",
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
   *  Name: fn-local-name-2 Description: Evaluation of the fn:local-name function with
   *          context item not a node. .
   */
  @org.junit.Test
  public void fnLocalName2() {
    final XQuery query = new XQuery(
      "(1 to 100)[fn:local-name()]",
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
   *  Evaluation of the fn:local-name function argument set to a directly constructed element node with no prefix used as argument to lower-case function. Use fn:string. .
   */
  @org.junit.Test
  public void fnLocalName20() {
    final XQuery query = new XQuery(
      "fn:lower-case(fn:string(fn:local-name(<anElement>Some content</anElement>)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "anelement")
    );
  }

  /**
   *  Evaluation of the fn:local-name function argument set to a computed attribite node with no prefix used as argument to upper-case function. Use fn:string. .
   */
  @org.junit.Test
  public void fnLocalName21() {
    final XQuery query = new XQuery(
      "fn:upper-case(fn:string(fn:local-name(attribute anAttribute {\"Some content\"})))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ANATTRIBUTE")
    );
  }

  /**
   *  Evaluation of the fn:local-name function argument set to a computed attribite node with no prefix used as argument to lower-case function. Use fn:string. .
   */
  @org.junit.Test
  public void fnLocalName22() {
    final XQuery query = new XQuery(
      "fn:lower-case(fn:string(fn:local-name(attribute anAttribute {\"Some content\"})))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "anattribute")
    );
  }

  /**
   *  Evaluation of the fn:local-name function with second argument set to "." and no context node set. Use fn:string. .
   */
  @org.junit.Test
  public void fnLocalName23() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace eg = \"http://example.org\"; \n" +
      "        declare function eg:noContextFunction() { fn:local-name(.) }; \n" +
      "        eg:noContextFunction()\n" +
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
   *  Name: fn-local-name-51 Description: Evaluation of the fn:local-name function
   *          with the argument set to the empty sequence. Uses the fn:string-length function to avoid
   *          empty file. .
   */
  @org.junit.Test
  public void fnLocalName51() {
    final XQuery query = new XQuery(
      "fn:string-length(fn:local-name(()))",
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
   *  Name: fn-local-name-52 Description: Evaluation of the fn:local-name function
   *          with the argument set to an element node. .
   */
  @org.junit.Test
  public void fnLocalName52() {
    final XQuery query = new XQuery(
      "(fn:local-name(./works[1]/employee[1]))",
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
      assertStringValue(false, "employee")
    );
  }

  /**
   *  Name: fn-local-name-53 Description: Evaluation of the fn:local-name function
   *          with the argument set to an attribute node. insert-start insert-end .
   */
  @org.junit.Test
  public void fnLocalName53() {
    final XQuery query = new XQuery(
      "(fn:local-name(./works[1]/employee[1]/@name))",
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
      assertStringValue(false, "name")
    );
  }

  /**
   *  Name: fn-local-name-54 Description: Evaluation of the fn:local-name function
   *          with the argument set to a document node. Use of "fn:string-length" to avoid empty file.
   *          insert-start insert-end .
   */
  @org.junit.Test
  public void fnLocalName54() {
    final XQuery query = new XQuery(
      "fn:string-length(fn:local-name(.))",
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
      assertEq("0")
    );
  }

  /**
   *  Name: fn-local-name-55 Description: Evaluation of the fn:local-name function
   *          with the argument set to a non existing element. Use of "fn:string-length" to avoid empty
   *          file. insert-start insert-end .
   */
  @org.junit.Test
  public void fnLocalName55() {
    final XQuery query = new XQuery(
      "fn:string-length(fn:local-name(./works[1]/nonexistent[1]))",
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
      assertEq("0")
    );
  }

  /**
   *  Name: fn-local-name-56 Description: Evaluation of the fn:local-name function
   *          with the argument set to a non existing element. Use of "fn:string-length" to avoid empty
   *          file. .
   */
  @org.junit.Test
  public void fnLocalName56() {
    final XQuery query = new XQuery(
      "for $h in ./works[1]/employee[2] return\n" +
      "         fn:string-length(fn:local-name($h/child::text()[last()]))",
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
      assertEq("0")
    );
  }

  /**
   *  Name: fn-local-name-57 Description: Evaluation of the fn:local-name function
   *          with an undefined context node. .
   */
  @org.junit.Test
  public void fnLocalName57() {
    final XQuery query = new XQuery(
      "fn:local-name()",
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
   *  Name: fn-local-name-58 Description: Evaluation of the fn:local-name function
   *          used as an argument to the fn:upper-case function .
   */
  @org.junit.Test
  public void fnLocalName58() {
    final XQuery query = new XQuery(
      "for $h in (./works/employee[2]) return fn:upper-case(fn:local-name($h))",
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
      assertStringValue(false, "EMPLOYEE")
    );
  }

  /**
   *  Name: fn-local-name-59 Description: Evaluation of the fn:local-name function
   *          used as an argument to the fn:lower-case function .
   */
  @org.junit.Test
  public void fnLocalName59() {
    final XQuery query = new XQuery(
      "for $h in (./works/employee[2]) return fn:lower-case(fn:local-name($h))",
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
      assertStringValue(false, "employee")
    );
  }

  /**
   *  Evaluation of the fn:local-name function argument set computed comment node. Use fn:count to avoid empty file .
   */
  @org.junit.Test
  public void fnLocalName6() {
    final XQuery query = new XQuery(
      "fn:count(fn:local-name((comment {\"A Comment Node\"})))",
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
   *  Name: fn-local-name-60 Description: Evaluation of the fn:local-name function
   *          with argument that uses the "parent" axes .
   */
  @org.junit.Test
  public void fnLocalName60() {
    final XQuery query = new XQuery(
      "for $h in (./works/employee[2]) return fn:local-name($h/parent::node())",
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
      assertStringValue(false, "works")
    );
  }

  /**
   *  Name: fn-local-name-61 Description: Evaluation of the fn:local-name function
   *          with argument that uses the "descendant" axes .
   */
  @org.junit.Test
  public void fnLocalName61() {
    final XQuery query = new XQuery(
      "for $h in (./works/employee[2]) return fn:local-name($h/descendant::empnum[position() =\n" +
      "         1])",
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
      assertStringValue(false, "empnum")
    );
  }

  /**
   *  Name: fn-local-name-62 Description: Evaluation of the fn:local-name function
   *          with argument that uses the "descendant-or-self" axes .
   */
  @org.junit.Test
  public void fnLocalName62() {
    final XQuery query = new XQuery(
      "for $h in (./works/employee[2]) return\n" +
      "         fn:local-name($h/descendant-or-self::empnum[position() = 1])",
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
      assertStringValue(false, "empnum")
    );
  }

  /**
   *  Name: fn-local-name-63 Description: Evaluation of the fn:local-name function
   *          used as argument to the fn-subtstring function. .
   */
  @org.junit.Test
  public void fnLocalName63() {
    final XQuery query = new XQuery(
      "for $h in (./works/employee[2]) return fn:substring(fn:local-name($h),2)",
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
      assertStringValue(false, "mployee")
    );
  }

  /**
   *  Name: fn-local-name-64 Description: Evaluation of the fn:local-name function
   *          used as argument to the fn:concat function. .
   */
  @org.junit.Test
  public void fnLocalName64() {
    final XQuery query = new XQuery(
      "for $h in (/works/employee[2]) return fn:concat(fn:local-name($h),\"A String\")",
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
      assertStringValue(false, "employeeA String")
    );
  }

  /**
   *  Name: fn-local-name-65 Description: Evaluation of the fn:local-name function
   *          with argument that uses the "self" axes. Returns a string .
   */
  @org.junit.Test
  public void fnLocalName65() {
    final XQuery query = new XQuery(
      "for $h in (./works/employee[2]) return fn:local-name($h/self::employee)",
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
      assertStringValue(false, "employee")
    );
  }

  /**
   *  Name: fn-local-name-66 Description: Evaluation of the fn:local-name function
   *          with argument that uses the "self" axes. Returns a empty sequence Uses fn:count to avoid
   *          empty file. .
   */
  @org.junit.Test
  public void fnLocalName66() {
    final XQuery query = new XQuery(
      "for $h in (./works/employee[2]) return fn:count(fn:local-name($h/self::div))",
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
      (
        assertStringValue(false, "1")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Name: fn-local-name-67 Description: Evaluation of the fn:local-name function
   *          with argument that uses the "parent::node()". The context node is an attribute node. .
   */
  @org.junit.Test
  public void fnLocalName67() {
    final XQuery query = new XQuery(
      " for $h in (/works/employee[2]/@name) return fn:local-name($h/parent::node())",
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
      assertStringValue(false, "employee")
    );
  }

  /**
   *  Name: fn-local-name-68 Description: Evaluation of the fn:local-name function as
   *          an argument to the string-length function. The context node is an attribute node. .
   */
  @org.junit.Test
  public void fnLocalName68() {
    final XQuery query = new XQuery(
      "fn:string-length(fn:local-name(./works[1]/employee[2]/@name))",
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
      assertStringValue(false, "4")
    );
  }

  /**
   *  Evaluation of the fn:local-name function argument set to a directly constructed comment node. Use fn:count to avoid empty file .
   */
  @org.junit.Test
  public void fnLocalName7() {
    final XQuery query = new XQuery(
      "fn:count(fn:local-name(<!-- A Comment Node -->))",
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
   *  Name: fn-local-name-71 Description: Evaluation of the fn:local-name function
   *          used as part of a sequence. .
   */
  @org.junit.Test
  public void fnLocalName71() {
    final XQuery query = new XQuery(
      "string-join((fn:local-name(./works[1]/employee[1]),fn:local-name(./works[1]/employee[2])),\n" +
      "         ' ')",
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
      assertStringValue(false, "employee employee")
    );
  }

  /**
   *  Name: fn-local-name-72 Description: Evaluation of the fn:local-name function as
   *          argument to fn:count. .
   */
  @org.junit.Test
  public void fnLocalName72() {
    final XQuery query = new XQuery(
      "fn:count(((fn:local-name(/works[1]/employee[1]),fn:local-name(/works[1]/employee[2]))))",
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
      assertStringValue(false, "2")
    );
  }

  /**
   *  Name: fn-local-name-73 Description: Evaluation of the fn:local-name function
   *          with an undefined context node and argument set to ".". .
   */
  @org.junit.Test
  public void fnLocalName73() {
    final XQuery query = new XQuery(
      "fn:local-name(.)",
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
   *  Name: fn-local-name-74 Description: Get the name of an element in a namespace. .
   */
  @org.junit.Test
  public void fnLocalName74() {
    final XQuery query = new XQuery(
      "name(/*)",
      ctx);
    try {
      query.namespace("ma", "http://www.example.com/AuctionWatch");
      query.namespace("xlink", "http://www.w3.org/1999/xlink");
      query.namespace("anyzone", "http://www.example.com/auctioneers#anyzone");
      query.namespace("eachbay", "http://www.example.com/auctioneers#eachbay");
      query.namespace("yabadoo", "http://www.example.com/auctioneers#yabadoo");
      query.context(node(file("docs/auction.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ma:AuctionWatchList")
    );
  }

  /**
   *  Name: fn-local-name-75 Description: Get the name of an attribute in a namespace. .
   */
  @org.junit.Test
  public void fnLocalName75() {
    final XQuery query = new XQuery(
      "name((//*:Start)[1]/@*)",
      ctx);
    try {
      query.namespace("ma", "http://www.example.com/AuctionWatch");
      query.namespace("xlink", "http://www.w3.org/1999/xlink");
      query.namespace("anyzone", "http://www.example.com/auctioneers#anyzone");
      query.namespace("eachbay", "http://www.example.com/auctioneers#eachbay");
      query.namespace("yabadoo", "http://www.example.com/auctioneers#yabadoo");
      query.context(node(file("docs/auction.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ma:currency")
    );
  }

  /**
   *  Name: fn-local-name-76 Description: Get the name of the xml:lang attribute. .
   */
  @org.junit.Test
  public void fnLocalName76() {
    final XQuery query = new XQuery(
      "name((//@xml:*)[1])",
      ctx);
    try {
      query.namespace("ma", "http://www.example.com/AuctionWatch");
      query.namespace("xlink", "http://www.w3.org/1999/xlink");
      query.namespace("anyzone", "http://www.example.com/auctioneers#anyzone");
      query.namespace("eachbay", "http://www.example.com/auctioneers#eachbay");
      query.namespace("yabadoo", "http://www.example.com/auctioneers#yabadoo");
      query.context(node(file("docs/auction.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "xml:lang")
    );
  }

  /**
   *  Get the name of a processing-instruction node. .
   */
  @org.junit.Test
  public void fnLocalName78() {
    final XQuery query = new XQuery(
      "name((//processing-instruction())[1])",
      ctx);
    try {
      query.namespace("ma", "http://www.example.com/AuctionWatch");
      query.namespace("xlink", "http://www.w3.org/1999/xlink");
      query.namespace("anyzone", "http://www.example.com/auctioneers#anyzone");
      query.namespace("eachbay", "http://www.example.com/auctioneers#eachbay");
      query.namespace("yabadoo", "http://www.example.com/auctioneers#yabadoo");
      query.context(node(file("docs/auction.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "xml-stylesheet")
    );
  }

  /**
   *  Get the name of an element in a default but non-null namespace. .
   */
  @org.junit.Test
  public void fnLocalName79() {
    final XQuery query = new XQuery(
      "name((//*[.='1983'])[1])",
      ctx);
    try {
      query.namespace("ma", "http://www.example.com/AuctionWatch");
      query.namespace("xlink", "http://www.w3.org/1999/xlink");
      query.namespace("anyzone", "http://www.example.com/auctioneers#anyzone");
      query.namespace("eachbay", "http://www.example.com/auctioneers#eachbay");
      query.namespace("yabadoo", "http://www.example.com/auctioneers#yabadoo");
      query.context(node(file("docs/auction.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "recorded")
    );
  }

  /**
   *  Evaluation of the fn:local-name function argument set to a directly constructed Document node. Use fn:count to avoid empty file .
   */
  @org.junit.Test
  public void fnLocalName8() {
    final XQuery query = new XQuery(
      "fn:count(fn:local-name(document {<aDocument>some content</aDocument>}))",
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
   *  Get the name of a comment node .
   */
  @org.junit.Test
  public void fnLocalName80() {
    final XQuery query = new XQuery(
      "name((//comment())[1]) = ''",
      ctx);
    try {
      query.namespace("ma", "http://www.example.com/AuctionWatch");
      query.namespace("xlink", "http://www.w3.org/1999/xlink");
      query.namespace("anyzone", "http://www.example.com/auctioneers#anyzone");
      query.namespace("eachbay", "http://www.example.com/auctioneers#eachbay");
      query.namespace("yabadoo", "http://www.example.com/auctioneers#yabadoo");
      query.context(node(file("docs/auction.xml")));
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
   *  Get the name of a text node .
   */
  @org.junit.Test
  public void fnLocalName81() {
    final XQuery query = new XQuery(
      "name((//text())[1]) = ''",
      ctx);
    try {
      query.namespace("ma", "http://www.example.com/AuctionWatch");
      query.namespace("xlink", "http://www.w3.org/1999/xlink");
      query.namespace("anyzone", "http://www.example.com/auctioneers#anyzone");
      query.namespace("eachbay", "http://www.example.com/auctioneers#eachbay");
      query.namespace("yabadoo", "http://www.example.com/auctioneers#yabadoo");
      query.context(node(file("docs/auction.xml")));
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
   *  Evaluation of the fn:local-name function argument set to a computed element node with no prefix. Use fn:string. .
   */
  @org.junit.Test
  public void fnLocalName9() {
    final XQuery query = new XQuery(
      "fn:string(fn:local-name(element anElement {\"Some content\"}))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "anElement")
    );
  }
}
