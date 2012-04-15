package org.basex.test.qt3ts.map;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the map:collation function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class MapCollation extends QT3TestSet {

  /**
   * Collation of empty map.
   */
  @org.junit.Test
  public void mapCollation001() {
    final XQuery query = new XQuery(
      "map:collation(map{})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.w3.org/2005/xpath-functions/collation/codepoint")
    );
  }

  /**
   * Collation of empty map.
   */
  @org.junit.Test
  public void mapCollation002() {
    final XQuery query = new XQuery(
      "map:collation(map:new(()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.w3.org/2005/xpath-functions/collation/codepoint")
    );
  }

  /**
   * Collation of singleton map.
   */
  @org.junit.Test
  public void mapCollation003() {
    final XQuery query = new XQuery(
      "map:collation(map:new(map:entry(\"a\",\"x\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.w3.org/2005/xpath-functions/collation/codepoint")
    );
  }

  /**
   * Collation of singleton map.
   */
  @org.junit.Test
  public void mapCollation004() {
    final XQuery query = new XQuery(
      "map:collation(map:entry(\"a\",\"x\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.w3.org/2005/xpath-functions/collation/codepoint")
    );
  }

  /**
   * Collation of 2-entry map.
   */
  @org.junit.Test
  public void mapCollation005() {
    final XQuery query = new XQuery(
      "map:collation(map{\"a\":=1,\"b\":=2})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.w3.org/2005/xpath-functions/collation/codepoint")
    );
  }

  /**
   * Collation of 2-entry map.
   */
  @org.junit.Test
  public void mapCollation006() {
    final XQuery query = new XQuery(
      "map:collation(map:new((map:entry(\"a\",\"x\"),map:entry(\"b\",\"y\"))))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.w3.org/2005/xpath-functions/collation/codepoint")
    );
  }

  /**
   * Collation of empty map given explicitly.
   */
  @org.junit.Test
  public void mapCollation007() {
    final XQuery query = new XQuery(
      "map:collation(map:new((), \"http://www.w3.org/2005/xpath-functions/collation/codepoint\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.w3.org/2005/xpath-functions/collation/codepoint")
    );
  }

  /**
   * Collation of non-empty map given explicitly.
   */
  @org.junit.Test
  public void mapCollation008() {
    final XQuery query = new XQuery(
      "map:collation(map:new(map{\"a\":=1,\"b\":=2}, \"http://www.w3.org/2005/xpath-functions/collation/codepoint\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.w3.org/2005/xpath-functions/collation/codepoint")
    );
  }

  /**
   * Collation of non-empty map given as relative URI.
   */
  @org.junit.Test
  public void mapCollation009() {
    final XQuery query = new XQuery(
      "map:collation(map:new(map{\"a\":=1,\"b\":=2}, \"xpath-functions/collation/codepoint\"))",
      ctx);
    query.baseURI("http://www.w3.org/2005/");

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.w3.org/2005/xpath-functions/collation/codepoint")
    );
  }
}
