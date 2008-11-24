package org.basex.query.xpath.expr;

import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.item.Bln;
import org.basex.query.xpath.locpath.Step;

/**
 * Logical FTUnaryNot expression.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class FTNot extends FTArrayExpr {
  /**
   * Constructor.
   * @param e expressions
   */
  public FTNot(final FTArrayExpr e) {
    exprs = new FTArrayExpr[] { e };
  }

  @Override
  public Bln eval(final XPContext ctx) throws QueryException {
    return Bln.get(!exprs[0].eval(ctx).bool()); 
  }

  @Override
  public FTArrayExpr comp(final XPContext ctx) throws QueryException {
    for(int i = 0; i != exprs.length; i++) {
      if(exprs[i].fto == null) exprs[i].fto = fto;
      else if (fto != null) exprs[i].fto.merge(fto);
      exprs[i] = exprs[i].comp(ctx);
    }
    return this;
  }

  /*
  @Override
  public boolean indexOptions(final MetaData meta) {
    // [SG] temporary
    return false;
  }
  */
  
  @Override
  public FTArrayExpr indexEquivalent(final XPContext ctx, final Step curr, 
      final boolean seq) throws QueryException {
    // find index equivalents
    final FTArrayExpr ex = exprs[0].indexEquivalent(ctx, curr, seq);
    if(ex != null) exprs[0] = ex;
    return new FTNotIter(exprs);
  }
  
  @Override
  public int indexSizes(final XPContext ctx, final Step curr, final int min) {
    final int nrIds = exprs[0].indexSizes(ctx, curr, min);
    return nrIds == 0 ? -1 : Integer.MAX_VALUE;
  }
}
