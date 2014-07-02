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
 * FTAnd expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
public final class FTAnd extends FTExpr {
  /** Flags for negative operators. */
  private boolean[] neg;

  /**
   * Constructor.
   * @param info input info
   * @param exprs expressions
   */
  public FTAnd(final InputInfo info, final FTExpr[] exprs) {
    super(info, exprs);
  }

  @Override
  public FTExpr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    super.compile(ctx, scp);
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
  public FTNode item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final FTNode item = exprs[0].item(ctx, info);
    final int es = exprs.length;
    for(int e = 1; e < es; ++e) and(item, exprs[e].item(ctx, info));
    return item;
  }

  @Override
  public FTIter iter(final QueryContext ctx) throws QueryException {
    // initialize iterators
    final int es = exprs.length;
    final FTIter[] ir = new FTIter[es];
    final FTNode[] it = new FTNode[es];
    for(int e = 0; e < es; ++e) {
      ir[e] = exprs[e].iter(ctx);
      it[e] = ir[e].next();
    }

    return new FTIter() {
      @Override
      public FTNode next() throws QueryException {
        // find item with lowest pre value
        for(int i = 0; i < it.length; ++i) {
          if(it[i] == null) {
            if(neg[i]) continue;
            return null;
          }

          final int d = it[0].pre - it[i].pre;
          if(neg[i]) {
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
        for(int i = 1; i < it.length; ++i) {
          // [CG] XQFT: item.all = FTMatches.not(it[i].all, 0);
          if(neg[i]) continue;
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
    final FTMatches all = new FTMatches((byte) Math.max(i1.all.pos, i2.all.pos));
    for(final FTMatch s1 : i1.all) {
      for(final FTMatch s2 : i2.all) {
        all.add(new FTMatch(s1.size() + s2.size()).add(s1).add(s2));
      }
    }
    i1.score(Scoring.merge(i1.score(), i2.score()));
    i1.all = all;
  }

  @Override
  public boolean indexAccessible(final IndexCosts ic) throws QueryException {
    final int es = exprs.length;
    neg = new boolean[es];

    int is = 0;
    int n = 0;
    for(int i = 0; i < es; ++i) {
      if(!exprs[i].indexAccessible(ic)) return false;
      neg[i] = ic.not;
      if(ic.not) ++n;
      ic.not = false;
      if(is == 0 || ic.costs() < is) is = ic.costs();
      if(ic.costs() == 0) break;
    }
    ic.costs(is);

    // no index access if first or all operators are negative
    return !neg[0] && n < es;
  }

  @Override
  public FTExpr copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    final FTAnd copy = new FTAnd(info, Arr.copyAll(ctx, scp, vs, exprs));
    if(neg != null) copy.neg = neg.clone();
    return copy;
  }

  @Override
  public String toString() {
    return PAR1 + toString(' ' + FTAND + ' ') + PAR2;
  }
}
