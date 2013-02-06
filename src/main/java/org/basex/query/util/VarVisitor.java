package org.basex.query.util;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.var.*;

/**
 * A visitor for variables declared and used in an {@link Expr}. All methods return a
 * {@code boolean} which signals if the tree walk should be continued.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("unused")
public abstract class VarVisitor {
  /**
   * Notifies the visitor of a variable declaration.
   * @param count declared variable
   * @return if more variables should be visited
   */
  public boolean declared(final Var count) {
    return true;
  };

  /**
   * Notifies the visitor of a variable reference.
   * @param ref used variable
   * @return if more variables should be visited
   */
  public boolean used(final VarRef ref) {
    return true;
  };

  /**
   * Notifies the visitor of a sub-scope.
   * @param sub scope
   * @return if more variables should be visited
   */
  public boolean subScope(final Scope sub) {
    return true;
  }

  /**
   * Notifies the visitor of a variable going out of scope.
   * @param var variable
   * @return if more variables should be visited
   */
  public boolean undeclared(final Var var) {
    return true;
  };

  /**
   * Visits all given expressions.
   * @param exprs expressions to visit
   * @return if more variables should be visited
   */
  public final boolean visitAll(final Expr...exprs) {
    for(final Expr e : exprs) if(!e.visitVars(this)) return false;
    return true;
  }

  /**
   * Declares the given variables, then visits the given expressions and finally
   * undeclares the variables.
   * @param vars variables to declare
   * @param exprs expressions to visit
   * @return if more variables should be visited
   */
  public final boolean withVars(final Var[] vars, final Expr...exprs) {
    for(final Var v : vars) if(!declared(v)) return false;
    if(!visitAll(exprs)) return false;
    for(int i = vars.length; --i >= 0;) if(!undeclared(vars[i])) return false;
    return true;
  }
}
