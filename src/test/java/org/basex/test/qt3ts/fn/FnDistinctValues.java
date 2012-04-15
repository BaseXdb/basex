package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the distinct-values() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnDistinctValues extends QT3TestSet {

  /**
   *  A test whose essence is: `distinct-values()`. .
   */
  @org.junit.Test
  public void kSeqDistinctValuesFunc1() {
    final XQuery query = new XQuery(
      "distinct-values()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `deep-equal(distinct-values((1, 2.0, 3, 2)), (1, 2.0, 3))`. .
   */
  @org.junit.Test
  public void kSeqDistinctValuesFunc10() {
    final XQuery query = new XQuery(
      "deep-equal(distinct-values((1, 2.0, 3, 2)), (1, 2.0, 3))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `exists(distinct-values((1, 2, 3, 1)))`. .
   */
  @org.junit.Test
  public void kSeqDistinctValuesFunc11() {
    final XQuery query = new XQuery(
      "exists(distinct-values((1, 2, 3, 1)))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(empty(distinct-values((1, 1))))`. .
   */
  @org.junit.Test
  public void kSeqDistinctValuesFunc12() {
    final XQuery query = new XQuery(
      "not(empty(distinct-values((1, 1))))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(distinct-values((1, 2, 2, current-time()))) eq 3`. .
   */
  @org.junit.Test
  public void kSeqDistinctValuesFunc13() {
    final XQuery query = new XQuery(
      "count(distinct-values((1, 2, 2, current-time()))) eq 3",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(distinct-values(())) eq 0`. .
   */
  @org.junit.Test
  public void kSeqDistinctValuesFunc14() {
    final XQuery query = new XQuery(
      "count(distinct-values(())) eq 0",
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
   *  fn:distinct-values() applied on an argument of cardinality exactly-one. .
   */
  @org.junit.Test
  public void kSeqDistinctValuesFunc15() {
    final XQuery query = new XQuery(
      "count(distinct-values(current-time())) eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Invoke fn:distinct-values() with a collation argument, although the function does not perform string comparison. For that reason, output is valid as well. .
   */
  @org.junit.Test
  public void kSeqDistinctValuesFunc2() {
    final XQuery query = new XQuery(
      "deep-equal(distinct-values((1, 2, 3), \"http://www.example.com/COLLATION/NOT/SUPPORTED\"), (1, 2, 3))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertBoolean(true)
      ||
        error("FOCH0002")
      )
    );
  }

  /**
   *  A test whose essence is: `distinct-values("a string", "http://www.w3.org/2005/xpath-functions/collation/codepoint", "wrong param")`. .
   */
  @org.junit.Test
  public void kSeqDistinctValuesFunc3() {
    final XQuery query = new XQuery(
      "distinct-values(\"a string\", \"http://www.w3.org/2005/xpath-functions/collation/codepoint\", \"wrong param\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `distinct-values("a string", "http://www.w3.org/2005/xpath-functions/collation/codepoint") eq "a string"`. .
   */
  @org.junit.Test
  public void kSeqDistinctValuesFunc4() {
    final XQuery query = new XQuery(
      "distinct-values(\"a string\", \"http://www.w3.org/2005/xpath-functions/collation/codepoint\") eq \"a string\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty(distinct-values(()))`. .
   */
  @org.junit.Test
  public void kSeqDistinctValuesFunc5() {
    final XQuery query = new XQuery(
      "empty(distinct-values(()))",
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
   *  A test whose essence is: `deep-equal(distinct-values( ("1", 1, 2, 1, 1, 3, 1, 1, 3, xs:anyURI("example.com/"), xs:anyURI("example.com/"))), ("1", 1, 2, 3, xs:anyURI("example.com/")))`. .
   */
  @org.junit.Test
  public void kSeqDistinctValuesFunc6() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:canon($arg) {\n" +
      "            for $i in \n" +
      "                for $s in $arg \n" +
      "                return string($s) \n" +
      "            order by $i \n" +
      "            return $i \n" +
      "        }; \n" +
      "        deep-equal(\n" +
      "            local:canon(\n" +
      "                distinct-values((\"1\", 1, 2, 1, 1, 3, 1, 1, 3, xs:anyURI(\"example.com/\"), xs:anyURI(\"example.com/\")))), \n" +
      "            local:canon((\"1\", 1, 2, 3, xs:anyURI(\"example.com/\"))))\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `distinct-values((1, 1))`. .
   */
  @org.junit.Test
  public void kSeqDistinctValuesFunc7() {
    final XQuery query = new XQuery(
      "distinct-values((1, 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("1")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  A test whose essence is: `distinct-values((-3, -3))`. .
   */
  @org.junit.Test
  public void kSeqDistinctValuesFunc8() {
    final XQuery query = new XQuery(
      "distinct-values((-3, -3))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("-3")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  A test whose essence is: `count(distinct-values((1, 2.0, 3, 2))) eq 3`. .
   */
  @org.junit.Test
  public void kSeqDistinctValuesFunc9() {
    final XQuery query = new XQuery(
      "count(distinct-values((1, 2.0, 3, 2))) eq 3",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Invoke fn:distinct-values() with an invalid collation. .
   */
  @org.junit.Test
  public void k2SeqDistinctValuesFunc1() {
    final XQuery query = new XQuery(
      "fn:distinct-values((\"1\", \"2\", \"3\"), \"http://www.example.com/COLLATION/NOT/SUPPORTED\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOCH0002")
    );
  }

  /**
   *  Test Bugzilla #5183, [FO] Effect of type promotion in fn:distinct-values .
   */
  @org.junit.Test
  public void fnDistinctValues1() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $input := (xs:float('1.0'), xs:decimal('1.0000000000100000000001'), \n" +
      "                       xs:double( '1.00000000001'), xs:float('2.0'), \n" +
      "                       xs:decimal('2.0000000000100000000001'), xs:double( '2.00000000001')), \n" +
      "            $distinct := distinct-values($input) \n" +
      "        return ( (every $n in $input satisfies $n = $distinct) and \n" +
      "        (every $bool in (for $d1 at $p in $distinct, $d2 in $distinct [position() > $p] return $d1 eq $d2) satisfies not($bool)) )",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * distinct-values() on a somewhat larger set of values.
   */
  @org.junit.Test
  public void fnDistinctValues2() {
    final XQuery query = new XQuery(
      "distinct-values((1 to 300, 100 to 400, 29, 145, 20 to 50, for $x in (30 to 40) return xs:string($x), \"foo\", \"bar\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertPermutation("1 to 400, \"30\", \"31\", \"32\", \"33\", \"34\", \"35\", \"36\", \"37\", \"38\", \"39\", \"40\", \"foo\", \"bar\"")
    );
  }

  /**
   *  arg: sequence of integer & decimal .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs001() {
    final XQuery query = new XQuery(
      "fn:distinct-values((1, 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "2 1")
      ||
        assertStringValue(false, "1 2")
      )
    );
  }

  /**
   *  arg: sequence of integer .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs002() {
    final XQuery query = new XQuery(
      "fn:distinct-values(( 1, (1), ((1)) ))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  arg: sequence of integer & double .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs003() {
    final XQuery query = new XQuery(
      "fn:distinct-values(( 1, 1.0e0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertCount(1)
      &&
        assertEq("1")
      &&
        (
          assertType("xs:integer")
        ||
          assertType("xs:double")
        )
      )
    );
  }

  /**
   *  arg: sequence of integer .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs004() {
    final XQuery query = new XQuery(
      "fn:distinct-values(( xs:integer(1), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  arg: sequence of integer & double .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs005() {
    final XQuery query = new XQuery(
      "fn:distinct-values(( 0e0, -0, 0, 1 ))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertPermutation("1, 0")
    );
  }

  /**
   *  arg: sequence of string .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs006() {
    final XQuery query = new XQuery(
      "fn:distinct-values(( \"cat\", 'CAT' ))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertPermutation("\"cat\", \"CAT\"")
    );
  }

  /**
   *  arg: sequence of string .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs007() {
    final XQuery query = new XQuery(
      "fn:distinct-values(( xs:string(\"hello\"), \"hello\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "hello")
    );
  }

  /**
   *  arg: sequence of string .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs008() {
    final XQuery query = new XQuery(
      "fn:distinct-values(( xs:string(\"\"), \"\", ''))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "")
    );
  }

  /**
   *  arg: sequence of integer,decimal,boolean,string .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs009() {
    final XQuery query = new XQuery(
      "fn:distinct-values((1, true(), true(), ()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertPermutation("1, true()")
    );
  }

  /**
   *  arg: sequence of decimal .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs010() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:decimal('1.2'), xs:decimal('1.2000000000000001')))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertPermutation("1.2000000000000001, 1.2")
    );
  }

  /**
   *  arg: sequence of decimal & string .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs011() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:decimal('1.2'), '1.2'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertPermutation("\"1.2\", 1.2")
    );
  }

  /**
   *  arg: sequence of decimal & float .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs012() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:decimal('1.2'), xs:float('1.2')))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1.2")
    );
  }

  /**
   *  arg: sequence of decimal & double .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs013() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:decimal('1.2'), xs:double('1.2')))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1.2")
    );
  }

  /**
   *  arg: sequence of float & string .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs014() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:float('NaN'), 'NaN'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NaN NaN")
    );
  }

  /**
   *  arg: sequence of float & string .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs015() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:float('INF'), 'INF'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "INF INF")
    );
  }

  /**
   *  arg: sequence of float & string .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs016() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:float('-INF'), '-INF'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-INF -INF")
    );
  }

  /**
   *  arg: sequence of float & float .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs017() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:float('INF'), xs:float('INF')))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "INF")
    );
  }

  /**
   *  arg: sequence of float & float .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs018() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:float('-INF'), xs:float('INF')))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertPermutation("xs:float('-INF'), xs:float('INF')")
    );
  }

  /**
   *  arg: sequence of float & float .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs019() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:float('NaN'), xs:float('NaN')))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   *  arg: sequence of float & float .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs020() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:float('NaN'), xs:float('NaN')))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   *  arg: sequence of float & double .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs021() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:float('NaN'), xs:double('NaN')))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   *  arg: sequence of float & double .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs022() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:float('INF'), xs:double('INF')))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "INF")
    );
  }

  /**
   *  arg: sequence of float & double .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs023() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:float('-INF'), xs:double('-INF')))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-INF")
    );
  }

  /**
   *  arg: sequence of double & double .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs024() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:double('-INF'), xs:double('INF')))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "INF -INF")
      ||
        assertStringValue(false, "-INF INF")
      )
    );
  }

  /**
   *  arg: sequence of double & double .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs025() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:double('NaN'), xs:double('NaN')))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   *  arg: sequence of double & double .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs026() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:double('NaN'), xs:double('NaN')))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   *  arg: sequence of string .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs027() {
    final XQuery query = new XQuery(
      "fn:distinct-values((\"NaN\", \"-NaN\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "NaN -NaN")
      ||
        assertStringValue(false, "-NaN NaN")
      )
    );
  }

  /**
   *  arg: sequence of string .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs028() {
    final XQuery query = new XQuery(
      "fn:distinct-values((\"-INF\", \"INF\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "INF -INF")
      ||
        assertStringValue(false, "-INF INF")
      )
    );
  }

  /**
   *  arg: sequence of boolean .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs029() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:boolean('true'), true()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  arg: sequence of boolean .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs030() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:boolean('true'), xs:boolean('1')))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  arg: sequence of boolean .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs031() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:boolean('false'), xs:boolean('0')))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  arg: sequence of boolean .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs032() {
    final XQuery query = new XQuery(
      "fn:distinct-values(( true(), false(), () ))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertPermutation("true(), false()")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesdbl1args1() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:double(\"-1.7976931348623157E308\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:double(mid range) .
   */
  @org.junit.Test
  public void fnDistinctValuesdbl1args2() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:double(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:double(upper bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesdbl1args3() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:double(\"1.7976931348623157E308\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesdec1args1() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:decimal(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:decimal(mid range) .
   */
  @org.junit.Test
  public void fnDistinctValuesdec1args2() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:decimal(\"617375191608514839\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("617375191608514839")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:decimal(upper bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesdec1args3() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:decimal(\"999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesflt1args1() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:float(\"-3.4028235E38\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("xs:float(\"-3.4028235E38\")")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:float(mid range) .
   */
  @org.junit.Test
  public void fnDistinctValuesflt1args2() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:float(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:float(upper bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesflt1args3() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:float(\"3.4028235E38\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("xs:float(\"3.4028235E38\")")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesint1args1() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:int(\"-2147483648\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-2147483648")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:int(mid range) .
   */
  @org.junit.Test
  public void fnDistinctValuesint1args2() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:int(\"-1873914410\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-1873914410")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:int(upper bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesint1args3() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:int(\"2147483647\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("2147483647")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesintg1args1() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:integer(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:integer(mid range) .
   */
  @org.junit.Test
  public void fnDistinctValuesintg1args2() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:integer(\"830993497117024304\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("830993497117024304")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:integer(upper bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesintg1args3() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:integer(\"999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnDistinctValueslng1args1() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:long(\"-92233720368547758\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-92233720368547758")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:long(mid range) .
   */
  @org.junit.Test
  public void fnDistinctValueslng1args2() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:long(\"-47175562203048468\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-47175562203048468")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:long(upper bound) .
   */
  @org.junit.Test
  public void fnDistinctValueslng1args3() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:long(\"92233720368547758\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("92233720368547758")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesnint1args1() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:negativeInteger(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:negativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnDistinctValuesnint1args2() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:negativeInteger(\"-297014075999096793\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-297014075999096793")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:negativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesnint1args3() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:negativeInteger(\"-1\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-1")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesnni1args1() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:nonNegativeInteger(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:nonNegativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnDistinctValuesnni1args2() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:nonNegativeInteger(\"303884545991464527\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("303884545991464527")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:nonNegativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesnni1args3() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:nonNegativeInteger(\"999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesnpi1args1() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:nonPositiveInteger(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:nonPositiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnDistinctValuesnpi1args2() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:nonPositiveInteger(\"-475688437271870490\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-475688437271870490")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:nonPositiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesnpi1args3() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:nonPositiveInteger(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnDistinctValuespint1args1() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:positiveInteger(\"1\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:positiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnDistinctValuespint1args2() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:positiveInteger(\"52704602390610033\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("52704602390610033")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:positiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnDistinctValuespint1args3() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:positiveInteger(\"999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnDistinctValuessht1args1() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:short(\"-32768\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-32768")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:short(mid range) .
   */
  @org.junit.Test
  public void fnDistinctValuessht1args2() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:short(\"-5324\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-5324")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:short(upper bound) .
   */
  @org.junit.Test
  public void fnDistinctValuessht1args3() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:short(\"32767\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("32767")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesulng1args1() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:unsignedLong(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:unsignedLong(mid range) .
   */
  @org.junit.Test
  public void fnDistinctValuesulng1args2() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:unsignedLong(\"130747108607674654\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("130747108607674654")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:unsignedLong(upper bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesulng1args3() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:unsignedLong(\"184467440737095516\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("184467440737095516")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesusht1args1() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:unsignedShort(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:unsignedShort(mid range) .
   */
  @org.junit.Test
  public void fnDistinctValuesusht1args2() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:unsignedShort(\"44633\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("44633")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:unsignedShort(upper bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesusht1args3() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:unsignedShort(\"65535\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("65535")
    );
  }
}
