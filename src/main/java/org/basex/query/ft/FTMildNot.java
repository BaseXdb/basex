package org.basex.query.ft;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * FTMildnot expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FTMildNot extends FTExpr {
  /**
   * Constructor.
   * @param ii input info
   * @param e1 first expression
   * @param e2 second expression
   * @throws QueryException query exception
   */
  public FTMildNot(final InputInfo ii, final FTExpr e1, final FTExpr e2)
      throws QueryException {
    super(ii, e1, e2);
    if(usesExclude()) FTMILD.thrw(info);
  }

  @Override
  public FTNode item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    return mildnot(expr[0].item(ctx, info), expr[1].item(ctx, info));
  }

  @Override
  public FTIter iter(final QueryContext ctx) throws QueryException {
    return new FTIter() {
      final FTIter i1 = expr[0].iter(ctx);
      final FTIter i2 = expr[1].iter(ctx);
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
            if(mildnot(it1, it2).all.size != 0) break;
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
  static FTNode mildnot(final FTNode it1, final FTNode it2) {
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
    final FTMatches all = new FTMatches(m1.sTokenNum);
    for(final FTMatch s1 : m1) {
      boolean n = true;
      for(final FTMatch s2 : m2) n &= s1.notin(s2);
      if(n) all.add(s1);
    }
    return all;
  }

  @Override
  public boolean indexAccessible(final IndexContext ic) throws QueryException {
    int is = ic.costs();
    for(final FTExpr e : expr) {
      if(!e.indexAccessible(ic)) return false;
      is = Math.min(Integer.MIN_VALUE, is + ic.costs());
    }
    ic.costs(is);
    return true;
  }

  @Override
  public String toString() {
    return toString(' ' + NOT + ' ' + IN + ' ');
  }

  @Override
  public boolean visitVars(final VarVisitor visitor) {
    return visitor.visitAll(expr);
  }
}
