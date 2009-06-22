package org.basex.query.ft;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;

import org.basex.data.FTMatch;
import org.basex.data.FTMatches;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTItem;
import org.basex.query.iter.FTIter;
import org.basex.query.util.Err;

/**
 * FTMildnot expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class FTMildNot extends FTExpr {
  /**
   * Constructor.
   * @param e1 first expression
   * @param e2 second expression
   * @throws QueryException query exception
   */
  public FTMildNot(final FTExpr e1, final FTExpr e2) throws QueryException {
    super(e1, e2);
    if(usesExclude()) Err.or(FTMILD);
  }

  @Override
  public FTItem atomic(final QueryContext ctx) throws QueryException {
    return mildnot(expr[0].atomic(ctx), expr[1].atomic(ctx));
  }

  @Override
  public FTIter iter(final QueryContext ctx) throws QueryException {
    return new FTIter() {
      final FTIter i1 = expr[0].iter(ctx);
      final FTIter i2 = expr[1].iter(ctx);
      FTItem it1 = i1.next();
      FTItem it2 = i2.next();

      @Override
      public FTItem next() throws QueryException {
        while(it1 != null && it2 != null) {
          final int d = it1.pre - it2.pre;
          if(d < 0) break;

          if(d > 0) {
            it2 = i2.next();
          } else {
            if(mildnot(it1, it2).all.size != 0) break;
            it1 = i1.next();
          }
        }
        final FTItem it = it1;
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
  FTItem mildnot(final FTItem it1, final FTItem it2) {
    it1.all = mildnot(it1.all, it2.all);
    // [CG] FT: check invalid milt not tests
    //if(it1.all == null) Err.or(FTMILD);
    return it1;
  }

  /**
   * Performs a mild not operation.
   * @param m1 first match list
   * @param m2 second match list
   * @return resulting match, or null if string exclude was found
   */
  private static FTMatches mildnot(final FTMatches m1, final FTMatches m2) {
    final FTMatches all = new FTMatches(m1.sTokenNum);
    for(final FTMatch s1 : m1) {
      //if(!s1.match()) return null;
      boolean n = true;
      for(final FTMatch s2 : m2) {
        //if(!s2.match()) return null;
        n &= s1.notin(s2);
      }
      if(n) all.add(s1);
    }
    return all;
  }

  @Override
  public boolean indexAccessible(final IndexContext ic) throws QueryException {
    int sum = ic.is;
    for(final FTExpr e : expr) {
      if(!e.indexAccessible(ic)) return false;
      sum += ic.is;
    }
    return true;
  }

  @Override
  public String toString() {
    return toString(" " + NOT + " " + IN + " ");
  }
}
