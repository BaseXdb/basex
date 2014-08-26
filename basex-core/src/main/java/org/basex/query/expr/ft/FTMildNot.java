package org.basex.query.expr.ft;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * FTMildnot expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FTMildNot extends FTExpr {
  /**
   * Constructor.
   * @param info input info
   * @param expr1 first expression
   * @param expr2 second expression
   * @throws QueryException query exception
   */
  public FTMildNot(final InputInfo info, final FTExpr expr1, final FTExpr expr2)
      throws QueryException {
    super(info, expr1, expr2);
    if(usesExclude()) throw FTMILD.get(info);
  }

  @Override
  public FTNode item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return mildnot(exprs[0].item(qc, info), exprs[1].item(qc, info));
  }

  @Override
  public FTIter iter(final QueryContext qc) throws QueryException {
    return new FTIter() {
      final FTIter i1 = exprs[0].iter(qc);
      final FTIter i2 = exprs[1].iter(qc);
      FTNode it1 = i1.next();
      FTNode it2 = i2.next();

      @Override
      public FTNode next() throws QueryException {
        while(it1 != null && it2 != null) {
          final int d = it1.pre - it2.pre;
          if(d < 0) break;

          if(d > 0) {
            it2 = i2.next();
          } else {
            if(!mildnot(it1, it2).all.isEmpty()) break;
            it1 = i1.next();
          }
        }
        final FTNode it = it1;
        it1 = i1.next();
        return it;
      }
    };
  }

  /**
   * Processes a hit.
   * @param it1 first item
   * @param it2 second item
   * @return specified item
   */
  private static FTNode mildnot(final FTNode it1, final FTNode it2) {
    it1.all = mildnot(it1.all, it2.all);
    return it1;
  }

  /**
   * Performs a mild not operation.
   * @param m1 first match list
   * @param m2 second match list
   * @return resulting match
   */
  private static FTMatches mildnot(final FTMatches m1, final FTMatches m2) {
    final FTMatches all = new FTMatches(m1.pos);
    for(final FTMatch s1 : m1) {
      boolean n = true;
      for(final FTMatch s2 : m2) n &= s1.notin(s2);
      if(n) all.add(s1);
    }
    return all;
  }

  @Override
  public boolean indexAccessible(final IndexInfo ii) throws QueryException {
    int costs = ii.costs;
    for(final FTExpr e : exprs) {
      if(!e.indexAccessible(ii)) return false;
      costs += ii.costs;
    }
    // use summarized costs for estimation
    ii.costs = costs;
    return true;
  }

  @Override
  public FTExpr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    try {
      return new FTMildNot(info, exprs[0].copy(qc, scp, vs), exprs[1].copy(qc, scp, vs));
    } catch(final QueryException e) {
      // checks were already done
      throw Util.notExpected(e);
    }
  }

  @Override
  public String toString() {
    return toString(' ' + NOT + ' ' + IN + ' ');
  }
}
