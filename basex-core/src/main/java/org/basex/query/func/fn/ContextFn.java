package org.basex.query.func.fn;

import org.basex.core.locks.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;

/**
 * Context-based function.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public abstract class ContextFn extends StandardFunc {
  /**
   * Index of the context argument.
   * @return index
   */
  public int contextIndex() {
    return 0;
  }

  /**
   * Indicates if the function accesses the current context.
   * @return result of check
   */
  public final boolean contextAccess() {
    return !defined(contextIndex());
  }

  /**
   * Returns the context argument, or the context value if it does not exist.
   * @param qc query context
   * @return expression
   * @throws QueryException query exception
   */
  protected final Expr context(final QueryContext qc) throws QueryException {
    final Expr expr = arg(contextIndex());
    return expr != Empty.UNDEFINED ? expr : ctxValue(qc);
  }

  @Override
  public boolean hasCTX() {
    return contextAccess();
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
    return (contextAccess() || arg(contextIndex()) instanceof ContextValue) &&
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
    final Expr[] args = args();
    Expr expr = ic.inline(args) ? this : null;
    // create new expression with inlined context value
    if(ic.var == null && !(ic.expr instanceof ContextValue) && contextAccess()) {
      // $v ! string()  ->  string($v)
      final Expr[] newArgs = new ExprList(args.length + 1).add(args).add(ic.copy()).finish();
      expr = definition.get(info, newArgs);
    }
    return expr != null ? expr.optimize(ic.cc) : null;
  }

  /**
   * Optimizes EBV checks.
   * @param cc compilation context
   * @param expr context expression (can be {@code null})
   * @param pred function for creating a predicate (can be {@code null})
   * @return optimized or original expression
   * @throws QueryException query exception
   */
  public final Expr simplifyEbv(final Expr expr, final CompileContext cc,
      final QuerySupplier<Expr> pred) throws QueryException {
    final SeqType st = expr.seqType();
    if(st.instanceOf(SeqType.ELEMENT_O) || st.instanceOf(SeqType.DOCUMENT_NODE_O)) {
      final Expr[] preds = pred != null ? new Expr[] { pred.get() } : new Expr[0];
      final Expr step = Step.get(cc, expr, info, Axis.DESCENDANT, KindTest.TEXT, preds);
      return Path.get(cc, info, expr, step);
    }
    return this;
  }
}
