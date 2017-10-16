package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;

/**
 * Or expression.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class Or extends Logical {
  /**
   * Constructor.
   * @param info input info
   * @param exprs expressions
   */
  public Or(final InputInfo info, final Expr... exprs) {
    super(info, exprs);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    return optimize(cc, false, (ex) -> new And(info, ex));
  }

  @Override
  void simplify(final CompileContext cc, final ExprList list) throws QueryException {
    final int es = exprs.length;
    for(int e = 0; e < es; e++) {
      Expr expr = exprs[e];
      if(expr instanceof CmpG) {
        // merge adjacent comparisons
        while(e + 1 < es && exprs[e + 1] instanceof CmpG) {
          final Expr tmp = ((CmpG) expr).union((CmpG) exprs[e + 1], cc);
          if(tmp != null) {
            expr = tmp;
            e++;
          } else {
            break;
          }
        }
      }
      if(exprs[e] != expr) cc.info(OPTSIMPLE_X, exprs[e]);
      if(!list.contains(expr) || expr.has(Flag.NDT) || expr.has(Flag.UPD)) list.add(expr);
    }
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // compute scoring
    if(qc.scoring) {
      double s = 0;
      boolean f = false;
      for(final Expr expr : exprs) {
        final Item it = expr.ebv(qc, info);
        f |= it.bool(info);
        s += it.score();
      }
      return Bln.get(f, Scoring.avg(s, exprs.length));
    }

    // standard evaluation
    for(final Expr expr : exprs) {
      if(expr.ebv(qc, info).bool(info)) return Bln.TRUE;
    }
    return Bln.FALSE;
  }

  @Override
  public Or copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new Or(info, copyAll(cc, vm, exprs));
  }

  @Override
  public boolean indexAccessible(final IndexInfo ii) throws QueryException {
    IndexCosts costs = IndexCosts.ZERO;
    final ExprList el = new ExprList(exprs.length);
    for(final Expr expr : exprs) {
      // check if expression can be rewritten, and if access is not sequential
      if(!expr.indexAccessible(ii)) return false;
      // skip expressions without results
      if(ii.costs.results() == 0) continue;
      costs = IndexCosts.add(costs, ii.costs);
      el.add(ii.expr);
    }
    // use summarized costs for estimation
    ii.costs = costs;
    // no expressions means no costs: expression will later be pre-evaluated
    ii.expr = el.size() == 1 ? el.get(0) : new Union(info, el.finish());
    return true;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Or && super.equals(obj);
  }

  @Override
  public String toString() {
    return toString(' ' + OR + ' ');
  }
}
