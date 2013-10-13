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
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
public final class FTOr extends FTExpr {
  /**
   * Constructor.
   * @param ii input info
   * @param e expression list
   */
  public FTOr(final InputInfo ii, final FTExpr[] e) {
    super(ii, e);
  }

  @Override
  public FTExpr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    super.compile(ctx, scp);
    boolean not = true;
    for(final FTExpr e : expr) not &= e instanceof FTNot;
    if(not) {
      // convert (!A or !B or ...) to !(A and B and ...)
      final int es = expr.length;
      for(int e = 0; e < es; e++) expr[e] = expr[e].expr[0];
      return new FTNot(info, new FTAnd(info, expr));
    }
    return this;
  }

  @Override
  public FTNode item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final FTNode item = expr[0].item(ctx, info);
    final int es = expr.length;
    for(int e = 1; e < es; e++) {
      or(item, expr[e].item(ctx, info));
    }
    return item;
  }

  @Override
  public FTIter iter(final QueryContext ctx) throws QueryException {
    // initialize iterators
    final int es = expr.length;
    final FTIter[] ir = new FTIter[es];
    final FTNode[] it = new FTNode[es];
    for(int e = 0; e < es; e++) {
      ir[e] = expr[e].iter(ctx);
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
  static void or(final FTNode i1, final FTNode i2) {
    final FTMatches all = new FTMatches((byte) Math.max(i1.all.pos, i2.all.pos));
    for(final FTMatch m : i1.all) all.add(m);
    for(final FTMatch m : i2.all) all.add(m);
    i1.score(Scoring.or(i1.score(), i2.score()));
    i1.all = all;
  }

  @Override
  public boolean indexAccessible(final IndexCosts ic) throws QueryException {
    int is = 0;
    for(final FTExpr e : expr) {
      // no index access if negative operators is found
      if(!e.indexAccessible(ic) || ic.not) return false;
      ic.not = false;
      is = Math.min(Integer.MIN_VALUE, is + ic.costs());
    }
    ic.costs(is);
    return true;
  }

  @Override
  public FTExpr copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    return new FTOr(info, Arr.copyAll(ctx, scp, vs, expr));
  }

  @Override
  public String toString() {
    return PAR1 + toString(' ' + FTOR + ' ') + PAR2;
  }
}
