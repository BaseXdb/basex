package org.basex.query.func;

import static org.basex.query.func.Function.*;
import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.junit.*;

/**
 * This class tests the functions of the Array Module.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNArrayTest extends AdvancedQueryTest {
  /** Test method. */
  @Test public void size() {
    query(_ARRAY_SIZE.args(" []"), 0);
    query(_ARRAY_SIZE.args(" [ 1 ]"), 1);
    query(_ARRAY_SIZE.args(" [ 1, 2 ]"), 2);
    query(_ARRAY_SIZE.args(" [ 1 to 3 ]"), 1);
    query(_ARRAY_SIZE.args(" array {}"), 0);
    query(_ARRAY_SIZE.args(" array { 1 }"), 1);
    query(_ARRAY_SIZE.args(" array { 1, 2 }"), 2);
    query(_ARRAY_SIZE.args(" array { 1 to 3 }"), 3);
  }

  /** Test method. */
  @Test public void append() {
    array(_ARRAY_APPEND.args(" []", "()"), "[()]");
    array(_ARRAY_APPEND.args(" [()]", "()"), "[(), ()]");
    array(_ARRAY_APPEND.args(" []", " 1"), "[1]");
    array(_ARRAY_APPEND.args(" [1]", " 2"), "[1, 2]");
    array(_ARRAY_APPEND.args(" [1,2,3]", "(4,5)"), "[1, 2, 3, (4, 5)]");
  }

  /** Test method. */
  @Test public void subarray() {
    array(_ARRAY_SUBARRAY.args(" []", " 1"), "[]");
    array(_ARRAY_SUBARRAY.args(" []", " 1", " 0"), "[]");
    array(_ARRAY_SUBARRAY.args(" [1]", " 1"), "[1]");
    array(_ARRAY_SUBARRAY.args(" [1]", " 1", " 0"), "[]");
    array(_ARRAY_SUBARRAY.args(" [1]", " 1", " 1"), "[1]");
    array(_ARRAY_SUBARRAY.args(" [1]", " 2", " 0"), "[]");
    array(_ARRAY_SUBARRAY.args(" array { 1 to 5 }", " 5"), "[5]");
    array(_ARRAY_SUBARRAY.args(" array { 1 to 5 }", " 6"), "[]");
    array(_ARRAY_SUBARRAY.args(" array { 1 to 5 }", " 1", " 1"), "[1]");
    array(_ARRAY_SUBARRAY.args(" array { 1 to 5 }", " 2", " 3"), "[2, 3, 4]");

    error(_ARRAY_SUBARRAY.args(" [1]", " 0", " 0"), ARRAYBOUNDS_X_X);
    error(_ARRAY_SUBARRAY.args(" [1]", " 1", " -1"), ARRAYNEG_X);
    error(_ARRAY_SUBARRAY.args(" []", " 1", " 1"), ARRAYBOUNDS_X_X);
    error(_ARRAY_SUBARRAY.args(" [1]", " 1", " 2"), ARRAYBOUNDS_X_X);
  }

  /** Test method. */
  @Test public void remove() {
    array(_ARRAY_REMOVE.args(" [1]", " 1"), "[]");
    array(_ARRAY_REMOVE.args(" [1, 2]", " 1"), "[2]");
    array(_ARRAY_REMOVE.args(" [1, 2]", " 2"), "[1]");
    array(_ARRAY_REMOVE.args(" array { 1 to 5 }", " 1"), "[2, 3, 4, 5]");
    array(_ARRAY_REMOVE.args(" array { 1 to 5 }", " 3"), "[1, 2, 4, 5]");
    array(_ARRAY_REMOVE.args(" array { 1 to 5 }", " 5"), "[1, 2, 3, 4]");

    error(_ARRAY_REMOVE.args(" []", " 0"), ARRAYBOUNDS_X_X);
    error(_ARRAY_REMOVE.args(" [1]", " 0"), ARRAYBOUNDS_X_X);
    error(_ARRAY_REMOVE.args(" [1]", " 2"), ARRAYBOUNDS_X_X);
  }

  /** Test method. */
  @Test public void insertBefore() {
    array(_ARRAY_INSERT_BEFORE.args(" []", " 1", " 1"), "[1]");
    array(_ARRAY_INSERT_BEFORE.args(" [1]", " 1", " 2"), "[2, 1]");
    array(_ARRAY_INSERT_BEFORE.args(" [1]", " 2", " 2"), "[1, 2]");

    error(_ARRAY_INSERT_BEFORE.args(" []", " 0", " 1"), ARRAYBOUNDS_X_X);
    error(_ARRAY_INSERT_BEFORE.args(" []", " 2", " 1"), ARRAYBOUNDS_X_X);
  }

  /** Test method. */
  @Test public void head() {
    query(_ARRAY_HEAD.args(" [1]"), "1");
    query(_ARRAY_HEAD.args(" array { 1 to 5 }"), "1");
    query(_ARRAY_HEAD.args(" [1 to 2, 3]"), "1 2");

    error(_ARRAY_HEAD.args(" []"), ARRAYBOUNDS_X_X);
  }

  /** Test method. */
  @Test public void tail() {
    array(_ARRAY_TAIL.args(" [1]"), "[]");
    array(_ARRAY_TAIL.args(" array { 1 to 5 }"), "[2, 3, 4, 5]");
    array(_ARRAY_TAIL.args(" [1 to 2, 3]"), "[3]");

    error(_ARRAY_TAIL.args(" []"), ARRAYBOUNDS_X_X);
  }

  /** Test method. */
  @Test public void reverse() {
    array(_ARRAY_REVERSE.args(" []"), "[]");
    array(_ARRAY_REVERSE.args(" [1]"), "[1]");
    array(_ARRAY_REVERSE.args(" [1, 2]"), "[2, 1]");
    array(_ARRAY_REVERSE.args(" [1 to 2]"), "[(1, 2)]");
    array(_ARRAY_REVERSE.args(" array { 1 to 3 }"), "[3, 2, 1]");
  }

  /** Test method. */
  @Test public void join() {
    array(_ARRAY_JOIN.args(" []"), "[]");
    array(_ARRAY_JOIN.args(" [1]"), "[1]");
    array(_ARRAY_JOIN.args("([1], [2])"), "[1, 2]");
    array(_ARRAY_JOIN.args("([1], [2], [3])"), "[1, 2, 3]");
    array(_ARRAY_JOIN.args("([1], [()], [2 to 3])"), "[1, (), (2, 3)]");
  }

  /** Test method. */
  @Test public void forEach() {
    array(_ARRAY_FOR_EACH.args(" []", "function($a) { $a }"), "[]");
    array(_ARRAY_FOR_EACH.args(" [1]", "function($a) { $a }"), "[1]");
    array(_ARRAY_FOR_EACH.args(" [1,2]", "function($a) { $a+1 }"), "[2, 3]");
    array(_ARRAY_FOR_EACH.args(" [1,2,3]", "function($a) { () }"), "[(), (), ()]");
  }

  /** Test method. */
  @Test public void filter() {
    array(_ARRAY_FILTER.args(" [1]", "function($a) { true() }"), "[1]");
    array(_ARRAY_FILTER.args(" [1]", "function($a) { false() }"), "[]");
    array(_ARRAY_FILTER.args(" [1,-2]", "function($a) { $a>0 }"), "[1]");
  }

  /** Test method. */
  @Test public void foldLeft() {
    query(_ARRAY_FOLD_LEFT.args(" [1,2]", " 0", "function($a,$b) { $a+$b }"), "3");
  }

  /** Test method. */
  @Test public void foldRight() {
    query(_ARRAY_FOLD_RIGHT.args(" [1,2]", "()", "function($a,$b) { $b,$a }"), "2 1");
  }

  /** Test method. */
  @Test public void forEachPair() {
    array(_ARRAY_FOR_EACH_PAIR.args(" []", " []", "function($a,$b) { $a+$b }"), "[]");
    array(_ARRAY_FOR_EACH_PAIR.args(" [1,2]", " []", "function($a,$b) { $a+$b }"), "[]");
    array(_ARRAY_FOR_EACH_PAIR.args(" [1]", " [2]", "function($a,$b) { $a+$b }"), "[3]");
    array(_ARRAY_FOR_EACH_PAIR.args(" [1,2,3]", " [2]", "function($a,$b) { $a+$b }"), "[3]");
  }

  /**
   * Compares the serialized version of an array.
   * @param query query string
   * @param exp expected result
   */
  private static void array(final String query, final String exp) {
    query(_ARRAY_SERIALIZE.args(' ' + query), exp);
  }
}
