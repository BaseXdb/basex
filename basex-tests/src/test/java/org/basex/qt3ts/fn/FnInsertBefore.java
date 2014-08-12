package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the insert-before() function.
 *
 * @author BaseX Team 2005-14, BSD License
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
   *  A test whose essence is: `insert-before((), 30, 7) eq 7`. .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc10() {
    final XQuery query = new XQuery(
      "insert-before((), 30, 7) eq 7",
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
   *  A test whose essence is: `count(insert-before((1, 2, 3, 4), 30, ())) eq 4`. .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc11() {
    final XQuery query = new XQuery(
      "count(insert-before((1, 2, 3, 4), 30, ())) eq 4",
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
   *  A test whose essence is: `insert-before(9, 30, ()) eq 9`. .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc12() {
    final XQuery query = new XQuery(
      "insert-before(9, 30, ()) eq 9",
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
   *  A test whose essence is: `count(insert-before((1, 2, 3, 4), 1, ())) eq 4`. .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc13() {
    final XQuery query = new XQuery(
      "count(insert-before((1, 2, 3, 4), 1, ())) eq 4",
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
   *  A test whose essence is: `count(insert-before((1, 2, 3), 30, (4, 5, 6))) eq 6`. .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc14() {
    final XQuery query = new XQuery(
      "count(insert-before((1, 2, 3), 30, (4, 5, 6))) eq 6",
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
   *  A test whose essence is: `count(insert-before((), 30, (1, 2, 3))) eq 3`. .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc15() {
    final XQuery query = new XQuery(
      "count(insert-before((), 30, (1, 2, 3))) eq 3",
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
   *  A test whose essence is: `count(insert-before((error(), 1), 1, (1, "two", 3))) > 1`. .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc16() {
    final XQuery query = new XQuery(
      "count(insert-before((error(), 1), 1, (1, \"two\", 3))) > 1",
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
   *  Apply a predicate to the result of fn:insert-before(). .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc18() {
    final XQuery query = new XQuery(
      "(insert-before((1, current-time(), 3), 10, (4, 5, 6))[last()] treat as xs:integer) eq 6",
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
   *  Apply a predicate to the result of fn:insert-before(). .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc19() {
    final XQuery query = new XQuery(
      "(insert-before((1, current-time(), 3), 10, (4, 5, 6))[last() - 3] treat as xs:integer) eq 3",
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
   *  A test whose essence is: `insert-before("wrong params", 2)`. .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc2() {
    final XQuery query = new XQuery(
      "insert-before(\"wrong params\", 2)",
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
   *  Apply a predicate to the result of fn:insert-before(). .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc20() {
    final XQuery query = new XQuery(
      "(insert-before((1, current-time(), 3), 10, ())[last()] treat as xs:integer) eq 3",
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
   *  Apply a predicate to the result of fn:insert-before(). .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc21() {
    final XQuery query = new XQuery(
      "empty(insert-before((1, current-time(), 3), 0, (4, 5, 6))[last() - 10])",
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
   *  A test whose essence is: `insert-before("wrong params", 2, 3, 4)`. .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc3() {
    final XQuery query = new XQuery(
      "insert-before(\"wrong params\", 2, 3, 4)",
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
   *  The second argument to fn:insert-before cannot be the empty sequence. .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc4() {
    final XQuery query = new XQuery(
      "insert-before((), (), \"a string\")",
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
   *  A test whose essence is: `insert-before((), -31, "a string") eq "a string"`. .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc5() {
    final XQuery query = new XQuery(
      "insert-before((), -31, \"a string\") eq \"a string\"",
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
   *  A test whose essence is: `count(insert-before((1, 2, 3), 1, (4, 5, 6))) eq 6`. .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc6() {
    final XQuery query = new XQuery(
      "count(insert-before((1, 2, 3), 1, (4, 5, 6))) eq 6",
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
   *  A test whose essence is: `insert-before((), 1, 3) eq 3`. .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc7() {
    final XQuery query = new XQuery(
      "insert-before((), 1, 3) eq 3",
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
   *  A test whose essence is: `insert-before((), 1, "a string") eq "a string"`. .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc8() {
    final XQuery query = new XQuery(
      "insert-before((), 1, \"a string\") eq \"a string\"",
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
   *  A test whose essence is: `count(insert-before((), 1, (1, 2, 3))) eq 3`. .
   */
  @org.junit.Test
  public void kSeqInsertBeforeFunc9() {
    final XQuery query = new XQuery(
      "count(insert-before((), 1, (1, 2, 3))) eq 3",
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
   *  tests insert-before at known positions .
   */
  @org.junit.Test
  public void cbclFnInsertBefore001() {
    final XQuery query = new XQuery(
      "insert-before(1 to 10,5,20 to 30)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2 3 4 20 21 22 23 24 25 26 27 28 29 30 5 6 7 8 9 10")
    );
  }

  /**
   *  Tests insert-before for known positions .
   */
  @org.junit.Test
  public void cbclFnInsertBefore002() {
    final XQuery query = new XQuery(
      "\n" +
      "        insert-before((1 to 10,(20 to 30)[. mod 2 = 0],30 to 40),12,\"blah\")\n" +
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
      assertStringValue(false, "1 2 3 4 5 6 7 8 9 10 20 blah 22 24 26 28 30 30 31 32 33 34 35 36 37 38 39 40")
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"a\", \"b\", \"c\", xs:time('12:30:00')")
    );
  }
}
