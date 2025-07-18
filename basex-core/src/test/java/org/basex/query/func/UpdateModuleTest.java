package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the Update Module.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class UpdateModuleTest extends SandboxTest {
  /** Resets optimizations. */
  @AfterEach public void init() {
    inline(false);
    unroll(false);
  }

  /** Test method. */
  @Test public void apply() {
    final Function func = _UPDATE_APPLY;
    query(func.args(" update:output#1", " [1]"), 1);
    query(func.args(" void#1", " [1]"), "");
    error(func.args(" count#1", " [1]"), FUNCNOTUP_X);
  }

  /** Test method. */
  @Test public void cache() {
    final Function func = _UPDATE_CACHE;
    query(func.args(), "");
    query(_UPDATE_OUTPUT.args(func.args()), "");
    query(_UPDATE_OUTPUT.args("x") + ',' + _UPDATE_OUTPUT.args(func.args()), "x\nx");
    query(_UPDATE_OUTPUT.args("x") + ',' + _UPDATE_OUTPUT.args("x") + ',' +
        _UPDATE_OUTPUT.args(func.args()), "x\nx\nx\nx");
  }

  /** Test method. */
  @Test public void forEach() {
    final Function func = _UPDATE_FOR_EACH;

    query(func.args(1, " update:output#1"), 1);
    query(func.args(" 1[. = 0]", " update:output#1"), "");
    query(func.args(" (1, 2)[. = 0]", " update:output#1"), "");
    query(func.args(1, " void#1"), "");

    inline(true);
    query(func.args(1, " update:output#1"), 1);
    query(func.args(" 1[. = 0]", " update:output#1"), "");
    query(func.args(" (1, 2)[. = 0]", " update:output#1"), "");
    query(func.args(1, " void#1"), "");

    error(func.args(1, " count#1"), FUNCNOTUP_X);
  }

  /** Test method. */
  @Test public void forEachPair() {
    final Function func = _UPDATE_FOR_EACH_PAIR;
    query(func.args(1, 2, " function($a, $b) {" + _UPDATE_OUTPUT.args(" $a + $b") + " }"), 3);
    query(func.args(1, 2, " function($a, $b) {" + VOID.args(" $a + $b") + " }"), "");
    error(func.args("A", "B", " compare#2"), FUNCNOTUP_X);
  }

  /** Test method. */
  @Test public void mapForEach() {
    final Function func = _UPDATE_MAP_FOR_EACH;
    query(func.args(" { 1: 2 }", " %updating function($a, $b) {"
        + _UPDATE_OUTPUT.args(" $a + $b") + " }"), 3);
    query(func.args(" { 1: 2 }", " function($a, $b) {"
        + VOID.args(" $a + $b") + " }"), "");
    error(func.args(" { 1: 2 }", " compare#2"), FUNCNOTUP_X);
  }

  /** Test method. */
  @Test public void output() {
    final Function func = _UPDATE_OUTPUT;
    query(func.args("x"), "x");
    query(func.args(" ('x', 'y')"), "x\ny");
    query(func.args(" <a/>"), "<a/>");
    error(func.args("x") + ", 1", UPALL);
    error(func.args(" count#1"), BASEX_STORE_X);
    error("copy $c := <a/> modify " + func.args("x") + " return $c", BASEX_UPDATE);
  }
}
