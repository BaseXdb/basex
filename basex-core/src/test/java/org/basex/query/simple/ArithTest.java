package org.basex.query.simple;

import static org.basex.query.QueryError.*;

import org.basex.*;
import org.basex.query.expr.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.junit.jupiter.api.*;

/**
 * Arithmetic tests.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArithTest extends SandboxTest {
  /** Test method. */
  @Test public void plus() {
    check("for $i in (1 to 2)[. != 0] return ($i * $i) + 1", "2\n5", exists(ArithSimple.class));

    // neutral number
    check("for $i in (1 to 2)[. != 0] return $i + 0",
        "1\n2", empty(ArithSimple.class), empty(GFLWOR.class));
    check("for $i in (1 to 2)[. != 0] return 0 + $i",
        "1\n2", empty(ArithSimple.class), empty(GFLWOR.class));
    check("for $i in (1 to 2)[. != 0] return 0e0 + $i",
        "1\n2", exists(Cast.class));
    check("for $i in (1 to 2)[. != 0] return $i + 0e0",
        "1\n2", exists(Cast.class));

    // counts
    check("let $n := " + wrap(1) + "[. = 1] return count($n) + count($n)",
        2, count(Function.COUNT, 1), exists(Function.REPLICATE));
    check("let $n := " + wrap(1) + "[. = 1] return count($n) + count($n) > 0",
        true, root(CmpSimpleG.class));

    check(wrap(3) + "! (. + .)", 6, exists(Itr.class), count(ArithSimple.class, 1));
    check(wrap(3) + "! (. + . + .)", 9, exists(Itr.class), count(ArithSimple.class, 1));
    check(wrap(3) + "! (. + -.)", 0, empty(Unary.class));
    check("xs:decimal(" + wrap(3) + ") ! (. + -.)", 0, empty(Unary.class), root(Dec.class));
  }

  /** Test method. */
  @Test public void minus() {
    check("for $i in (1 to 2)[. != 0] return ($i * $i) - 1", "0\n3", exists(ArithSimple.class));

    // neutral number
    check("for $i in (1 to 2)[. != 0] return $i - 0",
        "1\n2", empty(ArithSimple.class), empty(GFLWOR.class));
    check("for $i in (1 to 2)[. != 0] return $i - 0e0",
        "1\n2", exists(Cast.class));
    check("for $i in (1 to 2)[. != 0] return 0 - $i",
        "-1\n-2", exists(Unary.class));

    // identical arguments
    check("for $i in (1, xs:double('NaN'))[. != 0] return $i - $i",
        "0\nNaN", exists(Arith.class));
    check("for $i in (1 to 2)[. != 0] return $i - $i",
        "0\n0", empty(Arith.class), empty(ArithSimple.class), empty(GFLWOR.class));

    query("string(xs:dateTime('2017-07-07T18:30:00.1') - xs:dayTimeDuration('PT1S'))",
        "2017-07-07T18:29:59.1");
    query("string(xs:dateTime('2017-07-07T18:00:59.1') - xs:dayTimeDuration('PT1M'))",
        "2017-07-07T17:59:59.1");

    check("xs:decimal(" + wrap(3) + ") ! (. - .)", 0, root(Dec.class));
    check(wrap(3) + "! (. + . - .)", 3, exists(Cast.class), empty(ArithSimple.class));
    check(wrap(3) + "! (. - -.)", 6, exists(Itr.class), empty(Unary.class),
        count(ArithSimple.class, 1));
  }

  /** Test method. */
  @Test public void mult() {
    check("for $i in (1 to 2)[. != 0] return $i * 2", "2\n4", exists(ArithSimple.class));

    // neutral number
    check("for $i in (1 to 2)[. != 0] return $i * 1",
        "1\n2", empty(ArithSimple.class), empty(GFLWOR.class));
    check("for $i in (1 to 2)[. != 0] return 1 * $i",
        "1\n2", empty(ArithSimple.class), empty(GFLWOR.class));
    check("for $i in (1 to 2)[. != 0] return 1e0 * $i",
        "1\n2", exists(Cast.class));
    check("for $i in (1 to 2)[. != 0] return $i * 1e0",
        "1\n2", exists(Cast.class));

    // absorbing number
    check("for $i in (1 to 2)[. != 0] return $i * 0",
        "0\n0", empty(ArithSimple.class), empty(GFLWOR.class));
    check("for $i in (1 to 2)[. != 0] return 0 * $i",
        "0\n0", empty(ArithSimple.class), empty(GFLWOR.class));
    check("for $i in (1 to 2)[. != 0] return 0e0 * $i",
        "0\n0", exists(ArithSimple.class));
    check("for $i in (1 to 2)[. != 0] return $i * 0e0",
        "0\n0", exists(ArithSimple.class));

    check(wrap(3) + "! (. * .)", 9, exists(Dbl.class), exists(Function._MATH_POW));
    check(wrap(3) + "! (. * . * .)", 27, exists(Dbl.class), count(Function._MATH_POW, 1));
    check(wrap(3) + "! (. * (1 div .))", 1, root(Dbl.class));
  }

  /** Test method. */
  @Test public void div() {
    check("for $i in (2, 4)[. != 0] return $i div 2", "1\n2", exists(ArithSimple.class));
    check("for $i in (2, 4)[. != 0] return 1 div $i", "0.5\n0.25", exists(ArithSimple.class));

    // neutral number
    check("for $i in (2.0, 4.0) return $i div 1", "2\n4",
        empty(ArithSimple.class), empty(GFLWOR.class));
    check("for $i in (2e0, 4e0) return $i div 1", "2\n4",
        empty(ArithSimple.class), empty(GFLWOR.class));
    check("for $i in (2, 4)[. != 0] return $i div 1e0", "2\n4",
        exists(Cast.class));

    // identical arguments
    check("for $i in (1, xs:double('NaN'))[. != 0] return $i div $i",
        "1\nNaN", exists(Arith.class));
    check("for $i in (2.0, 4.0)[. != 0] return $i div $i", "1\n1",
        empty(Arith.class), empty(ArithSimple.class), empty(GFLWOR.class));

    error("xs:dayTimeDuration('PT0S') div xs:dayTimeDuration('PT0S')", DIVZERO_X);
    error("xs:yearMonthDuration('P0M') div xs:yearMonthDuration('P0M')", DIVZERO_X);

    check("xs:decimal(" + wrap(3) + ") ! (. div .)", 1, root(Dec.class));
    check(wrap(3) + "! (. * . div .)", 3, exists(Cast.class), empty(ArithSimple.class));

    // decimals: at least 18 fractional and 18 significant digits
    query("1 div 3.0", "0.333333333333333333");
    query("10 div 3.0", "3.333333333333333333");
    query("6.022 div 100000000000000000000000.0", "0.00000000000000000000006022");
    query("1 div 300000000000000000000.0", "0.00000000000000000000333333333333333333");
  }

  /** Test method. */
  @Test public void idiv() {
    check("for $i in (2, 4)[. != 0] return $i idiv 2", "1\n2", exists(ArithSimple.class));
    check("for $i in (2, 4)[. != 0] return 1 idiv $i", "0\n0", exists(ArithSimple.class));

    // neutral number
    check("for $i in (2, 4) return $i idiv 1e0", "2\n4",
        empty(ArithSimple.class), empty(GFLWOR.class));
    check("for $i in (2, 4) return $i idiv 1", "2\n4",
        empty(ArithSimple.class), empty(GFLWOR.class));
    check("for $i in (2, 4) return $i idiv 1", "2\n4",
        empty(ArithSimple.class), empty(GFLWOR.class));

    // identical arguments
    error("for $i in (1, xs:double('NaN')) return $i idiv $i", INVIDIV_X);
    check("for $i in (2, 4) return $i idiv $i", "1\n1",
        empty(ArithSimple.class), empty(GFLWOR.class));

    check("xs:decimal(" + wrap(3) + ") ! (. idiv .)", 1, root(Itr.class));

    // GH-2111
    check("xs:float  (1.13) idiv xs:float  (1.13)", 1, root(Itr.class));
    check("xs:double (1.13) idiv xs:double (1.13)", 1, root(Itr.class));
    check("xs:double (1.13) idiv xs:float  (1.13)", 1, root(Itr.class));
    check("xs:float  (1.13) idiv xs:double (1.13)", 0, root(Itr.class));
  }

  /** Test method. */
  @Test public void mod() {
    check("for $i in (-1, 0, 1) return $i mod 1", "0\n0\n0", empty(ArithSimple.class));
  }

  /** Merge arithmetic expressions. */
  @Test public void gh1938() {
    check("(" + wrap(1) + "+ 1 - 1)[. instance of xs:double]", 1, root(Cast.class));
    check(wrap(6) + "div 3 * 2", 4, count(ArithSimple.class, 1));
    check("(1 to 2) ! (" + wrap(1) + "+ . - .)", "1\n1", empty(ArithSimple.class));
    check("(1 to 2) ! (" + wrapContext() + "+ . - .)", "1\n2", empty(ArithSimple.class));
    check("(1 to 6) ! (. + 1 - .)", "1\n1\n1\n1\n1\n1",
        empty(ArithSimple.class), root(SingletonSeq.class));
  }

  /** Simplify arithmetic expressions. */
  @Test public void simplify() {
    final String i = "xs:integer(" + wrap(1) + ")";
    check(i + "- 1 = 0", true, empty(ArithSimple.class), count(Itr.class, 1));
    check(i + "- 1 = " + i + " - 1", true, empty(ArithSimple.class), empty(Itr.class));
    check(i + "- 1 != " + i + " - 2", true, count(ArithSimple.class, 1), count(Itr.class, 1));

    // untyped values: no rewrite (double arithmetic, decimal comparison)
    check(wrap(1) + "- 1 = 0", true, exists(ArithSimple.class));
    query(wrap("1.00000000000000001") + "- 1 = 0", true);
    query(wrap("1.00000000000000001") + "- 1 = " + wrap(1) + " - 1", true);

    // mixed types: no rewrite
    query("some $v in (1 to 20) ! (. div 10) satisfies $v = 0.1e0", false);
    query("(1 to 20)[. div 10 = 0.1e0]", "");
    query("(1 to 20)[. div 10 = 0.1]", 1);
    // no rewrite if the new operand exceeds the integer range
    query("(1 to 3)[. + 1 = -9223372036854775808]", "");
    query("(1 to 3)[. div 2 = 9223372036854775807]", "");
  }

  /** Error in arithmetic calculation result comparison. */
  @Test public void gh2188() {
    check("<x _='1'/>/@* *  2 >= -2", true, empty(ArithSimple.class));
    check("<x _='1'/>/@* *  2 >  -2", true, empty(ArithSimple.class));
    check("<x _='1'/>/@* *  2 <= -2", false, empty(ArithSimple.class));
    check("<x _='1'/>/@* *  2 <  -2", false, empty(ArithSimple.class));
    check("<x _='1'/>/@* *  2  = -2", false, empty(ArithSimple.class));
    check("<x _='1'/>/@* *  2 != -2", true, empty(ArithSimple.class));

    check("<x _='1'/>/@* * -2 >= 2", false, exists(Arith.class));
    check("<x _='1'/>/@* * -2 >  2", false, exists(Arith.class));
    check("<x _='1'/>/@* * -2 <= 2", true, exists(Arith.class));
    check("<x _='1'/>/@* * -2 <  2", true, exists(Arith.class));
    check("<x _='1'/>/@* * -2  = 2", false, exists(Arith.class));
    check("<x _='1'/>/@* * -2 != 2", true, exists(Arith.class));
    check("<x _='1'/>/@* ! xs:integer(.) * -2  = 2", false, empty(Arith.class));
    check("<x _='1'/>/@* ! xs:integer(.) * -2 != 2", true, empty(Arith.class));
    query("<x _='-1.00000000000000001'/>/@* * -2 = 2", true);
  }

  /** Unexpected exception, division by zero. */
  @Test public void gh2189() {
    check("<x _='1'/>/@* * 0  = 0", true, exists(Arith.class));
    check("<x _='1'/>/@* * 0  > 0", false, exists(Arith.class));
    check("<x _='1'/>/@* * 0  = 0", true, exists(Arith.class));
    check("<x _='1'/>/@* * 0 != 0", false, exists(Arith.class));
  }

  /** Number literals. */
  @Test public void literals() {
    query("1.+1.", 2);
  }

  /** Numeric limits. */
  @Test public void limits() {
    query("2 * 4611686018427387903", 9223372036854775806L);
    error("2 * 4611686018427387904", RANGE_X);
    query("-2 * 4611686018427387904", -9223372036854775808L);
    error("4611686018427387905 * -2", RANGE_X);
    query("xs:decimal('18446744073709551616') idiv -2", -9223372036854775808L);
    error("xs:decimal('18446744073709551616') idiv 2", RANGE_X);
    query("xs:decimal('18446744073709551612') idiv 2", 9223372036854775806L);
    error("-9223372036854775808 idiv -1", RANGE_X);
    error("-9223372036854775807 - 1024", RANGE_X);
    error("-9223372036854775808 - 1", RANGE_X);

    // unsigned long values beyond the integer range
    error("xs:unsignedLong('18446744073709551615')", INTRANGE_X);
    error("xs:unsignedLong('9223372036854775808')", INTRANGE_X);
    final String u = "xs:unsignedLong('9223372036854775807')";
    error(u + " + 1", RANGE_X);
    query("+" + u, Long.MAX_VALUE);
    query("-" + u, -Long.MAX_VALUE);
    error("round(" + u + ", -1)", RANGE_X);

    // integer range comparisons
    final String e = "(9223372036854775806, 9223372036854775807, -9223372036854775808)[. != 0]";
    query("count(" + e + "[. > 9223372036854775807])", 0);
    query("count(" + e + "[. = 9223372036854775806])", 1);
    query("count(" + e + "[. < -9223372036854775808])", 0);
    query("count(" + e + "[. <= 9223372036854775807])", 3);
  }

  /** Arithmetics with durations. */
  @Test public void durations() {
    query("string(.5 * xs:yearMonthDuration('P1Y'))", "P6M");
    query("string(<_>.5</_> * xs:yearMonthDuration('P1Y'))", "P6M");
    query("string(xs:yearMonthDuration('P1Y') * .5)", "P6M");
    query("string(xs:yearMonthDuration('P1Y') * <_>.5</_>)", "P6M");
    query("string(xs:yearMonthDuration('P1Y') div .5)", "P2Y");
    query("string(xs:yearMonthDuration('P1Y') div <_>.5</_>)", "P2Y");

    // overflow
    query("string(xs:dayTimeDuration('PT1S') * 1e18)", "P11574074074074DT1H46M40S");
    error("xs:dayTimeDuration('PT1S') * 1e300", SECDURRANGE_X);
    error("xs:dayTimeDuration('PT1S') div 1e-300", SECDURRANGE_X);

    // exact limits
    final String ym = "xs:yearMonthDuration('P9223372036854775807M')";
    query("string(" + ym + ")", "P768614336404564650Y7M");
    query("string(xs:yearMonthDuration('P768614336404564649Y'))", "P768614336404564649Y");
    error("xs:yearMonthDuration('P768614336404564650Y8M')", DURRANGE_X_X);
    query("string(" + ym + " + xs:yearMonthDuration('P0M'))", "P768614336404564650Y7M");
    error(ym + " + xs:yearMonthDuration('P1M')", MONTHRANGE_X);
    error("xs:yearMonthDuration('-P9223372036854775807M') - xs:yearMonthDuration('P1M')",
        MONTHRANGE_X);
    final String dt = "xs:dayTimeDuration('PT9223372036854775807S')";
    query("string(" + dt + ")", "P106751991167300DT15H30M7S");
    error("xs:dayTimeDuration('PT9223372036854775807.5S')", DURRANGE_X_X);
    query("string(xs:dayTimeDuration('PT9223372036854775806S') + xs:dayTimeDuration('PT1S'))",
        "P106751991167300DT15H30M7S");
    error(dt + " + xs:dayTimeDuration('PT1S')", SECDURRANGE_X);
  }
}
