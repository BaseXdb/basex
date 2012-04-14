package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the prod-TryCatchExpr operator.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdTryCatchExpr extends QT3TestSet {

  /**
   * Without try catch a division by 0 raises a dynamic error..
   */
  @org.junit.Test
  public void noTryCatch1() {
    final XQuery query = new XQuery(
      "1 div 0",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOAR0001")
    );
  }

  /**
   * Without try catch an undefined variable raises a static error..
   */
  @org.junit.Test
  public void noTryCatch2() {
    final XQuery query = new XQuery(
      "$x",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0008")
    );
  }

  /**
   * Without try catch a static type mismatch raises a type error..
   */
  @org.junit.Test
  public void noTryCatch3() {
    final XQuery query = new XQuery(
      "let $i as xs:string := 1 return $i",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void try001() {
    final XQuery query = new XQuery(
      "try { doc('rubbish.xml') } catch * {\"ok\"}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "ok")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void try002() {
    final XQuery query = new XQuery(
      "try { doc('rubbish.xml') } catch err:FODC0001 | err:FODC0002 | err:FODC0005 {\"ok\"}",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "ok")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void try003() {
    final XQuery query = new XQuery(
      "try { doc('rubbish.xml') } catch *:FODC0001 | *:FODC0002 | *:FODC0005 {\"ok\"}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "ok")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void try004() {
    final XQuery query = new XQuery(
      "\n" +
      "        try { doc('rubbish.xml') } \n" +
      "        catch err:FODC0001 {<caught-error code=\"FODC0001\"/>} \n" +
      "        catch err:FODC0002 {<caught-error code=\"FODC0002\"/>} \n" +
      "        catch err:FODC0005 {<caught-error code=\"FODC0005\"/>} \n" +
      "        catch err:* {<caught-error code=\"other\"/>}\n" +
      "      ",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<caught-error code=\"FODC0002\"/>", false)
    );
  }

  /**
   * test try/catch as an ExprSingle .
   */
  @org.junit.Test
  public void try005() {
    final XQuery query = new XQuery(
      "<out>{ try { doc('rubbish.xml') } catch * {<caught-error/>}, try { doc('rubbish.xml') } catch * {<caught-another/>} }</out>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<out><caught-error/><caught-another/></out>", false)
    );
  }

  /**
   * try/catch doesn't catch error evaluating global variable .
   */
  @org.junit.Test
  public void try006() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $doc := doc('rubbish.xml'); \n" +
      "        try { $doc } catch * {<caught-error/>}\n" +
      "       ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FODC0002")
    );
  }

  /**
   * doesn't catch error evaluating local variable .
   */
  @org.junit.Test
  public void try007() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $doc := doc('rubbish.xml') \n" +
      "        return try { $doc } catch * {<caught-error/>}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FODC0002")
    );
  }

  /**
   * does catch error evaluating function call .
   */
  @org.junit.Test
  public void try008() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f() { doc('rubbish.xml') }; \n" +
      "        try { local:f() } catch * {\"ok\"}\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "ok")
    );
  }

  /**
   * does catch error evaluating function call .
   */
  @org.junit.Test
  public void try009() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($d as xs:integer) { 10 div $d }; \n" +
      "        try { local:f(0) } catch err:FOAR0001 {\"ok\"}\n" +
      "      ",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "ok")
    );
  }

  /**
   * Use err:code local variable, implicitly declared.
   */
  @org.junit.Test
  public void try010() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($d as xs:integer) { 10 div $d };\n" +
      "        try { local:f(0) } catch * {local-name-from-QName($err:code)}\n" +
      "      ",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "FOAR0001")
    );
  }

  /**
   * Use err:code local variable in a nested tryCatch. The cast as expr should throw a dynamic error.
   */
  @org.junit.Test
  public void try011() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($d as xs:integer) { 10 div $d };\n" +
      "        declare variable $t as xs:string := \"text\";\n" +
      "        try { local:f(0) } catch * { try { local:f($t cast as xs:integer) } catch * {local-name-from-QName($err:code)} }\n" +
      "      ",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "FORG0001")
    );
  }

  /**
   * Use err:description local variable, implicitly declared.
   */
  @org.junit.Test
  public void try012() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($d as xs:integer) { 10 div $d };\n" +
      "        try { local:f(0) } catch * {$err:description}\n" +
      "      ",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "Integer division by zero")
      ||
        assertType("xs:string")
      )
    );
  }

  /**
   * Use err:value, err:line-number and err:column-number local variable, implicitly declared.
   */
  @org.junit.Test
  public void try013() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($d as xs:integer) { 10 div $d };\n" +
      "        try { local:f(0) } catch * {$err:column-number, $err:line-number, $err:line-number}\n" +
      "      ",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertType("xs:integer*")
    );
  }

  /**
   * XPDY0002 must be caught..
   */
  @org.junit.Test
  public void tryCatchAllDynamicErrorsCaught1() {
    final XQuery query = new XQuery(
      "try { . } catch err:XPDY0002 { \"Context item not set.\" }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Context item not set.")
    );
  }

  /**
   * XQDY0074 must be caught..
   */
  @org.junit.Test
  public void tryCatchAllDynamicErrorsCaught10() {
    final XQuery query = new XQuery(
      "try { element { \"prefix:name\" } {} } catch err:XQDY0074 { \"Invalid element.\" }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Invalid element.")
    );
  }

  /**
   * XQDY0091 must be caught..
   */
  @org.junit.Test
  public void tryCatchAllDynamicErrorsCaught11() {
    final XQuery query = new XQuery(
      "(try { (attribute xml:id {\"\"})/0 } catch err:XQDY0091 { \"Invalid attribute ID.\" })",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("0")
      ||
        assertEq("Invalid attribute ID.")
      )
    );
  }

  /**
   * XQDY0095 must be caught..
   */
  @org.junit.Test
  public void tryCatchAllDynamicErrorsCaught12() {
    final XQuery query = new XQuery(
      "try { let $x := (1,2)[position() < 3] group by $x return $x } catch err:XPTY0004 { \"More than a grouping item.\" }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "More than a grouping item.")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   * XQDY0096 must be caught..
   */
  @org.junit.Test
  public void tryCatchAllDynamicErrorsCaught13() {
    final XQuery query = new XQuery(
      "try { element { \"xmlns:name\" } {} } catch err:XQDY0096 { \"Invalid element.\" }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Invalid element.")
    );
  }

  /**
   * XQDY0101 must be caught..
   */
  @org.junit.Test
  public void tryCatchAllDynamicErrorsCaught14() {
    final XQuery query = new XQuery(
      "try { namespace xmlns { \"http://www.example.com\" } } catch err:XQDY0101 { \"Invalid namespace node.\" }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Invalid namespace node.")
    );
  }

  /**
   * XPDY0050 must be caught..
   */
  @org.junit.Test
  public void tryCatchAllDynamicErrorsCaught2() {
    final XQuery query = new XQuery(
      "try { \"\" treat as element() } catch err:XPDY0050 { \"Sequence type mismatch.\" }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Sequence type mismatch.")
    );
  }

  /**
   * XQDY0025 must be caught..
   */
  @org.junit.Test
  public void tryCatchAllDynamicErrorsCaught3() {
    final XQuery query = new XQuery(
      "try { element  element { attribute a {\"\"}, attribute a {\"\"} } } catch err:XQDY0025 { \"Attribute name duplicate.\" }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Attribute name duplicate.")
    );
  }

  /**
   * XQDY0026 must be caught..
   */
  @org.junit.Test
  public void tryCatchAllDynamicErrorsCaught4() {
    final XQuery query = new XQuery(
      "try { processing-instruction name { \"?>\" } } catch err:XQDY0026 { \"Invalid PI.\" }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Invalid PI.")
    );
  }

  /**
   * XQDY0041 must be caught..
   */
  @org.junit.Test
  public void tryCatchAllDynamicErrorsCaught5() {
    final XQuery query = new XQuery(
      "try { processing-instruction  { \"prefix:name\" } {} } catch err:XQDY0041 { \"Invalid PI.\" }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Invalid PI.")
    );
  }

  /**
   * XQDY0044 must be caught..
   */
  @org.junit.Test
  public void tryCatchAllDynamicErrorsCaught6() {
    final XQuery query = new XQuery(
      "try { attribute xmlns {} } catch err:XQDY0044 { \"Invalid attribute.\" }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Invalid attribute.")
    );
  }

  /**
   * XQDY0061 must be caught..
   */
  @org.junit.Test
  public void tryCatchAllDynamicErrorsCaught7() {
    final XQuery query = new XQuery(
      "\n" +
      "      try { validate { document { <a/>, <b/> }} } catch err:XQDY0061 { \"Invalid document.\" }\n" +
      "      ",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Invalid document.")
    );
  }

  /**
   * XQDY0064 must be caught..
   */
  @org.junit.Test
  public void tryCatchAllDynamicErrorsCaught8() {
    final XQuery query = new XQuery(
      "try { processing-instruction XML {} } catch err:XQDY0064 { \"Invalid PI.\" }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Invalid PI.")
    );
  }

  /**
   * XQDY0072 must be caught..
   */
  @org.junit.Test
  public void tryCatchAllDynamicErrorsCaught9() {
    final XQuery query = new XQuery(
      "try { comment { \"--\" } } catch err:XQDY0072 { \"Invalid comment.\" }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Invalid comment.")
    );
  }

  /**
   * A division by zero (dynamic error) is caught..
   */
  @org.junit.Test
  public void tryCatchDynamicError1() {
    final XQuery query = new XQuery(
      "try { 1 div 0 } catch * { \"Division by zero\" }",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Division by zero")
    );
  }

  /**
   * A division by zero (dynamic error) is caught when the error is specified..
   */
  @org.junit.Test
  public void tryCatchDynamicError2() {
    final XQuery query = new XQuery(
      "try { 1 div 0 } catch err:FOAR0001 { \"Division by zero\" }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Division by zero")
    );
  }

  /**
   * A division by zero (dynamic error) is caught when the error namespace is specified..
   */
  @org.junit.Test
  public void tryCatchDynamicError3() {
    final XQuery query = new XQuery(
      "try { 1 div 0 } catch err:* { \"Division by zero\" }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Division by zero")
    );
  }

  /**
   * A division by zero (dynamic error) is caught when the error local name is specified..
   */
  @org.junit.Test
  public void tryCatchDynamicError4() {
    final XQuery query = new XQuery(
      "try { 1 div 0 } catch *:FOAR0001 { \"Division by zero\" }",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Division by zero")
    );
  }

  /**
   * A dynamic error outside a try catch expression is not caught..
   */
  @org.junit.Test
  public void tryCatchDynamicErrorOutside1() {
    final XQuery query = new XQuery(
      "1 + (try { \"\" } catch * { \"Invalid argument\" })",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * A dynamic error outside a try catch expression is not caught..
   */
  @org.junit.Test
  public void tryCatchDynamicErrorOutside2() {
    final XQuery query = new XQuery(
      "1 + (try { \"\" } catch err:XPTY0004 { \"Invalid argument\" })",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * A dynamic error outside a try catch expression is not caught..
   */
  @org.junit.Test
  public void tryCatchDynamicErrorOutside3() {
    final XQuery query = new XQuery(
      "1 + (try { \"\" } catch err:* { \"Invalid argument\" })",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * A dynamic error outside a try catch expression is not caught..
   */
  @org.junit.Test
  public void tryCatchDynamicErrorOutside4() {
    final XQuery query = new XQuery(
      "1 + (try { \"\" } catch *:XPTY0004 { \"Invalid argument\" })",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * A dynamic error outside a try catch expression is not caught..
   */
  @org.junit.Test
  public void tryCatchDynamicErrorOutside5() {
    final XQuery query = new XQuery(
      "(try { \"\" } catch * { \"Invalid argument\" }) + 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * A dynamic error outside a try catch expression is not caught..
   */
  @org.junit.Test
  public void tryCatchDynamicErrorOutside6() {
    final XQuery query = new XQuery(
      "(try { \"\" } catch err:FOAR0001 { \"Invalid argument\" }) + 1",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * A dynamic error outside a try catch expression is not caught..
   */
  @org.junit.Test
  public void tryCatchDynamicErrorOutside7() {
    final XQuery query = new XQuery(
      "(try { \"\" } catch err:* { \"Invalid argument\" }) + 1",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * A dynamic error outside a try catch expression is not caught..
   */
  @org.junit.Test
  public void tryCatchDynamicErrorOutside8() {
    final XQuery query = new XQuery(
      "(try { \"\" } catch *:FOAR0001 { \"Invalid argument\" }) + 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * A dynamic error after a try catch expression is not caught..
   */
  @org.junit.Test
  public void tryCatchDynamicErrorOutsideAfter1() {
    final XQuery query = new XQuery(
      "try { () } catch * { \"Division by zero\" }, 1 div 0",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOAR0001")
    );
  }

  /**
   * A dynamic error after a try catch expression is not caught..
   */
  @org.junit.Test
  public void tryCatchDynamicErrorOutsideAfter2() {
    final XQuery query = new XQuery(
      "try { () } catch err:FOAR0001 { \"Division by zero\" }, 1 div 0",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOAR0001")
    );
  }

  /**
   * A dynamic error after a try catch expression is not caught..
   */
  @org.junit.Test
  public void tryCatchDynamicErrorOutsideAfter3() {
    final XQuery query = new XQuery(
      "try { () } catch err:* { \"Division by zero\" }, 1 div 0",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOAR0001")
    );
  }

  /**
   * A dynamic error after a try catch expression is not caught..
   */
  @org.junit.Test
  public void tryCatchDynamicErrorOutsideAfter4() {
    final XQuery query = new XQuery(
      "try { () } catch *:FOAR0001 { \"Division by zero\" }, 1 div 0",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOAR0001")
    );
  }

  /**
   * A dynamic error before a try catch expression is not caught..
   */
  @org.junit.Test
  public void tryCatchDynamicErrorOutsideBefore1() {
    final XQuery query = new XQuery(
      "1 div 0, try { () } catch * { \"Division by zero\" }",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOAR0001")
    );
  }

  /**
   * A dynamic error before a try catch expression is not caught..
   */
  @org.junit.Test
  public void tryCatchDynamicErrorOutsideBefore2() {
    final XQuery query = new XQuery(
      "1 div 0, try { () } catch err:FOAR0001 { \"Division by zero\" }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOAR0001")
    );
  }

  /**
   * A dynamic error before a try catch expression is not caught..
   */
  @org.junit.Test
  public void tryCatchDynamicErrorOutsideBefore3() {
    final XQuery query = new XQuery(
      "1 div 0, try { () } catch err:* { \"Division by zero\" }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOAR0001")
    );
  }

  /**
   * A dynamic error before a try catch expression is not caught..
   */
  @org.junit.Test
  public void tryCatchDynamicErrorOutsideBefore4() {
    final XQuery query = new XQuery(
      "1 div 0, try { () } catch *:FOAR0001 { \"Division by zero\" }",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOAR0001")
    );
  }

  /**
   * err:code returns the error code..
   */
  @org.junit.Test
  public void tryCatchErrCodeVariable1() {
    final XQuery query = new XQuery(
      "try { 1 div 0 } catch * { $err:code }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "err:FOAR0001")
    );
  }

  /**
   * err:code returns the error code..
   */
  @org.junit.Test
  public void tryCatchErrCodeVariable10() {
    final XQuery query = new XQuery(
      "try { fn:error() } catch * { fn:prefix-from-QName($err:code) }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "err")
    );
  }

  /**
   * err:code returns the error code..
   */
  @org.junit.Test
  public void tryCatchErrCodeVariable11() {
    final XQuery query = new XQuery(
      "try { fn:error() } catch * { fn:local-name-from-QName($err:code) }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "FOER0000")
    );
  }

  /**
   * err:code returns the error code..
   */
  @org.junit.Test
  public void tryCatchErrCodeVariable12() {
    final XQuery query = new XQuery(
      "try { fn:error() } catch * { fn:namespace-uri-from-QName($err:code) }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.w3.org/2005/xqt-errors")
    );
  }

  /**
   * err:code returns the error code..
   */
  @org.junit.Test
  public void tryCatchErrCodeVariable2() {
    final XQuery query = new XQuery(
      "try { 1 div 0 } catch * { fn:prefix-from-QName($err:code) }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "err")
    );
  }

  /**
   * err:code returns the error code..
   */
  @org.junit.Test
  public void tryCatchErrCodeVariable3() {
    final XQuery query = new XQuery(
      "try { 1 div 0 } catch * { fn:local-name-from-QName($err:code) }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "FOAR0001")
    );
  }

  /**
   * err:code returns the error code..
   */
  @org.junit.Test
  public void tryCatchErrCodeVariable4() {
    final XQuery query = new XQuery(
      "try { 1 div 0 } catch * { fn:namespace-uri-from-QName($err:code) }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.w3.org/2005/xqt-errors")
    );
  }

  /**
   * err:code returns the error code..
   */
  @org.junit.Test
  public void tryCatchErrCodeVariable5() {
    final XQuery query = new XQuery(
      "try { fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:FOER0001')) } catch * { $err:code }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "err:FOER0001")
    );
  }

  /**
   * err:code returns the error code..
   */
  @org.junit.Test
  public void tryCatchErrCodeVariable6() {
    final XQuery query = new XQuery(
      "try { fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:FOER0001')) } catch * { fn:prefix-from-QName($err:code) }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "err")
    );
  }

  /**
   * err:code returns the error code..
   */
  @org.junit.Test
  public void tryCatchErrCodeVariable7() {
    final XQuery query = new XQuery(
      "try { fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:FOER0001')) } catch * { fn:local-name-from-QName($err:code) }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "FOER0001")
    );
  }

  /**
   * err:code returns the error code..
   */
  @org.junit.Test
  public void tryCatchErrCodeVariable8() {
    final XQuery query = new XQuery(
      "try { fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:FOER0001')) } catch * { fn:namespace-uri-from-QName($err:code) }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.w3.org/2005/xqt-errors")
    );
  }

  /**
   * err:code returns the error code..
   */
  @org.junit.Test
  public void tryCatchErrCodeVariable9() {
    final XQuery query = new XQuery(
      "try { fn:error() } catch * { $err:code }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "err:FOER0000")
    );
  }

  /**
   * err:column-number must be defined..
   */
  @org.junit.Test
  public void tryCatchErrColumnNumberVariable1() {
    final XQuery query = new XQuery(
      "try { fn:error() } catch * { let $n := $err:column-number return true() }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * err:column-number must be defined..
   */
  @org.junit.Test
  public void tryCatchErrColumnNumberVariable2() {
    final XQuery query = new XQuery(
      "try { fn:error() } catch * { count($err:column-number) }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("0")
      ||
        assertEq("1")
      )
    );
  }

  /**
   * err:column-number must be an integer if it is not empty..
   */
  @org.junit.Test
  public void tryCatchErrColumnNumberVariable3() {
    final XQuery query = new XQuery(
      "try { fn:error() } catch * { if (count($err:column-number) eq 1) then $err:column-number else 0 }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertType("xs:integer")
    );
  }

  /**
   * err:description returns a description string or the empty sequence..
   */
  @org.junit.Test
  public void tryCatchErrDescriptionVariable1() {
    final XQuery query = new XQuery(
      "try { 1 div 0 } catch * { count($err:description) le 1 }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * err:code returns a description string or the empty sequence..
   */
  @org.junit.Test
  public void tryCatchErrDescriptionVariable2() {
    final XQuery query = new XQuery(
      "try { 1 div 0 } catch * { if (($err:description)) then $err:description instance of xs:string else fn:true() }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * err:code returns a description string or the empty sequence..
   */
  @org.junit.Test
  public void tryCatchErrDescriptionVariable3() {
    final XQuery query = new XQuery(
      "try { fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:FOER0000'), \"Description\") } catch * { $err:description }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Description")
    );
  }

  /**
   * A dynamic error raised by the chosen catch clause must be thrown..
   */
  @org.junit.Test
  public void tryCatchErrDynamicErrorInCatchClause1() {
    final XQuery query = new XQuery(
      "try { fn:error() } catch * { fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:FOER0001')) }",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOER0001")
    );
  }

  /**
   * A dynamic error raised by a non-executed catch clause must be ignored..
   */
  @org.junit.Test
  public void tryCatchErrDynamicErrorInCatchClause2() {
    final XQuery query = new XQuery(
      "try { 1 div 0 } catch err:FOER0000 { fn:error() }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOAR0001")
    );
  }

  /**
   * A dynamic error raised by a non-executed catch clause must be ignored..
   */
  @org.junit.Test
  public void tryCatchErrDynamicErrorInCatchClause3() {
    final XQuery query = new XQuery(
      "try { 0 } catch err:FOER0000 { fn:error() }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   * A dynamic error raised by a non-executed catch clause must be ignored..
   */
  @org.junit.Test
  public void tryCatchErrDynamicErrorInCatchClause4() {
    final XQuery query = new XQuery(
      "try { fn:error() } catch err:FOER0001 { fn:error() } catch err:FOER0000 { 0 }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   * A dynamic error raised by a non-executed catch clause must be ignored..
   */
  @org.junit.Test
  public void tryCatchErrDynamicErrorInCatchClause5() {
    final XQuery query = new XQuery(
      "try { fn:error() } catch err:FOER0000 { 0 } catch err:FOER0001 { fn:error() }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   * err:line-number must be defined..
   */
  @org.junit.Test
  public void tryCatchErrLineNumberVariable1() {
    final XQuery query = new XQuery(
      "try { fn:error() } catch * { let $n := $err:line-number return true() }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * err:line-number must be defined..
   */
  @org.junit.Test
  public void tryCatchErrLineNumberVariable2() {
    final XQuery query = new XQuery(
      "try { fn:error() } catch * { count($err:line-number) }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("0")
      ||
        assertEq("1")
      )
    );
  }

  /**
   * err:line-number must be an integer if it is not empty..
   */
  @org.junit.Test
  public void tryCatchErrLineNumberVariable3() {
    final XQuery query = new XQuery(
      "try { fn:error() } catch * { if (count($err:line-number) eq 1) then $err:line-number else 0 }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertType("xs:integer")
    );
  }

  /**
   * err:module must be defined..
   */
  @org.junit.Test
  public void tryCatchErrModuleVariable1() {
    final XQuery query = new XQuery(
      "try { fn:error() } catch * { let $n := $err:module return true() }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * err:module must be defined..
   */
  @org.junit.Test
  public void tryCatchErrModuleVariable2() {
    final XQuery query = new XQuery(
      "try { fn:error() } catch * { count($err:module) }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("0")
      ||
        assertEq("1")
      )
    );
  }

  /**
   * err:module must be a string if it is not empty..
   */
  @org.junit.Test
  public void tryCatchErrModuleVariable3() {
    final XQuery query = new XQuery(
      "try { fn:error() } catch * { if (count($err:module) eq 1) then $err:module else \"\" }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertType("xs:string")
    );
  }

  /**
   * err:other must not be defined..
   */
  @org.junit.Test
  public void tryCatchErrOtherVariable1() {
    final XQuery query = new XQuery(
      "try { fn:error() } catch * { $err:other }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0008")
    );
  }

  /**
   * err:value returns a sequence of error values..
   */
  @org.junit.Test
  public void tryCatchErrValueVariable1() {
    final XQuery query = new XQuery(
      "try { fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:FOER0000'), \"Description\", \"Value\") } catch * { $err:value }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Value")
    );
  }

  /**
   * err:value returns a sequence of error values..
   */
  @org.junit.Test
  public void tryCatchErrValueVariable2() {
    final XQuery query = new XQuery(
      "try { fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:FOER0000'), \"Description\", (\"Value\", 3, <a/>, true())) } catch * { count($err:value) }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("4")
    );
  }

  /**
   * err:value returns a sequence of error values..
   */
  @org.junit.Test
  public void tryCatchErrValueVariable3() {
    final XQuery query = new XQuery(
      "try { fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:FOER0000'), \"Description\", (\"Value\", 3)) } catch * { $err:value[2] }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("3")
    );
  }

  /**
   * An error raised with fn:error() must be handled as a dynamic error..
   */
  @org.junit.Test
  public void tryCatchErrorFunctionWithStaticErrorCode() {
    final XQuery query = new XQuery(
      "try { fn:error(xs:QName(\"err:XPST0008\")) } catch err:XPST0008 { 0 }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   * The first matching catch clause is used..
   */
  @org.junit.Test
  public void tryCatchFirstMatchingCatchClause1() {
    final XQuery query = new XQuery(
      "try { 1 div 0 } catch err:FOAR0001 { \"Clause 1\" } catch err:FOAR0001 { \"Clause 2\" }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Clause 1")
    );
  }

  /**
   * The first matching catch clause is used..
   */
  @org.junit.Test
  public void tryCatchFirstMatchingCatchClause10() {
    final XQuery query = new XQuery(
      "try { 1 div 0 } catch Q{http://www.w3.org/2005/xqt-errors}* { \"Clause 1\" } catch Q{http://www.w3.org/2001/XMLSchema}* { \"Clause 2\" }",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Clause 1")
    );
  }

  /**
   * The first matching catch clause is used..
   */
  @org.junit.Test
  public void tryCatchFirstMatchingCatchClause11() {
    final XQuery query = new XQuery(
      "try { 1 div 0 } catch Q{http://www.w3.org/2001/XMLSchema}* { \"Clause 1\" } catch Q{http://www.w3.org/2005/xqt-errors}* { \"Clause 2\" }",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Clause 2")
    );
  }

  /**
   * The first matching catch clause is used..
   */
  @org.junit.Test
  public void tryCatchFirstMatchingCatchClause12() {
    final XQuery query = new XQuery(
      "try { 1 div 0 } catch * { \"Clause 1\" } catch err:FOAR0001 { \"Clause 2\" }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Clause 1")
    );
  }

  /**
   * The first matching catch clause is used..
   */
  @org.junit.Test
  public void tryCatchFirstMatchingCatchClause2() {
    final XQuery query = new XQuery(
      "try { 1 div 0 } catch err:XQST008 { \"Clause 1\" } catch err:FOAR0001 { \"Clause 2\" }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Clause 2")
    );
  }

  /**
   * The first matching catch clause is used..
   */
  @org.junit.Test
  public void tryCatchFirstMatchingCatchClause3() {
    final XQuery query = new XQuery(
      "try { 1 div 0 } catch err:FOAR0001 { \"Clause 1\" } catch err:XQST008 { \"Clause 2\" }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Clause 1")
    );
  }

  /**
   * The first matching catch clause is used..
   */
  @org.junit.Test
  public void tryCatchFirstMatchingCatchClause4() {
    final XQuery query = new XQuery(
      "try { 1 div 0 } catch err:FOAR0001 { \"Clause 1\" } catch err:XQST008 { \"Clause 2\" } catch err:XPTY0004 { \"Clause 3\" }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Clause 1")
    );
  }

  /**
   * The first matching catch clause is used..
   */
  @org.junit.Test
  public void tryCatchFirstMatchingCatchClause5() {
    final XQuery query = new XQuery(
      "try { 1 div 0 } catch err:FOAR0001 { \"Clause 1\" } catch err:XPTY0004 { \"Clause 2\" } catch err:XQST008 { \"Clause 3\" }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Clause 1")
    );
  }

  /**
   * The first matching catch clause is used..
   */
  @org.junit.Test
  public void tryCatchFirstMatchingCatchClause6() {
    final XQuery query = new XQuery(
      "try { 1 div 0 } catch err:XPTY0004 { \"Clause 1\" } catch err:FOAR0001 { \"Clause 2\" } catch err:XQST008 { \"Clause 3\" }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Clause 2")
    );
  }

  /**
   * The first matching catch clause is used..
   */
  @org.junit.Test
  public void tryCatchFirstMatchingCatchClause7() {
    final XQuery query = new XQuery(
      "try { 1 div 0 } catch err:XPTY0004 { \"Clause 1\" } catch err:XPST0008 { \"Clause 2\" } catch err:FOAR0001 { \"Clause 3\" }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Clause 3")
    );
  }

  /**
   * The first matching catch clause is used..
   */
  @org.junit.Test
  public void tryCatchFirstMatchingCatchClause8() {
    final XQuery query = new XQuery(
      "try { 1 div 0 } catch err:* { \"Clause 1\" } catch xs:* { \"Clause 2\" }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Clause 1")
    );
  }

  /**
   * The first matching catch clause is used..
   */
  @org.junit.Test
  public void tryCatchFirstMatchingCatchClause9() {
    final XQuery query = new XQuery(
      "try { 1 div 0 } catch xs:* { \"Clause 1\" } catch err:* { \"Clause 2\" }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Clause 2")
    );
  }

  /**
   * If a function call occurs within a try clause, errors raised by evaluating the corresponding function are caught by the try/catch expression..
   */
  @org.junit.Test
  public void tryCatchFunctionCall1() {
    final XQuery query = new XQuery(
      "try { fn:error() } catch err:FOER0000 { 0 }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   * If a function call occurs within a try clause, errors raised by evaluating the corresponding function are caught by the try/catch expression..
   */
  @org.junit.Test
  public void tryCatchFunctionCall2() {
    final XQuery query = new XQuery(
      "try { fn:one-or-more(()) } catch err:FORG0004 { 0 }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   * If a function call occurs within a try clause, errors raised by evaluating the corresponding function are caught by the try/catch expression..
   */
  @org.junit.Test
  public void tryCatchFunctionCall3() {
    final XQuery query = new XQuery(
      "try { function() { fn:error() } () } catch err:FOER0000 { 0 }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   * If a function call occurs within a try clause, errors raised by evaluating the corresponding function are caught by the try/catch expression..
   */
  @org.junit.Test
  public void tryCatchFunctionCall4() {
    final XQuery query = new XQuery(
      "try { function() { 1 div 0 } () } catch err:FOAR0001 { 0 }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   * If there is no matching catch clause, the error is forwarded..
   */
  @org.junit.Test
  public void tryCatchNameNoMatchingCatchClause1() {
    final XQuery query = new XQuery(
      "try { 1 div 0 } catch err:XPST0008 { \"Division by zero\" }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOAR0001")
    );
  }

  /**
   * If there is no matching catch clause, the error is forwarded..
   */
  @org.junit.Test
  public void tryCatchNameNoMatchingCatchClause2() {
    final XQuery query = new XQuery(
      "try { 1 div 0 } catch xs:* { \"Division by zero\" }",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOAR0001")
    );
  }

  /**
   * If there is no matching catch clause, the error is forwarded..
   */
  @org.junit.Test
  public void tryCatchNameNoMatchingCatchClause3() {
    final XQuery query = new XQuery(
      "try { 1 div 0 } catch *:XPST0008 { \"Division by zero\" }",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOAR0001")
    );
  }

  /**
   * If there is no matching catch clause, the error is forwarded..
   */
  @org.junit.Test
  public void tryCatchNameNoMatchingCatchClause4() {
    final XQuery query = new XQuery(
      "try { 1 div 0 } catch Q{http://www.w3.org/2005/xqt-errors}XPST0008 { \"Division by zero\" }",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOAR0001")
    );
  }

  /**
   * If there is no matching catch clause, the error is forwarded..
   */
  @org.junit.Test
  public void tryCatchNameNoMatchingCatchClause5() {
    final XQuery query = new XQuery(
      "try { 1 div 0 } catch err:XPST0008 { \"Division by zero\" } catch xs:* { \"Division by zero\" } catch *:XPST0008 { \"Division by zero\" } catch Q{http://www.w3.org/2005/xqt-error}* { \"Division by zero\" } catch Q{http://www.w3.org/2005/xqt-errors}XPST0008 { \"Division by zero\" }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOAR0001")
    );
  }

  /**
   * URI literals are supported in name tests..
   */
  @org.junit.Test
  public void tryCatchNameTestsNamespace1() {
    final XQuery query = new XQuery(
      "try { 1 div 0 } catch Q{http://www.w3.org/2005/xqt-errors}* { \"Division by zero\" }",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Division by zero")
    );
  }

  /**
   * Try-catch expressions must nest..
   */
  @org.junit.Test
  public void tryCatchNest1() {
    final XQuery query = new XQuery(
      "try { try { 1 div 0 } catch * { \"Division by zero\" } } catch * { \"Should not be reached.\" }",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Division by zero")
    );
  }

  /**
   * Try-catch expressions must nest..
   */
  @org.junit.Test
  public void tryCatchNest2() {
    final XQuery query = new XQuery(
      "try { fn:error() } catch * { try { 1 div 0 } catch * { \"Division by zero\" } }",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Division by zero")
    );
  }

  /**
   * If no error is raised, the result of the try catch expression is the result of the try clause..
   */
  @org.junit.Test
  public void tryCatchNoError1() {
    final XQuery query = new XQuery(
      "try { 0 } catch * { \"No error\" }",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   * If no error is raised, the result of the try catch expression is the result of the try clause..
   */
  @org.junit.Test
  public void tryCatchNoError2() {
    final XQuery query = new XQuery(
      "try { 0 } catch err:FOAR0001 { \"No error\" }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   * If no error is raised, the result of the try catch expression is the result of the try clause..
   */
  @org.junit.Test
  public void tryCatchNoError3() {
    final XQuery query = new XQuery(
      "try { 0 } catch err:* { \"No error\" }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   * If no error is raised, the result of the try catch expression is the result of the try clause..
   */
  @org.junit.Test
  public void tryCatchNoError4() {
    final XQuery query = new XQuery(
      "try { 0 } catch *:FOAR0001 { \"No error\" }",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   * If no error is raised, the result of the try catch expression is the result of the try clause..
   */
  @org.junit.Test
  public void tryCatchNoError5() {
    final XQuery query = new XQuery(
      "try { 0 } catch err:FOAR0001 { \"No error\" } catch *:FOAR0001 { \"No error \"} catch err:* { \"No error \"}  catch * { \"No error \"}",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   * If the evaluation of an expression inside a try/catch is rewritten or deferred in this way, it must take its try/catch context with it..
   */
  @org.junit.Test
  public void tryCatchOptimizations1() {
    final XQuery query = new XQuery(
      "try { 1 div 0 } catch * { 0 }, try { 1 div 0 } catch err:FOAR0001 { 1}",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("(0, 1)")
    );
  }

  /**
   * If the evaluation of an expression inside a try/catch is rewritten or deferred in this way, it must take its try/catch context with it..
   */
  @org.junit.Test
  public void tryCatchOptimizations2() {
    final XQuery query = new XQuery(
      "try { fn:error() } catch * { 0 }, try { fn:error() } catch err:FOER0000 { 1}",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("0 1", false)
    );
  }

  /**
   * Expressions that were written outside the try/catch expression may be evaluated inside the try/catch, but only if they retain their original try/catch behavior..
   */
  @org.junit.Test
  public void tryCatchOptimizations3() {
    final XQuery query = new XQuery(
      "let $x := 1 div 0 return try { $x } catch * { 0 }",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOAR0001")
    );
  }

  /**
   * A catch clause with one or more NameTests matches any error whose error code matches one of these NameTests..
   */
  @org.junit.Test
  public void tryCatchSeveralNameTests1() {
    final XQuery query = new XQuery(
      "try { 1 div 0 } catch err:XPST0008 | err:FOAR0001 { \"Division by zero\" }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Division by zero")
    );
  }

  /**
   * A catch clause with one or more NameTests matches any error whose error code matches one of these NameTests..
   */
  @org.junit.Test
  public void tryCatchSeveralNameTests2() {
    final XQuery query = new XQuery(
      "try { 1 div 0 } catch err:FOAR0001 | err:XPST0008 { \"Division by zero\" }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Division by zero")
    );
  }

  /**
   * A catch clause with one or more NameTests matches any error whose error code matches one of these NameTests..
   */
  @org.junit.Test
  public void tryCatchSeveralNameTests3() {
    final XQuery query = new XQuery(
      "try { 1 div 0 } catch err:XPTY0004 | err:FOAR0001 | err:XPST0008 { \"Division by zero\" }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Division by zero")
    );
  }

  /**
   * Example from the specification..
   */
  @org.junit.Test
  public void tryCatchSpecExample1() {
    final XQuery query = new XQuery(
      "let $x := \"\" return try {\n" +
      "      $x cast as xs:integer\n" +
      "      }\n" +
      "      catch * {\n" +
      "      0\n" +
      "      }",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   * Example from the specification..
   */
  @org.junit.Test
  public void tryCatchSpecExample2() {
    final XQuery query = new XQuery(
      "let $x := \"\" return try {\n" +
      "      $x cast as xs:integer\n" +
      "      }\n" +
      "      catch err:FORG0001 {\n" +
      "      0\n" +
      "      }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   * Example from the specification..
   */
  @org.junit.Test
  public void tryCatchSpecExample3() {
    final XQuery query = new XQuery(
      "let $x := \"\" return try {\n" +
      "      $x cast as xs:integer\n" +
      "      }\n" +
      "      catch err:FORG0001 | err:XPTY0004 {\n" +
      "      0\n" +
      "      }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   * Example from the specification (must parse)..
   */
  @org.junit.Test
  public void tryCatchSpecExample4() {
    final XQuery query = new XQuery(
      "\n" +
      "      let $output := (try {\n" +
      "          fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:FOER0000'))\n" +
      "        }\n" +
      "        catch * {\n" +
      "          $err:code, $err:value, \" module: \",\n" +
      "          $err:module, \"(\", $err:line-number, \",\", $err:column-number, \")\"\n" +
      "        })\n" +
      "      return true()\n" +
      "    ",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Example from the specification..
   */
  @org.junit.Test
  public void tryCatchSpecExample5() {
    final XQuery query = new XQuery(
      "declare function local:thrice($x as xs:integer) as xs:integer\n" +
      "      {\n" +
      "      3*$x\n" +
      "      };\n" +
      "      \n" +
      "      local:thrice(try { \"oops\" } catch * { 3 } )\n" +
      "    ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * An undefined variable (static error) is not caught..
   */
  @org.junit.Test
  public void tryCatchStaticError1() {
    final XQuery query = new XQuery(
      "try { $x } catch * { \"Undefined variable\" }",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0008")
    );
  }

  /**
   * An undefined variable (static error) is not caught..
   */
  @org.junit.Test
  public void tryCatchStaticError2() {
    final XQuery query = new XQuery(
      "try { $x } catch err:XPST0008 { \"Undefined variable\" }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0008")
    );
  }

  /**
   * An undefined variable (static error) is not caught..
   */
  @org.junit.Test
  public void tryCatchStaticError3() {
    final XQuery query = new XQuery(
      "try { $x } catch err:* { \"Undefined variable\" }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0008")
    );
  }

  /**
   * An undefined variable (static error) is not caught..
   */
  @org.junit.Test
  public void tryCatchStaticError4() {
    final XQuery query = new XQuery(
      "try { $x } catch *:XPST0008 { \"Undefined variable\" }",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0008")
    );
  }

  /**
   * A static type mismatch (type error) is caught..
   */
  @org.junit.Test
  public void tryCatchTypeError1() {
    final XQuery query = new XQuery(
      "try { let $i as xs:string := 1 return $i } catch * { \"Undefined variable\" }",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * A static type mismatch (type error) is caught..
   */
  @org.junit.Test
  public void tryCatchTypeError2() {
    final XQuery query = new XQuery(
      "try { let $i as xs:string := 1 return $i } catch err:XPTY0004 { \"Undefined variable\" }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * A static type mismatch (type error) is caught..
   */
  @org.junit.Test
  public void tryCatchTypeError3() {
    final XQuery query = new XQuery(
      "try { let $i as xs:string := 1 return $i } catch err:* { \"Undefined variable\" }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * A static type mismatch (type error) is caught..
   */
  @org.junit.Test
  public void tryCatchTypeError4() {
    final XQuery query = new XQuery(
      "try { let $i as xs:string := 1 return $i } catch *:XPTY0004 { \"Undefined variable\" }",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * If a variable reference is used in a try clause, errors raised by binding a value to the variable are not caught unless the binding expression occurs within the try clause..
   */
  @org.junit.Test
  public void tryCatchVariableBindingOutside1() {
    final XQuery query = new XQuery(
      "let $x as xs:integer := \"\" return try { $x } catch err:XPTY0004 { 0 }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * If a variable reference is used in a try clause, errors raised by binding a value to the variable are not caught unless the binding expression occurs within the try clause..
   */
  @org.junit.Test
  public void tryCatchVariableBindingOutside2() {
    final XQuery query = new XQuery(
      "try { let $x as xs:integer := \"\" return $x } catch err:XPTY0004 { 0 }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("0")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   * If a variable reference is used in a try clause, errors raised by binding a value to the variable are not caught unless the binding expression occurs within the try clause..
   */
  @org.junit.Test
  public void tryCatchVariableBindingOutside3() {
    final XQuery query = new XQuery(
      "for $x as xs:integer in (0, 1, \"\") return try { $x } catch err:XPTY0004 { 0 }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("0")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   * If a variable reference is used in a try clause, errors raised by binding a value to the variable are not caught unless the binding expression occurs within the try clause..
   */
  @org.junit.Test
  public void tryCatchVariableBindingOutside4() {
    final XQuery query = new XQuery(
      "try { for $x as xs:integer in (0, 1, \"\") return $x } catch err:XPTY0004 { 0 }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   * If a variable reference is used in a try clause, errors raised by binding a value to the variable are not caught unless the binding expression occurs within the try clause..
   */
  @org.junit.Test
  public void tryCatchVariableBindingOutside5() {
    final XQuery query = new XQuery(
      "for tumbling window $x as xs:string in (2, 4, 6, 8, 10)\n" +
      "      start $s at $spos previous $sprev next $snext when true() end $e at\n" +
      "      $epos previous $eprev next $enext when true() return try { $x } catch err:XPTY0004 { 0 }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * If a variable reference is used in a try clause, errors raised by binding a value to the variable are not caught unless the binding expression occurs within the try clause..
   */
  @org.junit.Test
  public void tryCatchVariableBindingOutside6() {
    final XQuery query = new XQuery(
      "try { for tumbling window $x as xs:string in (2, 4, 6, 8, 10)\n" +
      "      start $s at $spos previous $sprev next $snext when true() end $e at\n" +
      "      $epos previous $eprev next $enext when true() return $x } catch err:XPTY0004 { 0 }",
      ctx);
    query.namespace("err", "http://www.w3.org/2005/xqt-errors");

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("0")
      ||
        error("XPTY0004")
      )
    );
  }
}
