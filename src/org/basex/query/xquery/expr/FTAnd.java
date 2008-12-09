package org.basex.query.xquery.expr;

import org.basex.index.FTIndexAcsbl;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.Dbl;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.iter.Iter;
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
  public Iter iter(final XQContext ctx) throws XQException {
    double d = 0;
    for(final Expr e : expr) {
      final Item it = ctx.iter(e).next();
      if(!it.bool()) return Dbl.iter(0);
      d = Scoring.and(d, it.dbl());
    }
    return Dbl.iter(d);
  }

  @Override
  public String toString() {
    return toString(" ftand ");
  }
  
  @Override
  public void indexAccessible(final XQContext ctx, final FTIndexAcsbl ia)
      throws XQException {
    final IntList i1 = new IntList();
    final IntList i2 = new IntList();
    int nmin = ia.indexSize;
    for (int i = 0; i < expr.length; i++) {
      expr[i].indexAccessible(ctx, ia);
      if (!ia.io) return;
      
      if (!(expr[i] instanceof FTNot)) {
        i1.add(i);
        nmin = ia.indexSize < nmin ? ia.indexSize : nmin;
      } else if (ia.indexSize > 0) {
        i2.add(i);
      }
    }
    pex = i1.finish();
    nex = i2.finish();
    ia.seq = i1.size == 0;
    ia.indexSize = nmin;
  }
  
  @Override
  public Expr indexEquivalent(final XQContext ctx, final FTIndexEq ieq) {
    if (pex.length == 1 && nex.length == 0)
      expr[pex[0]].indexEquivalent(ctx, ieq);

    for (int i = 0; i < expr.length; i++) {
      expr[i] = (FTExpr) expr[i].indexEquivalent(ctx, ieq);
    }
    return new FTIntersection(pex, nex, expr);

  }
}
