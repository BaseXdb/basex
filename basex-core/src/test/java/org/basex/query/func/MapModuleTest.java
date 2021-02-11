package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import org.basex.query.*;
import org.basex.query.ast.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the Map Module.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class MapModuleTest extends QueryPlanTest {
  /** Test method. */
  @Test public void contains() {
    final Function func = _MAP_CONTAINS;
    query(func.args(" map { }", 1), false);
    query(func.args(_MAP_ENTRY.args(1, 2), 1), true);
  }

  /** Test method. */
  @Test public void entry() {
    final Function func = _MAP_ENTRY;
    query("exists(" + func.args("A", "B") + ')', true);
    query("exists(" + func.args(1, 2) + ')', true);
    query("exists(" + _MAP_MERGE.args(func.args(1, 2)) + ')', true);
    error("exists(" + func.args(" ()", 2) + ')', EMPTYFOUND);
    error("exists(" + func.args(" (1,2)", 2) + ')', SEQFOUND_X);
  }

  /** Test method. */
  @Test public void forEach() {
    final Function func = _MAP_FOR_EACH;

    query("(1,map { 1: 2 })[. instance of map(*)] ! " +
        func.args(" .", " function($k, $v) { $v }"), 2);
    query("(1,matches#2)[. instance of function(*)] ! " +
        func.args(" map { 'aa': 'a' }", " ."), true);

    query(func.args(" map { }", " function($k, $v) { 1 }"), "");
    query(func.args(" map { 1: 2 }", " function($k, $v) { $k+$v }"), 3);
    query(func.args(" map { 'a': 1, 'b': 2 }", " function($k, $v) { $v }"), "1\n2");

    check(func.args(" map { 'aa': 'a' }", " matches#2"), true, type(func, "xs:boolean*"));
  }

  /** Test method. */
  @Test public void get() {
    final Function func = _MAP_GET;
    query(func.args(" map { }", 1), "");
    query(func.args(_MAP_ENTRY.args(1, 2), 1), 2);
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
  @Test public void put() {
    // no entry
    final Function func = _MAP_PUT;
    checkSize(func.args(" map { }", 1, 2), 1);
    checkSize(func.args(" map { }", "a", "b"), 1);
    checkSize(func.args(" map { 'a': 'b' }", "c", "d"), 2);
    checkSize(func.args(" map { 'a': 'b' }", "c", "d"), 2);

    query(func.args(" map { xs:time('01:01:01'): 'b' }", "xs:time('01:01:02+01:00')", 1));

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

  /**
   * Counts the map entries.
   * @param query query string
   * @param count expected number of entries
   */
  private static void checkSize(final String query, final int count) {
    query(_MAP_SIZE.args(' ' + query), count);
  }
}
