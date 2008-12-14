package org.basex.query.xquery.expr;

import org.basex.query.xquery.FTIndexAcsbl;
import org.basex.query.xquery.FTIndexEq;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Dbl;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.iter.Iter;
import org.basex.util.IntList;

/**
 * FTMildnot expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTMildNot extends FTExpr {
  /**
   * Constructor.
   * @param l expression list
   */
  public FTMildNot(final FTExpr... l) {
    super(l);
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    final Item it = ctx.iter(expr[0]).next();
    if(!it.bool()) return Dbl.iter(0);
    
    boolean f = false;
    for(int i = 1; i < expr.length; i++) f |= ctx.iter(expr[i]).next().bool();
    return !f || ctx.ftpos.mildNot() ? it.iter() : Dbl.iter(0);
  }

  @Override
  public String toString() {
    return toString(" not in ");
  }
  
  @Override
  public void indexAccessible(final XQContext ctx, final FTIndexAcsbl ia)
      throws XQException {
    final int mmin = ia.is;
    IntList il = new IntList(expr.length - 1);
    for (int i = 1; i < expr.length; i++) {
      expr[i].indexAccessible(ctx, ia);
      if (!ia.io) return;
      if (ia.is > 0) il.add(i);
    }
    
    if(il.size < expr.length - 1) {
      FTExpr[] e = new FTExpr[il.size + 1];
      e[0] = expr[0];
      int c = 1;
      for (int i = 0; i < il.size; i++) e[c++] = expr[il.list[i]];
      expr = e;
    }
    expr[0].indexAccessible(ctx, ia);
    ia.is = mmin < ia.is ? mmin : ia.is;
  }
  
  @Override
  public Expr indexEquivalent(final XQContext ctx, final FTIndexEq ieq)
    throws XQException {

    if (expr.length == 1) return expr[0].indexEquivalent(ctx, ieq);
    
    // assumption 1: ftcontains "a" not in "a b" not in "a c"
    // and ftcontains "a" not in "a b" ftand "a" not in "a c" are equivalent
    
    final FTExpr[] ie = new FTExpr[2];
    final FTMildNotIndex[] mne = new FTMildNotIndex[expr.length - 1];
    final int[] pex = new int[expr.length - 1];
    ie[0] = (FTExpr) expr[0].indexEquivalent(ctx, ieq);
    for (int i = 1; i < expr.length; i++) {
      ie[1] = (FTExpr) expr[i].indexEquivalent(ctx, ieq);
      mne[i - 1] = new FTMildNotIndex(ie);
      pex[i - 1] = i - 1;
    }
    if (mne.length == 1) {
      return mne[0];
    }
    return new FTIntersection(pex, new int[]{}, mne);
  }
}
