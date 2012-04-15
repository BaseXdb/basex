package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests the fn:map-pairs() function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnMapPairs extends QT3TestSet {

  /**
   * Apply deep-equal to corresponding pairs.
   */
  @org.junit.Test
  public void mapPairs001() {
    final XQuery query = new XQuery(
      "map-pairs(deep-equal#2, (\"aa\", \"bb\", \"cc\", \"dd\", \"ee\"), (\"AA\", \"BB\", \"cc\", \"dd\", \"EE\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("false(), false(), true(), true(), false()")
    );
  }

  /**
   * Apply deep-equal to corresponding pairs.
   */
  @org.junit.Test
  public void mapPairs002() {
    final XQuery query = new XQuery(
      "map-pairs(deep-equal#2, (\"aa\", \"bb\", \"cc\", \"dd\", \"ee\", \"ff\"), (\"AA\", \"BB\", \"cc\", \"dd\", \"EE\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("false(), false(), true(), true(), false()")
    );
  }

  /**
   * Apply deep-equal to corresponding pairs.
   */
  @org.junit.Test
  public void mapPairs003() {
    final XQuery query = new XQuery(
      "map-pairs(deep-equal#2, (\"aa\", \"bb\", \"cc\", \"dd\", \"ee\"), (\"AA\", \"BB\", \"cc\", \"dd\", \"EE\", \"ff\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("false(), false(), true(), true(), false()")
    );
  }

  /**
   * Apply concat to corresponding pairs.
   */
  @org.junit.Test
  public void mapPairs004() {
    final XQuery query = new XQuery(
      "map-pairs(concat(?, '-', ?), (\"aa\", \"bb\", \"cc\", \"dd\", \"ee\"), (\"AA\", \"BB\", \"cc\", \"dd\", \"EE\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("'aa-AA', 'bb-BB', 'cc-cc', 'dd-dd', 'ee-EE'")
    );
  }

  /**
   * Apply user-defined anonymous function to corresponding pairs.
   */
  @org.junit.Test
  public void mapPairs005() {
    final XQuery query = new XQuery(
      "map-pairs(function($a as xs:integer, $b as xs:integer) as xs:integer{$a + $b}, 1 to 5, 1 to 5)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("2, 4, 6, 8, 10")
    );
  }

  /**
   * Use a function that has a closure.
   */
  @org.junit.Test
  public void mapPairs006() {
    final XQuery query = new XQuery(
      " \n" +
      "            let $millenium := year-from-date(current-date()) idiv 1000 \n" +
      "            return map-pairs(function($a as xs:integer, $b as xs:integer) as xs:integer{$a + $b + $millenium}, 1 to 5, 2 to 6)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("5, 7, 9, 11, 13")
    );
  }

  /**
   * Use a function that has a closure.
   */
  @org.junit.Test
  public void mapPairs007() {
    final XQuery query = new XQuery(
      " \n" +
      "            let $millenium := year-from-date(current-date()) idiv 1000 \n" +
      "            return map-pairs(function($a, $b) as xs:integer* {1 to (string-length($a) + string-length($b))}, (\"a\", \"ab\", \"abc\", \"\"), (\"\", \"\", \"\", \"\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("1, 1, 2, 1, 2, 3")
    );
  }

  /**
   * map-pairs function - exercise from XML Prague 2010:
   *             form sum of adjacent values in an input sequence.
   */
  @org.junit.Test
  public void mapPairs008() {
    final XQuery query = new XQuery(
      " let $in := 1 to 5 return map-pairs(function($a, $b){$a+$b}, $in, tail($in)) ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("3, 5, 7, 9")
    );
  }

  /**
   * map-pairs function, wrong arity function.
   */
  @org.junit.Test
  public void mapPairs901() {
    final XQuery query = new XQuery(
      "map-pairs(deep-equal#3, (\"aa\", \"bb\", \"cc\", \"dd\", \"ee\"), (\"AA\", \"BB\", \"cc\", \"dd\", \"EE\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * map-pairs function, wrong input to function.
   */
  @org.junit.Test
  public void mapPairs902() {
    final XQuery query = new XQuery(
      "map-pairs(contains#2, (\"aa\", \"bb\", \"cc\", \"dd\", \"ee\"), (\"AA\", \"BB\", \"cc\", \"dd\", 12))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }
}
