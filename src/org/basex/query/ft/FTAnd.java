package org.basex.query.ft;

import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTNodeItem;
import org.basex.query.iter.FTNodeIter;
import org.basex.util.IntList;

/**
 * FTAnd expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class FTAnd extends FTExpr {
  /** Saving index of positive expressions. */
  private int[] pex;
  /** Saving index of negative expressions (FTNot). */
  private int[] nex;
  
  /**
   * Constructor.
   * @param e expression list
   */
  public FTAnd(final FTExpr... e) {
    super(e);
  }

  @Override
  public FTNodeIter iter(final QueryContext ctx) throws QueryException {
    double d = 0;
    for(final FTExpr e : expr) {
      final FTNodeItem it = e.iter(ctx).next();
      final double s = it.score();
      if(s == 0) return score(0);
      d = ctx.score.and(d, s);
    }
    return score(d);
  }
  
  @Override
  public boolean indexAccessible(final QueryContext ctx,
      final IndexContext ic) throws QueryException {

    final IntList i1 = new IntList();
    final IntList i2 = new IntList();
    int nmin = ic.is;
    for (int i = 0; i < expr.length; i++) {
      if(!expr[i].indexAccessible(ctx, ic)) return false;
      
      if(!(expr[i] instanceof FTNot)) {
        i1.add(i);
        nmin = ic.is < nmin ? ic.is : nmin;
      } else if (ic.is > 0) {
        i2.add(i);
      }
    }
    pex = i1.finish();
    nex = i2.finish();
    ic.seq |= i1.size == 0;
    ic.is = i1.size > 0 ? nmin : Integer.MAX_VALUE;
    return true;
  }
  
  @Override
  public FTExpr indexEquivalent(final QueryContext ctx, final IndexContext ic)
      throws QueryException {

    if (pex.length == 1 && nex.length == 0)
      expr[pex[0]].indexEquivalent(ctx, ic);

    for (int i = 0; i < expr.length; i++) {
      expr[i] = expr[i].indexEquivalent(ctx, ic);
    }
    
    if (pex.length == 0) {
      // !A FTAnd !B = !(a ftor b)
      for (int i = 0; i < nex.length; i++) {
        expr[nex[i]] = expr[nex[i]].expr[0];
      }
      final FTUnion fta = new FTUnion(nex, false, expr);
      final FTNotIndex ftn = new FTNotIndex(fta);
      return ftn; 
    }
    return new FTIntersection(pex, nex, expr);
  }

  @Override
  public String toString() {
    return toString(" ftand ");
  }
}
