package org.basex.query.func;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.util.*;

/**
 * Interface for built-in functions.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public interface AFunction {
  /**
   * Returns the function definition.
   * @return definition
   */
  FuncDefinition definition();

  /**
   * Creates a new instance of the function.
   * @param sc static context
   * @param ii input info
   * @param exprs arguments
   * @return function
   */
  default StandardFunc get(final StaticContext sc, final InputInfo ii, final Expr... exprs) {
    final FuncDefinition fd = definition();
    final StandardFunc sf = fd.ctor.get();
    sf.init(sc, ii, fd, exprs);
    return sf;
  }

  /**
   * Checks if the specified expression is an instance of this function.
   * @param ex expression
   * @return result of check
   */
  default boolean is(final Expr ex) {
    return ex instanceof StandardFunc && ((StandardFunc) ex).definition == definition();
  }

  /**
   * Returns a string representation of the function with the specified arguments
   * (see {@link FuncDefinition}).
   * @param args arguments
   * @return string representation
   */
  default String args(final Object... args) {
    return definition().args(args);
  }
}
