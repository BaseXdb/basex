package org.basex.query.func;

import static org.basex.query.func.Function.*;

import org.basex.*;
import org.basex.query.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the Profiling Module.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ProfModuleTest extends SandboxTest {
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
    query("(1 to 2) !" + func.args(" .", "", true), "1\n2");
  }

  /** Test method. */
  @Test public void runtime() {
    final Function func = _PROF_RUNTIME;
    query(func.args() + " instance of map(*)", true);
    query(func.args("used") + " instance of xs:integer", true);
    query(func.args("total") + " instance of xs:integer", true);
    query(func.args("max") + " instance of xs:integer", true);
    query(func.args("processors") + " instance of xs:integer", true);
    error(func.args("x"), QueryError.PROF_OPTION_X);
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
    query("(1 to 2) !" + func.args(" .", "", true), "1\n2");
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
    query(func.args(" ('x' cast as enum('a', 'x'), 'y' cast as enum('b', 'y'))"), "x\ny");
  }

  /** Test method. */
  @Test public void variables() {
    final Function func = _PROF_VARIABLES;
    final String name = func.args().replace("()", "");

    // ensure that profiling leads to no unexpected errors (the debug output is not tested)
    query("for $x in 1 to 2 return " + func.args(), "");
    query(func.args() + ", let $x := " + _RANDOM_DOUBLE.args() + " return floor($x * $x)", 0);
    query(func.args() + ", let $x := " + wrap(1) + " return $x, " + func.args(), 1);
    query("fn { " + func.args() + "}(1)", "");

    query("function-lookup(xs:QName('" + name + "'), 0)()", "");
    query("function-lookup(xs:QName(" + wrap(name) + "), 0)()", "");

    query(func.args(" {}"), "");
    query("function-lookup(xs:QName('" + name + "'), 1)({})", "");
    query("function-lookup(xs:QName(" + wrap(name) + "), 1)({})", "");
    error(func.args(1), QueryError.INVCONVERT_X_X_X);
  }
}
