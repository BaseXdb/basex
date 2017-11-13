package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.func.*;
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
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
abstract class Logical extends Arr {
  /**
   * Constructor.
   * @param info input info
   * @param exprs expressions
   */
  Logical(final InputInfo info, final Expr[] exprs) {
    super(info, SeqType.BLN, exprs);
  }

  @Override
  public final Expr compile(final CompileContext cc) throws QueryException {
    final int el = exprs.length;
    for(int e = 0; e < el; e++) {
      try {
        exprs[e] = exprs[e].compile(cc);
      } catch(final QueryException qe) {
        // first expression is evaluated eagerly
        if(e == 0) throw qe;
        exprs[e] = cc.error(qe, exprs[e]);
      }
    }
    return optimize(cc);
  }

  /**
   * Optimizes the expression.
   * @param cc compilation context
   * @param and and/or flag
   * @param negate negated constructor
   * @return resulting expression
   * @throws QueryException query exception
   */
  public final Expr optimize(final CompileContext cc, final boolean and,
      final java.util.function.Function<Expr[], Logical> negate) throws QueryException {

    ExprList list = new ExprList(exprs.length);
    for(final Expr expr : exprs) {
      final Expr ex = expr.optimizeEbv(cc);
      if(and ? ex instanceof And : ex instanceof Or) {
        // flatten nested expressions
        for(final Expr e : ((Logical) ex).exprs) list.add(e);
        cc.info(OPTFLAT_X_X, description(), ex);
      } else if(ex.isValue()) {
        // pre-evaluate values
        cc.info(OPTREMOVE_X_X, description(), expr);
        if(ex.ebv(cc.qc, info).bool(info) ^ and) return Bln.get(!and);
      } else {
        list.add(ex);
      }
    }
    // no operands left: return result
    if(list.isEmpty()) return Bln.get(and);
    exprs = list.finish();

    // perform operator-specific optimizations
    list = new ExprList(exprs.length);
    simplify(cc, list);
    if(list.size() == 1) return cc.replaceWith(this, FnBoolean.get(list.get(0), info, cc.sc()));
    exprs = list.finish();

    // negate expressions
    for(final Expr expr : exprs) {
      if(!expr.isFunction(Function.NOT)) return this;
    }
    list = new ExprList(exprs.length);
    for(final Expr expr : exprs) list.add(((FnNot) expr).exprs[0]);
    exprs = list.finish();
    final Expr expr = negate.apply(exprs).optimize(cc);
    return cc.replaceWith(this, cc.function(Function.NOT, info, expr));
  }

  /**
   * Simplifies the logical expression.
   * @param cc compilation context
   * @param list expression list
   * @throws QueryException query exception
   */
  abstract void simplify(CompileContext cc, ExprList list) throws QueryException;

  @Override
  public final void markTailCalls(final CompileContext cc) {
    // if the last expression surely returns a boolean, we can jump to it
    final Expr last = exprs[exprs.length - 1];
    if(last.seqType().eq(SeqType.BLN)) last.markTailCalls(cc);
  }

  @Override
  public Expr inline(final Var var, final Expr ex, final CompileContext cc) throws QueryException {
    final Expr[] arr = exprs;
    boolean change = false;
    final int al = arr.length;
    for(int a = 0; a < al; a++) {
      try {
        final Expr e = arr[a].inline(var, ex, cc);
        if(e != null) {
          arr[a] = e;
          change = true;
        }
      } catch(final QueryException qe) {
        // first expression is evaluated eagerly
        if(a == 0) throw qe;

        // everything behind the error is dead anyway
        final Expr[] nw = new Expr[a + 1];
        System.arraycopy(arr, 0, nw, 0, a);
        nw[a] = cc.error(qe, this);
        exprs = nw;
        change = true;
        break;
      }
    }
    return change ? optimize(cc) : null;
  }

  @Override
  public void plan(final FElem plan) {
    final FElem el = planElem();
    plan.add(el);
    for(final ExprInfo expr : exprs) expr.plan(el);
  }
}
