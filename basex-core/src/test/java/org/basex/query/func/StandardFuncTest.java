package org.basex.query.func;

import org.basex.query.ast.*;
import org.basex.util.*;
import org.junit.*;

/**
 * XQuery functions: AST tests.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class StandardFuncTest extends QueryPlanTest {
  /** Tests functions with first argument that can be empty ({@link StandardFunc#optFirst}). */
  @Test public void optFirst() {
    // use fn:abs as candidate
    final Function func = Function.ABS;
    final String name = Util.className(func.clazz);

    // pre-evaluate empty sequence
    check(func.args(" ()"), "", empty(name));
    // pre-evaluate argument
    check(func.args(1), 1, empty(name));

    // function is replaced by its argument (argument yields no result)
    check(func.args(" prof:void(())"), "", empty(name));
    // but type is adjusted
    check(func.args(" <_>1</_>"), 1, type(name, "xs:numeric"));
    // no adjustment of type
    check(func.args(" 1 ! array { . }"), 1, type(name, "xs:numeric?"));
  }

  /** Tests functions with higher-order functions. */
  @Test public void coerceFunc() {
    final Function func = Function.FOR_EACH;
    check(func.args(" (1 to 2)[. = 2]", " function($a) { $a * $a }"), 4,
        exists(Util.className(func.clazz) + "[@type = 'xs:integer*']"));
  }
}
