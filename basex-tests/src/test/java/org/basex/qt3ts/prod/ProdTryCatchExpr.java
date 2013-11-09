package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the prod-TryCatchExpr operator.
 *
 * @author BaseX Team 2005-13, BSD License
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
   * Without try catch a static type mismatch raises a type error..
   */
  @org.junit.Test
  public void noTryCatch3() {
    final XQuery query = new XQuery(
      "let $i as xs:string := 1 return $i",
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
  public void try001() {
    final XQuery query = new XQuery(
      "try { doc('rubbish.xml') } catch * {\"ok\"}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
      "\n" +
      "        declare namespace err = \"http://www.w3.org/2005/xqt-errors\";\n" +
      "        try { doc('rubbish.xml') } catch err:FODC0001 | err:FODC0002 | err:FODC0005 {\"ok\"}\n" +
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:integer*")
    );
  }

  /**
   * Integer literal out of range is a dynamic error.
   */
  @org.junit.Test
  public void try014() {
    final XQuery query = new XQuery(
      "try { 9999999999999999999999999999999999999999999999999999999999999999999999\n" +
      "                idiv\n" +
      "                9999999999999999999999999999999999999999999999999999999999999999999999 }\n" +
      "        catch err:FOAR0002 {1}\n" +
      "    ",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * Bad date is a dynamic error.
   */
  @org.junit.Test
  public void try015() {
    final XQuery query = new XQuery(
      "try { xs:date('2013-02-29') }\n" +
      "          catch err:FORG0001 {true()}\n" +
      "    ",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * No context item is a dynamic error.
   */
  @org.junit.Test
  public void try016() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:f() { .+3 };\n" +
      "      try {local:f()} catch err:XPDY0002 {true()}\n" +
      "    ",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * No context item is a dynamic error (but no context node is a type error...).
   */
  @org.junit.Test
  public void try017() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:f() { a };\n" +
      "      try {local:f()} catch err:XPDY0002 {true()}\n" +
      "    ",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * XPDY0002 must be caught..
   */
  @org.junit.Test
  public void tryCatchAllDynamicErrorsCaught1() {
    final XQuery query = new XQuery(
      "try { . } catch err:XPDY0002 { \"Context item not set.\" }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("0")
      ||
        assertStringValue(false, "Invalid attribute ID.")
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
      "try { element { \"xmlns:name\" } {} } catch err:XQDY0096 | err:XQDY0074 { \"Invalid element.\" }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Invalid element.")
    );
  }

  /**
   * XQDY0096 must be caught..
   */
  @org.junit.Test
  public void tryCatchAllDynamicErrorsCaught13b() {
    final XQuery query = new XQuery(
      "try { element { QName(\"http://www.w3.org/2000/xmlns/\", \"xmlns:name\") } {} } catch err:XQDY0096 { \"Invalid element.\" }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Invalid namespace node.")
    );
  }

  /**
   * FAOR0002 must be caught, even when detectable statically..
   */
  @org.junit.Test
  public void tryCatchAllDynamicErrorsCaught15() {
    final XQuery query = new XQuery(
      "try { 10000000000000000000000000001 - 10000000000000000000000000000 } catch err:FAOR0002 { 1 }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * XPDY0050 must be caught..
   */
  @org.junit.Test
  public void tryCatchAllDynamicErrorsCaught2() {
    final XQuery query = new XQuery(
      "try { \"\" treat as element() } catch err:XPDY0050 { \"Sequence type mismatch.\" }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
      "    ",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "Invalid document.")
      ||
        error("XQDY0084")
      )
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("\"Division by zero\"")
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("\"Division by zero\"")
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("\"Division by zero\"")
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("\"Division by zero\"")
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
   * A dynamic error outside a try catch expression is not caught..
   */
  @org.junit.Test
  public void tryCatchDynamicErrorOutside2() {
    final XQuery query = new XQuery(
      "1 + (try { \"\" } catch err:XPTY0004 { \"Invalid argument\" })",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * A dynamic error outside a try catch expression is not caught..
   */
  @org.junit.Test
  public void tryCatchDynamicErrorOutside3() {
    final XQuery query = new XQuery(
      "1 + (try { \"\" } catch err:* { \"Invalid argument\" })",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * A dynamic error outside a try catch expression is not caught..
   */
  @org.junit.Test
  public void tryCatchDynamicErrorOutside4() {
    final XQuery query = new XQuery(
      "1 + (try { \"\" } catch *:XPTY0004 { \"Invalid argument\" })",
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
   * A dynamic error outside a try catch expression is not caught..
   */
  @org.junit.Test
  public void tryCatchDynamicErrorOutside5() {
    final XQuery query = new XQuery(
      "(try { \"\" } catch * { \"Invalid argument\" }) + 1",
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
   * A dynamic error outside a try catch expression is not caught..
   */
  @org.junit.Test
  public void tryCatchDynamicErrorOutside6() {
    final XQuery query = new XQuery(
      "(try { \"\" } catch err:FOAR0001 { \"Invalid argument\" }) + 1",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * A dynamic error outside a try catch expression is not caught..
   */
  @org.junit.Test
  public void tryCatchDynamicErrorOutside7() {
    final XQuery query = new XQuery(
      "(try { \"\" } catch err:* { \"Invalid argument\" }) + 1",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * A dynamic error outside a try catch expression is not caught..
   */
  @org.junit.Test
  public void tryCatchDynamicErrorOutside8() {
    final XQuery query = new XQuery(
      "(try { \"\" } catch *:FOAR0001 { \"Invalid argument\" }) + 1",
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
   * A dynamic error after a try catch expression is not caught..
   */
  @org.junit.Test
  public void tryCatchDynamicErrorOutsideAfter1() {
    final XQuery query = new XQuery(
      "try { () } catch * { \"Division by zero\" }, 1 div 0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * err:column-number must be defined..
   */
  @org.junit.Test
  public void tryCatchErrColumnNumberVariable2() {
    final XQuery query = new XQuery(
      "try { fn:error() } catch * { count($err:column-number) }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * err:code returns a description string or the empty sequence..
   */
  @org.junit.Test
  public void tryCatchErrDescriptionVariable2() {
    final XQuery query = new XQuery(
      "try { 1 div 0 } catch * { if (($err:description)) then $err:description instance of xs:string else fn:true() }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * err:code returns a description string or the empty sequence..
   */
  @org.junit.Test
  public void tryCatchErrDescriptionVariable3() {
    final XQuery query = new XQuery(
      "try { fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:FOER0000'), \"Description\") } catch * { $err:description }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * A dynamic error raised by a non-executed catch clause must be ignored..
   */
  @org.junit.Test
  public void tryCatchErrDynamicErrorInCatchClause4() {
    final XQuery query = new XQuery(
      "try { fn:error() } catch err:FOER0001 { fn:error() } catch err:FOER0000 { 0 }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * A dynamic error raised by a non-executed catch clause must be ignored..
   */
  @org.junit.Test
  public void tryCatchErrDynamicErrorInCatchClause5() {
    final XQuery query = new XQuery(
      "try { fn:error() } catch err:FOER0000 { 0 } catch err:FOER0001 { fn:error() }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * err:line-number must be defined..
   */
  @org.junit.Test
  public void tryCatchErrLineNumberVariable1() {
    final XQuery query = new XQuery(
      "try { fn:error() } catch * { let $n := $err:line-number return true() }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * err:line-number must be defined..
   */
  @org.junit.Test
  public void tryCatchErrLineNumberVariable2() {
    final XQuery query = new XQuery(
      "try { fn:error() } catch * { count($err:line-number) }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * err:module must be defined..
   */
  @org.junit.Test
  public void tryCatchErrModuleVariable2() {
    final XQuery query = new XQuery(
      "try { fn:error() } catch * { count($err:module) }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * err:value returns a sequence of error values..
   */
  @org.junit.Test
  public void tryCatchErrValueVariable1() {
    final XQuery query = new XQuery(
      "try { fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:FOER0000'), \"Description\", \"Value\") } catch * { $err:value }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * err:value returns a sequence of error values..
   */
  @org.junit.Test
  public void tryCatchErrValueVariable3() {
    final XQuery query = new XQuery(
      "try { fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'err:FOER0000'), \"Description\", (\"Value\", 3)) } catch * { $err:value[2] }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * An error raised with fn:error() must be handled as a dynamic error..
   */
  @org.junit.Test
  public void tryCatchErrorFunctionWithStaticErrorCode() {
    final XQuery query = new XQuery(
      "try { fn:error(xs:QName(\"err:XPST0008\")) } catch err:XPST0008 { 0 }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * The first matching catch clause is used..
   */
  @org.junit.Test
  public void tryCatchFirstMatchingCatchClause1() {
    final XQuery query = new XQuery(
      "try { 1 div 0 } catch err:FOAR0001 { \"Clause 1\" } catch err:FOAR0001 { \"Clause 2\" }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Clause 2")
    );
  }

  /**
   * Try-catch must match fn:error semantics..
   */
  @org.junit.Test
  public void tryCatchFnError1() {
    final XQuery query = new XQuery(
      "fn:error(fn:QName(\"http://www.example.com/\", \"example:EXER3141\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("Q{http://www.example.com/}EXER3141")
    );
  }

  /**
   * Try-catch must match fn:error semantics..
   */
  @org.junit.Test
  public void tryCatchFnError10() {
    final XQuery query = new XQuery(
      "try { fn:error(fn:QName(\"http://www.example.com/\", \"example:EXER3141\"), \"Description\") } catch Q{http://www.example.com/}EXER3141 { $err:description }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'Description'")
    );
  }

  /**
   * Try-catch must match fn:error semantics..
   */
  @org.junit.Test
  public void tryCatchFnError11() {
    final XQuery query = new XQuery(
      "try { fn:error(fn:QName(\"http://www.example.com/\", \"example:EXER3141\"), \"Description\", (1, 2)) } catch Q{http://www.example.com/}EXER3141 { $err:value[2] }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * Try-catch must match any error raised by fn:error..
   */
  @org.junit.Test
  public void tryCatchFnError12() {
    final XQuery query = new XQuery(
      "try { fn:error(xs:QName(\"err:XPTY0004\")) } catch Q{http://www.w3.org/2005/xqt-errors}XPTY0004 { 0 }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * Try-catch must match any error raised by fn:error..
   */
  @org.junit.Test
  public void tryCatchFnError13() {
    final XQuery query = new XQuery(
      "try { fn:error(xs:QName(\"err:XPST0001\")) } catch Q{http://www.w3.org/2005/xqt-errors}XPST0001 { 0 }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * Try-catch must match any error raised by fn:error..
   */
  @org.junit.Test
  public void tryCatchFnError14() {
    final XQuery query = new XQuery(
      "try { fn:error(xs:QName(\"err:XPDY0002\")) } catch Q{http://www.w3.org/2005/xqt-errors}XPDY0002 { 0 }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * Try-catch must match any error raised by fn:error..
   */
  @org.junit.Test
  public void tryCatchFnError15() {
    final XQuery query = new XQuery(
      "try { fn:error(xs:QName(\"err:XQTY0105\")) } catch Q{http://www.w3.org/2005/xqt-errors}XQTY0105 { 0 }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * Try-catch must match any error raised by fn:error..
   */
  @org.junit.Test
  public void tryCatchFnError16() {
    final XQuery query = new XQuery(
      "try { fn:error(xs:QName(\"err:XQST0089\")) } catch Q{http://www.w3.org/2005/xqt-errors}XQST0089 { 0 }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * Try-catch must match any error raised by fn:error..
   */
  @org.junit.Test
  public void tryCatchFnError17() {
    final XQuery query = new XQuery(
      "try { fn:error(xs:QName(\"err:XQDY0061\")) } catch Q{http://www.w3.org/2005/xqt-errors}XQDY0061 { 0 }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * Try-catch must match any error raised by fn:error..
   */
  @org.junit.Test
  public void tryCatchFnError18() {
    final XQuery query = new XQuery(
      "try { fn:error(xs:QName(\"err:FOTY0014\")) } catch Q{http://www.w3.org/2005/xqt-errors}FOTY0014 { 0 }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * Try-catch must match any error raised by fn:error..
   */
  @org.junit.Test
  public void tryCatchFnError19() {
    final XQuery query = new XQuery(
      "try { fn:error(xs:QName(\"err:FORG0001\")) } catch Q{http://www.w3.org/2005/xqt-errors}FORG0001 { 0 }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * Try-catch must match fn:error semantics..
   */
  @org.junit.Test
  public void tryCatchFnError2() {
    final XQuery query = new XQuery(
      "try { fn:error(fn:QName(\"http://www.example.com/\", \"example:EXER3141\")) } catch * { 0 }",
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
   * Try-catch must match any error raised by fn:error..
   */
  @org.junit.Test
  public void tryCatchFnError20() {
    final XQuery query = new XQuery(
      "try { fn:error(xs:QName(\"err:FONS0004\")) } catch Q{http://www.w3.org/2005/xqt-errors}FONS0004 { 0 }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * Try-catch must match fn:error semantics..
   */
  @org.junit.Test
  public void tryCatchFnError3() {
    final XQuery query = new XQuery(
      "try { fn:error(fn:QName(\"http://www.example.com/\", \"example:EXER3141\")) } catch Q{http://www.example.com/}EXER3141 { 0 }",
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
   * Try-catch must match fn:error semantics..
   */
  @org.junit.Test
  public void tryCatchFnError4() {
    final XQuery query = new XQuery(
      "try { fn:error(fn:QName(\"http://www.example.com/\", \"example:EXER3141\")) } catch Q{http://www.example.com/}* { 0 }",
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
   * Try-catch must match fn:error semantics..
   */
  @org.junit.Test
  public void tryCatchFnError5() {
    final XQuery query = new XQuery(
      "try { fn:error(fn:QName(\"http://www.example.com/\", \"example:EXER3141\")) } catch *:EXER3141 { 0 }",
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
   * Try-catch must match fn:error semantics..
   */
  @org.junit.Test
  public void tryCatchFnError6() {
    final XQuery query = new XQuery(
      "try { fn:error(fn:QName(\"http://www.example.com/\", \"example:EXER3141\"), \"Description\") } catch Q{http://www.example.com/}EXER3141 { $err:code }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertQuery("local-name-from-QName($result) eq 'EXER3141'")
      &&
        assertQuery("namespace-uri-from-QName($result) eq \"http://www.example.com/\"")
      )
    );
  }

  /**
   * Try-catch must match fn:error semantics..
   */
  @org.junit.Test
  public void tryCatchFnError7() {
    final XQuery query = new XQuery(
      "try { fn:error(fn:QName(\"http://www.example.com/\", \"example:EXER3141\"), \"Description\") } catch Q{http://www.example.com/}EXER3141 { namespace-uri-from-QName($err:code) }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'http://www.example.com/'")
    );
  }

  /**
   * Try-catch must match fn:error semantics..
   */
  @org.junit.Test
  public void tryCatchFnError8() {
    final XQuery query = new XQuery(
      "try { fn:error(fn:QName(\"http://www.example.com/\", \"example:EXER3141\"), \"Description\") } catch Q{http://www.example.com/}EXER3141 { prefix-from-QName($err:code) }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'example'")
    );
  }

  /**
   * Try-catch must match fn:error semantics..
   */
  @org.junit.Test
  public void tryCatchFnError9() {
    final XQuery query = new XQuery(
      "try { fn:error(fn:QName(\"http://www.example.com/\", \"example:EXER3141\"), \"Description\") } catch Q{http://www.example.com/}EXER3141 { local-name-from-QName($err:code) }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'EXER3141'")
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * If a function call occurs within a try clause, errors raised by evaluating the corresponding function are caught by the try/catch expression..
   */
  @org.junit.Test
  public void tryCatchFunctionCall2() {
    final XQuery query = new XQuery(
      "try { fn:one-or-more(()) } catch err:FORG0004 { 0 }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * If a function call occurs within a try clause, errors raised by evaluating the corresponding function are caught by the try/catch expression..
   */
  @org.junit.Test
  public void tryCatchFunctionCall3() {
    final XQuery query = new XQuery(
      "try { function() { fn:error() } () } catch err:FOER0000 { 0 }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * If a function call occurs within a try clause, errors raised by evaluating the corresponding function are caught by the try/catch expression..
   */
  @org.junit.Test
  public void tryCatchFunctionCall4() {
    final XQuery query = new XQuery(
      "try { function() { 1 div 0 } () } catch err:FOAR0001 { 0 }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * Can't catch this error within the function body.
   */
  @org.junit.Test
  public void tryCatchFunctionResultType1() {
    final XQuery query = new XQuery(
      "declare function local:thrice($x as xs:integer) as xs:integer\n" +
      "      { try { if (current-date() gt xs:date('2000-01-01')) then \"three\" else 3 } catch * { 3 } };\n" +
      "      local:thrice(3)\n" +
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
      error("XPTY0004")
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
      "try { 1 div 0 } catch Q{http://www.w3.org/2005/not-xqt-errors}* { \"Division by zero\" }",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
      "try { 1 div 0 } catch Q{http://www.w3.org/2005/xqt-errors}XPST0008 { \"Division by zero\" }",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOAR0001")
    );
  }

  /**
   * If there is no matching catch clause, the error is forwarded..
   */
  @org.junit.Test
  public void tryCatchNameNoMatchingCatchClause6() {
    final XQuery query = new XQuery(
      "try { 1 div 0 } catch err:XPST0008 { \"Division by zero\" } catch xs:* { \"Division by zero\" } catch *:XPST0008 { \"Division by zero\" } catch Q{http://www.w3.org/2005/not-xqt-errors}* { \"Division by zero\" } catch Q{http://www.w3.org/2005/xqt-errors}XPST0008 { \"Division by zero\" }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
      "try { 1 div 0 } catch Q{http://www.w3.org/2005/xqt-errors}FOAR0001 { \"Division by zero\" }",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Division by zero")
    );
  }

  /**
   * URI literals are supported in name tests..
   */
  @org.junit.Test
  public void tryCatchNameTestsNamespace2() {
    final XQuery query = new XQuery(
      "try { 1 div 0 } catch Q{http://www.w3.org/2005/xqt-errors}* { \"Division by zero\" }",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
   * If no error is raised, the result of the try catch expression is the result of the try clause..
   */
  @org.junit.Test
  public void tryCatchNoError2() {
    final XQuery query = new XQuery(
      "try { 0 } catch err:FOAR0001 { \"No error\" }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * If no error is raised, the result of the try catch expression is the result of the try clause..
   */
  @org.junit.Test
  public void tryCatchNoError3() {
    final XQuery query = new XQuery(
      "try { 0 } catch err:* { \"No error\" }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * If no error is raised, the result of the try catch expression is the result of the try clause..
   */
  @org.junit.Test
  public void tryCatchNoError4() {
    final XQuery query = new XQuery(
      "try { 0 } catch *:FOAR0001 { \"No error\" }",
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
   * If no error is raised, the result of the try catch expression is the result of the try clause..
   */
  @org.junit.Test
  public void tryCatchNoError5() {
    final XQuery query = new XQuery(
      "try { 0 } catch err:FOAR0001 { \"No error\" } catch *:FOAR0001 { \"No error \"} catch err:* { \"No error \"}  catch * { \"No error \"}",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * If the evaluation of an expression inside a try/catch is rewritten or deferred in this way, it must take its try/catch context with it..
   */
  @org.junit.Test
  public void tryCatchOptimizations1() {
    final XQuery query = new XQuery(
      "try { 1 div 0 } catch * { 0 }, try { 1 div 0 } catch err:FOAR0001 { 1}",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("0, 1")
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * An undefined variable (static error) is not caught..
   */
  @org.junit.Test
  public void tryCatchStaticError1() {
    final XQuery query = new XQuery(
      "try { $x } catch * { \"Undefined variable\" }",
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
   * An undefined variable (static error) is not caught..
   */
  @org.junit.Test
  public void tryCatchStaticError2() {
    final XQuery query = new XQuery(
      "try { $x } catch err:XPST0008 { \"Undefined variable\" }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * An undefined variable (static error) is not caught..
   */
  @org.junit.Test
  public void tryCatchStaticError3() {
    final XQuery query = new XQuery(
      "try { $x } catch err:* { \"Undefined variable\" }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * An undefined variable (static error) is not caught..
   */
  @org.junit.Test
  public void tryCatchStaticError4() {
    final XQuery query = new XQuery(
      "try { $x } catch *:XPST0008 { \"Undefined variable\" }",
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
   * A static type mismatch (type error) is caught..
   */
  @org.junit.Test
  public void tryCatchTypeError1() {
    final XQuery query = new XQuery(
      "try { let $i as xs:string := 1 return $i } catch * { \"Type error\" }",
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
        assertEq("\"Type error\"")
      )
    );
  }

  /**
   * A static type mismatch (type error) is caught..
   */
  @org.junit.Test
  public void tryCatchTypeError2() {
    final XQuery query = new XQuery(
      "try { let $i as xs:string := 1 return $i } catch err:XPTY0004 { \"Type error\" }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
        assertEq("\"Type error\"")
      )
    );
  }

  /**
   * A static type mismatch (type error) is caught..
   */
  @org.junit.Test
  public void tryCatchTypeError3() {
    final XQuery query = new XQuery(
      "try { let $i as xs:string := 1 return $i } catch err:* { \"Type error\" }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
        assertEq("\"Type error\"")
      )
    );
  }

  /**
   * A static type mismatch (type error) is caught..
   */
  @org.junit.Test
  public void tryCatchTypeError4() {
    final XQuery query = new XQuery(
      "try { let $i as xs:string := 1 return $i } catch *:XPTY0004 { \"Type error\" }",
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
        assertEq("\"Type error\"")
      )
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * If a variable reference is used in a try clause, errors raised by binding a value to the variable are not caught unless the binding expression occurs within the try clause..
   */
  @org.junit.Test
  public void tryCatchVariableBindingOutside2() {
    final XQuery query = new XQuery(
      "try { let $x as xs:integer := \"\" return $x } catch err:XPTY0004 { 0 }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * If a variable reference is used in a try clause, errors raised by binding a value to the variable are not caught unless the binding expression occurs within the try clause..
   */
  @org.junit.Test
  public void tryCatchVariableBindingOutside5() {
    final XQuery query = new XQuery(
      "for tumbling window $x as xs:string in (2, 4, 6, 8, 10)\n" +
      "      start $s at $spos previous $sprev next $snext when true() end $e at\n" +
      "      $epos previous $eprev next $enext when true() return try { $x } catch err:XPTY0004 { 0 }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
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
   * If a variable reference is used in a try clause, errors raised by binding a value to the variable are not caught unless the binding expression occurs within the try clause..
   */
  @org.junit.Test
  public void tryCatchVariableBindingOutside6() {
    final XQuery query = new XQuery(
      "try { for tumbling window $x as xs:string in (2, 4, 6, 8, 10)\n" +
      "      start $s at $spos previous $sprev next $snext when true() end $e at\n" +
      "      $epos previous $eprev next $enext when true() return $x } catch err:XPTY0004 { 0 }",
      ctx);
    try {
      query.namespace("err", "http://www.w3.org/2005/xqt-errors");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("0")
      ||
        error("XPTY0004")
      )
    );
  }
}
