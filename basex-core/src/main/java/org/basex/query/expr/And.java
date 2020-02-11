package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.util.index.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;

/**
 * And expression.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class And extends Logical {
  /**
   * Constructor.
   * @param info input info
   * @param exprs expressions
   */
  public And(final InputInfo info, final Expr... exprs) {
    super(info, exprs);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    return optimize(cc, false);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // compute scoring
    if(qc.scoring) {
      double score = 0;
      for(final Expr expr : exprs) {
        final Item item = expr.ebv(qc, info);
        if(!item.bool(info)) return Bln.FALSE;
        score += item.score();
      }
      return Bln.get(true, Scoring.avg(score, exprs.length));
    }

    // standard evaluation
    for(final Expr expr : exprs) {
      if(!expr.ebv(qc, info).bool(info)) return Bln.FALSE;
    }
    return Bln.TRUE;
  }

  @Override
  public And copy(final CompileContext cc, final IntObjMap<Var> vars) {
    return new And(info, copyAll(cc, vars, exprs));
  }

  @Override
  public boolean indexAccessible(final IndexInfo ii) throws QueryException {
    IndexCosts costs = IndexCosts.ZERO;
    final ExprList list = new ExprList(exprs.length);
    for(final Expr expr : exprs) {
      // check if expression can be rewritten, and if access is not sequential
      if(!expr.indexAccessible(ii)) return false;
      // skip queries with no results
      if(ii.costs.results() == 0) return true;
      // summarize costs
      costs = IndexCosts.add(costs, ii.costs);
      list.add(ii.expr);
    }
    // use summarized costs for estimation
    ii.costs = costs;
    // create intersection of all index requests
    ii.expr = new Intersect(info, list.finish());
    return true;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof And && super.equals(obj);
  }

  @Override
  public String toString() {
    return toString(' ' + AND + ' ');
  }
}
