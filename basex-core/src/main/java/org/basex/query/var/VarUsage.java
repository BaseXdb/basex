package org.basex.query.var;

import org.basex.query.expr.*;

/**
 * A tri-state encoding the number of usages of a variable.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Leo Woerteler
 */
public enum VarUsage {
  /** No usages. */
  NEVER,
  /** Exactly one usage. */
  ONCE,
  /** More than one usage. */
  MORE_THAN_ONCE;

  /**
   * Combines the usages for two alternative branches.
   * @param other usage count to be combined
   * @return number of usages for the disjunction of both branches
   */
  public VarUsage max(final VarUsage other) {
    return compareTo(other) > 0 ? this : other;
  }

  /**
   * Combines the usages for two expressions that are both executed.
   * @param other usage count to be combined
   * @return number of usages of both expressions combined
   */
  public VarUsage plus(final VarUsage other) {
    return this == NEVER ? other : other == NEVER ? this : MORE_THAN_ONCE;
  }

  /**
   * Number of usages of the variable if the code is executed {@code count} times.
   * @param count number of executions, may be {@code -1} if not known
   * @return number of usages
   */
  public VarUsage times(final long count) {
    return count == 0 || this == NEVER ? NEVER : count == 1 ? this : MORE_THAN_ONCE;
  }

  /**
   * Checks how often the given variable is accessed in all of the given expressions.
   * @param var variable
   * @param exprs expressions
   * @return number of accesses to the variable in all expressions combined
   */
  public static VarUsage sum(final Var var, final Expr... exprs) {
    VarUsage all = NEVER;
    for(final Expr expr : exprs) {
      all = all.plus(expr.count(var));
      if(all == MORE_THAN_ONCE) break;
    }
    return all;
  }

  /**
   * Checks how often the given variable is used in any of the given expressions.
   * @param var variable
   * @param exprs expressions
   * @return maximum number of accesses in any given expression
   */
  public static VarUsage maximum(final Var var, final Expr... exprs) {
    VarUsage any = NEVER;
    for(final Expr expr : exprs) {
      any = any.max(expr.count(var));
      if(any == MORE_THAN_ONCE) break;
    }
    return any;
  }
}
