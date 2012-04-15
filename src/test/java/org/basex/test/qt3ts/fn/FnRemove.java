package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the remove() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnRemove extends QT3TestSet {

  /**
   *  A test whose essence is: `remove()`. .
   */
  @org.junit.Test
  public void kSeqRemoveFunc1() {
    final XQuery query = new XQuery(
      "remove()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `count(remove((1, "two", 3), 2)) eq 2`. .
   */
  @org.junit.Test
  public void kSeqRemoveFunc10() {
    final XQuery query = new XQuery(
      "count(remove((1, \"two\", 3), 2)) eq 2",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(remove((1, 2, "three"), 3)) eq 2`. .
   */
  @org.junit.Test
  public void kSeqRemoveFunc11() {
    final XQuery query = new XQuery(
      "count(remove((1, 2, \"three\"), 3)) eq 2",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `remove((3.1, "four"), 1)`. .
   */
  @org.junit.Test
  public void kSeqRemoveFunc12() {
    final XQuery query = new XQuery(
      "remove((3.1, \"four\"), 1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("\"four\"")
    );
  }

  /**
   *  A test whose essence is: `remove(error(), 1)`. .
   */
  @org.junit.Test
  public void kSeqRemoveFunc13() {
    final XQuery query = new XQuery(
      "remove(error(), 1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        error("FOER0000")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Combine fn:remove() with operator 'eq'. .
   */
  @org.junit.Test
  public void kSeqRemoveFunc14() {
    final XQuery query = new XQuery(
      "remove((5, 1e0), 2) eq 5",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertBoolean(true)
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  Combine fn:remove() with operator 'eq'. .
   */
  @org.junit.Test
  public void kSeqRemoveFunc15() {
    final XQuery query = new XQuery(
      "5 eq remove((5, 1e0), 2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertBoolean(true)
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  Combine remove(), with a predicate and the 'eq' operator. .
   */
  @org.junit.Test
  public void kSeqRemoveFunc16() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2), (1, 2)[remove((true(), \"a string\"), 2)]) eq 0",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  Combine fn:remove() with operator 'eq'. .
   */
  @org.junit.Test
  public void kSeqRemoveFunc17() {
    final XQuery query = new XQuery(
      "remove((4, xs:untypedAtomic(\"4\")), 1) eq 4",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  Combine fn:remove() with operator 'eq'. .
   */
  @org.junit.Test
  public void kSeqRemoveFunc18() {
    final XQuery query = new XQuery(
      "4 eq remove((4, xs:untypedAtomic(\"1\")), 1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  An expression involving the eq operator that trigger certain optimization paths in some implementations. .
   */
  @org.junit.Test
  public void kSeqRemoveFunc19() {
    final XQuery query = new XQuery(
      "count(remove(current-time(), 1)) eq 0",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `remove(1, 2, "wrong param")`. .
   */
  @org.junit.Test
  public void kSeqRemoveFunc2() {
    final XQuery query = new XQuery(
      "remove(1, 2, \"wrong param\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  An expression involving the eq operator that trigger certain optimization paths in some implementations. .
   */
  @org.junit.Test
  public void kSeqRemoveFunc20() {
    final XQuery query = new XQuery(
      "empty(remove(current-time(), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Apply a predicate to the result of fn:remove(). .
   */
  @org.junit.Test
  public void kSeqRemoveFunc21() {
    final XQuery query = new XQuery(
      "remove((1, 2, 3, current-time()), 4)[last()]",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("3")
    );
  }

  /**
   *  Apply a predicate to the result of fn:remove(). .
   */
  @org.junit.Test
  public void kSeqRemoveFunc22() {
    final XQuery query = new XQuery(
      "remove((1, 2, 3, current-time()), 4)[last() - 1]",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("2")
    );
  }

  /**
   *  Apply a predicate to the result of fn:remove(). .
   */
  @org.junit.Test
  public void kSeqRemoveFunc23() {
    final XQuery query = new XQuery(
      "remove((1, 2, 3, current-time()), 9)[last() - 1]",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("3")
    );
  }

  /**
   *  Apply a predicate to the result of fn:remove(). .
   */
  @org.junit.Test
  public void kSeqRemoveFunc24() {
    final XQuery query = new XQuery(
      "empty(remove((1, 2, 3, current-time()), 9)[last() - 10])",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(remove((1, 2, 3), 0)) eq 3`. .
   */
  @org.junit.Test
  public void kSeqRemoveFunc3() {
    final XQuery query = new XQuery(
      "count(remove((1, 2, 3), 0)) eq 3",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(remove((1, 2, 3), -4)) eq 3`. .
   */
  @org.junit.Test
  public void kSeqRemoveFunc4() {
    final XQuery query = new XQuery(
      "count(remove((1, 2, 3), -4)) eq 3",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty(remove((), 4))`. .
   */
  @org.junit.Test
  public void kSeqRemoveFunc5() {
    final XQuery query = new XQuery(
      "empty(remove((), 4))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertBoolean(true)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  A test whose essence is: `remove(("one", 2, 3), 1) instance of xs:integer+`. .
   */
  @org.junit.Test
  public void kSeqRemoveFunc6() {
    final XQuery query = new XQuery(
      "remove((\"one\", 2, 3), 1) instance of xs:integer+",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `remove((1, "two", 3), 2) instance of xs:integer+`. .
   */
  @org.junit.Test
  public void kSeqRemoveFunc7() {
    final XQuery query = new XQuery(
      "remove((1, \"two\", 3), 2) instance of xs:integer+",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `remove((1, 2, "three"), 3) instance of xs:integer+`. .
   */
  @org.junit.Test
  public void kSeqRemoveFunc8() {
    final XQuery query = new XQuery(
      "remove((1, 2, \"three\"), 3) instance of xs:integer+",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(remove(("one", 2, 3), 1)) eq 2`. .
   */
  @org.junit.Test
  public void kSeqRemoveFunc9() {
    final XQuery query = new XQuery(
      "count(remove((\"one\", 2, 3), 1)) eq 2",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  arg1: sequence of string, arg2: integer .
   */
  @org.junit.Test
  public void fnRemoveMixArgs001() {
    final XQuery query = new XQuery(
      "fn:remove ( (\"a\", \"b\", \"c\"), 1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("\"b\", \"c\"")
    );
  }

  /**
   *  arg1: sequence of string, arg2: integer .
   */
  @org.junit.Test
  public void fnRemoveMixArgs002() {
    final XQuery query = new XQuery(
      "fn:remove ( (\"a\", \"b\", \"c\"), 0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("\"a\", \"b\", \"c\"")
    );
  }

  /**
   *  arg1: sequence of string, arg2: integer .
   */
  @org.junit.Test
  public void fnRemoveMixArgs003() {
    final XQuery query = new XQuery(
      "fn:remove ( (\"a\", \"b\", \"c\", true()), 3)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("\"a\", \"b\", true()")
    );
  }

  /**
   *  arg1: sequence of string, arg2: integer .
   */
  @org.junit.Test
  public void fnRemoveMixArgs004() {
    final XQuery query = new XQuery(
      "fn:remove ( (xs:string(\"xyz\"), (), (), \"a\" , \"b\"), 2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("\"xyz\", \"b\"")
    );
  }

  /**
   *  arg1: sequence of string,anyURI,arg2: integer .
   */
  @org.junit.Test
  public void fnRemoveMixArgs005() {
    final XQuery query = new XQuery(
      "fn:remove( (xs:anyURI(\"www.example.com\"), \"a\", (\"\"), \"b\"), 2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("\"www.example.com\", \"\", \"b\"")
    );
  }

  /**
   *  arg1: sequence of string,anyURI,arg2: integer .
   */
  @org.junit.Test
  public void fnRemoveMixArgs006() {
    final XQuery query = new XQuery(
      "fn:remove( (xs:anyURI(\"www.example.com\"), \"a\", (\"\"), \"b\"), 10)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("\"www.example.com\", \"a\", \"\",  \"b\"")
    );
  }

  /**
   *  arg1: sequence of string,anyURI,arg2: integer .
   */
  @org.junit.Test
  public void fnRemoveMixArgs007() {
    final XQuery query = new XQuery(
      "fn:remove( (xs:anyURI(\"www.example.com\"), \"a\", (\"\"), \"b\"), -20)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("\"www.example.com\", \"a\", \"\",  \"b\"")
    );
  }

  /**
   *  arg1:sequence of string,integer,arg2: integer .
   */
  @org.junit.Test
  public void fnRemoveMixArgs008() {
    final XQuery query = new XQuery(
      "fn:remove( (xs:integer(\"100\"), xs:string(\"abc\")), 1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("\"abc\"")
    );
  }

  /**
   *  arg1: sequence of decimal, integer, anyURI arg2: integer .
   */
  @org.junit.Test
  public void fnRemoveMixArgs009() {
    final XQuery query = new XQuery(
      "fn:remove( (xs:decimal(\"1.01\"), xs:integer(\"12\"), xs:anyURI(\"www.example.com\")),3)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("1.01, 12")
    );
  }

  /**
   *  arg1: sequence of string,float ,arg2: integer .
   */
  @org.junit.Test
  public void fnRemoveMixArgs010() {
    final XQuery query = new XQuery(
      "fn:remove( (xs:float(\"1.01\"), xs:string(\"a\")), 2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1.01")
    );
  }

  /**
   *  arg1: sequence of float,integer,arg2: integer .
   */
  @org.junit.Test
  public void fnRemoveMixArgs011() {
    final XQuery query = new XQuery(
      "fn:remove( (xs:float(\"NaN\"), 100, (), 2), 2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("xs:float('NaN'), 2")
    );
  }

  /**
   *  arg1: sequence of string,float, decimal arg2: integer .
   */
  @org.junit.Test
  public void fnRemoveMixArgs012() {
    final XQuery query = new XQuery(
      "fn:remove( (xs:float(\"-INF\"), xs:decimal(\"2.34\"), \"abc\"), 2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("xs:float(\"-INF\"), \"abc\"")
    );
  }

  /**
   *  arg1: sequence of double,float,boolean ,arg2: integer .
   */
  @org.junit.Test
  public void fnRemoveMixArgs013() {
    final XQuery query = new XQuery(
      "fn:remove( (xs:double(\"1.34\"), xs:float(\"INF\"), true()), 1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("xs:float(\"INF\"), true()")
    );
  }

  /**
   *  arg1: sequence of double, integer ,arg2: integer .
   */
  @org.junit.Test
  public void fnRemoveMixArgs014() {
    final XQuery query = new XQuery(
      "fn:remove( (xs:double(\"INF\"), 2, 3), 2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("xs:double(\"INF\"), 3")
    );
  }

  /**
   *  arg1: sequence of string,double ,arg2: integer .
   */
  @org.junit.Test
  public void fnRemoveMixArgs015() {
    final XQuery query = new XQuery(
      "fn:remove( (xs:double(\"NaN\"), \"a\", \"b\"), 3)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("xs:double(\"NaN\"), \"a\"")
    );
  }

  /**
   *  arg1: sequence of string,boolean, double ,arg2: integer .
   */
  @org.junit.Test
  public void fnRemoveMixArgs016() {
    final XQuery query = new XQuery(
      "fn:remove( (xs:boolean(\"1\"), xs:double(\"-INF\"), \"s\"), 3)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("true(), xs:double(\"-INF\")")
    );
  }

  /**
   *  arg1: sequence of boolean ,arg2: integer .
   */
  @org.junit.Test
  public void fnRemoveMixArgs017() {
    final XQuery query = new XQuery(
      "fn:remove( (xs:boolean(\"0\")), 2 )",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  arg1: sequence of string,boolean, date ,arg2: integer .
   */
  @org.junit.Test
  public void fnRemoveMixArgs018() {
    final XQuery query = new XQuery(
      "fn:remove( (xs:boolean(\"true\"), xs:date(\"1993-03-31\"), 4, \"a\"),3)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("true(), xs:date('1993-03-31'), \"a\"")
    );
  }

  /**
   *  arg1: sequence of string,dateTime,boolean ,arg2: integer .
   */
  @org.junit.Test
  public void fnRemoveMixArgs019() {
    final XQuery query = new XQuery(
      "fn:remove( (xs:dateTime(\"1972-12-31T00:00:00\"), xs:boolean(\"false\"), (), (\" \")) ,3)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("xs:dateTime(\"1972-12-31T00:00:00\"), false()")
    );
  }

  /**
   *  arg1: sequence of time,decimal, integer ,arg2: integer .
   */
  @org.junit.Test
  public void fnRemoveMixArgs020() {
    final XQuery query = new XQuery(
      "fn:remove( (xs:time(\"12:30:00\"), xs:decimal(\"2.000003\"), 2), 2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("xs:time(\"12:30:00\"), 2")
    );
  }
}
