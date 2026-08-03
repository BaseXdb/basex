package org.basex.query.util;

import java.util.*;
import java.util.function.*;

import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.scope.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;

/**
 * A visitor for all sub-expressions in an {@link Expr}. All methods return a
 * {@code boolean} which signals if the tree walk should be continued.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public abstract class ASTVisitor {
  /**
   * Notifies the visitor of a variable declaration.
   * @param var declared variable
   * @return if more expressions should be visited ({@code true} by default)
   */
  @SuppressWarnings("unused")
  public boolean declared(final Var var) {
    return true;
  }

  /**
   * Notifies the visitor of a variable reference.
   * @param ref used variable
   * @return if more expressions should be visited ({@code true} by default)
   */
  @SuppressWarnings("unused")
  public boolean used(final VarRef ref) {
    return true;
  }

  /**
   * Notifies the visitor of a reference to a static variable.
   * @param var static variable
   * @return if more expressions should be visited ({@code true} by default)
   */
  @SuppressWarnings("unused")
  public boolean staticVar(final StaticVar var) {
    return true;
  }

  /**
   * Notifies the visitor of a sub-scope.
   * @param scope sub scope
   * @return if more expressions should be visited ({@code true} by default)
   */
  @SuppressWarnings("unused")
  public boolean inlineFunc(final Scope scope) {
    return true;
  }

  /**
   * Notifies the visitor of a static function call.
   * @param call function call
   * @return if more expressions should be visited ({@code true} by default)
   */
  @SuppressWarnings("unused")
  public boolean staticFuncCall(final StaticFuncCall call) {
    return true;
  }

  /**
   * Notifies the visitor of a function item.
   * @param func the function item
   * @return if more expressions should be visited ({@code true} by default)
   */
  @SuppressWarnings("unused")
  public boolean funcItem(final FuncItem func) {
    return true;
  }

  /**
   * Notifies the visitor of a dynamic function call.
   * @param func expression that yields the invoked function
   * @return if more expressions should be visited ({@code true} by default)
   */
  @SuppressWarnings("unused")
  public boolean dynFuncCall(final Expr func) {
    return true;
  }

  /**
   * Notifies the visitor of database locks. Overwritten by {@link MainModule}.
   * Returns {@code false} if the lock is not known statically.
   * @param list function supplying lock strings
   * @param write write access
   * @return if more expressions should be visited ({@code true} by default)
   */
  @SuppressWarnings("unused")
  public boolean lock(final Supplier<ArrayList<String>> list, final boolean write) {
    return true;
  }

  /**
   * Notifies the visitor of a database lock. Overwritten by {@link MainModule}.
   * Returns {@code false} if the lock is not known statically.
   * @param lock lock string (can be {@code null})
   * @param write write access
   * @return if more expressions should be visited ({@code true} by default)
   */
  @SuppressWarnings("unused")
  public boolean lock(final String lock, final boolean write) {
    return true;
  }

  /**
   * Notifies the visitor of custom query locks.
   * @param list function supplying lock strings
   */
  @SuppressWarnings("unused")
  public void queryLock(final Supplier<ArrayList<String>> list) { }

  /**
   * Notifies the visitor of an update whose target database cannot be resolved statically.
   */
  public void unresolvedTarget() { }

  /**
   * Notifies the visitor of an expression entering a modify clause.
   */
  public void enterModify() { }

  /**
   * Notifies the visitor of an expression leaving a modify clause.
   */
  public void exitModify() { }

  /**
   * Notifies the visitor of an expression entering a focus. Overwritten by {@link MainModule}.
   */
  public void enterFocus() { }

  /**
   * Notifies the visitor of an expression leaving a focus. Overwritten by {@link MainModule}.
   */
  public void exitFocus() { }
}
