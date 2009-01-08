package org.basex.query.xquery.expr;

import org.basex.query.xquery.IndexContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.FTNodeItem;
import org.basex.query.xquery.iter.FTNodeIter;
import org.basex.query.xquery.util.Scoring;
import org.basex.util.IntList;

/**
 * FTAnd expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
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
  public FTNodeIter iter(final XQContext ctx) throws XQException {
    double d = 0;
    for(final FTExpr e : expr) {
      final FTNodeItem it = e.iter(ctx).next();
      final double s = it.score();
      if(s == 0) return score(0);
      d = Scoring.and(d, s);
    }
    return score(d);
  }
  
  @Override
  public void indexAccessible(final XQContext ctx, final IndexContext ic)
      throws XQException {
    final IntList i1 = new IntList();
    final IntList i2 = new IntList();
    int nmin = ic.is;
    for (int i = 0; i < expr.length; i++) {
      expr[i].indexAccessible(ctx, ic);
      if (!ic.io) return;
      
      if (!(expr[i] instanceof FTNot)) {
        i1.add(i);
        nmin = ic.is < nmin ? ic.is : nmin;
      } else if (ic.is > 0) {
        i2.add(i);
      }
    }
    pex = i1.finish();
    nex = i2.finish();
    ic.seq = i1.size == 0 || ic.seq;
    ic.is = i1.size > 0 ? nmin : Integer.MAX_VALUE;
  }
  
  @Override
  public FTExpr indexEquivalent(final XQContext ctx, final IndexContext ic)
      throws XQException {

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
