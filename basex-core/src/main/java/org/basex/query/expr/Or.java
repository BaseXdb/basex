package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.util.index.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Or expression.
 *
 * @author BaseX Team 2005-21, BSD License
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
    flatten(cc);
    return optimize(cc, true);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    for(final Expr expr : exprs) {
      if(expr.ebv(qc, info).bool(info)) return Bln.TRUE;
    }
    return Bln.FALSE;
  }

  @Override
  public Or copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new Or(info, copyAll(cc, vm, exprs)));
  }

  @Override
  public boolean indexAccessible(final IndexInfo ii) throws QueryException {
    IndexCosts costs = IndexCosts.ZERO;
    final ExprList list = new ExprList(exprs.length);
    for(final Expr expr : exprs) {
      // check if expression can be rewritten, and if access is not sequential
      if(!expr.indexAccessible(ii)) return false;
      // skip expressions without results
      if(ii.costs.results() == 0) continue;
      costs = IndexCosts.add(costs, ii.costs);
      list.add(ii.expr);
    }
    // use summarized costs for estimation
    ii.costs = costs;
    // no expressions means no costs: expression will later be pre-evaluated
    ii.expr = list.size() == 1 ? list.get(0) : new Union(info, list.finish());
    return true;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Or && super.equals(obj);
  }

  @Override
  public void plan(final QueryString qs) {
    qs.tokens(exprs, ' ' + OR + ' ', true);
  }
}
