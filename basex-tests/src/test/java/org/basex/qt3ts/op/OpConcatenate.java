package org.basex.qt3ts.op;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the concatenate() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpConcatenate extends QT3TestSet {

  /**
   *  A heavily nested sequence of expressions with the comma operator. On some implementations this triggers certain optimization paths. .
   */
  @org.junit.Test
  public void kCommaOp1() {
    final XQuery query = new XQuery(
      "deep-equal(((1, (2, (3, 4, (5, 6)), 7), 8, (9, 10), 11)), (1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11))",
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
   *  An expression sequence containing only empty sequences. On some implementations this triggers certain optimization paths. .
   */
  @org.junit.Test
  public void kCommaOp2() {
    final XQuery query = new XQuery(
      "empty(((), (), ((), (), ((), (), (())), ()), (), (())))",
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
   *  An expression sequence containing many empty sequences and one xs:string. On some implementations this triggers certain optimization paths. .
   */
  @org.junit.Test
  public void kCommaOp3() {
    final XQuery query = new XQuery(
      "((), (), ((), (), ((), (), (\"str\")), ()), (), (())) eq \"str\"",
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
   *  Constructing sequences. Constructing a sequence, where one of the members is an addition operation .
   */
  @org.junit.Test
  public void constSeq1() {
    final XQuery query = new XQuery(
      "(1, 1 + 1, 3, 4, 5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("1, 2, 3, 4, 5")
    );
  }

  /**
   *  Constructing sequences. Constructing a sequence, where both members contains invocation to "fn:not()" function. .
   */
  @org.junit.Test
  public void constSeq10() {
    final XQuery query = new XQuery(
      " (fn:not(\"true\"),fn:not(\"false\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("false(), false()")
    );
  }

  /**
   *  Constructing sequences. Constructing a sequence, where one of its members contains a boolean (and) operation. .
   */
  @org.junit.Test
  public void constSeq11() {
    final XQuery query = new XQuery(
      " (fn:true() and fn:true(), fn:true())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("true(), true()")
    );
  }

  /**
   *  Constructing sequences. Constructing a sequence, where one of its members contains a boolean (or) operation. .
   */
  @org.junit.Test
  public void constSeq12() {
    final XQuery query = new XQuery(
      " (fn:true() or fn:true(), fn:true())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("true(), true()")
    );
  }

  /**
   *  Constructing sequences. Constructing a sequence, where one of its members contains invocation to "xs:string()". .
   */
  @org.junit.Test
  public void constSeq13() {
    final XQuery query = new XQuery(
      " (xs:string(\"ABC\"), \"D\", \"E\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"ABC\", \"D\", \"E\"")
    );
  }

  /**
   *  Constructing sequences. Constructing a sequence, where one of its members contains invocation to "xs:integer". .
   */
  @org.junit.Test
  public void constSeq14() {
    final XQuery query = new XQuery(
      " (xs:integer(1), 2, 3)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("1, 2, 3")
    );
  }

  /**
   *  Constructing sequences. Constructing a sequence, where one of its members contains invocation to "xs:decimal()". .
   */
  @org.junit.Test
  public void constSeq15() {
    final XQuery query = new XQuery(
      " (xs:decimal(1), 2, 3)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("1, 2, 3")
    );
  }

  /**
   *  Constructing sequences. Constructing a sequence, where both of its members contains invocation to "xs:anyURI". .
   */
  @org.junit.Test
  public void constSeq16() {
    final XQuery query = new XQuery(
      " (xs:anyURI(\"http://www.example.com\"),xs:anyURI(\"http://www.example1.com\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"http://www.example.com\", \"http://www.example1.com\"")
    );
  }

  /**
   *  Constructing sequences. Constructing a sequence, where one of its members contains invocation to "xs:float". .
   */
  @org.junit.Test
  public void constSeq17() {
    final XQuery query = new XQuery(
      " (xs:float(1.1), 2.2, 3.3)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("xs:float('1.1e0'), 2.2, 3.3")
    );
  }

  /**
   *  Constructing sequences. Constructing a sequence, where one of its members contains invocation to "xs:double()". .
   */
  @org.junit.Test
  public void constSeq18() {
    final XQuery query = new XQuery(
      " (xs:double(1.2E2), 2.2E2, 3.3E2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("120, 220, 330")
    );
  }

  /**
   *  Constructing sequences. Constructing a sequence, where one of its members contains invocation to "xs:boolean()". .
   */
  @org.junit.Test
  public void constSeq19() {
    final XQuery query = new XQuery(
      " (xs:boolean(fn:true()), fn:false(), fn:true())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("true(), false(), true()")
    );
  }

  /**
   *  Constructing sequences. Constructing a sequence, where one of the members is subtraction operation. .
   */
  @org.junit.Test
  public void constSeq2() {
    final XQuery query = new XQuery(
      "(1, 3 - 1, 3, 4, 5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("1, 2 ,3, 4, 5")
    );
  }

  /**
   *  Constructing sequences. Constructing a sequence, where both of its members contains invocation to "xs:date()". .
   */
  @org.junit.Test
  public void constSeq20() {
    final XQuery query = new XQuery(
      " (xs:date(\"2004-12-25Z\"),xs:date(\"2004-12-26Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2004-12-25Z 2004-12-26Z")
    );
  }

  /**
   *  Constructing sequences. Constructing a sequence, where both of its members contains invocation to "xs:dateTime()". .
   */
  @org.junit.Test
  public void constSeq21() {
    final XQuery query = new XQuery(
      " (xs:dateTime(\"1999-11-28T09:00:00Z\"),xs:dateTime(\"1998-11-28T09:00:00Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1999-11-28T09:00:00Z 1998-11-28T09:00:00Z")
    );
  }

  /**
   *  Constructing sequences. Constructing a sequence, where both of its members contains invocation to "xs:time()". .
   */
  @org.junit.Test
  public void constSeq22() {
    final XQuery query = new XQuery(
      " (xs:time(\"08:00:00+09:00\"),xs:time(\"08:00:00+10:00\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "08:00:00+09:00 08:00:00+10:00")
    );
  }

  /**
   *  Constructing sequences. Constructing a sequence, where one of the members is a multiplication operation. .
   */
  @org.junit.Test
  public void constSeq3() {
    final XQuery query = new XQuery(
      " (1, 2 * 1, 3, 4, 5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("1, 2, 3, 4, 5")
    );
  }

  /**
   *  Constructing sequences. Constructing a sequence, where one of the members is a division (div) operation. .
   */
  @org.junit.Test
  public void constSeq4() {
    final XQuery query = new XQuery(
      " (1, 4 div 2, 3, 4, 5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("1, 2, 3, 4, 5")
    );
  }

  /**
   *  Constructing sequences. Constructing a sequence, where one of the members is a division (idiv) operation. .
   */
  @org.junit.Test
  public void constSeq5() {
    final XQuery query = new XQuery(
      " (1, 4 idiv 2, 3, 4, 5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("1, 2, 3, 4, 5")
    );
  }

  /**
   *  Constructing sequences. Constructing a sequence, where one of the members contains invocation to "fn:count" function. .
   */
  @org.junit.Test
  public void constSeq6() {
    final XQuery query = new XQuery(
      " (1, fn:count((1, 2)), 3, 4, 5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("1, 2, 3, 4, 5")
    );
  }

  /**
   *  Constructing sequences. Constructing a sequence, where one of the members contains invocation to "fn:string-length" function. .
   */
  @org.junit.Test
  public void constSeq7() {
    final XQuery query = new XQuery(
      " (1, fn:string-length(\"AB\"), 3, 4, 5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("1, 2, 3, 4, 5")
    );
  }

  /**
   *  Constructing sequences. Constructing a sequence, where both members contains invocation to "fn:true" function. .
   */
  @org.junit.Test
  public void constSeq8() {
    final XQuery query = new XQuery(
      " (fn:true(),fn:true())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("true(), true()")
    );
  }

  /**
   *  Constructing sequences. Constructing a sequence, where both members contains invocation to "fn:false" function. .
   */
  @org.junit.Test
  public void constSeq9() {
    final XQuery query = new XQuery(
      " (fn:false(),fn:false())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("false(), false()")
    );
  }

  /**
   *  arg1 & arg2 : sequence of number .
   */
  @org.junit.Test
  public void opConcatenateMixArgs001() {
    final XQuery query = new XQuery(
      "(1) , (2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("1, 2")
    );
  }

  /**
   *  arg1 & arg2 : string .
   */
  @org.junit.Test
  public void opConcatenateMixArgs002() {
    final XQuery query = new XQuery(
      "xs:string(\"a\") , xs:string(\"b\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"a\", \"b\"")
    );
  }

  /**
   *  args : string .
   */
  @org.junit.Test
  public void opConcatenateMixArgs003() {
    final XQuery query = new XQuery(
      "xs:string(\"a\") , (), \"xyz\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"a\", \"xyz\"")
    );
  }

  /**
   *  args : string .
   */
  @org.junit.Test
  public void opConcatenateMixArgs004() {
    final XQuery query = new XQuery(
      "\"xyz\" , xs:string(\" \"), \"b\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"xyz\", \" \", \"b\"")
    );
  }

  /**
   *  arg1 : string, arg2:anyURI .
   */
  @org.junit.Test
  public void opConcatenateMixArgs005() {
    final XQuery query = new XQuery(
      "xs:string(\"a\") , xs:anyURI(\"www.example.com\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"a\", \"www.example.com\"")
    );
  }

  /**
   *  arg1 : string, arg2:integer, arg3:anyURI .
   */
  @org.junit.Test
  public void opConcatenateMixArgs006() {
    final XQuery query = new XQuery(
      "xs:string(\"hello\") , xs:integer(\"100\"), xs:anyURI(\"www.example.com\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"hello\", 100, \"www.example.com\"")
    );
  }

  /**
   *  arg1 : anyURI, arg2: decimal .
   */
  @org.junit.Test
  public void opConcatenateMixArgs007() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"www.example.com\") , xs:decimal(\"1.01\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"www.example.com\", 1.01")
    );
  }

  /**
   *  arg1 & arg2 : float .
   */
  @org.junit.Test
  public void opConcatenateMixArgs008() {
    final XQuery query = new XQuery(
      "xs:float(\"1.01\"), xs:float(\"NaN\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1.01 NaN")
    );
  }

  /**
   *  arg1:float, arg2: double .
   */
  @org.junit.Test
  public void opConcatenateMixArgs009() {
    final XQuery query = new XQuery(
      "xs:float(\"INF\") , xs:double(\"NaN\")",
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
   *  arg1: double, arg2: double, arg3:float .
   */
  @org.junit.Test
  public void opConcatenateMixArgs010() {
    final XQuery query = new XQuery(
      "xs:double(\"INF\"), xs:double(\"-INF\"), xs:float(\"-INF\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "INF -INF -INF")
    );
  }

  /**
   *  arg1:boolean, arg2: boolean, arg3: integer .
   */
  @org.junit.Test
  public void opConcatenateMixArgs011() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") , xs:boolean(\"0\"), xs:integer(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("true(), false(), 0")
    );
  }

  /**
   *  arg1:boolean, arg2: boolean .
   */
  @org.junit.Test
  public void opConcatenateMixArgs012() {
    final XQuery query = new XQuery(
      "xs:boolean(\"false\"), xs:boolean(\"1\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("false(), true()")
    );
  }

  /**
   *  arg1:date, arg2: boolean, arg3: string .
   */
  @org.junit.Test
  public void opConcatenateMixArgs013() {
    final XQuery query = new XQuery(
      "xs:date(\"1993-03-31\") , xs:boolean(\"true\"), xs:string(\"abc\")",
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
   *  arg1:dateTime, arg2: empty sequence .
   */
  @org.junit.Test
  public void opConcatenateMixArgs014() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1972-12-31T00:00:00Z\") , (())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1972-12-31T00:00:00Z")
    );
  }

  /**
   *  arg1:time, arg2: string , arg3: decimal .
   */
  @org.junit.Test
  public void opConcatenateMixArgs015() {
    final XQuery query = new XQuery(
      "xs:time(\"12:30:00Z\") , xs:string(\" \") , xs:decimal(\"2.000000000000002\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "12:30:00Z   2.000000000000002")
    );
  }

  /**
   *  arg1:empty seq, arg2: string , arg3: decimal .
   */
  @org.junit.Test
  public void opConcatenateMixArgs016() {
    final XQuery query = new XQuery(
      "() , xs:string(\" \") , xs:decimal(\"2.000000000000002\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "  2.000000000000002")
    );
  }

  /**
   *  Use simple arithmetic expression with concat .
   */
  @org.junit.Test
  public void opConcatenateMixArgs017() {
    final XQuery query = new XQuery(
      "(1+1), (2-2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("2, 0")
    );
  }

  /**
   *  Concat more than two sequences .
   */
  @org.junit.Test
  public void opConcatenateMixArgs018() {
    final XQuery query = new XQuery(
      "(1,2,2),(1,2,3),(123,\"\"),(),(\"\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("1, 2, 2, 1, 2, 3, 123, \"\", \"\"")
    );
  }

  /**
   * Written By: Ravindranath Chennnoju  Use an external variable with op:concatenate .
   */
  @org.junit.Test
  public void opConcatenateMixArgs019() {
    final XQuery query = new XQuery(
      "//book/price, (), (1)",
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
   *  Use two external variables with op:contenate .
   */
  @org.junit.Test
  public void opConcatenateMixArgs020() {
    final XQuery query = new XQuery(
      "//book/price, //book/title",
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
      assertSerialization("<price>65.95</price><price>65.95</price><price>39.95</price><price>129.95</price><title>TCP/IP Illustrated</title><title>Advanced Programming in the Unix environment</title><title>Data on the Web</title><title>The Economics of Technology and Content for Digital TV</title>", false)
    );
  }

  /**
   *  Constructing Sequences. Simple sequence involving integers. .
   */
  @org.junit.Test
  public void sequenceexpressionhc1() {
    final XQuery query = new XQuery(
      "(1,2,3,4,5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("1, 2, 3, 4, 5")
    );
  }

  /**
   *  Constructing Sequences. Simple sequence involving nested sequences. .
   */
  @org.junit.Test
  public void sequenceexpressionhc2() {
    final XQuery query = new XQuery(
      "(1,(2,3),4,5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("1, 2, 3, 4, 5")
    );
  }

  /**
   *  Constructing Sequences. Simple sequence involving the empty sequence. .
   */
  @org.junit.Test
  public void sequenceexpressionhc3() {
    final XQuery query = new XQuery(
      "(1, 2, (), 3, 4)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("1, 2, 3, 4")
    );
  }

  /**
   *  Constructing Sequences. Simple sequence involving the "to" operand. .
   */
  @org.junit.Test
  public void sequenceexpressionhc4() {
    final XQuery query = new XQuery(
      "(1, 2 to 5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("1, 2, 3, 4, 5")
    );
  }

  /**
   *  Constructing Sequences. Simple sequence involving repetition .
   */
  @org.junit.Test
  public void sequenceexpressionhc5() {
    final XQuery query = new XQuery(
      "(1, 2, 2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("1, 2, 2")
    );
  }

  /**
   *  Constructing Sequences. Simple sequence expression resulting in an empty sequence. Uses count to avoid empty file. .
   */
  @org.junit.Test
  public void sequenceexpressionhc6() {
    final XQuery query = new XQuery(
      "count((15 to 10))",
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
   *  Constructing Sequences. Simple sequence expression that results on a sequence in reverse order .
   */
  @org.junit.Test
  public void sequenceexpressionhc7() {
    final XQuery query = new XQuery(
      "fn:reverse(10 to 15)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("15, 14, 13, 12, 11, 10")
    );
  }

  /**
   *  Constructing Sequences. Sequence expression resulting by quering xml file string data .
   */
  @org.junit.Test
  public void sequenceexpressionhc8() {
    final XQuery query = new XQuery(
      "//empnum",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<empnum>E1</empnum><empnum>E1</empnum><empnum>E1</empnum><empnum>E1</empnum><empnum>E1</empnum><empnum>E1</empnum><empnum>E2</empnum><empnum>E2</empnum><empnum>E3</empnum><empnum>E3</empnum><empnum>E4</empnum><empnum>E4</empnum><empnum>E4</empnum>", false)
    );
  }

  /**
   *  Constructing Sequences. Sequence expression resulting by quering xml file string data (multiple xml sources) .
   */
  @org.junit.Test
  public void sequenceexpressionhc9() {
    final XQuery query = new XQuery(
      " ($works//empnum,$staff//empname)",
      ctx);
    try {
      query.bind("$works", node(file("docs/works.xml")));
      query.bind("$staff", node(file("docs/staff.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<empnum>E1</empnum><empnum>E1</empnum><empnum>E1</empnum><empnum>E1</empnum><empnum>E1</empnum><empnum>E1</empnum><empnum>E2</empnum><empnum>E2</empnum><empnum>E3</empnum><empnum>E3</empnum><empnum>E4</empnum><empnum>E4</empnum><empnum>E4</empnum><empname>Alice</empname><empname>Betty</empname><empname>Carmen</empname><empname>Don</empname><empname>Ed</empname>", false)
    );
  }
}
