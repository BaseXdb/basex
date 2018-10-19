package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.function.Supplier;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Logical expression, extended by {@link And} and {@link Or}.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
abstract class Logical extends Arr {
  /**
   * Constructor.
   * @param info input info
   * @param exprs expressions
   */
  Logical(final InputInfo info, final Expr[] exprs) {
    super(info, SeqType.BLN_O, exprs);
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
        for(final Expr exp : ((Logical) ex).exprs) list.add(exp);
        cc.info(OPTFLAT_X_X, (Supplier<?>) this::description, ex);
      } else if(ex instanceof Value) {
        // pre-evaluate values
        cc.info(OPTREMOVE_X_X, expr, (Supplier<?>) this::description);
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
    if(last.seqType().eq(SeqType.BLN_O)) last.markTailCalls(cc);
  }

  @Override
  public Expr inline(final Var var, final Expr ex, final CompileContext cc) throws QueryException {
    boolean changed = false;
    final int el = exprs.length;
    for(int e = 0; e < el; e++) {
      try {
        final Expr exp = exprs[e].inline(var, ex, cc);
        if(exp != null) {
          exprs[e] = exp;
          changed = true;
        }
      } catch(final QueryException qe) {
        // first expression is evaluated eagerly
        if(e == 0) throw qe;

        // everything behind the error is dead anyway
        final Expr[] nw = new Expr[e + 1];
        Array.copy(exprs, e, nw);
        nw[e] = cc.error(qe, this);
        exprs = nw;
        changed = true;
        break;
      }
    }
    return changed ? optimize(cc) : null;
  }

  @Override
  public void plan(final FElem plan) {
    final FElem elem = planElem();
    plan.add(elem);
    for(final ExprInfo expr : exprs) expr.plan(elem);
  }
}
