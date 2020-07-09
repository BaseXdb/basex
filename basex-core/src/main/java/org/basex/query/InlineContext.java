package org.basex.query;

import org.basex.query.expr.*;
import org.basex.query.expr.path.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.var.*;
import org.basex.util.hash.*;

/**
 * Context for inlining expressions.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class InlineContext {
  /** Compilation context. */
  public final CompileContext cc;
  /** Variable reference, or {@code null} for context reference. */
  public final Var var;
  /** Expression to inline. */
  public final Expr expr;

  /** Number of uses. */
  private VarUsage uses = VarUsage.MORE_THAN_ONCE;

  /**
   * Constructor.
   * @param var variable ({@link Var} reference) or context ({@code null}) to replace
   * @param expr expression to inline
   * @param cc compilation context
   */
  public InlineContext(final Var var, final Expr expr, final CompileContext cc) {
    this.cc = cc;
    this.var = var;
    this.expr = expr;
  }

  /**
   * Checks if the expression can be inlined.
   * @param target expression in which the expression will be inlined
   * @param count function for counting the variable usages (ignored if {@code null})
   * @return result of check
   * @throws QueryException query exception
   */
  public boolean inlineable(final Expr target, final QueryFunction<Var, VarUsage> count)
      throws QueryException {

    // count number of uses
    if(count != null) uses = count.apply(var);
    // no uses: no inlining required
    if(uses == VarUsage.NEVER) return true;

    // expensive expressions should be evaluated at most once
    if(uses == VarUsage.MORE_THAN_ONCE && !(
      expr instanceof Value ||
      expr instanceof VarRef ||
      expr instanceof ContextValue ||
      expr instanceof Path && expr.size() == 1 && !expr.has(Flag.CNS))) {
      return false;
    };

    // check if inlining is possible
    return target.inlineable(this);
  }

  /**
   * Inlines an expression into the specified target expression.
   * @param target expression in which the expression will be inlined
   * @return optimized or original expression
   * @throws QueryException query exception
   */
  public Expr inline(final Expr target) throws QueryException {
    final Expr inlined = uses == VarUsage.NEVER ? target : target.inline(this);
    return inlined != null ? inlined : target;
  }

  /**
   * Inlines an expression into the specified expressions.
   * @param exprs expressions to update
   * @return {@code true} if the array has changed, {@code false} otherwise
   * @throws QueryException query exception
   */
  public boolean inline(final Expr[] exprs) throws QueryException {
    boolean changed = false;
    final int el = exprs.length;
    for(int e = 0; e < el; e++) {
      final Expr inlined = exprs[e].inline(this);
      if(inlined != null) {
        exprs[e] = inlined;
        changed = true;
      }
    }
    return changed;
  }

  /**
   * Creates a copy from the expression to be inlined.
   * @return copy
   * @throws QueryException query exception
   */
  public Expr copy() throws QueryException {
    return expr instanceof Value ? expr : expr.copy(cc, new IntObjMap<>()).optimize(cc);
  }
}
