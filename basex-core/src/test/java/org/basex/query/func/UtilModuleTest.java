package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.*;
import org.basex.query.expr.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the Utility Module.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class UtilModuleTest extends SandboxTest {
  /** Test method. */
  @Test public void arrayMember() {
    final Function func = _UTIL_ARRAY_MEMBER;

    query(func.args(" ()"), "[()]");
    query(func.args(" 1"), "[1]");
    query(func.args(" (1, 2)"), "[(1,2)]");
    query(func.args(" [1]"), "[[1]]");
    query(func.args(" [1, 2]"), "[[1,2]]");
    query(func.args(" [ (1, 2) ]"), "[[(1,2)]]");

    check("array { <_>x</_> }", "[<_>x</_>]", root(func));
    check("[ <_>x</_> ]", "[<_>x</_>]", root(func));
    check("[ (<_>x</_>, <_>y</_>) ]", "[(<_>x</_>,<_>y</_>)]", root(func));
    check("array { <_>x</_>, <_>y</_> }", "[<_>x</_>,<_>y</_>]", empty(func));
    check("array { <_>x</_>, <_>y</_> }", "[<_>x</_>,<_>y</_>]", empty(func));
  }

  /** Test method. */
  @Test public void countWithin() {
    final Function func = _UTIL_COUNT_WITHIN;

    // minimum
    query(func.args(" ()", -1), true);
    query(func.args(" ()", 0), true);
    query(func.args(" ()", 1), false);

    query(func.args(9, -1), true);
    query(func.args(9, 0), true);
    query(func.args(9, 1), true);
    query(func.args(9, 2), false);

    query(func.args(" <a/>", -1), true);
    query(func.args(" <a/>", 0), true);
    query(func.args(" <a/>", 1), true);
    query(func.args(" <a/>", 2), false);

    query(func.args(" trace(1)", 1), true);

    query(func.args(" (8, 9)", 1), true);
    query(func.args(" (8, 9)", 2), true);
    query(func.args(" (8, 9)", 3), false);

    check(func.args(" (8, 9, 9)[. = 9]", 0), true, root(Bln.class));
    check(func.args(" (8, 9, 9)[. = 9]", 1), true, root(Bln.class));
    query(func.args(" (8, 9, 9)[. = 9]", 2), true);
    query(func.args(" (8, 9, 9)[. = 9]", 3), false);
    query(func.args(" (8, 9, 9)[. = 9]", wrap(1)), true);
    query(func.args(" (8, 9, 9)[. = 9]", wrap(2)), true);
    query(func.args(" (8, 9, 9)[. = 9]", wrap(3)), false);

    // minimum and maximum
    query(func.args(" ()", 0, 0), true);
    query(func.args(" ()", 0, 1), true);
    query(func.args(" ()", 1, 0), false);

    query(func.args(9, 0, 0), false);
    query(func.args(9, 0, 1), true);
    query(func.args(9, 0, 1), true);
    query(func.args(9, 1, 2), true);
    query(func.args(9, 2, 1), false);

    query(func.args(" <a/>", 0, 0), false);
    query(func.args(" <a/>", 0, 1), true);
    query(func.args(" <a/>", 0, 1), true);
    query(func.args(" <a/>", 1, 2), true);
    query(func.args(" <a/>", 2, 1), false);

    query(func.args(" trace(1)", 1, 2), true);

    query(func.args(" (8, 9)", 1, 1), false);
    query(func.args(" (8, 9)", 1, 2), true);
    query(func.args(" (8, 9)", 2, 2), true);
    query(func.args(" (8, 9)", 2, 3), true);
    query(func.args(" (8, 9)", 3, 2), false);

    check(func.args(" (8, 9, 9)[. = 9]", 0, 0), false, root(Bln.class));
    query(func.args(" (8, 9, 9)[. = 9]", 1, 1), false);
    query(func.args(" (8, 9, 9)[. = 9]", 1, 2), true);
    query(func.args(" (8, 9, 9)[. = 9]", 2, 2), true);
    query(func.args(" (8, 9, 9)[. = 9]", 2, 3), true);
    check(func.args(" (8, 9, 9)[. = 9]", 3, 2), false, root(Bln.class));

    query(func.args(" (8, 9, 9)[. = 9]", 0, wrap(0)), false);
    query(func.args(" (8, 9, 9)[. = 9]", 1, wrap(1)), false);
    query(func.args(" (8, 9, 9)[. = 9]", 1, wrap(2)), true);
    query(func.args(" (8, 9, 9)[. = 9]", 2, wrap(2)), true);
    query(func.args(" (8, 9, 9)[. = 9]", 2, wrap(3)), true);
    query(func.args(" (8, 9, 9)[. = 9]", 3, wrap(2)), false);

    query(func.args(" (8, 9, 9)[. = 9]", wrap(0), 0), false);
    query(func.args(" (8, 9, 9)[. = 9]", wrap(1), 1), false);
    query(func.args(" (8, 9, 9)[. = 9]", wrap(1), 2), true);
    query(func.args(" (8, 9, 9)[. = 9]", wrap(2), 2), true);
    query(func.args(" (8, 9, 9)[. = 9]", wrap(2), 3), true);
    query(func.args(" (8, 9, 9)[. = 9]", wrap(3), 2), false);

    query(func.args(" (8, 9, 9)[. = 9]", wrap(0), wrap(0)), false);
    query(func.args(" (8, 9, 9)[. = 9]", wrap(1), wrap(1)), false);
    query(func.args(" (8, 9, 9)[. = 9]", wrap(1), wrap(2)), true);
    query(func.args(" (8, 9, 9)[. = 9]", wrap(2), wrap(2)), true);
    query(func.args(" (8, 9, 9)[. = 9]", wrap(2), wrap(3)), true);
    query(func.args(" (8, 9, 9)[. = 9]", wrap(3), wrap(2)), false);

    // simplified arguments
    check(func.args(" sort(<_/>)", wrap(0)), true, empty(SORT));
    check(func.args(" reverse(<_/>)", wrap(0)), true, empty(REVERSE));
    check(func.args(" for $i in 1 to 2 order by $i return $i", wrap(0)),
        true, empty(GFLWOR.class));

    // rewritings
    check("count((8, 9, 9)[. >= 9]) < 3", true, root(func));
    check("count((8, 9, 9)[. >= 9]) < 2.1", true, root(func));
    check("count((8, 9, 9)[. >= 9]) < 2", false, root(func));

    check("count((8, 9, 9)[. >= 9]) <= 2.1", true, root(func));
    check("count((8, 9, 9)[. >= 9]) <= 2", true, root(func));
    check("count((8, 9, 9)[. >= 9]) <= 1.9", false, root(func));

    check("count((8, 9, 9)[. >= 9]) > 1", true, root(func));
    check("count((8, 9, 9)[. >= 9]) > 1.1", true, root(func));
    check("count((8, 9, 9)[. >= 9]) > 1.9", true, root(func));
    check("count((8, 9, 9)[. >= 9]) > 2", false, root(func));
    check("count((8, 9, 9)[. >= 9]) > 2.1", false, root(func));

    check("count((8, 9, 9)[. >= 9]) >= 1.9", true, root(func));
    check("count((8, 9, 9)[. >= 9]) >= 2", true, root(func));
    check("count((8, 9, 9)[. >= 9]) >= 2.1", false, root(func));

    check("count((8, 9, 9)[. >= 9]) = 1", false, root(func));
    check("count((8, 9, 9)[. >= 9]) = 2", true, root(func));
    check("count((8, 9, 9)[. >= 9]) = 3", false, root(func));

    check("count((1 to 10)[. < 5]) = 1 to 3", false, root(func));
    check("count((1 to 10)[. < 5]) = 3 to 5", true, root(func));
    check("count((1 to 10)[. < 5]) = 5 to 7", false, root(func));

    check("(1 to 2) ! (count((1 to 10)[. < 5]) = .)", "false\nfalse", exists(func));

    // merge multiple counts
    check("let $s := (1 to 6)[. < 5] return empty($s) or count($s) < 5", true,
        root(func), count(Int.class, 2), empty(EMPTY));
    check("let $s := (1 to 6)[. < 5] return exists($s) and count($s) < 5", true,
        root(func), count(Int.class, 2), empty(EXISTS));

    check("let $s := (1 to 6)[. < 5] return count($s) > 0 and count($s) < 5", true,
        root(func), count(Int.class, 2), empty(COUNT));
    check("let $s := (1 to 6)[. < 5] return count($s) >= 0 and count($s) < 5", true,
        root(func), count(Int.class, 2), empty(COUNT));
    check("let $s := (1 to 6)[. < 5] return count($s) > 1 and count($s) > 2", true,
        root(func), count(Int.class, 1), empty(COUNT));
    check("let $s := (1 to 6)[. < 5] return count($s) > 1 and count($s) > 2", true,
        root(func), count(Int.class, 1), empty(COUNT));
    check("let $s := (1 to 6)[. < 5] return count($s) > 1 or count($s) > 2", true,
        root(func), count(Int.class, 1), empty(COUNT));

    check("let $s := (1 to 6)[. < 5] return count($s) < 5 or count($s) > 5", true,
        count(_UTIL_COUNT_WITHIN, 2), empty(COUNT));

    check("let $s := (1 to 6)[. < 5] return count($s) > 0 or count($s) < 5", true,
        root(Bln.class), empty(COUNT));
    check("let $s := (1 to 6)[. < 5] return count($s) > 0 or count($s) > 2", true,
        root(EXISTS), empty(COUNT));
    check("let $s := (1 to 6)[. < 5] return empty($s) and count($s) < 5", false,
        root(EMPTY), empty(COUNT));

    check(_UTIL_COUNT_WITHIN.args(" (1 to 100)[. > 90] ! <_>{ . }</_>", 8, 10), true,
        empty(SimpleMap.class));
  }

  /** Test method. */
  @Test public void mapKeyAt() {
    final Function func = _UTIL_MAP_KEY_AT;
    query(func.args(" {}", 0), "");
    query(func.args(" {}", 1), "");

    final String map = " { 1: 'uno', 10: 'dieci', 100: 'cento' }";
    query(func.args(map, 0), "");
    query(func.args(map, 1), 1);
    query(func.args(map, 2), 10);
    query(func.args(map, 3), 100);
    query(func.args(map, 4), "");
    query(func.args(map, 2147483647L), "");
    query(func.args(map, 2147483648L), "");
    query(func.args(map, 4294967295L), "");
    query(func.args(map, 4294967296L), "");
  }

  /** Test method. */
  @Test public void mapPutAt() {
    final Function func = _UTIL_MAP_PUT_AT;
    query(func.args(" { 9: 8 }", 1, 0), "{9:0}");
    query(func.args(" { 9: 8, 7: 6 }", 0, 0), "{9:8,7:6}");
    query(func.args(" { 9: 8, 7: 6 }", 1, 0), "{9:0,7:6}");
    query(func.args(" { 9: 8, 7: 6 }", 2, 0), "{9:8,7:0}");
    query(func.args(" { 9: 8, 7: 6 }", 3, 0), "{9:8,7:6}");

    query(func.args(" { '9': 8, '7': 6 }", 0, 0), "{\"9\":8,\"7\":6}");
    query(func.args(" { '9': 8, '7': 6 }", 1, 0), "{\"9\":0,\"7\":6}");
    query(func.args(" { '9': 8, '7': 6 }", 2, 0), "{\"9\":8,\"7\":0}");
    query(func.args(" { '9': 8, '7': 6 }", 3, 0), "{\"9\":8,\"7\":6}");

    query(func.args(" { '9': 8, '7': 6 }", 0, "a"), "{\"9\":8,\"7\":6}");
    query(func.args(" { '9': 8, '7': 6 }", 1, "a"), "{\"9\":\"a\",\"7\":6}");
    query(func.args(" { '9': 8, '7': 6 }", 2, "a"), "{\"9\":8,\"7\":\"a\"}");
    query(func.args(" { '9': 8, '7': 6 }", 3, "a"), "{\"9\":8,\"7\":6}");

    query("map:build(1 to 100) => " + func.args(1, 0) + " => map:get(1)", 0);
    query("map:build((1 to 100) ! string()) => " + func.args(1, 0) + " => map:get('1')", 0);
    query("map:build((1 to 100) ! string()) => " + func.args(1, "0") + " => map:get('1')", 0);
  }

  /** Test method. */
  @Test public void mapValueAt() {
    final Function func = _UTIL_MAP_VALUE_AT;
    query(func.args(" {}", 0), "");
    query(func.args(" {}", 1), "");

    final String map = " { 1: 'uno', 10: 'dieci', 100: 'cento' }";
    query(func.args(map, 0), "");
    query(func.args(map, 1), "uno");
    query(func.args(map, 2), "dieci");
    query(func.args(map, 3), "cento");
    query(func.args(map, 4), "");
    query(func.args(map, 2147483647L), "");
    query(func.args(map, 2147483648L), "");
    query(func.args(map, 4294967295L), "");
    query(func.args(map, 4294967296L), "");
  }

  /** Test method. */
  @Test public void range() {
    final Function func = _UTIL_RANGE;
    query(func.args(" ()", 1, 2), "");
    query(func.args(1, 1, 2), 1);
    query(func.args(1, 0, 2), 1);
    query(func.args(1, 2, 2), "");
    query(func.args(" 1 to 2", 2, 2), 2);
    query(func.args(" 1 to 2", 3, 2), "");
    query(func.args(" 1 to 2", 0, 2), "1\n2");
    query(func.args(" 1 to 2", -1, 2), "1\n2");
    query(func.args(" 1 to 2", 1.5, 2), 2);

    query("for $i in 1 to 2 return " + func.args(" $i", 1, 2), "1\n2");
    query(func.args(" (<a/>, <b/>)", 0, 2), "<a/>\n<b/>");
    query(func.args(" (<a/>, <b/>)", 1, 2), "<a/>\n<b/>");
    query(func.args(" (<a/>, <b/>)", 2.5, 3.5), "");
    query(func.args(" (<a/>, <b/>)", 3, 4), "");

    query(func.args(" (<a/>, <b/>)[name()]", 1, 9223372036854775807L), "<a/>\n<b/>");
  }

  /** Test method. */
  @Test public void stripNamespaces() {
    final Function func = _UTIL_STRIP_NAMESPACES;

    query(func.args(" <x/>"), "<x/>");
    query(func.args(" <x/>", " ()"), "<x/>");
    query(func.args(" <x/>", " ('')"), "<x/>");

    query(func.args(" document { }"), "");
    query(func.args(" text { 'T' }"), "T");
    query(func.args(" comment { 'C' }"), "<!--C-->");
    query(func.args(" attribute N { 'V' }"), "N=\"V\"");
    query(func.args(" processing-instruction N { 'V' }"), "<?N V?>");

    query(func.args(" <x xmlns='G'/>"), "<x/>");
    query(func.args(" <x xmlns='G'/>", " ()"), "<x/>");
    query(func.args(" <x xmlns='G'/>", ""), "<x/>");

    query(func.args(" <l:x xmlns:l='L'/>"), "<x/>");
    query(func.args(" <l:x xmlns:l='L'/>", " ()"), "<x/>");
    query(func.args(" <l:x xmlns:l='L'/>", "l"), "<x/>");
    query(func.args(" <l:x xmlns:l='L'/>", "l") + " => in-scope-prefixes()", "xml");
    query(func.args(" <l:x xmlns:l='L'/>", "") + " => in-scope-prefixes()", "l\nxml");

    query(func.args(" <l:x xmlns:l='L' l:a=''/>", "l"), "<x a=\"\"/>");
    query(func.args(" <l:x xmlns:l='L' l:a='' l:b=''/>", "l"), "<x a=\"\" b=\"\"/>");

    query(func.args(" <l:x xmlns='G' xmlns:l='L'/>"), "<x/>");
    query(func.args(" <l:x xmlns='G' xmlns:l='L'/>", "l"), "<x/>");

    query(func.args(" <x xmlns=''><y xmlns=''/></x>", ""), "<x><y/></x>");
    query(func.args(" <x xmlns=''><l:y xmlns:l='L'/></x>", ""), "<x><l:y xmlns:l=\"L\"/></x>");
    query(func.args(" <x xmlns:x='L' x:a=''><y x:a=''/></x>") + "/y", "<y a=\"\"/>");

    error(func.args(" <_ xmlns:l='L' l:a='' a=''/>"), BASEX_STRIP_X);
    error(func.args(" <_ xmlns:l='L' a='' l:a=''/>"), BASEX_STRIP_X);
  }

  /** Test method. */
  @Test public void valuesExcept() {
    final Function func = _UTIL_VALUES_EXCEPT;
    query(func.args(" ()", " ()"), "");
    query(func.args(1, " ()"), 1);
    query(func.args(1, 1), "");
    query(func.args("a", ""), "a");
    query(func.args("a", "a"), "");

    query(func.args(" (1, 2)", " ()"), "1\n2");
    query(func.args(" (1, 2)", " (1)"), 2);
    query(func.args(" (1, 2)", " (1, 2)"), "");
    query(func.args(" (1, 2)", " (1e0)"), 2);
    query(func.args(" (1, 2)", " (1.0)"), 2);
    query(func.args(" (1, 2)", " (1.1)"), "1\n2");
    query(func.args(" (1, 2)", " (3)"), "1\n2");
    query(func.args(" (1, 2)", " (3.1)"), "1\n2");

    query(func.args(" (1)", " (1 to 5)"), "");
    query(func.args(" (1e0)", " (1 to 5)"), "");
    query(func.args(" (1.0)", " (1 to 5)"), "");
    query(func.args(" (1.1)", " (1 to 5)"), "1.1");

    query(func.args(" (1 to 1000000)", " (1 to 1000000)"), "");
    query(func.args(" (1 to 1000000)", " (1 to 999999)"), 1000000);
    query(func.args(" (1 to 1000000)", " (2 to 1000000)"), 1);

    query(func.args(" (1 to 6) ! string()", " (1 to 6)"), "1\n2\n3\n4\n5\n6");
    query(func.args(" (5,4, 3, 2, 1, true#0)[not(. instance of fn(*))]", 1), "5\n4\n3\n2");

    check(func.args(" (1 to 6)", " ()"), "1\n2\n3\n4\n5\n6", empty(func), root(RangeSeq.class));
    check(func.args(" (1 to 6) ! string()", " ()"), "1\n2\n3\n4\n5\n6", empty(func));
    check(func.args(" (1 to 6) ! data(attribute _ { . })", " ()"), "1\n2\n3\n4\n5\n6",
        empty(func), exists(DATA));

    final String c = "http://www.w3.org/2005/xpath-functions/collation/html-ascii-case-insensitive";
    query(func.args("a", "A"), "a");
    query(func.args("a", "A", c), "");
    query(func.args("A", "a", c), "");
  }
}
