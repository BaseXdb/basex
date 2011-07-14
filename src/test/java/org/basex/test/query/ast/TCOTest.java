package org.basex.test.query.ast;

import org.junit.Test;

/**
 * Tests for proper tail-calls.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Leo Woerteler
 */
public class TCOTest extends QueryPlanTest {
  /** Checks if tail-call optimization was applied. */
  @Test
  public void facTest() {
    check("declare function local:fac($n, $f) {" +
        "  if($n = 0) then $f" +
        "  else local:fac($n - 1, $f * $n)" +
        "};" +
        "local:fac(123, xs:decimal(1))",

        "12146304367025329675766243241881295855454217088483382315328918161829" +
        "23589236216766883115696061264020217073583522129404778259109157041165" +
        "14721860295199062616467307339074198149529600000000000000000000000000" +
        "00",

        "exists(//TailFuncCall)"
    );
  }

  /** Checks if TCO was applied to mutually recursive functions. */
  @Test
  public void evenOddTest() {
    check("declare function local:odd($n) {" +
        "  if($n = 0) then false()" +
        "  else local:even($n - 1)" +
        "};" +
        "declare function local:even($n) {" +
        "  if($n = 0) then true()" +
        "  else local:odd($n - 1)" +
        "};" +
        "local:odd(12345)",

        "true",

        "count(//TailFuncCall) eq 2"
    );
  }

  /** Checks if partially tail-recursive functions are properly optimized. */
  @Test
  public void fastPowTest() {
    check("declare function local:pow($n, $k) {" +
        "  if($k = 0) then xs:decimal(1)" +
        "  else if($k mod 2 = 0) then local:pow($n * $n, $k idiv 2)" +
        "  else $n * local:pow($n * $n, $k idiv 2)" +
        "};" +
        "local:pow(4, 5)",

        "1024",

        "exists(//TailFuncCall)",
        "count(//BaseFuncCall) eq 2"
    );
  }
}
