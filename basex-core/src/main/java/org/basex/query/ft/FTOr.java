package org.basex.query.ft;

import static org.basex.query.QueryText.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;

/**
 * FTOr expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
public final class FTOr extends FTExpr {
  /**
   * Constructor.
   * @param info input info
   * @param exprs expressions
   */
  public FTOr(final InputInfo info, final FTExpr[] exprs) {
    super(info, exprs);
  }

  @Override
  public FTExpr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    super.compile(qc, scp);
    boolean not = true;
    for(final FTExpr e : exprs) not &= e instanceof FTNot;
    if(not) {
      // convert (!A or !B or ...) to !(A and B and ...)
      final int es = exprs.length;
      for(int e = 0; e < es; e++) exprs[e] = exprs[e].exprs[0];
      return new FTNot(info, new FTAnd(info, exprs));
    }
    return this;
  }

  @Override
  public FTNode item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final FTNode item = exprs[0].item(qc, info);
    final int es = exprs.length;
    for(int e = 1; e < es; e++) {
      or(item, exprs[e].item(qc, info));
    }
    return item;
  }

  @Override
  public FTIter iter(final QueryContext qc) throws QueryException {
    // initialize iterators
    final int es = exprs.length;
    final FTIter[] ir = new FTIter[es];
    final FTNode[] it = new FTNode[es];
    for(int e = 0; e < es; e++) {
      ir[e] = exprs[e].iter(qc);
      it[e] = ir[e].next();
    }

    return new FTIter() {
      @Override
      public FTNode next() throws QueryException {
        // find item with smallest pre value
        int p = -1;
        for(int i = 0; i < es; ++i) {
          if(it[i] != null && (p == -1 || it[p].pre > it[i].pre)) p = i;
        }
        // no items left - leave
        if(p == -1) return null;

        // merge all matches
        final FTNode item = it[p];
        for(int i = 0; i < es; ++i) {
          if(it[i] != null && p != i && item.pre == it[i].pre) {
            or(item, it[i]);
            it[i] = ir[i].next();
          }
        }
        it[p] = ir[p].next();
        return item;
      }
    };
  }

  /**
   * Merges two matches.
   * @param i1 first item
   * @param i2 second item
   */
  private static void or(final FTNode i1, final FTNode i2) {
    final FTMatches all = new FTMatches((byte) Math.max(i1.all.pos, i2.all.pos));
    for(final FTMatch m : i1.all) all.add(m);
    for(final FTMatch m : i2.all) all.add(m);
    i1.score(Scoring.merge(i1.score(), i2.score()));
    i1.all = all;
  }

  @Override
  public boolean indexAccessible(final IndexInfo ii) throws QueryException {
    int costs = 0;
    for(final FTExpr e : exprs) {
      // no index access if negated queries is found
      if(!e.indexAccessible(ii)) return false;
      costs += ii.costs;
    }
    // use summarized costs for estimation
    ii.costs = costs;
    return true;
  }

  @Override
  public FTExpr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new FTOr(info, Arr.copyAll(qc, scp, vs, exprs));
  }

  @Override
  public String toString() {
    return PAREN1 + toString(' ' + FTOR + ' ') + PAREN2;
  }
}
