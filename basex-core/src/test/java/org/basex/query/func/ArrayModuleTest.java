package org.basex.query.func;

import static org.basex.query.QueryError.*;

import org.basex.query.ast.*;
import org.junit.*;

/**
 * This class tests the functions of the Array Module.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class ArrayModuleTest extends QueryPlanTest {
  /** Test method. */
  @Test public void append() {
    final Function func = Function._ARRAY_APPEND;
    query(func.args(" []", " ()"), "[()]");
    query(func.args(" [()]", " ()"), "[(), ()]");
    query(func.args(" []", 1), "[1]");
    query(func.args(" [1]", 2), "[1, 2]");
    query(func.args(" [1,2,3]", " (4,5)"), "[1, 2, 3, (4, 5)]");
  }

  /** Test method. */
  @Test public void filter() {
    final Function func = Function._ARRAY_FILTER;
    query("([1],1)[. instance of array(*)] ! " + func.args(" .",
        " function($a) { true() }"), "[1]");

    query(func.args(" [1]", " function($a) { true() }"), "[1]");
    query(func.args(" [1]", " function($a) { false() }"), "[]");
    query(func.args(" [1,-2]", " function($a) { $a>0 }"), "[1]");
    query(func.args(" [0,1]", " boolean#1"), "[1]");
  }

  /** Test method. */
  @Test public void flatten() {
    final Function func = Function._ARRAY_FLATTEN;
    query(func.args(" [1,2]"), "1\n2");
    query(func.args(" ([1],[2])"), "1\n2");
    query(func.args(" for $c in (1,2) return [$c]"), "1\n2");
    query(func.args(" [for $c in (1,2) return [$c]]"), "1\n2");
    query("head(" + func.args(" 1 to 1000000000000") + ')', "1");
  }

  /** Test method. */
  @Test public void foldLeft() {
    final Function func = Function._ARRAY_FOLD_LEFT;
    query(func.args(" [1,2]", 0, " function($a,$b) { $a+$b }"), 3);
  }

  /** Test method. */
  @Test public void foldRight() {
    final Function func = Function._ARRAY_FOLD_RIGHT;
    query(func.args(" [1,2]", " ()", " function($a,$b) { $b,$a }"), "2\n1");
  }

  /** Test method. */
  @Test public void forEach() {
    final Function func = Function._ARRAY_FOR_EACH;
    query("([2], 2)[. instance of array(*)] ! " +
        func.args(" .", " function($a) { $a * $a }"), "[4]");
    query("(1, not#1)[. instance of function(*)] ! " +
        func.args(" [1]", " ."), "[false()]");

    query(func.args(" []", " function($a) { $a }"), "[]");
    query(func.args(" [1]", " function($a) { $a }"), "[1]");
    query(func.args(" [1,2]", " function($a) { $a+1 }"), "[2, 3]");
    query(func.args(" [1,2,3]", " function($a) { () }"), "[(), (), ()]");
  }

  /** Test method. */
  @Test public void forEachPair() {
    final Function func = Function._ARRAY_FOR_EACH_PAIR;
    query("([2], 2)[. instance of array(*)] ! " +
        func.args(" .", " .", " function($a, $b) { $a * $b }"), "[4]");
    query("([2], 2)[. instance of array(*)] ! " +
        func.args(" [1]", " .", " function($a, $b) { $a * $b }"), "[2]");
    query("([2], 2)[. instance of array(*)] ! " +
        func.args(" .", " [1]", " function($a, $b) { $a * $b }"), "[2]");
    query("(1, deep-equal#2)[. instance of function(*)] ! " +
        func.args(" [1]", " [2]", " ."), "[false()]");

    query(func.args(" []", " []", " function($a,$b) { $a+$b }"), "[]");
    query(func.args(" [1,2]", " []", " function($a,$b) { $a+$b }"), "[]");
    query(func.args(" [1]", " [2]", " function($a,$b) { $a+$b }"), "[3]");
    query(func.args(" [1,2,3]", " [2]", " function($a,$b) { $a+$b }"), "[3]");
  }

  /** Test method. */
  @Test public void head() {
    final Function func = Function._ARRAY_HEAD;
    query(func.args(" [1]"), 1);
    query(func.args(" array { 1 to 5 }"), 1);
    query(func.args(" [1 to 2, 3]"), "1\n2");

    error(func.args(" []"), ARRAYEMPTY);
  }

  /** Test method. */
  @Test public void insertBefore() {
    final Function func = Function._ARRAY_INSERT_BEFORE;
    query(func.args(" []", 1, 1), "[1]");
    query(func.args(" [1]", 1, 2), "[2, 1]");
    query(func.args(" [1]", 2, 2), "[1, 2]");

    error(func.args(" []", 0, 1), ARRAYBOUNDS_X_X);
    error(func.args(" []", 2, 1), ARRAYBOUNDS_X_X);
  }

  /** Test method. */
  @Test public void join() {
    final Function func = Function._ARRAY_JOIN;
    query(func.args(" []"), "[]");
    query(func.args(" [1]"), "[1]");
    query(func.args(" ([1], [2])"), "[1, 2]");
    query(func.args(" ([1], [2], [3])"), "[1, 2, 3]");
    query(func.args(" ([1], [()], [2 to 3])"), "[1, (), (2, 3)]");
  }

  /** Test method. */
  @Test public void put() {
    final Function func = Function._ARRAY_PUT;
    query(func.args(" [1]", 1, " ()"), "[()]");
    error(func.args(" []", 1, " ()"), ARRAYEMPTY);
    error(func.args(" [1]", 2, " ()"), ARRAYBOUNDS_X_X);
  }

  /** Test method. */
  @Test public void remove() {
    final Function func = Function._ARRAY_REMOVE;
    query(func.args(" [1]", 1), "[]");
    query(func.args(" [1, 2]", 1), "[2]");
    query(func.args(" [1, 2]", 2), "[1]");
    query(func.args(" array { 1 to 5 }", 1), "[2, 3, 4, 5]");
    query(func.args(" array { 1 to 5 }", 3), "[1, 2, 4, 5]");
    query(func.args(" array { 1 to 5 }", 5), "[1, 2, 3, 4]");

    error(func.args(" []", 0), ARRAYEMPTY);
    error(func.args(" [1]", 0), ARRAYBOUNDS_X_X);
    error(func.args(" [1]", 2), ARRAYBOUNDS_X_X);
  }

  /** Test method. */
  @Test public void reverse() {
    final Function func = Function._ARRAY_REVERSE;
    query(func.args(" []"), "[]");
    query(func.args(" [1]"), "[1]");
    query(func.args(" [1, 2]"), "[2, 1]");
    query(func.args(" [1 to 2]"), "[(1, 2)]");
    query(func.args(" array { 1 to 3 }"), "[3, 2, 1]");
  }

  /** Test method. */
  @Test public void size() {
    final Function func = Function._ARRAY_SIZE;
    query(func.args(" []"), 0);
    query(func.args(" [ 1 ]"), 1);
    query(func.args(" [ 1, 2 ]"), 2);
    query(func.args(" [ 1 to 3 ]"), 1);
    query(func.args(" array {}"), 0);
    query(func.args(" array { 1 }"), 1);
    query(func.args(" array { 1, 2 }"), 2);
    query(func.args(" array { 1 to 3 }"), 3);
  }

  /** Test method. */
  @Test public void sort() {
    final Function func = Function._ARRAY_SORT;
    query("([2,1], 1)[. instance of array(*)] ! " +
        func.args(" .", " ()", " hof:id#1"), "[1, 2]");

    query(func.args(" [1,4,6,5,3]"), "[1, 3, 4, 5, 6]");
    query(func.args(" [(1,0), (1,1), (0,1), (0,0)]"), "[(0, 0), (0, 1), (1, 0), (1, 1)]");
    query(func.args(" [3,2,1]", "http://www.w3.org/2005/xpath-functions/collation/codepoint"),
        "[1, 2, 3]");
    query(func.args(" [1,-2,5,10,-10,10,8]", " ()", " abs#1"), "[1, -2, 5, 8, 10, -10, 10]");

    check(func.args(" [1,2]", " ()", " function($a) { -$a }"), "[2, 1]",
        exists("ArraySort[@type = 'array(xs:integer)']"));
  }

  /** Test method. */
  @Test public void subquery() {
    final Function func = Function._ARRAY_SUBARRAY;
    query(func.args(" []", 1), "[]");
    query(func.args(" []", 1, 0), "[]");
    query(func.args(" [1]", 1), "[1]");
    query(func.args(" [1]", 1, 0), "[]");
    query(func.args(" [1]", 1, 1), "[1]");
    query(func.args(" [1]", 2, 0), "[]");
    query(func.args(" array { 1 to 5 }", 5), "[5]");
    query(func.args(" array { 1 to 5 }", 6), "[]");
    query(func.args(" array { 1 to 5 }", 1, 1), "[1]");
    query(func.args(" array { 1 to 5 }", 2, 3), "[2, 3, 4]");

    error(func.args(" [1]", 0, 0), ARRAYBOUNDS_X_X);
    error(func.args(" [1]", 1, " -1"), ARRAYNEG_X);
    error(func.args(" []", 1, 1), ARRAYBOUNDS_X_X);
    error(func.args(" [1]", 1, 2), ARRAYBOUNDS_X_X);
  }

  /** Test method. */
  @Test public void tail() {
    final Function func = Function._ARRAY_TAIL;
    query(func.args(" [1]"), "[]");
    query(func.args(" array { 1 to 5 }"), "[2, 3, 4, 5]");
    query(func.args(" [1 to 2, 3]"), "[3]");

    error(func.args(" []"), ARRAYEMPTY);
  }
}
