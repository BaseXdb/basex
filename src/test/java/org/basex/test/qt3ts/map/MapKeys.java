package org.basex.test.qt3ts.map;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the map:keys function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class MapKeys extends QT3TestSet {

  /**
   * Keys in empty map.
   */
  @org.junit.Test
  public void mapKeys001() {
    final XQuery query = new XQuery(
      "map:keys(map{})",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   * Keys in empty map.
   */
  @org.junit.Test
  public void mapKeys002() {
    final XQuery query = new XQuery(
      "map:keys(map:new(()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   * Keys in singleton map.
   */
  @org.junit.Test
  public void mapKeys003() {
    final XQuery query = new XQuery(
      "map:keys(map{\"a\":=1})",
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
        assertType("xs:string")
      &&
        assertEq("\"a\"")
      )
    );
  }

  /**
   * Keys in singleton map.
   */
  @org.junit.Test
  public void mapKeys004() {
    final XQuery query = new XQuery(
      "map:keys(map:entry(\"a\", \"1\"))",
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
        assertType("xs:string")
      &&
        assertEq("\"a\"")
      )
    );
  }

  /**
   * Keys in two-entry map.
   */
  @org.junit.Test
  public void mapKeys005() {
    final XQuery query = new XQuery(
      "map:keys(map:new((map:entry(\"a\", \"1\"), map:entry(\"b\", 2))))",
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
        assertQuery("$result = \"a\"")
      &&
        assertQuery("$result = \"b\"")
      &&
        assertCount(2)
      )
    );
  }

  /**
   * Size of two-entry map.
   */
  @org.junit.Test
  public void mapKeys006() {
    final XQuery query = new XQuery(
      "map:keys(map{\"a\":=1, \"b\":=2})",
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
        assertQuery("$result = \"a\"")
      &&
        assertQuery("$result = \"b\"")
      &&
        assertCount(2)
      )
    );
  }

  /**
   * Elimination of duplicates.
   */
  @org.junit.Test
  public void mapKeys007() {
    final XQuery query = new XQuery(
      "map:keys(map{\"a\":=1, \"a\":=2})",
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
        assertType("xs:string")
      &&
        assertEq("\"a\"")
      )
    );
  }

  /**
   * Elimination of duplicates.
   */
  @org.junit.Test
  public void mapKeys008() {
    final XQuery query = new XQuery(
      "map:keys(map:new((map:entry(\"a\",1), map:entry(\"a\",2))))",
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
        assertType("xs:string")
      &&
        assertEq("\"a\"")
      )
    );
  }

  /**
   * Keys after removing an entry.
   */
  @org.junit.Test
  public void mapKeys009() {
    final XQuery query = new XQuery(
      "map:keys(map:remove(map{\"a\":=1,\"b\":=2}, \"b\"))",
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
        assertType("xs:string")
      &&
        assertEq("\"a\"")
      )
    );
  }

  /**
   * Keys after removing the only entry.
   */
  @org.junit.Test
  public void mapKeys010() {
    final XQuery query = new XQuery(
      "map:keys(map:remove(map:entry(1,2),1))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   * Keys after removing the only two entries.
   */
  @org.junit.Test
  public void mapKeys011() {
    final XQuery query = new XQuery(
      "map:keys(map:remove(map:remove(map{\"a\":=1,\"b\":=2},\"b\"),\"a\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   * Keys after adding a NaN entry.
   */
  @org.junit.Test
  public void mapKeys012() {
    final XQuery query = new XQuery(
      "map:keys(map{number('NaN'):=1,\"b\":=2})",
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
        assertType("xs:string")
      &&
        assertEq("\"b\"")
      )
    );
  }

  /**
   * Size after a null remove operation.
   */
  @org.junit.Test
  public void mapKeys013() {
    final XQuery query = new XQuery(
      "map:keys(map:remove(map{\"a\":=1,\"b\":=2}, \"c\"))",
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
        assertQuery("$result = \"a\"")
      &&
        assertQuery("$result = \"b\"")
      &&
        assertCount(2)
      )
    );
  }

  /**
   * Keys for a large map.
   */
  @org.junit.Test
  public void mapKeys014() {
    final XQuery query = new XQuery(
      "map:keys(map:new(for $n in 1 to 500000 return map:entry($n, $n+1)))",
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
        assertQuery("$result = 1")
      &&
        assertQuery("$result = 500000")
      &&
        assertCount(500000)
      )
    );
  }
}
