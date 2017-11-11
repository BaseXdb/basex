package org.basex.query.func;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.junit.*;

/**
 * This class tests the functions of the Profiling Module.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class ProfModuleTest extends AdvancedQueryTest {
  /** Test method. */
  @Test
  public void mem() {
    query(_PROF_MEM.args("()"));
    query("count(" + _PROF_MEM.args(" 1 to 100 ", false) + ")", 100);
    query("count(" + _PROF_MEM.args(" 1 to 100 ", true) + ")", 100);
    query("count(" + _PROF_MEM.args(" 1 to 100 ", true, "label") + ")", 100);
  }

  /** Test method. */
  @Test
  public void time() {
    query(_PROF_TIME.args("()"));
    query("count(" + _PROF_TIME.args(" 1 to 100 ", false) + ")", 100);
    query("count(" + _PROF_TIME.args(" 1 to 100 ", true) + ")", 100);
    query("count(" + _PROF_TIME.args(" 1 to 100 ", true, "label") + ")", 100);
  }

  /** Test method. */
  @Test
  public void sleep() {
    query(_PROF_SLEEP.args(" 10"));
    query(_PROF_SLEEP.args(" 1"));
    query(_PROF_SLEEP.args(" 0"));
    query(_PROF_SLEEP.args(" -1"));
  }

  /** Test method. */
  @Test
  public void human() {
    query(_PROF_HUMAN.args(" 1"), "1 b");
    query(_PROF_HUMAN.args(" 2"), "2 b");
    query(_PROF_HUMAN.args(" 512"), "512 b");
    query(_PROF_HUMAN.args(" 32768"), "32 kB");
    query(_PROF_HUMAN.args(" 1048576"), "1024 kB");
  }

  /** Test method. */
  @Test
  public void dump() {
    query(_PROF_DUMP.args("a"), "");
  }

  /** Test method. */
  @Test
  public void variables() {
    query("for $x in 1 to 2 return " + _PROF_VARIABLES.args(), "");
    query(_PROF_VARIABLES.args() + ", let $x := random:double() return floor($x * $x)", 0);
  }

  /** Test method. */
  @Test
  public void voidd() {
    query(_PROF_VOID.args(" ()"), "");
    query(_PROF_VOID.args(1), "");
    query(_PROF_VOID.args("1,2"), "");
  }

  /** Test method. */
  @Test
  public void type() {
    query(_PROF_TYPE.args(" ()"), "");
    query(_PROF_TYPE.args(1), 1);
    query(_PROF_TYPE.args(" (1, 2, 3)"), "1\n2\n3");
    query(_PROF_TYPE.args(" <x a='1' b='2' c='3'/>/@*/data()"), "1\n2\n3");
  }
}
