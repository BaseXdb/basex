package org.basex.query.ft;

import static org.basex.query.QueryTokens.*;
import org.basex.data.FTMatch;
import org.basex.data.FTMatches;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTItem;
import org.basex.query.iter.FTIter;

/**
 * FTOr expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
public final class FTOr extends FTExpr {
  /**
   * Constructor.
   * @param e expression list
   */
  public FTOr(final FTExpr[] e) {
    super(e);
  }

  @Override
  public FTExpr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    boolean not = true;
    for(FTExpr e : expr) not &= e instanceof FTNot;
    if(not) {
      // convert (!A or !B or ...) to !(A and B and ...)
      for(int e = 0; e < expr.length; e++) expr[e] = expr[e].expr[0];
      return new FTNot(new FTAnd(expr));
    }
    return this;
  }

  @Override
  public FTItem atomic(final QueryContext ctx) throws QueryException {
    final FTItem item = expr[0].atomic(ctx);
    for(int e = 1; e < expr.length; e++) {
      final FTItem it = expr[e].atomic(ctx);
      item.all = or(item.all, it.all);
      item.score(ctx.score.or(item.score(), it.score()));
    }
    return item;
  }

  @Override
  public FTIter iter(final QueryContext ctx) throws QueryException {
    // initialize iterators
    final FTIter[] ir = new FTIter[expr.length];
    final FTItem[] it = new FTItem[expr.length];
    for(int e = 0; e < expr.length; e++) {
      ir[e] = expr[e].iter(ctx);
      it[e] = ir[e].next();
    }

    return new FTIter() {
      @Override
      public FTItem next() throws QueryException {
        // find item with smallest pre value
        int p = -1;
        for(int i = 0; i < it.length; i++) {
          if(it[i] != null && (p == -1 || it[p].pre > it[i].pre)) p = i;
        }
        // no items left - leave
        if(p == -1) return null;

        // merge all matches
        final FTItem item = it[p];
        for(int i = 0; i < it.length; i++) {
          if(it[i] != null && p != i && item.pre == it[i].pre) {
            item.all = or(item.all, it[i].all);
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
   * @param m1 first match list
   * @param m2 second match list
   * @return resulting match
   */
  static FTMatches or(final FTMatches m1, final FTMatches m2) {
    final FTMatches all = new FTMatches(
      m1.sTokenNum > m2.sTokenNum ? m1.sTokenNum : m2.sTokenNum);
    for(final FTMatch m : m1) all.add(m);
    for(final FTMatch m : m2) all.add(m);
    return all;
  }

  @Override
  public boolean indexAccessible(final IndexContext ic) throws QueryException {
    int sum = 0;
    for(int i = 0; i < expr.length; i++) {
      // no index access if negative operators is found
      if(!expr[i].indexAccessible(ic) || ic.not) return false;
      sum += ic.is;
      ic.not = false;
    }
    ic.is = sum;
    return true;
  }

  @Override
  public String toString() {
    return "(" + toString(" " + FTOR + " ") + ")";
  }
}
