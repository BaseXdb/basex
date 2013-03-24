package org.basex.query.util;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;

/**
 * A visitor for all sub-expressions in an {@link Expr}. All methods return a
 * {@code boolean} which signals if the tree walk should be continued.
 *
 * @author Leo Woerteler
 */
@SuppressWarnings("unused")
public abstract class ASTVisitor {
  /**
   * Notifies the visitor of a variable declaration.
   * @param count declared variable
   * @return if more expressions should be visited
   */
  public boolean declared(final Var count) {
    return true;
  }

  /**
   * Notifies the visitor of a variable reference.
   * @param ref used variable
   * @return if more expressions should be visited
   */
  public boolean used(final VarRef ref) {
    return true;
  }

  /**
   * Notifies the visitor of a reference t oa static variable.
   * @param var static variable
   * @return if more expressions should be visited
   */
  public boolean staticVar(final StaticVar var) {
    return true;
  }

  /**
   * Notifies the visitor of a sub-scope.
   * @param sub scope
   * @return if more expressions should be visited
   */
  public boolean inlineFunc(final Scope sub) {
    return true;
  }

  /**
   * Notifies the visitor of a function call.
   * @param call function call
   * @return if more expressions should be visited
   */
  public boolean funcCall(final StaticFuncCall call) {
    return true;
  }

  /**
   * Notifies the visitor of a function item.
   * @param func the function item
   * @return if more expressions should be visited
   */
  public boolean funcItem(final FuncItem func) {
    return true;
  }
}
