package org.basex.query.ft;

import static org.basex.query.QueryTokens.*;
import org.basex.data.FTMatch;
import org.basex.data.FTMatches;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTItem;
import org.basex.query.iter.FTIter;
import org.basex.util.InputInfo;

/**
 * FTAnd expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
  public FTExpr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    boolean not = true;
    for(final FTExpr e : expr) not &= e instanceof FTNot;
    if(not) {
      // convert (!A and !B and ...) to !(A or B or ...)
      for(int e = 0; e < expr.length; ++e) expr[e] = expr[e].expr[0];
      return new FTNot(input, new FTOr(input, expr));
    }
    return this;
  }

  @Override
  public FTItem item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    final FTItem item = expr[0].item(ctx, input);
    for(int e = 1; e < expr.length; ++e) {
      and(ctx, item, expr[e].item(ctx, input));
    }
    return item;
  }

  @Override
  public FTIter iter(final QueryContext ctx) throws QueryException {
    // initialize iterators
    final FTIter[] ir = new FTIter[expr.length];
    final FTItem[] it = new FTItem[expr.length];
    for(int e = 0; e < expr.length; ++e) {
      ir[e] = expr[e].iter(ctx);
      it[e] = ir[e].next();
    }

    return new FTIter() {
      @Override
      public FTItem next() throws QueryException {
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
        final FTItem item = it[0];
        for(int i = 1; i < it.length; ++i) {
          // [CG] XQFT: item.all = FTMatches.not(it[i].all, 0);
          if(neg[i]) continue;
          and(ctx, item, it[i]);
          it[i] = ir[i].next();
        }
        it[0] = ir[0].next();
        return item;
      }
    };
  }

  /**
   * Merges two matches.
   * @param ctx query context
   * @param i1 first item
   * @param i2 second item
   */
  void and(final QueryContext ctx, final FTItem i1, final FTItem i2) {
    final FTMatches all = new FTMatches(
        (byte) Math.max(i1.all.sTokenNum, i2.all.sTokenNum));

    for(final FTMatch s1 : i1.all) {
      for(final FTMatch s2 : i2.all) {
        all.add(new FTMatch().add(s1).add(s2));
      }
    }
    i1.score(ctx.score.and(i1.score(), i2.score()));
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
      if(is == 0 || ic.costs < is) is = ic.costs;
      if(ic.costs == 0) break;
    }
    ic.costs = is;

    // no index access if first or all operators are negative
    return !neg[0] && n < expr.length;
  }

  @Override
  public String toString() {
    return PAR1 + toString(" " + FTAND + " ") + PAR2;
  }
}
