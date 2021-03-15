package org.basex.query;

import org.basex.query.expr.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.expr.path.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.var.*;
import org.basex.util.hash.*;

/**
 * Context for inlining expressions.
 *
 * @author BaseX Team 2005-21, BSD License
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
   * Checks if inlining into the specified expressions is possible.
   * See {@link Expr#inlineable} for further details.
   * @param targets target expressions
   * @return result of check
   */
  public boolean inlineable(final Expr... targets) {
    // skip early if no inlining is required
    final long[] minMax = { 1, 1 };
    uses = VarUsage.NEVER;
    for(final Expr target : targets) {
      uses = uses.plus(target.count(var).times(minMax[1]));
      if(target instanceof Clause) ((Clause) target).calcSize(minMax);
    }
    if(uses == VarUsage.NEVER) return true;

    // do not inline expensive expressions that are referenced more than once
    if(uses == VarUsage.MORE_THAN_ONCE && !(
      expr instanceof Value ||
      expr instanceof VarRef ||
      expr instanceof ContextValue ||
      expr instanceof Path && expr.size() == 1 && !expr.has(Flag.CNS)
    )) {
      return false;
    }

    // check if expression can be inlined into the specified target expressions
    for(final Expr target : targets) {
      if((var == null || target.uses(var)) && !target.inlineable(this)) return false;
    }
    return true;
  }

  /**
   * Inlines an expression into the specified target expression.
   * See {@link Expr#inline} for further details.
   * @param target expression in which the expression will be inlined
   * @return optimized or original expression
   * @throws QueryException query exception
   */
  public Expr inline(final Expr target) throws QueryException {
    final Expr inlined = inlineOrNull(target);
    return inlined != null ? inlined : target;
  }

  /**
   * Inlines an expression into the specified target expression,
   * or returns {@code null} if no inlining is required.
   * See {@link Expr#inline} for further details.
   * @param target expression in which the expression will be inlined
   * @return optimized expression or {@code null}
   * @throws QueryException query exception
   */
  public Expr inlineOrNull(final Expr target) throws QueryException {
    return uses == VarUsage.NEVER ? null : target.inline(this);
  }

  /**
   * Inlines an expression into the specified expressions.
   * @param exprs expressions to update
   * @return {@code true} if the array has changed, {@code false} otherwise
   * @throws QueryException query exception
   */
  public boolean inline(final Expr[] exprs) throws QueryException {
    return inline(exprs, false);
  }

  /**
   * Inlines an expression into the specified expressions.
   * @param exprs expressions to update
   * @param error catch errors
   * @return {@code true} if the array has changed, {@code false} otherwise
   * @throws QueryException query exception
   */
  public boolean inline(final Expr[] exprs, final boolean error) throws QueryException {
    boolean changed = false;
    final int el = exprs.length;
    for(int e = 0; e < el; e++) {
      Expr inlined;
      try {
        inlined = exprs[e].inline(this);
      } catch(final QueryException qe) {
        if(!error) throw qe;
        inlined = cc.error(qe, exprs[e]);
      }
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
