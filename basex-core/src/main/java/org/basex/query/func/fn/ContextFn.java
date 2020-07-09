package org.basex.query.func.fn;

import org.basex.core.locks.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;

/**
 * Context-based function.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public abstract class ContextFn extends StandardFunc {
  /**
   * Argument that provides the context.
   * @return context argument.
   */
  int contextArg() {
    return 0;
  }

  /**
   * Indicates if the function accesses the current context.
   * @return result of check
   */
  final boolean contextAccess() {
    return exprs.length == contextArg();
  }

  /**
   * Returns the specified argument, or the context value if it does not exist.
   * @param i index of argument
   * @param qc query context
   * @return expression
   * @throws QueryException query exception
   */
  protected final Expr ctxArg(final int i, final QueryContext qc) throws QueryException {
    return exprs.length == i ? ctxValue(qc) : exprs[i];
  }

  @Override
  public final boolean has(final Flag... flags) {
    return Flag.CTX.in(flags) && contextAccess() || super.has(flags);
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    return (!contextAccess() || visitor.lock(Locking.CONTEXT, false)) && super.accept(visitor);
  }

  /**
   * Optimizes a context-based function call.
   * @param cc compilation context
   */
  protected void optContext(final CompileContext cc) {
    final boolean context = contextAccess();
    final Expr expr = context ? cc.qc.focus.value : exprs[contextArg()];
    if(expr != null) {
      final SeqType st = expr.seqType();
      if(st.oneOrMore() && !st.mayBeArray()) exprType.assign(Occ.ONE);
    }
  }

  /**
   * Indicates if the function will evaluate the current context.
   * @return result of check
   */
  public boolean inlineable() {
    return (contextAccess() || exprs[contextArg()] instanceof ContextValue) &&
        definition.seqType.occ == Occ.ZERO_ONE;
  }

  @Override
  public VarUsage count(final Var var) {
    // context reference check: check if function accesses context
    return (var == null && contextAccess() ? VarUsage.ONCE : VarUsage.NEVER).plus(super.count(var));
  }

  @Override
  public final Expr inline(final InlineContext ic) throws QueryException {
    return inline(ic, () -> {
      if(!contextAccess()) return null;
      final Expr[] args = new ExprList(exprs.length + 1).add(exprs).add(ic.copy()).finish();
      return definition.get(ic.cc.sc(), info, args);
    });
  }
}
