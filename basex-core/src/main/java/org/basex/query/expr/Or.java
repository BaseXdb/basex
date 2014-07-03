package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
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
    // merge predicates if possible
    CmpG cmpg = null;
    final ExprList el = new ExprList(exprs.length);
    for(final Expr e : exprs) {
      boolean merged = false;
      if(e instanceof CmpG) {
        // merge general comparisons
        final CmpG g = (CmpG) e;
        if(cmpg == null) cmpg = g;
        else if(cmpg.union(g, qc, scp)) merged = true;
      }
      // no optimization found; add original expression
      if(!(merged || e == Bln.FALSE)) {
        if(e == Bln.TRUE) return optPre(Bln.TRUE, qc);
        el.add(e);
      }
    }

    // all arguments were false()
    if(el.isEmpty()) return optPre(Bln.FALSE, qc);

    if(exprs.length != el.size()) {
      qc.compInfo(OPTWRITE, this);
      exprs = el.finish();
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
      final Expr[] inner = new Expr[exprs.length];
      for(int i = 0; i < inner.length; i++) inner[i] = ((Arr) exprs[i]).exprs[0];
      final Expr and = new And(info, inner).optimize(qc, scp);
      return Function.NOT.get(null, and).optimize(qc, scp);
    }

    // return single expression if it yields a boolean
    return exprs.length == 1 ? compBln(exprs[0], info) : this;
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    for(int i = 0; i < exprs.length - 1; i++)
      if(exprs[i].ebv(qc, info).bool(info)) return Bln.TRUE;
    final Expr last = exprs[exprs.length - 1];
    return tailCall ? last.item(qc, ii) : last.ebv(qc, ii).bool(ii) ? Bln.TRUE : Bln.FALSE;
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new Or(info, copyAll(qc, scp, vs, exprs));
  }

  @Override
  public boolean indexAccessible(final IndexInfo ii) throws QueryException {
    int costs = 0;
    final ExprList ex = new ExprList(exprs.length);
    for(final Expr expr : exprs) {
      // check if expression can be rewritten, and if access is not sequential
      if(!expr.indexAccessible(ii) || ii.seq) return false;
      // skip expressions without results
      if(ii.costs == 0) continue;
      costs += ii.costs;
      ex.add(ii.expr);
    }
    // use summarized costs for estimation
    ii.costs = costs;
    // no expressions means no costs: expression will later be ignored
    ii.expr = ex.size() == 1 ? ex.get(0) : new Union(info, ex.finish());
    return true;
  }

  @Override
  public String toString() {
    return toString(' ' + OR + ' ');
  }
}
