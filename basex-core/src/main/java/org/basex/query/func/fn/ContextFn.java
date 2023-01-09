package org.basex.query.func.fn;

import org.basex.core.locks.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;

/**
 * Context-based function.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public abstract class ContextFn extends StandardFunc {
  /**
   * Argument that provides the context.
   * @return context argument.
   */
  public int contextArg() {
    return 0;
  }

  /**
   * Indicates if the function accesses the current context.
   * @return result of check
   */
  public final boolean contextAccess() {
    return exprs.length == contextArg();
  }

  /**
   * Returns the specified argument, or the context value if it does not exist.
   * @param i index of optional argument
   * @param qc query context
   * @return expression
   * @throws QueryException query exception
   */
  protected final Expr ctxArg(final int i, final QueryContext qc) throws QueryException {
    return i < exprs.length ? exprs[i] : ctxValue(qc);
  }

  @Override
  public final boolean has(final Flag... flags) {
    return Flag.CTX.in(flags) && contextAccess() || super.has(flags);
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    return (!contextAccess() || visitor.lock(Locking.CONTEXT)) && super.accept(visitor);
  }

  /**
   * Indicates if the function will evaluate the current context.
   * @return result of check
   */
  public boolean inlineable() {
    return (contextAccess() || exprs[contextArg()] instanceof ContextValue) &&
        definition.seqType.occ == Occ.ZERO_OR_ONE;
  }

  @Override
  public final VarUsage count(final Var var) {
    // context reference check: check if function accesses context
    return (var == null && contextAccess() ? VarUsage.ONCE : VarUsage.NEVER).plus(super.count(var));
  }

  @Override
  public final Expr inline(final InlineContext ic) throws QueryException {
    // try to inline arguments
    Expr expr = ic.inline(exprs) ? this : null;
    // create new expression with inlined context value
    if(ic.var == null && !(ic.expr instanceof ContextValue) && contextAccess()) {
      // $v ! string()  ->  string($v)
      final Expr[] args = new ExprList(exprs.length + 1).add(exprs).add(ic.copy()).finish();
      expr = definition.get(ic.cc.sc(), info, args);
    }
    return expr != null ? expr.optimize(ic.cc) : null;
  }

  /**
   * Optimizes EBV checks.
   * @param cc compilation context
   * @param expr context expression (can be {@code null})
   * @return optimized or original expression
   * @throws QueryException query exception
   */
  public final Expr simplifyEbv(final Expr expr, final CompileContext cc) throws QueryException {
    final SeqType st = expr.seqType();
    return st.instanceOf(SeqType.ELEMENT_O) || st.instanceOf(SeqType.DOCUMENT_NODE_O) ?
      Path.get(cc, info, expr, Step.get(cc, expr, info, Axis.DESCENDANT, KindTest.TEXT)) : this;
  }
}
