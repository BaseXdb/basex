package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the insert-before() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnInsertBefore extends QT3TestSet {

  /**
   *  A test whose essence is: `insert-before()`. .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc1() {
    final XQuery query = new XQuery(
      "insert-before()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `insert-before((), 30, 7) eq 7`. .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc10() {
    final XQuery query = new XQuery(
      "insert-before((), 30, 7) eq 7",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(insert-before((1, 2, 3, 4), 30, ())) eq 4`. .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc11() {
    final XQuery query = new XQuery(
      "count(insert-before((1, 2, 3, 4), 30, ())) eq 4",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `insert-before(9, 30, ()) eq 9`. .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc12() {
    final XQuery query = new XQuery(
      "insert-before(9, 30, ()) eq 9",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(insert-before((1, 2, 3, 4), 1, ())) eq 4`. .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc13() {
    final XQuery query = new XQuery(
      "count(insert-before((1, 2, 3, 4), 1, ())) eq 4",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(insert-before((1, 2, 3), 30, (4, 5, 6))) eq 6`. .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc14() {
    final XQuery query = new XQuery(
      "count(insert-before((1, 2, 3), 30, (4, 5, 6))) eq 6",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(insert-before((), 30, (1, 2, 3))) eq 3`. .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc15() {
    final XQuery query = new XQuery(
      "count(insert-before((), 30, (1, 2, 3))) eq 3",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(insert-before((error(), 1), 1, (1, "two", 3))) > 1`. .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc16() {
    final XQuery query = new XQuery(
      "count(insert-before((error(), 1), 1, (1, \"two\", 3))) > 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertBoolean(true)
      ||
        error("FOER0000")
      )
    );
  }

  /**
   *  Apply a predicate to the result of fn:insert-before(). .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc17() {
    final XQuery query = new XQuery(
      "(insert-before((1, current-time(), 3), 1, (4, 5, 6))[last()] treat as xs:integer) eq 3",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Apply a predicate to the result of fn:insert-before(). .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc18() {
    final XQuery query = new XQuery(
      "(insert-before((1, current-time(), 3), 10, (4, 5, 6))[last()] treat as xs:integer) eq 6",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Apply a predicate to the result of fn:insert-before(). .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc19() {
    final XQuery query = new XQuery(
      "(insert-before((1, current-time(), 3), 10, (4, 5, 6))[last() - 3] treat as xs:integer) eq 3",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `insert-before("wrong params", 2)`. .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc2() {
    final XQuery query = new XQuery(
      "insert-before(\"wrong params\", 2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  Apply a predicate to the result of fn:insert-before(). .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc20() {
    final XQuery query = new XQuery(
      "(insert-before((1, current-time(), 3), 10, ())[last()] treat as xs:integer) eq 3",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Apply a predicate to the result of fn:insert-before(). .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc21() {
    final XQuery query = new XQuery(
      "empty(insert-before((1, current-time(), 3), 0, (4, 5, 6))[last() - 10])",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `insert-before("wrong params", 2, 3, 4)`. .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc3() {
    final XQuery query = new XQuery(
      "insert-before(\"wrong params\", 2, 3, 4)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  The second argument to fn:insert-before cannot be the empty sequence. .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc4() {
    final XQuery query = new XQuery(
      "insert-before((), (), \"a string\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  A test whose essence is: `insert-before((), -31, "a string") eq "a string"`. .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc5() {
    final XQuery query = new XQuery(
      "insert-before((), -31, \"a string\") eq \"a string\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(insert-before((1, 2, 3), 1, (4, 5, 6))) eq 6`. .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc6() {
    final XQuery query = new XQuery(
      "count(insert-before((1, 2, 3), 1, (4, 5, 6))) eq 6",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `insert-before((), 1, 3) eq 3`. .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc7() {
    final XQuery query = new XQuery(
      "insert-before((), 1, 3) eq 3",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `insert-before((), 1, "a string") eq "a string"`. .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc8() {
    final XQuery query = new XQuery(
      "insert-before((), 1, \"a string\") eq \"a string\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(insert-before((), 1, (1, 2, 3))) eq 3`. .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc9() {
    final XQuery query = new XQuery(
      "count(insert-before((), 1, (1, 2, 3))) eq 3",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  arg1: Sequence of string, arg2:integer, arg3: string .
   */
  @org.junit.Test
  public void fnInsertBeforeMixArgs001() {
    final XQuery query = new XQuery(
      "fn:insert-before( (\"a\", \"b\", \"c\"),1, \"z\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("\"z\", \"a\", \"b\", \"c\"")
    );
  }

  /**
   *  arg1: Sequence of string, arg2:integer, arg3: empty sequence .
   */
  @org.junit.Test
  public void fnInsertBeforeMixArgs002() {
    final XQuery query = new XQuery(
      "fn:insert-before( (\"a\", \"b\", \"c\"),0, ())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("\"a\", \"b\", \"c\"")
    );
  }

  /**
   *  arg1: Sequence of string, arg2:integer, arg3: empty sequence .
   */
  @org.junit.Test
  public void fnInsertBeforeMixArgs003() {
    final XQuery query = new XQuery(
      "fn:insert-before( (\"a\", \"b\", \"c\"),2, ())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("\"a\", \"b\", \"c\"")
    );
  }

  /**
   *  arg1: Sequence of string, arg2:integer, arg3: string .
   */
  @org.junit.Test
  public void fnInsertBeforeMixArgs004() {
    final XQuery query = new XQuery(
      "fn:insert-before( (\"a\", \"b\", \"c\"),2, xs:string(\" \"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("\"a\", \" \", \"b\", \"c\"")
    );
  }

  /**
   *  arg1: Sequence of string, arg2:integer, arg3: anyURI .
   */
  @org.junit.Test
  public void fnInsertBeforeMixArgs005() {
    final XQuery query = new XQuery(
      "fn:insert-before( (\"a\", \"b\", \"c\"),2, xs:anyURI(\"www.example.com\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("\"a\", \"www.example.com\", \"b\", \"c\"")
    );
  }

  /**
   *  arg1: Sequence of string, arg2:integer, arg3: integer .
   */
  @org.junit.Test
  public void fnInsertBeforeMixArgs006() {
    final XQuery query = new XQuery(
      "fn:insert-before( (\"a\", \"b\", \"c\"),2, xs:integer(\"100\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("\"a\", 100, \"b\", \"c\"")
    );
  }

  /**
   *  arg1: Sequence of string, arg2:integer, arg3: decimal .
   */
  @org.junit.Test
  public void fnInsertBeforeMixArgs007() {
    final XQuery query = new XQuery(
      "fn:insert-before( (\"a\", \"b\", \"c\"),2, xs:decimal(\"1.1\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("\"a\", 1.1, \"b\", \"c\"")
    );
  }

  /**
   *  arg1: Sequence of string, arg2:integer, arg3: float .
   */
  @org.junit.Test
  public void fnInsertBeforeMixArgs008() {
    final XQuery query = new XQuery(
      "fn:insert-before( (\"a\", \"b\", \"c\"),2, xs:float(\"1.1\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("\"a\", 1.1, \"b\", \"c\"")
    );
  }

  /**
   *  arg1: Sequence of string, arg2:integer, arg3: float .
   */
  @org.junit.Test
  public void fnInsertBeforeMixArgs009() {
    final XQuery query = new XQuery(
      "fn:insert-before( (\"a\", \"b\", \"c\"),2, xs:float(\"NaN\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("\"a\", xs:float('NaN'), \"b\", \"c\"")
    );
  }

  /**
   *  arg1: Sequence of string, arg2:integer, arg3: float .
   */
  @org.junit.Test
  public void fnInsertBeforeMixArgs010() {
    final XQuery query = new XQuery(
      "fn:insert-before( (\"a\", \"b\", \"c\"),2, xs:float(\"-0\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("\"a\", 0, \"b\", \"c\"")
    );
  }

  /**
   *  arg1: Sequence of string, arg2:integer, arg3: float .
   */
  @org.junit.Test
  public void fnInsertBeforeMixArgs011() {
    final XQuery query = new XQuery(
      "fn:insert-before( (\"a\", \"b\", \"c\"),2, xs:float(\"-INF\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("\"a\", xs:float('-INF'), \"b\", \"c\"")
    );
  }

  /**
   *  arg1: Sequence of string, arg2:integer, arg3: double .
   */
  @org.junit.Test
  public void fnInsertBeforeMixArgs012() {
    final XQuery query = new XQuery(
      "fn:insert-before( (\"a\", \"b\", \"c\"),2, xs:double(\"NaN\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("\"a\", xs:double('NaN'),  \"b\", \"c\"")
    );
  }

  /**
   *  arg1: Sequence of string, arg2:integer, arg3: double .
   */
  @org.junit.Test
  public void fnInsertBeforeMixArgs013() {
    final XQuery query = new XQuery(
      "fn:insert-before( (\"a\", \"b\", \"c\"),2, xs:double(\"INF\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("\"a\", xs:double('INF'), \"b\", \"c\"")
    );
  }

  /**
   *  arg1: Sequence of string, arg2:integer, arg3: boolean .
   */
  @org.junit.Test
  public void fnInsertBeforeMixArgs014() {
    final XQuery query = new XQuery(
      "fn:insert-before( (\"a\", \"b\", \"c\"),2, xs:boolean(\"1\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("\"a\", true(), \"b\", \"c\"")
    );
  }

  /**
   *  arg1: Sequence of string, arg2:integer, arg3: boolean .
   */
  @org.junit.Test
  public void fnInsertBeforeMixArgs015() {
    final XQuery query = new XQuery(
      "fn:insert-before( (\"a\", \"b\", \"c\"),2, xs:boolean(\"0\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("\"a\", false(), \"b\", \"c\"")
    );
  }

  /**
   *  arg1: Sequence of string, arg2:integer, arg3: boolean .
   */
  @org.junit.Test
  public void fnInsertBeforeMixArgs016() {
    final XQuery query = new XQuery(
      "fn:insert-before( (\"a\", \"b\", \"c\"),2, xs:boolean(\"true\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("\"a\", true(), \"b\", \"c\"")
    );
  }

  /**
   *  arg1: Sequence of string, arg2:integer, arg3: boolean .
   */
  @org.junit.Test
  public void fnInsertBeforeMixArgs017() {
    final XQuery query = new XQuery(
      "fn:insert-before( (\"a\", \"b\", \"c\"),2, xs:boolean(\"false\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("\"a\", false(),  \"b\",  \"c\"")
    );
  }

  /**
   *  arg1: Sequence of string, arg2:integer, arg3: date .
   */
  @org.junit.Test
  public void fnInsertBeforeMixArgs018() {
    final XQuery query = new XQuery(
      "fn:insert-before( (\"a\", \"b\", \"c\"),4, xs:date(\"1993-03-31\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("\"a\", \"b\", \"c\", xs:date('1993-03-31')")
    );
  }

  /**
   *  arg1: Sequence of string, arg2:integer, arg3: dateTime .
   */
  @org.junit.Test
  public void fnInsertBeforeMixArgs019() {
    final XQuery query = new XQuery(
      "fn:insert-before( (\"a\", \"b\", \"c\"),4, xs:dateTime(\"1972-12-31T00:00:00\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("\"a\", \"b\", \"c\", xs:dateTime('1972-12-31T00:00:00')")
    );
  }

  /**
   *  arg1: Sequence of string, arg2:integer, arg3: time .
   */
  @org.junit.Test
  public void fnInsertBeforeMixArgs020() {
    final XQuery query = new XQuery(
      "fn:insert-before( (\"a\", \"b\", \"c\"),4, xs:time(\"12:30:00\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("\"a\", \"b\", \"c\", xs:time('12:30:00')")
    );
  }
}
