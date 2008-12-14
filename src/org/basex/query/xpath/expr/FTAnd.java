package org.basex.query.xpath.expr;

import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.item.Bln;
import org.basex.query.xpath.path.Step;
import org.basex.util.IntList;

/**
 * Logical FTAnd expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class FTAnd extends FTArrayExpr {
  /** Saving index of positive expressions. */
  private int[] pex;
  /** Saving index of negative expressions (FTNot). */
  private int[] nex;

  /**
   * Constructor.
   * @param e expressions
   */
  public FTAnd(final FTArrayExpr[] e) {
    exprs = e;
  }

  @Override
  public Bln eval(final XPContext ctx) throws QueryException {
    for(final Expr e : exprs) if(!ctx.eval(e).bool()) return Bln.get(false);
    return Bln.get(true);
  }

  @Override
  public FTArrayExpr comp(final XPContext ctx) throws QueryException {
    for(int i = 0; i != exprs.length; i++) {
      exprs[i] = exprs[i].comp(ctx);
    }
    return this;
  }

  @Override
  public FTArrayExpr indexEquivalent(final XPContext ctx, final Step curr,
      final boolean seq) throws QueryException {
    
    if (pex.length == 1 && nex.length == 0)
      exprs[pex[0]].indexEquivalent(ctx, curr, seq);

    for (int i = 0; i < exprs.length; i++) {
      exprs[i] = exprs[i].indexEquivalent(ctx, curr, seq);
    }
    return new FTIntersection(exprs, pex, nex);
  }
  
  /**
   * Add Expr to list.
   * @param ex new Expr
   */
  public void add(final FTArrayExpr ex) {
    FTArrayExpr[] ne = new FTArrayExpr[exprs.length + 1];
    System.arraycopy(exprs, 0, ne, 0, exprs.length);
    ne[exprs.length] = ex;
    exprs = ne;
  }

  @Override
  public int indexSizes(final XPContext ctx, final Step curr, final int min) {
    final IntList i1 = new IntList();
    final IntList i2 = new IntList();
    int nmin = min;
    for (int i = 0; i < exprs.length; i++) {
      final int nrIDs = exprs[i].indexSizes(ctx, curr, min);
      if (!(exprs[i] instanceof FTNot)) {
        i1.add(i);
        nmin = nrIDs < nmin ? nrIDs : nmin;
      } else if (nrIDs > 0) {
        i2.add(i);
      }
    }
    pex = i1.finish();
    nex = i2.finish();
    if (i1.size == 0) {
      ctx.iu = false;
      return Integer.MAX_VALUE;
    }
    
    return nmin;
  }
}
