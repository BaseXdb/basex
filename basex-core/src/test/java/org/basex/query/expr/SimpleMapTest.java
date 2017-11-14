package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.query.ast.*;
import org.basex.query.expr.constr.*;
import org.basex.query.func.fn.*;
import org.basex.query.value.item.*;
import org.junit.*;

/**
 * Tests for the simple map operator.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class SimpleMapTest extends QueryPlanTest {
  /** Basic tests. */
  @Test public void basic() {
    query("1 ! 2", 2);
    query("1 ! (1 to 2)", "1\n2");
    query("(1 to 2) ! 3", "3\n3");
    query("(1 to 2) ! (3 to 4)", "3\n4\n3\n4");

    query("(1 to 2) ! <a/>", "<a/>\n<a/>");
  }

  /** Empty results. */
  @Test public void noResults() {
    check("() ! ()", "", empty());
    check("1 ! ()", "", empty());
    check("() ! 1", "", empty());
    check("1 ! () ! 1", "", empty());
    check("1 ! prof:void('x')", "", empty(Int.class));
    check("1 ! prof:void(.) ! 1", "", count(Int.class, 1));
    check("<a/> ! <b/> ! ()", "", empty());

    check("prof:void('x') ! 1", "", empty(Int.class));
    check("() ! 'a'[.]", "", empty());
  }

  /** Context item. */
  @Test public void context() {
    query("2 ! number()", 2);
    query("3 ! number(.)", 3);
    query("4 ! string()", "4");
    query("5 ! string(.)", "5");
    query("(1,2) ! position()", "1\n2");
    query("(1,2) ! last()", "2\n2");

    final String nomap = empty("*[contains(name(), 'Map')]");
    check("1 ! .", 1, nomap);
    check("(1, 2)[. = 1] ! .", 1, nomap);
    check("(1,(2, 3)[. = 2]) ! .", "1\n2", nomap);
    check("(1,2) !.!.!.!.!.!.!.!.!.!.!.", "1\n2", nomap);
    check("<a/> ! . ! .", "<a/>", nomap);

    check("trace(1) ! (. + 1)", 2, exists(ItemMap.class));
    check("1[.= 1] ! trace(.)", 1, exists(FnTrace.class));
  }

  /** Typing. */
  @Test public void types() {
    check("(1, 2) ! .[. = 1]", 1, exists(IterMap.class));
    check("1[. = 1] ! 2", "2", exists("ItemMap[@type = 'xs:integer?']"));
    check("4[. = 4] ! (4, 5)[. = 4]", 4, exists("IterMap[@type = 'xs:integer*']"));
  }

  /** Errors. */
  @Test public void error() {
    error("(1 + 'a') ! 2", NONUMBER_X_X);
  }

  /** Replicate results. */
  @Test public void replicate() {
    check("<x/> ! 2[. = 2]", "2", empty(CElem.class));
    check("(1 to 2) ! 'a'[.]", "a\na", exists(_UTIL_REPLICATE.clazz));
    check("(1 to 2) ! (4, 5)[. = 4]", "4\n4", exists(_UTIL_REPLICATE.clazz));
    check("(1 to 2) ! prof:void(.)", "", empty(_UTIL_REPLICATE.clazz));
  }
}
