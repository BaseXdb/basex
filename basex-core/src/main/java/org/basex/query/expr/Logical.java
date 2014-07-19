package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Logical expression, extended by {@link And} and {@link Or}.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class Logical extends Arr {
  /** Tail-call flag. */
  boolean tailCall;

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
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    super.compile(qc, scp);
    final boolean and = this instanceof And;
    final int es = exprs.length;
    final ExprList el = new ExprList(es);
    for(final Expr e : exprs) {
      final Expr ex = e.compEbv(qc);
      if(ex.isValue()) {
        // atomic items can be pre-evaluated
        qc.compInfo(OPTREMOVE, this, e);
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
    if(last.seqType().eq(SeqType.BLN)) {
      tailCall = true;
      last.markTailCalls(qc);
    }
  }

  @Override
  public void plan(final FElem plan) {
    final FElem el = planElem();
    if(tailCall) el.add(planAttr(TCL, true));
    plan.add(el);
    for(final ExprInfo e : exprs) {
      if(e != null) e.plan(el);
    }
  }

  /**
   * Flattens nested logical expressions.
   * @param qc query context
   */
  final void compFlatten(final QueryContext qc) {
    // flatten nested expressions
    final int es = exprs.length;
    final ExprList tmp = new ExprList(es);
    final boolean and = this instanceof And;
    final boolean or = this instanceof Or;
    for(final Expr ex : exprs) {
      if(and && ex instanceof And || or && ex instanceof Or) {
        for(final Expr e : ((Arr) ex).exprs) tmp.add(e);
        qc.compInfo(OPTFLAT, ex);
      } else {
        tmp.add(ex);
      }
    }
    if(es != tmp.size()) exprs = tmp.finish();
  }
}
