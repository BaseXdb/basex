package org.basex.test.qt3ts.map;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the map:remove function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class MapRemove extends QT3TestSet {

  /**
   * Remove from empty map.
   */
  @org.junit.Test
  public void mapRemove001() {
    final XQuery query = new XQuery(
      "map:remove(map{}, 1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertQuery("map:size($result) eq 0")
    );
  }

  /**
   * Keys in empty map.
   */
  @org.junit.Test
  public void mapRemove002() {
    final XQuery query = new XQuery(
      "map:remove(map:new(()), \"abcd\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertQuery("map:size($result) eq 0")
    );
  }

  /**
   * Remove from singleton map.
   */
  @org.junit.Test
  public void mapRemove003() {
    final XQuery query = new XQuery(
      "map:remove(map{\"a\":=1}, \"a\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertQuery("map:size($result) eq 0")
    );
  }

  /**
   * Remove from singleton map.
   */
  @org.junit.Test
  public void mapRemove005() {
    final XQuery query = new XQuery(
      "map:remove(map:entry(\"a\", \"1\"), \"b\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertQuery("map:size($result) eq 1")
      &&
        assertQuery("map:contains($result, \"a\")")
      )
    );
  }

  /**
   * Remove from two-entry map.
   */
  @org.junit.Test
  public void mapRemove006() {
    final XQuery query = new XQuery(
      "map:remove(map:new((map:entry(\"a\", \"1\"), map:entry(\"b\", 2))), \"b\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertQuery("map:size($result) eq 1")
      &&
        assertQuery("map:contains($result, \"a\")")
      )
    );
  }

  /**
   * Null remove from two-entry map.
   */
  @org.junit.Test
  public void mapRemove007() {
    final XQuery query = new XQuery(
      "map:remove(map:new((map:entry(\"a\", \"1\"), map:entry(\"b\", 2))), \"c\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertQuery("map:keys($result) = \"a\"")
      &&
        assertQuery("map:keys($result) = \"b\"")
      &&
        assertQuery("map:size($result) = 2")
      )
    );
  }

  /**
   * Remove match of numeric values.
   */
  @org.junit.Test
  public void mapRemove008() {
    final XQuery query = new XQuery(
      "map:remove(map:new((map:entry(12, 1), map:entry(13, 2))), 12e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertQuery("map:size($result) = 1")
      &&
        assertType("map(xs:integer, xs:integer)")
      &&
        assertQuery("map:contains($result, 13)")
      )
    );
  }

  /**
   * Remove match of untypedAtomic.
   */
  @org.junit.Test
  public void mapRemove009() {
    final XQuery query = new XQuery(
      "map:remove(map:new((map:entry(\"a\",1), map:entry(\"b\",2))), xs:untypedAtomic(\"b\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertQuery("map:size($result) = 1")
      &&
        assertType("map(xs:string, xs:integer)")
      &&
        assertQuery("map:contains($result, \"a\")")
      )
    );
  }

  /**
   * Type after removing an entry.
   */
  @org.junit.Test
  public void mapRemove010() {
    final XQuery query = new XQuery(
      "map:remove(map{\"a\":=1,\"b\":=\"xyz\"}, \"b\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertType("map(xs:string, xs:integer)")
    );
  }

  /**
   * Type after removing an entry.
   */
  @org.junit.Test
  public void mapRemove011() {
    final XQuery query = new XQuery(
      "map:remove(map{\"a\":=1,12:=\"xyz\"}, 12)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertType("map(xs:string, xs:integer)")
    );
  }

  /**
   * Remove from a large map.
   */
  @org.junit.Test
  public void mapRemove012() {
    final XQuery query = new XQuery(
      "map:remove(map:new(for $n in 1 to 500000 return map:entry($n, $n+1)), 123456)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertQuery("map:contains($result, 1)")
      &&
        assertQuery("map:contains($result, 500000)")
      &&
        assertQuery("map:size($result) = 499999")
      &&
        assertQuery("not(map:contains($result, 123456))")
      )
    );
  }

  /**
   * Deep equal after removing an entry.
   */
  @org.junit.Test
  public void mapRemove013() {
    final XQuery query = new XQuery(
      "deep-equal(map:remove(map{\"a\":=1,\"b\":=(2,3)}, \"a\"), map:entry(\"b\", (2,3)))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }
}
