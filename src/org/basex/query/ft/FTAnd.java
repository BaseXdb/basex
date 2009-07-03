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
 * FTAnd expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
public final class FTAnd extends FTExpr {
  /** Flags for negative operators. */
  boolean[] neg;

  /**
   * Constructor.
   * @param e expression list
   */
  public FTAnd(final FTExpr[] e) {
    super(e);
  }

  @Override
  public FTExpr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    boolean not = true;
    for(FTExpr e : expr) not &= e instanceof FTNot;
    if(not) {
      // convert (!A and !B and ...) to !(A or B or ...)
      for(int e = 0; e < expr.length; e++) expr[e] = expr[e].expr[0];
      return new FTNot(new FTOr(expr));
    }
    return this;
  }

  @Override
  public FTItem atomic(final QueryContext ctx) throws QueryException {
    final FTItem item = expr[0].atomic(ctx);
    for(int e = 1; e < expr.length; e++) {
      final FTItem it = expr[e].atomic(ctx);
      item.all = and(item.all, it.all);
      item.score(ctx.score.and(it.score(), item.score()));
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
        // find item with lowest pre value
        for(int i = 0; i < it.length; i++) {
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
        for(int i = 1; i < it.length; i++) {
          // [CG] FT: item.all = FTMatches.not(it[i].all, 0);
          if(neg[i]) continue;
          item.all = and(item.all, it[i].all);
          it[i] = ir[i].next();
        }
        it[0] = ir[0].next();
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
  static FTMatches and(final FTMatches m1, final FTMatches m2) {
    final FTMatches all = new FTMatches(
        m1.sTokenNum > m2.sTokenNum ? m1.sTokenNum : m2.sTokenNum);

    for(final FTMatch s1 : m1) {
      for(final FTMatch s2 : m2) {
        all.add(new FTMatch().add(s1).add(s2));
      }
    }
    return all;
  }


  @Override
  public boolean indexAccessible(final IndexContext ic) throws QueryException {
    neg = new boolean[expr.length];

    int sum = 0;
    int n = 0;
    for(int i = 0; i < expr.length; i++) {
      if(!expr[i].indexAccessible(ic)) return false;
      neg[i] = ic.not;
      if(ic.not) n++;
      ic.not = false;
      sum += ic.is;
    }
    ic.is = sum;

    // no index access if first or all operators are negative
    return !neg[0] && n < expr.length;
  }

  @Override
  public String toString() {
    return "(" + toString(" " + FTAND + " ") + ")";
  }
}
