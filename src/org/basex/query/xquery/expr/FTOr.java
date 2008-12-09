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
 * FTOr expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTOr extends FTExpr {
  /** Saving index of positive expressions. */
  private int[] pex;
  /** Saving index of negative (ftnot) expressions. */
  private int[] nex;
  
  /**
   * Constructor.
   * @param e expression list
   */
  public FTOr(final FTExpr... e) {
    super(e);
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    double d = 0;
    for(final Expr e : expr) {
      final Item it = ctx.iter(e).next();
      if(it.bool()) d = Scoring.or(d, it.dbl());
    }
    return Dbl.iter(d);
  }

  @Override
  public String toString() {
    return toString(" ftor ");
  }
  
  @Override
  public void indexAccessible(final XQContext ctx, final FTIndexAcsbl ia)
      throws XQException {
    final IntList p = new IntList();
    final IntList n = new IntList();
    int min = ia.indexSize;
    int sum = 0;

    for (int i = 0; i < expr.length; i++) {
      expr[i].indexAccessible(ctx, ia);
      if (!ia.io) return;
      if (!ia.ftnot && ia.indexSize > 0) {
        p.add(i);
        sum += ia.indexSize;
      } else if (ia.ftnot) {
        if (ia.indexSize > 0) n.add(i);
        else {
          ia.iu = false;
          ia.seq = true;
          ia.indexSize = Integer.MAX_VALUE;
          return;
        }
      }
    }
    nex = n.finish();
    pex = p.finish();
    
    // [SG] think about index use for ftor ftnot
    if (pex.length == 0 || nex.length > 0 && pex.length > 0) {
      ia.iu = false;
      ia.indexSize = Integer.MAX_VALUE;
    } else {
      ia.indexSize = sum > min ? min : sum;
    } 
  }
  
  @Override
  public Expr indexEquivalent(final XQContext ctx, final FTIndexEq ieq) {
    for (int i = 0; i < expr.length; i++) {
      expr[i] = (FTExpr) expr[i].indexEquivalent(ctx, ieq);
    }
    
    if (pex.length == 0) return new FTUnion(nex, expr);
    return new FTUnion(pex, expr);

  }
}
