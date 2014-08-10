package org.basex.query.func;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.junit.*;

/**
 * This class tests the functions of the Map Module.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNMapTest extends AdvancedQueryTest {
  /** Test method. */
  @Test
  public void merge() {
    // no entry
    query(EXISTS.args(_MAP_MERGE.args(" ()")), true);
    count(_MAP_ENTRY.args(1, 2), 1);
    count(_MAP_MERGE.args(" ()"), 0);
    // single entry
    query(EXISTS.args(_MAP_MERGE.args(" map{ 'a':'b' }")), true);
    count(_MAP_MERGE.args(" map{ 'a':'b' }"), 1);
    // single entry
    query(EXISTS.args(_MAP_MERGE.args(" map{ 'a':'b','b':'c' }")), true);
    count(_MAP_MERGE.args(" map{ 'a':'b','b':'c' }"), 2);
  }

  /** Test method. */
  @Test
  public void entry() {
    query(EXISTS.args(_MAP_ENTRY.args("A", "B")), true);
    query(EXISTS.args(_MAP_ENTRY.args(1, 2)), true);
    query(EXISTS.args(_MAP_MERGE.args(_MAP_ENTRY.args(1, 2))), "true");
    error(EXISTS.args(_MAP_ENTRY.args("()", 2)), Err.EMPTYFOUND);
    error(EXISTS.args(_MAP_ENTRY.args("(1,2)", 2)), Err.SEQFOUND);
  }

  /** Test method. */
  @Test
  public void put() {
    // no entry
    count(_MAP_PUT.args(" map{}", 1, 2), 1);
    count(_MAP_PUT.args(" map{}", "a", "b"), 1);
    count(_MAP_PUT.args(" map{ 'a': 'b' }", "c", "d"), 2);
    count(_MAP_PUT.args(" map{ 'a': 'b' }", "c", "d"), 2);
  }

  /** Test method. */
  @Test
  public void get() {
    query(_MAP_GET.args(" map{}", 1), "");
    query(_MAP_GET.args(_MAP_ENTRY.args(1, 2), 1), 2);
  }

  /** Test method. */
  @Test
  public void contains() {
    query(_MAP_CONTAINS.args(" map{}", 1), false);
    query(_MAP_CONTAINS.args(_MAP_ENTRY.args(1, 2), 1), true);
  }

  /** Test method. */
  @Test
  public void remove() {
    count(_MAP_REMOVE.args(_MAP_ENTRY.args(1, 2), 1), 0);
  }

  /** Test method. */
  @Test
  public void keys() {
    query("for $i in " + _MAP_KEYS.args(
        _MAP_MERGE.args(" for $i in 1 to 3 return " +
        _MAP_ENTRY.args("$i", "$i+1"))) + " order by $i return $i", "1 2 3");
    query("let $map := " + _MAP_MERGE.args(" for $i in 1 to 3 return " +
        _MAP_ENTRY.args("$i", "$i + 1")) +
        "for $k in " + _MAP_KEYS.args("$map") + " order by $k return " +
        _MAP_GET.args("$map", "$k"), "2 3 4");
  }

  /** Test method. */
  @Test
  public void forEachEntry() {
    query(_MAP_FOR_EACH_ENTRY.args(" map{}", "function($a, $b) { 1 }"), "");
    query(_MAP_FOR_EACH_ENTRY.args(" map{1:2}", "function($a, $b) { $a+$b }"), "3");
    query(_MAP_FOR_EACH_ENTRY.args(" map{'a':1, 'b':2}", "function($a, $b) { $b }"), "1 2");
  }

  /**
   * Counts the map entries.
   * @param query query string
   * @param count expected number of entries
   */
  private static void count(final String query, final int count) {
    query(_MAP_SIZE.args(" " + query), count);
  }
}
