package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the higher-order fn:filter function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnFilter extends QT3TestSet {

  /**
   * Basic test using starts-with().
   */
  @org.junit.Test
  public void filter001() {
    final XQuery query = new XQuery(
      "filter(starts-with(?, \"a\"), (\"apple\", \"pear\", \"apricot\", \"advocado\", \"orange\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("\"apple\", \"apricot\", \"advocado\"")
    );
  }

  /**
   * Test using an inline user-defined function.
   */
  @org.junit.Test
  public void filter002() {
    final XQuery query = new XQuery(
      "filter(function($x){$x gt 10}, (12, 4, 46, 23, -8))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("12, 46, 23")
    );
  }

  /**
   * Test using an inline user-defined function.
   */
  @org.junit.Test
  public void filter003() {
    final XQuery query = new XQuery(
      "let $data := (/employees)\n" +
      "              return filter(function($x as element(emp)){xs:int($x/@salary) lt 300}, $data/emp)",
      ctx);
    query.context(node(file("fn/filter/filter003.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertCount(3)
      &&
        assertType("element(emp)*")
      &&
        assertQuery("$result/@name = 'john'")
      &&
        assertQuery("$result/@name = 'anne'")
      &&
        assertQuery("$result/@name = 'kumar'")
      )
    );
  }

  /**
   * Test using an inline user-defined function.
   */
  @org.junit.Test
  public void filter004() {
    final XQuery query = new XQuery(
      "(1 to 20)[. = filter(function($x){$x idiv 2 * 2 = $x}, 1 to position())]",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("2, 4, 6, 8, 10, 12, 14, 16, 18, 20")
    );
  }

  /**
   * implement eg:index-of-node().
   */
  @org.junit.Test
  public void filter005() {
    final XQuery query = new XQuery(
      "let $index-of-node := function($seqParam as node()*, $srchParam as node()) as xs:integer* \n" +
      "                                    { filter( function($this as xs:integer) as xs:boolean \n" +
      "                                              {$seqParam[$this] is $srchParam}, 1 to count($seqParam) ) },\n" +
      "            $nodes := /*/*,\n" +
      "            $perm := ($nodes[1], $nodes[2], $nodes[3], $nodes[1], $nodes[2], $nodes[4], $nodes[2], $nodes[1]) \n" +
      "            return $index-of-node($perm, $nodes[2]) ",
      ctx);
    query.context(node(file("fn/filter/filter005.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("2, 5, 7")
    );
  }

  /**
   * filter function - not a boolean.
   */
  @org.junit.Test
  public void filter901() {
    final XQuery query = new XQuery(
      "filter(normalize-space#1, (\"apple\", \"pear\", \"apricot\", \"advocado\", \"orange\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * filter function - returns empty sequence .
   */
  @org.junit.Test
  public void filter902() {
    final XQuery query = new XQuery(
      "filter(function($x){if(starts-with($x,'a')) then true() else ()}, (\"apple\", \"pear\", \"apricot\", \"advocado\", \"orange\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * filter function - returns non-singleton sequence.
   */
  @org.junit.Test
  public void filter903() {
    final XQuery query = new XQuery(
      "filter(function($x){if(starts-with($x,'a')) then (true(), true()) else false()}, (\"apple\", \"pear\", \"apricot\", \"advocado\", \"orange\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * filter function - item in sequence is of wrong type.
   */
  @org.junit.Test
  public void filter904() {
    final XQuery query = new XQuery(
      "filter(ends-with(?, 'e'), (\"apple\", \"pear\", \"apricot\", \"advocado\", \"orange\", current-date()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }
}
