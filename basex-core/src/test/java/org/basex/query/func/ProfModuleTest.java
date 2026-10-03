package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.*;
import org.basex.query.*;
import org.basex.query.func.prof.ProfType.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
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
    query(func.args(" 1"), "1 B");
    query(func.args(" 512"), "512 B");
    query(func.args(" 1023"), "1023 B");
    query(func.args(" 1024"), "1 kB");
    query(func.args(" 1536"), "1.5 kB");
    query(func.args(" 1048575"), "1 MB");
    query(func.args(" 1048576"), "1 MB");
    query(func.args(" 1125899906842624"), "1 PB");
    query(func.args(" 2.5"), "2.5 B");
    query(func.args(" -2048"), "-2 kB");
    query(func.args(" -1.25"), "-1.3 B");
    query(func.args(" -0.01"), "0 B");
    query(func.args(" 0"), "0 B");
    query(func.args(" 1e20"), "86.7 EB");
    query(func.args(" xs:untypedAtomic('2048')"), "2 kB");
    query(func.args(" xs:double('INF')"), "INF");

    query(func.args(" seconds(3600)"), "1 h");
    query(func.args(" seconds(5400)"), "1.5 h");
    query(func.args(" seconds(59.99)"), "1 min");
    query(func.args(" seconds(0.00085)"), "850 µs");
    query(func.args(" seconds(0.0000000005)"), "0.5 ns");
    query(func.args(" seconds(172800)"), "2 d");
    query(func.args(" seconds(0)"), "0 s");
    query(func.args(" xs:dayTimeDuration('PT1.25S')"), "1.3 s");

    check(func.args(1536), "1.5 kB", root(Str.class));

    error(func.args(" 'x'"), NONUMBER_X_X);
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
    error(func.args("x"), QueryError.EXP_FOUND_X_X);
  }

  /** Test method. */
  @Test public void shrink() {
    final Function func = _PROF_SHRINK;
    checkType("(1, 1 to 8) => remove(1)",
        new TypeInfo(SubSeq.class, "xs:integer+", 8));
    checkType(func.args(" (1, 1 to 8) => remove(1)"),
        new TypeInfo(RangeSeq.class, "xs:integer+", 8));
    checkType(func.args(" (1, 1 to 8) => remove(2)"),
        new TypeInfo(RangeSeq.class, "xs:integer+", 8));
    checkType(func.args(" (1, 1 to 8) => remove(3)"),
        new TypeInfo(BytSeq.class, "xs:integer+", 8));
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
    query("exists(" + func.args("A") + "?allocated)", "true");
    query("count(" + func.args("A") + "?*)", 3);
    query("empty(" + func.args("A", " { 'memory': false(), 'time': false(), " +
        "'allocated': false(), 'value': false() }") + "?*)", "true");
    query(func.args("A") + "?allocated >= 0", "true");
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
    error(func.args(1), QueryError.INVTYPE_X);
  }
}
