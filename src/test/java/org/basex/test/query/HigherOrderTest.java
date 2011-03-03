package org.basex.test.query;

import static org.junit.Assert.*;

import org.basex.core.BaseXException;
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
   * @throws BaseXException exception
   */
  @Test
  public void shadowingTest() throws BaseXException {
    query("let $x := 1 to 9 return fold-left(" +
        "function($x, $y){$x * 10 + $y}, 0, $x)", "123456789");
  }

  /**
   * Test for name shadowing.
   * @throws BaseXException exception
   */
  @Test
  public void shadowingTest2() throws BaseXException {
    query("for $a in (for $a in (1, 2, 3) return $a mod 2)" +
        "return function(){ $a }()", "1 0 1");
  }

  /**
   * Test for name shadowing.
   * @throws BaseXException exception
   */
  @Test
  public void shadowingTest3() throws BaseXException {
    query("for $a in (for $a in (1, 2, 3) return $a mod 2)" +
        "return 3 * $a", "3 0 3");
  }

  /**
   * Test for name heavy currying.
   * @throws BaseXException exception
   */
  @Test
  public void curryTest() throws BaseXException {
    query("let $digits := 1 to 9," +
        "$base-cmb := function($b, $n, $d) { $b * $n + $d }," +
        "$dec-cmb := $base-cmb(10, ?, ?)," +
        "$from-digits := fold-left($dec-cmb, 0, ?)" +
        "return $from-digits($digits)",
        "123456789");
  }

  /**
   * Test for name heavy currying.
   * @throws BaseXException exception
   */
  @Test
  public void curryTest2() throws BaseXException {
    query("let $digits := 1 to 9," +
        "$base-cmb := function($n, $d) { 10 * $n + $d }," +
        "$from-digits := fold-left($base-cmb, 0, ?)" +
        "return $from-digits(1 to 9)",
        "123456789");
  }

  /**  Test for name heavy currying. */
  @Test
  public void placeHolderTest() {
    try {
      fail("succeeded with: " + query("string-join(('a', 'b'), )(',')"));
    } catch(final BaseXException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("XPST0003"));
    }
  }

}
