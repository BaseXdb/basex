package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.func.*;
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
 * @author BaseX Team 2005-14, BSD License
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
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    // remove atomic values
    final Expr c = super.compile(qc, scp);
    return c != this ? c : optimize(qc, scp);
  }

  @Override
  public Expr optimize(final QueryContext qc, final VarScope scp) throws QueryException {
    final int es = exprs.length;
    final ExprList list = new ExprList(es);
    for(int i = 0; i < es; i++) {
      Expr e = exprs[i];
      if(e instanceof CmpG) {
        // merge adjacent comparisons
        while(i + 1 < es && exprs[i + 1] instanceof CmpG) {
          final Expr tmp = ((CmpG) e).union((CmpG) exprs[i + 1], qc, scp);
          if(tmp != null) {
            e = tmp;
            i++;
          } else {
            break;
          }
        }
      }
      // expression will always return true
      if(e == Bln.TRUE) return optPre(Bln.TRUE, qc);
      // skip expression yielding false
      if(e != Bln.FALSE) list.add(e);
    }

    // all arguments return false
    if(list.isEmpty()) return optPre(Bln.FALSE, qc);

    if(es != list.size()) {
      qc.compInfo(OPTWRITE, this);
      exprs = list.finish();
    }
    compFlatten(qc);

    boolean not = true;
    for(final Expr e : exprs) {
      if(!e.isFunction(Function.NOT)) {
        not = false;
        break;
      }
    }

    if(not) {
      qc.compInfo(OPTWRITE, this);
      final int el = exprs.length;
      final Expr[] inner = new Expr[el];
      for(int e = 0; e < el; e++) inner[e] = ((Arr) exprs[e]).exprs[0];
      final Expr ex = new And(info, inner).optimize(qc, scp);
      return Function.NOT.get(null, info, ex).optimize(qc, scp);
    }

    // return single expression if it yields a boolean
    return exprs.length == 1 ? compBln(exprs[0], info) : this;
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // compute scoring
    if(qc.scoring) {
      double s = 0;
      boolean f = false;
      for(final Expr e : exprs) {
        final Item it = e.ebv(qc, info);
        f |= it.bool(ii);
        s += it.score();
      }
      return Bln.get(f, Scoring.avg(s, exprs.length));
    }

    // standard evaluation
    for(final Expr e : exprs) {
      if(e.ebv(qc, info).bool(ii)) return Bln.TRUE;
    }
    return Bln.FALSE;
  }

  @Override
  public Or copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new Or(info, copyAll(qc, scp, vs, exprs));
  }

  @Override
  public boolean indexAccessible(final IndexInfo ii) throws QueryException {
    int costs = 0;
    final ExprList el = new ExprList(exprs.length);
    for(final Expr expr : exprs) {
      // check if expression can be rewritten, and if access is not sequential
      if(!expr.indexAccessible(ii)) return false;
      // skip expressions without results
      if(ii.costs == 0) continue;
      costs += ii.costs;
      el.add(ii.expr);
    }
    // use summarized costs for estimation
    ii.costs = costs;
    // no expressions means no costs: expression will later be ignored
    ii.expr = el.size() == 1 ? el.get(0) : new Union(info, el.finish());
    return true;
  }

  @Override
  public String toString() {
    return toString(' ' + OR + ' ');
  }
}
