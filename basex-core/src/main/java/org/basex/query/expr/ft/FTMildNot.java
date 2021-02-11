package org.basex.query.expr.ft;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.ft.*;
import org.basex.query.util.index.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * FTMildnot expression.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FTMildNot extends FTExpr {
  /**
   * Constructor.
   * @param info input info
   * @param expr1 first expression
   * @param expr2 second expression
   */
  public FTMildNot(final InputInfo info, final FTExpr expr1, final FTExpr expr2) {
    super(info, expr1, expr2);
  }

  @Override
  public FTNode item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return mildnot(exprs[0].item(qc, info), exprs[1].item(qc, info));
  }

  @Override
  public FTIter iter(final QueryContext qc) throws QueryException {
    return new FTIter() {
      final FTIter iter1 = exprs[0].iter(qc), iter2 = exprs[1].iter(qc);
      FTNode item1 = iter1.next(), item2 = iter2.next();

      @Override
      public FTNode next() throws QueryException {
        while(item1 != null && item2 != null) {
          final int d = item1.pre() - item2.pre();
          if(d < 0) break;

          if(d > 0) {
            item2 = iter2.next();
          } else {
            if(!mildnot(item1, item2).matches().isEmpty()) break;
            item1 = iter1.next();
          }
        }
        final FTNode item = item1;
        item1 = iter1.next();
        return item;
      }
    };
  }

  /**
   * Processes a hit.
   * @param item1 first item
   * @param item2 second item
   * @return specified item
   */
  private static FTNode mildnot(final FTNode item1, final FTNode item2) {
    item1.matches(mildnot(item1.matches(), item2.matches()));
    return item1;
  }

  /**
   * Performs a mild not operation.
   * @param matches1 first match list
   * @param matches2 second match list
   * @return resulting match
   */
  private static FTMatches mildnot(final FTMatches matches1, final FTMatches matches2) {
    final FTMatches all = new FTMatches(matches1.pos);
    for(final FTMatch match1 : matches1) {
      boolean n = true;
      for(final FTMatch match2 : matches2) n &= match1.notin(match2);
      if(n) all.add(match1);
    }
    return all;
  }

  @Override
  public boolean indexAccessible(final IndexInfo ii) throws QueryException {
    IndexCosts costs = ii.costs;
    for(final FTExpr expr : exprs) {
      if(!expr.indexAccessible(ii)) return false;
      costs = IndexCosts.add(costs, ii.costs);
    }
    ii.costs = costs;
    return true;
  }

  @Override
  public FTExpr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new FTMildNot(info, exprs[0].copy(cc, vm), exprs[1].copy(cc, vm)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof FTMildNot && super.equals(obj);
  }

  @Override
  public void plan(final QueryString qs) {
    qs.tokens(exprs, ' ' + NOT + ' ' + IN + ' ', true);
  }
}
