package org.basex.test.qt3ts.map;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the map:new function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class MapNew extends QT3TestSet {

  /**
   * Evaluate the function map:new() with no argument.
   */
  @org.junit.Test
  public void mapNew001() {
    final XQuery query = new XQuery(
      "map:new()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertCount(1)
      &&
        assertType("map(*)")
      &&
        assertType("map(xs:date, element()+)")
      &&
        assertType("function(xs:QName) as item()*")
      )
    );
  }

  /**
   * Evaluate the function map:new() with argument ().
   */
  @org.junit.Test
  public void mapNew002() {
    final XQuery query = new XQuery(
      "map:new(())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertCount(1)
      &&
        assertType("map(*)")
      &&
        assertType("map(xs:date, element()+)")
      )
    );
  }

  /**
   * Evaluate the function map:new() with a single singleton map.
   */
  @org.junit.Test
  public void mapNew003() {
    final XQuery query = new XQuery(
      "map:new(map:entry(\"foo\", 1 to 5))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertCount(1)
      &&
        assertType("map(*)")
      &&
        assertType("map(xs:string, xs:integer+)")
      &&
        assertType("function(xs:anyURI) as xs:integer*")
      )
    );
  }

  /**
   * Evaluate the function map:new() with two singleton maps.
   */
  @org.junit.Test
  public void mapNew004() {
    final XQuery query = new XQuery(
      "map:new((map:entry(\"foo\", 1 to 5), map:entry(\"bar\", 6 to 10)))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertCount(1)
      &&
        assertType("map(*)")
      &&
        assertType("map(xs:string, xs:integer+)")
      &&
        assertQuery("map:size($result) eq 2")
      )
    );
  }

  /**
   * Evaluate the function map:new() with a larger set of singleton maps.
   */
  @org.junit.Test
  public void mapNew005() {
    final XQuery query = new XQuery(
      "map:new(for $i in 1 to 20 return map:entry($i, $i*$i))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertCount(1)
      &&
        assertType("map(*)")
      &&
        assertType("map(xs:integer, xs:integer)")
      &&
        assertQuery("map:size($result) eq 20")
      &&
        assertQuery("map:get($result, 9) eq 81")
      )
    );
  }

  /**
   * Evaluate the function map:new() with duplicates in the input maps.
   */
  @org.junit.Test
  public void mapNew006() {
    final XQuery query = new XQuery(
      "map:new((map:entry(\"foo\", 3), map:entry(\"foo\", 4)))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertCount(1)
      &&
        assertType("map(*)")
      &&
        assertType("map(xs:string, xs:integer)")
      &&
        assertQuery("map:size($result) eq 1")
      &&
        assertQuery("map:get($result, \"foo\") eq 4")
      )
    );
  }

  /**
   * map:new() doesn't modify its input maps.
   */
  @org.junit.Test
  public void mapNew007() {
    final XQuery query = new XQuery(
      "let $foo := map:entry(\"foo\", 3), $bar := map:entry(\"foo\", 4), $foobar := map:new(($foo, $bar))\n" +
      "              return ($foobar, $bar, $foo)[3]",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertCount(1)
      &&
        assertType("map(*)")
      &&
        assertType("map(xs:string, xs:integer)")
      &&
        assertQuery("map:size($result) eq 1")
      &&
        assertQuery("map:get($result, \"foo\") eq 3")
      )
    );
  }

  /**
   * map:new() doesn't modify its input maps.
   */
  @org.junit.Test
  public void mapNew008() {
    final XQuery query = new XQuery(
      "let $foo := map:new(for $i in 1 to 20 return map:entry($i, $i*$i)), $bar := map:entry(8, 63), $foobar := map:new(($foo, $bar))\n" +
      "              return ($foobar, $bar, $foo)[3]",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertCount(1)
      &&
        assertType("map(*)")
      &&
        assertType("map(xs:integer, xs:integer)")
      &&
        assertQuery("map:size($result) eq 20")
      &&
        assertQuery("$result(8) eq 64")
      )
    );
  }

  /**
   * map:new() doesn't modify its input maps.
   */
  @org.junit.Test
  public void mapNew009() {
    final XQuery query = new XQuery(
      "let $foo := map:new(for $i in 1 to 20 return map:entry($i, $i*$i)), $bar := map:entry(8, 63), $foobar := map:new(($foo, $bar))\n" +
      "              return ($foobar, $bar, $foo)[2]",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertCount(1)
      &&
        assertType("map(*)")
      &&
        assertType("map(xs:integer, xs:integer)")
      &&
        assertQuery("map:size($result) eq 1")
      &&
        assertQuery("$result(8) eq 63")
      )
    );
  }

  /**
   * map:new() with an empty map in the input.
   */
  @org.junit.Test
  public void mapNew010() {
    final XQuery query = new XQuery(
      "let $foo := map:new(for $i in 1 to 20 return map:entry($i, $i*$i)), $bar := map:new(), $foobar := map:new(($foo, $bar))\n" +
      "              return $foobar",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertCount(1)
      &&
        assertType("map(*)")
      &&
        assertType("map(xs:integer, xs:integer)")
      &&
        assertQuery("map:size($result) eq 20")
      &&
        assertQuery("$result(8) eq 64")
      )
    );
  }

  /**
   * map:new() with assorted keys that compare equal.
   */
  @org.junit.Test
  public void mapNew011() {
    final XQuery query = new XQuery(
      "map:new((map:entry(3, \"three\"), map:entry(3.0e0, \"threeD\"), map:entry(xs:float('3.0'), \"threeF\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertCount(1)
      &&
        assertType("map(*)")
      &&
        assertType("map(xs:float, xs:string)")
      &&
        assertQuery("map:size($result) eq 1")
      &&
        assertQuery("$result(3.0) eq \"threeF\"")
      )
    );
  }

  /**
   * map:new() with nodes as values.
   */
  @org.junit.Test
  public void mapNew012() {
    final XQuery query = new XQuery(
      "map:new(for $n in distinct-values(//*/node-name()) \n" +
      "                      return map:entry($n, //*[node-name() eq $n]))",
      ctx);
    query.namespace("ma", "http://www.example.com/AuctionWatch");
    query.namespace("xlink", "http://www.w3.org/1999/xlink");
    query.namespace("anyzone", "http://www.example.com/auctioneers#anyzone");
    query.namespace("eachbay", "http://www.example.com/auctioneers#eachbay");
    query.namespace("yabadoo", "http://www.example.com/auctioneers#yabadoo");
    query.context(node(file("docs/auction.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertCount(1)
      &&
        assertType("map(xs:QName, element()+)")
      &&
        assertQuery("map:contains($result, QName(\"http://www.example.com/AuctionWatch\", \"Seller\"))")
      )
    );
  }

  /**
   * map:new() with maps as values.
   */
  @org.junit.Test
  public void mapNew013() {
    final XQuery query = new XQuery(
      "map:new(for $n in 1 to 20 return map:entry($n, map{$n:=string($n), $n+1:=string($n+1), $n+2:=string($n+2)}))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertType("map(xs:integer, map(xs:integer, xs:string))")
      &&
        assertQuery("$result(1)(3) eq \"3\"")
      )
    );
  }

  /**
   * map:new() with absolute collation URI.
   */
  @org.junit.Test
  public void mapNew014() {
    final XQuery query = new XQuery(
      "map:new(map{\"abc\":=1,\"xyz\":=2}, \"http://www.w3.org/2005/xpath-functions/collation/codepoint\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertQuery("map:collation($result) eq \"http://www.w3.org/2005/xpath-functions/collation/codepoint\"")
    );
  }

  /**
   * map:new() with relative collation URI.
   */
  @org.junit.Test
  public void mapNew015() {
    final XQuery query = new XQuery(
      "map:new(map{\"abc\":=1,\"xyz\":=2}, \"collation/codepoint\")",
      ctx);
    query.baseURI("http://www.w3.org/2005/xpath-functions/");

    final QT3Result res = result(query);
    result = res;
    test(
      assertQuery("map:collation($result) eq \"http://www.w3.org/2005/xpath-functions/collation/codepoint\"")
    );
  }

  /**
   * Deep equal of empty maps.
   */
  @org.junit.Test
  public void mapNew016() {
    final XQuery query = new XQuery(
      "deep-equal(map{}, map:new(()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Deep equal of singleton maps.
   */
  @org.junit.Test
  public void mapNew017() {
    final XQuery query = new XQuery(
      "deep-equal(map{\"a\":=1}, map:entry(\"a\", 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Deep equal of larger maps.
   */
  @org.junit.Test
  public void mapNew018() {
    final XQuery query = new XQuery(
      "deep-equal(map{\"a\":=1,\"b\":=2,\"c\":=(3,4,5)}, map{\"c\":=(3,4,5),\"a\":=1,\"b\":=2})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Deep equal of empty maps.
   */
  @org.junit.Test
  public void mapNew019() {
    final XQuery query = new XQuery(
      "deep-equal(map{\"a\":=1}, map:new(()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * Deep equal of larger maps.
   */
  @org.junit.Test
  public void mapNew020() {
    final XQuery query = new XQuery(
      "deep-equal(map:new(for $i in 1 to 1000 return map:entry($i, $i+1)),\n" +
      "                         map:new(for $i in 2 to 1001 return map:entry($i, $i+1)))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * Deep equal of larger maps.
   */
  @org.junit.Test
  public void mapNew021() {
    final XQuery query = new XQuery(
      "deep-equal(map:new(for $i in 1 to 1000 return map:entry($i, $i+1)),\n" +
      "                         map:new(((for $i in 1 to 1000 return map:entry($i, $i+1)), map:entry(400, 402))))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * Deep equal: first operand is a submap.
   */
  @org.junit.Test
  public void mapNew022() {
    final XQuery query = new XQuery(
      "deep-equal(map:new(for $i in 1 to 1000 return map:entry($i, $i+1)),\n" +
      "                         map:new(for $i in 0 to 1000 return map:entry($i, $i+1)))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * Deep equal: second operand is a submap.
   */
  @org.junit.Test
  public void mapNew023() {
    final XQuery query = new XQuery(
      "deep-equal(map:new(for $i in 1 to 1000 return map:entry($i, $i+1)),\n" +
      "                         map:new(for $i in 2 to 1000 return map:entry($i, $i+1)))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }
}
