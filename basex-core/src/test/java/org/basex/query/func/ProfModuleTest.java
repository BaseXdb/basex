package org.basex.query.func;

import static org.basex.query.func.Function.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the Profiling Module.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ProfModuleTest extends SandboxTest {
  /** Test method. */
  @Test public void dump() {
    final Function func = _PROF_DUMP;
    query(func.args("a"), "");
  }

  /** Test method. */
  @Test public void human() {
    final Function func = _PROF_HUMAN;
    query(func.args(" 1"), "1 b");
    query(func.args(" 2"), "2 b");
    query(func.args(" 512"), "512 b");
    query(func.args(" 32768"), "32 kB");
    query(func.args(" 1048576"), "1024 kB");
  }

  /** Test method. */
  @Test public void memory() {
    final Function func = _PROF_MEMORY;
    query(func.args(" ()"));
    query("count(" + func.args(" 1 to 100 ") + ")", 100);
    query("count(" + func.args(" 1 to 100 ", "label") + ")", 100);
  }

  /** Test method. */
  @Test public void sleep() {
    final Function func = _PROF_SLEEP;
    query(func.args(" 10"));
    query(func.args(" 1"));
    query(func.args(" 0"));
    query(func.args(" -1"));
  }

  /** Test method. */
  @Test public void time() {
    final Function func = _PROF_TIME;
    query(func.args(" ()"));
    query("count(" + func.args(" 1 to 100 ") + ")", 100);
    query("count(" + func.args(" 1 to 100 ", "label") + ")", 100);
  }

  /** Test method. */
  @Test public void track() {
    final Function func = _PROF_TRACK;
    query(func.args(" ()"));
    query("exists(" + func.args("A") + "?memory)", "false");
    query("exists(" + func.args("A") + "?time)", "true");
    query("exists(" + func.args("A") + "?value)", "true");
    query("count(" + func.args("A") + "?*)", 2);
    query("empty(" + func.args("A",
        " map { 'memory': false(), 'time': false(), 'value': false() }") + "?*)", "true");
  }

  /** Test method. */
  @Test public void type() {
    final Function func = _PROF_TYPE;
    query(func.args(" ()"), "");
    query(func.args(1), 1);
    query(func.args(" (1, 2, 3)"), "1\n2\n3");
    query(func.args(" <x a='1' b='2' c='3'/>/@*/data()"), "1\n2\n3");
  }

  /** Test method. */
  @Test public void variables() {
    final Function func = _PROF_VARIABLES;
    query("for $x in 1 to 2 return " + func.args(), "");
    query(func.args() + ", let $x := random:double() return floor($x * $x)", 0);
  }

  /** Test method. */
  @Test public void voidd() {
    final Function func = _PROF_VOID;
    query(func.args(" ()"), "");
    query(func.args(1), "");
    query(func.args("1,2"), "");
  }
}
