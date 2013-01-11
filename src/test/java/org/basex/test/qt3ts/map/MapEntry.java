package org.basex.test.qt3ts.map;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the map:entry function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class MapEntry extends QT3TestSet {

  /**
   * Integer key, singleton value.
   */
  @org.junit.Test
  public void mapEntry001() {
    final XQuery query = new XQuery(
      "map:entry(3, 5)",
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
        assertCount(1)
      &&
        assertType("map(*)")
      &&
        assertType("map(xs:integer, xs:integer)")
      &&
        assertType("function(xs:anyAtomicType) as xs:integer?")
      &&
        assertQuery("$result(3) eq 5")
      &&
        assertQuery("map:size($result) eq 1")
      &&
        assertQuery("map:size(map:remove($result, 3)) eq 0")
      &&
        assertQuery("map:size(map:remove($result, 1)) eq 1")
      )
    );
  }

  /**
   * Integer key, empty value.
   */
  @org.junit.Test
  public void mapEntry002() {
    final XQuery query = new XQuery(
      "map:entry(3, ())",
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
        assertCount(1)
      &&
        assertType("map(*)")
      &&
        assertType("map(xs:integer, empty-sequence())")
      &&
        assertQuery("empty($result(3))")
      &&
        assertQuery("map:size($result) eq 1")
      &&
        assertQuery("map:size(map:remove($result, 3)) eq 0")
      &&
        assertQuery("map:size(map:remove($result, 1)) eq 1")
      )
    );
  }

  /**
   * String key, sequence value.
   */
  @org.junit.Test
  public void mapEntry003() {
    final XQuery query = new XQuery(
      "map:entry(\"foo\", (\"x\", \"y\", \"z\"))",
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
        assertCount(1)
      &&
        assertType("map(*)")
      &&
        assertType("map(xs:string, xs:string*)")
      &&
        assertQuery("empty($result(\"bar\"))")
      &&
        assertQuery("count($result(\"foo\")) eq 3")
      &&
        assertQuery("map:get($result, \"foo\") = \"z\"")
      &&
        assertQuery("map:get($result, xs:untypedAtomic(\"foo\")) = \"z\"")
      &&
        assertQuery("map:size($result) eq 1")
      &&
        assertQuery("map:size(map:remove($result, \"foo\")) eq 0")
      &&
        assertQuery("map:size(map:remove($result, \"bar\")) eq 1")
      )
    );
  }

  /**
   * Untyped atomic key, map as value.
   */
  @org.junit.Test
  public void mapEntry004() {
    final XQuery query = new XQuery(
      "map:entry(xs:untypedAtomic(\"foo\"), map{})",
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
        assertCount(1)
      &&
        assertType("map(*)")
      &&
        assertType("map(xs:string, map(*))")
      &&
        assertQuery("empty($result(\"bar\"))")
      &&
        assertQuery("count($result(\"foo\")) eq 1")
      &&
        assertQuery("empty($result(\"foo\")(\"bar\"))")
      &&
        assertQuery("empty($result(xs:untypedAtomic(\"foo\"))(\"bar\"))")
      )
    );
  }

  /**
   * NaN as key.
   */
  @org.junit.Test
  public void mapEntry005() {
    final XQuery query = new XQuery(
      "map:entry(number('NaN'), 'NaN')",
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
        assertCount(1)
      &&
        assertType("map(*)")
      &&
        assertQuery("map:size($result) eq 0")
      &&
        assertQuery("empty($result(number('NaN')))")
      )
    );
  }

  /**
   * float NaN as key.
   */
  @org.junit.Test
  public void mapEntry006() {
    final XQuery query = new XQuery(
      "map:entry(xs:float('NaN'), 'NaN')",
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
        assertCount(1)
      &&
        assertType("map(*)")
      &&
        assertQuery("map:size($result) eq 0")
      )
    );
  }

  /**
   * collation of a singleton map.
   */
  @org.junit.Test
  public void mapEntry007() {
    final XQuery query = new XQuery(
      "map:collation(map:entry(0,1))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.w3.org/2005/xpath-functions/collation/codepoint")
    );
  }
}
