package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.func.fn.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Logical expression, extended by {@link And} and {@link Or}.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
abstract class Logical extends Arr {
  /**
   * Constructor.
   * @param info input info
   * @param exprs expressions
   */
  Logical(final InputInfo info, final Expr[] exprs) {
    super(info, exprs);
    seqType = SeqType.BLN;
  }

  @Override
  public final Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    for(int i = 0; i < exprs.length; i++) {
      try {
        exprs[i] = exprs[i].compile(qc, scp);
      } catch(final QueryException qe) {
        // first expression is evaluated eagerly
        if(i == 0) throw qe;
        exprs[i] = FnError.get(qe, exprs[i].seqType());
      }
    }
    return optimize(qc, scp);
  }

  @Override
  public Expr optimize(final QueryContext qc, final VarScope scp) throws QueryException {
    final boolean and = this instanceof And;
    final int es = exprs.length;
    final ExprList el = new ExprList(es);
    for(final Expr expr : exprs) {
      final Expr ex = expr.optimizeEbv(qc, scp);
      if(ex.isValue()) {
        // atomic items can be pre-evaluated
        qc.compInfo(OPTREMOVE_X_X, this, expr);
        if(ex.ebv(qc, info).bool(info) ^ and) return Bln.get(!and);
      } else {
        el.add(ex);
      }
    }
    if(el.isEmpty()) return Bln.get(and);
    exprs = el.finish();
    return this;
  }

  @Override
  public final void markTailCalls(final QueryContext qc) {
    // if the last expression surely returns a boolean, we can jump to it
    final Expr last = exprs[exprs.length - 1];
    if(last.seqType().eq(SeqType.BLN)) last.markTailCalls(qc);
  }

  @Override
  public void plan(final FElem plan) {
    final FElem el = planElem();
    plan.add(el);
    for(final ExprInfo expr : exprs) if(expr != null) expr.plan(el);
  }

  @Override
  public Expr inline(final QueryContext qc, final VarScope scp, final Var var, final Expr ex)
      throws QueryException {
    final Expr[] arr = exprs;
    boolean change = false;
    for(int i = 0; i < arr.length; i++) {
      try {
        final Expr e = arr[i].inline(qc, scp, var, ex);
        if(e != null) {
          arr[i] = e;
          change = true;
        }
      } catch(final QueryException qe) {
        // first expression is evaluated eagerly
        if(i == 0) throw qe;

        // everything behind the error is dead anyway
        final Expr[] nw = new Expr[i + 1];
        System.arraycopy(arr, 0, nw, 0, i);
        nw[i] = FnError.get(qe, seqType());
        exprs = nw;
        change = true;
        break;
      }
    }
    return change ? optimize(qc, scp) : null;
  }

  /**
   * Flattens nested logical expressions.
   * @param qc query context
   */
  final void compFlatten(final QueryContext qc) {
    // flatten nested expressions
    final ExprList tmp = new ExprList(exprs.length);
    final boolean and = this instanceof And;
    final boolean or = this instanceof Or;
    for(final Expr ex : exprs) {
      if(and && ex instanceof And || or && ex instanceof Or) {
        for(final Expr e : ((Arr) ex).exprs) tmp.add(e);
        qc.compInfo(OPTFLAT_X_X, description(), ex);
      } else {
        tmp.add(ex);
      }
    }
    exprs = tmp.finish();
  }
}
