package org.basex.query.xpath.expr;


import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.item.Bln;
import org.basex.query.xpath.locpath.Step;
import org.basex.util.IntList;

/**
 * Logical FTOr expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class FTOr extends FTArrayExpr {
  /** Saving index of expressions. */
  private int[] pex;


  /**
   * Constructor.
   * @param e expressions
   */
  public FTOr(final FTArrayExpr[] e) {
    exprs = e;
  }

  @Override
  public Bln eval(final XPContext ctx) throws QueryException {
    for(final Expr e : exprs) if(ctx.eval(e).bool()) return Bln.TRUE;
    return Bln.FALSE;
  }

  @Override
  public FTArrayExpr comp(final XPContext ctx) throws QueryException {
    for(int i = 0; i != exprs.length; i++) {
      if(exprs[i].fto == null) exprs[i].fto = fto;
      exprs[i] = exprs[i].comp(ctx);
    }
    return this;
   }

  @Override
  public FTArrayExpr indexEquivalent(final XPContext ctx, final Step curr,
      final boolean seq) throws QueryException {

    if (pex.length == 1 && seq)
      exprs[pex[0]].indexEquivalent(ctx, curr, seq);

    final FTArrayExpr[] indexExprs = new FTArrayExpr[exprs.length];
    for (int i = 0; i < exprs.length; i++) {
      indexExprs[i] = exprs[i].indexEquivalent(ctx, curr, seq);
    }
    return new FTUnion(indexExprs, pex);
  }

  @Override
  public int indexSizes(final XPContext ctx, final Step curr, final int min) {
    final IntList i1 = new IntList();
    boolean seq = false;
    int sum = 0;

    for (int i = 0; i < exprs.length; i++) {
      final int nrIDs = exprs[i].indexSizes(ctx, curr, min);
      if (!(exprs[i] instanceof FTUnaryNot) && nrIDs > 0) {
        i1.add(i);
        sum += nrIDs;
      } else if (nrIDs > 0) {
        i1.add(i);
        seq = true;
      }
    }

    pex = i1.finish();
    return seq ? Integer.MAX_VALUE : sum > min ? min : sum;
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

}
