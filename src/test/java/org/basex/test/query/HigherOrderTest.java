package org.basex.test.query;

import static org.junit.Assert.*;
import org.basex.query.QueryException;
import org.junit.Test;

/**
 * Higher-order function tests.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Leo Woerteler
 */
public class HigherOrderTest extends AdvancedQueryTest {

  /** Constructor. */
  public HigherOrderTest() {
    super("fn");
  }

  /**
   * Test for name shadowing.
   * @throws QueryException exception
   */
  @Test
  public void shadowingTest() throws QueryException {
    query("let $x := 1 to 9 return fold-left(" +
        "function($x, $y){$x * 10 + $y}, 0, $x)", "123456789");
  }

  /**
   * Test for name heavy currying.
   * @throws QueryException exception
   */
  @Test
  public void curryTest() throws QueryException {
    query("let $digits := 1 to 9," +
        "$base-cmb := function($b, $n, $d) { $b * $n + $d }," +
        "$dec-cmb := $base-cmb(10, ?, ?)," +
        "$from-digits := fold-left($dec-cmb, 0, ?)" +
        "return $from-digits($digits)",
        "123456789");
  }

  /**
   * Test for name heavy currying.
   * @throws QueryException exception
   */
  @Test
  public void curryTest2() throws QueryException {
    query("let $digits := 1 to 9," +
        "$base-cmb := function($n, $d) { 10 * $n + $d }," +
        "$from-digits := fold-left($base-cmb, 0, ?)" +
        "return $from-digits(1 to 9)",
        "123456789");
  }

  /**
   * Test for name heavy currying.
   * @throws QueryException exception
   */
  @Test
  public void foldRightTest() throws QueryException {
    query("declare function local:before-first(" +
        "  $input as item()*," +
        "  $pred as function(item()) as item()*" +
        ") as item()* {" +
        "  fold-right(" +
        "    function($x, $xs) { if($pred($x)) then () else ($x, $xs) }," +
        "      (), $input)" +
        "};" +
        "local:before-first((<h1/>, <p/>, <h1/>, <h2/>, <h3/>)," +
        "  function($it) { name($it) = 'h2' })",
        "<h1/><p/><h1/>");
  }

  /**
   * Test for name heavy currying.
   * @throws QueryException exception
   */
  @Test
  public void typeTest() throws QueryException {
    query("declare function local:f($x as xs:long, $y as xs:NCName)" +
        "    as element(e) {" +
        "  <e x='{$x}' y='{$y}'/>" +
        "};" +
        "local:f#2 instance of function(xs:long, xs:NCName) as element(e)",
        "true");
  }

  /**  Test for name heavy currying. */
  @Test
  public void placeHolderTest() {
    try {
      fail("succeeded with: " + query("string-join(('a', 'b'), )(',')"));
    } catch(final QueryException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("XPST0003"));
    }
  }

}
