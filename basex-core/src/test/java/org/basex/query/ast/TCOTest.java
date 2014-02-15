package org.basex.query.ast;

import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.junit.*;

/**
 * Tests for proper tail-calls.
 *
 * @author BaseX Team 2005-14, BSD License
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
        "local:fac(123, xs:decimal('1'))",

        "12146304367025329675766243241881295855454217088483382315328918161829" +
        "23589236216766883115696061264020217073583522129404778259109157041165" +
        "14721860295199062616467307339074198149529600000000000000000000000000" +
        "00",

        "exists(//" + Util.className(StaticFuncCall.class) + "[@tailCall = 'true'])"
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

        "count(//" + Util.className(StaticFuncCall.class) + "[@tailCall eq 'false']) eq 1"
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

        "exists(//" + Util.className(StaticFuncCall.class) + "[@tailCall eq 'true'])",
        "count(//" + Util.className(StaticFuncCall.class) + "[@tailCall eq 'false']) eq 2"
    );
  }

  /** Checks if a function only containing a tail call is properly optimized. */
  @Test
  public void tightLoopTest() {
    check("declare function local:foo() { local:foo() }; local:foo()",

        null,

        "exists(//" + Util.className(StaticFunc.class) + '/' +
            Util.className(StaticFuncCall.class) + "[@tailCall eq 'true'])",
        "exists(//" + Util.className(StaticFuncCall.class) + "[@tailCall eq 'false'])"
    );
  }

  /** Checks if a function only containing a tail call is properly optimized. */
  @Test
  public void selfRecursive() {
    check("declare function local:f($i) { if($i eq 12345) then $i else local:f($i+1) };" +
        "local:f(0)",

        "12345",

        "exists(//" + Util.className(If.class) + '/' +
            Util.className(StaticFuncCall.class) + "[@tailCall eq 'true'])"
    );
  }

  /** Checks if a function only containing a tail call is properly optimized. */
  @Test
  public void mixedSelfRecursive() {
    check("declare function local:inc($i) { $i + 1 };" +
        "declare function local:f($i) { if($i eq 12345) then $i " +
        "else local:f(local:inc($i)) };" +
        "local:f(0)",

        "12345",

        "exists(//" + Util.className(If.class) + '/' +
            Util.className(StaticFuncCall.class) + "[@tailCall eq 'true'])"
    );
  }

  /** Checks if dynamic function calls are tail-call optimized. */
  @Test
  public void dynFuncCall() {
    check("let $sum :=" +
        "  function($seq) {" +
        "    let $go :=" +
        "      function($seq, $acc, $go) {" +
        "        if(empty($seq)) then $acc" +
        "        else $go(tail($seq), $acc + head($seq), $go)" +
        "      }" +
        "    return $go($seq, 0, $go)" +
        "  }" +
        "return $sum(1 to 100000)",

        "5000050000",

        "empty(//" + Util.className(FuncItem.class) +
            "//" + Util.className(DynFuncCall.class) + "[@tailCall eq 'false'])"
    );
  }

  /** Checks if continuations are caught in built-in HOFs. */
  @Test
  public void hofCont() {
    check("declare function local:f($n) { if($n eq 0) then 42 else local:f($n - 1) };" +
        "distinct-values(fn:for-each((1 to 10) ! 1000, function($x) { local:f($x) }))",

        "42"
    );
  }
}
