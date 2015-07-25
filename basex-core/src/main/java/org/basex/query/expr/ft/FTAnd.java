package org.basex.query.expr.ft;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.ft.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;

/**
 * FTAnd expression.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
public final class FTAnd extends FTExpr {
  /** Flags for negated query results. */
  private boolean[] negated;

  /**
   * Constructor.
   * @param info input info
   * @param exprs expressions
   */
  public FTAnd(final InputInfo info, final FTExpr[] exprs) {
    super(info, exprs);
  }

  @Override
  public FTExpr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    super.compile(qc, scp);
    boolean not = true;
    for(final FTExpr e : exprs) not &= e instanceof FTNot;
    if(not) {
      // convert (!A and !B and ...) to !(A or B or ...)
      final int es = exprs.length;
      for(int e = 0; e < es; ++e) exprs[e] = exprs[e].exprs[0];
      return new FTNot(info, new FTOr(info, exprs));
    }
    return this;
  }

  @Override
  public FTNode item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final FTNode item = exprs[0].item(qc, info);
    final int es = exprs.length;
    for(int e = 1; e < es; ++e) and(item, exprs[e].item(qc, info));
    return item;
  }

  @Override
  public FTIter iter(final QueryContext qc) throws QueryException {
    // initialize iterators
    final int es = exprs.length;
    final FTIter[] ir = new FTIter[es];
    final FTNode[] it = new FTNode[es];
    for(int e = 0; e < es; ++e) {
      ir[e] = exprs[e].iter(qc);
      it[e] = ir[e].next();
    }

    return new FTIter() {
      @Override
      public FTNode next() throws QueryException {
        // find item with lowest pre value
        final int il = it.length;
        for(int i = 0; i < il; ++i) {
          if(it[i] == null) {
            if(negated[i]) continue;
            return null;
          }

          final int d = it[0].pre() - it[i].pre();
          if(negated[i]) {
            if(d >= 0) {
              if(d == 0) it[0] = ir[0].next();
              it[i] = ir[i].next();
              i = -1;
            }
          } else {
            if(d != 0) {
              if(d < 0) i = 0;
              it[i] = ir[i].next();
              i = -1;
            }
          }
        }

        // merge all matches
        final FTNode item = it[0];
        for(int i = 1; i < il; ++i) {
          // [CG] XQFT: item.all = FTMatches.not(it[i].all, 0);
          if(negated[i]) continue;
          and(item, it[i]);
          it[i] = ir[i].next();
        }
        it[0] = ir[0].next();
        return item;
      }
    };
  }

  /**
   * Merges two matches.
   * @param i1 first item
   * @param i2 second item
   */
  private static void and(final FTNode i1, final FTNode i2) {
    final FTMatches all = new FTMatches((byte) Math.max(i1.matches().pos, i2.matches().pos));
    for(final FTMatch s1 : i1.matches()) {
      for(final FTMatch s2 : i2.matches()) {
        all.add(new FTMatch(s1.size() + s2.size()).add(s1).add(s2));
      }
    }
    i1.score(Scoring.avg(i1.score() + i2.score(), 2));
    i1.matches(all);
  }

  @Override
  public boolean indexAccessible(final IndexInfo ii) throws QueryException {
    final int es = exprs.length;
    final boolean[] ng = new boolean[es];
    int costs = 0;
    for(final FTExpr expr : exprs) {
      if(!expr.indexAccessible(ii)) return false;
      // use worst costs for estimation, as all index results may need to be scanned
      if(costs < ii.costs) costs = ii.costs;
    }

    ii.costs = costs;
    negated = ng;
    return true;
  }

  @Override
  public FTExpr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    final FTAnd copy = new FTAnd(info, Arr.copyAll(qc, scp, vs, exprs));
    if(negated != null) copy.negated = negated.clone();
    return copy;
  }

  @Override
  public String toString() {
    return PAREN1 + toString(' ' + FTAND + ' ') + PAREN2;
  }
}
