package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.basex.query.*;
import org.basex.query.expr.constr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the Map Module.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class MapModuleTest extends SandboxTest {
  /** Months. */
  private static final String MONTHS = " ('January', 'February', 'March', 'April', 'May', "
      + "'June', 'July', 'August', 'September', 'October', 'November', 'December')";

  /** Test method. */
  @Test public void build() {
    final Function func = _MAP_BUILD;

    query(func.args(" ()", " boolean#1"), "map{}");
    query(func.args(" 0", " boolean#1"), "map{false():0}");
    query(func.args(" 1", " boolean#1"), "map{true():1}");
    query(func.args(" (0, 1)", " boolean#1") + " => map:size()", 2);
    query(func.args(" (0, 1)", " function($i) { boolean($i)[.] }"), "map{true():1}");

    query(func.args(" (1 to 100)", " function($i) { }"), "map{}");
    query(func.args(" (1 to 100)", " boolean#1") + " => map:size()", 1);
    query(func.args(" (1 to 100)", " string#1") + " => map:size()", 100);
    query(func.args(" (1 to 100)", " function($i) { $i mod 10 }") + " => map:size()", 10);

    query(func.args(MONTHS, " string-length#1") + " => map:size()", 7);
    query(func.args(" (1 to 100)", " function($i) { $i mod 10 }") + " => map:size()", 10);
    query(func.args(" <xml>{ (1 to 9) ! <sub>{ . }</sub> }</xml>/*", " string-length#1")
        + " => map:keys()", 1);
    query("for $f in (true#0, false#0, concat#2, substring#2, contains#2, identity#1)"
        + "[function-arity(.) = 1] return " + func.args(5, " $f"), "map{5:5}");
    query("for $f in (1, 2, 3, 4, string#1, 6)"
        + "[. instance of function(*)] return " + func.args(8, " $f"), "map{\"8\":8}");
  }

  /** Test method. */
  @Test public void contains() {
    final Function func = _MAP_CONTAINS;
    query(func.args(" map { }", 1), false);
    query(func.args(_MAP_ENTRY.args(1, 2), 1), true);
  }

  /** Test method. */
  @Test public void entries() {
    final Function func = _MAP_ENTRIES;
    query(func.args(" map {}"), "");
    query(func.args(" map { 1: 2 }") + " !" + _MAP_KEYS.args(" ."), 1);
    query(func.args(" map { 1: 2 }") + " !" + _MAP_VALUES.args(" ."), 2);
    query(func.args(" map { 1: (2, 3) }") + " !" + _MAP_KEYS.args(" ."), 1);
    query(func.args(" map { 1: (2, 3) }") + " !" + _MAP_VALUES.args(" ."), "2\n3");
    query(func.args(" map { 1: 2, 3: 4 }") + " !" + _MAP_KEYS.args(" ."), "1\n3");
    query(func.args(" map { 1: 2, 3: 4 }") + " !" + _MAP_VALUES.args(" ."), "2\n4");
  }

  /** Test method. */
  @Test public void entry() {
    final Function func = _MAP_ENTRY;
    query("exists(" + func.args("A", "B") + ')', true);
    query("exists(" + func.args(1, 2) + ')', true);
    query("exists(" + _MAP_MERGE.args(func.args(1, 2)) + ')', true);

    check(func.args(" <_>A</_>", 0), "map{\"A\":0}", empty(CElem.class), root(XQMap.class));

    error("exists(" + func.args(" ()", 2) + ')', EMPTYFOUND);
    error("exists(" + func.args(" (1, 2)", 2) + ')', SEQFOUND_X);
  }

  /** Test method. */
  @Test public void filter() {
    final Function func = _MAP_FILTER;
    query(func.args(" map { }", " function($k, $v) { true() } "), "map{}");
    query(func.args(" map { }", " function($k, $v) { false() } "), "map{}");

    query(func.args(" map { 1: 2 }", " function($k, $v) { true() } "), "map{1:2}");
    query(func.args(" map { 1: 2 }", " function($k, $v) { false() } "), "map{}");
    query(func.args(" map { 1: 2 }", " function($k, $v) { $k = 1 } "), "map{1:2}");
    query(func.args(" map { 1: 2 }", " function($k, $v) { $k = 2 } "), "map{}");
    query(func.args(" map { 1: 2 }", " function($k, $v) { $v = 1 } "), "map{}");
    query(func.args(" map { 1: 2 }", " function($k, $v) { $v = 2 } "), "map{1:2}");

    query(func.args(" map:merge((1 to 10) ! map:entry(., string()))",
        " function($k, $v) { $k < 2 } ") + " => map:keys()", 1);
    query(func.args(" map:merge((1 to 10) ! map:entry(., string()))",
        " function($k, $v) { $v < '2' } ") + "?* => sort()", "1\n10");
    query(func.args(" map { 'abc': 'a', 'def': 'g' }", " contains#2") + "?*", "a");
    query("map { 'aba': 'a', 'abc': 'a', 'cba': 'a' }" +
        " =>" + func.args(" contains#2") +
        " =>" + func.args(" starts-with#2") +
        " =>" + func.args(" ends-with#2") +
        " => map:keys()", "aba");

    // function coercion: allow function with lower arity
    query(func.args(" map { 1: 2 }", " true#0"), "map{1:2}");
    // reject function with higher arity
    error(func.args(" map { 'abc': 'a', 'def': 'g' }", " substring#2"), INVCONVERT_X_X_X);
  }

  /** Test method. */
  @Test public void forEach() {
    final Function func = _MAP_FOR_EACH;

    query("(1, map { 1: 2 })[. instance of map(*)] ! " +
        func.args(" .", " function($k, $v) { $v }"), 2);
    query("(1, matches#2)[. instance of function(*)] ! " +
        func.args(" map { 'aa': 'a' }", " ."), true);

    query(func.args(" map { }", " function($k, $v) { 1 }"), "");
    query(func.args(" map { 1: 2 }", " function($k, $v) { $k+$v }"), 3);
    query(func.args(" map { 'a': 1, 'b': 2 }", " function($k, $v) { $v }"), "1\n2");

    query("count(" + func.args(" map:merge((1 to 10) ! map:entry(., ()))",
        " function($k, $v) { $v }") + ')', 0);
    query("count(" + func.args(" map:merge((1 to 10) ! map:entry(., .))",
        " function($k, $v) { $v }") + ')', 10);
    query("count(" + func.args(" map:merge((1 to 10) ! map:entry(., (., .)))",
        " function($k, $v) { $v }") + ')', 20);

    check(func.args(" map { 'aa': 'a' }", " matches#2"), true, type(func, "xs:boolean*"));
  }

  /** Test method. */
  @Test public void get() {
    final Function func = _MAP_GET;
    query(func.args(" map { }", 1), "");
    query(func.args(_MAP_ENTRY.args(1, 2), 1), 2);

    query(func.args(_MAP_ENTRY.args(1, 2), 3), "");
    query(func.args(_MAP_ENTRY.args(1, 2), 3, " function($k) { }"), "");
    query(func.args(_MAP_ENTRY.args(1, 2), 3, " function($k) { 4, 5 }"), "4\n5");
  }

  /** Test method. */
  @Test public void keys() {
    final Function func = _MAP_KEYS;
    query("for $i in " + func.args(
        _MAP_MERGE.args(" for $i in 1 to 3 return " +
        _MAP_ENTRY.args(" $i", " $i+1"))) + " order by $i return $i", "1\n2\n3");
    query("let $map := " + _MAP_MERGE.args(" for $i in 1 to 3 return " +
        _MAP_ENTRY.args(" $i", " $i + 1")) +
        "for $k in " + func.args(" $map") + " order by $k return " +
        _MAP_GET.args(" $map", " $k"), "2\n3\n4");

    query(func.args(" map:merge((1 to 9) ! map:entry(., string()))", " op('<')(?, '2')"), 1);
  }

  /** Test method. */
  @Test public void merge() {
    // no entry
    final Function func = _MAP_MERGE;
    query("exists(" + func.args(" ()") + ')', true);
    checkSize(_MAP_ENTRY.args(1, 2), 1);
    checkSize(func.args(" ()"), 0);
    // single entry
    query("exists(" + func.args(" map { 'a': 'b' }") + ')', true);
    checkSize(func.args(" map { 'a': 'b' }"), 1);
    // single entry
    query("exists(" + func.args(" map { 'a': 'b', 'b': 'c' }") + ')', true);
    checkSize(func.args(" map { 'a': 'b', 'b': 'c' }"), 2);

    query(func.args(" (map { xs:time('01:01:01'): '' }, map { xs:time('01:01:01+01:00'): '' })"));

    // duplicates option
    query(func.args(" (map { 1: 2 }, map { 1: 3 })") + "(1)", 2);
    query(func.args(" (map { 1: 2 }, map { 1: 3 })",
        " map { 'duplicates': 'use-first' }") + "(1)", 2);
    query(func.args(" (map { 1: 2 }, map { 1: 3 })",
        " map { 'duplicates': 'use-last' }") + "(1)", 3);
    query(func.args(" (map { 1: 2 }, map { 1: 3 })",
        " map { 'duplicates': 'combine' }") + "(1)", "2\n3");
    error(func.args(" (map { 1: 2 }, map { 1: 3 })",
        " map { 'duplicates': 'reject' }") + "(1)",
        MERGE_DUPLICATE_X);

    // GH-1543
    query("map:for-each(" + func.args(" (map { 'a': () }, map { 'a': () })") +
        ", function($k, $v) { () })", "");
    query("map:for-each(" + func.args(" (map { 'a': () }, map { 'a': () })",
        " map { 'duplicates': 'combine' }") + ", function($k, $v) { () })", "");
    query("map:for-each(" + func.args(" (map { 'a': () }, map { 'a': () })",
        " map { 'duplicates': 'use-first' }") + ", function($k, $v) { () })", "");
    query("map:for-each(" + func.args(" (map { 'a': () }, map { 'b': () })") +
        ", function($k, $v) { $v })", "");
    query("map:for-each(" + func.args(" (map { 'a': () }, map { 'b': () })",
        " map { 'duplicates': 'combine' }") + ", function($k, $v) { $v })", "");
    query("map:for-each(" + func.args(" (map { 'a': () }, map { 'b': () })",
        " map { 'duplicates': 'use-first' }") + ", function($k, $v) { $v })", "");

    // GH-1561
    final String arg1 = " (map { 'A': 'a' }, map { 'A': 'a', 'B': 'b' })";
    query("map:size(" + func.args(arg1) + ")", 2);
    query("map:size(" + func.args(arg1, " map { 'duplicates': 'use-first' }") + ")", 2);
    query("map:size(" + func.args(arg1, " map { 'duplicates': 'use-last' }") + ")", 2);
    query("map:size(" + func.args(arg1, " map { 'duplicates': 'combine' }") + ")", 2);

    // GH-1602
    query("let $_ := 'combine' return " + func.args(" map { 0:1 }",
        " map { 'duplicates': $_ }") + "?0", 1);

    check(func.args(" map { 1: <a/> }") + "?1", "<a/>", empty(func));
    check(func.args(" (map { 1: <a/> }, map { })") + "?1", "<a/>", empty(func));
    check(func.args(" (map { 1: <a/> }, map { })") + "?*", "<a/>", empty(func));

    // GH-1954
    query(func.args(" if (<a/>/text()) then map { } else ()") + " ! map:keys(.)", "");

    //
    check(func.args(" (map:entry(1, <a/>), map { 1: <b/> })") + "?*", "<a/>", empty(func));
  }

  /**
   * Regression tests for {@code map:merge(...)} in the presence of hash collisions.
   *
   * @throws QueryException exception
   */
  @Test public void gh1779() throws QueryException {
    final Function func = _MAP_MERGE;
    final Str[] keys = { Str.get("DENW21AL100077Hs"), Str.get("DENW21AL100076i5"),
        Str.get("DENW21AL100076hT") };
    assertEquals(keys[0].hash(null), keys[1].hash(null));
    assertEquals(keys[1].hash(null), keys[2].hash(null));

    final String mapAB = Util.info("map { '%': %, '%': % }", keys[0], 1, keys[1], 1);
    final String mapABC = Util.info("map { '%': %, '%': %, '%': % }",
        keys[0], 2, keys[2], 2, keys[1], 2);
    // use-first
    query(
        DEEP_EQUAL.args(
            func.args(" (" + mapAB + "," + mapABC + ")", " map { 'duplicates': 'use-first' }"),
            Util.info(" map { '%': %, '%': %, '%': % }", keys[0], 1, keys[1], 1, keys[2], 2)
        ),
        true
    );
    query(
        DEEP_EQUAL.args(
            func.args(" (" + mapABC + "," + mapAB + ")", " map { 'duplicates': 'use-first' }"),
            Util.info(" map { '%': %, '%': %, '%': % }", keys[0], 2, keys[1], 2, keys[2], 2)
        ),
        true
    );
    // use-last
    query(
        DEEP_EQUAL.args(
            func.args(" (" + mapAB + "," + mapABC + ")", " map { 'duplicates': 'use-last' }"),
            Util.info(" map { '%': %, '%': %, '%': % }", keys[0], 2, keys[1], 2, keys[2], 2)
        ),
        true
    );
    query(
        DEEP_EQUAL.args(
            func.args(" (" + mapABC + "," + mapAB + ")", " map { 'duplicates': 'use-last' }"),
            Util.info(" map { '%': %, '%': %, '%': % }", keys[0], 1, keys[1], 1, keys[2], 2)
        ),
        true
    );
    // merge
    query(
        DEEP_EQUAL.args(
            func.args(" (" + mapAB + "," + mapABC + ")", " map { 'duplicates': 'combine' }"),
            Util.info(" map { '%': (%, %), '%': (%, %), '%': % }",
                keys[0], 1, 2, keys[1], 1, 2, keys[2], 2)
        ),
        true
    );
    query(
        DEEP_EQUAL.args(
            func.args(" (" + mapABC + "," + mapAB + ")", " map { 'duplicates': 'combine' }"),
            Util.info(" map { '%': (%, %), '%': (%, %), '%': % }",
                keys[0], 2, 1, keys[1], 2, 1, keys[2], 2)
        ),
        true
    );
  }

  /** Test method. */
  @Test public void pairs() {
    final Function func = _MAP_PAIRS;
    query(func.args(" map {}"), "");
    query(func.args(" map { 1: 2 }") + "?key", 1);
    query(func.args(" map { 1: 2 }") + "?value", 2);
    query(func.args(" map { 1: (2, 3) }") + "?key", 1);
    query(func.args(" map { 1: (2, 3) }") + "?value", "2\n3");
    query(func.args(" map { 1: 2, 3: 4 }") + "?key", "1\n3");
    query(func.args(" map { 1: 2, 3: 4 }") + "?value", "2\n4");
  }

  /** Test method. */
  @Test public void put() {
    // no entry
    final Function func = _MAP_PUT;
    checkSize(func.args(" map { }", 1, 2), 1);
    checkSize(func.args(" map { }", "a", "b"), 1);
    checkSize(func.args(" map { 'a': 'b' }", "c", "d"), 2);
    checkSize(func.args(" map { 'a': 'b' }", "c", "d"), 2);

    query(func.args(" map { xs:time('01:01:01'): 'b' }", "xs:time('01:01:02+01:00')", 1));

    check(func.args(" map { <?_ 1?>: 2, 3: 4 }", " <_>5</_>", 6),
        "map{3:4,\"1\":2,\"5\":6}", empty(CElem.class));

    query("deep-equal(" + func.args(" map { 0: 1 }", -1, 2) + ", map { 0: 1, -1: 2 })", true);
  }

  /** Test method. */
  @Test public void remove() {
    final Function func = _MAP_REMOVE;
    checkSize(func.args(_MAP_ENTRY.args(1, 2), 1), 0);
  }

  /** Test method. */
  @Test public void size() {
    final Function func = _MAP_SIZE;
    query(func.args(" map { }"), 0);
  }

  /** Test method. */
  @Test public void values() {
    final Function func = _MAP_VALUES;

    query(func.args(" map {}"), "");
    query(func.args(" map { 1: 2 }"), 2);
    query(func.args(" map { 1: (2, 3) }"), "2\n3");
    query(func.args(" map { 1: 2, 3: 4 }"), "2\n4");
  }

  /**
   * Counts the map entries.
   * @param query query string
   * @param count expected number of entries
   */
  private static void checkSize(final String query, final int count) {
    query(_MAP_SIZE.args(' ' + query), count);
  }
}
