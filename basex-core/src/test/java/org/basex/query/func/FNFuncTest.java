package org.basex.query.func;

import org.basex.query.ast.*;
import org.basex.query.expr.*;
import org.basex.query.func.fn.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.junit.*;

/**
 * This class tests the standard higher-order functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public class FNFuncTest extends QueryPlanTest {
  /** Tests if {@code fn:fold-left} is unrolled if the sequence has less than 10 items. */
  @Test
  public void unrollFoldLeft() {
    // should be unrolled and evaluated at compile time
    check("fn:fold-left(2 to 10, 1, function($a,$b) {$a+$b})",
        "55",
        "empty(//" + Util.className(FnFoldLeft.class) + "[contains(@name, 'fold-left')])",
        "exists(*/" + Util.className(Int.class) + ')');
    // should be unrolled but not evaluated at compile time
    check("fn:fold-left(2 to 10, 1, function($a,$b) {0*random:integer($a)+$b})",
        "10",
        "empty(//" + Util.className(FnFoldLeft.class) + "[contains(@name, 'fold-left')])",
        "empty(*/" + Util.className(Int.class) + ')',
        "count(//" + Util.className(Arith.class) + "[@op = '+']) eq 9");
    // should not be unrolled
    check("fn:fold-left(1 to 10, 0, function($a,$b) {$a+$b})",
        "55",
        "exists(//" + Util.className(FnFoldLeft.class) + "[contains(@name, 'fold-left')])");
  }

  /** Tests if {@code fn:fold-right} is unrolled if the sequence has less than 10 items. */
  @Test
  public void unrollFoldRight() {
    // should be unrolled and evaluated at compile time
    check("fn:fold-right(1 to 9, 10, function($a,$b) {$a+$b})",
        "55",
        "empty(//" + Util.className(FnFoldRight.class) + ')',
        "exists(*/" + Util.className(Int.class) + ')');
    // should be unrolled but not evaluated at compile time
    check("fn:fold-right(1 to 9, 10, function($a,$b) {0*random:integer($a)+$b})",
        "10",
        "empty(//" + Util.className(FnFoldRight.class) + ')',
        "empty(*/" + Util.className(Int.class) + ')',
        "count(//" + Util.className(Arith.class) + "[@op = '+']) eq 9");
    // should not be unrolled
    check("fn:fold-right(0 to 9, 10, function($a,$b) {$a+$b})",
        "55",
        "exists(//" + Util.className(FnFoldRight.class) + "[contains(@name, 'fold-right')])");
  }

  /** Tests if {@code fn:for-each} is unrolled if the sequence has less than 10 items. */
  @Test
  public void unrollForEach() {
    // should be unrolled and evaluated at compile time
    check("fn:for-each(0 to 8, function($x) {$x+1})",
        "1 2 3 4 5 6 7 8 9",
        "empty(//" + Util.className(FnForEach.class) + ')',
        "exists(*/" + Util.className(IntSeq.class) + ')');
    // should be unrolled but not evaluated at compile time
    check("fn:for-each(1 to 9, function($x) {0*random:integer()+$x})",
        "1 2 3 4 5 6 7 8 9",
        "empty(//" + Util.className(FnForEach.class) + ')',
        "empty(*/" + Util.className(IntSeq.class) + ')',
        "count(//" + Util.className(Arith.class) + "[@op = '+']) eq 9");
    // should not be unrolled
    check("fn:for-each(0 to 9, function($x) {$x+1})",
        "1 2 3 4 5 6 7 8 9 10",
        "exists(//" + Util.className(FnForEach.class) + "[contains(@name, 'for-each')])");
  }
}
