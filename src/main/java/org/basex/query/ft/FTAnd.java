package org.basex.query.ft;

import static org.basex.query.QueryText.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.util.*;
import org.basex.util.ft.*;

/**
 * FTAnd expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
public final class FTAnd extends FTExpr {
  /** Flags for negative operators. */
  boolean[] neg;

  /**
   * Constructor.
   * @param ii input info
   * @param e expression list
   */
  public FTAnd(final InputInfo ii, final FTExpr[] e) {
    super(ii, e);
  }

  @Override
  public FTExpr compile(final QueryContext ctx) throws QueryException {
    super.compile(ctx);
    boolean not = true;
    for(final FTExpr e : expr) not &= e instanceof FTNot;
    if(not) {
      // convert (!A and !B and ...) to !(A or B or ...)
      for(int e = 0; e < expr.length; ++e) expr[e] = expr[e].expr[0];
      return new FTNot(info, new FTOr(info, expr));
    }
    return this;
  }

  @Override
  public FTNode item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final FTNode item = expr[0].item(ctx, info);
    for(int e = 1; e < expr.length; ++e) {
      and(item, expr[e].item(ctx, info));
    }
    return item;
  }

  @Override
  public FTIter iter(final QueryContext ctx) throws QueryException {
    // initialize iterators
    final FTIter[] ir = new FTIter[expr.length];
    final FTNode[] it = new FTNode[expr.length];
    for(int e = 0; e < expr.length; ++e) {
      ir[e] = expr[e].iter(ctx);
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
  static void and(final FTNode i1, final FTNode i2) {
    final FTMatches all = new FTMatches(
        (byte) Math.max(i1.all.sTokenNum, i2.all.sTokenNum));

    for(final FTMatch s1 : i1.all) {
      for(final FTMatch s2 : i2.all) {
        all.add(new FTMatch().add(s1).add(s2));
      }
    }
    i1.score(Scoring.and(i1.score(), i2.score()));
    i1.all = all;
  }

  @Override
  public boolean indexAccessible(final IndexContext ic) throws QueryException {
    neg = new boolean[expr.length];

    int is = 0;
    int n = 0;
    for(int i = 0; i < expr.length; ++i) {
      if(!expr[i].indexAccessible(ic)) return false;
      neg[i] = ic.not;
      if(ic.not) ++n;
      ic.not = false;
      if(is == 0 || ic.costs() < is) is = ic.costs();
      if(ic.costs() == 0) break;
    }
    ic.costs(is);

    // no index access if first or all operators are negative
    return !neg[0] && n < expr.length;
  }

  @Override
  public String toString() {
    return PAR1 + toString(' ' + FTAND + ' ') + PAR2;
  }
}
