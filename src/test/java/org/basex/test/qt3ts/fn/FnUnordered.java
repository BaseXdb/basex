package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the unordered() function. (Note: many of these tests will only work if unordered() is implemented as a no-op. The tests are therefore incorrect. I'm leaving them as such until someone cares to challenge the results - Michael Kay 2011-06-30 .
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnUnordered extends QT3TestSet {

  /**
   *  A test whose essence is: `unordered()`. .
   */
  @org.junit.Test
  public void kSeqUnorderedFunc1() {
    final XQuery query = new XQuery(
      "unordered()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `unordered(1, 2)`. .
   */
  @org.junit.Test
  public void kSeqUnorderedFunc2() {
    final XQuery query = new XQuery(
      "unordered(1, 2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `unordered(1) eq 1`. .
   */
  @org.junit.Test
  public void kSeqUnorderedFunc3() {
    final XQuery query = new XQuery(
      "unordered(1) eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(unordered((1, 2, 3))) eq 3`. .
   */
  @org.junit.Test
  public void kSeqUnorderedFunc4() {
    final XQuery query = new XQuery(
      "count(unordered((1, 2, 3))) eq 3",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(unordered((1, 2, current-time()))) eq 3`. .
   */
  @org.junit.Test
  public void kSeqUnorderedFunc5() {
    final XQuery query = new XQuery(
      "count(unordered((1, 2, current-time()))) eq 3",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty(unordered(()))`. .
   */
  @org.junit.Test
  public void kSeqUnorderedFunc6() {
    final XQuery query = new XQuery(
      "empty(unordered(()))",
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
   *  A test whose essence is: `deep-equal((1, 2, 3), unordered((1, 2, 3)))`. .
   */
  @org.junit.Test
  public void kSeqUnorderedFunc7() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 3), unordered((1, 2, 3)))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `unordered(error())`. .
   */
  @org.junit.Test
  public void kSeqUnorderedFunc8() {
    final XQuery query = new XQuery(
      "unordered(error())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOER0000")
    );
  }

  /**
   *  arg: sequence of string .
   */
  @org.junit.Test
  public void fnUnorderedMixArgs001() {
    final XQuery query = new XQuery(
      "fn:unordered( (\"c\",1, \"xzy\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertPermutation("\"c\", 1, \"xzy\"")
    );
  }

  /**
   *  arg: sequence of string .
   */
  @org.junit.Test
  public void fnUnorderedMixArgs002() {
    final XQuery query = new XQuery(
      "fn:unordered( (\"c\", \"b\", \"a\") )",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertPermutation("\"c\",  \"b\", \"a\"")
    );
  }

  /**
   *  arg: sequence of string .
   */
  @org.junit.Test
  public void fnUnorderedMixArgs003() {
    final XQuery query = new XQuery(
      "fn:unordered ( (\"a\", \"b\", \"c\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertPermutation("\"a\", \"b\", \"c\"")
    );
  }

  /**
   *  arg: sequence of string .
   */
  @org.junit.Test
  public void fnUnorderedMixArgs004() {
    final XQuery query = new XQuery(
      "fn:unordered ( (\"a\", xs:string(\"\"), \"b\", \"c\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertPermutation("\"a\", \"\",  \"b\", \"c\"")
    );
  }

  /**
   *  arg: sequence of string & anyURI .
   */
  @org.junit.Test
  public void fnUnorderedMixArgs005() {
    final XQuery query = new XQuery(
      "fn:unordered ( (\"a\", xs:anyURI(\"www.example.com\"), \"b\", \"c\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertPermutation("\"a\", \"www.example.com\", \"b\", \"c\"")
    );
  }

  /**
   *  arg: sequence of string .
   */
  @org.junit.Test
  public void fnUnorderedMixArgs006() {
    final XQuery query = new XQuery(
      "fn:unordered ( (\"a\", (), (), \"b\", \"c\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertPermutation("\"a\", \"b\", \"c\"")
    );
  }

  /**
   *  arg: sequence of string & integer .
   */
  @org.junit.Test
  public void fnUnorderedMixArgs007() {
    final XQuery query = new XQuery(
      "fn:unordered ( (\"a\", xs:integer(\"100\"), xs:integer(\"-100\"), \"b\", \"c\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertPermutation("\"a\", 100, -100, \"b\", \"c\"")
    );
  }

  /**
   *  arg: sequence of string , decimal & integer .
   */
  @org.junit.Test
  public void fnUnorderedMixArgs008() {
    final XQuery query = new XQuery(
      "fn:unordered ( (\"a\", xs:decimal(\"-1.000000000001\"), xs:integer(\"-100\"), \"b\", \"c\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertPermutation("\"a\", -1.000000000001, -100, \"b\", \"c\"")
    );
  }

  /**
   *  arg: sequence of string & float .
   */
  @org.junit.Test
  public void fnUnorderedMixArgs009() {
    final XQuery query = new XQuery(
      "fn:unordered ( (\"a\", xs:float(\"INF\"), \"b\", \"c\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertPermutation("\"a\", xs:float(\"INF\"), \"b\", \"c\"")
    );
  }

  /**
   *  arg: sequence of string & float .
   */
  @org.junit.Test
  public void fnUnorderedMixArgs010() {
    final XQuery query = new XQuery(
      "fn:unordered ( (\"a\", xs:float(\"-INF\"), \"b\", \"c\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertPermutation("\"a\", xs:float('-INF'), \"b\", \"c\"")
    );
  }

  /**
   *  arg: sequence of string & float .
   */
  @org.junit.Test
  public void fnUnorderedMixArgs011() {
    final XQuery query = new XQuery(
      "fn:unordered ( (\"a\", xs:float(\"NaN\"), \"b\", \"c\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertPermutation("\"a\", xs:float('NaN'), \"b\", \"c\"")
    );
  }

  /**
   *  arg: sequence of string & float .
   */
  @org.junit.Test
  public void fnUnorderedMixArgs012() {
    final XQuery query = new XQuery(
      "fn:unordered ( (\"a\", xs:float(\"1.01\"), \"b\", \"c\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertPermutation("\"a\", 1.01, \"b\", \"c\"")
    );
  }

  /**
   *  arg: sequence of string & double .
   */
  @org.junit.Test
  public void fnUnorderedMixArgs013() {
    final XQuery query = new XQuery(
      "fn:unordered ( (\"a\", xs:double(\"NaN\"), \"b\", \"c\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertPermutation("\"a\", xs:double('NaN'), \"b\", \"c\"")
    );
  }

  /**
   *  arg: sequence of string & double .
   */
  @org.junit.Test
  public void fnUnorderedMixArgs014() {
    final XQuery query = new XQuery(
      "fn:unordered ( (\"a\", xs:double(\"1.01\"), \"b\", \"c\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertPermutation("\"a\", 1.01, \"b\", \"c\"")
    );
  }

  /**
   *  arg: sequence of string & double .
   */
  @org.junit.Test
  public void fnUnorderedMixArgs015() {
    final XQuery query = new XQuery(
      "fn:unordered ( (\"a\", xs:double(\"-INF\"), \"b\", \"c\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertPermutation("\"a\", xs:double('-INF'), \"b\", \"c\"")
    );
  }

  /**
   *  arg: sequence of string & double .
   */
  @org.junit.Test
  public void fnUnorderedMixArgs016() {
    final XQuery query = new XQuery(
      "fn:unordered ( (\"a\", xs:double(\"INF\"), \"b\", \"c\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertPermutation("\"a\", xs:double(\"INF\"), \"b\", \"c\"")
    );
  }

  /**
   *  arg: sequence of string & boolean .
   */
  @org.junit.Test
  public void fnUnorderedMixArgs017() {
    final XQuery query = new XQuery(
      "fn:unordered ( (\"a\", xs:boolean(\"1\"), \"b\", \"c\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertPermutation("\"a\", true(), \"b\", \"c\"")
    );
  }

  /**
   *  arg: sequence of string & boolean .
   */
  @org.junit.Test
  public void fnUnorderedMixArgs018() {
    final XQuery query = new XQuery(
      "fn:unordered ( (\"a\", xs:boolean(\"0\"), \"b\", \"c\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertPermutation("\"a\", false(), \"b\", \"c\"")
    );
  }

  /**
   *  arg: sequence of string & boolean .
   */
  @org.junit.Test
  public void fnUnorderedMixArgs019() {
    final XQuery query = new XQuery(
      "fn:unordered ( (\"a\", xs:boolean(\"true\"), \"b\", \"c\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertPermutation("\"a\", true(), \"b\", \"c\"")
    );
  }

  /**
   *  arg: sequence of string & boolean .
   */
  @org.junit.Test
  public void fnUnorderedMixArgs020() {
    final XQuery query = new XQuery(
      "fn:unordered ( (\"a\", xs:boolean(\"false\"), \"b\", \"c\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertPermutation("\"a\", false(), \"b\", \"c\"")
    );
  }

  /**
   *  arg: sequence of string & date .
   */
  @org.junit.Test
  public void fnUnorderedMixArgs021() {
    final XQuery query = new XQuery(
      "fn:unordered ( (\"a\", xs:date(\"1993-03-31\"), \"b\", \"c\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertPermutation("\"a\", xs:date('1993-03-31'), \"b\", \"c\"")
    );
  }

  /**
   *  arg: sequence of string & dateTime .
   */
  @org.junit.Test
  public void fnUnorderedMixArgs022() {
    final XQuery query = new XQuery(
      "fn:unordered ( (\"a\", xs:dateTime(\"1972-12-31T00:00:00\"), \"b\", \"c\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertPermutation("\"a\", xs:dateTime(\"1972-12-31T00:00:00\"), \"b\", \"c\"")
    );
  }

  /**
   *  arg: sequence of string & time .
   */
  @org.junit.Test
  public void fnUnorderedMixArgs023() {
    final XQuery query = new XQuery(
      "fn:unordered ( (\"a\", xs:time(\"12:30:00\"), \"b\", \"c\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertPermutation("\"a\", xs:time('12:30:00'), \"b\", \"c\"")
    );
  }
}
