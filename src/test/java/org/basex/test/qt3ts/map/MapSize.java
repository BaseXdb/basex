package org.basex.test.qt3ts.map;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the map:size function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class MapSize extends QT3TestSet {

  /**
   * Size of empty map.
   */
  @org.junit.Test
  public void mapSize001() {
    final XQuery query = new XQuery(
      "map:size(map{})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("0")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Size of empty map.
   */
  @org.junit.Test
  public void mapSize002() {
    final XQuery query = new XQuery(
      "map:size(map:new(()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("0")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Size of singleton map.
   */
  @org.junit.Test
  public void mapSize003() {
    final XQuery query = new XQuery(
      "map:size(map{\"a\":=1})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("1")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Size of singleton map.
   */
  @org.junit.Test
  public void mapSize004() {
    final XQuery query = new XQuery(
      "map:size(map:entry(\"a\", \"1\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("1")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Size of two-entry map.
   */
  @org.junit.Test
  public void mapSize005() {
    final XQuery query = new XQuery(
      "map:size(map:new((map:entry(\"a\", \"1\"), map:entry(\"b\", 2))))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("2")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Size of two-entry map.
   */
  @org.junit.Test
  public void mapSize006() {
    final XQuery query = new XQuery(
      "map:size(map{\"a\":=1, \"b\":=2})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("2")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Elimination of duplicates.
   */
  @org.junit.Test
  public void mapSize007() {
    final XQuery query = new XQuery(
      "map:size(map{\"a\":=1, \"a\":=2})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("1")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Elimination of duplicates.
   */
  @org.junit.Test
  public void mapSize008() {
    final XQuery query = new XQuery(
      "map:size(map:new((map:entry(\"a\",1), map:entry(\"a\",2))))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("1")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Size after removing an entry.
   */
  @org.junit.Test
  public void mapSize009() {
    final XQuery query = new XQuery(
      "map:size(map:remove(map{\"a\":=1,\"b\":=2}, \"b\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("1")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Size after removing the only entry.
   */
  @org.junit.Test
  public void mapSize010() {
    final XQuery query = new XQuery(
      "map:size(map:remove(map:entry(1,2),1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("0")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Size after removing the only two entries.
   */
  @org.junit.Test
  public void mapSize011() {
    final XQuery query = new XQuery(
      "map:size(map:remove(map:remove(map{\"a\":=1,\"b\":=2},\"b\"),\"a\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("0")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Size after adding a NaN entry.
   */
  @org.junit.Test
  public void mapSize012() {
    final XQuery query = new XQuery(
      "map:size(map{number('NaN'):=1,\"b\":=2})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("1")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Size after a null remove operation.
   */
  @org.junit.Test
  public void mapSize013() {
    final XQuery query = new XQuery(
      "map:size(map:remove(map{\"a\":=1,\"b\":=2}, \"c\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("2")
      &&
        assertType("xs:integer")
      )
    );
  }
}
